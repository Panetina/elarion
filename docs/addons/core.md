# Core Contract

Last reviewed: 2026-06-15

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
- shared registries
- shared UI theme and notification HUD rail
- canonical data exported to the web/bridge layer
- active-citizen recency truth
- durable claimable reward grants and delivery receipts
- notification categories and notification snapshot/claim payloads
- durable notification storage, audience snapshots, and action dispatch

## Public API

Use grouped facades from `ElarionApi` for new work:

- identity
- realm
- messaging
- progression
- system

## Runtime State

```text
world/elarion/citizens/
world/elarion/history/
world/elarion/player-stats/
world/elarion/progression/
world/elarion/title-claims.json
world/elarion/reward-grants.json
world/elarion/notifications/notifications.json
world/elarion/addon-state/realms/
```

`config/elarion/core/activity.yml` controls the default active-citizen recency
window. Citizen records persist `lastSeenAt`; online players are active and
offline players remain active until the configured window expires.

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
citizens of pre-global Realms do not receive World entries. Quests is a
placeholder for future accepted, assigned, and random quests.

The drawer uses a compact brown/gold Minecraft-style notification center. The
left HUD icons are the only category selectors: envelope for Personal, Realm
icon for Realm, World icon for global events, and Quest icon for Quests. The panel is narrow/tall with a
vertical virtualized card list, no top text tab row, no visible scrollbar, and
no close button. It closes through ESC or the inventory key.

Reward cards can expand in-place to show compact item/currency preview icons
with count overlays.

`/e realm reward`, `/e realm give`, and Offering/Shrine reward milestones queue
claimable reward notifications. Rewards are inserted or paid only after the
player presses Claim in the Personal drawer.

`/e realm mail <realm> <title> <message>` creates a persistent Realm
notification from the admin instead of sending chat text. Realm announcements,
Government founding results, and Offering/Shrine level changes also publish
Realm notification entries. Realm mail/news remain until the recipient presses
Dismiss.

Future Quest systems must publish quest entries through Core notification APIs
instead of creating a separate HUD stack. Quest gameplay, quest storage, and
quest commands are not implemented in Core.

## Notification Event Matrix

| Publisher | Category | Current events |
|---|---|---|
| Core rewards | Personal/Reward | claimable Realm grants and Offering rewards |
| Core Realm delivery | Realm | admin mail and Realm announcements |
| Titles | Personal and World | all grants/revokes are Personal; constrained title grants are also World announcements |
| Groups | Personal | invitation, acceptance, kick, leadership transfer, deletion |
| Government | Realm/Government | proposal/vote windows, runoffs, results, office changes |
| Realm governance | Realm/Government | relationship decisions with Approve/Reject and final results |
| Offerings | Realm or World | configured milestone notices and Shrine progression notices |
| Portals | Realm or World | Ancient Gate unlocks are Realm; scheduled Nether/End unlocks are World |
| Quests | Quest | reserved; no gameplay publisher yet |

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
