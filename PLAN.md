# Elarion Project Plan

Last reviewed: 2026-06-09

Author: Panyel

Team: Panetina Team

This file is the durable project brief for Elarion. Read it before making
architectural or gameplay changes. It records the intended experience, the
technical rules, lessons from the four legacy mods, the current implementation
state, and the planned build order.

## Source Material

- Original design document: `initial plan.pdf`
- Legacy Teams mod: https://github.com/Panetina/teams
- Legacy Namer mod: https://github.com/Panetina/namer-1.21-1.21.1
- Legacy Player Title mod: https://github.com/Panetina/player_title
- Legacy Contributions mod

The legacy mods are behavioral references, not codebases that should be merged
unchanged. Their useful features should be rebuilt around Elarion Core.

## Vision

Elarion is a Fabric 1.21.1 platform for a controlled, realm-driven Minecraft
world. It is not meant to become a pile of independent mods that each maintain
their own player, team, name, title, or progression data.

The intended structure is:

- Core owns canonical truth.
- Addons interpret and act on Core truth.
- Player-facing identity is derived from canonical data.
- Addons never maintain competing copies of citizen identity.
- Features, rewards, restrictions, and progression should be configurable.
- New addons should integrate through stable Core APIs and events.
- The server owner should be able to add mechanics without repeatedly rewriting
  Core.

`Realm` is the canonical gameplay term. It may represent a nation, kingdom,
faction, settlement, or another organized society without changing the
architecture.

### Worldheart

Elarion contains multiple Realms. Ancient Portal Gates will eventually open
inside each Realm and connect to **Worldheart**.

Worldheart is the canonical name for the separate, neutral world where all
Realms converge. It is an ancient, lore-significant meeting place and the
destination of every Ancient Portal Gate.

## Product Goals

### Realms

Each citizen may belong to a realm. A realm can define:

- Stable ID
- Display name and short name
- Chat/name prefix
- Vanilla Minecraft formatting color
- World ID and spawn
- Visibility scope
- Flags and progression state
- Portal and travel relationships
- Future alliances, government rules, and borders

Realm definitions belong in YAML. Citizen membership belongs in mutable
world storage.

Realm gameplay should eventually cover both identity and practical server
rules. Membership is not just a tag: it decides long-range chat, spawn and
respawn destinations, default visibility, relationship rules, reward delivery,
administrative announcements, and future portal/travel permissions.

The intended realm lifecycle includes:

- A new citizen starts in the lobby/default holding world until they choose or
  are assigned to a realm.
- When a citizen joins a realm, the server can teleport them to that realm's
  configured spawn and execute configured membership actions.
- A citizen normally respawns by hierarchy:
  1. valid bed or explicit vanilla spawnpoint
  2. configured realm spawn in that realm's managed world
  3. lobby/default spawn as the final fallback
- Jail, underworld, death events, and future temporary event states may override
  all normal spawn rules.
- Realm relationships such as allies, neutral, hostile, embargoed, or hidden
  should be data-driven and consumed by visibility, chat, portals, maps, and
  future government systems.

#### Realm Leadership

Each Realm should have exactly one leader. The leader's displayed role name
must be configurable globally or per Realm so it can appear as King, Governor,
Chief, or another lore-appropriate title without changing the underlying
system.

Leader authority should still come from Core citizen, Realm, title, and ability
truth. The leader role must not become a separate ownership database. A future
custom Realm decision block will let the leader propose diplomacy and visibility
actions such as declaring war, ending war, proposing an alliance, declaring an
embargo, returning to neutral, or going into hiding.

#### Realm Relationships

Realm relationships describe the political and gameplay state between two
Realms. They should become the shared source of truth that chat, visibility,
portals, maps, rosters, tab list, nameplates, tracking tools, government,
newspapers, and future addons can ask before exposing information or allowing
cross-realm actions.

Baseline relationship meanings:

- `ALLY`: trusted and cooperative. Allied Realms can freely use each other's
  Realm-to-Worldheart portals and may unlock shared visibility, travel, chat,
  trade, maps, support systems, and other friendly-realm conveniences.
- `NEUTRAL`: the default state. No special trust, no special hostility, and no
  extra access beyond ordinary public/shared-space rules.
- `EMBARGOED`: not war, but deliberately restricted. It remains safety-wise
  similar to neutral, but cooperation such as trade, private messages, mail,
  diplomacy, and other convenience systems may be blocked or limited.
- `HOSTILE`: formal enemy or war state. Future war systems may allow enemy
  portal use, invasion objectives, PvP, war announcements, combat permissions,
  and interaction with doors, chests, traps, and other configured blocks after
  war is declared and accepted. War must still prevent griefing, including
  block breaking, TNT or explosion damage to bases, destructive terrain damage,
  and other permanent base destruction unless explicitly allowed later.
- `HIDDEN`: seclusion state. The Realm may disable outbound Realm-to-Worldheart
  portal travel, return to Realm-only visibility, and hide itself from rosters,
  maps, tab list, command suggestions, tracking, public interfaces, and other
  discovery systems. Worldheart-to-Realm return travel should remain available
  so players already in Worldheart are not stranded.

Detailed war mechanics such as declarations, acceptance, invasion timers,
victory conditions, and peace treaties are future features. `HOSTILE` is the
relationship foundation for those systems, not the full war system by itself.

#### Realm Decisions And Voting

Realm relationship and hiding changes should be proposed through the Realm
leader and resolved through a time-limited citizen vote.

Planned proposal types include:

- Declaring war
- Ending war or returning to neutral
- Proposing or accepting an alliance
- Declaring an embargo
- Going into hiding
- Cancelling or rejecting a pending relationship proposal

Voting rules:

- A proposal opens a pop-up or future GUI notification for citizens of the
  declaring Realm and, where applicable, the receiving Realm.
- Offline citizens receive a pending vote prompt when they next join during the
  active voting window.
- A proposal succeeds only if the leader approves and at least 51% of all
  citizens in the affected voting Realm approve. This is based on total
  citizens, not only online players or votes cast.
- Votes last three real-world days by default.
- When a vote expires, fails, succeeds, is declared, accepted, rejected, or
  cancelled, the system records durable history events.
- Expired votes delete their active vote state after the decision is recorded so
  long-running servers do not accumulate stale decision data.

### Identity

Core must derive one `PlayerIdentity` from:

```text
Citizen -> Realm -> Title -> Status -> Abilities -> Visibility -> Output
```

That identity should eventually be used consistently for:

- Overhead nametag
- Title rendered below the username
- Tab-list name and ordering
- Local and realm chat
- Command suggestions and player targeting
- Placeholders used by addons
- Tracking interfaces and visibility-sensitive menus

A nickname is cosmetic. The UUID and Minecraft username remain canonical.

### Titles and Abilities

Titles are functional roles, not only cosmetic labels.

A title may grant ability keys such as:

- `elarion.newspaper.publish`
- `elarion.portal.foreign_access`
- `elarion.craft.special_item`
- `elarion.command.some_action`
- Addon-defined abilities registered later

Examples:

- A News Reporter may be the only role allowed to publish newspapers, use
  newspaper commands, craft related items, or interact with newsroom blocks.
- A Diplomat may be the only ordinary citizen allowed through another
  realm's portal.

The exact abilities will evolve. The architecture must allow new checks without
adding hard-coded title names throughout the codebase. Addons ask
`AbilityService` whether a citizen may perform an action.

### Chat

- `/rc <message>` is the Realm Chat command.
- `/rc`, `/w`, `/r`, and `/help` are player-facing commands.
- Realm chat is visible only to online citizens of the same realm.
- Realm chat automatically applies the realm tag and color.
- The long-term design has no unrestricted, long-range global player chat.
- Normal conversation should eventually be local/proximity chat.
- Local chat should be configurable by range, dimension/world scope, formatting,
  spy/admin visibility, and whether special statuses can speak or listen.
- Join/leave notices should be scoped instead of globally revealing everyone.
  The intended default is realm-scoped notices for realm members, admin notices
  for OPs, and optional global notices only for public/shared spaces.
- Private whisper commands remain intentionally limited by visibility rules.
  `/w` and `/r` allow same-realm messages first, and allow foreign messages
  only when the recipient belongs to a `GLOBAL` visibility realm. They must not
  become a way to discover hidden or foreign citizens.
- System and administrative communication remain separate channels.

The original PDF used `/nc`; the canonical command is `/rc` for Realm Chat.

### Visibility

Visibility is information control, not automatic physical invisibility.

For `visibility-scope: REALM`, the intended default behavior is:

- Citizens see their own realm members in the tab list.
- Foreign names and nametags may be hidden until rules permit them.
- Command suggestions and ordinary player targeting do not reveal hidden
  foreign citizens.
- Realm rosters, maps, compasses, and tracking tools expose only permitted
  citizens.
- Join/leave notices may be scoped to the realm.
- OPs retain administrative visibility.
- Titles, abilities, alliances, portals, events, or shared spaces may grant
  exceptions.

Foreign player models should not be made physically invisible by default. That
would be confusing and exploitable.

Supported scopes currently modeled by Core:

- `REALM`
- `ALLIES`
- `GLOBAL`
- `ADMIN_ONLY`
- `HIDDEN`

### Status

Use a small state model instead of unrelated booleans:

- `ACTIVE`
- `JAILED`
- `DEAD`
- `UNDERWORLD`
- `EXILED`
- `DIPLOMAT`

Status may affect movement, rendering, chat, voice chat, portal use, respawn,
tab visibility, and other addon behavior. Addons change or react to Core status;
they do not create a second identity record.

### Progression and Rewards

Progression should be data-driven. Configured milestones can execute reusable
reward actions rather than embedding server-specific rewards in Java.

Core reward actions currently include:

- Player message
- Broadcast
- Server command
- Player command
- Status change
- Title grant
- Ability grant
- History/progression event

Addons can register action types such as portal state changes or contribution
events. Future actions should include realm unlocks and other durable state
changes.

## Command Policy

- `/rc <message>` is available to ordinary players.
- Every `/e ...` command requires OP permission level 4 by default.
- An addon command must register under `/e` unless it has been deliberately
  approved as player-facing.
- Permission exceptions must be explicit, visible in code/config, and
  documented here.

Administrative namespaces:

```text
/e realm ...
/e citizen ...
/e title ...
/e ability ...
/e reward ...
/e history ...
/e chat ...
/e portal ...
/e jail ...
/e underworld ...
/e world ...
/e contribution ...
```

## Architecture Rules

These are non-negotiable unless this plan is deliberately revised:

1. `platform/core` is the only canonical owner of citizens, realm
   membership, titles, nicknames, status, granted abilities, and derived
   identity.
2. Addons depend on Core. Core must not depend on gameplay addons.
3. Addons use `ElarionApi`, Core events, registered ability keys, reward action
   handlers, and Core command registration.
4. Addons do not duplicate citizen/realm/title truth.
5. Editable definitions use YAML under `config/elarion/`.
6. Mutable state belongs under the active world save, not inside editable
   configuration.
7. Canonical records use UUIDs. Names are display data and lookup aliases.
8. Rendering is derived. Rendered strings are not saved as canonical truth.
9. Vanilla scoreboard teams are the backend for standard Minecraft player
   colors.
10. New features should be modular and configurable when server owners are
    likely to change their values, rewards, restrictions, or progression.
11. Mixins should adapt Minecraft rendering/targeting to Core services, not
    become alternate state owners.
12. Avoid hard-coding a specific realm or title into general-purpose
    services.

## Configuration Layout

```text
config/elarion/
  core/
    realms.yml
    citizens-defaults.yml
    titles.yml
    abilities.yml
    identity.yml
    chat.yml
    visibility.yml
    rewards.yml
    commands.yml
  addons/
    contributions/
      projects.yml
    portals/
      portals.yml
    jail/
      jail.yml
    underworld/
      underworld.yml
```

Mutable state should follow this shape:

```text
world/elarion/
  citizens/
  progression/
  history/
  addon-state/
```

Generated YAML should include useful comments, supported values, and concise
examples. Reloading configuration must not erase user comments.

## Legacy Mod Findings

### `Panetina/teams`

The old Teams mod combined many responsibilities:

- Team definitions and member UUIDs in `config/teams/teams.json`
- A singleton `TeamStorage` with team and player-to-team maps
- `/t` private team chat with colored prefixes and names
- OP-level team add/remove/give/teleport/border/mail commands
- Per-team spawn and respawn fallback
- Automatic teleport and configured commands when joining a team
- Per-player world-border packets and merged-border mode
- Vanilla scoreboard teams for named nametag colors
- Client packets carrying team assignments, names, and colors
- Mixins for chat, tab list, display names, and disabling `/teammsg`
- Online item rewards plus pending rewards for offline members

What to preserve:

- Realm-only long-range chat
- Standard color propagation through scoreboard teams
- Realm spawn/respawn behavior
- Configurable actions on membership or progression
- Realm-wide rewards, including eventual offline delivery
- Realm-specific borders if they still fit the world design
- Administrative messaging and event control

What to replace:

- Realm member UUIDs must not live inside editable realm definitions.
- Teams must not own a second player identity database.
- Chat, names, tab rendering, rewards, borders, and membership should not be
  tightly coupled in one singleton.
- Hard-coded standalone commands become `/e` subcommands.
- Hex colors should not be canonical for vanilla nametag coloring. Use the 16
  supported Minecraft formatting colors unless a separate renderer is added.

New homes:

- Membership, colors, identity, chat, spawn references -> Core
- Borders/world mechanics -> worlds or realms addon
- Realm item delivery -> reusable reward action/addon service
- Tab rendering -> tablist addon consuming Core identity

### `Panetina/namer-1.21-1.21.1`

The old Namer implementation used:

- Cardinal Components attached to the scoreboard for synchronized nickname
  storage
- Owo config for maximum nickname length and formatting permission
- `/nick <player> "nickname"` and `clean`
- Mixins replacing `PlayerEntity.getDisplayName()`
- Player-list display-name updates
- Client and server command-suggestion mixins
- Nickname aliases for entity/player targeting
- Formatting parsing for styled nicknames

What to preserve:

- Nicknames visible wherever player identity is shown
- Immediate synchronization after nickname changes
- Configurable length and formatting rules
- Command suggestions that can display/search nicknames
- Commands resolving a citizen by nickname, visible display name, or username

What to replace:

- Nicknames now live in Core's UUID-based `CitizenRecord`.
- Cardinal Components must not become a second canonical nickname store.
- Suggestions and target resolution must filter through visibility rules.
- The lookup order must remain unambiguous and fall back safely to the canonical
  username.
- Nickname formatting must not be allowed to spoof trusted system, admin,
  realm, or title presentation.

New home:

- Storage and identity derivation -> Core
- Minecraft display/target/suggestion integration -> Core mixins/hooks, with
  tab-specific rendering in the tablist addon if appropriate

### `Panetina/player_title`

The old Player Title mod used:

- `/playertitle <player> <title>` and a clear command
- A text file mapping UUIDs to arbitrary title strings
- A server-to-client title update payload
- A client-side UUID-to-title cache
- A `PlayerEntityRenderer` mixin rendering a smaller second label below the
  username
- Join synchronization of all stored titles

What to preserve:

- Titles displayed below player usernames
- Live synchronization when titles change
- Correct synchronization for newly joining clients
- Clear administrative title assignment

What to replace:

- Titles are configured definitions referenced by stable IDs, not arbitrary
  strings stored in a separate text file.
- Citizen title ownership belongs to Core.
- Title rendering consumes `PlayerIdentity.titleText`.
- Titles also grant abilities and may affect gameplay.
- Old permission level 2 is replaced by Elarion's OP level 4 policy.

New homes:

- Title definitions, assignment, abilities, and derived text -> Core
- Client rendering and synchronization -> titles addon using Core data

### Legacy Contributions Mod

The old Contributions mod used:

- An unbreakable custom contribution block and block entity
- A custom screen and screen handler
- Client/server payloads for project information and donations
- Diamonds as a hard-coded currency
- `/contribute <amount>` near a contribution block
- Per-project UUID, location, title, level, progress, contributor totals, and
  donation history
- Per-project JSON files under the config directory
- Level thresholds with raw server commands as rewards
- Automatic level advancement and command execution
- Project deletion when its block was broken

What to preserve:

- A physical realm project block/interface
- Configurable project title, currency, levels, descriptions, and rewards
- Donation validation and inventory consumption
- Visible progress and recent contribution history
- Multiple projects with independent progress
- Milestones that unlock world, border, portal, government, or other features

What to replace:

- Currency must be configured per project instead of hard-coded to diamonds.
- Runtime project state belongs under `world/elarion/addon-state/contributions/`.
- Contributors use real player UUIDs, not name-derived UUIDs.
- Milestones call Core reward actions instead of relying only on raw commands.
- Projects may be owned/scoped by realm and enforce visibility/permissions.
- Breaking/replacing project blocks must have explicit safe lifecycle rules.
- The addon consumes Core citizen/realm identity and emits Core progression
  and history events.

New home:

- Project definitions and gameplay -> contributions addon
- Reward execution, citizen identity, abilities, events, and history -> Core

## Current Repository Structure

```text
platform/core
addons/titles
addons/realms
addons/names
addons/contributions
addons/portals
addons/jail
addons/underworld
addons/worlds
addons/voicechat-hooks
addons/tablist
addons/government
addons/newspapers
dev
```

`dev` is only the combined development launcher. It loads Core and all addons
for `runClient`, `runServer`, `runClientOne`, and `runClientTwo`. It is not a
production gameplay mod.

## Current Working State

The following are implemented:

- Gradle multi-project Fabric 1.21.1 structure using Java 21 and Yarn
- Core/addon bootstrap through `ElarionAddon` and `ElarionApi`
- Generated YAML defaults
- UUID-based citizen JSON storage in the world save
- Realm assignment/removal
- Realm color teams using vanilla scoreboard teams
- `/rc` realm-isolated messaging
- Realm tag and player-name coloring in `/rc`
- Nickname persistence and use in derived identity and `/rc`
- Nicknames applied to client display names, overhead nametags, and tab names
- Derived identity applied to ordinary vanilla chat sender names
- Nickname-aware direct player command suggestions and targeting
- Nickname enable/max-length settings enforced from `identity.yml`
- Configured title assignment and persistence
- Titles synchronized and rendered beneath visible player usernames
- Title-provided and explicitly granted ability checks
- Per-viewer identity synchronization
- `REALM`, `GLOBAL`, `ADMIN_ONLY`, and `HIDDEN` visibility enforcement for
  tab entries, nametags, command suggestions, and direct player targeting
- OP level 4 visibility override
- Configured reward execution
- Citizen and progression event hooks
- OP-level 4 `/e` command root
- Two independent development clients and a local dedicated test server

Current commands:

```text
/rc <message>
/w <player> <message>
/r <message>
/help [command]

/e realm add <player> <realm>
/e realm remove <player>
/e realm list
/e citizen info <player>
/e citizen nickname set <player> <nickname>
/e citizen nickname clear <player>
/e title set <player> <title>
/e title clear <player>
/e title list
/e ability check <player> <ability>
/e ability grant <player> <ability>
/e ability revoke <player> <ability>
/e reward run <reward> <player>
/e history recent [limit]
/e history player <player> [limit]
/e history realm <realm> [limit]
/e history category <category> [limit]
/e reload
```

Command policy:

- `/msg`, `/tell`, `/teammsg`, `/tm`, and `/me` are removed.
- `/w` may target citizens in the sender's own Realm, plus members of Realms
  with `visibility-scope: GLOBAL`.
- `/r` replies to the last online player who sent the caller a `/w`.
- `/list` requires permission level 4.
- `/seed` requires permission level 4 and `show-seed=true` in
  `server.properties`.
- `/help` lists only commands available to the caller and includes syntax
  explanations.

## Project To-Do List

Checked items are implemented and practically testable. Unchecked items are
planned, partial, config-only, or addon shells.

### Platform and Core

- [x] Fabric 1.21.1 multi-project structure with Java 21
- [x] Core API and addon entrypoint system
- [x] UUID-based citizen storage under the world save
- [x] YAML default generation and Core reload command
- [x] Citizen, realm, title, status, ability, identity, and reward models
- [x] Citizen-change events and addon-facing service access
- [x] Validate every YAML field with clear startup/reload errors
- [x] Preserve comments and migrate versioned configs with pre-migration backups
- [x] Add modular durable history storage and history query commands
- [x] Add automated unit, integration, and server GameTest foundations
- [ ] Add gameplay-specific GameTests as realm, portal, title, and
  contribution mechanics are implemented

### Names and Identity

- [x] Persist nicknames in Core citizen records
- [x] Configure nickname enablement and maximum length
- [x] Show nicknames in normal chat, `/rc`, tab, and overhead nametags
- [x] Keep realm color on chat, tab, and overhead names
- [x] Keep realm tags out of normal chat, tab, and overhead names
- [x] Synchronize nickname, title, prefix, and suffix data on join and live changes
- [x] Suggest nickname aliases while completing canonical usernames
- [x] Support nickname suggestions containing whitespace
- [x] Place the cursor after the completed username when nickname and username
  lengths differ
- [x] Resolve visible players by username or unambiguous nickname
- [x] Reject nicknames that spoof protected staff, admin, system, realm,
  or title presentation
- [x] Add configurable nickname uniqueness and reserved-name rules
- [x] Treat nickname capitalization, whitespace, and common-separator variants
  as the same identity for uniqueness and reserved-name checks
- [x] Allow only letters, spaces, apostrophes, and hyphens in submitted
  nicknames
- [x] Automatically title-case every nickname segment for roleplay presentation
- [x] Reserve configured realm and title presentation automatically
- [x] Detect common Cyrillic and Greek lookalike characters in protected names
- [x] Reject protected-name extensions such as `Server Notice` or
  `Government Official`
- [x] Move optional presentation-specific behavior into the Names addon while
  keeping canonical nickname state in Core

### Realms and Chat

- [x] Load realm definitions from YAML
- [x] Assign and remove realm membership
- [x] Create and update vanilla scoreboard teams for realm colors
- [x] Provide realm-isolated `/rc`
- [x] Render the realm tag only in `/rc`
- [ ] Replace global vanilla chat with configurable local/proximity chat:
  configurable block radius, same-world requirement, formatting, admin/social
  spy behavior, status restrictions, and optional event/shared-space overrides
- [ ] Keep `/rc` as realm long-range chat and make it coexist cleanly with
  local chat without leaking hidden citizens
- [x] Let `/w` and `/r` message citizens in the sender's own realm, while
  allowing foreign whispers only to citizens in `GLOBAL` visibility realms
- [ ] Add scoped join/leave notices:
  realm notices for realm members, admin notices for OP level 4, optional global
  notices only for public/shared spaces such as Worldheart or explicitly
  `GLOBAL` visibility realms, and no identity leak for hidden citizens
- [ ] Implement realm spawn and respawn behavior:
  bed or explicit vanilla spawnpoint first, configured realm spawn second,
  lobby/default spawn third
- [ ] Allow jail, underworld, death events, and future event states to override
  bed, realm spawn, and default spawn behavior through a modular spawn policy
  chain
- [ ] Teleport a citizen to the configured realm spawn when added to a realm,
  with config flags for whether this happens automatically, only on first join,
  or only through an admin command
- [ ] Run configurable realm-join actions when a citizen is added to a realm:
  messages, broadcasts scoped to the realm, commands, starter rewards, history
  events, spawn teleport, and future GUI/tutorial hooks
- [ ] Implement alliances and relationships:
  `ALLY`, `NEUTRAL`, `HOSTILE`, `EMBARGOED`, `HIDDEN`, and future
  addon-defined relationship states
- [ ] Apply realm relationships to visibility, `/w`, `/r`, `/rc`, local chat
  exceptions, portals, maps, rosters, tab list, nameplates, tracking tools, and
  future government/newspaper rules by having those systems ask the shared
  relationship layer before exposing information or allowing cross-realm
  actions
- [ ] Add exactly one leader per realm, with configurable leader-title
  presentation such as King, Governor, or Chief
- [ ] Automatically render a crown for each Realm leader, overriding helmet
  appearance as a temporary placeholder until the final leader model exists
- [ ] Add a custom Realm decision block for leader-started diplomacy,
  relationship, war, neutral-return, embargo, and hiding proposals
- [ ] Add a three-day vote lifecycle with pop-up or future GUI prompts, offline
  vote delivery, leader approval, 51% all-citizen approval, expiration, cleanup,
  and history events
- [ ] Apply relationship effects to portals, visibility, PvP, interaction
  permissions, trade, private messaging, mail, diplomacy, and history
- [ ] Add non-griefing war protection so hostile players can use configured
  doors, chests, traps, portals, and PvP without breaking blocks, destroying
  bases, or using TNT/explosions for permanent base damage
- [ ] Implement optional realm borders
- [ ] Add realm-wide rewards with offline delivery:
  online members receive immediately, offline members receive pending rewards on
  next login, and all delivery attempts are recorded in history
- [ ] Add realm mail and administrative announcements:
  persistent realm-scoped messages, OP-authored announcements, optional read
  tracking, expiration, and history/audit entries

### Titles and Abilities

- [x] Load configured title definitions
- [x] Persist title assignment by stable title ID
- [x] Render titles below visible player usernames
- [x] Apply configured title suffixes to player identity
- [x] Grant abilities through titles and explicit citizen grants
- [x] Synchronize titles on join and live changes
- [ ] Enforce abilities on commands, crafting, blocks, items, portals, and GUIs
- [ ] Implement News Reporter newspaper permissions
- [ ] Implement Diplomat foreign-portal access
- [ ] Replace the single owned title field with:
  `unlocked-title-ids`, `active-title-id`, and optional title progress
- [ ] Give every citizen the non-removable `citizen` title by default
- [ ] Support title acquisition modes:
  `DEFAULT`, `ADMIN_ONLY`, `DISCOVERABLE`, `PROGRESSION`, and `ADDON`
- [ ] Support title ownership modes:
  `UNLIMITED`, `ONE_PER_PLAYER`, and `GLOBALLY_UNIQUE`
- [ ] Store globally unique title claims in durable world state so only the
  first successful player can own titles such as `maze_runner`
- [ ] Make unique-title claiming atomic so two simultaneous completions cannot
  both receive the same title
- [ ] Record title unlocks, failed unique claims, selection changes, revocations,
  and admin grants in modular history
- [ ] Add an unlock-condition registry so Core and addons can register condition
  types without hard-coded title IDs
- [ ] Add condition types for advancement, statistic, location/region,
  dimension, elapsed time, continuous survival, contribution milestone,
  progression event, command/admin grant, and addon-defined checks
- [ ] Support continuous-condition state, including `aquatic` for living
  underwater for a configured duration such as seven Minecraft days
- [ ] Support one-time discovery events, including `maze_runner` for reaching
  the configured end region of an indestructible maze
- [ ] Add configurable title effects through ability keys and registered effect
  handlers rather than hard-coded title-name checks
- [ ] Allow effects such as persistent water breathing while `aquatic` is
  active, with clean removal when the title is no longer active
- [ ] Keep admin roles such as `news_reporter` and `diplomat` hidden from normal
  discovery and grantable only by OP level 4 or an explicit trusted workflow
- [ ] Support multiple unlocked titles with explicit active-title display,
  suffix, priority, ability, and effect precedence
- [ ] Add `/titles` as an explicitly approved player-facing command that opens
  a future GUI
- [ ] In the `/titles` GUI, show `citizen` and only titles the player has
  unlocked; do not reveal undiscovered or locked title definitions
- [ ] Let players select an unlocked title as active, while the server validates
  ownership and applies/removes abilities and effects
- [ ] Show title description, acquisition history, uniqueness, active effects,
  and unlock date without exposing secret unlock requirements
- [ ] Add OP level 4 commands to grant, revoke, inspect, release unique claims,
  and repair title ownership state
- [ ] Add migration from the current single `titleId` citizen field to the new
  unlocked/active title model
- [ ] Add unit, integration, and GameTests for unique-title races, admin-only
  grants, hidden locked titles, active effect cleanup, persistence, and GUI
  ownership validation

### Visibility

- [x] Filter tab entries, nametags, suggestions, and player targeting
- [x] Implement `REALM`, `GLOBAL`, `ADMIN_ONLY`, and `HIDDEN`
- [x] Preserve OP level 4 administrative visibility
- [x] Restrict `/list` to permission level 4
- [ ] Add a comment in the visibility.yml with current "`REALM`, `GLOBAL`, `ADMIN_ONLY`, and `HIDDEN`"
- [ ] Implement alliance-aware `ALLIES` visibility
- [ ] Apply visibility to maps, trackers, rosters, menus, and join/leave notices
- [ ] Add title, ability, portal, event, and shared-space visibility exceptions

### Progression and Rewards

- [x] Execute configured messages, broadcasts, commands, status changes, title
  grants, ability grants, and history events
- [ ] Add durable realm unlock actions
- [ ] Add portal state actions
- [ ] Add configurable progression conditions and milestones
- [ ] Add offline and delayed reward delivery
- [ ] Add audit/history views for reward execution

### Contributions

- [ ] Rebuild the contribution block and block entity
- [ ] Rebuild the contribution screen and networking
- [ ] Load configurable projects, currencies, levels, and rewards
- [ ] Store project progress and donation history in world addon state
- [ ] Consume currency safely and use real contributor UUIDs
- [ ] Execute Core reward actions at milestones
- [ ] Add realm ownership, visibility, and permission rules
- [ ] Define safe project-block break, move, and deletion behavior

### Portals and Worlds

- [x] Create and manage persistent addon-owned worlds through Elarion Worlds
- [x] Resolve configured world IDs and spawn points through `ElarionWorldsApi`
- [x] Add strict modular `worlds.yml` definitions for seeds, templates,
  difficulty, gamerules, spawn points, and per-world borders
- [x] Add modular `VOID`, `FLAT`, `OVERWORLD`, `NETHER`, `END`, `CAVE`, and
  `CUSTOM` world types, plus confirmed `/e world create|remove`
- [x] Keep configured and command-created worlds together in the canonical
  `worlds.yml`
- [x] Add configurable real teleport destinations, starting with `lobby` and
  ready for future `jail`, `underworld`, `nether`, or `end` destinations
- [x] Make `/worldborder` target the command source's current world and configure
  and persist independent borders for addon-managed worlds
- [x] Add a tiny one-block VOID lobby with no spawn chunks or mob spawning for
  players waiting to choose a realm and as the no-bed world-spawn fallback
- [x] Add deterministic per-world block and mob abundance controls for future
  trade/resource specialization
- [x] Synchronize independent world borders only to players in that world
- [x] Add `/e world list|reload|load|unload|tp|info`
- [ ] Make sure that If I delete the world (the folder) - rebuilt it (useful for clean resets of everything/or new server/or regeneration with new ore/mobs rules after editing the config)
- [ ] Connect realm membership to managed world spawn and respawn policy
- [ ] Implement portal definitions and physical portal travel
- [ ] Implement dormant, active, open, locked, restoring, and disabled states
- [ ] Gate portal use through progression and abilities
- [ ] Enforce Diplomat or `elarion.portal.foreign_access` for foreign portals
- [ ] Add portal administration commands and feedback
- [ ] Delete End Portal generation/ stronghold generation/ nether portal creation/ both portals functioning
- [ ] Add custom portals for end and nether for progression and to place them in the center of Worldheart.

### Jail, Death, and Underworld

- [ ] Implement jail cells, sentences, release times, and persistence
- [ ] Enforce jail movement, portal, chat, and voice restrictions
- [ ] Implement safe release and state restoration
- [ ] Implement death-to-underworld flow
- [ ] Implement underworld spawn, return, and progression rules
- [ ] Enforce underworld portal, chat, and voice restrictions

### Future Addons

- [ ] Integrate Simple Voice Chat restrictions
- [ ] Build advanced tab ordering and admin grouping
- [ ] Build configurable government roles, laws, elections, and decisions
- [ ] Build newspapers, publishing, crafting, blocks, items, and interfaces
- [ ] Add history, events, and advanced progression interfaces

## Placeholder or Incomplete State

These exist as models, configuration, ability registrations, or addon shells but
do not yet produce their final gameplay effect:

- Ordinary vanilla chat now displays derived identity, but it remains global
  until the planned local/proximity chat system replaces it.
- Nickname suggestions resolve to canonical usernames because vanilla entity
  arguments cannot safely accept a nickname containing spaces as the actual
  selector value.
- Title abilities are not yet enforced by crafting, portal, newspaper, or other
  gameplay hooks.
- Normal vanilla chat remains global; local-by-default chat is not implemented.
- `ALLIES` visibility currently behaves like `REALM` until alliance data is
  implemented.
- Visibility is not yet applied to future maps, trackers, rosters, join/leave
  notices, or addon interfaces.
- Realm spawn/world/flags are loaded but do not yet drive teleport,
  respawn, world, or border mechanics.
- Status changes persist but do not yet impose movement, chat, voice, portal,
  death, or rendering restrictions.
- Portal states are config/event placeholders with no portal block or travel.
- Contributions has config/event placeholders but no block, GUI, currency
  consumption, project progress, or persistent project state.
- Jail, underworld, worlds, tablist, government, newspapers, and voicechat
  integration are addon shells.
- Dynamic command names and permission values from `commands.yml` are not yet
  enforced.

## Planned Addons

### Titles

- Synchronize derived title data to clients.
- Render title under username without duplicating title storage.
- Refresh on assignment, reload, join, status change, and visibility change.

### Names

- Apply nickname through all identity surfaces.
- Enforce length and formatting policy.
- Add visibility-aware nickname/username lookup.
- Add visibility-aware Brigadier suggestions.
- Prevent spoofing of protected prefixes and admin/system presentation.

### Realms

- Realm lifecycle rules built on Core membership events.
- Realm spawn, respawn, join-teleport, and membership-action integration using
  Core identity plus the worlds addon for managed destination resolution.
- Relationship and alliance data that can be consumed by visibility, chat,
  portals, rosters, maps, government, newspapers, and future addons.
- Scoped join/leave messaging that respects visibility and OP-level
  administrative visibility.
- Realm-wide actions, mail, announcements, and offline reward delivery using
  Core reward actions and history storage.
- Optional border rules if retained, coordinated with worlds rather than
  stored as duplicate realm truth.

### Contributions

- Rebuild the contribution block, block entity, screen, and networking.
- Load project definitions from YAML.
- Store runtime project state under the world save.
- Use actual UUIDs and Core identity.
- Execute Core reward actions at configured milestones.
- Emit history/progression events.
- Support realm ownership, permissions, and visibility.

### Portals

Portal states:

- `DORMANT`
- `ACTIVE`
- `OPEN`
- `LOCKED`
- `RESTORING`
- `DISABLED`

Rules:

- Dormant portals have no active animation, particles, sound, or travel.
- Active portals may be visible but progression-gated.
- Open portals permit valid travel.
- Locked portals are visible but unusable.
- Restoring portals expose progress feedback.
- Disabled portals are administratively shut down.
- Foreign-realm access checks
  `elarion.portal.foreign_access`.

Planned commands:

```text
/e portal status
/e portal set-state <portal> <state>
/e portal open <portal>
/e portal close <portal>
/e portal lock <portal>
/e portal unlock <portal>
/e portal restore <portal>
/e portal enable <portal>
/e portal disable <portal>
```

### Jail

- Set `CitizenStatus.JAILED`.
- Move citizens to configured cells/worlds.
- Store sentence, reason, start, and release time in addon state.
- Restrict portals, voice chat, and configured actions.
- Restore normal state safely on release.

### Underworld

- Treat underworld presence as gameplay state, not only dimension location.
- Route death/respawn through Core and worlds services.
- Restrict portals and voice according to config.
- Provide explicit send, return, status, and spawn administration.

### Worlds

- Create/load/resolve addon-managed worlds.
- Keep low-level world lifecycle outside Core.
- Let Core retain only world identifiers and spawn/world policy.

### Voice Chat Hooks

- Integrate with Simple Voice Chat as an optional dependency.
- React immediately to Core status/visibility changes.
- Apply configured mute/restriction behavior for jail, underworld, exile, and
  future states.

### Tab List

- Render Core identity and scoreboard colors.
- Filter entries according to visibility scope and exceptions.
- Keep permitted admins visible and grouped at the bottom.
- Expand visibility when portals, alliances, abilities, or progression allow.

### Government

- Consume realm/citizen/title/ability state.
- Keep roles, decisions, elections, laws, and progression modular.
- Trigger configured rewards and history events.

### Newspapers

- Restrict publishing and management through abilities.
- Make News Reporter capabilities configurable.
- Support future commands, crafting, items, blocks, and interfaces without
  hard-coding the title ID into every action.

## Recommended Build Order

1. Finish Core identity integration:
   nickname rendering, nametags, tab identity contract, target resolution, and
   synchronized identity updates.
2. Implement title rendering below usernames through the titles addon.
3. Enforce visibility for tab list, nametags, suggestions, and targeting.
4. Replace unrestricted vanilla player chat with local chat while retaining
   `/rc` as realm long-range chat.
5. Implement worlds/spawn resolution needed by travel and respawn features.
6. Implement portals and progression-controlled visibility/travel.
7. Rebuild contributions using Core rewards and world-state storage.
8. Implement jail and its restrictions.
9. Implement underworld death/respawn flow.
10. Add voice-chat hooks.
11. Add government, newspapers, history, and advanced progression.

This order may be adjusted for testing, but canonical state and visibility must
be finished before addons expose hidden citizens through commands, UIs, or
travel.

## Definition of Done for a Feature

A feature is not considered complete merely because its config or model exists.
It is complete when:

- Its gameplay behavior is implemented.
- It uses Core canonical state correctly.
- Permission and ability checks are enforced at the action boundary.
- Mutable state survives restart in world storage.
- Configuration is generated, documented, loaded, validated, and reload-safe
  where practical.
- Client/server synchronization works for joins and live changes.
- Visibility rules do not leak hidden identity.
- OP-level command policy is respected.
- The combined dev server and two-client setup can test it.
- `gradlew build` passes.
- The working/placeholder sections of this file are updated.

## Instructions for Future Development Sessions

When opening this repository:

1. Read this file and `README.md`.
2. Inspect the current source before assuming the status list is still exact.
3. Treat `initial plan.pdf` and the linked legacy repositories as design and
   behavior references.
4. Preserve useful legacy behavior, but rebuild it through Core services.
5. Do not introduce duplicate citizen, realm, title, nickname, status, or
   ability storage in addons.
6. Keep `/rc`, `/w`, `/r`, and `/help` player-facing. Keep administrative
   Elarion commands OP level 4 unless this file is explicitly changed.
7. Update this file when a design decision changes or a placeholder becomes a
   working feature.
