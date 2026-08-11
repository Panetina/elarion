# Elarion Knowledge Map

Use `INDEX.md` as the entry point. This file shows where information lives.

```text
Elarion
|-- Authority
|   |-- RULES.md
|   `-- AGENTS.md
|-- Navigation
|   |-- INDEX.md
|   |-- docs/ai/CURRENT_STATUS.md
|   |-- docs/ai/AI_SEARCH_HINTS.md
|   |-- docs/ai/routes.json
|   |-- docs/ai/archive/ (explicit history only)
|   `-- docs/architecture/KNOWLEDGE_MAP.md
|-- Architecture
|   |-- docs/architecture/PROJECT_STRUCTURE.md
|   `-- docs/architecture/DEPENDENCY_GRAPH.md
|-- Systems
|   |-- docs/systems/
|   `-- docs/addons/
|-- Source
|   |-- platform/core/
|   |-- addons/
|   |-- tests/gametest/
|   `-- dev/
|-- GUI
|   |-- platform/core/src/main/java/panetina/elarion/core/client/ui/
|   |-- addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/
|   |-- addons/offerings/src/main/java/panetina/elarion/addons/offerings/client/
|   `-- addons/government/src/main/java/panetina/elarion/addons/government/client/
|-- Networking
|   |-- platform/core/src/main/java/panetina/elarion/core/network/
|   |-- addons/npcs/src/main/java/panetina/elarion/addons/npcs/network/
|   |-- addons/offerings/src/main/java/panetina/elarion/addons/offerings/network/
|   |-- addons/portals/src/main/java/panetina/elarion/addons/portals/network/
|   |-- addons/government/src/main/java/panetina/elarion/addons/government/network/
|   |-- addons/underworld/src/main/java/panetina/elarion/addons/underworld/network/
|   `-- addons/mounts/src/main/java/panetina/elarion/addons/mounts/network/
|-- Persistence
|   |-- platform/core/src/main/java/panetina/elarion/core/storage/
|   `-- addons/*/src/main/java/**/storage/
|-- Commands
|   |-- platform/core/src/main/java/panetina/elarion/core/command/
|   `-- addons/*/src/main/java/**/command/
|-- Fabric References
|   |-- docs/fabric-reference/
|   `-- external/fabric-api, external/fabric-loom, external/yarn
|-- NeoForge References
|   |-- docs/neoforge-reference/
|   `-- external/neoforge
|-- Porting
|   `-- docs/porting/
|-- Lore
|   |-- LORE.md
|   `-- lore/folklore/
|-- Plans
|   |-- PLAN.md
|   `-- TODO.md
`-- Reports
    `-- docs/reports/
```

## Source-Backed Navigation Rules

- Source code decides implementation reality.
- `RULES.md` decides permanent policy.
- `AGENTS.md` decides local Codex behavior and source-map rules.
- `INDEX.md` decides ownership and navigation.
- `docs/ai/routes.json` decides bounded task routing; it points to authority but
  does not replace it.
- `TODO.md` decides current work.
- `PLAN.md` decides active direction and future constraints.
- `LORE.md` decides established canon.

## Exclusion Rule

`docs/ai/archive/**`, `external/**`, and `addons/angling/reference/**` are
excluded from ordinary task context. Read them only for explicit historical,
upstream, or Angling-porting work.

If a document conflicts with source, mark the document stale and update it
before building on it.
