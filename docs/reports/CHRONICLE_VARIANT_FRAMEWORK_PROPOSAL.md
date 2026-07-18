# Chronicle Variant Framework Proposal

Audit date: 2026-07-10

Scope: Phase 8, Slice 1 - Chronicle variant framework audit/proposal. This
slice did not modify production Java, resources, config formats, packets, UI,
or persistence.

## Objective

Prepare the Chronicle/public-history system for the future in-game library by
defining how Elarion will author and select varied Chronicle prose without
duplicating history state or making old records unstable.

User decision captured in this slice: every event family that becomes
library-ready must have at least 10 authored text variants. "Random" means a
stable selected variant per event, not a sentence that changes every time the
player opens the library.

## Current Architecture Findings

- Core owns durable history and public-history infrastructure through
  `HistoryService`, `HistoryStorage`, `HistoryIndexStorage`,
  `ChronicleArchiveStorage`, `ElarionPublicHistoryApi`, and
  `ChronicleRendererRegistry`.
- Structured event data currently flows from `HistoryEvent` into
  `HistoryIndexEntry`, `ChronicleEntry`, and `PublicHistoryEntry`, including
  bounded metadata maps. This is the right foundation for readable library
  text.
- `ChronicleRendererRegistry` already exposes the variant metadata key
  `chronicle.variant` and falls back to deterministic default ids, but it does
  not yet own an authored template library or selected-variant persistence.
- Government is the first real domain renderer through
  `GovernmentChronicleText`. It currently has one or a few prose strings per
  handled event type and should be the first migration target.
- Portals and Offerings already emit Chronicle events, but their text is still
  generic fallback prose.
- Core also records many non-Chronicle audit/history events. These should not
  automatically become library-facing stories; only meaningful player-facing
  lifecycle events should be promoted.
- Public-history queries are bounded through archives and live indexes. The
  future library must consume this API and must not scan raw JSONL history
  files.

## Required Model

The next implementation should add a Core-owned template library with concepts
equivalent to:

- `ChronicleTemplate`
- `ChronicleTemplateFamily`
- `ChronicleTemplateLibrary`
- `ChronicleTemplateRenderer`
- `ChronicleVariantSelector`

Names should follow source conventions after implementation starts.

Each template family should define:

- stable family id, usually `<category>.<event-family>`
- event category and one or more event types
- title/category/detail-label strategy
- at least 10 authored body variants before the family is library-ready
- required metadata keys
- optional metadata keys
- safe missing-context fallback
- visibility notes when relevant

Variant selection must be stable:

- If metadata already contains `chronicle.variant`, use it.
- Otherwise derive a deterministic variant from event id plus family id.
- A later persistence slice may stamp `chronicle.variant` at event creation or
  first projection, but that requires a separate persistence-safe proposal.

Do not use true runtime randomness on every render. A Chronicle entry should
read the same after server restart, archive generation, and future library
openings.

## Initial Event Families

These are the first target families for the library. Each family should receive
at least 10 authored variants before it is considered complete.

- character death
- True Death
- murder or player-caused death when reliably classified
- government leader death
- office holder death
- election victory
- election loss or failed candidacy
- election tie
- election reopened or vacancy election
- law/proposal created
- law/proposal passed
- law/proposal failed
- law repealed
- government founded or founding phase completed
- government dissolved or transformed
- office granted
- office vacated
- title unlocked
- title revoked where supported
- Realm joined
- Realm left
- Realm founded
- Realm identity/name/color/form chosen
- group founded
- shrine milestone completed
- major Offering completed
- portal route opened
- portal travel milestone when meaningful
- questline completed
- major NPC relationship change
- mount unlocked
- future pet unlocked
- major reward unlocked
- major Worldheart/Realm treasury or tax event when player-facing

Routine chat messages, ordinary transactions, repeated progress ticks, UI
browsing, diagnostics, and noisy admin/audit events should stay out of the
player-facing library unless a later slice explicitly promotes them.

## Authoring Rules

- Structured event data remains the source of truth. Do not persist only final
  English prose.
- Variants must be hand-authored enough to read naturally. Avoid meaningless
  word-swapping combinations.
- Use metadata placeholders only when the emitter reliably supplies them.
- Missing names/counts/Realm ids must degrade to clear fallback text.
- Do not use words such as "murdered" unless event data reliably supports that
  classification.
- Avoid sensitive/private data in public Chronicle variants. Use profile and
  public-history visibility rules before syncing to clients.
- Addon renderers may own domain phrasing, but Core owns the registry,
  selection contract, fallback behavior, and future library query boundary.

## Recommended Implementation Slices

### Phase 8 Slice 2 - Core Template Library Skeleton

Classification: Medium.

Add the reusable Core template/variant selector API and tests. Do not migrate
all domains yet.

Expected files:

- `platform/core/src/main/java/panetina/elarion/core/model/`
- `platform/core/src/main/java/panetina/elarion/core/service/`
- `platform/core/src/test/java/panetina/elarion/core/service/`
- `docs/systems/Chronicles.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- deterministic selector test
- existing `chronicle.variant` metadata wins test
- missing metadata fallback test
- projection fallback compatibility test

### Phase 8 Slice 3 - Government Template Pilot

Classification: Medium.

Migrate one narrow Government family, preferably proposal/law outcomes or
office changes, to the template library with at least 10 variants per migrated
family.

Verification:

- Government projection tests for stable variant id and readable fallback.
- Existing Civic Forum/Seat of Rule archive row visibility still works.

### Phase 8 Slice 4 - Government Election And Founding Families

Classification: Medium or High depending on breadth.

Add 10+ variants each for founding election completion, election reopened,
candidate nominated, Realm identity/form/founding choices, and vacancy events.

### Phase 8 Slice 5 - Portal And Offering Families

Classification: Medium.

Add renderers or template-family registrations for Portal route/window/travel
milestones and Offering milestone/project completion events.

### Phase 8 Slice 6 - Core Death, Title, Reward, Realm Families

Classification: High.

Add or promote Core-owned player-facing families for death lifecycle, titles,
rewards, and Realm membership/identity events. This likely touches event
emission policy and requires careful visibility checks.

### Phase 8 Slice 7 - In-Game Library Query Contract

Classification: Medium.

Design the library/bookshelf API around bounded public-history queries and
projected Chronicle text. Do not build a raw history-file browser.

### Phase 8 Slice 8 - Variant Persistence Strategy

Classification: High.

Decide whether `chronicle.variant` should be stamped at event emission, archive
generation, or first projection. Any write-back/migration behavior requires its
own persistence-safe proposal.

## Verification Plan

- Core variant selector unit tests.
- Template rendering tests for required/optional metadata.
- Government renderer tests for migrated families.
- Public-history bounded query tests.
- Archive round-trip tests preserving metadata.
- Missing-context fallback tests.
- Manual in-game library QA only after the API and first renderer families are
  stable.

## Deferred Work

- No in-game library UI was implemented in this slice.
- No broad Chronicle text family was authored in this slice.
- No history persistence migration was attempted.
- No existing Chronicle records were rewritten.
- No addon event policy was changed.

## Next Recommended Slice

Phase 8 Slice 2 - Core Template Library Skeleton.

Recommended model: Medium. Use High only if also migrating Government families
or adding large authored variant sets in the same turn.
