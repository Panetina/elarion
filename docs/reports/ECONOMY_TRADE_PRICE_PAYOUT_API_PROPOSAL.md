# Economy Trade Price And Payout API Proposal

Date: 2026-07-09

Status: proposal only. No production behavior changed.

## Objective

Define the Economy-owned API surface required before NPC Sell/buyback can move
items or pay players, while keeping future inflation, scarcity, stock-sensitive
pricing, taxes, and physical payouts modular and dupe-safe.

This proposal supports NPC trades first, but the API must be reusable by future
marketplace, portal-service, event-shop, and Worldheart-owned public revenue
systems.

## Current Verified Base

- `EconomyPricingService.currentPrice(priceId)` returns static service price
  config values. It has a comment placeholder for future Governor/dynamic state.
- `ElarionEconomyApi.quoteTax(...)` returns checked subtotal/tax/total quotes
  for a unit price and quantity.
- Economy has idempotent operation receipts through `EconomyOperationKey` and
  `EconomyTransactionService.executeOnce(...)`.
- `ElarionEconomyApi.payPhysicalOnlyOnce(...)` safely charges carried physical
  Sigils before NPC BUY settlement.
- Economy can mint rewards to players through `reward(...)`, but there is no
  idempotent physical payout wrapper that checks an operation receipt before
  inserting currency into inventory.
- NPC offers already carry `price-key` and fixed `price` fallback.

## Ownership Decision

Economy owns:

- Price resolution for `price-key`.
- Dynamic price modifiers, inflation, scarcity, demand, taxes, fees, and policy
  revisions.
- Currency payout and audit receipts.
- Bounded price-policy indexes/caches.

NPCs owns:

- Catalog definitions, offer IDs, stock state, sale journals, and item escrow.
- Passing compact stock/context inputs to Economy.
- Recording the returned price snapshot in the NPC trade journal.

Economy must not read NPC storage directly. NPCs must not calculate Economy
inflation or mutate balances directly.

## Trade Price Quote API

Add an Economy-owned quote model and API after implementation approval.

Conceptual API:

```text
EconomyTradePriceQuote quoteTradePrice(
  EconomyTradePriceRequest request
)
```

Conceptual request fields:

```text
direction                  BUY | SELL
authority                  Realm or Worldheart authority
taxCategory                NPC_TRADE or a future split category
priceKey                   optional stable key
fixedUnitPriceFallback     required positive fallback
quantity                   bounded positive quantity
maxQuantity                caller/server maximum
stockRemaining             optional; -1 for unlimited
stockLimit                 optional; 0 for unlimited/unknown
catalogId
offerId
sourceSystem
context                    bounded string map
```

Conceptual quote fields:

```text
direction
unitPrice
quantity
maxQuantity
subtotal
feeOrTaxBasisPoints
feeOrTax
totalCost                 for BUY
totalPayout               for SELL
policyRevision
priceRevision
priceSource               fixed, service-price, dynamic, unavailable
valid
message
```

Rules:

- If `priceKey` is known, Economy uses it. If it is blank or unknown and the
  fallback is positive, Economy uses the fallback with `priceSource=fixed`.
- Unknown `priceKey` should not crash an addon. It should return an invalid
  quote only when the offer explicitly requires dynamic pricing.
- Arithmetic must use checked multiplication/addition/subtraction.
- `quantity` is clamped or rejected consistently with current tax quotes.
- Quote metadata is immutable enough to store in NPC journals and compare on
  stale requests.
- The quote must include revision fields so NPCs can reject stale client quotes.

## Inflation And Stock Inputs

Do not implement full inflation in the first Java slice. Design the quote API
so future policy can use bounded inputs without changing NPCs.

Allowed future inputs:

- price key
- Realm/Worldheart authority
- trade direction
- quantity
- stock remaining and stock limit
- recent demand counters maintained by Economy-owned bounded summaries
- configured min/max price limits
- optional category or tag from the offer context

Forbidden implementation pattern:

- scanning all Economy transaction JSONL history during quote
- reading NPC stock files from Economy
- using display names as price keys
- deriving price from client-submitted item data
- storing inflation state in NPC catalogs

Recommended dynamic model:

- Static config defines base/min/max and optional sensitivity values per
  `price-key`.
- Economy maintains bounded counters by `price-key`, authority, and time bucket
  only when a trade completes.
- Quote lookup is O(1) or O(log n) over bounded in-memory maps loaded from
  Economy state.
- Policy revision increments when dynamic config or relevant policy state
  changes.

## Sell Payout API

NPC Sell requires an idempotent payout API that checks receipts before inserting
currency.

Conceptual API:

```text
TransactionResult payPhysicalRewardOnce(
  ServerPlayerEntity player,
  EconomyOperationKey operation,
  long amount,
  String reason,
  String sourceSystem,
  Map<String, String> metadata
)
```

Rules:

- Check `operationReceipt(operation)` before touching player inventory.
- Persist an idempotent Economy `REWARD` or dedicated payout transaction before
  inserting physical Sigils.
- Replaying the same operation returns the original receipt/result and must not
  insert more currency.
- Reusing an operation ID with a different amount, player, reason, or metadata
  fingerprint returns an idempotency conflict.
- Amount must be positive and within safe physical stack insertion limits.
- If physical inventory insertion fails, use an explicit approved recovery
  path. Preferred future path: Core claimable/deferred delivery. Temporary
  fallback must be documented before implementation; silent dropping is not
  acceptable for seller payouts.

Implementation note:

- Current `EconomyTransactionType.REWARD` permits `MINT -> PHYSICAL_CURRENCY`.
  The payout wrapper can reuse this flow if the fingerprint includes the
  intended player and amount. A dedicated transaction type may be added only if
  reports/commands need to distinguish merchant payouts from other rewards.

## Tax/Fee Policy For SELL

BUY currently treats subtotal plus tax as public revenue. SELL needs a separate
policy decision:

- Option A: no tax on seller payouts in V1.
- Option B: marketplace fee withheld from payout and routed to Realm/Worldheart
  treasury.

Recommendation: implement quote model with a generic `feeOrTax` field and
start V1 with configurable zero fee. Do not hard-code no-fee in packet/model
names, because Realm/Worldheart markets will likely need fees later.

If fees are enabled:

```text
grossPayout = unitPrice * quantity
fee         = floor(grossPayout * basisPoints / 10000)
netPayout   = grossPayout - fee
```

Fee routing belongs to Economy. NPCs records the quote and calls the payout
API; it does not decide treasury routing.

## Receipt Fingerprint

The idempotency fingerprint for trade pricing/payout should include:

- operation owner, operation UUID
- player UUID
- direction
- authority kind/id
- catalog ID and offer ID
- quantity
- unit price, fee/tax, total cost or payout
- policy revision and price revision
- source system

Metadata should include bounded identifiers for audit/search:

- `npcId`
- `catalogId`
- `offerId`
- `direction`
- `priceKey`
- `quantity`
- `unitPrice`
- `gross`
- `feeOrTax`
- `total`
- `authority`
- `policyRevision`
- `priceRevision`

## Tests Required Before Java Implementation

- Static fallback price quote for BUY and SELL.
- Known `price-key` quote resolves through service price config.
- Unknown optional `price-key` falls back safely.
- Unknown required `price-key` returns invalid quote.
- Checked arithmetic rejects overflow.
- Stock context does not require Economy to read NPC files.
- Policy revision changes when price policy changes.
- `payPhysicalRewardOnce` replay does not insert currency twice.
- Reused payout operation ID with different fingerprint conflicts.
- Payout transaction persists before inventory insertion.
- Full-inventory behavior follows the approved recovery policy.
- Optional NPC absence: Economy API tests do not import NPC internals.

## Implementation Slices

### Slice 23G - Proposal

Status: this document. No production code.

### Slice 23H - Economy Trade Price Models And Tests

Status: completed on 2026-07-09.

- Added `EconomyTradePriceRequest`, `EconomyTradePriceQuote`,
  `EconomyTradeDirection`, and `EconomyTradePriceSource`.
- Added `EconomyPricingService.quoteTradePrice(...)` and
  `ElarionEconomyApi.quoteTradePrice(...)`.
- Current implementation resolves fixed fallbacks or existing service-price
  keys only. Full dynamic inflation/scarcity counters remain future work behind
  the same API.
- SELL quotes return net payout as subtotal minus fee/tax. BUY quotes return
  total cost as subtotal plus tax.
- Added unit coverage for fallback price, service price, unknown required
  `price-key`, SELL net payout, overflow/out-of-bounds rejection, and bounded
  context normalization.
- No payout and no NPC mutation.

### Slice 23I - Idempotent Physical Payout Wrapper

Status: completed on 2026-07-09.

- Added `EconomyInventoryService.payPhysicalRewardOnce(...)` and public
  `ElarionEconomyApi.payPhysicalRewardOnce(...)`.
- The wrapper checks the Economy operation receipt before inventory insertion
  so replay does not insert duplicate Sigils.
- The wrapper rejects invalid amounts and full/insufficient inventory capacity
  before writing a transaction. It does not drop overflow currency on the
  ground.
- The wrapper records an idempotent `REWARD` transaction from `MINT` to
  `PHYSICAL_CURRENCY` before inserting physical Sigils.
- Added capacity math tests. Existing transaction receipt tests cover
  idempotent replay/fingerprint conflict at the transaction layer.
- No NPC Sell mutation.

Limitation: this wrapper is dupe-safe but still has a crash recovery gap if the
server stops after the transaction receipt is persisted and before physical
Sigils are inserted. NPC Sell must not rely on this wrapper for live escrowed
sales until a claimable/deferred payout delivery path or accepted bank-payout
policy closes that gap.

### Slice 23J - Payout Delivery Recovery Policy

Status: completed on 2026-07-09.

- Added `EconomyTransactionService.rewardOnce(...)`.
- Added public `ElarionEconomyApi.rewardOnce(...)`.
- Added public `ElarionEconomyApi.payPlayerBalanceRewardOnce(...)` for V1
  seller payouts.
- V1 NPC Sell should pay the seller's bank wallet, not physical inventory.
  This is the accepted recovery-safe policy for the first Sell implementation:
  the Economy transaction receipt and player wallet balance are persisted in
  one Economy transaction path and replay after restart does not duplicate
  credit.
- Physical-only spending remains unchanged. NPC shop purchases, Shrine
  contributions, and Portal/service payments should still require carried
  Sigils through physical payment APIs.
- `payPhysicalRewardOnce(...)` remains available for future physical payout
  work, but it is not approved for live NPC Sell until Core/Economy has a
  delivery queue that can close the post-receipt inventory insertion gap.
- Added transaction tests proving `rewardOnce(...)` credits a player wallet
  once, rejects conflicting replay, and survives restart replay without
  duplicate credit.
- No NPC Sell definitions, prompts, packet mutations, item escrow, or client
  shop mutation were added.

## Recommended Next Slice

Phase 4 Slice 23K: NPC Sell definition parsing. Add disabled-by-default SELL
definitions, descriptors, and validation only. No inventory mutation, payout,
escrow, prompts/actions, packets, or client-side shop mutation.
