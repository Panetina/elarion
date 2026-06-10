# Core Contract

Last reviewed: 2026-06-09

Author: Panyel  
Team: Panetina Team

## Owns

- citizens
- Realm definitions and membership
- nicknames
- titles and active title
- abilities
- identity
- base Realm relationships and hiding state
- rewards
- history and progression events
- player stats
- task queues
- shared registries

## Public API

Use grouped facades from `ElarionApi` for new work:

- identity
- realm
- messaging
- progression
- system

## Runtime State

```text
world/elarion/citizens/
world/elarion/history/
world/elarion/player-stats/
world/elarion/progression/
world/elarion/title-claims.json
world/elarion/addon-state/realms/
```

## Performance Notes

Core services should be event-driven, cache derived lookups only, and invalidate
caches on canonical source changes.
