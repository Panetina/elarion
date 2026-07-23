# Metrics And Rankings

Last reviewed: 2026-07-19

Status: typed descriptors, bounded update batches, append-first journals,
atomic projection snapshots, restart recovery, bounded single-writer
lifecycle binding, logarithmic rankings, competition ranking, revisioned
pagination, around-player queries, and the read-only/submit Core API are
implemented. Versioned metric events, metric-indexed title conditions, and
bounded lazy title reconciliation are also implemented. Completed-event
retention/compaction and web projections remain release gates.

## Ownership

Core owns reusable metric identity, idempotent acceptance, current values,
rankings, persistence, name resolution, title evaluation, and web projections.
Addons register descriptors and submit authoritative batches. They must not
persist a parallel leaderboard or player-name cache.

## Implemented Contract

- `MetricDescriptor` freezes metric ID, aggregation operation, sort direction,
  unit, legal scopes, indexed dimension names, and retention policy.
- Operations are `ADD`, `MAX`, `MIN`, and `SET_ONCE`.
- Scopes are global, realm, and bounded event scope.
- A descriptor allows at most eight named dimensions. An update/query may use
  only those names; arbitrary combinations are never inferred or expanded.
- `MetricUpdateBatch` contains source system, bounded source partition,
  positive monotonic sequence, event UUID, actor UUID, timestamp, authoritative
  realm, and at most 64 updates.
- Exact retry of the latest partition sequence/event/content is ignored.
  Stale sequences and same-sequence conflicting content fail closed.
- A batch validates and calculates every touched projection before mutating any
  projection, so validation and overflow failures are atomic in memory.
- Rankings store UUIDs and fixed-point `long` values. Identity names resolve
  outside the ranking index.
- Competition ranking is used: equal values share rank (`1, 1, 3`). UUID order
  is only the stable presentation tiebreaker.
- Ranking mutation uses ordered sets plus a deterministic order-statistic value
  index. Value/rank lookup and mutation are logarithmic; top/page/around output
  is capped at 100 entries.
- Page cursors carry a projection revision and fail when stale.

## Persistent Runtime

Implemented:

1. `MetricProjectionWorker` admits at most 4,096 queued immutable batches and
   serializes journal fsync/projection mutation on one worker;
2. exact source sequence/event retries are accepted without double applying;
3. atomic snapshots restore values, ranks, revisions, and source sequences,
   then replay only the journal tail;
4. checkpoints compact only owned metric journal segments;
5. normal queries read current indexes only;
6. `api.metrics()` exposes registration, asynchronous durable submit, bounded
   queries, revisions, and diagnostics;
7. shutdown drains accepted work before permitting a clean rebind.

8. an applied durable batch emits one schema-versioned `MetricUpdatedEvent`;
   exact retries emit no duplicate event;
9. title rules are indexed by metric ID and evaluate only the matching bounded
   projection query; online citizens reconcile lazily on join and reload.

Still required: completed-event retention summaries, cursor-based offline
admin reconciliation, and web projections.

## Angling Dimensions

`AnglingMetricDescriptors` freezes and registers all 18 approved metric IDs,
operations, sort directions, units, scopes, dimensions, and retention
policies. Accepted Angling transactions submit only these materializations:

- catch count: overall, fish ID, and rarity ID;
- catch performance: overall and fish ID;
- tournament metrics: overall and bounded event scope;
- milestones: registered milestone ID.

It must not submit arbitrary combinations such as fish × rarity × biome ×
equipment. Extra filtering belongs on accepted catch events or a separately
designed bounded projection.

## Verification

Current tests cover descriptor/update validation, realm authority, unindexed
dimension rejection, exact retry/conflict semantics, overflow atomicity,
competition ties, stable UUID display order, ranking changes, top/page/around
bounds, stale cursors, and randomized insertion/removal order statistics.
