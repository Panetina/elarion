# Economy

Admin guide for Sigils, bank balances, treasuries, transactions, service prices, and Economy Pulse.

[Home](../README.md) | [Admin](README.md) | [Commands](commands.md)

## Status

`Implemented`, `Admin-only`

Economy owns the physical currency item, banked player balances, Realm treasuries, transactions, service prices, and Economy Pulse.

Retry-safe addon payments use bounded operation receipts. Their retention is
controlled by `operations.receipt-retention-days` and
`operations.max-receipts` in `economy.yml`.

Planned tax administration uses Realm treasuries for Realm activity and an
owner-administered Worldheart treasury for Worldheart, marketplace, Nether,
End, and other non-Realm world services. Shop purchases use physical Sigils.

Definitions:

```text
config/elarion/addons/economy/
```

Runtime state:

```text
world/elarion/addon-state/economy/
```

## Common Commands

```text
/e economy wallet get <player>
/e economy wallet give <player> <amount>
/e economy wallet take <player> <amount>
/e economy wallet deposit <player> <amount>
/e economy wallet withdraw <player> <amount>
/e economy treasury get <realm>
/e economy treasury give <realm> <amount>
/e economy treasury take <realm> <amount>
/e economy transfer player <from> <to> <amount>
/e economy transactions player <player> [limit]
/e economy transactions realm <realm> [limit]
/e economy pulse
/e economy recalculate
/e economy reload
```

Reload validates both Economy settings and service prices before applying
either file. If one file is invalid, all previous runtime values remain active.

## Service Prices

Portal ticket and Ancient Gate prices are Economy-owned service prices, not hard-coded Portal values.

```text
portal_ticket.nether
portal_ticket.end
ancient_gate.passage
```

## Verification

Realm and Worldheart category tax overrides are persisted server policy, but
there is no player-facing editor yet. The future Seat of Rule Taxes tab edits
its Realm; owner Admin tooling edits Worldheart policy. Trader quotes display
the resolved tax but cannot purchase yet.

Worldheart revenue is stored in a stable Worldheart treasury, not an admin
wallet, not the Hollow Emperor as a fake player, and not a future Emperor's
personal balance. Core separately stores who may govern Worldheart. Future
Worldheart control blocks can allow either OP4/server administrators or the
current Worldheart ruler without granting that ruler full server operator
permissions.

NPC shop BUY purchases now charge carried physical Sigils only. Successful
purchases settle public revenue into the resolved Realm or Worldheart treasury
through an idempotent Economy receipt. Bank balances are not used for NPC shop
spending; players must withdraw first.

- Use `/e economy pulse` after server startup.
- Deposit and withdraw Sigils through the Banker NPC.
- Confirm Portal tickets charge the configured service price.
- Confirm Ancient Gate passage uses physical Sigils only.
- Inspect recent transactions after purchases or refunds.

## Source-Backed Notes

- Addon docs: [../../docs/addons/economy.md](../../docs/addons/economy.md)
- Commands: [../../addons/economy/src/main/java/panetina/elarion/addons/economy/command/EconomyCommands.java](../../addons/economy/src/main/java/panetina/elarion/addons/economy/command/EconomyCommands.java)
- API: [../../addons/economy/src/main/java/panetina/elarion/addons/economy/api/ElarionEconomyApi.java](../../addons/economy/src/main/java/panetina/elarion/addons/economy/api/ElarionEconomyApi.java)
