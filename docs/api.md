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

Existing direct service getters remain compatible. New addons should use the
facades unless a direct service is needed for an existing API.

## Economy API

Use `ElarionEconomyApi.get()` after addon initialization. Economy owns wallet,
Realm treasury, transaction, and Governor state; Core remains authoritative
for citizens, Realms, and history.

All sigil movement must call the Economy API. Addons must not modify wallet or
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

The public-history layer composes weekly Chronicle archives with live monthly
history indexes, deduplicates events, applies category/Realm/player/text
filters, and keeps raw JSONL scans on the OP/audit path.

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
