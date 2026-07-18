# PLAN

Short project memory and current read order.

## Active Approved Work

Latest completed implementation phase: Phase 8 Chronicle Variant Framework.
Core now owns reusable Chronicle template families, deterministic variant
selection, renderer registration, and a bounded `chronicleLibrary(realmId,
limit)` query helper for future in-game library views. Existing
`chronicle.variant` metadata remains authoritative; records without that
metadata select a stable variant from event id plus family id. This phase did
not rewrite old history, add first-render persistence stamping, or build a new
library UI.

Library-ready families completed in Phase 8:

- Government: `proposal-approved`, `proposal-rejected`, and
  `civic-record-created`.
- Portals: `route-unlocked`.
- Offerings: `project-completed` and `project-force-completed`.
- Core: `title.progression-unlocked`.
- Underworld: PVE death, PVP death, self-inflicted death, void death, and True
  Death.
- Offerings: Realm global-access changes.

Project rule added: any future Chronicle/library-visible family, including war,
peace, revolution, story quest, seasonal event, NPC relationship, Realm, mount,
pet, and major reward features, must ship with 10 authored stable variants,
fallback text, metadata requirements, and tests when promoted.

Latest completed implementation phase: Phase 9 NPC Narrative Readiness. NPCs
now owns per-player/per-placed-NPC relationship scores and durable story state.
Story state persists flags, consumed one-time choices, endings, and opt-in
re-entry nodes in `world/elarion/addon-state/npcs/story-state.json` schema 1.
Dialogue authors use registered NPC actions/conditions; clients receive only
server-filtered options. Explicit `history-worthy` outcomes create structured
`npc/story-outcome` records with persisted variant ids and ten authored
Chronicle variants. Ordinary dialogue and relationship changes remain silent.
Reports: `docs/reports/NPC_RELATIONSHIP_STATE_DESIGN.md` and
`docs/reports/NPC_STORY_STATE_DESIGN.md`.

The master-plan next slice is Phase 10 Slice 1, Placeholder Consolidation
Audit. Recommended model: `Medium`. Inventory existing placeholder systems and
compatibility-sensitive keys before adding a shared registry; no broad
placeholder migration belongs in the audit slice.

Latest completed follow-up slice: player-facing identity terminology now uses
`Ember`/`Embers` while `Citizenship` and compatibility-sensitive technical
contracts (`CitizenRecord`, storage paths, packet/API names, config key
`citizens`, and stable title ID `citizen`) remain unchanged. The former Citizen
Ledger is now **Character Menu**. `C` and hidden client `/charactermenu` open it;
the old `/ledger` and `/collection` aliases were removed. The server identity
command is `/e ember`. Core and Government tests cover the migration.

Latest completed follow-up slice: Mount Collection preview and flight-animation
repair. All seven V1 mount previews retain bounds-aware sizing, with measured
visual offsets for asymmetrical converted models. The Wyvern's invalid baked
`shadow` plane was removed from its GeckoLib geometry; the canonical texture is
unchanged and its Ledger preview uses a static pose to prevent animated wing
ghosting. Turn intent once again activates lean overlays
and composes with ascend/descend overlays. Focused Mount tests pass. Final
Wyvern QA: `build/ui-qa/mount-preview-20260711-final/wyvern-verified.png`.

Earlier completed implementation slice: Phase 8 Slice 3. Government
`proposal-approved` Chronicle rows now use the Core template-family/variant
selector contract with 10 authored variants. Existing `chronicle.variant`
metadata still wins; otherwise the selected variant is deterministic for the
event/family. Missing `title` metadata uses a safe Government fallback. No
history persistence, event emission, packets, UI geometry, or live QA changed.

Latest completed implementation slice: Phase 8 Slice 2. Core now has the
reusable Chronicle template skeleton: `ChronicleTemplate`,
`ChronicleTemplateFamily`, `ChronicleTemplateLibrary`,
`ChronicleVariantSelector`, and `ChronicleTemplateRenderer`. Existing
`chronicle.variant` metadata wins; otherwise selection is stable per
event/family. Template families define required metadata and missing-context
fallbacks, and are considered library-ready at 10 authored variants. No
history persistence, event emission, Government renderer migration, packets, or
UI changed.

Project-wide revamp master plan is active. Implementation is approved only one
explicit slice at a time.

Latest completed implementation slice: Phase 8 Slice 1. Chronicle variant
framework audit/proposal is complete as docs-only planning. The current
public-history chain already preserves structured metadata from `HistoryEvent`
through `HistoryIndexEntry`, `ChronicleEntry`, and `PublicHistoryEntry`.
`ChronicleRendererRegistry` already reads `chronicle.variant`, but there is no
Core template library or consistent selected-variant persistence yet. User
decision recorded: every event family that becomes future-library-ready needs
at least 10 authored stable variants. "Random" means deterministic/stable per
event, not changing prose every render. Report:
`docs/reports/CHRONICLE_VARIANT_FRAMEWORK_PROPOSAL.md`.

Previous completed implementation slice: Phase 7 Slice 9. Character Menu now has
its fourth owner-maintained addon summary: Portals increments the Core
player-stat key `portal_journeys` after successful server-authoritative portal
travel, and `PortalProfileContributor` contributes `portals/journeys` with
`SELF` visibility. No Portal state schema, route definition format, tickets,
payments, entitlements, commands, config, packets, or UI geometry changed.
This closes the Medium-safe Phase 7 backend summary run.

Earlier completed implementation slice: Phase 7 Slice 8. Character Menu added
its third owner-maintained addon summary: Quests increments the Core
player-stat key `quests_completed` when a player actor locks an ending on a
questline scope that did not already have one, and `QuestProfileContributor`
contributes `quests/quests-completed` with `SELF` visibility. No Quest state
schema, package definition format, commands, config, packets, scheduled
consequences, or notification behavior changed.

Earlier completed implementation slice: Phase 7 Slice 7. Character Menu added
its second owner-maintained addon summary: Offerings increments the Core
player-stat key `offerings_score` after direct accepted player item/currency
Shrine contributions, and `OfferingProfileContributor` contributes
`offerings/offering-score` with `SELF` visibility. No Offering state schema,
project definition format, Shrine UI geometry, packets, config, commands, or
milestone behavior changed.

Earlier completed implementation slice: Phase 7 Slice 6. Character Menu added
its first owner-maintained addon summary: Underworld increments the Core
player-stat key `underworld_lifetime_deaths` at authoritative living-world and
repeat-Underworld death capture points, and `UnderworldProfileContributor`
contributes `underworld/deaths` with `SELF` visibility.

Earlier completed implementation slice: Phase 7 Slice 5. Character Menu
backend-summary field contract now lives in Core `CitizenProfileSummaryFields`.
The Ledger Profile tab, Core progression contributor, and Government profile
contributor use shared source/field constants for the reserved dossier slots
instead of repeating string literals. This created the safe contract for future
owner-maintained summaries without adding fake quest, Offering, NPC, Portal, or
Chronicle values.

Earlier completed implementation slice: Phase 7 Slice 4. Grave Recovery
code-side QA follow-up now routes item slots through the shared Core
`ElarionItemSlotLayout` helper. Native item rendering and tooltip hitboxes use
the same item rectangle, so lore/enchantments appear only when hovering the
actual item icon instead of the full grave slot frame. Recovery packets,
server-side corpse authority, storage, and inventory mutation behavior are
unchanged.

Earlier completed implementation slice: Phase 7 Slice 3. Portal Confirmation
QA and follow-up added an OP4 `/e portal preview <state>` command for safe
visual prompt coverage, wrapped long Portal prompt messages inside the prompt
body, and live-QA captured neutral/free, Nether ticket, End ticket, paid
Sigil, blocked, and return states under `build/ui-qa/portal-phase7/final/`.
Preview prompts do not mutate route state or bypass server-side travel
validation.

Phase 7 Slice 9 verification passed:
`.\gradlew.bat :addons:portals:test --tests panetina.elarion.addons.portals.service.PortalProfileContributorTest --tests panetina.elarion.addons.portals.storage.PortalStorageTest --tests panetina.elarion.addons.portals.service.PortalFreePassagePolicyTest --tests panetina.elarion.addons.portals.network.PortalTravelPromptPayloadTest :platform:core:test --tests panetina.elarion.core.model.profile.CitizenProfileSummaryFieldsTest --tests panetina.elarion.core.service.CitizenProfileServiceTest :addons:portals:compileJava`.

No live screenshot QA was run in this Medium slice.

Current next slice:

- Phase 10 Slice 1 - Placeholder Consolidation Audit.
- Recommended model: `Medium`.
- Objective: inventory placeholder/token implementations, owners, contexts,
  visibility, missing-data behavior, costs, and compatibility keys.
- Scope: audit and proposal only; do not migrate all placeholders or introduce
  a universal runtime resolver before ownership is verified.

Completed current slices:

- Phase 0, Slice 1: audit preparation and repository map verification.
- Report: `docs/reports/PROJECT_REVAMP_AUDIT.md`.
- Phase 0, Slice 2: Configuration and Admin Panel deep audit.
- Report: `docs/reports/CONFIG_ADMIN_AUDIT.md`.
- Phase 2, Slice 1: Core read-only config descriptor registry.
- Added `ElarionConfigRegistry`, read-only descriptor contracts, Core
  `ui_theme.yml` / `server_identity.yml` descriptors, and
  `ElarionApi.system().configs()`.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigRegistryTest`
  and `.\gradlew.bat :platform:core:test`.
- Phase 2, Slice 2: Groups read-only config descriptor domain.
- Added `GroupConfigDescriptors`, registered the `groups` domain through
  `ElarionApi.system().configs()`, and exposed current values from
  `GroupService.config()`.
- Verification passed:
  `.\gradlew.bat :addons:groups:test --tests panetina.elarion.addons.groups.config.GroupConfigDescriptorsTest`,
  `.\gradlew.bat :addons:groups:test`, and `.\gradlew.bat :platform:core:test`.
- Phase 3, Slice 1: read-only Admin Panel config browser skeleton.
- Added config-domain rows to the Admin Panel Systems tab through the existing
  row/detail model. The browser displays registered domains, source files,
  reload command, categories, entries, current/default values, bounds, choices,
  and reload/restart markers.
- Superseded by Phase 3, Slice 3: config-domain rows now live in the dedicated
  Config tab.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest`
  and `.\gradlew.bat :platform:core:test :addons:groups:test`.
- Config file formats, config writes, reload behavior, packets, and
  persistence were not changed.
- Phase 2, Slice 3: Economy read-only config descriptor domain.
- Added `EconomyConfigDescriptors`, registered the `economy` domain through
  `ElarionApi.system().configs()`, and exposed current values from
  `EconomyTransactionService.config()` and `EconomyPricingService.definitions()`.
- Verification passed:
  `.\gradlew.bat :addons:economy:test --tests panetina.elarion.addons.economy.config.EconomyConfigDescriptorsTest`
  and `.\gradlew.bat :addons:economy:test :platform:core:test`.
- Economy reload behavior, config writes, packets, file formats, and
  persistence were not changed.
- Phase 2, Slice 4: Worlds read-only config descriptor domain.
- Added `WorldsConfigDescriptors`, registered the `worlds` domain through
  `ElarionApi.system().configs()`, and exposed current values from
  `WorldsConfigManager`.
- Verification passed:
  `.\gradlew.bat :addons:worlds:test --tests panetina.elarion.addons.worlds.config.WorldsConfigDescriptorsTest`
  and `.\gradlew.bat :addons:worlds:test :platform:core:test`.
- World storage, managed-world behavior, reload semantics, config writes,
  packets, file formats, and persistence were not changed.
- Phase 2, Slice 5: Portals read-only config descriptor domain.
- Added `PortalConfigDescriptors`, registered the `portals` domain through
  `ElarionApi.system().configs()`, and exposed current values from
  `PortalDefinitionService` route/UI snapshots.
- Verification passed:
  `.\gradlew.bat :addons:portals:test --tests panetina.elarion.addons.portals.config.PortalConfigDescriptorsTest`
  and `.\gradlew.bat :addons:portals:test :platform:core:test`.
- Portal travel behavior, schedule evaluation, Economy price integration,
  config writes, packets, file formats, and persistence were not changed.
- Phase 2, Slice 6: Offerings read-only config descriptor domain.
- Added `OfferingConfigDescriptors`, registered the `offerings` domain through
  `ElarionApi.system().configs()`, and exposed current values from
  `OfferingDefinitionService` project/UI snapshots.
- Verification passed:
  `.\gradlew.bat :addons:offerings:test --tests panetina.elarion.addons.offerings.config.OfferingConfigDescriptorsTest`
  and `.\gradlew.bat :addons:offerings:test :platform:core:test`.
- Offering donation/progression behavior, rewards, Shrine blocks, reload
  semantics, config writes, packets, file formats, and persistence were not
  changed.
- Phase 2, Slice 7: Government read-only config descriptor domain.
- Added `GovernmentConfigDescriptors`, registered the `government` domain
  through `ElarionApi.system().configs()`, and exposed current values from
  `GovernmentDefinitionService` settings/form snapshots.
- Verification passed:
  `.\gradlew.bat :addons:government:test --tests panetina.elarion.addons.government.config.GovernmentConfigDescriptorsTest`
  and `.\gradlew.bat :addons:government:test :platform:core:test`.
- Government voting, office, authority, form-loading, reload semantics,
  config writes, packets, file formats, and persistence were not changed.
- Phase 2, Slice 8: NPCs read-only config descriptor domain.
- Added `NpcConfigDescriptors`, registered the `npcs` domain after the first
  successful definition load, and exposed supplier-backed NPC definition,
  visual profile, dialogue summary, and UI snapshots.
- Verification passed:
  `.\gradlew.bat :addons:npcs:test --tests panetina.elarion.addons.npcs.config.NpcConfigDescriptorsTest`
  and `.\gradlew.bat :addons:npcs:test :platform:core:test`.
- NPC placement/runtime state, dialogue sessions, condition/action evaluation,
  reload semantics, config writes, packets, file formats, and persistence were
  not changed.
- Phase 2, Slice 9: Quests read-only config descriptor domain.
- Added `QuestConfigDescriptors`, registered the `quests` domain after the
  existing validated definition load, and exposed supplier-backed quest
  package metadata and graph summaries.
- Verification passed:
  `.\gradlew.bat :addons:quests:test --tests panetina.elarion.addons.quests.config.QuestConfigDescriptorsTest`
  and `.\gradlew.bat :addons:quests:test :platform:core:test`.
- Quest runtime state, progression, actions/conditions, scheduled consequences,
  notifications, reload semantics, config writes, packets, file formats, and
  persistence were not changed.
- Phase 2, Slice 10: Realms protection read-only config descriptor domain.
- Added `RealmConfigDescriptors`, registered the `realms` domain from the
  loaded protection snapshot, and centralized shipped fallback values in
  `RealmProtectionConfig.defaults()`.
- Verification passed:
  `.\gradlew.bat :addons:realms:test --tests panetina.elarion.addons.realms.config.RealmConfigDescriptorsTest`
  and `.\gradlew.bat :addons:realms:test :platform:core:test`.
- Protection behavior, config loading semantics, packets, file formats, and
  persistence were not changed.
- Phase 2, Slice 11: Mounts collection text read-only config descriptor domain.
- Added `MountConfigDescriptors`, registered the `mounts` domain from the
  loaded Collection text snapshot, and exposed four presentation fields for
  each registered mount type.
- Verification passed:
  `.\gradlew.bat :addons:mounts:test --tests panetina.elarion.addons.mounts.config.MountConfigDescriptorsTest`
  and `.\gradlew.bat :addons:mounts:test :platform:core:test`.
- Collection rendering, mount runtime state, config loading, packets, file
  formats, and persistence were not changed.
- Phase 2, Slice 12: Underworld read-only config descriptor domain.
- Added `UnderworldConfigDescriptors`, registered the `underworld` domain from
  `UnderworldService.config()`, and exposed five bounded categories with 33
  settings.
- Verification passed:
  `.\gradlew.bat :addons:underworld:test --tests panetina.elarion.addons.underworld.config.UnderworldConfigDescriptorsTest`
  and `.\gradlew.bat :addons:underworld:test :platform:core:test`.
- Death/corpse/PvP loot/combat-tag/Soul Fracture behavior, config loading,
  packets, files, and persistence were not changed.
- Phase 2, Slice 13: Optimization/performance read-only config descriptor
  domain.
- Added `PerformanceConfigDescriptors`, registered the `optimization` domain
  with explicit `platform:core` ownership, and exposed 16 host, budget, and
  monitoring settings from `ElarionTaskService.snapshot()`.
- Verification passed:
  `.\gradlew.bat :addons:optimization:test --tests panetina.elarion.addons.optimization.PerformanceConfigDescriptorsTest`
  and `.\gradlew.bat :addons:optimization:test :platform:core:test`.
- Worker/queue behavior, budgets, monitoring, config loading, files, and
  runtime behavior were not changed.

- Phase 2, Slice 14: Core scalar read-only config descriptor expansion.
- Added 41 scalar entries across citizen/activity, chat, identity/nickname, and
  history categories to the existing `core` domain.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigRegistryTest`
  and `.\gradlew.bat :platform:core:test`.
- Core load/reload behavior, files, commands, persistence, and runtime behavior
  were not changed.

- Phase 2, Slice 15: Core Realm definition read-only descriptors.
- Added `realms.yml` to the existing `core` domain with Realm count/IDs and
  per-Realm presentation, visibility, spawn, and flag descriptors.
- Dynamic Realm rows are fixed to IDs present at descriptor registration;
  current values for those IDs remain supplier-backed through
  `CoreConfigManager.realms()` after successful `/e reload` operations.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigRegistryTest`
  and `.\gradlew.bat :platform:core:test`.
- Realm membership, governance, config loading, files, commands, persistence,
  and gameplay behavior were not changed.

- Phase 2, Slice 16: Core title and title-progression read-only descriptors.
- Added `titles.yml` and `title-progression.yml` to the existing `core` domain
  with title count/IDs, per-title presentation/acquisition/ownership summaries,
  progression region summaries, and unlock-rule summaries.
- Dynamic title/progression rows are fixed to IDs present at descriptor
  registration; current values for those IDs remain supplier-backed through
  existing `CoreConfigManager` snapshots after successful `/e reload`
  operations.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigRegistryTest`
  and `.\gradlew.bat :platform:core:test`.
- Title ownership/activation, ability ownership, config loading, files,
  commands, persistence, and gameplay behavior were not changed.

- Phase 2, Slice 17: Core reward read-only descriptors.
- Added `rewards.yml` to the existing `core` domain with reward count/IDs and
  per-reward action count/type/parameter descriptors.
- Dynamic reward rows are fixed to IDs and action indexes present at descriptor
  registration; current values for those rows remain supplier-backed through
  `CoreConfigManager.rewards()` after successful `/e reload` operations.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigRegistryTest`
  and `.\gradlew.bat :platform:core:test`.
- Reward execution, claim state, config loading, files, commands, packets,
  persistence, and gameplay behavior were not changed.

Addon runtime config and Core loaded-definition descriptor coverage are
complete for the currently parsed runtime config models. Jail, Security, and
Core abilities only generate placeholder/default YAML; typed loaders are
required before truthful descriptors can be registered for them.

- Phase 4, Slice 35: Bank quote live QA attempt with nonzero dev withdrawal
  tax.
- Added a dev-run-only `bank.withdrawal-tax-basis-points: 250` value to
  `dev/run/config/elarion/addons/economy/economy.yml` so Fee/Total can be seen
  in screenshots without changing production defaults.
- Verified narrow code path with `.\gradlew.bat :addons:economy:test
  :addons:npcs:test`.
- Captured live setup evidence under `build/ui-qa/slice-35-bank-quote/`,
  including a clean high-platform banker root dialogue.
- Blocker: the live synthetic click path entered the Sigils lore node; Back and
  close/dismiss did not reliably return to the root before the client dropped
  back to the multiplayer screen. Taxed Withdraw Fee/Total was not captured.
- Next recommended slice: investigate NPC dialogue option activation/dismiss
  reliability, then rerun bank quote QA and capture Deposit 100 plus Withdraw
  100 with the visible 2.5% dev tax.

- Phase 4, Slice 36: NPC visual config cleanup after removing obsolete banker
  art.
- Removed the stale `dunk_banker` skin profile and fallback texture from active
  dev-run NPC config, changed the NPC renderer default fallback to
  `worldheart_banker.png`, and updated generated defaults so new configs do not
  reference the deleted PNG.
- Trimmed active dev-run NPC visual profiles to the two current service NPCs:
  `worldheart_banker` and `worldheart_trader`.
- Updated the dev-run banker dialogue file to the explicit current
  conversation-first `Open Bank` -> `presentation: bank` shape instead of
  relying on the legacy prompt migration bridge.
- Verification passed: `.\gradlew.bat :addons:npcs:test
  :addons:npcs:processResources`.
- Quick restart check passed: `dev/run/logs/latest.log` reports
  `Loaded 2 NPC definitions, 2 skin profiles, 2 portrait profiles, 2 dialogues,
  and 1 trade catalogs` after the cleanup.
- Next recommended slice remains the bank quote QA rerun from a clean restart:
  verify no client missing-texture warning; then capture Deposit 100 and
  Withdraw 100 with visible server-authored Fee/Total.

- Phase 4, Slice 37: NPC service default guardrails, no live QA.
- Added focused regression coverage in
  `NpcBankPresentationMigrationTest` so generated service NPC defaults cannot
  reintroduce the deleted `dunk_banker` texture and the generated banker
  dialogue must keep the explicit `Open Bank` -> `presentation: bank` route.
- Verification passed:
  `.\gradlew.bat :addons:npcs:test --tests panetina.elarion.addons.npcs.config.NpcBankPresentationMigrationTest`.
- No gameplay code, packets, persistence, commands, or live QA changed in this
  slice.
- Next recommended slice without QA: continue the revamp with a nonvisual
  bounded implementation slice, preferably NPC dialogue interaction reliability
  tests or the next small UI-system backend cleanup. If QA is allowed again,
  rerun bank quote screenshots from a clean client/server start.

- Phase 4, Slice 38: NPC dialogue typing-click contract fix, no live QA.
- Wired `NpcDialogueScreen` to respect the server-authored
  `typing-click-completes` payload field instead of always completing
  typewriter phases on click/Enter/Space.
- Added focused `ElarionConversationControllerTest` coverage for
  typing-disabled immediate input and null/blank text fallback behavior.
- Verification passed:
  `.\gradlew.bat :addons:npcs:test --tests panetina.elarion.addons.npcs.client.ui.ElarionConversationControllerTest --tests panetina.elarion.addons.npcs.config.NpcBankPresentationMigrationTest`.
- Packets, config files, persistence, commands, server gameplay, and live QA
  were not changed.
- Next recommended slice: if QA is allowed, rerun the bank quote screenshots
  from a clean client/server start. If no QA, continue with the next small
  nonvisual UI-system cleanup or NPC service reliability test.

- Phase 4, Slice 39: NPC service QA open command and helper upgrade, no live QA.
- Added OP-only `/e npc open <npcId>` to open a nearby placed NPC through the
  existing server-authoritative `NpcInteractionService.open(...)` path.
- `NpcInteractionService.open(...)` now returns success/failure so command
  output does not claim an open when range, definition, permission, or dialogue
  validation rejects the interaction.
- Updated `dev/tools/npc-trade-qa.ps1` so banker/trader QA opens the initial
  conversation with `/e npc open <id>` instead of brittle right-click
  automation, then still uses the server-authored `Open Bank` / `Trade` option
  route for service screens. Added `open-bank` and `capture-bank` actions.
- Verification passed:
  `.\gradlew.bat :addons:npcs:test --tests panetina.elarion.addons.npcs.config.NpcBankPresentationMigrationTest --tests panetina.elarion.addons.npcs.client.ui.ElarionConversationControllerTest`.
- Helper parse check passed:
  `[scriptblock]::Create((Get-Content dev/tools/npc-trade-qa.ps1 -Raw))`.
- No NPC placement storage format, service presentation packets, bank/trade
  mutation logic, Economy behavior, or live QA changed.
- Next recommended slice: use the upgraded helper from a clean client/server
  start to capture bank Deposit and Withdraw quote states, or continue no-QA
  backend cleanup if live QA remains disabled.

- Phase 4, Slice 40: Manual NPC bank/trader polish follow-up.
- Applied manual QA fixes from the banker/trader screenshots: configured NPC
  portraits draw one pixel lower to remove the bottom gutter, bank Fee/Total
  render as compact label + Sigil + amount groups, the bank balance badge sits
  higher in the header, trader row stock is left-aligned and vertically
  centered, row Sigil icons are slightly smaller, row range text moves below
  the row border, quantity controls are evenly spaced, and Total/Payout is
  visually emphasized over Subtotal/Tax.
- Updated generated and dev-run Nether/End ticket lore from "Valid for one
  outbound ..." to "Grants one ... Gate passage." to avoid implying the same
  ticket blocks return travel.
- Verification passed:
  `.\gradlew.bat :addons:npcs:test --tests panetina.elarion.addons.npcs.config.NpcBankPresentationMigrationTest --tests panetina.elarion.addons.npcs.client.ui.ElarionConversationControllerTest`.
- No Economy tax logic, bank quote logic, trade stock mutation, NPC persistence
  format, packets, or live QA automation changed.
- Next recommended slice: manual or live screenshot recheck of banker portrait,
  bank Fee/Total grouping, trader stock/range/quantity controls, and ticket
  tooltip wording; then continue broader revamp slices.

- Phase 3, Slice 2: Admin Panel config browser contract proposal.
- Inspected the current Admin Panel service, snapshot/tab/row records, screen,
  and service tests.
- Decision: add a dedicated read-only `configs` Admin Panel tab as the next
  implementation step, using the existing snapshot/tab/row packet model.
- Defer page/category provider contracts, config writes, typed editing, reload
  orchestration, packet schema changes, persistence, and gameplay behavior.
- Risk recorded: the current fixed-width tab layout must be adjusted before
  adding a sixth tab.

- Phase 3, Slice 3: Dedicated read-only Admin Panel Config tab.
- Added a `configs` tab to the Admin Panel snapshot and moved config-domain
  rows out of Systems.
- Systems now contains provider-owned testing and repair rows only.
- Adjusted Admin Panel tab layout to compute tab width from tab count so six
  tabs fit inside the existing 660px panel.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest --tests panetina.elarion.core.client.ElarionAdminPanelScreenLayoutTest`
  and `.\gradlew.bat :platform:core:test`.
- Config writes, typed editing, reload orchestration, page/category provider
  contracts, packet schema changes, persistence, commands, and gameplay
  behavior were not changed.

- Phase 3, Slice 4: Admin Panel typed config detail model proposal.
- Inspected current Admin Panel row/body/payload limits.
- Decision: do not add a new packet schema or page/category provider contract
  yet. The next implementation should improve read-only Config browsing by
  adding domain and category rows using the existing snapshot/tab/row packet
  model.
- Defer true typed controls, config writes, reload orchestration, packet schema
  changes, persistence, commands, and gameplay behavior.

- Phase 3, Slice 5: Config tab domain/category read-only rows.
- Expanded `ElarionAdminPanelService.configRows(...)` to emit one domain
  summary row plus one per-category detail row per registered config domain.
- Domain rows summarize owner, files, reload command, category count, entry
  count, reloadable/restart-required/invalid counts, and category summaries.
- Category rows use stable `config:<domain>:category:<category>` IDs and keep
  entry details scoped to that category.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest`
  and `.\gradlew.bat :platform:core:test`.
- Config writes, typed editing, reload orchestration, page/category provider
  contracts, packet schema changes, persistence, commands, and gameplay
  behavior were not changed.

Phase 3, Slice 6: Admin Panel config mutation/readiness audit was completed as
documentation/proposal only.

- Verified `ElarionConfigEntry` exposes validation, bounds, reload/restart,
  read permission, and write permission metadata, but no apply/write contract.
- Verified the config registry remains read-only discovery.
- Verified the current Admin Panel action payload is generic string-parameter
  provider dispatch, not a typed config mutation API.
- Verified Config tab rows remain read-only and carry no edit actions.
- Decision: do not enable config editing yet and do not reuse generic provider
  actions as the long-term mutation API.

Phase 3, Slice 7: Core config mutation contract records was completed.

- Added `ElarionConfigChangeRequest`, `ElarionConfigChangeError`, and
  `ElarionConfigChangeResult`.
- The records model normalized targets, raw proposed values, optional
  expected-current values, actor/reason metadata, stable error codes,
  validation/applied/rejected statuses, old/new display values, reload/restart
  flags, audit event type, and immutable errors.
- No file writes, apply services, reload orchestration, packet schema changes,
  Admin Panel edit actions, persistence, commands, or gameplay behavior were
  added.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigChangeContractTest`
  and `.\gradlew.bat :platform:core:test`.

Phase 3, Slice 8: Core config mutation validation service was completed.

- Added `ElarionConfigChangeValidator`.
- The validator resolves requests against the descriptor registry, checks write
  permission metadata, detects stale expected-current values, parses submitted
  raw values through entry codecs, runs entry validators, and returns
  `ElarionConfigChangeResult.validated` or `rejected`.
- The validator is pure/read-only. It does not write files, apply config,
  reload config, emit packets, add Admin Panel edit actions, touch persistence,
  register commands, or change gameplay behavior.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigChangeValidatorTest`
  and `.\gradlew.bat :platform:core:test`.

Phase 3, Slice 9: Admin Panel config validation preview action proposal was
completed.

- Inspected Admin Panel action/open payloads, row/action models, screen action
  flow, service dispatch, Config tab row IDs, and current tests.
- Decision: do not add a dedicated packet/model yet for read-only validation
  preview.
- Decision: do not validate category rows and do not add edit/apply buttons.
- The existing generic Admin Panel action payload can be reused narrowly for a
  preview-only `Validate Value` action only after Config gains stable
  entry-level rows.
- A dedicated config packet/model remains required before true editing, typed
  controls, diff display, audit preview, or apply/reload orchestration.

Phase 3, Slice 10: Config tab entry rows and validation preview action was
completed.

- Added stable Config entry rows under each category row in the dedicated
  Config tab.
- Entry rows expose path, description, current/default values, type, bounds,
  choices, permissions, runtime marker, and current validation state.
- Added a preview-only `Validate Value` action per entry row. The action calls
  `ElarionConfigChangeValidator` server-side and returns a short valid/invalid
  message.
- The validation preview does not write files, apply values, reload config,
  emit audit events, change runtime state, add typed controls, alter packet
  schemas, touch persistence, register commands, or change gameplay behavior.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest`
  and `.\gradlew.bat :platform:core:test`.

Phase 3, Slice 11: dedicated config editing packet/model proposal was
completed.

- Inspected current Admin Panel payload/model contracts and config change
  request/result/error records.
- Decision: do not evolve `AdminPanelActionPayload` into the long-term config
  editing protocol. Keep it for generic provider actions and preview-only
  validation.
- Decision: true editing needs a Core-owned config edit packet/model family
  with explicit target, typed control metadata, expected-current value,
  proposed raw value, structured validation/apply result, reload/restart
  policy, audit preview, and bounded failure states.
- Decision: descriptor visibility does not imply editability. Each editable
  domain must later register an explicit reload-safe applier/edit provider.
- No Java behavior, packets, file writes, reloads, persistence, commands,
  gameplay behavior, or UI edit/save controls were changed.

Phase 4, Slice 15: first live server-backed UI screenshot QA pass was completed.

- Started `runServer` and `runClientOne`, connected `ElarionAdmin` to the saved
  `localhost` server, and opened each screen through its real authoritative
  interaction/session path.
- Captured Realm notification empty state, Civic Forum History, Create Civic
  Proposal modal, linked Shrine UI, and allowed Neutral Gate confirmation under
  `build/ui-qa/`.
- No custom-payload decode/disconnect error occurred. The server shut down
  cleanly after the pass.
- Findings: populated Notification rows/actions remain untested; the Neutral
  Gate icon frame was empty; the completed Shrine snapshot showed zero progress
  and zero requirements while reporting completion.
- Temporary Government/Shrine blocks, route unlock state, player position, and
  Offering anchor coordinates were restored after QA.
- No production Java, config schema, packet, command, or UI rendering code was
  changed.

Next recommended slice requiring approval:

Phase 3, Slice 12: Core config edit packet/model records was completed.

- Added `ElarionConfigEditTarget` and `ElarionConfigEditControl`.
- Added `ElarionConfigEditOpenPayload`,
  `ElarionConfigEditRequestPayload`, and
  `ElarionConfigEditResultPayload`.
- Added bounded packet codecs for edit-control snapshots, edit requests,
  edit results, targets, structured errors, and list fields.
- Added focused tests for target normalization and packet round trips.
- No server receiver registration, UI controls, file writes, apply services,
  reload orchestration, persistence, commands, or gameplay behavior were
  changed.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.network.ElarionConfigEditPayloadTest`
  and `.\gradlew.bat :platform:core:test`.

Next recommended slice requiring approval:

Phase 3, Slice 13: config edit packet registration audit/proposal was
completed.

- Verified current payload codec registration lives in `ElarionCoreMod`
  through `PayloadTypeRegistry`.
- Verified server receivers live in `ElarionCoreMod` and client screen
  receivers live in `ElarionCoreClient`.
- Decision: register config edit payload types before adding live handlers.
- Decision: `ElarionConfigEditOpenPayload` and
  `ElarionConfigEditResultPayload` are S2C; `ElarionConfigEditRequestPayload`
  is C2S.
- Decision: receiver registration should be deferred until a Core dispatch
  method is added and reviewed. Future dispatch should stay OP-gated and
  service-owned, not implemented in the receiver lambda.
- No Java behavior, packet registration, receivers, UI controls, file writes,
  apply services, reload orchestration, persistence, commands, or gameplay
  behavior were changed.

Phase 3, Slice 14: config edit payload type registration was completed.

- Registered `ElarionConfigEditOpenPayload` as S2C.
- Registered `ElarionConfigEditResultPayload` as S2C.
- Registered `ElarionConfigEditRequestPayload` as C2S.
- No server/client receiver registration, UI controls, file writes, apply
  services, reload orchestration, persistence, commands, or gameplay behavior
  were changed.
- Verification passed:
  `.\gradlew.bat :platform:core:compileJava :platform:core:test --tests panetina.elarion.core.network.ElarionConfigEditPayloadTest`
  and `.\gradlew.bat :platform:core:test`.

Next recommended slice requiring approval:

Phase 3, Slice 15: config edit receiver/dispatch proposal was completed.

- Decision: the first live receiver should support validation only.
- Decision: the receiver should delegate to `ElarionAdminPanelService` or a
  narrow Core config edit coordinator; it must not implement validation or
  mutation directly in the receiver lambda.
- Decision: `VALIDATE` converts `ElarionConfigEditRequestPayload` into
  `ElarionConfigChangeRequest`, runs `ElarionConfigChangeValidator`, and
  returns `ElarionConfigEditResultPayload`.
- Decision: `APPLY` must return an `UNSUPPORTED` rejected result until an
  editable-domain applier registry exists.
- Decision: non-OP players must receive `PERMISSION_DENIED` without target
  existence details.
- No Java behavior, receivers, UI controls, file writes, apply services, reload
  orchestration, persistence, commands, or gameplay behavior were changed.

Phase 3, Slice 16: validation-only config edit receiver and dispatch was
completed.

- Added the server receiver for `ElarionConfigEditRequestPayload`.
- Added validation-only config edit dispatch to `ElarionAdminPanelService`.
- Validation requests now convert to `ElarionConfigChangeRequest`, run through
  `ElarionConfigChangeValidator`, send `ElarionConfigEditResultPayload`, and
  refresh the Admin Panel Config tab message.
- `APPLY` returns `UNSUPPORTED`; non-OP requests return `PERMISSION_DENIED`;
  `canApply` remains false for all results.
- No client edit UI, client result handler, config writes, apply services,
  reload orchestration, persistence, commands, or gameplay behavior were added.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest`
  and `.\gradlew.bat :platform:core:test`.

Phase 3, Slice 17: config edit client result handling proposal was completed.

- Decision: add a lightweight client receiver/state cache as the next
  implementation slice.
- Decision: keep visible feedback in the existing Admin Panel refresh message
  until a dedicated edit/detail UI is specified.
- Rejected a no-op-only receiver because it would discard structured results.
- Rejected a dedicated edit detail state in the proposal slice because typed
  controls and editable-domain appliers do not exist yet.

Phase 3, Slice 18: config edit client result receiver/cache was completed.

- Added `ElarionConfigEditClientState` to store the last config edit result.
- Registered the client receiver for `ElarionConfigEditResultPayload` in
  `ElarionCoreClient`.
- Cleared cached config edit result state on client join and disconnect.
- Added focused tests for cache update, clear, and rejected-result retention.
- No Admin Panel edit controls, dedicated result rendering, server behavior,
  descriptor changes, config writes, apply services, reload orchestration,
  persistence, commands, or gameplay behavior were added.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionConfigEditClientStateTest`
  and `.\gradlew.bat :platform:core:test`.

Phase 3, Slice 19: config edit open/detail UX proposal was completed.

- Decision: config edit detail should be a dedicated state opened from a Config
  entry row, not another generic one-field modal.
- Decision: use the existing generic Admin Panel action path only to ask the
  server to open a selected entry; the server resolves the target and sends
  `ElarionConfigEditOpenPayload`.
- Decision: the client should store the open `ElarionConfigEditControl` and
  later render a dedicated detail/modal state with current/default values,
  proposed value, choices/bounds, validation errors, old/new diff,
  reload/restart policy, permission labels, Validate, and a visible disabled
  Apply state.
- `Apply` remains disabled until domain appliers exist and validation returns
  `canApply=true`.
- No Java behavior, UI rendering, config writes, apply services, reload
  orchestration, persistence, commands, or gameplay behavior were changed.

Phase 3, Slice 20: config edit open payload receiver and client open-state
cache was completed.

- Extended `ElarionConfigEditClientState` to store the current open
  `ElarionConfigEditControl`.
- Registered a client receiver for `ElarionConfigEditOpenPayload`.
- Opening a new control clears stale validation result state.
- Existing join/disconnect clearing now clears both open control and result.
- Added focused tests for open control update, clear, and stale result reset.
- No server open action, Admin Panel row action, rendered edit UI, validation
  input controls, config writes, apply services, reload orchestration,
  descriptor changes, persistence, commands, or gameplay behavior were added.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionConfigEditClientStateTest`
  and `.\gradlew.bat :platform:core:test`.

Phase 3, Slice 21: config edit server open action proposal was completed.

- Decision: add a Core-owned `Open Editor` Admin Panel row action for config
  entry rows.
- Decision: reuse the existing OP-gated `AdminPanelActionPayload` path rather
  than adding a new C2S open request payload.
- Decision: server resolves `config-entry|domain|category|entry` through
  `ElarionConfigRegistry` and sends `ElarionConfigEditOpenPayload` only for
  valid targets.

Phase 3, Slice 22: config edit server open action and Config row action was
completed.

- Added the `Open Editor` action to Config entry rows.
- Added the server-side resolver/sender in `ElarionAdminPanelService`.
- Controls are sent with `editable=false` and
  `Config editing is not enabled yet.`
- Unknown/stale targets return an Admin Panel failure message.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest`
  and `.\gradlew.bat :platform:core:test`.

Phase 3, Slice 23: config edit detail shell proposal was completed.

- Decision: first rendered detail should be a presentation-only modal/detail
  shell over `ElarionAdminPanelScreen` using
  `ElarionConfigEditClientState.openControl()`.
- Decision: show label, description, path, current/default values, type,
  choices/bounds, reload/restart policy, permissions, disabled reason, Close,
  and disabled/non-interactive Validate/Apply affordances.
- Text input, validation submission, Apply, result rendering, writes, reload
  orchestration, and appliers remain deferred.

Phase 3, Slice 24: config edit read-only detail shell was completed.

- `ElarionAdminPanelScreen` now renders a read-only config edit shell from
  `ElarionConfigEditClientState.openControl()`.
- The shell shows descriptor metadata, disabled reason, Close, and disabled
  Validate/Apply affordances.
- Escape and Close clear only the open-control state.
- `ElarionConfigEditClientState.closeOpenControl()` was added and tested.
- No text input, validation submission, result rendering beyond disabled
  placeholders, config writes, apply services, reload orchestration,
  descriptor changes, persistence, commands, or gameplay behavior were added.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionConfigEditClientStateTest`
  and `.\gradlew.bat :platform:core:test`.

Phase 3, Slice 25: config edit validation input proposal was completed.

- Decision: add proposed-value input state to the config edit shell, seeded
  from the current display value when a control opens.
- Decision: input is local to `ElarionAdminPanelScreen` and is discarded when
  the open control closes or changes.
- Decision: Validate sends `ElarionConfigEditRequestPayload` with `VALIDATE`,
  expected current display value from the server-authored control, and reason
  `admin-panel-config-edit-preview`.
- Decision: result display must be target-matched and must not show stale
  results after the proposed value changes.
- `Apply` remains disabled/non-interactive even after successful validation.
- No Java behavior, packets, UI input, config writes, apply services, reload
  orchestration, descriptor changes, persistence, commands, or gameplay
  behavior were changed.

Phase 3, Slice 26: config edit validation input and request was completed.

- Added local proposed-value input state to `ElarionAdminPanelScreen`.
- Input seeds from the server-authored current display value when a control
  opens.
- Typing and Backspace edit the proposed value while the shell is open.
- Input changes clear stale validation result state.
- Enter and Validate send `ElarionConfigEditRequestPayload` with `VALIDATE`.
- Result rendering is target-matched against the open control.
- Apply remains visible but disabled/non-interactive.
- No `APPLY` requests, config writes, reload orchestration, descriptor changes,
  persistence, commands, or gameplay behavior were added.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionConfigEditClientStateTest`
  and `.\gradlew.bat :platform:core:test`.

Phase 3, Slice 27: config edit apply/readiness proposal was completed.

- Verified current config change handling is validation-only. It can resolve
  descriptors, check permissions, reject stale expected-current values, parse
  proposed values, and run descriptor validators, but it has no writer,
  reload orchestrator, rollback behavior, or apply audit contract.
- Decision: `Apply` must stay disabled until Core has a separate
  apply/applier contract. Descriptor visibility and successful validation do
  not imply editability.
- Future apply support must require explicit domain appliers, re-run
  server-side validation before write, serialize concurrent mutation per
  domain/file group, preserve previous runtime snapshots on failure, emit
  administrative domain events/history only after successful mutation, and
  refresh affected Admin Panel/client snapshots.
- No production code, config files, descriptors, packets, reload behavior, or
  UI behavior changed in this proposal-only slice.

Phase 3, Slice 28: Core config apply contract records/registry was completed.

- Added `ElarionConfigApplyRegistry`, `ElarionConfigApplier`,
  `ElarionConfigApplyCapability`, `ElarionConfigApplyContext`, and
  `ElarionConfigApplyReadiness`.
- Registrations are explicit per `ElarionConfigEditTarget`, reject duplicates,
  declare audit/file/reload/restart capability metadata, and expose
  descriptor-aware readiness failures for unknown, missing, disabled, or
  policy-incompatible targets.
- The contract is inert: it is not exposed through `ElarionApi`, has no
  production registrations, and is not wired to Admin Panel dispatch.
- No file writes, reload/rollback orchestration, UI Apply enablement,
  descriptors, or addon/domain appliers changed.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigApplyRegistryTest`
  and `.\gradlew.bat :platform:core:test`.

Next recommended slice requiring approval:

- Phase 3, Slice 29: config apply registry ownership and readiness wiring
  proposal.
- Classification: SMALL.
- Audit registry lifetime/API ownership and how readiness should feed
  server-authored edit controls and validation results while Apply remains
  disabled.
- Exclude implementation, file writes, reload/rollback orchestration, UI Apply
  enablement, descriptor changes, and addon/domain appliers.

Phase 3, Slice 29: config apply registry ownership and readiness wiring
proposal was completed.

- Decision: Core owns one process-lifetime canonical apply registry beside the
  descriptor registry.
- Decision: addons must receive a registration-only facade. Do not expose the
  concrete registry because its registration lookup contains executable
  appliers that could bypass Admin authorization and audit orchestration.
- Decision: Admin consumes a non-executable readiness view. Open controls may
  show target-specific readiness failures, but `editable` and result
  `canApply` remain false until a Core apply coordinator exists.
- Decision: an addon registers an applier immediately after its matching
  descriptor and validated runtime snapshot exist. Deferred descriptor domains
  such as NPCs register both under the same one-time startup guard.
- No production code, packets, config, persistence, reload behavior, or UI
  behavior changed.

Next recommended slice requiring approval:

- Phase 3, Slice 30: canonical apply registry ownership and registration-only
  Core API.
- Classification: MEDIUM.
- Add a narrow registrar interface/facade, construct one canonical registry in
  Core, and expose only registration through `ElarionSystemApi`.
- Exclude Admin readiness integration, production/domain registrations,
  applier execution, file writes, reload/rollback, audit emission, and UI Apply
  enablement.

Phase 3, Slice 30: canonical apply registry ownership and registration-only
Core API was completed.

- Added `ElarionConfigApplyRegistrar` as the public registration-only contract.
- Core constructs one canonical `ElarionConfigApplyRegistry` beside the
  descriptor registry before API/addon initialization.
- `ElarionSystemApi.configAppliers()` exposes a method-reference facade, not the
  concrete registry, so addons cannot retrieve executable registrations.
- Added focused coverage for facade delegation, duplicate rejection through the
  facade, its single-method surface, and the public API return type.
- No production appliers, Admin readiness integration, execution, file writes,
  reload/rollback, audit emission, packets, persistence, or UI behavior changed.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigApplyRegistryTest`
  and `.\gradlew.bat :platform:core:test`.

Next recommended slice requiring approval:

- Phase 3, Slice 31: non-executable Admin config readiness integration.
- Classification: MEDIUM.
- Add a readiness-only Core view and use it to provide target-specific disabled
  reasons in server-authored edit controls/results while `editable=false`,
  `canApply=false`, and the client Apply button remain unchanged.
- Exclude applier execution, production/domain registrations, file writes,
  reload/rollback, audit emission, and UI Apply enablement.

Phase 3, Slice 31: non-executable Admin config readiness integration was
completed.

- Added `ElarionConfigApplyReadinessProvider`, exposing only
  `readiness(target)`.
- Core binds a method-reference view over the canonical apply and descriptor
  registries into `ElarionAdminPanelService`.
- Config edit controls and successful validation results now show precise
  missing/policy readiness reasons. A ready registration reports that execution
  is not enabled.
- `editable=false`, `canApply=false`, server `APPLY` rejection, and the disabled
  client button remain unchanged.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest --tests panetina.elarion.core.config.ElarionConfigApplyRegistryTest`
  and `.\gradlew.bat :platform:core:test`.

Next approved slice:

- Phase 3, Slice 32: Core config apply coordinator contract audit.
- Classification: SMALL.
- Decide validation/readiness recheck, locking, trusted context resolution,
  applier failure handling, atomicity/rollback ownership, audit emission, and
  result invariants before implementing execution.
- Exclude production code, Admin Apply wiring, production/domain appliers, file
  writes, reload behavior, and UI enablement.

Phase 3, Slice 32: Core config apply coordinator contract audit was completed.

- Verified the current direct `ElarionConfigApplier.apply(...)` callback has no
  coordinator-controlled prepare/commit/rollback boundary.
- Verified audit currently requires two post-mutation operations: domain event
  emission and administration history recording. A failure after mutation
  cannot be safely handled by the current callback contract.
- Verified `ElarionConfigChangeResult.applied(...)` currently drops descriptor
  reload/restart flags, so coordinator results cannot yet preserve validated
  runtime policy.
- Decision: do not implement the coordinator or enable Apply on the current
  callback contract. First evolve appliers to prepare a transaction with
  explicit commit and rollback operations.
- Future coordinator uses one conservative global config-mutation lock initially,
  revalidates and rechecks readiness under that lock, resolves trusted
  descriptors, prepares the owner transaction, commits, emits audit/history,
  and rolls back if commit/audit completion fails.
- No production code changed in this audit slice.

Next approved slice:

- Phase 3, Slice 33: transactional config applier contract.
- Classification: MEDIUM.
- Add a prepared-change transaction contract, change the unused applier API from
  immediate apply to prepare, preserve reload/restart metadata in applied
  results, and update focused tests.
- Exclude coordinator implementation, production registrations, Admin Apply
  wiring, file writes, reload behavior, audit emission, and UI enablement.

Phase 3, Slice 33: transactional config applier contract was completed.

- Added `ElarionConfigPreparedChange` with `commit()`, `rollback()`, and a safe
  `of(commit, rollback)` helper.
- The helper permits one successful commit, prevents commit after rollback,
  permits rollback after a failed or successful commit, and invokes rollback at
  most once.
- Changed the unused `ElarionConfigApplier` API from immediate `apply(context)`
  to non-mutating `prepare(context)` returning a prepared transaction.
- Added an applied-result factory preserving reload/restart flags and required a
  nonblank audit event type for every `APPLIED` result.
- Updated focused registry/Admin tests and added prepared-change lifecycle
  coverage.
- Verification passed: focused prepared-change, change-contract, registry, and
  Admin tests; full `.\gradlew.bat :platform:core:test`; then focused
  prepared-change/change-contract tests after final API cleanup.
- No coordinator, production applier, Admin Apply, file write, reload, audit,
  packet, persistence, or UI behavior was added.

Next recommended slice requiring approval:

- Phase 3, Slice 34: internal config apply coordinator implementation.
- Classification: MEDIUM.
- Add an unwired Core coordinator with one global mutation lock, in-lock
  validation/readiness, trusted context resolution, transactional
  prepare/commit, mandatory injected audit sink, strict result invariants, and
  rollback on commit/audit failure.
- Exclude Core/addon production registrations, Admin/network/UI wiring, real
  file writes, reload orchestration, and client synchronization.

Phase 3, Slice 34: internal config apply coordinator implementation was
completed.

- Added structured `ElarionConfigApplyAuditRecord` and mandatory
  `ElarionConfigApplyAuditSink` contracts.
- Added an unwired `ElarionConfigApplyCoordinator` using one fair,
  process-global mutation lock.
- The coordinator revalidates and rechecks readiness under lock, resolves
  trusted descriptors, prepares/commits the owner transaction, strictly checks
  returned request/values/runtime policy/audit type, and records audit only
  after a trusted commit.
- Prepare, commit, owner-result, or audit failures return `APPLY_FAILED`;
  post-prepare failures invoke rollback and include rollback failure details.
- Added focused tests for success/audit data, invalid/stale/missing rejection,
  preparation and commit failure, invalid owner result, audit rollback,
  rollback failure reporting, and concurrent serialization.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigApplyCoordinatorTest`
  and `.\gradlew.bat :platform:core:test`.
- No production coordinator instance, applier, Admin/network/UI wiring, real
  file write, reload orchestration, or client synchronization was added.

Next recommended slice requiring approval:

- Phase 3, Slice 35: coordinator ownership and audit-adapter proposal.
- Classification: SMALL.
- Decide canonical coordinator lifetime, history/domain-event audit ordering and
  metadata, Admin execution exposure, and behavior when Core history is not
  bound.
- Exclude implementation, production appliers, Admin Apply enablement, file
  writes, reload orchestration, packets, UI, and synchronization.

Phase 3, Slice 35: coordinator ownership and audit-adapter proposal was
completed.

- Verified `HistoryService.record(...)` requires a bound server, is filtered by
  configurable history policy, and queues writes. It cannot be the mandatory
  durable audit authority for config mutation.
- Verified domain-event dispatch catches listener failures and is suitable as a
  post-commit integration projection, not durable audit state.
- Decision: do not construct the canonical production coordinator until Core
  has a bound write-ahead config audit journal.
- The audit sequence must be journal PREPARED (forced), owner commit, journal
  COMMITTED (forced), then best-effort History/domain-event projections. A
  projection failure after durable COMMITTED must not roll back the config.
- Admin will eventually receive an execution-only facade, not the concrete
  coordinator or registry. Execution remains unavailable while the journal is
  unbound.
- No production code changed.

Next recommended slice requiring approval:

- Phase 3, Slice 36: write-ahead config audit session contracts.
- Classification: MEDIUM.
- Replace the one-shot audit sink with prepare/committed/failed session
  semantics and adjust the unwired coordinator/tests. Keep persistence and
  production ownership unwired.
- Exclude journal storage/files, production coordinator construction, appliers,
  Admin/network/UI wiring, reload, and synchronization.

Phase 3, Slice 36: write-ahead config audit session contracts was completed.

- Added `ElarionConfigApplyAuditSession` with committed, rolled-back, and failed
  terminal operations.
- Changed `ElarionConfigApplyAuditSink` from one-shot post-commit recording to
  `prepare(record)` returning the write-ahead session.
- The coordinator now prepares audit after non-mutating owner preparation but
  before commit. Audit preparation failure rolls back prepared resources without
  committing.
- Trusted success requires the session's committed terminal operation. Commit,
  result, or committed-terminal failure rolls back and records rolled-back;
  rollback failure records failed. Audit outcome failures remain visible in the
  returned `APPLY_FAILED` message.
- Updated focused tests for phase ordering and all terminal paths.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigApplyCoordinatorTest`
  and `.\gradlew.bat :platform:core:test`.
- No audit storage/file, production coordinator, applier, Admin/network/UI
  wiring, reload, or synchronization was added.

Next recommended slice requiring approval:

- Phase 3, Slice 37: durable config audit journal storage.
- Classification: MEDIUM persistence slice.
- Implement the unbound journal sink with versioned JSONL PREPARED/COMMITTED/
  ROLLED_BACK/FAILED records, synchronous append+force, bounded unresolved-tail
  recovery, and temp-directory tests.
- Exclude production binding, coordinator ownership, appliers,
  Admin/network/UI wiring, config file writes, reload, and synchronization.

Phase 3, Slice 37: durable config audit journal storage was completed.

- Added `ElarionConfigApplyAuditPhase`.
- Added `ElarionConfigApplyAuditJournal` as the unbound durable audit sink.
- The journal writes versioned JSONL PREPARED, COMMITTED, ROLLED_BACK, and
  FAILED records, synchronously appends and forces every record, and stores the
  full normalized audit record with one audit ID across phases.
- The journal exposes `journalPath(root)` for
  `world/elarion/core/audit/config-changes.jsonl` style binding, but no
  production service binds it yet.
- Added bounded unresolved-tail recovery for startup diagnostics and
  temporary-directory tests for phase appends, terminal outcomes, pending
  recovery, bounded tail behavior, double terminal rejection, append failure,
  and path shape.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigApplyAuditJournalTest`,
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigApplyCoordinatorTest`,
  and `.\gradlew.bat :platform:core:test`.
- No production coordinator owner, server/world binding, domain applier,
  Admin/network/UI Apply wiring, config file write, reload, or synchronization
  was added.

Next recommended slice requiring approval:

- Phase 3, Slice 38: production config apply ownership proposal.
- Classification: SMALL proposal slice.
- Decide the Core service/facade that will bind the descriptor registry, apply
  registry, coordinator, and durable audit journal to the active server/world
  path.
- Exclude domain appliers, Admin Apply wiring, config file writes, reload, and
  synchronization until the ownership boundary is approved.

Phase 3, Slice 38: production config apply ownership proposal was completed.

- Decision: add a Core-owned `ElarionConfigApplyService` as the production
  owner for constructing/binding `ElarionConfigApplyCoordinator`.
- The service should receive the canonical descriptor registry and concrete
  apply registry during Core initialization, then bind on `SERVER_STARTED` using
  `JsonStateStorage.elarionRoot(server)` and
  `ElarionConfigApplyAuditJournal.journalPath(root)`.
- The service should refuse execution while unbound, when journal recovery is
  unsafe, or when bounded recovery sees unresolved PREPARED audit records.
- Public addon API remains registration-only:
  `ElarionApi.system().configAppliers()`.
- Admin should receive only a narrow readiness/executor facade, not coordinator,
  concrete registry lookup, or journal mutation access.
- No code, appliers, Admin Apply wiring, config writes, reload, sync, or packet
  behavior changed in this proposal slice.

Next recommended slice requiring approval:

- Phase 3, Slice 39: implement `ElarionConfigApplyService` skeleton.
- Classification: SMALL.
- Add the service class, bind it in `ElarionCoreMod`, route Admin readiness
  through the service, and add focused tests for unbound, bound, unresolved
  audit recovery, and readiness delegation.
- Exclude execute/apply wiring, domain appliers, Admin Apply enablement, config
  file writes, reload, and synchronization.

Phase 3, Slice 39: `ElarionConfigApplyService` skeleton was completed.

- Added `ElarionConfigApplyService` as the Core-owned production lifecycle
  owner for future config apply execution.
- Core constructs the service with the canonical descriptor registry and
  concrete apply registry.
- Core binds the service on `SERVER_STARTED` using
  `JsonStateStorage.elarionRoot(server)` and unbinds on `SERVER_STOPPING`.
- Admin readiness now flows through the service instead of directly through the
  concrete apply registry.
- The service blocks otherwise-ready targets while unbound, when bounded audit
  recovery is truncated, or when unresolved PREPARED audit records exist.
- Added focused tests for unbound, bound, missing applier/descriptor delegation,
  unresolved audit recovery, truncated recovery, and unbind behavior.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionConfigApplyServiceTest`,
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigApplyCoordinatorTest --tests panetina.elarion.core.config.ElarionConfigApplyAuditJournalTest`,
  and `.\gradlew.bat :platform:core:test`.
- No execute/apply method, domain applier, Admin Apply enablement, config file
  write, reload, synchronization, or packet behavior was added.

Next recommended slice requiring approval:

- Phase 3, Slice 40: backend config apply execution method.
- Classification: SMALL.
- Add a narrow `apply(request, actorPermission)` method to
  `ElarionConfigApplyService` that returns `UNSUPPORTED` while unbound/unsafe
  and delegates to the coordinator only when service execution is ready.
- Cover success/failure with fake in-test appliers only.
- Exclude production domain appliers, Admin/network/UI Apply wiring, config
  file writes, reload, and synchronization.

Phase 3, Slice 40: backend config apply execution method was completed.

- Added `ElarionConfigApplyService.apply(request, actorPermission)`.
- The method rejects with `UNSUPPORTED` while the service is unbound or audit
  recovery is unsafe.
- When the service is bound and safe, it delegates to the internal coordinator.
- Focused tests cover unbound rejection, unsafe-recovery rejection, successful
  delegation through a fake in-test applier, validation failure delegation, and
  missing-applier readiness failure delegation.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionConfigApplyServiceTest`,
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigApplyCoordinatorTest --tests panetina.elarion.core.config.ElarionConfigApplyAuditJournalTest`,
  and `.\gradlew.bat :platform:core:test`.
- No production domain applier, Admin/network/UI Apply wiring, config file
  write, reload, or synchronization was added.

Next recommended slice requiring approval:

- Phase 3, Slice 41: Admin config apply executor boundary proposal.
- Classification: SMALL proposal slice.
- Decide the narrow interface Admin should use for apply execution, how OP
  checks and packet responses should flow, and how to keep the UI disabled until
  production appliers are explicitly approved.
- Exclude implementation, production domain appliers, config file writes,
  reload, and synchronization.

Phase 3, Slice 41: Admin config apply executor boundary proposal was completed.

- Decision: add a narrow `ElarionConfigApplyExecutor` contract.
- The executor should expose only readiness lookup and backend apply execution.
- `ElarionConfigApplyService` should implement this executor.
- `ElarionAdminPanelService` should bind the executor instead of only a
  readiness provider.
- The first implementation should preserve current behavior: server-side
  `Intent.APPLY` still rejects, and the client Apply button remains disabled.
- Later server-side APPLY dispatch must be its own slice with OP checks, stale
  expected-current behavior, result payload conversion, and Admin refresh tests.
- Existing packet schema already has `Intent.APPLY`; no packet schema change is
  required for the boundary.

Next recommended slice requiring approval:

- Phase 3, Slice 42: add Admin config apply executor facade.
- Classification: SMALL.
- Add `ElarionConfigApplyExecutor`, have `ElarionConfigApplyService` implement
  it, and bind Admin to the executor while keeping APPLY rejected.
- Exclude server-side APPLY dispatch, production appliers, config file writes,
  reload, synchronization, and UI Apply enablement.

Phase 3, Slice 42: Admin config apply executor facade was completed.

- Added `ElarionConfigApplyExecutor` with readiness lookup and backend apply
  execution.
- `ElarionConfigApplyService` now implements the executor.
- `ElarionAdminPanelService` now binds an executor instead of only a readiness
  provider.
- Core initialization binds Admin to `configApplyService` through
  `bindConfigApplyExecutor(...)`.
- The legacy readiness-only binding helper remains available for focused tests
  and wraps readiness in an executor that rejects apply.
- Server-side `Intent.APPLY` still rejects before dispatch, and the client Apply
  button remains disabled.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest --tests panetina.elarion.core.service.ElarionConfigApplyServiceTest`
  and `.\gradlew.bat :platform:core:test`.
- No server-side APPLY dispatch, production applier, config file write, reload,
  synchronization, or UI Apply enablement was added.

Next recommended slice requiring approval:

- Phase 3, Slice 43: server-side Admin APPLY dispatch proposal.
- Classification: SMALL proposal slice.
- Decide exact request/result conversion from `ElarionConfigEditRequestPayload`
  APPLY to `ElarionConfigApplyExecutor.apply(...)`, OP checks, stale-value
  behavior, Admin refresh, and whether to keep the client Apply button disabled
  until a production applier exists.
- Exclude implementation, production appliers, config file writes, reload, and
  synchronization.

Phase 3, Slice 43: server-side Admin APPLY dispatch proposal was completed.

- Decision: keep `ElarionConfigEditRequestPayload` unchanged; existing
  `Intent.APPLY` is sufficient.
- Keep the `ElarionCoreMod` receiver shape unchanged: packet enters the server
  thread, Admin service returns `ElarionConfigEditResultPayload`, client gets
  the result, and Config tab refreshes with the result message.
- Evolve Admin service conversion internally so VALIDATE uses the validator and
  APPLY calls `ElarionConfigApplyExecutor.apply(...)` after OP checks.
- Build `ElarionConfigChangeRequest` once from target, proposed raw value,
  expected-current display value, actor UUID, and reason.
- Empty reasons should become `admin-panel-config-edit-preview` for VALIDATE and
  `admin-panel-config-edit-apply` for APPLY.
- APPLIED results convert to `ElarionConfigEditResultPayload` with status
  APPLIED, old/new values, runtime flags, no errors, `canApply=false`, audit
  preview, and an `Applied:` message.
- Client Apply remains disabled until a later UI slice enables it
  conditionally from server-authored controls.

Next recommended slice requiring approval:

- Phase 3, Slice 44: implement server-side Admin APPLY dispatch.
- Classification: SMALL.
- Change only Admin service request/result conversion and focused tests.
- Keep client Apply disabled and use fake test executors only.
- Exclude production appliers, config file writes, reload, synchronization, and
  UI Apply enablement.

Phase 3, Slice 44: server-side Admin APPLY dispatch was completed.

- `ElarionAdminPanelService.configEditResult(...)` now handles both VALIDATE and
  APPLY.
- VALIDATE behavior remains unchanged.
- APPLY now builds the same `ElarionConfigChangeRequest` shape and calls
  `ElarionConfigApplyExecutor.apply(...)` after OP checks.
- Empty APPLY reasons use `admin-panel-config-edit-apply`.
- APPLIED executor results convert to `ElarionConfigEditResultPayload` with
  old/new values, runtime flags, no errors, `canApply=false`, an audit preview,
  and an `Applied:` message.
- Rejected executor results preserve errors and return `Apply failed: ...`.
- Focused tests cover non-OP rejection before dispatch, OP fake-executor
  dispatch, APPLIED payload conversion, rejected executor result conversion,
  stale expected-current behavior through executor validation, and unchanged
  VALIDATE behavior.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest`,
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionConfigApplyServiceTest`,
  and `.\gradlew.bat :platform:core:test`.
- No production applier, config file write, reload, synchronization, or UI Apply
  enablement was added.

Next recommended slice requiring approval:

- Phase 3, Slice 45: Admin Apply UI enablement proposal.
- Classification: SMALL proposal slice.
- Decide how server-authored edit controls should expose Apply availability,
  whether the client button stays disabled until a production applier exists,
  and what manual/automated checks are required before any user-facing Apply
  button is clickable.
- Exclude implementation, production appliers, config file writes, reload, and
  synchronization.

Phase 3, Slice 45: Admin Apply UI enablement proposal was completed.

- Decision: keep the client Apply button disabled until at least one production
  applier is approved and tested.
- Apply availability must be server-authored, not inferred from validation
  success.
- The existing `editable` control flag is too broad because it mixes input
  editability and apply execution.
- Future control metadata should distinguish input editability from apply
  availability, for example `inputEditable`, `applyAvailable`, and
  `applyDisabledReason`.
- Future client Apply enablement must require an open control, nonblank input,
  matching latest VALIDATED result, matching old/new values, server-authored
  apply availability, and result `canApply=true`.
- The server must continue to reject forged APPLY packets when no safe executor
  or applier is available.

Next recommended slice requiring approval:

- Phase 3, Slice 46: split config edit control input/apply state.
- Classification: SMALL.
- Add explicit apply availability metadata to the control/payload model and
  focused packet/client-state tests, but keep the Admin screen Apply button
  disabled.
- Exclude production appliers, config file writes, reload, synchronization, and
  clickable Apply UI.

Phase 3, Slice 46: split config edit control input/apply state was completed.

- `ElarionConfigEditControl` now carries `inputEditable`, `applyAvailable`,
  `disabledReason`, and `applyDisabledReason`.
- The old constructor remains as a compatibility path and maps `editable` to
  `inputEditable` with `applyAvailable=false`.
- The legacy `editable()` method remains as an alias for `inputEditable()`.
- `ElarionConfigEditOpenPayload` round-trip now includes explicit apply
  availability metadata.
- The Admin screen continues to render/click Apply as disabled, but can display
  `applyDisabledReason` separately when the input is editable.
- Focused tests cover packet round-trip, client-state preservation, and
  Admin-open controls exposing explicit apply-disabled state.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.network.ElarionConfigEditPayloadTest --tests panetina.elarion.core.client.ElarionConfigEditClientStateTest --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest`,
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionConfigApplyServiceTest`,
  and `.\gradlew.bat :platform:core:test`.
- No production applier, config file write, reload, synchronization, or
  clickable Apply UI was added.

Next recommended slice requiring approval:

- Phase 3, Slice 47: production config applier selection proposal.
- Classification: SMALL proposal slice.
- Choose the first low-risk production config entry/domain to make writable and
  define file write, reload, rollback, audit, and UI rollout requirements.
- Exclude implementation.

Phase 3, Slice 47: production config applier selection proposal was completed.

- Selected first writable target:
  `core:ui_theme:defaults.font-scale-percent`.
- Source file: `config/elarion/core/ui_theme.yml`.
- YAML path: `defaults.font-scale-percent`.
- Type/policy: bounded integer `100-150`, runtime-reloadable, not
  restart-required, OPERATOR write permission.
- Future audit event type:
  `core.config.ui_theme.font_scale_changed`.
- Reason for choosing it first: one scalar value, existing descriptor and
  validator coverage, existing Core reload path, existing client UI theme sync,
  and no mutation of gameplay authority, Realm routing, economy, title
  ownership, or runtime persistence.
- Deferred Realm spawn edits because they are multi-field, gameplay-affecting
  location transactions and should not be the first production config write.
- Deferred server identity, dynamic Realm/title/reward definition rows, addon
  domains, and restart-required settings.
- Implementation requirements for the next backend slice are documented in
  `docs/reports/CONFIG_ADMIN_AUDIT.md`.
- No production applier, file write, reload, synchronization, UI click
  enablement, config behavior, or gameplay behavior was changed.

Next recommended slice requiring approval:

- Phase 3, Slice 48: Core UI theme font-scale applier backend.
- Classification: MEDIUM.
- Implement only the backend applier registration, file mutation, reload,
  rollback, audit-backed apply execution, and focused tests for
  `core:ui_theme:defaults.font-scale-percent`.
- Keep the Admin screen Apply button disabled unless separately approved for UI
  click enablement.

Phase 3, Slice 48: Core UI theme font-scale applier backend was completed.

- Added `CoreUiThemeFontScaleConfigApplier`.
- Registered exactly one production backend target:
  `core:ui_theme:defaults.font-scale-percent`.
- The target writes only `config/elarion/core/ui_theme.yml` at
  `defaults.font-scale-percent`.
- The applier stages exact old/new file contents, replaces exactly one
  `font-scale-percent:` scalar line, writes through a temp file plus replace,
  reloads `CoreConfigManager`, and resyncs UI themes through the existing Core
  UI theme service.
- Rollback restores the prior file contents, reloads the previous valid theme,
  and resyncs UI themes.
- Core initialization registers the applier with the canonical apply registry
  and tracks the active server only for post-apply theme sync.
- Focused verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.CoreUiThemeFontScaleConfigApplierTest --tests panetina.elarion.core.service.ElarionConfigApplyServiceTest --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest`.
- Full Core verification passed: `.\gradlew.bat :platform:core:test`.
- The visible Admin Apply button remains disabled. No client click path, new
  addon appliers, Realm spawn editing, server identity editing, or
  restart-required config writing was added.

Next recommended slice requiring approval:

- Phase 3, Slice 49: Admin Apply click enablement for the single font-scale
  target.
- Classification: MEDIUM.
- Enable the client Apply button only when server-authored apply availability
  and a matching latest validation result allow it, and keep all other config
  targets disabled.
- Exclude adding any new production config appliers.

Phase 3, Slice 49: Admin Apply click enablement for the single font-scale
target was completed.

- Server-opened config edit controls now derive `inputEditable` and
  `applyAvailable` from backend apply readiness.
- Validation results now return `canApply=true` only when the target has a
  ready apply backend.
- The Admin client enables Apply only when:
  - the open control is input-editable and apply-available.
  - the proposed input is nonblank.
  - the latest result targets the same config entry.
  - the latest result is `VALIDATED`.
  - the latest result has `canApply=true`.
  - the result has no errors.
  - the validated old value matches the current control value.
  - the validated new value matches the current proposed input.
- Apply sends `ElarionConfigEditRequestPayload.Intent.APPLY` only from that
  predicate.
- Applied results close the edit shell so stale current values are not reused
  after reload/sync.
- Config edit packet registrations were verified in source:
  `ElarionConfigEditOpenPayload` and `ElarionConfigEditResultPayload` are S2C;
  `ElarionConfigEditRequestPayload` is C2S.
- Focused verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest --tests panetina.elarion.core.network.ElarionConfigEditPayloadTest --tests panetina.elarion.core.client.ElarionConfigEditClientStateTest --tests panetina.elarion.core.config.CoreUiThemeFontScaleConfigApplierTest`.
- Full Core verification passed: `.\gradlew.bat :platform:core:test`.
- No new production config appliers, addon config editing, Realm spawn editing,
  server identity editing, restart-required config writes, or generic
  all-config Apply behavior was added.

Phase 3 V1 status:

- Complete for the first safe Admin config edit path:
  read-only descriptor discovery, server-side validation, audit-backed backend
  apply, safe file write/reload/rollback, UI theme resync, and strict client
  Apply gating for `core:ui_theme:defaults.font-scale-percent`.
- Remaining work moves to later phases/slices: broader Admin page/provider
  contracts, additional production config appliers, addon-specific reload
  safety, manual client QA, and Phase 4 shared UI tokens/primitives.

Phase 4, Slice 1: shared UI token/component audit and proposal was completed.

- Report: `docs/reports/UI_SYSTEM_AUDIT.md`.
- Verified Core already owns base UI primitives and scaled typography, while
  the strongest civic row/action/shell helpers remain split between Government,
  Notification, and Admin classes.
- No production code was changed.
- Decision: add generic civic UI tokens and primitives to Core before any full
  screen migration.

Phase 4, Slice 2: Core civic UI token and primitive helper foundation was
completed.

- Added `ElarionCivicColors`, `ElarionCivicUi`, and `ElarionUiMetrics`.
- Added focused metric coverage in `ElarionUiMetricsTest`.
- No existing screen was migrated and no packet, persistence, config, or
  server behavior changed.
- Focused verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionUiMetricsTest`.
- Full Core verification passed: `.\gradlew.bat :platform:core:test`.

Phase 4, Slice 3: Notification HUD helper adoption was completed.

- Notification rail slots and notification rows now use
  `ElarionCivicUi.rowSurface`.
- Notification thin boxes now delegate to `ElarionCivicUi.thinBox`.
- Notification action button frames now use
  `ElarionCivicUi.compactActionButtonFrame` while preserving icon-plus-label
  centering.
- Notification action height now uses `ElarionUiMetrics.controlHeight`.
- Notification local civic color names now alias `ElarionCivicColors`.
- Focused verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionUiMetricsTest`.
- Full Core verification passed: `.\gradlew.bat :platform:core:test`.

Phase 4, Slice 4: Notification HUD shell helper adoption was completed for
code; live screenshot QA remains pending.

- Added Core civic tokens for rail/message/divider/destructive/info UI states.
- Added Core civic helpers for header shells, rail shells, message bodies,
  header ornaments, close buttons, and reusable dimming.
- Notification HUD now delegates rail shell, drawer shell, header ornaments,
  close button, divider, message body, row surfaces, action button frames, thin
  boxes, and action height to Core civic helpers.
- No notification packet, persistence, storage, filtering, category, or action
  semantic behavior changed.
- Focused verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionUiMetricsTest`.
- Full Core verification passed: `.\gradlew.bat :platform:core:test`.
- Live screenshot QA was not run because no active Minecraft client/screenshot
  control path was available in this turn.

Phase 4, Slice 5: Notification HUD layout contract was completed.

- No active Minecraft client/screenshot control path was available, so live
  screenshot QA was not run.
- Added bootstrap-free `ElarionNotificationHudLayout` for tested Notification
  HUD geometry and scaled metric constants.
- `ElarionNotificationHud` now aliases tested layout constants and metric
  helpers from `ElarionNotificationHudLayout`.
- Added `ElarionNotificationHudLayoutTest` covering close-button/header
  centering, list bounds, and scaled row/action heights.
- The first direct-HUD test attempt failed because `ItemStack.EMPTY` requires
  Minecraft registry bootstrap in a plain JVM test; extracting the layout
  helper fixed that.
- Focused verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionNotificationHudLayoutTest`.
- Additional focused verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionUiMetricsTest`.
- Full Core verification passed: `.\gradlew.bat :platform:core:test`.

Phase 4, Slice 6: Notification HUD QA follow-up was completed in limited form.

- No attached Minecraft client, app terminal, or screenshot-control path was
  available, so live Notification drawer screenshot QA could not be performed.
- The only code correction made was routing the Notification action-footer
  divider through `ElarionCivicUi.divider`.
- Focused verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionNotificationHudLayoutTest`.
- Full Core verification passed: `.\gradlew.bat :platform:core:test`.
- Live Notification drawer screenshot QA remains a high-priority manual task.

Phase 4, Slice 7: Admin Panel helper adoption was completed.

- Non-danger Admin rows now use `ElarionCivicUi.rowSurface`.
- Danger rows use Core civic destructive colors.
- Detail action buttons now use `ElarionCivicUi.compactActionButton`.
- Confirmation modal and config edit shell overlays now use
  `ElarionCivicColors.MODAL_OVERLAY`.
- Confirmation/config modal buttons now use `ElarionCivicUi.compactActionButton`
  with normal, primary, or destructive tones.
- Admin click targets, config validation/apply predicates, packet sends,
  permissions, storage, and provider action semantics were not changed.
- Focused verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionAdminPanelScreenLayoutTest --tests panetina.elarion.core.client.ElarionConfigEditClientStateTest`.
- Full Core verification passed: `.\gradlew.bat :platform:core:test`.

Next recommended slice requiring approval:

- If live QA becomes available: run Notification drawer and Admin Panel
  screenshot QA and apply only small visual corrections.
- If live QA remains unavailable: continue Phase 4 helper adoption with Admin
  Panel shell/detail panels or another low-risk Core screen.
- Classification: MEDIUM for another helper-adoption slice.
- Exclude Admin config packet/edit behavior, permissions, storage, and config
  apply semantics.

Phase 4, Slice 8: Admin Panel shell and detail helper adoption was completed.

- The Admin Panel root frame now uses `ElarionCivicUi.attachedShell`.
- The Admin header now uses shared `ElarionCivicUi.headerOrnament` accents.
- The row list outer frame and filter field now use shared civic thin boxes.
- The detail panel and confirmation/config modal shells now use
  `ElarionCivicUi.headerShell`.
- Modal text input, config proposed value, and config result surfaces now use
  civic thin/message body helpers.
- Admin click targets, list/detail geometry constants, config validation/apply
  predicates, packet sends, permissions, storage, and provider action
  semantics were not changed.
- Focused verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionAdminPanelScreenLayoutTest --tests panetina.elarion.core.client.ElarionConfigEditClientStateTest`.
- Full Core verification passed: `.\gradlew.bat :platform:core:test`.
- Live Admin Panel and Notification drawer screenshot QA remain pending.

Next recommended slice requiring approval:

- If live QA becomes available: run Notification drawer and Admin Panel
  screenshot QA and apply only small visual corrections.
- If live QA remains unavailable: continue Phase 4 helper adoption with
  Collection shell/list/detail surfaces or another low-risk Core screen.
- Classification: MEDIUM for another helper-adoption slice.
- Exclude Collection packet/action/provider behavior, unlock state, title
  activation, mount preview behavior, storage, and server authority.

Phase 4, Slice 9: Collection shell/list/detail helper adoption was completed.

- The Collection root frame now uses `ElarionCivicUi.attachedShell`.
- The Collection title header now uses shared `ElarionCivicUi.headerOrnament`
  accents.
- The content header and list frame now use shared civic thin boxes.
- The detail panel now uses `ElarionCivicUi.headerShell`.
- The preview frame and preview body now use civic thin/message body helpers.
- Collection action buttons now use `ElarionCivicUi.compactActionButton`.
- Collection-specific active/selected row rendering and icon-frame rendering
  were left intact because they encode existing Collection visual states.
- Click targets, layout constants, packet sends, provider actions, unlock
  state, title activation, mount preview rendering, storage, and server
  authority were not changed.
- Focused verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionCollectionScreenLayoutTest --tests panetina.elarion.core.network.CollectionPayloadTest --tests panetina.elarion.core.service.ElarionCollectionServiceTest`.
- Full Core verification passed: `.\gradlew.bat :platform:core:test`.
- Live Collection, Admin Panel, and Notification drawer screenshot QA remain
  pending.

Next recommended slice requiring approval:

- If live QA becomes available: run Notification drawer, Admin Panel, and
  Collection screenshot QA and apply only small visual corrections.
- If live QA remains unavailable: continue Phase 4 helper adoption with
  Character Creation and Realm Assignment shell/input/choice surfaces.
- Classification: MEDIUM for another helper-adoption slice.
- Exclude character lifecycle behavior, Realm membership behavior, packets,
  persistence, commands, and server authority.

Phase 4, Slice 10: Character Creation and Realm Assignment helper adoption was
completed.

- Character Creation now uses `ElarionCivicUi.attachedShell` and shared header
  ornaments.
- Character Creation name and biography input surfaces now use civic
  thin/message body helpers.
- Character Creation submit and cooldown buttons now use
  `ElarionCivicUi.compactActionButton`.
- Realm Assignment now uses `ElarionCivicUi.attachedShell` and shared header
  ornaments.
- Realm Assignment option rows now use `ElarionCivicUi.rowSurface` with the
  assigned row selected and future choices disabled.
- Realm Assignment Continue now uses `ElarionCivicUi.compactActionButton`.
- Click targets, input focus behavior, submit/close behavior, payload sends,
  lifecycle state, Realm assignment state, persistence, commands, and server
  authority were not changed.
- Focused verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.network.CharacterCreationPayloadTest --tests panetina.elarion.core.service.CharacterLifecycleServiceTest`.
- Full Core verification passed: `.\gradlew.bat :platform:core:test`.
- Live Character Creation, Realm Assignment, Collection, Admin Panel, and
  Notification drawer screenshot QA remain pending.

Next recommended slice requiring approval:

- If live QA becomes available: run live QA for Notification drawer, Admin
  Panel, Collection, Character Creation, and Realm Assignment and apply only
  small visual corrections.
- If live QA remains unavailable: continue Phase 4 helper adoption with a small
  addon screen pair such as Portal Confirmation and Grave Recovery shell
  surfaces.
- Classification: MEDIUM for another helper-adoption slice.
- Exclude portal travel behavior, grave recovery behavior, packets,
  persistence, commands, inventory mutation, and server authority.

Phase 4, Slice 11: Portal Confirmation and Grave Recovery helper adoption was
completed.

- Portal Confirmation now uses `ElarionCivicUi.attachedShell`, shared header
  ornaments, a civic message body, a civic gate icon frame, and civic action
  buttons.
- Portal Confirmation still sends only the existing
  `PortalTravelConfirmPayload` when the server-authored prompt allows travel.
- Grave Recovery now uses `ElarionCivicUi.attachedShell`, shared header
  ornaments, `statusChip`, civic item-slot frames, and civic action buttons.
- Grave Recovery still decodes/display items client-side only for rendering and
  sends only the existing `GraveRecoverPayload` for server-side recovery.
- Portal and Grave click targets, packet payloads, travel/recovery validation,
  persistence, inventory mutation, commands, and server authority were not
  changed.
- Focused verification passed:
  `.\gradlew.bat :addons:portals:test --tests panetina.elarion.addons.portals.network.PortalRouteStatusSyncPayloadTest :addons:underworld:test --tests panetina.elarion.addons.underworld.network.GraveOpenPayloadTest`.
- Touched addon verification passed:
  `.\gradlew.bat :addons:portals:test :addons:underworld:test`.
- Live Portal Confirmation, Grave Recovery, Character Creation, Realm
  Assignment, Collection, Admin Panel, and Notification drawer screenshot QA
  remain pending.

Next recommended slice requiring approval:

- If live QA becomes available: run live QA for the migrated Core/addon screens
  and apply only small visual corrections.
- If live QA remains unavailable: continue Phase 4 helper adoption with another
  bounded addon screen target, likely NPC Dialogue or Shrine UI shell/action
  surfaces after a short proposal/audit of their local drawing complexity.
- Classification: MEDIUM for another helper-adoption slice.
- Exclude NPC dialogue condition/action evaluation, Offering donation or
  milestone behavior, packets, persistence, commands, inventory mutation, and
  server authority.

Phase 4, Slice 12: NPC Dialogue and Shrine UI helper-adoption proposal was
completed.

- Inspected `NpcDialogueScreen` and `ShrineOfFoundationScreen`.
- NPC Dialogue already uses Core primitives for scaled screens, typography,
  dialogue boxes, portrait frames, cards, virtual lists, scrollbars, and
  numeric prompts, but still has direct shell/header/options/footer/prompt
  drawing that can safely adopt civic helpers.
- Shrine UI has broader local drawing density across project summary,
  progress, tabs, requirement rows, history rows, numeric prompt, rewards,
  reward slots, tooltips, icons, scrollbars, and contribution messages.
- Decision: migrate NPC Dialogue first. Defer Shrine helper adoption until
  after NPC or until live QA identifies a higher-priority Shrine visual issue.
- No production code was changed.

Next recommended slice requiring approval:

- Phase 4, Slice 13: NPC Dialogue shell/options/footer/prompt helper adoption.
- Classification: MEDIUM.
- Change only `NpcDialogueScreen` and relevant docs.
- Keep NPC portrait rendering, dialogue boxes, relation hearts, currency badge,
  cards, typing phases, sounds, option selection state, virtual-list scroll
  behavior, prompt validation, packet sends, condition/action evaluation,
  persistence, commands, and server authority unchanged.
- Exclude Shrine UI from this implementation slice.

Phase 4, Slice 13: NPC Dialogue helper adoption was completed.

- NPC Dialogue root now uses `ElarionCivicUi.attachedShell`.
- Dialogue option rows now use `ElarionCivicUi.compactActionButton` with muted,
  normal, or primary tone based on existing input/selection state.
- Footer Close now uses `ElarionCivicUi.compactActionButton`.
- Numeric prompt overlay now uses `ElarionCivicUi.headerShell`.
- Numeric prompt input now uses `ElarionCivicUi.thinBox`.
- NPC portrait rendering, dialogue boxes, relation hearts, currency badge,
  cards, typing phases, sounds, option selection state, virtual-list scroll
  behavior, prompt validation, packet sends, condition/action evaluation,
  persistence, commands, and server authority were not changed.
- Shrine UI was not changed.
- Focused verification passed:
  `.\gradlew.bat :addons:npcs:test --tests panetina.elarion.addons.npcs.client.ui.ElarionConversationControllerTest --tests panetina.elarion.addons.npcs.network.NpcDialoguePromptSubmitPayloadTest`.
- Full NPC addon verification passed: `.\gradlew.bat :addons:npcs:test`.
- Live NPC Dialogue and migrated-screen screenshot QA remain pending.

Next recommended slice requiring approval:

- If live QA becomes available: run live QA for migrated Core/addon screens and
  apply only small visual corrections.
- If live QA remains unavailable: continue Phase 4 helper adoption with Shrine
  UI shell/header/close/numeric prompt surfaces only.
- Classification: MEDIUM for another helper-adoption slice.
- Exclude Offering donation behavior, milestone behavior, reward rendering
  semantics, row icon semantics, packets, persistence, commands, inventory
  mutation, and server authority.

Phase 4, Slice 14: live screenshot QA capture path was completed.

- Added `dev/tools/capture-minecraft-window.ps1`, a Windows helper that captures
  the Minecraft window by title using `PrintWindow` by default.
- Verified `.\gradlew.bat runClientOne` launches the full dev client as
  `ElarionAdmin` and reaches a `Minecraft* 1.21.1` window.
- Verified raw screen capture can be polluted by overlapping desktop windows,
  while `PrintWindow` produced a clean Minecraft main-menu PNG at
  `build/ui-qa/script-verified-main-menu.png`.
- Direct script execution is blocked by local PowerShell policy on this
  machine; use `powershell -NoProfile -ExecutionPolicy Bypass -File
  .\dev\tools\capture-minecraft-window.ps1`.
- Updated `CODEX.md`, `docs/systems/GUI.md`, `docs/systems/UI_JOURNAL.md`,
  `docs/test-commands.md`, `docs/reports/UI_SYSTEM_AUDIT.md`, `INDEX.md`,
  `TODO.md`, and `docs/ai/CURRENT_STATUS.md` with the live capture workflow.
- No production Java, UI rendering code, packets, config, persistence,
  commands, or server-authoritative behavior were changed.
- This slice establishes capture only. Opening specific Elarion UI states is
  still manual/dev-world setup until a future dev-only UI gallery or test hooks
  exist.

Phase 3/4 live Admin Panel payload hardening was completed on 2026-07-06.

- Fixed `/e panel` custom payload disconnects by serializing rows only for the
  selected tab and by scoping Config rows: Config opens with domain/category
  summaries, and category clicks request only that category's entry rows.
- Added Admin Panel payload caps for tabs, rows, row actions, and action input
  suggestions before packet write.
- Added server-authored action suggestions and client Tab completion for
  single-field Admin Panel actions.
- Added Core `Set Realm` player action through server-authoritative
  `RealmService.assign`; Core player actions now suggest Realm IDs, title IDs,
  and registered abilities.
- Updated Mount admin player actions to suggest registered mount IDs.
- Added `dev/tools/minecraft-qa.ps1` for faster live QA command sending,
  client-area clicks, focus/maximize, and screenshots.
- Focused verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest --tests panetina.elarion.core.network.AdminPanelPayloadTest --tests panetina.elarion.core.client.ElarionAdminPanelScreenLayoutTest`
  and `.\gradlew.bat :addons:mounts:compileJava`.
- Live QA passed with patched `runServer`/`runClientOne`: `/e panel` opened,
  Config tab clicked, `Core: UI Theme` category loaded, Players tab opened, and
  Set Realm modal Tab-completed a Realm id with no custom-payload disconnect.
  Evidence: `build/ui-qa/admin-config-*-fixed.png`,
  `build/ui-qa/admin-players-set-realm-fixed.png`, and
  `build/ui-qa/admin-set-realm-tab-complete.png`.

Completed follow-up recommendation:

- Continue Phase 3 Admin config edit verification: open
  `core:ui_theme:defaults.font-scale-percent`, validate/apply a safe value,
  confirm UI theme sync/reflow, and confirm non-applier config entries keep
  Apply disabled.
- Also verify provider suggestions in a player action such as Mount grant after
  scrolling the player action list.
- Classification: SMALL to MEDIUM.
- Exclude new config appliers, broad Admin Panel redesign, persistence
  migrations, and unrelated UI polish.

Live server QA workflow addendum:

- For real UI state checks, start `.\gradlew.bat runServer`, then
  `.\gradlew.bat runClientOne`, then join Multiplayer using the saved
  `localhost` server entry.
- Use server-authoritative commands/interactions to open target screens:
  `/e panel`, `/charactermenu`, HUD notification rail, linked Shrine block,
  Civic Forum/Seat of Rule blocks, placed NPC interaction, configured portal
  route interaction, and grave/tomb interaction.
- Some screens intentionally do not have direct open commands because their
  action packets require server-issued block/entity/session context.

Deferred combined QA recommendation:

- Run a focused populated-Notification QA pass. Seed Realm notifications
  through existing server-owned events and verify rows/actions/detail.
- The Admin config edit portion of this earlier combined recommendation is now
  completed and verified below.
- Classification: MEDIUM.
- Expected outputs: local PNGs under `build/ui-qa/`, documented findings, and
  only small visual corrections if clearly necessary.
- Exclude packet schemas, notification storage/filtering/action semantics,
  config mutation semantics, persistence, commands, and server authority.

Admin config edit live verification was completed on 2026-07-06.

- Fixed the first live Apply failure for stale development `ui_theme.yml`
  files that predated `defaults.font-scale-percent`; the applier now inserts
  the missing scalar under the existing `defaults` block, while duplicate
  scalar lines still reject without mutation.
- Added mouse-wheel scroll support to `dev/tools/minecraft-qa.ps1` so long
  Admin Panel action lists can be tested repeatably.
- Live patched `runServer`/`runClientOne` QA passed:
  - Config tab opens without custom-payload disconnect.
  - `Core: UI Theme` category loads scoped entry rows.
  - `core:ui_theme:defaults.font-scale-percent` validates and applies `125`,
    reflows the Admin Panel with larger text, then applies back to `100`.
  - Non-applier row `defaults.logical-width` reports no registered applier and
    Apply remains non-mutating.
  - Players action list scrolls to Mount actions; `Grant Mount` modal
    Tab-completes `airship` from server-authored Mount suggestions.
- Live evidence:
  `build/ui-qa/font-apply-fixed-applied-125.png`,
  `build/ui-qa/font-apply-restored-100.png`,
  `build/ui-qa/config-non-applier-apply-disabled.png`,
  `build/ui-qa/admin-players-mount-actions.png`, and
  `build/ui-qa/admin-grant-mount-tab-complete.png`.
- Focused verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.CoreUiThemeFontScaleConfigApplierTest --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest --tests panetina.elarion.core.network.AdminPanelPayloadTest --tests panetina.elarion.core.client.ElarionAdminPanelScreenLayoutTest :addons:mounts:compileJava`.
- Checked live logs: no custom-payload decoder/encoder/disconnect errors were
  found; remaining logged errors were unrelated invalid item model rotations.

Next recommended slice:

- Phase 4, Slice 16: populated Notification drawer live QA and bounded visual
  corrections.
- Seed Realm/Personal notifications through existing server-owned events,
  capture compact rows, unread state, selected state, action footer, detail
  view, reward tooltips, and empty-category behavior.
- Classification: MEDIUM.
- Exclude notification packet/storage/filter/action semantic changes unless a
  live defect proves one is required.

## Phase 4, Slice 16 - Populated Notification Live QA

Status: completed on 2026-07-06.

- Seeded a claimable `council_hall_blessing` reward through the existing
  server-authoritative Realm reward command and used existing Realm mail for
  mixed read/unread rows.
- Captured populated Personal and Realm lists, selected/unread rows, action
  footers, Realm detail, reward detail, Sharpness III reward tooltip, Quest
  empty state, and World empty state under `build/ui-qa/`.
- `View` and `Go To` now use the neutral action tone; Claim, Accept, and
  Approve retain the green primary tone.
- Short detail drawers now fit their content while long details retain the
  existing maximum scrolling viewport.
- Empty lower categories no longer extend the drawer solely to reach a rail
  pointer; the selected slot stays visible and its pointer is hidden when it
  would land below the bounded drawer.
- The QA helper now preserves maximized windows and supports native-cursor
  movement so hover/tooltips can be captured reliably.
- Notification packets, storage contracts, category filters, and server
  action semantics were not changed.
- Notification, reward-grant, and July history QA state was restored from the
  pre-seed backup after the live pass.
- Verification passed: `.\gradlew.bat :platform:core:test`.

Next recommended slice:

- Phase 4, Slice 17: Collection live screenshot QA and bounded visual
  corrections.
- Open `/charactermenu`, verify Mounts/Pets/Titles tabs, selected/active rows,
  icon frames, live mount preview, hidden scrolling, action buttons, and title
  activation through the authoritative server flow.
- Classification: MEDIUM.
- Exclude Collection packet, persistence, unlock, and action semantics unless
  a live defect requires a bounded correction.

## Approved Option A Design Reference Slice - 2026-07-07

Status: completed; reference art and planning only.

- The user approved the first generated Civic Ledger direction for custom
  screens that have not received a complete reference-aligned redesign.
- Added 14 indexed boards under `docs/ui/revamp-option-a/`, including Profile,
  Unlockables, character creation, balanced Realm placement, Shrine,
  quest/banker/trader NPC surfaces, trade, three Portal pop-ups, Grave
  Recovery, Admin Panel, and generic event feedback.
- Confirmed Portals do not receive a full menu; Grave Recovery remains a
  separate menu.
- Confirmed character biography/identity precedes server-enforced balanced
  Realm placement.
- Confirmed the future player hub aggregates visibility-safe addon projections
  while addons retain canonical state ownership.
- At the time of this reference-only slice, no command, packet, class, or
  player-facing rename was made. Slice 17D later made `Character Menu` the
  player-facing shell label and `/charactermenu` alias while preserving Collection
  internals.
- No Java, resource, config, networking, persistence, or runtime behavior was
  changed.

Correction recorded after visual review:

- Replaced the Shrine board with a layout-preserving art/UI revamp based on
  the live Shrine screen. No project browser, filters, rankings, milestone
  timeline, or three-column information redesign is planned.
- Replaced the quest NPC board with a compact conversation window: NPC
  identity, current line, at most three choices, and a small optional quest
  summary.
- Replaced the banker board with a compact Deposit/Withdraw interaction:
  balances, amount controls, fee/total, confirmation, and three recent
  transactions. Administrative/account/security dashboards are excluded.

Next recommended slice:

- Phase 4, Slice 17A: audit the current Collection shell and future player-hub
  boundary before implementation.
- Map command/keybind/payload/class naming impact, provider/action contracts,
  profile data owners, visibility rules, and bounded query/index needs.
- Produce a migration proposal that preserves existing unlock actions and
  addon ownership.
- Classification: MEDIUM, documentation-only.

## Phase 4, Slice 17A - Collection And Character Menu Audit

Status: completed on 2026-07-07; documentation only.

- Report: `docs/reports/CITIZEN_LEDGER_AUDIT.md`.
- Verified that Core Collection already has the correct Unlockables authority
  split: Core shell/generic packets, Core Titles provider, Mount-owned mount
  provider/state/actions, and server-authoritative action dispatch.
- Selected `Character Menu` as the planned user-facing shell name while keeping
  Collection Java/API names, packet IDs, mount config/runtime filenames, and
  resource paths stable initially. Slice 17D implemented the visible label and
  `/charactermenu` alias.
- Planned `/charactermenu` as the preferred command with `/charactermenu` retained as a
  compatibility alias; Slice 17D implemented this without changing Collection
  internals.
- Determined that Profile must use a separate read-only contributor contract,
  conservative server-side visibility, lazy bounded requests, and owner-owned
  summary APIs. It must not reuse action-oriented Collection entries.
- Verified that Core recent history already has a bounded `LEDGER` public
  history consumer over archives/monthly indexes.
- Confirmed that completed quests, Offering score, NPC reputation, and lifetime
  deaths are not ready profile projections and must not be derived through
  storage scans or placeholder semantics.
- Found a packet-safety prerequisite: Collection decoding bounds provider list
  counts, while encoding currently writes unbounded list sizes.
- No Java, packet, config, command, persistence, or runtime behavior changed.

Next recommended slice:

- Phase 4, Slice 17B: Collection snapshot wire-boundary hardening.
- Match encoder/decoder count limits, safely handle oversized strings/IDs,
  preserve valid selection, and add focused service/payload tests.
- Exclude Character Menu naming, Profile contracts, screen redesign, addon
  storage, and unlock/action behavior.
- Classification: SMALL.

## Phase 4, Slice 17B - Collection Wire-Boundary Hardening

Status: completed on 2026-07-07.

- Added a wire-safe Collection snapshot projection in
  `CollectionOpenPayload`.
- Matched outbound and inbound limits at 32 tabs, 512 entries per tab, and 16
  actions per entry.
- Filtered blank, whitespace-mutated, unsafe-control, oversized, and duplicate
  actionable IDs before transmission.
- Preserved valid display text through the existing bounded string codec.
- Repaired selected-tab state when validation or list limits omit the requested
  tab.
- Kept Collection APIs, packet IDs, provider dispatch, unlock/action behavior,
  storage, config, commands, and UI layout unchanged.
- Focused Collection network/service tests passed.
- Full `:platform:core:test` passed.

Next recommended slice:

- Phase 4, Slice 17C: capture a live Collection regression baseline before
  Character Menu shell/naming work.
- Verify Mounts/Pets/Titles tabs, selected/active/locked rows, hidden scrolling,
  live mount preview, title activation, and authoritative action refresh.
- Classification: MEDIUM.

## Phase 4, Slice 17C - Collection Live Regression Baseline

Status: completed on 2026-07-07; live QA and documentation only.

- Ran `runServer` and `runClientOne`, joined the saved `localhost`
  multiplayer server as `ElarionAdmin`, and opened `/charactermenu`.
- Captured live screenshots under `build/ui-qa/slice-17c/`:
  `11-collection-mounts.png`, `12-collection-mount-active.png`,
  `13-collection-mount-scroll.png`, `14-collection-pets.png`, and
  `15-collection-titles.png`.
- Verified the current baseline: Mounts/Pets/Titles tabs render, Airship model
  preview renders, Set Active refresh works, hidden mount scrolling works,
  empty Pets state renders, and the Titles tab selection/detail path renders.
- Client/server logs showed no Elarion Collection/custom-payload
  encode/decode crash during the QA flow. The only matched client errors were
  unrelated resource-pack model rotation errors.
- QA changed the runtime test player's active mount to `airship` in
  `dev/run/world/elarion/addon-state/mounts/collection.json`. No pre-click
  runtime-state backup existed, so this side effect is documented rather than
  guessed back to an unknown prior value.
- Discovered a Windows live-QA issue: the main menu can appear as a tiny/white
  framebuffer after startup until the Minecraft window is toggled with `F11`
  or otherwise forced to recreate/maximize the framebuffer. This was not caused
  by shaders; Iris state was restored after the temporary check.
- No production Java, resources, packet identifiers, config files, persistence
  schemas, commands, or gameplay behavior changed in this slice.

Next recommended slice:

- Phase 4, Slice 17D: introduce the `Character Menu` user-facing shell label
  and `/charactermenu` alias while preserving Collection internals, packet IDs,
  provider/action contracts, runtime storage, and `/charactermenu` compatibility.
- Include focused command/keybind label tests/docs if the existing test surface
  supports them.
- Exclude profile aggregation, shell redesign, art asset promotion, and addon
  state changes.
- Classification: SMALL.

## Phase 4, Slice 17D - Character Menu Label And Alias

Status: completed on 2026-07-07.

- `ElarionCollectionService` now sends `Character Menu` as the snapshot title
  while keeping the truthful current subtitle `Mounts, pets, and titles.`
- `ElarionCollectionScreen` uses `Character Menu` as its client screen title.
- Added `/charactermenu` as the preferred command path to open the existing
  server-authoritative Collection snapshot.
- Kept `/charactermenu` as a compatibility alias.
- Updated command help and the keybind translation to `Open Character Menu`.
- Preserved Collection internals: Java/API names, packet identifiers, provider
  contracts, action semantics, config/runtime filenames, resource paths,
  storage, and unlock state.
- No Profile aggregation, shell redesign, art asset promotion, or addon state
  change was made.
- Focused Collection service/network/screen-layout tests passed.

Next recommended slice:

- Phase 4, Slice 17E: define the Core profile aggregation and presentation
  boundary with conservative server-side visibility.
- Start with Core-only profile records and tests; do not add addon
  contributors, visual shell redesign, or raw storage scans yet.
- Classification: MEDIUM.

## Phase 4, Slice 17E - Core Profile Boundary

Status: completed on 2026-07-07.

- Added Core profile model records:
  `CitizenProfileRequestContext`, `CitizenProfileSnapshot`,
  `CitizenProfileSection`, `CitizenProfileField`, `CitizenProfileCard`,
  `CitizenProfileContributor`, and `ProfileVisibility`.
- Added `CitizenProfileService`, exposed through `ElarionApi.profiles()` and
  `ElarionApi.system().profiles()`.
- The service builds Core-only profile sections from canonical Core state:
  identity, Realm, and active title.
- Visibility is server-side and conservative: `PUBLIC`, `SELF`, and `ADMIN`.
- Aggregation is bounded to 16 sections, 24 fields per section, and 8 cards
  per section.
- Contributor registration is explicit and rejects duplicate contributor IDs,
  but no addon contributors are registered yet.
- `snapshot(context)` resolves one target citizen by UUID through
  `CitizenService.find`; it does not scan all citizens or any addon storage.
- No Collection packets/UI/commands, profile packets, Profile tab rendering,
  persistence, config, art assets, or addon state changed.
- Focused profile service tests passed.

Next recommended slice:

- Phase 4, Slice 17F: add a bounded lazy profile transport contract for
  Character Menu Profile requests/snapshots, starting with packet records and
  round-trip/bounds tests only.
- Exclude Profile tab rendering, addon contributors, raw storage scans, and
  screen redesign.
- Classification: MEDIUM.

## Phase 4, Slice 17F - Bounded Profile Transport Records

Status: completed on 2026-07-07.

- Added unregistered profile payload records/codecs:
  `CitizenProfileRequestPayload` and `CitizenProfileSnapshotPayload`.
- Request payload carries target citizen UUID plus optional section id.
- Snapshot payload carries one `CitizenProfileSnapshot`.
- Snapshot encoding enforces the same caps as `CitizenProfileService`:
  16 sections, 24 fields per section, and 8 cards per section.
- All profile packet strings use explicit maximum lengths and the shared
  string codec.
- No Fabric payload registration, server receiver, client receiver/cache,
  Profile tab UI, addon contributor, Collection behavior, persistence, config,
  command, or art asset changed.
- Focused profile payload/service tests passed.

Superseded next slice:

- Phase 4, Slice 17G: register the profile payload types and add a narrow
  server-authoritative request/response dispatch proposal, or implement type
  registration only if keeping slices smaller.
- The receiver must derive the viewer from the connection, authorize target
  visibility server-side, build through `CitizenProfileService`, and keep the
  client read-only.
- Exclude Profile tab rendering, addon contributors, raw storage scans, and
  screen redesign.
- Classification: SMALL to MEDIUM depending on whether receiver dispatch is
  included. Completed by Slice 17G.

## Phase 4, Slice 17G - Profile Transport Registration And Cache

Status: completed on 2026-07-07.

- Registered `CitizenProfileRequestPayload` as C2S and
  `CitizenProfileSnapshotPayload` as S2C in Core.
- Added a server receiver that derives the viewer from the network connection,
  resolves zero target UUID to the connected player, builds through
  `CitizenProfileService`, optionally narrows the response to one requested
  visible section, and sends a bounded `CitizenProfileSnapshotPayload`.
- Added `CitizenProfileClientState` and a client receiver that caches the
  latest server-authored profile snapshot, clearing it on join/disconnect.
- No Profile tab UI, addon contributor, raw storage scan, persistence, config,
  command, art asset, or Collection action behavior changed.
- Focused profile service/payload/client-state tests passed.

Next recommended slice:

- Phase 4, Slice 17H: add a Core-only Profile tab surface to Character Menu
  that sends `CitizenProfileRequestPayload`, reads `CitizenProfileClientState`,
  and renders identity, Realm, and active-title sections only.
- Exclude addon profile contributors, completed quests, Offering scores, NPC
  reputation, lifetime deaths, broad shell redesign, and art-asset promotion.
- Classification: MEDIUM.

## Phase 4, Slice 17H - Core Profile Tab Surface

Status: completed on 2026-07-07.

- Added a fixed Core `Profile` tab to the Character Menu snapshot while
  preserving the existing default `Mounts` selection for `/charactermenu` opens.
- Updated the Character Menu subtitle to `Profile, mounts, pets, and titles.`
- The Profile tab sends `CitizenProfileRequestPayload` on selection and renders
  the latest server-authored `CitizenProfileClientState` snapshot.
- Profile content is read-only and limited to visible Core sections from
  `CitizenProfileService`: identity, Realm, and active title.
- The first implementation used section rows and a detail panel; live review
  found that too menu-like and visually weak.
- Corrected the Profile tab to render one composed civic profile sheet with a
  name/identity strip plus Realm, office/title, and record panels.
- No addon profile contributors, completed quest projection, Offering score,
  NPC reputation, lifetime death total, persistence, config, command, packet
  schema, art asset, or broad shell redesign changed.
- Focused Collection service/screen-layout/profile-client-state tests passed.

Next recommended slice:

## Phase 4, Slice 17I - Corrected Profile Sheet Live QA

Status: completed on 2026-07-07.

- Restarted the dev client, joined the saved localhost multiplayer server, and
  opened `/charactermenu`.
- Captured the existing Mounts default at
  `build/ui-qa/slice-17j-profile-sheet/10-ledger-default.png`.
- Captured the corrected Profile sheet at
  `build/ui-qa/slice-17j-profile-sheet/11-ledger-profile-sheet.png`.
- Verified the Profile request/snapshot path did not trigger a custom payload
  crash in the live client.
- The corrected Profile tab now presents a single civic record sheet instead
  of the rejected nested section-button/detail-panel layout.
- No production code changed in this live-QA closeout slice.

Next recommended slice:

- Phase 4, Slice 17J: start the broader Option A Character Menu shell/art pass
  as a bounded proposal, using the captured live Profile sheet and the approved
  Option A references. Keep addon profile contributors, completed quest
  projection, Offering score, NPC reputation, lifetime death totals, and
  persistence out of that slice unless explicitly approved.
- Classification: MEDIUM.

## Phase 4, Slice 17J - Citizen Profile Dossier

Status: completed on 2026-07-07.

- Expanded the bounded Character Menu shell and rebuilt Profile against the
  approved Option A hierarchy.
- Added the live player head, identity/Realm/title banner, and six compact
  dossier panels.
- Added only cheap Core-owned summary facts: citizenship, civic standing,
  unlocked-title count, granted-ability count, and the existing bounded
  Collection mount count.
- Reserved stable source/field slots for future addon-owned Offerings, Quests,
  NPC reputation, Groups, Government office history, Underworld deaths,
  Portal journeys, progression milestones, and Chronicle summary data.
- Missing addon data is labeled as unrecorded; no duplicate state, storage
  scan, persistence change, or client mutation was introduced.
- Full Core tests passed. Live multiplayer QA passed at
  `build/ui-qa/slice-17j-profile-redesign/18-profile-final.png`.

Next recommended slice:

- Phase 4, Slice 17K: migrate Mounts, Titles, and Pets presentation into the
  same Character Menu visual system while preserving all current provider and
  action behavior. Classification: MEDIUM.

## Phase 4, Slice 17K - Character Menu Unlockables

Status: completed on 2026-07-07.

- Rebalanced the Ledger into a compact six-row collection list and large
  showcase panel based on the approved Option A reference.
- Added icon tabs, completion totals, active/owned/locked labels, designed
  empty states, larger previews, and bounded record/action areas.
- Fixed repeated icon tiling by matrix-scaling complete 16x16 source textures.
- Preserved providers, packets, action IDs, hidden scrolling, preview registry,
  and server authority.
- Full Core tests passed. Live Mounts/Pets/Titles QA passed, including clean
  title activation/refresh and restoration of Monarch as active.

Next recommended slice:

- Resume the approved project-wide UI migration with one bounded screen family
  selected from Character Creation/Realm Assignment, Shrine, NPC Dialogue,
  Portal pop-ups, Grave Recovery, or Admin follow-up. Generated asset promotion
  remains a separate resource slice. Classification: MEDIUM.

## Phase 4, Slice 17L - Ledger Rank, Title Color, And Profile Metadata

Status: completed on 2026-07-07.

- Added config-backed Core title colors with `#RRGGBB` validation,
  read-only descriptors, default colors for built-in titles, and migration for
  missing known title colors without overwriting custom colors.
- Extended Collection entry transport with optional accent/rank metadata and
  rendered small rank badges plus accent-colored selected/detail frames.
- Titles now render configured colors in identity text and Character Menu title
  rows/previews; unlocked title preview shows the live player model, username,
  and selected title.
- Mounts now own Common/Uncommon/Legendary Collection rank metadata:
  Airship/Hot Air Balloon/Ghast are Common Realm baseline mounts, Bee/Chinese
  Dragon/Wyvern are Uncommon, and Sci-Fi Bike is Legendary for the future
  full-advancement route.
- Profile now uses the exact advancement count synchronized from vanilla
  advancement progress and can show a Government-contributed active office role
  instead of generic Citizen when available.
- Deferred player-name double-click profile links and generic reward hooks for
  mount/pet unlocks to separate slices.

Verification:

- Passed `.\gradlew.bat :platform:core:test`.
- Passed `.\gradlew.bat :addons:mounts:test`.
- Passed focused Collection packet/config tests.
- `.\gradlew.bat :addons:government:test --tests panetina.elarion.addons.government.service.GovernmentProfileContributorTest`
  was blocked by existing `groups`/`offerings` compile failures pulled in as
  Government dependencies; the new Government contributor source compiled
  during the focused Core/Mounts verification path was not separately proven by
  that task.

Next recommended slice:

- Resume the approved project-wide UI migration with Character Creation plus
  balanced Realm Assignment as the next screen family, or run live Citizen
  Ledger screenshot QA first if you want visual proof of title colors/rank
  badges before moving on. Classification: MEDIUM.

## Phase 4, Slice 17M - Ledger Rank Palette And Live QA

Status: completed on 2026-07-07.

- Ran live `/charactermenu` screenshot QA against the dev server. Evidence is under
  `build/ui-qa/slice-17m-ledger-rank-qa/`.
- Verified pre-patch Mounts, Profile, and Titles views opened without custom
  payload crashes and showed rank badges, title colors, active office role,
  player head/profile dossier, live title preview, and centered mount preview.
- Added Core `ElarionCollectionRank` so Common, Uncommon, Rare, Epic,
  Legendary, Sovereign, Heir, Council, Synod, Officer, and Trusted colors are
  project-wide instead of duplicated by individual providers.
- Moved Mounts and Titles rank badge colors to the shared Core palette.
- Tightened Profile advancement count to visible completed Minecraft
  advancements only, excluding hidden/internal advancement records.
- Removed duplicate active-title/civic-role text in the Profile header when
  both resolve to the same title.

Verification:

- Passed
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.model.ElarionCollectionRankTest --tests panetina.elarion.core.network.CollectionPayloadTest`.
- Passed
  `.\gradlew.bat :addons:mounts:test --tests panetina.elarion.addons.mounts.entity.ElarionMountTypeTest`.
- Passed `.\gradlew.bat :platform:core:test :addons:mounts:test`.
- Final post-patch live screenshot was blocked by the client returning
  `Invalid Session` while reconnecting to the test server; QA server/client
  processes were stopped afterward.

Next recommended slice:

- Resume the approved project-wide UI migration with Character Creation plus
  balanced Realm Assignment as the next screen family. Keep art-bank
  generation, player-name double-click profile links, and generic mount/pet
  reward hooks separate unless explicitly selected. Classification: MEDIUM.

## Phase 4, Slice 17N - Character Creation And Realm Placement UI

Status: completed on 2026-07-07.

- Migrated `CharacterCreationScreen` to the approved Option A civic onboarding
  layout while preserving the same mandatory flow and submit payload.
- Added a step strip, identity preview, larger bounded biography panel,
  biography readiness row, and bottom action band.
- Migrated `CharacterRealmAssignmentScreen` to a wider civic placement sheet
  with assigned Realm summary, up to three vertical Realm rows,
  assigned-row highlight, population metadata, and a bottom confirmation band.
- Preserved server-authoritative Realm assignment, payload shape, lifecycle
  storage, validation, cooldown behavior, and client request semantics.
- Added `CharacterOnboardingScreenLayoutTest` to lock key panel/input/row
  bounds.
- Follow-up live QA reached both migrated screens after using the saved
  localhost multiplayer server and the supported `/e realm remove` plus
  `/e test character reset` commands.
- Corrected `ElarionCivicUi.compactActionButton` label placement by one pixel
  upward so `Enter The Living World`, `Confirm Placement`, and other civic
  action labels sit visually centered inside beveled buttons.
- Centered both onboarding action-button bounds inside their footer bands using
  shared footer geometry constants. Layout tests now reject asymmetric top and
  bottom spacing greater than one logical pixel.

Verification:

- Passed
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.CharacterOnboardingScreenLayoutTest --tests panetina.elarion.core.network.CharacterCreationPayloadTest`.
- Passed `.\gradlew.bat :platform:core:test`.
- Live screenshot QA passed on 2026-07-07. Evidence is under
  `build/ui-qa/slice-17n-onboarding-live-qa-3/` and
  `build/ui-qa/slice-17n-onboarding-live-qa-5-footer-centered/`.
- Captured empty and filled Character Creation, Realm Assignment, and
  post-confirm gameplay screenshots. Both button bounds have balanced footer
  spacing, and the confirm button closes the placement panel through its
  matching client-area hitbox.
- QA note: `dev/tools/minecraft-qa.ps1` sends Minecraft client-area
  coordinates. Full-window screenshots include the Windows title bar, so visual
  y-coordinates from screenshots are about 30 px lower than the coordinates the
  helper needs.
- Character/citizen dev runtime state was backed up before QA mutations and
  restored afterward. No server/client QA process was left running.

Next recommended slice:

- Continue the approved Option A UI migration with the layout-preserving Shrine
  reskin as the next screen family, without changing Offering behavior,
  progress math, rewards, contribution authority, or Shrine information
  architecture. Classification: MEDIUM.

## Phase 4, Slice 17O - Shrine And Offerings Civic Reskin

Status: completed on 2026-07-07.

- Rebuilt `ShrineOfFoundationScreen` around the shared civic attached shell,
  ornamental header, bordered progress band, selected-tab treatment, compact
  requirement/history rows, framed project/reward summary, civic numeric modal,
  and centered footer action.
- Kept the approved Shrine information architecture: project summary and
  rewards remain on the left; Contribute and History remain on the right.
- Kept the server-authored payload, contribution request, range/world checks,
  progress calculations, reward delivery, history bounds, persistence, and
  keyboard/mouse behavior unchanged.
- Kept active project titles visible in the summary while using `SHRINE OF
  FOUNDATION` as the stable screen title, avoiding duplicate level labels.
- Added `ShrineOfFoundationScreenLayoutTest` for header/content/footer bounds at
  default and reduced configured dimensions.

Verification:

- Passed focused Shrine layout, packet, and submit tests.
- Passed `\.\gradlew.bat :addons:offerings:test`.
- Live screenshot QA passed against the linked Realm Shrine. Evidence is under
  `build/ui-qa/slice-17o-shrine-civic-reskin/` and covers completed/incomplete
  projects, six reward slots, enchanted item tooltip, selected Contribute and
  History tabs, empty history, and the contribution amount modal.
- The temporary `/e test shrine reset realm1` runtime mutation was restored from
  backup with zero hash differences. No QA server/client process remains.

Next recommended slice:

- Migrate the simple quest NPC dialogue surface to the approved Option A civic
  reference without changing dialogue graphs, condition evaluation, actions,
  relationship state, or server authority. Classification: MEDIUM.

## Phase 4, Slice 17P - Shared Runtime Icon Catalog And Art Pass

Status: completed on 2026-07-07.

- Added Core `ElarionUiIcons`, a semantic runtime icon catalog backed by the
  curated PNG library under `assets/elarion_core/textures/gui/library/`.
- Rewired Government UI glyph resolution, notifications, Character Menu/profile
  panels, Character Creation, Realm Assignment, Shrine, Portal confirmation,
  and Nether/End Portal HUD route slots to prefer semantic library icons.
- Preserved the notification rail read/unread icon behavior and native
  Minecraft item rendering/tooltips for explicit item rewards and costs.
- Free/neutral Portal confirmation prompts no longer draw the payment/item
  slot. Ticketed and fee prompts keep the framed slot and render ticket,
  currency, or gate art.
- Kept server payloads, persistence, action requests, reward delivery,
  contribution authority, dialogue/state ownership, and collection/provider
  contracts unchanged.

Verification:

- Verified all `ElarionUiIcons` mapped PNG paths exist.
- Passed `.\gradlew.bat :platform:core:compileJava`.
- Passed `.\gradlew.bat :platform:core:test`.
- Passed `.\gradlew.bat :addons:government:test`.
- Passed `.\gradlew.bat :addons:offerings:test`.
- Passed `.\gradlew.bat :addons:portals:test`.
- No live screenshot QA was run, by user request.

Resolved later:

- The Portal free/no-fee slot hiding text heuristic recorded here was replaced
  in Phase 4 Slice 30 by server-authored `PortalTravelPromptPayload.costKind`.

Next recommended slice:

- Let the user manually inspect the art pass, then continue Phase 4 with the
  simple quest NPC dialogue Option A migration. Preserve NPC-owned dialogue
  graphs, server-side condition/action evaluation, relationship state, packets,
  and existing interaction flow. Classification: MEDIUM.

## Phase 4, Slice 17Q - Civic UI Polish And Onboarding Confirm

Status: completed on 2026-07-08.

- Centered shared civic action-button text through `ElarionCivicUi` and moved
  older Government tab/action text to metric-based centering.
- Centered Shrine reward slots inside the bounded reward panel.
- Replaced generic profile/identity/people/civic/quest/world/Nether/End/project
  and Pets semantic icon mappings with non-portrait curated library art.
- Made Civic Forum and Seat of Rule header titles larger and lower.
- Changed Character Creation's primary action to `Continue`, sent Realm
  placement before closing creation, and added
  `CharacterRealmAssignmentConfirmPayload` so teleporting happens only after
  placement confirmation.
- Changed the Character Menu default selected tab to Profile and adjusted Mount
  preview calibration for Sci-Fi Bike.

Verification:

- Passed `.\gradlew.bat :addons:economy:jar`.
- Passed `.\gradlew.bat :platform:core:compileJava :platform:core:test --tests panetina.elarion.core.network.CharacterCreationPayloadTest --tests panetina.elarion.core.service.ElarionCollectionServiceTest`.
- Passed `.\gradlew.bat :addons:government:test --tests panetina.elarion.addons.government.client.GovernmentUiIconAssetTest`.
- Passed `.\gradlew.bat :addons:mounts:test --tests panetina.elarion.addons.mounts.client.ElarionMountCollectionPreviewRendererTest`.
- Passed `.\gradlew.bat :addons:offerings:compileJava`.
- No live screenshot QA was run by request.

Next recommended slice:

- Let the user manually inspect the polish pass, then continue Phase 4 with the
  simple quest NPC dialogue Option A migration. Preserve NPC-owned dialogue
  graphs, server-side condition/action evaluation, relationship state, packets,
  and existing interaction flow. Classification: MEDIUM.

## Phase 4, Slice 17R - Shrine Reward And Civic Baseline Follow-Up

Status: completed on 2026-07-08.

- Fixed the completed Shrine summary reward panel so six rewards reserve two
  visible rows instead of dropping the second row when the footer/status band is
  present.
- Lowered shared civic compact-control text by one pixel through
  `ElarionCivicUi.centeredTextY` and removed Shrine tab-local upward offsets.
- Lowered the Shrine header title by one pixel.
- Replaced the notification rail Quest and World glyphs with shared semantic
  `ElarionUiIcons` art and kept unread state as a small overlay marker.
- Reduced Civic Forum and Seat of Rule crest rendering to source-square 32 px
  inside the header and mapped the shared Civic crest to shield art.

Verification:

- Passed `.\gradlew.bat :platform:core:jar`.
- Passed `.\gradlew.bat :addons:government:test --tests panetina.elarion.addons.government.client.GovernmentUiIconAssetTest`.
- Passed `.\gradlew.bat :addons:offerings:test --tests panetina.elarion.addons.offerings.client.ShrineOfFoundationScreenLayoutTest`.
- A parallel Gradle verification attempt raced on the generated Core dev jar
  and was rerun sequentially; do not run dependent addon tests in parallel with
  Core jar generation on this workstation.
- No live screenshot QA was run by request.

Next recommended slice:

- Let the user manually inspect the latest Shrine/Government/notification icon
  polish, then continue Phase 4 with the simple quest NPC dialogue Option A
  migration. Preserve NPC-owned dialogue graphs, server-side condition/action
  evaluation, relationship state, packets, and existing interaction flow.
  Classification: MEDIUM.

## Phase 4, Slice 17S - Ledger Keybind, Hidden Aliases, Player Head Preview, Title Color

Status: completed on 2026-07-08.

- Character Creation identity preview now renders the current player's head
  instead of the generic profile icon.
- Core keeps the default `C` keybind as the Character Menu opener and
  registers client-side hidden `/charactermenu` aliases that send
  the existing `CollectionOpenRequestPayload`.
- Removed `/charactermenu` from the server command tree and Core
  command help so they no longer appear in slash recommendations or `/help`.
- Added active-title ARGB color to `IdentitySyncPayload` and the client identity
  cache.
- Title nameplate rendering now uses configured Core title colors, so Monarch
  and other active titles render above players with the same color used in the
  Ledger.

Verification:

- Passed `.\gradlew.bat :platform:core:compileJava :platform:core:test --tests panetina.elarion.core.network.IdentitySyncPayloadTest --tests panetina.elarion.core.client.ClientIdentityTest --tests panetina.elarion.core.service.ElarionCollectionServiceTest`.
- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ClientIdentityTest` after adding direct configured-title-color coverage.
- Passed `.\gradlew.bat :addons:titles:compileJava`.
- No live screenshot QA was run.

Next recommended slice:

- Manually verify Character Creation head preview, C key Ledger open for a new
  player, hidden slash recommendations for `/charactermenu`, and
  Monarch/title color above the player's head. Then continue Phase 4 with the
  simple quest NPC dialogue Option A migration. Classification: MEDIUM.

## Phase 4, Slice 17T - Citizen Color, Rank Fallback, And C-Key Conflict

Status: completed on 2026-07-08.

- Citizen's shipped title color is white-gray `#C9C9C9`; the old shipped gold
  migrates without overwriting custom administrator colors.
- Explicit title colors win. Missing colors use known rank-family colors,
  Legendary for unknown globally unique titles, and simple white otherwise.
- Core clears vanilla Save Hotbar Activator on the first safe client tick only
  when it still owns its default `C` binding, preserving user-rebound controls
  and avoiding entrypoint access to uninitialized client options.
- Focused Core config, descriptor, identity, and title-presentation tests pass.

## Phase 4, Slice 18 - Quest NPC Dialogue Option A Migration

Status: completed on 2026-07-08.

- Reworked the existing NPC dialogue renderer into the compact Option A
  hierarchy: larger title, real NPC portrait, one active conversation body,
  three visible choices, bounded overflow scrolling, header close control, and
  the existing metadata/card strip.
- Player and NPC typing phases share the conversation body. Existing sounds,
  prompt input, saved selection/scroll state, server-authored options, and
  C2S selection/dismiss packets remain unchanged.
- Did not add localized-text heuristics for option icons or quest metadata; the
  current payload has no semantic fields for those concepts.
- Focused NPC layout/controller/list tests and module compilation pass. Live
  screenshot QA remains deferred by request.

Next recommended slice:

- Audit and define the explicit server-authored presentation boundary required
  to separate compact banker dialogue from ordinary quest dialogue before
  implementing the banker Option A surface. Classification: SMALL audit.

## Phase 4, Slice 19 - Conversation-First NPC Services And Dedicated Bank

Status: completed on 2026-07-08.

- Added a server-authored NPC presentation boundary. Dialogue nodes now carry
  `dialogue`, `bank`, or reserved `trade` presentation kinds, and options carry
  stable `presentation-role` values such as `open_bank`, `deposit`, `withdraw`,
  and `back`.
- All NPCs remain conversation-first. Bankers open the normal compact NPC
  conversation first, then expose `Open Bank` to transition into a dedicated
  bank service screen inside the same validated session.
- Added a dedicated `NpcBankScreen` with portrait, deposited balance,
  Deposit/Withdraw modes, bounded amount input, preset buttons, fee/total
  summary, authoritative feedback, and Back to Conversation.
- Legacy root-level Economy deposit/withdraw banker prompt actions are
  projected into an in-memory `bank_service` node without rewriting admin
  dialogue files.
- Removed fake hard-coded `Neutral/0` relationship presentation. Aggregate
  NPC/Realm reputation belongs in Character Menu. NPC screens may show only the
  relationship with the currently interacted NPC after NPC-owned data exists.
- Trade presentation is reserved and currently rejected by validation until
  its dedicated screen contract is implemented.

Verification:

- Passed `.\gradlew.bat :addons:npcs:test :addons:economy:test`.
- Live screenshot QA passed with evidence under
  `build/ui-qa/slice-19-npc-bank/`:
  - `12-after-right-click.png`: normal NPC conversation opens first with
    `Open Bank`.
  - `13-bank-screen.png`: dedicated bank service screen opens.
  - `14-back-to-conversation.png`: Back to Conversation returns to dialogue.

Next recommended slice:

- Start the dedicated trade presentation contract against
  `docs/ui/revamp-option-a/09-npc-trade-screen-v2.png`, beginning with
  model/payload/config validation and a non-mutating screen shell.
  Classification: MEDIUM.

## Phase 4, Slice 20 - Dedicated Trade Presentation Shell

Status: completed on 2026-07-08.

- Enabled `presentation: trade` for NPC dialogue nodes and routed
  `presentationKind=trade` to a dedicated `NpcTradeScreen`.
- Added a compact Buy/Sell shell using the approved trade reference direction.
  It shows NPC identity, Buy/Sell modes, pending-catalog messaging, and Back to
  Conversation.
- Kept the shell non-mutating. `NpcConfigValidator` rejects prompts and
  executable actions on options inside `trade` nodes until stock, prices,
  inventory checks, and buy/sell mutations have an owner contract.
- Added focused validator coverage for accepted non-mutating trade nodes and
  rejected mutating trade options.
- Added descriptor coverage proving Admin config discovery reports `dialogue,
  trade` presentation kinds.

Verification:

- Passed `.\gradlew.bat :addons:npcs:compileJava :addons:npcs:compileTestJava`.
- Passed `.\gradlew.bat :addons:npcs:test`.

Next recommended slice:

- Audit/propose the real trade owner boundary before enabling buy/sell
  mutations: stock provider, price provider, inventory checks,
  server-authoritative request/result packets, persistence owner, and relation
  to Economy formatting. Classification: MEDIUM audit/proposal.

## Phase 4 Follow-Up - Default Trader Content

Status: completed on 2026-07-08.

Scope:

- Added a default `worldheart_trader` NPC definition and
  `worldheart_trader` dialogue.
- Updated the existing dev-run NPC config and added `worldheart_trader_1` to
  `dev/run/world/elarion/addon-state/npcs/placed-npcs.json`, two blocks to the
  visual left of `worldheart_banker_1`.
- Kept the trader non-mutating: normal NPC conversation first, then `Trade`
  opens the existing dedicated Buy/Sell shell.

Verification:

- Passed `.\gradlew.bat :addons:npcs:compileJava :addons:npcs:compileTestJava`.
- Passed `.\gradlew.bat :addons:npcs:test`.
- Live QA started the dev server and client. The server loaded the two NPC
  definitions, but the client join was blocked by Minecraft's `Invalid Session`
  screen before the local server received a login. Captures are under
  `build/ui-qa/slice-trader-placement/`.

Next recommended slice:

- Live QA `worldheart_trader_1` once the dev server/client are launched, then
  continue the MEDIUM trade-owner boundary audit before enabling real buy/sell
  behavior.

## Phase 4 Follow-Up - Banker And Trader Visual Polish

Status: completed on 2026-07-08.

Scope:

- Cleaned the default banker/trader service NPC visuals without changing
  dialogue authority, Economy mutation, trade mutation, or persistence.
- Added dedicated `worldheart_banker.png` and `worldheart_trader.png` 64x64
  texture skins with standard Minecraft UV spacing and transparent unused
  regions.
- Wired the banker portrait to
  `portrait_character_portrait_icons_03_icons_03.png` and the trader portrait
  to `portrait_character_portrait_icons_27_icons_27.png`.
- Added 32x32 library portrait handling to the NPC portrait renderer.
- Aligned the bank quick amount buttons with the amount field, added a
  blinking numeric caret, and used the shared Sigil currency icon in the bank
  balance/input/fee/total surfaces and trader price rows.
- Added display-only trader preview rows for two Nether Gate Tickets, one
  Cobblestone, a Protection IV chestplate, and a named/lore Protection IV
  chestplate.

Verification:

- Passed `.\gradlew.bat :platform:core:compileJava :addons:npcs:compileJava :addons:npcs:compileTestJava`.
- Passed `.\gradlew.bat :addons:npcs:test`.
- No live screenshot QA was run at the user's request.

Next recommended slice:

- Continue with the MEDIUM trade-owner boundary audit before enabling real
  buy/sell behavior. Define stock provider, price provider, inventory checks,
  server-authoritative request/result packets, persistence owner, and Economy
  formatter integration.

## Phase 4 Follow-Up - Banker And Trader Live QA

Status: completed on 2026-07-08.

- Full 32x32 portrait textures now sample once instead of tiling into a 2x2
  grid. Shared Sigil icons use explicit 16x16 source dimensions when scaled.
- The bank amount starts after the Sigil icon. Its blinking caret uses the same
  rendered text width and remains directly after the final digit, including
  preset values and backspace changes.
- Fee and Total each render one compact Sigil icon plus a right-aligned value.
- Trader rows retain the real `ItemStack` under the cursor and display native
  item tooltips after scaled rendering completes.
- Banker/trader standard 64x64 skins render correctly in the dev world.

Verification:

- Passed `.\gradlew.bat :platform:core:compileJava :addons:npcs:compileJava :addons:npcs:compileTestJava :addons:npcs:test`.
- Live evidence is under `build/ui-qa/npc-polish-live-2/`, including banker and
  trader dialogue, caret blink states, both armor tooltips, and world skins.

Next recommended slice:

- Continue with the MEDIUM trade-owner boundary audit before enabling real
  buy/sell behavior. Keep the current catalog non-mutating.

## Phase 4, Slice 21 - NPC Trade Owner Audit

Status: completed on 2026-07-08. Production code was not changed.

- Report: `docs/reports/NPC_TRADE_OWNER_AUDIT.md`.
- Verified that NPCs should own merchant catalogs, offer visibility, stock,
  trade sessions, and purchase orchestration; Economy remains the only currency
  authority; Core remains the reward/history/shared-UI owner.
- Confirmed the current client catalog is display-only and cannot become an
  authority source.
- Identified the crash-safety prerequisite: Economy needs bounded idempotent
  operation receipts before a cross-addon purchase journal can safely reconcile
  payment and delivery after restart.
- Split implementation into read-only catalogs, Economy payment receipts,
  unlimited buying, finite stock, and selling rather than one broad mutation.

Next recommended slice:

- Phase 4, Slice 22: server-authored read-only NPC trade catalogs. Add parsed
  definitions, validation, descriptors, bounded snapshot transport, and client
  rendering. Keep Buy/Sell mutations, Economy dependency, stock persistence,
  and purchase packets excluded. Classification: MEDIUM.

## Phase 4, Slice 22 - Server-Authored Read-Only NPC Trade Catalogs

Status: completed on 2026-07-08.

- Added NPC-owned parsed trade catalog definitions in
  `config/elarion/addons/npcs/trades.yml`.
- Added trade catalog/offer/enchantment models, default catalog content,
  loader parsing, validation, read-only descriptors, and descriptor/validator
  tests.
- Linked NPC definitions to catalog IDs through `trade-catalog`.
- Added bounded `NpcTradeSnapshotPayload` / `NpcTradeOfferPayload` transport.
- Updated the trade UI to render server-authored offers and native `ItemStack`
  tooltips instead of client-hardcoded preview rows.
- Added dimension ticket art: Nether ticket stacks use the crimson stele icon,
  End ticket stacks use the blue stele icon, and Portal confirmation prompts
  route ticket icons to the same assets. Follow-up moved item-model textures
  into the portals item texture namespace so inventory rendering does not rely
  on GUI-library paths.
- Added a compatibility bridge so older `worldheart_trader` definitions with a
  missing or blank `trade-catalog` use the generated `worldheart_trader`
  catalog in memory; custom traders still need explicit catalog IDs.
- Added `price-key` to NPC trade offers as future Economy/tax/inflation pricing
  metadata while the current read-only UI continues to display fixed `price`.
- Preserved the selected banker Deposit/Withdraw tab across server feedback
  refreshes.
- Kept all trade rows non-mutating. Payment, purchase packets, stock
  persistence, Economy dependency, and inventory mutation remain excluded.
- Also adjusted trade shell button text baseline and paired Sigil price icons
  directly with right-aligned prices.

Verification:

- Passed `.\gradlew.bat :platform:core:compileJava :addons:npcs:compileJava :addons:npcs:compileTestJava`.
- Passed `.\gradlew.bat :addons:npcs:test --tests panetina.elarion.addons.npcs.config.NpcConfigDescriptorsTest --tests panetina.elarion.addons.npcs.config.NpcConfigValidatorTest`.
- Passed `.\gradlew.bat :platform:core:compileJava :addons:npcs:compileJava :addons:npcs:compileTestJava :addons:portals:compileJava :addons:portals:compileTestJava`.
- Passed `.\gradlew.bat :addons:npcs:test --tests panetina.elarion.addons.npcs.config.NpcConfigDescriptorsTest --tests panetina.elarion.addons.npcs.config.NpcConfigValidatorTest :addons:portals:test --tests panetina.elarion.addons.portals.PortalTicketItemTest`.
- Live QA server startup reached ready state and loaded `1 trade catalogs`;
  client visual QA was blocked by Minecraft `Invalid Session` before joining
  localhost. Evidence/logs are under `build/ui-qa/npc-trade-snapshot-redo/`.

Next recommended slice:

- Phase 4, Slice 23: Economy idempotent operation receipt proposal/foundation
  for future crash-safe NPC purchases. Classification: LARGE proposal, then
  split implementation. Do not add NPC purchase packets, stock persistence, or
  buy/sell mutations until the receipt contract is approved.

## Economy Policy Follow-Up - Bank Interest, Taxes, Physical Service Payments

Status: completed on 2026-07-08.

- Added config-backed bank interest fields to `economy.yml`: enable flag,
  interval, rate in basis points, minimum balance, minimum payout, and maximum
  accounts processed per tick.
- Added bounded bank-interest processing in `EconomyTransactionService`.
  Interest is disabled by default and, when enabled, pays audited reward
  transactions in account batches rather than one unbounded tick.
- Added `bank.withdrawal-tax-basis-points`; withdrawals can charge bank tax,
  deposits remain untaxed.
- Added `shops.sales-tax-basis-points` as the Economy-owned policy hook for
  future shop/trader purchase settlement.
- Added `ElarionEconomyApi.payPhysicalOnly(...)` and moved Portal fee-passage
  and Shrine currency offerings to carried physical currency only. Banked
  money must be withdrawn before those spends.
- Increased physical Sigil item max stack size to 999; withdrawal stack
  creation already uses the registered item max count.

Verification:

- Passed `.\gradlew.bat :addons:economy:compileJava :addons:economy:compileTestJava :addons:offerings:compileJava :addons:portals:compileJava`.
- Passed `.\gradlew.bat :addons:economy:test --tests panetina.elarion.addons.economy.config.EconomyConfigDescriptorsTest --tests panetina.elarion.addons.economy.service.EconomyTransactionServiceTest --tests panetina.elarion.addons.economy.service.EconomyGovernorServiceTest --tests panetina.elarion.addons.economy.registry.EconomyNpcActionsTest`.

Next recommended slice:

- Phase 4, Slice 23 remains the Economy idempotent operation receipt
  proposal/foundation before enabling real NPC shop purchases.

## Latest Follow-Up - Trader Row Polish And QA Relaunch Fix

Date: 2026-07-08.

Objective:

- Fix trader row price/tooltip presentation and make the live QA client easier
  to relaunch with stable local identities.

Decisions:

- Trader item tooltips now activate only from the actual 16x16 item icon
  hitbox, not the whole row.
- Trader price rows now use a fixed Sigil icon column, with the number drawn
  immediately after the icon so every row aligns consistently.
- NPC trade preview custom names/lore and real Portal ticket names/lore now
  explicitly disable Minecraft's default italic custom-name styling.
- `runClientOne` and `runClientTwo` now pass stable dev UUID/access-token
  values to avoid unnecessary account-state drift during QA launches.
- Physical Sigil max stack remains source-configured at 999; focused compile
  and constant test pass. The still-visible 99 stack in an old client view
  requires a refreshed runtime/session to verify.

Verification:

- Passed `.\gradlew.bat :addons:npcs:compileJava :addons:portals:compileJava :addons:economy:compileJava :addons:economy:compileTestJava :addons:economy:test --tests panetina.elarion.addons.economy.EconomyItemsTest`.
- Dev server reached ready state and loaded `1 trade catalogs` under
  `build/ui-qa/trade-fixes-20260708/`.
- Live in-world UI QA unblocked: the `Invalid Session` captures were caused by
  clicking Realms with full-window screenshot coordinates. `minecraft-qa.ps1`
  expects client-area coordinates, so maximized-menu clicks must subtract the
  Windows title bar.
- Follow-up live QA under
  `build/ui-qa/trade-fixes-20260708-stack999/` verified one-slot 999 Sigils,
  fixed trader Sigil price columns, row-only hover without tooltips, icon-only
  native item tooltips, and non-italic ticket text.

Next recommended slice:

- Resume Phase 4, Slice 23: Economy idempotent operation receipt foundation
  before real NPC shop purchases.

## Phase 4 Slice 23 Proposal - NPC Purchase Foundation

Status: policy approved; Slices 23A-23C.1 completed by 2026-07-09.

- Added `docs/reports/NPC_TRADE_PURCHASE_FOUNDATION_PROPOSAL.md`.
- Proposed `tax-jurisdiction: auto|realm:<id>|world:<id>` on definitions plus
  placement-owned resolved registration so definitions remain reusable.
- Realm taxes route to Realm treasuries. Non-Realm world activity routes to an
  owner-administered Worldheart treasury. Shop purchases use physical Sigils.
- Proposed a selected-offer panel with minus, numeric quantity, plus, Max, and
  server-authored Subtotal/Tax/Total.
- Split implementation into 23A Economy receipts, 23B NPC jurisdiction, 23C
  tax quote/quantity UI, 23D unlimited purchases, and 23E finite stock.

Slice 23A result:

- Added generic idempotent Economy operation keys/receipts and O(1) lookup.
- Economy state schema v2 persists bounded receipts and migrates schema v1
  after creating a backup.
- Transaction journal metadata reconstructs a missing receipt after a crash
  before snapshot, preventing duplicate charges.
- Full `:addons:economy:test` passes.

Slice 23B result:

- Added `tax-jurisdiction: auto|realm:<id>|world:<id>` definition policy and
  matching read-only descriptors.
- Placed NPC schema v2 stores resolved `REALM|WORLD` jurisdiction; schema v1 is
  backed up and atomically migrated.
- Place, duplicate, move, repair, startup, and reload paths validate policy
  against Core Realm/world ownership before mutating entities.
- `/e npc inspect` reports resolved jurisdiction.
- Full `:addons:npcs:test` passes.

Slice 23C result:

- Economy owns strict schema-v1 Realm/Worldheart category tax policy and O(1)
  checked quote calculation.
- NPCs uses an optional Economy adapter and bounded quantity quote packets.
- The trader has a selected-offer quantity/tax/total panel; Confirm remains
  disabled and no money or inventory moves.

Slice 23C.1 result:

- Core owns persistent Worldheart governing authority with `SYSTEM` and
  `PLAYER` modes. Missing state defaults to system governance by the
  lore-facing `Hollow Emperor`.
- `ElarionApi.worldheart()` centralizes role checks for OP4/admin, current
  Worldheart ruler, and ordinary players. Future Worldheart blocks or UI must
  call this service.
- Economy owns the stable `WORLDHEART_TREASURY` account and schema-v3
  `worldheartTreasury` balance. Changing authority does not transfer or reset
  treasury funds.
- Economy tax destination routing now maps Realm authorities to Realm
  treasuries and Worldheart/non-Realm authorities to the stable Worldheart
  treasury.
- Focused Core and Economy authority/routing tests pass.

Slice 23D result:

- Added `NpcTradePurchaseRequestPayload` and `NpcTradePurchaseResultPayload`.
- Added NPC-owned schema-v1 `trade-purchases.json` with PREPARED, PAID,
  COMPLETE, and FAILED records.
- Added Economy `PUBLIC_REVENUE` transactions and
  `ElarionEconomyApi.payPhysicalOnlyOnce(...)` so NPC purchases consume carried
  physical Sigils only and replay safely through Economy receipts.
- Unlimited BUY settlement now credits the resolved Realm or Worldheart
  treasury with subtotal/tax metadata; bank balances are not used.
- Trader Confirm is enabled only for valid server-authored quotes and displays
  server result feedback.
- `:addons:economy:test :addons:npcs:test` passes.

## Phase 4 Slice 23E - Finite Placed-NPC Stock

- Added parsed NPC trade stock fields: `stock-limit`, `restock-amount`, and
  `restock-interval-seconds`. Existing offers without these fields remain
  unlimited.
- Added NPC-owned schema-v1 `trade-stock.json` runtime state keyed by placed
  NPC UUID and offer ID. Stock is separate from `trades.yml` config definitions
  and from Economy transactions.
- Added lazy restock during trader open/quote/purchase paths. No per-tick
  merchant stock scan was introduced.
- Added bounded consumed-purchase ID retention per stock record so replayed
  purchase IDs do not decrement stock twice.
- Wired stock into server-authored quote maximums, purchase revalidation, and
  compact row metadata labels. Native item tooltips remain icon-only.
- Updated NPC config descriptors/tests, persistence docs, UI journal/audit,
  addon docs, and handoff.
- Verified with `.\gradlew.bat :addons:npcs:compileJava :addons:npcs:test`.

Next recommended slice: NPC Sell/buyback audit and proposal. Define accepted
item filters, inventory validation, price policy, tax/fee routing, rollback,
packet shape, and UI states before adding Sell mutations. Classification:
MEDIUM proposal.

## Phase 4 Slice 23F - NPC Sell/Buyback Audit And Proposal

- Added `docs/reports/NPC_SELL_BUYBACK_PROPOSAL.md`.
- Decision: Sell is not the inverse of Buy. A dupe-safe sale must remove exact
  matching player stacks into durable NPC-owned escrow before any payout.
- Decision: NPCs owns sell definitions, inventory matching, sale journals,
  escrow, and stock destination policy. Economy owns dynamic price/inflation
  quotes, fees/taxes, idempotent payouts, and currency delivery.
- Decision: V1 should support explicit `exact_item` and `exact_stack` sell
  match modes only. Arbitrary sell-anything appraisal is a later market system.
- Decision: UI remains a thin client. It may render server-authored eligible
  sell offers and quote results, but it must not choose authoritative slots,
  item components, price, fee, or payout.
- Proposed implementation order: Economy price/payout API proposal, disabled
  sell definition parsing/descriptors, sale journal/escrow storage, then live
  Sell settlement.
- Verification: documentation/source consistency checks only; no production
  code changed in this proposal slice.

Next recommended slice: Phase 4 Slice 23G, Economy dynamic price and
idempotent payout API proposal for NPC trades. Keep it documentation/API design
first unless the exact API surface and tests are approved. Classification:
MEDIUM proposal.

## Phase 4 Slice 23G - Economy Trade Price And Payout API Proposal

- Added `docs/reports/ECONOMY_TRADE_PRICE_PAYOUT_API_PROPOSAL.md`.
- Decision: Economy owns `price-key` resolution, dynamic price/inflation
  policy, fee/tax calculation, idempotent seller payouts, and bounded
  price-policy indexes.
- Decision: NPCs may pass catalog/offer/stock context to Economy but must not
  read Economy internals, calculate inflation, or mutate balances directly.
- Proposed `EconomyTradePriceRequest` / `EconomyTradePriceQuote` boundary:
  supports BUY/SELL, authority, price key, fixed fallback, quantity, stock
  context, policy revisions, and checked arithmetic.
- Proposed `payPhysicalRewardOnce(...)` boundary: checks operation receipts
  before inventory insertion, persists a payout transaction first, and replays
  without inserting duplicate Sigils.
- Decision: full dynamic inflation is not first implementation. First Java
  slice should return static fallback/service-price quotes through the final
  shape so future inflation can be added behind Economy.
- Verification: documentation/source consistency checks only; no production
  code changed in this proposal slice.

Next recommended slice: Phase 4 Slice 23H, implement Economy trade price
request/quote records and a pure static/service-price quote method with unit
tests. Keep idempotent physical payout and NPC Sell mutation out of that slice.
Classification: MEDIUM.

## Phase 4 Slice 23H - Economy Trade Price Quote API

- Added `EconomyTradeDirection`, `EconomyTradePriceSource`,
  `EconomyTradePriceRequest`, and `EconomyTradePriceQuote`.
- Added `EconomyPricingService.quoteTradePrice(...)` and public
  `ElarionEconomyApi.quoteTradePrice(...)`.
- Current implementation resolves known service-price `price-key` values or
  fixed fallback prices, then delegates fee/tax arithmetic to
  `EconomyTaxPolicyService`. Dynamic inflation/scarcity remains future work
  behind the same API.
- BUY quotes expose total cost. SELL quotes expose net payout after fee/tax.
- Added focused tests for fallback pricing, service-price resolution, required
  unknown price-key rejection, SELL net payout, overflow/quantity rejection,
  and bounded dynamic-pricing context.
- Verified with `.\gradlew.bat :addons:economy:compileJava :addons:economy:test`.

Next recommended slice: Phase 4 Slice 23I, idempotent physical payout wrapper
for seller rewards. Decide and implement full-inventory recovery behavior
without adding NPC Sell mutations. Classification: MEDIUM.

## Phase 4 Slice 23I - Idempotent Physical Payout Wrapper

- Added `EconomyInventoryService.payPhysicalRewardOnce(...)` and public
  `ElarionEconomyApi.payPhysicalRewardOnce(...)`.
- The wrapper checks existing Economy operation receipts before touching
  inventory, so operation replay does not insert duplicate Sigils.
- The wrapper rejects invalid amounts and insufficient physical inventory space
  before writing the payout transaction. It does not drop seller payouts on the
  ground.
- The wrapper records an idempotent `REWARD` transaction from `MINT` to
  `PHYSICAL_CURRENCY` before inserting Sigils.
- Added package-level capacity math tests for empty/partial/full capacity
  behavior. Existing Economy receipt tests cover transaction-layer replay and
  fingerprint conflicts.
- Verified with `.\gradlew.bat :addons:economy:compileJava :addons:economy:test`.
- Important limitation: this is dupe-safe but not yet complete for live NPC
  Sell. A crash after receipt persistence and before physical insertion can
  leave payout undelivered. NPC Sell must wait for claimable/deferred payout
  delivery or an approved bank payout policy.

Next recommended slice: Phase 4 Slice 23J, payout delivery recovery hardening.
Preferred path: Core/Economy claimable physical payout delivery keyed by the
same operation ID, or an explicit approved bank-payout policy. Keep NPC Sell
mutation out until post-receipt delivery is restart-safe. Classification:
MEDIUM.

## Phase 4 Slice 23J - Bank-Backed Seller Payout Recovery

- Added `EconomyTransactionService.rewardOnce(...)` as the canonical
  idempotent reward transaction wrapper.
- Added public `ElarionEconomyApi.rewardOnce(...)`.
- Added public `ElarionEconomyApi.payPlayerBalanceRewardOnce(...)` for V1 NPC
  seller payouts.
- Chose the bank-backed payout policy for first live Sell support because
  physical inventory insertion cannot currently be made atomic with Economy
  receipt persistence.
- Seller payouts can now be credited to the player's wallet through one
  persisted Economy operation receipt, and replay after restart returns the
  original transaction without duplicate credit.
- Physical-only spending remains unchanged: NPC shops, Shrines, and Portals
  should continue using carried Sigils unless a future approved policy changes
  them.
- `payPhysicalRewardOnce(...)` remains documented as not approved for live NPC
  Sell until a Core/Economy delivery queue closes the post-receipt inventory
  insertion gap.
- Added transaction tests for wallet payout replay, idempotency conflict, and
  restart replay.
- Verified with `.\gradlew.bat :addons:economy:compileJava :addons:economy:test`.
- No NPC Sell definitions, prompts/actions, item escrow, payout execution,
  packets, or client-side shop mutation were added.

Next recommended slice: Phase 4 Slice 23K, NPC Sell definition parsing. Add
disabled-by-default SELL definitions, descriptors, and validation only. Exclude
inventory mutation, item escrow, payout execution, prompts/actions, packets,
and client-side shop mutation. Classification: SMALL.

## Phase 4 Slice 23K - NPC Sell Definition Parsing

- Extended `NpcTradeOfferDefinition` with Sell metadata: `sellMatch`,
  `componentPolicy`, `maxQuantity`, and `stockDestination`.
- `trades.yml` now parses `direction: sell`, `sell-match`,
  `component-policy`, `max-quantity`, and `stock-destination`.
- The default `worldheart_trader` catalog includes a disabled
  `cobblestone_buyback` Sell definition as an example config shape.
- Validation now allows `direction: sell` only as disabled config data.
  Enabled Sell offers are rejected until sale journal, escrow, payout,
  packets, and UI mutation exist.
- Sell validation accepts only `sell-match: exact_item|exact_stack`,
  `component-policy: vanilla_only|exact_components`,
  `stock-destination: none|placed_npc`, and `max-quantity` 1-64.
- Read-only NPC config descriptors expose offer direction, enabled state,
  count, and the Sell-specific fields.
- Added focused validator and descriptor coverage.
- Verified with `.\gradlew.bat :addons:npcs:compileJava :addons:npcs:test`.
- No server snapshot rows, inventory mutation, item escrow, payout execution,
  prompts/actions, packets, or client-side shop mutation were added.

Next recommended slice: Phase 4 Slice 23L, NPC sale journal and escrow storage.
Add schema-v1 NPC-owned sale records and serialized item escrow with round-trip,
restart, unsupported-schema, and replay-state tests only. Exclude payout,
player inventory mutation, packets, prompts/actions, and UI mutation.
Classification: MEDIUM.

## Phase 4 Slice 23L - NPC Sale Journal And Escrow Storage

- Added `NpcTradeSaleStatus` with explicit recovery states: PREPARED,
  ITEMS_ESCROWED, PAID, STOCK_UPDATED, COMPLETE, FAILED, and RESTORED.
- Added `NpcTradeEscrowStack` for serialized escrow item identity.
- Added `NpcTradeSaleRecord` with operation/session/catalog/offer fields,
  server quote snapshot values, escrow payload, Economy receipt IDs, request
  matching, and transition helpers.
- Added `NpcTradeSaleStorage` for schema-v1
  `world/elarion/addon-state/npcs/trade-sales.json`.
- Added storage tests for escrow round-trip, restart/replay-state persistence,
  request matching, and unsupported schema fail-closed behavior.
- Verified with `.\gradlew.bat :addons:npcs:compileJava :addons:npcs:test`.
- No payout, player inventory mutation, packets, prompts/actions, stock update
  from sales, or UI mutation was added.

Next recommended slice: Phase 4 Slice 23M-Prereq, inventory escrow service
audit/proposal. Inspect Fabric 1.21.1 item/component APIs and current NPC item
stack helpers, then define exact matching, bounded removal, serialized escrow,
restore behavior, and tests. Do not mutate player inventory or add Sell
packets/UI yet. Classification: SMALL.

## Phase 4 Slice 23M-Prereq - Inventory Escrow Service Proposal

- Inspected `NpcTradeItemStacks`, `NpcTradePurchaseService`,
  `EconomyInventoryService`, and Underworld's `StoredItemStack`.
- Confirmed NPC BUY delivery, Economy physical-currency helpers, and Underworld
  recovery storage are useful references but should not be copied directly into
  live NPC Sell settlement.
- Defined the server-only escrow helper contract for exact-item/exact-stack
  matching, bounded preflight, partial stack removal, encoded component
  serialization, restoration, and replay states.
- Recorded that the existing `NpcTradeEscrowStack` audit fields are not enough
  for live Sell; the next code slice must add full encoded `ItemStack` payload
  storage before any player inventory mutation is allowed.
- No production Java, packets, prompts/actions, UI mutation, player inventory
  mutation, payout, or stock update behavior was added.
- Verification: documentation-only slice; `git diff --check` was run for the
  touched planning/report files.

Next recommended slice: Phase 4 Slice 23M1, NPC stored escrow stack payload.
Add an NPC-owned component-preserving stored stack record and storage tests
using the Fabric 1.21.1 `ItemStack.encode(registries)` pattern. Extend sale
escrow storage to write full encoded stack payload while preserving old lossy
test-row compatibility. Do not remove player inventory, pay sellers, add Sell
packets, or mutate UI yet. Classification: SMALL.

## Phase 4 Slice 23M - Server Sell Settlement Completion

- Extended `NpcTradeEscrowStack` with full encoded `ItemStack` payload,
  source-slot metadata, and compatibility constructors for older lossy rows.
- Added `NpcTradeInventoryEscrow` for server-side main-inventory matching,
  bounded removal, encoded escrow creation, and restoration.
- Added `NpcTradeEscrowResult`, `NpcTradeSaleSettlement`,
  `NpcTradeSaleProvider`, and `EconomyNpcTradeSaleProvider`.
- Updated Economy quote integration to use `quoteTradePrice(...)` for both BUY
  cost and SELL net payout so future tax/inflation/scarcity policy stays
  Economy-owned.
- Wired Sell through `NpcTradePurchaseService` using the existing request/result
  packet path, separate sale journal, idempotent Economy wallet payout, and
  replay handling for escrowed, paid, stock-updated, complete, failed, and
  restored sales.
- Enabled Sell rows in `NpcTradeSnapshotService` and the Trade screen. The
  client renders server-authored Sell rows and sends only offer/quantity
  requests; it never selects authoritative inventory slots.
- Default `worldheart_trader` cobblestone buyback is enabled. Config validation
  now accepts enabled Sell rows when their matching/component policies are
  valid.
- Added sale storage coverage for encoded escrow payload fields.
- Superseded by Slice 24: `stock-destination: placed_npc` originally parsed as
  a no-op completion step because no destination offer ID or stock pool existed.
- Verification: `.\gradlew.bat :addons:npcs:compileJava :addons:npcs:test`
  passed.

Next recommended slice: Phase 4 Slice 24, NPC trade stock destination
hardening. Add an explicit destination-offer field or stock-pool model for
`stock-destination: placed_npc`, then route sold items into a configured resale
stock target with tests. Classification: MEDIUM.

## Phase 4 Slice 24 - NPC Trade Stock Destination Hardening

- Added `destination-offer` to `NpcTradeOfferDefinition`, `trades.yml` parsing,
  generated defaults, read-only config descriptors, and descriptor tests.
- Validation now rejects `stock-destination: placed_npc` Sell offers unless
  `destination-offer` points to a BUY offer in the same catalog.
- Default `worldheart_trader` cobblestone buyback now routes sold cobblestone
  into the placed NPC's `cobblestone` BUY stock.
- Added `NpcTradeStockService.supply(...)`, using the same bounded operation-ID
  replay protection as stock consumption so sale retries cannot duplicate stock
  and supply cannot exceed the BUY offer's stock limit.
- Wired paid Sell completion to perform the destination stock update before
  transitioning the sale through `STOCK_UPDATED`/`COMPLETE`. If payout already
  happened but the destination is unavailable, the sale remains paid and can be
  retried after config/storage is fixed.
- Verification: `.\gradlew.bat :addons:npcs:compileJava :addons:npcs:test`
  passed.

Next recommended slice: Phase 4 Slice 25, NPC trade live QA and older-config
migration assessment. Verify Buy/Sell replay, destination stock replenishment,
tooltips, Sigil presentation, and older `trades.yml` files that may lack
`destination-offer`. Classification: SMALL.

## Phase 4 Slice 25 - NPC Trade Compatibility And Admin Authoring Notes

- Added a narrow in-memory compatibility bridge for the known generated legacy
  `worldheart_trader.cobblestone_buyback` route: if it still has
  `stock-destination: placed_npc` but no `destination-offer`, the loader
  projects `destination-offer: cobblestone`.
- Custom trader catalogs remain strict. The loader does not guess custom Sell
  destinations, so admins must author `destination-offer` explicitly for custom
  placed-NPC stock routes.
- Added `NpcTradeOfferDefinition.withDestinationOfferId(...)` for the bridge
  without mutating config files.
- Added focused migration tests proving the shipped legacy route is bridged and
  custom routes are not guessed.
- Updated NPC/admin/config docs to state that a new NPC can be assigned a trade
  screen entirely through YAML: `trade-catalog`, a `presentation: trade`
  dialogue node, and `trades.yml` offers.
- Verification: `.\gradlew.bat :addons:npcs:compileJava :addons:npcs:test`
  passed.

Next recommended slice: Phase 4 Slice 26, NPC trade live QA. Use the dev
server/client path to verify the trade screen, item tooltips, Sigil columns,
Buy/Sell replay behavior, and visible stock replenishment on the placed trader.
Classification: SMALL.

## Phase 4 Slice 26 - NPC Trade Live QA And Stock Refresh

Status: completed on 2026-07-09.

Objective:

- Verify the dedicated trader UI live against the current dev server/client and
  fix any focused blocker discovered during QA.

What changed:

- Updated the current dev-run NPC configs so the Worldheart trader has the
  live `worldheart_trader` catalog, current dialogue text, Nether/End ticket
  offers, stock metadata, and explicit `destination-offer` for cobblestone
  buyback.
- Fixed a live stock-refresh bug: after a successful trade mutation,
  `NpcInteractionService` now sends the result payload and then a fresh
  `NpcTradeSnapshotPayload`. The client no longer needs to reopen the trader to
  see finite stock decrement.
- Documented the server-owned snapshot refresh invariant in NPC docs and the
  project index.

Live QA evidence:

- Captures are under `build/ui-qa/slice-26-npc-trade-live/`.
- `08-trader-dialogue.png`: conversation-first trader entry with the curated
  portrait and `Trade` option.
- `09-trade-buy.png`: Buy catalog loaded with Nether/End ticket icons, stock,
  Sigil price columns, and quote panel.
- `10-hover-nether-icon.png` / `11-hover-nether-text.png`: native tooltip opens
  only over the item icon, not the row text.
- `12-trade-sell.png`: Sell tab opens and disables Confirm when no matching
  cobblestone is present.
- `18-after-refresh-fix-stock10.png`: after the second successful purchase,
  visible Nether ticket stock refreshes from `Stock 11` to `Stock 10`.

Verification:

- Passed `.\gradlew.bat :addons:npcs:compileJava :addons:npcs:test`.
- Restarted `runServer`, reconnected `runClientOne`, reopened
  `worldheart_trader_1`, bought a Nether ticket bundle, and verified stock
  refresh plus persisted `trade-stock.json` state.

Unresolved risk / deferred:

- The dev-run trader was placed manually for QA in this local world after the
  restart showed no placed NPC records. Future reusable QA should script banker
  and trader placement/reset deterministically.
- Client log still contains unrelated resource-pack model warnings for
  Excalibured hat models; no NPC trade payload crash was observed.

Next recommended slice: Phase 4 Slice 27, NPC trade QA automation/harness
cleanup. Make dev QA setup faster and repeatable by scripting placed banker and
trader reset/open steps, then continue the broader revamp plan with the next
approved UI or economy slice. Classification: SMALL.

## Phase 4 Slice 27 - NPC Trade Action Band Polish

Status: completed on 2026-07-09.

Objective:

- Fix the visually awkward selected-offer band in the dedicated trader UI.

What changed:

- Reworked `NpcTradeScreen` layout constants for the catalog and selected-offer
  action band.
- Expanded the catalog shell so the selected-offer controls no longer spill
  outside the parent frame.
- Replaced the shallow mixed row with a clearer bounded action panel:
  left-side Amount controls, center status/error feedback, larger right-side
  Confirm button, and a separated lower row for Subtotal, Tax, and Total/Payout.
- Expanded the visible catalog area to four rows and moved the row counter
  above the action band so price/Confirm/Amount no longer fight for space.
- Added a smaller currency-icon rendering path for footer totals so Sigil
  icons do not dominate the action band.
- Updated the UI journal with the rule for future trader/shop screens.

Verification:

- Passed `.\gradlew.bat :addons:npcs:compileJava :addons:npcs:test`.
- First live visual evidence before the final row-count expansion:
  `build/ui-qa/slice-27-trade-action-band/06-trade-action-band.png`.
- Post-expansion live rerun started `runServer` and `runClientOne`; the server
  reached ready state, but the client hit Minecraft's `Invalid Session` dialog
  before reaching the server list. Evidence:
  `build/ui-qa/slice-27-trade-action-band/10-multiplayer-rerun.png`.

Unresolved risk / deferred:

- Final post-expansion in-world screenshot QA is still pending because the
  client could not join the dev server in this rerun.

Next recommended slice: Phase 4 Slice 28, NPC trade QA automation/harness
cleanup. Add a narrow dev QA setup path that can reset/place banker and trader
and speed up screenshot capture, then continue the broader revamp plan.
Classification: SMALL.

## Phase 4 Slice 28 - NPC Trade Layout And QA Helper

Status: completed on 2026-07-09.

Objective:

- Fix the remaining weirdness in the trader amount/price/Confirm area and make
  banker/trader screenshot QA faster.

What changed:

- `NpcTradeScreen` now uses a taller shell with four visible catalog rows and a
  contained selected-offer panel.
- The selected-offer panel has a clear top control strip for Qty, minus/plus,
  quantity, Max, status text, and Confirm.
- Subtotal, jurisdiction tax, and Total/Payout now sit in a lower totals strip.
- Added `dev/tools/npc-trade-qa.ps1` to rebuild the default banker/trader pair
  through existing `/e npc` commands after joining the dev server.
- Documented the helper in `CODEX.md`, `docs/test-commands.md`,
  `INDEX.md`, `docs/systems/NPCs.md`, and `docs/systems/UI_JOURNAL.md`.

Verification:

- Passed `.\gradlew.bat :addons:npcs:compileJava :addons:npcs:test`.
- `dev/tools/npc-trade-qa.ps1` PowerShell syntax parses successfully.
- Live QA started `runServer`/`runClientOne`, joined the saved localhost server,
  ran the setup helper, opened the trader, and captured the fixed Buy screen at
  `build/ui-qa/slice-28-npc-trade-layout-qa/05-trade-buy-layout.png`.

Unresolved risk / deferred:

- `capture-trader` works only when the client is already in-world. The helper
  intentionally does not auto-join the server.
- `-CloseScreens` is opt-in because automatic ESC can disrupt the current
  connection/menu state.

Next recommended slice: Phase 4 Slice 29, continue the broader UI revamp with
the next approved migrated screen family or return to economy tax/stock
integration if the trader backend is the priority. Classification: SMALL.

## Phase 4 Slice 29 - NPC Trade Readability Polish

Status: completed on 2026-07-09.

Objective:

- Tighten trader row readability, compact purchase totals, clean page/range
  text, and remove the shared NPC portrait bottom gutter.

What changed:

- Trader rows now show icon, title, finite stock, and price only.
- Removed row subtitle/description text from compact rows.
- Stock is right-aligned before the fixed Sigil price column.
- The row range text is centered as `Rows A-B / N` instead of a cramped
  right-side counter.
- Purchase summary cells now keep label, Sigil icon, and amount close together
  for Subtotal, tax, and Total/Payout.
- Configured NPC portraits draw closer to the frame edge to avoid the black
  bottom gap under portrait art.

Verification:

- Passed `.\gradlew.bat :addons:npcs:compileJava :addons:npcs:test`.
- Live QA rerun started `runServer`/`runClientOne`, but the dev client hit
  Minecraft's `Invalid Session` before the server list. Evidence:
  `build/ui-qa/slice-29-npc-trade-readable-polish/02-menu-fullscreen.png`.

Next recommended slice: Phase 4 Slice 30, move on from NPC micro-polish and
continue the broader revamp with the next approved non-NPC screen family.
Classification: SMALL.

## Phase 4 Slice 30 - Portal Prompt Cost Kind Contract

Objective:

- Replace the Portal Confirmation free/no-fee display-text heuristic with an
  explicit server-authored prompt cost kind.

What changed:

- `PortalTravelPromptPayload` now carries normalized `costKind` values:
  `free`, `ticket`, or `fee`.
- `PortalRouteService` assigns the cost kind from route mode, direction, fee
  passage, stored return passage, and free-passage state.
- `PortalConfirmationScreen` uses `costKind` for payment-slot visibility and
  semantic ticket/currency icon selection instead of reading localized
  requirement text.
- Added packet round-trip/fallback coverage for the new prompt field.
- Removed the completed TODO about the old Portal free/no-fee text heuristic.

Verification:

- Passed `.\gradlew.bat :addons:portals:compileJava :addons:portals:test`.

Next recommended slice: Phase 4 Slice 31, continue the broader UI revamp with
the next non-NPC/non-Portal screen family, preferably Grave Recovery shell
polish or Admin Panel danger/confirmation QA cleanup depending on current user
priority.
Classification: SMALL.

## Phase 4 Slice 31 - Grave Recovery Shell Polish

Objective:

- Bring the Underworld Grave Recovery screen closer to the shared civic popup
  pattern without changing recovery behavior or packets.

What changed:

- `GraveRecoveryScreen` now uses one internal layout snapshot for render,
  scroll, and click math.
- The title is rendered through the shared typography helper and aligned with
  the ornamented civic header.
- Status/body content now sits in a framed message panel with the status chip,
  item count, owner line, and wrapped body text.
- Grave contents now sit inside a framed grid section with a section title,
  scroll row label, and item-slot-only native tooltips.
- Footer actions now sit in a bounded action band and use the shared centered
  compact action buttons.

Verification:

- Passed `.\gradlew.bat :addons:underworld:compileJava :addons:underworld:test`.

Next recommended slice: Phase 4 Slice 32, continue through remaining
player-facing UI families with a small Admin Panel danger/confirmation polish
slice or begin a bounded Bank/Economy UI audit depending on current priority.
Classification: SMALL.

## Phase 4 Slice 32 - Admin Panel Danger Modal Polish

Objective:

- Tighten Admin Panel danger-row and generic confirmation/input modal layout
  without changing admin behavior, packets, permissions, provider actions, or
  config edit shell behavior.

What changed:

- Danger rows now preserve destructive styling while showing hover and selected
  state, including the active-green selected marker.
- Generic Admin Panel action confirmation/input modals now use one
  `ActionModalLayout` metric record for render and click geometry.
- Modal body text renders inside a bounded message panel; danger confirmations
  use a destructive accent.
- Input action modals keep the field and Tab suggestions between the body panel
  and footer action band.
- Added layout tests to keep confirmation/input modal body, field, and buttons
  inside the modal frame.

Verification:

- Passed `.\gradlew.bat :platform:core:compileJava :platform:core:test --tests panetina.elarion.core.client.ElarionAdminPanelScreenLayoutTest`.

Next recommended slice: Phase 4 Slice 33, continue with a bounded
Bank/Economy UI audit before changing the bank screen, because Economy service
tax/interest/physical-currency rules are now more complex than a pure visual
patch.
Classification: SMALL.

## Phase 4 Slice 33 - Bank/Economy UI Audit

Status: completed on 2026-07-09.

Objective:

- Audit the dedicated NPC bank service UI against Economy-owned withdrawal tax,
  bank interest, physical Sigil, and server-authoritative settlement rules
  before changing the screen again.

Files inspected:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcBankScreen.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/service/NpcInteractionService.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/network/NpcDialogueOpenPayload.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/network/NpcDialogueOptionPayload.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/network/NpcTradeQuotePayload.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/network/NpcTradeQuoteRequestPayload.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/api/ElarionEconomyApi.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/service/EconomyInventoryService.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/service/EconomyTransactionService.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/registry/EconomyNpcActions.java`

Files changed:

- `docs/reports/BANK_ECONOMY_UI_AUDIT.md`
- `INDEX.md`
- `TODO.md`
- `PLAN.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/addons/economy.md`
- `docs/addons/npcs.md`
- `docs/ai/CURRENT_STATUS.md`

Decisions:

- Keep NPCs as the bank presentation owner and Economy as the sole money-policy
  owner.
- Do not make the bank client compute withdrawal tax or total debit.
- Use the existing NPC trade quote pattern as the model for bank Fee/Total
  previews: request a server quote, render server values, and keep mutation on
  a server-validated path.

Verification:

- No production code changed; no compile/test run was required for this audit.

Next recommended slice: Phase 4 Slice 34, add the Economy bank quote contract
and NPC bank quote request/response transport, then wire `NpcBankScreen` to
render Fee/Total from the latest server quote. Classification: MEDIUM because
it crosses Economy API, NPC networking, client UI, and tests.

## Phase 4 Slice 34 - Server-Authored Bank Quotes

Status: completed on 2026-07-09.

Objective:

- Add an Economy-owned bank quote contract and dedicated NPC bank quote
  request/response transport so `NpcBankScreen` renders Fee/Total from
  server-authored values.

Files changed:

- `addons/economy/src/main/java/panetina/elarion/addons/economy/model/EconomyBankMode.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/model/EconomyBankQuote.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/api/ElarionEconomyApi.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/service/EconomyInventoryService.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/service/EconomyTransactionService.java`
- `addons/economy/src/test/java/panetina/elarion/addons/economy/service/EconomyInventoryServiceTest.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/model/NpcBankQuote.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/service/NpcBankQuoteProvider.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/integration/EconomyNpcBankQuoteProvider.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/network/NpcBankQuoteRequestPayload.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/network/NpcBankQuotePayload.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/ElarionNpcsAddon.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/service/NpcInteractionService.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/ElarionNpcsClient.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcBankScreen.java`
- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/network/NpcBankQuotePayloadTest.java`
- `INDEX.md`
- `TODO.md`
- `PLAN.md`
- `docs/ai/CURRENT_STATUS.md`
- `docs/systems/Networking.md`
- `docs/systems/GUI.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/addons/economy.md`
- `docs/addons/npcs.md`

Behavior changed:

- Economy exposes pure Deposit/Withdraw bank quotes through
  `ElarionEconomyApi.quoteBank(...)`.
- NPCs registers `NpcBankQuoteRequestPayload` and `NpcBankQuotePayload`.
- The server revalidates active NPC session, distance, bank node, and visible
  Deposit/Withdraw option before returning a quote.
- `NpcBankScreen` clears stale quotes on mode/amount changes, ignores stale
  responses, renders Fee/Total from the latest matching quote, and disables
  Confirm until the quote is valid.

Behavior not changed:

- Deposit/Withdraw mutation still uses the existing
  `NpcDialoguePromptSubmitPayload` path and Economy action handlers.
- Bank interest UI, transaction history, tax editing, direct bank spending for
  Portal/Shrine/trader services, persistence, config format, and commands were
  not changed.

Verification:

- Passed `.\gradlew.bat :addons:economy:test :addons:npcs:test`.

Next recommended slice: Phase 4 Slice 35, run live banker QA with a nonzero
withdrawal tax config and capture Deposit/Withdraw quote states, or continue to
the next approved screen family if live QA is deferred. Classification: SMALL.

## Current Focus

## Phase 4 Slice 41 - NPC Trade Action Band And Portal Fee Icon Polish

Completed on 2026-07-09.

Objective:

- Finish the final manual-QA polish from the trader selected-offer panel and
  paid Portal confirmation icon.

Changes:

- Trader scroll range text now sits slightly higher between the catalog and
  selected-offer band, with small `v` and `^` markers flanking the row range.
- Trader quantity value text now uses shared civic centered text metrics
  instead of a hard-coded vertical offset.
- Trader Total/Payout now renders under the Confirm button, separate from
  Subtotal and Realm/Worldheart tax.
- Paid Portal confirmation prompts now draw the shared Sigil currency icon in
  the framed payment slot instead of using the generic coin-pouch catalog art.

Files changed:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcTradeScreen.java`
- `addons/portals/src/main/java/panetina/elarion/addons/portals/client/PortalConfirmationScreen.java`
- `docs/addons/portals.md`
- `docs/systems/UI_JOURNAL.md`
- `TODO.md`
- `PLAN.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :platform:core:compileJava :addons:npcs:compileJava :addons:portals:compileJava`.

Next recommended slice:

- Continue the big revamp with the next non-NPC screen family or performance
  cleanup slice after manual QA confirms this final trader/portal polish.
  Classification: SMALL to MEDIUM depending on selected target.

## Credit-Aware Remaining Revamp Roadmap

Status: updated on 2026-07-09 after user manual QA confirmed the latest
bank/trader/portal polish is acceptable.

Model-tier rule for future slices:

- `Light`: documentation, planning, narrow QA checklist updates, tiny UI
  coordinate/icon/text fixes, no broad source reading.
- `Medium`: contained implementation in one subsystem, focused tests, known
  patterns, no persistence migration.
- `High`: cross-module APIs, networking, server-authoritative behavior,
  config apply/edit behavior, Economy/payment/tax/dupe-sensitive logic.
- `Very High`: major architecture decisions, migrations, concurrency, broad
  persistence changes, or large refactors. Avoid unless the slice truly needs
  it.

Remaining slices should be chosen in this order unless the user reports a live
bug:

1. Phase 4 Slice 42 - Remaining UI Family Inventory And QA Queue
   - Status: completed.
   - Recommended model used: `Light`.
   - Output: `docs/reports/UI_FAMILY_INVENTORY.md` plus index/status updates.

2. Phase 4 Slice 43 - Portal And Grave Recovery Manual-QA Polish
   - Status: completed as focused layout hardening; live screenshot QA remains
     optional/manual.
   - Recommended model: `Medium`.
   - Objective: finish the remaining Portal prompt states and Grave Recovery
     shell polish against the existing civic helpers.
   - Scope: visual/layout only unless a live functional bug is reproduced.
   - Verification: focused compile plus manual QA checklist; live screenshots
     only if the user asks.

3. Phase 4 Slice 44 - Admin Panel Final Visual/Config UX Polish
   - Recommended model: `Medium`.
   - Objective: finish Admin Panel config/danger/modal usability issues after
     the custom payload crash fixes.
   - Scope: UI layout, disabled/enabled state clarity, and docs. Do not add new
     config mutation powers.

4. Phase 5 Slice 1 - Semantic UI Component Audit
   - Recommended model: `Light`.
   - Objective: identify repeated row/card/button/status patterns now proven by
     Notifications, Government, Ledger, Shrine, NPC Bank, Trader, Portal, and
     Grave Recovery.
   - Output: component list and migration order. No production code.

5. Phase 5 Slice 2 - First Reusable Semantic Components
   - Recommended model: `Medium`.
   - Objective: extract only low-risk shared presentation helpers for common
     civic row totals, currency pairs, icon rows, and action bands.
   - Scope: no behavior changes; migrate one caller only.

6. Phase 6 Slice 1 - Government Archive/History Row and Chronicle Projection
   - Status: complete.
   - Verified: Core public-history metadata flow, Government archive
     projection/filtering, and centered compact record row geometry.

7. Phase 6 Slice 2 - Chronicle Variant Contract Proposal and Narrow Prototype
   - Status: complete.
   - Verified: Core Chronicle renderer/provider contract, deterministic
     fallback variant ids, Government renderer registration, and Government
     notification action/icon/source helper standardization.

8. Phase 6 Slice 3 - Government Notification Standardization Follow-Up
   - Recommended model: `Medium`.
   - Objective: ensure Government notifications use Core notification
     categories/actions/icons consistently with the redesigned drawer.

9. Phase 7 Slice 1 - Bank/Economy UI Follow-Up Audit
   - Recommended model: `Light` for docs/audit only, `Medium` if fixing one
     small non-QA issue.
   - Objective: refresh the player-facing UI inventory and choose the next
     single screen family. Bank/Trader is mostly verified manually, so do not
     spend code here unless the inventory finds a real current gap.
   - Output: docs/TODO only unless the user reports a bug or approves one
     narrow UI fix.

10. Phase 7 Slice 2 - Realm Selection And Character Creation Final QA Polish
    - Recommended model: `Medium`.
    - Objective: finish onboarding flow consistency, especially character
      `Continue` -> Realm placement -> final teleport.

11. Phase 7 Slice 3 - Shrine/Offerings Final State Audit
    - Recommended model: `Medium`.
    - Objective: investigate the Shrine state mismatch noted in TODO
      (`Complete` while progress looked `0 / ...`) before doing more visual
      work.
    - Risk: possible projection/state bug.

12. Phase 8 Slice 1 - Chronicle Variant Framework Proposal
    - Status: complete.
    - Verified: current public-history metadata flow, renderer contract,
      variant metadata gap, and future library requirement for 10+ stable
      authored variants per event family.

13. Phase 8 Slice 2 - Core Chronicle Template Library Skeleton
    - Recommended model: `Medium`.
    - Objective: add reusable Core template-family and deterministic stable
      variant-selection contracts with focused tests. Existing
      `chronicle.variant` metadata must win.
    - Scope: no broad family migration and no persistence migration.

14. Phase 8 Slice 3 - Government Template Pilot
    - Recommended model: `Medium`.
    - Objective: migrate one narrow Government family to the Core template
      library with at least 10 authored variants and old-data fallbacks.

15. Phase 8 Slice 4 - Government Election And Founding Families
    - Recommended model: `Medium` or `High`.
    - Objective: add 10+ variants for founding election, vacancy/election
      reopened, nomination, and Realm founding identity families.

16. Phase 8 Slice 5 - Portal And Offering Families
    - Recommended model: `Medium`.
    - Objective: add Portal route/window/travel and Offering milestone/project
      completion template families without changing travel or donation logic.

17. Phase 8 Slice 6 - Core Death, Title, Reward, Realm Families
    - Recommended model: `High`.
    - Objective: promote Core-owned library-worthy lifecycle families with
      careful visibility and event-emission review.

18. Phase 8 Slice 7 - In-Game Library Query Contract
    - Recommended model: `Medium`.
    - Objective: design the future library/bookshelf query contract over
      bounded public-history projections, not raw history files.

19. Phase 8 Slice 8 - Variant Persistence Strategy
    - Recommended model: `High`.
    - Objective: decide and implement where selected `chronicle.variant` ids
      are stamped, with persistence-safe tests and migration/fallback notes.

20. Phase 9 Slice 1 - NPC Narrative Architecture Audit
    - Status: complete.
    - Report: `docs/reports/NPC_NARRATIVE_READINESS_AUDIT.md`.
    - Verified that NPCs are data-driven and server-authoritative, with gaps in
      graph validation, relationship state, story flags, and history-worthy
      action handling.

21. Phase 9 Slice 2 - NPC Graph Validation V1
    - Status: complete.
    - Added pure config validation and tests for root reachability,
      unreachable nodes, duplicate option ids, duplicate variant ids, blank ids,
      and service-node exit safety. Runtime behavior and persistence unchanged.

22. Phase 9 Slice 3 - NPC Relationship State Proposal
    - Status: complete.
    - Defined NPC-owned per-player/per-placed-NPC relationship state,
      visibility, registry contracts, Character Menu boundaries, and
      history/Chronicle rules. Report:
      `docs/reports/NPC_RELATIONSHIP_STATE_DESIGN.md`.

23. Phase 9 Slice 4 - NPC Relationship Persistence V1
    - Status: complete.
    - Added schema-v1 NPC relationship storage/service plus
      `set_relationship`, `add_relationship`, and `relationship_at_least`
      registry handlers. No UI/profile/Chronicle integration yet.

24. Phase 9 Slice 5 - Story Flags And One-Time Choices
    - Status: complete.
    - Added schema-v1 story persistence, one-time options, durable flags,
      endings, opt-in re-entry, and server-authored close behavior.

25. Phase 9 Slice 6 - NPC History/Chronicle Integration
    - Status: complete.
    - Added explicit structured story outcomes with persisted variant ids and
      ten authored Chronicle variants.

26. Phase 10 Slice 1 - Placeholder Audit
    - Recommended model: `Medium`.
    - Objective: inventory placeholders and duplicated formatting tokens.

27. Phase 10 Slice 2 - Placeholder Registry Core Contracts
    - Recommended model: `High`.
    - Objective: add namespaced placeholder registry contracts, visibility
    rules, and bounded resolution pattern.

28. Phase 11 Slice 1 - Profile Contributor Audit
    - Recommended model: `Light`.
    - Objective: list which addon profile projections are safe now versus
    blocked on owner-maintained summaries.

28. Phase 11 Slice 2 - First Addon Profile Contributor
    - Recommended model: `Medium`.
    - Objective: add one low-risk contributor, likely Government office/role
    summary, without scanning broad storage.

29. Phase 12 Slice 1 - Persistence And Performance Target Audit
    - Recommended model: `Medium`.
    - Objective: pick the highest-risk verified persistence/performance item
    from `OPTIMIZATION_TRACKER.md` and source evidence.

30. Phase 12 Slice 2+ - Individual Persistence/Performance Fixes
    - Recommended model: `High` for each fix that touches persistence,
      networking, Economy, tax, NPC stock, or server-authoritative mutation.
    - Rule: one risk at a time, with tests.

31. Phase 13 Slice 1 - Duplicate/Dead Code Classification
    - Recommended model: `Light`.
    - Objective: classify duplicates and shell modules; delete nothing.

30. Phase 13 Slice 2+ - Confirmed Duplicate Removal
    - Recommended model: `Medium` unless persistence/API is touched, then
      `High`.

31. Phase 14 Slice 1 - Final Verification Plan
    - Recommended model: `Medium`.
    - Objective: define the cheapest broad verification set: focused module
      tests first, full build only when needed, manual QA checklist for visuals.

32. Phase 14 Slice 2 - Maintenance Guide
    - Recommended model: `Light` to `Medium`.
    - Objective: document how to add a config domain, Admin page, UI component,
      placeholder, profile section, Chronicle renderer, NPC action/condition,
      notification, and addon integration.

Current next slice:

- Recent completed slice: Character Menu Mount Preview QA / Polish. Focused
  Mounts preview tests pass, Wyvern geometry format was normalized for
  GeckoLib, and live screenshots were captured under
  `build/ui-qa/mount-preview-20260710-redo/`.
- Phase 8 Slice 2 - Core Chronicle Template Library Skeleton.
- Use model: `Medium`.
- Reason: the future library needs a reusable stable-variant selector before we
  add 10+ authored texts to Government, Portal, Offering, death, title, reward,
  and Realm event families.

1. Read `AGENTS.md`.
2. Read `CODEX.md`.
3. Use `INDEX.md` to reach the source-of-truth docs.
4. Read `docs/ai/CURRENT_STATUS.md` for current handoff status.
5. Read `docs/reports/PROJECT_REVAMP_AUDIT.md`.
6. Read `docs/reports/CONFIG_ADMIN_AUDIT.md`.
7. Continue only with the next explicitly approved slice.
8. Work in the existing subsystem first instead of inventing a new one.
9. Before adding new systems, clear or deliberately defer the current audit
   findings in `TODO.md`.

## Current Reality

- Fabric 1.21.1 is the target platform.
- Core owns canonical truth: citizens, Realms, titles, identity, history,
  rewards, permissions, and shared infrastructure.
- Addons extend behavior, but they do not duplicate Core state.
- `addons/offerings` owns Shrine / Offering gameplay.
- `addons/government` owns the civic flow and authority backend.
- `addons/groups` owns public groups and Confederation eligibility hooks.
- `addons/npcs` owns static NPCs and dialogue.
- `addons/portals` owns linked scheduled portals and tickets.
- Current build status: `.\gradlew.bat build` passes when run with a larger
  one-off Gradle heap on this machine (`GRADLE_OPTS=-Xmx4g --no-daemon`);
  focused Core/Mounts compile/tests pass normally.
- Current audit status: no duplicate canonical systems found. Government UI
  actions are now tied to tested server-issued block sessions; remaining
  Government work is command/GameTest coverage and vote lifecycle polish.
- Current handoff snapshot: `docs/ai/CURRENT_STATUS.md`.
- Project-wide revamp audit snapshot:
  `docs/reports/PROJECT_REVAMP_AUDIT.md`.
- Config/Admin deep audit snapshot:
  `docs/reports/CONFIG_ADMIN_AUDIT.md`.
- Current active manual verification areas: Character Menu redesign follow-up,
  Government civic records, Shrine/Government reset preservation, tablist Realm
  headers, Underworld V1, and Character Lifecycle.

## Paused Work Snapshot

- Resumed on 2026-06-29 for Collection preview hardening.
- Implemented but not yet manually verified in-game: Core direct
  `ElarionMenuEntityPreviewRenderer` path for selected unlocked mount previews
  with pre-scissor UI flushing, bounds-aware Mounts preview scale/offset from
  converted geo model bounds, clean row icon frames without black corner pixels,
  pinned Mounts/Pets/Titles tab order, and Core-owned Titles Collection tab
  backed by `TitleService`.
- Last focused verification:
  `.\gradlew.bat :platform:core:compileJava :platform:core:test :addons:mounts:compileJava :addons:mounts:test`
  passed.
- Next resume step for Collection/Character Menu work: define the Core profile
  aggregation boundary before adding profile rows or redesigning the shell
  against the approved Option A boards.

## Next Work Style

- Make small, focused edits.
- Prefer bounded event-driven work over global scans.
- Update docs and tests when behavior changes.
- Keep runtime state in `world/elarion/` and editable definitions in
  `config/elarion/`.
- Treat client packets as requests only; server-side context must prove the
  player is allowed to mutate state.
