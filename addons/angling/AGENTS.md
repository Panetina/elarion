# Elarion Engineering Instructions

These instructions apply to every change in this repository.

## Required Reading

Before editing implementation code, read:

1. `RULES.md` for permanent policy and prohibited changes.
2. `INDEX.md` for ownership, locations, and extension points.
3. `TODO.md` for current work.
4. `PLANS.md` for the stable phased port plan.
5. `REPLACE.md` when touching assets, names, text, content, or UI.

## Working Contract

- Read the repository before changing it.
- Keep changes small, local, testable, and aligned with existing ownership.
- Core owns canonical state. Addons and integrations extend Core through APIs
  and events; they do not duplicate state.
- Use data-driven definitions, explicit registries, reload-safe validation,
  bounded queries, caches, and event-driven work.
- Do not add per-tick global scans, unbounded history reads, repeated parsing,
  synchronous heavy IO, or broad object churn in gameplay paths.
- Keep editable definitions in config or data resources. Keep runtime state in
  world storage with explicit schema versions and migration rules.
- Keep client-only code out of dedicated-server classloading paths.
- Use Fabric APIs and Minecraft abstractions. Do not preserve NeoForge
  architecture merely because the upstream implementation used it.
- Do not copy upstream creative assets or content identity.
- Update code, tests, `INDEX.md`, and the relevant policy/work document in the
  same change.
- Run the policy check before considering a change complete:
  `powershell -NoProfile -ExecutionPolicy Bypass -File
  scripts/check-project-policy.ps1`.

## Documentation Ownership

- `RULES.md`: permanent design, architecture, legal, and quality policy.
- `INDEX.md`: project dictionary, ownership map, and code location index.
- `TODO.md`: current implementation work only.
- `PLANS.md`: stable future phases and design direction.
- `REPLACE.md`: manual replacement inventory and content status.

If implementation and documentation disagree, stop and correct both. Do not
silently establish a second source of truth.
