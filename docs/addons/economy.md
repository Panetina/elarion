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
`elarion:currency`.

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

## Public API

Future Bank, Market, Portal, Quest, Offering, Government, NPC, and Contract
systems consume `ElarionEconomyApi`. They must use distinctive `sourceSystem`
IDs so Governor diagnostics can attribute faucets and sinks without parsing
reason text.

Economy owns the current Banker NPC actions:

- `elarion:economy_wallet_balance`
- `elarion:economy_deposit_all_currency`
- `elarion:economy_withdraw_currency`
- `elarion:economy_bank_balance`
- `elarion:economy_deposit_currency_amount`
- `elarion:economy_withdraw_currency_amount`

These actions are registered by Economy and called by NPC dialogue config. NPCs
never mutate wallets or physical currency directly.

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

Economy registers a read-only config descriptor domain named `economy` through
`ElarionApi.system().configs()`. The descriptors read the active
`EconomyTransactionService.config()` and `EconomyPricingService.definitions()`
snapshots, so Admin Panel discovery does not parse config files. The domain
exposes persistence, query, Governor, and service-price metadata. Config
editing and reload-atomicity fixes remain future approved Config/Admin slices.

## Command Presentation

Economy inspection commands use `CommandOutput` for chat-readable sections and
key-value rows. Keep `/e economy pulse`, wallet/treasury inspection,
transaction query output, and future Governor dashboards readable for server
owners who are not reading raw logs.
