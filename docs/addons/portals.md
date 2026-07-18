# Elarion Portals

Last reviewed: 2026-07-08

## Purpose

The Portals addon owns linked travel gates. Each route joins `a_gate` and
`b_gate`, with cross-travel landing at the opposite side's configured arrival.
Portals also owns the Character Menu portal-journey summary projection for
successful authoritative player portal travel.

- `scheduled_ticketed` routes consume one physical ticket on outbound travel
  and grant one durable return entitlement.
- `fee_passage` routes charge outbound passage only. Payment consumes carried
  physical currency only; banked currency must be withdrawn before travel.
  Every successful outbound passage stores one return passage; entering
  the linked return gate consumes that stored return without another fee.
  Routes require a progression unlock and may also grant the first
  outbound-and-return cycle for free.
- `always_open` routes need no lock, schedule window, ticket, or entitlement.
  Their source and destination may be `*`, allowing both endpoints and arrivals
  to be configured in any loaded world. The default Neutral Gate uses `*` to
  `*`.

## Definitions And State

Editable definitions:

```text
config/elarion/addons/portals/routes.yml
config/elarion/addons/portals/ui.yml
```

Portals registers a read-only config descriptor domain named `portals` through
`ElarionApi.system().configs()`. The descriptors read the active
`PortalDefinitionService` route/UI snapshots for route IDs, modes,
source/destination dimensions, Economy price keys, schedule settings, visual
settings, and prompt UI sizing. Admin Panel discovery does not parse config
files. Config editing, travel behavior changes, schedule evaluation changes,
Economy price integration changes, packet changes, and persistence changes
remain future approved slices.

Route display names and descriptions may use Core identity placeholders plus
runtime Realm presentation placeholders:

```text
%realm_display%
%realm_official%
%realm_tag%
```

Realm placeholders resolve when snapshots, notifications, and travel prompts
are built. For example, an Ancient Gate route owned by `realm1` can render
`Kingdom of Oak` without rewriting the stable route ID or runtime state.
Default Ancient Gate routes connect each Realm world (`elarion:realm_world_1`,
`elarion:realm_world_2`, `elarion:realm_world_3`) to `elarion:worldheart`;
`elarion:lobby` is not a Realm Ancient Gate destination.
Default scheduled Nether and End gates also depart from `elarion:worldheart`
so Realm players converge at Worldheart before using major progression gates.
The default Neutral Gate is intentionally unrestricted (`source-dimension: "*"`
and `destination-dimension: "*"`) so it can be opened from anywhere and linked
to anywhere by an administrator.

Mutable state:

```text
world/elarion/addon-state/portals/state.json
```

This runtime file stores admin-selected `a_gate`/`b_gate` cuboids,
`a_arrival`/`b_arrival` positions, forced windows, return entitlements, and
first-free-passage state. Locations intentionally do not belong in editable
`routes.yml`.

The Character Menu portal-journey count is maintained separately as the Core
player-stat key `portal_journeys`. `PortalRouteService` increments it only
after server-authoritative travel succeeds, payment/ticket/return state has
been handled, and the travel event is recorded. Existing travel history is not
backfilled, and profile snapshot creation must not scan Portal state or history
records to derive totals.

Source:

```text
addons/portals/src/main/java/panetina/elarion/addons/portals/
```

## Geometry

An endpoint is an inclusive cuboid one block thick on exactly one axis.
Selections thin on multiple axes are rejected because their render orientation
is ambiguous. Portal fields use a centered 4/16 outline and no collision.

The player-built frame is not scanned. Activation validates only selected
interior cells and replaces air or replaceable blocks in bounded Core
server-thread tasks.

## Travel Contract

Travel model:

1. Enter `a_gate` and arrive at `b_arrival`.
2. Enter `b_gate` and arrive at `a_arrival`.

Outbound:

1. The route must be unlocked, complete, and inside an active schedule window.
2. The player must still be inside the source region.
3. One matching physical ticket is consumed.
4. A one-use return entitlement is persisted before teleport.
5. Failed teleport restores the ticket and previous entitlement state.

Ticketed return:

1. The linked route must be active.
2. The player must be inside the return region.
3. The matching entitlement must exist.
4. The entitlement is removed only after successful return.

Fee-passage return:

1. Entering `a_gate` charges the configured fee unless the first free trip is
   still available.
2. Successful outbound travel stores one return passage.
3. Entering `b_gate` consumes the stored return passage and does not charge
   currency.
4. Return without a stored passage is rejected instead of silently charging.

Vanilla Nether and End dimension changes without Portal authorization are
rejected. Administrators use `/e portal endpoint set <route> a_gate`,
`a_arrival`, `b_gate`, and `b_arrival` to complete the linkage, with
`/e portal setup enter <route> [x y z]` and `/e portal setup return` available
when side B lives in a protected or unloaded destination world.

For UI verification, OP4 administrators can use
`/e portal preview <neutral|nether|end|fee|blocked|return>` to open a local
representative Portal Confirmation prompt. Preview prompts do not mutate route
state, grant entitlement, or bypass travel authority; clicking `Yes` still
sends the ordinary server-validated travel request.

## Performance

- Endpoints are indexed by world and intersecting chunk.
- Online players are checked every five server ticks only against endpoints in
  their current chunk.
- Field placement/removal is queued through the bounded Core server queue.
- Structures are not scanned per tick.
- Visual snapshots synchronize only on join and route transitions/edits.
- Character Menu portal journey count reads the bounded `portal_journeys`
  player stat; profile snapshot creation must not scan Portal runtime state or
  history records.
- Route status synchronizes opening/closing timestamps once. The client renders
  unlocked scheduled routes as compact HUD icons, greyed while closed and
  colored while open, with countdown details calculated locally on hover.
  Portal slots use the same responsive rail scale as Core notifications. While
  a route is open, its colored bottom bar shrinks from full to empty across the
  active window without additional server packets.
  The closed overlay renders above item icons so the state remains visible.
  Locked routes remain invisible. Client route/visual caches clear on
  disconnect to avoid stale state between servers. Future Atlas consumers
  should reuse the same snapshots instead of polling the server.
- Scheduled transitions emit API events and Chronicle records but do not
  broadcast opening, closing, or countdown warnings in chat.
- Unlocking an Ancient Gate publishes one dismissible Realm notification to
  that route's Realm. Unlocking a scheduled Nether/End route publishes one
  dismissible World notification to citizens of globally connected Realms.
  Scheduled open/close transitions intentionally do not create drawer cards.

Portals emits one Core domain event per authoritative lifecycle transition:
`portal-route-unlocked`, `portal-route-locked`, `portal-window-opened`, and
`portal-window-closed`. Events contain compact route, mode, Realm, actor, and
state metadata. Notifications remain explicit projections and are not produced
automatically by the event bus.

`PortalChronicleText` registers with Core public history and renders
`route-unlocked` Chronicle projections through the shared template-family
contract. The `portal.route-unlocked` family has 10 authored stable variants,
requires route metadata, honors persisted `chronicle.variant` values, and falls
back safely when older records lack route context. This renderer changes only
player-facing Chronicle wording; route state, schedules, tickets, payments, and
travel authority remain owned by Portals.

`PortalProfileContributor` registers with Core's `CitizenProfileService` and
contributes the reserved `portals/journeys` Ledger slot with `SELF` visibility
by reading `portal_journeys`. This is personal self/admin profile data, not
public profile data.

`visual.status-icon-item` configures the live HUD icon independently from the
ticket/prompt icon. Defaults use Netherrack for Nether and End Stone for End.

Physical portal tickets use one registered item ID, `elarion:portal_ticket`.
The item stores the stable route ticket ID in component NBT and derives its
custom model data from that ID for dimension-specific art: Nether tickets use
custom model data `1` and the crimson stele icon, while End tickets use custom
model data `2` and the blue stele icon. Unknown ticket IDs currently fall back
to the base crimson ticket model. NPC trade previews and real purchased tickets
should use the same item stack path instead of hard-coded GUI-only ticket art.
Inventory item models use local portals item textures copied from the approved
stele assets so item model stitching does not depend on GUI-library texture
paths. Ticket custom names and lore explicitly disable Minecraft's default
italic styling so Nether/End tickets read like ordinary named service items.

The client travel confirmation prompt extends Core `ElarionScreen` and uses
the shared Core civic shell, body, icon-frame, and action-button helpers. The
screen remains presentation-only: it renders the server-authored prompt
snapshot and sends only the existing typed travel confirmation request. The
prompt payload carries an explicit cost kind (`free`, `ticket`, or `fee`) so
the client never infers payment-slot visibility or ticket/currency art from
localized requirement text. Fee prompts render the shared Sigil currency icon
inside the framed slot; ticket prompts render Nether/End ticket art; free
prompts omit the payment slot.

Metrics include `portal-field-queued`, `portal-field-placed`,
`portal-field-removed`, `portal-field-failed`, `portal-field-queue-full`,
`portal-field-obstructed`, and `portal-travel`.

## Ownership

Portals owns routes, endpoints, schedules, ticket identity, entitlements,
free-passage state, transition events, and travel. Economy owns ticket and
passage prices, payments, and refunds. Offerings can unlock routes through
registered actions. NPCs may present purchase actions but do not own tickets or
route state.

## Passenger And Entity Contract

The current player-only teleport path must be extended before mounted travel is
declared complete.

- Players must eventually cross while mounted on horses, pigs, camels, future
  Elarion mounts such as dragons, and backported Happy Ghasts.
- The complete passenger graph must transfer atomically and preserve rider,
  vehicle, ownership, and passenger relationships.
- Tamed pets should follow through a bounded, ownership-aware transfer path
  after the rider arrives.
- Projectiles, falling or piston-pushed blocks, and other non-player moving
  entities must never trigger portal travel.
- Detection must stay limited to active indexed endpoints, without global
  per-tick entity scans.

## Structural Cleanup Notes

Phase 13 classified `PortalRouteService` before extraction. It remains the
canonical owner of live portal route state, entitlements, free passages, and
travel authority. `PortalEndpointIndex`, `PortalFieldController`, and
`PortalStateMigration` remain valid helper boundaries. Route administration is
now isolated in `PortalRouteAdminMutator`, schedule and field lifecycle in
`PortalScheduleReconciler`, and bounded endpoint-entry prompts in
`PortalPlayerPromptDetector`. `PortalTravelExecutor` now coordinates tested
ticket/payment rollback and successful-only state/history/stat changes, while
`PortalWorldTravelGuard` owns transient setup and movement authorization. All
remain behind public `PortalRouteService` facades and none owns a second copy
of `PortalState`. See `docs/reports/PHASE_13_PORTAL_EXTRACTIONS.md`.
