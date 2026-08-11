# Elarion Guilds Addon

Technical contract for reusable public player guilds.

Last reviewed: 2026-08-11.

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
- secret-guild flag, bounded announcements, hierarchy-safe custom roles and
  role assignments, member join timestamps, and a revisioned 16x16
  fixed-palette Guild icon

Guilds does not own:

- Realm membership
- Government offices
- Economy balances
- Ledger reputation
- war/crime systems
- secret society mechanics

Economy owns physical-inventory Sigil fee consumption. Core owns citizen and Realm truth.

The unique internal `owner` role is presented to players as `Leader`; it is
assigned only to the current Guild leader and is demoted on ownership transfer.
Only that leader may transfer ownership (25 carried Sigils) or change Secret
Guild status (50 carried Sigils); `/e guild transfer` remains the separate
operator recovery action.
Ranks have an explicit positive position (`1` is highest authority). Every
Guild includes the ordered `Leader`, `Officer`, `Recruiter`, `Member`,
`Veteran`, `Initiate`, and `Newcomer` defaults. A player may only change a target and assign a rank strictly below
their own position; `Leader` is never assignable. Existing saves are migrated
in memory: missing join times use the Guild founding time and missing standard
ranks are backfilled without deleting custom roles.

## Config

```text
config/elarion/addons/guilds/guilds.yml
```

Config controls creation fee, internal guild ID pattern, display-name length, tag length,
tag regex, blocked public tags, invitation lifetime, and the ordered
`progression.levels` tiers. Each tier declares a cumulative physical-Sigil
contribution threshold and member capacity; level one must begin at zero.

## Runtime State

```text
world/elarion/addon-state/guilds/guilds.json
```

Runtime state stores guilds, membership indexes, invites, role assignments,
and member join timestamps. The join timestamp is server-owned and remains
stable through future rank changes. Guilds also stores only its bounded
per-member contribution aggregate; Economy owns the actual physical-Sigil
payment and transaction ledger. A contribution is paid from carried Sigils,
never from a bank balance. Member admission is capped by the active configured
progression tier.

## Commands

Player commands:

```text
G
```

`G` requests the server-authoritative Guild surface; non-members receive a
read-only empty state. Guild creation is only available at the configured
Registrar action. Its bounded server projection
contains Overview, Members, News, Roles, and Emblem tabs. Invitations are
issued through the Shift + right-click player action, not a duplicate screen
tab. The context menu remains open when Shift is released and closes only on
Escape, an action selection, or a click outside it.
Client actions are requests: the server derives the player's Guild and checks
the same service permission before leaving, inviting, assigning/creating a
role, publishing an announcement, or redrawing the icon.
Overview shows the current level, contribution total, next threshold, and
member capacity. Every member can submit a positive carried-Sigil donation;
the server charges it through Economy before updating Guild progression.
Donation requests carry a UUID operation ID. Guilds keeps the most recent 128
Guild receipt projections for bounded replay protection, while Economy retains
the canonical idempotent payment receipt for the same operation key.
Members show their Core-owned Realm display name in its configured Realm
colour, alongside their role and join date. Guilds receives only that bounded
presentation projection; it does not own or cache Realm identity.

The default Guildmaster dialogue opens a Guild-services node. It exposes
registration only to players outside a Guild; members can open their records;
and only the current leader sees visibility and ownership-transfer choices.
Changing visibility toggles Secret/Public and charges 50 carried Sigils. The
transfer path opens Members, where the leader explicitly selects a successor;
the server then validates eligibility and charges 25 carried Sigils. These
conditions and actions are registered as `elarion_guilds:not_in_guild`,
`elarion_guilds:in_guild`, `elarion_guilds:guild_leader`,
`elarion_guilds:open_registrar`, `elarion_guilds:open_guild_menu`, and
`elarion_guilds:toggle_secret`.

The Registrar receives the current creation fee, inventory Sigil count, currency name,
and field bounds from the server. Players choose only the display name, public
tag, and Secret setting; Guilds generates the stable internal ID. The action
opens Guild management for an existing member and the Guild-owned creation form
otherwise; the NPC never owns Guild state, fees, IDs, or permissions.

Admin commands:

```text
/e guild reload
/e guild list
/e guild inspect <guild>
/e guild delete <guild>
/e guild transfer <guild> <player>
```

## Public Presentation

Only the normalized tag renders publicly when the guild is configured to show
it:

```text
[MERC] PlayerName
```

Full guild names, announcements, membership, and roles are shown in the Guild
management surface. The selected chat channel remains selected between
messages; Guild is sent through the Core chat composer after server validation.

## Notifications

Guild invitations use only the immediate in-world Accept/Deny modal, so they
do not also appear in the notification tab. Acceptance, removal, leadership
transfer, and guild deletion publish dismissible Personal notices to affected
players. Guilds owns invite validation and expiry; Core owns notification
storage and presentation.

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

The Registrar and five-tab management surface are implemented with shared Core
theme, scaling, text-input, button, and fixed-palette canvas primitives. Invite
candidates and viewer permissions are bounded server projections. A non-leader
must confirm leaving before the server receives the request; a confirmed
departure explicitly closes the now-stale management screen. Guild emblems are
intentionally not rendered in chat.

## Bridge Heraldry Fields

Guild authority and membership projections include `iconRevision` and
`iconPaletteBase64`. The palette value is the exact fixed 16×16 (256-byte)
palette-index buffer encoded as Base64; bridge consumers cache by revision and
must treat the Guild addon as canonical. Legacy 32×32 Guild emblems are
deterministically downsampled when loaded and receive a new revision. Realm
identity projections keep their independent 32×32 `heraldryRevision` and
`heraldryPaletteBase64` fields.
