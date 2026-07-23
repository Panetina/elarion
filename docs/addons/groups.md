# Elarion Groups Addon

Technical contract for reusable public player groups.

Last reviewed: 2026-07-18.

## Status

Implemented V1 foundation. Groups is separate from Government and remains
reusable for guilds, cities, parties, cults, criminal organizations,
revolutionary movements, and secret societies later. Those specializations are
not implemented in V1.

## Ownership

Groups owns:

- stable internal group IDs
- public display tags
- optional hidden public tag preference
- full display names
- one public group membership per player
- group leaders
- invites
- group chat
- group runtime storage

Groups does not own:

- Realm membership
- Government offices
- Economy balances
- Ledger reputation
- war/crime systems
- secret society mechanics

Economy owns group creation payment. Core owns citizen and Realm truth.

## Config

```text
config/elarion/addons/groups/groups.yml
```

Config controls creation fee, group ID pattern, display-name length, tag length,
tag regex, blocked public tags, and invitation lifetime.

## Runtime State

```text
world/elarion/addon-state/groups/groups.json
```

Runtime state stores groups, membership indexes, and invites.

## Commands

Player commands:

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

Admin commands:

```text
/e groups reload
/e groups list
/e groups inspect <group>
/e groups delete <group>
/e groups transfer <group> <player>
```

## Public Presentation

Only the normalized tag renders publicly when the group is configured to show
it:

```text
[MERC] PlayerName
```

Full group names are shown in `/group info` and future group UI screens.

## Notifications

Group invitations are actionable Personal notifications with Accept and
Decline actions. Acceptance, removal, leadership transfer, and group deletion
publish dismissible Personal notices to affected players. Groups owns invite
validation and expiry; Core owns notification storage and presentation.

## Website Authority Projection

Groups publishes one authenticated `authority.group.lore` state projection per
group through Core's restart-safe website outbox. The payload identifies the
current leader UUID as `ownerUuid` with `authorityRole=leader`.

## Source

```text
addons/groups/src/main/java/panetina/elarion/addons/groups/
```

Primary services:

- `GroupService`
- `GroupStorage`
- `GroupCommands`
- `ElarionGroupsApi`
- `GroupConfigDescriptors`
- `GroupWebProjectionPublisher`
