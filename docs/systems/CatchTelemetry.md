# Catch Telemetry

Last reviewed: 2026-06-12

Status: durable event-bus acceptance, append-first journal, bounded replay,
immutable per-player summaries, lifecycle persistence, and read-only Core
queries are implemented. Angling vanilla fishing emission is implemented;
downstream progression consumers are not implemented.

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
  IDs, and bounded immutable metadata.
- `AcceptedCatchRecord` defines the versioned Core-owned durable record shape.
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
  and rarity, bounded newest-first recent records, timestamps, and the replay
  checkpoint.
- `CatchSummaryProjection` advances checkpoints for every successfully replayed
  page, uses overflow-safe counter updates, and caps recent records at 32.
- `CatchSummaryStorage` uses an explicit validated JSON shape and atomic
  replacement under `catch-telemetry/players/`.
- `CatchSummaryRepository` loads one player directly, caches immutable
  summaries, marks only changed players dirty, persists dirty snapshots, and
  retains dirty state after save failure.
- `CatchTelemetryService` is the durable event-bus listener. It appends before
  projection, applies one bounded replay page immediately, queues remaining
  pages by player, and saves dirty summaries periodically, on disconnect, and
  on shutdown.
- Player join and read-only query access activate direct-player replay without
  scanning other players or journal partitions.
- `ElarionCatchTelemetryApi` returns immutable direct-player summaries,
  indexed totals, and bounded recent records through `api.catchTelemetry()`.
- `ElarionEventBus` provides synchronous subscription and emission.
- `AnglingRarity.id()` exposes stable placeholder technical identifiers.

Not implemented:

- a gameplay trigger for Angling catch emission;
- History, title, Chronicle, reward, command, GUI, or gameplay consumers.

Emitting the current event durably appends the accepted catch before updating
the Core summary. It still does not grant rewards or mutate progression.

## Processing Decision

Catch telemetry uses a dedicated Core processing service rather than a
direct `HistoryService` listener.

Current flow:

```text
Angling server-authoritative catch result
  -> CatchTelemetryEvent
  -> Core CatchTelemetryService
  -> append accepted record to player-partitioned journal
  -> apply record once to cached per-player summary
  -> mark summary checkpoint dirty
  -> later consumers query immutable Core summary snapshots
```

Every catch must not become a general `HistoryEvent`. Catch volume can be high,
and copying every catch into the global History JSONL and public-memory index
would create unnecessary storage and Chronicle noise. Dedicated catch storage
is canonical for catch facts. Later noteworthy outcomes, such as a first catch,
rarity milestone, title unlock, or public record, may emit separate Core
history/progression events.

## Contract Hardening

Before durable processing is wired, `CatchTelemetryEvent` must add:

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
- bounded metadata copied from the event.

The record schema is versioned from its first persisted form.

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
- bounded recent accepted catches.

No API returns mutable internal maps. A query may apply one bounded replay page
for that player before returning, so a large backlog converges across bounded
calls/ticks rather than through one unbounded scan. No player-facing consumer
reads journal files directly. Cross-player leaderboards or search require a
separate bounded index before exposure.

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
5. Complete: added an internal server-owned Angling placeholder catch result
   and resolution service that validates the loaded definition and emits
   retry-stable telemetry.
6. Complete: added bounded Angling condition evaluation and deterministic
   weighted candidate selection from the current immutable definition snapshot.
7. Complete: added direct-player ephemeral sessions with reload-stable selected
   IDs, retry-stable completion, and bounded deadline-queue expiry.
8. Complete: added server bobber fishing-position/unload handling and one
   narrow successful-retrieval hook with telemetry-before-vanilla-loot
   ordering.
9. Complete: added one-shot per-cast selection and placeholder-only,
   direct-player rate-limited feedback with nonfatal presentation failure.
10. Next: run dedicated-server fishing smoke validation and specify custom
    reward delivery semantics. Rewards, titles, History milestones, Chronicle
    prose, networking, and UI remain later consumers.

## Required Tests

- contract validation for event ID and timestamp;
- accepted-record JSON round trip;
- journal append and ordered replay;
- duplicate ID applies once inside the replay window;
- append failure does not mutate summary;
- summary round trip and immutable accessors;
- restart replay from checkpoint;
- corrupt journal/snapshot fails clearly without resetting totals;
- quantity accumulation uses overflow-safe arithmetic;
- queries touch one player snapshot/cache and never scan all players;
- server shutdown drains dirty summaries.
