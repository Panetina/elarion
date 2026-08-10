# Elarion

Fabric 1.21.1 multi-module Minecraft project.

Start with `RULES.md`, `AGENTS.md`, and the bounded
`docs/ai/CURRENT_STATUS.md`. Use `dev/tools/ai-context.ps1` and
`docs/ai/routes.json` to select task-specific source/docs; `INDEX.md` remains
the complete human navigation map. Core owns canonical truth and shared
infrastructure; addons extend behavior without duplicating Core state.

Use `TODO.md` for current work, `PLAN.md` for the approved roadmap, and
`docs/architecture/PROJECT_STRUCTURE.md` for the source map.

Active foundations include Core, Economy, Offerings, Government, Guilds, NPCs,
Quests, Portals, Worlds, Realms, Underworld, Mounts, and shared presentation
addons.

Build all deployable Core/addon jars and the curated modpack dependency jars
into export folders with:

```text
.\gradlew.bat exportMods
.\gradlew.bat rebuildExportMods
```

The export contains exactly two install roots:

- `build/export/server`: server `mods/`, managed `config/`, and
  `manifest.json`.
- `build/export/client`: client `mods/`, managed/default `config/`, shared
  `resourcepacks/`, and launcher `manifest.json`.

`rebuildExportMods` deletes `build/export` first, then rebuilds Core plus every
release-enabled `addons:*` module and recopies the canonical pinned dependency
set from `distribution/mods.json`. See `docs/systems/Distribution.md`.

After local QA is accepted and the live server is stopped, the guarded release
path is:

```text
.\gradlew.bat deployLiveServerMods -PliveDeployConfirmed=true -PserverStopped=true
```

This runs the full module/context verification, rebuilds the exports, stages
the server jars over SFTP, preserves the previous remote `mods` directory, and
promotes the staged set. It never starts or restarts PebbleHost. See
`docs/systems/LiveDeployment.md`.

Development runtime sync installs the canonical server/client performance and
gameplay sets. Distant Horizons and Bobby are required on all clients. Axiom
and WorldEdit are installed on the server and `dev/run/client-one`; they are
not included in other development clients or the player launcher export.
