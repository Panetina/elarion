# Phase 13 Duplicate And Structural Cleanup Completion

Date: 2026-07-18

## Scope And Method

Phase 13 classified production Java source by ownership, duplicate filename,
size, helper role, compatibility role, and shell readiness. It reused the
project audit reports and excluded build output, `external/**`, Angling
reference source, and historical AI archives. Data-driven assets were not
declared unused merely because no Java literal referenced them.

The production Java filename scan found no duplicate filenames. Tiny records,
enums, exceptions, and functional interfaces were reviewed as explicit domain
contracts rather than treated as dead code by line count.

## Completed Structural Cleanup

`PortalRouteService` was the strongest verified extraction target. It remains
the sole canonical owner of live `PortalState` and the stable public facade,
while these responsibilities now have explicit internal owners:

| Helper | Responsibility | State ownership |
| --- | --- | --- |
| `PortalRouteAdminMutator` | Endpoint/arrival administration, overlap validation, lock/unlock, forced windows | Receives current state per operation; owns no canonical copy |
| `PortalScheduleReconciler` | Window evaluation, field lifecycle, obstruction deduplication, route-state publication | Owns transient diagnostics only |
| `PortalPlayerPromptDetector` | Chunk-indexed gate entry and read-only prompt construction | Owns transient occupied-entry keys only |
| `PortalTravelExecutor` | Restriction, ticket/payment rollback, entitlement/free-passage transitions, successful-only history/stats | Mutates service-supplied canonical state through narrow effects |
| `PortalWorldTravelGuard` | Setup origins, teleport authorization, restricted-dimension rejection | Owns transient movement/session guards only |

The service reduced from approximately 960 lines at classification to about
680 lines while preserving its public methods, persistence schema, packets,
commands, configuration, and Economy API contracts.

## Classification Results

| Candidate | Classification | Decision |
| --- | --- | --- |
| Core citizens, Realms, identity, notifications, history, profile/config registries | Canonical implementations | Retain in Core |
| Addon storage and domain services | Intentional domain-specific implementations | Retain with addon owner |
| `PortalStateMigration` and placeholder aliases | Required compatibility layers | Retain until a separately approved compatibility removal |
| Config descriptor classes | Intentional declarative schema catalogs | Do not split by line count alone |
| Small records/enums/interfaces | Stable typed contracts | Retain |
| Shell addons | Intentional foundation modules | Retain in build and label readiness truthfully |
| Government, Underworld, Offering, and Admin large services | Canonical but possible future extraction candidates | No speculative Phase 13 split; require a behavior-specific audit and focused tests |
| Collection, Government, Notification, Shrine, and Admin large screens | Canonical UI shells | No structural split without live layout regression coverage |
| Data-driven textures/models/config assets | Uncertain from static reference search | Do not delete |
| Duplicate production Java filenames | Safe-duplicate scan | None found |

No confirmed duplicate canonical state, duplicate production class, obsolete
adapter, or safely dead asset remained after classification. Therefore no
additional deletion was justified.

## Travel Atomicity Coverage

`PortalTravelExecutorTest` verifies:

- ticket restoration and prior-entitlement restoration after teleport failure;
- successful outbound grant and successful return consumption;
- paid-passage refund after teleport failure;
- no free-passage progression or journey accounting after failure;
- successful fee record before successful journey record;
- payment failure causes no teleport or state mutation;
- restrictions stop all travel side effects.

`PortalWorldTravelGuardTest` pins the restricted-dimension boundary to Nether
and End; Worldheart, Realm worlds, and Overworld are not blocked by this guard.

## Verification

- `./gradlew.bat :addons:portals:test --console=plain` passed with 59 tests.
- `./gradlew.bat build --console=plain` passed with 189 actionable tasks.
- `dev/tools/verify-ai-context.ps1` passed all 12 benchmark cases with 95.96%
  aggregate capsule savings.
- `git diff --check` passed for the Phase 13 source, tests, and documentation.
- No live screenshot QA was required because this phase changed no rendering or
  player-facing layout.

## Deferred Work

The remaining large classes are not automatically Phase 13 debt. Split them
only when a concrete behavior slice establishes an ownership boundary and
tests. Unused asset removal requires a data/resource reachability audit that
understands config and runtime identifiers. Broad builds, GameTests, startup,
restart, optional-addon, and screenshot verification belong to Phase 14.
