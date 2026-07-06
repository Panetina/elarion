# Portals

Purpose: linked travel between configured source worlds and Worldheart,
Nether, End, or future dimensions. Routes may be scheduled/ticketed,
Economy-fee passages, or always-open.

Main classes: `PortalRouteService`, `PortalDefinitionService`,
`PortalFieldBlock`, `PortalTicketItem`, `PortalCommands`,
`ElarionPortalsApi`.

Entry point: `ElarionPortalsAddon`.

Commands: `/e portal ...`. `/e portal guide <route>` shows the ordered
`a_gate/a_arrival/b_gate/b_arrival` setup workflow. `/e portal setup enter
<route> [x y z]` is the authorized OP path into a protected destination before
its gate is linked.

Network packets: travel prompt, travel confirmation, screen close, compact
route visual synchronization, and compact route-status/timer synchronization.

GUI/screens: `PortalConfirmationScreen`, built from the shared Core UI
foundation. Client route-status icons render as notification-rail accessories
through Core's `ElarionHudOverlayRegistry` and
`ElarionNotificationHud.accessoryAnchor()`.

Storage/persistence: `world/elarion/addon-state/portals/state.json`.

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
route definitions, and shared confirmation UI.

Risks: addon action-registration order for custom startup NPC dialogue,
real-world timezone changes, obstructed endpoint interiors, and future
relationship/Government access policy integration. Mounted cross-dimension
travel is not complete until whole passenger graphs and owned pets can transfer
without duplication or loss. Projectiles and moving blocks must remain
ineligible.

Do not duplicate this system by creating: a second portal schedule manager,
separate return-token storage, direct cross-addon teleport commands, or another
ticket payment ledger.
