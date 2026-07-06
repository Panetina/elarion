# Portals

Admin guide for linked Elarion gates, tickets, Ancient Gates, and neutral gates.

[Home](../README.md) | [Admin](README.md) | [Commands](commands.md)

## Status

`Implemented`, `Admin-only`

Manual verification passed for linked A/B setup, Iris rendering, ticket travel, return entitlements, Ancient Gates, fee passage, schedule closure, and restart recovery.

Portals are hand-built gate interiors selected with the Portal Surveyor. Runtime locations are stored in world state, not in `routes.yml`.

Runtime state:

```text
world/elarion/addon-state/portals/state.json
```

Definitions:

```text
config/elarion/addons/portals/routes.yml
config/elarion/addons/portals/ui.yml
```

Ancient Gate descriptions can use `%realm_display%`, `%realm_official%`, and
`%realm_tag%`. Use `%realm_official%` when the text should follow the current
Government form, such as `Kingdom of Oak`.

## A/B Gate Model

```text
a_gate     = portal frame A
a_arrival  = where players appear at A
b_gate     = portal frame B
b_arrival  = where players appear at B
```

Travel logic:

```text
Enter a_gate -> arrive at b_arrival
Enter b_gate -> arrive at a_arrival
```

## Setup Flow

```text
/e portal wand

# Select first portal frame interior with the wand
/e portal endpoint set nether a_gate

# Move to side B, or use setup travel if the destination is protected
/e portal setup enter nether 0 80 0

# Stand where players should appear on side B
/e portal endpoint set nether b_arrival

# Select second portal frame interior with the wand
/e portal endpoint set nether b_gate

# Return to the original setup position
/e portal setup return

# Stand where players should appear on side A
/e portal endpoint set nether a_arrival

/e portal inspect nether
/e portal repair nether
/e portal unlock nether
```

The selected cuboid must be one block thick on exactly one axis and at least two blocks wide and tall. Invalid selections show operator feedback instead of crashing packet handling.

## Route Types

- `scheduled_ticketed`: Nether and End-style gates. Outbound travel consumes one physical route-bound ticket and creates one return entitlement.
- `fee_passage`: Shrine-unlocked Ancient Gates. First outbound and return trip can be free.
  Later Realm-to-Worldheart outbound trips charge the Economy-owned passage
  price and store one free return passage. Returning through the linked
  Worldheart gate consumes that stored return and does not charge again.
- `always_open`: neutral gates. No lock, schedule, ticket, or return entitlement. Destination may be any configured world.

## Ancient Gates

Stable development route IDs:

```text
realm1
realm2
realm3
```

Ancient Gates are not tied to temporary Realm display names. Later crossings use the Economy service price key:

```text
ancient_gate.passage
```

Payment consumes physical Sigils first, then bank balance. Return travel does
not charge currency if the player has a stored return passage from entering the
Realm-side gate. The return confirmation reports that the return is already
paid; it does not repeat the first-round-trip message after later paid journeys.

Completing the configured Shrine milestone sets the Realm flag
`ancient_gate_unlocked` and unlocks the matching Realm route. The unlock appears
in that Realm's notification feed. This flag also makes the World notification
icon available to the Realm's citizens.

Resetting the Realm's Shrine progression reverses that project-owned unlock:
the `ancient_gate_unlocked` flag is cleared, the matching route is locked, and
the old gate-unlocked notification is resolved. Rebuilding the Shrine unlocks
the route again and creates a fresh notification.

## Tickets

Portal tickets are physical route-bound items in the Economy creative tab. The Portal Surveyor is command-only through `/e portal wand`.

Ticket prices are owned by Economy service prices:

```text
portal_ticket.nether
portal_ticket.end
```

## Route Status Icons

Unlocked scheduled routes render compact HUD icons in the reserved accessory
space below the notification category rail. They are not notification
categories and do not create notification cards when the route opens or closes.

- locked routes are invisible
- closed routes are greyed out
- open routes are colored
- hovering shows the local opening or closing countdown
- the colored bottom bar shrinks as an open window approaches closure
- Portal icons resize with the notification rail on small windows
- route icons and visual caches clear when disconnecting from a server

Configure the icon independently from the prompt/ticket icon:

```yaml
visual:
  status-icon-item: "minecraft:netherrack"
```

Nether and End unlocks publish World notifications only to citizens whose Realm
has unlocked global access. Open/close transitions do not create notification
cards.

## Troubleshooting

- Use `/e portal guide <route>` for the current ordered setup workflow.
- Use `/e portal inspect <route>` to see linkage, arrivals, active state, schedule, and runtime location.
- Use `/e portal repair <route>` after changing endpoints or if field blocks look stale.
- If a gate does not open, check for obstructed interior cells and route unlock state.
- Scheduled routes do not broadcast open/close countdowns in chat; the HUD and
  future Atlas use synchronized route snapshots.

## Source-Backed Notes

- Addon docs: [../../docs/addons/portals.md](../../docs/addons/portals.md)
- System docs: [../../docs/systems/Portals.md](../../docs/systems/Portals.md)
- Commands: [../../addons/portals/src/main/java/panetina/elarion/addons/portals/command/PortalCommands.java](../../addons/portals/src/main/java/panetina/elarion/addons/portals/command/PortalCommands.java)
- Route service: [../../addons/portals/src/main/java/panetina/elarion/addons/portals/service/PortalRouteService.java](../../addons/portals/src/main/java/panetina/elarion/addons/portals/service/PortalRouteService.java)
