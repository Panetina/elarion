[CmdletBinding()]
param(
    [ValidateSet('Generate', 'Verify')]
    [string]$Mode = 'Generate'
)

$ErrorActionPreference = 'Stop'
$moduleRoot = Split-Path -Parent $PSScriptRoot
$referenceRoot = Join-Path $moduleRoot 'reference\upstream-starcatcher-neoforge-1.21.1'
$sourceRoot = Join-Path $referenceRoot 'src\generated\resources\data\starcatcher\data_maps'
$targetRoot = Join-Path $moduleRoot 'src\main\resources\data\elarion_angling\elarion_angling'
$identityPath = Join-Path $moduleRoot 'porting\fish-identity.json'
$ledgerPath = Join-Path $moduleRoot 'porting\ledger\domain-maps.json'

$specs = @(
    [ordered]@{ Source = 'item/aquarium_interaction.json'; Target = 'aquarium/interactions.json'; Domain = 'aquarium-interactions' }
    [ordered]@{ Source = 'item/modifiers.json'; Target = 'equipment/item_modifiers.json'; Domain = 'item-modifiers' }
    [ordered]@{ Source = 'item/tackle_skin.json'; Target = 'equipment/tackle_skins.json'; Domain = 'tackle-skins' }
    [ordered]@{ Source = 'mob_effect/modifiers.json'; Target = 'equipment/effect_modifiers.json'; Domain = 'effect-modifiers' }
    [ordered]@{ Source = 'starcatcher/fish/treasures.json'; Target = 'treasure/by_catch.json'; Domain = 'catch-treasures' }
)

$identity = Get-Content -Raw -LiteralPath $identityPath | ConvertFrom-Json
if ($identity.sourceRevision -ne '016161dfc2d556d20fa641cd275e18c539256d4d') {
    throw 'Fish identity contract does not match the frozen source revision.'
}
$renames = @{}
foreach ($property in $identity.renames.PSObject.Properties) {
    $renames[$property.Name] = [string]$property.Value
}
$orderedRenameKeys = @($renames.Keys | Sort-Object { $_.Length } -Descending)

function Convert-PortString {
    param([string]$Value)
    $result = $Value
    foreach ($sourceId in $orderedRenameKeys) {
        $result = $result.Replace("starcatcher:$sourceId", "elarion_angling:$($renames[$sourceId])")
    }
    $result = $result.Replace('starcatcher:', 'elarion_angling:')
    $result = $result.Replace('.starcatcher.', '.elarion_angling.')
    return $result.Replace('/starcatcher/', '/elarion_angling/')
}

function Convert-PortNode {
    param([object]$Node)
    if ($null -eq $Node) { return $null }
    if ($Node -is [string]) { return Convert-PortString $Node }
    if ($Node -is [System.Management.Automation.PSCustomObject] -or $Node -is [System.Collections.IDictionary]) {
        $properties = if ($Node -is [System.Management.Automation.PSCustomObject]) { $Node.PSObject.Properties }
            else { $Node.Keys | ForEach-Object { [pscustomobject]@{ Name = [string]$_; Value = $Node[$_] } } }
        $converted = [ordered]@{}
        foreach ($property in $properties) {
            $name = Convert-PortString ([string]$property.Name)
            if ($converted.Contains($name)) { throw "Domain-map transformation collision: $name" }
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

function Get-Sha256Text {
    param([string]$Text)
    $sha = [System.Security.Cryptography.SHA256]::Create()
    try {
        $bytes = [System.Text.Encoding]::UTF8.GetBytes($Text)
        return ([BitConverter]::ToString($sha.ComputeHash($bytes))).Replace('-', '').ToLowerInvariant()
    } finally { $sha.Dispose() }
}

function Compare-Or-Write {
    param([string]$Path, [string]$Expected)
    if ($Mode -eq 'Verify') {
        if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) { throw "Domain map is missing: $Path" }
        if ((Get-Content -Raw -LiteralPath $Path).TrimEnd() -cne $Expected.TrimEnd()) {
            throw "Domain map is stale: $Path"
        }
        return
    }
    New-Item -ItemType Directory -Path (Split-Path -Parent $Path) -Force | Out-Null
    [System.IO.File]::WriteAllText($Path, $Expected, [System.Text.UTF8Encoding]::new($false))
}

$entries = @()
foreach ($spec in $specs) {
    $sourcePath = Join-Path $sourceRoot $spec.Source.Replace('/', '\')
    if (-not (Test-Path -LiteralPath $sourcePath -PathType Leaf)) { throw "Domain-map source is missing: $sourcePath" }
    $sourceText = Get-Content -Raw -LiteralPath $sourcePath
    $sourceJson = $sourceText | ConvertFrom-Json
    $converted = Convert-PortNode $sourceJson
    $convertedValues = $converted['values']
    $output = [ordered]@{ schema_version = 1; values = $convertedValues }
    $targetText = ($output | ConvertTo-Json -Depth 100) + [Environment]::NewLine
    $targetPath = Join-Path $targetRoot $spec.Target.Replace('/', '\')
    Compare-Or-Write $targetPath $targetText
    $entries += [ordered]@{
        sourcePath = 'src/generated/resources/data/starcatcher/data_maps/' + $spec.Source
        targetPath = 'src/main/resources/data/elarion_angling/elarion_angling/' + $spec.Target
        domain = $spec.Domain
        valueCount = if ($convertedValues -is [System.Collections.IDictionary]) {
            $convertedValues.Count
        } else {
            ($convertedValues.PSObject.Properties | Measure-Object).Count
        }
        sourceSha256 = Get-Sha256Text $sourceText
        targetSha256 = Get-Sha256Text $targetText
        disposition = 'ported'
    }
}

$ledger = [ordered]@{
    schemaVersion = 1
    sourceRevision = $identity.sourceRevision
    summary = [ordered]@{ files = $entries.Count; values = [int](($entries.valueCount | Measure-Object -Sum).Sum) }
    entries = $entries
}
$ledgerText = ($ledger | ConvertTo-Json -Depth 10) + [Environment]::NewLine
Compare-Or-Write $ledgerPath $ledgerText
Write-Host "$Mode complete: $($entries.Count) domain maps with $($ledger.summary.values) explicit values."
