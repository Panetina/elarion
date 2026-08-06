# INDEX

Canonical repository dictionary, ownership map, and documentation index.

## Authority Order

When sources disagree, use this precedence and investigate the drift:

1. Current source and tests establish implemented behavior.
2. `RULES.md` establishes permanent engineering policy.
3. `AGENTS.md` establishes repository workflow and bounded context rules.
4. Authoritative system/addon docs establish accepted domain contracts.
5. `TODO.md` lists active incomplete work; `PLANS.md` contains future design.

Start with `RULES.md`, `AGENTS.md`, and
`docs/ai/CURRENT_STATUS.md`. Select task context through
`dev/tools/ai-context.ps1` or `docs/ai/routes.json`; do not load this entire
index when a route already answers the task.

## Root Documents

- `README.md`: project entry and build/export overview.
- `RULES.md`: permanent policy and documentation maintenance matrix.
- `AGENTS.md`: AI/engineering workflow, ownership summary, and context budget.
- `TODO.md`: active implementation and verification work only.
- `PLANS.md`: future ideas and design directions only.
- `LORE.md`: setting/lore entry point.

Completed work, superseded plans, and old verification narratives live in Git
history, not parallel Markdown archives or completion reports.

## Repository Map

- `platform/core`: canonical shared truth and infrastructure.
- `addons/*`: independently owned domain extensions.
- `tests/gametest`: cross-module Fabric GameTests.
- `config/elarion`: editable typed definitions and defaults.
- `distribution`: pinned third-party artifacts and managed export config.
- `dev`: development runtime, QA helpers, export/deployment tools, and tracked
  runtime resource packs.
- `docs/systems`: cross-domain contracts and shared technical systems.
- `docs/addons`: source-backed addon ownership/status contracts.
- `docs/architecture`: repository structure and dependency boundaries.
- `docs/fabric-reference`: local Fabric 1.21.1 implementation reference.
- `docs/porting`: focused NeoForge-to-Fabric mapping guidance.
- `docs/neoforge-reference`: upstream-loader concepts used only for explicit
  porting comparisons.
- `docs/ui`: shared UI contracts and approved visual references.
- `wiki`: player/admin-facing documentation.
- `external` and `addons/angling/reference`: excluded local reference inputs.

## Canonical Ownership

- Core: citizens, Realms, identity, titles, relationships, rewards, history,
  permissions, server identity, shared UI, notifications, queues, storage
  primitives, config descriptors, networking foundations, and bridge outboxes.
- Economy: currency, balances, treasuries, transactions, prices, tax, and
  Economy action adapters.
- NPCs: NPC definitions/placements, dialogue, relationships, story state,
  portraits, service UI, catalogs, and NPC-owned trade state.
- Quests: quest definitions, objectives, consequences, and quest runtime state.
- Offerings: Shrines, Offering projects, donations, progress, and milestones.
- Government: civic blocks, forms, votes, offices, records, and authority UI.
- Guilds: public guild state, invites, roles/tags, and guild chat.
- Portals: gates, routes, tickets, fields, prompts, and return entitlements.
- Worlds: managed worlds, borders, abundance, protection, and processed chunks.
- Realms: Realm protection behavior; Realm identity remains Core-owned.
- Underworld: death capture, corpses/graves, recovery, sessions, Soul
  Fractures, and Core True Death handoff.
- Mounts: native rideable mounts, Collection projection, control, and rendering.
- Atlas: current client shell; future bounded map/discovery projections.
- Names and Titles: identity/title presentation hooks.
- Optimization and Security: diagnostics/evidence, never duplicate domain state.
- Angling: fishing runtime; Core retains accepted catches, metrics, rewards,
  titles, rankings, and reusable events.
- Jail, Newspapers, Tablist, and Voice Chat Hooks: shells until source and
  their addon docs state otherwise.

Cross-domain access uses Core APIs/events or explicit addon APIs. No addon may
read another domain's storage or import its internal managers/network payloads.

## Technical Documentation

Core references:

- `docs/api.md`: public API contracts.
- `docs/config.md`: typed config/descriptors and reload behavior.
- `docs/commands.md`: command surface and permissions.
- `docs/history.md`: History storage and query contract.
- `docs/performance.md`: current operational performance risks and rules.
- `docs/test-commands.md`: technical QA commands.

Primary cross-system contracts:

- Architecture: `docs/architecture/PROJECT_STRUCTURE.md` and
  `docs/architecture/DEPENDENCY_GRAPH.md`.
- Persistence/networking/UI: `docs/systems/Persistence.md`,
  `docs/systems/Networking.md`, and `docs/systems/GUI.md`.
- Characters/permissions: `docs/systems/Characters.md` and
  `docs/systems/Permissions.md`.
- History: `docs/systems/Chronicles.md` and `docs/systems/Metrics.md`.
- External platform/release: `docs/systems/MinecraftBridge.md`,
  `docs/systems/Distribution.md`, and `docs/systems/LiveDeployment.md`.
- Domain systems: `docs/systems/NPCs.md`, `Quests.md`, `Government.md`,
  `Guilds.md`, `Portals.md`, `Underworld.md`, `Realms.md`, `Treasury.md`,
  `CommunityContribution.md`, `CatchTelemetry.md`, and `Atlas.md`.
- Extension rules: `docs/systems/EXTENSION_GUIDE.md` and
  `docs/systems/PLACEHOLDERS.md`.

`docs/addons/README.md` indexes per-addon technical contracts. The system docs
describe cross-domain behavior; addon docs describe owner-local source,
config/state, commands, packets, events, notifications, and current status.
They are complementary, not competing sources.

## Human Documentation

- `wiki/README.md`: published documentation entry.
- `wiki/admin/README.md`: setup and operator guides.
- `wiki/admin/commands.md`: administrator command reference.
- `wiki/admin/test-commands.md`: administrator QA guide.
- `wiki/addons/README.md`: human-readable addon status.
- `wiki/players/README.md`: player-facing entry.

The wiki explains operation and gameplay. It must not redefine technical
ownership, persistence, or packet contracts.

## Verification Entry Points

Use Java 21 and the narrowest applicable route command first.

```text
.\gradlew.bat :platform:core:test
.\gradlew.bat :addons:<owner>:test
.\gradlew.bat :tests:gametest:runGameTest
.\gradlew.bat verifyAiContext
.\gradlew.bat build
```

Distribution and release commands are documented only in
`docs/systems/Distribution.md` and `docs/systems/LiveDeployment.md`. Live
promotion and destructive world operations require their explicit approvals.

## Documentation Rules

- Do not create Markdown islands, dated completion logs, parallel TODOs, or
  report-only contracts.
- Update the owner addon doc and affected cross-system/wiki doc in the same
  behavior slice according to `RULES.md`.
- Keep future behavior explicitly marked and outside `TODO.md` unless it is an
  active implementation item.
- Use Git history for recovery and past evidence.
- Ignore `external/**` and `addons/angling/reference/**` during ordinary work;
  use them only for an explicitly requested comparison/port.
