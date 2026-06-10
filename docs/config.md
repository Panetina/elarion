# Elarion Config And State

Last reviewed: 2026-06-10

Author: Panyel  
Team: Panetina Team

Editable definitions live under `config/elarion/`. Mutable runtime state lives
under `world/elarion/`.

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

## Runtime State Rules

Runtime state stores compact IDs, counters, timestamps, progress, and flags.
Do not copy full config definitions into world state.

Writes must be:

- dirty-tracked
- atomic
- bounded
- scheduled
- saved on logout/server stop where player-specific

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
