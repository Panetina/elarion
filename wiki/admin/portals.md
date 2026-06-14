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
- `fee_passage`: Ancient Gates. First outbound and return trip can be free; later crossings charge the Economy-owned passage price.
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

Payment consumes physical Sigils first, then bank balance.

## Tickets

Portal tickets are physical route-bound items in the Economy creative tab. The Portal Surveyor is command-only through `/e portal wand`.

Ticket prices are owned by Economy service prices:

```text
portal_ticket.nether
portal_ticket.end
```

## Troubleshooting

- Use `/e portal guide <route>` for the current ordered setup workflow.
- Use `/e portal inspect <route>` to see linkage, arrivals, active state, schedule, and runtime location.
- Use `/e portal repair <route>` after changing endpoints or if field blocks look stale.
- If a gate does not open, check for obstructed interior cells and route unlock state.
- Scheduled routes do not broadcast open/close countdowns in chat; route status is available through API snapshots for future HUD/map consumers.

## Source-Backed Notes

- Addon docs: [../../docs/addons/portals.md](../../docs/addons/portals.md)
- System docs: [../../docs/systems/Portals.md](../../docs/systems/Portals.md)
- Commands: [../../addons/portals/src/main/java/panetina/elarion/addons/portals/command/PortalCommands.java](../../addons/portals/src/main/java/panetina/elarion/addons/portals/command/PortalCommands.java)
- Route service: [../../addons/portals/src/main/java/panetina/elarion/addons/portals/service/PortalRouteService.java](../../addons/portals/src/main/java/panetina/elarion/addons/portals/service/PortalRouteService.java)
