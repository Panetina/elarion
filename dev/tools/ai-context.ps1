[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [ValidateNotNullOrEmpty()]
    [string]$Task,

    [ValidateSet('explore', 'debug', 'change', 'refactor')]
    [string]$Mode = 'explore',

    [ValidateRange(1000, 24000)]
    [int]$BudgetTokens = 6000,

    [ValidateSet('markdown', 'json')]
    [string]$Format = 'markdown'
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$repoRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\..'))
$routePath = Join-Path $repoRoot 'docs\ai\routes.json'

function Convert-ToRepoPath {
    param([string]$Path)
    $full = [IO.Path]::GetFullPath($Path)
    if (-not $full.StartsWith($repoRoot, [StringComparison]::OrdinalIgnoreCase)) {
        return $null
    }
    return $full.Substring($repoRoot.Length).TrimStart('\', '/').Replace('\', '/')
}

function Get-ExistingCommand {
    param([string[]]$Names)
    foreach ($name in $Names) {
        $command = Get-Command $name -ErrorAction SilentlyContinue
        if ($null -ne $command) {
            return $command.Source
        }
    }
    return $null
}

function Test-RoutePath {
    param($Route, [string]$Path)
    foreach ($glob in $Route.sourceGlobs + $Route.testGlobs) {
        $pattern = ([string]$glob).Replace('\', '/').Replace('**', '*')
        if ($Path -like $pattern) {
            return $true
        }
    }
    return $false
}

function Get-DirtyPaths {
    $paths = @{}
    $lines = & git -C $repoRoot -c core.quotepath=false status --porcelain=v1 --untracked-files=all 2>$null
    foreach ($line in $lines) {
        if ([string]::IsNullOrWhiteSpace($line) -or $line.Length -lt 4) {
            continue
        }
        $path = $line.Substring(3).Trim()
        if ($path.Contains(' -> ')) {
            $path = $path.Substring($path.LastIndexOf(' -> ') + 4)
        }
        $path = $path.Trim('"').Replace('\', '/')
        $paths[$path] = $true
    }
    return $paths
}

function Get-SearchTerms {
    param([string]$Text)
    $stop = @{
        'about'=$true; 'after'=$true; 'before'=$true; 'change'=$true;
        'debug'=$true; 'feature'=$true; 'from'=$true; 'into'=$true;
        'project'=$true; 'refactor'=$true; 'should'=$true; 'system'=$true;
        'that'=$true; 'their'=$true; 'there'=$true; 'this'=$true;
        'through'=$true; 'with'=$true; 'without'=$true
    }
    $terms = New-Object System.Collections.Generic.List[string]
    foreach ($match in [regex]::Matches($Text, '[A-Za-z_][A-Za-z0-9_:.\/-]{3,}')) {
        $value = $match.Value.Trim('.', ':', '/', '-')
        $lower = $value.ToLowerInvariant()
        if ($value.Length -ge 4 -and -not $stop.ContainsKey($lower) -and -not $terms.Contains($value)) {
            $terms.Add($value)
        }
    }
    return @($terms | Select-Object -First 12)
}

function Add-Candidate {
    param([hashtable]$Candidates, [string]$Path, [int]$Score, [string]$Reason, [hashtable]$DirtyPaths)
    if ([string]::IsNullOrWhiteSpace($Path)) {
        return
    }
    $normalized = $Path.Replace('\', '/').TrimStart('./')
    $absolute = Join-Path $repoRoot $normalized
    if (-not (Test-Path -LiteralPath $absolute -PathType Leaf)) {
        return
    }
    if ($normalized -like 'docs/ai/archive/*' -or
        $normalized -like 'external/*' -or
        $normalized -like 'addons/angling/reference/*' -or
        $normalized -like 'build/*' -or
        $normalized -like '*/build/*' -or
        $normalized -like 'dev/run/*') {
        return
    }
    if (-not $Candidates.ContainsKey($normalized)) {
        $isDirty = $DirtyPaths.ContainsKey($normalized)
        $Candidates[$normalized] = [ordered]@{
            path = $normalized
            score = if ($isDirty) { 6 } else { 0 }
            reasons = New-Object System.Collections.Generic.List[string]
            dirty = $isDirty
        }
        if ($isDirty) {
            $Candidates[$normalized].reasons.Add('current dirty-worktree file')
        }
    }
    $Candidates[$normalized].score += $Score
    if (-not $Candidates[$normalized].reasons.Contains($Reason)) {
        $Candidates[$normalized].reasons.Add($Reason)
    }
}

function Get-MarkdownSection {
    param([string]$RelativePath, [string[]]$Keywords, [int]$MaximumCharacters = 2600)
    $absolute = Join-Path $repoRoot $RelativePath
    if (-not (Test-Path -LiteralPath $absolute -PathType Leaf)) {
        return $null
    }
    $lines = @(Get-Content -LiteralPath $absolute)
    if ($lines.Count -eq 0) {
        return ''
    }
    $sections = New-Object System.Collections.Generic.List[object]
    $starts = New-Object System.Collections.Generic.List[int]
    for ($i = 0; $i -lt $lines.Count; $i++) {
        if ($lines[$i] -match '^(#{1,6})\s+(.+)$') {
            $starts.Add($i)
        }
    }
    if ($starts.Count -eq 0) {
        $text = $lines -join "`n"
        return $text.Substring(0, [Math]::Min($MaximumCharacters, $text.Length))
    }
    for ($index = 0; $index -lt $starts.Count; $index++) {
        $start = $starts[$index]
        $level = ([regex]::Match($lines[$start], '^#+')).Value.Length
        $end = $lines.Count
        for ($next = $index + 1; $next -lt $starts.Count; $next++) {
            $nextLevel = ([regex]::Match($lines[$starts[$next]], '^#+')).Value.Length
            if ($nextLevel -le $level) {
                $end = $starts[$next]
                break
            }
        }
        $text = ($lines[$start..($end - 1)] -join "`n").Trim()
        $score = 0
        foreach ($keyword in $Keywords) {
            if ($lines[$start].IndexOf($keyword, [StringComparison]::OrdinalIgnoreCase) -ge 0) {
                $score += 5
            } elseif ($text.IndexOf($keyword, [StringComparison]::OrdinalIgnoreCase) -ge 0) {
                $score += 1
            }
        }
        $sections.Add([pscustomobject]@{ score = $score; start = $start + 1; text = $text })
    }
    $selected = $sections | Sort-Object @{Expression='score';Descending=$true}, @{Expression='start';Ascending=$true} | Select-Object -First 1
    $value = [string]$selected.text
    if ($value.Length -gt $MaximumCharacters) {
        $value = $value.Substring(0, $MaximumCharacters).TrimEnd() + "`n[section truncated at deterministic capsule limit]"
    }
    return [pscustomobject]@{ line = $selected.start; text = $value }
}

function Get-FallbackOutline {
    param([string]$RelativePath)
    $absolute = Join-Path $repoRoot $RelativePath
    $patterns = @(
        '^\s*(public|protected|private)?\s*(abstract\s+|final\s+|sealed\s+|non-sealed\s+)?(class|interface|record|enum)\s+[A-Za-z_][A-Za-z0-9_]*',
        '^\s*(public|protected)\s+(static\s+)?([A-Za-z0-9_<>,.?\[\]\s]+)\s+[A-Za-z_][A-Za-z0-9_]*\s*\([^;]*\)\s*(\{|throws)',
        '^\s*(public|protected)\s+(static\s+)?final\s+[A-Za-z0-9_<>,.?\[\]\s]+\s+[A-Z][A-Z0-9_]*\s*='
    )
    $matches = Select-String -LiteralPath $absolute -Pattern $patterns | Select-Object -First 80
    if ($null -eq $matches) {
        return 'No public structural signatures found by fallback extractor.'
    }
    return (($matches | ForEach-Object { "{0}: {1}" -f $_.LineNumber, $_.Line.Trim() }) -join "`n")
}

function Get-Outline {
    param([string]$RelativePath)
    return Get-FallbackOutline -RelativePath $RelativePath
}

function New-MarkdownOutput {
    param($Capsule)
    $builder = New-Object Text.StringBuilder
    [void]$builder.AppendLine('# Elarion Context Capsule')
    [void]$builder.AppendLine()
    [void]$builder.AppendLine("- Task: $($Capsule.task)")
    [void]$builder.AppendLine("- Mode: $($Capsule.mode)")
    [void]$builder.AppendLine("- Budget: $($Capsule.budget) tokens")
    [void]$builder.AppendLine("- Confidence: $($Capsule.confidence)")
    [void]$builder.AppendLine("- Routes: $($Capsule.routes -join ', ')")
    [void]$builder.AppendLine()
    [void]$builder.AppendLine('## Policies')
    foreach ($policy in $Capsule.policies) {
        [void]$builder.AppendLine()
        [void]$builder.AppendLine("### $($policy.path)")
        [void]$builder.AppendLine()
        [void]$builder.AppendLine($policy.content)
    }
    [void]$builder.AppendLine()
    [void]$builder.AppendLine('## Authority Documents')
    foreach ($doc in $Capsule.docSections) {
        [void]$builder.AppendLine()
        [void]$builder.AppendLine("### $($doc.path):$($doc.line)")
        [void]$builder.AppendLine()
        [void]$builder.AppendLine($doc.content)
    }
    [void]$builder.AppendLine()
    [void]$builder.AppendLine('## Pivot Files')
    foreach ($pivot in $Capsule.pivots) {
        [void]$builder.AppendLine()
        [void]$builder.AppendLine("### $($pivot.path)")
        [void]$builder.AppendLine("Reason: $($pivot.reason)")
        [void]$builder.AppendLine()
        [void]$builder.AppendLine('```')
        [void]$builder.AppendLine($pivot.content)
        [void]$builder.AppendLine('```')
    }
    [void]$builder.AppendLine()
    [void]$builder.AppendLine('## Supporting Outlines')
    foreach ($outline in $Capsule.supportingOutlines) {
        [void]$builder.AppendLine()
        [void]$builder.AppendLine("### $($outline.path)")
        [void]$builder.AppendLine("Reason: $($outline.reason)")
        [void]$builder.AppendLine()
        [void]$builder.AppendLine('```text')
        [void]$builder.AppendLine($outline.content)
        [void]$builder.AppendLine('```')
    }
    [void]$builder.AppendLine()
    [void]$builder.AppendLine('## Verification')
    foreach ($item in $Capsule.verification) {
        [void]$builder.AppendLine("- $item")
    }
    [void]$builder.AppendLine()
    [void]$builder.AppendLine('## Omissions and Warnings')
    if ($Capsule.omissions.Count -eq 0) {
        [void]$builder.AppendLine('- None.')
    } else {
        foreach ($item in $Capsule.omissions) {
            [void]$builder.AppendLine("- $item")
        }
    }
    return $builder.ToString()
}

if (-not (Test-Path -LiteralPath $routePath -PathType Leaf)) {
    throw "Missing route catalog: $routePath"
}

$catalog = Get-Content -Raw -LiteralPath $routePath | ConvertFrom-Json

$crossModule = $false
$taskLower = $Task.ToLowerInvariant()
$broadScopeIndicators = @(
    'cross-module', 'cross module', 'repository-wide', 'repository wide',
    'project-wide', 'project wide', 'whole repository', 'all modules',
    'toate modulele', 'build', 'gradle', 'compile', 'compilation',
    'compilare', 'documenta', 'status', 'context routing', 'rutare context'
)
$matchedBroadScopeIndicators = @($broadScopeIndicators | Where-Object {
    $taskLower.Contains($_)
} | Select-Object -Unique)
$explicitBroadScope = $taskLower.Contains('cross-module') -or
        $taskLower.Contains('cross module') -or
        $taskLower.Contains('repository-wide') -or
        $taskLower.Contains('repository wide') -or
        $taskLower.Contains('project-wide') -or
        $taskLower.Contains('project wide') -or
        $taskLower.Contains('whole repository') -or
        $taskLower.Contains('all modules') -or
        $taskLower.Contains('toate modulele')
$requiresBroadCoverage = $explicitBroadScope -or $matchedBroadScopeIndicators.Count -ge 2
$scoredRoutes = foreach ($route in $catalog.domains) {
    $score = 0
    foreach ($keyword in $route.keywords) {
        $keywordLower = ([string]$keyword).ToLowerInvariant()
        $matched = if ($keywordLower.Length -le 3) {
            [regex]::IsMatch($Task, '(?<![A-Za-z0-9])(?i:' + [regex]::Escape($keywordLower) + ')(?=[A-Z]|[^A-Za-z0-9]|$)')
        } else {
            $taskLower.Contains($keywordLower)
        }
        if ($matched) {
            $score += if (([string]$keyword).Contains(' ')) { 4 } else { 2 }
        }
    }
    if ($score -gt 0) {
        [pscustomobject]@{ route = $route; score = $score }
    }
}
$selectedRoutes = @($scoredRoutes | Sort-Object score -Descending | Select-Object -First 3 | ForEach-Object { $_.route })
if ($selectedRoutes.Count -gt 1) {
    $crossModule = $true
}

$maximum = if ($crossModule) { [int]$catalog.maximumCrossModuleTokens } else { [int]$catalog.maximumStandardTokens }
if ($BudgetTokens -gt $maximum) {
    throw "BudgetTokens $BudgetTokens exceeds the $maximum-token limit for this task classification."
}

$dirtyPaths = Get-DirtyPaths
$candidates = @{}
$terms = Get-SearchTerms -Text $Task
$rgPath = Get-ExistingCommand -Names @('rg')
$omissions = New-Object System.Collections.Generic.List[string]
$routeCoverageInsufficient = $requiresBroadCoverage -and $selectedRoutes.Count -lt 2

if ($selectedRoutes.Count -eq 0) {
    $omissions.Add('No task domain matched docs/ai/routes.json; add a precise domain or identifier.')
}
if ($routeCoverageInsufficient) {
    $omissions.Add('Broad or cross-module task matched fewer than two domains; use repository discovery or narrow the task before editing.')
}

if ($null -eq $rgPath) {
    $omissions.Add('rg is unavailable; source discovery cannot meet the required confidence gate.')
} elseif ($terms.Count -gt 0) {
    $pattern = ($terms | ForEach-Object { [regex]::Escape($_) }) -join '|'
    $rgArgs = @(
        '--files-with-matches', '--ignore-case',
        '--glob', '*.java', '--glob', '*.json', '--glob', '*.yml', '--glob', '*.yaml',
        '--glob', '!docs/ai/archive/**', '--glob', '!external/**',
        '--glob', '!addons/angling/reference/**', '--glob', '!**/build/**',
        '--glob', '!dev/run/**', $pattern, '.'
    )
    Push-Location $repoRoot
    try {
        $matches = & $rgPath @rgArgs 2>$null
    } finally {
        Pop-Location
    }
    foreach ($match in $matches) {
        $path = ([string]$match).TrimStart('.', '/', '\').Replace('\', '/')
        $routeScore = 0
        foreach ($route in $selectedRoutes) {
            if (Test-RoutePath -Route $route -Path $path) {
                $routeScore += 4
            }
        }
        $nameScore = 0
        $fileName = [IO.Path]::GetFileNameWithoutExtension($path)
        foreach ($term in $terms) {
            if ($fileName.Equals($term, [StringComparison]::OrdinalIgnoreCase)) {
                $nameScore += 30
            } elseif ($fileName.IndexOf($term, [StringComparison]::OrdinalIgnoreCase) -ge 0 -or
                $term.IndexOf($fileName, [StringComparison]::OrdinalIgnoreCase) -ge 0) {
                $nameScore += 12
            }
        }
        if ($path -like '*/src/test/*' -and $taskLower -notmatch '\b(test|tests|coverage)\b') {
            $nameScore -= 4
        }
        $reason = if ($nameScore -ge 12) { 'task identifier matches file name' } else { 'task identifier match' }
        Add-Candidate -Candidates $candidates -Path $path -Score (3 + $routeScore + $nameScore) -Reason $reason -DirtyPaths $dirtyPaths
    }
}

foreach ($path in $dirtyPaths.Keys) {
    foreach ($route in $selectedRoutes) {
        if (Test-RoutePath -Route $route -Path $path) {
            Add-Candidate -Candidates $candidates -Path $path -Score 5 -Reason "dirty file in $($route.id) route" -DirtyPaths $dirtyPaths
            break
        }
    }
}

$rankedCandidates = @($candidates.Values | Where-Object {
    $_.path.EndsWith('.java', [StringComparison]::OrdinalIgnoreCase)
} | Sort-Object @{Expression={ [int]$_['score'] };Descending=$true}, @{Expression={ [string]$_['path'] };Descending=$false})

$policies = New-Object System.Collections.Generic.List[object]
$docSections = New-Object System.Collections.Generic.List[object]
$pivots = New-Object System.Collections.Generic.List[object]
$supportingOutlines = New-Object System.Collections.Generic.List[object]
$verification = New-Object System.Collections.Generic.List[string]
$contentBudgetCharacters = [Math]::Floor($BudgetTokens * 3.35)
$usedCharacters = 0

foreach ($policyPath in @('RULES.md', 'AGENTS.md')) {
    $content = Get-Content -Raw -LiteralPath (Join-Path $repoRoot $policyPath)
    $policies.Add([pscustomobject]@{ path = $policyPath; content = $content.Trim() })
    $usedCharacters += $content.Length
}

$docPaths = New-Object System.Collections.Generic.List[string]
foreach ($route in $selectedRoutes) {
    foreach ($doc in $route.docs) {
        if (-not $docPaths.Contains([string]$doc)) {
            $docPaths.Add([string]$doc)
        }
    }
    foreach ($command in $route.commands) {
        if (-not $verification.Contains([string]$command)) {
            $verification.Add([string]$command)
        }
    }
    foreach ($check in $route.companionChecks) {
        $line = "Required check: $check"
        if (-not $verification.Contains($line)) {
            $verification.Add($line)
        }
    }
}

$sectionKeywords = @($terms + ($selectedRoutes | ForEach-Object { $_.keywords } | Select-Object -Unique))
foreach ($docPath in $docPaths | Select-Object -First 4) {
    if (-not (Test-Path -LiteralPath (Join-Path $repoRoot $docPath) -PathType Leaf)) {
        $omissions.Add("Route authority document is missing: $docPath")
        continue
    }
    $section = Get-MarkdownSection -RelativePath $docPath -Keywords $sectionKeywords -MaximumCharacters 1800
    if ($null -eq $section) {
        continue
    }
    $cost = $section.text.Length + $docPath.Length + 80
    if ($usedCharacters + $cost -le $contentBudgetCharacters) {
        $docSections.Add([pscustomobject]@{ path = $docPath; line = $section.line; content = $section.text })
        $usedCharacters += $cost
    } else {
        $omissions.Add("Budget omitted authority section from $docPath")
    }
}

$pivotLimit = if ($Mode -eq 'explore') { 2 } else { 3 }
$outlineReserveCharacters = 0
foreach ($candidate in $rankedCandidates) {
    if ($pivots.Count -ge $pivotLimit) {
        break
    }
    $absolute = Join-Path $repoRoot $candidate.path
    $content = Get-Content -Raw -LiteralPath $absolute
    $cost = $content.Length + $candidate.path.Length + 100
    if ($usedCharacters + $cost + $outlineReserveCharacters -le $contentBudgetCharacters) {
        $pivots.Add([pscustomobject]@{
            path = $candidate.path
            reason = ($candidate.reasons -join '; ')
            dirty = [bool]$candidate.dirty
            content = $content.TrimEnd()
        })
        $usedCharacters += $cost
    } else {
        $omissions.Add("Full pivot did not fit budget: $($candidate.path)")
        if ($outlineReserveCharacters -eq 0) {
            $outlineReserveCharacters = 3200
        }
    }
}

$outlineCandidates = @($rankedCandidates | Where-Object {
    $path = $_.path
    -not ($pivots | Where-Object { $_.path -eq $path })
} | Select-Object -First 8)
foreach ($candidate in $outlineCandidates) {
    $outline = Get-Outline -RelativePath $candidate.path
    if ($outline.Length -gt 3000) {
        $outline = $outline.Substring(0, 3000) + "`n[outline truncated]"
    }
    $cost = $outline.Length + $candidate.path.Length + 100
    if ($usedCharacters + $cost -le $contentBudgetCharacters) {
        $supportingOutlines.Add([pscustomobject]@{
            path = $candidate.path
            reason = ($candidate.reasons -join '; ')
            dirty = [bool]$candidate.dirty
            content = $outline
        })
        $usedCharacters += $cost
    } else {
        $omissions.Add("Budget omitted supporting outline: $($candidate.path)")
    }
}

$confidence = 'high'
if ($selectedRoutes.Count -eq 0 -or $routeCoverageInsufficient -or $null -eq $rgPath -or $docSections.Count -eq 0) {
    $confidence = 'insufficient'
} elseif ($pivots.Count -eq 0) {
    $confidence = if ($Mode -eq 'explore') { 'medium' } else { 'insufficient' }
}
if ($Mode -ne 'explore' -and $pivots.Count -gt 0) {
    $dirtyPivotCount = @($pivots | Where-Object { $_.dirty }).Count
    if ($dirtyPivotCount -eq 0 -and $dirtyPaths.Count -gt 0) {
        $omissions.Add('No dirty edit target matched the task; verify the intended file before applying a patch.')
    }
}

$routeIds = [string[]]($selectedRoutes | ForEach-Object { [string]$_.id })
$policyArray = $policies.ToArray()
$pivotArray = $pivots.ToArray()
$outlineArray = $supportingOutlines.ToArray()
$docArray = $docSections.ToArray()
$verificationArray = $verification.ToArray()
$omissionArray = $omissions.ToArray()
$contentParts = New-Object System.Collections.Generic.List[string]
foreach ($entry in @($policyArray + $pivotArray + $outlineArray + $docArray)) {
    $contentParts.Add([string]$entry.content)
}

$capsule = [ordered]@{
    version = 1
    task = $Task
    mode = $Mode
    budget = $BudgetTokens
    routes = $routeIds
    policies = $policyArray
    pivots = $pivotArray
    supportingOutlines = $outlineArray
    docSections = $docArray
    verification = $verificationArray
    omissions = $omissionArray
    confidence = $confidence
    metrics = [ordered]@{
        repositoryContentCharacters = $usedCharacters
        repositoryContentUtf8Bytes = [Text.Encoding]::UTF8.GetByteCount(($contentParts.ToArray() -join "`n"))
        estimatedTokens = [Math]::Ceiling($usedCharacters / 4.0)
        tokenCountKind = 'character-estimate; set provider-reported usage in benchmark results for exact end-to-end measurement'
    }
}

$output = if ($Format -eq 'json') {
    $capsule | ConvertTo-Json -Depth 10
} else {
    New-MarkdownOutput -Capsule $capsule
}

Write-Output $output
if ($confidence -eq 'insufficient') {
    exit 2
}
