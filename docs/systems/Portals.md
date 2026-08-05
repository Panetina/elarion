# Portals

Purpose: linked travel between configured source worlds and Worldheart,
Nether, End, or future dimensions. Default Realm Ancient Gates link Realm
worlds to Worldheart, and default Nether/End scheduled gates depart from
Worldheart. The default Neutral Gate is unrestricted from any configured world
to any configured world. Routes may be scheduled/ticketed, Economy-fee
passages, or always-open.

Main classes: `PortalRouteService`, `PortalDefinitionService`,
`PortalFieldBlock`, `PortalTicketItem`, `PortalCommands`,
`ElarionPortalsApi`, `PortalProfileContributor`, `PortalRouteAdminMutator`,
`PortalScheduleReconciler`, `PortalPlayerPromptDetector`,
`PortalTravelExecutor`, `PortalWorldTravelGuard`.

`PortalRouteService` remains the canonical live route state owner. Admin
mutation, schedule reconciliation, player prompts, travel execution, and
setup/world guards stay behind its public facade and focused portal tests.

Phase 13 completed all five classified extractions. `PortalRouteService` still
owns canonical state and the stable public facade; travel and movement helpers
operate only through narrow effects and transient guards.

Entry point: `ElarionPortalsAddon`.

Commands: `/e portal ...`. `/e portal guide <route>` shows the ordered
`a_gate/a_arrival/b_gate/b_arrival` setup workflow. `/e portal setup enter
<route> [x y z]` is the authorized OP path into a protected destination before
its gate is linked.

Network packets: travel prompt, travel confirmation, screen close, compact
route visual synchronization, and compact route-status/timer synchronization.

GUI/screens: `PortalConfirmationScreen`, built from the shared Core UI
foundation. The travel prompt payload carries server-authored `costKind`
presentation values (`free`, `ticket`, or `fee`), and the client uses that
contract for payment-slot visibility and ticket/currency art instead of
parsing requirement text. Client route-status icons render as notification-rail
accessories through Core's `ElarionHudOverlayRegistry` and
`ElarionNotificationHud.accessoryAnchor()`.

Storage/persistence: `world/elarion/addon-state/portals/state.json`. Supported
snapshots normalize null collections and discard structurally unusable route,
return-entitlement, and free-passage rows before binding. Invalid persisted gate
endpoints are cleared so the route becomes incomplete instead of crashing index
rebuild, while valid rows from the same file remain intact.

Lifecycle: route state binds on `SERVER_STARTED`, then field reconciliation is
queued through Core's server task queue so managed worlds opened by other
startup handlers are available first. A linked route with an unavailable world
stays inactive and is retried by normal reconciliation instead of crashing the
server.

Dependencies: Core and Economy. Offerings consumes Portal actions without
owning route state.

Portal prompts and travel confirmation consume Core `PlayerRestrictionService`
before teleporting. When Character Lifecycle or Underworld blocks travel,
Portals shows the source restriction message instead of a generic denial.

Extension points: config-defined route modes, Economy service-price keys,
route-state transition listeners, registered
unlock/lock/status/ticket-purchase actions, immutable `ElarionPortalsApi`,
route definitions, shared confirmation UI, and the owner-maintained Citizen
Ledger portal-journey profile contributor.

Risks: addon action-registration order for custom startup NPC dialogue,
real-world timezone changes, obstructed endpoint interiors, and future
relationship/Government access policy integration. Mounted cross-dimension
travel is not complete until whole passenger graphs and owned pets can transfer
without duplication or loss. Projectiles and moving blocks must remain
ineligible.

Do not duplicate this system by creating: a second portal schedule manager,
separate return-token storage, direct cross-addon teleport commands, or another
ticket payment ledger.

`PortalProfileContributor` contributes `portals/journeys` with `SELF`
visibility by reading the Core player-stat key `portal_journeys`.
`PortalRouteService` increments that stat only after successful
server-authoritative travel. Existing travel history is not backfilled, and
profile snapshot creation must not scan Portal runtime state or history
records.
