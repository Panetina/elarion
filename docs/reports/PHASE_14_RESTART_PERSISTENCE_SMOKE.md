# Phase 14 Restart And Persistence Smoke

Date: 2026-07-18

## Scope

This report records the backup-first Phase 14 controlled restart smoke against
the existing dev runtime. It verifies representative owner state through a
real server stop/start cycle without resetting the dev world or introducing
test-only production APIs.

## Safety

- Confirmed no Elarion Minecraft server process was running before mutation.
- Copied all 28 files under `dev/run/world/elarion` to the timestamped evidence
  backup before starting the server.
- Used empty Realm 3 for reversible Government and Offering markers.
- Used owner commands for every mutation and cleanup.
- Kept the backup after success; no direct live-state file replacement was
  needed.

Generated evidence is under
`build/phase14/restart-smoke-20260718-011616/` and includes the baseline hash
manifest, three server logs, owner checkpoints, restart assertions, cleanup
assertions, and the complete backup.

## Markers

The first server run created these reversible markers:

- Economy: Realm 3 treasury `0 -> 17` sigils.
- Government: Realm 3 active form `blank -> republic`.
- Offerings: `offering_realm_realm3_1`, a Realm 3 Council Hall instance.

Portal used an unchanged snapshot because the neutral route is
`always_open` and correctly rejects explicit unlocking, while the configured
Realm route is not fully linked. NPC, Underworld, and Character lifecycle state
were intentionally observed without synthetic player mutations.

## Restart Result

After a clean stop and second server start:

- Realm 3 treasury loaded with exactly 17 sigils.
- Realm 3 Government loaded the Republic form.
- the Council Hall instance loaded with its stable ID, Realm, project, level,
  and progress;
- Portal state remained byte-for-byte unchanged;
- both NPC placements reappeared with stable handles and locations;
- Underworld state remained byte-for-byte unchanged;
- Character lifecycle state remained byte-for-byte unchanged;
- Core History advanced from 228 to 244 records;
- Economy journal advanced from one to two records, with no replay duplicate;
- all verification runs contained zero error-level log entries.

## Cleanup Result

A third clean server run removed the markers through owner commands:

- Realm 3 treasury returned to zero.
- Realm 3 Government reset to its exact baseline file.
- the temporary Offering was deleted and the Offering file returned exactly to
  baseline.
- Portal, Underworld, and Character lifecycle files remained exactly at their
  baseline hashes.
- NPC placement identity, definition, jurisdiction, world, and coordinates
  remained semantically identical; entity UUID refresh is allowed during NPC
  reconciliation.

Economy sequence and Core audit/history records intentionally remain advanced;
they are the append-only evidence of the smoke and cleanup transactions.

## Performance Observation

The first run logged one bounded Elarion warning: `server-queue-apply` took
about 1.43 seconds while startup managed-world work was being applied. It did
not recur as an error or prevent readiness. Keep it as a profiling observation,
not a correctness failure.

## Remaining Boundary

This slice does not prove client registry/payload parity, fresh onboarding,
multiplayer authorization/resynchronization, optional-addon absence, or live UI
layout. Those remain separate Phase 14 slices.
