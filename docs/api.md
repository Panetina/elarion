# Elarion API

Last reviewed: 2026-06-10

Author: Panyel  
Team: Panetina Team

Use `ElarionApi.get()` only after Core initialization. Prefer grouped facades
for new addon work.

## Preferred Facades

- `api.identity()`: identity rendering, nickname validation, title ownership,
  and identity sync.
- `api.realm()`: citizens, Realms, relationships, spawns, leaders, decisions,
  and Realm deliveries.
- `api.messaging()`: Realm chat, alliance chat, local chat, private messages,
  whispers, yells, and spy delivery.
- `api.progressionApi()`: player stats, progression events, rewards, and
  durable history.
- `api.publicHistory()`: Chronicle archives and composed public-memory feeds
  for newspapers, ledgers, NPC rumors, and GUI search.
- `api.system()`: abilities, events, admin command registration, registries,
  and task queues.
- `api.uiThemes()`: current Core-owned UI theme snapshot and bounded join/reload
  synchronization. Addons own screen composition, not shared visual tokens.

Client screens compose primitives from `panetina.elarion.core.client.ui`.
This is a small render API extracted from real NPC and Shrine screens, not a
declarative framework.

## Web And Bridge Boundary

The website/backend scaffold is an external read-model and intake layer, not a
canonical gameplay owner. It may consume Core data for:

- Minecraft account linking
- whitelist application intake and review
- Discord placeholder linking
- Chronicle/news publication views
- realm-facing profile and access pages

The bridge layer must not duplicate citizen, Realm, title, or history ownership.
When it needs those facts, it should receive them from Core-owned APIs or
bounded sync payloads and persist only cache/read-model copies.

Existing direct service getters remain compatible. New addons should use the
facades unless a direct service is needed for an existing API.

## Economy API

Use `ElarionEconomyApi.get()` after addon initialization. Economy owns wallet,
Realm treasury, transaction, and Governor state; Core remains authoritative
for citizens, Realms, and history.

All currency movement must call the Economy API. Addons must not modify wallet or
treasury state directly. Every request supplies an explicit transaction type,
accounts, amount, actor, reason, and `sourceSystem`.

Supported transaction types are:

```text
TRANSFER
DEPOSIT
WITHDRAW
REWARD
FEE
TAX
SINK
TREASURY_GRANT
ADMIN_ADJUSTMENT
```

Transaction records include success/failure and both account balances before
and after the attempt. Successful transactions emit Core history events.

## Offerings API

`ElarionOfferingsApi` owns project definitions, instances, Shrine links,
progress, contributor totals, and bounded donation summaries.

- `contributeItems(...)` validates and consumes matching inventory items.
- `contributeCurrency(...)` consumes deposited Economy balance.
- `contributeEvent(...)` is for registered addon/integration events only.

Player-facing requests contain only instance ID, requirement key, and amount.
The Offering service validates Shrine world/range, project state, requirement
type, remaining progress, payment, persistence, and completion.

Core remains the owner of active-citizen truth and deferred reward delivery.
Economy remains the owner of currency sinks/refunds and transaction records.

## Portals API

Use `ElarionPortalsApi.get()` after addon initialization. Portals owns route
definitions, linked source/destination endpoints, schedule state, physical ticket
identity, return entitlements, field activation, and travel validation.

Route definitions expose `scheduled_ticketed`, `fee_passage`, or
`always_open`. Fee passages reference Economy-owned price keys.

- `definitions()` exposes immutable configured route definitions.
- `routes()` and `route(id)` expose immutable route snapshots.
- `travel(player, route, direction)` performs server-authoritative travel.
- `entitlement(player, route)` exposes the durable one-use return passage.
- `onRouteStateChanged(listener)` receives bounded `OPENED` and `CLOSED`
  events for notification, Atlas, and other addon projections.

Offering milestones should invoke registered `elarion:portal_unlock` or
`elarion:portal_lock` actions. Economy owns ticket payment and refund
transactions. Callers must not mutate Portal runtime JSON or teleport players
around Portal validation.

## Deferred Rewards

Use `api.deferredRewards()` when a server-owned system must grant a snapshotted
Core reward to online or offline players.

Grant IDs must be deterministic for the source outcome. Delivery persists
completed action indexes after each successful action and retains delivered
receipts so restart/retry cannot pay the same grant twice.

When adding a new addon, start from the grouped facade that owns the concern.
Only use direct service getters when the facade does not yet expose the required
behavior, then consider whether the facade should grow a focused method.

Current addon sources should scan clean for direct legacy `api.<service>()`
getter use. Core-internal command and service code may still use direct service
access where it is the owning layer.

## Public History

Use `api.publicHistory()` for player-facing memory views. Addons should not read
`world/elarion/history/`, `history-index/`, or `chronicles/` directly.

Core currently exposes:

- `query(PublicHistoryQuery)`
- `newspaper(realmId, limit)`
- `ledger(playerId, limit)`
- `npcRumors(realmId, limit)`
- `search(text, limit)`
- `recentChronicles(weeks)`
- `generateChronicles()`

External bridge consumers should use these same public-history projections for
Chronicle/news pages instead of raw history files.

The public-history layer composes weekly Chronicle archives with live monthly
history indexes, deduplicates events, applies category/Realm/player/text
filters, and keeps raw JSONL scans on the OP/audit path.

## Catch Telemetry

Core owns the immutable `CatchTelemetryEvent` transport contract, durable catch
journal, and per-player summary projections. Core will later own title,
History, and Chronicle-facing consumers.

Angling and future fishing adapters may publish through:

```text
api.system().events().emitCatchTelemetry(event)
```

Consumers may subscribe through:

```text
api.system().events().onCatchTelemetry(listener)
```

The event carries a stable event ID, occurrence time, actor, source,
fish-definition, rarity, quantity, optional world/dimension/biome identifiers,
and bounded immutable metadata. Producers reuse an event ID only when retrying
the same resolved catch. Emission is synchronous: Core appends the accepted
record before applying a bounded replay page to the player's summary. It does
not record generic History, mutate progression, grant rewards, unlock titles,
or create Chronicle entries.

Core must not depend on Angling model classes. Angling translates its
definition and rarity IDs into the Core-owned event contract.

Angling's current internal `AnglingCatchResolutionService` creates immutable
retry-stable results from definitions in the active reload snapshot and emits
them through the event bus. No command, packet, item, bobber, or other gameplay
trigger calls that service yet.

Read-only consumers use:

```text
api.catchTelemetry().summary(playerId)
api.catchTelemetry().totalQuantity(playerId)
api.catchTelemetry().quantityForSource(playerId, sourceId)
api.catchTelemetry().quantityForFishDefinition(playerId, fishDefinitionId)
api.catchTelemetry().quantityForRarity(playerId, rarityId)
api.catchTelemetry().recentCatches(playerId)
```

These methods use direct player lookup and immutable indexes. A query may apply
one bounded replay page for that player; it never scans every player. The
durable processing and failure contract is documented in
`docs/systems/CatchTelemetry.md`.

## Registry Execution

Shared data-driven systems use:

- `ConditionContext`
- `ActionContext`
- `RequirementContext`
- `MilestoneContext`
- `RegistryExecutionResult`

Core provides initial executable built-in handlers for the first registered
condition, action, requirement, and milestone IDs. Handlers that need richer
addon state should fail safely until their owning addon supplies the execution
context.

Handlers should return failure results instead of throwing for ordinary player
or config-state failures. Throw only for programmer errors or impossible
internal states.

## Server Thread Rule

Registry handlers may plan work off-thread only when the result is immutable.
World, player, entity, inventory, networking, and registry mutation must happen
through server-thread work.
