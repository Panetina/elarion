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

## Change Rules

- Update docs when ownership, commands, configs, APIs, or behavior change.
- Do not add duplicate managers, registries, services, or screens.
- Do not expose raw storage paths as the long-term player-facing interface for
  large history-style systems.
- Do not use client-trusted mutations for gameplay state.
