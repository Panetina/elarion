# Chronicle And Public History System

Purpose: store durable audit history, build indexes, generate weekly Chronicles, and serve bounded public-memory views.

Main classes: `HistoryService`, `HistoryStorage`, `HistoryIndexStorage`,
`ChronicleArchiveStorage`, `ElarionPublicHistoryApi`,
`ChronicleRendererRegistry`, `ChronicleRenderer`, `ChronicleProjection`,
`ChronicleRenderContext`, `ChronicleTemplateFamily`, `ChronicleTemplate`,
`ChronicleTemplateLibrary`, `ChronicleVariantSelector`, and
`ChronicleTemplateRenderer`.

Entry points: Core services, history commands, addon history emissions.

Commands: `/e history ...`, `/e history chronicle ...`.

Network packets: none yet; future GUI/search/news views should use bounded public-history APIs.

Website projection: only events explicitly authored through `recordChronicle`
are projected as ordered `chronicle` events through the durable Minecraft bridge
outbox. Ordinary `record` calls remain durable audit History and never become
public news merely because their category is Chronicle-eligible. The website
never scans or receives raw history JSONL, indexes, or archives.

GUI/screens: future Chronicle bookshelf, newspaper, Ledger, NPC rumor, and search views.

Storage/persistence: `world/elarion/history`, `history-index`, `chronicles/weekly`.

Each monthly index has a compact `.summary.json` routing sidecar. Category,
Realm, and player public filters use it to skip known-nonmatching monthly entry
files; a missing or invalid sidecar falls back to the full index.

`history.yml.public-query.max-weeks` hard-caps every public-history request at
52 weeks (including addon API callers). The configured default is clamped to
the same bound, so a caller cannot turn a player-facing archive request into an
unbounded archive/index read. The API's `recentChronicles(...)` and
`recentIndexes(...)` helpers apply the same public-week and configured
month-scan caps; callers cannot bypass bounded storage reads with a large
argument.

Dependencies: Core task service, config recording policy, addon event emissions.

Related systems: Newspapers, Ledger, NPC rumors, Offerings, Government, Economy.

Extension points: `api.history()`, `api.publicHistory()`, category/type
filters, Chronicle text, and registered `ChronicleRenderer` providers.

Risks: raw JSONL scans for player-facing views; noisy event spam; missing chronicle text for major events.

The `history.yml.archive` Chronicle policy is the single eligibility boundary
for weekly archive books, `chronicleLibrary(...)`, and website `chronicle`
projections. It first selects categories, then applies `enabled-chronicle-types`
and `disabled-chronicle-types` (plain `type` or scoped `category:type`). This
filters public Chronicle spam without suppressing the durable audit event.
For live website projection there is an additional deliberate-emission boundary:
an event must have been recorded with `recordChronicle`; raw audit `record`
events are never projected. `recordChronicle` persists the reserved
`chronicle.intent=true` metadata marker, so the same boundary also applies to
weekly archives and the in-game library after restart.

Do not duplicate this system by creating: addon-local history logs, separate newspaper event storage, or unbounded GUI searches.

Public-history projections carry bounded event metadata from `HistoryEvent`
through `HistoryIndexEntry`, `ChronicleEntry`, and `PublicHistoryEntry`. Old
records without metadata load as empty metadata maps. Addons may use this
metadata to render domain-specific readable archive rows, but they must not
read raw history/index/archive files directly or replace structured event data
with preformatted prose.

Core owns the reusable Chronicle projection contract. Addons can register a
`ChronicleRenderer` through `api.publicHistory().registerRenderer(...)`.
Consumers can request a `ChronicleProjection` through
`api.publicHistory().project(entry, context)`. The registry falls back to a
safe title-cased event projection when no addon renderer supports the entry.

Variant IDs use the metadata key `chronicle.variant` when present. If a record
does not carry that key, Core `ChronicleVariantSelector` derives a stable
variant from the event id and template-family id. Records without a registered
template family still fall back to `<category>.<type>.default`. This keeps old
data live-safe and establishes the selected-variant contract without forcing a
broad migration of existing history events.

Template families are Core-owned presentation contracts for library-ready
Chronicle prose. A family declares the category/type match, labels, required
and optional metadata keys, a safe missing-context body, and authored
`ChronicleTemplate` variants. A family is considered library-ready only when it
has at least 10 authored variants. The current skeleton does not persist a
selected variant id, mutate existing history records, or automatically promote
all history events into the future library.

Current narrow consumer: Government registers `GovernmentChronicleText` as a
domain renderer for Civic Forum History and Seat of Rule Archive rows. This is
the first reusable renderer, not a complete migration of all Chronicle prose.
Government `proposal-approved` is the first migrated template-family pilot:
it has 10 authored variants, stable selected ids, required-title fallback, and
focused tests.
Government `proposal-rejected` and `civic-record-created`, Portal
`route-unlocked`, Offering `project-completed`, and Core
`title.progression-unlocked` are also migrated library-ready families. Each has
at least 10 authored variants and a safe missing-context fallback.

Phase 8 direction: the future in-game library must use this public-history
projection boundary, not raw history JSONL. Library-ready event families require
at least 10 authored text variants each. Variant choice must be stable per
event: use `chronicle.variant` when persisted, otherwise derive a deterministic
variant from the event id and template family. Do not make Chronicle wording
change every time a player opens the library.

This is a project rule, not only a Phase 8 preference. Future player-facing
systems such as war, peace, revolution, imperial succession, story quests,
seasonal events, NPC relationship milestones, Realm diplomacy, mounts, pets,
and major rewards must add their Chronicle families with 10 variants, required
metadata checks, fallback text, and tests in the same slice that promotes them
to Chronicle/library visibility.

Current promoted families:

- Government `proposal-approved`, `proposal-rejected`, and
  `civic-record-created`.
- Portal `route-unlocked`.
- Offering `project-completed`, `project-force-completed`, and
  `realm-global-access-changed`.
- Core `title.progression-unlocked`.
- Underworld `death-pve`, `death-pvp`, `death-suicide`, `death-void`, and
  `true-death`.

The in-game library query boundary is `ElarionPublicHistoryApi` and its
`chronicleLibrary(realmId, limit)` helper. Future UI must request bounded
`PublicHistoryResult` snapshots and project each row through
`api.publicHistory().project(...)`; it must not read raw history JSONL files.

## In-Game Library Status And Delivery Boundary

Not implemented: custom library blocks, copied/placed library structures,
book-item generation, shelves that receive Chronicle volumes, formatted book
pages, and a player-facing library screen. Existing Chronicle archives,
projections, renderers, and `chronicleLibrary(...)` are the server-side source
boundary only; they are not a physical-library feature.

When the library slice is scheduled, it must keep this ownership split:

- Core History remains the only owner of Chronicle eligibility, archives,
  library query results, and formatted `ChronicleProjection` data.
- The library addon or Core presentation slice may own block definitions,
  structure placement, page layout, navigation, and client assets. It must not
  copy history into block entities, item NBT, or a second archive store.
- A shelf/volume stores only stable references such as archive week or event
  IDs. Opening a volume resolves a fresh bounded Core snapshot and degrades to
  a visible "record unavailable" state if archival retention removed it.
- The same configured Chronicle policy controls archive books, the in-game
  library, and website projections. A type excluded as spam is not published
  by any of those surfaces, while its audit event remains durable.

The first implementation slice should be deliberately narrow: one server-owned
library block, one bounded archive list, one immutable weekly volume format,
and no global scans on interaction. It needs tests for permission/range,
restart and missing-archive behavior, policy filtering, formatted pagination,
and no block-entity history duplication. Website and launcher work stays on
the bridge boundary in `MinecraftBridge.md`; it must not become an alternate
Chronicle store.

Variant persistence strategy: current records are live-safe and deterministic
without write-back. Existing `chronicle.variant` metadata remains authoritative;
missing metadata is derived from event id plus family id. Future event emitters
may stamp `chronicle.variant` at emission time once their owning family is
registered and stable. Do not rewrite old history/archive files or perform
first-render write-back without a separate persistence migration proposal.

## Verification Contract

Every promoted family needs selector and rendering coverage for deterministic
choice, required/optional metadata, and its missing-context fallback. Public
views need bounded-query and archive metadata round-trip coverage. A physical
library additionally needs permission/range, restart, missing-archive,
pagination, and no-block-entity-history-duplication tests before release.
