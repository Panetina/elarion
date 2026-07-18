# NPC Trade Owner Audit

Date: 2026-07-08

Status: architecture audit and proposal only. No production behavior changed.

## Objective

Define a modular, server-authoritative owner boundary for NPC buy/sell behavior
before the current display-only trader catalog is allowed to move currency,
items, unlocks, or stock.

## Audited Sources

- NPC dialogue/session authority: `NpcInteractionService`, dialogue models,
  dialogue payloads, config loader/validator, `NpcTradeScreen`, and
  `ElarionNpcApi`.
- Economy authority: `ElarionEconomyApi`, `EconomyInventoryService`,
  `EconomyTransactionService`, service-price definitions, transaction models,
  and persistence docs.
- Existing cross-domain compensation example: Portal fee/ticket travel.
- Existing item delivery/recovery: `RewardActionService`,
  `DeferredRewardGrantService`, and Underworld component-safe item storage.
- Shared contracts: Networking, Persistence, GUI, NPC, and dependency docs.
- Module declarations: NPCs currently depends only on Core; Portals demonstrates
  an explicit addon-to-addon dependency on Economy's public API.

## Verified Current State

1. `NpcTradeScreen` is intentionally non-mutating and constructs four preview
   rows entirely on the client. Labels, prices, stock text, item stacks,
   enchantments, and lore are not authoritative.
2. `NpcConfigValidator` rejects prompts and executable actions on `trade`
   presentation nodes. This is the correct safety gate and must remain until a
   real trade service is installed.
3. `NpcInteractionService` already owns active dialogue sessions, NPC range,
   current node, option visibility, and server-side action execution.
4. Economy owns wallet balances, physical Sigils, service prices, audited
   transactions, and mixed physical-then-bank payment/refund.
5. Economy's current mixed payment API is compensating but not idempotent. It
   has no caller operation ID or bounded receipt lookup suitable for crash
   recovery across addon stores.
6. Core reward actions can safely preflight and deliver ordinary item rewards.
   `DeferredRewardGrantService` persists per-action completion and deduplicates
   grant IDs, making it suitable as a recovery delivery mechanism.
7. No Market/Trade addon currently owns merchant catalogs or stock. Moving
   this state into Economy would incorrectly make Economy own NPC merchandise;
   moving it into Core would create shared-state ownership without a universal
   platform requirement.
8. NPC placement state already has schema-versioned atomic JSON storage under
   `world/elarion/addon-state/npcs/`. Trade runtime state can remain NPC-owned
   beside it, but must use a separate schema and migration plan.

## Ownership Decision

### NPCs Owns

- Merchant catalog definitions and validation.
- Mapping an NPC definition/placement to a catalog.
- Offer visibility and eligibility in the current NPC context.
- Stock policy and NPC-owned stock runtime state.
- Active trade session nonce/revision and replay protection.
- Purchase/sale orchestration and trade result snapshots.
- Trade history/domain-event meaning after a completed trade.

### Economy Owns

- Currency balances and physical currency inventory.
- Currency formatting and service-price lookup.
- Charge/refund/payout transaction records.
- Idempotent payment receipts once that API is added.
- Economy audit metadata for every merchant payment or payout.

### Core Owns

- Generic reward actions and restart-safe deferred grants.
- Shared item/reward preview contracts only if they become useful beyond NPCs.
- History, domain events, notifications, and shared UI primitives.

Core does not own merchant catalogs, stock, or purchase state.

### Other Addons Own

- Their unlock state and reward action handlers. A merchant may sell a Mount,
  title, ability, ticket, or future pet through registered reward actions, but
  NPCs must not mutate those addon stores directly.

## Dependency Decision

NPCs should keep Economy optional. Add an NPC-owned Economy integration adapter
that compiles against `ElarionEconomyApi` but is instantiated only when
`elarion_economy` is loaded. The Fabric dependency should be optional/suggested,
not required. When Economy is absent, priced offers remain visible only if the
server chooses to expose them, but actions are disabled with an explicit
server-authored reason.

Do not add a universal Core service locator or make Economy depend on NPCs.

Suggested internal boundary:

```text
NpcTradePaymentGateway
  available()
  resolvePrice(priceReference)
  chargeOnce(player, operationId, amount, metadata)
  refundOnce(player, receipt, reason)
  payOnce(player, operationId, amount, metadata)
```

The interface belongs to NPCs. The Economy-backed adapter belongs under an NPC
integration package and calls only Economy's public API.

## Definition Model

Add a separate parsed file such as
`config/elarion/addons/npcs/trades.yml`. Keep it separate from dialogue graphs
because catalogs have different validation, reload, and runtime lifecycles.

Recommended definition concepts:

```text
NpcTradeCatalogDefinition
NpcTradeOfferDefinition
NpcTradeDirection              BUY | SELL
NpcTradePriceReference         fixed or Economy service-price key
NpcTradeStockPolicy            UNLIMITED | PLACED_NPC
NpcTradeRewardDefinition       one or more Core RewardAction values
NpcTradeItemRequirement        for future SELL offers
```

Every catalog and offer uses stable IDs. Display labels are never keys.

An NPC definition references a catalog ID. A trade node selects presentation;
it does not duplicate offer definitions or execute arbitrary actions.

Offer validation must reject:

- duplicate/blank IDs
- unknown item IDs or invalid reward actions
- missing or conflicting fixed price/price key
- non-positive price, quantity, or stock bounds
- unsupported stock policies
- invalid restock durations
- unknown catalog references from NPC definitions
- SELL definitions until the sell contract is implemented
- payload-visible strings or lists above explicit limits

The matching NPC read-only config descriptors and descriptor tests are required
in the same slice as parsed trade definitions.

## Server Snapshot Contract

The current hardcoded client catalog must be replaced before mutation work.
Use a dedicated bounded server snapshot rather than adding more fields to the
already large `NpcDialogueOpenPayload`.

Suggested records:

```text
NpcTradeSnapshotPayload
  npcId
  dialogueNodeId
  sessionNonce
  catalogId
  catalogRevision
  mode
  balance presentation
  offers[]

NpcTradeOfferPayload
  offerId
  display label/subtitle
  authoritative item/reward preview
  unit price
  available quantity or unlimited flag
  max request quantity
  enabled
  disabled reason
```

Cap catalogs and payload lists. Initial recommendation: at most 128 parsed
offers per catalog, at most 64 visible offers per snapshot, bounded strings,
and paged/virtualized UI beyond the visible row count.

For item offers, transmit an authoritative item stack or component-preserving
item representation so Minecraft can render native tooltips. For non-item
rewards, transmit a semantic icon plus bounded tooltip lines. The client never
reconstructs enchantments, lore, stock, or prices.

## Mutation Request Contract

Suggested buy request:

```text
NpcTradePurchasePayload
  npcId
  dialogueNodeId
  sessionNonce
  catalogRevision
  offerId
  quantity
  operationId
```

The request must not contain item IDs, item components, labels, prices, stock,
currency type, reward actions, or NPC names.

The server re-resolves and validates:

1. active player session and nonce
2. current NPC, world, range, and trade node
3. current catalog and revision
4. offer visibility and conditions
5. quantity bounds and current stock
6. current server price
7. inventory/reward eligibility
8. replay/idempotency state
9. Economy availability and payment result

The result packet returns the operation ID, typed status, short message, fresh
balance, changed stock, and refreshed offer revision. It never trusts client
state as the next source of truth.

## Crash-Safe Buy Flow

A simple `charge -> insert item -> decrement stock` flow is not live-safe. A
crash between steps can lose money, duplicate delivery, or restore stock.

Required staged flow:

1. Persist an NPC-owned `PREPARED` purchase record with operation ID, player,
   NPC, offer snapshot, quantity, price, and catalog revision.
2. Call an idempotent Economy charge using the same operation ID.
3. Persist the payment receipt / `PAID` state.
4. Enqueue a Core deferred reward grant with a deterministic grant ID derived
   from the operation ID.
5. Persist stock consumption and mark the purchase `DELIVERY_QUEUED`.
6. Attempt immediate claim/delivery. If inventory is full, keep the grant in
   claimable mail rather than dropping or deleting the purchase.
7. Mark the trade complete after the durable grant and stock state exist.

Startup reconciliation handles every non-terminal state using Economy's
idempotent receipt lookup and Core's deduplicated grant ID. It must never scan
all historical Economy JSONL files during gameplay or restart.

This requires a bounded Economy operation-receipt index. Adding that index is a
separate persistence/API migration slice with backup, rollback, and old-state
tests.

## Stock Persistence

Recommended first real stock policy:

- `UNLIMITED`: no runtime state.
- `PLACED_NPC`: stock keyed by placed NPC UUID plus offer ID.

Do not create per-player stock maps in V1. They grow without bound and need a
separate expiry/index policy.

Finite stock state should include schema version, current quantity, last
restock time, and definition revision. Restock should be lazy/event-driven when
the catalog opens or a purchase occurs, not polled every tick.

Completed operation receipts must have a bounded retention window/count and be
safe to prune after the originating session can no longer replay them.

## Sell Flow

Selling is not the inverse of buying and should be a later slice. It requires:

- exact item/component matching rules
- inventory slot reservation/removal
- payout idempotency
- restoration after payout failure
- handling partial stacks and quantity
- buyback pricing rules
- stock destination policy
- crash reconciliation

Keep the Sell tab disabled/pending until this contract is implemented and
tested. Do not implement selling through arbitrary dialogue actions.

## History And Notifications

Emit one NPC-owned trade domain event after durable completion. Include stable
NPC, catalog, offer, operation, quantity, and price identifiers/values. Core
history may project major or administratively useful trades.

Routine purchases should not create inbox notifications. Use a claimable Core
reward notification only when delivery is deferred or recovery is required.

## Performance Rules

- Parse and validate catalogs only on startup/reload; cache immutable snapshots.
- Resolve offers by catalog/offer maps, never by scanning all NPCs or history.
- Keep stock and operation lookup O(1) by stable composite key.
- Do not read files during screen open or purchase execution.
- Use bounded payloads and virtualized rows.
- Avoid per-tick restock polling.
- Record focused timing diagnostics around snapshot build and purchase commit.
- Do not query Economy JSONL history to detect duplicate operations.

## Compatibility

- Existing dialogue, banker, NPC placement, and Economy state remain unchanged.
- Existing `trade` nodes stay non-mutating until their referenced catalog is
  valid and a payment gateway is available.
- Existing generated config files must not be silently overwritten.
- Adding `trades.yml` requires default creation without replacing customized
  files, read-only descriptors, validation, and reload rollback tests.
- Trade runtime state starts as a new schema-versioned file; no migration from
  the display-only client catalog exists because it is not authoritative state.

## Recommended Implementation Slices

### Slice A - Server-Authored Read-Only Catalogs

Classification: MEDIUM.
Status: completed on 2026-07-08.

- Add trade definition models, loader, validation, defaults, descriptors, and
  tests. Completed.
- Link NPC definitions to catalog IDs.
- Add a bounded trade snapshot payload and client state.
- Replace hardcoded preview rows with server-authored rows.
- Keep all rows non-mutating and Sell disabled.
- No Economy dependency, runtime stock, or persistence change.

### Slice B - Economy Idempotent Payment Proposal And Migration

Classification: LARGE persistence/API proposal, then contained implementation.

- Define operation IDs, durable receipt index, charge/refund semantics, schema
  migration, backup, rollback, and restart tests.
- Do not implement as part of Slice A.

### Slice C - NPC Purchase Journal And Unlimited Buy

Classification: LARGE persistence/network slice.

- Add session nonce, purchase request/result, PREPARED/PAID/delivery states,
  deferred grant integration, replay tests, and restart reconciliation.
- Start with `UNLIMITED` stock only.

### Slice D - Finite Per-NPC Stock

Classification: MEDIUM persistence slice.

- Add `PLACED_NPC` stock, lazy restock, bounded operation retention, and
  concurrent/restart tests.

### Slice E - Selling

Classification: LARGE inventory/payment slice.

- Define exact component matching, removal reservation, payout/refund, and
  crash recovery before enabling the Sell tab.

## Verification Matrix

- Config defaults, malformed input, duplicate IDs, invalid items/rewards,
  unknown catalogs, reload rollback, and descriptor truthfulness.
- Packet round trips, bounded counts/strings, malformed IDs/quantities, and
  stale revision/session rejection.
- Optional Economy absence and unavailable-price behavior.
- Replay of the same operation ID before/after restart.
- Crash checkpoints between prepare, payment, grant enqueue, stock commit, and
  completion.
- Full inventory, disconnect, NPC removal, range exit, catalog reload, and
  player logout behavior.
- Two-player contention for the final finite item.
- Native item tooltip fidelity for enchantments, names, lore, and components.
- No per-tick scans and bounded operation/stock maps.

## Decisions Needed Before Mutation Slices

The read-only Slice A does not depend on these answers. Before Slice B/C:

1. Payment source: recommended physical Sigils first, then banked balance, to
   match Portal payments. Alternative: bank-only merchant purchases.
2. Full inventory: recommended preflight rejection, with claimable mail used
   only for crash recovery or a race after payment.
3. Finite stock scope: recommended per placed NPC. Shared catalog stock can be
   added later as an explicit policy.
4. Sell pricing: defer until Slice E; choose fixed price keys or a configured
   percentage only after exact item matching is settled.

## Recommended Next Slice

Slice A is complete. The updated purchase, jurisdiction, tax, and quantity
sequence is specified in
`docs/reports/NPC_TRADE_PURCHASE_FOUNDATION_PROPOSAL.md`. Begin Economy
idempotent receipts only after world-tax destination and shop payment source
are approved.
