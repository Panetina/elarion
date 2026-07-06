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

addons/groups
  -> Core citizens/identity/history
  -> Economy for group creation fee sinks

addons/government
  -> Core Realm/citizen/history
  -> Groups API for Confederation delegate eligibility and group locks

addons/underworld
  -> Core player restrictions
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
```

## Shared Infrastructure

```text
Core config          -> all config loaders should follow validation/default patterns
Core storage         -> JSON state, history JSONL, indexes, archives
Core registries      -> actions, conditions, requirements, milestone handlers
Core networking      -> typed payload examples and identity/theme sync
Core UI              -> panels, buttons, lists, scaled layout, numeric input, Collection menu
Core command output  -> readable admin command style
Core history         -> audit and public-memory source
Core domain events   -> bounded cross-addon lifecycle signals
Core notifications  -> persistent player-facing event projections
```

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
Government <-> Groups
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

For example, NPC banker actions are registered by Economy. NPCs dispatch actions but do not own wallet state.
Quest dialogue choices should follow the same rule: NPCs dispatch registered
Quest actions and conditions, while Quests owns questline state.
Quests may reference placed NPC UUIDs through the NPC API for actor bindings,
but NPCs remain the owner of placement records and dialogue sessions.

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
- Groups membership vs Government offices/delegate seats.
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
