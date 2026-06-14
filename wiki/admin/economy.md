# Economy

Admin guide for Sigils, bank balances, treasuries, transactions, service prices, and Economy Pulse.

[Home](../README.md) | [Admin](README.md) | [Commands](commands.md)

## Status

`Implemented`, `Admin-only`

Economy owns the physical currency item, banked player balances, Realm treasuries, transactions, service prices, and Economy Pulse.

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

## Service Prices

Portal ticket and Ancient Gate prices are Economy-owned service prices, not hard-coded Portal values.

```text
portal_ticket.nether
portal_ticket.end
ancient_gate.passage
```

## Verification

- Use `/e economy pulse` after server startup.
- Deposit and withdraw Sigils through the Banker NPC.
- Confirm Portal tickets charge the configured service price.
- Confirm Ancient Gate passage uses physical Sigils first, then banked Sigils.
- Inspect recent transactions after purchases or refunds.

## Source-Backed Notes

- Addon docs: [../../docs/addons/economy.md](../../docs/addons/economy.md)
- Commands: [../../addons/economy/src/main/java/panetina/elarion/addons/economy/command/EconomyCommands.java](../../addons/economy/src/main/java/panetina/elarion/addons/economy/command/EconomyCommands.java)
- API: [../../addons/economy/src/main/java/panetina/elarion/addons/economy/api/ElarionEconomyApi.java](../../addons/economy/src/main/java/panetina/elarion/addons/economy/api/ElarionEconomyApi.java)
