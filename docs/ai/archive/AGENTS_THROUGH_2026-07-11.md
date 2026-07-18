# Elarion Agent Notes

Fabric 1.21.1 is the source of truth and target platform. NeoForge is
reference-only for understanding and porting mods.

This repository is an engineering environment, not a brainstorming sandbox.
Optimize for correctness, modularity, performance, maintainability, and future
live-server scale.

Core owns canonical truth: citizens, Realms, titles, identity, relationships,
rewards, history, permissions, server identity, shared UI primitives, task
queues, and shared infrastructure. Addons may extend behavior, but they must
not duplicate Core state.

Before creating a system, search the repository for an existing service,
registry, payload, UI primitive, command pattern, storage helper, API, or docs
page to extend.

Keep editable definitions in `config/elarion/` and mutable runtime state in
`world/elarion/`. Validate configs on startup/reload and cache immutable
definitions.

Whenever parsed config, config-backed content, addon definitions, or Core
definition maps are added or changed, update the matching read-only config
descriptors and descriptor tests in the same slice. Do not expose generated-only
YAML as active descriptor current values until it is parsed into a typed runtime
snapshot.

Prefer extending current networking, GUI, command, registry, and persistence
patterns. Do not create duplicate managers or invent new architecture unless the
existing system is demonstrably the wrong abstraction.

Prefer data-driven registries, bounded event-driven work, queued/batched IO,
modular systems, and explicit APIs. Avoid global scans, repeated parsing,
hard-coded gameplay names, and client-trusted mutations.

For every existing addon touched and every new addon, identify meaningful
player-facing lifecycle events. Emit reusable Core domain events for
cross-system consumers and publish explicit Core notifications where the event
deserves player attention. Do not create addon-specific inboxes or automatically
notify every event; avoid routine-action and diagnostic spam.

Document new systems in `docs/systems/`. Use `rg` for discovery, `apply_patch`
for manual edits, and update relevant docs when ownership, commands, configs,
networking, UI, or persistence changes.

## Documentation Maintenance

`RULES.md` owns the canonical documentation maintenance matrix. Before ending
an implementation task, check that matrix and update only the docs affected by
the actual ownership, command, config, API, packet, UI, permission, event,
notification, or addon-status change. Keep this file focused on local workflow
and source navigation.

## Markdown And Navigation Index

Root authority and planning:

- `RULES.md`: permanent project policy.
- `AGENTS.md`: local Codex operating rules and repository map.
- `CODEX.md`: quick command/source navigation.
- `INDEX.md`: single project navigation entry point.
- `TODO.md`: current implementation work only.
- `PLAN.md`: short current-focus memory.
- `PLANS.md`: future ideas and design directions.
- `LORE.md`: root lore summary.
- `OPTIMIZATION_TRACKER.md`: active performance/health tracker.
- `README.md`: lightweight public project summary.
- `docs/ai/CURRENT_STATUS.md`: compact current-state handoff for new AI/new PC
  recovery.
- `docs/ai/AI_SEARCH_HINTS.md`: targeted lookup shortcuts.

Human-readable wiki:

- `wiki/README.md`: wiki landing page.
- `wiki/_sidebar.md`: GitHub Pages/sidebar navigation.
- `wiki/admin/*.md`: admin manuals and command/setup pages.
- `wiki/addons/README.md`: addon status table.
- `wiki/players/README.md`: future player-facing guide index.

Technical docs:

- `docs/architecture/`: project structure, dependency graph, knowledge map.
- `docs/systems/`: source-backed system notes.
- `docs/addons/`: addon technical notes.
- `docs/fabric-reference/`: Fabric 1.21.1 development references.
- `docs/neoforge-reference/`: NeoForge reference-only notes.
- `docs/porting/`: NeoForge-to-Fabric mapping notes.
- `docs/reports/`: audit/reference reports.

Addon-local docs:

- `addons/angling/*.md`: Angling-only porting/status notes.
- `addons/worlds/NOTICE.md`: Worlds notice.
- `addons/angling/reference/README.md`: upstream reference marker only.

Do not create new Markdown islands unless they are indexed here, in `INDEX.md`,
or in the wiki.

## Source Map

- `platform/core`: canonical Core systems, Worldheart governing authority,
  character lifecycle/archives,
  shared APIs, read-only config descriptor registry, shared UI, modular Character Menu shell, Citizen-profile aggregation boundary, notification HUD rail/drawer, claimable reward
  notifications, commands, networking, config, storage, identity, rewards,
  history, and task queues.
- `addons/economy`: currency, bank balances, treasuries, transactions, Economy
  pulse, Economy-owned actions.
- `addons/offerings`: Shrine blocks, Offering projects, progress, donations,
  rewards/milestones, Shrine UI.
- `addons/government`: Civic Forum, Seat of Rule, government forms, founding
  votes, office holders, authority chat, authority markers.
- `addons/groups`: public groups, tags, invites, group chat, Confederation
  eligibility hooks.
- `addons/npcs`: static NPCs, dialogue, skins, portraits, NPC UI.
- `addons/quests`: data-driven quest definitions, scoped questline runtime
  state, registered quest actions/conditions, Quest notifications, and
  quest-to-Shrine display projections.
- `addons/portals`: linked A/B gates, tickets, Ancient Gates, portal fields,
  return entitlements.
- `addons/worlds`: managed worlds, borders, abundance/protection rules.
- `addons/realms`: Realm protection behavior.
- `addons/names`: nameplate/tablist identity rendering hooks.
- `addons/titles`: title rendering.
- `addons/optimization`: performance diagnostics and queue visibility.
- `addons/security`: security/evidence foundation.
- `addons/underworld`: death capture, component-safe corpse/grave recovery,
  recovery vaults, Underworld sessions, Soul Fractures, and True Death handoff
  to Core Character Lifecycle.
- `addons/mounts`: native Fabric rideable mount entities, Collection-menu
  unlock/active-mount state, deprecated legacy whistle item ids/icons,
  converted geo assets for all seven V1 mounts rendered through GeckoLib,
  mount input, creative tab registration, and movement profiles.
- `addons/angling`: fishing foundation. Ignore `addons/angling/reference/**`
  unless explicitly resuming Angling porting work.
- `addons/jail`, `addons/newspapers`, `addons/tablist`,
  `addons/voicechat-hooks`: shell/foundation modules unless current source says
  otherwise.

## Angling Reference Rule

`addons/angling/reference/**` is upstream reference material. It is not active
Elarion source and should be ignored for ordinary searches, audits, build
quality decisions, and architecture planning. Only read it when the user
explicitly resumes Angling porting or asks for comparison against the upstream
reference.

## Mandatory Workflow

Before implementing any feature:

1. Read `RULES.md`.
2. Read `AGENTS.md`.
3. Read `CODEX.md`.
4. Read the relevant `docs/systems/*.md` file.
5. Read relevant architecture docs under `docs/architecture/`.
6. Search existing implementations.
7. Search existing networking patterns.
8. Search existing GUI patterns.
9. Search existing persistence patterns.
10. Prefer extending existing systems.
11. Avoid duplicate managers, handlers, registries, controllers, services, screens, or infrastructure.
12. Verify consistency with Fabric 1.21.1.
13. Verify consistency with Elarion architecture.
14. Update documentation when architecture changes.
15. Review meaningful lifecycle events for Core domain-event and notification
    integration, including audience, deduplication, expiry, and action ownership.
16. If the change touches parsed config or config-backed definitions, update
    the read-only descriptor domain, descriptor tests, `docs/config.md`, and
    affected addon docs before ending the slice.

Architecture changes require justification. Documentation must stay synchronized with code. If docs and code disagree, inspect the code and update the docs to match reality.

## Credit-Efficient Work Rule

Split planning, audits, implementation, testing, and documentation into the
smallest useful slices that preserve quality. Avoid re-reading unrelated files,
repeating settled context, or expanding the task beyond the current objective.

Default sequence:

1. Identify the narrow subsystem.
2. Read only the authority docs and source needed for that subsystem.
3. Make the smallest coherent change.
4. Run the narrowest meaningful verification first.
5. Update only the docs affected by the change.
6. Run full build only when the change crosses modules or before final handoff.

This is a token/credit economy rule, not a permission to skip rigor. If a task
needs broader context for correctness, read it; otherwise keep discovery
bounded.
