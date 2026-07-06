# INDEX

Single navigation entry point for the repository.

## Authority Order

1. `RULES.md`
2. `AGENTS.md`
3. `CODEX.md`
4. `INDEX.md`
5. `TODO.md`
6. `PLAN.md`
7. `PLANS.md`
8. `LORE.md`
9. `OPTIMIZATION_TRACKER.md`

## Project Overview

- Fabric 1.21.1 is the source of truth and target platform.
- Core owns canonical truth, shared infrastructure, the modular Collection menu
  shell, and the OP Admin Panel shell.
- Addons extend Core with feature-specific behavior and runtime state.
- `wiki/` is the human-readable manual.
- `docs/` is the technical reference and architecture layer.

## Core Docs

- [AGENTS.md](AGENTS.md)
- [CODEX.md](CODEX.md)
- [RULES.md](RULES.md)
- [README.md](README.md)
- [Config reference](docs/config.md)
  - Core config descriptors and edit/mutation-readiness contracts live under
    `platform/core/src/main/java/panetina/elarion/core/config/`; config edit
    payload records/codecs live under
    `platform/core/src/main/java/panetina/elarion/core/network/`; passive
    config edit client result state lives under
    `platform/core/src/main/java/panetina/elarion/core/client/`. The inert apply
    contract is `ElarionConfigApplyRegistry` plus its registrar, transactional
    prepared change, capability, executor, context, readiness, audit, and
    internal coordinator/session contracts. `ElarionConfigApplyAuditJournal` is
    the durable JSONL audit sink for future apply execution, and
    `ElarionConfigApplyService` owns production lifecycle/readiness. Addons
    receive registration-only access through
    `ElarionApi.system().configAppliers()`; it has no production registrations
    yet.
- [TODO.md](TODO.md)
- [PLAN.md](PLAN.md)
- [PLANS.md](PLANS.md)
- [LORE.md](LORE.md)
- [OPTIMIZATION_TRACKER.md](OPTIMIZATION_TRACKER.md)

## Architecture

- [Project Structure](docs/architecture/PROJECT_STRUCTURE.md)
- [Dependency Graph](docs/architecture/DEPENDENCY_GRAPH.md)
- [Knowledge Map](docs/architecture/KNOWLEDGE_MAP.md)
- [Current AI handoff/status snapshot](docs/ai/CURRENT_STATUS.md)
- [AI search hints](docs/ai/AI_SEARCH_HINTS.md)

## Systems

- [Core system docs](docs/systems/README.md)
- `docs/systems/NPCs.md`
- `docs/systems/Quests.md`
- `docs/systems/Realms.md`
- `docs/systems/Treasury.md`
- `docs/systems/CommunityContribution.md`
- `docs/systems/Chronicles.md`
- `docs/systems/Teams.md`
- `docs/systems/Maps.md`
- `docs/systems/Permissions.md`
- `docs/systems/GUI.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/systems/Government.md`
- `docs/systems/Underworld.md`
- `docs/systems/Characters.md`
- `docs/systems/Networking.md`
- `docs/systems/Persistence.md`

## Addon Technical Docs

- [Addon docs index](docs/addons/README.md)
- [Core](docs/addons/core.md)
- [Economy](docs/addons/economy.md)
- [Offerings](docs/addons/offerings.md)
- [Government](docs/addons/government.md)
- [Groups](docs/addons/groups.md)
- [NPCs](docs/addons/npcs.md)
- [Quests](docs/addons/quests.md)
- [Portals](docs/addons/portals.md)
- [Optimization](docs/addons/optimization.md)
- [Security](docs/addons/security.md)
- [Angling](docs/addons/angling.md)
- [Worlds](docs/addons/worlds.md)
- [Realms](docs/addons/realms.md)
- [Names](docs/addons/names.md)
- [Titles](docs/addons/titles.md)
- [Jail](docs/addons/jail.md)
- [Newspapers](docs/addons/newspapers.md)
- [Tablist](docs/addons/tablist.md)
- [Underworld](docs/addons/underworld.md)
- [Mounts](docs/addons/mounts.md)
- [Voice Chat Hooks](docs/addons/voicechat-hooks.md)

## Addons

- `docs/addons/`
- `addons/`
- Active foundations:
  - `addons/economy`
  - `addons/offerings`
  - `addons/government`
  - `addons/groups`
  - `addons/npcs`
  - `addons/quests`
  - `addons/portals`
  - `addons/worlds`
  - `addons/realms`
  - `addons/names`
  - `addons/titles`
  - `addons/optimization`
  - `addons/security`
  - `addons/angling`
  - `addons/underworld`
  - `addons/mounts`
- Shell/foundation modules:
  - `addons/jail`
  - `addons/newspapers`
  - `addons/tablist`
  - `addons/voicechat-hooks`

## Ignore Unless Explicitly Requested

- `addons/angling/reference/**`: upstream reference material for future
  Angling porting only. Do not include it in ordinary project audits,
  architecture decisions, or source searches.

## References

- [UI reference images](docs/ui/)
- [Fabric reference docs](docs/fabric-reference/)
- [NeoForge reference docs](docs/neoforge-reference/)
- [Porting docs](docs/porting/)
- [Reference setup report](docs/REFERENCE_SETUP_REPORT.md)

## Reports

- `docs/ai/CURRENT_STATUS.md`
- `docs/ai/AI_SEARCH_HINTS.md`
- `docs/reports/`
- `docs/reports/PROJECT_REVAMP_AUDIT.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `docs/REFERENCE_SETUP_REPORT.md`
- `docs/reports/REPOSITORY_AUDIT_REPORT.md`
- `docs/reports/WORKTREE_CLEANUP_AUDIT.md`

## Git Documentation Policy

- Root authority docs, `docs/**/*.md`, and `wiki/**/*.md` are project
  knowledge and should be commit-ready.
- `external/` and `lore/folklore/` remain local/reference material unless a
  future decision promotes specific files.
- Do not create new Markdown islands unless they are linked from this index,
  `AGENTS.md`, or the wiki.

## Test Commands

- [Technical test command contract](docs/test-commands.md)
- [Admin wiki test command guide](wiki/admin/test-commands.md)
- Live UI screenshot capture helper:
  `dev/tools/capture-minecraft-window.ps1`
- Fast live UI driver helper:
  `dev/tools/minecraft-qa.ps1`

## Lore

- `LORE.md`
- `lore/folklore/`

## Working Rule

If a document conflicts with source, update the document instead of building on
the stale text.

Every addon follows the Core domain-event and notification contract in
`RULES.md` and `docs/addons/core.md`. Addon docs record meaningful emitted
events, notification projections, and intentional noise exclusions.

## Documentation Maintenance

`RULES.md` owns the canonical documentation maintenance matrix. This index
lists where information lives; update it when ownership, source locations,
addon status, or the repository navigation map changes.
