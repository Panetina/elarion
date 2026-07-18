# Phase 14 Verification Matrix

Date: 2026-07-18

## Purpose

This matrix is the execution contract for final Elarion verification. A green
unit build is not treated as evidence for dedicated startup, restart behavior,
optional-addon absence, multiplayer authority, live UI layout, or deployment.
Each row has a separate pass condition and evidence location.

Status values:

- `PASS`: current evidence exists from this Phase 14 baseline.
- `PARTIAL`: automated coverage exists, but required runtime evidence is open.
- `PENDING`: not executed in Phase 14 yet.
- `BLOCKED`: cannot execute without a named external prerequisite.

## Baseline Matrix

| Area | Command or procedure | Status | Current evidence | Final pass condition |
| --- | --- | --- | --- | --- |
| Full compile/unit build | `./gradlew.bat build --console=plain` | PASS | 2026-07-18: 189 actionable tasks, build successful | All modules compile and all unit tests pass |
| AI context/docs routing | `./gradlew.bat verifyAiContext` | PASS | 12/12 benchmark cases; 95.97% aggregate savings | Routes, bounded context, refusal gates, and links pass |
| Release preparation | `./gradlew.bat prepareLiveServerRelease --console=plain` | PASS | 192 actionable tasks, build successful | Verification and exact exports complete without mutation of live server |
| Export composition | Inspect `build/export/server-mods` and `client-mods` | PASS | 42 server jars, 43 client jars, no duplicate names; only client-only jar is `modmenu-11.0.3.jar` | Server/client sets match declared distribution ownership |
| Portal travel atomicity | `:addons:portals:test` through full build | PASS | Ticket rollback, payment refund, entitlement/free-pass transitions, restrictions, journey accounting | All focused Portal tests pass |
| GameTest server | `./gradlew.bat runGameTests --console=plain` | PASS | 2026-07-18: all 1 required tests passed; broad Core/addon command and service scenario completed | Headless GameTest server exits successfully with every registered test passing |
| Command integration | Expand `tests/gametest` where Minecraft context is required | PARTIAL | Broad GameTest covers Core, Economy, Offerings, NPC, Portal, Groups, and Government admin paths; player-context lifecycle/multiplayer paths remain open | Critical Core, Government, Portal, lifecycle, and admin command paths have permission/help/execution evidence |
| Dedicated server startup | `./gradlew.bat runServer` in a visible terminal | PASS | 2026-07-18: reached `Done (1.770s)`, opened six managed worlds, logged zero errors, and saved all dimensions on stop | Server reaches `Done`, addon initialization completes, and logs contain no fatal/error contract violations |
| Dedicated restart | Controlled stop/start using backed-up dev state | PASS | 2026-07-18: backed up 28 runtime files; Economy, Government, Offering markers survived restart; Core/NPC/Portal/Underworld/Character snapshots verified; cleanup passed | Representative persisted Core/Economy/Government/NPC/Portal/Offering/Underworld state survives restart |
| Config reload and Admin apply | `/e reload` plus Admin Panel validation/apply checks | PARTIAL | Descriptor, malformed reload, rollback, and apply tests pass | Live server rejects malformed values without replacing valid runtime state; approved values synchronize |
| Optional-addon absence | `dev/tools/optional-addon-qa.ps1` | PASS | Core-only, NPCs without Economy, and Voice Chat Hooks without a provider reached `Done`, stabilized, and stopped in isolated runtimes; shipped banker fallback defect was corrected | Core and documented optional consumers start with providers absent and degrade safely |
| Client/server mod parity | Start server plus generic/stable clients | PASS | Slice 5 hash manifest: shared gameplay jars match; generic and both stable clients joined without registry/payload mismatch | Generic, Client One, and Client Two join without registry/payload mismatch |
| Generic onboarding client | `./gradlew.bat runClient` | PASS | `Player745` completed blank-prefill creation, balanced `Wilderness II` assignment, and confirm-only living-world teleport | Fresh identity reaches character creation, Realm assignment, and living-world entry correctly |
| Multiplayer authority | Client One plus Client Two | PARTIAL | Client Two lacked `/e`; Client One authorized mutation resynced immediately and was reverted. Concurrent clients hit native `glfw.dll` crashes on this QA host | Unauthorized requests reject; authorized state changes resynchronize both clients in one stable concurrent run |
| Persistence/replay | Focused storage tests plus controlled restart | PASS | Unit round-trip/replay tests plus controlled restart: Economy journal advanced once before restart, no replay duplicate, and reversible owner state reloaded exactly | No duplicated rewards/payments, lost state, stale projections, or unsafe fallback after restart |
| UI screenshot QA | `minecraft-qa.ps1` and family-specific helpers | PASS | Fresh Character/Admin/notification/Portal evidence plus indexed accepted family captures in `PHASE_14_UI_RESOURCE_QA.md` | Required screens pass representative GUI/font scales with no overlap, clipping, inaccessible actions, or blank assets |
| Resource packs and assets | Client startup log plus representative visual checks | PASS | Complete Excalibured art pack plus separate font pack; zero client error/missing-resource matches; idempotent activation in all client profiles | No missing/incompatible resource errors; required Elarion packs and font pack load in declared order |
| Live deployment preparation | `prepareLiveServerRelease` plus `deploy-live-server.ps1 -PlanOnly` | PASS | 42-jar manifest and stage/commit/rollback batches generated without network after guard correction | Release plan preserves staging, backup-before-promote, and rollback without mutating live state |
| Live deployment execution | `deployLiveServerMods` with both explicit safety properties | BLOCKED | Deliberately not run | User confirms release, live server is stopped, backup succeeds, upload/promote succeeds, startup is manually verified |
| Final docs/extension guides | Source/document consistency audit | PASS | `EXTENSION_GUIDE.md`, final reports, indexes, UI journal/audit, deployment docs, and bounded handoff updated | Guides cover config, Admin pages, UI, placeholders, profiles, Chronicles, NPC actions, notifications, and addon integration |

## Ordered Remaining Slices

1. **Slices 2-3 complete** (`Medium`): GameTest and dedicated startup evidence
   is recorded in `PHASE_14_RUNTIME_BASELINE.md`.
2. **Slice 4 complete** (`High`): backup-first restart and owner persistence
   evidence is recorded in `PHASE_14_RESTART_PERSISTENCE_SMOKE.md`.
3. **Slice 5 complete** (`High`): parity and onboarding pass; authority
   rejection and authorized client resync pass. Simultaneous dual-window
   observation is partial due native GLFW host crashes. Evidence is in
   `PHASE_14_CLIENT_AUTHORITY_QA.md`.
4. **Slice 6 complete** (`High`): isolated Core-only, NPC-without-Economy, and
   Voice Chat Hooks cases pass. Evidence is in
   `PHASE_14_OPTIONAL_ADDON_ABSENCE_QA.md`.
5. **Slice 7 complete** (`High`): representative UI/resource evidence and the
   complete art/font pack repair are recorded in `PHASE_14_UI_RESOURCE_QA.md`.
6. **Slice 8 complete** (`Medium`): safe deployment planning and extension
   documentation are recorded in `PHASE_14_DEPLOYMENT_MAINTENANCE_QA.md`.
7. **Slice 9 complete** (`Medium`): final build, GameTests, release preparation,
   export inspection, docs/context checks, and consistency audit pass. See
   `PHASE_14_COMPLETION.md`.

Phase 14 is complete. `PARTIAL` and `BLOCKED` rows above are explicit external
or post-revamp residuals, not hidden release claims.

## Safety Rules

- Never run live deployment merely to test the command.
- Never reset or delete the dev world for startup verification.
- Stop server/client processes before cleaning Fabric remap locks or rebuilding
  jars they may hold.
- Back up state before restart or migration smoke tests.
- Keep screenshot QA separate from functional authority/persistence evidence.
- Classify failures before editing; do not turn verification into unrelated
  cleanup.
