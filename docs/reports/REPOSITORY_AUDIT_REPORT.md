# Repository Audit Report

Audit date: 2026-06-12

Scope: documentation, reference material, architecture notes, planning docs,
lore, tracking docs, and repository organization. Gameplay code was not
modified.

## Authority Chain Reviewed

- `RULES.md`: authoritative policy. Current and detailed.
- `AGENTS.md`: concise host instructions. Updated with mandatory workflow.
- `CODEX.md`: project navigation context. Updated with faster source lookup.

Conflict found:

- `RULES.md` requires docs to be updated with architecture changes.
- The latest request requires Markdown/docs to be ignored for commit/push
  except `INDEX.md`.
- Resolution: `.gitignore` now prevents new Markdown/reference docs from being
  accidentally added, but existing tracked Markdown files remain tracked until
  deliberately removed from Git. This means local docs can still guide Codex,
  while `INDEX.md` remains the committed navigation entry point.

## Documentation Reviewed

Reviewed categories:

- root Markdown: `RULES.md`, `AGENTS.md`, `CODEX.md`, `INDEX.md`, `TODO.md`,
  `PLAN.md`, `PLANS.md`, `LORE.md`, `OPTIMIZATION_TRACKER.md`, `README.md`
- architecture docs: `docs/architecture/`
- system docs: `docs/systems/`
- addon docs: `docs/addons/`
- Fabric references: `docs/fabric-reference/`
- NeoForge references: `docs/neoforge-reference/`
- porting references: `docs/porting/`
- reports: `docs/reports/`
- lore archive: `lore/folklore/`
- addon-local docs under `addons/angling/`

## Source Validation Summary

Source-backed implementation claims verified by current file presence:

- Core UI primitives:
  `platform/core/src/main/java/panetina/elarion/core/client/ui/`
- Core commands:
  `platform/core/src/main/java/panetina/elarion/core/command/`
- Core storage:
  `platform/core/src/main/java/panetina/elarion/core/storage/`
- Core API facades:
  `platform/core/src/main/java/panetina/elarion/core/api/`
- NPC addon implementation:
  `addons/npcs/src/main/java/panetina/elarion/addons/npcs/`
- NPC networking:
  `addons/npcs/src/main/java/panetina/elarion/addons/npcs/network/`
- Offering/Shrine implementation:
  `addons/offerings/src/main/java/panetina/elarion/addons/offerings/`
- Offering networking:
  `addons/offerings/src/main/java/panetina/elarion/addons/offerings/network/`
- Economy implementation:
  `addons/economy/src/main/java/panetina/elarion/addons/economy/`
- Worlds implementation:
  `addons/worlds/src/main/java/panetina/elarion/addons/worlds/`
- Optimization implementation:
  `addons/optimization/src/main/java/panetina/elarion/addons/optimization/`
- Security foundation:
  `addons/security/src/main/java/panetina/elarion/addons/security/`
- Angling foundation:
  `addons/angling/src/main/java/panetina/elarion/addons/angling/`

UNVERIFIED or partly speculative documentation:

- `docs/systems/Maps.md`: Atlas/map system is future-facing.
- `docs/systems/Teams.md`: team behavior overlaps Minecraft teams and Core
  Realm presentation; verify before implementation.
- Most Government, Ledger, Trade, Adventure Guild, Portal-ticket, War, and
  Contract material in `PLANS.md` is future design, not current source truth.
- Fabric/NeoForge reference docs are local knowledge notes backed by official
  references and local clones, but they are not exhaustive API specifications.

## Files Updated

- `.gitignore`: added documentation/reference ignore policy.
- `AGENTS.md`: added mandatory feature implementation workflow.
- `CODEX.md`: added fast navigation for systems, networking, GUI, persistence,
  commands, references, and porting docs.
- `docs/ai/AI_SEARCH_HINTS.md`: added first-pass workflow and audit lookup.
- `docs/systems/README.md`: clarified verified vs future system docs.
- `LORE.md`: corrected currency registry ID to `elarion:currency`.
- `docs/architecture/KNOWLEDGE_MAP.md`: created repository knowledge map.
- `docs/reports/REPOSITORY_AUDIT_REPORT.md`: created this report.

## Files Created

- `docs/architecture/KNOWLEDGE_MAP.md`
- `docs/reports/REPOSITORY_AUDIT_REPORT.md`

## Files Archived

None. No useful historical information was deleted.

## Duplicate Documents Found

- `INDEX.md` and `CODEX.md` both contain navigation. Kept both:
  `INDEX.md` is authoritative ownership/navigation, `CODEX.md` is quick Codex
  operating context.
- `TODO.md` and `PLANS.md` overlap on Season 2 direction. Current split is
  acceptable: `TODO.md` contains actionable work, `PLANS.md` contains design
  philosophy and future systems.
- `OPTIMIZATION_TRACKER.md` duplicates some completed optimization work already
  moved into `RULES.md`, `TODO.md`, and focused docs. Retained as historical
  tracker; future cleanup can archive it once no longer useful.
- Addon-local Angling docs duplicate some root docs. Retained because they are
  porting/reference notes for that addon, not general architecture.

## Missing Documentation

Missing or thin docs to consider later:

- Dedicated docs for `addons/worlds` as a system-level page, if worldgen work
  grows beyond the current addon doc.
- Dedicated docs for `addons/optimization` and `addons/security` under
  `docs/systems/` only if they become large cross-addon systems.
- Dedicated web/bridge architecture docs before website integration starts.
- Dedicated Offering reward/deferred-grant docs once that implementation
  resumes.

## Incorrect Documentation Fixed

- `LORE.md` said the registered Sigil item was `elarion:sigil`. Current
  registry/resource direction is `elarion:currency`, with visible name
  configured as Sigil/Sigils.

## Architecture Concerns

- The repository has many local docs and reference files. This improves AI
  navigation, but it can become noise if every idea becomes a new document.
  Keep `INDEX.md` as the single entry point and prefer cross-links.
- Future website integration should not read mutable runtime files directly.
  It should consume explicit Core/addon APIs, snapshots, or generated read
  models.
- Raw history JSONL should stay audit/backing storage. Player-facing Chronicle,
  ledger, newspaper, search, and rumor views should use indexes, archives, or
  dedicated read models.
- Offerings reward distribution work is paused. See
  `SHRINE_OFFERINGS_RESUME.md` before continuing.

## Planning Cleanup Summary

- `TODO.md`: mostly actionable and current, but large. Keep using it as active
  work only.
- `PLAN.md`: short memory/read-order file. Valid but overlaps with `CODEX.md`;
  keep unless it becomes stale.
- `PLANS.md`: future design book. Valid, but should not be used as proof of
  implementation.

## Lore Consistency Findings

- Current lore direction is consistent with the civilization/social-memory
  design.
- Folklore is correctly separated from runtime Chronicles.
- Currency registry wording was stale and was corrected.
- No new lore was invented in this audit.

## Optimization Tracker Findings

- Most near-term optimization items are completed and duplicated into permanent
  rules/docs.
- Remaining optimization work is correctly future-gated: command integration
  tests, stable ticking block-entity hooks, and persistent snapshots only if
  live diagnostics justify IO.

## Navigation Improvements

- Added `docs/architecture/KNOWLEDGE_MAP.md`.
- Added mandatory workflow to `AGENTS.md`.
- Added fast source-location lookup to `CODEX.md`.
- Added audit lookup guidance to `docs/ai/AI_SEARCH_HINTS.md`.
- Kept `INDEX.md` as the intended committed navigation entry point.

## Documentation Consolidation Decisions

- Did not delete or archive docs automatically because the worktree contains
  intentional ongoing documentation/lore moves.
- Did not rewrite Fabric/NeoForge reference docs; they are useful as local
  lookup material and should remain source-backed by official docs and clones.
- Did not create many new system docs; current priority is navigation quality,
  not document count.

## Recommended Maintenance Tasks

1. Decide whether root docs other than `INDEX.md` should stay tracked despite
   the new ignore policy. If yes, force-add intentionally or adjust `.gitignore`.
2. Before website integration, create one focused web/bridge architecture doc
   covering read models, account linking, whitelist intake, security, and API
   boundaries.
3. Resume Offerings from `SHRINE_OFFERINGS_RESUME.md`, then update
   `TODO.md`, `INDEX.md`, and `docs/addons/offerings.md`.
4. Add source line references to individual system docs during the next code
   change that touches each system.
5. Archive `OPTIMIZATION_TRACKER.md` only after confirming no active checklist
   item remains.
