# Phase 13 PortalRouteService Classification

Date: 2026-07-12

## Scope

Phase 13 Slice 1 classified `PortalRouteService` before any deletion,
extraction, or behavior change. No production code was changed in this slice.

## Source Inspected

- `addons/portals/src/main/java/panetina/elarion/addons/portals/service/PortalRouteService.java`
- `addons/portals/src/main/java/panetina/elarion/addons/portals/service/PortalEndpointIndex.java`
- `addons/portals/src/main/java/panetina/elarion/addons/portals/service/PortalFieldController.java`
- `addons/portals/src/main/java/panetina/elarion/addons/portals/service/PortalStateMigration.java`
- `addons/portals/src/main/java/panetina/elarion/addons/portals/service/PortalProfileContributor.java`
- `addons/portals/src/main/java/panetina/elarion/addons/portals/api/ElarionPortalsApi.java`
- `docs/addons/portals.md`
- `docs/systems/Portals.md`
- `OPTIMIZATION_TRACKER.md`

## Current Shape

`PortalRouteService` is about 960 lines and is still the canonical service for
Portal route runtime state. It currently owns or coordinates lifecycle binding,
reload, snapshots, admin endpoint/arrival mutation, lock/unlock/forced windows,
schedule ticking, field activation, player endpoint detection, prompt
construction, travel execution, ticket consumption, return entitlements,
fee/free-passage payment flow, setup travel, unauthorized Nether/End travel
rejection, field reconciliation, route state listeners, domain events/history,
route text formatting, and the portal journey stat.

Existing valid helper boundaries:

- `PortalEndpointIndex`: chunk-indexed endpoint lookup.
- `PortalFieldController`: queued portal field placement/removal and interior
  validation.
- `PortalStateMigration`: legacy route-id compatibility adapter.

## Classification

| Candidate | Classification | Reason |
| --- | --- | --- |
| Route state map, entitlements, free passages, dirty save | Canonical implementation | `PortalRouteService` currently owns live mutable `PortalState`; do not duplicate this state elsewhere. |
| `PortalEndpointIndex` | Canonical helper | Already extracted and bounded by world/chunk. Keep as-is. |
| `PortalFieldController` | Canonical helper | Already extracted and bounded through Core task queue. Keep as-is. |
| `PortalStateMigration` | Required compatibility adapter | Legacy route migration should remain isolated until compatibility is explicitly ended. |
| Snapshot construction | Domain-specific implementation, possible extraction later | Pure projection from definitions/state/time, but uses route text formatting and activity calculation. |
| Admin endpoint/arrival/link mutation | Strong extraction candidate | Cohesive command/admin responsibility: validate selected worlds, overlap, interior, mutate endpoints/arrivals, save, index, reconcile, record history. |
| Lock/unlock/force schedule commands | Strong extraction candidate | Cohesive admin state-transition responsibility with notifications/events/history. |
| Schedule ticking/open-close transitions | Strong extraction candidate | Cohesive periodic responsibility: window evaluation, field activation/deactivation, obstruction diagnostics, visual sync. |
| Player endpoint detection and prompt construction | Strong extraction candidate | Cohesive runtime detection responsibility; depends on endpoint index, restrictions, Economy quote, ticket presence, and prompt sender. |
| Travel execution | Strong extraction candidate, highest risk | Mutates player inventory, Economy payments/refunds, entitlements/free passages, teleport authorization, stats, history. Extract only with focused tests. |
| Setup travel and unauthorized world-change rejection | Strong extraction candidate | Cohesive world-movement guard/setup responsibility. Must preserve authorized-world-change semantics. |
| `record`, `emitRouteDomainEvent`, route text formatting | Possible shared support | Repeated support logic for admin/travel/schedule. Extract only after larger behaviors stop depending on private methods. |
| `PortalRouteService.TravelPrompt` and `TravelResult` | Required public/API adapter | `ElarionPortalsApi` exposes `TravelResult`. Rename/move only with API migration approval. |
| `PORTAL_JOURNEYS_STAT` | Required public/profile contract | Used by `PortalProfileContributor`; keep stable. |

## Recommended Extraction Order

1. **Route Admin Mutator**: extract endpoint/arrival/remove/lock/unlock/force-open/force-close behind existing `PortalRouteService` facade methods.
2. **Schedule Reconciler**: extract `tickSchedules`, `reconcileFields`, `reconcileRoute`, obstruction logging, and possibly route-state event dispatch.
3. **Player Detector And Prompt Builder**: extract `tickPlayers` and prompt construction, keeping endpoint detection bounded.
4. **Travel Executor**: extract travel/payment/ticket/free-passage logic only after focused tests cover ticket restoration, payment refund, entitlement consumption, restrictions, and journey stats.
5. **Setup/World-Guard Coordinator**: extract setup destination/return and unauthorized Nether/End world-change rejection.

## Anti-Patterns To Avoid

- Do not create a second `PortalState` owner.
- Do not copy entitlements or free-passage maps into separate managers.
- Do not expose mutable `PortalState` through a public API.
- Do not move route-specific payment decisions into Economy.
- Do not move portal field placement into a tick loop; keep task-queue based
  bounded placement/removal.
- Do not remove `PortalStateMigration` until save compatibility is explicitly
  ended.

## Next Slice Proposal

Phase 13 Slice 2 should extract only the route admin mutator behind the existing
`PortalRouteService` public methods.

Classification: Medium.

Expected files:

- `PortalRouteService.java`
- new `PortalRouteAdminMutator.java`
- focused portal service tests if current coverage is insufficient
- docs/status files

Verification:

```text
.\gradlew.bat :addons:portals:test --console=plain
```

No persistence schema, config format, packet, UI, Economy, or Core API changes
should be included.

## Completion Update

Slices 2-6 completed all five extraction candidates on 2026-07-18. See
`docs/reports/PHASE_13_PORTAL_EXTRACTIONS.md` and
`docs/reports/PHASE_13_COMPLETION.md`. Travel atomicity and rollback behavior
now have focused tests.
