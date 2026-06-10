# Elarion Index

Last reviewed: 2026-06-11

Author: Panyel  
Team: Panetina Team

This is the project dictionary and ownership map. Update it whenever data,
config, commands, services, models, storage, or addon responsibility changes.

Active cleanup tracker:

```text
OPTIMIZATION_TRACKER.md
```

Canonical setting reference:

```text
LORE.md
```

Numbered lost-age archive:

```text
Folklore/README.md
```

## Glossary

- **Realm**: canonical gameplay group. Can represent a kingdom, nation, faction,
  settlement, or society.
- **Worldheart**: canonical shared neutral world and future portal convergence.
- **Citizen**: UUID-based player record owned by Core.
- **Title**: Core-owned unlockable/active identity and ability container.
- **Ability**: namespaced permission/capability checked by Core and addons.
- **Progression Event**: Core event describing a player action such as kill,
  craft, advancement, region entry, stat threshold, or addon-defined event.
- **Reward Action**: config-defined action executed by Core rewards.
- **Chronicle**: weekly immutable archive generated from public history events.
- **Folklore Volume**: curated, stable-numbered history or character book from
  the lost first age. It is authored separately from generated Chronicles.
- **Echo Scroll**: planned unique discoverable object representing one
  recoverable Folklore volume.
- **Verdant Reawakening**: canonical present age, after nature reclaimed most
  of the earlier World of Elarion.
- **Green Silence**: unresolved interval between the end of the Harmonium
  Season and the Verdant Reawakening.
- **Harmonium**: historical name for the earlier shared center at the site now
  known as Worldheart.
- **Government Form**: addon-defined Realm government type such as republic,
  monarchy, council, or chiefdom.
- **Office**: Government addon position beyond the current Core leader field.
- **Society Pillar**: configurable contribution/progression category.
- **Ledger**: public civic reputation and activity record.
- **Wallet**: Economy addon player currency balance.
- **Treasury**: Economy addon Realm currency balance.
- **Sigil**: official Elarion currency minted by the Worldheart Treasury.
  Every physical Sigil bears the Elarion Seal.
- **Elarion Seal**: heraldic mark representing the shared civilization and
  unity of the Realms; displayed on official Sigils.
- **Worldheart Market**: planned closed official market district where players
  pay access fees and trade through server-validated manual stalls.
- **Seller Session**: planned temporary paid permission to sell manually in the
  Worldheart Market while online and present.
- **Portal Ticket**: planned consumed item or wallet entitlement required for
  ticketed portal travel.
- **Economy Governor**: planned Economy addon monitor that tracks faucets,
  sinks, prices, market activity, and concentration.
- **Adaptive NPC Price**: planned bounded NPC offer price with base, min, max,
  current value, trend, and reason.
- **Faucet**: system that creates sigils.
- **Sink**: system that permanently removes sigils.
- **Social Memory**: durable public record that makes meaningful actions part
  of the world through history, ledgers, Chronicles, newspapers, NPC rumors,
  monuments, offices, and ceremonies.
- **Realm Active Need**: planned small useful task request from a Realm, such as
  materials, food, votes, courier work, scouts, or defenders.
- **Civic Quest**: planned onboarding or society quest that teaches a citizen
  how to help their Realm and records first contributions.
- **Public Good Project**: planned contribution project that unlocks Realm-wide
  benefits when enough citizens help.
- **War Objective**: planned non-grief conflict goal such as banner capture,
  generated supply raid, outpost siege, caravan ambush, relic contest, or portal
  disruption.
- **War Fatigue**: planned Realm pressure that makes repeated wars costlier and
  less rewarding until peace or time reduces it.
- **Diplomatic Summit**: planned in-world meeting where leaders negotiate and
  sign major treaty proposals before citizen ratification.
- **Treaty Flag**: planned specific treaty benefit such as direct trade, portal
  discount, shared map visibility, alliance chat, defense pact, ticket subsidy,
  or market tax discount.
- **Treasury Audit**: planned public record of who proposed spending, who
  approved it, who benefited, and what changed.
- **Public Recognition Feed**: planned stream of earned public notices consumed
  by notifications, newspapers, ledgers, NPC rumors, and Chronicles.
- **Realm Ceremony**: planned public ritual for milestones such as project
  completion, elections, treaties, war outcomes, or major unlocks.
- **NPC**: planned placeable server-authoritative character or terminal with a
  durable ID, skin/profile, dialogue tree, and registered action buttons.
- **NPC Skin/Profile**: planned config-defined presentation data for an NPC,
  such as player-like skin reference, texture ID, portrait, or future modded
  NPC adapter profile.
- **Dialogue Session**: planned compact per-player NPC interaction state owned
  by the NPCs addon.
- **Task Queue**: Core-owned bounded background/server-thread work queue.
- **Grouped API Facade**: focused `ElarionApi` surface for identity, Realm,
  messaging, progression, or system work.
- **Security Evidence**: future moderation record for suspicious player,
  automation, or chunk-loading behavior.
- **Vanilla+ Modpack**: expected server environment where Elarion runs beside
  farming, food, villager, terrain, mob, backpack, voice, camera, knowledge,
  storage, and utility mods.

## Source Tree

```text
Folklore/
  README.md       permanent volume catalog and recovery contract
  chronicles/     numbered public histories and legends, volumes 001-099
  characters/     character-only books, volumes 101-199

platform/core/
  src/main/java/panetina/elarion/core/
    api/          ElarionApi, addon API, command registry
    command/      player/help/admin command registration and policy
    config/       Core YAML defaults, loading, validation helpers
    event/        Core event bus
    mixin/        server/client integration mixins
    model/        Core data models
    network/      identity sync payloads
    registry/     shared condition/action/requirement/milestone registries
    service/      Core services
    storage/      Core world-state storage and shared state helpers
  src/main/resources/
    fabric.mod.json
    elarion_core.mixins.json
    assets/
  src/test/
    Core unit tests

addons/
  realms/             Realm protection and Realm addon behavior
  worlds/             managed worlds, borders, templates, spawn/world rules
  optimization/       task queue diagnostics and performance monitoring
  economy/            currency, wallets, treasuries, transactions, monitoring
  security/           anti-cheat and anti-AFK farm protection foundation
  names/              optional nickname presentation integration
  titles/             title rendering addon
  contributions/      future contribution/progression projects
  government/         future government forms, offices, laws, votes
  portals/            future physical portals, fees, schedules
  jail/               future jail rules
  underworld/         future underworld/death flow
  newspapers/         future newspaper permissions/tools/UI
  npcs/               future placeable NPCs, skins, dialogues, and sessions
  tablist/            future advanced tab ordering/presentation
  voicechat-hooks/    future Simple Voice Chat restrictions

tests/gametest/       server GameTests and command harness
dev/                  combined local dev launcher only
```

## Core Config Index

```text
config/elarion/core/
  realms.yml              Realm definitions, colors, visibility, spawns, flags
  citizens-defaults.yml   default citizen status/title/flags
  titles.yml              title definitions, abilities, ownership, active effects
  title-progression.yml   title unlock rules and named progression regions
  abilities.yml           ability descriptions
  identity.yml            nickname/title identity settings
  chat.yml                local, whisper, yell, Realm, alliance, notices, spy
  visibility.yml          visibility defaults
  history.yml             history recording filters and live query bounds
  rewards.yml             reward bundles and action definitions
  commands.yml            command roots and permission defaults

config/elarion/addons/
  optimization/performance.yml      host profile, monitoring, and task budget defaults
  economy/economy.yml                persistence, query, and Governor monitor settings
  security/security.yml             evidence, anti-cheat, anti-AFK farm defaults
```

## Planned Addon Config Index

```text
config/elarion/addons/
  government/
    government.yml
    activity.yml
    forms/<form-id>/form.yml
    forms/<form-id>/offices.yml
    forms/<form-id>/actions.yml
    forms/<form-id>/laws.yml
    forms/<form-id>/transitions.yml
    laws/categories.yml
    laws/effects.yml
    votes/common.yml

  contributions/
    projects/<project-id>.yml
    society.yml
    requirements.yml
    blueprints/<blueprint-id>.json

  economy/
    currency.yml
    bank.yml
    interest.yml
    treasury.yml
    governor.yml
    adaptive-prices.yml

  trade/
    trade.yml
    market.yml
    stalls.yml
    access_policy.yml

  adventure-guild/
    quests/<quest-group>.yml
    quest_ui.yml

  npcs/
    npcs.yml
    skins.yml
    dialogues/<dialogue-id>.yml

  ledger/
    ledger.yml
    badges.yml

  portals/
    access.yml
    tickets.yml
    fees.yml
    schedules.yml
```

## Runtime State Index

```text
world/elarion/
  citizens/                         Core citizen records
  history/                          Core JSONL history events
  history-index/                    Core monthly history projections
  chronicles/weekly/                Core immutable weekly Chronicle archives
  player-stats/                     Core player stat counters
  progression/title-progress/       Core continuous title progress
  title-claims.json                 Core globally unique title claims
  addon-state/
    realms/
      realm-state.json              Core base relationships, hiding, decisions
      deliveries.json               Core pending Realm deliveries/mail/rewards
    government/                     future government runtime state
    contributions/                  future project/progression runtime state
    economy/
      economy-state.json            wallet/treasury snapshot and last sequence
      transactions/<yyyy-MM>.jsonl  append-only audited transaction journal
    trade/                          future market access, seller sessions,
                                     stalls, trade transactions
    adventure-guild/                future quest progress/cooldowns
    npcs/
      placed-npcs.json              future placed NPC durable IDs, locations,
                                     skins/profiles, and dialogue links
      sessions/                     future per-player dialogue/session state
    ledger/                         future civic stats and badges
    portals/                        future portal runtime state
    optimization/                    future performance snapshots and diagnostics
    security/
      evidence.json                  security evidence counters and summary state
```

## Current Core Services

- `CitizenService`: loads/saves UUID citizen records.
- `RealmService`: Realm definitions, membership lookup, scoreboard teams.
- `RealmService.ownerForWorld`: cached Realm-world ownership lookup.
- `TitleService`: title ownership, active title, unique claims, title grants.
- `ProgressionService`: progression events, title unlock rules, regions,
  trigger-indexed rules, interval-indexed continuous rules, world-indexed
  regions, continuous progress, crafting/advancement hooks, active title
  effects.
- `PlayerStatsService`: event-driven stat counters with dirty saves.
- `AbilityService`: title/citizen ability checks and explicit grants.
- `IdentityService`: derived display/chat/tab/nameplate identity.
- `IdentitySyncService`: server-to-client identity sync with per-tick
  coalescing for repeated full, viewer, and subject intents.
- `NicknameService`: nickname validation and formatting.
- `ChatService`: local chat, `/rc`, `/ac`, `/w`, `/yell`, spy delivery.
- `PrivateMessageService`: `/pm` and `/r` visibility-safe messages.
- `RealmGovernanceService`: current Core relationship, hiding, leader, decision
  groundwork.
- `RealmSpawnService`: Realm spawn routing.
- `RealmDeliveryService`: pending Realm delivery/mail/reward basics.
- `RewardActionService`: reward bundle execution.
- `HistoryService`: durable history, category/type recording policy, bounded
  newest-month queries, monthly index projections, weekly Chronicle archive
  generation through Core task queues, public-history composition, batched
  monthly JSONL writes, and Chronicle-ready prose.
- `ChronicleArchiveStorage`: immutable weekly Chronicle archive JSON records
  under `world/elarion/chronicles/weekly/`.
- `ElarionTaskService`: host-agnostic conservative task queues for IO, compute,
  and bounded server-thread apply work, with server/IO/compute diagnostics.
- `ElarionPerformanceMonitor`: shared slow-operation counters and rate-limited
  warnings for Elarion-owned work.
- `CoreConfigDefaults`: focused Core default config writer.
- `CoreConfigHistorySupport`: history config loading, default-appending, and
  validation support for recording, Chronicle archives, and public queries.
- `CoreConfigReferenceValidator`: current Core cross-file reference validation
  for built-in config references.
- `JsonStateStorage`: shared atomic JSON runtime-state writes and world-save
  path helpers.
- `DirtyTracker`: shared UUID dirty-set helper.

## Focused Documentation

```text
docs/api.md                  grouped API and registry execution contracts
docs/commands.md             command surface and future command test matrix
docs/performance.md          task queues, budgets, and performance rules
docs/config.md               config/runtime-state lifecycle
docs/history.md              history storage, filters, Chronicles, public memory
docs/operations.md           TPS workflow, generated files, config regeneration
docs/addons/core.md          Core ownership contract
docs/addons/worlds.md        Worlds addon contract
docs/addons/realms.md        Realms addon contract
docs/addons/names.md         Names addon contract
docs/addons/titles.md        Titles addon contract
docs/addons/optimization.md  Optimization addon contract
docs/addons/security.md      Security addon contract
```

Prefer these focused files when a future change touches only one area.

## Lore Ownership

- `LORE.md`: canonical setting facts and unresolved canon boundaries.
- `Folklore/README.md`: stable volume numbering and archive rules.
- `Folklore/chronicles/`: authored historical narratives and attributed
  legends.
- `Folklore/characters/`: one character per book, using roleplay identity only.
- Core History: runtime event truth and generated Chronicle source.
- Future Adventure Guild/NPC integration: discovery and return quests.
- Future archive display state: immutable unlocked-volume IDs, discoverer
  credit, shelf assignment, and read-only presentation.

Raw chat exports and account mappings are research sources only. They are not
runtime data, canonical identity storage, or player-facing content.

## Registry Index

Current registries exposed through `ElarionApi.registries()`:

- Reward action registry: Core-owned reward execution actions.
- Progression event registry: Core-owned title/stat/progression events.
- Condition registry: shared Core registry for data-driven checks.
- Action registry: shared Core registry for data-driven UI/NPC/project actions.
- Requirement registry: shared Core registry for project, quest, title, portal,
  and law requirements.
- Milestone event registry: shared Core registry for society/project milestone
  effects.

Each registry entry should document ID, owner addon, input fields, validation,
execution context, failure behavior, and emitted history/progression events.

Current built-in examples:

```text
conditions: has_realm, has_title, has_ability, relationship_is, is_leader,
  has_realm_flag, has_history_event
actions: run_reward, emit_history, close
requirements: items, kills, title_unlocked, ability_present, realm_flag
milestone events: elarion:grant_title, elarion:grant_ability,
  elarion:emit_history, elarion:run_reward
```

Registry execution scaffolding:

```text
RegistryExecutionContext
RegistryExecutionResult
ConditionContext / ConditionHandler
ActionContext / ActionHandler
RequirementContext / RequirementHandler
MilestoneContext / MilestoneEventHandler
```

Initial built-in handlers exist for the first Core condition, action,
requirement, and milestone IDs. Handlers that require richer addon-specific
state fail safely until the owning addon supplies executable context.

## Current Commands

Player-facing:

```text
/rc <message>
/ac <message>
/pm <player> <message>
/r <message>
/w <message>
/yell <message>
/help [command]
```

OP level 4:

```text
/spy chat
/list
/seed
/e reload
/e realm ...
/e citizen ...
/e title ...
/e ability ...
/e reward ...
/e progression ...
/e history ...
/e history chronicle list [weeks]
/e history chronicle inspect <week> [limit]
/e economy wallet get|give|take|deposit|withdraw ...
/e economy treasury get|give|take ...
/e economy transfer player ...
/e economy transactions player|realm ...
/e economy pulse|recalculate|reload
/random value|roll|reset ...        vanilla command, gated to OP level 4
/e world ...
/e perf status|queues|config|worlds|realms|realm <realm>|hotzones|security
```

## Data Ownership

- Citizen UUID, username, Realm, status, nickname, title ownership, active
  title, explicit abilities: Core citizen record.
- Realm definitions: `realms.yml`.
- Base relationships, hiding, pending Realm decisions, deliveries:
  Core runtime state under `world/elarion/addon-state/realms/`.
- Government forms/offices/laws/taxes/reforms: Government addon.
- Contribution project definitions and progress: Contributions addon.
- Wallets, treasuries, transactions, interest, Economy Governor, adaptive NPC
  prices: Economy addon.
- Closed Worldheart Market access, seller sessions, trade rules, manual stalls,
  market taxes: Trade addon.
- Quest definitions/progress: Adventure Guild addon.
- NPC definitions, skins/profiles, placed NPC state, dialogue trees, GUI action
  dispatch, and dialogue/session state: NPCs addon.
- Public civic reputation: Ledger addon.
- Public social memory projection: History, Ledger, Chronicles, Newspapers,
  NPCs, Contributions, Government, and Realms through events and registries.
- Realm active needs and onboarding: Realms, Contributions, Adventure Guild,
  NPCs, Ledger, and History.
- War objectives, war fatigue, and lawful conflict records: future war logic
  layered over Realms, Government, Ledger, History, Economy, and Portals.
- Treaty flags and diplomatic summits: Government addon consuming Core Realm
  relationship truth.
- Portal tickets, fees, schedules, and runtime access state: Portals addon.
- Performance diagnostics and Elarion queue status: Optimization addon.
- Anti-cheat, anti-AFK farm evidence, and security enforcement policy:
  Security addon.

## Task Queue Index

Core exposes `ElarionApi.tasks()` and `ElarionApi.system().tasks()`:

```text
io executor: filesystem-heavy and archive/report work
compute executor: immutable snapshot validation, indexing, planning, summaries
server queue: bounded application of world/player/entity/inventory changes
```

Current default baseline:

```text
io workers: 1
compute workers: 2
max queued server tasks: 4096
max server applies per tick: 256
max server apply budget: 2 ms
queue warning threshold: 2048
slow operation warning: 50 ms
sample interval: 30 seconds
headroom thresholds: warm 35 mspt, pressure 45 mspt, overloaded 50 mspt
```

Future heavy systems should document which work runs in each queue and what
happens when queues are full.

Current diagnostics:

```text
server queue: queued, completed, failed, rejected, by-family counters
io executor: submitted, queued, active, completed, failed, by-family counters
compute executor: submitted, queued, active, completed, failed, by-family counters
```

## Modpack Compatibility Index

Use this map when adding integrations:

```text
Farmer's Delight-style crops/foods:
  Data: item/block/recipe tags and registry IDs
  Owners: Titles, Contributions, Trade, Economy, Adventure Guild
  Rule: no vanilla-only crop or food assumptions

Scholar-style knowledge/books:
  Data: future knowledge, book, study, and title hooks
  Owners: NPCs, Adventure Guild, Ledger, Titles
  Rule: optional integration through registries and actions

Exposure-style photos/artifacts:
  Data: future photo/artifact evidence and reward hooks
  Owners: Newspapers, Adventure Guild, Ledger, History
  Rule: do not require the mod for base history correctness

Simple Voice Chat:
  Data: voice proximity, Realm, alliance, spy, and moderation hooks
  Owner: voicechat-hooks addon
  Rule: Core chat truth remains text/server authoritative

custom villagers and NPC-like mods:
  Data: entity registry IDs, professions, dialogue or trade adapters
  Owners: NPCs, Trade, Government, Ledger, Economy
  Rule: avoid treating vanilla villagers as the only civic NPCs

terrain generation, structures, mobs, and bosses:
  Data: biome/entity/structure/dimension IDs and tags
  Owners: Worlds, Titles, Adventure Guild, Security, Optimization
  Rule: worldgen and mob systems are modpack-owned shared server cost

backpacks, storage, containers, and item transfer:
  Data: item/block IDs, container APIs, transfer events where available
  Owners: Trade, Economy, Security
  Rule: policies must be practical and avoid naive false positives
```

## Addon Contracts

```text
Addon: optimization
Owner: addons/optimization
Purpose: Elarion performance diagnostics and task queue visibility.
Config paths: config/elarion/addons/optimization/performance.yml
Runtime state paths: future world/elarion/addon-state/optimization/
Commands: /e perf status, /e perf queues, /e perf config, /e perf worlds,
  /e perf realms, /e perf realm <realm>, /e perf hotzones, /e perf security
Services: ElarionPerformanceMonitor, PerformanceSampler
APIs/events consumed: ElarionApi.tasks()
APIs/events exposed: none yet
Registries used: none yet
History events emitted: future operational events only
Performance notes: reports queue pressure, validation warnings, sampled
  headroom, slow operations, world/Realm samples, hotzones with entity groups
  and previous-sample deltas, event-tracked block entity groups, bounded
  in-memory world trends, IO/compute queue diagnostics, and world-rule
  queue-full/slice events; must not sample every world/entity/block every tick
Cross-addon dependencies: Core only

Addon: worlds
Owner: addons/worlds
Purpose: managed worlds, lobby/world destinations, per-world borders, and
  block/mob abundance rules.
Config paths: config/elarion/addons/worlds/worlds.yml
Runtime state paths: world/elarion/addon-state/worlds/processed-chunks.json
Commands: /e world list, /e world info, /e world tp, /e world create,
  /e world remove, /e world reload
Services: WorldService, WorldRuleService
APIs/events consumed: Core task queues, history, admin commands
APIs/events exposed: ElarionWorldsApi
Registries used: Minecraft registries for world templates, blocks, biomes,
  mobs, and dimensions
History events emitted: world opened/created/remove/unload events
Performance notes: uses Fantasy `tickWhenEmpty(false)`, bounded server-thread
  queue work for 16-block vertical chunk scarcity slices, and dirty
  processed-chunk saves after every slice for a chunk finishes
Cross-addon dependencies: Core, Fantasy

Addon: realms
Owner: addons/realms
Purpose: Realm protection, shared-world safety, visitor access policy, and
  Diplomat death history.
Config paths: config/elarion/addons/realms/protection.yml
Runtime state paths: none yet
Commands: none; Core owns /e realm
Services: RealmProtectionService, RealmAccessPolicy
APIs/events consumed: Core citizens, Realms, relationships, abilities,
  identities, and history
APIs/events exposed: explosion block-protection hook
Registries used: Minecraft block registry for configurable interaction lists
History events emitted: diplomat-killed
Performance notes: event-driven protection only; no scans; feedback
  rate-limited per player
Cross-addon dependencies: Core

Addon: names
Owner: addons/names
Purpose: nickname presentation hooks and nickname-aware command suggestions.
Config paths: none; Core owns config/elarion/core/identity.yml
Runtime state paths: none; Core citizen state owns nicknames
Commands: none; Core owns citizen nickname commands
Services: mixin presentation hooks
APIs/events consumed: Core synced identity data
APIs/events exposed: none
Registries used: none
History events emitted: none
Performance notes: presentation hooks must use cached/synced identity data
Cross-addon dependencies: Core

Addon: titles
Owner: addons/titles
Purpose: client title and leader crown rendering.
Config paths: none; Core owns config/elarion/core/titles.yml
Runtime state paths: none; Core owns title state, progress, and claims
Commands: none; Core owns /e title
Services: client renderer mixin
APIs/events consumed: Core synced identity data
APIs/events exposed: none
Registries used: none
History events emitted: none
Performance notes: visual-only rendering must not compute title eligibility
Cross-addon dependencies: Core

Addon: security
Owner: addons/security
Purpose: evidence-first anti-cheat and anti-AFK farm protection foundation.
Config paths: config/elarion/addons/security/security.yml
Runtime state paths: world/elarion/addon-state/security/evidence.json
Commands: /e security status
Services: SecurityEvidenceService
APIs/events consumed: Core events, history, task queues
APIs/events exposed: Core diagnostics provider `security`
Registries used: future condition/action hooks
History events emitted: future admin/security evidence events
Performance notes: detection must be event-driven or sampled, never global scanning
Cross-addon dependencies: Core only by default

Addon: economy
Owner: addons/economy
Purpose: official sigil item, player wallets, Realm treasuries, audited
  transactions, physical sigil conversion, and monitor-only Economy Governor.
Config paths: config/elarion/addons/economy/economy.yml
Runtime state paths: world/elarion/addon-state/economy/economy-state.json,
  world/elarion/addon-state/economy/transactions/<yyyy-MM>.jsonl
Commands: /e economy wallet ..., /e economy treasury ...,
  /e economy transfer player ..., /e economy transactions ...,
  /e economy pulse, /e economy recalculate, /e economy reload
Services: EconomyTransactionService, EconomyInventoryService,
  EconomyGovernorService
Models: EconomyAccount, EconomyTransaction, EconomyTransactionType,
  TransactionResult, EconomyPulse
APIs/events consumed: Core Realm lookup, history, task queues, admin commands,
  diagnostics
APIs/events exposed: ElarionEconomyApi, diagnostics provider `economy`
Registries used: Minecraft item registry for elarion:sigil; shared gameplay
  registries remain future consumer-driven work; addon-owned Creative tab
  elarion:economy exposes Economy items
History events emitted: successful economy transactions with transaction ID,
  type, accounts, amount, source system, and Chronicle prose
Performance notes: balances are in memory; a compact forced journal append
  precedes mutation; snapshots are periodic and atomic; OP queries are bounded
  by month and result count
Cross-addon dependencies: Core only; all future Economy consumers use the
  public API rather than mutating balances
```

## Planned Addon Contracts

```text
Addon: npcs
Owner: future addons/npcs
Purpose: placeable NPCs, skins/profiles, dialogue trees, interaction GUI state,
  and registered NPC action dispatch.
Config paths: config/elarion/addons/npcs/npcs.yml,
  config/elarion/addons/npcs/skins.yml,
  config/elarion/addons/npcs/dialogues/<dialogue-id>.yml
Runtime state paths: world/elarion/addon-state/npcs/placed-npcs.json,
  world/elarion/addon-state/npcs/sessions/
Commands: future /e npc place|remove|list|inspect|move|skin|dialogue|reload
Services: future NpcDefinitionService, NpcPlacementService,
  DialogueService, NpcInteractionService
Models: future NpcDefinition, NpcSkinProfile, PlacedNpcRecord,
  DialogueDefinition, DialogueNode, DialogueOption, DialogueSession
APIs/events consumed: Core identities, abilities, events, registries, history,
  addon action handlers
APIs/events exposed: future NPC action/condition contexts and dialogue open
  events
Registries used: shared condition/action/requirement registries
History events emitted: meaningful NPC-driven outcomes only, such as purchases,
  quest accept/complete, civic registrations, government actions, or lore
  discoveries
Performance notes: static/event-driven by default; no NPC AI/pathing/global
  scans unless explicitly bounded, configurable, and measured
Cross-addon dependencies: Core required; Economy, Trade, Portals,
  Adventure Guild, Government, Ledger, Titles, and Contributions through
  registered actions or explicit addon APIs only
```

## Addon Contract Template

Use this template when an addon gains real behavior:

```text
Addon:
Owner:
Purpose:
Config paths:
Runtime state paths:
Commands:
Services:
Models:
APIs/events consumed:
APIs/events exposed:
Registries used:
History events emitted:
Performance notes:
Cross-addon dependencies:
```

## Config Schema Index Template

Use this template for every config file:

```text
Config file:
Owning service:
Generated default: yes/no
Reload-safe: yes/no
Runtime state counterpart:
Validation summary:
Failure behavior:
```

## History Event Index Template

Current Economy transaction history family:

```text
Event ID: transaction-<transaction-type>
Category: economy
Actor fields: transaction actor UUID when present
Realm fields: source or destination Realm account when present
Target fields: economy_transaction:<transaction-id>
World/location fields: none
Chronicle prose source: Economy addon transaction history adapter
Retention/archival behavior: Core history policy, monthly indexes, and
  Chronicle archive settings
```

Use this template for every durable history event family:

```text
Event ID:
Category:
Actor fields:
Realm fields:
Target fields:
World/location fields:
Chronicle prose source:
Retention/archival behavior:
```

## Update Checklist

When adding a feature, update this file with:

- source package/class
- config path
- runtime state path
- command path
- service/API owner
- addon ownership
- any cross-addon dependency
- registry entries
- history event entries
- cache invalidation rules

## Public Memory API

Use `api.publicHistory()` for player-facing memory systems:

```text
Chronicles
Newspapers
Citizen Ledger views
NPC rumors
GUI search
```

The API composes weekly Chronicle archives and live monthly history indexes.
Raw JSONL history remains the OP/audit source and should not be opened directly
by presentation addons.

## Generated Artifact Policy

Generated logs and run outputs are ignored by `.gitignore`:

```text
*.log
*.log.gz
logs/
**/logs/
dev/run/
dev/client1/
dev/client2/
```

Do not commit generated logs, dev worlds, or run-client/server output.
