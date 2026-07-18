# NPCs

Admin guide for placing, repairing, and configuring Elarion NPCs.

[Home](../README.md) | [Admin](README.md) | [Commands](commands.md)

## Status

`Implemented`, `Admin-only`, `Manual verification needed`

NPCs are static server-authoritative entities with durable placed IDs, skins, portraits, dialogue trees, conditional text variants, and registered action buttons.

Definitions:

```text
config/elarion/addons/npcs/npcs.yml
config/elarion/addons/npcs/skins.yml
config/elarion/addons/npcs/portraits.yml
config/elarion/addons/npcs/trades.yml
config/elarion/addons/npcs/dialogues/
```

Dialogue files may be grouped in subfolders, for example:

```text
config/elarion/addons/npcs/dialogues/generic_foundation/guide.yml
```

If a nested file has no explicit `id`, its dialogue ID is the slash path under
`dialogues/`, such as `generic_foundation/guide`.

Runtime state:

```text
world/elarion/addon-state/npcs/placed-npcs.json
world/elarion/addon-state/npcs/trade-purchases.json
```

Each definition can declare:

```yaml
faction: worldheart
tax-jurisdiction: auto
# or realm:oak
# or world:elarion:worldheart
```

`faction` controls reputation aggregation. Use `realm:<id>` for a Realm,
`worldheart`, `underworld`, or a stable custom faction id. It does not control
tax routing. Each NPC's personal relationship appears in its dialogue UI;
faction totals appear in the Character Menu Reputation tab.

Every placed NPC stores the resolved Realm/world jurisdiction. `auto` uses the
Realm owning the placement world, otherwise the world itself. Explicit values
must match the placement world. `/e npc inspect` shows the resolved value.
Schema-v1 placement files are backed up before migration to schema v2.

The Admin Panel Config tab exposes the loaded `npcs` config domain as a
read-only summary. It shows definition, visual profile, trade catalog, dialogue
graph, and UI metadata; configuration is still edited in YAML and applied with
`/e npc reload`.

## Trade NPCs

You can add a new trade NPC without Java code by assigning a `trade-catalog` in
`npcs.yml`, adding a dialogue node with `presentation: trade`, and defining the
catalog in `trades.yml`.

Custom Sell/buyback rows that feed placed-NPC stock must set both:

```yaml
stock-destination: placed_npc
destination-offer: <buy_offer_id>
```

The shipped legacy `worldheart_trader.cobblestone_buyback` route is bridged in
memory to `destination-offer: cobblestone` if that field is missing. Custom
traders are not guessed.

## Placement Flow

```text
/e npc reload
/e npc place worldheart_banker here
/e npc list near
/e npc inspect <npcId>
/e npc open <npcId>
```

Useful placement edits:

```text
/e npc face <npcId>
/e npc rotate <npcId> north|east|south|west|here
/e npc rotate <npcId> yaw <value>
/e npc move <npcId>
/e npc duplicate <npcId> here
/e npc remove <npcId>
/e npc repair <npcId|all>
```

## Presentation

```text
/e npc set skin <npcId> <skinProfile>
/e npc set portrait <npcId> <portraitProfile>
/e npc set dialogue <npcId> <dialogueId>
/e npc set name <npcId> <displayName>
```

Skins affect the visible body. Portraits affect dialogue UI. If no portrait is configured, the UI can fall back to the NPC skin head or placeholder.

## Verification

`/e npc inspect <id>` reports the placement's resolved Realm/world tax
jurisdiction. Trader quantity changes are server-quoted. Unlimited BUY Confirm
uses carried physical Sigils only and writes the NPC purchase journal before
delivery. Finite stock and Sell/buyback are server-authoritative and use NPC
purchase/sale journals, placed-NPC stock state, and Economy settlement.

- Place an NPC and inspect its ID.
- Set skin, portrait, and dialogue.
- Right-click the NPC and verify the dialogue screen opens.
- Use `/e npc open <npcId>` as a server-authoritative admin/QA alternative to
  right-click when you are already near the placed NPC.
- Test conditional node variants by changing the owning system state, such as
  quest variables, and reopening the dialogue.
- Test banker deposit/withdraw actions if using the banker dialogue.
- Remove the NPC and confirm its entity disappears.
- Run `/e npc repair all` after suspicious duplicate or missing entities.

## Source-Backed Notes

- Addon docs: [../../docs/addons/npcs.md](../../docs/addons/npcs.md)
- Commands: [../../addons/npcs/src/main/java/panetina/elarion/addons/npcs/command/](../../addons/npcs/src/main/java/panetina/elarion/addons/npcs/command/)
- Client UI: [../../addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/](../../addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/)
