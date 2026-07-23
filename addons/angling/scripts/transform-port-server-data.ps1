[CmdletBinding()]
param(
    [ValidateSet('Generate', 'Verify')]
    [string]$Mode = 'Generate'
)

$ErrorActionPreference = 'Stop'
$moduleRoot = Split-Path -Parent $PSScriptRoot
$referenceRoot = Join-Path $moduleRoot 'reference\upstream-starcatcher-neoforge-1.21.1'
$sourceRoots = @(
    (Join-Path $referenceRoot 'src\main\resources\data\starcatcher')
    (Join-Path $referenceRoot 'src\generated\resources\data\starcatcher')
)
$targetRoot = Join-Path $moduleRoot 'src\main\resources\data\elarion_angling'
$identity = Get-Content -Raw -LiteralPath (Join-Path $moduleRoot 'porting\fish-identity.json') | ConvertFrom-Json
$ledgerPath = Join-Path $moduleRoot 'porting\ledger\native-server-data.json'
$includedAreas = @('advancement', 'loot_table', 'recipe', 'tags')
$excludedAreas = [ordered]@{
    curios = 'dependency-unavailable-on-fabric'
    data_maps = 'ported'
    loot_modifiers = 'pending'
    starcatcher = 'ported-separately-as-catch-definitions'
}
$renames = @{}
foreach ($property in $identity.renames.PSObject.Properties) { $renames[$property.Name] = [string]$property.Value }
$orderedRenameKeys = @($renames.Keys | Sort-Object { $_.Length } -Descending)
$fishTokenPattern = '(?<![a-z0-9_])(?:' + (($orderedRenameKeys | ForEach-Object {
    [System.Text.RegularExpressions.Regex]::Escape($_)
}) -join '|') + ')(?![a-z0-9_])'

function Convert-FishIdTokens([string]$Value) {
    return [System.Text.RegularExpressions.Regex]::Replace(
        $Value,
        $fishTokenPattern,
        { param($match) [string]$renames[$match.Value] })
}

function Convert-PortString([string]$Value) {
    $result = Convert-FishIdTokens $Value
    return $result.Replace('starcatcher', 'elarion_angling')
}

function Convert-PortNode([object]$Node) {
    if ($null -eq $Node) { return $null }
    if ($Node -is [string]) { return Convert-PortString $Node }
    if ($Node -is [System.Management.Automation.PSCustomObject]) {
        $converted = [ordered]@{}
        foreach ($property in $Node.PSObject.Properties) {
            if ($property.Name -eq 'neoforge:conditions') { continue }
            $name = Convert-PortString $property.Name
            if ($converted.Contains($name)) { throw "Server-data key collision: $name" }
            $converted[$name] = Convert-PortNode $property.Value
        }
        return $converted
    }
    if ($Node -is [System.Collections.IEnumerable]) {
        $items = @()
        foreach ($item in $Node) { $items += ,(Convert-PortNode $item) }
        return ,$items
    }
    return $Node
}

function Convert-PortPath([string]$Value) {
    $result = Convert-FishIdTokens ($Value.Replace('tags/dimensions/', 'tags/dimension/'))
    return $result.Replace('starcatcher', 'elarion_angling')
}

function Get-Hash([byte[]]$Bytes) {
    $sha = [System.Security.Cryptography.SHA256]::Create()
    try { return ([BitConverter]::ToString($sha.ComputeHash($Bytes))).Replace('-', '').ToLowerInvariant() }
    finally { $sha.Dispose() }
}

function Compare-Or-Write([string]$Path, [string]$Expected) {
    if ($Mode -eq 'Verify') {
        if (-not (Test-Path -LiteralPath $Path -PathType Leaf) -or
            (Get-Content -Raw -LiteralPath $Path).TrimEnd() -cne $Expected.TrimEnd()) {
            throw "Transformed Angling server data is missing or stale: $Path"
        }
        return
    }
    New-Item -ItemType Directory -Path (Split-Path -Parent $Path) -Force | Out-Null
    [System.IO.File]::WriteAllText($Path, $Expected, [System.Text.UTF8Encoding]::new($false))
}

$candidates = @()
$excludedCounts = @{}
$excludedSourcePaths = @{}
foreach ($rootInput in $sourceRoots) {
    $root = (Resolve-Path $rootInput).Path
    foreach ($file in Get-ChildItem -LiteralPath $root -Recurse -File -Filter '*.json') {
        $relative = $file.FullName.Substring($root.Length).TrimStart([char]'\').Replace('\', '/')
        $area = $relative.Split('/')[0]
        if ($includedAreas -contains $area) {
            $candidates += [pscustomobject]@{ File = $file; Relative = $relative; Root = $root }
        } else {
            $excludedCounts[$area] = 1 + [int]$excludedCounts[$area]
            if (-not $excludedSourcePaths.ContainsKey($area)) { $excludedSourcePaths[$area] = @() }
            $excludedPath = $file.FullName.Substring($referenceRoot.Length).TrimStart('\', '/').Replace('\', '/')
            $excludedSourcePaths[$area] += $excludedPath
        }
    }
}
$candidates = @($candidates | Sort-Object Relative)
if ($candidates.Count -ne 347) { throw "Expected 347 native server-data files, found $($candidates.Count)." }

$targets = @{}
$entries = @()
$previousManagedTargets = @()
if ($Mode -eq 'Generate' -and (Test-Path -LiteralPath $ledgerPath -PathType Leaf)) {
    $previousLedger = Get-Content -Raw -LiteralPath $ledgerPath | ConvertFrom-Json
    $previousManagedTargets = @($previousLedger.entries | ForEach-Object { [string]$_.targetPath })
}
foreach ($candidate in $candidates) {
    $targetRelative = Convert-PortPath $candidate.Relative
    if ($targets.ContainsKey($targetRelative)) { throw "Server-data path collision: $targetRelative" }
    $targets[$targetRelative] = $true
    $sourceObject = Get-Content -Raw -LiteralPath $candidate.File.FullName | ConvertFrom-Json
    $converted = Convert-PortNode $sourceObject
    $serialized = ($converted | ConvertTo-Json -Depth 100) + [Environment]::NewLine
    $targetPath = Join-Path $targetRoot $targetRelative.Replace('/', '\')
    Compare-Or-Write $targetPath $serialized
    $entries += [ordered]@{
        sourcePath = $candidate.File.FullName.Substring($referenceRoot.Length).TrimStart('\', '/').Replace('\', '/')
        targetPath = 'src/main/resources/data/elarion_angling/' + $targetRelative
        area = $targetRelative.Split('/')[0]
        targetSha256 = Get-Hash ([System.Text.Encoding]::UTF8.GetBytes($serialized))
        disposition = 'ported'
    }
}

if ($Mode -eq 'Generate') {
    $managedPrefix = 'src/main/resources/data/elarion_angling/'
    foreach ($previousTarget in $previousManagedTargets) {
        if (-not $previousTarget.StartsWith($managedPrefix, [System.StringComparison]::Ordinal)) {
            throw "Previous server-data ledger target escaped its managed root: $previousTarget"
        }
        $relative = $previousTarget.Substring($managedPrefix.Length)
        if (-not $targets.ContainsKey($relative)) {
            $stalePath = Join-Path $targetRoot $relative.Replace('/', '\')
            if (Test-Path -LiteralPath $stalePath -PathType Leaf) {
                Remove-Item -LiteralPath $stalePath -Force
            }
        }
    }
}

$areaCounts = [ordered]@{}
foreach ($area in @($entries | ForEach-Object { $_['area'] } | Sort-Object -Unique)) {
    $areaCounts[$area] = @($entries | Where-Object { $_['area'] -eq $area }).Count
}
$excluded = @()
foreach ($area in $excludedAreas.Keys) {
    $excluded += [ordered]@{
        area = $area
        count = [int]$excludedCounts[$area]
        disposition = $excludedAreas[$area]
        sourcePaths = @($excludedSourcePaths[$area] | Sort-Object)
    }
}
$ledger = [ordered]@{
    schemaVersion = 1
    sourceRevision = $identity.sourceRevision
    summary = [ordered]@{ files = $entries.Count; areas = $areaCounts }
    excluded = $excluded
    entries = $entries
}
$ledgerJson = ($ledger | ConvertTo-Json -Depth 10) + [Environment]::NewLine
if ($Mode -eq 'Verify') {
    if (-not (Test-Path -LiteralPath $ledgerPath -PathType Leaf) -or
        (Get-Content -Raw -LiteralPath $ledgerPath).TrimEnd() -cne $ledgerJson.TrimEnd()) {
        throw "Native server-data ledger is missing or stale: $ledgerPath"
    }
} else {
    New-Item -ItemType Directory -Path (Split-Path -Parent $ledgerPath) -Force | Out-Null
    [System.IO.File]::WriteAllText($ledgerPath, $ledgerJson, [System.Text.UTF8Encoding]::new($false))
}
Write-Host "$Mode complete: 347 native server-data files; excluded areas remain explicitly ledgered."
