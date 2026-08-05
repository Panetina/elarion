# Admin Commands

Implemented command surface for server owners and OP level 4 operators.

[Home](../README.md) | [Admin](README.md) | [Setup Checklist](setup-checklist.md)

## Player Commands

```text
/help [command]
/rc <message>
/ac <message>
/pm <player> <message>
/r <message>
/w <message>
/yell <message>
/guild
/gc <message>
/lc <message>
```

The Character Menu opens from the default `C` keybind. `/charactermenu` remains
a hidden client command for manual use, but it is not
advertised in slash recommendations or `/help`.

Guild creation is available through the configured Guild Registrar NPC action.
The Guild screen owns its typed invitation, role, announcement, and emblem
requests; `/gc` remains the Guild-chat keyboard fallback. Vanilla `/say` is
unavailable so it cannot bypass Elarion chat restrictions.

## Core Admin Commands

```text
/spy chat
/list
/seed
/random value <range>
/random roll <range>
/random reset ...
/e reload
/e panel
/e reset players
/e realm ...
/e realm announce <realm> <message...>
/e realm mail <realm> "<title>" <message...>
/e Ember ...
/e title ...
/e ability ...
/e reward ...
/e progression ...
/e history ...
/e history chronicle list [weeks]
/e history chronicle inspect <week> [limit]
```

`/e panel` opens the in-game Admin Panel for OP level 4 players. It guilds
player inspection/move/edit tools, system resets, Realm-scoped actions, and a
click-confirmed runtime-only Danger Zone reset.

`/e reset players` is the separate complete player-data reset. It previews
affected counts and requires an executor-bound confirmation within 60 seconds.
The confirmed reset creates a timestamped backup, disconnects everyone, clears
all vanilla playerdata/stats/advancements, operators, whitelist entries, and
cached profiles, then asks every registered Elarion domain to remove its own
player-owned state. Shared worlds, terrain, buildings, NPCs, Shrines, portals,
Realm/world definitions, configuration, and map infrastructure remain intact.
Use the console or a fresh signed website/bridge access operation afterward to
restore whitelist access; restore OPs from console. Historical bridge commands
are not replayed by the reset.

`/e reset world <world>` is separate and OP-only. It offers only server-known
managed world IDs, requires executor-bound confirmation, backs up the Fantasy
persistent dimension plus world-scoped addon state with `manifest.json`, and
waits for the old runtime world to be deleted before opening the replacement.
Definitions and configuration remain intact.

## Economy

```text
/e economy wallet get <player>
/e economy wallet give|take <player> <amount>
/e economy wallet deposit|withdraw <player> <amount>
/e economy treasury get <realm>
/e economy treasury give|take <realm> <amount>
/e economy transfer player <from> <to> <amount>
/e economy transactions player <player> [limit]
/e economy transactions realm <realm> [limit]
/e economy pulse
/e economy recalculate
/e economy reload
```

## Offerings

```text
/e offerings reload
/e offerings projects
/e offerings inspect <project>
/e offerings instances
/e offerings state <instance>
/e offerings start realm <realm> <project>
/e offerings start global <project>
/e offerings start location <project>
/e offerings shrine link <instance>
/e offerings shrine unlink
/e offerings shrine inspect
/e offerings shrine remove
/e offerings shrine repair
/e offerings delete <instance>
/e offerings reset <instance>
/e offerings complete <instance>
/e test shrine reset [realm]
/e test realm global <realm> on|off
```

## Quests

```text
/e quest reload
/e quest list
/e quest inspect <quest>
/e quest state <quest> [scope-key]
/e quest reset <quest> <scope-key>
/e quest bind actor <quest> <scope-key> <actor> <npcIdOrHandle>
/e quest unbind actor <quest> <scope-key> <actor>
/e quest bindings <quest> <scope-key>
/e quest validate <quest|all>
```

Quest scope keys are explicit strings such as `realm:realm1`, `world:overworld`,
`player:<uuid>`, or `global`.

## Portals

```text
/e portal reload
/e portal wand
/e portal list
/e portal inspect <route>
/e portal guide <route>
/e portal setup enter <route> [x y z]
/e portal setup return
/e portal preview <neutral|nether|end|fee|blocked|return>
/e portal endpoint set <route> a_gate|a_arrival|b_gate|b_arrival
/e portal unlock|lock|remove <route>
/e portal repair <route>|all
/e portal window open <route> <duration>
/e portal window close <route>
/e portal entitlement inspect|grant|clear <player> <route>
```

## Underworld

```text
/banish <player> <minutes> <reason...>
/banish <player> permanent <reason...>
/banish list
/unbanish <player>
/e death reload
/e death inspect <player>
/e death corpse list
/e death corpse inspect <corpseId>
/e death corpse recover <corpseId> <player>
/e death vault recover <player>
/e death underworld send <player> [minutes]
/e death underworld return <player>
/e death soul inspect <player>
/e death soul add-fracture <player>
/e death soul remove-fracture <player>
/e death soul clear-fractures <player>
/e test death send <player> <minutes>
/e test death return <player>
/e test death fracture <player>
/e test death fracture add <player>
/e test death fracture remove <player>
/e test death clear <player>
/e test death reset-state
/e character inspect <player>
/e character recreate-now <player>
/e character archive <player>
/e test character finish-cooldown <player>
/e test character trigger-true-death <player>
/e test character reset <player>
/e test character force-active <player>
```

`/banish` confines an online player to the Underworld for the supplied number
of minutes or permanently. A reason is mandatory and is shown to the player in
chat and on the Underworld HUD. Banished players may walk and look around but
cannot chat, fight, break/use blocks, use items, interact with NPCs or Shrines,
travel through portals, or use addon teleport actions. `/unbanish` also works
for an offline player already present in the persisted banishment list.

## Mounts

```text
/e mounts grant <player> <type>
/e mounts revoke <player> <type>
/e mounts set-active <player> <type>
/e mounts list <player>
/e test mounts summon <type>
/e test mounts debug
/e test mounts clear-nearby
```

## Government

```text
/e government reload
/e government forms
/e government inspect <form>
/e government state <realm>
/e government gates <realm>
/e government audience <realm>
/e government audience inspect <realm> <record>
/e government laws <realm>
/e government law archive <realm> <law>
/e government law restore <realm> <law>
/e government set-form <realm> <form>
/e government identity set <realm> <tag> <display-name...>
/e government founding complete <realm>
/e government authority cleanup
/e government reset <realm>
/e test government reset [realm]
/e test government advance <realm>
/e government block remove
/e government office assign <realm> <office> <player>
/e government office remove <realm> <office> <player>
```

## Guilds

```text
/e guilds reload
/e guilds list
/e guilds inspect <guild>
/e guilds delete <guild>
/e guilds transfer <guild> <player>
```

## NPCs

```text
/e npc reload
/e npc place <npcDefinition> [north|east|south|west|here]
/e npc place <npcDefinition> yaw <value>
/e npc remove <npcId>
/e npc remove nearest
/e npc face <npcId>
/e npc rotate <npcId> <north|east|south|west|here>
/e npc rotate <npcId> yaw <value>
/e npc repair <npcId|all>
/e npc tp <npcId>
/e npc duplicate <npcId> [north|east|south|west|here]
/e npc duplicate <npcId> yaw <value>
/e npc nearest
/e npc list [world|near]
/e npc list tag <tag>
/e npc inspect <npcId>
/e npc inspect nearest
/e npc move <npcId>
/e npc set name|skin|portrait|dialogue <npcId> ...
/e npc dialogue inspect <dialogueId>
```

## Worlds, Performance, Security

```text
/e world ...
/e perf status
/e perf queues
/e perf config
/e perf worlds
/e perf realms
/e perf realm <realm>
/e perf hotzones
/e perf security
/e security status
```

## Source-Backed Notes

- Technical command contract: [../../docs/commands.md](../../docs/commands.md)
- Core commands: [../../platform/core/src/main/java/panetina/elarion/core/command/](../../platform/core/src/main/java/panetina/elarion/core/command/)
- Addon commands: [../../addons/](../../addons/)

When this page changes, update [../../docs/commands.md](../../docs/commands.md)
and any affected system guide in the same pass.
