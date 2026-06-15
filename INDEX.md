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
- Core owns canonical truth and shared infrastructure.
- Addons extend Core with feature-specific behavior and runtime state.
- `wiki/` is the human-readable manual.
- `docs/` is the technical reference and architecture layer.

## Core Docs

- [AGENTS.md](AGENTS.md)
- [CODEX.md](CODEX.md)
- [RULES.md](RULES.md)
- [README.md](README.md)
- [TODO.md](TODO.md)
- [PLAN.md](PLAN.md)
- [PLANS.md](PLANS.md)
- [LORE.md](LORE.md)
- [OPTIMIZATION_TRACKER.md](OPTIMIZATION_TRACKER.md)

## Architecture

- [Project Structure](docs/architecture/PROJECT_STRUCTURE.md)
- [Dependency Graph](docs/architecture/DEPENDENCY_GRAPH.md)
- [Knowledge Map](docs/architecture/KNOWLEDGE_MAP.md)

## Systems

- [Core system docs](docs/systems/README.md)
- `docs/systems/NPCs.md`
- `docs/systems/Realms.md`
- `docs/systems/Treasury.md`
- `docs/systems/CommunityContribution.md`
- `docs/systems/Chronicles.md`
- `docs/systems/Teams.md`
- `docs/systems/Maps.md`
- `docs/systems/Permissions.md`
- `docs/systems/GUI.md`
- `docs/systems/Government.md`
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
  - `addons/portals`
  - `addons/worlds`
  - `addons/realms`
  - `addons/names`
  - `addons/titles`
  - `addons/optimization`
  - `addons/security`
  - `addons/angling`
- Shell/foundation modules:
  - `addons/jail`
  - `addons/newspapers`
  - `addons/tablist`
  - `addons/underworld`
  - `addons/voicechat-hooks`

## Ignore Unless Explicitly Requested

- `addons/angling/reference/**`: upstream reference material for future
  Angling porting only. Do not include it in ordinary project audits,
  architecture decisions, or source searches.

## References

- [Fabric reference docs](docs/fabric-reference/)
- [NeoForge reference docs](docs/neoforge-reference/)
- [Porting docs](docs/porting/)
- [Reference setup report](docs/REFERENCE_SETUP_REPORT.md)

## Reports

- `docs/reports/`
- `docs/REFERENCE_SETUP_REPORT.md`
- `docs/reports/REPOSITORY_AUDIT_REPORT.md`

## Lore

- `LORE.md`
- `lore/folklore/`

## Working Rule

If a document conflicts with source, update the document instead of building on
the stale text.

Every addon follows the Core domain-event and notification contract in
`RULES.md` and `docs/addons/core.md`. Addon docs record meaningful emitted
events, notification projections, and intentional noise exclusions.
