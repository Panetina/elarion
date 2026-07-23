[CmdletBinding()]
param(
    [ValidateRange(5, 120)]
    [int]$SettleSeconds = 20
)

$ErrorActionPreference = 'Stop'

$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$gradleWrapper = Join-Path $repositoryRoot 'gradlew.bat'
$serverRunRoot = Join-Path $repositoryRoot 'dev\run'
$serverModsRoot = (Resolve-Path (Join-Path $serverRunRoot 'mods')).Path
$serverLog = Join-Path $serverRunRoot 'logs\latest.log'
$runId = (Get-Date).ToUniversalTime().ToString('yyyyMMddTHHmmssZ')
$evidenceRoot = Join-Path $repositoryRoot "build\performance-validation\$runId-server-ab"
$disabledRoot = Join-Path $evidenceRoot 'disabled-mods'
$performanceJars = @(
    'lithium-fabric-0.15.3+mc1.21.1.jar',
    'ferritecore-7.0.3-fabric.jar',
    'modernfix-fabric-5.25.1+mc1.21.1.jar'
)

New-Item -ItemType Directory -Force -Path $disabledRoot | Out-Null
if (-not $disabledRoot.StartsWith($repositoryRoot, [StringComparison]::OrdinalIgnoreCase)) {
    throw "Benchmark staging directory escaped the repository: $disabledRoot"
}

foreach ($jar in $performanceJars) {
    $source = Join-Path $serverModsRoot $jar
    if (-not (Test-Path -LiteralPath $source -PathType Leaf)) {
        throw "Missing benchmark artifact: $source"
    }
}

function Start-ServerGradle {
    $startInfo = New-Object System.Diagnostics.ProcessStartInfo
    $startInfo.FileName = $env:ComSpec
    $startInfo.WorkingDirectory = $repositoryRoot
    $startInfo.UseShellExecute = $false
    $startInfo.CreateNoWindow = $true
    $startInfo.RedirectStandardInput = $true
    $startInfo.RedirectStandardOutput = $true
    $startInfo.RedirectStandardError = $true
    $startInfo.Arguments = "/d /s /c `"`"$gradleWrapper`" :dev:runServer --console=plain`""

    $process = New-Object System.Diagnostics.Process
    $process.StartInfo = $startInfo
    $previousInputEncoding = [Console]::InputEncoding
    try {
        [Console]::InputEncoding = [System.Text.Encoding]::ASCII
        if (-not $process.Start()) {
            throw 'Failed to start the benchmark server.'
        }
    } finally {
        [Console]::InputEncoding = $previousInputEncoding
    }

    return [pscustomobject]@{
        Process = $process
        StandardOutput = $process.StandardOutput.ReadToEndAsync()
        StandardError = $process.StandardError.ReadToEndAsync()
    }
}

function Send-ServerCommand {
    param(
        [Parameter(Mandatory = $true)]
        [System.Diagnostics.Process]$Process,
        [Parameter(Mandatory = $true)]
        [string]$Command
    )

    $bytes = [System.Text.Encoding]::ASCII.GetBytes("$Command`r`n")
    $Process.StandardInput.BaseStream.Write($bytes, 0, $bytes.Length)
    $Process.StandardInput.BaseStream.Flush()
}

function Set-PerformanceModsEnabled {
    param([Parameter(Mandatory = $true)][bool]$Enabled)

    foreach ($jar in $performanceJars) {
        $active = Join-Path $serverModsRoot $jar
        $disabled = Join-Path $disabledRoot $jar
        if ($Enabled) {
            if (Test-Path -LiteralPath $disabled) {
                if (Test-Path -LiteralPath $active) {
                    throw "Both active and disabled copies exist for $jar"
                }
                Move-Item -LiteralPath $disabled -Destination $active
            }
            if (-not (Test-Path -LiteralPath $active -PathType Leaf)) {
                throw "Failed to restore $jar"
            }
        } else {
            if (Test-Path -LiteralPath $active) {
                if (Test-Path -LiteralPath $disabled) {
                    throw "Both active and disabled copies exist for $jar"
                }
                Move-Item -LiteralPath $active -Destination $disabled
            }
            if (-not (Test-Path -LiteralPath $disabled -PathType Leaf)) {
                throw "Failed to stage $jar"
            }
        }
    }
}

function Wait-ForServerReady {
    param([Parameter(Mandatory = $true)][datetime]$StartedAt)

    $deadline = (Get-Date).AddMinutes(4)
    while ((Get-Date) -lt $deadline) {
        if (Test-Path -LiteralPath $serverLog) {
            $item = Get-Item -LiteralPath $serverLog
            if ($item.LastWriteTime -ge $StartedAt) {
                $match = Select-String -Path $serverLog -Pattern 'Done \(([0-9.]+)s\)!' | Select-Object -Last 1
                if ($null -ne $match) {
                    return [double]$match.Matches[0].Groups[1].Value
                }
            }
        }
        Start-Sleep -Seconds 1
    }
    throw 'Dedicated server did not reach Done within four minutes.'
}

function Get-ServerJavaProcess {
    $candidate = Get-CimInstance Win32_Process -ErrorAction SilentlyContinue |
        Where-Object {
            $_.Name -match '^java(w)?\.exe$' -and
            $_.CommandLine -match 'net\.fabricmc\.devlaunchinjector\.Main' -and
            $_.CommandLine -match 'fabric\.dli\.env=server'
        } |
        Select-Object -First 1
    if ($null -eq $candidate) {
        throw 'Could not locate the benchmark server JVM.'
    }
    return Get-Process -Id $candidate.ProcessId
}

$trials = New-Object System.Collections.Generic.List[object]
$trialOrder = @('optimized', 'baseline', 'baseline', 'optimized')

try {
    for ($index = 0; $index -lt $trialOrder.Count; $index++) {
        $scenario = $trialOrder[$index]
        Set-PerformanceModsEnabled -Enabled ($scenario -eq 'optimized')
        if (Test-Path -LiteralPath $serverLog) {
            Remove-Item -LiteralPath $serverLog -Force
        }

        $server = $null
        try {
            $startedAt = Get-Date
            $server = Start-ServerGradle
            $reportedStartupSeconds = Wait-ForServerReady -StartedAt $startedAt
            $readyAt = Get-Date
            Start-Sleep -Seconds $SettleSeconds

            $java = Get-ServerJavaProcess
            $logText = Get-Content -LiteralPath $serverLog -Raw
            $problemLines = @($logText -split "`r?`n" | Where-Object {
                $_ -match '/ERROR\]|overwrite conflict|ClosedByInterrupt|NoSuchFileException'
            })

            Send-ServerCommand -Process $server.Process -Command 'stop'
            if (-not $server.Process.WaitForExit(120000)) {
                throw 'Benchmark server did not stop within 120 seconds.'
            }

            $trialNumber = $index + 1
            $prefix = "trial-$trialNumber-$scenario"
            Copy-Item -LiteralPath $serverLog -Destination (Join-Path $evidenceRoot "$prefix-latest.log")
            $server.StandardOutput.GetAwaiter().GetResult() |
                Set-Content -LiteralPath (Join-Path $evidenceRoot "$prefix-stdout.log") -Encoding UTF8
            $server.StandardError.GetAwaiter().GetResult() |
                Set-Content -LiteralPath (Join-Path $evidenceRoot "$prefix-stderr.log") -Encoding UTF8

            $trials.Add([pscustomobject][ordered]@{
                trial = $trialNumber
                scenario = $scenario
                startupWallSeconds = [math]::Round(($readyAt - $startedAt).TotalSeconds, 3)
                minecraftReportedStartupSeconds = $reportedStartupSeconds
                settleSeconds = $SettleSeconds
                workingSetBytes = [int64]$java.WorkingSet64
                privateMemoryBytes = [int64]$java.PrivateMemorySize64
                cpuSeconds = [math]::Round($java.CPU, 3)
                serverProblemLineCount = $problemLines.Count
                exitCode = $server.Process.ExitCode
            })
        } finally {
            if ($null -ne $server -and -not $server.Process.HasExited) {
                try {
                    Send-ServerCommand -Process $server.Process -Command 'stop'
                } catch {
                    # The failed process may already have closed stdin.
                }
                if (-not $server.Process.WaitForExit(120000)) {
                    $server.Process.Kill()
                    [void]$server.Process.WaitForExit(10000)
                }
            }
        }
    }
} finally {
    Set-PerformanceModsEnabled -Enabled $true
}

$summaries = foreach ($scenario in 'optimized', 'baseline') {
    $scenarioTrials = @($trials | Where-Object scenario -eq $scenario)
    [ordered]@{
        scenario = $scenario
        trials = $scenarioTrials.Count
        meanStartupWallSeconds = [math]::Round(($scenarioTrials | Measure-Object startupWallSeconds -Average).Average, 3)
        meanMinecraftStartupSeconds = [math]::Round(($scenarioTrials | Measure-Object minecraftReportedStartupSeconds -Average).Average, 3)
        meanWorkingSetBytes = [int64](($scenarioTrials | Measure-Object workingSetBytes -Average).Average)
        meanPrivateMemoryBytes = [int64](($scenarioTrials | Measure-Object privateMemoryBytes -Average).Average)
    }
}

$report = [ordered]@{
    schemaVersion = 1
    runId = $runId
    order = $trialOrder
    disabledArtifacts = $performanceJars
    settleSeconds = $SettleSeconds
    trials = $trials
    summaries = $summaries
}
$reportPath = Join-Path $evidenceRoot 'report.json'
$report | ConvertTo-Json -Depth 6 | Set-Content -LiteralPath $reportPath -Encoding UTF8
Write-Output $reportPath
