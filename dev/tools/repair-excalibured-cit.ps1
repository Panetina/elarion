param(
    [Parameter(Mandatory = $true)]
    [string] $PackPath
)

$ErrorActionPreference = 'Stop'

Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem

$pack = (Resolve-Path -LiteralPath $PackPath).Path
if ([System.IO.Path]::GetFileName($pack) -ne 'Elarion Excalibured v1.zip') {
    throw "Refusing to rewrite unexpected resource pack: $pack"
}

$temporary = "$pack.cit-repair.tmp"
if (Test-Path -LiteralPath $temporary) {
    Remove-Item -LiteralPath $temporary -Force
}
Copy-Item -LiteralPath $pack -Destination $temporary

$removedEntries = [System.Collections.Generic.HashSet[string]]::new(
    [System.StringComparer]::Ordinal)
@(
    'assets/minecraft/optifine/cit/custom/armor/hide_boots_overlay_model.properties',
    'assets/minecraft/optifine/cit/custom/armor/hide_chestplate_overlay_model.properties',
    'assets/minecraft/optifine/cit/custom/armor/hide_helmet_overlay_model.properties',
    'assets/minecraft/optifine/cit/custom/armor/hide_leggings_overlay_model.properties',
    'cit_1.21.4/assets/minecraft/optifine/cit/46_vanilla/firework_rocket/rocket_flight_1.properties',
    'cit_1.21.4/assets/minecraft/optifine/cit/46_vanilla/suspicious_stew/glowing.properties'
) | ForEach-Object { $removedEntries.Add($_) | Out-Null }

$customPrefix = 'assets/minecraft/optifine/cit/custom/'
$fireworkPrefix = 'assets/minecraft/optifine/cit/vanilla/firework_rocket/'
$paintingPrefix = 'assets/minecraft/optifine/cit/vanilla/painting/'
$stewPrefix = 'assets/minecraft/optifine/cit/vanilla/suspicious_stew/'
$utf8 = [System.Text.UTF8Encoding]::new($false)
$migrated = 0
$removed = 0
$modelsRepaired = 0

$archive = [System.IO.Compression.ZipFile]::Open(
    $temporary,
    [System.IO.Compression.ZipArchiveMode]::Update)
try {
    foreach ($entry in @($archive.Entries)) {
        if ($removedEntries.Contains($entry.FullName)) {
            $entry.Delete()
            $removed++
            continue
        }
        if (-not $entry.FullName.EndsWith('.properties', [System.StringComparison]::Ordinal)) {
            continue
        }

        $reader = [System.IO.StreamReader]::new($entry.Open())
        try {
            $content = $reader.ReadToEnd()
        } finally {
            $reader.Dispose()
        }

        $updated = $content
        if ($entry.FullName.StartsWith($customPrefix, [System.StringComparison]::Ordinal)) {
            $updated = [regex]::Replace(
                $updated,
                '(?m)^nbt\.display\.Name=',
                'components.custom_name=')
        } elseif ($entry.FullName.StartsWith($fireworkPrefix, [System.StringComparison]::Ordinal)) {
            $updated = [regex]::Replace(
                $updated,
                '(?m)^nbt\.Fireworks\.Flight=',
                'components.fireworks.flight_duration=')
        } elseif ($entry.FullName.StartsWith($paintingPrefix, [System.StringComparison]::Ordinal)) {
            $updated = [regex]::Replace(
                $updated,
                '(?m)^nbt\.EntityTag\.variant=',
                'components.entity_data.variant=')
        } elseif ($entry.FullName.StartsWith($stewPrefix, [System.StringComparison]::Ordinal)) {
            $effect = [System.IO.Path]::GetFileNameWithoutExtension($entry.Name)
            $updated = [regex]::Replace(
                $updated,
                '(?m)^nbt\.Effects\.0\.EffectId=.*$',
                "components.suspicious_stew_effects.*.id=minecraft:$effect")
        }

        if ($updated -eq $content) {
            continue
        }

        $name = $entry.FullName
        $lastWriteTime = $entry.LastWriteTime
        $entry.Delete()
        $replacement = $archive.CreateEntry(
            $name,
            [System.IO.Compression.CompressionLevel]::Optimal)
        $replacement.LastWriteTime = $lastWriteTime
        $writer = [System.IO.StreamWriter]::new($replacement.Open(), $utf8)
        try {
            $writer.Write($updated)
        } finally {
            $writer.Dispose()
        }
        $migrated++
    }
} finally {
    $archive.Dispose()
}

$shieldModelPath = 'assets/minecraft/models/item/excalibur/custom_name/shield/shield_round.json'
$archive = [System.IO.Compression.ZipFile]::Open(
    $temporary,
    [System.IO.Compression.ZipArchiveMode]::Update)
try {
    $entry = $archive.GetEntry($shieldModelPath)
    if ($null -eq $entry) {
        throw "Missing round-shield model: $shieldModelPath"
    }
    $reader = [System.IO.StreamReader]::new($entry.Open())
    try {
        $content = $reader.ReadToEnd()
    } finally {
        $reader.Dispose()
    }
    $updated = $content.Replace(
        'clarentmod:item/shield/shield_round_blocking',
        'minecraft:item/excalibur/custom_name/shield/shield_round_blocking')
    if ($updated -ne $content) {
        $lastWriteTime = $entry.LastWriteTime
        $entry.Delete()
        $replacement = $archive.CreateEntry(
            $shieldModelPath,
            [System.IO.Compression.CompressionLevel]::Optimal)
        $replacement.LastWriteTime = $lastWriteTime
        $writer = [System.IO.StreamWriter]::new($replacement.Open(), $utf8)
        try {
            $writer.Write($updated)
        } finally {
            $writer.Dispose()
        }
        $modelsRepaired++
    }
} finally {
    $archive.Dispose()
}

$activePrefixes = @(
    'assets/',
    'overlay_mod_support/assets/',
    'overlay_1.21.11/assets/',
    'overlay_1.21.8/assets/',
    'overlay_1.21.5/assets/',
    'overlay_1.21.4/assets/',
    'overlay_1.21.1/assets/',
    'cit_1.21.4/assets/'
)
$verified = 0
$archive = [System.IO.Compression.ZipFile]::OpenRead($temporary)
try {
    foreach ($entry in $archive.Entries) {
        $active = $false
        foreach ($prefix in $activePrefixes) {
            if ($entry.FullName.StartsWith($prefix, [System.StringComparison]::Ordinal)) {
                $active = $true
                break
            }
        }
        if (-not $active -or
                -not $entry.FullName.Contains('/optifine/cit/') -or
                -not $entry.FullName.EndsWith('.properties', [System.StringComparison]::Ordinal)) {
            continue
        }

        $reader = [System.IO.StreamReader]::new($entry.Open())
        try {
            $content = $reader.ReadToEnd()
        } finally {
            $reader.Dispose()
        }
        $activeLines = @($content -split '\r?\n' | Where-Object {
            $line = $_.Trim()
            $line.Length -gt 0 -and -not $line.StartsWith('#')
        })
        if ($activeLines -match '^nbt\.') {
            throw "Active legacy NBT CIT remains: $($entry.FullName)"
        }
        if ($activeLines -contains 'type=item' -and
                -not ($activeLines -match '^(items|matchItems)=')) {
            throw "Item CIT has no item target: $($entry.FullName)"
        }
        $verified++
    }
    foreach ($removedEntry in $removedEntries) {
        if ($null -ne $archive.GetEntry($removedEntry)) {
            throw "Invalid CIT definition was not removed: $removedEntry"
        }
    }
} finally {
    $archive.Dispose()
}

Move-Item -LiteralPath $temporary -Destination $pack -Force
Write-Host "Migrated $migrated CIT definitions, removed $removed invalid definitions, repaired $modelsRepaired model references, and verified $verified active definitions."
