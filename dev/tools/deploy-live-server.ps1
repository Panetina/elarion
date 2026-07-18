[CmdletBinding()]
param(
    [switch]$ConfirmDeploy,
    [switch]$ServerStopped,
    [switch]$ValidateOnly,
    [switch]$PlanOnly,
    [string]$ConfigPath
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

if ([string]::IsNullOrWhiteSpace($ConfigPath)) {
    $ConfigPath = Join-Path $PSScriptRoot '..\..\.elarion-deploy.local.psd1'
}

if (-not $ValidateOnly -and -not $PlanOnly -and (-not $ConfirmDeploy -or -not $ServerStopped)) {
    throw 'Live deployment requires both -ConfirmDeploy and -ServerStopped.'
}

$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$configFile = Resolve-Path -LiteralPath $ConfigPath -ErrorAction SilentlyContinue
if (-not $configFile) {
    throw "Missing local deployment config: $ConfigPath. Copy dev/deploy/live-server.example.psd1 to .elarion-deploy.local.psd1 and fill in the SFTP target."
}

$config = Import-PowerShellDataFile -LiteralPath $configFile.Path
$target = if ($env:ELARION_PEBBLEHOST_TARGET) { $env:ELARION_PEBBLEHOST_TARGET } else { [string]$config.Target }
$port = if ($env:ELARION_PEBBLEHOST_PORT) { [int]$env:ELARION_PEBBLEHOST_PORT } else { [int]$config.Port }
$identityFile = if ($env:ELARION_PEBBLEHOST_IDENTITY_FILE) {
    $env:ELARION_PEBBLEHOST_IDENTITY_FILE
} else {
    [string]$config.IdentityFile
}
$remoteRoot = if ($config.RemoteRoot) { ([string]$config.RemoteRoot).TrimEnd('/') } else { '.' }

if ([string]::IsNullOrWhiteSpace($target) -or $target -like 'replace-*') {
    throw 'The PebbleHost SFTP target is not configured.'
}
if ($identityFile.StartsWith('~/') -or $identityFile.StartsWith('~\')) {
    $identityFile = Join-Path $env:USERPROFILE $identityFile.Substring(2)
}
$identityFile = (Resolve-Path -LiteralPath $identityFile -ErrorAction Stop).Path

$serverMods = Join-Path $repositoryRoot 'build\export\server-mods'
$jars = @(Get-ChildItem -LiteralPath $serverMods -File -Filter '*.jar' | Sort-Object Name)
if ($jars.Count -eq 0) {
    throw "No server jars found in $serverMods. Run the guarded Gradle deployment task instead of this script directly."
}

$stamp = [DateTime]::UtcNow.ToString('yyyyMMdd-HHmmss')
$workDirectory = Join-Path $repositoryRoot "build\deploy-live-server\$stamp"
New-Item -ItemType Directory -Path $workDirectory -Force | Out-Null
$manifestPath = Join-Path $workDirectory 'server-mods.sha256'
$manifest = $jars | ForEach-Object {
    $hash = (Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
    "$hash  $($_.Name)"
}
Set-Content -LiteralPath $manifestPath -Value $manifest -Encoding ascii

$remoteStage = "$remoteRoot/.elarion-staging/release-$stamp"
$remoteBackup = "$remoteRoot/.elarion-backups/release-$stamp"
$sftp = Join-Path $env:WINDIR 'System32\OpenSSH\sftp.exe'
if (-not (Test-Path -LiteralPath $sftp -PathType Leaf)) {
    throw "OpenSSH SFTP was not found at $sftp."
}

if ($ValidateOnly) {
    Write-Host "Live deployment preflight passed for $($jars.Count) server jars."
    Write-Host "Manifest: $manifestPath"
    exit 0
}

function Quote-SftpPath([string]$Path) {
    return '"' + $Path.Replace('\', '/').Replace('"', '\"') + '"'
}

function Write-SftpBatch([string]$Name, [string[]]$Commands) {
    $batchPath = Join-Path $workDirectory "$Name.sftp"
    Set-Content -LiteralPath $batchPath -Value ($Commands + 'bye') -Encoding ascii
    return $batchPath
}

function Invoke-SftpBatch([string]$Name, [string[]]$Commands) {
    $batchPath = Write-SftpBatch -Name $Name -Commands $Commands
    $arguments = @(
        '-q', '-b', $batchPath,
        '-i', $identityFile,
        '-P', [string]$port,
        '-o', 'BatchMode=yes',
        '-o', 'StrictHostKeyChecking=yes',
        $target
    )
    & $sftp @arguments
    if ($LASTEXITCODE -ne 0) {
        throw "PebbleHost SFTP $Name step failed with exit code $LASTEXITCODE."
    }
}

$stageCommands = @(
    "-mkdir $remoteRoot/.elarion-staging",
    "mkdir $remoteStage",
    "mkdir $remoteStage/mods"
)
foreach ($jar in $jars) {
    $stageCommands += 'put ' + (Quote-SftpPath $jar.FullName) + ' ' + (Quote-SftpPath "$remoteStage/mods/$($jar.Name)")
}
$stageCommands += 'put ' + (Quote-SftpPath $manifestPath) + ' ' + (Quote-SftpPath "$remoteStage/server-mods.sha256")
$stageCommands += "ls -la $remoteStage/mods"

$commitCommands = @(
    "-mkdir $remoteRoot/.elarion-backups",
    "mkdir $remoteBackup",
    "rename $remoteRoot/mods $remoteBackup/mods",
    "rename $remoteStage/mods $remoteRoot/mods",
    "rename $remoteStage/server-mods.sha256 $remoteRoot/.elarion-live-server-mods.sha256",
    "ls -la $remoteRoot/mods"
)
$rollbackCommands = @(
    "-rename $remoteRoot/mods $remoteStage/failed-mods",
    "-rename $remoteBackup/mods $remoteRoot/mods"
)

if ($PlanOnly) {
    $stagePlan = Write-SftpBatch -Name 'stage' -Commands $stageCommands
    $commitPlan = Write-SftpBatch -Name 'commit' -Commands $commitCommands
    $rollbackPlan = Write-SftpBatch -Name 'rollback' -Commands $rollbackCommands
    Write-Host "Live deployment plan generated for $($jars.Count) server jars."
    Write-Host "Manifest: $manifestPath"
    Write-Host "Stage plan: $stagePlan"
    Write-Host "Commit plan: $commitPlan"
    Write-Host "Rollback plan: $rollbackPlan"
    Write-Host 'No network connection was opened and no live-server state was changed.'
    exit 0
}

Write-Host "Staging $($jars.Count) verified jars for live release $stamp..."
Invoke-SftpBatch -Name 'stage' -Commands $stageCommands

try {
    Write-Host "Promoting release $stamp. The server must remain stopped..."
    Invoke-SftpBatch -Name 'commit' -Commands $commitCommands
} catch {
    Write-Warning "Promotion failed. Attempting to restore $remoteBackup/mods."
    try {
        Invoke-SftpBatch -Name 'rollback' -Commands $rollbackCommands
    } catch {
        Write-Warning 'Automatic rollback could not be confirmed. Keep the server stopped and inspect the remote mods and backup directories.'
    }
    throw
}

Write-Host "Live server mods promoted successfully."
Write-Host "Release: $stamp"
Write-Host "Previous mods: $remoteBackup/mods"
Write-Host 'The script did not start or restart the server. Verify the live startup log after starting it manually.'
