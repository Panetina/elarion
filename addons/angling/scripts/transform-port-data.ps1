[CmdletBinding()]
param(
    [ValidateSet('Generate', 'Verify')]
    [string]$Mode = 'Generate'
)

$ErrorActionPreference = 'Stop'

$moduleRoot = Split-Path -Parent $PSScriptRoot
$referenceRoot = Join-Path $moduleRoot 'reference\upstream-starcatcher-neoforge-1.21.1'
$generatedDataRoot = Join-Path $referenceRoot 'src\generated\resources\data'
$nativeSourceRoot = Join-Path $generatedDataRoot 'starcatcher\starcatcher\fish'
$targetRoot = Join-Path $moduleRoot 'src\main\resources\data\elarion_angling\elarion_angling\fish'
$identityPath = Join-Path $moduleRoot 'porting\fish-identity.json'
$ledgerRoot = Join-Path $moduleRoot 'porting\ledger'
$nativeLedgerPath = Join-Path $ledgerRoot 'native-catches.json'
$schemaLedgerPath = Join-Path $ledgerRoot 'catch-schema.json'
$runtimeLedgerPath = Join-Path $ledgerRoot 'runtime-types-and-packets.json'

if (-not (Test-Path -LiteralPath $nativeSourceRoot -PathType Container)) {
    throw "Authorized Angling catch source is missing: $nativeSourceRoot"
}

$identity = Get-Content -Raw -LiteralPath $identityPath | ConvertFrom-Json
if ($identity.schemaVersion -ne 1 -or
    $identity.sourceRevision -ne '016161dfc2d556d20fa641cd275e18c539256d4d') {
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
    $result = $result.Replace('/starcatcher/', '/elarion_angling/')
    return $result
}

function Convert-PortNode {
    param([object]$Node)

    if ($null -eq $Node) { return $null }
    if ($Node -is [string]) { return Convert-PortString $Node }
    if ($Node -is [System.Management.Automation.PSCustomObject]) {
        $converted = [ordered]@{}
        foreach ($property in $Node.PSObject.Properties) {
            if ($property.Name -eq 'neoforge:conditions') { continue }
            $convertedName = Convert-PortString $property.Name
            if ($converted.Contains($convertedName)) {
                throw "Port transformation produced a duplicate JSON key: $convertedName"
            }
            $converted[$convertedName] = Convert-PortNode $property.Value
        }
        return $converted
    }
    if ($Node -is [System.Collections.IDictionary]) {
        $converted = [ordered]@{}
        foreach ($key in $Node.Keys) {
            $convertedName = Convert-PortString ([string]$key)
            if ($converted.Contains($convertedName)) {
                throw "Port transformation produced a duplicate JSON key: $convertedName"
            }
            $converted[$convertedName] = Convert-PortNode $Node[$key]
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
    } finally {
        $sha.Dispose()
    }
}

function Get-RelativeReferencePath {
    param([System.IO.FileInfo]$File)

    $File.FullName.Substring($referenceRoot.Length).TrimStart('\', '/').Replace('\', '/')
}

function Get-NodeTypes {
    param([object[]]$Nodes)

    @($Nodes | Where-Object { $null -ne $_ -and $null -ne $_.type } |
        ForEach-Object { [string]$_.type } | Sort-Object -Unique)
}

function Get-ModifierTypes {
    param([object]$Definition)

    $types = @()
    if ($null -ne $Definition.difficulty.modifiers) {
        $types += Get-NodeTypes @($Definition.difficulty.modifiers)
    }
    foreach ($spot in @($Definition.difficulty.sweetspots)) {
        if ($null -ne $spot.add_modifiers_on_hit) {
            $types += Get-NodeTypes @($spot.add_modifiers_on_hit)
        }
    }
    @($types | Sort-Object -Unique)
}

function Get-ConditionMods {
    param([object]$Definition)

    @($Definition.'neoforge:conditions' | Where-Object { $null -ne $_.modid } |
        ForEach-Object { [string]$_.modid } | Sort-Object -Unique)
}

function ConvertTo-StableJson {
    param([object]$Value)
    ($Value | ConvertTo-Json -Depth 100) + [Environment]::NewLine
}

function Compare-Or-Write {
    param(
        [string]$Path,
        [string]$Expected
    )

    if ($Mode -eq 'Verify') {
        if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) {
            throw "Generated Angling port artifact is missing: $Path"
        }
        $actual = Get-Content -Raw -LiteralPath $Path
        if ($actual.TrimEnd() -cne $Expected.TrimEnd()) {
            throw "Generated Angling port artifact is stale: $Path"
        }
        return
    }
    $parent = Split-Path -Parent $Path
    New-Item -ItemType Directory -Path $parent -Force | Out-Null
    [System.IO.File]::WriteAllText($Path, $Expected, [System.Text.UTF8Encoding]::new($false))
}

$nativeEntries = @()
$expectedTargetNames = @()
$nativeFiles = @(Get-ChildItem -LiteralPath $nativeSourceRoot -File -Filter '*.json' |
    Sort-Object Name)
foreach ($sourceFile in $nativeFiles) {
    $sourceId = $sourceFile.BaseName
    $targetId = if ($renames.ContainsKey($sourceId)) { $renames[$sourceId] } else { $sourceId }
    $expectedTargetNames += "$targetId.json"
    $sourceObject = Get-Content -Raw -LiteralPath $sourceFile.FullName | ConvertFrom-Json
    $convertedBody = Convert-PortNode $sourceObject
    $converted = [ordered]@{ schema_version = 1 }
    foreach ($key in $convertedBody.Keys) { $converted[$key] = $convertedBody[$key] }
    $serialized = ConvertTo-StableJson $converted
    $targetPath = Join-Path $targetRoot "$targetId.json"
    Compare-Or-Write $targetPath $serialized

    $catchType = if ($null -ne $sourceObject.catch_info.type) {
        [string]$sourceObject.catch_info.type
    } elseif ($sourceId.StartsWith('secret_')) {
        'secret'
    } elseif ($sourceId.StartsWith('trophy_')) {
        'trophy'
    } elseif ($sourceId.StartsWith('extra_')) {
        'extra'
    } else {
        'fish'
    }
    $nativeEntries += [ordered]@{
        sourceId = "starcatcher:$sourceId"
        targetId = "elarion_angling:$targetId"
        sourcePath = Get-RelativeReferencePath $sourceFile
        targetPath = "src/main/resources/data/elarion_angling/elarion_angling/fish/$targetId.json"
        catchType = $catchType
        rarity = [string]$sourceObject.rarity
        restrictionTypes = @(Get-NodeTypes @($sourceObject.restrictions))
        modifierTypes = @(Get-ModifierTypes $sourceObject)
        sweetspotTypes = @($sourceObject.difficulty.sweetspots |
            ForEach-Object { [string]$_.sweetspot_type } | Sort-Object -Unique)
        transformedSha256 = Get-Sha256Text $serialized
        disposition = 'ported'
    }
}

if ($nativeEntries.Count -ne 148 -or @($nativeEntries.targetId | Sort-Object -Unique).Count -ne 148) {
    throw 'Native catch transformation must produce exactly 148 unique target IDs.'
}
if (Test-Path -LiteralPath $targetRoot -PathType Container) {
    $actualTargetNames = @(Get-ChildItem -LiteralPath $targetRoot -File -Filter '*.json' |
        ForEach-Object { $_.Name } | Sort-Object)
    $expectedNames = @($expectedTargetNames | Sort-Object)
    if (Compare-Object $expectedNames $actualTargetNames) {
        throw 'Transformed native catch directory contains missing or stale files.'
    }
}

$nativeLedger = [ordered]@{
    schemaVersion = 1
    sourceRevision = $identity.sourceRevision
    count = $nativeEntries.Count
    identityMappingCount = $renames.Count
    entries = $nativeEntries
}
Compare-Or-Write $nativeLedgerPath (ConvertTo-StableJson $nativeLedger)

$allDefinitionEntries = @()
$allDefinitionFiles = @(Get-ChildItem -LiteralPath $generatedDataRoot -Recurse -File -Filter '*.json' |
    Where-Object {
        $_.FullName.Replace('\', '/').Contains('/starcatcher/fish/') -and
        (Get-Content -Raw -LiteralPath $_.FullName).Contains('"catch_info"')
    } | Sort-Object FullName)
foreach ($file in $allDefinitionFiles) {
    $definition = Get-Content -Raw -LiteralPath $file.FullName | ConvertFrom-Json
    $relativeData = $file.FullName.Substring($generatedDataRoot.Length).TrimStart('\', '/').Replace('\', '/')
    $originNamespace = $relativeData.Split('/')[0]
    $isNative = $file.DirectoryName -eq $nativeSourceRoot
    $allDefinitionEntries += [ordered]@{
        sourcePath = Get-RelativeReferencePath $file
        sourceId = "$originNamespace`:$($file.BaseName)"
        originNamespace = $originNamespace
        native = $isNative
        rarity = [string]$definition.rarity
        restrictionTypes = @(Get-NodeTypes @($definition.restrictions))
        modifierTypes = @(Get-ModifierTypes $definition)
        sweetspotTypes = @($definition.difficulty.sweetspots |
            ForEach-Object { [string]$_.sweetspot_type } | Sort-Object -Unique)
        requiredMods = @(Get-ConditionMods $definition)
        disposition = if ($isNative) { 'ported' } else { 'pending' }
    }
}
if ($allDefinitionEntries.Count -ne 463) {
    throw "Semantic catch ledger expected 463 definitions, found $($allDefinitionEntries.Count)."
}

function Get-TypeUsage {
    param([string]$Property)
    $values = @($allDefinitionEntries | ForEach-Object { @($_[$Property]) })
    @($values | Group-Object | Sort-Object Name | ForEach-Object {
        [ordered]@{ id = $_.Name; definitionCount = $_.Count }
    })
}

$schemaLedger = [ordered]@{
    schemaVersion = 1
    sourceRevision = $identity.sourceRevision
    summary = [ordered]@{
        totalDefinitions = $allDefinitionEntries.Count
        nativeDefinitions = @($allDefinitionEntries | Where-Object { $_.native }).Count
        compatibilityDefinitions = @($allDefinitionEntries | Where-Object { -not $_.native }).Count
    }
    restrictionTypes = @(Get-TypeUsage 'restrictionTypes')
    modifierTypes = @(Get-TypeUsage 'modifierTypes')
    sweetspotTypes = @(Get-TypeUsage 'sweetspotTypes')
    definitions = $allDefinitionEntries
}
Compare-Or-Write $schemaLedgerPath (ConvertTo-StableJson $schemaLedger)

$restrictionSource = Join-Path $referenceRoot 'src\main\java\com\wdiscute\starcatcher\registry\fishrestrictions\SCFishRestrictions.java'
$modifierSource = Join-Path $referenceRoot 'src\main\java\com\wdiscute\starcatcher\modifiers\Modifier.java'
$sweetspotSource = Join-Path $referenceRoot 'src\main\java\com\wdiscute\starcatcher\registry\sweetspotbehaviour\SCSweetSpotsBehaviour.java'
$restrictionIds = @([regex]::Matches((Get-Content -Raw $restrictionSource), 'registerFishRestriction\("([^"]+)"') |
    ForEach-Object { $_.Groups[1].Value } | Sort-Object -Unique)
$modifierIds = @([regex]::Matches((Get-Content -Raw $modifierSource), 'Modifier\.MODIFIERS\.put\(Starcatcher\.rl\("([^"]+)"\)') |
    ForEach-Object { $_.Groups[1].Value } | Sort-Object -Unique)
$sweetspotIds = @([regex]::Matches((Get-Content -Raw $sweetspotSource), 'registerSweetspot\("([^"]+)"') |
    ForEach-Object { $_.Groups[1].Value } | Sort-Object -Unique)

function New-RuntimeEntries {
    param([string[]]$Ids, [string]$Kind, [string]$SourcePath)
    @($Ids | ForEach-Object {
        [ordered]@{
            id = "elarion_angling:$_"
            kind = $Kind
            sourcePath = $SourcePath
            disposition = 'pending'
        }
    })
}

$networkRoot = Join-Path $referenceRoot 'src\main\java\com\wdiscute\starcatcher\io\network'
$packetEntries = @(Get-ChildItem -LiteralPath $networkRoot -Recurse -File -Filter '*Payload.java' |
    Sort-Object FullName | ForEach-Object {
        $direction = if ($_.BaseName.StartsWith('CB')) { 'clientbound' }
            elseif ($_.BaseName.StartsWith('SB')) { 'serverbound' }
            else { 'serverbound' }
        [ordered]@{
            sourceClass = $_.BaseName
            sourcePath = Get-RelativeReferencePath $_
            direction = $direction
            disposition = 'pending'
        }
    })

$runtimeLedger = [ordered]@{
    schemaVersion = 1
    sourceRevision = $identity.sourceRevision
    summary = [ordered]@{
        restrictions = $restrictionIds.Count
        modifiers = $modifierIds.Count
        sweetspotBehaviors = $sweetspotIds.Count
        packets = $packetEntries.Count
    }
    restrictions = @(New-RuntimeEntries $restrictionIds 'restriction' 'SCFishRestrictions.java')
    modifiers = @(New-RuntimeEntries $modifierIds 'modifier' 'Modifier.java')
    sweetspotBehaviors = @(New-RuntimeEntries $sweetspotIds 'sweetspot-behavior' 'SCSweetSpotsBehaviour.java')
    packets = $packetEntries
}
Compare-Or-Write $runtimeLedgerPath (ConvertTo-StableJson $runtimeLedger)

Write-Host "$Mode complete: 148 native catches, 463 total catch definitions, $($packetEntries.Count) packets."
