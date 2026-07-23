# Groups

Admin guide for reusable public player groups.

[Home](../README.md) | [Admin](README.md) | [Commands](commands.md)

## Status

`Implemented foundation`

Groups are public player-created organizations. V1 supports one group per
player, one leader, invites, membership management, group chat, and admin
inspection. Secret societies, cities, guild types, and political-party style
metadata are future work.

## Config And Runtime

Config:

```text
config/elarion/addons/groups/groups.yml
```

Runtime:

```text
world/elarion/addon-state/groups/groups.json
```

Config controls the creation fee, group ID pattern, public tag pattern, tag
length, blocked tags, and maximum display-name length.

## Player Commands

```text
/group create <id> <tag> <display-name...>
/group invite <player>
/group accept <group>
/group kick <player>
/group leave
/group transfer <player>
/group info [group]
/gc <message>
```

Group creation uses deposited Economy balance and sinks the configured fee.
The default fee is 25 Sigils.

## Admin Commands

```text
/e groups reload
/e groups list
/e groups inspect <group>
/e groups delete <group>
/e groups transfer <group> <player>
```

## Public Rendering

Only the public tag renders next to a player identity:

```text
[MERC] PlayerName
Ember
```

Full display names appear in `/group info` and future group UI screens.

## Government Integration

Government no longer uses Groups for active founding offices. Groups remains a
reusable social/faction system for future mechanics.

## Source-Backed Notes

- Commands: [../../addons/groups/src/main/java/panetina/elarion/addons/groups/command/GroupCommands.java](../../addons/groups/src/main/java/panetina/elarion/addons/groups/command/GroupCommands.java)
- Service: [../../addons/groups/src/main/java/panetina/elarion/addons/groups/service/GroupService.java](../../addons/groups/src/main/java/panetina/elarion/addons/groups/service/GroupService.java)
- Storage: [../../addons/groups/src/main/java/panetina/elarion/addons/groups/storage/GroupStorage.java](../../addons/groups/src/main/java/panetina/elarion/addons/groups/storage/GroupStorage.java)
