# Elarion Config And State

Last reviewed: 2026-07-18

Author: Panyel  
Team: Panetina Team

Editable definitions live under `config/elarion/`. Mutable runtime state lives
under `world/elarion/`.

Third-party client/server pack settings are a separate distribution boundary.
Safety settings live under `distribution/*/managed` and launcher updates always
replace them; this includes disabling ModernFix's Paper chunk and biome-cache
patches because Lithium owns the overlapping implementations. Defaults live under
`distribution/client/defaults` and YOSBR applies them only when absent. These
files are not Elarion typed server config descriptors. See
`docs/systems/Distribution.md`.

Global player-facing server terms live in
`config/elarion/core/server_identity.yml`. Specific Realm display names, short
tags, prefixes, colors, visibility, and spawns remain in `realms.yml`.

Shared visual tokens live in `config/elarion/core/ui_theme.yml`. Core validates
and synchronizes immutable `default`, `npc`, and `shrine` theme variants on
join and successful reload. The shared `defaults.font-scale-percent` value is
server-wide, defaults to `100`, and accepts `100-150`; failed reloads preserve
the previous valid theme snapshot. Addon UI files may define layout and
behavior, but must not duplicate shared colors, textures, borders, buttons,
typography scaling, or progress styles.

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

## Config Descriptor Registry

Core now owns a read-only config descriptor registry exposed through
`ElarionApi.system().configs()`. The registry describes validated runtime
config snapshots; it must not parse config files when an admin opens a UI.

The first registered domains are:

- `core`, backed by `CoreConfigDescriptors`. It exposes selected
  `ui_theme.yml`, `server_identity.yml`, Realm definitions, title definitions,
  title-progression definitions, reward definitions, citizen/activity, chat,
  identity/nickname, and history values with stable IDs, labels, descriptions,
  owner module, source files, reload command, type codecs, default/current
  display values, bounds, permissions, and reload/restart markers. Scalar and
  existing definition rows remain supplier-backed across successful
  `/e reload` operations. Dynamic Realm, title, progression-region,
  unlock-rule, reward, and reward-action rows are fixed to the IDs/indexes
  present when the domain is registered.
  Core title definitions may include optional `color: "#RRGGBB"` values.
  Missing built-in title colors are migrated in-place for known Core title IDs
  without overwriting admin-customized colors. Existing configs that still use
  the old shipped default-title gold `#D19B42` are migrated to the current
  white-gray Ember display default `#C9C9C9`. Its stable config ID remains
  `citizen` for compatibility. The same color is used by identity title
  rendering and Character Menu title presentation. Explicit configured colors
  always win. When a custom definition omits `color`, known title families use
  the shared rank palette, globally unique titles fall back to Legendary, and
  otherwise the title uses simple white.
- `guilds`, backed by `GuildConfigDescriptors`. It exposes read-only
  `guilds.yml` values from the current validated `GuildService.config()`
  snapshot.
- `economy`, backed by `EconomyConfigDescriptors`. It exposes read-only
  `economy.yml` values from `EconomyTransactionService.config()` and
  `service_prices.yml` metadata from `EconomyPricingService.definitions()`.
  Built-in service price IDs compare current values against shipped defaults;
  custom price IDs default to their current validated value. The domain also
  exposes bank interest, bank withdrawal tax, and shop sales tax policy.
  Interest is processed in bounded account batches when enabled. Withdrawal
  tax applies only when converting bank balance to physical currency; deposits
  are untaxed.
- `worlds`, backed by `WorldsConfigDescriptors`. It exposes read-only
  `worlds.yml` metadata from the loaded `WorldsConfigManager`: schema,
  lobby routing, current world keys/counts, and per-world identity/type/rule
  summaries. Dynamic world entries are descriptor rows for the worlds present
  when the domain is registered; summary values continue to read the active
  manager snapshot. The shipped managed-world defaults include lobby, three
  Realm overworlds, Worldheart, and Underworld. The Realm/Worldheart seeds and
  spawn/border centers are: `realm_world_1`
  `-4581459134664415489` at `-367,75,138`, `realm_world_2`
  `-8539746165762652083` at `3000,128,3920`, `realm_world_3`
  `3812826322966527666` at `6061,84,5122`, and `worldheart`
  `-7114969613679385277` at `-86,82,-48`.
- `portals`, backed by `PortalConfigDescriptors`. It exposes read-only
  `routes.yml` and `ui.yml` metadata from the loaded Portal definition
  service: route IDs, modes, source/destination dimensions, Economy price keys,
  schedule settings, visual settings, and prompt UI sizing. Dynamic route
  entries are descriptor rows for routes present when the domain is registered;
  route values continue to read the active definition snapshot.
- `offerings`, backed by `OfferingConfigDescriptors`. It exposes read-only
  Shrine UI and project metadata from `OfferingDefinitionService`: project IDs,
  scopes, repeatability flags, requirement/milestone/level counts, presentation
  fields, and Shrine UI sizing/placeholders. `society.yml` is reported as a
  reserved V1 surface because it is currently generated but not parsed into a
  runtime model.
- `government`, backed by `GovernmentConfigDescriptors`. It exposes read-only
  Government settings and form metadata from `GovernmentDefinitionService`:
  authority cleanup timing, form IDs, display fields, authority offices,
  office counts/holder limits, configured action guilds, and transitions.
  Dynamic form entries are descriptor rows for the forms present when the
  domain is registered; values continue to read the active definition snapshot.
- `npcs`, backed by `NpcConfigDescriptors`. It exposes read-only NPC
  definitions, skin and portrait profiles, trade catalog summaries, dialogue
  graph summaries, and UI settings from `NpcDefinitionService`. The domain
  registers after the first successful definition load. Dynamic NPC, profile,
  trade, and dialogue rows are fixed to the IDs present at registration; their
  values continue to read the active definition snapshot. Generated defaults
  include `worldheart_banker`, `worldheart_trader`, and
  `trades.yml` catalog `worldheart_trader`, but existing config files are not
  overwritten by the default writer. The generated banker/trader profiles use
  dedicated texture skins and curated 32x32 portrait library assets. A loaded
  `worldheart_trader` with a missing or blank `trade-catalog` is bridged in
  memory to the generated `worldheart_trader` catalog, and the known legacy
  `worldheart_trader.cobblestone_buyback` route is bridged to
  `destination-offer: cobblestone` when missing. Custom trader definitions and
  custom Sell stock routes still need explicit catalog and destination IDs.
  Trade offers may include
  `custom-model-data` for server-authored item preview art, `price-key` as an
  Economy hook for taxes, inflation, or dynamic merchant pricing, and
  `stock-limit` / `restock-amount` / `restock-interval-seconds` for finite
  per-placed-NPC stock. Zero stock limit means unlimited. Sell offers may set
  `direction: sell`, `sell-match`, `component-policy`, `max-quantity`,
  `stock-destination`, and `destination-offer`; descriptors expose those fields
  and validation accepts enabled Sell rows when the matching/component policies
  are valid and `placed_npc` stock destinations target a BUY offer. The
  default Nether/End ticket
  offers use custom model data to select the same Portal ticket stele models
  used by real purchased ticket stacks. Each NPC definition exposes a stable
  `faction`: `realm:<id>`, `worldheart`, `underworld`, or a custom faction id.
  Faction ids drive the Character Menu Reputation tab and remain separate from
  territorial tax routing. Each NPC definition also exposes
  `tax-jurisdiction`: `auto`, `realm:<id>`, or
  `world:<namespaced-id>`. `auto` resolves to the canonical Realm owning the
  placement world and otherwise to that world. Explicit policies must match
  the placement world. Generated Worldheart banker/trader definitions use
  `world:elarion:worldheart`. Dialogue descriptors expose the active node
  presentation kinds. Dialogue nodes accept `presentation:
  dialogue|bank|trade`. `trade` currently provides server-authoritative BUY
  purchases and SELL buybacks through NPC-owned catalog/stock/purchase/sale
  state and optional Economy settlement. Validation rejects prompts and
  executable actions on trade-node options so trade mutations stay on the
  dedicated request path. Options
  accept stable `presentation-role` identifiers so clients do not parse
  localized labels. Narrative options may declare `one-time: true`; durable
  story actions use `elarion_npcs:set_story_flag`, `clear_story_flag`,
  `set_ending`, and `set_reentry_node`, while conditions use
  `story_flag_set` and `ending_is`. Explicit public-history outcomes require
  both `history-worthy: true` and `history-outcome`. Legacy root-level Economy deposit/withdraw prompts are
  projected into a dedicated bank node in memory without rewriting
  administrator files. Decimal range values are currently exposed as strings
  because the shared descriptor registry has no decimal codec yet.
- `quests`, backed by `QuestConfigDescriptors`. It exposes read-only quest
  package metadata and graph summaries from `QuestDefinitionService`: scope,
  root stage, version, tags, actors, variables, stages, evidence, endings,
  reusable conditions/consequences, authoring keys, and metadata keys. Dynamic
  questline rows are fixed to IDs present at registration; values continue to
  read the active atomic definition snapshot.
- `realms`, backed by `RealmConfigDescriptors`. It exposes the loaded
  `protection.yml` snapshot: shared world IDs, operator bypass, explosion block
  protection, denial-feedback cooldown, and additional mechanism/container
  block IDs. Realms has no live reload path, so these entries are explicitly
  non-runtime-reloadable and restart-required.
- `mounts`, backed by `MountConfigDescriptors`. It exposes the four loaded
  Collection text fields for each of the seven registered mount types, plus
  bounded mount count/ID summaries. Mounts has no live config reload path, so
  these entries are non-runtime-reloadable and restart-required.
- `underworld`, backed by `UnderworldConfigDescriptors`. It exposes the active
  `UnderworldService.config()` snapshot across Underworld, corpse, PvP loot,
  combat-tag, and Soul Fracture categories. `/e death reload` replaces the
  service snapshot, so values are supplier-backed and runtime-reloadable.
  Decimal settings remain read-only string descriptors until Core gains a
  decimal codec.
- `optimization`, backed by `PerformanceConfigDescriptors`. It exposes the
  Core-owned task settings loaded from `performance.yml`: host metadata, worker
  and server-apply budgets, sampling controls, warning thresholds, and headroom
  thresholds. Core loads these settings before addons initialize, so the domain
  declares `platform:core` ownership and all entries are restart-required.

Current descriptor types live under
`platform/core/src/main/java/panetina/elarion/core/config/`:

- `ElarionConfigRegistry`
- `ElarionConfigDomain`
- `ElarionConfigCategory`
- `ElarionConfigEntry`
- `ElarionConfigCodec`
- `ElarionConfigValidator`
- `ElarionConfigPermission`
- `ElarionConfigChangeRequest`
- `ElarionConfigChangeResult`
- `ElarionConfigChangeError`
- `ElarionConfigChangeValidator`
- `ElarionConfigEditTarget`
- `ElarionConfigEditControl`
- `ElarionConfigApplyRegistrar`
- `ElarionConfigApplyRegistry`
- `ElarionConfigApplyExecutor`
- `ElarionConfigApplier`
- `ElarionConfigPreparedChange`
- `ElarionConfigApplyCapability`
- `ElarionConfigApplyContext`
- `ElarionConfigApplyReadiness`
- `ElarionConfigApplyReadinessProvider`
- `ElarionConfigApplyAuditRecord`
- `ElarionConfigApplyAuditSink`
- `ElarionConfigApplyAuditSession`
- `ElarionConfigApplyAuditPhase`
- `ElarionConfigApplyAuditJournal`
- `ElarionConfigApplyCoordinator`
- `ElarionConfigApplyService`

The registry is read-only. Addon config files currently parsed into runtime
models have descriptor coverage. Core scalar manager settings, Realm
definition summaries, title definitions, title-progression definitions, and
reward definitions have descriptor coverage. Generated-only Core abilities,
Jail, and Security YAML need typed runtime loaders before they can truthfully
expose current-value descriptors.

Title-progression rules may include one optional bounded `metric` condition:
`id`, `scope` (`global`, `realm`, `realm:<id>`, or `event:<id>`),
`comparator` (`GTE`, `LTE`, or `EQ`), `threshold`, and up to four exact
`dimensions`. Core indexes these rules by metric ID and reads only the matching
current projection; formatting or gameplay never scans metric history.
Config mutation request/result/error records and a pure validation helper now
exist. The validator resolves a request against the registry, checks write
permission metadata, detects stale expected-current values, parses the
submitted raw value through the entry codec, runs the entry validator, and
returns a validated/rejected result.

Core also defines a config apply registration contract. An explicit
`ElarionConfigEditTarget` registration binds capability metadata and one
`ElarionConfigApplier`; duplicate registrations are rejected. Capability
metadata declares the audit event type, affected files, runtime-reload support,
restart-required support, or a disabled reason. Descriptor-aware readiness
lookup blocks unknown targets, missing/disabled appliers, and entries whose
reload/restart policy is not supported. Ready results require non-empty audit
and affected-file declarations. Core constructs one canonical registry and
exposes only the non-castable `ElarionConfigApplyRegistrar` method-reference
facade through `ElarionApi.system().configAppliers()`. Addons can register but
cannot look up or invoke executable appliers. Core registers one production
backend target, `core:ui_theme:defaults.font-scale-percent`; addon production
registrations do not exist yet. Admin cannot access executable registration
lookup.

Core binds the narrow `ElarionConfigApplyExecutor` facade into the Admin
service. The facade exposes readiness and backend apply execution, not concrete
registry lookup. The config edit shell uses readiness to explain missing,
disabled, or policy-unsafe targets. The client Apply button remains
non-interactive until a separate UI enablement slice approves click behavior.

Appliers use a transactional preparation contract. `prepare(context)` returns
an `ElarionConfigPreparedChange`; preparation must not mutate authoritative
files or runtime snapshots. Its `commit()` operation may perform the owner
mutation, and `rollback()` must restore the prior state. The provided
`ElarionConfigPreparedChange.of(...)` helper permits one successful commit,
prevents commit after rollback, and invokes rollback at most once. Applied
results require an audit event type and can preserve validated reload/restart
flags.

Core now has an internal, unwired `ElarionConfigApplyCoordinator`. It uses one
fair process-global mutation lock, reruns validation and readiness under that
lock, resolves trusted descriptor records, and prepares the owner transaction.
Before commit it asks the mandatory audit sink to prepare a write-ahead
`ElarionConfigApplyAuditSession`. Trusted commit then requires the session's
COMMITTED terminal operation. Commit, owner-result, or audit-terminal failure
invokes rollback and records ROLLED_BACK; rollback failure records FAILED.
Audit preparation failure rolls back prepared resources before commit.

`ElarionConfigApplyAuditJournal` is the unbound durable audit sink for this
contract. It writes versioned JSONL records with PREPARED, COMMITTED,
ROLLED_BACK, and FAILED phases, synchronously appends and forces each record,
and exposes bounded unresolved-tail recovery for startup diagnostics before
production apply execution is enabled. Its target path helper resolves to
`world/elarion/core/audit/config-changes.jsonl` when passed the Elarion world
root.

`ElarionConfigApplyService` owns production lifecycle for the future apply
path. Core constructs it with the canonical descriptor registry and concrete
apply registry, binds it on server start using the active world Elarion root,
and routes Admin readiness through it. The service refuses ready targets while
unbound, when bounded audit recovery is truncated, or when unresolved PREPARED
journal records exist. It does not expose coordinator, registry lookup, or
journal mutation through the public addon API. Its backend `apply(...)` method
returns `UNSUPPORTED` while unbound/unsafe and delegates to the coordinator only
when execution is ready. The first production backend applier is registered
for `core:ui_theme:defaults.font-scale-percent`; it writes
`config/elarion/core/ui_theme.yml`, reloads Core config, and resyncs UI themes
through the existing Core UI theme service. The Admin screen can send Apply
only for a server-authored ready target with a matching latest validation
result.

Admin binds the config apply service through the narrow
`ElarionConfigApplyExecutor` facade. The facade exposes readiness lookup and
backend apply execution only; it does not expose coordinator, concrete registry
lookup, or journal mutation. Server-side `Intent.APPLY` now dispatches through
that executor after OP checks. The only production target currently registered
is `core:ui_theme:defaults.font-scale-percent`.

Config edit controls now separate `inputEditable` from `applyAvailable`.
`disabledReason` explains why the proposed-value input is disabled, while
`applyDisabledReason` explains why Apply is unavailable. The legacy
`editable()` method remains a compatibility alias for `inputEditable()`.
The Admin screen enables Apply only when the open control is apply-available,
the latest result is `VALIDATED`, the result has `canApply=true`, and the
validated old/new values still match the current control and proposed input.

`jail.yml` and `security.yml` are currently generated placeholders, not loaded
runtime configuration. They must not be presented as active current-value
domains until their owning addons have typed loaders and validated snapshots.

## Descriptor Maintenance Rule

Every future slice that adds or changes parsed config, config-backed content,
addon definition files, or Core definition maps must update the matching
read-only descriptor domain and focused descriptor tests in the same slice.
Descriptor discovery must read current values from the owning validated runtime
snapshot or service, never by reparsing files during Admin Panel discovery.

Generated-only YAML is not enough for descriptor coverage. If a file is only
written as a placeholder/default and is not parsed into a typed runtime model,
document that boundary and add the typed loader first before exposing current
values in the descriptor registry.

## Admin Panel Discovery

The Core Admin Panel includes a dedicated read-only Config tab over registered
descriptor domains. Each domain appears as a server-authored summary row, each
domain category appears as its own read-only detail row, and each descriptor
entry appears as its own stable entry row. Domain rows show owner, source
files, reload command, category/entry counts, reload/static counts,
restart-required counts, validation status, and category summaries. Category
rows show scoped entry details with current/default display values, bounds,
choices, reload/restart markers, and validation errors. Entry rows show one
setting's path, description, current/default values, type, bounds, choices,
permissions, runtime marker, and current validation state.

Entry rows expose a preview-only `Validate Value` action. The action submits
one proposed raw value through the existing generic Admin Panel action payload,
calls `ElarionConfigChangeValidator` on the server, and returns a short
valid/invalid message. It does not write files, apply values, reload config,
emit audit events, or change runtime state. This browser still uses the
existing row/detail packet model only and does not parse files when opened.

The Systems tab remains for provider-owned testing and repair rows. Config
writes, typed editing, page/category provider contracts, reload orchestration,
and packet schema changes remain deferred.

Page/category provider contracts and broader addon config editing remain future
slices. Editing must not be enabled through the generic Admin Panel validation
action. Addon domains need explicit, proven reload-safe appliers before their
entries can become editable.

The edit protocol is separate from `AdminPanelActionPayload`. Core now has
records and packet codecs for config edit targets, edit-control snapshots,
edit requests, and edit results:

- `ElarionConfigEditTarget`
- `ElarionConfigEditControl`
- `ElarionConfigEditOpenPayload`
- `ElarionConfigEditRequestPayload`
- `ElarionConfigEditResultPayload`

These records carry one descriptor target, typed control metadata, the current
display value shown to the admin, the proposed raw value, structured
validation/apply results, reload/restart policy, audit preview text, and
bounded errors. Their payload types are registered. Core also registers a C2S
receiver for `ElarionConfigEditRequestPayload`: OP level 4 admins can request
descriptor validation, and server-side `APPLY` dispatches through the
audit-backed apply executor when the target has a ready production backend
applier. The visible Admin client sends Apply only after a matching successful
validation result for an apply-available control. Client receivers store the
last `ElarionConfigEditResultPayload` and current
`ElarionConfigEditOpenPayload` control in passive client state for future edit
UI use. Config entry rows expose `Open Editor`, which asks the OP-gated server
action to resolve the selected descriptor and send a server-authored edit
control. The Admin Panel renders that control in a detail shell with Close,
descriptor metadata, a proposed-value input, server-side Validate, structured
validation-result display, and Apply. A visible descriptor is not automatically
editable: the owning domain must register an explicit applier or edit provider
before values can be written or applied.

Registered config edit packet directions:
`ElarionConfigEditOpenPayload` and `ElarionConfigEditResultPayload` are S2C,
and `ElarionConfigEditRequestPayload` is C2S. The C2S receiver delegates to
Core Admin/config service code, converts validation requests into
`ElarionConfigChangeRequest`, runs `ElarionConfigChangeValidator`, returns an
`ElarionConfigEditResultPayload`, and refreshes the Admin Panel Config tab with
a server-authored message. The client receiver records the structured result
and clears it on join/disconnect. The open-payload receiver records the current
server-authored edit control and clears stale validation results when a new
control opens. The detail shell sends `VALIDATE` for validation and sends
`APPLY` only when the server-authored control and latest validation result
match exactly. Applied results close the open edit shell so stale current values
are not reused after reload/sync.

## Addon Validation Pattern

New addon config loaders should follow the same sequence:

1. Generate missing defaults only.
2. Parse YAML/JSON into raw maps or DTOs.
3. Validate local schema first: required fields, types, bounds, IDs, unknown
   fields, and enum values.
4. Build immutable definition records only after local schema is valid enough
   to avoid misleading follow-up errors.
5. Validate references against the owning source of truth:
   - Core-owned references through `ElarionApi` or guilded facades.
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

Current coverage exists for Core cross-file references, Worlds config, NPC
dialogue/action/prompt validation, and Quest default/round-trip validation.
Economy, Portals, Offerings, Government, Trade, Adventure Guild, and Ledger
should add equivalent tests as their config surfaces become real.

## Quest Config

Questline definitions live in:

```text
config/elarion/addons/quests/questlines/<quest-id>/
config/elarion/addons/quests/questlines/<quest-id>.yml
```

Folder packages are preferred for new questlines and may contain `quest.yml`,
`actors.yml`, `variables.yml`, `stages.yml`, `evidence.yml`, `endings.yml`,
`conditions.yml`, `consequences.yml`, and `authoring.yml`. Legacy single-file
questlines remain supported. Supported V1 scopes are `realm`, `global`,
`world`, and `player`. Variables are typed as `integer`, `boolean`, or
`string` and scoped as `shared` or `player`.

Runtime quest state lives in:

```text
world/elarion/addon-state/quests/state.json
```

Runtime state stores compact questline records, player records, flags,
variables, evidence, endings, actor bindings, and scheduled consequences. It
must not copy NPC dialogue definitions, NPC placement ownership, Offering
project definitions, Government state, or Core citizen/title truth.

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
action metadata, transition metadata, and official name templates. Active
Government forms are currently Monarchy and Republic.

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
    - npc
    - economy
    - government
    - offering
    - portal
    - underworld
  # Type rules can be "proposal-approved" or
  # "government:proposal-approved".
  default-chronicle-type-enabled: true
  enabled-chronicle-types: []
  disabled-chronicle-types: []
```

`npc` includes only explicitly `history-worthy` meaningful story outcomes;
ordinary dialogue choices and service interactions are not recorded.

Public-memory feed bounds are configured separately from OP command bounds:

```yaml
public-query:
  default-weeks: 8
  max-weeks: 52
  default-limit: 50
  max-limit: 200
```

Future newspaper, ledger, NPC rumor, and GUI search implementations should
consume `api.publicHistory()` and only add dedicated indexes after real usage
shows the generic bounded composition path is not selective enough.

## Mounts Config

Mount Collection display text lives in:

```text
config/elarion/addons/mounts/collection.yml
```

Each mount entry can define the locked/unlocked row text and locked/unlocked
detail text used by the Core Collection screen. Realm vendor text may use the
`{realm}` token. Runtime unlocks and active mount choices remain in world
state, not config.

Mount rarity/accent presentation is code-owned by `ElarionMountType`, not this
text file. Airship, Hot Air Balloon, and Ghast are Common Realm baseline
mounts; Bee, Chinese Dragon, and Wyvern are Uncommon future reward/progression
mounts; Sci-Fi Bike is Legendary for the future full-advancement route.

`MountConfigDescriptors` exposes the loaded text snapshot through the read-only
Admin Panel config browser. The descriptor surface is bounded by
`ElarionMountType`; it does not expose collection unlock state, active mounts,
or summon sessions.

The Admin Panel Config tab loads descriptor rows in bounded scopes. Opening the
tab sends domain/category summaries only; selecting a category asks the server
for that category's entry rows. Do not rely on the client having every
descriptor entry in the initial `/e panel` payload.

The only production Admin Panel Apply target currently registered is
`core:ui_theme:defaults.font-scale-percent`. It validates through the Core
descriptor, writes `config/elarion/core/ui_theme.yml` with a temp-file replace,
reloads Core config, resyncs UI theme snapshots, and rolls back on failed
reload. Stale development copies of `ui_theme.yml` that predate
`font-scale-percent` are repaired by inserting the missing scalar into the
existing `defaults` block; duplicate scalar lines are rejected without
mutation.

## Economy Config

Economy persistence and monitor settings live in:

```text
config/elarion/addons/economy/economy.yml
```

The file controls snapshot frequency, forced transaction journal writes,
bounded operation receipt retention, bounded OP query limits, Governor mode,
monitor window size, bank interest,
bank withdrawal tax, and shop sales tax policy. The default Governor mode is
`MONITOR_ONLY`; Governor config values do not enable automatic adaptive price
or reward changes. Bank interest is separately disabled by default and only
accrues when `bank.interest.enabled` is true.

`/e economy reload` uses a two-stage prepare/commit boundary. Both
`economy.yml` and `service_prices.yml` must parse and validate successfully
before either runtime service is changed. A pricing error therefore preserves
the complete previous Economy snapshot instead of partially reloading
transaction settings.

```yaml
operations:
  receipt-retention-days: 30
  max-receipts: 10000
```

These values bound the O(1) idempotency index used by retry-safe addon
transactions. They do not widen transaction-history scans.

Economy runtime state lives under:

```text
world/elarion/addon-state/economy/
```

`economy-state.json` is a compact schema-versioned wallet/treasury/operation
receipt snapshot. Schema v2 adds bounded idempotent receipts; schema v1 is
backed up and atomically migrated on load.
Monthly transaction JSONL files are the durable write-ahead journal and audit
source. A transaction append must succeed before any balance changes.

Mutable category tax overrides are runtime policy, not editable definition
config, and live in `tax-policies.json` beside Economy state. The strict file
stores Realm/Worldheart authority, category, basis points, and revision.
Future Seat of Rule and owner Admin Panel editors must call Economy's
server-authorized policy API; they must not edit or duplicate this file.

Worldheart governing authority is runtime world state, not active config. It is
persisted by Core in `world/elarion/worldheart/authority.json` and defaults to
system governance by the lore-facing `Hollow Emperor`. Future config/Admin UI
may expose controlled authority tools, but gameplay code must use the Core
Worldheart service rather than reading a config string or fake player ID.
