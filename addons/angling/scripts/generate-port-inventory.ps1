[CmdletBinding()]
param(
    [ValidateSet('Generate', 'Compare')]
    [string]$Mode = 'Generate'
)

$ErrorActionPreference = 'Stop'

$moduleRoot = Split-Path -Parent $PSScriptRoot
$repoRoot = (Resolve-Path (Join-Path $moduleRoot '..\..')).Path
$referenceRoot = Join-Path $moduleRoot 'reference\upstream-starcatcher-neoforge-1.21.1'
$inventoryPath = Join-Path $moduleRoot 'porting\inventory\source-inventory.json'
$delightPrefix = 'Starcatcher-Delight-1.21/'
$excludedDirectoryNames = @('.git', '.gradle', 'build', 'run', 'logs')
$allowedDispositions = @(
    'pending',
    'ported',
    'superseded-by-core-contract',
    'dependency-unavailable-on-fabric'
)

if (-not (Test-Path -LiteralPath $referenceRoot -PathType Container)) {
    throw "Authorized Angling reference is missing: $referenceRoot"
}

function Get-NormalizedRelativePath {
    param([System.IO.FileInfo]$File)

    $absolutePath = $File.FullName
    if (-not $absolutePath.StartsWith($referenceRoot, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "Reference file escaped the authorized root: $absolutePath"
    }
    $absolutePath.Substring($referenceRoot.Length).TrimStart('\', '/').Replace('\', '/')
}

function Get-SourceKind {
    param([string]$RelativePath)

    $extension = [System.IO.Path]::GetExtension($RelativePath).ToLowerInvariant()
    switch ($extension) {
        '.java' { return 'java' }
        '.json' { return 'json' }
        '.png' { return 'png' }
        '.ogg' { return 'ogg' }
        '.mcmeta' { return 'resource-metadata' }
        '.toml' { return 'metadata' }
        '.properties' { return 'build-metadata' }
        '.gradle' { return 'build-metadata' }
        '.bat' { return 'build-tooling' }
        '.md' { return 'documentation' }
        '.txt' { return 'documentation' }
        default {
            if ([System.IO.Path]::GetFileName($RelativePath) -eq 'gradlew') {
                return 'build-tooling'
            }
            return 'other'
        }
    }
}

function Get-SourceArea {
    param([string]$RelativePath)

    if ($RelativePath -match '(^|/)src/(main|generated)/java/') { return 'code' }
    if ($RelativePath -match '(^|/)src/(main|generated)/resources/data/') { return 'server-data' }
    if ($RelativePath -match '(^|/)src/(main|generated)/resources/assets/') { return 'client-assets' }
    if ($RelativePath -match '(^|/)src/test/') { return 'tests' }
    if ($RelativePath -match '(^|/)(build\.gradle|settings\.gradle|gradle\.properties|gradle/|gradlew)') {
        return 'reference-build-metadata'
    }
    return 'reference-metadata'
}

$previousDispositions = @{}
if (Test-Path -LiteralPath $inventoryPath -PathType Leaf) {
    $previous = Get-Content -Raw -LiteralPath $inventoryPath | ConvertFrom-Json
    foreach ($entry in $previous.entries) {
        if ($allowedDispositions -contains [string]$entry.disposition) {
            $previousDispositions[[string]$entry.path] = [string]$entry.disposition
        }
    }
}

$ledgerDispositions = @{}
$ledgerRoot = Join-Path $moduleRoot 'porting\ledger'
if (Test-Path -LiteralPath $ledgerRoot -PathType Container) {
    foreach ($ledgerFile in Get-ChildItem -LiteralPath $ledgerRoot -File -Filter '*.json') {
        $ledger = Get-Content -Raw -LiteralPath $ledgerFile.FullName | ConvertFrom-Json
        $candidateEntries = @()
        if ($null -ne $ledger.entries) { $candidateEntries += @($ledger.entries) }
        if ($null -ne $ledger.definitions) { $candidateEntries += @($ledger.definitions) }
        foreach ($entry in $candidateEntries) {
            $path = [string]$entry.sourcePath
            $disposition = [string]$entry.disposition
            if (-not [string]::IsNullOrWhiteSpace($path) -and $allowedDispositions -contains $disposition) {
                $ledgerDispositions[$path.Replace('\', '/')] = $disposition
            }
        }
        foreach ($excluded in @($ledger.excluded)) {
            $disposition = [string]$excluded.disposition
            if ($allowedDispositions -contains $disposition) {
                foreach ($path in @($excluded.sourcePaths)) {
                    if (-not [string]::IsNullOrWhiteSpace([string]$path)) {
                        $ledgerDispositions[[string]$path.Replace('\', '/')] = $disposition
                    }
                }
            }
        }
    }
}

$filesByPath = @{}
Get-ChildItem -LiteralPath $referenceRoot -Recurse -File -Force |
    Where-Object {
        $relative = Get-NormalizedRelativePath $_
        $segments = $relative.Split('/')
        -not ($segments | Where-Object { $excludedDirectoryNames -contains $_ })
    } |
    ForEach-Object { $filesByPath[(Get-NormalizedRelativePath $_)] = $_ }
$sortedPaths = [string[]]$filesByPath.Keys
[Array]::Sort($sortedPaths, [System.StringComparer]::Ordinal)
$files = @($sortedPaths | ForEach-Object { $filesByPath[$_] })

$entries = foreach ($file in $files) {
    $path = Get-NormalizedRelativePath $file
    $module = if ($path.StartsWith($delightPrefix, [System.StringComparison]::Ordinal)) {
        'elarion_angling_delight'
    } else {
        'elarion_angling'
    }
    $disposition = if ($ledgerDispositions.ContainsKey($path)) {
        $ledgerDispositions[$path]
    } elseif ($previousDispositions.ContainsKey($path)) {
        $previousDispositions[$path]
    } else {
        'pending'
    }

    [ordered]@{
        path = $path
        module = $module
        area = Get-SourceArea $path
        kind = Get-SourceKind $path
        sizeBytes = [long]$file.Length
        sha256 = (Get-FileHash -LiteralPath $file.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
        disposition = $disposition
    }
}

$moduleCounts = [ordered]@{}
foreach ($moduleName in @('elarion_angling', 'elarion_angling_delight')) {
    $moduleEntries = @($entries | Where-Object { $_['module'] -eq $moduleName })
    $moduleBytes = [long](($moduleEntries | ForEach-Object { [long]$_['sizeBytes'] } |
        Measure-Object -Sum).Sum)
    $moduleCounts[$moduleName] = [ordered]@{
        files = $moduleEntries.Count
        bytes = $moduleBytes
    }
}

$kindCounts = [ordered]@{}
foreach ($kind in @($entries | ForEach-Object { $_['kind'] } | Sort-Object -Unique)) {
    $kindCounts[$kind] = @($entries | Where-Object { $_['kind'] -eq $kind }).Count
}

$dispositionCounts = [ordered]@{}
foreach ($disposition in $allowedDispositions) {
    $dispositionCounts[$disposition] = @($entries | Where-Object { $_['disposition'] -eq $disposition }).Count
}

$totalBytes = [long](($entries | ForEach-Object { [long]$_['sizeBytes'] } | Measure-Object -Sum).Sum)

$inventory = [ordered]@{
    schemaVersion = 1
    sourceRevision = '016161dfc2d556d20fa641cd275e18c539256d4d'
    referenceRoot = 'addons/angling/reference/upstream-starcatcher-neoforge-1.21.1'
    generatedBy = 'addons/angling/scripts/generate-port-inventory.ps1'
    exclusions = [ordered]@{
        directoryNames = $excludedDirectoryNames
        reason = 'Ephemeral VCS, Gradle, development-runtime, and log state is not port input.'
    }
    allowedDispositions = $allowedDispositions
    summary = [ordered]@{
        totalFiles = @($entries).Count
        totalBytes = $totalBytes
        modules = $moduleCounts
        kinds = $kindCounts
        dispositions = $dispositionCounts
    }
    entries = @($entries)
}

$serialized = $inventory | ConvertTo-Json -Depth 8
if ($Mode -eq 'Compare') {
    if (-not (Test-Path -LiteralPath $inventoryPath -PathType Leaf)) {
        throw "Tracked inventory is missing: $inventoryPath"
    }
    $tracked = Get-Content -Raw -LiteralPath $inventoryPath
    if ($tracked.TrimEnd() -cne $serialized.TrimEnd()) {
        throw 'Tracked Angling source inventory differs from the authorized reference. Run this script in Generate mode and review the diff.'
    }
    Write-Host "Angling source inventory matches $($entries.Count) authorized reference files."
    exit 0
}

$inventoryDirectory = Split-Path -Parent $inventoryPath
New-Item -ItemType Directory -Path $inventoryDirectory -Force | Out-Null
[System.IO.File]::WriteAllText($inventoryPath, $serialized + [Environment]::NewLine,
    [System.Text.UTF8Encoding]::new($false))
Write-Host "Wrote Angling source inventory with $($entries.Count) files to $inventoryPath"
