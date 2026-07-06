param(
    [string] $Output,
    [string] $TitlePattern = "Minecraft* 1.21.1",
    [switch] $ScreenCapture,
    [switch] $Restore
)

$ErrorActionPreference = "Stop"

if (-not ("ElarionWindowCapture" -as [type])) {
    Add-Type @"
using System;
using System.Runtime.InteropServices;

public static class ElarionWindowCapture {
    [DllImport("user32.dll")]
    public static extern bool GetWindowRect(IntPtr hWnd, out RECT rect);

    [DllImport("user32.dll")]
    public static extern bool PrintWindow(IntPtr hwnd, IntPtr hdcBlt, uint nFlags);

    [DllImport("user32.dll")]
    public static extern bool ShowWindow(IntPtr hWnd, int nCmdShow);

    public struct RECT {
        public int Left;
        public int Top;
        public int Right;
        public int Bottom;
    }
}
"@
}

Add-Type -AssemblyName System.Drawing

$process = Get-Process |
    Where-Object { $_.MainWindowHandle -ne 0 -and $_.MainWindowTitle -like $TitlePattern } |
    Select-Object -First 1

if (-not $process) {
    throw "No window matched title pattern '$TitlePattern'. Start the dev client first, for example: .\gradlew.bat runClientOne"
}

if ($Restore) {
    [ElarionWindowCapture]::ShowWindow($process.MainWindowHandle, 9) | Out-Null
    Start-Sleep -Milliseconds 250
}

$rect = New-Object ElarionWindowCapture+RECT
[ElarionWindowCapture]::GetWindowRect($process.MainWindowHandle, [ref] $rect) | Out-Null

$width = [Math]::Max(1, $rect.Right - $rect.Left)
$height = [Math]::Max(1, $rect.Bottom - $rect.Top)

if ([string]::IsNullOrWhiteSpace($Output)) {
    $directory = Join-Path (Get-Location) "build\ui-qa"
    New-Item -ItemType Directory -Force -Path $directory | Out-Null
    $timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
    $Output = Join-Path $directory "minecraft-window-$timestamp.png"
} else {
    $parent = Split-Path -Parent $Output
    if (-not [string]::IsNullOrWhiteSpace($parent)) {
        New-Item -ItemType Directory -Force -Path $parent | Out-Null
    }
}

$bitmap = New-Object System.Drawing.Bitmap $width, $height
$graphics = [System.Drawing.Graphics]::FromImage($bitmap)

try {
    if ($ScreenCapture) {
        $graphics.CopyFromScreen($rect.Left, $rect.Top, 0, 0, $bitmap.Size)
    } else {
        $hdc = $graphics.GetHdc()
        try {
            $ok = [ElarionWindowCapture]::PrintWindow($process.MainWindowHandle, $hdc, 2)
        } finally {
            $graphics.ReleaseHdc($hdc)
        }
        if (-not $ok) {
            throw "PrintWindow capture failed for '$($process.MainWindowTitle)'. Try -ScreenCapture with the Minecraft window visible."
        }
    }

    $bitmap.Save($Output, [System.Drawing.Imaging.ImageFormat]::Png)
} finally {
    $graphics.Dispose()
    $bitmap.Dispose()
}

Write-Output "Captured '$($process.MainWindowTitle)' ($width x $height) to $Output"
