# Elarion Project Memory

Last reviewed: 2026-06-10

Author: Panyel  
Team: Panetina Team

This is the project start file. Keep it short. Future sessions should read this
file first, then open only the focused files needed for the current task.

## Read Order

1. `RULES.md` - mandatory architecture, modularity, performance, gameplay, and
   documentation rules.
2. `INDEX.md` - project dictionary, ownership map, config map, runtime-state
   map, command index, and addon contracts.
3. `TODO.md` - actionable work only, grouped by implementation area.
4. `PLANS.md` - future design book for Season 2 ideas that are not yet direct
   implementation tasks.
5. Focused docs under `docs/` only for the area being changed.

## North Star

Elarion is a Realm-based civilization modpack server, not only Minecraft with
teams. The target loop is:

```text
Realm identity
-> useful contribution
-> public memory
-> economy and scarcity
-> NPCs and quests
-> government and diplomacy
-> controlled conflict
-> Chronicles, ledgers, monuments, and stories
```

Players should return because their Realm needs them, their friends notice
them, their work remains, their vote matters, and their name becomes part of
the world.

## Current Architecture

Elarion is a Fabric 1.21.1 multi-project mod.

- `platform/core` owns canonical truth for citizens, Realms, titles, nicknames,
  statuses, abilities, identity, rewards, relationships, history, progression,
  and current base leadership data.
- Addons consume Core truth through `ElarionApi`, grouped API facades, Core
  services, events, ability checks, registries, reward actions, and
  history/progression events.
- Core owns bounded task queues for IO, compute, and server-thread apply work.
  Defaults assume an unknown online host with possible shared CPU, variable
  disk, and a larger Vanilla+ modpack also using server resources.
- Definitions are YAML/JSON under `config/elarion/`.
- Mutable runtime state belongs under the world save, especially
  `world/elarion/` and `world/elarion/addon-state/`.
- Realm, Realms, and Worldheart are the canonical terms.

## Current Gameplay Foundation

Implemented foundations include:

- Realm membership, Realm chat, alliance chat, local/proximity chat, private
  messages, whispers, yells, scoped join/leave notices, and chat spy.
- Realm worlds, lobby, world borders, world rules, and bounded world-rule work.
- Core titles, title progression, unique title claims, active title effects,
  event-driven player stats, nicknames, identities, and rendering hooks.
- Durable history events with Chronicle-ready prose.
- Realm protection rules for own-Realm citizens, visitors, allies, hostiles,
  lobby, and Worldheart.
- Core registries for conditions, actions, requirements, and milestone events,
  with initial executable handlers and reference validation.
- Optimization and Security addon foundations.

## Current Focus

Prefer backend foundations before large GUI surfaces:

- Economy addon: sigil, wallets, Realm treasuries, transaction history, Economy
  Governor monitor state, and admin commands.
- Contributions addon: contribution block, project loader, society pillars,
  society ranks, mixed requirements, milestone execution, and public memory
  hooks.
- NPCs addon: placeable NPC definitions, skin/profile configuration, dialogue
  trees, server-authoritative GUI interaction, and action/condition integration.
- Portals addon: physical portals and ticketed access.
- Government addon: form loader, offices, founding/reform votes, treasury audit,
  law/tax foundations, and treaty metadata.
- History/Chronicles: weekly immutable archive records and display hooks.
- Tests around commands, registries, persistence, Realm protection, economy,
  contribution, NPC dialogue, and performance diagnostics.

## Documentation Rule

Every implementation that adds, moves, removes, or changes a service, command,
config file, runtime-state file, model, registry, ability, event, addon
responsibility, or user-visible rule must update:

- `TODO.md` when task status changes.
- `INDEX.md` when data/config/code ownership changes.
- `RULES.md` when a reusable architecture rule changes.
- `PLANS.md` when future design assumptions change.
- focused docs under `docs/` when a contract changes.

Do not leave architecture knowledge only in chat. If docs and code disagree,
inspect the code first, then update the docs to match the real implementation.

## Build Check

Use:

```text
.\gradlew.bat build
```

The build must pass after code changes.
