# Codex Project Context

## Important Folders

- `platform/core`: canonical Elarion platform code, Worldheart governing authority, character lifecycle, archives, and shared APIs.
- `addons`: feature modules loaded through the `elarion:addon` entrypoint.
- `addons/economy`: currency, bank balances, treasuries, transactions, Economy pulse.
- `addons/offerings`: Shrine of Foundation, Offering projects, progress, donation records, Shrine UI.
- `addons/npcs`: static NPCs, dialogue, skins, portraits, NPC UI.
- `addons/quests`: data-driven questlines, scoped quest state, quest actions/conditions, Quest notifications.
- `addons/realms`: Realm protection behavior.
- `addons/worlds`: managed worlds, borders, abundance rules.
- `addons/government`: Civic Forum, Seat of Rule, founding votes, offices, authority chat.
- `addons/groups`: public groups, tags, invites, group chat, Confederation hooks.
- `addons/portals`: linked A/B gates, tickets, portal fields, return entitlements.
- `addons/names`: identity/nameplate/tablist presentation hooks.
- `addons/titles`: title rendering hooks.
- `addons/optimization`: performance diagnostics.
- `addons/security`: security/evidence foundation.
- `addons/angling`: fishing foundation; ignore `addons/angling/reference/**` unless porting resumes.
- `addons/underworld`: death capture, dedicated graves, recovery vaults, Underworld sessions, Soul Fractures.
- `addons/mounts`: native Fabric rideable mount entity, Collection-menu unlock/active-mount state, converted geo assets for all seven V1 mounts, native mount input, and GeckoLib mesh/animation rendering.
- `addons/jail`, `addons/newspapers`, `addons/tablist`, `addons/voicechat-hooks`: shell/foundation modules.
- `tests/gametest`: Fabric GameTest and command integration coverage.
- `docs`: implementation contracts and architecture notes.
- `external`: cloned Fabric/NeoForge reference repositories.
- `lore/folklore`: curated in-world folklore/book source material.

## Best Starting Files

- `AGENTS.md`: local engineering rules for Codex.
- `README.md`: project summary and module overview.
- `RULES.md`: permanent architecture and quality policy.
- `INDEX.md`: ownership/source-of-truth dictionary.
- `TODO.md`: current work only.
- `PLANS.md`: future ideas only.
- `PLAN.md`: short project memory and read order.
- `docs/architecture/PROJECT_STRUCTURE.md`: current module/system map.
- `docs/architecture/DEPENDENCY_GRAPH.md`: dependency and separation rules.
- `docs/architecture/KNOWLEDGE_MAP.md`: documentation and source navigation tree.
- `docs/ai/CURRENT_STATUS.md`: compact current status handoff for new AI/new PC recovery.
- `docs/ai/AI_SEARCH_HINTS.md`: quick lookup guide for future sessions.

## Common Commands

```text
.\gradlew.bat runClient
.\gradlew.bat runClientOne
.\gradlew.bat runClientTwo
.\gradlew.bat runServer
.\gradlew.bat build
.\gradlew.bat exportMods
.\gradlew.bat rebuildExportMods
.\gradlew.bat :platform:core:test
.\gradlew.bat :addons:economy:test
.\gradlew.bat :addons:npcs:test
.\gradlew.bat :addons:offerings:test
.\gradlew.bat :addons:quests:test
.\gradlew.bat :addons:government:test
.\gradlew.bat :tests:gametest:runGameTest
```

Use focused module builds during iteration:

```text
.\gradlew.bat :platform:core:compileJava
.\gradlew.bat :addons:offerings:compileJava
.\gradlew.bat :addons:npcs:compileJava
.\gradlew.bat :addons:quests:compileJava
```

Live UI screenshot QA:

```text
.\gradlew.bat runServer
.\gradlew.bat runClientOne
powershell -NoProfile -ExecutionPolicy Bypass -File .\dev\tools\capture-minecraft-window.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\dev\tools\capture-minecraft-window.ps1 -Output build\ui-qa\notification-drawer.png
powershell -NoProfile -ExecutionPolicy Bypass -File .\dev\tools\minecraft-qa.ps1 -Action command -Command '/e panel'
powershell -NoProfile -ExecutionPolicy Bypass -File .\dev\tools\minecraft-qa.ps1 -Action click -X 490 -Y 88
powershell -NoProfile -ExecutionPolicy Bypass -File .\dev\tools\minecraft-qa.ps1 -Action click -Button right -X 960 -Y 520
powershell -NoProfile -ExecutionPolicy Bypass -File .\dev\tools\minecraft-qa.ps1 -Action key -Keys '{ENTER}'
powershell -NoProfile -ExecutionPolicy Bypass -File .\dev\tools\minecraft-qa.ps1 -Action move -X 318 -Y 250
powershell -NoProfile -ExecutionPolicy Bypass -File .\dev\tools\minecraft-qa.ps1 -Action scroll -X 655 -Y 375 -Wheel -1 -Count 4
powershell -NoProfile -ExecutionPolicy Bypass -File .\dev\tools\minecraft-qa.ps1 -Action capture -Output build\ui-qa\admin-panel.png
```

Use the live dev-server path for real UI QA: start `runServer`, start
`runClientOne`, then join Multiplayer using the saved `localhost` server entry.
Build or compile changed modules before starting either process. Never rebuild
Core/addon dev jars while an existing dev server is running: Loom's server and
client classpaths share those jars, and replacing one under the server can
produce `ZipFile invalid LOC header` during lazy class loading. After code
changes, stop the server, build once, restart the server, then launch clients.
The dev server uses `dev/log4j-elarion-dev.xml` through the dev Loom run
configuration. It hides only known harmless modpack/development noise such as
Forge Config API's Mod Menu recommendation and optional missing mixin targets;
real Elarion warnings, crashes, and performance warnings should remain visible.
`runClientOne` launches the full dev client as `ElarionAdmin`.
The dev client run configs pass stable local UUID/access-token values so QA
screenshots do not depend on a real account session. If Multiplayer still
shows `Invalid Session`, treat that as a QA harness blocker and investigate
the run arguments/session environment before testing UI flows.
`capture-minecraft-window.ps1` captures the Minecraft window contents with
`PrintWindow`, so the screenshot is not blocked by another overlapping desktop
window. If `PrintWindow` produces a black image on a driver setup, make the
Minecraft window visible and retry with `-ScreenCapture`.
If the main menu or client appears as a tiny/white framebuffer after startup,
focus Minecraft and toggle `F11` once before continuing; this forces the client
window/framebuffer to settle and is faster than debugging shaders first.
`minecraft-qa.ps1` is the faster wrapper for repeat checks: it can focus or
maximize Minecraft, send a slash command through chat, move the native cursor
for tooltip QA, post a client-area click or mouse-wheel scroll, and delegate
screenshots to the capture helper. Focus/click actions preserve an already
maximized window.

NPC trader/banker QA helper:

```text
powershell -NoProfile -ExecutionPolicy Bypass -File .\dev\tools\npc-trade-qa.ps1 -Action setup -World minecraft:overworld -ClickX 960 -ClickY 540 -TradeButtonX 960 -TradeButtonY 503 -OutputDir build\ui-qa\npc-trade-qa
powershell -NoProfile -ExecutionPolicy Bypass -File .\dev\tools\npc-trade-qa.ps1 -Action capture-trader -World minecraft:overworld -ClickX 960 -ClickY 540 -TradeButtonX 960 -TradeButtonY 503 -OutputDir build\ui-qa\npc-trade-qa
powershell -NoProfile -ExecutionPolicy Bypass -File .\dev\tools\npc-trade-qa.ps1 -Action capture-bank -World minecraft:overworld -TradeButtonX 960 -TradeButtonY 503 -OutputDir build\ui-qa\npc-trade-qa
```

Run it only after joining the dev server. It rebuilds `worldheart_banker_1` and
`worldheart_trader_1` through normal `/e npc` commands, opens conversations via
`/e npc open <id>`, and positions the player for screenshot checks. Use
`-CloseScreens` only when you intentionally want it to send ESC before command
setup.

Coordinate rule: `minecraft-qa.ps1` click/move/scroll inputs are Minecraft
client-area coordinates. Captures include the Windows title bar, so subtract
the title bar height from screenshot Y coordinates before clicking. A maximized
main-menu Multiplayer click is around client `967,415`; clicking the same
visible screenshot Y without subtraction can hit Realms and show Minecraft's
`Invalid Session` dialog.

Useful UI entry points for manual QA:

- Admin Panel: `/e panel`.
- Character Menu / unlockables: press `C` by default. Core clears vanilla's
  default Save Hotbar Activator binding when it still owns `C`. `/charactermenu` and
  `/charactermenu` are hidden client aliases for manual use, not server
  recommendation entries.
- Notifications: click the left HUD notification rail; seed entries through
  domain actions such as Realm mail/announcements, Government, Offerings,
  Quests, rewards, and Portal/world events.
- Shrine UI: right-click a linked Shrine of Foundation block; prepare with
  `/e offerings start ...` and `/e offerings shrine link <instance>` while
  looking at the Shrine when needed.
- Government UI: right-click Civic Forum or Seat of Rule blocks. These screens
  use server-issued block sessions, so do not expect a generic direct-open
  command.
- NPC Dialogue: place or find an NPC, then interact with it. Useful setup
  commands include `/e npc place <npcDefinition> here`, `/e npc list`, and
  `/e npc tp <npcId>`. OP QA can use `/e npc open <npcId>` when already near
  a placed NPC to open the same authoritative conversation without a right-click.
- Portal Confirmation: interact with a configured/unlocked portal route while
  its route/window rules allow a prompt.
- Grave Recovery: interact with an Underworld tomb/grave after creating or
  locating a corpse through the Underworld test/admin commands.

Primary build command:

```text
.\gradlew.bat build
```

Export all deployable Elarion mod jars plus curated modpack dependency jars:

```text
.\gradlew.bat exportMods
.\gradlew.bat rebuildExportMods
.\gradlew.bat syncDevClientMods
.\gradlew.bat --stop
.\gradlew.bat cleanDevRunLocks
```

`exportMods` builds `platform:core` plus every `addons:*` remapped jar and
copies them with the curated modpack dependency jars into:

- `build/export/server-mods`: install this set on the dedicated server.
- `build/export/client-mods`: install this set on clients.
- `build/export/elarion/both`: Elarion Core/addon jars classified as
  both-side.
- `build/export/modpack/both`: curated third-party jars classified as
  both-side.
- `build/export/modpack/client`: curated client-only third-party jars such as
  Mod Menu.
- `build/export/mods`: flat Elarion-only compatibility export.

`syncDevClientMods` copies client-only jars into `dev/run/client-one/mods` and
`dev/run/client-two/mods`. Do not copy those jars into `dev/run/mods` or the
dedicated server folder.

If `runServer` or a dev client fails with locked `processedMods`, `latest.log`,
or `debug.log`, stop the old Minecraft/Gradle process first, then run
`.\gradlew.bat --stop` and `.\gradlew.bat cleanDevRunLocks`.

`rebuildExportMods` clears `build/export` first. Future included addon
projects are picked up automatically when their Gradle path starts with
`:addons:`.

Focused test/check commands:

```text
.\gradlew.bat :platform:core:test
.\gradlew.bat :addons:economy:test
.\gradlew.bat :addons:npcs:test
.\gradlew.bat :addons:offerings:test
.\gradlew.bat :addons:quests:test
.\gradlew.bat :addons:government:test
```

## Module Descriptions

- Core: config defaults/validation, read-only config descriptor registry,
  citizens, character lifecycle/archives, Realms, titles, identity, rewards,
  claimable reward notifications, history, public history, task queues,
  commands, shared UI theme, modular Collection menu, Character Menu profile
  aggregation boundary, notification HUD/drawer.
- Economy: official currency, deposited balances, treasuries, transactions, NPC/reward action handlers, Economy pulse.
- Offerings: project definitions, runtime instances, Shrine anchors, progress, player offering flow, milestone dispatch.
- NPCs: placeable static NPC entity, placement storage, dialogue engine, prompt handling, skin/portrait rendering.
- Quests: questline YAML definitions, scoped shared/player quest state, registry actions/conditions, scheduled consequences, Quest notifications, and Shrine display projections.
- Realms: Realm protection and interaction rules.
- Worlds: managed world configuration, borders, spawn protection, block/mob abundance.
- Optimization: diagnostics, queue status, world trend/hotzone foundations.
- Security: evidence/status foundation for anti-cheat and anti-AFK work.
- Angling: fishing definitions and rarity indexing foundation.
- Underworld: death capture, component-safe corpse/grave state, recovery vaults, Underworld timers, chat/travel restrictions, Soul Fractures, client status HUD/recovery UI, and True Death handoff.
- Mounts: Collection-menu active mount selection, seven rideable flying mount types, owner-only dismiss/remount, type-driven GeckoLib renderer, movement profiles, rider/camera behavior, and session recovery.
- Jail/Newspapers/Tablist/Voice Chat Hooks: shell or early integration modules; check docs/addons before extending.

## Reference Repositories

- `external/fabric-api`
- `external/fabric-loom`
- `external/yarn`
- `external/example-mods/fabric-example-mod`
- `external/neoforge`

Do not copy architecture directly from references. Translate patterns into Elarion's Core/addon design.

## Documentation Locations

- Architecture docs: `docs/architecture/`
- AI handoff/search docs: `docs/ai/`
- Fabric reference docs: `docs/fabric-reference/`
- NeoForge porting docs: `docs/porting/`
- NeoForge overview: `docs/neoforge-reference/`
- System docs: `docs/systems/`
- Addon docs: `docs/addons/`
- Setup reports: `docs/reports/` and `docs/REFERENCE_SETUP_REPORT.md`

## Documentation Updates

Use the canonical maintenance matrix in `RULES.md`. Fast reminders: command
changes update `docs/commands.md` and `wiki/admin/commands.md`; test command
changes also update `docs/test-commands.md` and `wiki/admin/test-commands.md`;
addon status changes update `INDEX.md`, this file, `AGENTS.md`,
`docs/addons/README.md`, and `wiki/addons/README.md`.

Parsed config, config-backed content, addon definition files, and Core
definition maps must keep their read-only config descriptors and descriptor
tests current in the same slice. Generated-only YAML, such as shell/foundation
defaults without a typed runtime loader, must not be presented as active
descriptor current values.

## Fast Navigation

- Systems live under `addons/*` and shared truth lives under `platform/core`.
- Networking patterns live in `platform/core/src/main/java/panetina/elarion/core/network`, `addons/npcs/.../network`, and `addons/offerings/.../network`.
- GUI logic lives in `platform/core/.../client/ui`, Core Collection screen/client, `addons/npcs/.../client`, and `addons/offerings/.../client`.
- Persistence lives in `platform/core/.../storage` plus addon `storage` packages.
- Commands live in `platform/core/.../command` plus addon `command` packages.
- Config loaders live in `platform/core/.../config` plus addon `config` packages.
- Fabric reference notes live in `docs/fabric-reference/`; external source clones live in `external/`.
- NeoForge is reference only. Porting notes live in `docs/porting/`.
- Ignore `addons/angling/reference/**` unless the task explicitly resumes Angling porting.
