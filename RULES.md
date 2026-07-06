# RULES

Permanent project policy for Elarion.

## Source Of Truth

- Fabric 1.21.1 is the target platform.
- Core owns canonical truth: citizens, Realms, titles, identity, rewards,
  history, permissions, and shared infrastructure.
- Addons may extend behavior, but they must not duplicate Core state.
- Config lives under `config/elarion/`.
- Runtime state lives under `world/elarion/`.

## Architecture

- Prefer existing systems before creating new ones.
- Prefer bounded, event-driven, cacheable work over polling or global scans.
- Prefer explicit ownership boundaries over shared global logic.
- Prefer small, local changes that fit the existing architecture.
- Prefer stable extension points that future addons can reuse.

## Events And Notifications

- Every existing addon, and every new addon before it is considered complete,
  must review its meaningful player-facing lifecycle events for notifications.
- Addons own event meaning and authoritative validation. Core owns notification
  persistence, audiences, actions, synchronization, and the shared HUD.
- Meaningful state changes must expose a stable Core domain event through
  `ElarionApi.system().events()` when future addons, Chronicle, newspapers,
  NPC rumors, the website bridge, or diagnostics may need to react.
- Notifications are explicit projections of domain events. Do not
  automatically turn every event into a notification.
- Use Personal for private outcomes, Realm for Realm-scoped civic events,
  World for globally relevant unlocked events, and Quest for quest/task events.
- Do not notify routine transactions, repeated progress ticks, UI browsing,
  ordinary dialogue choices, or low-level diagnostics.
- Do not create addon-owned inboxes, notification stores, HUD rails, polling
  loops, or direct reads of another addon's runtime files.
- Notification actions remain server-authoritative, deduplicated, bounded, and
  persistent when the action must survive logout or restart.

## Documentation

- `INDEX.md` is the navigation entry point.
- `TODO.md` is current implementation work only.
- `PLAN.md` is the short read-order / project memory file.
- `PLANS.md` is future ideas and design direction.
- `LORE.md` is the root lore summary.
- `OPTIMIZATION_TRACKER.md` is the active optimization health tracker.
- `docs/ai/CURRENT_STATUS.md` is the compact current-state handoff for new
  AI/new PC recovery.
- `docs/ai/AI_SEARCH_HINTS.md` is the targeted source lookup guide.
- `docs/addons/<addon>.md` is the addon technical contract.
- `docs/systems/<system>.md` is the cross-addon system contract.
- `wiki/` is the human-readable admin/player manual.

## Documentation Maintenance Matrix

- Ownership, architecture, or source-map changes: update `INDEX.md`,
  `AGENTS.md`, `CODEX.md`, the relevant `docs/systems/*.md`, and
  `docs/addons/*.md`.
- Commands or test commands: update `docs/commands.md`,
  `docs/test-commands.md` when relevant, `wiki/admin/commands.md`, and the
  affected admin wiki page.
- Config, runtime state, APIs, packets, UI behavior, permissions, events, or
  notifications: update the affected addon/system docs and wiki page.
- Any new or changed parsed config, config-backed content, addon definition
  file, or Core definition map must update or add the matching read-only
  config descriptors, descriptor tests, `docs/config.md`, and affected addon
  docs in the same slice unless the data is generated-only and not parsed into
  a typed runtime snapshot. Generated-only YAML must be documented as not ready
  for truthful descriptor exposure.
- New addon or changed addon status: update `AGENTS.md`, `INDEX.md`,
  `CODEX.md`, `docs/addons/README.md`, `wiki/addons/README.md`, and relevant
  wiki navigation.
- Current work goes in `TODO.md`; future design goes in `PLANS.md`; do not mix
  them.

## Change Rules

- Update docs when ownership, commands, configs, APIs, or behavior change.
- Do not add duplicate managers, registries, services, or screens.
- Do not expose raw storage paths as the long-term player-facing interface for
  large history-style systems.
- Do not use client-trusted mutations for gameplay state.
