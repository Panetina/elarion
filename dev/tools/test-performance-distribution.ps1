[CmdletBinding()]
param(
    [ValidateRange(1, 1440)]
    [int]$DurationMinutes = 120,

    [ValidateRange(5, 300)]
    [int]$SampleSeconds = 30,

    [switch]$SkipClientTravel
)

$ErrorActionPreference = 'Stop'

$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$gradleWrapper = Join-Path $repositoryRoot 'gradlew.bat'
$serverRunRoot = Join-Path $repositoryRoot 'dev\run'
$clientRunRoot = Join-Path $serverRunRoot 'client-two'
$serverMinecraftLog = Join-Path $serverRunRoot 'logs\latest.log'
$clientMinecraftLog = Join-Path $clientRunRoot 'logs\latest.log'
$startedAt = Get-Date
$runId = $startedAt.ToUniversalTime().ToString('yyyyMMddTHHmmssZ')
$evidenceRoot = Join-Path $repositoryRoot "build\performance-validation\$runId-soak"

New-Item -ItemType Directory -Force -Path $evidenceRoot | Out-Null

function Start-GradleProcess {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Task,

        [string]$ExtraArguments = ''
    )

    $processStartInfo = New-Object System.Diagnostics.ProcessStartInfo
    $processStartInfo.FileName = $env:ComSpec
    $processStartInfo.WorkingDirectory = $repositoryRoot
    $processStartInfo.UseShellExecute = $false
    $processStartInfo.CreateNoWindow = $true
    $processStartInfo.RedirectStandardInput = $true
    $processStartInfo.RedirectStandardOutput = $true
    $processStartInfo.RedirectStandardError = $true
    $processStartInfo.Arguments = "/d /s /c `"`"$gradleWrapper`" $Task --console=plain $ExtraArguments`""

    $process = New-Object System.Diagnostics.Process
    $process.StartInfo = $processStartInfo
    $previousInputEncoding = [Console]::InputEncoding
    try {
        [Console]::InputEncoding = [System.Text.Encoding]::ASCII
        if (-not $process.Start()) {
            throw "Failed to start Gradle task $Task"
        }
    } finally {
        [Console]::InputEncoding = $previousInputEncoding
    }

    return [pscustomobject]@{
        Task = $Task
        Process = $process
        StandardOutput = $process.StandardOutput.ReadToEndAsync()
        StandardError = $process.StandardError.ReadToEndAsync()
    }
}

function Send-ProcessInput {
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

function Reset-LaunchLog {
    param([Parameter(Mandatory = $true)][string]$Path)

    if (Test-Path -LiteralPath $Path) {
        Remove-Item -LiteralPath $Path -Force
    }
}

function Wait-ForLogPattern {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path,

        [Parameter(Mandatory = $true)]
        [string]$Pattern,

        [Parameter(Mandatory = $true)]
        [datetime]$NotBefore,

        [int]$TimeoutSeconds = 180
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        if (Test-Path -LiteralPath $Path) {
            $item = Get-Item -LiteralPath $Path
            if ($item.LastWriteTime -ge $NotBefore) {
                $text = Get-Content -LiteralPath $Path -Raw -ErrorAction SilentlyContinue
                if ($text -match $Pattern) {
                    return $true
                }
            }
        }
        Start-Sleep -Seconds 2
    }
    return $false
}

function Get-TreeBytes {
    param([string]$Path)

    if (-not (Test-Path -LiteralPath $Path)) {
        return [int64]0
    }
    $sum = (Get-ChildItem -LiteralPath $Path -File -Recurse -ErrorAction SilentlyContinue |
        Measure-Object -Property Length -Sum).Sum
    if ($null -eq $sum) {
        return [int64]0
    }
    return [int64]$sum
}

function Get-DistantHorizonsBytes {
    $directories = @(Get-ChildItem -LiteralPath $clientRunRoot -Directory -Recurse -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -match '^(Distant_Horizons|DistantHorizons)' })
    $total = [int64]0
    foreach ($directory in $directories) {
        $total += Get-TreeBytes -Path $directory.FullName
    }
    return $total
}

function Get-GameJavaProcesses {
    return @(Get-CimInstance Win32_Process -ErrorAction SilentlyContinue |
        Where-Object {
            $_.Name -match '^java(w)?\.exe$' -and
            $_.CommandLine -match 'net\.fabricmc\.devlaunchinjector\.Main'
        })
}

function Stop-ClientProcesses {
    $clientProcesses = @(Get-GameJavaProcesses | Where-Object {
        $_.CommandLine -match 'fabric\.dli\.env=client'
    })
    foreach ($clientProcess in $clientProcesses) {
        $process = Get-Process -Id $clientProcess.ProcessId -ErrorAction SilentlyContinue
        if ($null -ne $process -and $process.MainWindowHandle -ne 0) {
            [void]$process.CloseMainWindow()
        }
    }

    $deadline = (Get-Date).AddSeconds(30)
    while ((Get-Date) -lt $deadline) {
        if (@(Get-GameJavaProcesses | Where-Object {
            $_.CommandLine -match 'fabric\.dli\.env=client'
        }).Count -eq 0) {
            return
        }
        Start-Sleep -Seconds 2
    }

    foreach ($clientProcess in @(Get-GameJavaProcesses | Where-Object {
        $_.CommandLine -match 'fabric\.dli\.env=client'
    })) {
        Stop-Process -Id $clientProcess.ProcessId -Force -ErrorAction SilentlyContinue
    }
}

function Save-ProcessOutput {
    param(
        [Parameter(Mandatory = $true)]
        [pscustomobject]$GradleProcess,

        [Parameter(Mandatory = $true)]
        [string]$Prefix
    )

    if ($GradleProcess.Process.HasExited) {
        $GradleProcess.StandardOutput.GetAwaiter().GetResult() |
            Set-Content -LiteralPath (Join-Path $evidenceRoot "$Prefix-stdout.log") -Encoding UTF8
        $GradleProcess.StandardError.GetAwaiter().GetResult() |
            Set-Content -LiteralPath (Join-Path $evidenceRoot "$Prefix-stderr.log") -Encoding UTF8
    }
}

$server = $null
$client = $null
$metrics = New-Object System.Collections.Generic.List[object]
$travelCommands = @(
    'execute in elarion:lobby run tp ElarionPlayer1 0.5 65 0.5',
    'execute in elarion:worldheart run tp ElarionPlayer1 -86 82 -48',
    'execute in elarion:realm_world_1 run tp ElarionPlayer1 -367 75 138'
)
$travelIndex = 0
$travelCommandCount = 0
$nextTravelAt = (Get-Date).AddMinutes(2)
$clientJoined = $false
$completed = $false

try {
    Reset-LaunchLog -Path $serverMinecraftLog
    $serverLaunchTime = Get-Date
    $server = Start-GradleProcess -Task ':dev:runServer'
    if (-not (Wait-ForLogPattern -Path $serverMinecraftLog -Pattern 'Done \([0-9.]+s\)!' -NotBefore $serverLaunchTime)) {
        throw 'Dedicated server did not reach Done within 180 seconds.'
    }

    Reset-LaunchLog -Path $clientMinecraftLog
    $clientLaunchTime = Get-Date
    $clientArguments = '--args="--username ElarionPlayer1 --uuid 00000000-0000-4000-8000-000000000002 --accessToken 0 --quickPlayMultiplayer localhost:25565"'
    $client = Start-GradleProcess -Task ':dev:runClientTwo' -ExtraArguments $clientArguments
    $clientJoined = Wait-ForLogPattern -Path $serverMinecraftLog -Pattern 'ElarionPlayer1.*(joined the game|logged in)' -NotBefore $clientLaunchTime
    if (-not $clientJoined) {
        throw 'Client Two did not join the dedicated server within 180 seconds.'
    }

    $soakStartedAt = Get-Date
    $soakEndsAt = $soakStartedAt.AddMinutes($DurationMinutes)
    $bobbyStartBytes = Get-TreeBytes -Path (Join-Path $clientRunRoot '.bobby')
    $distantHorizonsStartBytes = Get-DistantHorizonsBytes

    while ((Get-Date) -lt $soakEndsAt) {
        Start-Sleep -Seconds $SampleSeconds

        if (-not $SkipClientTravel -and (Get-Date) -ge $nextTravelAt) {
            Send-ProcessInput -Process $server.Process -Command $travelCommands[$travelIndex]
            $travelIndex = ($travelIndex + 1) % $travelCommands.Count
            $travelCommandCount++
            $nextTravelAt = (Get-Date).AddMinutes(10)
        }

        $sample = [ordered]@{
            timestamp = (Get-Date).ToUniversalTime().ToString('o')
            elapsedSeconds = [math]::Round(((Get-Date) - $soakStartedAt).TotalSeconds, 1)
            bobbyBytes = Get-TreeBytes -Path (Join-Path $clientRunRoot '.bobby')
            distantHorizonsBytes = Get-DistantHorizonsBytes
            processes = @()
        }
        foreach ($gameProcess in Get-GameJavaProcesses) {
            $process = Get-Process -Id $gameProcess.ProcessId -ErrorAction SilentlyContinue
            if ($null -ne $process) {
                $side = 'unknown'
                if ($gameProcess.CommandLine -match 'fabric\.dli\.env=server') {
                    $side = 'server'
                } elseif ($gameProcess.CommandLine -match 'fabric\.dli\.env=client') {
                    $side = 'client'
                }
                $sample.processes += [ordered]@{
                    side = $side
                    pid = $process.Id
                    workingSetBytes = [int64]$process.WorkingSet64
                    privateMemoryBytes = [int64]$process.PrivateMemorySize64
                    cpuSeconds = [math]::Round($process.CPU, 3)
                }
            }
        }
        $metrics.Add([pscustomobject]$sample)

        if ($server.Process.HasExited) {
            throw "Dedicated server exited early with code $($server.Process.ExitCode)."
        }
        if ($client.Process.HasExited) {
            throw "Client Two exited early with code $($client.Process.ExitCode)."
        }
    }

    Stop-ClientProcesses
    if (-not $client.Process.WaitForExit(60000)) {
        $client.Process.Kill()
        [void]$client.Process.WaitForExit(10000)
    }

    Send-ProcessInput -Process $server.Process -Command 'stop'
    if (-not $server.Process.WaitForExit(120000)) {
        throw 'Dedicated server did not stop within 120 seconds.'
    }
    $completed = $server.Process.ExitCode -eq 0

    $serverLogText = Get-Content -LiteralPath $serverMinecraftLog -Raw
    $clientLogText = Get-Content -LiteralPath $clientMinecraftLog -Raw
    $serverErrorLines = @($serverLogText -split "`r?`n" | Where-Object {
        $_ -match '/ERROR\]|overwrite conflict|ClosedByInterrupt|NoSuchFileException'
    })
    $clientErrorLines = @($clientLogText -split "`r?`n" | Where-Object {
        $_ -match '/ERROR\]|GL_INVALID_OPERATION|NoSuchFileException'
    })

    $report = [ordered]@{
        schemaVersion = 1
        runId = $runId
        startedAt = $startedAt.ToUniversalTime().ToString('o')
        finishedAt = (Get-Date).ToUniversalTime().ToString('o')
        requestedDurationMinutes = $DurationMinutes
        completed = $completed
        clientJoined = $clientJoined
        travelCommandsSent = $travelCommandCount
        sampleCount = $metrics.Count
        serverExitCode = $server.Process.ExitCode
        clientExitCode = $client.Process.ExitCode
        serverProblemLineCount = $serverErrorLines.Count
        serverProblemLines = $serverErrorLines
        clientErrorLineCount = $clientErrorLines.Count
        clientErrorLines = $clientErrorLines
        bobbyStartBytes = $bobbyStartBytes
        bobbyEndBytes = Get-TreeBytes -Path (Join-Path $clientRunRoot '.bobby')
        distantHorizonsStartBytes = $distantHorizonsStartBytes
        distantHorizonsEndBytes = Get-DistantHorizonsBytes
        metrics = $metrics
    }
    $report | ConvertTo-Json -Depth 8 |
        Set-Content -LiteralPath (Join-Path $evidenceRoot 'report.json') -Encoding UTF8
    Copy-Item -LiteralPath $serverMinecraftLog -Destination (Join-Path $evidenceRoot 'server-latest.log')
    Copy-Item -LiteralPath $clientMinecraftLog -Destination (Join-Path $evidenceRoot 'client-latest.log')
} finally {
    if ($null -ne $client) {
        Stop-ClientProcesses
        if (-not $client.Process.HasExited) {
            $client.Process.Kill()
            [void]$client.Process.WaitForExit(10000)
        }
        Save-ProcessOutput -GradleProcess $client -Prefix 'client-gradle'
    }
    if ($null -ne $server -and -not $server.Process.HasExited) {
        try {
            Send-ProcessInput -Process $server.Process -Command 'stop'
        } catch {
            # The process may already have closed its input stream.
        }
        if (-not $server.Process.WaitForExit(120000)) {
            $server.Process.Kill()
            [void]$server.Process.WaitForExit(10000)
        }
    }
    if ($null -ne $server) {
        Save-ProcessOutput -GradleProcess $server -Prefix 'server-gradle'
    }
}

if (-not $completed) {
    throw 'Performance distribution soak did not complete successfully.'
}

Write-Output (Join-Path $evidenceRoot 'report.json')
