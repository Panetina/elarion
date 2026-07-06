# Elarion Project Index

This is the project dictionary, ownership map, and source location index. Read
this before broad repository searches.

## Canonical Identity

| Concept | Value | Owner |
| --- | --- | --- |
| Product name | `Elarion` | Project policy |
| Mod ID | `elarion` | Bootstrap metadata |
| Resource namespace | `elarion` | Resource/data layer |
| Minecraft target | `1.21.1` | Build |
| Loader | Fabric | Platform boundary |
| Java version | 21 | Build |
| Java package root | `panetina.elarion.addons.angling` | Addon source |
| Mapping set | Yarn v2 | Monorepo build |

## Source-of-Truth Documents

| File | Owns |
| --- | --- |
| `AGENTS.md` | Agent workflow and required reading |
| `RULES.md` | Permanent policy, architecture, restrictions, release gates |
| `INDEX.md` | Names, ownership, code locations, APIs, persistence map |
| `TODO.md` | Current work and recently completed work |
| `PLANS.md` | Stable phased port plan |
| `REPLACE.md` | Asset, text, name, UI, and content replacement inventory |
| `VISUAL_ASSET_REDIRECT.md` | Current visual asset handoff checklist and working fish-name map |
| `LICENSE` | MIT license and preserved upstream notice |
| `NOTICE.md` | Provenance, attribution, and excluded upstream material |
| `reference/README.md` | Local upstream reference contract and commit |

## Reference Baseline

| Field | Value |
| --- | --- |
| Repository | `https://github.com/wdiscute/starcatcher` |
| Branch | `2.4-neoforge-1.21.1` |
| Commit | `06b2bd98c8db30f9eacfebfab04aa070e28a4e8b` |
| Local checkout | `reference/upstream-starcatcher-neoforge-1.21.1/` |
| Parent Git status | Ignored; must remain untracked |
| Upstream Java files | 364 |
| Upstream tests found | 0 |

The reference checkout is not part of Elarion and is never a build source.

## Current Code Map

| Path | Owner | Purpose | Provenance | Tests |
| --- | --- | --- | --- | --- |
| `build.gradle` | Build | Register the module as an Elarion Fabric addon with Core dependency and packaged notices | New Elarion code | `:addons:angling:build` |
| `../../dev/build.gradle` | Development runtime | Load Angling in the aggregate client and dedicated-server development runtime | New Elarion integration | `:dev:runServer` |
| `src/main/java/panetina/elarion/addons/angling/AnglingItems.java` | Angling content registration | Register the temporary catch fallback, bait item, and current fish-specific reward items without static Minecraft item initialization in pure tests | New Elarion code | `AnglingRewardDeliveryServiceTest`, `:addons:angling:build` |
| `src/main/java/panetina/elarion/addons/angling/ElarionAnglingAddon.java` | Bootstrap | Core addon entrypoint, definition reload, bounded session/feedback lifecycle, and vanilla bobber event registration | New Elarion code | `:addons:angling:build` |
| `src/main/java/panetina/elarion/addons/angling/model/AnglingRarity.java` | Angling definitions | Neutral placeholder rarity values for fish definitions | New Elarion code | `FishDefinitionTest`, `FishDefinitionIndexTest` |
| `src/main/java/panetina/elarion/addons/angling/model/AnglingConditionId.java` | Angling definitions | Validated condition identifier wrapper with local Angling namespace defaulting | New Elarion code | `FishDefinitionTest`, `FishDefinitionIndexTest` |
| `src/main/java/panetina/elarion/addons/angling/model/FishDefinition.java` | Angling definitions | Immutable placeholder fish definition with placeholder-mode validation | New Elarion code | `FishDefinitionTest` |
| `src/main/java/panetina/elarion/addons/angling/model/FishDefinitionIndex.java` | Angling definition index | Immutable construction-time fish lookup by ID, rarity, and condition | New Elarion code | `FishDefinitionIndexTest` |
| `src/main/java/panetina/elarion/addons/angling/model/FishDefinitionValidationException.java` | Angling definitions | Testable validation failure for fish model and index construction | New Elarion code | `FishDefinitionTest`, `FishDefinitionIndexTest` |
| `src/main/java/panetina/elarion/addons/angling/model/AnglingCatchResult.java` | Angling catch resolution | Immutable retry-safe resolved catch fact that converts to Core telemetry without visible content | New Elarion code | `AnglingCatchResultTest`, `AnglingCatchResolutionServiceTest` |
| `src/main/java/panetina/elarion/addons/angling/model/AnglingFishingSession.java` | Angling sessions | Immutable compact active-session, captured bait ID, and retry-stable completion state | New Elarion code | `AnglingFishingSessionTest`, `AnglingFishingSessionServiceTest` |
| `src/main/java/panetina/elarion/addons/angling/condition/AnglingConditionContext.java` | Angling selection | Immutable server condition snapshot with validated time/weather and mod-compatible technical IDs | New Elarion code | `AnglingConditionContextTest` |
| `src/main/java/panetina/elarion/addons/angling/condition/AnglingConditionEvaluator.java` | Angling selection | Pure condition predicate contract | New Elarion code | `AnglingConditionRegistryTest`, `FishCandidateSelectorTest` |
| `src/main/java/panetina/elarion/addons/angling/condition/AnglingConditionRegistry.java` | Angling selection | Bounded condition evaluator registry with duplicate rejection and fail-closed lookup | New Elarion code | `AnglingConditionRegistryTest` |
| `src/main/java/panetina/elarion/addons/angling/condition/AnglingBuiltinConditions.java` | Angling selection | Register Elarion-owned placeholder condition evaluators for fluid, dimension, weather, time, elevation, biome, and bait presence | Adapted category pattern only from reference restrictions; IDs and implementation are new Elarion code | `AnglingBuiltinConditionsTest`, `PlaceholderFishResourceTest` |
| `src/main/java/panetina/elarion/addons/angling/condition/AnglingBaitContextResolver.java` | Angling selection | Resolve and consume the technical placeholder bait ID from player hands for server-owned condition context and successful custom catches | New Elarion code | `AnglingBaitContextResolverTest`, `PlaceholderFishResourceTest` |
| `src/main/java/panetina/elarion/addons/angling/loader/FishDefinitionLoader.java` | Angling definitions | Pure JSON-to-index loader for placeholder fish definitions before Fabric reload integration | New Elarion code | `FishDefinitionLoaderTest` |
| `src/main/java/panetina/elarion/addons/angling/loader/FishDefinitionParseException.java` | Angling definitions | Document and field-scoped parse failure for definition loading diagnostics | New Elarion code | `FishDefinitionLoaderTest` |
| `src/main/java/panetina/elarion/addons/angling/resource/FishDefinitionRepository.java` | Angling definition index | Atomic holder for the currently published immutable fish definition snapshot | New Elarion code | `FishDefinitionRepositoryTest` |
| `src/main/java/panetina/elarion/addons/angling/resource/FishDefinitionResourceReloadListener.java` | Fabric resource boundary | Server-data reload adapter for `data/elarion_angling/angling/fish/*.json` | New Elarion code | `FishDefinitionResourceReloadListenerTest` |
| `src/main/java/panetina/elarion/addons/angling/service/AnglingCatchResolutionService.java` | Angling catch resolution | Validate loaded definitions, assign server-owned identity/time, and emit technical Core telemetry | New Elarion code | `AnglingCatchResolutionServiceTest` |
| `src/main/java/panetina/elarion/addons/angling/service/FishCandidateSelector.java` | Angling selection | Current-snapshot eligibility filtering and deterministic overflow-safe weighted selection | New Elarion code | `FishCandidateSelectorTest` |
| `src/main/java/panetina/elarion/addons/angling/service/AnglingFishingSessionService.java` | Angling sessions | Direct-player start/complete/cancel and bounded deadline-queue expiry | New Elarion code | `AnglingFishingSessionServiceTest` |
| `src/main/java/panetina/elarion/addons/angling/service/AnglingFishingTriggerService.java` | Angling gameplay boundary | Replace stale cast state, complete the direct-player session with captured bait context, and cancel unfinished casts | New Elarion code | `AnglingFishingTriggerServiceTest` |
| `src/main/java/panetina/elarion/addons/angling/service/AnglingFeedbackService.java` | Angling presentation | Direct-player accepted/unavailable action-bar cooldowns with one bounded ephemeral map value per player | New Elarion code | `AnglingFeedbackServiceTest` |
| `src/main/java/panetina/elarion/addons/angling/service/AnglingFishDisplayNames.java` | Angling rewards | Map current technical placeholder fish IDs to original working display names for snapshotted catch rewards | New Elarion code | `AnglingRewardDeliveryServiceTest` |
| `src/main/java/panetina/elarion/addons/angling/service/AnglingRewardDeliveryService.java` | Angling rewards | Convert accepted catch results into deterministic Core deferred reward grants for fish-specific reward items with a generic fallback | New Elarion code | `AnglingRewardDeliveryServiceTest`, `AnglingFishingSessionServiceTest` |
| `src/main/java/panetina/elarion/addons/angling/integration/VanillaFishingHooks.java` | Fabric fishing boundary | Convert server bobber fishing position/unload/retrieval/disconnect into authoritative direct session operations, successful bait consumption, and vanilla bobber cleanup | New Elarion code | `AnglingFishingTriggerServiceTest`, `:addons:angling:build` |
| `src/main/java/panetina/elarion/addons/angling/mixin/FishingBobberEntityMixin.java` | Fabric fishing boundary | Attempt selection once per bobber and complete Core telemetry immediately before vanilla successful-catch loot generation | New Elarion code | `:addons:angling:build` |
| `src/test/java/panetina/elarion/addons/angling/model/FishDefinitionTest.java` | Angling tests | Validate placeholder fish definition and condition ID rules | New Elarion code | `:addons:angling:test` |
| `src/test/java/panetina/elarion/addons/angling/model/FishDefinitionIndexTest.java` | Angling tests | Validate duplicate rejection, lookup behavior, grouping, and immutability | New Elarion code | `:addons:angling:test` |
| `src/test/java/panetina/elarion/addons/angling/model/CatchTelemetryContractTest.java` | Angling tests | Validate translation from Angling definition/rarity IDs into the Core telemetry contract | New Elarion code | `:addons:angling:test` |
| `src/test/java/panetina/elarion/addons/angling/loader/FishDefinitionLoaderTest.java` | Angling tests | Validate pure JSON loading, diagnostics, duplicate rejection, and deterministic ordering | New Elarion code | `:addons:angling:test` |
| `src/test/java/panetina/elarion/addons/angling/resource/FishDefinitionRepositoryTest.java` | Angling tests | Validate empty snapshot, publication, and failed reload preserving previous snapshot | New Elarion code | `:addons:angling:test` |
| `src/test/java/panetina/elarion/addons/angling/resource/FishDefinitionResourceReloadListenerTest.java` | Angling tests | Validate resource path filtering, diagnostic document IDs, and deterministic ordering | New Elarion code | `:addons:angling:test` |
| `src/test/java/panetina/elarion/addons/angling/resource/PlaceholderFishResourceTest.java` | Angling tests | Smoke-test the packaged placeholder fish JSON through the pure loader and indexes | New Elarion code | `:addons:angling:test` |
| `src/test/java/panetina/elarion/addons/angling/model/AnglingCatchResultTest.java` | Angling tests | Validate Core contract conversion, immutable empty metadata, and quantity validation | New Elarion code | `:addons:angling:test` |
| `src/test/java/panetina/elarion/addons/angling/service/AnglingCatchResolutionServiceTest.java` | Angling tests | Validate known-definition resolution, event-bus emission, rejection, and retry-stable identity | New Elarion code | `:addons:angling:test` |
| `src/test/java/panetina/elarion/addons/angling/condition/AnglingConditionContextTest.java` | Angling tests | Validate context identifiers, time bounds, and weather consistency | New Elarion code | `:addons:angling:test` |
| `src/test/java/panetina/elarion/addons/angling/condition/AnglingConditionRegistryTest.java` | Angling tests | Validate fail-closed matching, duplicate rejection, immutability, and evaluator limits | New Elarion code | `:addons:angling:test` |
| `src/test/java/panetina/elarion/addons/angling/condition/AnglingBuiltinConditionsTest.java` | Angling tests | Validate built-in condition registration, namespace ownership, and technical context matching | New Elarion code | `:addons:angling:test` |
| `src/test/java/panetina/elarion/addons/angling/condition/AnglingBaitContextResolverTest.java` | Angling tests | Validate placeholder bait detection from offhand/main-hand technical item IDs | New Elarion code | `:addons:angling:test` |
| `src/test/java/panetina/elarion/addons/angling/service/FishCandidateSelectorTest.java` | Angling tests | Validate filtering, deterministic weighted intervals, empty results, bounds, and snapshot replacement | New Elarion code | `:addons:angling:test` |
| `src/test/java/panetina/elarion/addons/angling/model/AnglingFishingSessionTest.java` | Angling tests | Validate immutable completion transition and timestamp invariants | New Elarion code | `:addons:angling:test` |
| `src/test/java/panetina/elarion/addons/angling/service/AnglingFishingSessionServiceTest.java` | Angling tests | Validate one-session ownership, completion, retry, reload stability, cancellation, and bounded expiry | New Elarion code | `:addons:angling:test` |
| `src/test/java/panetina/elarion/addons/angling/service/AnglingFishingTriggerServiceTest.java` | Angling tests | Validate stale-cast replacement, one-shot completion, cancellation without telemetry, and fresh cast after cancellation | New Elarion code | `:addons:angling:test` |
| `src/test/java/panetina/elarion/addons/angling/service/AnglingFeedbackServiceTest.java` | Angling tests | Validate independent cooldowns and direct-player/all-state cleanup | New Elarion code | `:addons:angling:test` |
| `src/test/java/panetina/elarion/addons/angling/service/AnglingRewardDeliveryServiceTest.java` | Angling tests | Validate deterministic placeholder reward grant IDs/actions, duplicate idempotency, and failure propagation for retry | New Elarion code | `:addons:angling:test` |
| `../../platform/core/src/main/java/panetina/elarion/core/model/AcceptedCatchRecord.java` | Core telemetry | Versioned immutable accepted-catch persistence shape | New Elarion code | `AcceptedCatchRecordTest` |
| `../../platform/core/src/main/java/panetina/elarion/core/storage/CatchTelemetryJournalCodec.java` | Core telemetry storage | Explicit JSONL encoding/decoding and UTC player/month journal path contract | New Elarion code | `CatchTelemetryJournalCodecTest` |
| `../../platform/core/src/main/java/panetina/elarion/core/storage/CatchTelemetryFormatException.java` | Core telemetry storage | Document-scoped malformed-record diagnostic | New Elarion code | `CatchTelemetryJournalCodecTest` |
| `../../platform/core/src/main/java/panetina/elarion/core/model/CatchJournalCheckpoint.java` | Core telemetry storage | Validated player journal month/line replay cursor | New Elarion code | `CatchJournalCheckpointTest`, `CatchTelemetryJournalStorageTest` |
| `../../platform/core/src/main/java/panetina/elarion/core/model/CatchJournalReplay.java` | Core telemetry storage | Immutable bounded replay-page result | New Elarion code | `CatchTelemetryJournalStorageTest` |
| `../../platform/core/src/main/java/panetina/elarion/core/storage/CatchTelemetryJournalStorage.java` | Core telemetry storage | Forced player/month append and bounded checkpoint replay with exact deduplication | New Elarion code | `CatchTelemetryJournalStorageTest` |
| `../../platform/core/src/main/java/panetina/elarion/core/model/CatchSummary.java` | Core telemetry summary | Immutable consistent per-player totals, timestamps, checkpoint, and bounded recent records | New Elarion code | `CatchSummaryTest`, `CatchSummaryProjectionTest` |
| `../../platform/core/src/main/java/panetina/elarion/core/service/CatchSummaryProjection.java` | Core telemetry summary | Overflow-safe replay-page projection into immutable summary indexes | New Elarion code | `CatchSummaryProjectionTest` |
| `../../platform/core/src/main/java/panetina/elarion/core/storage/CatchSummaryCodec.java` | Core telemetry storage | Explicit validated summary snapshot JSON format | New Elarion code | `CatchSummaryStorageTest` |
| `../../platform/core/src/main/java/panetina/elarion/core/storage/CatchSummaryStorage.java` | Core telemetry storage | Direct per-player load and atomic summary snapshot replacement | New Elarion code | `CatchSummaryStorageTest` |
| `../../platform/core/src/main/java/panetina/elarion/core/service/CatchSummaryRepository.java` | Core telemetry summary | Direct player cache, dirty tracking, projection, and retry-safe snapshot saves | New Elarion code | `CatchSummaryRepositoryTest` |
| `../../platform/core/src/main/java/panetina/elarion/core/service/CatchTelemetryService.java` | Core telemetry processing | Append-first event acceptance, bounded player replay queue, lifecycle saves, and corruption isolation | New Elarion code | `CatchTelemetryServiceTest` |
| `../../platform/core/src/main/java/panetina/elarion/core/api/ElarionCatchTelemetryApi.java` | Core telemetry API | Immutable direct-player summary and indexed total queries | New Elarion code | `ElarionCatchTelemetryApiTest` |
| `src/main/resources/fabric.mod.json` | Metadata | Fabric metadata for `elarion_angling` | New Elarion code | `:addons:angling:processResources` |
| `src/main/resources/elarion_angling.mixins.json` | Fabric metadata | Required server mixin registration for the successful vanilla retrieval boundary | New Elarion code | `:addons:angling:build` |
| `src/main/resources/assets/elarion_angling/lang/en_us.json` | Resources | Working original fish and item display names plus placeholder feedback copy | New Elarion code | `:addons:angling:processResources`, `PlaceholderFishResourceTest` |
| `src/main/resources/assets/elarion_angling/models/item/*.json` | Resources | Temporary catch, bait, and fish item models pointing at Angling-owned working item textures | New Elarion code | `PlaceholderFishResourceTest` |
| `src/main/resources/assets/elarion_angling/textures/item/**/*.png` | Resources | Original temporary catch, bait, and fish item icons indexed for later replacement | New Elarion content | `PlaceholderFishResourceTest` |
| `src/main/resources/data/elarion_angling/angling/fish/placeholder_fish_*.json` | Angling definitions | Small placeholder-only fish definition set exercising built-in technical conditions without upstream roster identity | New Elarion content | `PlaceholderFishResourceTest` |
| `scripts/check-project-policy.ps1` | Build/policy | Reject forbidden branding, loader residue, tracked reference files, and copied asset bytes | New Elarion code | Manual script execution |
| `docs/REFERENCE_AUDIT.md` | Porting docs | Classify reference subsystems as adapt, redesign, defer, or reject | New Elarion documentation | Manual review |
| `docs/REFERENCE_INDEX.md` | Porting docs | Map reference file groups to future Elarion owners and slice boundaries | New Elarion documentation | Manual review |
| `docs/DATA_MODEL_SPEC.md` | Design docs | Define the next Angling model skeleton before Java implementation | New Elarion documentation | Manual review |
| `docs/DEFINITION_LOADER_SPEC.md` | Design docs | Define the pure JSON definition loader slice before reload/resource integration | New Elarion documentation | Manual review |
| `docs/RESOURCE_RELOAD_SPEC.md` | Design docs | Define the Fabric server-data reload and atomic snapshot foundation | New Elarion documentation | Manual review |
| `docs/PLACEHOLDER_RESOURCE_SPEC.md` | Design docs | Define the one-placeholder fish data resource and reload smoke-validation slice | New Elarion documentation | Manual review |
| `docs/CANDIDATE_SELECTION_SPEC.md` | Design docs | Define bounded condition evaluation, weighted selection, failure semantics, and next session boundary | New Elarion documentation | Manual review |
| `docs/FISHING_SESSION_SPEC.md` | Design docs | Define ephemeral session ownership, retry semantics, bounded expiry, and trigger boundary | New Elarion documentation | Manual review |
| `docs/VANILLA_FISHING_TRIGGER_SPEC.md` | Design docs | Record the Fabric hook audit, telemetry-before-loot ordering, bounds, and exclusions | New Elarion documentation | Manual review |
| `docs/FISHING_FEEDBACK_SPEC.md` | Design docs | Define placeholder feedback timing, rate limits, cleanup, and nonfatal presentation semantics | New Elarion documentation | Manual review |
| `docs/REWARD_DELIVERY_SPEC.md` | Design docs | Define placeholder reward identity, Core deferred reward ordering, idempotency, inventory-full handling, and recovery before item registration | New Elarion documentation | Manual review |
| `../../docs/systems/CatchTelemetry.md` | Core architecture docs | Define durable catch acceptance, journal replay, per-player summaries, and bounded query ownership | New Elarion documentation | Manual review |
| `PORTING_LOG.md` | Porting docs | Restart-safe log of Angling port slices, checks, failures, and next action | New Elarion documentation | Manual review |

## Planned Ownership Map

These are architectural destinations, not permission to create empty layers.
Add exact paths when implementation begins.

| Domain | Canonical owner | Boundary |
| --- | --- | --- |
| Bootstrap and registries | Angling addon bootstrap | Registration only; no gameplay policy |
| Fishing definitions and codecs | Angling definitions | Immutable validated placeholder definitions |
| Fishing definition indexes | Angling definition index | Derived, rebuildable, atomically swapped |
| Fishing sessions | Angling gameplay | Server-authoritative ephemeral state |
| Catch resolution requests | Angling gameplay | Vanilla server bobber lifecycle drives immutable retry-safe Core telemetry before vanilla loot generation |
| Player progression | Core progression | Canonical persistent player state |
| World/tournament state | Core world state | Canonical persistent world state |
| Networking | Fabric platform boundary | Typed payloads and validation |
| Commands | Command module | Thin adapters over domain services |
| Client screens/rendering | Client module | Presentation only; no authority |
| Datagen | Datagen module | Reproducible resource generation |
| Compatibility | One module per optional mod | Translation into Core contracts |
| Addon API | Public API module | Stable registration/events, no mutable internals |

## Planned Runtime State Classes

Do not add a state class without entering its exact path, owner, persistence
scope, schema version, and invalidation behavior here.

| State | Scope | Persistent | Query/index strategy | Implementation |
| --- | --- | --- | --- | --- |
| Angling definition snapshot | Server reload | No | Precomputed immutable indexes with atomic publication | Repository and reload listener implemented |
| Active fishing session | Player/session | No | Direct actor lookup plus bounded deadline priority queue | Driven by server bobber fishing tick, retrieval, unload, disconnect hard-cancel, and captured bait context |
| Angling feedback cooldowns | Connected player | No | One direct UUID entry holding at most two timestamps; cleared on disconnect/stop | Implemented |
| Player progression | Player | Yes | Direct player lookup | Not implemented |
| Tournament state | World/server | Yes | Active tournament index plus summaries | Not implemented |
| Core catch journal | Player/month | Yes | Forced append, UTC partitioning, bounded checkpoint replay, exact deduplication | Processing service registered |
| Core catch summary | Player | Yes | Direct lookup, immutable indexes, replay checkpoint, atomic dirty saves | Lifecycle and read-only API implemented |

Phase 1 adds no runtime state. Future catch history, rarity summaries, and
title-facing progression state must be Core-owned before becoming player-facing.

Next implementation target:

```text
Remaining fishing smoke validation
```

The model skeleton, pure JSON-to-index loader, Fabric server-data reload
listener, atomic snapshot repository, one placeholder fish data resource,
Core-owned immutable catch telemetry transport, processing architecture,
versioned accepted record, explicit JSONL codec/path contract, forced
player/month append, bounded checkpoint replay, immutable per-player summaries,
atomic snapshot storage, dirty tracking, Core processing service,
lifecycle/event-bus binding, bounded queued replay, read-only summary API, and
one server-owned placeholder catch-result emission service, bounded condition
registry/context, deterministic weighted candidate selection, direct-player
ephemeral session orchestration, a bounded vanilla server fishing trigger,
one-shot per-cast selection, rate-limited placeholder feedback, disconnect
hard-cancellation of vanilla bobbers, one placeholder catch item, deterministic
Core deferred reward enqueue, retry-stable session completion, and vanilla loot
suppression after accepted custom completion exist. Successful custom catches
now consume one matching bait item captured by the active session after Core
telemetry and reward enqueue acceptance. Current known fish now grant
fish-specific reward item IDs with original working names and temporary
Angling-owned icons; the generic catch item remains a fallback for unknown fish
IDs. Manual smoke confirmed the
normal reward notification and claim path, the fixed rod-return behavior, and
full-inventory pending-claim retry. The packaged placeholder fish data set now
exercises built-in water, overworld, river, rain, thunder, night,
underground, and bait-present conditions. Next validate unavailable selection,
telemetry-failure retry, and bait consumption in-game. Keep commands,
networking, UI, titles, History milestones, Chronicle integration, rarity
scaling, and copied content out of the next slice.

The visible fish names in `en_us.json` are original working names for gameplay
testing. Technical IDs intentionally remain placeholder IDs until the data
schema and final content plan are stable.

The placeholder catch and bait items now have original working visible names
and Angling-owned temporary icon textures. Current fish reward items also have
original working visible names and temporary icon textures. Replacement paths
are indexed in `VISUAL_ASSET_REDIRECT.md` and `REPLACE.md`.

## Extension Point Registry

Every public extension point must be added here before external use.

| Extension point | Owner | Contract | Status |
| --- | --- | --- | --- |
| Fishing definition registration/reload | Angling definitions | Validated placeholder fishing definitions | Planned |
| Catch condition type | Angling gameplay | Pure/bounded condition evaluation with fail-closed unknown IDs and Elarion-owned built-in technical evaluators | Implemented internally |
| Minigame behavior type | Angling gameplay | Server-approved behavior ID and result | Planned |
| Reward provider | Core rewards | Deterministic deferred reward grant from accepted Angling catch result, with known fish delivered as fish-specific items and unknown fish falling back to the generic catch item | Implemented for current fish set |
| Compatibility adapter | Compatibility | External-to-Core translation | Planned |
| Catch telemetry event | Core public API | Immutable bounded catch fact with durable append-first Core processing | Implemented |

## Index Update Template

For every new or moved production unit, add one row to `Current Code Map`:

| Path | Owner | Purpose | Provenance | Tests |
| --- | --- | --- | --- | --- |
| `exact/path` | One canonical owner | One-sentence responsibility | New or adapted with upstream path | Exact test path |

Also update:

- runtime state table for persistent or cached state;
- extension point table for public APIs/events;
- `REPLACE.md` for assets or visible content;
- `TODO.md` for status;
- `NOTICE.md` if provenance or dependency attribution changes.

Generated files may be indexed by generator/root rather than one row per file.
