# NPC Sell And Buyback Proposal

Date: 2026-07-09

Status: proposal only. No production behavior changed.

## Objective

Define a dupe-safe, modular Sell/buyback contract for NPC traders before the
Sell tab is allowed to move player items, pay currency, affect stock, or use
future dynamic pricing/inflation rules.

## Current Verified Base

- NPCs owns catalogs, placed-NPC stock, trade sessions, purchase journals, and
  trade UI orchestration.
- Economy owns currency, physical Sigils, treasury destinations, taxes,
  idempotent operation receipts, and future dynamic price policy.
- BUY purchases are server-authoritative and use physical Sigils only.
- Stock is keyed by placed NPC UUID plus offer ID and restocks lazily.
- `price-key` already exists on NPC offers as the future hook for Economy-owned
  dynamic pricing/inflation.
- Sell is currently UI-only/pending and must not be implemented through
  arbitrary dialogue actions.

## Non-Negotiable Safety Invariants

- The client never supplies item identity, NBT/components, price, tax, stock, or
  payout amount as truth.
- A sale cannot pay the player until the exact sold stacks are removed from the
  player inventory and persisted in an NPC-owned escrow record.
- A replayed operation ID must never remove items twice or pay twice.
- A failed payout after escrow must leave a recoverable escrow record, not
  silently restore or delete items.
- A failed escrow/removal must not pay.
- Stock updates happen only after escrow and payout state are durable.
- Dynamic price/inflation policies are read through an Economy-owned pricing
  API. NPCs does not calculate inflation or duplicate Economy price rules.
- Runtime sale state lives under `world/elarion/addon-state/npcs/`; editable
  sell definitions live in `config/elarion/addons/npcs/trades.yml`.

## Ownership

NPCs owns:

- Sell offer definitions and accepted-item rules.
- Server-side inventory matching against the current player inventory.
- Sale journal and item escrow state.
- Merchant stock destination policy after a sale completes.
- Sale result events and UI snapshots.

Economy owns:

- Sell/buyback price calculation from fixed fallback price or `price-key`.
- Inflation/scarcity/tax/fee policy.
- Idempotent payouts and audit records.
- Currency delivery policy.

Core owns:

- Shared UI primitives.
- Domain event bus/history/notifications.
- Future deferred delivery/claimable reward infrastructure if used for payout
  recovery.

## Sell Definition Model

Extend `NpcTradeOfferDefinition` only after validation rules are approved.
Suggested fields:

```yaml
- id: cobblestone_buyback
  direction: sell
  label: "Cobblestone"
  subtitle: "Trader buys clean stone"
  item: "minecraft:cobblestone"
  count: 1
  price-key: "npc.sell.cobblestone"
  price: 1
  sell-match: exact_item
  component-policy: vanilla_only
  max-quantity: 64
  stock-destination: placed_npc
  enabled: true
```

Recommended V1 match modes:

- `exact_item`: registry item must match; components must be empty/default.
- `exact_stack`: registry item, custom name, lore, enchantments, model data,
  and allowed components must match the configured stack exactly.

Do not add broad wildcard buyback in V1. Arbitrary “sell anything” is a
separate market/appraisal system because it needs item valuation, component
normalization, exploit protection, and stronger audit tooling.

## Pricing And Inflation Boundary

Add an Economy-owned price quote boundary before Sell mutation:

```text
EconomyTradePriceProvider
  quoteBuy(authority, priceKey, fixedFallback, quantity, stockContext)
  quoteSell(authority, priceKey, fixedFallback, quantity, stockContext)
```

Conceptual inputs:

- authority: Realm or Worldheart tax/economy authority
- category: NPC_TRADE_BUY or NPC_TRADE_SELL if Economy splits the category
- price key
- fixed fallback price
- quantity
- placed NPC stock context: current stock, stock limit, catalog ID, offer ID
- optional future market context: Realm/world, time window, demand counters

Rules:

- Economy returns unit price, subtotal, fee/tax, total payout/cost, policy
  revision, and reason.
- NPCs records the returned price snapshot in the sale journal.
- NPCs must not read Economy internals or duplicate inflation math.
- Dynamic pricing must be bounded and cacheable. It must not scan all
  transaction history during ordinary quotes.
- `price-key` remains stable and admin-authored; display labels are never used
  as price identifiers.

## Server Sale Flow

Suggested request:

```text
NpcTradeSaleRequestPayload
  operationId
  npcId
  nodeId
  catalogId
  catalogRevision
  offerId
  quantity
```

Do not include item IDs, components, slot IDs, prices, taxes, or labels.

Server flow:

1. Revalidate active session, range, trade node, catalog revision, offer ID,
   direction `sell`, enabled state, and player permissions.
2. Ask Economy for a server sell quote using `price-key`, fixed fallback, and
   stock context.
3. Scan the current player inventory for matching stacks. Inventory scan is
   bounded to the player inventory only.
4. Persist `PREPARED` sale record with operation ID, player, NPC, offer,
   quantity, price snapshot, and exact required stack fingerprint.
5. Remove matching items from inventory into a serialized NPC-owned escrow
   payload, then persist `ITEMS_ESCROWED`.
6. Execute an Economy idempotent payout with the same operation ID.
7. Persist `PAID` with receipt IDs.
8. Apply stock destination policy, then persist `STOCK_UPDATED`.
9. Mark `COMPLETE` and send a refreshed snapshot/result.

If payout fails, keep `ITEMS_ESCROWED` and return a recoverable error. A
reconcile pass can retry payout or restore escrowed items through a deliberate
admin/recovery path. Do not guess.

## Sale Journal

Create a separate sale journal or a generalized trade operation journal. Do not
overload purchase records if that makes state transitions ambiguous.

Required states:

- `PREPARED`
- `ITEMS_ESCROWED`
- `PAID`
- `STOCK_UPDATED`
- `COMPLETE`
- `FAILED`
- `RESTORED`

Required persisted data:

- operation ID
- player UUID
- placed NPC UUID
- node ID, catalog ID, catalog revision, offer ID
- direction `SELL`
- requested quantity
- exact item escrow payload
- price snapshot and policy revision
- Economy payout operation/transaction IDs
- stock update result
- timestamps and message

Replay behavior:

- `PREPARED`: revalidate inventory and continue or fail without payout.
- `ITEMS_ESCROWED`: retry payout or mark recoverable; never remove more items.
- `PAID`: finish stock update.
- `STOCK_UPDATED`: complete.
- `COMPLETE` / `FAILED` / `RESTORED`: replay deterministic result.

## Inventory Escrow Contract

Slice 23M-Prereq inspected the current implementation before live Sell
settlement. Relevant source facts:

- `NpcTradeItemStacks` can create configured trade preview/delivery stacks and
  applies custom name, lore, custom model data, and enchantments through
  Minecraft 1.21.1 data components.
- `NpcTradePurchaseService` journals BUY operations before Economy settlement
  and final delivery. Its overflow delivery may drop items, so it is not a safe
  model for Sell escrow or restoration.
- `EconomyInventoryService` has bounded physical-Sigil helpers, but those are
  currency-specific and should not become a general item escrow service.
- `addons/underworld/model/StoredItemStack` already demonstrates the correct
  Fabric 1.21.1 mechanism for durable component-bearing stacks:
  `ItemStack.encode(registries)` compressed into Base64 and restored with
  `ItemStack.fromNbt(registries, ...)`.

Live Sell settlement must add an NPC-owned inventory escrow helper before any
player inventory mutation is enabled. It must run entirely on the server thread
and be called only from the future Sell settlement service.

Required helper responsibilities:

- Preflight the player's main inventory only for the requested offer and
  quantity. Do not trust client-selected slots.
- Match at most `offer.maxQuantity()` and the server quote quantity.
- Refuse stale catalog revisions and stale pricing policy revisions before
  removal.
- Serialize the exact removed stacks with `ItemStack.encode(registries)` before
  or during removal. The current lossy `NpcTradeEscrowStack` fields are not
  sufficient for live restoration of all component-bearing items.
- Remove items only after the helper has identified enough matching stack
  counts for the full request.
- Return a single result object containing success/failure, exact serialized
  escrow stacks, total removed count, and a stable item fingerprint for audit
  and replay checks.
- Restore from serialized escrow stacks if settlement fails before payout.
  Restoration should try the original slot first when recorded, then merge into
  compatible stacks, then empty slots. If anything cannot fit, keep the sale
  record non-complete and recoverable instead of dropping items.
- Mark the player inventory dirty after removal or restoration.

Required matching policy:

- `sell-match: exact_item` matches only the configured item ID.
- `sell-match: exact_stack` matches the configured offer-created stack by item
  and components, ignoring count, using Minecraft's item/component equality
  semantics.
- `component-policy: vanilla_only` rejects custom names, lore, enchantments,
  custom model data, and custom data beyond the default stack for that item.
  This is the safe default for simple buyback rows such as cobblestone.
- `component-policy: exact_components` requires the sold stack to match the
  configured offer-created stack's components. This is required for renamed,
  lored, enchanted, or custom-model offers.

Required storage adjustment before live settlement:

- Extend `NpcTradeEscrowStack` or add a replacement NPC-owned stored-stack
  record with full encoded stack payload, source slot, source label, item ID,
  count, and fingerprint.
- Do not depend on Underworld's `StoredItemStack` class from NPCs. Reuse the
  pattern, not the addon type, to avoid addon-to-addon implementation coupling.
- Keep schema-v1 compatibility by allowing existing lossy escrow rows to load,
  but new live rows must write the full encoded payload.

Recovery semantics:

- `PREPARED`: no inventory was removed. Revalidate and retry with a new server
  quote or fail safely.
- `ITEMS_ESCROWED`: items were already removed and serialized. Do not remove
  again. Retry payout, restore, or remain recoverable.
- `PAID`: do not restore automatically; the player has already received bank
  payout. Continue stock update and completion.
- `STOCK_UPDATED`: mark complete if the stock update is already recorded.
- `FAILED`: no payout should exist. If escrow exists, restore once and transition
  to `RESTORED`; otherwise remain failed.
- `RESTORED`: items were returned or remain in the recoverable restore path.
  Do not pay.

Required focused tests before enabling Sell packets/UI:

- Exact-item buyback removes the requested count across partial stacks once.
- Exact-stack buyback rejects a renamed/lored/enchanted item when the policy is
  `vanilla_only`.
- Exact-stack buyback accepts a configured named/lored/enchanted item only under
  `exact_components`.
- Partial-stack removal serializes and restores exact counts.
- Full inventory restore does not drop items; it leaves the sale recoverable if
  restoration cannot fit.
- Replaying `ITEMS_ESCROWED` does not remove more items.
- Replaying `PAID` does not restore items or pay twice.
- Stale catalog revision and stale pricing policy revision reject before
  inventory mutation.
- Two rapid Sell requests cannot consume the same player stack twice.
- Encoded stack round-trip preserves custom name, lore, enchantments, custom
  model data, damage, and custom data.

## Payout Policy

Approved V1 payout: bank wallet payout through Economy idempotent receipts.

Required Economy API before live Sell settlement:

```text
payPlayerBalanceRewardOnce(playerId, operation, amount, reason, sourceSystem, metadata)
```

This is a deliberate recovery policy, not an accidental shortcut. With the
current storage model, physical inventory insertion cannot be made atomic with
Economy receipt persistence. Wallet payout lets the Economy transaction receipt
and the player's bank balance persist through one journaled operation, so
restart replay does not duplicate credit.

Physical spending remains separate. NPC shop purchases, Shrine contributions,
and Portal/service payments still require carried Sigils unless a later
approved policy changes them. Future physical seller payout would require a
Core/Economy delivery queue that closes the post-receipt inventory insertion
gap.

## Stock Destination Policy

V1 options:

- `none`: sold items are consumed by the trader/economy sink; no resale stock.
- `placed_npc`: sold quantity increases the matching placed-NPC stock up to a
  configured cap.

Do not mix player sales into unrelated offers unless the catalog explicitly
declares a destination offer ID. Future marketplaces can add shared stock pools
as a separate policy.

## UI Contract

Sell UI must remain a thin client:

- Render server-authored accepted sell offers.
- Show server-authored max sellable quantity from current inventory.
- Show server quote, fee/tax, and payout.
- Send only operation ID, NPC/session identifiers, offer ID, and quantity.
- Use native item tooltips only on item icons.
- Show recovery messages for escrowed/pending payout states.

Do not let the UI choose inventory slots as authoritative. Slot previews may be
visual only after the server has sent an eligible inventory snapshot.

## Anti-Dupe Tests Required Before Mutation

- Replay same sale operation before and after restart does not remove/pay twice.
- Reusing operation ID with different offer/quantity/player is rejected.
- Payout failure after escrow keeps items in escrow and does not restore/pay
  automatically.
- Crash after escrow, after payout, and after stock update reconciles correctly.
- Partial stacks are removed exactly once.
- Full inventory payout recovery does not lose or duplicate Sigils.
- Component mismatch rejects renamed/lored/enchanted items unless exact-stack
  policy explicitly allows them.
- Two rapid Sell requests cannot sell the same stack twice.
- Catalog reload between quote and sell rejects stale revision.
- NPC removal after escrow does not lose recovery state.
- Dynamic price policy revision mismatch rejects stale client quotes.

## Implementation Slices

### Slice 23F - Sell/Buyback Proposal

Status: this document. No production code.

### Slice 23G - Economy Price/Payout API Proposal

Define dynamic price quote and idempotent physical payout APIs. Include
inflation/scarcity inputs and bounded lookup guarantees. No NPC mutation yet.

### Slice 23H - Economy Trade Price Models

Status: completed.

Added Economy-owned `quoteTradePrice(...)` models/API. No NPC mutation.

### Slice 23I - Idempotent Physical Payout Wrapper

Status: completed.

Added `payPhysicalRewardOnce(...)`, but it is not approved for live Sell
because physical inventory delivery still has a post-receipt recovery gap.

### Slice 23J - Bank-Backed Seller Payout Recovery

Status: completed.

Added `payPlayerBalanceRewardOnce(...)` and selected wallet payout as the V1
Sell recovery policy.

### Slice 23K - Sell Definition Parsing

Status: completed on 2026-07-09.

Added disabled-by-default SELL definitions, validation, descriptors/tests, and
default config shape. No server snapshot rows, inventory mutation, item escrow,
payout execution, packets, prompts/actions, or client-side shop mutation.

### Slice 23L - Sale Journal And Escrow Storage

Status: completed on 2026-07-09.

Added schema-v1 sale journal and escrow state at
`world/elarion/addon-state/npcs/trade-sales.json`. Added explicit sale states,
serialized escrow stack records, request matching, transition helpers,
round-trip tests, restart/replay-state tests, and unsupported-schema
fail-closed tests. No payout, player inventory mutation, packets, prompts,
actions, or client-side UI mutation.

### Slice 23M - Server Sell Settlement

Status: completed on 2026-07-09.

Added component-preserving escrow payloads, server-side inventory matching and
removal, Economy wallet payout through idempotent sale receipts, sale replay
handling for escrowed/paid/stock-updated records, and client Sell tab
enablement through the existing trade request/result payload path.

Superseded limitation: `stock-destination: placed_npc` originally parsed as a
no-op because the config model did not define a destination offer ID. Slice 24
added that route.

### Slice 24 - Stock Destination Hardening

Status: completed on 2026-07-09.

Added `destination-offer` to Sell offer definitions and descriptors. Validation
requires `stock-destination: placed_npc` to target a BUY offer in the same
catalog. Paid sales now idempotently supply the target placed-NPC BUY stock by
sale ID before completing. Supply is capped by the destination stock limit and
replay cannot increment stock twice.

### Slice 23M-Prereq - Inventory Escrow Service Proposal

Status: completed on 2026-07-09.

Inspected current NPC item stack helpers, BUY settlement, Economy physical
currency helpers, and Underworld component-bearing stack serialization. Defined
the required NPC-owned escrow helper contract, matching policy, full encoded
stack storage requirement, replay semantics, and focused tests. No production
inventory mutation, packets, prompts, actions, or UI mutation was added.

## Recommended Next Slice

Phase 4 Slice 25: NPC trade live QA and older-config migration assessment.
Verify Buy/Sell replay, destination stock replenishment, item tooltips, Sigil
presentation, and older `trades.yml` files that may lack `destination-offer`.
Do not add wildcard selling, dynamic stock pools, or richer pricing policy
without a separate proposal.
