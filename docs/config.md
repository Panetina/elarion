# Elarion Config And State

Last reviewed: 2026-06-11

Author: Panyel  
Team: Panetina Team

Editable definitions live under `config/elarion/`. Mutable runtime state lives
under `world/elarion/`.

Global player-facing server terms live in
`config/elarion/core/server_identity.yml`. Specific Realm display names, short
tags, prefixes, colors, visibility, and spawns remain in `realms.yml`.

Shared visual tokens live in `config/elarion/core/ui_theme.yml`. Core validates
and synchronizes immutable `default`, `npc`, and `shrine` theme variants on
join and successful reload. Addon UI files may define layout and behavior, but
must not duplicate shared colors, textures, borders, buttons, or progress
styles.

`default` is the canonical Elarion visual language. Semantic variants such as
`npc` and `shrine` inherit it and should override only presentation values that
are genuinely unique to that screen family. Future screens should inherit
`default` rather than inheriting from another feature's variant.

The shared theme separates `header` and `panel` body colors. Its
`bevel-highlight` and `bevel-shadow` tokens give panels, cards, buttons, tabs,
portraits, scrollbars, and progress bars the same shaded, chamfered
Minecraft-style frame instead of isolated one-pixel addon borders.

## Required Config Behavior

Every config file must define:

- generated default
- schema version
- clear validation errors
- reload behavior
- runtime-state counterpart when applicable
- owner service or addon

Definitions load into immutable runtime records. Gameplay actions must not parse
YAML.

Core default file creation is handled by a focused default writer. Parsing,
validation, and migration should continue moving out of the public manager in
small behavior-preserving steps.

Validation must cover both local schema and cross-file references. Current Core
validation checks built-in references such as default title IDs, title grant
targets, title abilities, title active effects, reward action types, item IDs,
status values, and ability IDs. As addons add executable registries and config
sections for actions, conditions, requirements, milestones, worlds, tickets,
quests, NPCs, government forms, markets, and portals, their loaders must extend
the same rule: unknown executable IDs and broken references fail reload with a
clear path.

Core can validate only Core-owned config at Core startup. Addon-owned config
must validate addon-owned references after the addon has registered its
definitions and executable handlers.

## Addon Validation Pattern

New addon config loaders should follow the same sequence:

1. Generate missing defaults only.
2. Parse YAML/JSON into raw maps or DTOs.
3. Validate local schema first: required fields, types, bounds, IDs, unknown
   fields, and enum values.
4. Build immutable definition records only after local schema is valid enough
   to avoid misleading follow-up errors.
5. Validate references against the owning source of truth:
   - Core-owned references through `ElarionApi` or grouped facades.
   - Shared executable references through registered action, condition,
     requirement, and milestone registries.
   - Minecraft content through registry IDs, tags, or explicit config lists.
   - Addon-local references against the addon's immutable definition maps.
6. Reject reload with precise path-oriented errors if any reference is unknown.
7. Keep the previous valid snapshot active when reload fails.

Do not create a large shared validation framework until at least two real addon
loaders need the same helper. Prefer small shared helpers for obvious repeated
checks such as ID normalization, positive number bounds, registry ID/tag
syntax, and unknown-field reporting.

Validation tests should cover at least:

- valid default config loads
- unknown executable action/condition/requirement IDs
- broken local references
- invalid enum/type/bounds values
- reload failure preserving previous valid state when the loader supports live
  reload

Current coverage exists for Core cross-file references, Worlds config, and NPC
dialogue/action/prompt validation. Economy, Portals, Offerings, Government,
Trade, Adventure Guild, and Ledger should add equivalent tests as their config
surfaces become real.

## Portal Config

Portal route definitions live in:

```text
config/elarion/addons/portals/routes.yml
config/elarion/addons/portals/ui.yml
```

`routes.yml` owns route display text, source/destination dimensions, route
mode, required physical ticket identity, recurring real-world schedule,
warnings, and field presentation. Prices reference stable keys from
`config/elarion/addons/economy/service_prices.yml`. Supported modes are:

- `scheduled_ticketed`: requires complete linkage, unlock state, an active
  schedule window, one outbound ticket, and a return entitlement.
- `fee_passage`: activates after linkage, charges the current Economy service
  price per crossing, and may grant one free first round trip.
- `always_open`: activates automatically once linked and uses no lock, window,
  ticket, or return entitlement.

Linked source/destination cuboids, arrivals, unlock state, sent warnings,
return entitlements, and free-passage state live in
`world/elarion/addon-state/portals/state.json` and are never copied into
editable config.

The current renderer intentionally uses the shared vanilla animated Nether
portal texture through the standard translucent render layer for Iris
compatibility. Route color, brightness, and opacity are validated config.
Custom textures and animation timing remain reserved until a renderer that can
apply them without per-route block registration is justified.

## Government Config

Government settings and form definitions live in:

```text
config/elarion/addons/government/government.yml
config/elarion/addons/government/forms/<form-id>/form.yml
```

`government.yml` owns global Government settings. Current keys:

```text
authority.inactivity-days
authority.inactivity-check-interval-seconds
```

The default authority inactivity window is 7 days, checked every 600 seconds.
The check is bounded to stored Government office holders and uses Core citizen
`lastSeenAt` truth.

Each form file owns form definitions, office definitions, authority offices,
action metadata, transition metadata, official name templates, and
Confederation group-delegate metadata.

Government runtime state lives in:

```text
world/elarion/addon-state/government/state.json
```

Runtime state owns active Realm government form state, voted identity overlay,
founding completion marker, and office holders. It must not copy full form
definitions or Core Realm membership.

## Runtime State Rules

Runtime state stores compact IDs, counters, timestamps, progress, and flags.
Do not copy full config definitions into world state.

Writes must be:

- dirty-tracked
- atomic
- bounded
- scheduled
- saved on logout/server stop where player-specific

## Activity Config

Core activity eligibility is configured in:

```text
config/elarion/core/activity.yml
```

The default inactivity window is 14 real-world days. Citizen records persist
`lastSeenAt`; login and disconnect refresh it. Callers evaluate eligibility
lazily through Core rather than scanning all citizens every tick.

Activity recency is separate from citizen status. It is intended for vote,
population, office, interest, and reward eligibility while preserving citizen
records and mail delivery.

## Deferred Reward State

Core stores deferred grants in:

```text
world/elarion/reward-grants.json
```

Each grant snapshots reward actions, completed action indexes, source metadata,
creation time, and delivery time. Delivered receipts are retained as compact
deduplication records until a future explicit compaction/migration policy is
implemented.

## Migration Rule

When schemas change, add an explicit migration helper or deliberately reset the
development state. Do not silently reinterpret old fields.

## Regeneration Policy

Generated defaults are starter definitions. They are not runtime state.

- Missing config files may be regenerated from defaults.
- Existing config files must be migrated or rejected with clear validation
  errors.
- Development-only config may be deleted and regenerated when explicitly
  approved.
- Production config should be backed up before schema migration.
- Runtime state under `world/elarion/` must never be replaced just because a
  config file was regenerated.

## History Recording Config

Core history recording is controlled by:

```text
config/elarion/core/history.yml
```

This file can enable or disable recording by category and event type. Event
types may be plain, such as `realm-assigned`, or scoped, such as
`citizen:realm-assigned`.

History filters are evaluated at record time before JSONL writes are queued.
Use them to reduce noisy event storage, not to hide required audit trails.

The same file also controls ordinary live query bounds:

```yaml
query:
  max-months-scanned: 3
  command-limit-max: 100
```

Raise `max-months-scanned` only when OP history commands need older live
records. Rich public history, Chronicle, newspaper, and ledger views should use
monthly history indexes and Chronicle archive summaries instead of widening
normal command scans.

Chronicle generation is also configured here:

```yaml
archive:
  enabled: true
  max-completed-weeks-per-generation: 8
  chronicle-categories:
    - realm
    - realm-decision
    - diplomacy
    - leadership
    - title
    - reward
    - world
    - administration
    - security
```

Public-memory feed bounds are configured separately from OP command bounds:

```yaml
public-query:
  default-weeks: 8
  default-limit: 50
  max-limit: 200
```

Future newspaper, ledger, NPC rumor, and GUI search implementations should
consume `api.publicHistory()` and only add dedicated indexes after real usage
shows the generic bounded composition path is not selective enough.

## Economy Config

Economy persistence and monitor settings live in:

```text
config/elarion/addons/economy/economy.yml
```

The file controls snapshot frequency, forced transaction journal writes,
bounded OP query limits, Governor mode, and monitor window size. The default
Governor mode is `MONITOR_ONLY`; config values do not enable automatic price,
reward, tax, or interest changes.

Economy runtime state lives under:

```text
world/elarion/addon-state/economy/
```

`economy-state.json` is a compact schema-versioned wallet/treasury snapshot.
Monthly transaction JSONL files are the durable write-ahead journal and audit
source. A transaction append must succeed before any balance changes.
