# Placeholder Consolidation Audit

Phase 10 source-backed inventory and migration record. Core owns resolution
contracts and limits; each listed domain retains ownership of its values.

## Inventory

| Family | Syntax | Owner/source | Contexts | Cost and visibility | Phase 10 result |
|---|---|---|---|---|---|
| Server identity | `%server%`, `%realm_term%`, `%currency%` and case transforms | Core `ServerIdentityConfig` | config, chat, UI, dialogue | Immutable parsed config; public | Registered canonical `core.identity.*` descriptors with compatibility aliases |
| Chat fields | `%realm_short%`, `%realm%`, `%player%`, `%message%` | Core Chat service and current message context | chat | Already-loaded request values; public to recipients | Identity portion migrated; styled message fields remain a deliberate Chat presentation adapter |
| Realm presentation | `%realm_display%`, `%realm_official%`, `%realm_tag%` | Core Realm presentation, consumed by Portals | UI | Already-resolved Realm snapshot; public | Migrated to Core context descriptors and bounded resolution |
| Reward commands | `{player}`, `{uuid}`, `{reward}` | Core reward action request | command | Current request only; server-side | Migrated to bounded schema resolution |
| Chronicle fields | `{actor}`, `{eventText}`, `{category}`, `{type}`, `{realm}` and event metadata | Core Chronicle schema plus addon-authored structured metadata | chronicle | Immutable event projection; history visibility is enforced before rendering | Migrated to bounded per-render schema descriptors |
| Mount collection | `{realm}` | Mounts collection presentation | UI | Already-available definition value; public | Migrated to bounded schema resolution |
| Government official names | `%realm%`, `%REALM%`, `%realm_lower%` | Government config/runtime Realm presentation | config/UI | Loaded config and Realm snapshot | Retained as a compatibility adapter because uppercase legacy syntax is case-sensitive |
| Economy, Quests, notifications | Direct typed values; no independent resolver found | Owning addon | UI/notification | Owner-formatted snapshots | No artificial placeholder layer added |

## Contracts And Limits

- Stable IDs are lower-case namespaced identifiers. Legacy aliases normalize
  to canonical IDs and may apply identity, upper, lower, or title transforms.
- Descriptors declare owner, value type, allowed contexts, required context
  keys, visibility, and missing/unauthorized behavior.
- Resolution preserves unknown tokens; bounds token count, output length,
  nesting depth, and diagnostics; detects cycles; and memoizes canonical values
  for one request.
- Resolvers receive immutable context only. They must not scan storage,
  history, worlds, players, or ledgers; perform IO/network calls; mutate state;
  or perform broad parsing.
- Schema-local Chronicle/reward/Mount fields exist only for the bounded render
  request. They do not become global mutable Core state.

## Compatibility

`ServerIdentityConfig.replace` remains only as a bootstrap compatibility
adapter because identity config must be parsed before the service exists.
Addon loaders and runtime consumers use `ElarionSystemApi.placeholders()`.

Government's case-sensitive `%REALM%` behavior remains local until a tested
alias migration can preserve old authored configs exactly. Chat retains its
component-aware player/message adapter so styled text is not flattened.

## Verification

- Core resolver tests: registration conflicts, aliases/transforms, unknown
  tokens, visibility, required context, cycles, count limits, and memoization.
- Existing Chronicle tests plus Government/NPC structured-metadata tests.
- Portal runtime Realm-presentation test.
- Full `build verifyAiContext`: passed; 12/12 context cases and 95.94% aggregate
  context reduction.
