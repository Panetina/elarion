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
/group create <id> <tag> <display-name...>
/group invite <player>
/group accept <group>
/group kick <player>
/group leave
/group transfer <player>
/group info [group]
/gc <message>
/lc <message>
```

## Core Admin Commands

```text
/spy chat
/list
/seed
/random value <range>
/random roll <range>
/random reset ...
/e reload
/e realm ...
/e realm announce <realm> <message...>
/e realm mail <realm> "<title>" <message...>
/e citizen ...
/e title ...
/e ability ...
/e reward ...
/e progression ...
/e history ...
/e history chronicle list [weeks]
/e history chronicle inspect <week> [limit]
```

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
```

## Portals

```text
/e portal reload
/e portal wand
/e portal list
/e portal inspect <route>
/e portal guide <route>
/e portal setup enter <route> [x y z]
/e portal setup return
/e portal endpoint set <route> a_gate|a_arrival|b_gate|b_arrival
/e portal unlock|lock|remove <route>
/e portal repair <route>|all
/e portal window open <route> <duration>
/e portal window close <route>
/e portal entitlement inspect|grant|clear <player> <route>
```

## Government

```text
/e government reload
/e government forms
/e government inspect <form>
/e government state <realm>
/e government gates <realm>
/e government set-form <realm> <form>
/e government identity set <realm> <tag> <display-name...>
/e government founding complete <realm>
/e government authority cleanup
/e government test advance <realm>
/e government block remove
/e government office assign <realm> <office> <player>
/e government office remove <realm> <office> <player>
```

## Groups

```text
/e groups reload
/e groups list
/e groups inspect <group>
/e groups delete <group>
/e groups transfer <group> <player>
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
