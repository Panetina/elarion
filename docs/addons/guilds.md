# Elarion Guilds Addon

Technical contract for reusable public player guilds.

Last reviewed: 2026-07-26.

## Status

Implemented V1 foundation plus the first management surface. Guilds is separate from Government and remains
reusable for guilds, cities, parties, cults, criminal organizations,
revolutionary movements, and secret societies later. Those specializations are
not implemented in V1.

## Ownership

Guilds owns:

- stable internal guild IDs
- public display tags
- optional hidden public tag preference
- full display names
- one public guild membership per player
- guild leaders
- invites
- guild chat
- guild runtime storage
- secret-guild flag, bounded announcements, custom roles, role assignments,
  and a revisioned 32x32 fixed-palette Guild icon

Guilds does not own:

- Realm membership
- Government offices
- Economy balances
- Ledger reputation
- war/crime systems
- secret society mechanics

Economy owns guild creation payment. Core owns citizen and Realm truth.

## Config

```text
config/elarion/addons/guilds/guilds.yml
```

Config controls creation fee, internal guild ID pattern, display-name length, tag length,
tag regex, blocked public tags, and invitation lifetime.

## Runtime State

```text
world/elarion/addon-state/guilds/guilds.json
```

Runtime state stores guilds, membership indexes, and invites.

## Commands

Player commands:

```text
/guild
/guild invite <player>
/guild accept <guild>
/guild kick <player>
/guild leave
/guild transfer <player>
/guild info [guild]
/gc <message>
```

`/guild` opens the current member's management surface. Guild creation is only
available at the configured Registrar action. Its bounded server projection
contains Overview, Members, News, Invites, Roles, and Emblem tabs.
Client actions are requests: the server derives the player's Guild and checks
the same service permission before leaving, inviting, assigning/creating a
role, publishing an announcement, or redrawing the icon.

NPC dialogue may use the registry action `elarion_guilds:open_registrar`.

The Registrar receives the current creation fee, wallet balance, currency name,
and field bounds from the server. Players choose only the display name, public
tag, and Secret setting; Guilds generates the stable internal ID. The action
opens Guild management for an existing member and the Guild-owned creation form
otherwise; the NPC never owns Guild state, fees, IDs, or permissions.

Admin commands:

```text
/e guilds reload
/e guilds list
/e guilds inspect <guild>
/e guilds delete <guild>
/e guilds transfer <guild> <player>
```

## Public Presentation

Only the normalized tag renders publicly when the guild is configured to show
it:

```text
[MERC] PlayerName
```

Full guild names, announcements, membership, and roles are shown in the Guild
management surface. The selected chat channel remains selected between
messages; Guild is sent through the addon-owned `/gc` route after the Core
chat composer validates it.

## Notifications

Guild invitations are actionable Personal notifications with Accept and
Decline actions. Acceptance, removal, leadership transfer, and guild deletion
publish dismissible Personal notices to affected players. Guilds owns invite
validation and expiry; Core owns notification storage and presentation.

## Website Authority Projection

Guilds publishes an authenticated `authority.guild.lore` state projection only
for non-secret Guilds through Core's restart-safe website outbox. A secret
Guild clears both authority and membership web projections because the bridge
does not provide a member-targeted visibility primitive.

## Source

```text
addons/guilds/src/main/java/panetina/elarion/addons/guilds/
```

Primary services:

- `GuildService`
- `GuildStorage`
- `GuildCommands`
- `ElarionGuildsApi`
- `GuildConfigDescriptors`
- `GuildWebProjectionPublisher`

## Current UI Contract

The Registrar and six-tab management surface are implemented with shared Core
theme, scaling, text-input, button, and fixed-palette canvas primitives. Invite
candidates and viewer permissions are bounded server projections. Leaving a
Guild explicitly closes the now-stale management screen. Guild emblems are
intentionally not rendered in chat.

## Bridge Heraldry Fields

Guild authority and membership projections include `iconRevision` and
`iconPaletteBase64`. The palette value is the exact fixed 32×32 (1,024-byte)
palette-index buffer encoded as Base64; bridge consumers cache by revision and
must treat the Guild addon as canonical. Realm identity projections use the
equivalent `heraldryRevision` and `heraldryPaletteBase64` fields.
