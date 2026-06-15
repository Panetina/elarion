# PLAN

Short project memory and current read order.

## Current Focus

1. Read `AGENTS.md`.
2. Read `CODEX.md`.
3. Use `INDEX.md` to reach the source-of-truth docs.
4. Work in the existing subsystem first instead of inventing a new one.
5. Before adding new systems, clear the current audit findings in `TODO.md`.

## Current Reality

- Fabric 1.21.1 is the target platform.
- Core owns canonical truth: citizens, Realms, titles, identity, history,
  rewards, permissions, and shared infrastructure.
- Addons extend behavior, but they do not duplicate Core state.
- `addons/offerings` owns Shrine / Offering gameplay.
- `addons/government` owns the civic flow and authority backend.
- `addons/groups` owns public groups and Confederation eligibility hooks.
- `addons/npcs` owns static NPCs and dialogue.
- `addons/portals` owns linked scheduled portals and tickets.
- Current build status: `.\gradlew.bat build` passes.
- Current audit status: no duplicate canonical systems found. Government UI
  actions are now tied to tested server-issued block sessions; remaining
  Government work is command/GameTest coverage and vote lifecycle polish.

## Next Work Style

- Make small, focused edits.
- Prefer bounded event-driven work over global scans.
- Update docs and tests when behavior changes.
- Keep runtime state in `world/elarion/` and editable definitions in
  `config/elarion/`.
- Treat client packets as requests only; server-side context must prove the
  player is allowed to mutate state.
