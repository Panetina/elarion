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
- canonical data exported to the web/bridge layer
- active-citizen recency truth
- durable deferred reward grants and delivery receipts

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
world/elarion/reward-grants.json
world/elarion/addon-state/realms/
```

`config/elarion/core/activity.yml` controls the default active-citizen recency
window. Citizen records persist `lastSeenAt`; online players are active and
offline players remain active until the configured window expires.

Deferred reward grants snapshot their reward actions and use deterministic IDs.
They deliver immediately to online recipients or on next login to offline
recipients. Delivered compact receipts remain durable to prevent duplicate
payment across restart.

## External Bridge Contract

The website/backend scaffold may consume Core-owned identity, Realm, whitelist,
and public-history projections through explicit bridge APIs or sync payloads.

It should receive:

- user account identity snapshots
- whitelist application decisions and status
- Realm membership and access visibility needed for web pages
- Chronicle/news-ready public history summaries

It should not receive:

- direct ownership of citizens or Realms
- raw mutable Core state files
- a second source of truth for history or titles

## Performance Notes

Core services should be event-driven, cache derived lookups only, and invalidate
caches on canonical source changes.
