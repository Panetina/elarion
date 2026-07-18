# Phase 13 Portal Service Extractions

Date: 2026-07-18

## Completed Slices

Phase 13 Slices 2-6 extracted all five classified responsibilities from
`PortalRouteService` without changing its public facade, persistence schema,
packets, commands, route configuration, Economy behavior, or travel behavior.

- `PortalRouteAdminMutator` owns endpoint/arrival administration, overlap
  validation, remove, lock/unlock, and forced-window mutations. It receives the
  current `PortalState` for each operation and does not own canonical state.
- `PortalScheduleReconciler` owns schedule evaluation, field reconciliation,
  obstruction deduplication, and route-state publication. Its transient maps
  are diagnostics/publication guards, not persisted route state.
- `PortalPlayerPromptDetector` owns chunk-indexed gate-entry detection,
  entry deduplication, and read-only prompt construction. It does not consume
  tickets, debit currency, mutate entitlements, or teleport players.
- `PortalTravelExecutor` owns travel orchestration behind generic, testable
  effects. It preserves ticket restoration, payment refunds, entitlement and
  free-passage transitions, and successful-only history/stat accounting.
- `PortalWorldTravelGuard` owns transient setup origins, last-known positions,
  teleport authorization, and restricted Nether/End world-change rejection.

`PortalRouteService` remains the sole live owner of `PortalState`, dirty-save
coordination, entitlements, free passages, travel authority, and the public API
facade. Helpers receive narrow effects rather than storage or another service
locator.

## Exact Production Files

- `addons/portals/src/main/java/panetina/elarion/addons/portals/service/PortalRouteService.java`
- `addons/portals/src/main/java/panetina/elarion/addons/portals/service/PortalRouteAdminMutator.java`
- `addons/portals/src/main/java/panetina/elarion/addons/portals/service/PortalScheduleReconciler.java`
- `addons/portals/src/main/java/panetina/elarion/addons/portals/service/PortalPlayerPromptDetector.java`
- `addons/portals/src/main/java/panetina/elarion/addons/portals/service/PortalTravelExecutor.java`
- `addons/portals/src/main/java/panetina/elarion/addons/portals/service/PortalWorldTravelGuard.java`

## Tests

- `PortalRouteAdminMutatorTest` covers same-world overlap rejection and valid
  endpoint replacement/cross-world reuse.
- `PortalScheduleReconcilerTest` covers always-open, unlock/window, forced-open,
  and forced-closed decisions.
- `PortalTravelExecutorTest` covers ticket rollback, payment refund, successful
  entitlement/free-passage transitions, restrictions, and journey accounting.
- `PortalWorldTravelGuardTest` covers restricted dimension classification.
- `./gradlew.bat :addons:portals:test --console=plain` passed after each
  extraction; final result was successful with 10 actionable tasks.

## Result

All classified Portal boundaries are extracted. No helper owns a duplicate
`PortalState`, and the stable `PortalRouteService` API remains unchanged.
