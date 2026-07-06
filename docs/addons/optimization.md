# Optimization Addon Contract

Last reviewed: 2026-07-05

Author: Panyel  
Team: Panetina Team

## Owns

- Elarion task queue diagnostics
- rolling server-queue apply timing
- slow apply tick counts
- rejected, completed, and failed queue counters by task family
- IO and compute submitted, queued, active, completed, and failed counters
- IO and compute family counters
- sampled world/Realm counters
- sampled world entity categories and previous-sample deltas
- event-tracked block entity counts and block entity type groups
- bounded in-memory per-world trend windows
- slow operation warning counters for Core reloads, history writes, state saves,
  and server-queue application
- Chronicle archive write/failure metrics
- sampled tick/headroom monitoring
- performance config validation warnings
- world block-rule queue-full and slice completion/failure metrics

## Config

```text
config/elarion/addons/optimization/performance.yml
```

Core reads task budgets from this file before addons initialize.

`PerformanceConfigDescriptors` registers the read-only `optimization` config
domain after Core has loaded the task service. The domain explicitly declares
`platform:core` ownership and exposes only parsed host metadata, task budgets,
and monitoring thresholds from `ElarionTaskService.snapshot()`. Live queue
counters and ignored compatibility notes are not config entries. There is no
runtime reload path, so all entries are restart-required. Decimal values remain
read-only strings until Core gains a decimal descriptor codec.

## Commands

```text
/e perf status
/e perf queues
/e perf config
/e perf worlds
/e perf realms
/e perf realm <realm>
/e perf hotzones
/e perf security
```

`status` is the short operational summary, including server, IO, and compute
queue pressure. `queues` is the task-family breakdown for server, IO, and
compute work. `config` shows loaded settings and validation warnings,
`worlds`/`realms` show cached samples, `realm` focuses one Realm, `hotzones`
sorts sampled worlds by current pressure and shows entity groups plus
block entity groups, previous-sample deltas, in-memory trends, and `security`
shows the Security addon evidence summary.

Optimization commands use `CommandOutput` for chat-readable headers, sections,
and key-value rows. Do not collapse `/e perf` diagnostics back into packed
single-line strings; these commands are an operator-facing triage surface.

## Rules

This addon reports and controls Elarion cost. It is not a replacement for
general server performance mods.

Do not add persistent performance snapshots until live diagnostics show they
are worth the IO and storage cost.

Do not use private chunk internals for hotzone detail. Add block-entity and
ticking-chunk detail only through stable public APIs or an intentionally
documented compatibility hook. Current block entity counts use Fabric lifecycle
load/unload events instead of scanning loaded chunks.
