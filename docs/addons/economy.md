# Economy Addon Contract

Last reviewed: 2026-07-05

Author: Panyel  
Team: Panetina Team

## Ownership

Economy owns:

- `elarion:currency`
- `elarion:economy` Creative tab
- player wallets
- Realm treasuries
- transaction records
- physical currency deposit/withdraw conversion
- bank interest and withdrawal tax policy
- shop sales tax policy metadata
- Economy Governor monitor state
- future adaptive price state

Core remains authoritative for citizens, Realms, permissions, and durable
history. Economy validates Realm IDs through Core and emits successful
transaction outcomes into Core history.

All future Economy items must be added to the Economy-owned Creative tab in
the same change that registers the item.

## Currency Lore

By default, the official currency is shown as **Sigils**. The visible name,
treasury, seal, and related wording are configured in
`config/elarion/core/server_identity.yml`; the registry ID remains the generic
`elarion:currency`. Physical Sigil item stacks can hold up to 999 to reduce
inventory clutter when players withdraw larger bank balances. Economy owns the
runtime support for this higher physical stack size through its item setting,
inventory slot ceiling mixin, and serialized ItemStack count codec mixin;
changing only `Item.Settings.maxCount` falls back to the vanilla 99-count
inventory cap.

`LORE.md` owns the canonical narrative wording. Economy code owns the currency
item, balances, treasuries, and transactions.

## Transaction Rule

Wallets and treasuries never mutate themselves. Every movement passes through
`EconomyTransactionService` and identifies:

- transaction type
- source and destination accounts
- amount
- actor
- reason
- source system
- success or failure
- source and destination balances before and after

The monthly JSONL transaction log is written before a successful balance
mutation. If the journal append fails, balances remain unchanged. Compact
atomic snapshots prevent full history replay during ordinary restart.

Idempotent addon operations use `EconomyOperationKey` and
`ElarionEconomyApi.transactOnce(...)`. Operation identity and request
fingerprints are included in the durable transaction journal, while a bounded
O(1) receipt index is stored in schema-v2 `economy-state.json`. Startup replay
reconstructs receipts written after the previous snapshot, preventing a
crash/retry from charging twice. Reusing an operation ID for different request
data returns `IDEMPOTENCY_CONFLICT`. Retention is bounded by
`operations.receipt-retention-days` and `operations.max-receipts`.

Schema-v1 snapshots are backed up as
`economy-state.json.schema-v1.bak` before atomic migration. Unsupported or
malformed state fails closed instead of silently replacing balances.

## Public API

Future Bank, Market, Portal, Quest, Offering, Government, NPC, and Contract
systems consume `ElarionEconomyApi`. They must use distinctive `sourceSystem`
IDs so Governor diagnostics can attribute faucets and sinks without parsing
reason text.

Addons that only need the Economy currency identifier or Economy item-group
key use `EconomyContentApi`; they must not import the internal
`EconomyItems` registration class. Portal uses this content-only boundary for
ticket-tab contribution and route-icon validation.

Economy owns the current Banker NPC actions:

- `elarion:economy_wallet_balance`
- `elarion:economy_deposit_all_currency`
- `elarion:economy_withdraw_currency`
- `elarion:economy_bank_balance`
- `elarion:economy_deposit_currency_amount`
- `elarion:economy_withdraw_currency_amount`

These actions are registered by Economy and called by NPC dialogue config. NPCs
never mutate wallets or physical currency directly.

The NPC addon may present these actions through its dedicated bank node/screen,
but Economy remains authoritative. The bank client sends the same bounded NPC
prompt request; the server revalidates the active NPC session, range, option,
amount, and Economy transaction before updating and resynchronizing the bank
view. No player-facing transaction history is scanned for this screen.
Deposits are untaxed. Withdrawals can charge a configurable bank tax from the
bank balance before physical currency is issued.
The bank UI must not derive this tax locally. Visible Fee/Total preview comes
from `ElarionEconomyApi.quoteBank(...)`, which delegates to
`EconomyInventoryService.quoteBank(...)`. Quotes report Deposit as zero-fee
physical-currency intake and Withdraw as `amount + withdrawal tax`. Quotes do
not mutate wallets, physical currency, journals, or treasury state; submit still
uses Economy's authoritative deposit/withdraw transaction path.

Economy also owns Core reward handlers for currency rewards and sinks:

- `currency-reward`
- `currency-sink`
- `realm-currency-reward`
- `realm-currency-sink`
- `realm-treasury-grant`

Core config validation explicitly allows these action IDs because reward config
loads before addons can register handlers. The implementation remains
Economy-owned.

Future addons can use shared registry actions instead of calling transaction
services directly:

- `elarion:economy_reward_player`
- `elarion:economy_sink_player`
- `elarion:economy_reward_realm`
- `elarion:economy_sink_realm`
- `elarion:economy_treasury_grant`

All of these routes must supply a positive `amount`, a clear `reason`, and a
distinctive `source-system` when practical.

OP transaction queries are bounded by month and result count. Before rich
player-facing transaction GUIs are added, create dedicated account indexes or
summaries rather than widening JSONL scans.

Bounded service prices live in
`config/elarion/addons/economy/service_prices.yml`. Consumers reference stable
IDs and call `ElarionEconomyApi.servicePrice(id)`. Current Portal keys are
`portal_ticket.nether`, `portal_ticket.end`, and `ancient_gate.passage`.
Future Governor adjustments can therefore change current prices without moving
route ownership into Economy.

Portal passage fees and Shrine currency offerings must consume carried
physical currency only. Banked currency is protected from direct service
spending; players must withdraw first, paying any configured withdrawal tax.
The legacy mixed physical-then-bank API remains internal compatibility surface
for future approved migrations, but current Portals and Offerings do not use
bank balances.

Bank interest is config-backed and disabled by default. When enabled, Economy
processes eligible wallet accounts in bounded batches using
`bank.interest.max-accounts-per-tick`; payouts are audited reward
transactions. Shop sales tax is exposed as config metadata for the future
server-authoritative shop/trader purchase slice, but current trader rows remain
read-only and non-mutating.

Economy registers a read-only config descriptor domain named `economy` through
`ElarionApi.system().configs()`. The descriptors read the active
`EconomyTransactionService.config()` and `EconomyPricingService.definitions()`
snapshots, so Admin Panel discovery does not parse config files. The domain
exposes persistence, query, Governor, and service-price metadata. Config
editing and reload-atomicity fixes remain future approved Config/Admin slices.

## Tax Policy And Quotes

Economy owns mutable category tax policy in
`world/elarion/addon-state/economy/tax-policies.json`. The strict schema-v1
file stores explicit Realm or Worldheart overrides plus a monotonic policy
revision. Missing NPC-trade overrides fall back to
`economy.yml.shops.sales-tax-basis-points`; other categories default to zero.

`EconomyTaxPolicyService` performs O(1) lookup and checked quote arithmetic.
`ElarionEconomyApi.quoteTax(...)` is the shared server boundary. The setter is
for trusted server addons; future Government and Admin Panel surfaces must
authorize callers. Quotes do not move money; treasury settlement remains in
the purchase slice.

Worldheart revenue uses the dedicated `WORLDHEART_TREASURY` account and the
schema-v3 `worldheartTreasury` balance in Economy state. This is not a generic
system account and not a fake player account. `ElarionEconomyApi.taxDestination`
routes Realm authorities to their Realm treasury and Worldheart/non-Realm
authorities to the stable Worldheart treasury. Core owns who may govern
Worldheart; Economy owns where Worldheart money is stored.

NPC shop purchases use `PUBLIC_REVENUE` transactions from physical Sigils into
the resolved public treasury. `ElarionEconomyApi.payPhysicalOnlyOnce(...)`
checks for an existing Economy operation receipt before touching inventory, so
purchase replays do not remove carried Sigils twice. This helper is for
physical-currency services only; bank balances remain unavailable for NPC shop,
Shrine, and Portal service spending unless a future approved policy changes it.

NPC Sell/buyback and inflation work must use the Economy-owned trade price and
payout API.
`ElarionEconomyApi.quoteTradePrice(...)` currently resolves fixed fallbacks or
known service-price `price-key` values, then applies Economy tax/fee policy.
Future dynamic inflation/scarcity counters must be added behind this API.
`ElarionEconomyApi.payPhysicalRewardOnce(...)` provides an idempotent physical
payout wrapper that checks operation receipts before inserting Sigils and
rejects insufficient inventory space before writing a transaction. It must not
be used for live NPC Sell settlement until a claimable/deferred payout delivery
path closes the crash-after-receipt delivery gap.

V1 NPC Sell settlement should use the bank-backed recovery policy through
`ElarionEconomyApi.payPlayerBalanceRewardOnce(...)`. That method credits the
player wallet with an idempotent Economy `REWARD` receipt, survives restart
replay without duplicate credit, and avoids physical inventory delivery as a
settlement dependency. This does not make banked money spendable for NPC shops,
Shrines, or Portals. NPCs may pass catalog/offer/stock context into Economy,
but must not calculate inflation or mutate balances directly.

## Command Presentation

Economy inspection commands use `CommandOutput` for chat-readable sections and
key-value rows. Keep `/e economy pulse`, wallet/treasury inspection,
transaction query output, and future Governor dashboards readable for server
owners who are not reading raw logs.
