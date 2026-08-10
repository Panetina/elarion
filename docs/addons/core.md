# Core Contract

Last reviewed: 2026-07-07

Author: Panyel  
Team: Panetina Team

## Owns

- citizens
- Realm definitions and membership
- nicknames
- titles and active title
- abilities
- identity
- base Realm relationships and hiding state
- rewards
- history and progression events
- player stats
- task queues
- root-command extension registration and global player-interaction restriction gates
- shared registries
- read-only config descriptor registry
- shared UI theme and notification HUD rail
- modular Collection menu shell, Pets placeholder tab, and Core-owned Titles
  collection tab
- Character Menu profile aggregation boundary, Core identity/Realm/active-title
  profile projection, and server-side profile visibility filtering
- Admin Panel shell, `/e panel`, packets, and provider registry for OP testing
  and repair actions
- canonical data exported to the web/bridge layer
- outbound signed website whitelist synchronization, durable sequence cursor,
  and pending acknowledgement recovery
- active-citizen recency truth
- durable claimable reward grants and delivery receipts
- notification categories and notification snapshot/claim payloads
- durable notification storage, audience snapshots, and action dispatch
- canonical account-to-character lifecycle, dead-character archives, reserved
  RP names, and restart-safe True Death reset coordination
- Worldheart governing authority state and domain permission checks; the
  Economy addon owns the Worldheart treasury balance, not Core

## Public API

Use grouped facades from `ElarionApi` for new work:

- identity
- realm
- messaging
- progression
- system

`ElarionApi.system().configs()` exposes the server-side read-only
`ElarionConfigRegistry`. Current registered domains include `core`, with
UI theme, server identity, Realm definition, title/title-progression
definition, reward definition, citizen/activity, chat, identity/nickname, and
history descriptors backed by the current validated `CoreConfigManager`
snapshot; `guilds`, backed by the
current validated Guilds config snapshot; `economy`, backed by the current
validated Economy transaction config and service-price snapshots; and `worlds`,
backed by the current validated Worlds manager snapshot; and `portals`, backed
by current Portal route and UI definition snapshots; and `offerings`, backed
by current Offering project and Shrine UI definition snapshots; and
`government`, backed by current Government settings and form definition
snapshots; and `npcs`, backed by current NPC definition, visual profile,
dialogue summary, and UI snapshots; and `quests`, backed by current quest
package metadata and graph summaries; and `realms`, backed by the loaded Realm
protection config snapshot; and `mounts`, backed by loaded Collection row and
detail text; and `underworld`, backed by the active Underworld service config
snapshot; and `optimization`, backed by Core-owned task settings surfaced by
the Optimization addon. Addons should register future domains through this
registry instead of adding separate discovery systems.

`ElarionApi.system().commands()` accepts both `/e` subcommands and intentionally
rare top-level commands. Root suppliers remain Core-registered so addons do not
install independent Fabric command callbacks. `ElarionApi.system().restrictions()`
supports live-player restrictions plus UUID-only account restrictions for
pre-entity admission decisions. Core's global block/entity/item/combat gates
run before addon interaction callbacks; domain addons contribute policy but do
not duplicate those gates. `queued_admission` is reserved for the future server
admission queue.

Core also owns config mutation-readiness contracts:
`ElarionConfigChangeRequest`, `ElarionConfigChangeResult`,
`ElarionConfigChangeError`, `ElarionConfigChangeValidator`,
`ElarionConfigApplyRegistrar`, `ElarionConfigApplyRegistry`,
`ElarionConfigApplier`, `ElarionConfigPreparedChange`,
`ElarionConfigApplyCapability`, `ElarionConfigApplyContext`, and
`ElarionConfigApplyReadiness`, plus the non-executable
`ElarionConfigApplyReadinessProvider`. Core also owns config edit transport contracts:
`ElarionConfigEditTarget`,
`ElarionConfigEditControl`, `ElarionConfigEditOpenPayload`,
`ElarionConfigEditRequestPayload`, and `ElarionConfigEditResultPayload`.
These records and codecs model future edit requests, typed control snapshots,
and validation/apply results only. The validator resolves requests against the
descriptor registry, checks permission metadata, parses raw submitted values,
runs entry validators, and detects stale expected-current values. The inert
apply registry can explicitly bind an edit target to capability metadata and
an applier and can report descriptor-aware readiness. Core constructs one
canonical registry and exposes only a method-reference
`ElarionConfigApplyRegistrar` through
`ElarionApi.system().configAppliers()`. Consumers cannot retrieve executable
registrations. Core currently registers one production backend applier for
`core:ui_theme:defaults.font-scale-percent`; addon production appliers do not
exist yet. Admin receives only the executor facade/readiness provider and uses
it for target-specific disabled reasons. The visible Admin Apply button remains
disabled until the UI enablement slice approves click behavior.

The applier contract is transactional: owner code prepares without mutation,
then a future Core coordinator may commit and invoke idempotent rollback if a
later mandatory step fails. Core now contains the unwired coordinator and
structured audit sink/record contracts. It revalidates under a process-global
lock and prepares a write-ahead audit session before owner commit. Session
terminal operations distinguish committed, rolled-back, and failed outcomes.
Core also contains the unbound JSONL audit journal sink with synchronous
append+force and bounded unresolved-tail recovery. `ElarionConfigApplyService`
binds the journal to the active world on server start, routes Admin readiness,
blocks execution if recovery is unsafe, and can delegate backend apply requests
to the coordinator when ready. Admin sees it through
`ElarionConfigApplyExecutor`, not coordinator/registry internals. The first
registered backend target writes `config/elarion/core/ui_theme.yml`, reloads
Core config, and resyncs UI themes. The client Apply control is enabled only
for server-authored apply-available controls with a matching latest validation
result. Config edit controls carry separate input editability and Apply
availability metadata.

`ElarionApi.worldheart()` exposes the Core-owned Worldheart governance service.
It persists current authority as `SYSTEM` or `PLAYER`, defaults missing state
to the lore-facing `Hollow Emperor`, emits a Core domain event on authority
changes, and centralizes checks for server administrator versus current
Worldheart ruler. Future Worldheart blocks, Admin controls, or political
systems must mutate authority through this service and must not treat the
Worldheart treasury as a player wallet.

`ElarionApi.system().profiles()` exposes the Core-owned
`CitizenProfileService`. It can build bounded, server-filtered profile
snapshots for one target citizen at a time. Core contributes identity, Realm,
active-title, and progression summary data; addons may contribute bounded
sections such as Government office roles through explicit contributors. The
profile model consists
of `CitizenProfileRequestContext`, `CitizenProfileSnapshot`,
`CitizenProfileSection`, `CitizenProfileField`, `CitizenProfileCard`,
`ProfileVisibility`, and the future addon extension point
`CitizenProfileContributor`. Addons must not copy profile state into Core or
scan their runtime storage for profile rows. They may register contributors
only after their owning addon has a bounded summary API and explicit
visibility rules.

`CitizenProfileSummaryFields` is the canonical Core contract for stable
Character Menu summary source and field identifiers. Current reserved summary
sources include `progression`, `offerings`, `quests`, `npcs`, `guilds`,
`government`, `underworld`, `portals`, and `history`. Future addon
contributors must use those constants when filling the existing Ledger summary
slots, and must not invent parallel IDs for completed quests, Offering score,
NPC reputation, deaths, portal journeys, milestones, office history, or recent
history.

Core also owns the Character Menu profile request/response packets:
`CitizenProfileRequestPayload` and `CitizenProfileSnapshotPayload`. The server
receiver derives the viewer from the connection, builds snapshots through
`CitizenProfileService`, can narrow the response to one visible section, and
returns only bounded presentation data. `CitizenProfileClientState` caches the
latest client snapshot for the Character Menu Profile tab. Profile clients
remain read-only; no client mutation packet exists.

Character Menu renders the Profile tab from that cached snapshot. The tab
presents server-visible identity, Realm, active title, citizenship, active
Government office role when contributed, visible completed advancement count
from the Progression service, self/admin completed quest count, Offering score,
Portal journey count, and Underworld lifetime death count when contributed,
Core-owned title/ability counts, and the bounded Collection mount count as one
portrait-led civic dossier, not as nested section buttons. The live player-list
skin supplies the head portrait, with a neutral fallback when no player-list
entry is available. NPC reputation and Chronicle summaries remain explicit
empty states until their owners expose bounded summary APIs and visibility
rules.

Character Menu unlockable tabs use the same Option A civic shell. Core renders
icon tabs, unlocked/total completion, six-row hidden scrolling, explicit
active/owned/locked state, provider-owned rank badges, accent-colored
selected/active frames, large preview frames, bounded record text, action
controls, and designed empty states. Providers continue to own entries,
actions, and rank assignment; shared rank colors come from Core
`ElarionCollectionRank`, and Core transports/renders the presentation metadata
without owning addon unlock state. Mount model previews remain Mounts-owned
through the client preview registry.

## Addon Lifecycle

Core registers its server lifecycle bind/stop/tick callbacks before it
initializes custom addon entrypoints. Addon `SERVER_STARTED` callbacks may rely
on Core services such as citizens, titles, notifications, history, and
character lifecycle already being bound to the active server. Addons still own
their feature state and must not duplicate Core-owned truth.

## Runtime State

```text
world/elarion/citizens/
world/elarion/history/
world/elarion/player-stats/
world/elarion/progression/
world/elarion/title-claims.json
world/elarion/reward-grants.json
world/elarion/notifications/notifications.json
world/elarion/core/characters/state.json
world/elarion/core/minecraft-bridge/state.json
world/elarion/addon-state/realms/
```

`config/elarion/core/minecraft-bridge.yml` controls the disabled-by-default
website whitelist bridge. Core starts it only when the explicit configuration
is valid and the server has both online mode and the whitelist enabled. The
bridge performs bounded outbound HTTPS polling and applies mutations only on
the server thread. Its restart-safe state records only entries added by the
bridge, so a manual console whitelist entry remains server-owned and cannot be
removed by a stale website command. See `docs/systems/MinecraftBridge.md`.

Character Lifecycle requires one preservation confirmation from existing
citizens and fresh character creation from new accounts. Its mandatory client
screen waits until no other mod screen is active and never replaces an external
screen. The character name and biography inputs use the shared Core text-input
helper; failed validation preserves the local fields, and the biography box
scrolls internally. New Realm-less characters are automatically placed into the
least-populated starter Realm among `realm1`, `realm2`, and `realm3`, with
random tie-breaking. The Realm-choice panel is currently presentation-only and
keeps future manual selection disabled. Addons register idempotent cleanup handlers through
`ElarionApi.characters()`; they must not create another character manager.

`config/elarion/core/activity.yml` controls the default active-citizen recency
window. Citizen records persist `lastSeenAt`; online players are active and
offline players remain active until the configured window expires.

`config/elarion/core/titles.yml` supports optional `color: "#RRGGBB"` per
title. Missing built-in title colors are migrated for known Core title IDs
without overwriting custom colors. Existing configs with the old shipped
Citizen gold `#D19B42` are migrated to the white-gray Citizen default
`#C9C9C9`. Title colors are used in identity title text and Character Menu
title rows/previews. Explicit colors take precedence; missing colors use the
shared rank palette for known title families, Legendary for otherwise unknown
globally unique titles, and plain white for other unranked custom titles.

Deferred reward grants snapshot their reward actions and use stable grant IDs.
They are claimable through the Core notification drawer instead of being pushed
directly into player inventories. Delivered compact receipts remain durable to
prevent duplicate payment across restart. If an item reward cannot fit in the
player inventory, the claim fails and the grant remains pending.

## Notification HUD

Core owns the left-side notification HUD rail and themed slideout drawer.
Current drawer filters are Personal, Realm, World, and Quests. Personal includes
direct player notifications, mail, and reward claims. Realm includes Realm and
Government entries delivered to a snapshotted Realm audience. World contains
global-stage events such as Nether/End route unlocks and constrained title
claims. The World icon and feed are hidden until the citizen belongs to a Realm
with the Offering-owned `ancient_gate_unlocked` flag. Neutral players and
citizens of pre-global Realms do not receive World entries. Quest-category
entries are published by the Quests addon for meaningful quest outcomes and
reminders.

The drawer uses a compact brown/gold Minecraft-style notification center. The
left HUD icons are the only category selectors: envelope for Personal, Realm
icon for Realm, World icon for global events, and Quest icon for Quests. The panel is narrow/tall with a
vertical virtualized card list, no top text tab row, no visible scrollbar, and
no close button. It closes through ESC or the inventory key.
Cards are ordered newest-first regardless of read state. Marking a card read
does not move it; unread cards use stronger accents in the list in addition to
the rail icon's new-message state.

Core owns ordered HUD composition through `ElarionHudOverlayRegistry`.
Addon status elements render before the notification drawer and foreground
tooltips render afterward. The combined stack is drawn above chat text so chat
cannot obscure notification cards or addon tooltips.

Reward cards can expand in-place to show compact item/currency preview icons
with count overlays.

## Admin Panel

Core owns `/e panel`, the Admin Panel packets, the themed
`ElarionAdminPanelScreen`, and the provider registry exposed through
`ElarionApi.system().adminPanel()`. The panel is OP level 4 only and requires
an in-game player source. Addons contribute rows/actions through providers and
remain authoritative for their own reset or repair behavior.

The V1 tabs are Overview, Players, Systems, Config, Realms, and Danger Zone.
Player tools cover online-player inspection, teleport helpers, Realm
assignment, nickname edits, title/ability edits, character lifecycle repair,
and addon-contributed player actions such as mount grants or Underworld
cleanup. Single-field player actions can carry server-authored Tab completion
suggestions; current Core suggestions cover Realm IDs, title IDs, and
registered abilities, while addon providers can attach scoped suggestions such
as Mount IDs. The client only cycles these supplied values and the server
validates every mutation. Destructive actions use a second click-confirm
modal. `Reset Everything` is runtime-only: it invokes
registered provider resets and must preserve configs, world files, placed
blocks, NPC placements, portal endpoints, and player inventories.

Core also owns the independent `/e reset players` coordinator. The command is
OP level 4, preview-first, executor-bound, and expires its confirmation after
60 seconds. It creates one timestamped backup before invoking domain-owned
handlers. Core's Minecraft handler clears vanilla playerdata, statistics,
advancements, `ops.json`, `whitelist.json`, and the persisted plus in-memory
profile cache; registered Core/addon handlers remove only their player-owned
records below `world/elarion`. Shared runtime/configuration state is never
removed by scanning or deleting the entire Elarion tree.

The completed backup has an atomic `manifest.json` containing only
backup-relative handler targets. It is an operator recovery inventory, not an
automatic restore mechanism.

The Config tab shows read-only config descriptor rows from
`ElarionApi.system().configs()`. It shows domain summary rows, per-category
detail rows, and stable per-entry rows for OP discovery. The tab is scoped for
packet safety: opening Config sends only domain/category summaries, and
selecting a category asks the server for that category's entry rows. Admin
Panel payloads also cap tab, row, action, and suggestion counts before writing
the custom payload. These rows expose descriptor metadata, current/default
values, bounds, choices, reload/restart markers, permissions, and validation
errors. Entry rows expose a preview-only `Validate Value` action that submits
one proposed raw value to the server and returns the
`ElarionConfigChangeValidator` result. It does not write files, apply values,
reload config, emit audit events, change runtime state, add typed edit
controls, change packet schemas, or touch persistence. The Systems tab remains
for provider-owned testing and repair rows.

True editing still requires a dedicated config packet/model, apply service,
audit path, and reload/rollback policy. Do not convert the preview action into
an edit/save path.

The proposed edit protocol is Core-owned and separate from generic Admin Panel
actions. It carries config edit targets, typed control metadata,
expected-current values, proposed raw values, structured result errors,
reload/restart policy, and audit preview text. The packet records/codecs exist,
their payload types are registered, and Core now handles validation-only config
edit requests from OP level 4 admins. Addon config domains remain read-only
until the owning addon registers an explicit reload-safe applier or edit
provider.

Config edit open and result payloads are S2C; config edit requests are C2S.
The C2S receiver OP-gates on the server, delegates to Core Admin/config
dispatch, returns `ElarionConfigEditResultPayload`, refreshes the Admin Panel
message, and can route server-side `APPLY` through the audit-backed apply
executor for registered production backend targets. The visible client sends
Apply only after a matching successful validation result for an apply-available
control. The client registers a result receiver that stores the last
structured result in passive client state and clears it on join/disconnect.
The client also registers an open-payload receiver that stores the current
server-authored edit control and clears stale validation results when a new
control opens. Config entry rows expose `Open Editor`; the server resolves the
selected descriptor and sends a disabled `ElarionConfigEditOpenPayload` with
server-authored input/apply availability. The Admin Panel renders the open
control in a detail shell with Close, descriptor metadata, proposed-value input,
server-side Validate, structured validation-result display, and Apply. Applied
results close the edit shell to avoid stale current values after reload/sync.
The first production applier is limited to
`core:ui_theme:defaults.font-scale-percent`; it writes `ui_theme.yml`,
reloads Core, resyncs theme packets, and rolls back the exact previous file on
failure. Old development files missing the scalar are migrated in place by
inserting it under `defaults`; duplicate `font-scale-percent` lines are
rejected.

`/e realm reward`, `/e realm give`, and Offering/Shrine reward milestones queue
claimable reward notifications. Rewards are inserted or paid only after the
player presses Claim in the Personal drawer.

`/e realm mail <realm> <title> <message>` creates a persistent Realm
notification from the admin instead of sending chat text. Realm announcements,
Government founding results, and Offering/Shrine level changes also publish
Realm notification entries. Realm mail/news remain until the recipient presses
Dismiss.

Quest systems publish quest entries through Core notification APIs instead of
creating a separate HUD stack. Quest gameplay, quest storage, and quest
commands are owned by `addons/quests`, not Core.

## Collection Menu

Core owns the shared Collection menu shell, `/charactermenu`, the `C` key opener,
generic collection networking, the empty Pets placeholder tab, and the Titles
tab. Addons may contribute additional tabs through
`ElarionApi.system().collections()`, but the player-facing mutation remains
server-authoritative through the provider action callback.

Current pinned tab order is Mounts, Pets, then Titles. The Titles tab is backed
by existing `TitleService` and citizen title state; Collection does not own or
duplicate title unlocks, active title, or title persistence.

Government authority titles are normal Core title definitions. The Government
addon temporarily unlocks and activates them while a citizen holds an office,
then revokes the temporary unlock when the rank is gone. Core remains the
source of truth for title definitions, active title storage, and rendering;
Government owns the office-to-title restore pointer in Government runtime state.

## Notification Event Matrix

| Publisher | Category | Current events |
|---|---|---|
| Core rewards | Personal/Reward | claimable Realm grants and Offering rewards |
| Core Realm delivery | Realm | admin mail and Realm announcements |
| Titles | Personal and World | all grants/revokes are Personal; constrained title grants are also World announcements |
| Guilds | Personal | invitation, acceptance, kick, leadership transfer, deletion |
| Government | Realm/Government | proposal/vote windows, runoffs, results, office changes |
| Realm governance | Realm/Government | relationship decisions with Approve/Reject and final results |
| Offerings | Realm or World | configured milestone notices and Shrine progression notices |
| Portals | Realm or World | Ancient Gate unlocks are Realm; scheduled Nether/End unlocks are World |
| Quests | Quest | questline notifications and reminders |

Title announcement policy is ownership-driven:

- `UNLIMITED`: Personal grant/revoke notification only.
- `ONE_PER_PLAYER`: Personal notification plus a World announcement for each
  citizen's first successful unlock.
- `GLOBALLY_UNIQUE`: Personal notification plus one World announcement for
  the successful claimant.

World delivery still requires the recipient's Realm to have global notification
access. Existing unlocks are not replayed when a title definition changes.

Informational notifications expire after 30 days by default. Actionable
notifications remain available until their owning domain action expires or
invalidates them. Realm and World publication snapshots eligible recipients at
publication time; it does not recalculate historical audiences when membership
changes. Core caches only the eligible Realm IDs; Offerings remains the source
of truth for `ancient_gate_unlocked`.

Economy transaction feedback, NPC dialogue, world-management diagnostics,
Optimization, and Security do not publish drawer cards in V1. Those systems
retain their dedicated command/UI feedback until a real player-facing event
requires a notification.

## Domain Event And Notification Contract

`ElarionApi.system().events()` exposes a bounded in-process domain-event stream.
Domain owners emit `ElarionDomainEvent` after authoritative state changes that
future Chronicle, newspaper, NPC rumor, website bridge, diagnostics, or addon
integrations may consume. Events contain stable source/event identifiers,
optional actor/Realm/subject context, a timestamp, and compact metadata.

Domain events are integration signals, not canonical state and not durable
storage. Consumers must query the owning API for current truth when needed.
Listeners must remain fast and must queue expensive IO or computation through
Core task services.

Notifications are explicit player-facing projections of selected domain events.
They are never generated automatically from the whole event stream. Each addon
must document:

- meaningful events it emits
- which events create Personal, Realm, World, or Quest notifications
- deduplication and expiry behavior
- server-authoritative actions and invalidation rules
- events intentionally kept silent to avoid notification spam

New addons must use Core notifications and domain events instead of creating a
second inbox, HUD rail, event poller, or cross-addon runtime-file dependency.

Current stable event identifiers include:

- `title-granted`
- `title-revoked`
- `title.progression-unlocked` Chronicle projections, rendered by
  `CoreChronicleText` through the shared template-family contract with 10
  authored stable variants.
- `portal-route-unlocked`
- `portal-route-locked`
- `portal-window-opened`
- `portal-window-closed`
- `realm-global-access-changed`

The event bus isolates listeners: one failing consumer is logged and cannot
break the authoritative mutation or prevent other listeners from receiving the
event.

## External Bridge Contract

The website/backend scaffold may consume Core-owned identity, Realm, whitelist,
and public-history projections through explicit bridge APIs or sync payloads.

It should receive:

- user account identity snapshots
- whitelist application decisions and status
- Realm membership and access visibility needed for web pages
- Chronicle/news-ready public history summaries

It should not receive:

- direct ownership of citizens or Realms
- raw mutable Core state files
- a second source of truth for history or titles

## Performance Notes

Core services should be event-driven, cache derived lookups only, and invalidate
caches on canonical source changes.
