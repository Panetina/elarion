# TODO

Only approved, unfinished execution work. `PLAN.md` contains the roadmap;
completed evidence belongs in Git and `docs/ai/archive/`.

## P0.0 — Protect and Integrate Current WIP

1. Preserve the recovery snapshot and classify the dirty worktree into focused,
   independently testable commits: Guilds, Backpacks, Chat, Government,
   NPC quest markers, World Reset, and Core UI/docs.
2. Run focused builds/tests for each slice before staging it. A failed slice
   stays protected locally and is not mixed into a different commit.
3. After all slices are verified, run the cross-module build, GameTests and a
   local dedicated-server startup/shutdown; then push the verified commits.
4. Keep `CURRENT_STATUS.md`, `INDEX.md`, addon/system docs and wiki pages true
   to code as each slice is accepted.

## Next: P0.1 — Release-Safe Modpack

1. Finish and test `WORLD-01` as an executor-bound, recoverable managed-world
   reset. It remains separate from ordinary player reset.
2. Verify Backpacks + Trinkets on client and dedicated server: no crafting,
   smithing or dyeing; only the four Creative/NPC-acquirable items.
3. Complete the Guild foundation: typed Registrar request, physical-inventory
   Sigil fees, `G` entry/empty state, invitation modal, `/e guild` admin path,
   themed UI cleanup, and hierarchy-safe persistence/tests.
4. Complete `CHAT-01`: overlay above chat, server-authoritative Local/Realm/
   Guild/Alliance eligibility, and Global hidden/rejected until the canonical
   Worldheart-portal projection allows it.
5. Finish Government tax/heraldry and NPC quest-marker WIP with migration and
   restart tests; investigate the zero-requirement Shrine projection at its
   actual owner.
6. Perform the remaining local QA: bridge dedicated server, YOSBR seed-once,
   reset preservation, Realm tablist headers, Underworld/banishment, character
   lifecycle, and component-safe grave recovery.
7. Audit hashes and references for staging releases `20260718-005248` and
   `20260718-005342`; remove them only after the audit proves they are unused.

## Explicitly Deferred

Angling remains release-gated off until its vertical parity and restart tests
pass. P1–P3 work is scheduled only in `PLAN.md` and must not be pulled forward
without completing the active release-safety gate.
