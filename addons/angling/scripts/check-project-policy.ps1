[CmdletBinding()]
param(
    [string]$Root
)

$ErrorActionPreference = 'Stop'
if ([string]::IsNullOrWhiteSpace($Root)) {
    $scriptDirectory = Split-Path -Parent $MyInvocation.MyCommand.Path
    $Root = Split-Path -Parent $scriptDirectory
}
$rootPath = (Resolve-Path -LiteralPath $Root).Path
$failures = [System.Collections.Generic.List[string]]::new()

function Add-Failure {
    param([string]$Message)
    $failures.Add($Message)
}

function Get-RelativePath {
    param(
        [string]$BasePath,
        [string]$TargetPath
    )

    $baseUri = [Uri]((Join-Path $BasePath '.') + [IO.Path]::DirectorySeparatorChar)
    $targetUri = [Uri]$TargetPath
    return [Uri]::UnescapeDataString(
        $baseUri.MakeRelativeUri($targetUri).ToString()
    ).Replace('/', [IO.Path]::DirectorySeparatorChar)
}

$forbiddenPaths = @(
    'src/main/resources/assets/starcatcher',
    'src/main/resources/data/starcatcher',
    'src/generated/resources/assets/starcatcher',
    'src/generated/resources/data/starcatcher',
    'src/main/templates/META-INF/neoforge.mods.toml',
    'src/main/resources/META-INF/neoforge.mods.toml'
)

foreach ($relativePath in $forbiddenPaths) {
    $candidate = Join-Path $rootPath $relativePath
    if (Test-Path -LiteralPath $candidate) {
        Add-Failure "Forbidden inherited path exists: $relativePath"
    }
}

$trackedReference = @(
    git -C $rootPath ls-files -- 'reference/upstream-starcatcher-neoforge-1.21.1'
)
if ($LASTEXITCODE -ne 0) {
    Add-Failure 'Unable to inspect tracked files with git.'
}
elseif ($trackedReference.Count -gt 0) {
    Add-Failure 'The local upstream reference checkout is tracked by Git.'
}

$scanFiles = [System.Collections.Generic.List[System.IO.FileInfo]]::new()
$sourceRoot = Join-Path $rootPath 'src'
if (Test-Path -LiteralPath $sourceRoot) {
    Get-ChildItem -LiteralPath $sourceRoot -Recurse -File | ForEach-Object {
        $scanFiles.Add($_)
    }
}

$buildFiles = @(
    'build.gradle',
    'build.gradle.kts',
    'settings.gradle',
    'settings.gradle.kts',
    'gradle.properties'
)
foreach ($buildFile in $buildFiles) {
    $candidate = Join-Path $rootPath $buildFile
    if (Test-Path -LiteralPath $candidate) {
        $scanFiles.Add((Get-Item -LiteralPath $candidate))
    }
}

$textExtensions = @(
    '.java', '.kt', '.kts', '.gradle', '.properties', '.json', '.json5',
    '.toml', '.xml', '.mcmeta', '.cfg', '.accesswidener', '.mixins'
)
$forbiddenPatterns = [ordered]@{
    'upstream branding or namespace' = '(?i)starcatcher'
    'upstream Java package' = '(?i)com\.wdiscute'
    'NeoForge/Forge production reference' = '(?i)net\.(neo)?forged|neoforge\.mods\.toml'
}

foreach ($file in $scanFiles) {
    if ($textExtensions -notcontains $file.Extension.ToLowerInvariant()) {
        continue
    }

    $relativePath = Get-RelativePath $rootPath $file.FullName
    foreach ($entry in $forbiddenPatterns.GetEnumerator()) {
        $matches = Select-String -LiteralPath $file.FullName -Pattern $entry.Value
        foreach ($match in $matches) {
            Add-Failure "$($entry.Key) in ${relativePath}:$($match.LineNumber)"
        }
    }
}

$upstreamAssetRoot = Join-Path $rootPath `
    'reference/upstream-starcatcher-neoforge-1.21.1/src/main/resources'
$elarionAssetRoot = Join-Path $rootPath 'src/main/resources/assets/elarion'
$assetExtensions = @(
    '.png', '.jpg', '.jpeg', '.webp', '.gif', '.ogg', '.wav', '.mp3',
    '.ase', '.aseprite', '.ttf', '.otf', '.bbmodel', '.obj', '.mtl',
    '.gltf', '.glb'
)

if (
    (Test-Path -LiteralPath $upstreamAssetRoot) -and
    (Test-Path -LiteralPath $elarionAssetRoot)
) {
    $upstreamHashes = [System.Collections.Generic.HashSet[string]]::new(
        [StringComparer]::OrdinalIgnoreCase
    )

    Get-ChildItem -LiteralPath $upstreamAssetRoot -Recurse -File |
        Where-Object {
            $assetExtensions -contains $_.Extension.ToLowerInvariant()
        } |
        ForEach-Object {
            $null = $upstreamHashes.Add(
                (Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash
            )
        }

    Get-ChildItem -LiteralPath $elarionAssetRoot -Recurse -File |
        Where-Object {
            $assetExtensions -contains $_.Extension.ToLowerInvariant()
        } |
        ForEach-Object {
            $hash = (Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash
            if ($upstreamHashes.Contains($hash)) {
                $relativePath = Get-RelativePath $rootPath $_.FullName
                Add-Failure "Asset is byte-identical to upstream: $relativePath"
            }
        }
}

if ($failures.Count -gt 0) {
    Write-Error (
        "Project policy check failed:`n - " +
        ($failures -join "`n - ")
    )
    exit 1
}

Write-Output 'Project policy checks passed.'
