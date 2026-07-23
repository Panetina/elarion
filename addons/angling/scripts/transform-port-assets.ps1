[CmdletBinding()]
param(
    [ValidateSet('Generate', 'Verify')]
    [string]$Mode = 'Generate'
)

$ErrorActionPreference = 'Stop'

$moduleRoot = Split-Path -Parent $PSScriptRoot
$referenceRoot = Join-Path $moduleRoot 'reference\upstream-starcatcher-neoforge-1.21.1'
$sourceRoots = @(
    (Join-Path $referenceRoot 'src\main\resources\assets\starcatcher')
    (Join-Path $referenceRoot 'src\generated\resources\assets\starcatcher')
)
$targetRoot = Join-Path $moduleRoot 'src\main\resources\assets\elarion_angling'
$identityPath = Join-Path $moduleRoot 'porting\fish-identity.json'
$ledgerPath = Join-Path $moduleRoot 'porting\ledger\assets.json'
$runtimeExtensions = @('.json', '.png', '.ogg', '.mcmeta', '.fsh', '.vsh')
$textExtensions = @('.json', '.mcmeta', '.fsh', '.vsh')

$identity = Get-Content -Raw -LiteralPath $identityPath | ConvertFrom-Json
$renames = @{}
foreach ($property in $identity.renames.PSObject.Properties) {
    $renames[$property.Name] = [string]$property.Value
}
$orderedRenameKeys = @($renames.Keys | Sort-Object { $_.Length } -Descending)
$fishTokenPattern = '(?<![a-z0-9_])(?:' + (($orderedRenameKeys | ForEach-Object {
    [System.Text.RegularExpressions.Regex]::Escape($_)
}) -join '|') + ')(?![a-z0-9_])'

function Convert-FishIdTokens {
    param([string]$Value)
    return [System.Text.RegularExpressions.Regex]::Replace(
        $Value,
        $fishTokenPattern,
        { param($match) [string]$renames[$match.Value] })
}

function Convert-PortText {
    param(
        [string]$Value,
        [string]$RelativePath
    )
    $result = Convert-FishIdTokens $Value
    $result = $result.Replace('StarcatcherCompatible™', 'Elarion Angling-compatible')
    $result = $result.Replace('StarcatcherCompatible™', 'Elarion Angling-compatible')
    $result = $result.Replace("Starcatcher's", "Elarion Angling's")
    $result = $result.Replace('Starcatcher', 'Elarion Angling')
    $result = $result.Replace('Elarion AnglingCompatible™', 'Elarion Angling-compatible')
    $result = [System.Text.RegularExpressions.Regex]::Replace(
        $result,
        'Elarion AnglingCompatible[^\x00-\x7F]+',
        'Elarion Angling-compatible')
    $result = $result.Replace('STARCATCHER', 'ELARION ANGLING')
    $result = $result.Replace('starcatcher', 'elarion_angling')

    if ($RelativePath -eq 'lang/en_us.json') {
        $textInfo = [System.Globalization.CultureInfo]::InvariantCulture.TextInfo
        $sourceLanguage = $Value | ConvertFrom-Json
        $displayRenames = @{}
        foreach ($sourceId in $orderedRenameKeys) {
            $sourceKey = 'item.starcatcher.' + $sourceId
            if ($null -eq $sourceLanguage.PSObject.Properties[$sourceKey]) {
                $sourceKey = 'block.starcatcher.' + $sourceId
            }
            if ($null -ne $sourceLanguage.PSObject.Properties[$sourceKey]) {
                $sourceDisplay = [string]$sourceLanguage.PSObject.Properties[$sourceKey].Value
                $targetDisplay = $textInfo.ToTitleCase($renames[$sourceId].Replace('_', ' '))
                if ($displayRenames.ContainsKey($sourceDisplay) -and
                        $displayRenames[$sourceDisplay] -cne $targetDisplay) {
                    throw "Ambiguous Angling display-name transformation: $sourceDisplay"
                }
                $displayRenames[$sourceDisplay] = $targetDisplay
            }
        }
        $orderedDisplays = @($displayRenames.Keys | Sort-Object { $_.Length } -Descending)
        $displayPattern = '(?<![A-Za-z])(?:' + (($orderedDisplays | ForEach-Object {
            [System.Text.RegularExpressions.Regex]::Escape($_)
        }) -join '|') + ')(?![A-Za-z])'
        $result = [System.Text.RegularExpressions.Regex]::Replace(
            $result,
            $displayPattern,
            { param($match) [string]$displayRenames[$match.Value] })
        $missingTranslations = @()
        foreach ($targetId in @($renames.Values | Sort-Object -Unique)) {
            $displayName = $textInfo.ToTitleCase($targetId.Replace('_', ' '))
            $escapedTarget = [System.Text.RegularExpressions.Regex]::Escape($targetId)
            $itemPattern = '("item\.elarion_angling\.' + $escapedTarget + '"\s*:\s*)"[^"]*"'
            $blockPattern = '("block\.elarion_angling\.' + $escapedTarget + '"\s*:\s*)"[^"]*"'
            if ([System.Text.RegularExpressions.Regex]::IsMatch($result, $itemPattern)) {
                $result = [System.Text.RegularExpressions.Regex]::Replace(
                    $result,
                    $itemPattern,
                    { param($match) $match.Groups[1].Value + '"' + $displayName + '"' })
            } elseif ([System.Text.RegularExpressions.Regex]::IsMatch($result, $blockPattern)) {
                $result = [System.Text.RegularExpressions.Regex]::Replace(
                    $result,
                    $blockPattern,
                    { param($match) $match.Groups[1].Value + '"' + $displayName + '"' })
            } else {
                $missingTranslations += '  "item.elarion_angling.' + $targetId + '": "' + $displayName + '"'
            }
        }
        if ($missingTranslations.Count -gt 0) {
            $result = [System.Text.RegularExpressions.Regex]::Replace(
                $result,
                '\r?\n}\s*$',
                ',' + [Environment]::NewLine + ($missingTranslations -join (',' + [Environment]::NewLine)) +
                    [Environment]::NewLine + '}')
        }
    }
    return [System.Text.RegularExpressions.Regex]::Replace(
        $result,
        '[\t ]+(?=\r?$)',
        '',
        [System.Text.RegularExpressions.RegexOptions]::Multiline)
}

function Convert-PortPath {
    param([string]$Value)
    $result = Convert-FishIdTokens $Value
    return $result.Replace('starcatcher', 'elarion_angling')
}

function Get-Sha256Bytes {
    param([byte[]]$Bytes)
    $sha = [System.Security.Cryptography.SHA256]::Create()
    try {
        return ([BitConverter]::ToString($sha.ComputeHash($Bytes))).Replace('-', '').ToLowerInvariant()
    } finally {
        $sha.Dispose()
    }
}

function Compare-Or-WriteBytes {
    param([string]$Path, [byte[]]$Expected)
    if ($Mode -eq 'Verify') {
        if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) {
            throw "Transformed Angling asset is missing: $Path"
        }
        $actual = [System.IO.File]::ReadAllBytes($Path)
        if ((Get-Sha256Bytes $actual) -ne (Get-Sha256Bytes $Expected)) {
            throw "Transformed Angling asset is stale: $Path"
        }
        return
    }
    New-Item -ItemType Directory -Path (Split-Path -Parent $Path) -Force | Out-Null
    [System.IO.File]::WriteAllBytes($Path, $Expected)
}

$sourceFiles = @()
foreach ($root in $sourceRoots) {
    if (-not (Test-Path -LiteralPath $root -PathType Container)) {
        throw "Authorized Angling asset root is missing: $root"
    }
    $sourceFiles += Get-ChildItem -LiteralPath $root -Recurse -File |
        Where-Object { $runtimeExtensions -contains $_.Extension.ToLowerInvariant() } |
        ForEach-Object {
            [pscustomobject]@{
                File = $_
                Root = $root
                Relative = $_.FullName.Substring($root.Length).TrimStart('\', '/').Replace('\', '/')
            }
        }
}
$sourceFiles = @($sourceFiles | Sort-Object Relative)
if ($sourceFiles.Count -ne 1010) {
    throw "Expected 1,010 runtime Angling assets, found $($sourceFiles.Count)."
}

$targetPaths = @{}
$entries = @()
$previousManagedTargets = @()
if ($Mode -eq 'Generate' -and (Test-Path -LiteralPath $ledgerPath -PathType Leaf)) {
    $previousLedger = Get-Content -Raw -LiteralPath $ledgerPath | ConvertFrom-Json
    $previousManagedTargets = @($previousLedger.entries | ForEach-Object { [string]$_.targetPath })
}
foreach ($source in $sourceFiles) {
    $targetRelative = Convert-PortPath $source.Relative
    if ($targetPaths.ContainsKey($targetRelative)) {
        throw "Asset identity transformation collision: $targetRelative"
    }
    $targetPaths[$targetRelative] = $true
    $extension = $source.File.Extension.ToLowerInvariant()
    $sourceBytes = [System.IO.File]::ReadAllBytes($source.File.FullName)
    $binaryUnchanged = -not ($textExtensions -contains $extension)
    $targetBytes = if ($binaryUnchanged) {
        $sourceBytes
    } else {
        $text = [System.Text.Encoding]::UTF8.GetString($sourceBytes)
        [System.Text.UTF8Encoding]::new($false).GetBytes((Convert-PortText -Value $text -RelativePath $targetRelative))
    }
    $targetPath = Join-Path $targetRoot $targetRelative.Replace('/', '\')
    Compare-Or-WriteBytes $targetPath $targetBytes
    $sourcePrefix = if ($source.Root.Contains('generated')) { 'src/generated/resources/assets/starcatcher/' }
        else { 'src/main/resources/assets/starcatcher/' }
    $entries += [ordered]@{
        sourcePath = $sourcePrefix + $source.Relative
        targetPath = 'src/main/resources/assets/elarion_angling/' + $targetRelative
        kind = $extension.TrimStart('.')
        sizeBytes = $targetBytes.Length
        sourceSha256 = Get-Sha256Bytes $sourceBytes
        targetSha256 = Get-Sha256Bytes $targetBytes
        binaryUnchanged = $binaryUnchanged
        disposition = 'ported'
    }
}

if ($Mode -eq 'Generate') {
    $managedPrefix = 'src/main/resources/assets/elarion_angling/'
    foreach ($previousTarget in $previousManagedTargets) {
        if (-not $previousTarget.StartsWith($managedPrefix, [System.StringComparison]::Ordinal)) {
            throw "Previous asset ledger target escaped its managed root: $previousTarget"
        }
        $relative = $previousTarget.Substring($managedPrefix.Length)
        if (-not $targetPaths.ContainsKey($relative)) {
            $stalePath = Join-Path $targetRoot $relative.Replace('/', '\')
            if (Test-Path -LiteralPath $stalePath -PathType Leaf) {
                Remove-Item -LiteralPath $stalePath -Force
            }
        }
    }
}

$kindCounts = [ordered]@{}
foreach ($kind in @($entries.kind | Sort-Object -Unique)) {
    $kindCounts[$kind] = @($entries | Where-Object { $_.kind -eq $kind }).Count
}
$ledger = [ordered]@{
    schemaVersion = 1
    sourceRevision = $identity.sourceRevision
    summary = [ordered]@{
        files = $entries.Count
        kinds = $kindCounts
        binaryFilesUnchanged = @($entries | Where-Object { $_.binaryUnchanged }).Count
        transformedTextFiles = @($entries | Where-Object { -not $_.binaryUnchanged }).Count
    }
    excludedSourceFormats = @(
        [ordered]@{
            extension = 'ase'
            count = 2
            reason = 'Aseprite authoring sources are not Minecraft runtime assets and are not packaged.'
        }
    )
    entries = $entries
}
$ledgerJson = ($ledger | ConvertTo-Json -Depth 10) + [Environment]::NewLine
if ($Mode -eq 'Verify') {
    if (-not (Test-Path -LiteralPath $ledgerPath -PathType Leaf)) {
        throw "Angling asset ledger is missing: $ledgerPath"
    }
    if ((Get-Content -Raw -LiteralPath $ledgerPath).TrimEnd() -cne $ledgerJson.TrimEnd()) {
        throw "Angling asset ledger is stale: $ledgerPath"
    }
} else {
    New-Item -ItemType Directory -Path (Split-Path -Parent $ledgerPath) -Force | Out-Null
    [System.IO.File]::WriteAllText($ledgerPath, $ledgerJson, [System.Text.UTF8Encoding]::new($false))
}

Write-Host "$Mode complete: $($entries.Count) Angling runtime assets; $(@($entries | Where-Object binaryUnchanged).Count) binaries unchanged."
