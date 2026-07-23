param(
    [Parameter(Mandatory = $true)]
    [string] $PackPath
)

$ErrorActionPreference = 'Stop'

Add-Type -AssemblyName System.IO.Compression.FileSystem

$pack = (Resolve-Path -LiteralPath $PackPath).Path
$activePrefixes = @(
    '',
    'overlay_mod_support/',
    'overlay_1.21.11/',
    'overlay_1.21.8/',
    'overlay_1.21.5/',
    'overlay_1.21.4/',
    'overlay_1.21.1/',
    'cit_1.21.4/'
)
$archive = [System.IO.Compression.ZipFile]::OpenRead($pack)
try {
    $entryNames = [System.Collections.Generic.HashSet[string]]::new(
        [System.StringComparer]::Ordinal)
    foreach ($entry in $archive.Entries) {
        $entryNames.Add($entry.FullName) | Out-Null
        if ($entry.FullName.EndsWith('.json', [System.StringComparison]::Ordinal)) {
            $reader = [System.IO.StreamReader]::new($entry.Open())
            try {
                $content = $reader.ReadToEnd()
            } finally {
                $reader.Dispose()
            }
            if ($content.Contains('clarentmod:')) {
                throw "Active model still references the removed clarentmod namespace: $($entry.FullName)"
            }
        }
    }

    $verified = 0
    foreach ($entry in $archive.Entries) {
        $active = $false
        foreach ($prefix in $activePrefixes) {
            if ($entry.FullName.StartsWith("${prefix}assets/", [System.StringComparison]::Ordinal)) {
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
            throw "Active legacy NBT CIT is not supported on Minecraft 1.21.1: $($entry.FullName)"
        }
        if ($activeLines -contains 'type=item' -and
                -not ($activeLines -match '^(items|matchItems)=')) {
            throw "Item CIT has no item target: $($entry.FullName)"
        }

        foreach ($line in $activeLines) {
            if ($line -notmatch '^model(?:\.[^=]+)?=(.+)$') {
                continue
            }
            $model = $Matches[1].Trim()
            if ($model.StartsWith('./')) {
                continue
            }
            $namespace = 'minecraft'
            $modelPath = $model
            if ($model.Contains(':')) {
                $namespace, $modelPath = $model -split ':', 2
            }
            $modelPath = $modelPath -replace '\.json$', ''
            $found = $false
            foreach ($prefix in $activePrefixes) {
                $candidate = "${prefix}assets/$namespace/models/$modelPath.json"
                if ($entryNames.Contains($candidate)) {
                    $found = $true
                    break
                }
            }
            if (-not $found) {
                throw "CIT model cannot be resolved: $($entry.FullName) -> $model"
            }
        }
        $verified++
    }

    if ($verified -ne 267) {
        throw "Expected 267 active Minecraft 1.21.1 CIT definitions, verified $verified"
    }
} finally {
    $archive.Dispose()
}

Write-Host "Verified 267 active Excalibured CIT definitions for Minecraft 1.21.1."
