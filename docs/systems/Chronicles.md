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

Website projection: accepted public Chronicle categories are projected as
ordered `chronicle` events through the durable Minecraft bridge outbox. The
website never scans or receives raw history JSONL, indexes, or archives.

GUI/screens: future Chronicle bookshelf, newspaper, Ledger, NPC rumor, and search views.

Storage/persistence: `world/elarion/history`, `history-index`, `chronicles/weekly`.

Dependencies: Core task service, config recording policy, addon event emissions.

Related systems: Newspapers, Ledger, NPC rumors, Offerings, Government, Economy.

Extension points: `api.history()`, `api.publicHistory()`, category/type
filters, Chronicle text, and registered `ChronicleRenderer` providers.

Risks: raw JSONL scans for player-facing views; noisy event spam; missing chronicle text for major events.

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

Current proposal: `docs/reports/CHRONICLE_VARIANT_FRAMEWORK_PROPOSAL.md`.
The in-game library query boundary is `ElarionPublicHistoryApi` and its
`chronicleLibrary(realmId, limit)` helper. Future UI must request bounded
`PublicHistoryResult` snapshots and project each row through
`api.publicHistory().project(...)`; it must not read raw history JSONL files.

Variant persistence strategy: current records are live-safe and deterministic
without write-back. Existing `chronicle.variant` metadata remains authoritative;
missing metadata is derived from event id plus family id. Future event emitters
may stamp `chronicle.variant` at emission time once their owning family is
registered and stable. Do not rewrite old history/archive files or perform
first-render write-back without a separate persistence migration proposal.
