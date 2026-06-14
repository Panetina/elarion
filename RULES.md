# Elarion Rules

Last reviewed: 2026-06-10

Author: Panyel  
Team: Panetina Team

These rules are mandatory for future work.

## Canonical Ownership

`platform/core` owns canonical truth for:

- citizens
- Realm membership
- Realm definitions
- nicknames
- titles
- status
- granted abilities
- derived identity
- base Realm relationships
- existing reward execution
- existing history and progression events
- existing pending decision groundwork
- visibility rules

Addons must not duplicate Core truth. Addons consume Core through `ElarionApi`,
Core events, ability checks, reward action handlers, and history/progression
events.

The current Core leader field is existing minimal authority data. Do not treat
the one-leader model as the final government design. Season 2 government work
must keep broader offices, laws, reforms, councils, ministries, judges, scribes,
quartermasters, and other government roles in the Government addon while
consuming Core citizen and Realm truth.

## Config And Runtime State

Editable definitions belong under:

```text
config/elarion/
```

Mutable runtime state belongs under:

```text
world/elarion/
world/elarion/addon-state/
```

Never store runtime contribution progress, government state, wallets, quest
progress, NPC session state, trade state, or ledger records inside editable
config.

Definitions must be reload-safe, validated, and immutable after load until the
next reload. Runtime state must survive restart.

## Data Lifecycle

Every data-driven system follows this lifecycle:

- editable YAML/JSON definitions load on startup and reload
- definitions validate into immutable in-memory records
- runtime state stores compact IDs, counters, progress, timestamps, and flags
- services mutate runtime state through explicit methods
- important outcomes emit history/progression events
- UI receives server-authoritative snapshots instead of reading local guesses

Do not parse config during gameplay actions. Do not copy full definitions into
runtime state.

## Modularity

Do not hard-code specific Realms, titles, government forms, NPCs, quests,
projects, portal IDs, or special gameplay cases into general-purpose services.

Use this pattern for every extensible gameplay system:

```text
generic engine
+ config-defined content
+ registered conditions/actions/requirements/events
+ compact world state
+ server-side validation
+ Core history/progression emission
```

Avoid logic shaped like:

```text
if government == republic
if npcId == banker
if projectId == council_hall
```

Prefer registry-driven lookups:

```text
definition = registry.get(id)
visibleActions = definition.visibleActions(context)
registry.execute(action, context)
```

Every major extensible system should have:

- definition loader
- immutable validated definitions
- runtime state service
- action registry
- condition registry
- requirement registry where needed
- clear startup/reload validation
- server-side action validation
- history emission
- tests

Avoid adding new responsibilities to files that already mix unrelated concerns.
When a Java file approaches roughly 500 lines, prefer extracting focused
helpers or registrars before adding more behavior. Existing large files should
be reduced through mechanical, behavior-preserving splits.

Cleanup-only splits must preserve public API, command behavior, generated
config shape, runtime-state schema, history event shape, and player-visible
behavior. If a cleanup needs to change behavior, treat it as a feature change
and update `TODO.md`, `INDEX.md`, focused docs, and tests accordingly.

Do not introduce abstraction for its own sake. Add a shared abstraction only
when at least two real call sites need it, or when one public extension contract
clearly needs a stable boundary.

## Addon Boundaries

Core must not depend on gameplay addons.

Addons may depend on Core and may depend on explicit addon APIs only when that
dependency is intentional and documented in `INDEX.md`.

Addons must not directly read or mutate another addon's runtime files. Cross
addon behavior must go through APIs, events, registries, ability checks, reward
actions, or history/progression events.

New addon code should prefer grouped `ElarionApi` facades over direct concrete
service getters. Direct getters remain compatible for existing code and for
cases where the facade does not yet expose the required behavior.

Use or create addons for these responsibilities:

- `addons/government`: government forms, offices, laws, votes, government blocks
- `addons/contributions`: contribution blocks, projects, society progression, blueprints
- `addons/economy`: currency, wallets, treasuries, bank, transactions,
  Economy Governor, adaptive price state
- `addons/trade`: Worldheart Market access, trade policy, seller sessions,
  manual stalls, market taxes
- `addons/adventure-guild`: quests, objectives, quest rewards, quest HUD sync
- `addons/npcs`: placeable NPC definitions, skins/profiles, dialogue trees,
  interaction GUI state, dialogue sessions, and NPC action dispatch
- `addons/ledger`: public civic reputation and activity stats
- `addons/portals`: portal access, tickets, fees, schedules

## Economy Design

Sigil must not replace Realm barter, Realm scarcity, contribution projects, or
player trade in scarce resources. Use sigil for official Worldheart systems:
access, convenience, tickets, licenses, services, prestige, progression
accelerators, and long-term sinks.

Do not implement passive offline selling by default. Worldheart Market selling
requires official server-validated trade flows, seller presence, bounded seller
sessions, and clear rollback behavior on failure.

Do not let NPC vendors sell unlimited scarce Realm resources, cheap diamonds,
or other stock that erases inter-Realm trade. NPC vendors may sell services,
tickets, backpacks, cosmetics, permits, tools, and other controlled
convenience or prestige items.

Economy adjustments must be bounded, logged, and explainable. Adaptive prices,
quest reward changes, interest changes, and market fee changes require base
values, min/max bounds, maximum change rates, reason text, and admin visibility.
Prices must not change during an open transaction.

Market, economy, ticket, and pricing rules must be config-driven and owned by
their addons. Core must not hard-code market policy.

## NPC And Interaction Design

NPCs are a future interaction layer, not a place to hide hard-coded service
logic. NPC content belongs in config, runtime placement/session state belongs
under world addon state, and behavior must dispatch through registered actions,
conditions, requirements, Core APIs, addon APIs, ability checks, and history
events.

Placeable NPCs should have durable IDs, configurable names, skins/profiles,
dialogue IDs, location metadata, and optional presentation data. The NPC addon
owns NPC placement and dialogue sessions; Economy, Trade, Portals,
Adventure Guild, Government, Ledger, Titles, and Contributions own the actions
that affect their own state.

Do not hard-code one-off Java paths like `if npcId == banker`. Define the
banker as content and let its buttons call registered actions such as
`open_bank`, `deposit_wallet`, or `withdraw_wallet`.

Default NPCs should be static and event-driven for performance. Do not add NPC
pathing, AI, or per-tick scans unless the feature genuinely requires it and the
cost is bounded, configurable, and measured.

Every NPC GUI action must be validated server-side. Client UI state is a
presentation cache, not authority.

## Social Civilization Design

Elarion should optimize for identity attachment and public memory, not
manipulative retention. Players should return because their Realm needs them,
their friends notice them, their work remains, their enemies remember them,
their vote matters, and their name is part of the world.

Every major gameplay action should emit durable social memory when appropriate:
history, Citizen Ledger, progression events, Chronicle prose, notifications,
newspaper hooks, NPC rumor hooks, monument/plaque records, office records, war
records, or treasury audit events.

Prefer visible earned recognition over raw reward inflation. Reward useful
behavior with public thanks, ledger credit, titles, badges, ceremonies,
monuments, offices, and story consequences.

Do not add unhealthy retention patterns:

- harsh daily login streaks
- anxiety-driven limited-time pressure
- random reward gambling
- offline raid anxiety
- permanent base destruction by default
- invisible corruption
- untraceable theft
- public shame boards for weak, new, inactive, or low-contribution players
- passive income systems that remove social interaction

Conflict should create story, demand, rivalry, and public memory without
deleting months of work. War should be objective-based, scheduled when possible,
audited through history, and safe from uncontrolled base grief by default.

Politics should create consequence, not unchecked power. Offices, laws,
treaties, war declarations, treasury spending, recalls, and reforms need
history/audit records and future accountability hooks.

## Modpack Compatibility

Elarion is designed to run inside a larger Vanilla+ Fabric modpack. Do not
optimize, balance, protect, or validate systems as if Elarion is the only mod
on the server.

Assume other mods may own or heavily affect:

- crops, food, cooking, and farming
- villagers, professions, NPC-like entities, and trading
- terrain generation, structures, caves, ores, dimensions, and loot
- mobs, bosses, attributes, spawns, and drops
- backpacks, storage blocks, containers, bundles, and item transfer
- cameras, photos, books, knowledge systems, newspapers, and artifacts
- voice chat, proximity audio, party/group features, and social systems

Do not hard-code vanilla-only item, block, entity, biome, recipe, loot, or
dimension assumptions except as generated defaults and examples. Prefer
registry IDs, tags, optional integration adapters, config-defined allow/deny
lists, and server-side validation through Core services.

Optional integrations must be isolated in the correct addon. Examples:

- Simple Voice Chat behavior belongs in `addons/voicechat-hooks`.
- modded crops, foods, mobs, recipes, and blocks must work through tags,
  registry IDs, requirements, progression events, and config.
- modded backpacks and storage must be handled by Trade/Security policies where
  practical, without assuming one specific backpack implementation.
- custom villagers and NPC-like mods must integrate through NPC, Trade, Ledger,
  Government, and Economy APIs instead of Core ownership.

## Custom Item And Creative Tab Rules

Every addon that registers custom items must also register an addon-owned
Creative Mode tab so its items are easy to locate during development and
administration.

- Use an `Elarion: <Addon>` display name and an `elarion:<addon-id>` item-group
  ID.
- The addon that owns an item also owns its Creative tab registration and
  entries.
- Core must not maintain a central hard-coded list of addon items.
- Every new item must be added to its owning addon's tab in the same change.
- Item groups are presentation/discovery only and must not own gameplay state.
- Add or update a registry/GameTest assertion when an addon first gains items
  or its Creative tab.

## Lore And Naming

`LORE.md` is the canonical source for established setting facts, official
names, symbols, institutions, and public-facing terminology.

- Keep code, config defaults, commands, GUIs, item text, Chronicles,
  newspapers, NPC dialogue, and documentation consistent with `LORE.md`.
- Add new lore gradually. Clearly separate established canon from future ideas.
- Do not silently overwrite established lore while implementing unrelated
  features.
- Update `LORE.md`, `INDEX.md`, and affected feature documentation together
  when a canonical name, symbol, institution, or narrative contract changes.
- The official currency is the **Sigil** (`Sigils` plural), minted by the
  Worldheart Treasury and bearing the Elarion Seal.
- `Folklore/` owns curated, numbered, book-ready accounts from the lost first
  age. Volume IDs are stable and must not be silently renumbered.
- Keep curated Folklore separate from generated weekly Chronicles. Folklore is
  authored canon or attributed legend; Chronicles summarize runtime history.
- Published lore must use roleplay identities only. Do not expose Discord
  names, Minecraft account names, UUIDs, or source account IDs in player-facing
  books.
- Where no roleplay name survives, use a neutral archival epithet and state the
  limits of the evidence instead of inventing a false identity.
- Preserve uncertainty. Conflicting testimony may become folklore, but must not
  be rewritten as confirmed fact without a canonical decision in `LORE.md`.
- New recoverable lore volumes must update the Folklore catalog, `LORE.md` when
  canon changes, `INDEX.md`, and the relevant quest/archive TODO or feature
  documentation.

For safety systems, fail closed only for authority or exploit prevention where
the config explicitly says so. For presentation or optional integration, fail
nonfatally and keep the server playable.

Security and anti-AFK checks must be mod-aware and evidence-first. A modded
farm, villager, backpack, crop, mob, chunk behavior, or voice feature can be
legitimate gameplay. Do not punish players from naive vanilla assumptions.

Performance budgets must reserve headroom for the full modpack. Elarion task
queues, scans, caches, and background jobs must not consume all CPU, memory,
chunk, entity, or IO budget just because local tests pass with only Elarion
loaded.

## Performance

Avoid:

- global per-tick scans
- parsing YAML during gameplay actions
- loading every ledger/history file for one GUI
- storing large histories in block entities
- recomputing society rank every tick
- sending full blueprints every tick
- placing massive structures instantly
- broad item-entity scans for trade prevention

Prefer:

- immutable definitions loaded on startup/reload
- runtime state storing compact IDs/progress
- event-driven updates
- dirty saves every fixed interval and on logout/server stop
- batched work queues
- cached Realm-world ownership lookup
- cache invalidation on reload, membership, relationship, law, or world changes
- server-side validation at action boundaries
- rate-limited player feedback
- bounded recent-history lists
- atomic file writes

Cache only derived lookup data. Invalidate caches when relevant source truth
changes, including reload, Realm membership, Realm relationship, world load,
world ownership, law, title, ability, nickname, and status changes.

Runtime writes must be dirty-tracked, atomic, bounded, and scheduled. A runtime
write every tick requires a clear technical reason and should be avoided.

Performance targets must assume an online host with unknown shared CPU, memory,
disk, and network behavior. Strong hardware is extra headroom, not a design
requirement. Defaults must stay conservative until measured server diagnostics
justify raising budgets.

Use Core task queues for multicore work:

- `io` for saves, archives, reports, and other filesystem-heavy work
- `compute` for immutable snapshot processing, validation, planning, indexing,
  and summaries
- server-thread queue for bounded world/player/entity/inventory application

Background threads may not directly mutate Minecraft worlds, entities, players,
inventories, registries that require server-thread access, or networking state.
Background tasks produce immutable result objects. Server-thread queued work
applies those results with per-tick limits.

Bobby and Distant Horizons are optional compatibility targets, not authority
systems. Server-side rules, visibility, anti-cheat, protection, and gameplay
state must remain correct without them.

Security and anti-AFK systems must start evidence-first. Automatic punishment
requires explicit config, tested thresholds, admin visibility, and history or
audit records.

Generated logs, dev worlds, run outputs, and cache artifacts must not be kept in
source modules or committed. If a generated artifact appears in the worktree,
remove it or ignore it as part of the same cleanup.

## Commands

`/rc`, `/ac`, `/pm`, `/r`, `/w`, `/yell`, and `/help` are player-facing.

All `/e ...` commands are OP level 4 unless a feature is explicitly approved as
player-facing.

Addons register admin commands under `/e`.

Every new player command must define:

- permission level
- visibility and relationship rules
- target selection rules
- tab suggestions
- `/help` text
- rate limits or cooldowns when spam is possible

Every new config field must define:

- default value
- validation rule
- reload behavior
- clear failure message
- runtime-state counterpart when applicable

## Testing And Definition Of Done

A feature is complete only when:

- gameplay works server-side
- config is generated, commented, loaded, validated, and reload-safe
- mutable state survives restart
- client UI sync works on join and live update when UI exists
- server validates every GUI/action request
- Core canonical truth is not duplicated
- Core ability, relationship, and visibility rules are respected
- history/progression events are emitted
- OP command policy is respected
- performance avoids per-tick global scans
- tests cover success, failure, persistence, and abuse cases
- `TODO.md` and `INDEX.md` are updated
- `.\gradlew.bat build` passes

## Documentation Maintenance

Update the docs as part of the same change:

- `TODO.md`: add, remove, or check tasks.
- `INDEX.md`: add every new config file, runtime file, service, registry,
  command, model, API, storage path, and ownership decision.
- `RULES.md`: add reusable rules only when they govern future implementation.
- `PLANS.md`: add future design ideas that are not actionable yet.
- `docs/api.md`, `docs/performance.md`, `docs/config.md`, or
  `docs/addons/*.md`: update focused contracts when the change affects them.

Do not leave new architecture knowledge only in chat.
