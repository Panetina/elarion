param(
    [ValidateSet('all', 'core-only', 'npcs-without-economy', 'voicechat-without-provider')]
    [string]$Case = 'all',
    [int]$StartupTimeoutSeconds = 180,
    [int]$StabilizationSeconds = 3
)

$ErrorActionPreference = 'Stop'
$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$qaRoot = Join-Path $repoRoot 'build\qa\optional-addon'

$cases = @(
    [pscustomobject]@{
        Name = 'core-only'
        Task = ':platform:core:runServer'
        RunDirectory = 'platform-core'
        ExpectedMarker = 'Elarion Core initialized'
        ForbiddenMarkers = @('Elarion NPCs initialized', 'Elarion Economy initialized')
    },
    [pscustomobject]@{
        Name = 'npcs-without-economy'
        Task = ':addons:npcs:runServer'
        RunDirectory = 'addons-npcs'
        ExpectedMarker = 'Elarion NPCs initialized'
        ForbiddenMarkers = @('Elarion Economy initialized')
    },
    [pscustomobject]@{
        Name = 'voicechat-without-provider'
        Task = ':addons:voicechat-hooks:runServer'
        RunDirectory = 'addons-voicechat-hooks'
        ExpectedMarker = 'Elarion Voice Chat Hooks addon shell initialized'
        ForbiddenMarkers = @('voicechat.api', 'Simple Voice Chat initialized')
    }
)

if ($Case -ne 'all') {
    $cases = @($cases | Where-Object Name -eq $Case)
}

function Assert-QaPath([string]$Path) {
    $fullPath = [System.IO.Path]::GetFullPath($Path)
    $fullQaRoot = [System.IO.Path]::GetFullPath($qaRoot)
    if (!$fullPath.StartsWith($fullQaRoot, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "Refusing to modify non-QA path: $fullPath"
    }
}

function Invoke-OptionalAddonCase($Definition) {
    $runDirectory = Join-Path $qaRoot $Definition.RunDirectory
    Assert-QaPath $runDirectory
    if (Test-Path -LiteralPath $runDirectory) {
        Remove-Item -LiteralPath $runDirectory -Recurse -Force
    }
    New-Item -ItemType Directory -Path $runDirectory -Force | Out-Null
    Set-Content -LiteralPath (Join-Path $runDirectory 'eula.txt') -Value 'eula=true' -Encoding ascii
    @(
        'online-mode=false'
        'server-port=0'
        'level-name=world'
        'motd=Elarion Optional Addon QA'
        'max-players=1'
        'view-distance=2'
        'simulation-distance=2'
    ) | Set-Content -LiteralPath (Join-Path $runDirectory 'server.properties') -Encoding ascii

    $output = [System.Text.StringBuilder]::new()
    $processInfo = [System.Diagnostics.ProcessStartInfo]::new()
    $processInfo.FileName = 'cmd.exe'
    $processInfo.WorkingDirectory = $repoRoot
    $processInfo.Arguments = "/d /s /c `"`"$repoRoot\gradlew.bat`" -PoptionalAddonQa=true $($Definition.Task) --console=plain --no-daemon`""
    $processInfo.UseShellExecute = $false
    $processInfo.RedirectStandardInput = $true
    $processInfo.RedirectStandardOutput = $true
    $processInfo.RedirectStandardError = $true
    $processInfo.CreateNoWindow = $true

    $process = [System.Diagnostics.Process]::new()
    $process.StartInfo = $processInfo
    $handler = {
        if ($null -ne $EventArgs.Data) {
            [void]$Event.MessageData.AppendLine($EventArgs.Data)
        }
    }
    $stdoutEvent = Register-ObjectEvent -InputObject $process -EventName OutputDataReceived -Action $handler -MessageData $output
    $stderrEvent = Register-ObjectEvent -InputObject $process -EventName ErrorDataReceived -Action $handler -MessageData $output

    try {
        Write-Host "[$($Definition.Name)] starting isolated server..."
        if (!$process.Start()) {
            throw "Could not start Gradle for $($Definition.Name)"
        }
        $process.BeginOutputReadLine()
        $process.BeginErrorReadLine()
        $deadline = [DateTime]::UtcNow.AddSeconds($StartupTimeoutSeconds)
        $started = $false
        while (!$process.HasExited -and [DateTime]::UtcNow -lt $deadline) {
            Start-Sleep -Milliseconds 250
            if ($output.ToString() -match 'Done \([0-9.]+s\)!') {
                $started = $true
                break
            }
        }

        if (!$started) {
            if ($process.HasExited) {
                throw "Gradle exited with code $($process.ExitCode) before the server reached Done"
            }
            throw "Server did not reach Done within $StartupTimeoutSeconds seconds"
        }

        $stableUntil = [DateTime]::UtcNow.AddSeconds($StabilizationSeconds)
        while (!$process.HasExited -and [DateTime]::UtcNow -lt $stableUntil) {
            Start-Sleep -Milliseconds 250
        }
        if ($process.HasExited) {
            throw "Server exited during the $StabilizationSeconds-second stabilization window"
        }

        $process.StandardInput.WriteLine('stop')
        $process.StandardInput.Flush()
        if (!$process.WaitForExit(60000)) {
            $process.Kill($true)
            throw 'Server reached Done but did not stop cleanly within 60 seconds'
        }
        $process.WaitForExit()

        $text = $output.ToString()
        if ($process.ExitCode -ne 0) {
            throw "Gradle exited with code $($process.ExitCode)"
        }
        if (!$text.Contains($Definition.ExpectedMarker)) {
            throw "Missing expected marker: $($Definition.ExpectedMarker)"
        }
        foreach ($marker in $Definition.ForbiddenMarkers) {
            if ($text.Contains($marker)) {
                throw "Provider-absence assertion failed; found: $marker"
            }
        }
        if ($text -match '(?m)^.*\b(ERROR|FATAL)\b.*$') {
            throw 'Startup output contains an ERROR or FATAL line'
        }

        Write-Host "[$($Definition.Name)] PASS"
        return [pscustomobject]@{
            Case = $Definition.Name
            Status = 'PASS'
            Task = $Definition.Task
            RunDirectory = $runDirectory
        }
    } finally {
        if ($process -and !$process.HasExited) {
            $process.Kill($true)
            $process.WaitForExit()
        }
        Start-Sleep -Milliseconds 200
        $logPath = Join-Path $runDirectory 'optional-addon-qa.log'
        Set-Content -LiteralPath $logPath -Value $output.ToString() -Encoding utf8
        Unregister-Event -SourceIdentifier $stdoutEvent.Name -ErrorAction SilentlyContinue
        Unregister-Event -SourceIdentifier $stderrEvent.Name -ErrorAction SilentlyContinue
        $process.Dispose()
    }
}

New-Item -ItemType Directory -Path $qaRoot -Force | Out-Null
$results = foreach ($definition in $cases) {
    Invoke-OptionalAddonCase $definition
}
$results | Format-Table -AutoSize
