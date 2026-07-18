# AI Search Hints

Use these shortcuts before adding new code.

Mandatory first pass:
Read `RULES.md`, `AGENTS.md`, `CODEX.md`, `INDEX.md`,
`docs/ai/CURRENT_STATUS.md`, the relevant `docs/systems/*.md`, and
`docs/architecture/DEPENDENCY_GRAPH.md`. Then search source with `rg` before
creating new infrastructure.

Before finishing a change:
Use the canonical documentation maintenance matrix in `RULES.md`, then update
only the docs affected by the actual ownership, command, config, API, packet,
UI, permission, event, notification, or addon-status change.

Need to change NPC dialogue:
Look in `addons/npcs/src/main/java/panetina/elarion/addons/npcs`, `config/elarion/addons/npcs`, and `docs/systems/NPCs.md`.

Need to add a new packet:
Look in `docs/fabric-reference/Networking.md`, `docs/systems/Networking.md`, existing payload records under Core/NPCs/Offerings, and addon initializers that register payloads.

Need to add a new GUI:
Look in `platform/core/src/main/java/panetina/elarion/core/client/ui`,
`docs/systems/GUI.md`, `ElarionCollectionScreen`, `NpcDialogueScreen`,
`ShrineOfFoundationScreen`, Government screens, Portal confirmation/HUD
classes, and Underworld HUD/recovery screens.

Need to store player data:
Look in `CitizenService`, `CitizenRecord`, `CitizenStorage`, `EconomyStorage`, and `docs/systems/Persistence.md`. Core owns identity/citizen truth.

Need to store world or addon state:
Look in `JsonStateStorage`, `OfferingStorage`, `NpcPlacementStorage`,
`EconomyStorage`, Government/Groups/Portal/Underworld/Mount storage, and
`docs/fabric-reference/PersistentData.md`.

Need to add a Realm feature:
Look in Core Realm/citizen services, `addons/realms`, `docs/systems/Realms.md`, and `docs/architecture/DEPENDENCY_GRAPH.md`.

Need to add Economy behavior:
Look in `addons/economy`, `ElarionEconomyApi`, `EconomyTransactionService`, and `docs/systems/Treasury.md`.

Need to add Shrine/Offering behavior:
Look in `addons/offerings`, `OfferingService`, `ShrineOfFoundationScreen`, and `docs/systems/CommunityContribution.md`.

Need to add quest/NPC story behavior:
Look in `addons/quests`, `docs/systems/Quests.md`,
`docs/addons/quests.md`, NPC dialogue config, and Core action/condition
registries. NPCs should dispatch quest actions/conditions; Quests owns
questline state.

Need to add Chronicle/search/newspaper/ledger/radio/NPC rumor views:
Read `docs/systems/Chronicles.md`, `docs/history.md`, and use `api.publicHistory()` before reading raw files.

Need to add permissions:
Look in `AbilityService`, command registrars, `docs/systems/Permissions.md`, and command GameTests.

Need to add a command:
Look in `docs/fabric-reference/Commands.md`, `docs/commands.md`, `platform/core/.../command`, and addon command packages.

Need to work on death/True Death:
Read `docs/systems/Underworld.md`, `docs/systems/Characters.md`,
`docs/addons/underworld.md`, Core character lifecycle services, and
`addons/underworld`.

Need to work on mounts:
Read `docs/addons/mounts.md`, then use `addons/mounts/src/main/java` and
`addons/mounts/src/main/resources/assets/elarion_mounts`. Active-mount UI uses
Core Collection contracts in `platform/core/.../network` and
`ElarionCollectionService`; the Collection screen layout is in
`ElarionCollectionScreen`.

Need to port NeoForge code to Fabric:
Read `docs/porting/NEOFORGE_TO_FABRIC.md` and the focused mapping file under `docs/porting/`.

Need to find Fabric patterns:
Read `docs/fabric-reference/FeatureDiscovery.md` first, then the specific Fabric reference file.

Need to use external source references:
Look in `external/fabric-api`, `external/fabric-loom`, `external/yarn`, `external/example-mods/fabric-example-mod`, and `external/neoforge`.

Need to know whether a doc is authoritative or speculative:
Start with `INDEX.md` and `docs/ai/CURRENT_STATUS.md`, then read
`docs/reports/REPOSITORY_AUDIT_REPORT.md`. Treat `RULES.md`, `AGENTS.md`, and
`CODEX.md` as the authority chain for future sessions.
