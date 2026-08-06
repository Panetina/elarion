# Elarion History

Last reviewed: 2026-06-11

Author: Panyel  
Team: Panetina Team

Core owns durable history events. History is the raw source for Chronicles,
public memory, audit views, newspapers, NPC rumors, ledgers, GUI search, and
administrative inspection.

## Storage

Runtime path:

```text
world/elarion/history/<yyyy-MM>.jsonl
```

Each line is one `HistoryEvent` JSON object. The JSONL shape is stable:

- `id`
- `timestamp`
- `category`
- `type`
- `actorId`
- `subjectType`
- `subjectId`
- `realmId`
- `metadata`
- `chronicleText`

Writes are queued and batched by monthly target file. Pending writes flush
periodically, at batch threshold, and on server shutdown before task workers
stop. A failed target remains queued ahead of newer entries for retry; a
blocking flush fails explicitly instead of silently discarding canonical audit
events. Targets confirmed earlier in the same batch are not replayed.

## Monthly Index

Core also writes a compact monthly index for public-memory systems:

```text
world/elarion/history-index/<yyyy-MM>.json
world/elarion/history-index/<yyyy-MM>.summary.json
```

The index is a projection, not the audit log. It contains:

- month bounds
- total event count
- category counts
- type counts
- Realm counts
- player counts
- lightweight event references with `eventId`, `timestamp`, category, type,
  actor, subject, Realm, and `chronicleText`

The paired `.summary.json` sidecar contains only month routing metadata.
Category, Realm, and player public filters consult it before deserializing the
full monthly index, so known-nonmatching months do not add entry IO. Missing or
invalid sidecars fall back to the full index to preserve results.
Both index and sidecar use temporary-file replacement with an atomic move when
the filesystem supports it. A failed index write therefore leaves the previous
parseable projection intact; failed projection entries remain queued for retry,
and a blocking flush fails explicitly. The JSONL audit log remains canonical
regardless.

It intentionally does not copy full event metadata. Full detail remains in the
raw JSONL history files.

Chronicle, search, newspaper, ledger, NPC rumor, and public-memory views read
this index or weekly archive summaries first. They should not scan raw JSONL
unless an OP audit path explicitly needs full event detail.

## Weekly Chronicles

Core generates immutable weekly Chronicle archives from completed calendar
weeks. Weeks use the server timezone and start on Monday. Automatic generation
runs through the Core task queues instead of ordinary server-tick work. It
filters monthly index streams by the target week and Chronicle policy before
materializing archive rows; it does not first copy every loaded index row into
a combined intermediate list.

Runtime path:

```text
world/elarion/chronicles/weekly/<yyyy-MM-dd>.json
```

The filename is the week start date. Existing archive files are not overwritten.
If a week was already archived, later generation attempts skip rebuilding it and
leave it unchanged.
Archive writes use a temporary file and atomic move where the filesystem
supports it.

Each `ChronicleArchive` stores:

- week bounds
- generated timestamp
- total event count
- category, type, Realm, and player counts
- compact `ChronicleEntry` records with reader-facing text

Chronicles are summaries for public memory and presentation. They are not the
full audit log. Full event metadata remains in JSONL history.

Editable archive controls live in `history.yml`:

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

`chronicle-categories` decides which categories can enter weekly archives.
It is the same eligibility boundary used by Chronicle library reads and
website `chronicle` projections. `default-chronicle-type-enabled`,
`enabled-chronicle-types`, and `disabled-chronicle-types` provide the next,
more precise filter. Type rules may be `type` or `category:type`; use the
disabled list to suppress spam from public Chronicle outputs while leaving the
underlying audit event intact. The live website bridge has a second intentional
boundary: it only projects events emitted with `recordChronicle`; ordinary
`record` calls are audit-only and cannot turn operational activity into public
Chronicle news. `recordChronicle` persists `chronicle.intent=true`; archive and
library reads require that marker too, so restart-safe book volumes follow the
same anti-spam rule.
When the default is false, only the explicit allow-list can enter a Chronicle.

Major systems should emit short, immersive `chronicleText` for events that are
expected to appear in Chronicles, newspapers, NPC rumors, ledgers, or public
search.

## Public History API

Addons should consume public memory through `api.publicHistory()` instead of
reading history files directly.

Available consumers:

- `CHRONICLE`
- `NEWSPAPER`
- `LEDGER`
- `NPC_RUMOR`
- `GUI_SEARCH`

Core composes public results from recent weekly Chronicle archives and live
monthly indexes, deduplicates events, applies category/Realm/player/text
filters, and returns `PublicHistoryResult` entries with a source marker:
`chronicle` or `live-index`.

Convenience methods:

```java
api.publicHistory().newspaper(realmId, limit);
api.publicHistory().ledger(playerId, limit);
api.publicHistory().npcRumors(realmId, limit);
api.publicHistory().search(text, limit);
api.publicHistory().recentChronicles(weeks);
```

OP inspection commands:

```text
/e history chronicle list [weeks]
/e history chronicle inspect <week> [limit]
```

These commands are the server-side validation path before Chronicle bookshelf,
book GUI, newspaper, ledger, and NPC rumor consumers are built.

Default public query bounds live in `history.yml`:

```yaml
public-query:
  default-weeks: 8
  default-limit: 50
  max-limit: 200
```

## Recording Policy

Editable config:

```text
config/elarion/core/history.yml
```

`history.yml` can disable all recording, opt categories/types in or out, or
filter scoped event types such as `citizen:realm-assigned`.

The policy is evaluated when `HistoryService.record(...)` is called. Disabled
events are not written to disk. Callers still receive the created event object,
so gameplay code does not need branching for history storage policy.

Use category/type filters carefully:

- Disable noisy development events if they are not useful.
- Chat recording is disabled by generated defaults because chat can grow
  history quickly. Enable `chat` only if you want chat auditing.
- Keep diplomacy, treasury, title, security, Realm, and administration events
  enabled when they are needed for audit or Chronicles.
- Do not use history filtering to hide moderation or security decisions from
  required audit workflows once those systems exist.

## Scaling Direction

Current live history queries drain pending writes, scan newest monthly JSONL
files first, and stop at the configured month bound. This keeps ordinary OP
commands from reading every historical file forever.

Public views should use `api.publicHistory()` first. That path reads Chronicle
archives and monthly index projections instead of full JSONL event metadata.
Its category, Realm, and player filters use monthly summary sidecars to skip
known-nonmatching full index files. Text search remains conservative and reads
the selected bounded indexes.

Editable query controls live in `history.yml`:

```yaml
query:
  max-months-scanned: 3
  command-limit-max: 100
```

Future rich views should still add dedicated consumer-specific indexes or
archive summaries when the basic public-history API is not selective enough.

Query scaling should prefer:

- monthly file bounds, already used by live queries
- category/type indexes, already summarized per month
- Realm/player indexes, already summarized per month
- weekly Chronicle archive summaries, already used by public-history queries
- dedicated newspaper/search/ledger indexes only when real GUI usage proves
  the generic public-history API is too broad
- bounded query limits
- compute-queue archive generation

Avoid:

- loading every history file for one ordinary GUI
- parsing history every tick
- storing large history blobs in block entities
- changing the stable event JSONL shape without migration

## Chronicle Preparation

When emitting major events, include reader-friendly `chronicleText` when the
event is likely to appear in a weekly Chronicle. Keep prose immersive but short.

Physical Chronicle libraries, custom shelves, copied library structures, and
formatted in-game books are not implemented yet. They must resolve bounded
Core archive/projection data at read time rather than copying history into
block entities, book NBT, or addon-local files. The delivery boundary and
required tests are maintained in `docs/systems/Chronicles.md`.

Good Chronicle source events include:

- Realm founding, hiding, relationship, and diplomacy events
- leader, office, government, law, and treasury events
- title unlocks, unique claims, and failed unique claims
- major rewards and Realm deliveries
- world, portal, war, security, and administration events
- contribution project milestones and public ceremonies

## Curated Folklore

Curated lost-age books under `lore/folklore/` are a separate source class. They
are authored, stable-numbered records rather than summaries of runtime events.
Future archive presentation may display Folklore and Chronicles through one
reader interface, but Core must not merge their source ownership or treat
Folklore Markdown as live history storage.
