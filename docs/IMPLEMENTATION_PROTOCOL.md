# Elarion Codex Implementation Protocol

1. Work on exactly one named task ID per run.
2. Never begin later roadmap tasks automatically.
3. Read:
   - PLAN.md
   - RULES.md
   - INDEX.md
   - TODO.md
   - the single focused task document
4. Do not read all documentation or map the whole repository.
5. Use git ls-files and targeted rg searches to locate existing ownership.
6. Inspect git status before editing and preserve unrelated work.
7. Reuse existing services, APIs, persistence, events, UI components and tests.
8. Do not create parallel identity, messaging, economy, notification or persistence systems.
9. Core owns canonical cross-addon truth. An addon owns its feature-specific state.
10. Server authority is mandatory for inventories, permissions, currencies, roles,
    taxes, world access, teleportation and relationships.
11. No global per-tick player/entity/world scans.
12. No synchronous disk IO on the server thread.
13. Use event-driven updates, indexed lookups, bounded queues and dirty-state saves.
14. No broad refactors, formatting passes or unrelated cleanup.
15. Do not introduce abstractions without at least two real consumers.
16. Run focused tests first. Run the full build once after focused tests pass.
17. Update only documentation affected by the actual change.
18. If the expected code is in another repository, stop and identify the correct repo.
19. If a requirement conflicts with existing code, report the conflict instead of guessing.
20. Final response must contain only:
    - behavior completed;
    - files changed;
    - tests/build result;
    - unresolved risks.