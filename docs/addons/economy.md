# Economy Addon Contract

Last reviewed: 2026-06-10

Author: Panyel  
Team: Panetina Team

## Ownership

Economy owns:

- `elarion:sigil`
- `elarion:economy` Creative tab
- player wallets
- Realm treasuries
- transaction records
- physical sigil deposit/withdraw conversion
- Economy Governor monitor state
- future adaptive price state

Core remains authoritative for citizens, Realms, permissions, and durable
history. Economy validates Realm IDs through Core and emits successful
transaction outcomes into Core history.

All future Economy items must be added to the Economy-owned Creative tab in
the same change that registers the item.

## Currency Lore

The Worldheart Treasury mints the official currency known as **Sigils**. Each
Sigil bears the **Elarion Seal**, the ancient heraldic mark recognized by every
Realm and representing their shared civilization.

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

Future Bank, Market, Portal, Quest, Contribution, Government, NPC, and Contract
systems consume `ElarionEconomyApi`. They must use distinctive `sourceSystem`
IDs so Governor diagnostics can attribute faucets and sinks without parsing
reason text.

OP transaction queries are bounded by month and result count. Before rich
player-facing transaction GUIs are added, create dedicated account indexes or
summaries rather than widening JSONL scans.
