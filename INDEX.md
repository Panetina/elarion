# INDEX

Single navigation entry point for the repository.

## Authority Precedence

This list defines conflict precedence, not a mandatory sequential read. Use the
bounded context command and `docs/ai/routes.json` to select task material.

1. `RULES.md`
2. `AGENTS.md`
3. `CODEX.md`
4. `INDEX.md`
5. `PLAN.md`
6. `TODO.md`
7. `PLANS.md`
8. `LORE.md`
9. `OPTIMIZATION_TRACKER.md`

## Project Overview

- Fabric 1.21.1 is the source of truth and target platform.
- Core owns canonical truth, shared infrastructure, the modular Collection menu
  shell, the Character Menu profile boundary, and the OP Admin Panel shell.
- Addons extend Core with feature-specific behavior and runtime state.
- `wiki/` is the human-readable manual.
- `docs/` is the technical reference and architecture layer.

## Core Docs

- [AGENTS.md](AGENTS.md)
- [CODEX.md](CODEX.md)
- [RULES.md](RULES.md)
- [README.md](README.md)
- [Config reference](docs/config.md)
  - Core config descriptors and edit/mutation-readiness contracts live under
    `platform/core/src/main/java/panetina/elarion/core/config/`; config edit
    payload records/codecs live under
    `platform/core/src/main/java/panetina/elarion/core/network/`; passive
    config edit client result state lives under
    `platform/core/src/main/java/panetina/elarion/core/client/`. The inert apply
    contract is `ElarionConfigApplyRegistry` plus its registrar, transactional
    prepared change, capability, executor, context, readiness, audit, and
    internal coordinator/session contracts. `ElarionConfigApplyAuditJournal` is
    the durable JSONL audit sink for future apply execution, and
    `ElarionConfigApplyService` owns production lifecycle/readiness. Addons
    receive registration-only access through
    `ElarionApi.system().configAppliers()`; it has no production registrations
    yet.
- [TODO.md](TODO.md)
- [PLAN.md](PLAN.md)
- [PLANS.md](PLANS.md)
- [LORE.md](LORE.md)
- [OPTIMIZATION_TRACKER.md](OPTIMIZATION_TRACKER.md)
- [Superseded implementation protocol redirect](docs/IMPLEMENTATION_PROTOCOL.md)

## Architecture

- [Remaining Revamp Roadmap (Phases 10-14)](docs/architecture/REVAMP_REMAINING_ROADMAP.md)
- [Project Structure](docs/architecture/PROJECT_STRUCTURE.md)
- [Dependency Graph](docs/architecture/DEPENDENCY_GRAPH.md)
- [Knowledge Map](docs/architecture/KNOWLEDGE_MAP.md)
- [Current AI handoff/status snapshot](docs/ai/CURRENT_STATUS.md)
- [AI search hints](docs/ai/AI_SEARCH_HINTS.md)
- [Machine-readable AI task routes](docs/ai/routes.json)
- [Historical AI handoff archive](docs/ai/archive/README.md) — explicit
  historical recovery only; excluded from normal context retrieval.

## Systems

- [Core system docs](docs/systems/README.md)
- [Extension and maintenance guide](docs/systems/EXTENSION_GUIDE.md)
- [Placeholder system](docs/systems/PLACEHOLDERS.md)
- [Placeholder consolidation audit](docs/reports/PLACEHOLDER_CONSOLIDATION_AUDIT.md)
- [Profile aggregation completion](docs/reports/PROFILE_AGGREGATION_COMPLETION.md)
- `docs/systems/NPCs.md`
  - Phase 9 narrative readiness audit:
    `docs/reports/NPC_NARRATIVE_READINESS_AUDIT.md`. Current NPCs are
    data-driven and server-authoritative for definitions, placement,
    dialogue, bank/trade service screens, registry-backed actions/conditions,
    graph validation, bounded relationship state, durable story flags,
    one-time choices, endings/re-entry, and explicit structured story outcomes.
  - `NpcReputationTabProvider` exposes constant-time per-faction reputation
    rows maintained by `NpcRelationshipService`; each NPC dialogue shows only
    that placed NPC's personal relationship.
  - `ElarionNpcApi.reputation()` and
    `elarion_npcs:faction_reputation_at_least` are the bounded integration
    boundaries for faction-gated shops, quests, titles, and rewards.
- `docs/systems/Quests.md`
- `docs/systems/Realms.md`
- `docs/systems/Treasury.md`
- `docs/systems/CommunityContribution.md`
- `docs/systems/Chronicles.md`
  - Core public-history records preserve bounded event metadata through
    `HistoryIndexEntry`, `ChronicleEntry`, and `PublicHistoryEntry`. Government
    currently consumes that metadata through the Core Chronicle renderer
    contract:
    `platform/core/src/main/java/panetina/elarion/core/model/ChronicleRenderer.java`,
    `platform/core/src/main/java/panetina/elarion/core/model/ChronicleProjection.java`,
    `platform/core/src/main/java/panetina/elarion/core/model/ChronicleRenderContext.java`,
    `platform/core/src/main/java/panetina/elarion/core/model/ChronicleTemplate.java`,
    `platform/core/src/main/java/panetina/elarion/core/model/ChronicleTemplateFamily.java`,
    and
    `platform/core/src/main/java/panetina/elarion/core/service/ChronicleRendererRegistry.java`,
    `platform/core/src/main/java/panetina/elarion/core/service/ChronicleTemplateLibrary.java`,
    `platform/core/src/main/java/panetina/elarion/core/service/ChronicleVariantSelector.java`,
    and
    `platform/core/src/main/java/panetina/elarion/core/service/ChronicleTemplateRenderer.java`.
    Core registers
    `platform/core/src/main/java/panetina/elarion/core/service/CoreChronicleText.java`
    for Core-owned title progression wording. Government registers
    `addons/government/src/main/java/panetina/elarion/addons/government/GovernmentChronicleText.java`
    for Civic Forum History and Seat of Rule Archive row wording. Portals,
    Offerings, and Underworld register
    `addons/portals/src/main/java/panetina/elarion/addons/portals/PortalChronicleText.java`
    ,
    `addons/offerings/src/main/java/panetina/elarion/addons/offerings/OfferingChronicleText.java`
    , and
    `addons/underworld/src/main/java/panetina/elarion/addons/underworld/UnderworldChronicleText.java`
    for their first library-ready Chronicle families.
  - Phase 8 Chronicle variant proposal:
    `docs/reports/CHRONICLE_VARIANT_FRAMEWORK_PROPOSAL.md`. Future
    library-ready event families require at least 10 authored stable variants
    each, selected by `chronicle.variant` or deterministic event/family id.
- `docs/systems/Teams.md`
- [Atlas system](docs/systems/Atlas.md)
  - Typed website marker contract:
    `platform/core/src/main/java/panetina/elarion/core/integration/minecraft/WebsiteMapMarker.java`.
  - Offerings adapter:
    `addons/offerings/src/main/java/panetina/elarion/addons/offerings/service/OfferingWebProjectionPublisher.java`.
- `docs/systems/Permissions.md`
- `docs/systems/MinecraftBridge.md`
  - Core-owned outbound HTTPS whitelist synchronization. The website owns
    application decisions and ordered commands; Core owns application to the
    canonical Minecraft whitelist, restart-safe cursor state, and acknowledgements.
  - Core also owns the restart-safe bounded website projection outbox exposed
    to addons through `api.system().webProjections()`. The website stores read
    models only; initial kinds cover realms, citizen identity, elections,
    persisted advancement rankings, Shrine aggregates, typed map markers,
    Chronicles, and modular metrics/map markers.
- `docs/systems/LiveDeployment.md`
  - Guarded full-export promotion to PebbleHost with explicit approval,
    stopped-server confirmation, timestamped remote backup, SHA-256 manifest,
    and separate post-start verification.
  - `deploy-live-server.ps1 -PlanOnly` writes the exact stage, commit, rollback,
    and hash-manifest plan without opening an SFTP connection.
  - Website, launcher, Discord, and Fabric release/ownership boundaries.
- `docs/systems/Distribution.md`
  - Canonical `distribution/mods.json` third-party pins, official-origin and
    SHA-512 rules, client/server install roots, IntelliJ/Loom synchronization,
    managed/default config policy, and launcher manifest contract.
  - Audits: `docs/reports/FABULOUSLY_OPTIMIZED_ADAPTATION.md` and
    `docs/reports/FABRIC_1_21_1_PERFORMANCE_CATALOG.md`.
  - Runtime validation:
    `docs/reports/PERFORMANCE_DISTRIBUTION_VALIDATION.md`.
  - Release soak harness: `dev/tools/test-performance-distribution.ps1` joins
    Client Two, exercises safe managed-world travel, samples memory/cache
    growth, and captures deterministic server/client evidence.
  - Server A/B harness: `dev/tools/benchmark-performance-server.ps1`
    temporarily stages only Lithium, FerriteCore, and ModernFix, uses ABBA
    ordering, restores exact jars, and records startup/memory evidence.
- `docs/systems/GUI.md`
  - Shared runtime UI icon catalog:
    `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionUiIcons.java`
    maps semantic ids to curated art under
    `platform/core/src/main/resources/assets/elarion_core/textures/gui/library/`.
  - Shared compact list/page marker, money-summary, action-band layout,
    semantic row layout, and detail-header layout helpers:
    `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionListRangeMarker.java`,
    `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionMoneySummary.java`,
    `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionActionBandLayout.java`,
    `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionSemanticRowLayout.java`,
    `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionDetailCardLayout.java`,
    `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionDetailBodyLayout.java`,
    `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionBadgeLayout.java`,
    `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionProgressTrackLayout.java`,
    `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionEmptyStateLayout.java`,
    `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionModalLayout.java`,
    `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionInputFieldLayout.java`,
    `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionIconLabelLineLayout.java`,
    `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionStatusLineLayout.java`,
    `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionSectionHeaderLayout.java`,
    `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionPanelHeaderLayout.java`,
    `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionServiceHeaderLayout.java`,
    `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionPairedButtonLayout.java`,
    `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionFooterActionLayout.java`,
    `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionPresetButtonRowLayout.java`,
    `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionSplitSummaryLayout.java`,
    `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionScrollViewportLayout.java`,
    `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionTextViewportLayout.java`,
    `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionTooltipShellLayout.java`,
    and
    `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionItemSlotLayout.java`.
- `docs/systems/UI_JOURNAL.md`
- `docs/systems/Government.md`
- `docs/systems/Underworld.md`
- `docs/systems/Characters.md`
- `docs/systems/Networking.md`
- `docs/systems/Persistence.md`

## Addon Technical Docs

- [Addon docs index](docs/addons/README.md)
- [Core](docs/addons/core.md)
  - Worldheart governing authority is Core-owned:
    `platform/core/src/main/java/panetina/elarion/core/service/WorldheartGovernanceService.java`,
    `platform/core/src/main/java/panetina/elarion/core/model/WorldheartAuthority.java`,
    and
    `platform/core/src/main/java/panetina/elarion/core/storage/WorldheartAuthorityStorage.java`.
    It is separate from Economy's Worldheart treasury.
- [Atlas](docs/addons/atlas.md)
- [Economy](docs/addons/economy.md)
  - Currency registration lives in
    `addons/economy/src/main/java/panetina/elarion/addons/economy/EconomyItems.java`.
    Economy-owned mixins under `addons/economy/src/main/java/panetina/elarion/addons/economy/mixin/`
    raise the inventory and serialized stack ceilings required by the 999-Sigil
    stack contract; ordinary items retain their own item-level limits.
  - Retry-safe transaction contracts are `EconomyOperationKey` and
    `EconomyOperationReceipt`; `EconomyTransactionService.executeOnce(...)`
    owns journal-backed O(1) receipt replay and schema-v2 persistence.
    `EconomyTransactionService.rewardOnce(...)` and
    `ElarionEconomyApi.payPlayerBalanceRewardOnce(...)` are the V1
    restart-safe seller payout boundaries.
  - Worldheart tax routing uses `EconomyAccount.WORLDHEART_TREASURY`,
    `EconomyTaxDestinationResolver`, and Economy state schema v3.
- [Offerings](docs/addons/offerings.md)
- [Government](docs/addons/government.md)
- [Guilds](docs/addons/guilds.md)
  - Guilds owns canonical leadership and publishes the current leader through
    the bounded authenticated `authority.guild.lore` state projection. Website
    roles and Discord roles never become guild ownership truth.
- [NPCs](docs/addons/npcs.md)
  - Conversation and service presentation contracts live under
    `addons/npcs/src/main/java/panetina/elarion/addons/npcs/model/` and
     `addons/npcs/src/main/java/panetina/elarion/addons/npcs/network/`.
     Normal dialogue renders in `NpcDialogueScreen`; dedicated bank service
     presentation renders in `NpcBankScreen`; dedicated trade service
     presentation renders in `NpcTradeScreen`. Parsed merchant catalog
     definitions are loaded from `config/elarion/addons/npcs/trades.yml` and
     synchronized through `NpcTradeSnapshotPayload`.
     BUY purchases are coordinated by
     `addons/npcs/src/main/java/panetina/elarion/addons/npcs/service/NpcTradePurchaseService.java`,
     persisted by
     `addons/npcs/src/main/java/panetina/elarion/addons/npcs/storage/NpcTradePurchaseStorage.java`,
     and transported through `NpcTradePurchaseRequestPayload` /
     `NpcTradePurchaseResultPayload`.
     Finite placed-NPC stock is coordinated by
     `addons/npcs/src/main/java/panetina/elarion/addons/npcs/service/NpcTradeStockService.java`
     and persisted by
     `addons/npcs/src/main/java/panetina/elarion/addons/npcs/storage/NpcTradeStockStorage.java`.
     Sell/buyback definitions are parsed by `NpcTradeOfferDefinition` and
     exposed through `NpcConfigDescriptors`; enabled Sell offers are settled
     through the same request/result packet path. Sell sale replay state is
     modeled by `NpcTradeSaleRecord`,
     `NpcTradeEscrowStack`, `NpcTradeSaleStatus`, and `NpcTradeSaleSettlement`,
     persisted by
     `addons/npcs/src/main/java/panetina/elarion/addons/npcs/storage/NpcTradeSaleStorage.java`,
     removed/restored by
     `addons/npcs/src/main/java/panetina/elarion/addons/npcs/service/NpcTradeInventoryEscrow.java`,
     and paid through `NpcTradeSaleProvider` / `EconomyNpcTradeSaleProvider`.
     `stock-destination: placed_npc` routes through
     `NpcTradeOfferDefinition.destinationOfferId()` and
     `NpcTradeStockService.supply(...)`. Successful trade mutations send a
     fresh `NpcTradeSnapshotPayload` after the result so visible stock labels
     stay synchronized with persisted placed-NPC stock.
  - NPC tax-jurisdiction policy is resolved by `NpcTaxJurisdictionResolver` and
    persisted on `PlacedNpcRecord` in placement schema v2. Core remains the
    canonical Realm/world owner.
- [Quests](docs/addons/quests.md)
- [Portals](docs/addons/portals.md)
  - Canonical live facade/state owner: `PortalRouteService`.
  - Classified internal helpers: `PortalRouteAdminMutator`,
    `PortalScheduleReconciler`, `PortalPlayerPromptDetector`,
    `PortalTravelExecutor`, and `PortalWorldTravelGuard`.
  - Phase 13 extraction record:
    `docs/reports/PHASE_13_PORTAL_EXTRACTIONS.md`.
  - Phase 13 completion and project-wide classification:
    `docs/reports/PHASE_13_COMPLETION.md`.

- Phase 14 final verification:
  - [Runtime baseline](docs/reports/PHASE_14_RUNTIME_BASELINE.md)
  - [Restart and persistence smoke](docs/reports/PHASE_14_RESTART_PERSISTENCE_SMOKE.md)
  - [Client parity, onboarding, and authority QA](docs/reports/PHASE_14_CLIENT_AUTHORITY_QA.md)
  - [Optional-addon absence QA](docs/reports/PHASE_14_OPTIONAL_ADDON_ABSENCE_QA.md)
  - [Representative UI and resource QA](docs/reports/PHASE_14_UI_RESOURCE_QA.md)
  - [Deployment dry run and maintenance QA](docs/reports/PHASE_14_DEPLOYMENT_MAINTENANCE_QA.md)
  - [Phase 14 and revamp completion](docs/reports/PHASE_14_COMPLETION.md)
  - [Final post-revamp project review](docs/reports/POST_REVAMP_PROJECT_REVIEW.md)
  - Executable matrix and evidence status:
    `docs/reports/PHASE_14_VERIFICATION_MATRIX.md`.
- [Optimization](docs/addons/optimization.md)
- [Security](docs/addons/security.md)
- [Angling](docs/addons/angling.md)
  - [Core catch telemetry](docs/systems/CatchTelemetry.md) owns accepted catches
    and direct per-species projections.
  - [Core metrics and rankings](docs/systems/Metrics.md) documents the internal
    bounded ranking foundation and its persistence release gate.
  - `addons/angling` is the release-excluded Fabric port foundation.
  - `addons/angling-delight` is the separate release-excluded Farmer's Delight
    integration foundation.
  - Frozen-source, ownership, performance, parity, distribution, and
    cross-machine handoff contracts live in the authoritative addon doc and
    `addons/angling*/porting/foundation.json`.
  - The local reference is owner-authorized as the full port source, including
    its edited creative resources. Authorized files may move into proper
    Elarion module paths; only the raw checkout/build metadata stays excluded.
- [Worlds](docs/addons/worlds.md)
- [Realms](docs/addons/realms.md)
- [Names](docs/addons/names.md)
- [Titles](docs/addons/titles.md)
- [Jail](docs/addons/jail.md)
- [Newspapers](docs/addons/newspapers.md)
- [Tablist](docs/addons/tablist.md)
- [Underworld](docs/addons/underworld.md)
  - Moderation banishment source:
    `addons/underworld/src/main/java/panetina/elarion/addons/underworld/command/BanishCommands.java`,
    `addons/underworld/src/main/java/panetina/elarion/addons/underworld/model/BanishmentRecord.java`,
    and `addons/underworld/src/main/java/panetina/elarion/addons/underworld/service/UnderworldService.java`.
  - Living/Afterlife inventory boundary:
    `addons/underworld/src/main/java/panetina/elarion/addons/underworld/model/InventorySnapshot.java`
    and Underworld-owned `UnderworldState` maps; corpse recovery remains the
    only canonical store for Living inventory lost on a normal death.
  - Shared command/admission/interaction contracts remain Core-owned in
    `ElarionCommandRegistry`, `PlayerRestrictionService`, and
    `PlayerInteractionRestrictionRegistrar`.
- [Mounts](docs/addons/mounts.md)
- [Voice Chat Hooks](docs/addons/voicechat-hooks.md)

## Addons

- `docs/addons/`
- `addons/`
- Active foundations:
  - `addons/economy`
  - `addons/offerings`
  - `addons/government`
  - `addons/guilds`
  - `addons/npcs`
  - `addons/quests`
  - `addons/portals`
  - `addons/worlds`
  - `addons/realms`
  - `addons/names`
  - `addons/titles`
  - `addons/optimization`
  - `addons/security`
  - `addons/underworld`
  - `addons/mounts`
- Shell/foundation modules:
  - `addons/atlas`
  - `addons/angling`
  - `addons/angling-delight`
  - `addons/jail`
  - `addons/newspapers`
  - `addons/tablist`
  - `addons/voicechat-hooks`

## Ignore Unless Explicitly Requested

- `addons/angling/reference/**`: owner-authorized port source for explicitly
  requested Angling work. It remains excluded from ordinary project work and
  is not itself a Gradle source root, but selected files may be ported into the
  production Angling modules.

## References

- [UI reference images](docs/ui/)
- [Shared UI component reference](docs/ui/COMPONENT_REFERENCE.md)
- [Approved Option A Civic Ledger UI set](docs/ui/revamp-option-a/README.md)
- [Option A UI asset bank plan](docs/ui/revamp-option-a/ASSET_PLAN.md)
- [Fabric reference docs](docs/fabric-reference/)
- [NeoForge reference docs](docs/neoforge-reference/)
- [Porting docs](docs/porting/)
- [Reference setup report](docs/REFERENCE_SETUP_REPORT.md)

## Reports

- `docs/ai/CURRENT_STATUS.md`
- `docs/ai/AI_SEARCH_HINTS.md`
- `docs/ai/routes.json`
- `docs/reports/`
- `docs/reports/PROJECT_REVAMP_AUDIT.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- [UI family inventory and manual QA queue](docs/reports/UI_FAMILY_INVENTORY.md)
- [Semantic UI component audit](docs/reports/SEMANTIC_UI_COMPONENT_AUDIT.md)
- [NPC trade ownership and crash-safety audit](docs/reports/NPC_TRADE_OWNER_AUDIT.md)
- [NPC trade purchase, jurisdiction, tax, and quantity proposal](docs/reports/NPC_TRADE_PURCHASE_FOUNDATION_PROPOSAL.md)
- [NPC Sell and buyback anti-dupe proposal](docs/reports/NPC_SELL_BUYBACK_PROPOSAL.md)
- [NPC narrative readiness audit](docs/reports/NPC_NARRATIVE_READINESS_AUDIT.md)
- [NPC relationship state design](docs/reports/NPC_RELATIONSHIP_STATE_DESIGN.md)
- [NPC story state design](docs/reports/NPC_STORY_STATE_DESIGN.md)
- [Economy trade price and payout API proposal](docs/reports/ECONOMY_TRADE_PRICE_PAYOUT_API_PROPOSAL.md)
  - Economy tax policy owner:
    `addons/economy/src/main/java/panetina/elarion/addons/economy/service/EconomyTaxPolicyService.java`
  - Economy tax destination owner:
    `addons/economy/src/main/java/panetina/elarion/addons/economy/service/EconomyTaxDestinationResolver.java`
  - V1 seller payout owner:
    `addons/economy/src/main/java/panetina/elarion/addons/economy/service/EconomyTransactionService.java`
  - Core Worldheart authority owner:
    `platform/core/src/main/java/panetina/elarion/core/service/WorldheartGovernanceService.java`
  - Optional NPC quote boundary:
    `addons/npcs/src/main/java/panetina/elarion/addons/npcs/service/NpcTradeQuoteProvider.java`
- [Bank and Economy UI audit](docs/reports/BANK_ECONOMY_UI_AUDIT.md)
  - Bank presentation owner:
    `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcBankScreen.java`
  - Bank quote packets:
    `addons/npcs/src/main/java/panetina/elarion/addons/npcs/network/NpcBankQuoteRequestPayload.java`
    and
    `addons/npcs/src/main/java/panetina/elarion/addons/npcs/network/NpcBankQuotePayload.java`.
  - Authoritative withdrawal tax owner:
    `addons/economy/src/main/java/panetina/elarion/addons/economy/service/EconomyInventoryService.java`
    delegates to
    `addons/economy/src/main/java/panetina/elarion/addons/economy/service/EconomyTransactionService.java`.
  - Economy bank quote owner:
    `addons/economy/src/main/java/panetina/elarion/addons/economy/model/EconomyBankQuote.java`
    and
    `addons/economy/src/main/java/panetina/elarion/addons/economy/api/ElarionEconomyApi.java`.
- [Character Menu and Collection architecture audit](docs/reports/CITIZEN_LEDGER_AUDIT.md)
  - Current Collection transport bounds are implemented in
    `platform/core/src/main/java/panetina/elarion/core/network/CollectionOpenPayload.java`
    and verified by `CollectionPayloadTest`.
  - Core profile aggregation is implemented in
    `platform/core/src/main/java/panetina/elarion/core/service/CitizenProfileService.java`
    with profile model contracts under
    `platform/core/src/main/java/panetina/elarion/core/model/profile/`.
    `CitizenProfileSummaryFields` is the stable source/field-id contract for
    Character Menu dossier summary contributors.
  - Profile transport record/codecs live in
    `platform/core/src/main/java/panetina/elarion/core/network/CitizenProfileRequestPayload.java`
    and
    `platform/core/src/main/java/panetina/elarion/core/network/CitizenProfileSnapshotPayload.java`.
    They are registered in Core as a read-only server-authoritative
    request/response path. Per-player request budgets are implemented by
    `platform/core/src/main/java/panetina/elarion/core/network/ElarionRequestLimiter.java`.
  - Latest client profile snapshots are cached in
    `platform/core/src/main/java/panetina/elarion/core/client/CitizenProfileClientState.java`.
  - The Character Menu Profile tab renders from that cache in
    `platform/core/src/main/java/panetina/elarion/core/client/ElarionCollectionScreen.java`.
    Core progression contributes advancement count through
    `CoreProgressionProfileContributor`; Government contributes active office
    role metadata through `GovernmentProfileContributor`; Underworld
    contributes self/admin lifetime death count through
    `addons/underworld/src/main/java/panetina/elarion/addons/underworld/service/UnderworldProfileContributor.java`;
    Offerings contributes self/admin direct player Offering score through
    `addons/offerings/src/main/java/panetina/elarion/addons/offerings/service/OfferingProfileContributor.java`;
    Quests contributes self/admin completed quest count through
    `addons/quests/src/main/java/panetina/elarion/addons/quests/service/QuestProfileContributor.java`;
    Portals contributes self/admin journey count through
    `addons/portals/src/main/java/panetina/elarion/addons/portals/service/PortalProfileContributor.java`.
- `docs/REFERENCE_SETUP_REPORT.md`
- `docs/reports/REPOSITORY_AUDIT_REPORT.md`
- `docs/reports/WORKTREE_CLEANUP_AUDIT.md`
- [Post-revamp review](docs/reports/POST_REVAMP_PROJECT_REVIEW.md)
- [Post-revamp findings completion](docs/reports/POST_REVAMP_FINDINGS_COMPLETION.md)

## Git Documentation Policy

- Root authority docs, `docs/**/*.md`, and `wiki/**/*.md` are project
  knowledge and should be commit-ready.
- `external/` and `lore/folklore/` remain local/reference material unless a
  future decision promotes specific files.
- Do not create new Markdown islands unless they are linked from this index,
  `AGENTS.md`, or the wiki.

## Test Commands

- [Technical test command contract](docs/test-commands.md)
- [Admin wiki test command guide](wiki/admin/test-commands.md)
- Live UI screenshot capture helper:
  `dev/tools/capture-minecraft-window.ps1`
- Fast live UI driver helper:
  `dev/tools/minecraft-qa.ps1`
- NPC trader/banker live QA setup helper:
  `dev/tools/npc-trade-qa.ps1`
- Isolated optional-addon startup matrix:
  `dev/tools/optional-addon-qa.ps1`

## Lore

- `LORE.md`
- `lore/folklore/`

## Working Rule

Source and tests establish current behavior. `RULES.md` and accepted decisions
establish approved behavior. When they conflict, determine whether the cause is
a bug, regression, incomplete migration, partial implementation, stale
documentation, or an unrecorded decision before changing either side.

Every addon follows the Core domain-event and notification contract in
`RULES.md` and `docs/addons/core.md`. Addon docs record meaningful emitted
events, notification projections, and intentional noise exclusions.

## Documentation Maintenance

`RULES.md` owns the canonical documentation maintenance matrix. This index
lists where information lives; update it when ownership, source locations,
addon status, or the repository navigation map changes.
