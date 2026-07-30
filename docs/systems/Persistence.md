# Persistence System

Purpose: maintain durable configuration and runtime state with explicit ownership, validation, and bounded query paths.

Main classes: `JsonStateStorage`, `CoreConfigManager`, `CoreConfigValidator`, addon storage/config classes.

Entry points: server start/stop lifecycle and reload commands.

Commands: `/e reload` and addon reload commands.

Network packets: none directly.

GUI/screens: consume service snapshots, not files.

Storage/persistence: `config/elarion` for definitions; `world/elarion` for runtime.

The website whitelist bridge stores only its ordered cursor and unconfirmed
acknowledgement sequences under
`world/elarion/core/minecraft-bridge/state.json`. Atomic persistence happens
before acknowledgement delivery, so a restart retries acknowledgements rather
than skipping applied commands. Website account and application data never
becomes Core world state.

Dependencies: Core config/storage helpers, Gson/YAML parsing, task service for batched history.

Related systems: every Core/addon feature.

Extension points: new addon config loaders, storage wrappers, validation tests.

Risks: config-as-state, unbounded JSONL scans, and broad file parsing during gameplay.

Core maintains a Realm-membership UUID index inside `CitizenService`. The
index is built once from canonical citizen storage after each server bind and
updated only after durable citizen saves. Realm and World notification fan-out
and Realm reward delivery query this index, so ordinary delivery work is
bounded by the target audience and does not repeatedly scan every citizen
file.

`JsonStateStorage` distinguishes missing input from unreadable input. Missing
files use the owner-provided default. Malformed or otherwise unreadable files
are moved to a timestamped `.corrupt-*` sibling before the default is returned;
if quarantine fails, loading fails closed. Atomic write failures are logged and
thrown to the owner service, so a mutation cannot report success after failed
durability. Owner services must not catch and suppress that failure.

Government, Quest, and Underworld runtime states persist explicit schema
version `1`. Versionless legacy snapshots normalize to v1, while unsupported
future versions fail loading and are quarantined by the shared storage
contract. Each future format change requires an owner-specific migration and
old-data round-trip tests.

Do not duplicate this system by creating: ad hoc file writers or gameplay code that parses config on interaction.

## Proposed NPC Trade State

NPC trade definitions now live as parsed config in
`config/elarion/addons/npcs/trades.yml` and are immutable runtime snapshots.
They are not world state. Purchase recovery and finite placed-NPC stock are
NPC-owned world state. The approved audit requires schema-versioned state, O(1)
operation lookup, bounded receipt retention, lazy restock, and restart
reconciliation against an idempotent Economy operation receipt. See
`docs/reports/NPC_TRADE_OWNER_AUDIT.md`.

The purchase proposal adds two independent migrations that must not be bundled:

- Economy receipt state is now schema v2: bounded O(1) idempotent receipts,
  schema-v1 backup/migration, journal reconstruction, restart tests, and no
  ordinary JSONL history scans.
- Economy state is now schema v3: a dedicated `worldheartTreasury` balance is
  persisted beside Realm treasuries. Schema v1/v2 files are backed up before
  atomic migration, and Worldheart revenue does not use non-persistent generic
  system accounts.
- NPC placement schema v2 now stores resolved `REALM|WORLD` tax jurisdiction
  derived from definition policy and canonical placement world. Schema v1 is
  backed up before atomic migration; mismatch/unsupported state fails closed.

NPC purchase journaling remains the later NPC-owned schema after Economy
receipts and NPC jurisdiction foundations.
See `docs/reports/NPC_TRADE_PURCHASE_FOUNDATION_PROPOSAL.md`.

NPC trade purchases now use schema-v1 runtime state at
`world/elarion/addon-state/npcs/trade-purchases.json`. The journal records
unlimited BUY purchase IDs and transitions through PREPARED, PAID, COMPLETE,
or FAILED. Paid records that did not complete delivery are retried on request
replay and player join. Completed and failed purchase IDs replay
deterministically.

NPC trade stock now uses schema-v1 runtime state at
`world/elarion/addon-state/npcs/trade-stock.json`. Records are keyed by placed
NPC UUID plus offer ID, so multiple placed NPCs can share one catalog while
retaining independent inventory. Stock restocks lazily during trader
open/quote/purchase paths, not on a tick loop. Each stock record retains a
bounded set of consumed purchase IDs so request replay cannot decrement stock
twice. The same bounded operation-ID list is used for sale supply IDs when a
Sell offer has `stock-destination: placed_npc`; replay cannot increment the
target BUY stock twice and supply is capped by the destination offer's stock
limit.

NPC trade sales now have a schema-v1 storage contract at
`world/elarion/addon-state/npcs/trade-sales.json`. It records sale IDs,
player/NPC/session/catalog/offer fields, server quote snapshots, explicit
replay states, Economy receipt IDs, and serialized escrow stacks. New escrow
rows include a full encoded `ItemStack` payload plus source-slot metadata so
component-bearing items can be restored. The defined states are PREPARED,
ITEMS_ESCROWED, PAID, STOCK_UPDATED, COMPLETE, FAILED, and RESTORED. Runtime
Sell settlement is server-authoritative: it creates a prepared record, removes
only matching main-inventory stacks into escrow, persists escrow, pays the
seller's Economy wallet through an idempotent receipt, and restores escrowed
items on payout failure when possible. Paid sales then advance through an
idempotent stock update step, which can route the sold quantity into the
configured destination BUY offer for the same placed NPC.

## Config Startup And Reload Safety

Economy, Underworld, Offerings, and Government config reload paths now prepare
candidate snapshots before replacing live service state. Realms protection and
Mounts Collection text have no live reload command; malformed startup config
logs and falls back to safe defaults rather than aborting addon initialization.

## Economy Tax Policies

Category tax overrides are runtime policy state at
`world/elarion/addon-state/economy/tax-policies.json`, not NPC or Government
config. Schema v1 is strict and atomically written. Malformed or unsupported
state fails startup instead of resetting live policy. Updates persist a
candidate snapshot before replacing the in-memory O(1) map.

## Worldheart Authority

Core persists Worldheart governing authority at
`world/elarion/worldheart/authority.json`. Missing state defaults to
`SYSTEM` authority with the lore-facing display identity `Hollow Emperor`.
`PLAYER` authority requires a valid player UUID and is validated on load. Invalid
authority state falls back safely without touching unrelated world state.
Worldheart treasury ownership is not stored here; it remains Economy-owned
state under the stable Worldheart treasury account.
