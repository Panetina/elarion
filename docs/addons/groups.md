# Elarion Groups Addon

Technical contract for reusable public player groups.

## Status

Implemented V1 foundation. Secret groups, group types, group screens, city/guild
specialization, and rich delegate election screens remain future work.

## Ownership

Groups owns:

- stable internal group IDs
- public display tags
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
tag regex, blocked public tags, and invitation lifetime. Invitations default to
seven days.

Groups registers a read-only config descriptor domain named `groups` through
`ElarionApi.system().configs()`. The descriptors are backed by the current
validated `GroupService.config()` snapshot and do not parse `groups.yml` during
ordinary discovery. The Core Admin Panel Systems tab can display this domain
as a read-only config row. Config editing remains a future Core Config/Admin
slice.

## Runtime State

```text
world/elarion/addon-state/groups/groups.json
```

Runtime state stores groups, UUID membership indexes, invites, and groups locked
by Confederation delegate rules.

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

Only the normalized tag is rendered publicly:

```text
[MERC] PlayerName
```

Full group names are reserved for `/group info`, future Civic Forum screens,
Government UI, elections, and delegate nomination screens.

## Confederation Delegate Contract

Government consumes `ElarionGroupsApi`; it must not read Groups runtime files
directly.

Confederation delegate candidacy requires:

- candidate is the group leader
- every group member belongs to the candidate Realm
- the group is not cross-Realm at nomination time

When a group leader wins a Confederation delegate seat, Government locks that
group through Groups. Locked delegate groups cannot invite cross-Realm members.
When the delegate office is removed or vacated, Government releases the lock.
`/e groups inspect <group>` reports whether the group is currently
Confederation locked.

## Notifications

Group invitations are actionable Personal notifications with Accept and
Decline actions. Acceptance, removal, leadership transfer, and group deletion
publish dismissible Personal notices to affected players. Groups owns invite
validation and expiry; Core owns notification storage and presentation.

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
