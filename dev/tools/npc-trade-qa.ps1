param(
    [ValidateSet("setup", "open-trader", "open-banker", "open-bank", "capture-trader", "capture-banker", "capture-bank")]
    [string] $Action = "setup",
    [string] $World = "minecraft:overworld",
    [double] $BankerX = -2.0,
    [double] $TraderX = 2.0,
    [double] $NpcY = 65.0,
    [double] $NpcZ = 0.0,
    [double] $ViewerZ = 4.0,
    [int] $ClickX = 435,
    [int] $ClickY = 260,
    [int] $TradeButtonX = 435,
    [int] $TradeButtonY = 250,
    [int] $DelayMs = 220,
    [string] $OutputDir = "build\ui-qa\npc-trade-qa",
    [string] $TitlePattern = "Minecraft*",
    [switch] $CloseScreens
)

$ErrorActionPreference = "Stop"

$qa = Join-Path $PSScriptRoot "minecraft-qa.ps1"
if (-not (Test-Path $qa)) {
    throw "Missing minecraft-qa.ps1 next to this script."
}

function Invoke-QaCommand([string] $Command) {
    & powershell -NoProfile -ExecutionPolicy Bypass -File $qa `
        -Action command -Command $Command -TitlePattern $TitlePattern | Out-Host
    Start-Sleep -Milliseconds $DelayMs
}

function Invoke-QaClick([string] $Button = "right") {
    & powershell -NoProfile -ExecutionPolicy Bypass -File $qa `
        -Action click -Button $Button -X $ClickX -Y $ClickY -TitlePattern $TitlePattern | Out-Host
    Start-Sleep -Milliseconds $DelayMs
}

function Invoke-QaTradeButton {
    & powershell -NoProfile -ExecutionPolicy Bypass -File $qa `
        -Action click -Button left -X $TradeButtonX -Y $TradeButtonY -TitlePattern $TitlePattern | Out-Host
    Start-Sleep -Milliseconds $DelayMs
}

function Invoke-QaCapture([string] $Name) {
    New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null
    $output = Join-Path $OutputDir $Name
    & powershell -NoProfile -ExecutionPolicy Bypass -File $qa `
        -Action capture -Output $output -TitlePattern $TitlePattern | Out-Host
}

function Close-OpenScreens {
    & powershell -NoProfile -ExecutionPolicy Bypass -File $qa `
        -Action key -Keys "{ESC}" -Count 2 -TitlePattern $TitlePattern | Out-Host
    Start-Sleep -Milliseconds $DelayMs
}

function Teleport-Viewer([double] $X) {
    Invoke-QaCommand "/execute in $World run tp @s $X $NpcY $ViewerZ 180 8"
}

function Setup-NpcTradeScene {
    if ($CloseScreens) { Close-OpenScreens }
    Invoke-QaCommand "/gamemode creative"
    Invoke-QaCommand "/execute in $World run tp @s 0 $NpcY $ViewerZ 180 8"

    foreach ($index in 1..6) {
        Invoke-QaCommand "/e npc remove worldheart_banker_$index"
        Invoke-QaCommand "/e npc remove worldheart_trader_$index"
    }

    Invoke-QaCommand "/execute in $World run tp @s $BankerX $NpcY $NpcZ 180 0"
    Invoke-QaCommand "/e npc place worldheart_banker yaw 180"
    Invoke-QaCommand "/execute in $World run tp @s $TraderX $NpcY $NpcZ 180 0"
    Invoke-QaCommand "/e npc place worldheart_trader yaw 180"
    Invoke-QaCommand "/e npc repair all"
    Teleport-Viewer $TraderX
}

function Open-TraderTradeScreen {
    if ($CloseScreens) { Close-OpenScreens }
    Teleport-Viewer $TraderX
    Invoke-QaCommand "/e npc open worldheart_trader_1"
    Start-Sleep -Milliseconds 700
    Invoke-QaTradeButton
    Start-Sleep -Milliseconds 700
}

function Open-BankerBankScreen {
    if ($CloseScreens) { Close-OpenScreens }
    Teleport-Viewer $BankerX
    Invoke-QaCommand "/e npc open worldheart_banker_1"
    Start-Sleep -Milliseconds 700
    Invoke-QaTradeButton
    Start-Sleep -Milliseconds 700
}

switch ($Action) {
    "setup" {
        Setup-NpcTradeScene
        Write-Output "NPC trade QA scene ready. Player is facing worldheart_trader_1."
    }
    "open-trader" {
        Open-TraderTradeScreen
        Write-Output "Attempted to open worldheart_trader_1."
    }
    "open-banker" {
        if ($CloseScreens) { Close-OpenScreens }
        Teleport-Viewer $BankerX
        Invoke-QaCommand "/e npc open worldheart_banker_1"
        Write-Output "Attempted to open worldheart_banker_1."
    }
    "open-bank" {
        Open-BankerBankScreen
        Write-Output "Attempted to open worldheart_banker_1 bank service."
    }
    "capture-trader" {
        Open-TraderTradeScreen
        Invoke-QaCapture "trader.png"
    }
    "capture-banker" {
        if ($CloseScreens) { Close-OpenScreens }
        Teleport-Viewer $BankerX
        Invoke-QaCommand "/e npc open worldheart_banker_1"
        Start-Sleep -Milliseconds 700
        Invoke-QaCapture "banker.png"
    }
    "capture-bank" {
        Open-BankerBankScreen
        Invoke-QaCapture "bank.png"
    }
}
