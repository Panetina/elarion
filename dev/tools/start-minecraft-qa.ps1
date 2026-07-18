param(
    [ValidateSet("server", "client", "both", "status", "stop")]
    [string] $Action = "both",
    [switch] $Restart,
    [switch] $NoWait,
    [int] $WaitSeconds = 360,
    [string] $TitlePattern = "Minecraft*"
)

$ErrorActionPreference = "Stop"

$RepoRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")

function Get-RepoJavaProcesses {
    Get-CimInstance Win32_Process |
        Where-Object {
            ($_.Name -match "^(java|javaw|gradle)\.exe$") -and
            ($_.CommandLine -like "*$RepoRoot*" -or $_.CommandLine -like "*Desktop\Modding\elarion*")
        }
}

function Show-QAStatus {
    $repoProcesses = Get-RepoJavaProcesses |
        Select-Object ProcessId, Name, @{Name = "Command"; Expression = {
            if ($_.CommandLine.Length -gt 150) {
                $_.CommandLine.Substring(0, 150) + "..."
            } else {
                $_.CommandLine
            }
        }}
    $minecraftWindows = Get-Process |
        Where-Object { $_.MainWindowHandle -ne 0 -and $_.MainWindowTitle -like $TitlePattern } |
        Select-Object Id, ProcessName, MainWindowTitle

    Write-Output "Repository: $RepoRoot"
    Write-Output ""
    Write-Output "Repo Java/Gradle processes:"
    if ($repoProcesses) {
        $repoProcesses | Format-Table -AutoSize | Out-String -Width 240
    } else {
        Write-Output "  none"
    }
    Write-Output ""
    Write-Output "Minecraft windows matching '$TitlePattern':"
    if ($minecraftWindows) {
        $minecraftWindows | Format-Table -AutoSize | Out-String -Width 180
    } else {
        Write-Output "  none"
    }
}

function Stop-QAProcesses {
    $processes = Get-RepoJavaProcesses
    foreach ($process in $processes) {
        Write-Output "Stopping $($process.Name) pid=$($process.ProcessId)"
        Stop-Process -Id $process.ProcessId -Force -ErrorAction SilentlyContinue
    }
}

function Start-VisibleGradleWindow([string] $WindowTitle, [string] $TaskName) {
    $escapedRoot = $RepoRoot.Path.Replace("'", "''")
    $escapedTitle = $WindowTitle.Replace("'", "''")
    $command = @"
`$Host.UI.RawUI.WindowTitle = '$escapedTitle'
Set-Location -LiteralPath '$escapedRoot'
.\gradlew.bat $TaskName --console=plain
Write-Host ''
Write-Host 'Gradle task ended. Leave this window open if you need the log, or close it.'
"@
    Start-Process -FilePath "powershell.exe" -ArgumentList @(
        "-NoExit",
        "-NoProfile",
        "-ExecutionPolicy",
        "Bypass",
        "-Command",
        $command
    ) -WorkingDirectory $RepoRoot.Path
}

function Wait-MinecraftWindow {
    if ($NoWait) {
        return
    }

    $deadline = (Get-Date).AddSeconds([Math]::Max(1, $WaitSeconds))
    do {
        $window = Get-Process |
            Where-Object { $_.MainWindowHandle -ne 0 -and $_.MainWindowTitle -like $TitlePattern } |
            Select-Object -First 1
        if ($window) {
            Write-Output "Minecraft window ready: pid=$($window.Id), title='$($window.MainWindowTitle)'"
            return
        }
        Start-Sleep -Seconds 2
    } while ((Get-Date) -lt $deadline)

    throw "Timed out waiting for a Minecraft window matching '$TitlePattern'. Check the visible Elarion QA Client terminal."
}

if ($Restart) {
    Stop-QAProcesses
    Start-Sleep -Seconds 2
}

switch ($Action) {
    "server" {
        Start-VisibleGradleWindow "Elarion QA Server" "runServer"
        Write-Output "Started visible Elarion QA Server terminal."
    }
    "client" {
        Start-VisibleGradleWindow "Elarion QA Client" "runClientOne"
        Write-Output "Started visible Elarion QA Client terminal."
        Wait-MinecraftWindow
    }
    "both" {
        Start-VisibleGradleWindow "Elarion QA Server" "runServer"
        Write-Output "Started visible Elarion QA Server terminal."
        Start-Sleep -Seconds 4
        Start-VisibleGradleWindow "Elarion QA Client" "runClientOne"
        Write-Output "Started visible Elarion QA Client terminal."
        Wait-MinecraftWindow
    }
    "status" {
        Show-QAStatus
    }
    "stop" {
        Stop-QAProcesses
    }
}
