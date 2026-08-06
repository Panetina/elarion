# Elarion Dependency Graph

## High-Level Shape

```text
Minecraft/Fabric
  -> platform/core
       -> shared APIs
       -> config/storage/history/rewards/identity/UI theme/task queues
       -> addons/*
```

Core is the only canonical owner for citizens, Realms, titles, abilities,
identity, rewards, history, server identity, shared infrastructure, and the
generic Collection menu shell.

## External Platform Boundary

```text
Fabric Core/addons (canonical game truth)
  -> signed bounded projection outbox
  -> website read models
  -> public World Ledger / account views / Discord-safe notifications

Website whitelist workflow
  -> signed ordered command outbox
  -> Core bridge
  -> canonical Minecraft whitelist

Launcher
  -> signed immutable release feed
  -> build/export/client/manifest.json contract
  -> official-origin third-party downloads plus Elarion release-bundle files
  -> never receives server, database, Discord, or release-signing secrets
```

`distribution/mods.json` is canonical for third-party versions, side, source,
license, and SHA-512. `build.gradle` projects it into client/server install
roots and IntelliJ/Loom runs. The launcher consumes generated manifests but
does not become an owner of modpack truth.

The website owns web sessions, OAuth links, applications, review/audit state,
permissions, and acknowledged read models. Discord is an identity/notification
adapter. Neither surface may read addon storage or become a second owner of
game state.

## Direct Dependencies

```text
addons/economy
  -> Core history
  -> Core server identity
  -> Core rewards/registries

addons/npcs
  -> Core registries
  -> Core UI theme
  -> Core identity/ability checks
  -> Economy only through registered actions

addons/quests
  -> Core registries
  -> Core notifications/domain events
  -> NPCs API for admin actor binding to placed NPC UUIDs
  -> Offerings API for Shrine display-name projections

addons/offerings
  -> Core history
  -> Core rewards
  -> Core active-citizen eligibility
  -> Core UI primitives/theme
  -> Economy for banked-currency offerings

addons/guilds
  -> Core citizens/identity/history
  -> Economy for guild creation fee sinks

addons/government
  -> Core Realm/citizen/history
  -> Offerings flags for Foundation gates

addons/underworld
  -> Core player/account restrictions and global interaction gates
  -> Core root-command registry for /banish and /unbanish
  -> Core character lifecycle reset handlers
  -> Core domain events
  -> Worlds for configured Underworld dimension availability

addons/mounts
  -> Core commands/test command wiring
  -> Core Collection menu tab provider
  -> Core identity rendering indirectly through player labels
  -> GeckoLib runtime rendering dependency

addons/realms
  -> Core Realm/citizen APIs
  -> Core history

addons/worlds
  -> Core task queues/commands where needed

addons/optimization
  -> Core task queue diagnostics

addons/security
  -> Core commands/history future hooks

addons/angling
  -> Core accepted-catch, rewards, metrics/rankings, titles, history, and events
  -> Economy pricing and settlement contracts

addons/angling-delight
  -> Angling public item/component contracts
  -> Farmer's Delight Refabricated cooking and recipe APIs

addons/atlas (current shell)
  -> Core shared UI primitives only
  -> no Economy, Government, Offering, Portal, NPC, storage, or bridge dependency
```

## Shared Infrastructure

```text
Core config          -> all config loaders should follow validation/default patterns
Core storage         -> JSON state, history JSONL, indexes, archives
Core registries      -> actions, conditions, requirements, milestone handlers
Core networking      -> typed payload examples and identity/theme sync
Core UI              -> panels, buttons, lists, scaled layout, numeric input, Collection menu
Core command output  -> readable admin command style
Core command roots   -> bounded addon-contributed top-level commands
Core restrictions   -> live interaction gates plus UUID admission policy
Core history         -> audit and public-memory source
Core domain events   -> bounded cross-addon lifecycle signals
Core notifications  -> persistent player-facing event projections
```

## Character Menu Target

```text
Core Character Menu shell
  -> Core CitizenProfileService for aggregation and visibility filtering
  -> Core identity / Realm / active-title / progression projection
  -> Core bounded public-history LEDGER query
  -> existing Core Collection service for Unlockables

Addon profile contributor
  -> reads only addon-owned cached state or bounded summary
  -> returns authorized presentation data
  -> never exposes addon storage or mutates state

Existing Collection provider
  -> remains addon-owned unlock/action projection
  -> retains server-authoritative action validation
```

`Character Menu` is the player-facing shell label and `/charactermenu` command alias.
Collection remains the internal Unlockables contract so Mounts, Titles,
packets, config, runtime state, and addon APIs do not require a breaking
rename. `CitizenProfileService` now owns the Core profile aggregation boundary,
server-side visibility filtering, bounded section/field/card caps, and
Core identity/Realm/active-title/progression projections. Profile
request/snapshot payload records are registered as a read-only
server-authoritative request/response path. Addon profile contributors are a
separate read-only contract; Government currently contributes active office
role metadata through that path. Contributors must not be modeled as
Collection actions, copied into Core state, or backed by broad storage scans.

## Project Revamp Audit Note

Phase 0 Slice 1 of the project-wide revamp verified that the current dependency
shape still supports the planned Config/Admin-first path:

- Core already owns the Admin Panel shell, provider registry, shared UI,
  config defaults/validation patterns, notifications, history, Collection, and
  public APIs.
- Addons already own their domain config loaders and runtime state.
- The next audit slice can map config/Admin descriptors without moving addon
  state into Core and without changing current physical config formats.
- A future typed config registry should describe addon-owned config domains
  through explicit registration rather than making Core depend on addon
  implementation classes.

## Possible Circular Dependencies

Avoid:

```text
NPCs <-> Economy
NPCs <-> Offerings
Government <-> Guilds
Government <-> Economy
Ledger <-> History
Atlas <-> NPCs/Offerings/Portals
```

Preferred shape:

```text
Addon A owns state
Addon A exposes API or registry handler and emits domain events
Addon B calls API/handler or subscribes to bounded events
Selected events publish through Core notifications
Core owns shared truth and history
```

Atlas must follow that preferred shape. Future owner addons publish bounded
map projections through Core contracts/events; Atlas must not gain direct
required dependencies on NPCs, Offerings, Portals, Government, or Economy.

For example, NPC banker actions are registered by Economy. NPCs dispatch actions but do not own wallet state.
Quest dialogue choices should follow the same rule: NPCs dispatch registered
Quest actions and conditions, while Quests owns questline state.
Quests may reference placed NPC UUIDs through the NPC API for actor bindings,
but NPCs remain the owner of placement records and dialogue sessions.

NPC trade ownership follows a one-way optional integration, not an
`NPCs <-> Economy` cycle:

```text
NPCs trade service
  -> owns catalogs, offer eligibility, stock, session/replay state, purchase journal
  -> optional NPC-owned Economy adapter
       -> Economy public API owns price resolution, charge/refund/payout receipts
  -> Core deferred rewards own restart-safe delivery actions
```

Economy owns idempotent receipts, pricing, tax, and settlement. NPCs owns
jurisdiction, purchase/sale journals, finite stock, sessions, and authoritative
trade snapshots. The current trade screen is server-authored and mutating only
through its dedicated request/result payload path.

Core registers its server lifecycle callbacks before invoking custom addon
entrypoints, then invokes addon entrypoints in deterministic required-dependency
order. Addon `SERVER_STARTED` callbacks may rely on Core services being bound,
but must still use Core APIs instead of copying Core-owned state. If Addon B
validates config against handlers owned by Addon A, B must declare A as a
required Fabric dependency. Circular required dependencies are rejected during
Core bootstrap.

## Systems That Should Stay Separated

- Core citizens vs Ledger reputation.
- Core Realm membership vs Government offices/laws.
- Guilds membership vs Government offices.
- Economy balances vs Offering progress.
- NPC dialogue sessions vs Quest/Economy/Government state.
- Quest state vs NPC placements, Offering progress, Government offices, and
  Core Realm/citizen/title truth.
- Public History indexes vs curated Folklore Markdown.
- Atlas markers vs Realm/NPC/Portal canonical ownership.

## Systems That Should Reuse Common Code

Networking:

```text
Identity payloads
NPC payloads
Shrine payloads
future Market/Government/Atlas payloads
  -> typed CustomPayload + server validation + authoritative snapshot
```

GUI:

```text
NPC dialogue
Shrine UI
future shops/trade/government/quests/Atlas
  -> platform/core/client/ui primitives + ui_theme.yml
```

Persistence:

```text
Core state
Economy state
NPC placements
Offering state
Quest state
future Ledger/Atlas state
  -> config definitions + world runtime state + atomic writes
```

History/search:

```text
Raw JSONL
  -> monthly indexes
  -> weekly Chronicle archives
  -> public history API
  -> future newspaper/ledger/NPC rumor/search UI
```

## NPC Trade Jurisdiction Dependency

```text
Core RealmService -> canonical Realm/world ownership lookup
Core WorldheartGovernanceService -> Worldheart authority and govern checks
NPC definition    -> tax-jurisdiction policy
NPC placement     -> resolved Realm/world registration
NPC trade adapter -> Economy price/tax quote and idempotent receipt API
Economy           -> tax calculation, recipient account, transaction journal,
                     Realm treasuries, and stable Worldheart treasury
```

NPCs must not own tax rates or treasury balances. Economy must not read NPC
placement storage. The client receives a bounded quote and never supplies an
authoritative tax, total, recipient, item, or price.

The NPC definition/placement jurisdiction path is implemented. Core's
`RealmService.ownerForWorld` is read through the public API during placement
resolution; Core does not store NPC registrations, and Economy does not read
NPC placement files.

The quote edge is optional: NPCs suggests Economy and loads its adapter only
when Economy is present. Economy never depends on NPCs. NPCs owns catalog and
jurisdiction inputs; Economy owns rates, quote arithmetic, and settlement.
The shipped banker prompt IDs are recognized by NPC config validation as
optional-provider contracts, but NPCs does not register Economy handlers.
Without Economy, unavailable providers disable bank/trade settlement. The
isolated optional-addon loader matrix must remain green whenever this boundary
changes.

Worldheart authority is deliberately not Economy treasury ownership. Core may
answer whether OP4 or a future ascended ruler can govern Worldheart, but
Economy keeps `WORLDHEART_TREASURY` as a domain treasury before and after any
future succession event.
