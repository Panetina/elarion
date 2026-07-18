# NPC Trade Purchase Foundation Proposal

Date: 2026-07-08

Status: Slices 23A-23E complete. Finite BUY purchases are enabled. Selling
remains disabled.

## Objective

Define the bounded implementation sequence for crash-safe NPC purchases,
Realm/world tax jurisdiction, server-authored tax quotes, and multi-unit buying.

## Scope

Included:

- Economy idempotent operation receipts.
- One stable tax jurisdiction registration for every placed NPC.
- NPC definition policy plus placement-owned resolved registration.
- Realm/world-specific shop tax lookup and routing.
- Server-authored unit price, subtotal, tax, total, quantity limits, and reason.
- A selected-offer quantity/action panel in the existing trader screen.
- BUY purchases with finite placed-NPC stock before selling.

Excluded:

- Selling and buyback.
- Per-player stock.
- Dynamic inflation implementation beyond the existing `price-key` hook.
- Government-authored tax mutation until Economy exposes a stable policy API.
- Client-side price, tax, jurisdiction, stock, or eligibility decisions.

## Verified Existing Boundaries

- NPC definitions and catalogs are parsed immutable config snapshots.
- Placed NPC records already own stable NPC UUID, definition ID, world ID, and
  coordinates in schema-versioned NPC runtime storage.
- Core `RealmService.ownerForWorld(worldId)` provides cached canonical Realm
  ownership for configured Realm worlds.
- Economy owns Realm treasuries, transaction journals, physical Sigils, shop
  sales-tax configuration, and price-key resolution.
- The current trade snapshot is bounded to 64 offers and remains read-only.
- Economy has no idempotent caller-operation index. NPC purchases cannot be
  enabled safely until one exists.

## NPC Jurisdiction Contract

Each NPC definition gains a tax-jurisdiction policy:

```yaml
tax-jurisdiction: auto
# or realm:aaa
# or world:minecraft:overworld
```

The definition is policy, not mutable runtime truth. Each placed NPC record
stores the resolved registration:

```text
taxJurisdictionKind = REALM | WORLD
taxJurisdictionId   = stable Realm id or namespaced world id
```

Rules:

- `auto` resolves to the Realm owning the placement world; otherwise it
  resolves to that world.
- `realm:<id>` requires a canonical Core Realm and a placement world owned by
  that Realm.
- `world:<id>` requires the NPC to be placed in that exact world.
- Moving an `auto` NPC re-resolves registration atomically with the move.
- Moving an explicitly registered NPC to a conflicting world disables trade
  until an administrator corrects the registration; it never silently changes
  the tax recipient.
- Existing placement schema migrates by resolving current world ownership.
  A backup is required before writing the upgraded file.
- Definitions missing the field use `auto` for compatibility.

This supports reuse of one merchant definition in multiple Realms while every
placed NPC still has one explicit, auditable registration.

## Tax Policy And Quote

Economy remains the only tax calculator and money owner. NPCs sends an
immutable jurisdiction and price request through its optional Economy adapter.

Implemented policy order:

1. Exact Realm override.
2. Exact owner-administered Worldheart override for non-Realm activity.
3. Existing global `shops.sales-tax-basis-points` fallback for NPC trades.

Tax is calculated from the complete subtotal, not once per item:

```text
subtotal = unitPrice * requestedUnits
tax      = floor(subtotal * basisPoints / 10000)
total    = subtotal + tax
```

All multiplication/addition uses checked arithmetic. Overflow rejects the
quote and purchase. The quote records jurisdiction, policy source, unit price,
requested units, delivered item count, subtotal, tax, total, maximum quantity,
and catalog/price revisions. The server recalculates it during purchase.

Realm tax routes to the existing Realm treasury. Non-Realm tax routes to the
stable Worldheart treasury account. Worldheart treasury ownership is separate
from Worldheart governing authority; a future player ruler controls the domain
through Core authority checks and never owns the treasury as a personal wallet.
Settlement remains deferred to Slice 23D so the quote slice cannot move money.

## Quantity UI

Selecting a catalog row does not mutate state. It opens a bounded purchase
panel inside the same trader screen:

```text
[item icon] Offer title                     Unit: [Sigil] 25
Quantity       [-] [  1  ] [+] [Max]
Subtotal                                  [Sigil] 25
Tax (5%)                                    [Sigil] 2
Total                                     [Sigil] 27
[Back to Conversation]             [Buy 1]
```

Behavior:

- Catalog rows stay compact and keep native tooltips on the item icon only.
- Quantity counts offer units. An offer with item count `2` and quantity `3`
  delivers six items.
- Minus/plus step by one; the numeric field accepts bounded digits; `Max` uses
  the server-authored maximum.
- The field uses the shared blinking input caret.
- Subtotal, tax, and total are server-authored and refresh with quantity.
- Confirm stays disabled until the purchase mutation slice is complete.
- Tax is always visible, including `0`, so the total is unambiguous.
- Layout, hitboxes, clipping, and reflow use shared typography metrics.

Recommended initial request limit is 64 offer units. The server may lower this
for stock, delivery capacity, account limits, or offer bounds.

## Idempotent Economy Receipt Contract

Economy adds a bounded O(1) receipt index keyed by owner namespace and stable
operation ID. It must not detect duplicates by scanning transaction JSONL.

Suggested public contract:

```text
EconomyOperationKey(owner, operationId)
EconomyOperationReceipt(status, transactionIds, amount, metadata, createdAt)
chargeOnce(...)
refundOnce(...)
receipt(...)
```

Invariants:

- Replaying the same key and identical request returns the original receipt.
- Reusing a key with different accounts, amount, or purpose is rejected.
- A successful charge is journaled before its successful receipt.
- Refund is independently idempotent and references the charge receipt.
- Receipt lookup is O(1); ordinary gameplay never scans historical JSONL.
- Retention is bounded, but non-terminal NPC purchase references cannot prune.

## Implementation Slices

### Slice 23A - Economy Receipt Schema And Tests

Classification: LARGE persistence/API slice.

Status: completed on 2026-07-08.

- Added operation-key/receipt models, schema-v1 to schema-v2 migration with
  backup/fail-closed behavior, journal reconstruction, bounded retention,
  public idempotent methods, and restart/conflict tests.
- No NPC packets, UI mutations, or purchases.

### Slice 23B - NPC Jurisdiction Registration

Classification: MEDIUM config/persistence slice.

Status: completed on 2026-07-08.

- Added definition policy, defaults, loader, validator, descriptors/tests,
  schema-v2 placement migration, resolved jurisdiction, inspect output, and
  reload prevalidation/rollback.
- No tax charging or purchases.

### Slice 23C - Tax Quote And Quantity UI

Classification: MEDIUM network/UI slice.

Status: completed on 2026-07-08.

- Added strict schema-v1 `tax-policies.json`, O(1) Realm/Worldheart category
  lookup, checked quote arithmetic, optional NPC Economy adapter, bounded
  request/response payloads, and a compact selected-offer quantity panel.
- Server session, range, node, catalog revision, and offer ID are revalidated
  for every quote. Confirm remains disabled.

### Slice 23C.1 - Worldheart Authority And Treasury Route

Classification: MEDIUM Core/Economy infrastructure slice.

Status: completed on 2026-07-09.

- Added Core-owned persistent Worldheart governing authority state with
  `SYSTEM` and `PLAYER` modes. Missing state defaults to system governance by
  the lore-facing `Hollow Emperor`.
- Added central Worldheart role checks that distinguish server administrator,
  current Worldheart ruler, and ordinary players. Future blocks or UI controls
  should call this service instead of checking OP/player UUIDs directly.
- Added a Core domain event for authority changes. No future throne,
  ascension, ceremony, eligibility, UI, or Emperor gameplay was implemented.
- Added Economy's stable `WORLDHEART_TREASURY` account and schema-v3
  persistence so Worldheart revenue does not use fake players or generic
  non-persistent system accounts.
- Added Economy tax destination routing: Realm authorities resolve to their
  Realm treasury, and Worldheart/non-Realm authorities resolve to the stable
  Worldheart treasury.
- Verified that changing Worldheart authority to or from a player does not
  transfer or reset treasury funds.

### Slice 23D - Unlimited Purchase Journal

Classification: LARGE NPC persistence/network slice.

Status: completed on 2026-07-09.

- Added request/result packets with a client-generated purchase ID.
- Added NPC-owned schema-v1 `trade-purchases.json` journal with
  `PREPARED`, `PAID`, `COMPLETE`, and `FAILED` states.
- Added idempotent Economy public-revenue settlement from carried physical
  Sigils to the resolved Realm or Worldheart treasury. Bank balances are not
  used for shop purchases.
- Reused the existing server quote path during purchase; the server revalidates
  session, range, node, catalog revision, offer ID, item availability, quantity,
  and quote validity before charging.
- Delivery uses the same item-stack construction as catalog previews, including
  custom names, model data, lore, and enchantments.
- Paid-but-not-complete records are reconciled on player join and on request
  replay. Completed/failed purchase IDs replay deterministically.
- Only unlimited BUY offers are implemented.

### Slice 23E - Finite Placed-NPC Stock

Classification: MEDIUM persistence slice.

Status: completed on 2026-07-09.

- Added parsed offer stock fields: `stock-limit`, `restock-amount`, and
  `restock-interval-seconds`. Existing offers without stock settings remain
  unlimited.
- Added NPC-owned schema-v1 `trade-stock.json` keyed by placed NPC UUID plus
  offer ID. One catalog can be reused by multiple NPCs without sharing stock.
- Added lazy restock on open/quote/purchase paths. No merchant tick loop or
  global stock scan was introduced.
- Added bounded consumed-purchase ID retention per stock record so replaying
  the same purchase cannot decrement stock twice.
- Stock now clamps server-authored quote maximums and is revalidated during
  purchase before Economy settlement/delivery. Stock labels render in compact
  trade rows while item tooltips remain icon-only.
- Selling remains separate.

Sell/buyback is specified separately in
`docs/reports/NPC_SELL_BUYBACK_PROPOSAL.md`. Its next dependency is an
Economy-owned dynamic price and idempotent payout API proposal before any Sell
mutation is implemented.

## Compatibility And Risks

- Existing definitions remain valid through `tax-jurisdiction: auto`.
- Placement and Economy storage changes each require independent backup,
  failed-migration rollback, round-trip, and restart tests.
- Existing commands and read-only catalogs remain compatible.
- Optional Economy absence leaves priced offers visible but disabled.
- Realm/world tax overrides require truthful typed config descriptors.
- The UI never calculates authoritative tax or trusts client quantity.

## Approved Tax And Payment Policy

- Every Realm can configure category-specific tax rates through a future Seat
  of Rule Taxes tab. Economy owns rates and settlement; Government supplies
  authorized Realm administration and UI.
- Realm-jurisdiction revenue is deposited into that Realm's treasury.
- Non-Realm world activity uses an owner-administered Worldheart tax authority
  and treasury. This includes Worldheart, marketplace, Nether, End, and other
  configured world-level services/trades.
- Shop purchases consume physical Sigils only. Bank balances must be withdrawn
  before spending.
- Category policy must support at least NPC trade and Portal/service taxes
  without encoding rates inside NPC or Portal state.

## Verification

- Config descriptor and migration rollback tests.
- Economy receipt serialization/restart/idempotency tests.
- NPC placement old-schema migration tests.
- Quote arithmetic, overflow, and jurisdiction mismatch tests.
- Packet bounds and stale-revision rejection tests.
- Quantity panel layout/hitbox tests at 100%, 125%, and 150% font scale.
- Live QA for selection, caret, Max, tax visibility, item tooltips, and refresh.
