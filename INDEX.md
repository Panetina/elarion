# INDEX

Repository dictionary and navigation map. Source code decides implementation
reality; this file identifies the owner and authoritative document.

## Read Order

1. `RULES.md` — permanent engineering policy.
2. `AGENTS.md` — repository workflow, ownership map, and context limits.
3. `docs/ai/CURRENT_STATUS.md` — bounded current handoff.
4. `TODO.md` — actionable unfinished work.
5. `PLAN.md` — active direction and concise future constraints.
6. The system/addon document selected by `docs/ai/routes.json`.

`README.md` is the product overview. `LORE.md` is established canon. Historical
material under `docs/ai/archive/` is explicit recovery-only context; Git holds
completed implementation evidence.

## Ownership

| Owner | Canonical responsibility |
| --- | --- |
| `platform/core` | Shared truth/infrastructure: citizens, identity, Realms, lifecycle, permissions, history, UI (including player-context action routing), storage, queues, APIs and networking. |
| `addons/economy` | Currency, balances, treasury, transactions, pricing and tax. |
| `addons/npcs` | NPC definitions/placements, dialogue, relationship/story state and NPC services. |
| `addons/quests` | Quest definitions and runtime progress. |
| `addons/offerings` | Shrines, projects, donations, milestones and rewards. |
| `addons/government` | Civic blocks, forms, votes, offices and civic records. |
| `addons/guilds` | Guild membership, invitations, roles, tags and guild chat. |
| `addons/portals` | Gates, routes, tickets, fields and returns. |
| `addons/worlds` | Managed worlds, borders, abundance and world protection. |
| `addons/realms` | Realm protection behavior; Realm identity remains Core-owned. |
| `addons/underworld` | Death sessions, graves, recovery, Soul Fractures and Core True Death handoff. |
| `addons/mounts` | Native mounts, Collection projection, input and rendering. |
| `addons/atlas` | Client-only map shell; future rendering and exploration projections. |
| `addons/backpacks` | Acquisition policy and compatibility guard for Yyz's Backpack and Trinkets; upstream owns backpack contents/UI and Trinkets owns accessory slots. |
| `vendor/yyzsbackpack` | Pinned MIT upstream source, assets, bundled compatibility module, and dedicated-server source-separation patch. |
| `addons/names`, `addons/titles` | Identity and title presentation. |
| `addons/angling`, `addons/optimization`, `addons/security` | Fishing foundation, diagnostics, and evidence/security foundations. |

Core/addon dependencies are one-way. Cross-domain consumers use Core APIs,
events, or explicit addon APIs; they never read another owner's storage.

## Source And Contract Map

| Need | Start here |
| --- | --- |
| Source layout/dependencies | `docs/architecture/PROJECT_STRUCTURE.md`, `docs/architecture/DEPENDENCY_GRAPH.md` |
| Task routing/current state | `docs/ai/routes.json`, `docs/ai/CURRENT_STATUS.md` |
| Configuration | `docs/config.md`, owner config/descriptors/tests |
| Persistence | `docs/systems/Persistence.md`, owner `storage/` package |
| Networking | `docs/systems/Networking.md`, owner `network/` package |
| UI | `docs/systems/GUI.md`, Core `client/ui` then owner client package |
| Commands | `docs/commands.md`, owner `command/` package |
| Chronicle/history | `docs/systems/Chronicles.md`, Core public-history APIs |
| Distribution/launcher export | `distribution/mods.json`, `docs/systems/Distribution.md` |
| Live deployment | `docs/systems/LiveDeployment.md` |
| Addon contracts | `docs/addons/README.md` then `docs/addons/<owner>.md` |
| Wiki/admin operation | `wiki/README.md`, `wiki/admin/` |

## Documentation Rules

- Update the owning system/addon document and affected wiki page with any
  public behavior, command, config, persistence, permission, UI, event, or
  notification change.
- Keep detailed implementation evidence in `docs/reports/`; do not duplicate
  it in root navigation, TODO, or PLAN files.
- Do not create a new root Markdown authority without adding a clear purpose to
  this index and removing overlapping material first.
