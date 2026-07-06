# Elarion Project Structure

Elarion is a Fabric 1.21.1 multi-module project. Core owns canonical server truth; addons provide optional systems that consume Core APIs and may own only their feature-specific runtime state.

## Root Modules

- `platform/core`: canonical citizens, Realms, titles, abilities, identity
  sync, rewards, history, public history, notifications, character lifecycle,
  commands, task queues, UI primitives/theme infrastructure, modular Collection
  menu shell, and shared APIs.
- `addons/*`: feature modules loaded through the custom `elarion:addon`
  entrypoint. Core topologically orders addon providers by required Fabric
  dependencies before invoking them.
- `dev`: development runtime that loads Core and addons together.
- `tests/gametest`: Fabric GameTest coverage and command integration support.
- `docs`: architecture, API, config, command, addon, performance, and reference documentation.
- `external`: local upstream reference repositories. These are research inputs, not Elarion source modules.

## Addon Modules

- `addons/economy`: currency item, deposited balances, treasuries, transactions, Economy pulse/governor foundation, reward and NPC action handlers.
- `addons/offerings`: Shrine of Foundation block/item, project definitions, instances, anchors, Offering progress, donation records, Shrine UI, and Offering milestones.
- `addons/npcs`: dedicated static NPC entity, placements, skins, portraits, dialogue definitions, dialogue sessions, prompt handling, and NPC commands.
- `addons/quests`: questline YAML definitions, scoped shared/player quest
  runtime state, registered quest actions/conditions, bounded scheduled
  consequences, Quest notifications, and Shrine display-name projections.
- `addons/realms`: Realm protection policy and region interaction protections.
- `addons/worlds`: managed worlds, borders, spawn protection, world rules, abundance rules, and processed-chunk state.
- `addons/optimization`: performance diagnostics, queue reporting, hotzone/trend foundations.
- `addons/security`: security evidence/status foundation.
- `addons/angling`: fishing definitions, bounded condition/candidate selection,
  ephemeral player sessions, and catch-result telemetry foundation.
- `addons/groups`: public player groups, tags, invites, one-public-group
  membership, group chat, and Confederation delegate integration.
- `addons/names`: client and identity presentation mixins.
- `addons/titles`: title rendering client hooks.
- `addons/tablist`: tablist integration shell.
- `addons/portals`: linked scheduled portal routes, physical tickets, return
  entitlements, thin portal fields, confirmation UI, and travel validation.
- `addons/government`: Civic Forum, Seat of Rule, government forms, founding
  votes, offices, laws/civic records, authority chat, and authority markers.
- `addons/underworld`: death capture, component-safe corpse/grave recovery,
  recovery vaults, Underworld sessions, Soul Fractures, client status
  HUD/recovery UI, and True Death handoff to Core Character Lifecycle.
- `addons/mounts`: native Fabric rideable flying mount entity, Collection-menu
  unlock/active-mount state, deprecated legacy whistle ids/icons, movement
  profiles, GeckoLib geo/animation rendering, rider/camera presentation, and
  active mount session recovery.
- `addons/jail`, `addons/newspapers`, `addons/voicechat-hooks`: shell or early
  integration points for future systems.

## Networking Systems

Core networking:

- Identity sync payloads and request payloads.
- UI theme sync payloads.
- Character creation and Realm assignment payloads.
- Notification snapshot/action/dismiss/claim payloads.

NPC networking:

- Open/update/close dialogue payloads.
- Option select payloads.
- Numeric prompt submit payloads.
- Visual sync payloads for client NPC rendering.

Quest networking:

- None in V1. Quests mutate through server-side registry actions, commands,
  notifications, and other addon UIs such as NPC dialogue or Shrines.

Offering networking:

- Shrine UI snapshot payloads.
- Shrine contribution submit payloads.

Portal networking:

- Route visual synchronization.
- Travel prompt, confirmation, and screen-close payloads.
- Route status synchronization.

Government networking:

- Government UI open/action payloads.

Underworld networking:

- Underworld status synchronization.
- Grave open/recover payloads.

Mount networking:

- Compact held-input payloads for authoritative native mount movement.

Pattern:

- Register typed payloads with Fabric `PayloadTypeRegistry`.
- Client sends IDs and input only.
- Server validates and mutates.
- Server sends authoritative snapshots back to clients.

## GUI Systems

- Shared UI primitives live in `platform/core/src/main/java/panetina/elarion/core/client/ui`.
- The modular Collection menu shell and generic collection packets live in
  Core; addons contribute server-authoritative tabs.
- Current consumers:
  - NPC dialogue UI.
  - Shrine of Foundation UI.
  - Government Civic Forum/Seat/status screens.
  - Portal confirmation and route HUD.
  - Underworld timer HUD and grave recovery screen.
  - Mount rider/camera presentation.
- Core owns visual theme snapshots through `config/elarion/core/ui_theme.yml`.
- Addons own screen-specific layout, behavior, and payloads.

Future GUI work should extend these primitives before introducing a new UI framework.

## NPC Systems

- Static NPCs use `elarion_npcs:npc`, a dedicated non-AI entity.
- Runtime placement state lives under `world/elarion/addon-state/npcs/placed-npcs.json`.
- Editable NPC definitions live under `config/elarion/addons/npcs/`.
- Dialogue actions and conditional text variants route through registries and
  addon handlers.
- NPCs do not own Economy, Ledger, Government, Quest, Portal, Realm, or Offering state.

## Quest Systems

- Editable quest package definitions live under
  `config/elarion/addons/quests/questlines/` as folder packages or legacy
  single-file YAML.
- Runtime quest state lives under
  `world/elarion/addon-state/quests/state.json`.
- Quests owns scoped questline/player variables, flags, evidence, endings,
  actor bindings, and scheduled consequences. It does not own NPC placements,
  Offering progress, Realm membership, titles, rewards, or Government state.
- NPCs, Offerings, rewards, and future addons integrate through Core
  action/condition registries or `ElarionQuestsApi`, not direct file reads.

## Realm Systems

- Core owns Realm definitions, membership, leaders, relationships, citizen records, identity, and visibility truth.
- Realm protection extends behavior through `addons/realms` without becoming the owner of Realm identity.
- Realm-specific commands and history are Core-facing and should not read addon state directly unless the addon owns that feature.

## Group Systems

- `addons/groups` owns public player groups, leaders, invites, membership, and
  group chat.
- Groups consumes Core citizen/identity/history truth and Economy creation-fee
  sinks.
- Future Confederation logic must consume `ElarionGroupsApi` instead of reading
  group runtime files directly.

## Contribution / Offering Systems

- Legacy Contribution terminology is being replaced with Shrine/Offering terminology.
- `addons/offerings` owns Offering projects, instances, anchors, progress, donation records, and Shrine UI.
- Economy owns currency balances and transactions.
- Core owns reward definitions, history, citizens, active-citizen eligibility, and deferred reward grants.
- Player-facing offerings must remain server-authoritative and bounded.

## Chronicle Systems

- Core raw history is stored as monthly JSONL under `world/elarion/history/`.
- Monthly history indexes live under `world/elarion/history-index/`.
- Weekly Chronicle archives live under `world/elarion/chronicles/weekly/`.
- Public consumers should use `api.publicHistory()` rather than scanning raw history files.
- Folklore under `lore/folklore/` is curated authored content, not runtime history.

## Development Rules

- Read existing implementation before adding new systems.
- Keep definitions in config and mutable state in world storage.
- Keep hot paths bounded, cached, and event-driven.
- Do not duplicate Core-owned truth in addons.
- Add tests and docs when a feature changes ownership, persistence, commands, networking, or UI contracts.
