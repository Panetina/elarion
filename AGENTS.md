# Elarion Agent Notes

This is a Fabric 1.21.1 multi-module engineering repository. Optimize for
correctness, modularity, performance, maintainability, and future live-server
scale. Permanent policy is in `RULES.md`.

## Start Here

1. Read `RULES.md` and the bounded `docs/ai/CURRENT_STATUS.md`.
2. Run `dev/tools/ai-context.ps1` or inspect `docs/ai/routes.json` for the task
   domain.
3. Read only the selected authoritative system/addon docs and current source.
4. Search existing implementations, networking, UI, persistence, and tests
   before creating anything.
5. Preserve the large existing dirty worktree; never revert unrelated changes.

Do not read `docs/ai/archive/**`, `external/**`, or
`addons/angling/reference/**` unless the user explicitly requests historical,
upstream, or Angling-porting research.

For explicitly requested Angling port work, the local reference is the
owner-authorized source for code, content, edited artwork, models, sounds, and
text. Port those files into proper Fabric/Elarion paths; do not package the raw
checkout, NeoForge build metadata, or Git internals. No separate replacement-
asset review is required for files supplied inside the authorized `Modding`
tree.

## Ownership Map

- `platform/core`: canonical shared truth and infrastructure, Worldheart,
  character lifecycle, APIs, config descriptors, shared UI, Character Menu,
  notifications, rewards, history, commands, networking, storage, and queues.
- `addons/economy`: currency, balances, treasuries, transactions, pricing, tax,
  and Economy actions.
- `addons/npcs`: NPC definitions/placements, dialogue, relationship/story state,
  portraits, service screens, and NPC-owned trade state.
- `addons/quests`: data-driven quest definitions and quest runtime state.
- `addons/offerings`: Shrines, Offering projects/progress/donations/milestones.
- `addons/government`: civic blocks, forms, votes, offices, records, and
  authority presentation.
- `addons/guilds`: public guilds, invites, tags, and guild chat.
- `addons/portals`: gates, routes, tickets, fields, and return entitlements.
- `addons/worlds`: managed worlds, borders, abundance, and protection rules.
- `addons/realms`: Realm protection behavior; Realm identity stays Core-owned.
- `addons/underworld`: death capture, graves, recovery, sessions, Soul
  Fractures, and Core True Death handoff.
- `addons/mounts`: native rideable mounts, Collection projection, input, and
  GeckoLib rendering.
- `addons/atlas`: current client-only map shell; future Atlas rendering,
  exploration projections, and personal tracking state.
- `addons/backpacks`: Yyz's Backpack/Trinkets integration and non-crafting
  acquisition policy; it does not duplicate backpack contents or slot state.
- `vendor/yyzsbackpack`: pinned MIT upstream implementation; changes are limited
  to documented compatibility/release policy and must preserve upstream IDs.
- `addons/names`, `addons/titles`: identity/title presentation.
- `addons/optimization`, `addons/security`, `addons/angling`: diagnostics,
  evidence foundation, and fishing foundation.
- Atlas, Jail, Newspapers, Tablist, and Voice Chat Hooks remain shells unless current
  source/docs say otherwise.
- `distribution/mods.json` owns pinned third-party client/server artifacts;
  `docs/systems/Distribution.md` owns export, launcher-manifest, config-update,
  and managed-development synchronization contracts.

## Required Patterns

- Core/addon dependencies are one-way. Never read another domain's storage
  directly or create circular required dependencies.
- Config definitions are typed, validated, cached, reload-safe, and paired with
  truthful descriptors.
- Networking uses typed Fabric payloads; clients send IDs/input and servers
  validate/mutate before returning authoritative snapshots.
- UI reuses Core primitives and theme contracts before adding new frameworks.
- Storage remains domain-owned, atomic, restart-safe, and bounded for normal
  player actions.
- Cross-system consumers use Core APIs/events or explicit addon APIs, not
  duplicated managers.
- Core owns placeholder registry/resolution infrastructure; each addon owns its
  domain values. Placeholder resolvers are bounded, side-effect-free, and may
  not read storage or scan live global state while formatting.

## Workflow

- Use `rg`/`rg --files` for discovery and the repository's deterministic
  signature extractor for compact supporting outlines. Apply production
  changes with `apply_patch`.
- If parsed config changes, update descriptors, descriptor tests,
  `docs/config.md`, and affected addon docs in the same slice.
- Review meaningful lifecycle events and notification audience/deduplication/
  expiry/action ownership for every addon behavior changed.
- Run focused compile/tests during iteration. Run `gradlew build` only when the
  change crosses modules or before final handoff.
- Export scope rule: when a mod, mod resource, or mod-owned config changes,
  build that module and update only its corresponding artifact/config in the
  export roots. Do not run the full `exportMods` task for a single-mod change;
  it rewrites timestamps/metadata for unrelated files and makes upload review
  ambiguous. Keep a changed-file list and verify hashes for the changed files.
  Run the full export only when explicitly requested, when the distribution
  manifest changes, or when a cross-module release requires it.
- Apply the documentation matrix in `RULES.md`; do not create unindexed
  Markdown islands.

## Context Budget

- Normal repository context: 6,000-token capsule, 12,000-token hard ceiling.
- Cross-module work: up to 24,000 tokens only with an explicit expansion
  reason.
- Full source is required for files being edited. Supporting files should use
  signatures or exact relevant sections first.
- Never trade correctness, authority checks, or verification for a smaller
  capsule. If required truth does not fit, report insufficient context and
  expand deliberately.
