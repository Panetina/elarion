param(
    [ValidateSet("focus", "maximize", "command", "move", "click", "scroll", "capture")]
    [string] $Action,
    [string] $Command,
    [int] $X = 0,
    [int] $Y = 0,
    [int] $Wheel = -1,
    [int] $Count = 1,
    [string] $Output,
    [string] $TitlePattern = "Minecraft*",
    [switch] $ScreenCapture
)

$ErrorActionPreference = "Stop"

if (-not ("ElarionMinecraftQa" -as [type])) {
    Add-Type @"
using System;
using System.Runtime.InteropServices;

public static class ElarionMinecraftQa {
    [StructLayout(LayoutKind.Sequential)]
    public struct POINT {
        public int X;
        public int Y;
    }

    [DllImport("user32.dll")]
    public static extern bool SetForegroundWindow(IntPtr hWnd);

    [DllImport("user32.dll")]
    public static extern bool ShowWindow(IntPtr hWnd, int nCmdShow);

    [DllImport("user32.dll")]
    public static extern bool IsIconic(IntPtr hWnd);

    [DllImport("user32.dll")]
    public static extern bool ClientToScreen(IntPtr hWnd, ref POINT point);

    [DllImport("user32.dll")]
    public static extern bool SetCursorPos(int x, int y);

    [DllImport("user32.dll")]
    public static extern bool PostMessage(IntPtr hWnd, int msg, IntPtr wParam, IntPtr lParam);
}
"@
}

function Get-MinecraftProcess {
    $process = Get-Process |
        Where-Object { $_.MainWindowHandle -ne 0 -and $_.MainWindowTitle -like $TitlePattern } |
        Select-Object -First 1
    if (-not $process) {
        throw "No window matched title pattern '$TitlePattern'. Start runClientOne and join the local server first."
    }
    return $process
}

function Focus-Minecraft([System.Diagnostics.Process] $Process) {
    if ([ElarionMinecraftQa]::IsIconic($Process.MainWindowHandle)) {
        [ElarionMinecraftQa]::ShowWindow($Process.MainWindowHandle, 9) | Out-Null
    }
    [ElarionMinecraftQa]::SetForegroundWindow($Process.MainWindowHandle) | Out-Null
    Start-Sleep -Milliseconds 120
}

function Send-MinecraftClick([System.Diagnostics.Process] $Process, [int] $ClickX, [int] $ClickY) {
    Move-MinecraftCursor $Process $ClickX $ClickY
    $lParam = [IntPtr](($ClickY -shl 16) -bor ($ClickX -band 0xffff))
    [ElarionMinecraftQa]::PostMessage($Process.MainWindowHandle, 0x0200, [IntPtr]::Zero, $lParam) | Out-Null
    [ElarionMinecraftQa]::PostMessage($Process.MainWindowHandle, 0x0201, [IntPtr]1, $lParam) | Out-Null
    [ElarionMinecraftQa]::PostMessage($Process.MainWindowHandle, 0x0202, [IntPtr]::Zero, $lParam) | Out-Null
}

function Move-MinecraftCursor([System.Diagnostics.Process] $Process, [int] $MoveX, [int] $MoveY) {
    $point = New-Object ElarionMinecraftQa+POINT
    $point.X = $MoveX
    $point.Y = $MoveY
    [ElarionMinecraftQa]::ClientToScreen($Process.MainWindowHandle, [ref] $point) | Out-Null
    [ElarionMinecraftQa]::SetCursorPos($point.X, $point.Y) | Out-Null
}

function Send-MinecraftScroll([System.Diagnostics.Process] $Process, [int] $ScrollX, [int] $ScrollY, [int] $WheelSteps) {
    $delta = [Math]::Max(-10, [Math]::Min(10, $WheelSteps)) * 120
    $wParam = [IntPtr]((($delta -band 0xffff) -shl 16))
    $lParam = [IntPtr](($ScrollY -shl 16) -bor ($ScrollX -band 0xffff))
    [ElarionMinecraftQa]::PostMessage($Process.MainWindowHandle, 0x0200, [IntPtr]::Zero, $lParam) | Out-Null
    [ElarionMinecraftQa]::PostMessage($Process.MainWindowHandle, 0x020A, $wParam, $lParam) | Out-Null
}

$minecraft = Get-MinecraftProcess

switch ($Action) {
    "focus" {
        Focus-Minecraft $minecraft
        Write-Output "Focused '$($minecraft.MainWindowTitle)'."
    }
    "maximize" {
        [ElarionMinecraftQa]::ShowWindow($minecraft.MainWindowHandle, 3) | Out-Null
        Focus-Minecraft $minecraft
        Write-Output "Maximized '$($minecraft.MainWindowTitle)'."
    }
    "command" {
        if ([string]::IsNullOrWhiteSpace($Command)) {
            throw "-Command is required for -Action command."
        }
        Focus-Minecraft $minecraft
        Set-Clipboard -Value $Command
        $shell = New-Object -ComObject WScript.Shell
        $shell.SendKeys("t")
        Start-Sleep -Milliseconds 120
        $shell.SendKeys("^v")
        Start-Sleep -Milliseconds 80
        $shell.SendKeys("{ENTER}")
        Write-Output "Sent command: $Command"
    }
    "move" {
        Focus-Minecraft $minecraft
        Move-MinecraftCursor $minecraft $X $Y
        Start-Sleep -Milliseconds 90
        Write-Output "Moved cursor to $X,$Y."
    }
    "click" {
        Focus-Minecraft $minecraft
        for ($index = 0; $index -lt [Math]::Max(1, $Count); $index++) {
            Send-MinecraftClick $minecraft $X $Y
            Start-Sleep -Milliseconds 90
        }
        Write-Output "Clicked $X,$Y x$([Math]::Max(1, $Count))."
    }
    "scroll" {
        Focus-Minecraft $minecraft
        for ($index = 0; $index -lt [Math]::Max(1, $Count); $index++) {
            Send-MinecraftScroll $minecraft $X $Y $Wheel
            Start-Sleep -Milliseconds 90
        }
        Write-Output "Scrolled $X,$Y wheel=$Wheel x$([Math]::Max(1, $Count))."
    }
    "capture" {
        $capture = Join-Path $PSScriptRoot "capture-minecraft-window.ps1"
        $args = @("-File", $capture, "-TitlePattern", $TitlePattern)
        if (-not [string]::IsNullOrWhiteSpace($Output)) {
            $args += @("-Output", $Output)
        }
        if ($ScreenCapture) {
            $args += "-ScreenCapture"
        }
        & powershell -NoProfile -ExecutionPolicy Bypass @args
    }
}
