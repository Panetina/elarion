param(
    [Parameter(Mandatory = $true)]
    [string] $SourcePack,

    [Parameter(Mandatory = $true)]
    [string] $OutputDirectory
)

$ErrorActionPreference = 'Stop'

Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem

$source = (Resolve-Path -LiteralPath $SourcePack).Path
$output = [System.IO.Path]::GetFullPath($OutputDirectory)
[System.IO.Directory]::CreateDirectory($output) | Out-Null

$mainPath = Join-Path $output 'Elarion Excalibured v1.zip'
$fontPath = Join-Path $output 'Elarion Font v1.zip'
$temporaryMain = "$mainPath.tmp"
$temporaryFont = "$fontPath.tmp"

foreach ($path in @($temporaryMain, $temporaryFont)) {
    if (Test-Path -LiteralPath $path) {
        Remove-Item -LiteralPath $path -Force
    }
}

$fontPrefix = 'assets/minecraft/textures/font/'
$fontEntries = @(
    'assets/minecraft/textures/font/accented.png',
    'assets/minecraft/textures/font/ascii.png',
    'assets/minecraft/textures/font/ascii_legacy.png',
    'assets/minecraft/textures/font/nonlatin_european.png'
)

function Copy-ZipEntry {
    param(
        [System.IO.Compression.ZipArchiveEntry] $Entry,
        [System.IO.Compression.ZipArchive] $Destination
    )

    $copy = $Destination.CreateEntry(
        $Entry.FullName,
        [System.IO.Compression.CompressionLevel]::Optimal)
    $copy.LastWriteTime = $Entry.LastWriteTime
    $inputStream = $Entry.Open()
    $outputStream = $copy.Open()
    try {
        $inputStream.CopyTo($outputStream)
    } finally {
        $outputStream.Dispose()
        $inputStream.Dispose()
    }
}

$sourceArchive = [System.IO.Compression.ZipFile]::OpenRead($source)
$mainArchive = [System.IO.Compression.ZipFile]::Open(
    $temporaryMain,
    [System.IO.Compression.ZipArchiveMode]::Create)
$fontArchive = [System.IO.Compression.ZipFile]::Open(
    $temporaryFont,
    [System.IO.Compression.ZipArchiveMode]::Create)

try {
    $sourceNames = [System.Collections.Generic.HashSet[string]]::new(
        [System.StringComparer]::Ordinal)
    foreach ($entry in $sourceArchive.Entries) {
        $sourceNames.Add($entry.FullName) | Out-Null
        if ($entry.FullName.EndsWith('/')) {
            continue
        }
        if ($entry.FullName.StartsWith($fontPrefix, [System.StringComparison]::Ordinal)) {
            Copy-ZipEntry -Entry $entry -Destination $fontArchive
        } else {
            Copy-ZipEntry -Entry $entry -Destination $mainArchive
        }
    }

    foreach ($required in $fontEntries) {
        if (-not $sourceNames.Contains($required)) {
            throw "Source pack is missing required font texture: $required"
        }
    }

    $fontMetadata = @'
{"pack":{"pack_format":34,"supported_formats":{"min_inclusive":34,"max_inclusive":34},"description":"Elarion Font v1"}}
'@
    $metadataEntry = $fontArchive.CreateEntry(
        'pack.mcmeta',
        [System.IO.Compression.CompressionLevel]::Optimal)
    $metadataWriter = [System.IO.StreamWriter]::new(
        $metadataEntry.Open(),
        [System.Text.UTF8Encoding]::new($false))
    try {
        $metadataWriter.Write($fontMetadata.Trim())
    } finally {
        $metadataWriter.Dispose()
    }

    $packIcon = $sourceArchive.GetEntry('pack.png')
    if ($null -ne $packIcon) {
        Copy-ZipEntry -Entry $packIcon -Destination $fontArchive
    }
} finally {
    $fontArchive.Dispose()
    $mainArchive.Dispose()
    $sourceArchive.Dispose()
}

Move-Item -LiteralPath $temporaryMain -Destination $mainPath -Force
Move-Item -LiteralPath $temporaryFont -Destination $fontPath -Force

$mainInfo = Get-Item -LiteralPath $mainPath
$fontInfo = Get-Item -LiteralPath $fontPath
Write-Host "Main pack: $($mainInfo.FullName) ($($mainInfo.Length) bytes)"
Write-Host "Font pack: $($fontInfo.FullName) ($($fontInfo.Length) bytes)"
