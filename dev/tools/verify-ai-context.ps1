[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$repoRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\..'))
$contextScript = Join-Path $PSScriptRoot 'ai-context.ps1'
$benchmarkPath = Join-Path $PSScriptRoot 'ai-context-benchmark.json'
$routePath = Join-Path $repoRoot 'docs\ai\routes.json'
$failures = New-Object System.Collections.Generic.List[string]

function Add-Failure([string]$Message) {
    $failures.Add($Message)
}

function Get-Length([string]$RelativePath) {
    $path = Join-Path $repoRoot $RelativePath
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        Add-Failure "Missing required file: $RelativePath"
        return 0
    }
    return (Get-Item -LiteralPath $path).Length
}

$rootContextBytes = (Get-Length 'RULES.md') + (Get-Length 'AGENTS.md') + (Get-Length 'CODEX.md')
if ($rootContextBytes -gt 16384) {
    Add-Failure "RULES.md + AGENTS.md + CODEX.md exceed 16 KB: $rootContextBytes bytes"
}

foreach ($bounded in @('docs/ai/CURRENT_STATUS.md', 'PLAN.md', 'TODO.md')) {
    $size = Get-Length $bounded
    if ($size -gt 12288) {
        Add-Failure "$bounded exceeds 12 KB: $size bytes"
    }
    $datedHeadings = Select-String -LiteralPath (Join-Path $repoRoot $bounded) -Pattern '^##\s+20\d\d-' -ErrorAction SilentlyContinue
    if ($null -ne $datedHeadings) {
        Add-Failure "$bounded contains append-only dated completion headings"
    }
}

$archiveMinimums = @{
    'docs/ai/archive/CURRENT_STATUS_THROUGH_2026-07-11.md' = 500000
    'docs/ai/archive/PLAN_THROUGH_2026-07-11.md' = 200000
    'docs/ai/archive/TODO_THROUGH_2026-07-11.md' = 18000
}
foreach ($entry in $archiveMinimums.GetEnumerator()) {
    $size = Get-Length $entry.Key
    if ($size -lt $entry.Value) {
        Add-Failure "$($entry.Key) is smaller than the preserved-history floor: $size bytes"
    }
}

try {
    $routes = Get-Content -Raw -LiteralPath $routePath | ConvertFrom-Json
} catch {
    Add-Failure "routes.json is invalid JSON: $($_.Exception.Message)"
    $routes = $null
}
if ($null -ne $routes) {
    $routeIds = @{}
    foreach ($route in $routes.domains) {
        if ($routeIds.ContainsKey([string]$route.id)) {
            Add-Failure "Duplicate route id: $($route.id)"
        }
        $routeIds[[string]$route.id] = $true
        foreach ($doc in $route.docs) {
            if (-not (Test-Path -LiteralPath (Join-Path $repoRoot $doc) -PathType Leaf)) {
                Add-Failure "Route $($route.id) references missing doc: $doc"
            }
        }
        if ($route.sourceGlobs.Count -eq 0 -or $route.testGlobs.Count -eq 0 -or $route.commands.Count -eq 0) {
            Add-Failure "Route $($route.id) lacks source, test, or command metadata"
        }
    }
}

foreach ($toolFile in @('dev/tools/ai-context.ps1', 'dev/tools/verify-ai-context.ps1', 'dev/tools/ai-context-benchmark.json')) {
    [void](Get-Length $toolFile)
}
$contextToolText = Get-Content -Raw -LiteralPath $contextScript
foreach ($excludedPath in @('docs/ai/archive/**', 'external/**', 'addons/angling/reference/**', '**/build/**', 'dev/run/**')) {
    if (-not $contextToolText.Contains($excludedPath)) {
        Add-Failure "Context tool is missing ordinary-search exclusion: $excludedPath"
    }
}

$baselineFiles = @(
    'docs/ai/archive/RULES_THROUGH_2026-07-11.md',
    'docs/ai/archive/AGENTS_THROUGH_2026-07-11.md',
    'docs/ai/archive/CODEX_THROUGH_2026-07-11.md',
    'INDEX.md',
    'docs/ai/archive/CURRENT_STATUS_THROUGH_2026-07-11.md',
    'docs/ai/archive/AI_SEARCH_HINTS_THROUGH_2026-07-11.md',
    'docs/architecture/DEPENDENCY_GRAPH.md'
)
$baselineCharacters = 0
foreach ($file in $baselineFiles) {
    $path = Join-Path $repoRoot $file
    if (Test-Path -LiteralPath $path -PathType Leaf) {
        $baselineCharacters += (Get-Content -Raw -LiteralPath $path).Length
    } else {
        Add-Failure "Baseline source is missing: $file"
    }
}

$benchmark = Get-Content -Raw -LiteralPath $benchmarkPath | ConvertFrom-Json
$results = New-Object System.Collections.Generic.List[object]
$aggregateCapsuleCharacters = 0

foreach ($case in $benchmark.cases) {
    $watch = [Diagnostics.Stopwatch]::StartNew()
    $output = & powershell -NoProfile -ExecutionPolicy Bypass -File $contextScript `
        -Task ([string]$case.task) -Mode ([string]$case.mode) `
        -BudgetTokens ([int]$case.budgetTokens) -Format json 2>$null
    $exitCode = $LASTEXITCODE
    $watch.Stop()
    if ($exitCode -ne 0) {
        Add-Failure "Benchmark $($case.id) returned exit code $exitCode"
        continue
    }
    try {
        $capsule = ($output -join "`n") | ConvertFrom-Json
    } catch {
        Add-Failure "Benchmark $($case.id) returned invalid JSON"
        continue
    }
    if ($capsule.confidence -ne 'high') {
        Add-Failure "Benchmark $($case.id) confidence was $($capsule.confidence), expected high"
    }
    foreach ($route in $case.expectedRoutes) {
        if ($capsule.routes -notcontains $route) {
            Add-Failure "Benchmark $($case.id) missed route $route"
        }
    }
    $contextPaths = @()
    foreach ($entry in @($capsule.pivots)) {
        $contextPaths += [string]$entry.path
    }
    foreach ($entry in @($capsule.supportingOutlines)) {
        $contextPaths += [string]$entry.path
    }
    foreach ($requiredPath in $case.requiredPaths) {
        if ($contextPaths -notcontains $requiredPath) {
            Add-Failure "Benchmark $($case.id) missed required source $requiredPath"
        }
    }
    $documentPaths = @()
    foreach ($entry in @($capsule.docSections)) {
        $documentPaths += [string]$entry.path
    }
    foreach ($requiredDoc in $case.requiredDocs) {
        if ($documentPaths -notcontains $requiredDoc) {
            Add-Failure "Benchmark $($case.id) missed required doc $requiredDoc"
        }
    }
    if ([int]$capsule.metrics.estimatedTokens -gt [int]$case.budgetTokens) {
        Add-Failure "Benchmark $($case.id) exceeded its token budget"
    }
    $characters = [int64]$capsule.metrics.repositoryContentCharacters
    $aggregateCapsuleCharacters += $characters
    $results.Add([pscustomobject]@{
        id = [string]$case.id
        confidence = [string]$capsule.confidence
        routes = @($capsule.routes)
        repositoryCharacters = $characters
        estimatedTokens = [int]$capsule.metrics.estimatedTokens
        budgetTokens = [int]$case.budgetTokens
        elapsedMilliseconds = $watch.ElapsedMilliseconds
        providerReportedInputTokens = $null
    })
}

$lowConfidenceOutput = & powershell -NoProfile -ExecutionPolicy Bypass -File $contextScript `
    -Task 'florbulate unrelated quux mechanism' -Mode change -BudgetTokens 3000 -Format json 2>$null
$lowConfidenceExit = $LASTEXITCODE
if ($lowConfidenceExit -ne 2) {
    Add-Failure "Low-confidence query returned $lowConfidenceExit instead of exit code 2"
} else {
    $lowCapsule = ($lowConfidenceOutput -join "`n") | ConvertFrom-Json
    if ($lowCapsule.confidence -ne 'insufficient') {
        Add-Failure "Low-confidence query did not report insufficient context"
    }
}

$caseCount = [Math]::Max(1, $benchmark.cases.Count)
$aggregateBaselineCharacters = [int64]$baselineCharacters * $caseCount
$savingsPercent = if ($aggregateBaselineCharacters -gt 0) {
    [Math]::Round((1.0 - ($aggregateCapsuleCharacters / [double]$aggregateBaselineCharacters)) * 100.0, 2)
} else {
    0.0
}
if ($savingsPercent -lt 75.0) {
    Add-Failure "Aggregate repository-context savings were $savingsPercent%, below 75%"
}

$report = [ordered]@{
    version = 1
    generatedAt = [DateTimeOffset]::Now.ToString('o')
    baselineCharactersPerTask = $baselineCharacters
    aggregateBaselineCharacters = $aggregateBaselineCharacters
    aggregateCapsuleCharacters = $aggregateCapsuleCharacters
    aggregateSavingsPercent = $savingsPercent
    tokenMeasurement = 'Capsule estimates use characters/4; populate providerReportedInputTokens during fresh-session Codex qualification.'
    contextRecallCases = $results.ToArray()
    failures = $failures.ToArray()
}

$reportDir = Join-Path $repoRoot 'build\ai-context'
New-Item -ItemType Directory -Force -Path $reportDir | Out-Null
$report | ConvertTo-Json -Depth 10 | Set-Content -LiteralPath (Join-Path $reportDir 'benchmark.json') -Encoding UTF8

Write-Output "AI context root bytes: $rootContextBytes / 16384"
Write-Output "Baseline characters per task: $baselineCharacters"
Write-Output "Aggregate capsule savings: $savingsPercent%"
Write-Output "Benchmark cases completed: $($results.Count) / $($benchmark.cases.Count)"

if ($failures.Count -gt 0) {
    foreach ($failure in $failures) {
        Write-Error $failure
    }
    exit 1
}

Write-Output 'AI context verification passed.'
