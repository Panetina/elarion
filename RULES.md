# RULES

Permanent Elarion policy. Keep this file compact; subsystem detail belongs in
authoritative docs selected through `docs/ai/routes.json`.

## Platform and Ownership

- Fabric 1.21.1 is the runtime and implementation target. NeoForge loader code
  never ships. The owner-authorized local Angling reference is an approved
  port source: its code, content, and edited resources may be adapted into the
  Fabric Angling modules, while its raw checkout and build metadata remain
  excluded from jars.
- Core owns canonical citizens, Realms, titles, identity, relationships,
  rewards, history, permissions, server identity, shared UI, task queues, and
  infrastructure.
- Addons may extend behavior but must not duplicate Core or another addon's
  state. Integrate through public APIs, registries, events, and bounded
  projections.
- Editable definitions live under `config/elarion/`; mutable runtime state lives
  under `world/elarion/`.
- Fabric remains canonical for game state. The website, launcher, Discord bot,
  and bridge may consume signed bounded projections or submit typed commands,
  but must not duplicate or directly mutate Core/addon-owned truth.

## Architecture and Performance

- Search existing services, registries, payloads, UI primitives, storage, and
  APIs before adding infrastructure.
- Prefer small, local, data-driven, event-driven, cacheable changes with clear
  ownership.
- Avoid per-tick global scans, repeated parsing, broad history/state loads, and
  heavy IO or allocation in hot paths.
- Player-facing history/search views must use dedicated bounded indexes or
  summaries, never raw JSONL scans as the long-term query path.
- Treat client packets as requests only; the server validates authorization,
  context, values, and mutations.
- Use queued/batched writes, lazy loading, and explicit cache invalidation when
  data can grow.
- Core owns shared placeholder contracts, bounded resolution, visibility,
  aliases, and diagnostics. Addons retain canonical domain values and expose
  them only through registered side-effect-free resolvers.
- Placeholder rendering must not perform storage/history/world/player scans,
  IO, writes, mutation, network calls, or broad parsing. Resolution must bound
  placeholder count, output length, nesting, cycles, memoization, and
  diagnostics while preserving unknown tokens safely.

## Config and Persistence

- Parse editable config into typed immutable snapshots, validate on
  startup/reload, and keep reload behavior explicit.
- Every parsed config domain or definition map change must update its read-only
  descriptors, descriptor tests, `docs/config.md`, and affected addon docs.
- Generated-only YAML is not active descriptor truth until a typed loader owns
  it.
- Persistence changes require round-trip, reload, and restart coverage as
  applicable.

## Events and Notifications

- Addon owners validate event meaning. Core owns shared event delivery,
  notification persistence, audiences, actions, synchronization, and HUD.
- Emit reusable Core domain events for meaningful lifecycle changes that future
  addons or public-history consumers may need. Notifications are explicit,
  selective projections; do not notify routine actions or diagnostics.
- Chronicle/library-visible event families require ten authored stable
  variants, metadata requirements, deterministic selection, fallback text, and
  tests before promotion.

## Quality and Documentation

- Make minimal focused edits, preserve unrelated user changes, and use
  `apply_patch` for manual changes.
- Commands require registration, permission, help/suggestion, and execution
  coverage. Player-facing changes update docs and tests together.
- Ownership/architecture changes: update `INDEX.md`, `AGENTS.md`, `CODEX.md`,
  and affected system/addon docs.
- Command changes: update `docs/commands.md`, relevant test-command docs, and
  `wiki/admin/commands.md`.
- Config, persistence, API, packet, UI, permission, event, or notification
  changes: update affected system/addon docs and the relevant wiki page.
- Addon status changes: update the root source maps, addon indexes, and wiki
  addon status.
- `TODO.md` contains unfinished work; `PLAN.md` current direction;
  `PLANS.md` future ideas. Historical completion logs belong only under
  `docs/ai/archive/`.

## Verification

- Run the narrowest meaningful module tests first; run the full build for
  cross-module changes or final handoff.
- Export only the changed scope: after editing one mod, build that mod and copy
  only its changed JAR/resources/config into the matching client/server export
  root. Do not run a whole-pack export for a single-mod change, because it
  marks unrelated files as changed and obscures the upload set. Record and
  hash-verify the changed export files. A full export is reserved for explicit
  release requests, distribution-manifest changes, or cross-module releases.
- Performance changes need diagnostics or tests proving work is bounded.
- Architecture changes require explicit justification and synchronized docs.
- Live-server promotion requires a verified canonical export, an explicit owner
  confirmation that the server is stopped, a remote backup, and post-start log
  verification. Deployment tooling must never embed passwords or bridge secrets.
