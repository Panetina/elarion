# Elarion

Fabric 1.21.1 multi-module Minecraft project.

Start with `INDEX.md`, `RULES.md`, `AGENTS.md`, `CODEX.md`, and
`docs/ai/CURRENT_STATUS.md`. Core owns canonical truth and shared
infrastructure; addons extend behavior without duplicating Core state.

Use `TODO.md` for current work, `PLANS.md` for future ideas, and
`docs/architecture/PROJECT_STRUCTURE.md` for the source map.

Active foundations include Core, Economy, Offerings, Government, Groups, NPCs,
Quests, Portals, Worlds, Realms, Underworld, Mounts, and shared presentation
addons.

Build all deployable Core/addon jars into one export folder with:

```text
.\gradlew.bat exportMods
.\gradlew.bat rebuildExportMods
```

The export folder is `build/export/mods`. `rebuildExportMods` deletes the old
export first, then rebuilds Core plus every `addons:*` module.
