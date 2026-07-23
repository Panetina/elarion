# Minecraft Whitelist Bridge

Purpose: apply website-approved whitelist additions and removals to the
canonical online-mode Minecraft whitelist, and publish bounded game read
models to the website.

Owner: Core owns the outbound client, cursor, pending acknowledgements, and
Minecraft whitelist mutation. The website owns applications, decisions, and
the ordered command outbox.

Main package:
`platform/core/src/main/java/panetina/elarion/core/integration/minecraft/`.

## Runtime Contract

- Disabled by default.
- Requires `online-mode=true` and `white-list=true`.
- Makes outbound HTTPS requests only.
- Polls with one daemon worker every 30 seconds or slower.
- Fetches at most 100 commands ordered by sequence.
- Applies whitelist changes on the Minecraft server thread.
- Stores the last applied sequence and pending acknowledgements at
  `world/elarion/core/minecraft-bridge/state.json`.
- Stores the ordered website projection outbox at
  `world/elarion/core/minecraft-bridge/projection-outbox.json`.
- Coalesces unsent `STATE` projections by kind and entity while preserving
  append-only `EVENT` projections. The outbox is capped at 10,000 entries and
  each request is capped at 100 entries.
- Flushes durable pending acknowledgements before fetching more work after a
  restart.
- Add/remove operations are idempotent. Removal disconnects an affected online
  player only when that entry was originally added by the bridge. A manual
  console whitelist entry remains server-owned and is never removed by a
  stale website command after restart.
- Does not receive RCON access, database credentials, browser cookies, or
  administrator credentials.
- On a successful online-mode join, issues the client an opaque, seven-day
  `launcher-passage` receipt. The website verifies it with the existing bridge
  HMAC secret and returns only that UUID's website read model. This eliminates
  a website-to-Minecraft-Services verification call; the receipt contains no
  game data and Core does not persist it.

Requests and responses use HMAC-SHA256 over five newline-delimited fields:

```text
METHOD
PATH_AND_QUERY
LOWERCASE_SHA256_OF_EXACT_BODY
UNIX_TIMESTAMP_SECONDS
NONCE
```

Responses use `RESPONSE` as the method and the configured server ID as the
path field. Timestamps have a five-minute acceptance window. Response nonces
are remembered in a bounded in-memory replay set.

## Configuration

Core creates `config/elarion/core/minecraft-bridge.yml` on first startup:

```yaml
version: 1
enabled: false
base-url: "https://ashesofelarion.com"
server-id: "production"
secret: ""
poll-seconds: 30
```

The shared secret must contain at least 32 characters and must never be
committed or logged. Environment overrides are supported through
`ELARION_MINECRAFT_BRIDGE_ENABLED`, `ELARION_MINECRAFT_BRIDGE_URL`,
`ELARION_MINECRAFT_SERVER_ID`, `ELARION_MINECRAFT_BRIDGE_SECRET`, and
`ELARION_MINECRAFT_BRIDGE_POLL_SECONDS`.

Website endpoint and response details are maintained in the website
repository at `docs/MINECRAFT_BRIDGE.md`.

## Projection Ownership

Core and addons remain canonical. The website is a permission-filtered read
model and must not become a second owner of citizens, realms, votes, history,
shrines, advancements, or map state.

Addons publish through `api.system().webProjections()`. Initial contracts are:

- `realm`: Core-owned member and online aggregates plus configured identity.
- `realm.identity`: Government-owned voted presentation overrides.
- `citizen`: whitelisted-only per-player Realm tag and identity summary.
- `citizen.notifications`: Core-owned, recipient-scoped snapshot of at most five
  unread notification entries for the launcher. It is refreshed only on
  notification lifecycle changes, server startup recovery, and the recipient's
  bounded join sync; it is never a history scan or a public feed.
- `world.presentation`: public display label keyed by world ID. Core owns base
  worlds and Realm spawn mappings; addons may publish their own worlds without
  changing citizen storage.
- `government.office`: Government-owned resolved office display label keyed by
  the office holder UUID. It prevents launcher clients from interpreting
  internal office/title IDs.
- `group.membership`: Groups-owned recipient-scoped active group identity keyed
  by member UUID. It exposes only the member's group display name, tag, and
  resolved member role; membership removal is an explicit inactive projection.
- `underworld.standing`: Underworld-owned recipient status (`Alive`, `Dead`, or
  `Banished`), where banishment always takes precedence over a death session.
- `election`: Government-owned aggregate lifecycle, never voter identities.
- `chronicle`: append-only public Chronicle projections selected by the Core
  public-history category policy.
- `metric.advancement-leaderboard`: Core's persisted owner-maintained top-10
  advancement index. It updates on join/advancement synchronization and never
  scans all player-stat files for an ordinary request.
- `metric.shrine-contribution`: Offerings-owned per-instance aggregate without
  contributor identities.
- `map.marker.*`: typed bounded map markers validated by Core; inactive state
  is an explicit tombstone and never requires raw chunk or region scans.

New projection kinds require an owning system, a bounded payload, explicit
visibility, a stable entity key, and tests for restart/idempotency behavior.

Citizen lifecycle persistence records the last world ID only on join, world
transition, and disconnect. It is not sampled or rewritten per tick. The
launcher resolves this exact key through `world.presentation`; unknown worlds
remain `Unknown World` rather than exposing raw dimension identifiers.
