# Elarion Offerings Addon

Last reviewed: 2026-07-05

Status: Shrine-backed Offerings V1 implemented. Holograms, blueprints, advanced
requirements, and society ranks remain future work. The current
Shrine of Foundation is a 2x2x5 OP-placed multiblock so the final model can be
designed and tested in-game.

## Ownership

Offerings owns:

- the Shrine of Foundation item/block
- project definition loading
- project instances
- project anchors
- Offering progress counters
- contributor totals
- the owner-maintained Character Menu Offering score projection for new direct
  accepted player contributions
- bounded donation records
- Offering-owned progression flags
- optional per-instance display-name override used by quest memorial outcomes
- bounded public Shrine progress and typed map-marker projections derived from
  canonical Offering instances and anchors

Offerings does not own:

- Realm membership or Realm definitions
- citizen records
- Economy balances or transactions
- Core rewards/history truth
- Government offices/laws
- future Ledger reputation

## Config

Definitions live under:

```text
config/elarion/addons/offerings/
  ui.yml
  society.yml
  projects/<project-id>.yml
```

Offerings registers a read-only config descriptor domain named `offerings`
through `ElarionApi.system().configs()`. The descriptors read active
`OfferingDefinitionService` project/UI snapshots for project IDs, scopes,
repeatability flags, requirement/milestone/level counts, presentation fields,
and Shrine UI sizing/placeholders. `society.yml` is reported as reserved V1
metadata because it is generated but not parsed into a runtime model. Admin
Panel discovery does not parse config files. Config editing, donation or
progression behavior changes, rewards, Shrine block changes, reload semantic
changes, packet changes, and persistence changes remain future approved slices.

`/e offerings reload` prepares project definitions and Shrine UI config before
publishing either snapshot through `OfferingDefinitionService`. If UI config
loading fails after project parsing succeeds, the previous live project/UI
snapshot remains active instead of leaving the addon in a mixed old/new state.

Project definitions are reusable templates. Runtime instances can be:

- `realm`: explicitly owned by a Realm.
- `global`: public/global project.
- `location`: tied to the admin's current world/position when started.

Location does not imply Realm ownership. Realm ownership is explicit.

Supported V1 requirement counters:

- `items`
- `currency`
- `events`

Supported V1 milestone types:

- `elarion:set_realm_flag`
- `elarion:clear_realm_flag`
- `elarion:run_reward`
- `elarion:emit_history`
- `elarion:economy_reward_realm`
- `elarion:economy_sink_realm`
- `elarion:notify_realm`
- `elarion:notify_world`

Unknown requirement and milestone IDs fail config validation.

Notification milestones publish through Core and support configured
`title`, `body`, and `icon` presentation fields. Realm notifications require a
Realm-owned instance; World notifications target the current eligible global
audience. The Realm flag `ancient_gate_unlocked` is the canonical boundary for
World notification eligibility and World icon visibility. Offerings owns that
flag; Core caches eligible Realm IDs for bounded notification filtering.
Offerings does not store a second notification inbox.

`elarion:run_reward` milestones expose each item and currency action from their
Core reward definition as an individual Minecraft item stack. The configured
quantity is drawn over the icon and hovering uses the item's native tooltip.

Non-item milestones and reward actions may expose an event-style summary:

```yaml
parameters:
  display-label: "50 Sigils and 3 Emeralds"
  display-body: "Granted to the citizen who completes the project."
  display-icon: "item:elarion:currency"
```

These fields are presentation metadata only. They are intended for effects such
as opening a portal, unlocking an office, or enabling a service. The milestone
action and Core reward definition remain authoritative for the actual reward.

The Shrine renders configured rewards as centered icon slots. Hovering a slot
shows its `display-label` and `display-body` in a standard Minecraft tooltip.
The project title is subtly brighter than ordinary themed headings so the
active public project remains the primary visual landmark.
When an instance is already completed, the Shrine UI projects requirement rows
and totals as visually complete even if completion was forced/admin-triggered
and the persisted progress map was not filled. This is a presentation-only
projection; stored progress remains the audit trail of actual contributions.

Offering instances may carry an optional display-name override. The Shrine UI
uses that override as the title when present. Without an override, the title is
the active level's configured presentation text, then the level display name,
then the project display name. Progress, requirements, milestones, anchors, and
project definitions remain owned by Offerings. Quests use this through the
Offerings API for content-defined outcome or memorial names; they do not own
Shrine progress or anchors.

Future Shrine stories should stay in Quest/NPC/content-pack data. Offerings
only supplies the reusable Shrine, Offering progress, milestones, rewards,
reset behavior, and display-name projection target.

`ui.yml` owns Shrine layout and placeholder text. Shared visual tokens come
from Core `config/elarion/core/ui_theme.yml` variant `shrine`.

Projects may define:

```yaml
presentation:
  level-text: "Foundation I"
  icon: "minecraft:textures/item/amethyst_shard.png"
```

## Runtime State

Runtime state lives under:

```text
world/elarion/addon-state/offerings/
  state.json
  projects.json
  anchors.json
```

`state.json` is the compact canonical addon snapshot. `projects.json` and
`anchors.json` are operator-readable snapshots of the same runtime data.

Donation history retains at most 50 records per instance. Shrine UI snapshots
send at most the newest 20 records. Full long-term public meaning belongs in
Core history/Chronicle projections rather than unbounded Offering state.

`OfferingWebProjectionPublisher` listens to accepted instance mutations and
publishes `metric.shrine-contribution` plus `map.marker.shrine`. Startup makes
one bounded pass over the compact instance snapshot. Ordinary contributions
update only their affected instance. Contributor identities and donation
records are never included. Removed instances/anchors publish inactive
tombstones so the website cannot retain stale landmarks.

`OfferingChronicleText` registers with Core public history and renders
`project-completed`, `project-force-completed`, and
`realm-global-access-changed` Chronicle projections through the shared
template-family contract. The `offering.project-completed` and
`offering.realm-global-access-changed` families have 10 authored stable
variants, honor persisted `chronicle.variant` values, and fall back safely when
older records lack context. Offering project state, donations, milestone
execution, global-access flags, and reward delivery remain owned by Offerings.

## Commands

All commands require OP level 4:

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

`start location` requires an executing player because it uses the admin's
current world and block position.

`shrine link` creates and manages the internal location link automatically.
There are no public anchor-management commands.

`shrine remove` removes the structure and deletes its linked project instance.
Breaking a linked Shrine does the same. Use `shrine unlink` first when the
instance must survive removal of the structure.

`/e test shrine reset` resets all Offering instance progress, donation history,
display-name overrides, and Offering-owned Realm Foundation/global flags while
preserving Shrine blocks and links. Reset instances return to their first
configured project level, so a reset Shrine title falls back to that level's
presentation text such as `Foundation I`. Reset also reverses project-owned
side effects declared by milestones, including clearing `elarion:set_realm_flag`
flags and locking routes previously opened by `elarion:portal_unlock`.
`/e test shrine reset <realm>` limits that reset to Realm-scoped instances and
flags for one Realm. It does not touch NPC placements, Quest state, or other
addon coordinates. Reset increments the instance reward generation, so
configured `elarion:run_reward`, `elarion:notify_realm`, and
`elarion:notify_world` milestones can fire again when the Shrine is rebuilt
without duplicating rewards inside the same generation.

Project requirements are edited in project YAML definitions and loaded with
`/e offerings reload`. Administrative progress-injection commands are not
exposed.

## API

`ElarionOfferingsApi` exposes:

- definitions
- instances
- anchors
- start Realm/global/location project
- contribute item/currency/event progress
- progress view
- Offering-owned Realm progression flags
- per-instance display-name override

Future blocks, NPCs, Government pages, Quests, and Portals should use this API
or registered actions instead of reading runtime files directly.

Offerings also registers `OfferingProfileContributor` with Core's
`CitizenProfileService`. It contributes the reserved Character Menu summary
slot `offerings/offering-score` with `SELF` visibility by reading the bounded
Core player-stat key `offerings_score`. `OfferingService` increments this stat
only after a direct item or currency player contribution has been validated and
persisted successfully. Event/admin progress injections do not count as a
personal Offering score, old contributions are not backfilled, and the Ledger
must not scan Offering instances or donation history to invent totals.

## Shrine Of Foundation

The current placeholder item is:

```text
elarion:shrine_of_foundation
```

It is available in the Elarion Shrines creative tab.

Placement behavior:

- Requires a clear 2x2x5 block area.
- Places a 20-block placeholder structure.
- Breaking any part removes the whole Shrine.
- Placement is OP level 4 only.
- The structure stores horizontal facing on every part for future oriented
  models.
- Uses a magenta/black missing-texture style until the final model exists.
- It does not auto-link to a project. OPs manually link the Shrine they are
  looking at with `/e offerings shrine link <instance>`.
- Right-clicking a linked Shrine opens a responsive sacred project UI with
  Contribute and History tabs.
- The Shrine uses the canonical civic brown/gold shell. `SHRINE OF FOUNDATION`
  is the stable screen title; the server-authored active project title remains
  visible in the left summary beside its description and rewards.
- Selected tabs use a green border/accent, requirements use compact semantic
  rows, and the contribution amount prompt uses the shared civic modal/buttons.
- The main Shrine view keeps a compact Rewards summary in the left project
  column, so a separate Rewards tab is unnecessary.
- Requirements and aggregate progress are server-authored. Item and currency
  rows open a bounded numeric prompt and send IDs plus requested amount only.
- Item offerings consume matching inventory items by registry ID or configured
  item tag.
- Currency offerings consume carried physical currency only. Banked currency
  must be withdrawn first and is not charged directly by Shrine contributions.
- The server caps requests to remaining progress, validates Shrine range/world,
  persists progress and donation records, then sends a fresh snapshot.
- Requirement icons render at the standard 16px item size inside fixed-height
  rows. Reward slots remain bounded and expose native item/enchantment tooltips.
- History rows center the contributor nickname and offered amount. Contributor
  names use their Realm color, currency uses the violet-blue Sigil accent from
  the banking UI, and ordinary item labels use neutral gray.
- Failed persistence restores items or physical currency through Economy's
  audited refund path.
- Rewards use Core reward definitions and deterministic deferred grants.
- Offline eligible recipients receive grants on next login.
- Event requirements remain externally credited through registered actions.
- Right-clicking an unlinked Shrine gives OP setup guidance, or a neutral
  “not awakened” message for non-OP players.

Modeling dimensions:

- Width: 2 blocks = 32 pixels.
- Depth: 2 blocks = 32 pixels.
- Height: 5 blocks = 80 pixels.
- Total footprint: 32 px x 32 px.
- Total volume envelope: 32 px x 80 px x 32 px.

New Realm project instances use readable IDs such as:

```text
offering_realm_oak_1
offering_realm_oak_2
```

## Registry Actions

Offerings registers:

- `elarion:offering_add_event`
- `elarion:offering_start_realm_project`
- `elarion:offering_start_global_project`
- `elarion:offering_complete_project`
- `elarion:offering_set_display_name`

These are intended for future NPCs, quests, rewards, and milestone pipelines.

## Performance Contract

- Definitions load into immutable caches.
- Shared UI themes synchronize on join and successful Core reload.
- Shrine lists render bounded visible rows.
- Requirement/history scroll positions survive authoritative snapshot refresh.
- Mouse wheel, scrollbar dragging, track clicks, keyboard selection, and Enter
  are supported for contribution rows.
- Gameplay/admin actions do not parse YAML.
- Runtime state uses atomic JSON writes.
- Completed projects resume missing milestone processing after restart.
- Reward grants use deterministic IDs. Non-reward milestone handlers should be
  idempotent before stronger automatic retry semantics are enabled.
- No per-tick global project scans exist in V1.
- Character Menu Offering score reads the bounded `offerings_score` player
  stat; profile snapshot creation must not scan project instances or donation
  records.
- Player-facing rich views must use bounded summaries or indexes before they
  become large-scale UI surfaces.
