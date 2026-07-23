# Catch Telemetry

Last reviewed: 2026-07-19

Status: durable worker acceptance, append-first journal, bounded replay,
schema-v2 server-outcome details, immutable per-player and per-species
summaries, lifecycle persistence, schema-v1 migration, memory-only normal
queries, and asynchronous Core submit are implemented. Angling has a
crash-recoverable telemetry/metric/reward coordinator, but public fishing
remains release-gated. Titles, milestones, and tournaments remain incomplete.

## Ownership

Core owns:

- the immutable cross-module telemetry contract;
- durable accepted-catch records;
- per-player catch summaries and rarity totals;
- title, History, Chronicle, archive, and dashboard projections;
- persistence, replay, deduplication, and bounded query APIs.

Angling owns:

- fish definitions and definition indexes;
- active fishing sessions;
- server-authoritative catch resolution;
- translation from Angling definition and rarity IDs into Core telemetry;
- fishing rewards requested through approved Core reward boundaries.

Core must not depend on Angling classes. Angling must not persist duplicate
catch history or title-facing progression.

## Current Source Truth

Implemented:

- `CatchTelemetryEvent` validates stable event identity, occurrence time,
  actor, source, fish-definition, rarity, positive quantity, optional location
  IDs, bounded immutable metadata, and optional typed server-outcome details.
- `CatchTelemetryDetails` carries output item, catch type, size, weight,
  percentile, minigame duration/hits/outcomes, equipment, fluid, realm, and
  tournament IDs. The producer must compute it on the server.
- `AcceptedCatchRecord` defines the versioned Core-owned durable record shape.
  Schema 2 persists rich details; schema-1 records migrate to schema 2 with no
  fabricated outcome details.
- `CatchTelemetryJournalCodec` explicitly encodes identifiers as strings,
  validates decoded JSON with document-scoped diagnostics, and computes
  player/month journal paths in UTC without performing IO.
- `CatchTelemetryJournalStorage` synchronously appends and forces one
  player/month record before returning.
- Replay uses a validated month/line checkpoint, scans at most the requested
  physical line count, returns immutable records, and carries a replay-session
  record map for exact duplicate detection across pages.
- Exact duplicate event records apply once. Reusing an event ID with different
  content, malformed JSON, or an actor/partition mismatch fails the replay page
  without advancing caller-owned deduplication state.
- `CatchSummary` stores consistent immutable totals by source, fish definition,
  and rarity, direct per-species projections, bounded newest-first recent
  records, timestamps, and the replay checkpoint.
- `CatchSpeciesSummary` materializes count, first catch, fastest and accumulated
  time/sample count, largest size, heaviest weight, lowest recorded percentile,
  and golden/perfect/treasure counts. Normal queries never scan JSONL.
- `CatchSummaryProjection` advances checkpoints for every successfully replayed
  page, uses overflow-safe counter updates, and caps recent records at 32.
- `CatchSummaryStorage` uses an explicit validated JSON shape and atomic
  replacement under `catch-telemetry/players/`.
- `CatchSummaryRepository` loads one player directly, caches immutable
  summaries, marks only changed players dirty, persists dirty snapshots, and
  retains dirty state after save failure.
- `CatchTelemetryWorker` is the bounded 4,096-task single-writer lane for
  acceptance, replay, and checkpoints. IO does not run on reel, join,
  disconnect, query, or server-tick threads.
- Player join schedules direct-player activation. Read-only queries return the
  current cached immutable projection and never parse JSONL.
- `ElarionCatchTelemetryApi` returns immutable direct-player summaries,
  indexed totals, direct species records, and bounded recent records through
  `api.catchTelemetry()`.
- accepted-catch events emit only after Angling durably enqueues its
  deterministic Core reward grant; consumers deduplicate by event UUID.
- `AnglingRarity.id()` exposes stable placeholder technical identifiers.

Not implemented: title/milestone/tournament/Chronicle consumers and the final
release-enabled rod/client trigger.

Emitting the current event durably appends the accepted catch before updating
the Core summary. It still does not grant rewards or mutate progression.

## Processing Decision

Catch telemetry uses a dedicated Core processing service rather than a
direct `HistoryService` listener.

Current flow:

```text
Angling server-authoritative catch result
  -> forced Angling transaction request journal
  -> bounded Core CatchTelemetryWorker
  -> append accepted record to player-partitioned journal
  -> apply record once to cached per-player summary
  -> durable bounded Core metric batch
  -> idempotent claimable Core reward grant
  -> accepted CatchTelemetryEvent delivery
  -> Angling delivered marker
  -> later consumers query immutable Core summary snapshots
```

Every catch must not become a general `HistoryEvent`. Catch volume can be high,
and copying every catch into the global History JSONL and public-memory index
would create unnecessary storage and Chronicle noise. Dedicated catch storage
is canonical for catch facts. Later noteworthy outcomes, such as a first catch,
rarity milestone, title unlock, or public record, may emit separate Core
history/progression events.

## Current Event Contract

`CatchTelemetryEvent` requires:

- `UUID eventId`: stable ID created once by the server-authoritative catch
  resolution result;
- `long occurredAt`: server time of the resolved catch.

Validation:

- a missing `eventId` is rejected;
- `occurredAt` must be positive;
- all current field validation remains unchanged;
- producers must reuse the same `eventId` only when retrying the same resolved
  catch;
- metadata remains diagnostic context, not an indexing or gameplay authority
  surface.
- rich outcome details are optional for legacy and non-fishing sources, but an
  Angling accepted catch must supply them from its server-owned session result;
- clients never supply duration, hits, perfect, golden, treasure, size, weight,
  percentile, equipment, fluid, realm, or tournament truth.

Core assigns no visible name and does not resolve Angling definitions. It stores
technical identifiers exactly as accepted.

## Durable Record

The processing service converts telemetry into an immutable Core-owned
`AcceptedCatchRecord` containing:

- event ID;
- occurrence timestamp;
- actor UUID;
- source, fish-definition, and rarity identifiers;
- quantity;
- optional world, dimension, and biome identifiers;
- optional typed server-derived outcome details: output item and catch type;
  size in millimetres; weight in grams; percentile basis points; minigame
  duration and hit count; perfect, golden, and treasure flags; bait, rod,
  bobber, hook, fluid, realm, and tournament IDs;
- bounded metadata copied from the event.

The current record schema is 2. Schema-1 records decode into schema 2 with an
absent details object and remain replayable.

## Persistence

Runtime paths:

```text
world/elarion/catch-telemetry/journal/<player-uuid>/<yyyy-MM>.jsonl
world/elarion/catch-telemetry/players/<player-uuid>.json
```

The journal is append-first. A catch is not applied to an in-memory summary
until its accepted record has been appended successfully. Journal failure
causes processing to fail without mutating the summary.

The player snapshot is an atomic, compact projection. It stores:

- schema version;
- player UUID;
- total caught quantity;
- totals by source ID;
- totals by fish-definition ID;
- totals by rarity ID;
- per-species count and performance projections;
- first and latest catch timestamps;
- checkpoint month and processed line count;
- a bounded recent-catch list for diagnostics and future UI previews.

The snapshot does not copy arbitrary event metadata into aggregate maps.

Writes:

- journal append happens at the acceptance boundary;
- summary snapshots use dirty tracking and atomic replacement on a periodic
  interval, player disconnect, and server shutdown;
- ordinary catch handling performs no global scan;
- filesystem work moves to the Core IO queue only when ordering and failure
  propagation remain explicit.

## Replay And Idempotency

- A loaded snapshot replays only journal lines after its checkpoint.
- Replayed records apply in journal order.
- Reapplying an already checkpointed record is forbidden.
- Replay calls are bounded by physical line count and return `hasMore`.
- The service retains one replay-session map across pages and seeds it from the
  bounded recent-catch window. Exact duplicate event records in that window
  apply once; conflicting reuse of an event ID fails closed.
- Each acceptance, join, or query applies at most one replay page. The server
  tick processes a bounded number of queued player pages once per second.
- Shutdown saves the current dirty projection without performing an unbounded
  backlog drain. Remaining journal records stay authoritative and replay after
  restart.
- A corrupt record fails that player's load with a clear diagnostic; it must
  not silently reset canonical totals.
- A missing or corrupt snapshot requires an explicit rebuild path. Full
  historical journal scans are maintenance work, never an ordinary GUI or
  gameplay query.
- Monthly journal retention or compaction is deferred until real volume data
  exists, but the file layout permits month-bounded maintenance.

## Summary API

`api.catchTelemetry()` returns immutable snapshots and direct indexed totals:

- summary for one player;
- total quantity;
- quantity by fish-definition ID;
- quantity by rarity ID;
- quantity by source ID;
- one direct per-species record by fish-definition ID;
- bounded recent accepted catches.

No API returns mutable internal maps. Queries never trigger replay or disk IO;
join activation and coalesced worker maintenance converge backlogs in bounded
pages. Cross-player rankings use the dedicated metric indexes.

## History And Progression Boundary

The processing service does not automatically:

- grant rewards;
- unlock titles;
- mutate generic progression;
- write Chronicle prose;
- publish newspaper or Ledger entries.

Later consumers may react to accepted summary changes through a separate
Core-owned post-acceptance event. Those consumers must use the accepted event
ID and summary snapshot, not reread Angling state or scan catch journals.

## Failure Semantics

- Invalid telemetry fails before persistence.
- Journal append failure leaves summaries unchanged.
- Summary snapshot failure leaves the journal authoritative and replayable.
- Join, disconnect, periodic-save, and shutdown maintenance failures are
  logged without crashing those server lifecycle callbacks.
- Event emission remains synchronous. Append or replay failure propagates to
  the producer; an append that already succeeded remains authoritative and is
  safe to replay.
- Angling completes telemetry immediately before vanilla fishing loot
  generation. If acceptance fails, the boundary logs and cancels that
  retrieval with zero rod damage; the vanilla reward branch does not run and
  the retry-stable session remains pending.
- Custom Angling rewards remain deferred until their delivery and failure
  semantics are explicitly designed.

## Implementation Slices

1. Complete: added `eventId`, `occurredAt`, the accepted-record model, and pure
   journal codec/path tests without registering a processor.
2. Complete: added player-partitioned forced journal append, bounded replay
   pages, exact deduplication, conflict/corruption diagnostics, and restart
   tests.
3. Complete: added immutable per-player summary models, checkpointed
   projection, atomic storage, dirty tracking, and replay/restart tests.
4. Complete: added `CatchTelemetryService`, server lifecycle/event-bus binding,
   bounded queued replay, dirty-save lifecycle, and `api.catchTelemetry()`.
5. Complete: added schema-2 typed server-outcome details, schema-1 journal and
   summary migration, and direct per-species count/performance projections.
6. Next: add the Core metric projection/ranking service and metric-indexed title
   conditions before Angling emits player-facing catches.
7. Next: wire the reconstructed Angling server session result to telemetry,
   metrics, idempotent rewards, milestones, and tournament association.

## Required Tests

- contract validation for event ID and timestamp;
- accepted-record JSON round trip;
- schema-1 journal and summary migration;
- rich server-outcome round trip and per-species projection;
- journal append and ordered replay;
- duplicate ID applies once inside the replay window;
- append failure does not mutate summary;
- summary round trip and immutable accessors;
- restart replay from checkpoint;
- corrupt journal/snapshot fails clearly without resetting totals;
- quantity accumulation uses overflow-safe arithmetic;
- queries touch one player snapshot/cache and never scan all players;
- server shutdown drains dirty summaries.
