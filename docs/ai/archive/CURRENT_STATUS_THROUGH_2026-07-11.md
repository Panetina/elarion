# Current Project Status

Last audited in this documentation pass: 2026-07-10.
Last project-wide revamp audit slice: 2026-07-05.

This file is the fast recovery snapshot for a new AI session or a new machine.
It does not replace source-backed docs. Use it to orient, then follow the
authority chain in `INDEX.md`.

Latest terminology follow-up: all player-facing identity text uses
`Ember`/`Embers`; `Citizenship` is intentionally retained. Character Menu is
the current player-facing name for the former Citizen Ledger. It opens with
`C` or the hidden client command `/charactermenu`; `/ledger` and `/collection`
were removed. The server identity command is `/e ember`. Do not rename stable
technical contracts such as `CitizenRecord`, Citizen profile payload/API
types, `world/elarion/citizens`, the `citizens` config key, or title ID
`citizen`: existing worlds and addon integrations depend on those identifiers.

Latest UI follow-up: Character Menu title previews now show the synchronized
nickname, render a larger head-and-shoulders portrait, and temporarily
normalize/restore player yaw, body yaw, head yaw, and pitch so the preview
always faces the camera. Focused Core identity/layout tests pass.

Latest follow-up slice: Mount Collection previews and flight lean repair.
Live captures established final per-model visual calibration for Airship, Bee,
Chinese Dragon, Sci-Fi Bike, and Wyvern; Ghast and Hot Air Balloon required no
special offset. The Wyvern artifact source included an invalid 33x0x33 baked
`shadow` cube and animated wing ghosting in the Ledger preview. The shadow bone
was removed, the canonical texture was restored unchanged, and the Wyvern uses
a static Ledger preview pose. Do not delete detached texture islands: they are
valid UV artwork. The normalized turn-intent lean deadzone now
allows lean overlays to trigger and compose with ascend/descend. Focused Mount
tests pass. `dev/tools/minecraft-qa.ps1` now posts single alphanumeric keys
directly to Minecraft, so the `C` QA key no longer lands in visible terminals.
Final screenshot:
`build/ui-qa/mount-preview-20260711-final/wyvern-verified.png`.

Latest implementation slice: Phase 8 Slice 3, Government Chronicle template
pilot. Government `proposal-approved` archive rows now use the Core
template-family contract with 10 authored variants, deterministic selected ids,
persisted `chronicle.variant` precedence, required `title` metadata, optional
`category` metadata, and a missing-title fallback. No history persistence,
event emission, packets, UI geometry, or live QA changed. Focused Government
and Core Chronicle tests pass. Recommended next slice: Phase 8 Slice 4,
Government Chronicle family expansion. Recommended model: `Medium`; use High
only if migrating many families, stamping variants into persistence, or running
live UI QA.

Previous implementation slice: Phase 7 Slice 9, fourth owner-maintained Citizen
Ledger addon summary. Portals now increments the Core player-stat key
`portal_journeys` after successful server-authoritative travel, then
contributes that value to the Ledger as `portals/journeys` with `SELF`
visibility through `PortalProfileContributor`. This does not backfill old
travel history and does not change Portal state schema, route definitions,
tickets, payments, entitlements, commands, config, packets, or UI geometry.
Verification passed:
`:addons:portals:test --tests panetina.elarion.addons.portals.service.PortalProfileContributorTest --tests panetina.elarion.addons.portals.storage.PortalStorageTest --tests panetina.elarion.addons.portals.service.PortalFreePassagePolicyTest --tests panetina.elarion.addons.portals.network.PortalTravelPromptPayloadTest :platform:core:test --tests panetina.elarion.core.model.profile.CitizenProfileSummaryFieldsTest --tests panetina.elarion.core.service.CitizenProfileServiceTest :addons:portals:compileJava`.
No live screenshot QA was run in this Medium slice.
Phase 7 is closed for Medium-safe UI/profile migration work. Remaining profile
gaps, NPC reputation and Chronicle recent summary, belong to later NPC and
Chronicle phases.

Previous implementation slice: Phase 7 Slice 8, third owner-maintained Citizen
Ledger addon summary. Quests increments `quests_completed` and contributes
`quests/quests-completed` with `SELF` visibility through
`QuestProfileContributor`.

Earlier implementation slice: Phase 7 Slice 7, second owner-maintained Citizen
Ledger addon summary. Offerings increments `offerings_score` and contributes
`offerings/offering-score` with `SELF` visibility through
`OfferingProfileContributor`.

Earlier implementation slice: Phase 7 Slice 6, first owner-maintained Citizen
Ledger addon summary. Underworld now increments `underworld_lifetime_deaths`
and contributes `underworld/deaths` with `SELF` visibility through
`UnderworldProfileContributor`.

Earlier implementation slice: Phase 7 Slice 5, Character Menu backend-summary
field contract. Core now owns `CitizenProfileSummaryFields`, the canonical
source/field-id contract for Ledger dossier summary slots. The Ledger Profile
tab, Core progression contributor, and Government profile contributor use the
shared constants for reserved profile facts instead of repeating string
literals.

Earlier implementation slice: Phase 7 Slice 4, Grave Recovery QA / Follow-up.
Grave Recovery item slots now use the shared Core `ElarionItemSlotLayout`
helper so native item rendering and tooltip hitboxes share the actual item icon
rectangle. Item lore/enchantments appear only when hovering the icon area, not
the whole civic slot frame. Recovery packets, server-side corpse authority,
storage, and inventory mutation behavior were not changed. Verification passed:
`:addons:underworld:test --tests panetina.elarion.addons.underworld.client.GraveRecoveryScreenLayoutTest --tests panetina.elarion.addons.underworld.network.GraveOpenPayloadTest :addons:underworld:compileJava`.

Earlier implementation slice: Phase 7 Slice 3, Portal Confirmation QA /
Follow-up. Added OP4 `/e portal preview <neutral|nether|end|fee|blocked|return>`
for safe prompt-only visual QA, wrapped long Portal prompt messages inside the
prompt body, and live-QA captured neutral/free, Nether ticket, End ticket, paid
Sigil, blocked, and return states under `build/ui-qa/portal-phase7/final/`.
Preview prompts do not mutate route state or bypass server-side travel
validation.
Verification passed:
`:addons:portals:test --tests panetina.elarion.addons.portals.command.PortalCommandsPreviewTest --tests panetina.elarion.addons.portals.client.PortalConfirmationScreenLayoutTest --tests panetina.elarion.addons.portals.network.PortalTravelPromptPayloadTest --tests panetina.elarion.addons.portals.config.PortalConfigDefaultsTest :addons:portals:compileJava`.

Latest implementation slice: Portal hub defaults and dev-server log cleanup.
Default Portal route definitions and the active dev route config now make
scheduled Nether/End gates depart from `elarion:worldheart`, and Realm Ancient
Gates continue to link Realm worlds to Worldheart instead of lobby. The neutral
route is explicitly unrestricted from any configured source world to any
configured destination world. The dev module now registers
`dev/log4j-elarion-dev.xml` as its single Loom Log4j config, filtering known
harmless modpack/development warnings while leaving real errors and performance
warnings visible. Focused Portal default/descriptor tests passed and
`:dev:configureLaunch` confirms the generated launch config points at the
custom log config.

Latest UI micro-slice: Phase 4 Slice 44 polished the Admin Panel Config edit
shell and action overflow layout. Config edit controls now share one
`ConfigEditLayout` record for render/click/test geometry, detail action lists
use centered `Rows first-last / total` markers with tiny arrows, and selected
danger rows get an active rail while retaining destructive styling.
`:platform:core:test --tests panetina.elarion.core.client.ElarionAdminPanelScreenLayoutTest`
passed. No new config mutation powers, packets, permissions, or provider
semantics changed.

Latest planning slice: Phase 4 Slice 42 added
`docs/reports/UI_FAMILY_INVENTORY.md`, a docs-only inventory of accepted,
pending, and QA-needed custom UI families. It is linked from `INDEX.md` and is
the first stop before choosing more UI polish work. No production code changed.
Latest implementation slice: Realm/Worldheart world seed defaults. Core Realm
spawns in `realms.yml` and Worlds managed-world defaults now use the supplied
Realm and Worldheart seeds/coordinates, with matching active `dev/run` config
values for local restart/reset testing. Focused Core and Worlds descriptor
tests passed. Recommended next slice: Phase 5 Slice 1, Semantic UI Component
Audit. Recommended model: `Light`.

## Active Project-Wide Revamp Status

The project-wide revamp master plan is active. The completed current slices
are Phase 0, Slice 1: audit preparation and repository map verification,
Phase 0, Slice 2: Configuration and Admin Panel deep audit, and Phase 2,
Slice 1: Core read-only config descriptor registry, and Phase 2, Slice 2:
Groups read-only config descriptor domain, and Phase 3, Slice 1: read-only
Admin Panel config browser skeleton, and Phase 2, Slice 3: Economy read-only
config descriptor domain, and Phase 2, Slice 4: Worlds read-only config
descriptor domain, and Phase 2, Slice 5: Portals read-only config descriptor
domain, Phase 2, Slice 6: Offerings read-only config descriptor domain, and
Phase 2, Slice 7: Government read-only config descriptor domain, and Phase 2,
Slice 8: NPCs read-only config descriptor domain, and Phase 2, Slice 9: Quests
read-only config descriptor domain, and Phase 2, Slice 10: Realms protection
read-only config descriptor domain, and Phase 2, Slice 11: Mounts Collection
text read-only config descriptor domain, and Phase 2, Slice 12: Underworld
read-only config descriptor domain, Phase 2, Slice 13: Core-owned
Optimization/performance read-only config descriptor domain, Phase 2, Slice 14:
Core scalar read-only config descriptor expansion, and Phase 2, Slice 15: Core
Realm definition read-only descriptors, and Phase 2, Slice 16: Core title and
title-progression read-only descriptors, and Phase 2, Slice 17: Core reward
read-only descriptors, Phase 3, Slice 2: Admin Panel config browser contract
proposal, and Phase 3, Slice 3: dedicated read-only Admin Panel Config tab.
Phase 3, Slice 4 completed the typed config detail model proposal, and
Phase 3, Slice 5 added Config tab domain/category read-only rows. Phase 3,
Slice 6 completed the config mutation/readiness audit, Phase 3, Slice 7 added
Core config mutation contract records without write support, and Phase 3,
Slice 8 added a pure Core config mutation validator without write support.
Phase 3, Slice 9 completed the Admin Panel config validation preview action
proposal, and Phase 3, Slice 10 added Config entry rows plus a preview-only
`Validate Value` action without write support. Phase 3, Slice 11 completed
the dedicated config editing packet/model proposal. Phase 3, Slice 12 added
Core config edit model/payload records and codecs without registering
handlers. Phase 3, Slice 13 completed the config edit packet registration and
dispatch proposal. Phase 3, Slice 14 registered config edit payload types
without adding receivers. Phase 3, Slice 15 completed the validation-only
receiver/dispatch proposal. Phase 3, Slice 16 added the validation-only
config edit receiver and service dispatch. Phase 3, Slice 17 completed the
config edit client-result handling proposal. Phase 3, Slice 18 added the
client result receiver/cache. Phase 3, Slice 19 completed the config edit
open/detail UX proposal. Phase 3, Slice 20 added the config edit open-payload
client receiver/cache. Phase 3, Slice 21 completed the server open action
proposal, Phase 3, Slice 22 added the server `Open Editor` action and Config
row action, and Phase 3, Slice 23 completed the read-only detail shell
 proposal. Phase 3, Slice 24 added the read-only config edit detail shell.
Phase 3, Slice 25 completed the validation input/request proposal.
Phase 3, Slice 26 added validation input and request handling in the config
edit shell. Phase 3, Slice 27 completed the config edit Apply/readiness
proposal. Phase 3 V1 later added the first safe production Apply target for
`core:ui_theme:defaults.font-scale-percent`, including audit-backed
write/reload/rollback, strict client Apply gating, UI theme resync, and live
screenshot verification. Other config entries remain read-only or
validation-only until their owners register safe appliers. Phase 4 Slice 17C
captured the current Collection live baseline, and Phase 4 Slice 17D made
`Character Menu` the player-facing shell label plus `/charactermenu` alias while
preserving Collection internals and `/charactermenu` compatibility. Phase 4
Slice 17E added the Core-only Citizen Profile boundary with bounded
server-side visibility filtering and no addon contributors. Phase 4 Slice 17F
added unregistered bounded Citizen Profile request/snapshot payload records and
codec tests. Phase 4 Slice 17G registered the profile payloads and added the
server-authoritative request/response path plus client snapshot cache. Phase 4
Slice 17H added the Core-only Character Menu Profile tab surface and then
corrected it from a section-button layout into one composed civic profile
sheet after live review. Phase 4 Slice 17Q completed a focused civic UI polish
pass: shared button text now centers through `ElarionCivicUi`, Government
header titles render larger/lower, Shrine reward slots center inside their
bounded panel, generic semantic icons no longer use portrait art, Character
Creation now continues into Realm placement before teleporting, Realm placement
confirmation uses `CharacterRealmAssignmentConfirmPayload`, Character Menu opens
Profile by default, and Sci-Fi Bike preview calibration is bounded by tests.
Phase 4 Slice 19 added conversation-first NPC services and a dedicated bank
presentation: bankers now open normal NPC conversation first, then `Open Bank`
transitions the same validated session into `NpcBankScreen`. Live QA for the
conversation -> bank -> back flow passed under
`build/ui-qa/slice-19-npc-bank/`. Phase 4 Slice 20 added a non-mutating
dedicated `NpcTradeScreen` shell for `presentation: trade`; prompts/actions
inside trade nodes remain rejected until the real trade owner boundary exists.
The latest follow-up polished the banker/trader visuals: dedicated service NPC
skins, curated 32x32 portraits, Sigil currency icons across bank/trade money
surfaces, aligned bank quick amount buttons, a blinking amount caret, and
display-only trader catalog rows. Phase 4 Slice 23D then enabled
server-authoritative unlimited BUY purchases through NPC purchase journals and
Economy physical-Sigil settlement. Phase 4 Slice 23E added finite placed-NPC
  stock, lazy restock, and row stock labels for BUY offers. Later Slice 23M
  enabled server-authoritative Sell/buyback, and Slice 24 added explicit
  placed-NPC destination stock routing. The latest bank QA slice added a
  dev-run-only 2.5% withdrawal tax for visual testing and confirmed
  `:addons:economy:test :addons:npcs:test`, but the live taxed Withdraw
  screenshot is blocked by NPC dialogue Back/close activation reliability; see
  `build/ui-qa/slice-35-bank-quote/` and `docs/systems/UI_JOURNAL.md`.
  Follow-up cleanup removed the obsolete `dunk_banker` active config/default
  references after the PNG was deleted: the renderer fallback now uses
  `worldheart_banker.png`, active dev-run NPC visual profiles are only
  banker/trader, and the dev-run banker dialogue now uses the explicit current
  `Open Bank` -> `presentation: bank` node shape. A quick server restart after
  the cleanup reported `Loaded 2 NPC definitions, 2 skin profiles, 2 portrait
  profiles, 2 dialogues, and 1 trade catalogs`. The next no-QA slice added
  focused regression tests preventing generated service NPC defaults from
  reintroducing `dunk_banker` and requiring the generated banker dialogue to
  keep the explicit bank presentation route. The latest no-QA slice fixed the
  NPC dialogue screen so it respects the server-authored
  `typing-click-completes` payload field instead of always completing typed
  phases on click/Enter/Space, with focused controller coverage for
  typing-disabled and blank-text behavior. The newest no-QA slice added
  OP-only `/e npc open <npcId>` and changed `dev/tools/npc-trade-qa.ps1` to use
  that command for banker/trader root conversations before clicking the normal
  server-authored service option. The helper now has `open-bank` and
  `capture-bank` actions for the next bank quote screenshot pass. Manual QA
  then verified deposit/withdraw quotes, Back/ESC flow, item tooltips, stock
  decrement, and skins; the latest polish pass fixed the remaining portrait
  gutter, bank Fee/Total spacing, bank balance header placement, trader
  stock/range/quantity/totals alignment, row Sigil size, and Nether/End ticket
  tooltip wording. Phase 4 Slice 44 then polished Admin Panel config edit and
  action-overflow geometry with focused Core layout coverage.

Slice reports:

- `docs/reports/PROJECT_REVAMP_AUDIT.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`

Decisions confirmed for the long-term plan:

- Config/Admin comes before UI/Profile/Chronicle implementation.
- Existing config formats are preserved initially.
- Admin config starts as read-only discovery before editing.
- The civic brown/gold UI style is canonical for custom Elarion screens.
- Chronicle archive wording persists selected variant IDs.
- Profile visibility defaults conservative.
- Saves/config migrations must be live-safe.
- Shell addons stay in the build but must be labeled honestly.
- A dev-only UI gallery is part of the UI target after components stabilize.

Files changed in the 2026-07-05 slice:

- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigRegistry.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigDomain.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigCategory.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigEntry.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigCodec.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigValidator.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigPermission.java`
- `platform/core/src/main/java/panetina/elarion/core/config/CoreConfigDescriptors.java`
- `addons/groups/src/main/java/panetina/elarion/addons/groups/config/GroupConfigDescriptors.java`
- `addons/groups/src/main/java/panetina/elarion/addons/groups/ElarionGroupsAddon.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/config/EconomyConfigDescriptors.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/ElarionEconomyAddon.java`
- `addons/worlds/src/main/java/panetina/elarion/addons/worlds/config/WorldsConfigDescriptors.java`
- `addons/worlds/src/main/java/panetina/elarion/addons/worlds/ElarionWorldsAddon.java`
- `addons/portals/src/main/java/panetina/elarion/addons/portals/config/PortalConfigDescriptors.java`
- `addons/portals/src/main/java/panetina/elarion/addons/portals/ElarionPortalsAddon.java`
- `addons/offerings/src/main/java/panetina/elarion/addons/offerings/config/OfferingConfigDescriptors.java`
- `addons/offerings/src/main/java/panetina/elarion/addons/offerings/ElarionOfferingsAddon.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/config/GovernmentConfigDescriptors.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/ElarionGovernmentAddon.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfigDescriptors.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/ElarionNpcsAddon.java`
- `addons/quests/src/main/java/panetina/elarion/addons/quests/config/QuestConfigDescriptors.java`
- `addons/quests/src/main/java/panetina/elarion/addons/quests/ElarionQuestsAddon.java`
- `addons/realms/src/main/java/panetina/elarion/addons/realms/config/RealmConfigDescriptors.java`
- `addons/realms/src/main/java/panetina/elarion/addons/realms/config/RealmProtectionConfig.java`
- `addons/realms/src/main/java/panetina/elarion/addons/realms/ElarionRealmsAddon.java`
- `addons/mounts/src/main/java/panetina/elarion/addons/mounts/config/MountConfigDescriptors.java`
- `addons/mounts/src/main/java/panetina/elarion/addons/mounts/ElarionMountsAddon.java`
- `addons/underworld/src/main/java/panetina/elarion/addons/underworld/config/UnderworldConfigDescriptors.java`
- `addons/underworld/src/main/java/panetina/elarion/addons/underworld/ElarionUnderworldAddon.java`
- `addons/optimization/src/main/java/panetina/elarion/addons/optimization/PerformanceConfigDescriptors.java`
- `addons/optimization/src/main/java/panetina/elarion/addons/optimization/ElarionOptimizationAddon.java`
- `platform/core/src/main/java/panetina/elarion/core/api/ElarionApi.java`
- `platform/core/src/main/java/panetina/elarion/core/api/ElarionSystemApi.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`
- `platform/core/src/test/java/panetina/elarion/core/config/ElarionConfigRegistryTest.java`
- `addons/groups/src/test/java/panetina/elarion/addons/groups/config/GroupConfigDescriptorsTest.java`
- `addons/economy/src/test/java/panetina/elarion/addons/economy/config/EconomyConfigDescriptorsTest.java`
- `addons/worlds/src/test/java/panetina/elarion/addons/worlds/config/WorldsConfigDescriptorsTest.java`
- `addons/portals/src/test/java/panetina/elarion/addons/portals/config/PortalConfigDescriptorsTest.java`
- `addons/offerings/src/test/java/panetina/elarion/addons/offerings/config/OfferingConfigDescriptorsTest.java`
- `addons/government/src/test/java/panetina/elarion/addons/government/config/GovernmentConfigDescriptorsTest.java`
- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/config/NpcConfigDescriptorsTest.java`
- `addons/quests/src/test/java/panetina/elarion/addons/quests/config/QuestConfigDescriptorsTest.java`
- `addons/realms/src/test/java/panetina/elarion/addons/realms/config/RealmConfigDescriptorsTest.java`
- `addons/mounts/src/test/java/panetina/elarion/addons/mounts/config/MountConfigDescriptorsTest.java`
- `addons/underworld/src/test/java/panetina/elarion/addons/underworld/config/UnderworldConfigDescriptorsTest.java`
- `addons/optimization/src/test/java/panetina/elarion/addons/optimization/PerformanceConfigDescriptorsTest.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`
- `docs/config.md`
- `docs/addons/core.md`
- `docs/addons/economy.md`
- `docs/addons/groups.md`
- `docs/addons/portals.md`
- `docs/addons/offerings.md`
- `docs/addons/government.md`
- `docs/addons/npcs.md`
- `docs/addons/quests.md`
- `docs/addons/realms.md`
- `docs/addons/mounts.md`
- `docs/addons/underworld.md`
- `docs/addons/optimization.md`
- `wiki/admin/performance.md`
- `wiki/admin/npcs.md`
- `wiki/admin/quests.md`
- `wiki/admin/README.md`
- `docs/addons/worlds.md`
- `docs/systems/GUI.md`
- `AGENTS.md`
- `CODEX.md`
- `docs/reports/PROJECT_REVAMP_AUDIT.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `INDEX.md`
- `PLAN.md`
- `TODO.md`
- `OPTIMIZATION_TRACKER.md`
- `docs/ai/CURRENT_STATUS.md`

Tests/builds run in this slice:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigRegistryTest`
- `.\gradlew.bat :platform:core:test`
- `.\gradlew.bat :addons:groups:test --tests panetina.elarion.addons.groups.config.GroupConfigDescriptorsTest`
- `.\gradlew.bat :addons:groups:test`
- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest`
- `.\gradlew.bat :platform:core:test :addons:groups:test`
- `.\gradlew.bat :addons:economy:test --tests panetina.elarion.addons.economy.config.EconomyConfigDescriptorsTest`
- `.\gradlew.bat :addons:economy:test :platform:core:test`
- `.\gradlew.bat :addons:worlds:test --tests panetina.elarion.addons.worlds.config.WorldsConfigDescriptorsTest`
- `.\gradlew.bat :addons:worlds:test :platform:core:test`
- `.\gradlew.bat :addons:portals:test --tests panetina.elarion.addons.portals.config.PortalConfigDescriptorsTest`
- `.\gradlew.bat :addons:portals:test :platform:core:test`
- `.\gradlew.bat :addons:offerings:test --tests panetina.elarion.addons.offerings.config.OfferingConfigDescriptorsTest`
- `.\gradlew.bat :addons:offerings:test :platform:core:test`
- `.\gradlew.bat :addons:government:test --tests panetina.elarion.addons.government.config.GovernmentConfigDescriptorsTest`
- `.\gradlew.bat :addons:government:test :platform:core:test`
- `.\gradlew.bat :addons:npcs:test --tests panetina.elarion.addons.npcs.config.NpcConfigDescriptorsTest`
- `.\gradlew.bat :addons:npcs:test :platform:core:test`
- `.\gradlew.bat :addons:quests:test --tests panetina.elarion.addons.quests.config.QuestConfigDescriptorsTest`
- `.\gradlew.bat :addons:quests:test :platform:core:test`
- `.\gradlew.bat :addons:realms:test --tests panetina.elarion.addons.realms.config.RealmConfigDescriptorsTest`
- `.\gradlew.bat :addons:realms:test :platform:core:test`
- `.\gradlew.bat :addons:mounts:test --tests panetina.elarion.addons.mounts.config.MountConfigDescriptorsTest`
- `.\gradlew.bat :addons:mounts:test :platform:core:test`
- `.\gradlew.bat :addons:underworld:test --tests panetina.elarion.addons.underworld.config.UnderworldConfigDescriptorsTest`
- `.\gradlew.bat :addons:underworld:test :platform:core:test`
- `.\gradlew.bat :addons:optimization:test --tests panetina.elarion.addons.optimization.PerformanceConfigDescriptorsTest`
- `.\gradlew.bat :addons:optimization:test :platform:core:test`
- No GameTest or broad build was run.

Key Config/Admin findings and current status:

- Core has the strongest current config validation/reload rollback behavior;
  `/e reload` is Core-only and resyncs identity/theme snapshots on success.
- Addon config reloads are domain-specific and uneven. Worlds restores prior
  config on failure; Groups, NPCs, Quests, and Portals mostly assign after
  successful loads; Economy, Government, and Offerings have partial-apply
  risks; Underworld and Mounts fall back to defaults on malformed config.
- `AddonConfigFiles` only writes defaults. Core now has a read-only
  `ElarionConfigRegistry` exposed through `ElarionApi.system().configs()`;
  `core`, `groups`, `economy`, `worlds`, `portals`, `offerings`, and
  `government`, `npcs`, `quests`, `realms`, `mounts`, `underworld`, and
  `optimization` are registered domains.
- Admin Panel rows/actions are bounded and server-authoritative, but the model
  is operational-card oriented. It now displays read-only config summaries in
  a dedicated Config tab with domain/category/entry rows and a preview-only
  entry validation action, but not typed controls or multi-field config editing
  without a richer contract.
- Slice 11 decided that true editing must use dedicated Core-owned config edit
  models/packets rather than expanding generic Admin Panel actions. Descriptor
  visibility does not imply editability; editable domains must opt in later
  with reload-safe appliers/edit providers.
- Slice 12 added config edit target/control models and open/request/result
  payload codecs only. No config edit payload receivers are registered yet.
- Slice 13 decided the next safe implementation is payload type registration
  only: config edit open/result as S2C and config edit request as C2S. Receiver
  registration and UI remain deferred.
- Slice 14 registered the three config edit payload types in `ElarionCoreMod`.
- Slice 15 decided the first receiver should be validation-only, delegated to
  Core Admin/config dispatch, with `APPLY` returning unsupported until
  editable-domain appliers exist.
- Slice 16 registered the C2S receiver for `ElarionConfigEditRequestPayload`
  and delegated validation to `ElarionAdminPanelService`. The server now sends
  `ElarionConfigEditResultPayload` and refreshes the Admin Panel Config tab
  message. `APPLY` is still unsupported, all results have `canApply=false`, and
  there is still no client edit UI.
- Slice 17 decided to use a lightweight client receiver/cache before building
  typed edit UI.
- Slice 18 registered the client receiver for `ElarionConfigEditResultPayload`
  and stores the last structured result in `ElarionConfigEditClientState`,
  clearing it on join/disconnect.
- Slice 19 decided that config edit detail should be a dedicated state opened
  from a Config entry row by a server-authored `ElarionConfigEditOpenPayload`,
  not another generic text-input modal.
- Slice 20 registered the client receiver for `ElarionConfigEditOpenPayload`
  and stores the current server-authored `ElarionConfigEditControl` in
  `ElarionConfigEditClientState`. Opening a new control clears stale
  validation result state.
- Slice 21 decided to reuse the existing OP-gated Admin Panel action path for
  opening config edit details.
- Slice 22 added `Open Editor` on Config entry rows and server-side resolution
  of `config-entry|domain|category|entry` into a disabled
  `ElarionConfigEditOpenPayload`.
- Slice 23 decided the first rendered config edit detail should be a
  read-only modal/detail shell over `ElarionAdminPanelScreen`, with Close and
  disabled Validate/Apply affordances only.
- Slice 24 rendered that read-only shell and added close-only open-control
  clearing.
- Slice 25 decided that the next validation-capable shell should use local
  proposed-value input state, send `ElarionConfigEditRequestPayload` with
  `VALIDATE`, render only target-matched results, and keep Apply disabled.
- Slice 26 implemented that validation-capable shell.
- Slice 27 established that descriptor visibility and successful validation do
  not imply editability; editable domains must explicitly register safe
  appliers with validation re-checks, persistence ownership, rollback,
  audit/history events, and post-apply sync behavior.
- The first production applier now covers only
  `core:ui_theme:defaults.font-scale-percent`. Live QA verified apply from
  `100` to `125`, UI reflow/theme sync, restore to `100`, and disabled Apply
  for a non-applier entry.

Phase 2 Slice 1 implementation:

- Added read-only descriptor contracts under `platform/core/.../config`:
  registry, domain, category, entry, codec, validator, and permission.
- Registered a `core` domain through `CoreConfigDescriptors`, covering selected
  `ui_theme.yml` and `server_identity.yml` values from the current
  `CoreConfigManager` snapshot.
- Exposed the registry through `ElarionApi.system().configs()`.
- Deferred mutation, Admin Panel rendering, addon migration, reload
  orchestration, packets, and persistence.

Phase 2 Slice 2 implementation:

- Added `GroupConfigDescriptors` under `addons/groups/.../config`.
- Registered the `groups` domain from `ElarionGroupsAddon` through
  `api.system().configs()`.
- Descriptors expose current values from `GroupService.config()` and do not
  parse `groups.yml` during discovery.
- Deferred Admin Panel rendering, config writes, reload orchestration, packets,
  and persistence.

Phase 3 Slice 1 implementation:

- Added read-only config-domain rows to the Admin Panel Systems tab through
  `ElarionAdminPanelService`.
- Superseded by Phase 3 Slice 3: config-domain rows now live in a dedicated
  Config tab.
- Rows are built from the current registered config descriptors and show domain
  owner, source files, reload command, categories, entries, current/default
  values, bounds, choices, and reload/restart markers.
- Reused the existing row/detail packet model. No config actions, typed edit
  controls, reload orchestration, packet schema changes, file format changes,
  or persistence changes were added.

Phase 2 Slice 3 implementation:

- Added `EconomyConfigDescriptors` under `addons/economy/.../config`.
- Registered the `economy` domain from `ElarionEconomyAddon` through
  `api.system().configs()`.
- Descriptors expose current values from `EconomyTransactionService.config()`
  and `EconomyPricingService.definitions()` and do not parse config files
  during discovery.
- The domain covers persistence, query, Governor, and service-price metadata.
- Deferred Economy reload atomicity fixes, config writes, packet schema
  changes, file format changes, and persistence changes.

Phase 2 Slice 4 implementation:

- Added `WorldsConfigDescriptors` under `addons/worlds/.../config`.
- Registered the `worlds` domain from `ElarionWorldsAddon` through
  `api.system().configs()`.
- Descriptors expose current values from `WorldsConfigManager` and do not parse
  config files during discovery.
- The domain covers schema, lobby routing, current world keys/counts, and
  per-world identity/type/rule summaries.
- Deferred world storage changes, managed-world behavior changes, reload
  semantic changes, config writes, packet schema changes, file format changes,
  and persistence changes.

Phase 2 Slice 5 implementation:

- Added `PortalConfigDescriptors` under `addons/portals/.../config`.
- Registered the `portals` domain from `ElarionPortalsAddon` through
  `api.system().configs()`.
- Descriptors expose current values from `PortalDefinitionService` route and
  UI snapshots and do not parse config files during discovery.
- The domain covers route IDs, modes, source/destination dimensions, Economy
  price keys, schedule settings, visual settings, and prompt UI sizing.
- Deferred Portal travel behavior changes, schedule evaluation changes, Economy
  price integration changes, config writes, packet schema changes, file format
  changes, and persistence changes.

Phase 2 Slice 6 implementation:

- Added `OfferingConfigDescriptors` under `addons/offerings/.../config`.
- Registered the `offerings` domain from `ElarionOfferingsAddon` through
  `api.system().configs()`.
- Descriptors expose current values from `OfferingDefinitionService` project
  and UI snapshots and do not parse config files during discovery.
- The domain covers reserved society metadata, Shrine UI settings, project IDs,
  scopes, repeatability flags, requirement/milestone/level counts, and
  presentation fields.
- Deferred Offering donation/progression behavior changes, reward or Shrine
  block changes, reload semantic changes, config writes, packet schema changes,
  file format changes, and persistence changes.

Phase 2 Slice 7 implementation:

- Added `GovernmentConfigDescriptors` under `addons/government/.../config`.
- Registered the `government` domain from `ElarionGovernmentAddon` through
  `api.system().configs()`.
- Descriptors expose current values from `GovernmentDefinitionService` settings
  and form snapshots and do not parse config files during discovery.
- The domain covers authority cleanup timing, form IDs, display metadata,
  authority offices, office counts/holder limits, action groups, and
  transitions.
- Deferred Government voting, office, authority, form-loading, reload semantic
  changes, config writes, packet schema changes, file format changes, and
  persistence changes.

Phase 2 Slice 8 implementation:

- Added `NpcConfigDescriptors` under `addons/npcs/.../config`.
- Registered the `npcs` domain from `ElarionNpcsAddon` after the first
  successful server definition load.
- Descriptors expose supplier-backed NPC definitions, skin and portrait
  profiles, dialogue graph summaries, and dialogue UI settings without parsing
  files during Admin Panel discovery.
- Dynamic rows are fixed to IDs present at registration. Current values remain
  supplier-backed.
- Deferred NPC placement/runtime state, dialogue session, condition/action,
  reload semantic, config write, packet, file format, and persistence changes.
- Decimal NPC ranges remain read-only string descriptors until Core gains a
  decimal codec.

Phase 2 Slice 9 implementation:

- Added `QuestConfigDescriptors` under `addons/quests/.../config`.
- Registered the `quests` domain from `ElarionQuestsAddon` immediately after
  the existing validated definition load.
- Descriptors expose supplier-backed package identity, scope, root stage,
  version/tags, actor, variable, stage/edge, evidence, ending/Shrine projection,
  reusable condition/consequence, authoring, and metadata summaries.
- Dynamic questline rows are fixed to IDs present at registration. Current
  values remain backed by the atomic `QuestDefinitionService` snapshot.
- Deferred Quest runtime state, progression, action/condition, scheduled
  consequence, notification, reload semantic, config write, packet, file
  format, and persistence changes.

Phase 2 Slice 10 implementation:

- Added `RealmConfigDescriptors` under `addons/realms/.../config`.
- Registered the `realms` domain from `ElarionRealmsAddon` using the same
  loaded `RealmProtectionConfig` snapshot passed to the protection service.
- Added and reused `RealmProtectionConfig.defaults()` so loader fallback and
  descriptor defaults cannot drift.
- Descriptors expose shared worlds, OP bypass, explosion block protection,
  feedback cooldown, and extra mechanism/container block IDs.
- All entries are restart-required and non-runtime-reloadable. Protection
  behavior, config loading semantics, packets, file formats, and persistence
  were not changed.

Phase 2 Slice 11 implementation:

- Added `MountConfigDescriptors` under `addons/mounts/.../config`.
- Registered the `mounts` domain from `ElarionMountsAddon` using the same
  loaded `MountCollectionTextConfig` snapshot consumed by Collection rows.
- Descriptors expose a bounded mount count/ID summary and four presentation
  fields for every registered `ElarionMountType`.
- All entries are restart-required and non-runtime-reloadable. Collection
  rendering, mount runtime state, config loading, packets, file formats, and
  persistence were not changed.

Phase 2 Slice 12 implementation:

- Added `UnderworldConfigDescriptors` under `addons/underworld/.../config`.
- Registered the `underworld` domain from `ElarionUnderworldAddon` through
  `UnderworldService.config()`.
- Descriptors expose 33 settings across Underworld, corpse, PvP loot,
  combat-tag, and Soul Fracture categories.
- Suppliers follow `/e death reload`; Admin Panel discovery does not parse
  YAML. Decimal fields remain validated read-only strings.
- Death/corpse/PvP loot/combat-tag/Soul Fracture behavior, config loading,
  packets, files, and persistence were not changed.

Phase 2 Slice 13 implementation:

- Added `PerformanceConfigDescriptors` under `addons/optimization`.
- Registered the `optimization` domain with `platform:core` ownership because
  Core loads and owns task settings before addon initialization.
- Descriptors expose 16 host, task-budget, and monitoring settings from
  `ElarionTaskService.snapshot()` while excluding live counters and ignored
  compatibility metadata.
- All entries are restart-required and non-runtime-reloadable. Worker, queue,
  budget, monitoring, config loading, file, and runtime behavior were not
  changed.

Descriptor coverage status:

- Every addon config file currently parsed into an addon runtime model now has
  a registered read-only descriptor domain.
- The Core domain now exposes UI theme, server identity, Realm definitions,
  title definitions, title-progression definitions, reward definitions,
  citizen/activity, chat, identity/nickname, and history settings.
- Jail and Security generate YAML placeholders but do not parse runtime config;
  truthful domains require typed loaders first. Core abilities are also
  generated defaults rather than a typed runtime snapshot.

Phase 2 Slice 14 implementation:

- Expanded `CoreConfigDescriptors` with 41 scalar entries across citizens,
  chat, identity, and history categories.
- Added five Core scalar source files to the domain metadata.
- Current values remain supplier-backed through existing `CoreConfigManager`
  getters and update after successful `/e reload` operations.
- Core load/reload behavior, files, commands, persistence, and runtime behavior
  were not changed.

Phase 2 Slice 15 implementation:

- Added `realms.yml` to the existing `core` domain metadata.
- Added Core Realm count/ID summary descriptors and per-Realm presentation,
  visibility, spawn, and flag descriptors backed by
  `CoreConfigManager.realms()`.
- Dynamic Realm rows are fixed to IDs present at descriptor registration;
  current values for those rows remain supplier-backed after successful
  `/e reload` operations.
- Decimal spawn values are represented as read-only string descriptors until
  Core gains a decimal config descriptor codec.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigRegistryTest`
  and `.\gradlew.bat :platform:core:test`.
- Realm runtime membership/governance, config loading, files, commands,
  persistence, and gameplay behavior were not changed.

Phase 2 Slice 16 implementation:

- Added `titles.yml` and `title-progression.yml` to the existing `core` domain
  metadata.
- Added Core title count/ID summary descriptors and per-title presentation,
  priority, visibility, acquisition, ownership, ability, and active-effect
  summaries backed by `CoreConfigManager.titles()`.
- Added title progression region and unlock-rule summaries backed by
  `CoreConfigManager.progressionRegions()` and
  `CoreConfigManager.titleUnlockRules()`.
- Dynamic title, region, and rule rows are fixed to IDs present at descriptor
  registration; current values for those rows remain supplier-backed after
  successful `/e reload` operations.
- Decimal progression-region coordinates are represented as read-only string
  descriptors until Core gains a decimal config descriptor codec.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigRegistryTest`
  and `.\gradlew.bat :platform:core:test`.
- Title ownership/activation, ability ownership, config loading, files,
  commands, persistence, and gameplay behavior were not changed.

Phase 2 Slice 17 implementation:

- Added `rewards.yml` to the existing `core` domain metadata.
- Added Core reward count/ID summary descriptors plus per-reward action count,
  ordered action type, action type, and action parameter descriptors backed by
  `CoreConfigManager.rewards()`.
- Dynamic reward rows are fixed to IDs and action indexes present at descriptor
  registration; current values for those rows remain supplier-backed after
  successful `/e reload` operations.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigRegistryTest`
  and `.\gradlew.bat :platform:core:test`.
- Reward execution, claim state, config loading, files, commands, persistence,
  packets, and gameplay behavior were not changed.

Phase 3 Slice 2 implementation:

- Completed as a documentation/proposal slice.
- Inspected `ElarionAdminPanelService`, Admin Panel snapshot/tab/row records,
  `ElarionAdminPanelScreen`, and `ElarionAdminPanelServiceTest`.
- Decision: next implementation should add a dedicated read-only `configs`
  Admin Panel tab using the existing snapshot/tab/row packet model.
- Config-domain rows should move out of Systems into the new Config tab.
- The current fixed-width tab layout must be adjusted so six tabs fit inside
  the panel.
- Page/category provider contracts, config writes, typed editing, reload
  orchestration, packet schema changes, persistence, commands, and gameplay
  behavior remain deferred.

Phase 3 Slice 3 implementation:

- Added a dedicated `configs` Admin Panel tab using the existing
  snapshot/tab/row packet model.
- Moved config-domain rows out of Systems; Systems now remains provider-owned
  testing and repair rows.
- Adjusted Admin Panel tab layout/hitboxes to compute width from tab count so
  six tabs fit inside the existing panel.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest --tests panetina.elarion.core.client.ElarionAdminPanelScreenLayoutTest`
  and `.\gradlew.bat :platform:core:test`.
- Config writes, typed editing, reload orchestration, page/category provider
  contracts, packet schema changes, persistence, commands, and gameplay
  behavior were not changed.

Phase 3 Slice 4 implementation:

- Completed as a documentation/proposal slice.
- Inspected current Admin Panel row/body/payload limits.
- Decision: do not add a new Admin Panel packet schema or page/category
  provider contract yet.
- Next implementation should add domain summary rows plus per-category
  read-only rows in the existing Config tab using the current snapshot/tab/row
  packet model.
- True typed controls, config writes, reload orchestration, packet schema
  changes, persistence, commands, and gameplay behavior remain deferred.

Phase 3 Slice 5 implementation:

- Expanded `ElarionAdminPanelService.configRows(...)` from one row per domain
  to one domain summary row plus one category detail row per category.
- Domain rows summarize owner, files, reload command, category count, entry
  count, reloadable/restart-required/invalid counts, and category summaries.
- Category rows use stable `config:<domain>:category:<category>` IDs and keep
  entry details scoped to one category to reduce body truncation.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest`
  and `.\gradlew.bat :platform:core:test`.
- Config writes, typed editing, reload orchestration, page/category provider
  contracts, packet schema changes, persistence, commands, and gameplay
  behavior were not changed.

## Phase 3 Slice 6 Handoff

Objective:

- Audit Admin Panel config mutation readiness without changing production
  behavior.

Inspected:

- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigEntry.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigRegistry.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigDomain.java`
- `platform/core/src/main/java/panetina/elarion/core/network/AdminPanelActionPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ElarionAdminPanelAction.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`
- Current reload/load findings in `docs/reports/CONFIG_ADMIN_AUDIT.md`

Decisions:

- Do not enable config editing yet.
- Do not add Admin Panel edit actions to Config rows yet.
- Do not use generic provider actions as the long-term config mutation API.
- Add typed Core config change request/result/error records before any write
  support.

Verified facts:

- `ElarionConfigEntry` exposes validators, current/default display values,
  bounds, reload/restart markers, read permission, and write permission
  metadata.
- `ElarionConfigRegistry` is read-only discovery and has no apply/write path.
- `AdminPanelActionPayload` carries generic string parameters; it has no
  config-entry type, validation-result shape, old/new value reporting, reload
  policy, or audit hook.
- Config tab rows remain server-authored read-only rows.
- Addon reload/apply behavior is not uniform enough for global editing.

Files changed:

- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/config.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Tests:

- Not run; this was a documentation/proposal slice.

Unresolved risks:

- Future mutation contracts become public Core API, so fields should stay
  intentionally small and stable.
- Addon-specific file writes and reload rollback remain unimplemented and must
  be approved separately.

Completed follow-up:

- Phase 3, Slice 7: Core config mutation contract records, no writes.

## Phase 3 Slice 7 Handoff

Objective:

- Add Core config mutation contract records without enabling config writes.

Files changed:

- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigChangeRequest.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigChangeError.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigChangeResult.java`
- `platform/core/src/test/java/panetina/elarion/core/config/ElarionConfigChangeContractTest.java`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/config.md`
- `docs/addons/core.md`
- `INDEX.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Implementation:

- `ElarionConfigChangeRequest` normalizes domain/category/entry IDs, preserves
  raw submitted values for future typed parsing, carries optional
  expected-current value, actor UUID, and reason, and exposes a stable target
  key.
- `ElarionConfigChangeError` defines stable error codes for unknown targets,
  permission denial, stale values, parse/validation failures, unsupported
  edits, reload/restart gating, apply failures, and internal failures.
- `ElarionConfigChangeResult` models `VALIDATED`, `APPLIED`, and `REJECTED`
  states with old/new display values, reload/restart flags, audit event type,
  and immutable error lists.

Tests:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigChangeContractTest`.
- Passed `.\gradlew.bat :platform:core:test`.

Deferred:

- File writes.
- Apply services.
- Reload orchestration and rollback.
- Config mutation packets.
- Admin Panel edit actions.
- Audit-history emission.
- Addon-specific editable-domain opt-in.
- Persistence, commands, and gameplay behavior.

Completed follow-up:

- Phase 3, Slice 8: Core config mutation validation service, no writes.

## Phase 3 Slice 8 Handoff

Objective:

- Add a pure Core config mutation validation service without enabling config
  writes.

Files changed:

- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigChangeValidator.java`
- `platform/core/src/test/java/panetina/elarion/core/config/ElarionConfigChangeValidatorTest.java`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/config.md`
- `docs/addons/core.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Implementation:

- `ElarionConfigChangeValidator.validate(registry, request, actorPermission)`
  resolves domain/category/entry IDs against the descriptor registry.
- Null actor permission is treated as `PUBLIC`.
- Write permission metadata is checked before parsing.
- Nonblank `expectedCurrentValue` is compared to the descriptor current display
  value to detect stale requests.
- Proposed raw values are parsed through the entry codec and then checked with
  the entry validator.
- Successful checks return `ElarionConfigChangeResult.validated` with old/new
  display values and reload/restart flags.
- Unknown targets, permission denial, stale values, parse failures, validation
  failures, current-value supplier failures, and validator failures return
  structured rejected results.

Tests:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigChangeValidatorTest`.
- Passed `.\gradlew.bat :platform:core:test`.

Deferred:

- File writes.
- Config apply services.
- Reload orchestration and rollback.
- Config mutation packets.
- Admin Panel edit controls.
- Audit-history emission.
- Addon-specific editable-domain opt-in.
- Persistence, commands, and gameplay behavior.

Completed follow-up:

- Phase 3, Slice 9: Admin Panel config validation preview action proposal.

## Phase 3 Slice 9 Handoff

Objective:

- Audit whether Admin Panel config validation preview should reuse the existing
  generic action payload or require a dedicated config packet/model before UI
  work.

Inspected:

- `platform/core/src/main/java/panetina/elarion/core/network/AdminPanelActionPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/network/AdminPanelOpenPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ElarionAdminPanelAction.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ElarionAdminPanelRow.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`
- `platform/core/src/test/java/panetina/elarion/core/network/AdminPanelPayloadTest.java`

Decisions:

- Do not add a dedicated packet/model yet for read-only validation preview.
- Do not add validation actions to current category rows.
- Do not add edit/apply buttons.
- Reuse the existing generic Admin Panel action payload only for a narrow
  preview-only `Validate Value` action after Config gains stable entry-level
  rows.
- Add a dedicated config packet/model later before true editing, typed
  controls, diff display, audit preview, or apply/reload orchestration.

Verified facts:

- The generic action payload can carry a selected tab, provider, target row,
  action id, one or more string parameters, and confirmation state.
- The current screen supports one text input per action.
- Current Config rows are domain/category rows only; a category body can
  describe many entries and cannot safely identify one target entry.
- The server reopens the panel with a short message after actions, which is
  acceptable for validation preview but not rich editing state.

Files changed:

- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/config.md`
- `docs/addons/core.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Tests:

- Not run; this was a documentation/proposal slice.

Completed follow-up:

- Phase 3, Slice 10: Config tab entry rows and validation preview action.
- Add stable read-only entry rows under each Config category and a preview-only
  `Validate Value` action per entry row. The action should call
  `ElarionConfigChangeValidator` server-side and return a short result
  message.
- Exclude file writes, actual edit/apply controls, reload orchestration,
  persistence, commands, gameplay behavior, and dedicated packet/model changes.

## Phase 3 Slice 10 Handoff

Objective:

- Add stable Config tab entry rows and a preview-only validation action.

Files changed:

- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/config.md`
- `docs/addons/core.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Implementation:

- `ElarionAdminPanelService.configRows(...)` now emits one stable entry row
  per descriptor entry after each category row.
- Entry rows use IDs shaped as
  `config-entry|<domain>|<category>|<entry>` and expose one setting's path,
  description, current/default display values, type, bounds, choices,
  permissions, runtime marker, and current validation state. The pipe-delimited
  internal form avoids ambiguity because descriptor IDs may contain colons.
- Each entry row has a preview-only `Validate Value` input action. The action
  routes through the existing server-authoritative Admin Panel action path,
  calls `ElarionConfigChangeValidator`, and returns a short valid/invalid
  message.
- The preview intentionally does not pass `expectedCurrentValue`, does not
  write files, does not apply values, does not reload config, does not emit
  audit/history events, and does not change runtime state.
- Config actions now reopen the Config tab and preserve the selected entry
  row.

Tests:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest`.
- Passed `.\gradlew.bat :platform:core:test`.

Deferred:

- Manual in-client UI verification.
- Dedicated config edit packet/model.
- Typed config controls.
- Diff display and audit preview.
- File writes, apply services, reload orchestration, rollback, audit emission,
  persistence, commands, and gameplay behavior.

Completed follow-up:

- Phase 3, Slice 11: dedicated config editing packet/model proposal.
- Classification: MEDIUM.
- Specify the packet/model shape required before true config editing: typed
  controls, expected-current values, validation result payloads, diff display,
  audit preview, reload/restart policy, apply permissions, and
  server-authoritative failure states.

## Phase 3 Slice 11 Handoff

Objective:

- Specify the dedicated packet/model contract required before true Admin Panel
  config editing.

Files changed:

- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/config.md`
- `docs/addons/core.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Inspected:

- `platform/core/src/main/java/panetina/elarion/core/network/AdminPanelActionPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/network/AdminPanelOpenPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ElarionAdminPanelAction.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ElarionAdminPanelRow.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ElarionAdminPanelSnapshot.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ElarionAdminPanelTab.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigChangeRequest.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigChangeResult.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigChangeError.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigChangeValidator.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/config.md`
- `docs/addons/core.md`

Decisions:

- Do not evolve `AdminPanelActionPayload` into the long-term config editing
  protocol.
- Keep generic Admin Panel actions for provider operations and the current
  preview-only `Validate Value` action.
- Add a dedicated Core-owned config edit packet/model family before true
  editing.
- The future protocol must carry edit targets, typed control metadata,
  expected-current display values, proposed raw values, structured result
  errors, reload/restart policy, audit preview text, and server-authored
  failure states.
- Descriptor visibility does not imply editability. Each editable domain must
  later register an explicit reload-safe applier or edit provider.
- The first implementation should add records/codecs/tests only, with no
  receiver registration and no UI behavior.

Tests:

- Not run; this was a documentation/proposal slice.
- Lightweight verification was limited to targeted source inspection and
  Markdown search checks.

Deferred:

- Core edit packet/model Java records.
- Packet codec tests.
- Server receiver registration.
- UI controls.
- Config file writes.
- Apply services.
- Reload orchestration and rollback.
- Audit-history emission.
- Addon editable-domain opt-in.
- Persistence, commands, and gameplay behavior.

Completed follow-up:

- Phase 3, Slice 12: Core config edit packet/model records, no handlers.
- Classification: MEDIUM.
- Add Core model/network records and packet codecs for config edit targets,
  edit-control snapshots, edit requests, and edit results. Add focused codec
  round-trip and validation tests.
- Exclude server receiver registration, UI controls, file writes, apply
  services, reload orchestration, persistence, commands, and gameplay behavior.

## Phase 3 Slice 12 Handoff

Objective:

- Add Core config edit packet/model records and codecs without registering
  handlers or enabling editing.

Files changed:

- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigEditTarget.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigEditControl.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditPayloadCodecs.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditOpenPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditRequestPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditResultPayload.java`
- `platform/core/src/test/java/panetina/elarion/core/network/ElarionConfigEditPayloadTest.java`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/config.md`
- `docs/addons/core.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Implementation:

- `ElarionConfigEditTarget` normalizes domain/category/entry IDs and exposes a
  stable target key.
- `ElarionConfigEditControl` carries a server-authored typed control snapshot:
  target, label, description, path, value type, current/default display values,
  choices, bounds, reload/restart markers, read/write permissions, editable
  flag, and disabled reason.
- `ElarionConfigEditOpenPayload` carries one edit-control snapshot and a short
  message.
- `ElarionConfigEditRequestPayload` carries target, expected-current display
  value, proposed raw value, reason, and `VALIDATE`/`APPLY` intent.
- `ElarionConfigEditResultPayload` carries target, result status, old/new
  display values, reload/restart markers, `canApply`, audit preview,
  structured errors, and a short message.
- `ElarionConfigEditPayloadCodecs` centralizes bounded target/control/error
  serialization.

Tests:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.network.ElarionConfigEditPayloadTest`.
- Passed `.\gradlew.bat :platform:core:test`.

Deferred:

- Server receiver registration.
- UI edit controls.
- Config file writes.
- Config apply services.
- Reload orchestration and rollback.
- Audit-history emission.
- Addon editable-domain opt-in.
- Persistence, commands, and gameplay behavior.

Completed follow-up:

- Phase 3, Slice 13: config edit packet registration audit/proposal.
- Classification: MEDIUM.
- Decide where and how the new config edit payload IDs should be registered,
  how OP/admin permission checks should be enforced for validate/apply
  requests, what server service method should own dispatch, and how results
  should reopen or update the Config tab.
- Exclude UI controls, file writes, apply services, reload orchestration,
  persistence, commands, and gameplay behavior.

## Phase 3 Slice 13 Handoff

Objective:

- Audit and specify registration/dispatch for the new config edit packet
  contracts before adding live handlers.

Files changed:

- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/config.md`
- `docs/addons/core.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Inspected:

- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreClient.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditOpenPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditRequestPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditResultPayload.java`

Decisions:

- Config edit payload codecs should be registered in `ElarionCoreMod`, where
  Core registers its current play payload types.
- `ElarionConfigEditOpenPayload` is S2C.
- `ElarionConfigEditResultPayload` is S2C.
- `ElarionConfigEditRequestPayload` is C2S.
- The next implementation should register payload types only. Do not add
  server/client receivers in that slice.
- Future request handling should be OP-gated and delegated to a Core
  Admin/config service method, not implemented directly inside the receiver
  lambda.
- Future `APPLY` requests must return unsupported/domain-not-editable until a
  separately approved editable-domain applier registry exists.

Tests:

- Not run; this was a documentation/proposal slice.
- Lightweight verification was limited to targeted source inspection and
  Markdown search checks.

Deferred:

- Payload type registration implementation.
- Server receiver registration.
- Client receiver registration.
- UI edit controls.
- Config file writes.
- Config apply services.
- Reload orchestration and rollback.
- Audit-history emission.
- Addon editable-domain opt-in.
- Persistence, commands, and gameplay behavior.

Completed follow-up:

- Phase 3, Slice 14: register config edit payload types only.
- Classification: SMALL.
- Add `PayloadTypeRegistry.playS2C()` registrations for
  `ElarionConfigEditOpenPayload` and `ElarionConfigEditResultPayload`, plus
  `PayloadTypeRegistry.playC2S()` registration for
  `ElarionConfigEditRequestPayload`.
- Exclude server/client receiver registration, UI controls, file writes, apply
  services, reload orchestration, persistence, commands, and gameplay behavior.

## Phase 3 Slice 14 Handoff

Objective:

- Register config edit payload types only.

Files changed:

- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/config.md`
- `docs/addons/core.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Implementation:

- Registered `ElarionConfigEditOpenPayload` through
  `PayloadTypeRegistry.playS2C()`.
- Registered `ElarionConfigEditResultPayload` through
  `PayloadTypeRegistry.playS2C()`.
- Registered `ElarionConfigEditRequestPayload` through
  `PayloadTypeRegistry.playC2S()`.
- No `ServerPlayNetworking.registerGlobalReceiver(...)` or
  `ClientPlayNetworking.registerGlobalReceiver(...)` calls were added for
  these payloads.

Tests:

- Passed `.\gradlew.bat :platform:core:compileJava :platform:core:test --tests panetina.elarion.core.network.ElarionConfigEditPayloadTest`.
- Passed `.\gradlew.bat :platform:core:test`.

Deferred:

- Server receiver registration.
- Client receiver registration.
- UI edit controls.
- Config file writes.
- Config apply services.
- Reload orchestration and rollback.
- Audit-history emission.
- Addon editable-domain opt-in.
- Persistence, commands, and gameplay behavior.

Completed follow-up:

- Phase 3, Slice 15: config edit receiver/dispatch proposal.
- Classification: MEDIUM.
- Specify the minimal server receiver and service dispatch shape for
  validation-only requests: OP gating, target resolution, conversion to
  `ElarionConfigChangeRequest`, result payload conversion, and Config tab
  refresh behavior.
- Exclude implementation, UI controls, file writes, apply services, reload
  orchestration, persistence, commands, and gameplay behavior.

## Phase 3 Slice 15 Handoff

Objective:

- Specify the minimal receiver and service dispatch shape for validation-only
  config edit requests.

Files changed:

- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/config.md`
- `docs/addons/core.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Inspected:

- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditRequestPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditResultPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/config.md`
- `docs/addons/core.md`

Decisions:

- The first live config edit receiver should support validation only.
- The receiver should delegate to `ElarionAdminPanelService` or a narrow Core
  config edit coordinator.
- Receiver lambdas must not parse config, mutate state, or construct detailed
  validation behavior directly.
- `VALIDATE` should convert `ElarionConfigEditRequestPayload` into
  `ElarionConfigChangeRequest`, run `ElarionConfigChangeValidator`, and return
  `ElarionConfigEditResultPayload`.
- `APPLY` must return a rejected `UNSUPPORTED` result until an editable-domain
  applier registry is approved and implemented.
- Non-OP players must receive `PERMISSION_DENIED` without target-existence
  details.
- `canApply` remains false for every result until apply support exists.
- Validation preview must not emit audit/history events.

Tests:

- Not run; this was a documentation/proposal slice.
- Lightweight verification was limited to targeted source inspection and
  Markdown search checks.

Deferred:

- Validation-only receiver implementation.
- Result conversion helpers.
- Service tests for valid, invalid, stale, permission-denied, and
  apply-unsupported cases.
- UI edit controls.
- Config file writes.
- Config apply services.
- Reload orchestration and rollback.
- Audit-history emission.
- Addon editable-domain opt-in.
- Persistence, commands, and gameplay behavior.

Next recommended slice:

- Phase 3, Slice 16: validation-only config edit receiver and dispatch.
- Classification: MEDIUM.
- Add the server receiver for `ElarionConfigEditRequestPayload`, a
  validation-only dispatch method on `ElarionAdminPanelService`, result payload
  conversion helpers, and focused service tests for valid, invalid,
  permission-denied, stale, and apply-unsupported cases.
- Exclude UI controls, file writes, apply services, reload orchestration,
  persistence, commands, and gameplay behavior.

## Phase 3 Slice 16 Handoff

Slice name:

- Validation-only config edit receiver and dispatch.

Objective:

- Add the first live server handling path for dedicated config edit requests,
  limited to validation preview only.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditRequestPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditResultPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigEditTarget.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigChangeResult.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigChangeError.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`

Decisions made:

- Keep dispatch inside `ElarionAdminPanelService` instead of placing validation
  logic in the networking receiver.
- Use the existing `ElarionConfigChangeValidator` for packet-backed validation
  so generic row preview and future edit UI share one validation path.
- `APPLY` remains closed and returns `UNSUPPORTED` until editable-domain
  appliers, audit logging, and reload orchestration exist.
- All result payloads report `canApply=false` in this slice.
- The receiver refreshes the Admin Panel Config tab with the result message
  because no dedicated edit screen exists yet.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`
- `docs/config.md`
- `docs/addons/core.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `INDEX.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

APIs and packets affected:

- Added `ElarionAdminPanelService.validateConfigEdit`.
- Added package-visible `ElarionAdminPanelService.configEditResult` for focused
  service testing.
- Registered the live C2S receiver for `ElarionConfigEditRequestPayload`.
- Existing payload record shapes and codecs were not changed.

Config and persistence impact:

- No config keys were added or changed.
- No descriptor domains changed.
- No config files are written.
- No runtime config snapshots are mutated.
- No persistent formats changed.

Tests run:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest`
- `.\gradlew.bat :platform:core:test`

Unresolved risks:

- `ElarionConfigEditResultPayload` is sent by the server, but no dedicated
  client receiver or edit UI consumes it yet. The Admin Panel refresh message
  is the current visible feedback.
- True edit/apply support still needs explicit domain appliers, reload-safety
  rules, rollback behavior, and admin audit logging.

Questions requiring user input:

- None for the completed validation-only receiver.

Work deliberately deferred:

- Client result receiver.
- Typed edit controls.
- Dedicated config edit detail state.
- Config writes/apply services.
- Reload orchestration and rollback.
- Audit/history entries for applied admin changes.
- Editable-domain provider registration.

Precise recommended next slice:

- Phase 3, Slice 17: config edit client result handling proposal.
- Classification: SMALL.
- Decide whether the client should use a no-op receiver, lightweight screen
  cache, Admin Panel message-only flow, or dedicated edit detail state for
  `ElarionConfigEditResultPayload` before implementing real edit controls.
- Search terms: `ElarionConfigEditResultPayload`,
  `ClientPlayNetworking.registerGlobalReceiver`, `AdminPanelScreen`,
  `AdminPanelOpenPayload`, `ElarionAdminPanelService.validateConfigEdit`.

## Phase 3 Slice 17 Handoff

Slice name:

- Config edit client result handling proposal.

Objective:

- Decide the minimum client-side handling required for structured config edit
  results before adding real edit controls.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreClient.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditResultPayload.java`

Decisions made:

- Add a lightweight client receiver/state cache as the next implementation
  slice.
- Keep current visible feedback in the server-refreshed Admin Panel message.
- Defer dedicated edit/detail UI until the open/detail UX is specified.

Exact files changed:

- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- Later Slice 18 documentation updates also touched `PLAN.md`, `TODO.md`,
  `docs/config.md`, `docs/addons/core.md`, and this handoff.

Tests run:

- None for the proposal-only slice.

Precise recommended next slice:

- Phase 3, Slice 18: config edit client result receiver/cache.

## Phase 3 Slice 18 Handoff

Slice name:

- Config edit client result receiver/cache.

Objective:

- Consume `ElarionConfigEditResultPayload` on the client without adding edit UI
  or any config write path.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreClient.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditResultPayload.java`

Decisions made:

- Store the last structured result in passive client state for future UI use.
- Clear the cached result on join and disconnect.
- Do not render the result yet; the existing Admin Panel message remains the
  visible feedback.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreClient.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionConfigEditClientState.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionConfigEditClientStateTest.java`
- `docs/config.md`
- `docs/addons/core.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

APIs and packets affected:

- Added client-local `ElarionConfigEditClientState`.
- Registered client receiver for `ElarionConfigEditResultPayload`.
- No payload record shapes or codecs changed.

Config and persistence impact:

- No config keys were added or changed.
- No descriptors changed.
- No config files are written.
- No persistent formats changed.

Tests run:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionConfigEditClientStateTest`
- `.\gradlew.bat :platform:core:test`

Unresolved risks:

- There is still no typed config edit UI.
- The client cache is passive and currently only prepares for future UI state.
- `APPLY` remains unsupported server-side and must stay disabled until
  editable-domain appliers, reload safety, rollback, and audit logging exist.

Work deliberately deferred:

- Config edit open/detail UI.
- Typed controls.
- Result rendering.
- Apply/write services.
- Reload orchestration.
- Admin audit/history entries.
- Editable-domain provider registration.

Precise recommended next slice:

- Phase 3, Slice 19: config edit open/detail UX proposal.
- Classification: SMALL.
- Decide how an admin opens a typed edit/detail view from a Config entry row
  and how that UI should present current value, proposed value, validation
  errors, old/new diff, reload/restart policy, and disabled Apply state.
- Search terms: `ElarionAdminPanelScreen`, `ElarionConfigEditOpenPayload`,
  `ElarionConfigEditControl`, `ElarionConfigEditClientState`,
  `config-entry|`, `VALIDATE_CONFIG_VALUE_ACTION`.

## Phase 3 Slice 19 Handoff

Slice name:

- Config edit open/detail UX proposal.

Objective:

- Define how a future typed config edit/detail view should open and what it
  must display before adding UI code.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/model/ElarionAdminPanelAction.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ElarionAdminPanelRow.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditOpenPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigEditControl.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionConfigEditClientState.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreClient.java`

Decisions made:

- Config edit detail should be a dedicated state opened from a Config entry
  row.
- The generic Admin Panel action path can request opening the selected row
  because it carries no proposed value and does not mutate state.
- The server must resolve the target and send `ElarionConfigEditOpenPayload`;
  the client must not build edit controls from row body text.
- The future detail state should show label, description, path, current/default
  values, type, choices, bounds, reload/restart policy, read/write
  permissions, proposed value input, validation errors, old/new diff,
  Validate, and disabled Apply with disabled reason.
- `Apply` remains unavailable until editable-domain appliers exist and the
  server returns `canApply=true`.

Rejected options:

- Keep using the one-field generic modal as the long-term config edit UX.
- Add config writes before the open/detail state exists.
- Let the client infer editable metadata from visible row text.
- Add a dedicated C2S open request payload before the generic Admin Panel
  action path proves insufficient.

Exact files changed:

- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

APIs and packets affected:

- None. Proposal only.

Config and persistence impact:

- No config keys changed.
- No descriptors changed.
- No config files or persistence changed.

Tests run:

- None. Proposal-only slice.

Unresolved risks:

- No rendered edit/detail UI exists yet.
- No server open action exists yet.
- No config write/apply path exists yet.

Work deliberately deferred:

- Client open payload receiver/cache.
- Server open action.
- Admin Panel row action.
- Rendered edit/detail UI.
- Validation input controls.
- Apply services, reload orchestration, rollback, and audit logging.

Precise recommended next slice:

- Phase 3, Slice 20: config edit open payload receiver and client open-state
  cache.
- Classification: SMALL.
- Extend `ElarionConfigEditClientState` to store the current open control,
  register a client receiver for `ElarionConfigEditOpenPayload`, clear open
  state on join/disconnect, and add focused cache tests.
- Exclude server open actions, Admin Panel row actions, rendered edit UI,
  validation input controls, config writes, apply services, reload
  orchestration, descriptor changes, and addon appliers.
- Search terms: `ElarionConfigEditClientState`,
  `ElarionConfigEditOpenPayload`, `ElarionCoreClient`,
  `ElarionConfigEditControl`.

## Phase 3 Slice 20 Handoff

Slice name:

- Config edit open payload receiver and client open-state cache.

Objective:

- Consume `ElarionConfigEditOpenPayload` on the client by storing the current
  server-authored edit control.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionConfigEditClientState.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionConfigEditClientStateTest.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreClient.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigEditControl.java`

Decisions made:

- Store the open control alongside the last validation result in passive client
  state.
- Opening a new control clears `lastResult` so future UI does not display stale
  validation output for the wrong target.
- Reuse existing join/disconnect `clear()` behavior to clear both result and
  open-control state.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionConfigEditClientState.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreClient.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionConfigEditClientStateTest.java`
- `docs/config.md`
- `docs/addons/core.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

APIs and packets affected:

- Added client-local `open(ElarionConfigEditControl)` and `openControl()` on
  `ElarionConfigEditClientState`.
- Registered client receiver for `ElarionConfigEditOpenPayload`.
- No payload shapes or codecs changed.

Config and persistence impact:

- No config keys changed.
- No descriptors changed.
- No config files or persistence changed.

Tests run:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionConfigEditClientStateTest`
- `.\gradlew.bat :platform:core:test`

Unresolved risks:

- No server action sends `ElarionConfigEditOpenPayload` yet.
- No rendered edit/detail UI exists yet.
- `APPLY` remains unsupported.

Work deliberately deferred:

- Server open action.
- Admin Panel row action.
- Rendered edit/detail UI.
- Validation input controls.
- Config writes, apply services, reload orchestration, rollback, and audit
  logging.

Precise recommended next slice:

- Phase 3, Slice 21: config edit server open action proposal.
- Classification: SMALL.
- Decide the narrow server-side action that opens `ElarionConfigEditOpenPayload`
  from a selected Config entry row, including OP gating, target parsing,
  unknown-target fallback, disabled edit reason, selected-row refresh behavior,
  and tests.
- Search terms: `ElarionAdminPanelService`, `configEntryRowId`,
  `ConfigEntryTarget`, `ElarionConfigEditOpenPayload`,
  `ElarionConfigEditControl.fromEntry`, `coreAction`.

## Phase 3 Slice 21 Handoff

Slice name:

- Config edit server open action proposal.

Objective:

- Decide the server-side action path for opening a config edit detail from a
  Config entry row.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ElarionAdminPanelAction.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ElarionAdminPanelRow.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditOpenPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigEditControl.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`

Decisions made:

- Add a Core-owned `Open Editor` action to config entry rows.
- Reuse `AdminPanelActionPayload`; do not add another C2S open request yet.
- Server parses row IDs, resolves descriptors from `ElarionConfigRegistry`, and
  sends `ElarionConfigEditOpenPayload` only for valid targets.
- Send controls with `editable=false` until appliers exist.

Tests run:

- None. Proposal-only slice.

## Phase 3 Slice 22 Handoff

Slice name:

- Config edit server open action and Config row action.

Objective:

- Expose `Open Editor` on Config entry rows and send server-authored disabled
  edit controls for valid entry targets.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`
- `docs/config.md`
- `docs/addons/core.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Implementation facts:

- Added `OPEN_CONFIG_EDITOR_ACTION`.
- Config entry rows now include `Open Editor` and `Validate Value`.
- Added `configEditOpenControl` resolver.
- Successful open sends `ElarionConfigEditOpenPayload`.
- Unknown/stale targets fail with messages and do not send open payloads.
- Controls remain `editable=false` with
  `Config editing is not enabled yet.`

Tests run:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest`

Work deliberately deferred:

- Rendered edit UI.
- Text input.
- Validation submission from the edit detail.
- Apply/write services.
- Reload orchestration, rollback, and audit logging.

## Phase 3 Slice 23 Handoff

Slice name:

- Config edit read-only detail shell proposal.

Objective:

- Define the first rendered edit detail shell before adding UI code.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionConfigEditClientState.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigEditControl.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditResultPayload.java`

Decisions made:

- Render a presentation-only modal/detail shell over `ElarionAdminPanelScreen`.
- Source data from `ElarionConfigEditClientState.openControl()`.
- Include Close and disabled/non-interactive Validate/Apply affordances.
- Do not send validation requests or accept input in the first render slice.

Precise recommended next slice:

- Phase 3, Slice 24: config edit read-only detail shell.
- Classification: MEDIUM.

- Render the read-only shell, add Close behavior, keep Validate/Apply disabled,
  and add focused helper tests where practical.
- Search terms: `ElarionAdminPanelScreen`, `renderModal`,
  `ElarionConfigEditClientState.openControl`, `ElarionConfigEditControl`,
  `ElarionUiRenderer.compactButton`.

## Phase 3 Slice 24 Handoff

Slice name:

- Config edit read-only detail shell.

Objective:

- Render the current server-authored config edit control in the Admin Panel
  without adding input, validation submission, Apply, or writes.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionConfigEditClientState.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionConfigEditClientStateTest.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionUiRenderer.java`

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionConfigEditClientState.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionConfigEditClientStateTest.java`
- `docs/config.md`
- `docs/addons/core.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Implementation facts:

- Added read-only shell rendering in `ElarionAdminPanelScreen`.
- Shell displays label, description, path, current/default values, type,
  bounds, choices, runtime policy, permissions, disabled reason, Close, and
  disabled Validate/Apply affordances.
- Shell consumes mouse/typing/scroll while open.
- Escape closes the shell before closing the Admin Panel.
- Added `ElarionConfigEditClientState.closeOpenControl()`.

Tests run:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionConfigEditClientStateTest`
- `.\gradlew.bat :platform:core:test`

Work deliberately deferred:

- Proposed-value text input.
- Validation submission from the detail shell.
- Structured result rendering.
- Apply/write services.
- Reload orchestration and rollback.
- Admin audit/history entries.
- Editable-domain appliers.

Precise recommended next slice:

- Phase 3, Slice 25: config edit validation input proposal.
- Classification: SMALL.
- Decide proposed-value input state, focus/keyboard behavior, Validate button
  enablement, C2S validation request construction, stale-result clearing, and
  result display boundaries.
- Search terms: `ElarionAdminPanelScreen`, `ElarionTextInput`,
  `ElarionConfigEditRequestPayload`, `ElarionConfigEditClientState`,
  `renderConfigEditShell`, `handleConfigEditShellClick`.

## Phase 3 Slice 25 Handoff

Slice name:

- Config edit validation input proposal.

Objective:

- Define how the read-only config edit shell should send validation requests
  and display validation results without enabling Apply.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionTextInput.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditRequestPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionConfigEditClientState.java`

Decisions made:

- Proposed value input should be local to `ElarionAdminPanelScreen`.
- Input should seed from `ElarionConfigEditControl.currentDisplayValue()` when
  a control opens.
- Input should clear/discard when the open control closes or changes.
- Enter and Validate should send a `VALIDATE` request.
- The request should include target, expected current display value from the
  server-authored control, proposed raw value, reason
  `admin-panel-config-edit-preview`, and intent `VALIDATE`.
- Result display should only use `lastResult()` when its target matches the
  open control target.
- Any local input change after a result should clear or mark stale displayed
  result state.
- Apply remains disabled/non-interactive.

Rejected options:

- Reuse the generic `Validate Value` modal inside the edit shell.
- Enable Apply after successful validation.
- Store proposed-value input in global client state.
- Trust client-side parsing/validation for anything beyond simple blank or
  unchanged convenience states.

Exact files changed:

- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

APIs and packets affected:

- None. Proposal-only slice.

Tests run:

- None. Proposal-only slice.

Precise recommended next slice:

- Phase 3, Slice 26: config edit validation input and request.
- Classification: MEDIUM.

- Add proposed-value input state to `ElarionAdminPanelScreen`, focus and
  keyboard handling while the shell is open, Validate button enablement,
  `ElarionConfigEditRequestPayload` send with `VALIDATE`, and target-matched
  result rendering from `ElarionConfigEditClientState.lastResult()`.
- Exclude Apply, config writes, reload orchestration, descriptor changes, and
  addon appliers.
- Search terms: `ElarionAdminPanelScreen`, `ElarionTextInput`,
  `ElarionConfigEditRequestPayload`, `ElarionConfigEditClientState.update`,
  `ElarionConfigEditClientState.lastResult`, `renderConfigEditShell`.

## Phase 3 Slice 26 Handoff

Slice name:

- Config edit validation input and request.

Objective:

- Let admins validate a proposed config value from the config edit shell while
  keeping Apply and all write paths disabled.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionConfigEditClientState.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionTextInput.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditRequestPayload.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionConfigEditClientStateTest.java`

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionConfigEditClientState.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionConfigEditClientStateTest.java`
- `docs/config.md`
- `docs/addons/core.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Implementation facts:

- Proposed-value input is local to `ElarionAdminPanelScreen`.
- Input initializes from the open control current display value.
- Typing/Backspace edit the proposed value and clear stale validation results.
- Enter and Validate send `ElarionConfigEditRequestPayload` with intent
  `VALIDATE`.
- Result display uses only target-matched `ElarionConfigEditResultPayload`
  values from `ElarionConfigEditClientState.lastResult()`.
- Apply remains visible but disabled.

Tests run:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionConfigEditClientStateTest`
- `.\gradlew.bat :platform:core:test`

Work deliberately deferred:

- Apply enablement.
- Config writes.
- Reload orchestration and rollback.
- Admin audit/history entries.
- Editable-domain appliers.
- Descriptor changes.

Precise recommended next slice:

- Phase 3, Slice 27: config edit apply/readiness proposal.
- Classification: SMALL.
- Audit what must exist before Apply can ever become enabled, including
  editable-domain applier contracts, reload safety, rollback, audit/history
  entries, permissions, stale expected-current handling, persistence, and
  tests.
- Search terms: `ElarionConfigChangeResult`, `canApply`,
  `ElarionConfigChangeRequest`, `ElarionConfigChangeValidator`,
  `ElarionConfigEditRequestPayload.Intent.APPLY`,
  `ElarionConfigRegistry`, `CoreConfigManager.reload`.

## Phase 3 Slice 27 Handoff

Slice name:

- Config edit Apply/readiness proposal.

Objective:

- Audit and document what must exist before the config edit shell can enable
  `Apply`.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigChangeRequest.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigChangeResult.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigChangeValidator.java`
- `platform/core/src/main/java/panetina/elarion/core/config/CoreConfigManager.java`
- `platform/core/src/main/java/panetina/elarion/core/command/ElarionCommands.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/event/ElarionDomainEvent.java`
- `platform/core/src/main/java/panetina/elarion/core/event/ElarionEventBus.java`
- `platform/core/src/main/java/panetina/elarion/core/service/HistoryService.java`

Exact files changed:

- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Decisions made:

- Do not enable `Apply` yet.
- Add a Core-owned apply/applier contract before any config entry can become
  editable.
- Keep apply/write behavior separate from descriptors and validation.
- Explicit domain appliers must own file mutation, runtime reload/update,
  rollback/failure recovery, audit metadata, and post-apply synchronization.
- `Apply` must re-run server-side validation immediately before writing and
  must reject stale expected-current values.
- Validation previews must not emit audit/history records; successful applies
  must emit an administration domain event and history record.
- Sensitive config values need a redaction/sensitivity flag before they can be
  safely edited or audited through Admin Panel.

Tests run:

- No Java tests were run; this was a documentation/proposal slice only.
- Documentation whitespace/diff checks were run after the slice.

Work deliberately deferred:

- Core apply contract records/registry.
- File writes.
- Reload orchestration and rollback.
- UI Apply enablement.
- Descriptor changes.
- Addon/domain appliers.

Unresolved risks:

- No applier registry exists yet.
- No domain can safely persist Admin Panel config edits yet.
- No rollback, temp-write, backup, or cross-domain reload orchestration exists.
- `ElarionConfigChangeResult.applied(...)` exists as a model helper but is not
  used by production code.
- Addon config reload safety remains uneven and must be solved per domain
  before writes are enabled.

Precise recommended next slice:

- Phase 3, Slice 28: Core config apply contract records/registry, no writers.
- Classification: MEDIUM.
- Add the Core apply contract types and focused tests for registration,
  duplicate/missing applier behavior, and apply-readiness lookup.
- Exclude file writes, reload orchestration, UI Apply enablement, descriptor
  changes, and addon/domain appliers.
- Search terms: `ElarionConfigChangeResult`, `ElarionConfigChangeRequest`,
  `ElarionConfigChangeValidator`, `ElarionConfigEntry`, `canApply`,
  `ElarionAdminPanelService.dispatchConfigEditRequest`.

## Phase 3 Slice 28 Handoff

Status: completed on 2026-07-06.

Objective:

- Add Core config apply contract records and a registration/readiness registry
  without implementing writers or enabling Apply.

Files inspected:

- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigRegistry.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigDomain.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigCategory.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigEntry.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigEditTarget.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigChangeRequest.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigChangeResult.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigChangeError.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigChangeValidator.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- Existing focused config contract and registry tests.

Exact production files added:

- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplier.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyCapability.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyContext.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyReadiness.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyRegistry.java`

Test file added:

- `platform/core/src/test/java/panetina/elarion/core/config/ElarionConfigApplyRegistryTest.java`

Documentation files changed:

- `docs/config.md`
- `docs/addons/core.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `INDEX.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Decisions and contract behavior:

- Apply ownership remains separate from read-only descriptors and pure
  validation.
- Registrations bind one normalized `ElarionConfigEditTarget` to immutable
  capability metadata and one applier.
- Enabled capabilities must declare audit event type and affected files;
  reload/restart support is explicit. Disabled capabilities carry a reason.
- Descriptor-aware readiness returns structured failures for unknown targets,
  missing/disabled registrations, and unsupported reload/restart policies.
- Ready results must declare audit and affected-file metadata and cannot carry
  errors; blocked results must carry at least one error.
- `ElarionConfigApplyContext` verifies request IDs match the resolved domain,
  category, and entry.

APIs/config/persistence affected:

- New Core Java contract types were added under the config package.
- No existing public facade exposes the registry yet.
- No config keys, files, packet formats, or persistent formats changed.
- No applier is registered or called in production.

Tests run:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigApplyRegistryTest`.
- Passed `.\gradlew.bat :platform:core:test`.

Unresolved risks and deliberately deferred work:

- Registry lifetime/API exposure and registration timing are undecided.
- Atomic validate/write/reload execution, per-domain serialization, backups,
  rollback, audit/history, and client resync are not implemented.
- Admin Panel `canApply` remains false and the client does not send `APPLY`.
- No Core/addon domain appliers exist.
- No sensitive-value descriptor/redaction contract exists.

Questions requiring user input:

- None for this completed contract-only slice.

Precise recommended next slice:

- Phase 3, Slice 29: config apply registry ownership and readiness wiring
  proposal.
- Classification: SMALL.
- Inspect `ElarionApi`, `ElarionSystemApi`, Core initialization,
  `ElarionAdminPanelService.configEditOpenControl`, config edit dispatch, and
  addon descriptor registration timing. Decide registry ownership, exposure,
  and readiness consumption while Apply remains disabled.
- Exclude implementation, writers, reload/rollback, audit emission, UI Apply
  enablement, descriptor changes, and domain appliers.
- Search terms: `ElarionSystemApi`, `configs()`, `configEditOpenControl`,
  `dispatchConfigEditRequest`, `canApply`, `registerDomain`.

## Phase 3 Slice 29 Handoff

Status: completed on 2026-07-06.

Objective:

- Produce the ownership and wiring design for the inert apply registry without
  modifying production code.

Files inspected:

- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`
- `platform/core/src/main/java/panetina/elarion/core/AddonInitializationOrder.java`
- `platform/core/src/main/java/panetina/elarion/core/api/ElarionApi.java`
- `platform/core/src/main/java/panetina/elarion/core/api/ElarionSystemApi.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigRegistry.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyRegistry.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigEditControl.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditResultPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- Core Admin/apply registry focused tests.
- Representative descriptor registration in Government, Economy, and NPCs.

Decisions:

- Core constructs and owns one canonical process-lifetime apply registry beside
  the descriptor registry before addon initialization.
- Addons receive only a registration facade through `ElarionSystemApi`. The
  facade must wrap or method-reference the registry so consumers cannot cast it
  back to the concrete registry and retrieve executable appliers.
- Admin receives readiness without executable lookup. A future Core apply
  coordinator alone may resolve and invoke an applier.
- Register each applier immediately after its matching descriptor and validated
  service/snapshot exist. Deferred domains such as NPCs register both once after
  the first successful definition load.
- Readiness may later improve server-authored disabled reasons, but `editable`
  and `canApply` stay false until full apply orchestration exists.
- A future `canApply=true` requires validation success, current readiness,
  authorization, and coordinator availability; Apply must revalidate instead of
  trusting a preview.

Exact files changed:

- `PLAN.md`
- `TODO.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`

APIs/config/persistence affected:

- None. This slice changed documentation only.

Tests run:

- No Java tests were run; this was a proposal-only slice.

Unresolved risks and deliberately deferred work:

- The registration-only interface/facade does not exist yet.
- Core does not yet construct a canonical apply registry.
- Admin does not consume readiness and still uses a generic disabled reason.
- No coordinator, serialization, writer, backup, rollback, audit, or resync path
  exists.
- No production appliers are registered.

Questions requiring user input:

- None for this proposal slice.

Precise recommended next slice:

- Phase 3, Slice 30: canonical apply registry ownership and registration-only
  Core API.
- Classification: MEDIUM.
- Expected production files: a new registrar contract under Core config,
  `ElarionCoreMod`, `ElarionApi`, and `ElarionSystemApi`; focused Core tests and
  affected config/Core API docs.
- Compatibility: additive API only; no config/save/packet/command behavior.
- Verification: focused registrar/registry tests followed by full Core tests.
- Exclude Admin readiness integration, production/domain registrations,
  applier execution, file writes, reload/rollback, audit emission, and UI Apply
  enablement.
- Search terms: `ElarionConfigApplyRegistry`, `ElarionSystemApi`,
  `ElarionCoreMod configRegistry`, `new ElarionApi`, `registerDomain`.

## Phase 3 Slice 30 Handoff

Status: completed on 2026-07-06.

Objective:

- Establish canonical Core apply-registry ownership and safe registration-only
  addon access without registering or executing any production applier.

Production files changed:

- Added
  `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyRegistrar.java`.
- Updated `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`.
- Updated `platform/core/src/main/java/panetina/elarion/core/api/ElarionApi.java`.
- Updated
  `platform/core/src/main/java/panetina/elarion/core/api/ElarionSystemApi.java`.

Test file changed:

- `platform/core/src/test/java/panetina/elarion/core/config/ElarionConfigApplyRegistryTest.java`.

Documentation files changed:

- `docs/api.md`
- `docs/config.md`
- `docs/addons/core.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `INDEX.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Implementation decisions:

- `ElarionConfigApplyRegistrar` declares only `register(target, capability,
  applier)`.
- Core creates one canonical concrete registry beside the descriptor registry
  before API construction and addon initialization.
- The API stores a method-reference facade (`configApplyRegistry::register`),
  not the concrete registry typed as the registrar. Consumers cannot cast the
  returned object back to `ElarionConfigApplyRegistry` to obtain executable
  registrations.
- `ElarionSystemApi.configAppliers()` is the addon registration extension point.
- No applier is registered in this slice.

API/config/persistence impact:

- Additive public Java API: `ElarionConfigApplyRegistrar` and
  `ElarionSystemApi.configAppliers()`.
- No config keys, descriptors, files, packet formats, commands, or persisted
  formats changed.
- No runtime apply behavior changed.

Tests run:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigApplyRegistryTest`.
- Passed `.\gradlew.bat :platform:core:test`.

Unresolved risks and deferred work:

- The canonical registry has no readiness-only Admin view yet.
- No production registrations or apply coordinator exist.
- Admin still uses a generic disabled reason, reports `canApply=false`, and
  rejects `APPLY`.
- File mutation, reload/rollback, audit/history, and post-apply synchronization
  remain unimplemented.

Questions requiring user input:

- None for this completed slice.

Precise recommended next slice:

- Phase 3, Slice 31: non-executable Admin config readiness integration.
- Classification: MEDIUM.
- Add a readiness-only functional contract/view backed by
  `target -> configApplyRegistry.readiness(configRegistry, target)` and inject it
  into Admin config open/validation helpers.
- Use it only to report precise target-specific disabled reasons. Preserve
  `editable=false`, `canApply=false`, server-side `APPLY` rejection, and the
  disabled/non-interactive client Apply button.
- Add focused tests for missing, disabled, reload-unsafe, restart-unsafe, and
  ready-but-execution-disabled targets, then run full Core tests.
- Exclude production/domain registrations, executable lookup/invocation, file
  writes, reload/rollback, audit emission, and UI Apply enablement.
- Search terms: `configEditOpenControl`, `configEditResult`,
  `CONFIG_EDIT_DISABLED_REASON`, `ElarionConfigApplyReadiness`, `canApply`.

## Phase 3 Slice 31 Handoff

Status: completed on 2026-07-06.

Production files changed:

- Added
  `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyReadinessProvider.java`.
- Updated `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`.
- Updated
  `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`.

Test file changed:

- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`.

Behavior and decisions:

- Core injects a readiness-only method reference into Admin; executable lookup
  remains unavailable there.
- Open controls and successful validation messages report target-specific
  readiness failures.
- A ready registration still reports that execution is disabled.
- `editable=false`, `canApply=false`, server `APPLY` rejection, and the client
  button remain unchanged.

Tests run:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest --tests panetina.elarion.core.config.ElarionConfigApplyRegistryTest`.
- Passed `.\gradlew.bat :platform:core:test`.

Deferred:

- Coordinator/execution contract, production registrations, file writes,
  reload/rollback, audit/history, synchronization, and UI Apply enablement.

Precise next approved slice:

- Phase 3, Slice 32: Core config apply coordinator contract audit.
- Classification: SMALL.
- Inspect apply/change contracts, Admin audit emission, event/history handling,
  and config reload guarantees. Produce the bounded Slice 33 implementation
  contract without modifying production code.
- Search terms: `ElarionConfigApplier`, `ElarionConfigChangeResult.applied`,
  `ElarionDomainEvent`, `emit`, `HistoryService`, `ConfigValidationException`.

## Phase 3 Slice 32 Handoff

Status: completed on 2026-07-06.

Files inspected:

- `ElarionConfigApplier`, `ElarionConfigApplyContext`,
  `ElarionConfigApplyCapability`, `ElarionConfigApplyRegistry`.
- `ElarionConfigChangeValidator`, `ElarionConfigChangeResult`, and error codes.
- `ElarionAdminPanelService` administration event/history emission.
- `ElarionDomainEvent`, `ElarionEventBus`, and `HistoryService.record`.

Decisions:

- Do not implement coordinator execution against the immediate callback API.
- Add a prepare/commit/rollback transaction boundary first.
- Future coordinator uses one global mutation lock initially; it revalidates,
  rechecks readiness, and resolves trusted descriptors under that lock.
- Preparation is non-mutating. Commit performs owner mutation. Rollback is
  idempotent and must restore prior file/runtime state when later coordinator
  steps fail.
- Applied results must preserve validated reload/restart policy and registered
  audit type.

Files changed:

- `PLAN.md`
- `TODO.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`

Tests run:

- No additional Java tests; Slice 32 was documentation/audit only. Slice 31's
  focused and full Core tests were already green immediately before this audit.

Precise next approved slice:

- Phase 3, Slice 33: transactional config applier contract.
- Classification: MEDIUM.
- Add `ElarionConfigPreparedChange` (or convention-matching equivalent) with
  `commit()` and idempotent `rollback()`. Change `ElarionConfigApplier` to
  `prepare(context)`. Add an applied-result factory preserving reload/restart
  flags and update apply registry tests.
- No coordinator, production registrations, Admin Apply, file writes, reload,
  audit, packets, persistence, or UI changes.

## Phase 3 Slice 33 Handoff

Status: completed on 2026-07-06.

Production files changed:

- Added
  `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigPreparedChange.java`.
- Updated `ElarionConfigApplier.java` to prepare transactions.
- Updated `ElarionConfigChangeResult.java` with runtime-policy-preserving applied
  results and applied audit-type validation.

Tests changed:

- Added `ElarionConfigPreparedChangeTest.java`.
- Updated `ElarionConfigApplyRegistryTest.java`.
- Updated `ElarionConfigChangeContractTest.java`.
- Updated `ElarionAdminPanelServiceTest.java`.

Behavior and invariants:

- `prepare(context)` is the owner extension point and must not mutate state.
- Prepared changes commit at most once and may roll back after success/failure;
  the standard helper invokes rollback at most once.
- `APPLIED` results require an audit event type and preserve reload/restart
  flags when supplied.
- No code invokes a prepared transaction in production.

Tests run:

- Focused prepared-change, change-contract, apply-registry, and Admin tests.
- Full `.\gradlew.bat :platform:core:test` passed.
- Final focused prepared-change/change-contract rerun passed after API cleanup.

Deferred:

- Coordinator, production registrations, Admin/network/UI Apply wiring, real
  files, reload/rollback implementations, audit/history, and synchronization.

Precise recommended next slice:

- Phase 3, Slice 34: internal config apply coordinator implementation.
- Classification: MEDIUM.
- Add an unwired coordinator using the canonical descriptor/apply registries, a
  global `ReentrantLock`, and a mandatory audit sink. Revalidate/readiness-check
  under lock, resolve trusted context, prepare/commit, validate returned result,
  audit, and roll back on commit/audit failure.
- Add focused tests for rejection without preparation, stale recheck, missing
  registration, prepare/commit exceptions, invalid owner result, successful
  audit, rollback after commit/audit failure, and lock serialization.
- Exclude production registrations, Admin/network/UI wiring, real file writes,
  reload orchestration, and client synchronization.

## Phase 3 Slice 34 Handoff

Status: completed on 2026-07-06.

Production files added:

- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyAuditRecord.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyAuditSink.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyCoordinator.java`

Test file added:

- `platform/core/src/test/java/panetina/elarion/core/config/ElarionConfigApplyCoordinatorTest.java`

Behavior:

- One fair static lock serializes all coordinator instances.
- Validation, stale detection, readiness, and trusted descriptor resolution run
  under that lock.
- Owner preparation is non-mutating; commit results are accepted only when all
  trusted fields match validation and capability metadata.
- Structured audit is mandatory after trusted commit.
- Commit/result/audit failure rolls back. Rollback failure is retained in the
  returned `APPLY_FAILED` message.

Tests run:

- Passed focused `ElarionConfigApplyCoordinatorTest` before and after final lock
  and failure-coverage corrections.
- Passed full `.\gradlew.bat :platform:core:test`.

Deferred:

- Production coordinator construction/ownership.
- Core history/domain-event audit adapter.
- Production appliers, real file writers, reload orchestration, Admin/network/UI
  Apply wiring, and synchronization.

Precise recommended next slice:

- Phase 3, Slice 35: coordinator ownership and audit-adapter proposal.
- Classification: SMALL.
- Inspect Core construction order, `HistoryService.bind/record`, non-throwing
  `ElarionEventBus.emitDomainEvent`, Admin config request dispatch, and current
  audit metadata conventions. Decide ownership and safe history-first/event-last
  adapter ordering before implementation.
- Exclude implementation, production registrations, Apply enablement, files,
  reload orchestration, packets, UI, and synchronization.
- Search terms: `new ElarionConfigApplyCoordinator`, `HistoryService.record`,
  `emitDomainEvent`, `validateConfigEdit`, `APPLY`, `canApply`.

## Phase 3 Slice 35 Handoff

Status: completed on 2026-07-06.

Inspected:

- Core construction and `SERVER_STARTED` binding order.
- `HistoryService.bind`, policy-filtered `record`, and queued storage/index
  append behavior.
- Non-throwing domain-event listener dispatch.
- Admin config request dispatch and current unconditional `APPLY` rejection.
- Coordinator audit record/sink and rollback behavior.

Decisions:

- General History cannot be the mandatory config audit authority because it is
  filterable and queued.
- Add a dedicated write-ahead config audit journal under
  `world/elarion/core/audit/` before constructing the production coordinator.
- Audit uses PREPARED before commit and COMMITTED after commit; terminal failure
  phases preserve rollback/recovery evidence.
- History and domain events are post-COMMITTED projections and do not control
  rollback.
- Coordinator execution is unavailable until the audit journal is bound.
- Admin eventually receives only an execution facade.
- Secret-like entries remain non-editable until sensitivity/redaction metadata
  exists.

Files changed:

- `PLAN.md`
- `TODO.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`

Tests run:

- No Java tests; Slice 35 was proposal-only. Slice 34 focused and full Core tests
  were green immediately before this audit.

Precise recommended next slice:

- Phase 3, Slice 36: write-ahead config audit session contracts.
- Classification: MEDIUM.
- Evolve `ElarionConfigApplyAuditSink` into `prepare(record)` returning a session
  with committed/rolledBack/failed terminal operations. Update the unwired
  coordinator and focused tests so projections are no longer modeled as the
  mandatory audit step.
- Exclude storage/files, production coordinator ownership, appliers,
  Admin/network/UI wiring, reload, and synchronization.
- Search terms: `ElarionConfigApplyAuditSink`, `prepare(record)`,
  `ElarionConfigApplyCoordinator`, `rollback`, `ElarionConfigApplyCoordinatorTest`.

## Audit Scope

- Read all Markdown files currently tracked or present in the repository.
- Scanned project source outside `addons/angling/**`.
- Kept `addons/angling/reference/**` as upstream reference material only.
- Treated the current dirty worktree as current project reality; do not revert
  uncommitted files unless explicitly asked.

Current non-Angling source footprint from this pass:

- `platform/core`: 244 code/build/resource files, including 240 Java files and
  44 Java tests.
- Active addon/source modules: Economy, Government, Groups, Mounts, Names,
  NPCs, Offerings, Optimization, Portals, Quests, Realms, Security, Titles,
  Underworld, Worlds.
- Shell or early integration modules: Jail, Newspapers, Tablist,
  Voice Chat Hooks.
- `tests/gametest`: command and integration GameTest support.
- `dev`: aggregate Fabric development runtime.

## Current Build Shape

Fabric 1.21.1 multi-module Gradle project:

- Root: `build.gradle`, `settings.gradle`, `gradle.properties`.
- Core: `platform/core`.
- Addons: `addons/*`, loaded through the custom `elarion:addon` entrypoint.
- Dev runtime: `dev`.
- GameTests: `tests/gametest`.

Primary commands:

```text
.\gradlew.bat build
.\gradlew.bat runClient
.\gradlew.bat runServer
.\gradlew.bat runGameTests
.\gradlew.bat exportMods
.\gradlew.bat rebuildExportMods
.\gradlew.bat :platform:core:test
.\gradlew.bat :addons:<addon>:test
```

Use focused compile/test tasks first, then full `build` when a change crosses
modules or before handoff.
Use `exportMods` to copy deployable Core/addon remapped jars and the curated
modpack dependency set into `build/export`; use `rebuildExportMods` when the
export folder should be cleared first. Output folders:

- `build/export/server-mods`: install-ready dedicated-server folder containing
  Elarion jars plus required both-side modpack dependencies.
- `build/export/client-mods`: install-ready client folder containing Elarion
  jars plus required both-side modpack dependencies.
- `build/export/elarion/both`: Elarion Core/addon jars classified as
  both-side. Current Elarion mod metadata declares `environment: "*"`.
- `build/export/modpack/both`: curated third-party dependency jars classified
  as both-side, including Fabric API, Geckolib, Fantasy, and the current
  worldgen/continent dependency set.
- `build/export/mods`: flat Elarion-only compatibility export.

The export task automatically includes `:platform:core` and all
current/future `:addons:*` projects, excluding `dev` and `tests`.

## Current Source Truth

Core owns canonical truth and shared infrastructure:

- citizens, identity, Realms, titles, abilities, rewards, history, public
  history, permissions, server identity, shared UI primitives, task queues,
  character lifecycle, True Death reset orchestration, notifications, and
  domain events
- config defaults and validation patterns under `platform/core/.../config`
- runtime world storage helpers under `platform/core/.../storage`
- networking examples under `platform/core/.../network`
- command registration under `platform/core/.../command`

Addons own only feature-specific behavior and runtime state:

- Economy: currency, wallets, treasuries, transactions, pricing, Economy pulse,
  reward/NPC action registrations.
- Offerings: Shrine of Foundation, Offering projects, instances, anchors,
  donations, milestones, Shrine UI.
- Government: Civic Forum, Seat of Rule, forms, founding votes, offices,
  authority chat, proposals, laws/civic records, gate/status UI.
- Groups: public groups, tags, invites, membership, group chat,
  Confederation delegate hooks.
- NPCs: static NPC entity, placement storage, dialogue definitions/sessions,
  prompt handling, skins, portraits, NPC UI.
- Quests: data-driven questline definitions, scoped shared/player quest state,
  registered quest actions/conditions, scheduled consequences, Quest
  notifications, and Shrine display-name projections through Offerings.
- Portals: linked A/B routes, portal fields, tickets, schedules, route windows,
  return entitlements, travel confirmation UI.
- Worlds: managed worlds, per-world borders, spawn protection, abundance rules.
- Realms: Realm protection behavior that consumes Core Realm truth.
- Names/Titles: presentation hooks for identity, titles, tablist/nameplates.
- Optimization: diagnostics and queue/world trend foundations.
- Security: evidence/status foundation.
- Underworld: death capture, component-safe graves/corpses, recovery vaults,
  nickname-labeled tombs, modular item source metadata, Underworld sessions,
  Soul Fractures, True Death handoff to Core.
- Mounts: native Fabric flying mount entity, Core Collection menu tab with
  top-tab Mounts/Pets/Titles layout, persistent unlock/active-mount state,
  whistle placeholder collection icons, config-driven collection text,
  client-only Collection model previews through Core's generic preview
  registry and direct entity-dispatch preview utility, bounds-aware preview
  scale/offset from converted geo bounds with Collection-only art calibration
  for long/tiny mounts, seven legacy whistle icon ids, GeckoLib geo rendering,
  mount input, camera/rider presentation, session recovery.
- Angling: active fishing foundation, but exclude `addons/angling/reference/**`
  unless explicitly resuming Angling porting.

## Current Runtime And Config Rules

- Editable definitions live under `config/elarion/`.
- Mutable runtime state lives under `world/elarion/`.
- Growing history-like features must expose bounded APIs, indexes, or archive
  summaries before becoming player-facing.
- Do not make players, commands, GUIs, web bridges, Chronicle, newspapers,
  ledgers, or rumors scan raw JSONL or broad world state.

Important state locations:

- Core citizens/realms/rewards/history/notifications/characters:
  `world/elarion/core/` and `world/elarion/history*`.
- Economy: `world/elarion/addon-state/economy/`.
- Offerings: `world/elarion/addon-state/offerings/`.
- Government: `world/elarion/addon-state/government/`.
- Groups: `world/elarion/addon-state/groups/`.
- NPC placements: `world/elarion/addon-state/npcs/placed-npcs.json`.
- Quests: `world/elarion/addon-state/quests/state.json`.
- Portals: `world/elarion/addon-state/portals/`.
- Underworld: `world/elarion/addon-state/underworld/`.
- Mount sessions: `world/elarion/addon-state/mounts/sessions.json`.
- Mount collection state: `world/elarion/addon-state/mounts/collection.json`.

## Current Verification Status

High-priority manual verification still lives in `TODO.md`. Current major
pending areas:

- Government civic-record V1 in-game flows and broader GameTest coverage.
- Shrine/Government reset command manual verification before the next
  progression pass.
- Tablist Realm header rows at varied GUI scales/player counts.
- Underworld V1 death, corpse, vault, restart, Soul Fracture, and True Death
  flows.
- Character Lifecycle migration, new-character creation, cooldown, and
  restart-safe cleanup.
- Collection UI manual check after the paused 2026-06-29 pass: verify selected
  unlocked mounts render in the right preview frame after the bounds-aware
  preview pass, row icon frames have no black corner pixels, top tabs show only
  Mounts, Pets, and Titles, and Titles can set active titles through the
  existing Core title state.

Known focused automated coverage exists for Core storage/services/network
payloads, Economy, Government models/services/storage, Groups, NPCs, Offerings,
Portals, Realms, Worlds, Optimization, Mounts, Angling, and GameTest command
basics. Full in-game coverage is intentionally still incomplete.

## Documentation Map

Start here:

1. `RULES.md`
2. `AGENTS.md`
3. `CODEX.md`
4. `INDEX.md`
5. `docs/ai/CURRENT_STATUS.md`
6. Relevant `docs/systems/*.md`
7. Relevant `docs/addons/*.md`
8. Relevant `wiki/admin/*.md`

Use:

- `TODO.md` for current implementation work.
- `PLAN.md` for short project memory/read order.
- `PLANS.md` for future design directions.
- `OPTIMIZATION_TRACKER.md` for performance/health risks.
- `docs/architecture/PROJECT_STRUCTURE.md` for module ownership.
- `docs/architecture/DEPENDENCY_GRAPH.md` for separation rules.
- `docs/architecture/KNOWLEDGE_MAP.md` for navigation.
- `docs/ai/AI_SEARCH_HINTS.md` for targeted source lookup.

## Hard Rules For Future Agents

- Search before adding infrastructure.
- Extend Core APIs/events/registries instead of direct cross-addon state reads.
- Keep runtime state server-authoritative.
- Keep config reload-safe and validated.
- Add or update tests when persistence, commands, networking, permissions, or
  behavior changes.
- Update docs in the same pass when ownership, command, config, API, packet,
  UI, permission, event, notification, or addon status changes.
- If docs and source disagree, inspect source and update docs before building
  on stale text.

## Latest Slice Handoff - Phase 3, Slice 36

Slice name:

- Write-ahead config audit session contracts.

Objective:

- Replace the coordinator's one-shot audit sink with a pre-commit audit session
  contract that can record committed, rolled-back, or failed terminal outcomes.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyAuditSink.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyCoordinator.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigPreparedChange.java`
- `platform/core/src/test/java/panetina/elarion/core/config/ElarionConfigApplyCoordinatorTest.java`
- `docs/config.md`
- `docs/api.md`
- `docs/addons/core.md`
- `INDEX.md`
- `PLAN.md`
- `TODO.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`

Decisions made:

- Mandatory config apply audit is modeled as
  `ElarionConfigApplyAuditSink.prepare(record)`.
- `prepare(record)` returns `ElarionConfigApplyAuditSession`.
- The session terminal methods are `committed()`, `rolledBack(failure)`, and
  `failed(failure)`.
- The coordinator prepares audit after non-mutating owner preparation but
  before commit.
- Audit preparation failure prevents commit and rolls back prepared owner
  resources.
- Commit, trusted-result validation, or committed-terminal failure triggers
  rollback and a rolled-back audit terminal outcome.
- Rollback failure triggers the failed audit terminal outcome.
- The path remains internal and unwired; no production apply execution exists.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyAuditSink.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyAuditSession.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyCoordinator.java`
- `platform/core/src/test/java/panetina/elarion/core/config/ElarionConfigApplyCoordinatorTest.java`
- `docs/config.md`
- `docs/api.md`
- `docs/addons/core.md`
- `INDEX.md`
- `PLAN.md`
- `TODO.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`

Tests run:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigApplyCoordinatorTest`
- `.\gradlew.bat :platform:core:test`

Unresolved risks at the end of Slice 36:

- No durable audit journal existed yet. This was resolved by Slice 37's
  unbound journal; production binding is still unresolved.
- No production coordinator/executor owns a server/world path.
- No domain appliers are registered.
- Admin/network/UI still cannot apply config changes.
- Secret/sensitive config entries still require sensitivity and redaction
  metadata before editability.

Precise recommended next slice:

- Phase 3, Slice 37: durable config audit journal storage.
- Classification: MEDIUM persistence slice.
- Implement an unbound journal audit sink with versioned JSONL records for
  PREPARED, COMMITTED, ROLLED_BACK, and FAILED, synchronous append plus force,
  bounded unresolved-tail recovery, and temporary-directory tests.
- Exclude production binding, coordinator ownership, appliers, Admin/network/UI
  execution, config file writes, reload, and synchronization.

Search terms:

- `ElarionConfigApplyAuditSession`
- `ElarionConfigApplyAuditSink`
- `ElarionConfigApplyCoordinator`
- `committed()`
- `rolledBack`
- `failed`
- `CONFIG_ADMIN_AUDIT`

## Latest Slice Handoff - Phase 3, Slice 37

Slice name:

- Durable config audit journal storage.

Objective:

- Add the unbound JSONL write-ahead audit journal for future config apply
  execution.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/storage/CatchTelemetryJournalStorage.java`
- `platform/core/src/main/java/panetina/elarion/core/storage/CatchTelemetryJournalCodec.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyAuditRecord.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyAuditSink.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyAuditSession.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyCoordinator.java`
- `docs/config.md`
- `docs/api.md`
- `docs/addons/core.md`
- `INDEX.md`
- `PLAN.md`
- `TODO.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`

Decisions made:

- Config apply audit durability is owned by a dedicated Core journal, not
  general History.
- The journal is unbound: tests pass it an explicit path, and no production
  server/world owner constructs it yet.
- `ElarionConfigApplyAuditPhase` defines PREPARED, COMMITTED, ROLLED_BACK, and
  FAILED.
- `ElarionConfigApplyAuditJournal` implements `ElarionConfigApplyAuditSink`.
- Each journal line is versioned JSONL with one audit ID, phase, timestamp,
  full normalized audit record, and optional failure text.
- Append calls are synchronized and force the file before returning.
- Recovery is tail-bounded and reports `tailTruncated` so production binding
  can reject unsafe states later.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyAuditPhase.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyAuditJournal.java`
- `platform/core/src/test/java/panetina/elarion/core/config/ElarionConfigApplyAuditJournalTest.java`
- `docs/config.md`
- `docs/api.md`
- `docs/addons/core.md`
- `INDEX.md`
- `PLAN.md`
- `TODO.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`

Tests run:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigApplyAuditJournalTest`
- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigApplyCoordinatorTest`
- `.\gradlew.bat :platform:core:test`

Unresolved risks:

- No production service binds the journal to the active server/world path.
- No production coordinator/executor facade exists.
- No domain appliers are registered.
- Admin/network/UI still cannot apply config changes.
- Config file writes, reload, rollback around real files, synchronization, and
  sensitivity/redaction remain future work.

Precise recommended next slice:

- Phase 3, Slice 38: production config apply ownership proposal.
- Classification: SMALL proposal slice.
- Decide the Core-owned service/facade that binds descriptor registry, apply
  registry, coordinator, and durable audit journal to the active server/world
  path.
- Exclude domain appliers, Admin Apply wiring, config file writes, reload, and
  synchronization until the ownership boundary is approved.

Search terms:

- `ElarionConfigApplyAuditJournal`
- `ElarionConfigApplyAuditPhase`
- `recoverUnresolvedTail`
- `journalPath`
- `ElarionConfigApplyCoordinator`
- `config-changes.jsonl`

## Latest Slice Handoff - Phase 3, Slice 38

Slice name:

- Production config apply ownership proposal.

Objective:

- Decide the production owner/facade for config apply execution before wiring
  mutable Admin behavior.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`
- `platform/core/src/main/java/panetina/elarion/core/api/ElarionApi.java`
- `platform/core/src/main/java/panetina/elarion/core/api/ElarionSystemApi.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/storage/JsonStateStorage.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyCoordinator.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyAuditJournal.java`

Decisions made:

- Add a future Core-owned `ElarionConfigApplyService`.
- The service owns production construction and lifecycle for
  `ElarionConfigApplyCoordinator`.
- The service receives the canonical descriptor registry and concrete
  `ElarionConfigApplyRegistry` during Core initialization.
- The service binds on `SERVER_STARTED` using
  `JsonStateStorage.elarionRoot(server)` and
  `ElarionConfigApplyAuditJournal.journalPath(root)`.
- Apply execution stays unavailable while unbound, when journal recovery is
  unsafe, or when bounded recovery finds unresolved PREPARED audit records.
- `ElarionApi.system().configAppliers()` remains registration-only.
- Admin should receive only a narrow readiness/executor facade, not coordinator,
  concrete registry lookup, or journal mutation access.

Exact files changed:

- `PLAN.md`
- `TODO.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`

Tests run:

- No Java tests; this was a proposal-only slice.

Unresolved risks at the end of Slice 38:

- No `ElarionConfigApplyService` implementation existed yet. This was resolved
  by Slice 39's skeleton; backend execution remains unresolved.
- Admin/network/UI still cannot apply config changes.
- No domain appliers are registered.
- Config file writes, reload, synchronization, and sensitivity/redaction remain
  future work.

Precise recommended next slice:

- Phase 3, Slice 39: implement `ElarionConfigApplyService` skeleton.
- Classification: SMALL.
- Add the service class, bind it in `ElarionCoreMod`, route Admin readiness
  through the service, and add focused tests for unbound, bound, unresolved
  audit recovery, and readiness delegation.
- Exclude execute/apply wiring, domain appliers, Admin Apply enablement, config
  file writes, reload, and synchronization.

Search terms:

- `ElarionConfigApplyService`
- `ElarionConfigApplyCoordinator`
- `ElarionConfigApplyAuditJournal`
- `JsonStateStorage.elarionRoot`
- `bindConfigApplyReadiness`
- `SERVER_STARTED`

## Latest Slice Handoff - Phase 3, Slice 39

Slice name:

- Config apply service skeleton.

Objective:

- Add the Core-owned service skeleton that binds the durable audit journal and
  gates future config apply readiness.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyReadiness.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyRegistry.java`
- `platform/core/src/test/java/panetina/elarion/core/config/ElarionConfigApplyRegistryTest.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`

Decisions made:

- `ElarionConfigApplyService` owns production config apply lifecycle.
- Core constructs it with the descriptor registry and concrete apply registry.
- Core binds it on `SERVER_STARTED` with `JsonStateStorage.elarionRoot(server)`.
- Core unbinds it on `SERVER_STOPPING`.
- Admin readiness now goes through the service.
- The service blocks otherwise-ready targets while unbound, when audit recovery
  is truncated, or when unresolved PREPARED records exist.
- No execution method or Admin Apply path was added.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/service/ElarionConfigApplyService.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionConfigApplyServiceTest.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`
- `docs/config.md`
- `docs/api.md`
- `docs/addons/core.md`
- `INDEX.md`
- `PLAN.md`
- `TODO.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`

Tests run:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionConfigApplyServiceTest`
- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigApplyCoordinatorTest --tests panetina.elarion.core.config.ElarionConfigApplyAuditJournalTest`
- `.\gradlew.bat :platform:core:test`

Unresolved risks:

- No backend execution method exists yet.
- No domain appliers are registered.
- Admin/network/UI still cannot apply config changes.
- Config file writes, reload, synchronization, and sensitivity/redaction remain
  future work.

Precise recommended next slice:

- Phase 3, Slice 40: backend config apply execution method.
- Classification: SMALL.
- Add `ElarionConfigApplyService.apply(request, actorPermission)` so it rejects
  while unbound/unsafe and delegates to the coordinator only when ready.
- Use fake test-only appliers; exclude production appliers, Admin/network/UI
  Apply wiring, config file writes, reload, and synchronization.

Search terms:

- `ElarionConfigApplyService`
- `executionReady`
- `recoverUnresolvedTail`
- `bindConfigApplyReadiness`
- `ElarionConfigApplyServiceTest`

## Latest Slice Handoff - Phase 3, Slice 40

Slice name:

- Backend config apply execution method.

Objective:

- Add backend-only apply delegation to `ElarionConfigApplyService` without
  enabling Admin Apply or production config writers.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigChangeValidator.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionConfigApplyService.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionConfigApplyServiceTest.java`

Decisions made:

- `ElarionConfigApplyService.apply(request, actorPermission)` is the backend
  execution method for future Admin Apply.
- It returns `UNSUPPORTED` while unbound or audit recovery is unsafe.
- It delegates to the coordinator only when the service is bound and safe.
- Tests use fake in-test appliers only.
- Admin/network/UI Apply remains disabled.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/service/ElarionConfigApplyService.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionConfigApplyServiceTest.java`
- `docs/config.md`
- `docs/api.md`
- `docs/addons/core.md`
- `PLAN.md`
- `TODO.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`

Tests run:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionConfigApplyServiceTest`
- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigApplyCoordinatorTest --tests panetina.elarion.core.config.ElarionConfigApplyAuditJournalTest`
- `.\gradlew.bat :platform:core:test`

Unresolved risks:

- Admin/network/UI still cannot apply config changes.
- No production domain appliers are registered.
- Config file writes, reload, synchronization, and sensitivity/redaction remain
  future work.

Precise recommended next slice:

- Phase 3, Slice 41: Admin config apply executor boundary proposal.
- Classification: SMALL proposal slice.
- Decide the narrow interface Admin should use for apply execution, OP-level
  checks, packet response behavior, and the UI-disabled rollout boundary.
- Exclude implementation, production domain appliers, config file writes,
  reload, and synchronization.

Search terms:

- `ElarionConfigApplyService.apply`
- `ElarionConfigEditRequestPayload.Intent.APPLY`
- `validateConfigEdit`
- `configEditResult`
- `canApply`

## Latest Slice Handoff - Phase 3, Slice 41

Slice name:

- Admin config apply executor boundary proposal.

Objective:

- Decide the narrow boundary between Admin config edit handling and backend
  config apply execution.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditRequestPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditResultPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`
- `platform/core/src/test/java/panetina/elarion/core/network/ElarionConfigEditPayloadTest.java`

Decisions made:

- Add a future `ElarionConfigApplyExecutor` contract.
- The executor should expose only readiness lookup and backend apply execution.
- `ElarionConfigApplyService` should implement it.
- `ElarionAdminPanelService` should bind the executor instead of only a
  readiness provider.
- The first executor-facade implementation should keep `Intent.APPLY` rejected
  and keep the client Apply button disabled.
- Server-side APPLY dispatch must be a separate slice with OP, stale-value,
  result-payload, and Admin refresh tests.

Exact files changed:

- `PLAN.md`
- `TODO.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`

Tests run:

- No Java tests; this was a proposal-only slice.

Precise recommended next slice:

- Phase 3, Slice 42: add Admin config apply executor facade.
- Classification: SMALL.
- Add `ElarionConfigApplyExecutor`, have `ElarionConfigApplyService` implement
  it, and bind Admin to the executor while keeping APPLY rejected.
- Exclude server-side APPLY dispatch, production appliers, config file writes,
  reload, synchronization, and UI Apply enablement.

Search terms:

- `ElarionConfigApplyExecutor`
- `bindConfigApplyReadiness`
- `ElarionConfigApplyService`
- `ElarionAdminPanelService`
- `Intent.APPLY`

## Latest Slice Handoff - Phase 3, Slice 42

Slice name:

- Admin config apply executor facade.

Objective:

- Add the narrow Admin-facing executor facade while keeping server-side APPLY
  dispatch disabled.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionConfigApplyService.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`

Decisions made:

- `ElarionConfigApplyExecutor` is the Admin-facing facade.
- The facade exposes readiness and backend apply only.
- `ElarionConfigApplyService` implements the facade.
- `ElarionAdminPanelService` binds an executor through
  `bindConfigApplyExecutor(...)`.
- The readiness-only binding helper remains for focused tests and wraps
  readiness in an executor that rejects apply.
- `Intent.APPLY` still rejects before executor dispatch.
- The client Apply button remains disabled.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyExecutor.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionConfigApplyService.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`
- `docs/config.md`
- `docs/api.md`
- `docs/addons/core.md`
- `INDEX.md`
- `PLAN.md`
- `TODO.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`

Tests run:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest --tests panetina.elarion.core.service.ElarionConfigApplyServiceTest`
- `.\gradlew.bat :platform:core:test`

Unresolved risks:

- Server-side APPLY dispatch is still disabled.
- No production domain appliers are registered.
- Config file writes, reload, synchronization, and sensitivity/redaction remain
  future work.

Precise recommended next slice:

- Phase 3, Slice 43: server-side Admin APPLY dispatch proposal.
- Classification: SMALL proposal slice.
- Decide exact request/result conversion from `ElarionConfigEditRequestPayload`
  APPLY to `ElarionConfigApplyExecutor.apply(...)`, OP checks, stale-value
  behavior, Admin refresh, and whether the client Apply button stays disabled
  until a production applier exists.
- Exclude implementation, production appliers, config file writes, reload, and
  synchronization.

Search terms:

- `ElarionConfigApplyExecutor`
- `bindConfigApplyExecutor`
- `Intent.APPLY`
- `configEditResult`
- `validateConfigEdit`

## Latest Slice Handoff - Phase 3, Slice 43

Slice name:

- Server-side Admin APPLY dispatch proposal.

Objective:

- Decide how Admin config edit APPLY requests should route to the executor in a
  future implementation slice.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditRequestPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditResultPayload.java`

Decisions made:

- Keep the existing config edit packet schema; `Intent.APPLY` already exists.
- Keep the Core network receiver shape unchanged.
- Evolve Admin service request/result conversion internally.
- OP level 4 remains checked before validation or apply dispatch.
- VALIDATE keeps using `ElarionConfigChangeValidator.validate(...)`.
- APPLY should call `ElarionConfigApplyExecutor.apply(...)` after OP checks.
- Empty reason fallback should be `admin-panel-config-edit-preview` for
  VALIDATE and `admin-panel-config-edit-apply` for APPLY.
- APPLIED results should convert to `ElarionConfigEditResultPayload` with
  `status=APPLIED`, old/new values, runtime flags, no errors, `canApply=false`,
  an audit preview, and an `Applied:` message.
- Client Apply remains disabled until a later UI slice.

Exact files changed:

- `PLAN.md`
- `TODO.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`

Tests run:

- No Java tests; this was a proposal-only slice.

Precise recommended next slice:

- Phase 3, Slice 44: implement server-side Admin APPLY dispatch.
- Classification: SMALL.
- Change only Admin service request/result conversion and focused tests.
- Keep client Apply disabled and use fake test executors only.
- Exclude production appliers, config file writes, reload, synchronization, and
  UI Apply enablement.

Search terms:

- `configEditResult`
- `validateConfigEdit`
- `ElarionConfigApplyExecutor.apply`
- `ElarionConfigEditRequestPayload.Intent.APPLY`
- `ElarionAdminPanelServiceTest`

## Latest Slice Handoff - Phase 3, Slice 44

Slice name:

- Server-side Admin APPLY dispatch.

Objective:

- Route server-side Admin config APPLY requests to `ElarionConfigApplyExecutor`
  while keeping the client Apply button disabled.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`

Decisions made:

- `configEditResult(...)` now handles both VALIDATE and APPLY.
- VALIDATE behavior remains unchanged.
- APPLY builds an `ElarionConfigChangeRequest` and dispatches to
  `ElarionConfigApplyExecutor.apply(...)` after OP checks.
- Empty APPLY reasons become `admin-panel-config-edit-apply`.
- APPLIED results convert to `ElarionConfigEditResultPayload` with status
  APPLIED, old/new values, runtime flags, no errors, `canApply=false`, audit
  preview, and an `Applied:` message.
- Rejected executor results preserve errors and return `Apply failed: ...`.
- Client Apply remains disabled and no production applier exists.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`
- `docs/config.md`
- `docs/api.md`
- `docs/addons/core.md`
- `PLAN.md`
- `TODO.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`

Tests run:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest`
- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionConfigApplyServiceTest`
- `.\gradlew.bat :platform:core:test`

Unresolved risks:

- Client Apply button is still disabled.
- No production domain appliers are registered.
- Config file writes, reload, synchronization, and sensitivity/redaction remain
  future work.

Precise recommended next slice:

- Phase 3, Slice 45: Admin Apply UI enablement proposal.
- Classification: SMALL proposal slice.
- Decide how server-authored edit controls should expose Apply availability,
  whether the client button remains disabled until a production applier exists,
  and what verification is required before making Apply clickable.
- Exclude implementation, production appliers, config file writes, reload, and
  synchronization.

Search terms:

- `configEditApplyResult`
- `admin-panel-config-edit-apply`
- `ElarionConfigApplyExecutor.apply`
- `canApply`
- `ElarionAdminPanelScreen`

## Latest Slice Handoff - Phase 3, Slice 45

Slice name:

- Admin Apply UI enablement proposal.

Objective:

- Decide when and how the Admin config editor may make Apply clickable.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigEditControl.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionConfigEditClientState.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionConfigEditClientStateTest.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`

Decisions made:

- Keep the client Apply button disabled until at least one production applier is
  approved and tested.
- Apply availability must be server-authored on the edit control.
- The current `editable` flag mixes input editability and apply execution, so it
  is too broad for the rollout.
- Future control metadata should separate input editability from apply
  availability.
- A future client Apply predicate must require an open control, nonblank input,
  latest matching VALIDATED result, matching current/proposed values,
  server-authored apply availability, and `result.canApply()`.
- The server must continue rejecting forged APPLY packets when execution is not
  safe.

Exact files changed:

- `PLAN.md`
- `TODO.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`

Tests run:

- No Java tests; this was a proposal-only slice.

Precise recommended next slice:

- Phase 3, Slice 46: split config edit control input/apply state.
- Classification: SMALL.
- Add explicit apply availability metadata to the control/payload model and
  focused packet/client-state tests, but keep the Admin screen Apply button
  disabled.
- Exclude production appliers, config file writes, reload, synchronization, and
  clickable Apply UI.

Search terms:

- `ElarionConfigEditControl`
- `editable`
- `canApply`
- `sendConfigEditValidation`
- `ElarionConfigEditPayloadTest`
- `ElarionConfigEditClientStateTest`

## Latest Slice Handoff - Phase 3, Slice 46

Slice name:

- Split config edit control input/apply state.

Objective:

- Separate input editability from Apply availability in the server-authored
  config edit control while keeping Apply disabled.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigEditControl.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditPayloadCodecs.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/test/java/panetina/elarion/core/network/ElarionConfigEditPayloadTest.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionConfigEditClientStateTest.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`

Decisions made:

- `ElarionConfigEditControl` now carries `inputEditable`,
  `applyAvailable`, `disabledReason`, and `applyDisabledReason`.
- The old constructor remains as a compatibility path and maps `editable` to
  `inputEditable`, with `applyAvailable=false`.
- `editable()` remains as a compatibility alias for `inputEditable()`.
- The open-payload codec round-trips explicit apply availability state.
- The Admin screen can display `applyDisabledReason`, but Apply still renders
  and handles as disabled.
- No production applier, file write, reload, sync, or clickable Apply UI was
  added.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigEditControl.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditPayloadCodecs.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/test/java/panetina/elarion/core/network/ElarionConfigEditPayloadTest.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionConfigEditClientStateTest.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`
- `docs/config.md`
- `docs/api.md`
- `docs/addons/core.md`
- `PLAN.md`
- `TODO.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`

Tests run:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.network.ElarionConfigEditPayloadTest --tests panetina.elarion.core.client.ElarionConfigEditClientStateTest --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest`
- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionConfigApplyServiceTest`
- `.\gradlew.bat :platform:core:test`

Unresolved risks:

- No production domain appliers are registered.
- Apply remains disabled in the Admin screen.
- Config file writes, reload, synchronization, and sensitivity/redaction remain
  future work.

Precise recommended next slice:

- Phase 3, Slice 47: production config applier selection proposal.
- Classification: SMALL proposal slice.
- Choose the first low-risk production config entry/domain to make writable and
  define file write, reload, rollback, audit, UI, and test requirements.
- Exclude implementation.

Search terms:

- `ElarionConfigEditControl`
- `inputEditable`
- `applyAvailable`
- `applyDisabledReason`
- `ElarionConfigEditPayloadCodecs`
- `ElarionAdminPanelScreen`

## Latest Slice Handoff - Phase 3, Slice 47

Slice name:

- Production config applier selection proposal.

Objective:

- Choose the first low-risk production config entry/domain to make writable and
  define file write, reload, rollback, audit, UI, and test requirements before
  implementation.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/config/CoreConfigDescriptors.java`
- `platform/core/src/main/java/panetina/elarion/core/config/CoreConfigManager.java`
- `platform/core/src/main/java/panetina/elarion/core/command/ElarionCommands.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionUiThemeService.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionConfigApplyService.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyRegistry.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyCoordinator.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigPreparedChange.java`

Decisions made:

- The first production writable target should be
  `core:ui_theme:defaults.font-scale-percent`.
- The target maps to `config/elarion/core/ui_theme.yml` at
  `defaults.font-scale-percent`.
- The value is a bounded integer from `100` to `150`, runtime-reloadable, not
  restart-required, and OPERATOR-gated.
- Future audit event type should be
  `core.config.ui_theme.font_scale_changed`.
- This target was chosen because it is one scalar value with existing descriptor
  coverage, validation, Core reload behavior, and UI theme sync.
- Realm spawn editing is deferred because it is a multi-field
  gameplay-affecting location transaction.
- Server identity, dynamic Realm/title/reward definition rows, addon domains,
  and restart-required settings remain deferred.
- No production applier, file write, reload, synchronization, UI click
  enablement, config behavior, or gameplay behavior was changed.

Exact files changed:

- `PLAN.md`
- `TODO.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`

Tests run:

- No Java tests; this was a proposal-only documentation slice.

Unresolved risks:

- `ui_theme.yml` writer strategy must avoid partial writes on Windows.
- Generic YAML dumping may normalize formatting/comments; prefer a narrow
  scalar update helper or document normalization before implementation.
- Rollback must restore the exact previous file contents and active theme if
  post-write reload fails.
- The Admin Apply button is still disabled; click enablement remains a later
  slice unless separately approved.

Precise recommended next slice:

- Phase 3, Slice 48: Core UI theme font-scale applier backend.
- Classification: MEDIUM.
- Implement only backend applier registration, safe file mutation, Core reload,
  UI theme resync, rollback, audit-backed apply execution, and focused tests for
  `core:ui_theme:defaults.font-scale-percent`.
- Keep the Admin screen Apply button disabled unless separately approved for UI
  click enablement.

Search terms:

- `core:ui_theme:defaults.font-scale-percent`
- `CoreConfigDescriptors`
- `CoreConfigManager.load`
- `ElarionUiThemeService.syncAll`
- `ElarionConfigApplyRegistry`
- `ElarionConfigPreparedChange`

## Latest Slice Handoff - Phase 3, Slice 48

Slice name:

- Core UI theme font-scale applier backend.

Objective:

- Register the first production backend config applier for
  `core:ui_theme:defaults.font-scale-percent` without enabling the visible
  Admin Apply button.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`
- `platform/core/src/main/java/panetina/elarion/core/config/CoreConfigDefaultFiles.java`
- `platform/core/src/main/java/panetina/elarion/core/config/CoreConfigDescriptors.java`
- `platform/core/src/main/java/panetina/elarion/core/config/CoreConfigManager.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyCapability.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyCoordinator.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyRegistrar.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigPreparedChange.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionConfigApplyService.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionUiThemeService.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionConfigApplyServiceTest.java`
- `platform/core/src/test/java/panetina/elarion/core/config/ElarionConfigApplyCoordinatorTest.java`
- `platform/core/src/test/java/panetina/elarion/core/config/ElarionConfigRegistryTest.java`

Decisions made:

- The only production backend target registered in this slice is
  `core:ui_theme:defaults.font-scale-percent`.
- The applier preserves the current file format by replacing exactly one
  `font-scale-percent:` scalar line instead of dumping the whole YAML map.
- Commit writes through a temp file plus replace, reloads `CoreConfigManager`,
  and invokes a supplied UI theme sync hook.
- Rollback restores exact prior file contents, reloads the previous valid Core
  config, and invokes the same sync hook.
- Core initialization registers the target with the existing canonical apply
  registry and tracks the active server only for post-apply UI theme sync.
- The visible Admin Apply button remains disabled. No client click path was
  enabled.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/config/CoreUiThemeFontScaleConfigApplier.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`
- `platform/core/src/test/java/panetina/elarion/core/config/CoreUiThemeFontScaleConfigApplierTest.java`
- `docs/config.md`
- `docs/api.md`
- `docs/addons/core.md`
- `PLAN.md`
- `TODO.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`

Tests run:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.CoreUiThemeFontScaleConfigApplierTest --tests panetina.elarion.core.service.ElarionConfigApplyServiceTest --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest`
- `.\gradlew.bat :platform:core:test`

Unresolved risks:

- The Admin screen still cannot click Apply.
- Only the font-scale target has a production backend applier.
- Manual in-game verification is still needed for live UI reflow after theme
  sync at different Minecraft GUI scales.
- Realm spawn, server identity, addon config, dynamic definition rows, and
  restart-required config writes remain deferred.

Precise recommended next slice:

- Phase 3, Slice 49: Admin Apply click enablement for the single font-scale
  target.
- Classification: MEDIUM.
- Enable the client Apply button only when server-authored apply availability
  and a matching latest validation result allow it, and keep all other config
  targets disabled.
- Exclude adding any new production config appliers.

Search terms:

- `CoreUiThemeFontScaleConfigApplier`
- `core:ui_theme:defaults.font-scale-percent`
- `core.config.ui_theme.font_scale_changed`
- `ElarionConfigApplyService.apply`
- `sendConfigEditValidation`
- `canApply`

## Latest Slice Handoff - Phase 3, Slice 49

Slice name:

- Single-target Admin Apply click enablement.

Objective:

- Enable Admin Apply click behavior only for the server-authored ready
  `core:ui_theme:defaults.font-scale-percent` target and keep every other
  config target disabled.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditOpenPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditRequestPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditResultPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditPayloadCodecs.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreClient.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionConfigEditClientState.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionConfigEditClientStateTest.java`

Decisions made:

- Config edit packet directions are valid in source:
  `ElarionConfigEditOpenPayload` and `ElarionConfigEditResultPayload` are S2C;
  `ElarionConfigEditRequestPayload` is C2S.
- Server-opened controls now derive `inputEditable` and `applyAvailable` from
  backend apply readiness.
- Validation results now return `canApply=true` only for a target with a ready
  apply backend.
- The Admin client sends `Intent.APPLY` only when the control is
  apply-available and the latest validation result exactly matches target,
  old/current value, and new/proposed value.
- Applied results close the edit shell to avoid stale current values after
  reload/sync.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionConfigEditClientState.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionConfigEditClientStateTest.java`
- `docs/config.md`
- `docs/api.md`
- `docs/addons/core.md`
- `PLAN.md`
- `TODO.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`

Tests run:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest --tests panetina.elarion.core.network.ElarionConfigEditPayloadTest --tests panetina.elarion.core.client.ElarionConfigEditClientStateTest --tests panetina.elarion.core.config.CoreUiThemeFontScaleConfigApplierTest`
- `.\gradlew.bat :platform:core:test`

Phase 3 V1 status:

- Complete for the first safe Admin config edit path:
  read-only descriptor discovery, server-side validation, audit-backed backend
  apply, safe file write/reload/rollback, UI theme resync, and strict client
  Apply gating for `core:ui_theme:defaults.font-scale-percent`.
- Not complete for broad config editing. Addon config domains, Realm spawn,
  server identity, dynamic Core definitions, restart-required entries, and
  generic page/provider contracts remain future slices.

Unresolved risks:

- Manual dev-client QA is still needed to confirm the panel does not disconnect
  with a custom payload error in live play.
- Only the font-scale target can be applied.
- Other config entries must remain disabled until their owner appliers are
  explicitly implemented and tested.

Precise recommended next slice:

- Phase 4, Slice 1: shared UI token/component audit and proposal.
- Classification: SMALL proposal slice.
- Inspect current Core UI primitives, Government/notification style, and
  duplicated literals to define the first UI-token consolidation target.
- Exclude implementation.

Search terms:

- `ElarionConfigEditOpenPayload`
- `ElarionConfigEditRequestPayload`
- `ElarionConfigEditResultPayload`
- `canApplyConfigEdit`
- `sendConfigEditApply`
- `CoreUiThemeFontScaleConfigApplier`

## Latest Slice Handoff - Phase 4, Slice 1

Slice name:

- Shared UI token/component audit and proposal.

Objective:

- Inspect current Core UI primitives, Government civic UI helpers, and
  Notification/Admin styling duplication to define the first safe Phase 4 UI
  consolidation target.

What was inspected:

- `docs/systems/GUI.md`
- `docs/systems/UI_JOURNAL.md`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionUiStyle.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionUiRenderer.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionUiTypography.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionNotificationHud.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/client/GovernmentUiComponents.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/client/GovernmentUiGlyphs.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/client/GovernmentScreenChrome.java`

Decisions made:

- Core already owns base UI primitives and scaled typography; the next step is
  consolidation, not inventing a second UI framework.
- The civic brown/gold visual language should be represented by Core-owned
  generic helpers before migrating more screens.
- Government-specific semantic components should remain in the Government
  addon, but generic surfaces such as row boxes, action buttons, dividers,
  status chips, and attached shells belong in Core.
- The first implementation slice should be additive and should not migrate a
  complete screen.

Exact files changed:

- `docs/reports/UI_SYSTEM_AUDIT.md`
- `docs/systems/GUI.md`
- `docs/systems/UI_JOURNAL.md`
- `INDEX.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Tests run:

- Documentation sanity checks only:
  - `rg -n "UI_SYSTEM_AUDIT|Phase 4, Slice 2|ElarionCivic" PLAN.md TODO.md INDEX.md docs/systems/GUI.md docs/systems/UI_JOURNAL.md docs/reports/UI_SYSTEM_AUDIT.md docs/ai/CURRENT_STATUS.md`
  - `git diff --check -- PLAN.md TODO.md INDEX.md docs/systems/GUI.md docs/systems/UI_JOURNAL.md docs/reports/UI_SYSTEM_AUDIT.md docs/ai/CURRENT_STATUS.md`

Unresolved risks:

- Notification HUD and Admin Panel still have duplicated civic constants and
  row/action drawing logic.
- Government owns the most complete civic helpers today; those helpers should
  not become a cross-addon dependency.
- No live screenshot QA was run in this proposal slice.

Work deliberately deferred:

- Full Notification HUD migration.
- Full Admin Panel migration.
- Moving Government UI wholesale into Core.
- Dev-only UI gallery.
- NPC, Portal, Shrine, Collection, and character UI migrations.

Precise recommended next slice:

- Phase 4, Slice 2: Core civic UI token and primitive helper foundation.
- Classification: MEDIUM.
- Add additive Core helpers for civic colors, attached shell/frame, row
  surface, compact action button, divider, status chip, and font-scale-aware
  row/control metrics.
- Add focused metric tests.
- Exclude complete screen migrations and behavior changes.

Search terms:

- `ElarionUiStyle`
- `ElarionUiRenderer`
- `ElarionUiTypography`
- `GovernmentUiGlyphs`
- `GovernmentUiComponents`
- `GovernmentScreenChrome`
- `CIVIC_ROOT`
- `compactActionButton`
- `rowBox`

## Latest Slice Handoff - Phase 4, Slice 2

Slice name:

- Core civic UI token and primitive helper foundation.

Objective:

- Add additive Core helpers for the civic brown/gold visual language and
  font-scale-aware control metrics without migrating full screens or changing
  behavior.

What was inspected:

- `RULES.md`
- `CODEX.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionUiStyle.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionUiRenderer.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionUiTypography.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ElarionUiTheme.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ui/ElarionUiTypographyTest.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionAdminPanelScreenLayoutTest.java`

Decisions made:

- Core now owns generic civic UI color tokens and primitive drawing helpers.
- `ElarionCivicColors` mirrors the current Government/Notification civic
  palette so future migrations do not visually drift.
- `ElarionCivicUi` is additive and generic: thin boxes, attached shells, row
  surfaces, compact action buttons, dividers, and status chips.
- `ElarionUiMetrics` owns font-scale-aware control/list sizing helpers that
  can be tested without a Minecraft client renderer.
- No existing screen was migrated in this slice to keep behavior stable.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionCivicColors.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionCivicUi.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionUiMetrics.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ui/ElarionUiMetricsTest.java`
- `docs/systems/GUI.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Tests run:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionUiMetricsTest`
- `.\gradlew.bat :platform:core:test`

Unresolved risks:

- Notification HUD still has local civic constants and duplicated row/action
  drawing until a caller migration slice adopts the new helpers.
- Admin Panel still has local danger colors, modal overlays, and action/row
  drawing logic.
- Government still owns the richest semantic civic components; generic pieces
  should move to Core gradually as screens are touched.
- No live screenshot QA was run because no existing rendered screen changed.

Work deliberately deferred:

- Full Notification HUD redesign.
- Full Admin Panel redesign.
- Civic Forum and Seat of Rule migration.
- Dev-only UI gallery.
- NPC, Portal, Shrine, Collection, and character UI migration.

Precise recommended next slice:

- Phase 4, Slice 3: migrate one small existing caller to the new Core civic UI
  helpers.
- Classification: MEDIUM.
- Recommended first target: Notification HUD row/action drawing, because it
  duplicates the same civic constants and remains the most visible rough UI.
- Keep the slice to helper adoption and small visual cleanup. Do not change
  notification packets, persistence, storage, category filtering, action
  semantics, or the whole drawer layout.

Search terms:

- `ElarionCivicColors`
- `ElarionCivicUi`
- `ElarionUiMetrics`
- `ElarionNotificationHud`

## Latest Slice Handoff - Phase 4, Slice 15

Slice name:

- First live server-backed UI screenshot pass.

What was verified:

- `runServer` plus `runClientOne` connected `ElarionAdmin` to saved
  `localhost` without a custom-payload crash.
- Realm notification empty state, Civic History, Create Civic Proposal,
  Shrine, and allowed Neutral Gate confirmation were opened through real
  server-authoritative flows and captured under `build/ui-qa/`.
- Civic's `870 x 519` fallback is bounded; the complete Civic layouts rendered
  after maximizing to `1936 x 1048`.
- Server shutdown completed successfully.

Findings:

- Populated Notification rows/actions/detail remain unverified.
- The Neutral Gate icon frame was empty.
- The Shrine reported completion while aggregate and requirement progress were
  all zero.
- Client logs contain existing model JSON warnings for unsupported rotations;
  no Elarion custom-payload decode failure was found.

State restoration:

- Neutral was relocked.
- Temporary Government and Shrine test blocks were replaced with grass.
- Player position was restored.
- Offering anchor coordinates were restored offline to their exact pre-test
  values `elarion:realm_world_1 @ 3,67,2` in both Offering runtime files.
- Client and server processes were closed.

Exact files changed:

- `PLAN.md`
- `TODO.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`
- Runtime restoration only: `dev/run/world/elarion/addon-state/offerings/state.json`
  and `anchors.json` were returned to their pre-test coordinates.

Recommended next slice:

- Seed Realm notifications through existing domain events and capture populated
  row/action/detail states, then verify `/e panel` config edit validation/apply
  and font-scale resync. Keep any fixes bounded to defects proven by live QA.
- Classification: MEDIUM.
- `CIVIC_ROOT`
- `drawNotificationRow`
- `compactActionButton`
- `drawActionBand`

## Latest Slice Handoff - Phase 4, Slice 3

Slice name:

- Notification HUD helper adoption.

Objective:

- Adopt the new Core civic UI helpers in Notification HUD row/action drawing
  without changing notification packets, persistence, storage, filtering, or
  action semantics.

What was inspected:

- `docs/ai/CURRENT_STATUS.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionNotificationHud.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionCivicUi.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionCivicColors.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionUiMetrics.java`

Decisions made:

- Preserve Notification HUD behavior and layout in this slice.
- Keep the HUD's existing action glyphs and text centering, but draw the button
  surface through a new reusable `ElarionCivicUi.compactActionButtonFrame`
  helper.
- Use `ElarionCivicUi.rowSurface` for rail slots and list rows.
- Keep local Notification color names for now, but alias them to
  `ElarionCivicColors` so Core is the source of the palette.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionNotificationHud.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionCivicUi.java`
- `docs/systems/GUI.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Tests run:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionUiMetricsTest`
- `.\gradlew.bat :platform:core:test`

Unresolved risks:

- Notification shell/header/ornament drawing still has local rendering logic
  and some duplicated civic literals.
- Live screenshot QA was not run in this slice.
- Admin Panel still has duplicated row/action/modal drawing.
- Government still owns richer semantic civic components.

Work deliberately deferred:

- Notification packet/storage/action/filtering changes.
- Full Notification drawer redesign.
- Admin Panel helper migration.
- Government helper migration.
- UI gallery.

Precise recommended next slice:

- Phase 4, Slice 4: continue Notification HUD visual cleanup.
- Classification: MEDIUM.
- Migrate shell/header/ornament drawing and remaining duplicated civic literals
  toward Core helpers, then perform live screenshot QA of the drawer.
- Exclude notification packets, persistence, storage, category filtering, and
  action semantics.

Search terms:

- `ElarionNotificationHud`
- `drawDrawerShell`
- `drawHeaderOrnament`
- `drawRailShell`
- `ElarionCivicUi.thinBox`
- `ElarionCivicUi.rowSurface`
- `ElarionCivicUi.compactActionButtonFrame`

## Latest Slice Handoff - Phase 4, Slice 4

Slice name:

- Notification HUD shell helper adoption.

Objective:

- Continue Notification HUD visual cleanup by moving shell/header/ornament
  drawing and reusable civic literals into Core helpers.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionNotificationHud.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionCivicColors.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionCivicUi.java`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `docs/systems/GUI.md`
- `PLAN.md`
- `TODO.md`

Decisions made:

- Keep Notification HUD packet handling, storage, category filtering, and
  action semantics unchanged.
- Move reusable shell/header/ornament/message drawing into Core helpers rather
  than leaving it as Notification-local procedural code.
- Keep live screenshot QA as a separate next slice because no active Minecraft
  client/screenshot control path was available in this turn.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionNotificationHud.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionCivicColors.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionCivicUi.java`
- `docs/systems/GUI.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Implementation notes:

- Added `ElarionCivicColors` tokens for rail inset/marks, message-body
  surfaces, hover gloss, divider, destructive text/border, and info button
  states.
- Added `ElarionCivicUi` helpers for header shells, rail shells, message
  bodies, header ornaments, close buttons, and reusable dimming.
- Notification HUD now delegates rail shell, drawer shell, header ornaments,
  close button, divider, message body, row surfaces, action button frames, thin
  boxes, and action height to Core civic helpers.

Tests run:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionUiMetricsTest`
- `.\gradlew.bat :platform:core:test`

Not verified:

- Live Notification drawer screenshot QA was not run. Verify in a dev client
  before moving on to Admin Panel helper migration.

Unresolved risks:

- The new shared close button should be visually checked for centered X at the
  actual Minecraft GUI scale.
- Notification rail/header/detail/action states need live screenshot review
  against the reference after the helper migration.
- Admin Panel still has duplicated row/action/modal drawing.

Work deliberately deferred:

- Notification packet/storage/action/filtering changes.
- Full Notification drawer redesign.
- Admin Panel helper migration.
- Government helper migration.
- UI gallery.

Precise recommended next slice:

- Phase 4, Slice 5: live Notification drawer screenshot QA and small visual
  corrections.
- Classification: SMALL.
- Verify rail icons, drawer shell, header title/ornaments, centered close X,
  selected row, unread marker, action footer, empty state, and detail state.
- Exclude notification packets, persistence, storage, category filtering, and
  action semantics.

Search terms:

- `ElarionCivicUi.headerShell`
- `ElarionCivicUi.railShell`
- `ElarionCivicUi.closeButton`
- `ElarionCivicUi.headerOrnament`
- `ElarionCivicUi.messageBody`
- `ElarionNotificationHud`

## Latest Slice Handoff - Phase 4, Slice 5

Slice name:

- Notification HUD layout contract.

Objective:

- Continue the Notification HUD QA path. Live screenshot QA was requested by
  the previous slice, but no active Minecraft client/screenshot control path
  was available, so this slice added bounded automated layout coverage instead.

What was inspected:

- `docs/ai/CURRENT_STATUS.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionNotificationHud.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionNotificationHudLayout.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionNotificationHudLayoutTest.java`

Decisions made:

- Do not claim live screenshot QA without an active dev-client/screenshot path.
- Add a bootstrap-free Notification HUD layout helper for automated geometry
  coverage.
- Keep Notification HUD behavior unchanged; only route layout constants and
  metric helpers through the bootstrap-free helper.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionNotificationHud.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionNotificationHudLayout.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionNotificationHudLayoutTest.java`
- `docs/systems/GUI.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Implementation notes:

- Added `ElarionNotificationHudLayout` with the HUD geometry constants,
  `listX`, `closeX`, `listWidth`, scaled `rowHeight`,
  `actionHeaderHeight`, `actionButtonHeight`, and a `Metrics` record.
- `ElarionNotificationHud` now aliases the tested constants and metric helper
  methods from `ElarionNotificationHudLayout`.
- Added `ElarionNotificationHudLayoutTest` for close-button/header centering,
  list bounds, and font-scale growth of row/action heights.
- A first direct test of `ElarionNotificationHud` failed because the class
  initializes `ItemStack.EMPTY`, which requires Minecraft registry bootstrap in
  plain JVM tests. The final helper avoids that class initialization.

Tests run:

- Failed first attempt:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionNotificationHudLayoutTest`
  before extracting the bootstrap-free helper.
- Passing after fix:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionNotificationHudLayoutTest`
- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionUiMetricsTest`
- `.\gradlew.bat :platform:core:test`

Not verified:

- Live Notification drawer screenshot QA is still pending.

Unresolved risks:

- The shared close button and migrated shell/header states still need real
  Minecraft screenshot review.
- Admin Panel still has duplicated row/action/modal drawing.

Work deliberately deferred:

- Notification packet/storage/action/filtering changes.
- Full Notification drawer redesign.
- Admin Panel helper migration.
- Government helper migration.
- UI gallery.

Precise recommended next slice:

- Phase 4, Slice 6: actual dev-client Notification drawer screenshot QA and
  small visual corrections.
- Classification: SMALL.
- Verify rail icons, drawer shell, header title/ornaments, centered close X,
  selected row, unread marker, action footer, empty state, and detail state.
- Exclude notification packets, persistence, storage, category filtering, and
  action semantics.

Search terms:

- `ElarionNotificationHudLayout`
- `ElarionNotificationHudLayoutTest`
- `layoutMetrics`
- `closeButtonIsCenteredInsideHeaderAndPanelBounds`
- `rowAndActionHeightsGrowWithFontScale`

## Latest Slice Handoff - Phase 4, Slice 6

Slice name:

- Notification HUD QA follow-up.

Objective:

- Run actual dev-client Notification drawer screenshot QA and make small visual
  corrections only.

What was inspected:

- `CODEX.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionNotificationHud.java`

Decisions made:

- Do not claim live screenshot QA without an attached dev client or screenshot
  control path.
- Keep Notification packet, storage, filtering, category, and action semantics
  unchanged.
- Make only one low-risk visual helper-adoption correction: route the
  Notification action-footer divider through `ElarionCivicUi.divider`.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionNotificationHud.java`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Tests run:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionNotificationHudLayoutTest`
- `.\gradlew.bat :platform:core:test`

Not verified:

- Live Notification drawer screenshot QA remains pending.

Unresolved risks:

- Notification drawer still needs real Minecraft screenshot review.
- Admin Panel still has duplicated row/action/modal drawing.

Precise recommended next slice:

- If a dev-client/screenshot path is available: run Notification drawer live
  QA and make only small visual corrections.
- If live QA remains unavailable: begin Admin Panel helper adoption for
  row/action/modal drawing.
- Admin helper-adoption scope classification: MEDIUM.

Search terms:

- `ElarionAdminPanelScreen`
- `ElarionCivicUi`
- `compactButton`
- `danger`
- `drawConfirmModal`
- `drawConfigEditShell`

## Latest Slice Handoff - Phase 4, Slice 7

Slice name:

- Admin Panel helper adoption.

Objective:

- Adopt shared Core civic helpers in Admin Panel row/action/modal surfaces
  without changing Admin config packets, permissions, validation, apply
  behavior, storage, provider actions, or server authority.

What was inspected:

- `docs/ai/CURRENT_STATUS.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `docs/systems/GUI.md`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionAdminPanelScreenLayoutTest.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionConfigEditClientStateTest.java`

Decisions made:

- Keep Admin Panel click targets, packet payloads, config validation/apply
  predicates, provider action dispatch, permissions, storage, and server
  behavior unchanged.
- Use Core civic helpers for repeated row/action/modal visual surfaces instead
  of adding more Admin-local drawing logic.
- Keep live screenshot QA as a separate manual task because no active
  Minecraft client/screenshot control path was available.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `docs/systems/GUI.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Implementation notes:

- Non-danger Admin rows now use `ElarionCivicUi.rowSurface`.
- Danger rows use `ElarionCivicColors` destructive tokens.
- Detail action buttons now use `ElarionCivicUi.compactActionButton`.
- Confirmation modal and config edit shell overlays now use
  `ElarionCivicColors.MODAL_OVERLAY`.
- Confirmation/config modal buttons now use `ElarionCivicUi.compactActionButton`
  with normal, primary, or destructive tones.

Tests run:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionAdminPanelScreenLayoutTest --tests panetina.elarion.core.client.ElarionConfigEditClientStateTest`
- `.\gradlew.bat :platform:core:test`

Not verified:

- Live Admin Panel screenshot QA.
- Live Notification drawer screenshot QA.

Unresolved risks:

- Admin Panel shell/detail panels still have some local drawing patterns and
  need incremental helper adoption or live QA-driven correction.
- Notification drawer still needs real Minecraft screenshot review against the
  reference.

Work deliberately deferred:

- Admin config packet/edit behavior changes.
- New config appliers or config write targets.
- Admin provider/action contract redesign.
- Full Admin Panel redesign.
- Notification packet/storage/action/filtering changes.

Precise recommended next slice:

- If a dev-client/screenshot path is available: run Notification drawer and
  Admin Panel live QA, then make only small visual corrections.
- If live QA remains unavailable: continue Phase 4 helper adoption with Admin
  Panel shell/detail panels or another low-risk Core screen.
- Classification: MEDIUM for another helper-adoption slice.

Search terms:

- `ElarionAdminPanelScreen`
- `ElarionCivicUi.rowSurface`
- `ElarionCivicUi.compactActionButton`
- `ElarionCivicColors.MODAL_OVERLAY`
- `renderDetail`
- `renderConfigEditShell`

## Latest Slice Handoff - Phase 4, Slice 8

Slice name:

- Admin Panel shell and detail helper adoption.

Objective:

- Continue Admin Panel helper adoption by routing the remaining main shell,
  header, list/detail frame, filter/input, and config edit result surfaces
  through shared Core civic helpers.

What was inspected:

- `RULES.md`
- `CODEX.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionCivicUi.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionCivicColors.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionAdminPanelScreenLayoutTest.java`

Decisions made:

- Keep Admin Panel click targets, list/detail geometry constants, packet
  payloads, config validation/apply predicates, provider action dispatch,
  permissions, storage, and server behavior unchanged.
- Use existing Core civic shell/frame helpers instead of adding another local
  Admin drawing abstraction.
- Keep live screenshot QA as a separate manual task because no active
  Minecraft client/screenshot control path was available.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `docs/systems/GUI.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Implementation notes:

- The Admin Panel root frame now uses `ElarionCivicUi.attachedShell`.
- The Admin header now uses shared `ElarionCivicUi.headerOrnament` accents.
- The row list outer frame and filter field now use shared civic thin boxes.
- The detail panel now uses `ElarionCivicUi.headerShell`.
- Confirmation and config edit modal shells now use `ElarionCivicUi.headerShell`.
- Modal text input, config proposed value, and config result surfaces now use
  civic thin/message body helpers.

Tests run:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionAdminPanelScreenLayoutTest --tests panetina.elarion.core.client.ElarionConfigEditClientStateTest`
- `.\gradlew.bat :platform:core:test`

Not verified:

- Live Admin Panel screenshot QA.
- Live Notification drawer screenshot QA.

Unresolved risks:

- Admin Panel visuals still need real Minecraft screenshot review at the
  configured GUI scales.
- Notification drawer still needs real Minecraft screenshot review against the
  reference.
- Collection, character creation, Realm assignment, and other Core screens
  still have older direct panel/list/detail drawing patterns.

Work deliberately deferred:

- Admin config packet/edit behavior changes.
- New config appliers or config write targets.
- Admin provider/action contract redesign.
- Full Admin Panel redesign.
- Collection packet/action/provider behavior changes.
- Notification packet/storage/action/filtering changes.

Precise recommended next slice:

- If a dev-client/screenshot path is available: run Notification drawer and
  Admin Panel live QA, then make only small visual corrections.
- If live QA remains unavailable: continue Phase 4 helper adoption with
  Collection shell/list/detail surfaces or another low-risk Core screen.
- Classification: MEDIUM for another helper-adoption slice.

Search terms:

- `ElarionCollectionScreen`
- `ElarionCivicUi.attachedShell`
- `ElarionCivicUi.headerShell`
- `ElarionCivicUi.rowSurface`
- `renderList`
- `renderDetail`

## Latest Slice Handoff - Phase 4, Slice 9

Slice name:

- Collection shell/list/detail helper adoption.

Objective:

- Adopt shared Core civic helpers in Collection shell/list/detail visual
  surfaces without changing Collection packets, actions, provider behavior,
  unlock state, title activation, mount preview behavior, storage, or server
  authority.

What was inspected:

- `docs/reports/UI_SYSTEM_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionCollectionScreen.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionCollectionScreenLayoutTest.java`
- `platform/core/src/test/java/panetina/elarion/core/network/CollectionPayloadTest.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionCollectionServiceTest.java`

Decisions made:

- Keep Collection packet/action/provider behavior, unlock state, title
  activation, mount preview behavior, storage, and server authority unchanged.
- Adopt shared Core civic helpers only for generic shell/list/detail surfaces.
- Leave Collection-specific active/selected row rendering and icon-frame
  rendering intact because they encode existing Collection visual states.
- Keep live screenshot QA as a separate manual task because no active
  Minecraft client/screenshot control path was available.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionCollectionScreen.java`
- `docs/systems/GUI.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Implementation notes:

- The Collection root frame now uses `ElarionCivicUi.attachedShell`.
- The Collection title header now uses shared `ElarionCivicUi.headerOrnament`
  accents.
- The content header and list frame now use shared civic thin boxes.
- The detail panel now uses `ElarionCivicUi.headerShell`.
- The preview frame and preview body now use civic thin/message body helpers.
- Collection action buttons now use `ElarionCivicUi.compactActionButton`.

Tests run:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionCollectionScreenLayoutTest --tests panetina.elarion.core.network.CollectionPayloadTest --tests panetina.elarion.core.service.ElarionCollectionServiceTest`
- `.\gradlew.bat :platform:core:test`

Not verified:

- Live Collection screenshot QA.
- Live Admin Panel screenshot QA.
- Live Notification drawer screenshot QA.

Unresolved risks:

- Collection visuals still need real Minecraft screenshot review, especially
  active/selected row clarity, icon frames, preview framing, mount rendering,
  and action button tone.
- Character Creation, Realm Assignment, and other Core screens still have older
  direct panel/input/choice drawing patterns.

Work deliberately deferred:

- Collection packet/action/provider behavior changes.
- Unlock state, title activation, or mount preview behavior changes.
- Character lifecycle behavior changes.
- Realm membership behavior changes.
- Notification/Admin live visual corrections.

Precise recommended next slice:

- If a dev-client/screenshot path is available: run Notification drawer,
  Admin Panel, and Collection live QA, then make only small visual corrections.
- If live QA remains unavailable: continue Phase 4 helper adoption with
  Character Creation and Realm Assignment shell/input/choice surfaces.
- Classification: MEDIUM for another helper-adoption slice.

Search terms:

- `CharacterCreationScreen`
- `CharacterRealmAssignmentScreen`
- `ElarionCivicUi.attachedShell`
- `ElarionCivicUi.headerShell`
- `ElarionCivicUi.rowSurface`
- `ElarionCivicUi.compactActionButton`

## Latest Slice Handoff - Phase 4, Slice 10

Slice name:

- Character Creation and Realm Assignment helper adoption.

Objective:

- Adopt shared Core civic helpers in Character Creation and Realm Assignment
  shell/input/choice/button visual surfaces without changing character
  lifecycle behavior, Realm membership behavior, packets, persistence,
  commands, or server authority.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/client/CharacterCreationScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/CharacterRealmAssignmentScreen.java`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`
- `platform/core/src/test/java/panetina/elarion/core/network/CharacterCreationPayloadTest.java`
- `platform/core/src/test/java/panetina/elarion/core/service/CharacterLifecycleServiceTest.java`

Decisions made:

- Keep character lifecycle behavior, Realm membership behavior, packets,
  persistence, commands, and server authority unchanged.
- Use existing Core civic helpers for shell/header/input/choice/button
  surfaces instead of introducing new screen-local drawing helpers.
- Keep live screenshot QA as a separate manual task because no active
  Minecraft client/screenshot control path was available.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/CharacterCreationScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/CharacterRealmAssignmentScreen.java`
- `docs/systems/GUI.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Implementation notes:

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

Tests run:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.network.CharacterCreationPayloadTest --tests panetina.elarion.core.service.CharacterLifecycleServiceTest`
- `.\gradlew.bat :platform:core:test`

Not verified:

- Live Character Creation screenshot QA.
- Live Realm Assignment screenshot QA.
- Live Collection screenshot QA.
- Live Admin Panel screenshot QA.
- Live Notification drawer screenshot QA.

Unresolved risks:

- Character Creation biography clipping/scroll hints and cooldown state need
  real Minecraft screenshot review.
- Realm Assignment option row clarity needs real Minecraft screenshot review.
- Portal Confirmation, Grave Recovery, NPC dialogue, Shrine, and addon screens
  still have older direct panel/list/action drawing patterns.

Work deliberately deferred:

- Character lifecycle behavior changes.
- Realm assignment behavior changes.
- Packet, command, persistence, or server-authority changes.
- Live visual corrections for Notification, Admin, Collection, Character
  Creation, and Realm Assignment.

Precise recommended next slice:

- If a dev-client/screenshot path is available: run live QA for Notification
  drawer, Admin Panel, Collection, Character Creation, and Realm Assignment,
  then make only small visual corrections.
- If live QA remains unavailable: continue Phase 4 helper adoption with a small
  addon screen pair such as Portal Confirmation and Grave Recovery shell
  surfaces.
- Classification: MEDIUM for another helper-adoption slice.

Search terms:

- `PortalConfirmationScreen`
- `GraveRecoveryScreen`
- `ElarionCivicUi.attachedShell`
- `ElarionCivicUi.headerShell`
- `ElarionCivicUi.rowSurface`
- `ElarionCivicUi.compactActionButton`

## Latest Slice Handoff - Phase 4, Slice 11

Slice name:

- Portal Confirmation and Grave Recovery helper adoption.

Objective:

- Adopt shared Core civic helpers in Portal Confirmation and Grave Recovery
  shell/body/status/slot/action visual surfaces without changing portal travel
  behavior, grave recovery behavior, packets, persistence, commands, inventory
  mutation, or server authority.

What was inspected:

- `addons/portals/src/main/java/panetina/elarion/addons/portals/client/PortalConfirmationScreen.java`
- `addons/underworld/src/main/java/panetina/elarion/addons/underworld/client/GraveRecoveryScreen.java`
- `docs/addons/portals.md`
- `docs/addons/underworld.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`
- `addons/underworld/src/test/java/panetina/elarion/addons/underworld/network/GraveOpenPayloadTest.java`
- Portal addon focused tests under `addons/portals/src/test/java`.

Decisions made:

- Keep portal travel behavior, grave recovery behavior, packets, persistence,
  commands, inventory mutation, and server authority unchanged.
- Use existing Core civic helpers for shell/header/body/status/slot/action
  surfaces instead of adding screen-local drawing helpers.
- Keep live screenshot QA as a separate manual task because no active
  Minecraft client/screenshot control path was available.

Exact files changed:

- `addons/portals/src/main/java/panetina/elarion/addons/portals/client/PortalConfirmationScreen.java`
- `addons/underworld/src/main/java/panetina/elarion/addons/underworld/client/GraveRecoveryScreen.java`
- `docs/addons/portals.md`
- `docs/addons/underworld.md`
- `docs/systems/GUI.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Implementation notes:

- Portal Confirmation now uses `ElarionCivicUi.attachedShell`, shared header
  ornaments, a civic message body, a civic gate icon frame, and civic action
  buttons.
- Portal Confirmation still sends only the existing
  `PortalTravelConfirmPayload` when the server-authored prompt allows travel.
- Grave Recovery now uses `ElarionCivicUi.attachedShell`, shared header
  ornaments, `statusChip`, civic item-slot frames, and civic action buttons.
- Grave Recovery still decodes/display items client-side only for rendering and
  sends only the existing `GraveRecoverPayload` for server-side recovery.

Tests run:

- `.\gradlew.bat :addons:portals:test --tests panetina.elarion.addons.portals.network.PortalRouteStatusSyncPayloadTest :addons:underworld:test --tests panetina.elarion.addons.underworld.network.GraveOpenPayloadTest`
- `.\gradlew.bat :addons:portals:test :addons:underworld:test`

Not verified:

- Live Portal Confirmation screenshot QA.
- Live Grave Recovery screenshot QA.
- Live Character Creation screenshot QA.
- Live Realm Assignment screenshot QA.
- Live Collection screenshot QA.
- Live Admin Panel screenshot QA.
- Live Notification drawer screenshot QA.

Unresolved risks:

- Portal Confirmation needs screenshot review for allowed/blocked button state,
  gate icon frame, prompt body spacing, and dynamic route dimensions.
- Grave Recovery needs screenshot review for item-slot grid density, item
  tooltips, scroll indicator placement, status chip clarity, and footer button
  spacing.
- NPC Dialogue and Shrine UI still have older direct panel/list/action drawing
  patterns and should be inspected before migration.

Work deliberately deferred:

- Portal travel behavior changes.
- Grave recovery behavior or inventory mutation changes.
- Packet, command, persistence, or server-authority changes.
- Live visual corrections for all migrated UI screens.

Precise recommended next slice:

- If a dev-client/screenshot path is available: run live QA for the migrated
  Core/addon screens, then make only small visual corrections.
- If live QA remains unavailable: continue Phase 4 helper adoption with another
  bounded addon screen target, likely NPC Dialogue or Shrine UI shell/action
  surfaces after a short proposal/audit of their local drawing complexity.
- Classification: MEDIUM for another helper-adoption slice.

Search terms:

- `NpcDialogueScreen`
- `ShrineOfFoundationScreen`
- `ElarionCivicUi.attachedShell`
- `ElarionCivicUi.headerShell`
- `ElarionCivicUi.rowSurface`
- `ElarionCivicUi.compactActionButton`

## Latest Slice Handoff - Phase 4, Slice 12

Slice name:

- NPC Dialogue and Shrine UI helper-adoption proposal.

Objective:

- Inspect NPC Dialogue and Shrine UI local drawing complexity and choose the
  next bounded helper-adoption target without changing production code.

What was inspected:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcDialogueScreen.java`
- `addons/offerings/src/main/java/panetina/elarion/addons/offerings/client/ShrineOfFoundationScreen.java`
- `docs/addons/npcs.md`
- `docs/addons/offerings.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`

Decisions made:

- NPC Dialogue is the safer next implementation target because it already uses
  Core UI primitives and has a narrow visual-only path for shell/header/options
  footer and numeric prompt surfaces.
- Shrine UI is deferred because it has denser local drawing across project
  summary, progress, tabs, requirements, history, numeric prompt, rewards,
  reward slots, tooltips, icons, scrollbars, and contribution messages.
- The next implementation slice must not change NPC dialogue semantics,
  packets, portrait rendering, typing phases, saved scroll/selection,
  prompt validation, server-side condition/action evaluation, persistence,
  commands, or server authority.

Exact files changed:

- `docs/reports/UI_SYSTEM_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Tests run:

- Documentation sanity checks only:
  - `rg -n "Phase 4, Slice 12|NPC Dialogue|Shrine UI|Phase 4, Slice 13" docs/reports/UI_SYSTEM_AUDIT.md PLAN.md TODO.md docs/ai/CURRENT_STATUS.md`
  - `git diff --check -- docs/reports/UI_SYSTEM_AUDIT.md PLAN.md TODO.md docs/ai/CURRENT_STATUS.md`

Not verified:

- Live screenshot QA was not run.
- No production code was changed.

Unresolved risks:

- NPC Dialogue has behavior-sensitive presentation state: typing phases, saved
  scroll/selection, numeric prompts, currency badge, cards, relationship hearts,
  portrait rendering, sounds, and server-authoritative option validation.
- Shrine UI remains a future helper-adoption target and will need a narrower
  implementation slice because it packs many row/prompt/reward variants into
  one screen.

Work deliberately deferred:

- NPC Dialogue implementation.
- Shrine UI implementation.
- Live visual corrections for all migrated screens.

Precise recommended next slice:

- Phase 4, Slice 13: NPC Dialogue shell/options/footer/prompt helper adoption.
- Classification: MEDIUM.
- Change only `NpcDialogueScreen` and relevant docs.
- Keep NPC portrait rendering, dialogue boxes, relation hearts, currency badge,
  cards, typing phases, sounds, option selection state, virtual-list scroll
  behavior, prompt validation, packet sends, condition/action evaluation,
  persistence, commands, and server authority unchanged.
- Exclude Shrine UI from this implementation slice.

Search terms:

- `NpcDialogueScreen`
- `renderOptions`
- `renderFooter`
- `renderPromptOverlay`
- `ElarionCivicUi.attachedShell`
- `ElarionCivicUi.compactActionButton`

## Latest Slice Handoff - Phase 4, Slice 13

Slice name:

- NPC Dialogue helper adoption.

Objective:

- Adopt shared Core civic helpers in NPC Dialogue shell/options/footer/prompt
  visual surfaces without changing dialogue packets, server validation,
  condition/action evaluation, portrait rendering, typing phases, sounds, saved
  scroll state, prompt semantics, persistence, commands, or server authority.

What was inspected:

- `docs/systems/GUI.md`
- `docs/addons/npcs.md`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcDialogueScreen.java`
- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/client/ui/ElarionConversationControllerTest.java`
- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/network/NpcDialoguePromptSubmitPayloadTest.java`

Decisions made:

- Keep NPC portrait rendering, dialogue boxes, relation hearts, currency badge,
  cards, typing phases, sounds, option selection state, virtual-list scroll
  behavior, prompt validation, packet sends, condition/action evaluation,
  persistence, commands, and server authority unchanged.
- Use Core civic helpers only for generic shell/options/footer/prompt surfaces.
- Leave Shrine UI untouched in this implementation slice.
- Keep live screenshot QA as a separate manual task because no active
  Minecraft client/screenshot control path was available.

Exact files changed:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcDialogueScreen.java`
- `docs/addons/npcs.md`
- `docs/systems/GUI.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Implementation notes:

- NPC Dialogue root now uses `ElarionCivicUi.attachedShell`.
- Dialogue option rows now use `ElarionCivicUi.compactActionButton` with muted,
  normal, or primary tone based on existing input/selection state.
- Footer Close now uses `ElarionCivicUi.compactActionButton`.
- Numeric prompt overlay now uses `ElarionCivicUi.headerShell`.
- Numeric prompt input now uses `ElarionCivicUi.thinBox`.

Tests run:

- `.\gradlew.bat :addons:npcs:test --tests panetina.elarion.addons.npcs.client.ui.ElarionConversationControllerTest --tests panetina.elarion.addons.npcs.network.NpcDialoguePromptSubmitPayloadTest`
- `.\gradlew.bat :addons:npcs:test`

Not verified:

- Live NPC Dialogue screenshot QA.
- Live migrated-screen screenshot QA for previous Phase 4 screens remains
  pending.

Unresolved risks:

- NPC Dialogue needs screenshot review for dynamic configured sizes, option
  row tone, prompt overlay fit, relation hearts, currency badge, cards, and
  scrollbars.
- Shrine UI remains a future helper-adoption target and should start with a
  narrow shell/header/close/numeric prompt slice.

Work deliberately deferred:

- Shrine UI migration.
- NPC dialogue behavior changes.
- Packet, command, persistence, or server-authority changes.
- Live visual corrections for migrated screens.

Precise recommended next slice:

- If live QA becomes available: run live QA for migrated Core/addon screens and
  make only small visual corrections.
- If live QA remains unavailable: continue Phase 4 helper adoption with Shrine
  UI shell/header/close/numeric prompt surfaces only.
- Classification: MEDIUM for another helper-adoption slice.
- Exclude Offering donation behavior, milestone behavior, reward rendering
  semantics, row icon semantics, packets, persistence, commands, inventory
  mutation, and server authority.

Search terms:

- `ShrineOfFoundationScreen`
- `renderNumericPrompt`
- `renderClose`
- `renderListBackground`
- `renderSummary`
- `ElarionCivicUi.attachedShell`
- `ElarionCivicUi.compactActionButton`

## Latest Slice Handoff - Phase 4, Slice 14

Slice name:

- Live screenshot QA capture path.

Objective:

- Establish a repeatable Windows dev-client screenshot capture path before
  continuing visual helper adoption.

What was inspected:

- `CODEX.md`
- `dev/build.gradle`
- `docs/systems/GUI.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/test-commands.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- Running Windows process/window state for the dev client.

Decisions made:

- Use `.\gradlew.bat runClientOne` as the default full-mod dev-client launch
  path for UI QA. It starts `ElarionAdmin` through the `dev` module.
- For real UI-state QA, also start `.\gradlew.bat runServer` and connect from
  Multiplayer using the saved `localhost` server entry. The main-menu capture
  only proves the screenshot mechanism.
- Use a local PowerShell helper under `dev/tools/` for window capture instead
  of keeping ad hoc snippets in handoff notes.
- Use `PrintWindow` by default because raw screen capture can include unrelated
  overlapping desktop windows.
- Keep `build/ui-qa/` as disposable local evidence unless a screenshot is
  intentionally promoted to `docs/ui/` as a canonical reference.

Exact files changed:

- `dev/tools/capture-minecraft-window.ps1`
- `CODEX.md`
- `docs/systems/GUI.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/test-commands.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `INDEX.md`
- `TODO.md`
- `PLAN.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Started the full dev client with `.\gradlew.bat runClientOne --console=plain`
  through a detached wrapper.
- Confirmed the dev client reached a `Minecraft* 1.21.1` window with the full
  Elarion mod set loaded.
- Confirmed raw `CopyFromScreen` capture can be polluted by another overlapping
  desktop app.
- Confirmed direct script execution is blocked by local PowerShell policy.
- Confirmed the documented bypass invocation works:
  `powershell -NoProfile -ExecutionPolicy Bypass -File .\dev\tools\capture-minecraft-window.ps1 -Output build\ui-qa\script-verified-main-menu.png`.
- Confirmed `PrintWindow` capture produced a clean Minecraft main-menu image:
  `build/ui-qa/script-verified-main-menu.png`.

Not verified:

- Notification drawer, Admin Panel, Collection, Character Creation, Realm
  Assignment, Portal Confirmation, Grave Recovery, NPC Dialogue, and Shrine UI
  screenshots were not yet captured in their target states.

Unresolved risks:

- The helper captures windows; it does not automate opening specific Elarion UI
  states.
- Driver-specific setups may return black images from `PrintWindow`; the script
  documents `-ScreenCapture` as the fallback when the Minecraft window is
  visible.
- Future dev-only UI gallery/test hooks are still needed for fully repeatable
  no-gameplay UI states.

Work deliberately deferred:

- Any production UI rendering changes.
- Any packet, config, command, persistence, or server-authority changes.
- Visual corrections for migrated screens.

Live screen access notes:

- Admin Panel: `/e panel`.
- Character Menu / unlockables: `/charactermenu` preferred, `/charactermenu`
  compatibility alias, or the Character Menu keybind.
- Notification drawer: HUD notification rail.
- Shrine UI: right-click a linked Shrine of Foundation block.
- Civic Forum / Seat of Rule: right-click their Government blocks.
- NPC Dialogue: interact with a placed NPC; setup helpers include
  `/e npc place <npcDefinition> here`, `/e npc list`, and `/e npc tp <npcId>`.
- Portal Confirmation: interact with a configured/unlocked portal route while
  the server route/window state allows prompting.
- Grave Recovery: interact with an Underworld grave/tomb after creating or
  locating a corpse with Underworld test/admin commands.
- These flows are server-authoritative. Do not bypass them with client-only
  screen construction when testing action behavior.

Precise recommended next slice:

- Run the first live migrated-screen QA pass using
  the dev-server path plus `dev/tools/capture-minecraft-window.ps1`.
- Start with Notification drawer and the Admin Panel config edit path because
  they have recent shared-helper changes and the Admin path also needs custom
  payload crash verification.
- Classification: MEDIUM.
- Keep fixes limited to small visual corrections if the screenshots reveal
  clear defects.

Search terms:

- `capture-minecraft-window.ps1`
- `runClientOne`
- `Notification drawer`
- `ElarionAdminPanelScreen`
- `ElarionNotificationHud`

## Latest Slice Handoff - Admin Panel Payload Hardening

Slice name:

- Admin Panel Config crash fix, scoped descriptor rows, action suggestions,
  and faster live QA driver.

Objective:

- Stop `/e panel` and Config-tab custom payload disconnects, add an OP player
  Realm assignment action, add Tab completion for server-authored Admin Panel
  action inputs, and speed up live command/screenshot QA.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ElarionAdminPanelAction.java`
- `platform/core/src/main/java/panetina/elarion/core/network/AdminPanelOpenPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/service/RealmService.java`
- `platform/core/src/main/java/panetina/elarion/core/service/AbilityService.java`
- `platform/core/src/main/java/panetina/elarion/core/service/TitleService.java`
- `addons/mounts/src/main/java/panetina/elarion/addons/mounts/service/MountAdminPanelProvider.java`
- `addons/mounts/src/main/java/panetina/elarion/addons/mounts/entity/ElarionMountType.java`
- Focused Admin Panel/Core/Mount tests and docs listed below.

Decisions made:

- Keep Config discovery inside the existing Admin Panel snapshot model but make
  it scoped: Config opens with domain/category summaries only; selecting a
  category requests that category's entry rows.
- Add packet write-side caps for Admin Panel tabs, rows, row actions, and
  action suggestions so future oversized snapshots cannot desynchronize the
  client custom payload stream.
- Extend `ElarionAdminPanelAction` with server-authored input suggestions
  instead of hard-coding client-side lists.
- Add Tab completion only inside the existing single-field modal. The client
  cycles supplied suggestions and still sends only the submitted value; the
  server remains authoritative.
- Add Core `Set Realm` as an OP-only player action routed through
  `RealmService.assign`, followed by identity sync and admin history/event
  emission.
- Add mount ID suggestions from the Mounts provider instead of Core importing
  Mounts implementation details.
- Add `dev/tools/minecraft-qa.ps1` as a wrapper around focus/maximize, command
  sending, client-area clicks, and screenshot capture.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/model/ElarionAdminPanelAction.java`
- `platform/core/src/main/java/panetina/elarion/core/network/AdminPanelOpenPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `addons/mounts/src/main/java/panetina/elarion/addons/mounts/service/MountAdminPanelProvider.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`
- `platform/core/src/test/java/panetina/elarion/core/network/AdminPanelPayloadTest.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionAdminPanelScreenLayoutTest.java`
- `dev/tools/minecraft-qa.ps1`
- `CODEX.md`
- `docs/test-commands.md`
- `docs/systems/GUI.md`
- `docs/addons/core.md`
- `docs/config.md`
- `docs/systems/UI_JOURNAL.md`
- `INDEX.md`
- `TODO.md`
- `PLAN.md`
- `docs/ai/CURRENT_STATUS.md`

APIs/packets changed:

- `ElarionAdminPanelAction` now includes bounded
  `parameterSuggestions`.
- `AdminPanelOpenPayload` now serializes those suggestions and caps tab, row,
  action, and suggestion lists before writing.

Config/persistence affected:

- No config files, parsed config formats, runtime state files, or persistence
  schemas changed.
- Config descriptor display behavior changed only in Admin Panel browsing:
  entry rows load by selected category instead of all at once.

Verification:

- Passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest --tests panetina.elarion.core.network.AdminPanelPayloadTest --tests panetina.elarion.core.client.ElarionAdminPanelScreenLayoutTest`
- Passed:
  `.\gradlew.bat :addons:mounts:compileJava`
- Live patched `runServer`/`runClientOne` QA passed:
  `/e panel` opened, Config tab clicked, `Core: UI Theme` category loaded,
  Players tab opened, and Set Realm modal Tab-completed a Realm ID.
- No `custom_payload`, `DecoderException`, or `Internal Exception` entries
  appeared in the checked client log after the Config/category clicks.

Live evidence:

- `build/ui-qa/admin-config-overview-fixed.png`
- `build/ui-qa/admin-config-tab-fixed.png`
- `build/ui-qa/admin-config-category-fixed.png`
- `build/ui-qa/admin-players-set-realm-fixed.png`
- `build/ui-qa/admin-set-realm-tab-complete.png`

Unresolved risks:

- Font-scale config Apply still needs live validation/apply QA.
- Provider-contributed suggestions were compiled for Mounts but not manually
  opened in the live action modal yet.
- The Admin Panel still has many actions in a single player detail list; future
  provider page/category work should improve discoverability.

Work deliberately deferred:

- New config appliers.
- Broad Admin Panel redesign.
- Config persistence migrations.
- Offline player Realm reassignment.
- Additional autocomplete widgets beyond the current modal Tab cycling.

Precise recommended next slice:

- Continue Phase 3 Admin config edit verification: open
  `core:ui_theme:defaults.font-scale-percent`, validate and apply a safe value,
  confirm UI theme sync/reflow, and confirm non-applier entries keep Apply
  disabled.
- Also scroll the Players action list and open one Mount action to verify mount
  ID suggestions in the modal.
- Classification: SMALL to MEDIUM.

Search terms:

- `configRows(`
- `shouldRequestScopedRows`
- `parameterSuggestions`
- `set_realm`
- `minecraft-qa.ps1`

## Admin Config Apply Live Verification Handoff

Slice name:

- Admin Config apply, non-applier gating, and provider suggestion live QA.

What was inspected:

- Core UI theme font-scale applier and tests.
- Admin Panel scoped Config rows, edit shell, player action list, payload
  bounds, and server-authored suggestions.
- Mount Admin Panel provider suggestions.
- Live `runServer`/`runClientOne` client/server logs and screenshots.

Decisions and implementation:

- Keep the only production Apply target limited to
  `core:ui_theme:defaults.font-scale-percent`.
- Repair stale development `ui_theme.yml` files missing that scalar by
  inserting it into the existing `defaults` block.
- Continue rejecting duplicate scalar lines without file mutation.
- Add mouse-wheel posting to `dev/tools/minecraft-qa.ps1` so long list QA is
  repeatable.

Exact files changed in this continuation:

- `platform/core/src/main/java/panetina/elarion/core/config/CoreUiThemeFontScaleConfigApplier.java`
- `platform/core/src/test/java/panetina/elarion/core/config/CoreUiThemeFontScaleConfigApplierTest.java`
- `dev/tools/minecraft-qa.ps1`
- `CODEX.md`
- `docs/test-commands.md`
- `docs/systems/GUI.md`
- `docs/config.md`
- `docs/addons/core.md`
- `PLAN.md`
- `TODO.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.CoreUiThemeFontScaleConfigApplierTest --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest --tests panetina.elarion.core.network.AdminPanelPayloadTest --tests panetina.elarion.core.client.ElarionAdminPanelScreenLayoutTest :addons:mounts:compileJava`
- Live Apply passed from `100` to `125`, reflowed/resynced the Admin Panel,
  and passed again restoring `125` to `100`.
- Live non-applier Apply remained disabled/non-mutating.
- Live Mount action Tab completion filled `airship` from Mount-owned
  suggestions.
- No custom-payload decoder/encoder/disconnect error appeared in checked logs.
  Unrelated invalid item model rotation errors remain outside this slice.

Live evidence:

- `build/ui-qa/font-apply-fixed-applied-125.png`
- `build/ui-qa/font-apply-restored-100.png`
- `build/ui-qa/config-non-applier-apply-disabled.png`
- `build/ui-qa/admin-players-mount-actions.png`
- `build/ui-qa/admin-grant-mount-tab-complete.png`

Unresolved risks and deferred work:

- Admin player actions still share one long list; provider page/category
  contracts remain future Phase 3/4 work.
- Danger-row confirmation-modal QA remains pending.
- No new config appliers, persistence migrations, offline-player assignment,
  or broad Admin Panel redesign were added.

Precise recommended next slice:

- Phase 4, Slice 16: populated Notification drawer live QA and bounded visual
  corrections.
- Seed notifications through existing authoritative events and capture rows,
  unread/selected states, action footer, detail view, reward tooltips, and
  empty-category behavior.
- Classification: MEDIUM.
- Avoid changing notification storage, filtering, packet contracts, or action
  semantics unless a live defect requires it.

## Populated Notification Live QA Handoff

Slice name:

- Phase 4, Slice 16: populated Notification drawer live QA and bounded visual
  corrections.

What was inspected:

- Core Notification rail, compact rows, action band, detail layout, reward
  previews/tooltips, and empty-category layout.
- `runServer`/`runClientOne` authoritative notification snapshots.
- Windows screenshot/input helper behavior at a maximized `1936 x 1048`
  Minecraft window.

Decisions and implementation:

- Keep selected rows visibly green, matching the accepted live selection
  language, while keeping local View and Go To actions neutral.
- Reserve the green primary button tone for Claim, Accept, and Approve.
- Fit short details to content; retain the maximum bounded viewport and scroll
  behavior for long details.
- Do not extend an empty drawer to a lower rail pointer. Hide only the pointer
  that cannot meet the drawer and keep the selected rail slot visible.
- Move the native cursor in the QA helper for real hover/tooltip polling and
  preserve maximized windows across later focus/input actions.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionNotificationHud.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionNotificationHudLayout.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionNotificationHudLayoutTest.java`
- `dev/tools/minecraft-qa.ps1`
- `CODEX.md`
- `docs/test-commands.md`
- `docs/systems/GUI.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :platform:core:test`.
- Live evidence:
  `build/ui-qa/notification-realm-selected-unread.png`,
  `build/ui-qa/notification-personal-selected-patched.png`,
  `build/ui-qa/notification-reward-detail-patched.png`,
  `build/ui-qa/notification-reward-tooltip-patched.png`, and
  `build/ui-qa/notification-world-empty-final.png`.
- Reward tooltip displayed `Diamond Sword` and `Sharpness III`.
- Notification, reward-grant, and July history files were restored to their
  pre-seed hashes after the live pass.
- No Elarion custom-payload encode/decode failure occurred. The final log has
  one vanilla duplicate-connect disconnect encoder warning caused by the
  automation double-click.

Unresolved risks and deferred work:

- Collection, Character/Realm, Portal blocked state, Grave Recovery, NPC
  Dialogue, and contradictory completed-Shrine live QA remain pending.
- No notification packet, storage, filter, persistence schema, or action
  semantic change was made.

Precise recommended next slice:

- Phase 4, Slice 17: Collection live screenshot QA and bounded visual
  corrections.
- Verify Mounts/Pets/Titles tabs, selected and active rows, icon frames, live
  mount preview, hidden scrolling, actions, and title activation.
- Classification: MEDIUM.

## Latest Slice Handoff - Option A UI Reference Expansion

Date: 2026-07-07.

Objective:

- Expand the approved first UI concept into a complete visual reference set
  and place all requested screen families into the project-wide revamp plan.

What was inspected:

- Government Civic Forum/inner-flow/Seat of Rule reference boards under
  `docs/ui/government/`.
- Current Phase 4 UI audit, GUI contract, UI journal, plan, TODO, index, and AI
  handoff.
- Generated board dimensions and completeness.

Decisions:

- Option A is the approved visual direction.
- At the time of this reference-only slice, `Character Menu` was the proposed
  player-facing name. Slice 17D later implemented it as the visible shell label
  and `/charactermenu` alias while preserving Collection internals.
- Profile data is aggregated by Core from server-authorized addon contributors;
  addons retain canonical state and enforce visibility.
- Character creation and balanced Realm placement are separate stages.
- Portals use scheduled, neutral, and fee ticket pop-ups only; no Portal menu.
- Grave Recovery is separate.
- Quest NPC, banker, trader entry, and trade use distinct role-specific
  surfaces over shared primitives.
- Generic event pop-ups reuse existing event/notification ownership.

Exact files added:

- `docs/ui/revamp-option-a/README.md`
- `docs/ui/revamp-option-a/00-option-a-overview.png`
- `docs/ui/revamp-option-a/01-citizen-ledger-profile.png`
- `docs/ui/revamp-option-a/02-citizen-ledger-unlockables.png`
- `docs/ui/revamp-option-a/03-character-creation.png`
- `docs/ui/revamp-option-a/04-balanced-realm-placement.png`
- `docs/ui/revamp-option-a/05-shrine-offerings.png`
- `docs/ui/revamp-option-a/06-npc-quest-dialogue.png`
- `docs/ui/revamp-option-a/07-npc-banker.png`
- `docs/ui/revamp-option-a/08-npc-trader-dialogue.png`
- `docs/ui/revamp-option-a/09-npc-trade-screen.png`
- `docs/ui/revamp-option-a/10-portal-ticket-popups.png`
- `docs/ui/revamp-option-a/11-grave-recovery.png`
- `docs/ui/revamp-option-a/12-admin-panel.png`
- `docs/ui/revamp-option-a/13-generic-event-popups.png`

Exact existing files changed:

- `PLAN.md`
- `PLANS.md`
- `TODO.md`
- `INDEX.md`
- `docs/systems/GUI.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- All 13 detailed boards are `1536x1024`; the original overview is
  `1672x941`.
- No production source, config, packet, persistence, or runtime behavior was
  changed, so no Gradle task was required.

Unresolved risks and deferred work:

- Broader Profile shell structure and compatibility strategy beyond the current
  `Character Menu` label/alias.
- Profile contributor contracts, visibility rules, and bounded indexes.
- Exact migration sequence and screen-specific implementation proposals.
- Live screenshot comparison only begins after one screen family is migrated.

Precise recommended next slice:

- Phase 4, Slice 17A: documentation-only Collection/player-hub architecture
  audit. Map existing contracts and data owners, then propose the first bounded
  implementation slice. Classification: MEDIUM.

Reference correction after review:

- Replaced `05-shrine-offerings.png` with a visual revamp that preserves the
  current live Shrine layout and interaction structure.
- Replaced `06-npc-quest-dialogue.png` with a simple conversation window.
- Replaced `07-npc-banker.png` with a compact Deposit/Withdraw window.
- Shrine feature expansion and NPC/banker dashboard complexity are explicitly
  excluded from the future migration.

## Latest Slice Handoff - Phase 4, Slice 17A

Date: 2026-07-07.

Objective:

- Audit the current Collection contract and define the future Character Menu
  boundary without changing production code.

What was inspected:

- Root authority and architecture docs.
- Core Collection service, models, payloads, screen, client/keybind, command,
  providers, and tests.
- Mount Collection provider/storage.
- Core citizen/title/Realm/history sources and addon-owned Quest, Offering,
  NPC, Government, Group-adjacent, Economy, and Underworld profile candidates.

Decisions:

- `Character Menu` is the player-facing shell name as of Slice 17D.
- Existing Collection APIs, packet IDs, config/runtime filenames, resources,
  and provider contracts remain the internal Unlockables subsystem initially.
- `/charactermenu` is preferred while `/charactermenu` remains an alias as of Slice 17D.
- Profile is a separate read-only contributor contract with server-filtered
  visibility and lazy bounded requests.
- Core identity/Realm/title and bounded recent history are viable first
  projections. Quest completion, Offering score, NPC reputation, and lifetime
  deaths require owner-maintained summaries first.

Key finding:

- `CollectionOpenPayload` decodes bounded tab/entry/action counts but encodes
  provider list sizes without applying matching bounds. This can produce a
  custom-payload decode failure when future providers grow.

Exact files added:

- `docs/reports/CITIZEN_LEDGER_AUDIT.md`

Exact files changed:

- `docs/architecture/DEPENDENCY_GRAPH.md`
- `OPTIMIZATION_TRACKER.md`
- `INDEX.md`
- `docs/systems/GUI.md`
- `TODO.md`
- `PLAN.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Source-backed documentation review and targeted `rg` searches completed.
- `git diff --check` passed for every document changed by this audit.
- No production code changed, so no Gradle task was required.

Unresolved risks:

- Collection outbound packet bounds.
- No profile visibility/contributor API.
- No scalable summaries for several requested profile fields.
- Duplicate Collection provider IDs currently replace silently.

Precise recommended next slice:

- Phase 4, Slice 17B: harden the Collection snapshot wire boundary with
  matching outbound limits, safe string/ID handling, valid selection fallback,
  and focused tests. Classification: SMALL.

## Latest Slice Handoff - Phase 4, Slice 17B

Date: 2026-07-07.

Objective:

- Prevent future Collection providers from causing custom-payload failures
  through oversized or unsafe snapshots.

Implementation:

- `CollectionOpenPayload` creates a wire-safe snapshot before encoding.
- Caps are 32 tabs, 512 entries per tab, and 16 actions per entry, matching the
  decoder.
- Blank, leading/trailing-whitespace, unsafe-control, oversized, and duplicate
  IDs are omitted per scope.
- Existing bounded string encoding remains responsible for display text.
- Invalid or out-of-range selected tabs fall back to the first transmitted tab.

Exact production file changed:

- `platform/core/src/main/java/panetina/elarion/core/network/CollectionOpenPayload.java`

Exact test file changed:

- `platform/core/src/test/java/panetina/elarion/core/network/CollectionPayloadTest.java`

Exact documentation files changed:

- `docs/systems/Networking.md`
- `docs/systems/GUI.md`
- `docs/reports/CITIZEN_LEDGER_AUDIT.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `OPTIMIZATION_TRACKER.md`
- `INDEX.md`
- `TODO.md`
- `PLAN.md`
- `docs/ai/CURRENT_STATUS.md`

Compatibility and authority:

- No packet ID, public API, command, config, persistence, Collection action,
  unlock state, or UI layout changed.
- Server-side provider action validation remains authoritative.

Verification:

- Passed focused `CollectionPayloadTest` and
  `ElarionCollectionServiceTest`.
- Passed full `:platform:core:test`.

Deferred:

- Character Menu naming and `/charactermenu` alias.
- Profile visibility/contributor contracts.
- Collection visual redesign.
- Duplicate provider registration policy remains separate from wire safety.

Precise recommended next slice:

- Phase 4, Slice 17C: run the current Collection screen through live
  server-backed screenshot QA as a regression baseline before changing the
  shell. Classification: MEDIUM.

## Latest Slice Handoff - Phase 4, Slice 17C

Date: 2026-07-07.

Objective:

- Capture the current Collection menu live as a regression baseline before the
  Character Menu shell/name work.

What was inspected:

- Live `runServer` and `runClientOne` flow.
- Saved `localhost` multiplayer entry as `ElarionAdmin`.
- `/charactermenu` Mounts, Pets, and Titles tabs.
- Server/client logs for Collection/custom-payload failures.
- Mount runtime collection state after QA.

Live screenshots captured:

- `build/ui-qa/slice-17c/11-collection-mounts.png`
- `build/ui-qa/slice-17c/12-collection-mount-active.png`
- `build/ui-qa/slice-17c/13-collection-mount-scroll.png`
- `build/ui-qa/slice-17c/14-collection-pets.png`
- `build/ui-qa/slice-17c/15-collection-titles.png`

Findings:

- Mounts/Pets/Titles tabs render in the current Collection shell.
- Airship live model preview renders in the detail pane.
- Set Active refreshed the server-authored mount row/detail state.
- Hidden mount-list scrolling works.
- Empty Pets state renders.
- Titles tab selection/detail renders.
- No Elarion Collection/custom-payload encode/decode crash occurred during the
  QA flow.
- Matched client errors were unrelated resource-pack model rotation errors for
  `excalibur/carved_pumpkin/*`.
- The current shell remains visually old: it still says `Collection`, sparse
  empty/detail panels need redesign, and the future implementation should use
  the approved Option A Character Menu boards.

Runtime side effect:

- QA changed the test player's active mount to `airship` in
  `dev/run/world/elarion/addon-state/mounts/collection.json`. There was no
  pre-click runtime-state backup, so do not guess a previous value.

Live QA helper note:

- If the dev client starts with a tiny/white framebuffer or blank main menu,
  focus Minecraft and toggle `F11` once before restarting or changing shader
  state. This run showed the issue was not shader-related; Iris state was
  restored after the temporary check.

Exact documentation files changed:

- `CODEX.md`
- `INDEX.md`
- `PLANS.md`
- `PLAN.md`
- `TODO.md`
- `docs/test-commands.md`
- `docs/ai/CURRENT_STATUS.md`
- `docs/reports/CITIZEN_LEDGER_AUDIT.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `docs/systems/GUI.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/ui/revamp-option-a/README.md`

Exact documentation files added:

- `docs/ui/revamp-option-a/ASSET_PLAN.md`

Production code changed:

- None in Slice 17C.

Verification:

- Live screenshots captured.
- Log search found no Elarion custom-payload crash during the Collection flow.
- Remaining server process was stopped after QA.

Deferred:

- Title activation click; the Titles selection/detail path was captured, but
  the live client disconnected before a title activation click.
- Character Menu label and `/charactermenu` alias.
- Profile aggregation/contributor contracts.
- Runtime art asset generation and resource promotion.

Precise recommended next slice:

- Phase 4, Slice 17D: introduce the `Character Menu` user-facing shell label
  and `/charactermenu` alias while preserving Collection internals, packet IDs,
  provider/action contracts, runtime files, and `/charactermenu` compatibility.
  Classification: SMALL.

## Latest Slice Handoff - Phase 4, Slice 17D

Date: 2026-07-07.

Objective:

- Add the `Character Menu` player-facing label and `/charactermenu` alias without
  changing Collection internals.

Implementation:

- `ElarionCollectionService` now sends `Character Menu` as the snapshot title
  and keeps the current truthful subtitle `Mounts, pets, and titles.`
- `ElarionCollectionScreen` uses `Character Menu` for the client screen title.
- `/charactermenu` opens the same server-authoritative Collection snapshot.
- `/charactermenu` remains a compatibility alias.
- Command help lists `/charactermenu` as preferred and `/charactermenu` as the alias.
- The keybind translation now reads `Open Character Menu`.

Exact production/resource files changed:

- `platform/core/src/main/java/panetina/elarion/core/service/ElarionCollectionService.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionCollectionScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/command/PlayerCommandRegistrar.java`
- `platform/core/src/main/resources/assets/elarion_core/lang/en_us.json`

Exact test files changed:

- `platform/core/src/test/java/panetina/elarion/core/service/ElarionCollectionServiceTest.java`

Exact documentation files changed:

- `CODEX.md`
- `PLAN.md`
- `PLANS.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`
- `docs/reports/CITIZEN_LEDGER_AUDIT.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `docs/systems/GUI.md`
- `docs/test-commands.md`
- `docs/ui/revamp-option-a/ASSET_PLAN.md`
- `docs/ui/revamp-option-a/README.md`

Compatibility:

- No Collection Java/API names, packet IDs, packet models, provider/action
  contracts, config paths, runtime files, storage, resource paths, unlock
  state, or addon behavior changed.
- No Profile aggregation, Profile rows, shell redesign, or art-asset promotion
  was added.

Verification:

- Passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionCollectionServiceTest --tests panetina.elarion.core.network.CollectionPayloadTest --tests panetina.elarion.core.client.ElarionCollectionScreenLayoutTest`

Deferred:

- Core profile aggregation/presentation boundary.
- Addon profile contributors.
- Character Menu shell redesign against Option A art.
- Runtime asset generation/promotion.

Precise recommended next slice:

- Phase 4, Slice 17E: define the Core profile aggregation and presentation
  boundary with conservative server-side visibility. Start with Core-only
  records/tests and no addon contributors or raw storage scans. Classification:
  MEDIUM.

## Latest Slice Handoff - Phase 4, Slice 17E

Date: 2026-07-07.

Objective:

- Define the Core Character Menu profile aggregation/presentation boundary
  before rendering a Profile tab or adding addon contributors.

Implementation:

- Added profile model contracts:
  `CitizenProfileRequestContext`, `CitizenProfileSnapshot`,
  `CitizenProfileSection`, `CitizenProfileField`, `CitizenProfileCard`,
  `CitizenProfileContributor`, and `ProfileVisibility`.
- Added `CitizenProfileService`.
- Exposed the service through `ElarionApi.profiles()` and
  `ElarionApi.system().profiles()`.
- Built only Core-owned profile sections: identity, Realm, and active title.
- Added conservative server-side visibility values: `PUBLIC`, `SELF`, and
  `ADMIN`.
- Bounded output to 16 sections, 24 fields per section, and 8 cards per
  section.
- Added explicit contributor registration with duplicate contributor ID
  rejection.
- `snapshot(context)` resolves one target citizen by UUID through
  `CitizenService.find`. It does not enumerate all citizens and does not touch
  addon runtime storage.

Exact production files added:

- `platform/core/src/main/java/panetina/elarion/core/model/profile/ProfileVisibility.java`
- `platform/core/src/main/java/panetina/elarion/core/model/profile/CitizenProfileRequestContext.java`
- `platform/core/src/main/java/panetina/elarion/core/model/profile/CitizenProfileField.java`
- `platform/core/src/main/java/panetina/elarion/core/model/profile/CitizenProfileCard.java`
- `platform/core/src/main/java/panetina/elarion/core/model/profile/CitizenProfileSection.java`
- `platform/core/src/main/java/panetina/elarion/core/model/profile/CitizenProfileSnapshot.java`
- `platform/core/src/main/java/panetina/elarion/core/model/profile/CitizenProfileContributor.java`
- `platform/core/src/main/java/panetina/elarion/core/service/CitizenProfileService.java`

Exact production files changed:

- `platform/core/src/main/java/panetina/elarion/core/api/ElarionApi.java`
- `platform/core/src/main/java/panetina/elarion/core/api/ElarionSystemApi.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`

Exact test files added:

- `platform/core/src/test/java/panetina/elarion/core/service/CitizenProfileServiceTest.java`

Exact documentation files changed:

- `AGENTS.md`
- `CODEX.md`
- `INDEX.md`
- `PLAN.md`
- `TODO.md`
- `docs/addons/core.md`
- `docs/ai/CURRENT_STATUS.md`
- `docs/architecture/DEPENDENCY_GRAPH.md`
- `docs/reports/CITIZEN_LEDGER_AUDIT.md`
- `docs/reports/PROJECT_REVAMP_AUDIT.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `docs/systems/GUI.md`

Compatibility:

- No Collection packet, command, screen behavior, config, runtime file,
  persistence schema, unlock state, or addon state changed.
- No profile network packets, Profile tab UI, addon contributor registration,
  Chronicle/history query, art asset, or raw storage scan was added.

Verification:

- Passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.CitizenProfileServiceTest`
- Passed:
  `.\gradlew.bat :platform:core:test`

Deferred:

- Bounded profile request/snapshot transport.
- Character Menu Profile tab rendering.
- Addon profile contributors.
- Owner-maintained summary APIs for quests, Offerings, NPC reputation, deaths,
  economy, groups, and other addon data.
- Public-history LEDGER integration into profile.

Precise recommended next slice:

- Phase 4, Slice 17F: add bounded profile transport records/codecs and
  round-trip/bounds tests for lazy Character Menu profile requests/snapshots.
  Exclude UI rendering, addon contributors, and raw storage scans.
  Classification: MEDIUM.

## Latest Slice Handoff - Phase 4, Slice 17H

Date: 2026-07-07.

Objective:

- Add a Core-only Profile tab surface to Character Menu using the registered
  profile request/cache path.

Implementation:

- Added fixed Core `Profile` tab metadata to `ElarionCollectionService`.
- Preserved the default `/charactermenu` selection on `Mounts`.
- Updated the Character Menu subtitle to `Profile, mounts, pets, and titles.`
- `ElarionCollectionScreen` now requests a server profile snapshot when the
  Profile tab is selected.
- The screen reads `CitizenProfileClientState` and renders visible Core
  identity, Realm, and active-title sections only.
- Initial Profile rendering used section rows plus a detail panel; live review
  rejected that as too menu-like.
- Corrected Profile rendering uses one composed civic sheet with a
  name/identity strip plus Realm, office/title, and record panels.

Exact production files changed:

- `platform/core/src/main/java/panetina/elarion/core/service/ElarionCollectionService.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionCollectionScreen.java`

Exact test files changed:

- `platform/core/src/test/java/panetina/elarion/core/service/ElarionCollectionServiceTest.java`

Exact documentation files changed:

- `INDEX.md`
- `PLAN.md`
- `TODO.md`
- `docs/addons/core.md`
- `docs/ai/CURRENT_STATUS.md`
- `docs/reports/CITIZEN_LEDGER_AUDIT.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `docs/systems/GUI.md`

Compatibility:

- Profile clients remain read-only; no profile mutation packet exists.
- No addon profile contributor, raw storage scan, completed quest projection,
  Offering score, NPC reputation, lifetime death total, persistence, config,
  command, packet schema, art asset, or broad shell redesign changed.
- Unlockables still open on Mounts by default.

Verification:

- Passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionCollectionServiceTest --tests panetina.elarion.core.client.ElarionCollectionScreenLayoutTest --tests panetina.elarion.core.client.CitizenProfileClientStateTest`

Deferred:

- Live screenshot QA for the corrected Profile sheet. The pre-correction live
  pass proved the profile transport and section switching worked without a
  custom-payload crash, but the corrected-sheet screenshot attempt returned
  stale main-menu pixels despite the process title reporting multiplayer.
- Addon profile contributors.
- Owner-maintained addon summary APIs.
- Broad Option A Character Menu redesign and art-asset promotion.

Precise recommended next slice:

- Phase 4, Slice 17I: re-run live QA `/charactermenu` with the corrected Profile
  sheet selected, capture screenshots, and verify no custom payload crash or
  text overlap. Exclude addon profile contributors and broad redesign unless a
  live defect requires a bounded fix.

## Latest Slice Handoff - Phase 4, Slice 17I

Date: 2026-07-07.

Objective:

- Live-verify the corrected Character Menu Profile sheet through the normal
  multiplayer `/charactermenu` flow.

Inspected:

- The running Fabric development server and client.
- `/charactermenu` default Mounts state and Profile-tab selection.
- Corrected Profile-sheet composition, clipping, spacing, and transport.

Implementation:

- No production or test source changed in this QA-only slice.
- Verified the corrected Profile tab as one composed civic sheet with an
  identity strip and Identity, Realm, Office and title, and Record panels.
- Verified that selecting Profile does not produce a custom-payload crash.

Live evidence:

- `build/ui-qa/slice-17j-profile-sheet/10-ledger-default.png`
- `build/ui-qa/slice-17j-profile-sheet/11-ledger-profile-sheet.png`

Exact documentation files changed:

- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`
- `docs/reports/CITIZEN_LEDGER_AUDIT.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`

Verification:

- Passed live multiplayer `/charactermenu` open and Profile-tab selection.
- No custom-payload disconnect or crash occurred.
- No obvious panel overlap was visible in the corrected sheet; long record
  values remained bounded through ellipsis.
- The full `:platform:core:test` suite had already passed after the corrected
  sheet implementation; no source changed during this QA-only slice.

Deferred:

- Addon profile contributors and owner-maintained summary APIs.
- Broad Character Menu shell and art redesign against the approved Option A
  reference.
- Runtime promotion of approved Character Menu art assets.

Precise recommended next slice:

- Phase 4, Slice 17J: perform a bounded Character Menu shell/art pass against
  the approved Option A reference while preserving the verified Profile
  transport and single-sheet composition. Exclude addon profile contributors
  and domain-storage queries. Classification: MEDIUM.

## Latest Slice Handoff - Phase 4, Slice 17J

Date: 2026-07-07.

Objective:

- Replace the plain Character Menu Profile sheet with a rich portrait-led
  dossier aligned with the approved Option A reference.

Production changes:

- `CitizenProfileService` now contributes cheap Core-owned citizenship,
  civic-standing, granted-ability count, and unlocked-title count fields.
- `ElarionCollectionScreen` now uses a wider bounded shell, the live player
  head, an identity/Realm/title banner, semantic panel accents/icons, real Core
  collection facts, and explicit future addon summary slots.
- Future slots use stable source/field ids documented in `docs/systems/GUI.md`.
- No addon storage scan, fake value, persistence change, new mutation packet,
  or addon contributor was added.

Exact source files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionCollectionScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/service/CitizenProfileService.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionCollectionScreenLayoutTest.java`
- `platform/core/src/test/java/panetina/elarion/core/service/CitizenProfileServiceTest.java`

Verification:

- Passed full `:platform:core:test`.
- Passed live multiplayer `/charactermenu` Profile selection without a custom-payload
  crash or visible overlap at the tested GUI scale.
- Final evidence:
  `build/ui-qa/slice-17j-profile-redesign/18-profile-final.png`.

Deferred:

- Owner-maintained bounded addon summaries and contributor registration.
- Mounts, Titles, and Pets visual migration.
- Generated art promotion beyond the current texture-based civic treatment.

Precise recommended next slice:

- Phase 4, Slice 17K: migrate existing unlockable tabs into the approved
  Character Menu visual system without changing provider ownership, action
  semantics, or persistence. Classification: MEDIUM.

## Latest Slice Handoff - Phase 4, Slice 17K

Date: 2026-07-07.

Objective:

- Bring Mounts, Pets, and Titles into the approved Character Menu visual system
  while preserving all Collection behavior.

Production changes:

- `ElarionCollectionScreen` now renders semantic icon tabs, unlocked/total
  summaries, compact 44-pixel state rows, a large showcase panel, designed
  empty states, and bounded record/action areas.
- The list still shows six rows and retains hidden wheel/keyboard scrolling.
- Preview providers and server-authored actions are unchanged.
- Complete 16x16 textures are now matrix-scaled, fixing repeated/tiled icons in
  tabs, rows, empty states, profile headers, and fallback previews.

Exact source files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionCollectionScreen.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionCollectionScreenLayoutTest.java`

Verification:

- Passed full `:platform:core:test`.
- Live evidence:
  `build/ui-qa/slice-17k-unlockables/05-mounts-final.png`,
  `06-pets-final.png`, `07-titles-final.png`, and
  `13-title-action-clean-runtime.png`.
- Clean-runtime title action and snapshot refresh passed; Monarch was restored
  afterward at `14-title-restored.png`.

Deferred:

- Custom generated Character Menu runtime art and atlas contract.
- Rewards/Achievements and other future Collection providers.
- Owner-specific bounded profile contributors.

Precise recommended next slice:

- Select one bounded Option A screen family for the next project-wide UI
  migration. Character Creation plus Realm Assignment is the strongest next
  dependency because it is the required first-join flow; keep it separate from
  gameplay/domain changes. Classification: MEDIUM.

## Latest Slice Handoff - Phase 4, Slice 17L

Date: 2026-07-07.

Objective:

- Make Character Menu title/mount presentation richer without changing
  Collection authority: configurable title colors, provider-owned rank badges,
  active office/advancement profile facts, and documented future profile-link
  and reward-unlock hooks.

Production changes:

- `TitleDefinition` now carries `colorArgb`; `titles.yml` supports optional
  `color: "#RRGGBB"` per title.
- `CoreConfigParser`, `CoreConfigValidator`, and `CoreConfigDescriptors` parse,
  validate, and expose title colors.
- `CoreConfigManager` migrates missing built-in title colors for known Core
  title IDs without overwriting custom colors.
- `IdentityService` uses configured title color for title text rendered under
  player names.
- `ElarionCollectionEntry` and `CollectionOpenPayload` now include optional
  `accentColor`, `rankLabel`, and `rankColor`; outbound packet projection
  bounds rank labels and preserves alpha-normalized colors.
- `ElarionCollectionScreen` renders provider rank badges, uses accent/rank
  colors for selected/detail frames, and renders unlocked title previews as
  title + username + live player model.
- `CoreTitleCollectionProvider` supplies title accent/rank metadata. Built-in
  ranks are Sovereign, Heir, Council, Synod, Officer, Trusted, Common, Rare,
  and Legendary.
- `ElarionMountType` supplies Common/Uncommon/Legendary Collection rank
  metadata. Airship, Hot Air Balloon, and Ghast are Common Realm baseline
  mounts; Bee, Chinese Dragon, and Wyvern are Uncommon; Sci-Fi Bike is
  Legendary for the future full-advancement route.
- `ProgressionService.recordAdvancement` now synchronizes the exact vanilla
  completed-advancement count instead of incrementing a potentially drifting
  counter; `CoreProgressionProfileContributor` exposes that count.
- `GovernmentProfileContributor` projects active office display names into the
  Citizen Profile so the Ledger can show an office role instead of generic
  Citizen when available.

Exact source files changed in this slice:

- `platform/core/src/main/java/panetina/elarion/core/model/TitleDefinition.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ElarionCollectionEntry.java`
- `platform/core/src/main/java/panetina/elarion/core/network/CollectionOpenPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionCollectionScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/config/CoreConfigDefaultFiles.java`
- `platform/core/src/main/java/panetina/elarion/core/config/CoreConfigManager.java`
- `platform/core/src/main/java/panetina/elarion/core/config/CoreConfigParser.java`
- `platform/core/src/main/java/panetina/elarion/core/config/CoreConfigValidator.java`
- `platform/core/src/main/java/panetina/elarion/core/config/CoreConfigDescriptors.java`
- `platform/core/src/main/java/panetina/elarion/core/service/CoreTitleCollectionProvider.java`
- `platform/core/src/main/java/panetina/elarion/core/service/IdentityService.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ProgressionService.java`
- `platform/core/src/main/java/panetina/elarion/core/service/PlayerStatsService.java`
- `platform/core/src/main/java/panetina/elarion/core/model/PlayerStats.java`
- `platform/core/src/main/java/panetina/elarion/core/service/CoreProgressionProfileContributor.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/service/GovernmentProfileContributor.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/ElarionGovernmentAddon.java`
- `addons/mounts/src/main/java/panetina/elarion/addons/mounts/entity/ElarionMountType.java`
- `addons/mounts/src/main/java/panetina/elarion/addons/mounts/ElarionMountsAddon.java`

Exact test files changed in this slice:

- `platform/core/src/test/java/panetina/elarion/core/network/CollectionPayloadTest.java`
- `platform/core/src/test/java/panetina/elarion/core/config/CoreConfigManagerTest.java`
- `platform/core/src/test/java/panetina/elarion/core/config/ElarionConfigRegistryTest.java`
- `addons/mounts/src/test/java/panetina/elarion/addons/mounts/entity/ElarionMountTypeTest.java`
- `addons/government/src/test/java/panetina/elarion/addons/government/service/GovernmentProfileContributorTest.java`

Documentation changed:

- `docs/config.md`
- `docs/systems/GUI.md`
- `docs/addons/core.md`
- `docs/addons/government.md`
- `docs/addons/mounts.md`
- `TODO.md`
- `PLAN.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :platform:core:test`.
- Passed `.\gradlew.bat :addons:mounts:test`.
- Passed focused `CollectionPayloadTest`, `CoreConfigManagerTest`,
  `ElarionConfigRegistryTest`, and `ElarionMountTypeTest`.
- `.\gradlew.bat :addons:government:test --tests panetina.elarion.addons.government.service.GovernmentProfileContributorTest`
  was blocked by existing `groups` and `offerings` compile failures pulled in
  through Government dependencies. Do not treat that as a Government profile
  assertion failure without first fixing or isolating those dependency source
  sets.

Deferred:

- Live screenshot QA of the rank badges and title colors.
- Player-name double-click profile links in History, Chronicle, Government,
  notifications, and future menus. This needs a server-authored clickable name
  span carrying UUID/citizen id, not client-side text parsing.
- Generic mount/pet reward hooks. Mounts needs an owner API/action for
  unlocking specific mounts from rewards/progression/NPC flows; Pets should do
  the same when implemented.
- Custom Character Menu runtime art batch and atlas promotion.

Precise recommended next slice:

- Either run live `/charactermenu` screenshot QA for title colors/rank badges, or
  move to the next approved UI family: Character Creation plus balanced Realm
  Assignment. Keep reward hooks, player-link profile routing, and art-bank
  generation separate unless explicitly selected. Classification: MEDIUM.

## Latest Slice Handoff - Phase 4, Slice 17M

Date: 2026-07-07.

Objective:

- Live-QA Character Menu rank/title/profile presentation, then centralize rank
  colors project-wide and tighten Profile advancement display semantics.

Production changes:

- Added Core `ElarionCollectionRank` as the shared semantic rank palette for
  Collection/Profile-style rewards and unlockables.
- Updated `CoreTitleCollectionProvider` to use `ElarionCollectionRank` for
  built-in title rank labels/colors instead of local color constants.
- Updated `ElarionMountType` to use `ElarionCollectionRank` for Common,
  Uncommon, and Legendary badge colors.
- Updated `ProgressionService.synchronizeAdvancementCount` to count only
  completed advancements with visible display metadata, not hidden/internal
  advancement records.
- Updated `ElarionCollectionScreen` Profile header so active title and civic
  role do not print the same word twice when both resolve to the same office
  title.

Exact source files changed in this slice:

- `platform/core/src/main/java/panetina/elarion/core/model/ElarionCollectionRank.java`
- `platform/core/src/main/java/panetina/elarion/core/service/CoreTitleCollectionProvider.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ProgressionService.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionCollectionScreen.java`
- `addons/mounts/src/main/java/panetina/elarion/addons/mounts/entity/ElarionMountType.java`

Exact test files changed in this slice:

- `platform/core/src/test/java/panetina/elarion/core/model/ElarionCollectionRankTest.java`
- `addons/mounts/src/test/java/panetina/elarion/addons/mounts/entity/ElarionMountTypeTest.java`

Documentation changed:

- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`
- `docs/systems/GUI.md`

Live QA:

- Captured pre-patch evidence under
  `build/ui-qa/slice-17m-ledger-rank-qa/`.
- Useful screenshots:
  `08-ledger-open.png`, `09-ledger-profile.png`, and
  `10-ledger-titles.png`.
- Confirmed the Ledger opened from `/charactermenu` against localhost with no custom
  payload crash; Mounts, Profile, and Titles rendered with no visible overlap
  at the tested GUI scale.
- Final post-patch screenshot was blocked by Minecraft client `Invalid Session`
  during reconnect. The QA server/client Java processes were stopped; only
  Gradle daemons remained.

Verification:

- Passed
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.model.ElarionCollectionRankTest --tests panetina.elarion.core.network.CollectionPayloadTest`.
- Passed
  `.\gradlew.bat :addons:mounts:test --tests panetina.elarion.addons.mounts.entity.ElarionMountTypeTest`.
- Passed `.\gradlew.bat :platform:core:test :addons:mounts:test`.

Deferred:

- Rerun final post-patch live Ledger screenshot QA after a clean client session.
- Character Creation plus balanced Realm Assignment UI migration.
- Art-bank generation and runtime asset promotion.
- Player-name double-click profile links.
- Generic mount/pet reward hooks.

Precise recommended next slice:

- Phase 4 next UI family: Character Creation plus balanced Realm Assignment,
  preserving server authority and existing onboarding behavior. Classification:
  MEDIUM.

## Latest Slice Handoff - Phase 4, Slice 17N

Date: 2026-07-07.

Objective:

- Migrate Character Creation and balanced Realm Assignment to the approved
  Option A civic UI language without changing onboarding authority, payloads,
  persistence, or Realm assignment logic.

Production changes:

- `CharacterCreationScreen` now uses a wider civic onboarding sheet with:
  header ornaments, step strip, identity panel, name field, identity preview,
  biography panel, biography readiness row, and bottom action band.
- The Character Creation bottom action button is right-aligned and the adjacent
  explanatory text is ellipsized to the available width, preventing footer text
  overlap at smaller GUI/window scales. Its bounds are vertically centered in
  the footer band.
- `CharacterRealmAssignmentScreen` now uses a wider civic placement sheet with:
  assigned Realm summary, up to three vertical Realm rows, assigned-row
  highlight, population metadata, disabled/future-transfer messaging for other
  rows, and bottom confirmation band.
- The Realm Assignment confirmation button is right-aligned and both footer
  text lines are ellipsized to the available width, matching the fixed-panel
  layout behavior used by the Character Creation footer. Its bounds are also
  vertically centered in the larger two-line footer band.
- `ElarionCivicUi.compactActionButton` applies a one-pixel optical correction to
  civic button labels. Draw bounds and hit bounds remain unchanged.
- The client still sends only `CharacterCreationSubmitPayload` for character
  creation and closes the Realm placement panel on confirmation. Server
  validation, lifecycle state, cooldown behavior, and Realm assignment remain
  unchanged.

Exact source files changed in this slice:

- `platform/core/src/main/java/panetina/elarion/core/client/CharacterCreationScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/CharacterRealmAssignmentScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionCivicUi.java`

Exact test files changed in this slice:

- `platform/core/src/test/java/panetina/elarion/core/client/CharacterOnboardingScreenLayoutTest.java`

Documentation changed:

- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`
- `docs/systems/Characters.md`
- `docs/systems/UI_JOURNAL.md`

Verification:

- Passed
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.CharacterOnboardingScreenLayoutTest --tests panetina.elarion.core.network.CharacterCreationPayloadTest`.
- Passed `.\gradlew.bat :platform:core:test`.
- Live screenshot QA passed on 2026-07-07:
  - Joined the saved localhost server as `ElarionAdmin` and used the supported
    `/e realm remove ElarionAdmin` plus `/e test character reset ElarionAdmin`
    commands to enter the mandatory onboarding flow.
  - Captured empty and filled Character Creation, Realm Assignment, and
    post-confirm gameplay under
    `build/ui-qa/slice-17n-onboarding-live-qa-5-footer-centered/`.
  - Verified both action-button bounds have balanced top/bottom footer spacing,
    their labels are optically centered, and `Confirm Placement` closes through
    the matching hitbox.
  - `minecraft-qa.ps1` uses client-area coordinates; full-window captures include
    the Windows title bar and therefore show visual y-coordinates about 30 px
    lower than helper click coordinates.
  - Stopped all QA server/client processes and restored the pre-QA `characters`
    and `citizens` state. State hashes match the backup and no QA process remains.

Deferred:

- Rich Realm metadata in onboarding rows. Current payload only provides Realm
  id, display name, population, and assigned flag, so the UI intentionally does
  not invent leader, Shrine, government, color, or availability facts.
- Generated runtime art assets for onboarding.

Precise recommended next slice:

- Phase 4 next UI family: layout-preserving Shrine reskin. Keep Offering
  behavior, progress calculations, reward delivery, contribution authority, and
  Shrine information architecture unchanged. Classification: MEDIUM.

## Latest Slice Handoff - Phase 4, Slice 17O

Date: 2026-07-07.

Objective:

- Apply the approved Option A civic presentation to the existing Shrine board
  without changing Offering ownership, contribution authority, packets,
  persistence, reward delivery, progress math, or information architecture.

Production changes:

- `ShrineOfFoundationScreen` now uses the Core civic attached shell, ornamental
  header, bordered progress band, semantic selected tabs, compact rows, framed
  summary/reward surfaces, civic numeric modal/buttons, and footer action.
- The fixed screen title is `SHRINE OF FOUNDATION`; the server-authored active
  project title remains visible in the left summary, preserving display-name
  overrides used by Quest outcomes.
- Requirement icons use 16px item sizing. Reward slots remain bounded, show all
  available rows that fit, and retain native item/enchantment tooltips.
- Existing list virtualization, tab/list positions, mouse/keyboard controls,
  numeric input, and `ShrineContributionSubmitPayload` request shape are
  unchanged.

Exact source files changed:

- `addons/offerings/src/main/java/panetina/elarion/addons/offerings/client/ShrineOfFoundationScreen.java`

Exact test files added:

- `addons/offerings/src/test/java/panetina/elarion/addons/offerings/client/ShrineOfFoundationScreenLayoutTest.java`

Documentation changed:

- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`
- `docs/addons/offerings.md`
- `docs/systems/CommunityContribution.md`
- `docs/systems/UI_JOURNAL.md`
- `wiki/admin/offerings.md`

Verification:

- Passed focused layout, `ShrineUiOpenPayload`, and
  `ShrineContributionSubmitPayload` tests.
- Passed `.\gradlew.bat :addons:offerings:test`.
- Live QA used the linked Realm Shrine at `elarion:realm_world_1` and captured
  evidence under `build/ui-qa/slice-17o-shrine-civic-reskin/`.
- Verified completed and incomplete projects, selected Contribute/History tabs,
  six configured reward slots, enchanted reward tooltip, empty History, and the
  numeric contribution modal.
- Backed up Offering runtime state before `/e test shrine reset realm1`, restored
  all Offering files afterward with zero hash differences, and stopped every QA
  server/client process.

Unresolved risk:

- The existing completed-instance projection can show `Complete` while current
  requirement counters are zero after a prior reset generation. This predates
  the visual migration and remains an explicit TODO for service/projection
  investigation; the client did not invent or mask state.

Precise recommended next slice:

- Phase 4: simple quest NPC dialogue Option A migration. Preserve NPC-owned
  dialogue graphs, server-side conditions/actions, relationship state, payloads,
  and existing interaction flow. Classification: MEDIUM.

## Latest Slice Handoff - Phase 4, Slice 17P

Date: 2026-07-07.

Objective:

- Replace placeholder/repeated icons across the already revamped UI surfaces
  with the newly promoted runtime art library, while preserving behavior and
  skipping live screenshot QA at the user's request.

Production changes:

- Added Core `ElarionUiIcons`, a semantic runtime catalog for curated PNGs under
  `assets/elarion_core/textures/gui/library/`.
- Government UI icon/glyph resolution now checks the shared catalog before
  falling back to Government-local swatches/assets.
- Notifications now use semantic category/action/reward icons while preserving
  the existing read/unread rail assets and native item-stack rendering for
  explicit item rewards.
- Character Menu/profile, Character Creation, Realm Assignment, Shrine,
  Portal Confirmation, and Portal Status HUD now prefer catalog icons for
  tabs, rows, headers, actions, summaries, route slots, and empty/default
  states.
- Free/neutral Portal confirmation prompts no longer show the payment/item
  slot. Ticket/fee prompts keep the slot and render ticket, currency, or gate
  art.

Exact source files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionUiIcons.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionNotificationHud.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionCollectionScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/CharacterCreationScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/CharacterRealmAssignmentScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/service/CoreTitleCollectionProvider.java`
- `platform/core/src/main/java/panetina/elarion/core/service/DeferredRewardGrantService.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/client/GovernmentUiIcons.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/client/GovernmentUiGlyphs.java`
- `addons/offerings/src/main/java/panetina/elarion/addons/offerings/client/ShrineOfFoundationScreen.java`
- `addons/portals/src/main/java/panetina/elarion/addons/portals/client/PortalConfirmationScreen.java`
- `addons/portals/src/main/java/panetina/elarion/addons/portals/client/PortalStatusHud.java`

Exact test files changed:

- `addons/government/src/test/java/panetina/elarion/addons/government/client/GovernmentUiIconAssetTest.java`
- `addons/government/src/test/java/panetina/elarion/addons/government/service/GovernmentProfileContributorTest.java`

Documentation changed:

- `PLAN.md`
- `TODO.md`
- `INDEX.md`
- `docs/ai/CURRENT_STATUS.md`
- `docs/systems/GUI.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/ui/revamp-option-a/ASSET_PLAN.md`

Verification:

- Verified every `ElarionUiIcons` mapped PNG path exists.
- Passed `.\gradlew.bat :platform:core:compileJava`.
- Passed `.\gradlew.bat :platform:core:test`.
- Passed `.\gradlew.bat :addons:government:test`.
- Passed `.\gradlew.bat :addons:offerings:test`.
- Passed `.\gradlew.bat :addons:portals:test`.
- No live screenshot QA was run, by user request.

Resolved later:

- The free/no-fee Portal prompt text heuristic recorded here was replaced in
  Phase 4 Slice 30 by server-authored `PortalTravelPromptPayload.costKind`.
- Manual visual inspection is still needed for icon fit/readability in
  Government, Notification drawer, Character Menu, onboarding, Shrine, free
  Portal prompts, ticket/fee Portal prompts, and Nether/End route HUD slots.

Precise recommended next slice:

- After manual inspection of this art pass, continue Phase 4 with the simple
  quest NPC dialogue Option A migration. Preserve NPC-owned dialogue graphs,
  server-side conditions/actions, relationship state, payloads, and interaction
  flow. Classification: MEDIUM.

## Latest Slice Handoff - Phase 4, Slice 17Q

Date: 2026-07-08.

Objective:

- Fix the reported visual alignment/icon issues in the already revamped UI
  surfaces and correct the new-player Character Creation -> Realm Placement
  flow so teleporting happens only after placement confirmation.

Production changes:

- Added `ElarionCivicUi.centeredTextY` and moved shared Core/Government button
  labels to metric-based vertical centering.
- Centered Shrine reward slots within their bounded reward body.
- Retuned `ElarionUiIcons` so generic profile, identity, people, civic, quest,
  world, Nether, End, project, and Pets icons use non-portrait curated library
  art.
- Made Civic Forum and Seat of Rule header titles larger and lower.
- Renamed Character Creation's footer action to `Continue`.
- Sent Realm assignment before closing Character Creation after accepted
  submission.
- Added `CharacterRealmAssignmentConfirmPayload`; Core now teleports to the
  assigned Realm only after the Realm Placement screen confirms.
- Changed Character Menu default selection to Profile for C, `/charactermenu`, and
  `/charactermenu`.
- Adjusted Sci-Fi Bike mount preview calibration while keeping preview offsets
  bounded by tests.

Exact source files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionCivicUi.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionUiRenderer.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionUiIcons.java`
- `platform/core/src/main/java/panetina/elarion/core/client/CharacterCreationScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/CharacterRealmAssignmentScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/network/CharacterRealmAssignmentConfirmPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/service/CharacterLifecycleService.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionCollectionService.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/client/GovernmentScreenChrome.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/client/GovernmentUiGlyphs.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/client/CivicForumScreen.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/client/seat/SeatOfRuleScreen.java`
- `addons/offerings/src/main/java/panetina/elarion/addons/offerings/client/ShrineOfFoundationScreen.java`
- `addons/mounts/src/main/java/panetina/elarion/addons/mounts/client/ElarionMountCollectionPreviewRenderer.java`

Exact test files changed:

- `platform/core/src/test/java/panetina/elarion/core/network/CharacterCreationPayloadTest.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionCollectionServiceTest.java`
- `addons/government/src/test/java/panetina/elarion/addons/government/client/GovernmentUiIconAssetTest.java`
- `addons/mounts/src/test/java/panetina/elarion/addons/mounts/client/ElarionMountCollectionPreviewRendererTest.java`

Documentation changed:

- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`
- `docs/systems/Characters.md`
- `docs/systems/GUI.md`
- `docs/systems/Networking.md`
- `docs/systems/UI_JOURNAL.md`

Verification:

- Rebuilt Economy once with `.\gradlew.bat :addons:economy:jar` after a
  parallel Gradle attempt hit a corrupt generated dev jar.
- Passed `.\gradlew.bat :platform:core:compileJava :platform:core:test --tests panetina.elarion.core.network.CharacterCreationPayloadTest --tests panetina.elarion.core.service.ElarionCollectionServiceTest`.
- Passed `.\gradlew.bat :addons:government:test --tests panetina.elarion.addons.government.client.GovernmentUiIconAssetTest`.
- Passed `.\gradlew.bat :addons:mounts:test --tests panetina.elarion.addons.mounts.client.ElarionMountCollectionPreviewRendererTest`.
- Passed `.\gradlew.bat :addons:offerings:compileJava`.

Unresolved risks:

- No live screenshot QA was run by user request. Manual visual verification is
  tracked in `TODO.md`.
- Sci-Fi Bike preview calibration is bounded by tests but still needs the
  user's manual visual check because the exact model framing depends on live
  renderer output.

Precise recommended next slice:

- After manual inspection, continue Phase 4 with the simple quest NPC dialogue
  Option A migration. Preserve NPC-owned dialogue graphs, server-side
  conditions/actions, relationship state, payloads, and interaction flow.
  Classification: MEDIUM.

## 2026-07-08 Follow-Up: Shrine Rewards, Civic Baseline, Quest/World Icons

Objective:

- Fix the user-reported Shrine reward row clipping, remaining high compact
  button/title text, stretched-looking Government crest, and old Quest/World
  notification rail icons.

Inspected:

- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionCivicUi.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionUiIcons.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionNotificationHud.java`
- `addons/offerings/src/main/java/panetina/elarion/addons/offerings/client/ShrineOfFoundationScreen.java`
- `addons/offerings/src/test/java/panetina/elarion/addons/offerings/client/ShrineOfFoundationScreenLayoutTest.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/client/CivicForumScreen.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/client/seat/SeatOfRuleScreen.java`
- `addons/government/src/test/java/panetina/elarion/addons/government/client/GovernmentUiIconAssetTest.java`

Decisions:

- Use the shared civic baseline helper for compact control text and remove
  local Shrine upward tab offsets.
- Make Shrine summary reward layout reserve panel height from reward count with
  a three-column cap before drawing.
- Use shared semantic icons for Quest/World notification rail glyphs and draw a
  small unread marker overlay for those semantic icons.
- Keep Mail and Realm rail read/unread texture assets unchanged.
- Draw Government header crest art at 32 px square instead of 36 px and use
  shield art for the shared Civic crest.

Exact source files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionCivicUi.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionUiIcons.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionNotificationHud.java`
- `addons/offerings/src/main/java/panetina/elarion/addons/offerings/client/ShrineOfFoundationScreen.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/client/CivicForumScreen.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/client/seat/SeatOfRuleScreen.java`

Exact test files changed:

- `addons/offerings/src/test/java/panetina/elarion/addons/offerings/client/ShrineOfFoundationScreenLayoutTest.java`
- `addons/government/src/test/java/panetina/elarion/addons/government/client/GovernmentUiIconAssetTest.java`

Docs changed:

- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`
- `docs/systems/GUI.md`
- `docs/systems/UI_JOURNAL.md`

Verification:

- Passed `.\gradlew.bat :platform:core:jar`.
- Passed `.\gradlew.bat :addons:government:test --tests panetina.elarion.addons.government.client.GovernmentUiIconAssetTest`.
- Passed `.\gradlew.bat :addons:offerings:test --tests panetina.elarion.addons.offerings.client.ShrineOfFoundationScreenLayoutTest`.
- Initial parallel Gradle attempt failed from a generated Core dev jar race
  (`zip END header not found`); rerunning sequentially passed.

Unresolved risks:

- No screenshot QA was run by user request. Manual check should verify the
  second Shrine reward row, button baselines, title baselines, Government
  header crest proportions, and Quest/World rail icons.

Precise recommended next slice:

- After manual visual approval, continue Phase 4 with simple quest NPC dialogue
  Option A migration. Preserve NPC-owned graphs, server-side option
  evaluation, relationship state, packets, and existing interaction flow.
  Classification: MEDIUM.

## Latest Slice Handoff - Phase 4, Slices 17T And 18

Objective:

- Close the Citizen title/keybind follow-up and migrate simple quest NPC
  dialogue to the approved compact Option A presentation.

Decisions and changes:

- Citizen is explicitly white-gray `#C9C9C9`; the old shipped gold migrates
  without replacing custom colors.
- Explicit title colors win. Missing colors use known rank-family colors,
  Legendary for unknown globally unique titles, and simple white otherwise.
- Core unbinds vanilla Save Hotbar Activator only while it still owns default
  `C`, then persists the option change.
- `NpcDialogueScreen` now renders a larger title, real NPC portrait, one active
  conversation body, at most three visible choices, virtual overflow scrolling,
  a header close control, and the existing bounded metadata/card strip.
- Player/NPC typing, sounds, numeric prompt, saved list state, payloads, and
  server-side option validation were preserved.
- Semantic option icons and a dedicated quest strip were not invented because
  the current server payload does not describe them.

Exact implementation files changed in the NPC slice:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcDialogueScreen.java`
- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/client/NpcDialogueScreenLayoutTest.java`

Documentation updated:

- `PLAN.md`
- `TODO.md`
- `docs/config.md`
- `docs/addons/core.md`
- `docs/addons/npcs.md`
- `docs/systems/GUI.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed focused Core title/config/descriptor/identity tests.
- Passed `:addons:npcs:compileJava`.
- Passed focused NPC layout, conversation-controller, and virtual-list tests.
- No live screenshot QA was run by request.

Unresolved risks:

- The new NPC geometry still needs eventual live QA at multiple GUI/font
  scales.
- Banker and trader screens need explicit server-authored presentation metadata
  before they diverge from ordinary dialogue; do not infer role from names or
  localized button text.

Precise recommended next slice:

- Audit and define the smallest explicit NPC presentation-kind contract needed
  for a distinct compact banker UI while preserving NPC graph/session ownership
  and Economy authority. Classification: SMALL audit.

## 2026-07-08 Follow-Up: Ledger Keybind, Hidden Aliases, Player Head Preview, Title Color

Objective:

- Replace the Character Creation preview icon with the player's head, make the
  default `C` key reliably open the Character Menu, keep manual `/charactermenu` and
  `/charactermenu` usable without server command recommendations, and fix active
  title color above player heads.

Inspected:

- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreClient.java`
- `platform/core/src/main/java/panetina/elarion/core/client/CharacterCreationScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/command/PlayerCommandRegistrar.java`
- `platform/core/src/main/java/panetina/elarion/core/network/IdentitySyncPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ClientIdentity.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ClientIdentityCache.java`
- `platform/core/src/main/java/panetina/elarion/core/service/IdentitySyncService.java`
- `addons/titles/src/main/java/panetina/elarion/addons/titles/mixin/PlayerEntityRendererMixin.java`

Decisions:

- Keep Ledger opening server-authoritative through the existing
  `CollectionOpenRequestPayload`.
- Hide `/charactermenu` from slash suggestions by removing server
  command-tree registration and handling those exact root commands with
  `ClientSendMessageEvents.ALLOW_COMMAND`.
- Preserve the default keybind as `C`; the keybind and hidden command aliases
  share the same client helper.
- Send active-title ARGB color in `IdentitySyncPayload` so the Titles renderer
  uses configured Core title colors instead of local hard-coding.

Exact source files changed:

- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreClient.java`
- `platform/core/src/main/java/panetina/elarion/core/client/CharacterCreationScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/command/PlayerCommandRegistrar.java`
- `platform/core/src/main/java/panetina/elarion/core/network/IdentitySyncPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ClientIdentity.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ClientIdentityCache.java`
- `platform/core/src/main/java/panetina/elarion/core/service/IdentitySyncService.java`

Exact test files changed:

- `platform/core/src/test/java/panetina/elarion/core/network/IdentitySyncPayloadTest.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ClientIdentityTest.java`

Docs changed:

- `CODEX.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`
- `docs/addons/titles.md`
- `docs/commands.md`
- `docs/test-commands.md`
- `docs/systems/GUI.md`
- `docs/systems/Networking.md`
- `wiki/admin/commands.md`

Verification:

- Passed `.\gradlew.bat :platform:core:compileJava :platform:core:test --tests panetina.elarion.core.network.IdentitySyncPayloadTest --tests panetina.elarion.core.client.ClientIdentityTest --tests panetina.elarion.core.service.ElarionCollectionServiceTest`.
- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ClientIdentityTest` after adding direct configured-title-color coverage.
- Passed `.\gradlew.bat :addons:titles:compileJava`.

Unresolved risks:

- No live screenshot QA was run. Manual check should verify the player-head
  preview, new-player `C` key open, hidden slash recommendations, manual hidden
  aliases, and Monarch/title color above the player.

Precise recommended next slice:

- The simple quest NPC dialogue Option A migration is now complete. Audit and
  define the smallest explicit NPC presentation-kind contract needed for a
  distinct compact banker UI while preserving NPC graph/session ownership and
  Economy authority. Classification: SMALL audit.

Startup/login crash follow-up, 2026-07-08:

- The initial C-key conflict fix accessed `MinecraftClient.options` from the
  Fabric client entrypoint, where options are still null. The one-time unbind
  now runs on the first client tick after options initialization.
- Focused Core compilation/tests passed and `:dev:runClientOne` passed the old
  initialization failure point.
- A subsequent `Invalid player data` disconnect was a separate Loom dev-artifact
  race: the already-running server held the Core dev JAR while the client build
  replaced it, leading to `ZipFile invalid LOC header` when `PlayerIdentity`
  was lazily loaded.
- Stopping the stale server, completing the build, starting `:dev:runServer`,
  and then starting `:dev:runClientOne` resolved the issue. Server logs confirm
  `ElarionAdmin` logged in successfully at 09:47:18.
- Future live QA must build first and must not rebuild shared dev jars while a
  server process is running.

## Latest Slice Handoff - Phase 4, Slice 19

Slice name:

- Conversation-first NPC services and dedicated bank presentation.

Objective:

- Keep all NPCs on the normal conversation screen first, then route banker
  service actions through a server-authored `Open Bank` option into a separate
  compact bank screen.

What was inspected:

- NPC dialogue models, config loader/defaults/validator/descriptors, NPC
  interaction service, NPC client screen dispatch, dialogue/bank UI rendering,
  Economy NPC action ownership, NPC/network/config docs, and the approved
  compact trade reference image.

Decisions made:

- NPCs own dialogue/session/presentation; Economy owns wallet, bank balance,
  deposits, withdrawals, and transaction validation.
- Clients use `presentationKind` and `presentationRole` metadata, not labels,
  names, or localized button text.
- Legacy root-level banker deposit/withdraw options are migrated in memory into
  a `bank_service` node so existing admin files continue to work.
- Aggregate NPC/Realm reputation belongs in Character Menu only. NPC screens
  may show only the relationship with the currently interacted NPC once
  NPC-owned authoritative relationship data exists.
- `trade` is reserved by the model and rejected by validation until a dedicated
  trade screen contract exists.

Exact source files changed:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/model/NpcPresentationKind.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/model/DialogueNode.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/model/DialogueOption.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/model/DialogueView.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfigLoader.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfigDefaults.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfigValidator.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfigDescriptors.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/network/NpcDialogueOpenPayload.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/network/NpcDialogueOptionPayload.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/service/NpcInteractionService.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/ElarionNpcsClient.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcDialogueScreen.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcBankScreen.java`

Exact tests changed:

- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/config/NpcBankPresentationMigrationTest.java`
- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/config/NpcConfigDescriptorsTest.java`
- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/network/NpcDialogueOptionPayloadTest.java`

Docs changed:

- `docs/addons/npcs.md`
- `docs/addons/economy.md`
- `docs/config.md`
- `docs/systems/NPCs.md`
- `docs/systems/Networking.md`
- `docs/systems/GUI.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `docs/ui/revamp-option-a/README.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Tests run:

- `.\gradlew.bat :addons:npcs:test :addons:economy:test`

Live screenshot QA:

- Passed under `build/ui-qa/slice-19-npc-bank/`.
- `12-after-right-click.png`: banker opens through normal NPC conversation
  with `Open Bank`.
- `13-bank-screen.png`: dedicated bank presentation renders with
  Deposit/Withdraw controls and balance badge.
- `14-back-to-conversation.png`: Back to Conversation returns to the normal
  NPC screen.

Unresolved risks:

- Dedicated trade presentation is not implemented.
- NPC-owned per-NPC relationship storage does not exist yet.
- Character Menu aggregate NPC/Realm reputation needs bounded owner-maintained
  summary APIs before exposure.

Precise recommended next slice:

- Implement the dedicated trade presentation contract against
  `docs/ui/revamp-option-a/09-npc-trade-screen-v2.png`, starting with
  model/payload/config validation and a non-mutating screen shell.

## Latest Slice Handoff - Phase 4, Slice 20

Slice name:

- Dedicated NPC trade presentation shell.

Objective:

- Add the approved compact Buy/Sell trade presentation as a dedicated,
  non-mutating NPC service screen without enabling stock, price, inventory, or
  buy/sell mutations.

What was inspected:

- `NpcBankScreen`, `ElarionNpcsClient`, NPC payload shape, NPC config
  validator/descriptors, validator tests, descriptor tests, and the approved
  `docs/ui/revamp-option-a/09-npc-trade-screen-v2.png` reference.

Decisions made:

- `presentation: trade` is now valid and routes to `NpcTradeScreen`.
- Trade nodes are UI/navigation only for now. Options inside trade nodes cannot
  define prompts or executable actions until a trade owner contract exists.
- The trade shell uses server-authored `presentationKind` and
  `presentationRole` metadata; it does not infer behavior from labels.
- Buy/Sell modes are local presentation states in this slice. No stock,
  pricing, or inventory mutation is implemented.

Exact source files changed:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/ElarionNpcsClient.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcTradeScreen.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfigValidator.java`

Exact tests changed:

- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/config/NpcConfigValidatorTest.java`
- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/config/NpcConfigDescriptorsTest.java`

Docs changed:

- `docs/addons/npcs.md`
- `docs/config.md`
- `docs/systems/NPCs.md`
- `docs/systems/Networking.md`
- `docs/systems/GUI.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Tests run:

- `.\gradlew.bat :addons:npcs:compileJava :addons:npcs:compileTestJava`
- `.\gradlew.bat :addons:npcs:test`

Unresolved risks:

- No live screenshot QA was run for the trade shell because no active default
  dialogue opens it yet.
- Real trade ownership is still undefined: stock provider, price provider,
  inventory validation, server-authoritative request/result packets,
  persistence, and Economy formatting integration all need an approved
  boundary before buy/sell mutations.

Precise recommended next slice:

- Audit/propose the real trade owner boundary before implementation.
  Classification: MEDIUM audit/proposal.

## Latest Slice Handoff - Default Trader Content

Date: 2026-07-08.

Objective:

- Add a trader NPC next to the existing banker in the dev world and make the
  trader available as generated default NPC content.

What changed:

- Added `worldheart_trader` to generated NPC defaults.
- Added generated `worldheart_trader` dialogue with normal conversation first,
  then `Trade` into the dedicated non-mutating `NpcTradeScreen`.
- Added the same trader definition and dialogue to the current dev-run config
  because default writers do not overwrite existing config files.
- Added `worldheart_trader_1` to
  `dev/run/world/elarion/addon-state/npcs/placed-npcs.json`, two blocks to the
  visual left of `worldheart_banker_1` in `elarion:lobby`.
- Left `entityId` null so the NPC placement reconciler owns spawning the
  entity on startup or repair.
- Added descriptor test coverage for additional/trader NPC definitions.

Files changed:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfigDefaults.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfigLoader.java`
- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/config/NpcConfigDescriptorsTest.java`
- `dev/run/config/elarion/addons/npcs/npcs.yml`
- `dev/run/config/elarion/addons/npcs/dialogues/worldheart_trader.yml`
- `dev/run/world/elarion/addon-state/npcs/placed-npcs.json`
- `docs/addons/npcs.md`
- `docs/config.md`
- `docs/systems/NPCs.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :addons:npcs:compileJava :addons:npcs:compileTestJava`.
- Passed `.\gradlew.bat :addons:npcs:test`.
- Parsed the placement JSON and confirmed `worldheart_trader_1` is present at
  `x=-3.4076143949727384, y=65.0, z=-0.4783191965878622`.
- Live QA attempt under `build/ui-qa/slice-trader-placement/` confirmed the
  server loaded `2 NPC definitions, 3 skin profiles, 2 portrait profiles, and 2
  dialogues`, but the client join was blocked by Minecraft's `Invalid Session`
  screen before the server received a player login. Captures:
  `01-client-current.png` and `02-multiplayer.png`.

Unresolved risks:

- Full in-world screenshot QA is still pending because the client join was
  blocked by `Invalid Session`.
- The trader still uses the placeholder/mara dev visual profiles until a
  dedicated trader skin/portrait asset is chosen.
- The trade screen remains non-mutating until the real trade owner boundary is
  approved.

Precise recommended next slice:

- Live QA `worldheart_trader_1`: verify placement beside the banker, normal NPC
  conversation first, and `Trade` opening the dedicated trade shell. Then
  continue the MEDIUM trade-owner boundary audit before enabling buy/sell
  behavior.

## Latest Slice Handoff - Banker And Trader Visual Polish

Date: 2026-07-08.

Objective:

- Polish the dedicated banker/trader service presentation without changing
  Economy mutation, trade mutation, dialogue authority, or persistence.

What was inspected:

- `NpcBankScreen`, `NpcTradeScreen`, `ElarionNpcPortraitRenderer`,
  `NpcConfigDefaults`, dev-run NPC definition/profile files, the existing
  Portal ticket item ID, the shared Core currency texture helper, NPC docs,
  UI journal, UI audit, `PLAN.md`, and `TODO.md`.

Decisions made:

- Currency presentation should use the shared Sigil icon in bank balance,
  amount input, fee/total, and trader price surfaces.
- Banker/trader service NPCs should use explicit texture skins plus curated
  portrait-library images rather than placeholder/player portraits.
- The trader catalog rows are display-only QA content. Real stock, pricing,
  inventory checks, and buy/sell mutations remain blocked on a future trade
  owner boundary.
- Standard Minecraft 64x64 skin UV layout keeps empty atlas gaps; black/dark
  seams inside active body faces are asset defects and were cleaned.

Exact source/resource files changed in this follow-up:

- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionUiRenderer.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcBankScreen.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcTradeScreen.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/ui/ElarionNpcPortraitRenderer.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfigDefaults.java`
- `addons/npcs/src/main/resources/assets/elarion/textures/entity/npc/worldheart_banker.png`
- `addons/npcs/src/main/resources/assets/elarion/textures/entity/npc/worldheart_trader.png`
- `dev/run/config/elarion/addons/npcs/npcs.yml`
- `dev/run/config/elarion/addons/npcs/skins.yml`
- `dev/run/config/elarion/addons/npcs/portraits.yml`

Docs changed:

- `docs/addons/npcs.md`
- `docs/systems/NPCs.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `docs/config.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Tests run:

- `.\gradlew.bat :platform:core:compileJava :addons:npcs:compileJava :addons:npcs:compileTestJava`
- `.\gradlew.bat :addons:npcs:test`

Live screenshot QA:

- Not run, by user request.

Unresolved risks:

- The trade catalog rows are not real stock and do not mutate inventory.
- The real trade owner boundary still needs stock provider, price provider,
  inventory validation, request/result packets, persistence owner, and Economy
  formatting integration.
- In-world visual QA for the cleaned banker/trader skins and curated portraits
  completed under `build/ui-qa/npc-polish-live-2/`.

Precise recommended next slice:

- Continue with the MEDIUM trade-owner boundary audit/proposal before enabling
  any real buy/sell behavior.

## Latest Slice Handoff - Banker And Trader Live QA Follow-Up

Date: 2026-07-08.

Objective:

- Correct portrait/currency sampling, native trader tooltips, and bank caret
  placement, then verify the banker/trader surfaces and skins live.

Decisions and implementation:

- Full portrait textures use explicit 32x32 or 64x64 source dimensions,
  preventing repeated portrait tiles.
- Shared Sigil icons use explicit 16x16 source dimensions at every destination
  size.
- Bank amount text begins after the Sigil icon. The caret uses the same scaled
  width metric and remains after the final digit; the empty field starts at the
  numeric input origin.
- Trader hover state keeps the real `ItemStack`, and native tooltips render
  after the scaled screen matrix is popped.
- `minecraft-qa.ps1` supports right-click and key actions for faster NPC QA.

Exact files changed in this follow-up:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcBankScreen.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcTradeScreen.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/ui/ElarionNpcPortraitRenderer.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionUiRenderer.java`
- `addons/npcs/src/main/resources/assets/elarion/textures/entity/npc/worldheart_banker.png`
- `addons/npcs/src/main/resources/assets/elarion/textures/entity/npc/worldheart_trader.png`
- `dev/tools/minecraft-qa.ps1`
- NPC/UI docs, audit, plan, TODO, and this handoff.

Verification:

- `.\gradlew.bat :platform:core:compileJava :addons:npcs:compileJava :addons:npcs:compileTestJava :addons:npcs:test` passed.
- Live QA passed for conversation-first banker/trader entry, dedicated bank and
  trade screens, caret blink/placement, Sigil icons, Protection IV tooltip,
  custom lore tooltip, and front-facing in-world skins.
- Evidence: `build/ui-qa/npc-polish-live-2/`.

Unresolved risks and deferred work:

- Real buy/sell remains intentionally unavailable. The trade owner boundary
  still needs stock provider, price provider, inventory validation,
  request/result packets, persistence ownership, and Economy formatting.
- Numeric deposit/withdraw success and rejection feedback remains future live
  QA; this pass did not mutate balances.
- NPC nameplates overlap at the current two-block dev placement from some
  camera angles; no nameplate or placement behavior changed in this slice.

Precise recommended next slice:

- MEDIUM audit/proposal for the real NPC trade owner boundary. Do not implement
  mutations until ownership, bounded stock queries, persistence, packets, and
  Economy integration are approved.

## Latest Slice Handoff - NPC Trade Owner Audit

Date: 2026-07-08.

Objective:

- Define ownership, dependency, network, persistence, recovery, and staged
  implementation contracts for real NPC trading without changing production
  behavior.

What was inspected:

- NPC trade UI, dialogue sessions, config validation, public API, networking,
  placement persistence, and module dependencies.
- Economy public API, mixed payments, transaction persistence, service prices,
  and Portal compensation behavior.
- Core reward actions, deferred reward grants, item fit/delivery, Networking,
  Persistence, dependency, and UI contracts.

Decisions:

- NPCs owns catalogs, offer eligibility, stock, trade sessions, purchase
  orchestration, and recovery state.
- Economy remains the only currency owner through a one-way optional
  NPC-owned adapter. Economy must not depend on NPCs.
- Core owns generic deferred reward delivery, history/events, and shared UI;
  it does not own merchant state.
- The hardcoded client catalog must be replaced by a bounded server-authored
  read-only snapshot before any mutation work.
- Real buying requires idempotent Economy operation receipts plus an NPC-owned
  purchase journal. Selling is a later independent contract.

Files changed:

- Added `docs/reports/NPC_TRADE_OWNER_AUDIT.md`.
- Updated `PLAN.md`, `TODO.md`, `INDEX.md`, `OPTIMIZATION_TRACKER.md`,
  `docs/architecture/DEPENDENCY_GRAPH.md`, `docs/systems/NPCs.md`,
  `docs/systems/Networking.md`, `docs/systems/Persistence.md`,
  `docs/systems/UI_JOURNAL.md`, `docs/reports/UI_SYSTEM_AUDIT.md`,
  `docs/addons/npcs.md`, and this handoff.

Production/API/config/persistence changes:

- None. This was an audit-only slice.

Verification:

- Source and docs were inspected with targeted repository searches.
- `git diff --check` is the required final documentation verification.
- No build was required because production source/resources did not change in
  this slice.

Unresolved decisions before mutation slices:

- Current service payment rule: Portal fees and Shrine currency offerings
  consume carried physical Sigils only. Banked balance must be withdrawn first.
  The bank may apply configured withdrawal tax; deposits are untaxed.
- Recommended finite stock scope is per placed NPC.
- Recommended full-inventory behavior is reject before payment, with claimable
  mail reserved for crash recovery.
- Sell pricing and component matching remain deferred to the Sell slice.

Precise recommended next slice:

- Phase 4, Slice 22, MEDIUM: server-authored read-only NPC trade catalogs.
  Include parsed `trades.yml` definitions, validation, defaults, read-only
  descriptors/tests, NPC catalog references, bounded snapshot transport, and
  client rendering. Exclude Economy dependencies, payment, purchase requests,
  runtime stock, persistence, and enabled Buy/Sell controls.

## Latest Slice Handoff - Server-Authored NPC Trade Catalogs

Date: 2026-07-08.

Objective:

- Replace client-hardcoded trader preview rows with NPC-owned parsed
  `trades.yml` definitions and a bounded server-authored read-only snapshot.

Decisions:

- NPCs owns trade catalog definitions and snapshot presentation.
- `trade-catalog` on `NpcDefinition` links an NPC to a catalog ID.
- `NpcTradeSnapshotPayload` is S2C-only and carries display previews, prices,
  disabled-state text, and `ItemStack` preview data for native tooltips.
- All offers remain non-mutating. Payment, stock, purchase packets, inventory
  mutation, and Economy dependency are deferred.
- Nether and End ticket art is selected through the real Portal ticket item:
  Nether/default uses the crimson stele icon, End uses custom model data `2`
  and the blue stele icon. NPC trade offers can set `custom-model-data` for
  server-authored preview art.
- Follow-up regression fixes moved Portal ticket item textures into the
  portals item texture namespace, bridged legacy `worldheart_trader` configs
  with missing/blank `trade-catalog` to the generated catalog in memory,
  preserved banker Deposit/Withdraw mode across server refreshes, and added
  `price-key` as future Economy/tax/inflation pricing metadata.
- The next dependency is an Economy idempotent operation receipt foundation
  before any crash-safe purchase journal can be implemented.

Exact production files changed:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/model/NpcDefinition.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/model/NpcTradeCatalogDefinition.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/model/NpcTradeOfferDefinition.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/model/NpcTradeEnchantmentDefinition.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfig.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfigDefaults.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfigLoader.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfigValidator.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfigDescriptors.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/network/NpcTradeOfferPayload.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/network/NpcTradeSnapshotPayload.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/service/NpcDefinitionService.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/service/NpcInteractionService.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/service/NpcTradeSnapshotService.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/ElarionNpcsClient.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcTradeScreen.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcBankScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionCivicUi.java`
- `addons/portals/src/main/java/panetina/elarion/addons/portals/PortalTicketItem.java`
- `addons/portals/src/main/java/panetina/elarion/addons/portals/client/PortalConfirmationScreen.java`
- `addons/portals/src/main/resources/assets/elarion/models/item/portal_ticket.json`
- `addons/portals/src/main/resources/assets/elarion/models/item/portal_ticket_end.json`
- `addons/portals/src/main/resources/assets/elarion/textures/item/portal_ticket_nether.png`
- `addons/portals/src/main/resources/assets/elarion/textures/item/portal_ticket_end.png`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionUiIcons.java`

Exact test files changed:

- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/config/NpcConfigDescriptorsTest.java`
- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/config/NpcConfigValidatorTest.java`
- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/config/NpcBankPresentationMigrationTest.java`
- `addons/portals/src/test/java/panetina/elarion/addons/portals/PortalTicketItemTest.java`

Docs changed:

- `PLAN.md`
- `TODO.md`
- `INDEX.md`
- `OPTIMIZATION_TRACKER.md`
- `docs/addons/npcs.md`
- `docs/addons/portals.md`
- `docs/config.md`
- `docs/systems/NPCs.md`
- `docs/systems/Networking.md`
- `docs/systems/Persistence.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `docs/reports/NPC_TRADE_OWNER_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :platform:core:compileJava :addons:npcs:compileJava :addons:npcs:compileTestJava`.
- Passed `.\gradlew.bat :addons:npcs:test --tests panetina.elarion.addons.npcs.config.NpcConfigDescriptorsTest --tests panetina.elarion.addons.npcs.config.NpcConfigValidatorTest`.
- Passed `.\gradlew.bat :platform:core:compileJava :addons:npcs:compileJava :addons:npcs:compileTestJava :addons:portals:compileJava :addons:portals:compileTestJava`.
- Passed `.\gradlew.bat :addons:npcs:test --tests panetina.elarion.addons.npcs.config.NpcConfigDescriptorsTest --tests panetina.elarion.addons.npcs.config.NpcConfigValidatorTest :addons:portals:test --tests panetina.elarion.addons.portals.PortalTicketItemTest`.
- Passed follow-up compile/test with
  `NpcBankPresentationMigrationTest` included after the `worldheart_trader`
  catalog bridge, banker tab preservation, `price-key`, and item-texture fixes.
- Live QA server startup was attempted under
  `build/ui-qa/npc-trade-snapshot-redo/`; server reached ready state and
  loaded `1 trade catalogs`, but the client hit Minecraft `Invalid Session`
  before joining localhost. No in-world trader screenshot was captured.

Unresolved risks:

- No in-world live screenshot QA was captured for this slice because the
  client was blocked by `Invalid Session` before joining localhost.
- No trade purchase request, stock persistence, payment, or inventory mutation
  exists. The UI must remain display-only.
- Older worlds with custom trader IDs still need explicit `trade-catalog`
  entries or a future approved migration because defaults do not overwrite
  customized files. Only the shipped `worldheart_trader` has the in-memory
  compatibility bridge.

Precise recommended next slice:

- Phase 4, Slice 23: Economy idempotent operation receipt proposal/foundation.
  Define operation IDs, durable receipt/index shape, charge/refund semantics,
  migration/backups, rollback, and restart tests before NPC purchases are
  enabled. Classification: LARGE proposal, then split implementation.

## Latest Follow-Up - Economy Policy For Bank Interest, Taxes, Physical Service Payments

Date: 2026-07-08.

Objective:

- Add config-backed Economy policy for bank interest, bank withdrawal tax, and
  future shop tax, while changing Shrine and Portal service payments to require
  carried physical currency instead of charging bank balances.

Decisions:

- Deposits remain untaxed.
- Withdrawals can charge `bank.withdrawal-tax-basis-points`; the tax is an
  audited `TAX` transaction before physical currency is issued.
- Bank interest is disabled by default and, when enabled, pays audited
  `REWARD` transactions in bounded account batches controlled by
  `bank.interest.max-accounts-per-tick`.
- Portal fee-passage and Shrine currency offerings now use
  `ElarionEconomyApi.payPhysicalOnly(...)`. Players must withdraw banked money
  before those spends.
- Physical Sigil item stacks now max at 999; withdrawal stack creation uses the
  registered item max count.
- `shops.sales-tax-basis-points` is config metadata for future real shop/NPC
  purchase settlement. Current trader rows remain read-only.
- The bank screen Fee row does not yet preview nonzero withdrawal tax; server
  feedback and transaction metadata include tax after submit. A TODO records
  the future server-authored tax preview payload.

Files changed in this follow-up:

- `addons/economy/src/main/java/panetina/elarion/addons/economy/config/EconomyConfig.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/config/EconomyConfigDescriptors.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/service/EconomyTransactionService.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/service/EconomyInventoryService.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/api/ElarionEconomyApi.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/registry/EconomyNpcActions.java`
- `addons/offerings/src/main/java/panetina/elarion/addons/offerings/service/OfferingService.java`
- `addons/portals/src/main/java/panetina/elarion/addons/portals/service/PortalRouteService.java`
- `addons/economy/src/test/java/panetina/elarion/addons/economy/config/EconomyConfigDescriptorsTest.java`
- `addons/economy/src/test/java/panetina/elarion/addons/economy/service/EconomyTransactionServiceTest.java`
- `addons/economy/src/test/java/panetina/elarion/addons/economy/service/EconomyGovernorServiceTest.java`
- `docs/addons/economy.md`
- `docs/addons/offerings.md`
- `docs/addons/portals.md`
- `docs/systems/CommunityContribution.md`
- `docs/config.md`
- `docs/api.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :addons:economy:compileJava :addons:economy:compileTestJava :addons:offerings:compileJava :addons:portals:compileJava`.
- Passed `.\gradlew.bat :addons:economy:test --tests panetina.elarion.addons.economy.config.EconomyConfigDescriptorsTest --tests panetina.elarion.addons.economy.service.EconomyTransactionServiceTest --tests panetina.elarion.addons.economy.service.EconomyGovernorServiceTest --tests panetina.elarion.addons.economy.registry.EconomyNpcActionsTest`.

Next recommended slice:

- Phase 4, Slice 23 remains the Economy idempotent operation receipt
  proposal/foundation before enabling real NPC shop purchases.

## Latest Follow-Up - Trader Row Polish And QA Session Gate

Date: 2026-07-08.

Objective:

- Fix trader row price alignment, row tooltip hitboxes, ticket name styling,
  and improve repeatable QA client launches.

Decisions:

- Trader prices use a fixed Sigil icon column with the amount drawn directly
  after the icon on every row.
- Native item tooltips open only from the actual item icon hitbox. Row hover
  still highlights the row but does not show enchantments/lore.
- NPC trade preview custom names/lore and real Portal ticket names/lore use
  non-italic text components.
- `runClientOne` and `runClientTwo` pass stable local UUID/access-token values
  for QA launches.
- Physical Sigil item max stack remains source-configured as 999; stale
  runtime views showing 99 require a refreshed join to verify.

Files changed:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcTradeScreen.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/service/NpcTradeSnapshotService.java`
- `addons/portals/src/main/java/panetina/elarion/addons/portals/PortalTicketItem.java`
- `dev/build.gradle`
- `CODEX.md`
- `PLAN.md`
- `TODO.md`
- `docs/systems/NPCs.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :addons:npcs:compileJava :addons:portals:compileJava :addons:economy:compileJava :addons:economy:compileTestJava :addons:economy:test --tests panetina.elarion.addons.economy.EconomyItemsTest`.
- Dev server reached ready state under `build/ui-qa/trade-fixes-20260708/`
  and loaded `1 trade catalogs`.
- Live in-world QA is blocked: `runClientOne` still shows Minecraft `Invalid
  Session` when opening Multiplayer, even after restarting with stable local
  UUID/token args. Captures:
  `build/ui-qa/trade-fixes-20260708/05-client-retry-menu.png` and
  `build/ui-qa/trade-fixes-20260708/06-multiplayer-after-session-args.png`.

Precise recommended next slice:

- First clear the QA session gate or add a local UI-open QA path that does not
  require Multiplayer. Then resume Phase 4, Slice 23: Economy idempotent
  operation receipt foundation before real NPC shop purchases.

## Latest Follow-Up - Trader QA Completion And 999 Sigil Runtime Ceiling

Date: 2026-07-08.

Objective:

- Complete the trader row QA follow-up and make physical Sigils truly stack to
  999 in live player inventory, not only in the Economy constant.

Decisions:

- The apparent Multiplayer `Invalid Session` blocker was an automation
  coordinate error. `minecraft-qa.ps1` clicks client-area coordinates while
  screenshots include the Windows title bar; the mistaken maximized click hit
  Realms instead of Multiplayer.
- Minecraft 1.21.1 has a vanilla `Inventory#getMaxCountPerStack()` default of
  99 and an ItemStack serialized count codec capped at 99. Economy now owns
  the extra runtime support needed for 999-count Sigil stacks.
- The inventory ceiling is raised to 999 globally, but ordinary vanilla item
  stacks remain limited by their own item max counts. The serialized ItemStack
  count codec is raised to 999 so high-count Sigil stacks can sync/save safely.

Files changed:

- `addons/economy/src/main/java/panetina/elarion/addons/economy/mixin/InventoryMaxStackCountMixin.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/mixin/ItemStackCountCodecMixin.java`
- `addons/economy/src/main/resources/elarion_economy.mixins.json`
- `addons/economy/src/main/resources/fabric.mod.json`
- `addons/economy/src/test/java/panetina/elarion/addons/economy/EconomyItemsTest.java`
- `CODEX.md`
- `INDEX.md`
- `PLAN.md`
- `TODO.md`
- `docs/addons/economy.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :addons:economy:compileJava :addons:economy:processResources :addons:economy:test --tests panetina.elarion.addons.economy.EconomyItemsTest`.
- Fresh dev server booted after the mixins and loaded `1 trade catalogs`.
- Live QA evidence under
  `build/ui-qa/trade-fixes-20260708-stack999/`:
  - `03-sigil-999-stack.png`: `/give @s elarion:currency 999` renders as one
    hotbar stack with `999`.
  - `05-trader-catalog.png`: trader prices share a fixed Sigil icon/value
    column.
  - `06-row-hover-no-tooltip-fixed.png`: row hover highlights without showing
    native item tooltip.
  - `07-icon-hover-tooltip-fixed.png`: armor icon hover shows enchantment and
    attributes.
  - `08-ticket-tooltip-fixed.png`: Nether ticket icon hover shows non-italic
    ticket name/lore.

Unresolved risks:

- High-count physical stacks now depend on Economy's mixins staying loaded on
  both server and client. Any future currency-like item should reuse or
  explicitly document this ceiling instead of changing only item settings.
- Older historical notes in `PLAN.md`/`TODO.md` may still mention invalid
  sessions from earlier slices. The current live QA path is unblocked when
  using client-area coordinates.

Precise recommended next slice:

- Phase 4, Slice 23: Economy idempotent operation receipt proposal/foundation
  before enabling real NPC shop purchases. Classification: LARGE proposal,
  then split implementation.

## Latest Slice Handoff - NPC Purchase Foundation Proposal

Date: 2026-07-08.

Objective:

- Extend the trade-owner plan with per-NPC Realm/world registration, separate
  tax jurisdiction, visible tax pricing, and multi-unit buying without changing
  production behavior.

Decisions:

- NPC definitions declare `tax-jurisdiction: auto|realm:<id>|world:<id>`.
- Each placed NPC persists the resolved registration; `auto` prefers the Realm
  owning its world and otherwise uses the world.
- Economy exclusively resolves price and tax. Realm tax targets the Realm
  treasury; non-Realm world activity targets the owner-administered Worldheart
  treasury.
- Selected offers use one detail/action panel with minus, numeric quantity,
  plus, Max, and server-authored Subtotal/Tax/Total.
- Implementation is split into Economy receipts, NPC jurisdiction migration,
  tax quote/quantity UI, unlimited purchases, and finite stock.

Files changed:

- `docs/reports/NPC_TRADE_PURCHASE_FOUNDATION_PROPOSAL.md`
- `docs/reports/NPC_TRADE_OWNER_AUDIT.md`
- `docs/architecture/DEPENDENCY_GRAPH.md`
- `docs/systems/NPCs.md`
- `docs/systems/Persistence.md`
- `docs/systems/UI_JOURNAL.md`
- `INDEX.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Production/API/config/persistence changes:

- None. This was the mandatory proposal slice before persistence/API work.

Verification:

- Targeted source audit covered NPC definition/placement ownership, Core Realm
  world lookup, Economy accounts/taxes, trade snapshots, and trader layout.
- `git diff --check` is the final documentation verification.

Resolved policy:

- Realm authorities set category taxes through the future Seat of Rule Taxes
  tab and receive revenue in their Realm treasury.
- Worldheart, marketplace, Nether, End, and other non-Realm activity uses the
  owner-administered Worldheart treasury.
- Shop purchases use physical Sigils only.

Precise recommended next slice:

- Phase 4 Slice 23A, LARGE: Economy idempotent receipt schema/API migration.
  No NPC packet/UI/purchase mutation in 23A.

## Latest Slice Handoff - Phase 4 Slice 23A Economy Receipts

Date: 2026-07-08.

Objective:

- Add a crash-safe, bounded, O(1) Economy idempotency foundation before NPC
  purchases can move physical Sigils.

Implementation:

- Added `EconomyOperationKey` and `EconomyOperationReceipt`.
- Added `EconomyTransactionService.executeOnce(...)` and O(1) receipt lookup;
  `ElarionEconomyApi` exposes `transactOnce(...)` and `operationReceipt(...)`.
- Operation owner, UUID, request fingerprint, and result message are durable
  transaction metadata. Journal replay reconstructs receipts created after the
  previous snapshot.
- Economy state schema v2 stores bounded receipts. Schema v1 is backed up as
  `economy-state.json.schema-v1.bak` and atomically migrated. Unsupported or
  malformed schemas fail closed.
- Receipt retention is bounded by `operations.receipt-retention-days` and
  `operations.max-receipts`; cap eviction is O(1), expiration cleanup is
  batched.
- Identical replay returns the original transaction. Conflicting reuse returns
  `IDEMPOTENCY_CONFLICT`. Persisted rejected operations also replay
  deterministically.

Exact production files changed:

- `addons/economy/src/main/java/panetina/elarion/addons/economy/model/EconomyOperationKey.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/model/EconomyOperationReceipt.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/model/TransactionStatus.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/storage/EconomyState.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/storage/EconomyStorage.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/service/EconomyTransactionService.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/api/ElarionEconomyApi.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/config/EconomyConfig.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/config/EconomyConfigDescriptors.java`

Tests changed:

- `addons/economy/src/test/java/panetina/elarion/addons/economy/service/EconomyTransactionServiceTest.java`
- `addons/economy/src/test/java/panetina/elarion/addons/economy/service/EconomyGovernorServiceTest.java`
- `addons/economy/src/test/java/panetina/elarion/addons/economy/config/EconomyConfigDescriptorsTest.java`

Docs changed:

- `docs/reports/NPC_TRADE_PURCHASE_FOUNDATION_PROPOSAL.md`
- `docs/addons/economy.md`
- `docs/api.md`
- `docs/config.md`
- `docs/systems/Persistence.md`
- `docs/architecture/DEPENDENCY_GRAPH.md`
- `wiki/admin/economy.md`
- `OPTIMIZATION_TRACKER.md`
- `INDEX.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `./gradlew.bat :addons:economy:test`.
- Coverage includes identical replay, conflict rejection, rejected-operation
  replay, pre-snapshot journal reconstruction, snapshot restart, receipt cap
  eviction, schema-v1 backup migration, and unsupported-schema fail-closed.
- `git diff --check` remains required after final docs.

Deferred:

- Worldheart treasury account/policy, Realm tax rates, NPC jurisdiction,
  quote packets/UI, purchases, stock, and selling.

Precise recommended next slice:

- Phase 4 Slice 23B, MEDIUM config/persistence: NPC definition jurisdiction
  policy plus placed-NPC resolved Realm/world registration and schema migration.
  Exclude Economy tax settlement, quote UI, and purchases.

## Latest Slice Handoff - Phase 4 Slice 23B NPC Jurisdiction

Date: 2026-07-08.

Objective:

- Give every placed NPC a stable, auditable Realm or world tax jurisdiction
  without implementing tax settlement or purchases.

Implementation:

- NPC definitions accept `tax-jurisdiction: auto|realm:<id>|world:<id>`;
  missing values default to `auto`.
- Generated Worldheart banker/trader definitions use
  `world:elarion:worldheart`.
- `NpcTaxJurisdictionResolver` uses Core `RealmService.ownerForWorld` through
  the public API. `auto` prefers the Realm owner and otherwise records the
  world. Explicit policies reject mismatched placement worlds.
- `PlacedNpcRecord` stores typed `REALM|WORLD` kind and stable ID.
- Placement schema v2 backs up schema v1, resolves every record, and writes
  atomically. Invalid/unsupported state fails closed.
- Place, duplicate, move, repair, startup, and reload paths validate before
  entity mutation. Candidate config reload restores previous definitions if
  current placements reject the new policy.
- `/e npc inspect` reports the resolved jurisdiction.

Production files changed:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/model/NpcTaxJurisdictionKind.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/model/NpcDefinition.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/model/PlacedNpcRecord.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/service/NpcTaxJurisdictionResolver.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/service/NpcPlacementService.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/service/NpcDefinitionService.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/storage/NpcPlacementStorage.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfigLoader.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfigValidator.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfigDescriptors.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfigDefaults.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/command/NpcCommands.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/ElarionNpcsAddon.java`

Tests added/changed:

- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/service/NpcTaxJurisdictionResolverTest.java`
- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/storage/NpcPlacementStorageTest.java`
- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/model/PlacedNpcRecordTest.java`
- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/config/NpcConfigValidatorTest.java`
- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/config/NpcConfigDescriptorsTest.java`

Docs changed:

- `docs/reports/NPC_TRADE_PURCHASE_FOUNDATION_PROPOSAL.md`
- `docs/addons/npcs.md`
- `docs/config.md`
- `docs/commands.md`
- `docs/systems/Persistence.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/architecture/DEPENDENCY_GRAPH.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `wiki/admin/npcs.md`
- `INDEX.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `./gradlew.bat :addons:npcs:test`.
- Coverage includes auto Realm/world resolution, explicit mismatch rejection,
  policy validation, descriptor values, record-field preservation, schema-v1
  backup migration, migration failure preserving the original, and unsupported
  schema fail-closed.
- `git diff --check` is required after final documentation.

Deferred:

- Worldheart treasury account/rates, Realm category tax rates, Economy quote
  adapter, quantity UI, purchase packets/journal, stock, and selling.

Precise recommended next slice:

- Phase 4 Slice 23C, MEDIUM network/UI: Economy-owned tax policy/quote resolver,
  optional NPC Economy quote adapter, bounded quantity quote transport, and
  selected-offer quantity panel. Confirm remains disabled; no purchase mutation.

## Latest Slice Handoff - Phase 4 Slice 23C Tax Quotes

Date: 2026-07-08.

Objective:

- Add Economy-owned Realm/Worldheart category tax quotes and a bounded trader
  quantity panel without enabling purchases.

Implementation:

- Economy owns strict schema-v1 `tax-policies.json`, an O(1) policy map, typed
  tax authority/category models, and checked subtotal/tax/total arithmetic.
- Missing NPC-trade overrides use the existing shop-sales-tax config fallback;
  other categories default to zero.
- NPCs suggests Economy and loads an optional quote adapter. Economy absence
  leaves offers visible and disabled.
- Every quantity request is bounded to 1-64 and revalidates active session,
  range, trade node, catalog ID/revision, and offer ID.
- Trader rows select without changing height. A bounded panel renders minus,
  plus, Max, quantity,
  subtotal, Realm/Worldheart tax, total, and disabled Confirm.

Production areas changed:

- Economy tax models/service/storage and `ElarionEconomyApi`.
- NPC quote provider/integration, trade payloads, interaction validation,
  snapshot service, client receiver, and `NpcTradeScreen`.
- NPCs has an optional compile-time/public-API edge to Economy and a runtime
  `suggests` declaration; Economy has no NPC dependency.

Verification:

- `:addons:economy:compileJava` and `:addons:npcs:compileJava` passed.
- Focused tax-policy tests pass for fallback, independent authorities, restart,
  arithmetic, bounds, overflow, and malformed/unsupported fail-closed state.
- Full `:addons:economy:test :addons:npcs:test` passed.
- `git diff --check` is the final documentation verification.

Deferred:

- No physical Sigils, treasury balance, inventory, stock, or purchase state is
  mutated. No Worldheart treasury account exists yet.
- Seat of Rule and owner Admin Panel tax editors remain future authorized UI.

Precise recommended next slice:

- Phase 4 Slice 23D, LARGE persistence/network: unlimited BUY purchase journal,
  idempotent physical-Sigil settlement, Realm/Worldheart treasury routing,
  deferred delivery, restart reconciliation, and request/result tests.

## Latest Slice Handoff - Phase 4 Slice 23C.1 Worldheart Authority And Treasury Route

Date: 2026-07-09.

Objective:

- Add future-proof Worldheart authority infrastructure and stable Worldheart
  treasury routing before enabling NPC purchase settlement.

Implementation:

- Core owns persistent Worldheart governing authority state in
  `world/elarion/worldheart/authority.json`.
- Missing authority data defaults to `SYSTEM` governance with the lore-facing
  display identity `Hollow Emperor`.
- `PLAYER` authority stores a normal player UUID and requires that the player
  resolves as an existing citizen through the service dependency.
- `ElarionApi.worldheart()` exposes `WorldheartGovernanceService`, which
  centralizes authority display, `isRuler`, system/player switching, and role
  classification for server administrator, current ruler, or none.
- Authority changes emit the Core domain event
  `elarion_core:worldheart-authority-changed`.
- Economy owns the stable `EconomyAccount.WORLDHEART_TREASURY` account and a
  schema-v3 `worldheartTreasury` state field. This is separate from Core
  authority and never becomes a player wallet.
- `EconomyTaxDestinationResolver` maps Realm tax authorities to Realm
  treasuries and Worldheart/non-Realm authorities to the stable Worldheart
  treasury.

Production files changed in this slice:

- `platform/core/src/main/java/panetina/elarion/core/model/WorldheartAuthority.java`
- `platform/core/src/main/java/panetina/elarion/core/model/WorldheartAuthorityType.java`
- `platform/core/src/main/java/panetina/elarion/core/model/WorldheartGovernanceRole.java`
- `platform/core/src/main/java/panetina/elarion/core/storage/WorldheartAuthorityStorage.java`
- `platform/core/src/main/java/panetina/elarion/core/service/WorldheartGovernanceService.java`
- `platform/core/src/main/java/panetina/elarion/core/api/ElarionApi.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/model/EconomyAccountType.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/model/EconomyAccount.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/storage/EconomyState.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/storage/EconomyStorage.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/service/EconomyTransactionService.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/service/EconomyTaxDestinationResolver.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/api/ElarionEconomyApi.java`

Tests added/changed:

- `platform/core/src/test/java/panetina/elarion/core/storage/WorldheartAuthorityStorageTest.java`
- `platform/core/src/test/java/panetina/elarion/core/service/WorldheartGovernanceServiceTest.java`
- `addons/economy/src/test/java/panetina/elarion/addons/economy/service/EconomyTransactionServiceTest.java`
- `addons/economy/src/test/java/panetina/elarion/addons/economy/service/EconomyTaxDestinationResolverTest.java`

Docs updated:

- `AGENTS.md`
- `CODEX.md`
- `INDEX.md`
- `OPTIMIZATION_TRACKER.md`
- `PLAN.md`
- `TODO.md`
- `docs/addons/core.md`
- `docs/addons/economy.md`
- `docs/api.md`
- `docs/architecture/DEPENDENCY_GRAPH.md`
- `docs/config.md`
- `docs/reports/NPC_TRADE_PURCHASE_FOUNDATION_PROPOSAL.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `docs/systems/Networking.md`
- `docs/systems/Persistence.md`
- `docs/systems/UI_JOURNAL.md`
- `wiki/admin/economy.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.storage.WorldheartAuthorityStorageTest --tests panetina.elarion.core.service.WorldheartGovernanceServiceTest`.
- Passed `.\gradlew.bat :addons:economy:test --tests panetina.elarion.addons.economy.service.EconomyTransactionServiceTest --tests panetina.elarion.addons.economy.service.EconomyTaxDestinationResolverTest`.

Deferred:

- No Empty Throne/ascension event, claimant tracking, ceremony, Emperor UI,
  Emperor commands, or eligibility rules were added.
- No tax editor, Seat of Rule Taxes tab, owner Admin Panel Worldheart tool, or
  control block was implemented.
- No NPC purchases, inventory delivery, finite stock, selling, or purchase
  journal mutation was implemented.

Precise recommended next slice:

- Phase 4 Slice 23D, LARGE persistence/network: implement unlimited BUY
  purchase journaling and settlement. Use physical Sigils only, reuse Economy
  `executeOnce`, resolve tax destinations through `EconomyTaxDestinationResolver`
  / API, route Realm tax to Realm treasury and Worldheart/non-Realm tax to
  `WORLDHEART_TREASURY`, add deferred delivery/reconciliation, and keep stock
  and selling out of scope.

## Latest Slice Handoff - Phase 4 Slice 23D Unlimited NPC BUY Purchases

Date: 2026-07-09.

Objective:

- Enable crash-aware unlimited BUY purchases for NPC traders using physical
  Sigils, server quotes, idempotent Economy settlement, and the NPC purchase
  journal. Keep finite stock and selling out of scope.

Implementation:

- Added `NpcTradePurchaseRequestPayload` and `NpcTradePurchaseResultPayload`.
- Added `NpcTradePurchaseService`, `NpcTradePurchaseStorage`, and purchase
  records with PREPARED, PAID, COMPLETE, and FAILED states.
- Added `NpcTradeItemStacks` so preview and delivery share item construction
  for custom names, custom model data, lore, and enchantments.
- Added `NpcTradePurchaseProvider` and optional
  `EconomyNpcTradePurchaseProvider`.
- Added Economy `PUBLIC_REVENUE` transaction type and
  `ElarionEconomyApi.payPhysicalOnlyOnce(...)`.
- Purchases revalidate active dialogue session, distance, trade node, catalog
  revision, offer ID, item availability, quantity, and server quote.
- Purchase settlement consumes carried physical Sigils only, credits the
  resolved Realm or Worldheart treasury, and records subtotal/tax metadata.
- Existing completed/failed purchase IDs replay deterministically. PAID records
  reconcile delivery on request replay or player join.
- The trader screen enables Confirm only for valid server quotes and displays
  server result feedback.

Files changed in this slice:

- `addons/economy/src/main/java/panetina/elarion/addons/economy/model/EconomyTransactionType.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/service/EconomyTransactionService.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/service/EconomyInventoryService.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/api/ElarionEconomyApi.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/ElarionNpcsAddon.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/ElarionNpcsClient.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcTradeScreen.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/integration/EconomyNpcTradePurchaseProvider.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/model/NpcTradeDeliveryStack.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/model/NpcTradePurchaseRecord.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/model/NpcTradePurchaseSettlement.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/model/NpcTradePurchaseStatus.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/network/NpcTradePurchaseRequestPayload.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/network/NpcTradePurchaseResultPayload.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/service/NpcInteractionService.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/service/NpcTradeItemStacks.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/service/NpcTradePurchaseProvider.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/service/NpcTradePurchaseService.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/service/NpcTradeSnapshotService.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/storage/NpcTradePurchaseStorage.java`

Tests added/changed:

- `addons/economy/src/test/java/panetina/elarion/addons/economy/service/EconomyTransactionServiceTest.java`
- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/network/NpcTradePurchasePayloadTest.java`
- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/storage/NpcTradePurchaseStorageTest.java`

Docs updated:

- `INDEX.md`
- `OPTIMIZATION_TRACKER.md`
- `PLAN.md`
- `TODO.md`
- `docs/addons/economy.md`
- `docs/addons/npcs.md`
- `docs/api.md`
- `docs/reports/NPC_TRADE_PURCHASE_FOUNDATION_PROPOSAL.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `docs/systems/Persistence.md`
- `docs/systems/UI_JOURNAL.md`
- `wiki/admin/economy.md`
- `wiki/admin/npcs.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :addons:economy:compileJava :addons:npcs:compileJava`.
- Passed `.\gradlew.bat :addons:economy:test :addons:npcs:test`.

Unresolved risks / deferred:

- No live screenshot QA was run for Confirm feedback.
- Finite stock, lazy restock, Sell/buyback, dynamic `price-key`, and inflation
  pricing remain future work.
- Delivery currently inserts into inventory and drops overflow. A future
  Core-owned deferred item delivery/reward queue would make post-payment crash
  recovery stronger than inventory/drop-only delivery.

Precise recommended next slice:

- Phase 4 Slice 23E, MEDIUM persistence/server slice: finite placed-NPC stock
  for BUY offers. Add per-placed-NPC offer stock state, lazy restock policy,
  contention tests, purchase-journal interaction, restart behavior, and UI
  stock labels. Keep Sell/buyback and dynamic inflation pricing out of scope.

## Phase 4 Slice 23E - Finite Placed-NPC Stock

Status: completed on 2026-07-09.

Objective:

- Add finite BUY stock for placed NPCs without adding Sell/buyback or dynamic
  pricing.

What changed:

- `NpcTradeOfferDefinition` now parses `stock-limit`, `restock-amount`, and
  `restock-interval-seconds`. Missing fields keep offers unlimited.
- `NpcConfigLoader`, `NpcConfigValidator`, `NpcConfigDefaults`, and
  `NpcConfigDescriptors` expose those fields. The generated Worldheart trader
  defaults now include finite stock/restock values.
- `NpcTradeStockService` and `NpcTradeStockStorage` persist schema-v1
  `world/elarion/addon-state/npcs/trade-stock.json`.
- Stock records are keyed by placed NPC UUID plus offer ID, not by definition
  ID, so two placed traders using one catalog have independent inventory.
- Restock is lazy during trader open/quote/purchase paths; no tick loop or
  global merchant scan was added.
- Stock records retain a bounded set of consumed purchase IDs so request replay
  cannot decrement stock twice.
- `NpcTradeSnapshotService` clamps quote maximums by stock and sends stock
  metadata in `NpcTradeOfferPayload`.
- `NpcTradePurchaseService` revalidates stock before settlement and consumes it
  after successful Economy settlement.
- `NpcTradeScreen` shows `Stock N` as compact row metadata for finite offers.
  Native item stack tooltips remain icon-only.

Files changed in this slice:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/ElarionNpcsAddon.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcTradeScreen.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfigDefaults.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfigDescriptors.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfigLoader.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfigValidator.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/model/NpcTradeOfferDefinition.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/model/NpcTradeStockRecord.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/network/NpcTradeOfferPayload.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/service/NpcTradePurchaseService.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/service/NpcTradeSnapshotService.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/service/NpcTradeStockService.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/storage/NpcTradeStockStorage.java`
- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/config/NpcConfigDescriptorsTest.java`
- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/storage/NpcTradeStockStorageTest.java`

Docs updated:

- `INDEX.md`
- `OPTIMIZATION_TRACKER.md`
- `PLAN.md`
- `TODO.md`
- `docs/addons/npcs.md`
- `docs/config.md`
- `docs/reports/NPC_TRADE_PURCHASE_FOUNDATION_PROPOSAL.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `docs/systems/Persistence.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :addons:npcs:compileJava :addons:npcs:test`.

Unresolved risks / deferred:

- No live screenshot QA was run in this slice.
- At the time of Slice 23E, Sell/buyback remained unimplemented; later Slice
  23M and Slice 24 completed V1 Sell settlement and stock routing.
- Dynamic price-key/inflation behavior remains Economy-owned future work.
- If payment succeeds but stock consume unexpectedly fails, the current
  synchronous path reports failure after settlement. This should be hardened in
  the Sell/buyback proposal by defining explicit refund/rollback handling for
  all post-payment failures.

Precise recommended next slice:

- NPC Sell/buyback audit and proposal. Define accepted item filters,
  inventory validation, buyback pricing, tax/fee routing, rollback/refund
  behavior, packet shape, UI states, and tests before adding Sell mutations.

## Phase 4 Slice 23F - NPC Sell/Buyback Audit And Proposal

Status: completed on 2026-07-09.

Objective:

- Define the dupe-safe Sell/buyback architecture before adding any Sell
  mutations.

What was inspected:

- `docs/reports/NPC_TRADE_OWNER_AUDIT.md`
- `docs/reports/NPC_TRADE_PURCHASE_FOUNDATION_PROPOSAL.md`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/service/NpcTradePurchaseService.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/service/EconomyInventoryService.java`
- Current NPC trade quote/purchase/stock model and Economy tax/receipt search
  results.

Decisions:

- Sell is not the inverse of Buy. The safe invariant is item escrow before
  payout: exact matching player stacks must be removed into durable NPC-owned
  escrow before Economy pays anything.
- NPCs owns sell definitions, server-side inventory matching, sale journals,
  item escrow, and stock destination policy.
- Economy owns dynamic price/inflation quotes, fees/taxes, idempotent payouts,
  and currency delivery.
- V1 should support explicit `exact_item` and `exact_stack` match modes only.
  Arbitrary appraisal/sell-anything belongs to a later market system.
- UI remains non-authoritative and must not choose authoritative slots,
  components, prices, fees, stock, or payout.

Files changed:

- `docs/reports/NPC_SELL_BUYBACK_PROPOSAL.md`
- `docs/reports/NPC_TRADE_PURCHASE_FOUNDATION_PROPOSAL.md`
- `docs/addons/npcs.md`
- `INDEX.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Tests / checks:

- Documentation/source consistency checks only. No production code changed.

Unresolved risks / deferred:

- Economy still needs an explicit dynamic trade price API and idempotent
  physical payout API before Sell can move items or money.
- Full inventory payout recovery should use a deliberate Core claimable
  delivery path or an explicitly approved bank payout policy.
- The existing BUY post-payment stock-consume edge remains documented for
  hardening when settlement rollback/refund APIs are designed.

Precise recommended next slice:

- Phase 4 Slice 23G: Economy dynamic price and idempotent payout API proposal
  for NPC trades. Define price-key behavior, stock/inflation inputs, bounded
  lookup guarantees, physical payout idempotency, metadata, tests, and failure
  states before implementing Java changes.

## Phase 4 Slice 23G - Economy Trade Price And Payout API Proposal

Status: completed on 2026-07-09.

Objective:

- Define the Economy-owned API shape for NPC/future marketplace trade pricing,
  dynamic inflation hooks, and idempotent seller payouts before adding Java
  behavior.

What was inspected:

- `addons/economy/src/main/java/panetina/elarion/addons/economy/api/ElarionEconomyApi.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/service/EconomyPricingService.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/service/EconomyInventoryService.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/service/EconomyTransactionService.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/model/EconomyTaxQuote.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/model/EconomyTransactionType.java`
- Current NPC `price-key` usage and Economy/NPC docs.

Decisions:

- Economy owns `price-key` resolution, dynamic price/inflation policy, fee/tax
  calculation, idempotent seller payouts, and bounded price-policy indexes.
- NPCs passes catalog/offer/stock context to Economy; it must not duplicate
  pricing/inflation math or mutate balances.
- First implementation should add pure `EconomyTradePriceRequest` /
  `EconomyTradePriceQuote` records and a static/service-price quote method.
  Full inflation/scarcity counters come later behind the same API.
- Seller payout needs a future `payPhysicalRewardOnce(...)` wrapper that checks
  receipts before inserting currency. It should not be bundled with first
  price-quote models.

Files changed:

- `docs/reports/ECONOMY_TRADE_PRICE_PAYOUT_API_PROPOSAL.md`
- `docs/addons/economy.md`
- `docs/api.md`
- `INDEX.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Tests / checks:

- Documentation/source consistency checks only. No production code changed.

Unresolved risks / deferred:

- No Java API exists yet for trade price quotes.
- No idempotent physical payout wrapper exists yet for NPC Sell.
- Full inventory payout recovery policy is still unresolved; preferred future
  path is Core claimable/deferred delivery.

Precise recommended next slice:

- Phase 4 Slice 23H: implement Economy trade price request/quote records and a
  pure static/service-price quote method with unit tests. Exclude physical
  payout and NPC Sell mutation.

## Phase 4 Slice 23H - Economy Trade Price Quote API

Status: completed on 2026-07-09.

Objective:

- Implement the pure Economy trade price quote API shape without physical
  payout or NPC Sell mutation.

What changed:

- Added `EconomyTradeDirection`.
- Added `EconomyTradePriceSource`.
- Added `EconomyTradePriceRequest`.
- Added `EconomyTradePriceQuote`.
- Added `EconomyPricingService.quoteTradePrice(...)`.
- Added public `ElarionEconomyApi.quoteTradePrice(...)`.
- Added a test-only `EconomyPricingService(Map<String, EconomyServicePrice>)`
  constructor so tests do not touch host config files.
- Current quotes resolve known service-price `price-key` values or fixed
  fallback prices, then use `EconomyTaxPolicyService` for checked fee/tax math.
- BUY quotes return total cost; SELL quotes return net payout.
- Dynamic inflation/scarcity counters remain future work behind the same API.

Files changed:

- `addons/economy/src/main/java/panetina/elarion/addons/economy/api/ElarionEconomyApi.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/model/EconomyTradeDirection.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/model/EconomyTradePriceQuote.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/model/EconomyTradePriceRequest.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/model/EconomyTradePriceSource.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/service/EconomyPricingService.java`
- `addons/economy/src/test/java/panetina/elarion/addons/economy/service/EconomyTradePricingServiceTest.java`
- `docs/reports/ECONOMY_TRADE_PRICE_PAYOUT_API_PROPOSAL.md`
- `docs/addons/economy.md`
- `docs/api.md`
- `INDEX.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :addons:economy:compileJava :addons:economy:test`.

Unresolved risks / deferred:

- No idempotent physical payout wrapper exists yet.
- NPC Sell definitions and mutations remain disabled/deferred.
- Dynamic price/inflation state is not implemented; the API accepts bounded
  context so it can be added later without changing NPCs.

Precise recommended next slice:

- Phase 4 Slice 23I: idempotent physical payout wrapper for seller rewards.
  Decide and implement full-inventory recovery behavior without adding NPC Sell
  mutations.

## Phase 4 Slice 23I - Idempotent Physical Payout Wrapper

Status: completed on 2026-07-09.

Objective:

- Add the first Economy-owned idempotent physical seller-payout boundary
  without enabling NPC Sell or moving player items.

What was inspected:

- `addons/economy/src/main/java/panetina/elarion/addons/economy/service/EconomyInventoryService.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/service/EconomyTransactionService.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/api/ElarionEconomyApi.java`
- Core deferred reward/action search results.

What changed:

- Added `EconomyInventoryService.payPhysicalRewardOnce(...)`.
- Added public `ElarionEconomyApi.payPhysicalRewardOnce(...)`.
- The wrapper checks an existing `EconomyOperationKey` receipt before touching
  inventory, so identical replay cannot insert duplicate Sigils.
- The wrapper rejects null operation keys, invalid amounts, and insufficient
  physical inventory space before writing the payout transaction.
- Successful payout records an idempotent `REWARD` transaction from `MINT` to
  `PHYSICAL_CURRENCY`, then inserts physical Sigils.
- Added package-level capacity math coverage for empty slots, partial stacks,
  invalid inputs, and overflow clamping.

Files changed:

- `addons/economy/src/main/java/panetina/elarion/addons/economy/service/EconomyInventoryService.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/api/ElarionEconomyApi.java`
- `addons/economy/src/test/java/panetina/elarion/addons/economy/service/EconomyInventoryServiceTest.java`
- `docs/reports/ECONOMY_TRADE_PRICE_PAYOUT_API_PROPOSAL.md`
- `docs/addons/economy.md`
- `docs/api.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :addons:economy:compileJava :addons:economy:test`.
- Passed `git diff --check` for the touched Economy/API/docs files.

Unresolved risks / deferred:

- This wrapper is duplicate-safe but not yet delivery-complete for live NPC
  Sell. A crash after receipt persistence and before inventory insertion can
  leave the payout undelivered.
- NPC Sell definitions, Sell packet mutation, item escrow, and UI mutation
  remain disabled/deferred.
- Full dynamic inflation/scarcity pricing remains future work behind
  `quoteTradePrice(...)`.

Precise recommended next slice:

- Phase 4 Slice 23J: payout delivery recovery hardening. Preferred path is a
  Core/Economy claimable physical payout delivery keyed by the same operation
  ID, or an explicit approved bank-payout policy. Keep NPC Sell mutation out
  until post-receipt delivery is restart-safe. Classification: MEDIUM.

## Phase 4 Slice 23J - Bank-Backed Seller Payout Recovery

Status: completed on 2026-07-09.

Objective:

- Close the seller payout recovery gap for the first live NPC Sell path without
  adding Sell definitions, item escrow, packets, or client mutation.

Decision:

- V1 NPC Sell will pay sellers into their Economy player wallet through an
  idempotent operation receipt. This is the safe policy with the current
  storage model because physical inventory insertion is not atomic with Economy
  receipt persistence.
- Physical-only spending remains unchanged. NPC shop purchases, Shrine
  contributions, and Portal/service payments should still require carried
  Sigils unless a later approved policy changes them.
- `payPhysicalRewardOnce(...)` remains available but is not approved for live
  NPC Sell until Core/Economy has a real delivery queue for post-receipt
  physical insertion recovery.

What changed:

- Added `EconomyTransactionService.rewardOnce(...)`.
- Added public `ElarionEconomyApi.rewardOnce(...)`.
- Added public `ElarionEconomyApi.payPlayerBalanceRewardOnce(...)`.
- Added Economy transaction test coverage for wallet payout replay, conflicting
  replay rejection, and restart replay without duplicate credit.

Files changed:

- `addons/economy/src/main/java/panetina/elarion/addons/economy/service/EconomyTransactionService.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/api/ElarionEconomyApi.java`
- `addons/economy/src/test/java/panetina/elarion/addons/economy/service/EconomyTransactionServiceTest.java`
- `docs/reports/ECONOMY_TRADE_PRICE_PAYOUT_API_PROPOSAL.md`
- `docs/addons/economy.md`
- `docs/api.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :addons:economy:compileJava :addons:economy:test`.

Unresolved risks / deferred:

- NPC Sell/buyback still has no definitions, packets, prompts/actions, item
  escrow, payout execution, or UI mutation.
- Physical seller payout still needs a future Core/Economy delivery queue if it
  is ever preferred over wallet payout.
- Dynamic inflation/scarcity counters remain future work behind
  `quoteTradePrice(...)`.

Precise recommended next slice:

- Phase 4 Slice 23K: NPC Sell definition parsing. Add disabled-by-default SELL
  definitions, descriptors, and validation only. Exclude inventory mutation,
  item escrow, payout execution, prompts/actions, packets, and client-side shop
  mutation. Classification: SMALL.

## Phase 4 Slice 23K - NPC Sell Definition Parsing

Status: completed on 2026-07-09.

Objective:

- Add disabled-by-default Sell/buyback definition parsing and descriptor
  coverage without enabling any Sell runtime mutation.

What was inspected:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/model/NpcTradeOfferDefinition.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfigLoader.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfigValidator.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfigDescriptors.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfigDefaults.java`
- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/config/NpcConfigValidatorTest.java`
- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/config/NpcConfigDescriptorsTest.java`
- `docs/reports/NPC_SELL_BUYBACK_PROPOSAL.md`

What changed:

- Extended `NpcTradeOfferDefinition` with normalized Sell metadata:
  `sellMatch`, `componentPolicy`, `maxQuantity`, and `stockDestination`.
- `NpcConfigLoader` parses `sell-match`, `component-policy`,
  `max-quantity`, and `stock-destination` from `trades.yml`.
- `NpcConfigValidator` now accepts `direction: sell` only as disabled config
  data. Enabled Sell offers are rejected until the sale journal/escrow and
  settlement slices exist.
- Sell config validation accepts only `sell-match: exact_item|exact_stack`,
  `component-policy: vanilla_only|exact_components`,
  `stock-destination: none|placed_npc`, and `max-quantity` 1-64.
- The generated `worldheart_trader` catalog now includes a disabled
  `cobblestone_buyback` Sell example.
- `NpcConfigDescriptors` exposes direction, enabled state, count, and all
  Sell-specific fields for trade offers.
- Updated NPC/config/Sell docs and navigation.

Files changed:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/model/NpcTradeOfferDefinition.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfigLoader.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfigValidator.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfigDescriptors.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfigDefaults.java`
- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/config/NpcConfigValidatorTest.java`
- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/config/NpcConfigDescriptorsTest.java`
- `docs/addons/npcs.md`
- `docs/config.md`
- `docs/reports/NPC_SELL_BUYBACK_PROPOSAL.md`
- `INDEX.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :addons:npcs:compileJava :addons:npcs:test`.

Unresolved risks / deferred:

- Sell/buyback still has no sale journal, item escrow, inventory removal,
  payout execution, packets, prompts/actions, server snapshot rows, or
  client-side shop mutation.
- The V1 payout policy is wallet payout through
  `ElarionEconomyApi.payPlayerBalanceRewardOnce(...)`, but it is not wired to
  NPCs yet.
- Future dynamic inflation/scarcity pricing remains behind Economy
  `quoteTradePrice(...)`.

Precise recommended next slice:

- Phase 4 Slice 23L: NPC sale journal and escrow storage. Add schema-v1
  NPC-owned sale records and serialized item escrow with round-trip, restart,
  unsupported-schema, and replay-state tests only. Exclude payout, player
  inventory mutation, packets, prompts/actions, and UI mutation. Classification:
  MEDIUM.

## Phase 4 Slice 23L - NPC Sale Journal And Escrow Storage

Status: completed on 2026-07-09.

Objective:

- Add NPC-owned sale journal and serialized escrow storage without executing
  payouts, mutating player inventory, adding packets, adding prompts/actions,
  or enabling client-side Sell mutation.

What was inspected:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/model/NpcTradePurchaseRecord.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/storage/NpcTradePurchaseStorage.java`
- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/storage/NpcTradePurchaseStorageTest.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/model/NpcTradeDeliveryStack.java`
- `docs/reports/NPC_SELL_BUYBACK_PROPOSAL.md`
- `docs/systems/Persistence.md`

What changed:

- Added `NpcTradeSaleStatus` with explicit replay/recovery states:
  `PREPARED`, `ITEMS_ESCROWED`, `PAID`, `STOCK_UPDATED`, `COMPLETE`,
  `FAILED`, and `RESTORED`.
- Added `NpcTradeEscrowStack` for serialized item escrow identity: item ID,
  count, custom name, lore, enchantments, custom model data, and fingerprint.
- Added `NpcTradeSaleRecord` with sale ID, player/NPC/session/catalog/offer
  fields, quantity, server quote snapshot values, escrow payload, Economy
  operation/transaction IDs, request matching, and transition helpers.
- Added `NpcTradeSaleStorage` for schema-v1
  `world/elarion/addon-state/npcs/trade-sales.json`.
- Added storage tests for escrow round-trip, restart/replay-state persistence,
  request matching, and unsupported schema fail-closed behavior.
- Updated NPC docs, Persistence docs, Sell/buyback proposal, navigation,
  `PLAN.md`, and `TODO.md`.

Files changed:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/model/NpcTradeSaleStatus.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/model/NpcTradeEscrowStack.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/model/NpcTradeSaleRecord.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/storage/NpcTradeSaleStorage.java`
- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/storage/NpcTradeSaleStorageTest.java`
- `docs/addons/npcs.md`
- `docs/systems/Persistence.md`
- `docs/reports/NPC_SELL_BUYBACK_PROPOSAL.md`
- `INDEX.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :addons:npcs:compileJava :addons:npcs:test`.

Unresolved risks / deferred:

- No runtime service uses `NpcTradeSaleStorage` yet.
- No player inventory matching/removal, item restore, payout execution, stock
  update from sales, packets, prompts/actions, or UI Sell mutation exists yet.
- The next risky part is exact inventory matching and escrow removal semantics,
  especially Minecraft 1.21.1 item components and partial stacks.

Precise recommended next slice:

- Phase 4 Slice 23M-Prereq: inventory escrow service audit/proposal. Inspect
  Fabric 1.21.1 item/component APIs and current NPC item stack helpers, then
  define exact matching, bounded removal, serialized escrow, restore behavior,
  stale quote/catalog reload behavior, and tests. Do not mutate player
  inventory or add Sell packets/UI yet. Classification: SMALL.

## Phase 4 Slice 23M-Prereq - Inventory Escrow Service Proposal

Status: completed on 2026-07-09.

Objective:

- Define the server-side inventory escrow contract needed before live NPC
  Sell/buyback can remove items or pay sellers.

What was inspected:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/service/NpcTradeItemStacks.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/service/NpcTradePurchaseService.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/model/NpcTradeSaleRecord.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/model/NpcTradeEscrowStack.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/model/NpcTradeOfferDefinition.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/service/NpcTradeStockService.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/service/EconomyInventoryService.java`
- `addons/underworld/src/main/java/panetina/elarion/addons/underworld/model/StoredItemStack.java`
- `docs/reports/NPC_SELL_BUYBACK_PROPOSAL.md`
- `docs/addons/npcs.md`
- `PLAN.md`
- `TODO.md`

Decisions:

- Live NPC Sell needs an NPC-owned inventory escrow helper; do not use client
  slots as authoritative.
- The helper must preflight full quantity, serialize removed stacks with
  `ItemStack.encode(registries)`, remove only after enough matching stacks are
  found, and restore from durable escrow before payout when settlement fails.
- Use Underworld's `StoredItemStack` pattern as a reference only. NPCs should
  not depend on the Underworld addon type.
- The existing `NpcTradeEscrowStack` audit fields are not enough for live Sell
  because they cannot faithfully restore all components. Add full encoded stack
  payload before any player inventory mutation.
- Match policies stay server-authored:
  `exact_item` by item ID, `exact_stack` by item/component equality,
  `vanilla_only` for clean default stacks, and `exact_components` for named,
  lored, enchanted, damaged, or custom-component offers.

Files changed:

- `docs/reports/NPC_SELL_BUYBACK_PROPOSAL.md`
- `docs/addons/npcs.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Documentation-only slice; no Java compile was required.
- `git diff --check` was run for the touched docs/planning files.

Unresolved risks / deferred:

- No code exists yet for NPC stored escrow stack payloads.
- No player inventory removal, restore, payout, Sell packets, or Sell UI
  mutation is enabled yet.
- BUY delivery still has overflow-drop behavior; this slice did not change it,
  but live Sell must not copy that behavior.

Precise recommended next slice:

- Phase 4 Slice 23M1: NPC stored escrow stack payload. Add an NPC-owned
  component-preserving stored stack record and unit tests based on the Fabric
  1.21.1 `ItemStack.encode(registries)` / `ItemStack.fromNbt(...)` pattern.
  Extend sale escrow storage to write full encoded stack payload while
  preserving compatibility with existing lossy schema-v1 test rows. Do not
  remove player inventory, pay sellers, add Sell packets, or mutate UI yet.
  Classification: SMALL.

## Phase 4 Slice 23M - Server Sell Settlement Completion

Status: completed on 2026-07-09.

Objective:

- Finish the remaining NPC Sell/buyback Slice 23 work: component-preserving
  escrow payload, server-side inventory matching/removal, Economy payout,
  replay handling, and Trade UI Sell enablement.

What changed:

- Extended `NpcTradeEscrowStack` with full encoded `ItemStack` payload,
  source-slot metadata, restore support, and compatibility constructors for
  older lossy rows.
- Added `NpcTradeEscrowResult`.
- Added `NpcTradeInventoryEscrow` for server-side main-inventory matching,
  bounded removal, encoded escrow creation, and restore preflight/application.
- Added `NpcTradeSaleSettlement` and `NpcTradeSaleProvider`.
- Added `EconomyNpcTradeSaleProvider`, paying sellers through
  `ElarionEconomyApi.payPlayerBalanceRewardOnce(...)` with idempotent
  operation IDs.
- Updated `EconomyNpcTradeQuoteProvider` to use Economy `quoteTradePrice(...)`
  for both BUY and SELL, so SELL shows net payout after tax/fee.
- Updated `NpcTradeSnapshotService` to include Sell offers and compute Sell max
  quantity from the player's server-side matching inventory.
- Updated `NpcTradePurchaseService` to route Sell requests through the sale
  journal, escrow, idempotent payout, and replay states.
- Updated `ElarionNpcsAddon` to construct the sale provider/storage path.
- Updated `NpcTradeScreen` so the Sell tab renders server-authored rows instead
  of the old placeholder and sends the existing request payload.
- Enabled the generated default `worldheart_trader` cobblestone buyback row.
- Updated config validation to allow enabled Sell rows with valid policies.
- Added sale storage test coverage for encoded escrow payload fields.
- Updated NPC/config/persistence/report/index/plan/todo docs.

Files changed:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/model/NpcTradeEscrowStack.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/model/NpcTradeEscrowResult.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/model/NpcTradeSaleSettlement.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/service/NpcTradeInventoryEscrow.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/service/NpcTradeSaleProvider.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/service/NpcTradePurchaseService.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/service/NpcTradeSnapshotService.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/integration/EconomyNpcTradeQuoteProvider.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/integration/EconomyNpcTradeSaleProvider.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/ElarionNpcsAddon.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcTradeScreen.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfigDefaults.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfigValidator.java`
- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/storage/NpcTradeSaleStorageTest.java`
- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/config/NpcConfigValidatorTest.java`
- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/config/NpcConfigDescriptorsTest.java`
- `docs/addons/npcs.md`
- `docs/config.md`
- `docs/systems/Persistence.md`
- `docs/reports/NPC_SELL_BUYBACK_PROPOSAL.md`
- `INDEX.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :addons:npcs:compileJava :addons:npcs:test`.

Safety decisions:

- The client never selects authoritative inventory slots.
- Sell requests are revalidated against server catalog revision, server-side
  inventory, and Economy quote.
- Items are removed only after full matching quantity is found.
- Removed stacks are persisted with full encoded payloads before payout.
- Seller payout goes to the Economy wallet, not physical Sigils, because wallet
  payout is receipt-backed and restart-safe.
- Payout failure attempts escrow restoration before marking the sale failed.
- Replaying `ITEMS_ESCROWED` retries idempotent payout without removing items
  again. Replaying `PAID` or `STOCK_UPDATED` completes the journal.

Unresolved risk / limitation:

- Superseded by Slice 24: `stock-destination: placed_npc` no longer remains a
  parsed no-op; the config model now has `destination-offer` and the sale
  completion path can replenish configured resale stock.
- Full live UI QA was not run in this slice.

Precise recommended next slice:

- Phase 4 Slice 24, recorded below, completed the stock destination hardening.

## Phase 4 Slice 24 - NPC Trade Stock Destination Hardening

Status: completed on 2026-07-09.

Objective:

- Finish the `stock-destination: placed_npc` contract by making Sell offers
  name the BUY offer that receives stock, validating that route, and applying
  sold quantity idempotently after seller payout.

What changed:

- Added `destination-offer` to `NpcTradeOfferDefinition`, config parsing,
  generated `worldheart_trader` defaults, read-only descriptors, and descriptor
  tests.
- Updated NPC trade validation so a Sell offer using
  `stock-destination: placed_npc` must target a BUY offer in the same catalog.
- Added `NpcTradeStockService.supply(...)`, reusing bounded operation-ID replay
  protection so a sale retry cannot increment destination stock twice.
- Updated `NpcTradePurchaseService` so paid Sell completion supplies the
  configured destination BUY stock before marking the sale stock-updated and
  complete. If stock update fails after payout, the paid sale remains retryable
  instead of being marked failed.
- Updated NPC/config/persistence/report/index/plan/todo docs to remove the old
  no-op limitation.

Files changed:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/model/NpcTradeOfferDefinition.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfigLoader.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfigDefaults.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfigValidator.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfigDescriptors.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/service/NpcTradeStockService.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/service/NpcTradePurchaseService.java`
- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/config/NpcConfigValidatorTest.java`
- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/config/NpcConfigDescriptorsTest.java`
- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/storage/NpcTradeStockStorageTest.java`
- `docs/addons/npcs.md`
- `docs/config.md`
- `docs/systems/NPCs.md`
- `docs/systems/Persistence.md`
- `docs/reports/NPC_SELL_BUYBACK_PROPOSAL.md`
- `INDEX.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :addons:npcs:compileJava :addons:npcs:test`.

Safety decisions:

- Stock supply uses the sale ID as the replay key and is capped by the
  destination BUY offer's `stock-limit`.
- Sell payout remains Economy-wallet based; no tax/treasury or personal-wallet
  ownership changed in this slice.
- Destination validation is catalog-local and does not introduce cross-addon or
  cross-catalog stock ownership.

Unresolved risk / deferred:

- Full live QA was not run in this slice.
- Older existing `trades.yml` files are not overwritten by defaults, so worlds
  that already had `cobblestone_buyback` with `stock-destination: placed_npc`
  must add `destination-offer` or use an approved migration.
- Dynamic price/inflation/scarcity policy remains Economy-owned future work.

Precise recommended next slice:

- Phase 4 Slice 25: NPC trade live QA and older-config migration assessment.
  Verify Buy/Sell replay, destination stock replenishment, item tooltips, Sigil
  presentation, and older `trades.yml` files that may lack
  `destination-offer`. Classification: SMALL.

## Phase 4 Slice 25 - NPC Trade Compatibility And Admin Authoring Notes

Status: completed on 2026-07-09.

Objective:

- Assess the older generated `trades.yml` compatibility gap from Slice 24 and
  document how admins can add trade NPCs without Java code.

What changed:

- Added `NpcTradeOfferDefinition.withDestinationOfferId(...)`.
- Added `NpcConfigLoader.bridgeLegacyTradeDestinations(...)`, a narrow
  in-memory bridge for the known generated
  `worldheart_trader.cobblestone_buyback` route. If that route still has
  `stock-destination: placed_npc` and no `destination-offer`, it is projected
  to `destination-offer: cobblestone`.
- Custom trader catalogs remain strict; the loader does not guess arbitrary
  Sell destinations.
- Added focused tests proving the generated legacy route is bridged and custom
  routes are not guessed.
- Updated NPC/config/system/admin docs. New trade NPCs can be authored without
  new Java code by defining `trade-catalog` in `npcs.yml`, a
  `presentation: trade` node in the dialogue YAML, and offers in `trades.yml`.

Files changed:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/model/NpcTradeOfferDefinition.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfigLoader.java`
- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/config/NpcBankPresentationMigrationTest.java`
- `docs/addons/npcs.md`
- `docs/config.md`
- `docs/systems/NPCs.md`
- `wiki/admin/npcs.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :addons:npcs:compileJava :addons:npcs:test`.

Safety decisions:

- The bridge does not write or rewrite admin YAML.
- The bridge only covers the shipped generated route where the intended target
  is unambiguous.
- Custom Sell stock routes still fail validation unless explicitly configured.

Unresolved risk / deferred:

- No live screenshot QA was run in this slice.
- Richer stock policy, dynamic price/inflation behavior, and wildcard item
  buyback remain future work.

Precise recommended next slice:

- Phase 4 Slice 26: NPC trade live QA. Use the dev server/client path to verify
  the trade screen, item tooltips, Sigil columns, Buy/Sell replay behavior, and
  visible stock replenishment on the placed trader. Classification: SMALL.

## Phase 4 Slice 26 - NPC Trade Live QA And Stock Refresh

Status: completed on 2026-07-09.

Objective:

- Run live dev-client QA for the current NPC trader flow and fix focused
  blockers found during the test.

What was inspected:

- `RULES.md`, `CODEX.md`, `PLAN.md`, `TODO.md`,
  `docs/systems/NPCs.md`, `docs/addons/npcs.md`,
  `docs/systems/UI_JOURNAL.md`, `INDEX.md`.
- Dev-run NPC config under `dev/run/config/elarion/addons/npcs/`.
- NPC interaction/trade services and current live logs under
  `build/ui-qa/slice-26-npc-trade-live/`.

Decisions made:

- The stock-refresh bug is server-side. After a successful mutation, the
  server should send a fresh authoritative snapshot instead of the client
  locally decrementing or requiring a reopen.
- Dev-run `tax-jurisdiction` for the QA banker/trader must stay `auto` because
  the current placement world is `elarion:lobby`; forcing
  `world:elarion:worldheart` fails placement validation.

Exact files changed:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/service/NpcInteractionService.java`
- `dev/run/config/elarion/addons/npcs/npcs.yml`
- `dev/run/config/elarion/addons/npcs/dialogues/worldheart_trader.yml`
- `dev/run/config/elarion/addons/npcs/trades.yml`
- `docs/systems/NPCs.md`
- `docs/addons/npcs.md`
- `INDEX.md`
- `PLAN.md`
- `TODO.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/ai/CURRENT_STATUS.md`

Behavior changed:

- `NpcInteractionService.purchaseTrade(...)` now sends
  `NpcTradePurchaseResultPayload` and, when successful, sends a fresh
  `NpcTradeSnapshotPayload` for the same NPC/node. Visible stock labels now
  reflect persisted placed-NPC stock immediately after Buy/Sell completion.

Verification:

- Passed `.\gradlew.bat :addons:npcs:compileJava :addons:npcs:test`.
- Live QA started `runServer`, connected `runClientOne`, placed
  `worldheart_trader_1`, opened the conversation and trade screens, checked
  icon-only tooltip behavior, checked Sell disabled state without cobblestone,
  bought Nether ticket bundles, and confirmed stock persistence plus visible
  stock refresh.
- Relevant screenshots:
  - `build/ui-qa/slice-26-npc-trade-live/08-trader-dialogue.png`
  - `build/ui-qa/slice-26-npc-trade-live/09-trade-buy.png`
  - `build/ui-qa/slice-26-npc-trade-live/10-hover-nether-icon.png`
  - `build/ui-qa/slice-26-npc-trade-live/11-hover-nether-text.png`
  - `build/ui-qa/slice-26-npc-trade-live/12-trade-sell.png`
  - `build/ui-qa/slice-26-npc-trade-live/18-after-refresh-fix-stock10.png`

Unresolved risks:

- The live QA placed the trader manually because the restarted dev world had no
  placed NPC records. Future QA should script deterministic banker/trader
  placement/reset/opening.
- Client logs still contain unrelated Excalibured resource-pack model warnings.
  No NPC trade payload crash was observed.

Precise recommended next slice:

- Phase 4 Slice 27: NPC trade QA automation/harness cleanup. Add a narrow dev
  QA setup path that can reset/place banker and trader and speed up screenshot
  capture, then continue the broader revamp plan. Classification: SMALL.

## Phase 4 Slice 27 - NPC Trade Action Band Polish

Status: completed on 2026-07-09.

Objective:

- Respond to the visual issue where the trader amount/price/Confirm band looked
  awkward and crossed frame boundaries.

What changed:

- `NpcTradeScreen` now defines named layout constants for catalog rows and the
  selected-offer action band.
- The catalog shell is taller, shows four visible catalog rows, and the
  selected-offer action panel is contained inside it.
- The action band is split into clear zones: Amount controls on the left,
  status/error text in the center, a larger Confirm button on the right, and a
  lower totals row for Subtotal, Tax, and Total/Payout.
- Footer currency totals use a smaller Sigil icon to reduce crowding.
- Page-counter placement moved above the selected-offer band so it no longer
  competes with Amount/Confirm controls.
- Visual controls and mouse hitboxes use the same final coordinates.
- `docs/systems/UI_JOURNAL.md` records the anti-regression rule for future
  trader/shop UIs.

Exact files changed:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcTradeScreen.java`
- `PLAN.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :addons:npcs:compileJava :addons:npcs:test`.
- First live visual evidence before the final row-count expansion:
  `build/ui-qa/slice-27-trade-action-band/06-trade-action-band.png`.
- Post-expansion live rerun started `runServer` and `runClientOne`; the server
  reached ready state, but the client hit Minecraft's `Invalid Session` dialog
  before reaching the server list. Evidence:
  `build/ui-qa/slice-27-trade-action-band/10-multiplayer-rerun.png`.

Unresolved risks:

- Final post-expansion in-world screenshot QA is still pending because the
  client could not join the dev server in this rerun. Retry from a clean client
  session and open `worldheart_trader_1`.

Precise recommended next slice:

- Phase 4 Slice 28: NPC trade QA automation/harness cleanup. Add a small dev
  QA command/script path for resetting/placing banker and trader, then resume
  the broader revamp plan. Classification: SMALL.

## Phase 4 Slice 28 - NPC Trade Layout And QA Helper

Status: completed on 2026-07-09.

Objective:

- Fix the awkward trader amount/price/Confirm area and add a faster repeatable
  banker/trader QA setup path.

What changed:

- `NpcTradeScreen` now uses a taller 390px logical shell.
- The Buy/Sell catalog shows four visible rows above the selected-offer action
  panel.
- The selected-offer panel has a top control strip for `Qty`, `-`, fixed-width
  quantity value, `+`, `Max`, status text, and right-aligned `Confirm`.
- Subtotal, jurisdiction tax, and Total/Payout sit in a separated lower totals
  strip.
- Added `dev/tools/npc-trade-qa.ps1`.
  - `setup` removes/recreates `worldheart_banker_1` and
    `worldheart_trader_1` through normal `/e npc` commands.
  - `capture-trader` positions the player, opens the trader, clicks the trade
    option, and captures the resulting screen.
  - The helper assumes the client is already joined/in-world. `-CloseScreens`
    is opt-in because automatic ESC can disrupt connection/menu state.

Exact files changed:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcTradeScreen.java`
- `dev/tools/npc-trade-qa.ps1`
- `CODEX.md`
- `INDEX.md`
- `docs/test-commands.md`
- `docs/systems/NPCs.md`
- `docs/systems/UI_JOURNAL.md`
- `PLAN.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :addons:npcs:compileJava :addons:npcs:test`.
- `dev/tools/npc-trade-qa.ps1` PowerShell syntax parses successfully.
- Live QA started `runServer`/`runClientOne`, joined the saved localhost server
  after F11 fixed the white-framebuffer startup, ran the setup helper, opened
  the trader, and captured:
  `build/ui-qa/slice-28-npc-trade-layout-qa/05-trade-buy-layout.png`.

Unresolved risks:

- The helper does not auto-join the server. Join localhost first.
- `capture-trader` uses passed client-area coordinates. At fullscreen 1920x1080
  the verified values were `-ClickX 960 -ClickY 540 -TradeButtonX 960
  -TradeButtonY 503`.
- Client logs still show unrelated Excalibured resource-pack model warnings.

Precise recommended next slice:

- Phase 4 Slice 29: continue the broader UI revamp with the next approved
  migrated screen family, or return to economy tax/stock integration if trader
  backend work is the current priority. Classification: SMALL.

## Phase 4 Slice 29 - NPC Trade Readability Polish

Status: completed on 2026-07-09.

Objective:

- Fix remaining trader readability details requested from screenshots: compact
  prices/taxes, better stock placement, cleaner row range text, no redundant row
  descriptions, and no black gutter under NPC portraits.

What changed:

- `NpcTradeScreen` compact rows now show only icon, offer title, finite stock,
  and fixed-column Sigil price.
- Authored offer subtitle/description no longer renders in compact rows.
- Row range text is centered as `Rows A-B / N`.
- Purchase summary uses grouped total cells so label, Sigil icon, and value sit
  close together for Subtotal, tax authority, and Total/Payout.
- `ElarionNpcPortraitRenderer` draws configured portraits/player heads/skin
  heads with a 3px inset instead of 4px, filling the portrait frame more tightly
  and reducing the visible bottom gutter.
- Docs updated with the anti-regression rule.

Exact files changed:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcTradeScreen.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/ui/ElarionNpcPortraitRenderer.java`
- `docs/systems/NPCs.md`
- `docs/systems/UI_JOURNAL.md`
- `PLAN.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :addons:npcs:compileJava :addons:npcs:test`.
- Live QA rerun started `runServer`/`runClientOne`, but the dev client hit
  Minecraft's `Invalid Session` before the server list. Evidence:
  `build/ui-qa/slice-29-npc-trade-readable-polish/02-menu-fullscreen.png`.

Unresolved risks:

- Final screenshot of the new row/totals/portrait polish is pending because the
  client could not reach the server list in this run.

Precise recommended next slice:

- Phase 4 Slice 30: move on from NPC micro-polish and continue the broader
  revamp with the next approved non-NPC screen family. Classification: SMALL.

## Phase 4 Slice 30 - Portal Prompt Cost Kind Contract

Status: completed on 2026-07-09.

Objective:

- Remove the Portal Confirmation free/no-fee text heuristic and replace it
  with an explicit server-authored presentation contract.

What was inspected:

- `addons/portals/src/main/java/panetina/elarion/addons/portals/network/PortalTravelPromptPayload.java`
- `addons/portals/src/main/java/panetina/elarion/addons/portals/client/PortalConfirmationScreen.java`
- `addons/portals/src/main/java/panetina/elarion/addons/portals/ElarionPortalsAddon.java`
- `addons/portals/src/main/java/panetina/elarion/addons/portals/service/PortalRouteService.java`
- Existing Portal focused tests under `addons/portals/src/test/java`.
- Portal/UI docs and the active TODO/PLAN files.

Decisions made:

- Keep the contract small and packet-owned for now: `free`, `ticket`, and
  `fee`.
- Do not design future tax/inflation quote UI inside the travel prompt packet.
- Normalize unknown/null prompt cost kinds to `free` as the safest fallback so
  old or malformed values do not render a fake payment slot.
- Portal UI payment-slot visibility and ticket/currency art must not inspect
  localized requirement strings.

Exact files changed:

- `addons/portals/src/main/java/panetina/elarion/addons/portals/network/PortalTravelPromptPayload.java`
- `addons/portals/src/main/java/panetina/elarion/addons/portals/service/PortalRouteService.java`
- `addons/portals/src/main/java/panetina/elarion/addons/portals/ElarionPortalsAddon.java`
- `addons/portals/src/main/java/panetina/elarion/addons/portals/client/PortalConfirmationScreen.java`
- `addons/portals/src/test/java/panetina/elarion/addons/portals/network/PortalTravelPromptPayloadTest.java`
- `docs/addons/portals.md`
- `docs/systems/UI_JOURNAL.md`
- `TODO.md`
- `PLAN.md`
- `docs/ai/CURRENT_STATUS.md`

APIs/packets changed:

- `PortalTravelPromptPayload` now includes `costKind`.
- Valid normalized values are:
  - `PortalTravelPromptPayload.COST_FREE`
  - `PortalTravelPromptPayload.COST_TICKET`
  - `PortalTravelPromptPayload.COST_FEE`
- The prompt packet schema changed, but no persisted data or config format
  changed.

Verification:

- Passed `.\gradlew.bat :addons:portals:compileJava :addons:portals:test`.

Unresolved risks:

- No live screenshot QA was run in this slice. The code path is covered by
  compile and packet tests, but Portal visual confirmation still needs a later
  screenshot pass for free, ticket, fee, allowed, and blocked states.
- The `fee` cost kind currently represents payable fee prompts and blocked
  fee-return prompts without a stored passage. That is acceptable for current
  icon/layout behavior, but a future richer Portal prompt may want a separate
  non-payable blocked-state presentation kind.

Work deliberately deferred:

- Portal tax/inflation quote UI.
- Portal live screenshot QA.
- Any route/payment behavior change.

Precise recommended next slice:

- Phase 4 Slice 31: continue the broader UI revamp with the next
  non-NPC/non-Portal screen family. Prefer Grave Recovery shell polish if the
  goal is to keep moving through player-facing screens, or Admin Panel
  danger/confirmation QA cleanup if the priority is finishing Phase 3 polish.
  Classification: SMALL.

## Phase 4 Slice 31 - Grave Recovery Shell Polish

Status: completed on 2026-07-09.

Objective:

- Polish the Underworld Grave Recovery screen to better match the shared civic
  popup language while preserving server-authoritative recovery behavior.

What was inspected:

- `addons/underworld/src/main/java/panetina/elarion/addons/underworld/client/GraveRecoveryScreen.java`
- `addons/underworld/src/main/java/panetina/elarion/addons/underworld/network/GraveOpenPayload.java`
- `docs/addons/underworld.md`
- `docs/systems/Underworld.md`
- `docs/systems/UI_JOURNAL.md`

Decisions made:

- Keep the slice visual/client-only. Do not change corpse storage, recovery
  server validation, packets, commands, or item restoration.
- Use one internal layout record for render, scroll, and click calculations so
  visual positions and hitboxes do not drift.
- Keep native tooltips restricted to actual item slots.

Exact files changed:

- `addons/underworld/src/main/java/panetina/elarion/addons/underworld/client/GraveRecoveryScreen.java`
- `docs/addons/underworld.md`
- `docs/systems/Underworld.md`
- `docs/systems/UI_JOURNAL.md`
- `PLAN.md`
- `docs/ai/CURRENT_STATUS.md`

Behavior changed:

- Grave Recovery now uses a framed status/body panel and framed contents grid.
- Header title alignment uses shared typography.
- The footer actions are bounded and centered through shared compact action
  buttons.
- Scroll only consumes wheel input inside the contents frame.

Behavior not changed:

- `GraveOpenPayload` and `GraveRecoverPayload` schemas.
- Corpse access checks, ownership checks, world/range checks, recovery-vault
  behavior, item restoration, inventory mutation, tomb cleanup, persistence, and
  commands.

Verification:

- Passed `.\gradlew.bat :addons:underworld:compileJava :addons:underworld:test`.

Unresolved risks:

- No live screenshot QA was run in this slice. Grave Recovery should still be
  checked in-game with empty, protected, lootable, owner, and error states.

Precise recommended next slice:

- Phase 4 Slice 32: continue through remaining UI families. Recommended small
  choices are Admin Panel danger/confirmation polish, Bank/Economy UI audit, or
  the next bounded player-facing popup. Classification: SMALL.

## Phase 4 Slice 32 - Admin Panel Danger Modal Polish

Status: completed on 2026-07-09.

Objective:

- Polish Admin Panel danger rows and generic action confirmation/input modal
  geometry without changing server-authoritative admin behavior.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ElarionAdminPanelAction.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ElarionAdminPanelRow.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionAdminPanelScreenLayoutTest.java`
- `TODO.md`
- `docs/systems/GUI.md`
- `docs/systems/UI_JOURNAL.md`

Decisions made:

- Keep the slice client-layout only.
- Do not alter `AdminPanelActionPayload`, provider action dispatch, runtime
  reset semantics, permissions, config edit payloads, or config edit shell
  behavior.
- Centralize generic action modal geometry in one package-visible
  `ActionModalLayout` record so render and click handlers cannot drift.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionAdminPanelScreenLayoutTest.java`
- `TODO.md`
- `PLAN.md`
- `docs/ai/CURRENT_STATUS.md`
- `docs/systems/GUI.md`
- `docs/systems/UI_JOURNAL.md`

Behavior changed:

- Danger rows now show hover and selected state while preserving destructive
  styling.
- Generic action confirmation/input modals render body text in a bounded
  message panel.
- Generic action modal render/click geometry now comes from the same layout
  record.

Behavior not changed:

- Admin Panel packets, provider action IDs, permissions, runtime reset behavior,
  config edit shell behavior, config appliers, persistence, and commands.

Verification:

- Passed `.\gradlew.bat :platform:core:compileJava :platform:core:test --tests panetina.elarion.core.client.ElarionAdminPanelScreenLayoutTest`.

Unresolved risks:

- Live screenshot QA is still pending for the Danger Zone row and confirmation
  modal after this polish.

Precise recommended next slice:

- Phase 4 Slice 33: do a bounded Bank/Economy UI audit before changing bank UI
  again, because Economy service taxes, bank interest, physical-currency rules,
  and NPC service screens now interact. Classification: SMALL.

## Phase 4 Slice 33 - Bank/Economy UI Audit

Status: completed on 2026-07-09.

Objective:

- Audit the dedicated NPC bank service UI against Economy-owned withdrawal tax,
  bank interest, physical Sigil, and server-authoritative settlement rules
  before changing bank visuals again.

What was inspected:

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
- `docs/addons/economy.md`
- `docs/addons/npcs.md`
- `docs/systems/UI_JOURNAL.md`

Decisions made:

- Bank presentation stays NPC-owned, but fee/tax/total validity stays
  Economy-owned.
- `NpcBankScreen` must not compute withdrawal tax locally.
- The current Fee row is only safe for zero-tax configs. Nonzero tax preview
  needs a server-authored bank quote.
- Use the NPC trade quote pattern for the next implementation: C2S quote
  request, S2C quote payload, client renders values only, server revalidates on
  submit.

Exact files changed:

- `docs/reports/BANK_ECONOMY_UI_AUDIT.md`
- `INDEX.md`
- `TODO.md`
- `PLAN.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/addons/economy.md`
- `docs/addons/npcs.md`
- `docs/ai/CURRENT_STATUS.md`

Behavior changed:

- None. Documentation/audit slice only.

Verification:

- No production code changed; no compile/test run was required.

Unresolved risks:

- The live bank UI still renders Fee as zero until the next implementation
  adds server-authored quote data.
- Interest and transaction history are intentionally not part of the bank
  service screen yet.

Precise recommended next slice:

- Phase 4 Slice 34: add an Economy-owned bank quote contract and dedicated NPC
  bank quote request/response packets, then wire `NpcBankScreen` to request and
  render quotes for Deposit/Withdraw amount changes. Keep Confirm on the
  existing server-validated prompt submit path. Classification: MEDIUM.

## Phase 4 Slice 34 - Server-Authored Bank Quotes

Status: completed on 2026-07-09.

Objective:

- Add an Economy-owned bank quote contract and dedicated NPC bank quote
  request/response transport so the bank screen does not calculate Fee/Total
  locally.

What was inspected:

- `docs/reports/BANK_ECONOMY_UI_AUDIT.md`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/api/ElarionEconomyApi.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/service/EconomyInventoryService.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/service/EconomyTransactionService.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/ElarionNpcsAddon.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/service/NpcInteractionService.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcBankScreen.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/ElarionNpcsClient.java`
- Existing NPC trade quote packets/provider as the pattern.

Decisions made:

- Keep bank quotes read-only and side-effect free.
- Use a dedicated bank quote packet pair instead of adding bank-specific fields
  to generic dialogue snapshots.
- Keep Confirm on the existing prompt submit path to avoid adding a second
  money mutation route.
- Disable Confirm unless the latest quote matches the current mode and amount
  and is valid.

Exact files changed:

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

APIs/packets changed:

- Added `EconomyBankMode` and `EconomyBankQuote`.
- Added `ElarionEconomyApi.quoteBank(...)`.
- Added `NpcBankQuoteRequestPayload` as C2S.
- Added `NpcBankQuotePayload` as S2C.

Behavior changed:

- Bank Fee/Total preview is now driven by Economy-authored bank quotes.
- Stale bank quotes are ignored if the player changes mode or amount.
- Confirm is disabled until the current matching quote is valid.

Behavior not changed:

- Deposit/Withdraw settlement still goes through the existing
  `NpcDialoguePromptSubmitPayload` path and Economy transaction validation.
- Bank interest, transaction history, tax editing, direct bank spending for
  Portal/Shrine/trader services, persistence, config formats, and commands were
  not changed.

Verification:

- Passed `.\gradlew.bat :addons:economy:test :addons:npcs:test`.

Unresolved risks:

- Live screenshot QA is still needed with nonzero bank withdrawal tax to verify
  visual Fee/Total alignment and invalid/valid state readability.
- The screen does not predict physical inventory capacity on withdraw; current
  mutation behavior still gives currency and relies on existing item insertion
  behavior.

Precise recommended next slice:

- Phase 4 Slice 35: run live banker QA with nonzero withdrawal tax and capture
  Deposit valid/invalid, Withdraw valid/invalid, and post-submit refresh states.
  If live QA is deferred, continue with the next approved screen family. SMALL.

## 2026-07-09 - Phase 4 Slice 41 NPC Trade Action Band And Portal Fee Icon Polish

Objective:

- Apply the last manual-QA fixes for the trader selected-offer panel and paid
  Portal confirmation prompt.

What was inspected:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcTradeScreen.java`
- `addons/portals/src/main/java/panetina/elarion/addons/portals/client/PortalConfirmationScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionUiRenderer.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionUiIcons.java`
- `docs/addons/portals.md`
- `docs/systems/UI_JOURNAL.md`
- `TODO.md`
- `PLAN.md`

Decisions made:

- Keep trader quantity as a compact civic control, but center its value with
  shared text metrics.
- Keep Subtotal and tax in the left/middle summary area; place Total/Payout
  under Confirm because it is the final action amount.
- Use ASCII `v` and `^` as tiny scroll-range markers to stay consistent with
  the repo's ASCII default.
- Render paid Portal prompt currency through `ElarionUiRenderer.currencyIcon`
  instead of the semantic icon catalog so it matches the bank/trader Sigil art.

Exact files changed:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcTradeScreen.java`
- `addons/portals/src/main/java/panetina/elarion/addons/portals/client/PortalConfirmationScreen.java`
- `docs/addons/portals.md`
- `docs/systems/UI_JOURNAL.md`
- `TODO.md`
- `PLAN.md`
- `docs/ai/CURRENT_STATUS.md`

Behavior changed:

- Trader row range text has small `v`/`^` markers and no longer sits as low.
- Trader quantity value text uses the shared centered baseline.
- Trader Total/Payout is visually grouped under Confirm.
- Paid Portal prompts show the shared Sigil texture in the payment slot.

Verification:

- Passed `.\gradlew.bat :platform:core:compileJava :addons:npcs:compileJava :addons:portals:compileJava`.

Unresolved risks:

- No live screenshot QA was run in this slice. User requested manual QA for the
  recent visual work.

Precise recommended next slice:

- After manual QA, continue the big revamp with the next non-NPC screen family
  or a focused performance cleanup slice. Keep the same documentation cadence:
  update `PLAN.md`, `TODO.md`, `docs/ai/CURRENT_STATUS.md`, and UI docs when
  player-facing surfaces change.

## 2026-07-09 - Phase 4 Slice 43 Portal And Grave Recovery Polish

Objective:

- Harden Portal Confirmation and Grave Recovery layout behavior without changing
  Portal economy/tax rules, grave recovery authority, packets, persistence, or
  server-side mutations.

What was inspected:

- `docs/reports/UI_FAMILY_INVENTORY.md`
- `docs/systems/GUI.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/systems/Portals.md`
- `addons/portals/src/main/java/panetina/elarion/addons/portals/client/PortalConfirmationScreen.java`
- `addons/underworld/src/main/java/panetina/elarion/addons/underworld/client/GraveRecoveryScreen.java`
- `addons/portals/src/test/java/panetina/elarion/addons/portals/network/PortalTravelPromptPayloadTest.java`
- `addons/underworld/src/test/java/panetina/elarion/addons/underworld/network/GraveOpenPayloadTest.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionUiIcons.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionUiRenderer.java`

Decisions made:

- Keep Portal payment-slot visibility based only on server-authored
  `PortalTravelPromptPayload.costKind`.
- Keep fee prompts on the shared Sigil currency texture; ticket prompts resolve
  Nether/End/general ticket art through the semantic icon catalog.
- Expose pure layout/icon helpers for tests instead of adding live-only visual
  assumptions.
- Keep Grave Recovery tooltips item-slot-only and keep recovery mutation on the
  existing typed server request.

Exact files changed:

- `addons/portals/src/main/java/panetina/elarion/addons/portals/client/PortalConfirmationScreen.java`
- `addons/portals/src/test/java/panetina/elarion/addons/portals/client/PortalConfirmationScreenLayoutTest.java`
- `addons/underworld/src/main/java/panetina/elarion/addons/underworld/client/GraveRecoveryScreen.java`
- `addons/underworld/src/test/java/panetina/elarion/addons/underworld/client/GraveRecoveryScreenLayoutTest.java`
- `docs/systems/UI_JOURNAL.md`
- `docs/reports/UI_FAMILY_INVENTORY.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :addons:portals:test --tests panetina.elarion.addons.portals.client.PortalConfirmationScreenLayoutTest --tests panetina.elarion.addons.portals.network.PortalTravelPromptPayloadTest`.
- Passed `.\gradlew.bat :addons:underworld:test --tests panetina.elarion.addons.underworld.client.GraveRecoveryScreenLayoutTest --tests panetina.elarion.addons.underworld.network.GraveOpenPayloadTest`.
- Passed targeted `git diff --check` for the touched files.

Unresolved risks:

- No live screenshot QA was run. Portal blocked/free/ticket/fee states and
  Grave populated/full-inventory/scroll states still need manual or live QA
  when desired.

Precise recommended next slice:

- Phase 4 Slice 44: Admin Panel Final Visual/Config UX Polish.
- Recommended model: `Medium`.
- Keep it visual/layout-focused; do not add new config mutation powers.

## 2026-07-09 - Credit-Aware Remaining Roadmap Update

Context:

- User manually confirmed the latest bank/trader/portal polish is acceptable.
- User is low on credits and requested model-tier guidance before every future
  slice.

Decision:

- Future slices must state the recommended model tier before starting:
  `Light`, `Medium`, `High`, or `Very High`.
- Prefer `Light`/`Medium` whenever the work is documentation, QA checklist,
  small UI polish, or contained implementation.
- Reserve `High` for cross-module APIs, networking, persistence, Economy/tax,
  server-authoritative mutation, and unclear bugs.
- Reserve `Very High` only for major architecture decisions, migrations,
  concurrency, or broad refactors.

Roadmap update:

- `PLAN.md` now contains a `Credit-Aware Remaining Revamp Roadmap` covering the
  remaining UI, semantic components, Government archive, Chronicle, NPC
  narrative, placeholders, profile contributors, performance/persistence,
  duplicate cleanup, final verification, and maintenance-guide slices.

Current next slice:

- Phase 5 Slice 15 - Shared Tooltip/Native Item Hover Boundary Audit.
- Recommended model: `Medium`.
- Scope: inventory native item tooltip hover boundaries and migrate one
  low-risk item-slot boundary family if repeated geometry is clear. Do not move
  tooltip contents, visibility rules, or server-authored preview data into Core
  UI.

## 2026-07-10 - Phase 5 Slice 9 Detail Body Layout

Objective:

- Add a shared Core helper for bounded detail body text, section titles, and
  simple key/value row geometry, then migrate one low-risk family.

What was inspected:

- `docs/ui/COMPONENT_REFERENCE.md`
- `docs/reports/SEMANTIC_UI_COMPONENT_AUDIT.md`
- `docs/systems/GUI.md`
- `docs/systems/UI_JOURNAL.md`
- `addons/government/src/main/java/panetina/elarion/addons/government/client/GovernmentUiComponents.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionUiComponentGalleryScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionSemanticRowLayout.java`

Decisions made:

- Keep the helper layout-only and preserve Government's existing rendered
  coordinates.
- Let Government retain icon choices, colors, wrapped text, detail semantics,
  and server-authoritative payloads.
- Include key/value geometry now because it is a repeated detail pattern, but
  migrate only Government's body/section methods in this slice.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionDetailBodyLayout.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ui/ElarionDetailBodyLayoutTest.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/client/GovernmentUiComponents.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionUiComponentGalleryScreen.java`
- `docs/ui/COMPONENT_REFERENCE.md`
- `docs/reports/SEMANTIC_UI_COMPONENT_AUDIT.md`
- `docs/systems/UI_JOURNAL.md`
- `PLAN.md`
- `TODO.md`
- `INDEX.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionDetailBodyLayoutTest :platform:core:compileJava :addons:government:compileJava :addons:government:test --tests panetina.elarion.addons.government.client.GovernmentUiScreenLayoutTest`.

Unresolved risks:

- No live screenshot QA was run; the migration is intended to preserve
  Government coordinates exactly.
- Notification, Ledger, Shrine, NPC, Admin Panel, and Portal detail body areas
  still need future migration after their shapes are audited.

Precise recommended next slice:

- Phase 5 Slice 10 - Shared Status/Badge/Chip Audit and Prototype.
- Recommended model: `Medium`; use `High` only if it becomes a broad style-token
  rewrite across many screen families.

## 2026-07-10 - Phase 5 Slice 10 Badge Layout

Objective:

- Add a shared Core badge/tag geometry helper and migrate one low-risk status
  chip/tag family.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionCivicUi.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/client/GovernmentUiGlyphs.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/client/GovernmentUiComponents.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionUiComponentGalleryScreen.java`
- `docs/ui/COMPONENT_REFERENCE.md`
- `docs/reports/SEMANTIC_UI_COMPONENT_AUDIT.md`
- `docs/systems/UI_JOURNAL.md`

Decisions made:

- Extract only badge geometry: width clamping, accent strip, top line, and
  text inset.
- Preserve caller-owned colors, tones, labels, active state, and domain
  semantics.
- Route both Core status chips and Government tags through the same geometry
  because they already share the same civic chip shape.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionBadgeLayout.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ui/ElarionBadgeLayoutTest.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionCivicUi.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/client/GovernmentUiGlyphs.java`
- `docs/ui/COMPONENT_REFERENCE.md`
- `docs/reports/SEMANTIC_UI_COMPONENT_AUDIT.md`
- `docs/systems/UI_JOURNAL.md`
- `PLAN.md`
- `TODO.md`
- `INDEX.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionBadgeLayoutTest :platform:core:compileJava :addons:government:compileJava :addons:government:test --tests panetina.elarion.addons.government.client.GovernmentUiScreenLayoutTest`.

Unresolved risks:

- No live screenshot QA was run; the helper preserves the existing 10px badge
  shape.
- Notification, Ledger, Shrine, NPC, Admin Panel, Portal, and Grave badges
  still need future migration after their shapes are audited.

Precise recommended next slice:

- Phase 5 Slice 11 - Shared Progress Track Layout and Prototype.
- Recommended model: `Medium`; use `High` only if progress semantics or
  server-authored progress models are being redesigned.

## 2026-07-10 - Phase 5 Slice 11 Progress Track Layout

Objective:

- Add a shared Core progress-track geometry helper and migrate one low-risk
  progress shape.

What was inspected:

- `addons/government/src/main/java/panetina/elarion/addons/government/client/GovernmentUiGlyphs.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/client/GovernmentUiComponents.java`
- `docs/ui/COMPONENT_REFERENCE.md`
- `docs/reports/SEMANTIC_UI_COMPONENT_AUDIT.md`
- `docs/systems/UI_JOURNAL.md`

Decisions made:

- Extract only track/top-line/fill rectangles and ratio clamping.
- Keep value/total math, labels, colors, selection state, and vote semantics
  in Government.
- Treat Shrine/Quest/Economy/Admin progress indicators as future migrations
  after their shapes are audited.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionProgressTrackLayout.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ui/ElarionProgressTrackLayoutTest.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/client/GovernmentUiGlyphs.java`
- `docs/ui/COMPONENT_REFERENCE.md`
- `docs/reports/SEMANTIC_UI_COMPONENT_AUDIT.md`
- `docs/systems/UI_JOURNAL.md`
- `PLAN.md`
- `TODO.md`
- `INDEX.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionProgressTrackLayoutTest :platform:core:compileJava :addons:government:compileJava :addons:government:test --tests panetina.elarion.addons.government.client.GovernmentUiScreenLayoutTest`.

Unresolved risks:

- No live screenshot QA was run; Government progress tracks should preserve
  the same position and color behavior.
- Shrine, Quest, Economy, Admin Panel, Notification, and Ledger progress
  indicators still need future migration after their screen-specific semantics
  are audited.

Precise recommended next slice:

- Phase 5 Slice 12 - Shared Empty-State Layout and Prototype.
- Recommended model: `Medium`; use `High` only if this becomes a broad
  screen-state framework or server-authored state model change.

## 2026-07-10 - Phase 5 Slice 12 Empty-State Layout

Objective:

- Add a shared Core empty-state layout helper and migrate one low-risk
  empty-state family.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionNotificationHud.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionNotificationHudLayout.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionCivicUi.java`
- `docs/ui/COMPONENT_REFERENCE.md`
- `docs/reports/SEMANTIC_UI_COMPONENT_AUDIT.md`
- `docs/systems/UI_JOURNAL.md`

Decisions made:

- Extract only panel/title/body rectangles for compact empty states.
- Preserve category-specific empty titles and body text in Notification HUD.
- Leave empty/error/loading conditions and visibility owned by each screen or
  server snapshot.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionEmptyStateLayout.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ui/ElarionEmptyStateLayoutTest.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionNotificationHud.java`
- `docs/ui/COMPONENT_REFERENCE.md`
- `docs/reports/SEMANTIC_UI_COMPONENT_AUDIT.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/systems/GUI.md`
- `PLAN.md`
- `TODO.md`
- `INDEX.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionEmptyStateLayoutTest :platform:core:compileJava`.

Unresolved risks:

- No live screenshot QA was run; the Notification migration is intended to
  preserve current empty-card coordinates.
- Character Menu, Admin Panel, Shrine, Portal, Grave, NPC, and Government
  empty states still need future migration after their shapes are audited.

Precise recommended next slice:

- Phase 5 Slice 13 - Shared Modal/Input Layout Audit and Prototype.
- Recommended model: `Medium`; use `High` only if this expands into Admin
  Panel config mutation semantics, new packet flows, or cross-screen form
  validation architecture.

## 2026-07-10 - Phase 5 Slice 13 Modal Layout

Objective:

- Add a shared Core modal layout helper and migrate one low-risk modal family.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionAdminPanelScreenLayoutTest.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcBankScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/CharacterCreationScreen.java`
- `docs/reports/SEMANTIC_UI_COMPONENT_AUDIT.md`
- `docs/ui/COMPONENT_REFERENCE.md`
- `docs/systems/UI_JOURNAL.md`

Decisions made:

- Extract only centered two-button modal geometry: shell, body, optional input
  row, and footer buttons.
- Keep Admin Panel's package-local action-modal record so existing screen tests
  and rendering stay stable.
- Preserve Admin Panel confirmation semantics, input text handling,
  autocomplete, validation, permissions, packets, and config apply behavior.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionModalLayout.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ui/ElarionModalLayoutTest.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `docs/ui/COMPONENT_REFERENCE.md`
- `docs/reports/SEMANTIC_UI_COMPONENT_AUDIT.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/systems/GUI.md`
- `PLAN.md`
- `TODO.md`
- `INDEX.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionModalLayoutTest --tests panetina.elarion.core.client.ElarionAdminPanelScreenLayoutTest :platform:core:compileJava`.

Unresolved risks:

- Config edit shell still has its own dedicated layout because it has a
  different three-button metadata/proposed/result shape.
- Bank, NPC, character creation, Portal, Grave, and Government modal/input
  areas still need future migration after their repeated shapes are audited.

Precise recommended next slice:

- Phase 5 Slice 14 - Shared Input Field Layout Audit and Prototype.
- Recommended model: `Medium`; use `High` only if this expands into
  cross-screen validation behavior, packet changes, or server-authored form
  schemas.

## 2026-07-10 - Phase 5 Slice 14 Input Field Layout

Objective:

- Add a shared Core single-line input field layout helper and migrate one
  low-risk field family.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcBankScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/CharacterCreationScreen.java`
- `addons/offerings/src/main/java/panetina/elarion/addons/offerings/client/ShrineOfFoundationScreen.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcDialogueScreen.java`
- `docs/reports/SEMANTIC_UI_COMPONENT_AUDIT.md`
- `docs/ui/COMPONENT_REFERENCE.md`

Decisions made:

- Extract only single-line field geometry: bounds, optional icon rect, text X,
  text max width, centered text baseline, and caret max X.
- Migrate NPC Bank amount input because it has the most visible icon/text/caret
  alignment requirement and no validation/packet changes are needed.
- Leave text editing, max digits, quote requests, validation, suggestions,
  permissions, and submits with the owning screens/services.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionInputFieldLayout.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ui/ElarionInputFieldLayoutTest.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcBankScreen.java`
- `docs/ui/COMPONENT_REFERENCE.md`
- `docs/reports/SEMANTIC_UI_COMPONENT_AUDIT.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/systems/GUI.md`
- `PLAN.md`
- `TODO.md`
- `INDEX.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionInputFieldLayoutTest :platform:core:compileJava :addons:npcs:compileJava`.

Unresolved risks:

- Admin Panel filter/action/config inputs, Character name input, Shrine amount
  input, and NPC prompt input still need future migration after their exact
  field shapes are audited.
- Multiline biography/prompt areas are explicitly outside this helper.

Precise recommended next slice:

- Phase 5 Slice 15 - Shared Tooltip/Native Item Hover Boundary Audit.
- Recommended model: `Medium`; use `High` only if this expands into custom
  tooltip packet data, server-authored visibility rules, or cross-screen
  item-preview APIs.

## 2026-07-10 - Phase 5 Slice 15 Item Slot Hover Layout

Objective:

- Add a shared Core item-slot layout helper and migrate one low-risk native
  item tooltip boundary family.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionNotificationHud.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionSemanticRowLayout.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionInputFieldLayout.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcTradeScreen.java`
- `addons/offerings/src/main/java/panetina/elarion/addons/offerings/client/ShrineOfFoundationScreen.java`
- `addons/underworld/src/main/java/panetina/elarion/addons/underworld/client/GraveRecoveryScreen.java`
- `docs/reports/SEMANTIC_UI_COMPONENT_AUDIT.md`
- `docs/ui/COMPONENT_REFERENCE.md`

Decisions made:

- Extract only item-slot geometry: outer slot bounds, inner item draw bounds,
  grid placement, and slot-only hover detection.
- Migrate Notification reward previews because their native item tooltip
  behavior is already server-authored and does not need packet or model
  changes.
- Keep tooltip contents, reward labels, enchantment/lore detail lines, storage
  lookup, visibility, and actions outside the layout helper.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionItemSlotLayout.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ui/ElarionItemSlotLayoutTest.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionNotificationHud.java`
- `docs/ui/COMPONENT_REFERENCE.md`
- `docs/reports/SEMANTIC_UI_COMPONENT_AUDIT.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/systems/GUI.md`
- `PLAN.md`
- `TODO.md`
- `INDEX.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionItemSlotLayoutTest :platform:core:compileJava`.

Unresolved risks:

- NPC Trade, Shrine rewards, Grave Recovery, Portal prompts, and Citizen
  Ledger item previews still have their own item-slot or tooltip geometry and
  should be migrated only after each shape is audited.
- No live screenshot QA was run for Notification reward preview hover because
  this was a geometry-only extraction and the user is conserving QA credits.

Precise recommended next slice:

- Phase 5 Slice 16 - Shared Scroll/Viewport Layout Audit and Prototype.
- Recommended model: `Medium`; use `High` only if this expands into
  scissor-stack architecture, virtual-list APIs, packet paging, or broad
  cross-screen scrolling rewrites.

## 2026-07-10 - Phase 5 Slice 16 Scroll Viewport Layout

Objective:

- Add a shared Core row-viewport layout helper and migrate one low-risk
  scroll-range family.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionUiMetrics.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionVirtualList.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcTradeScreen.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcDialogueScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionCollectionScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionNotificationHud.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/client/CivicForumScreen.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/client/seat/SeatOfRuleScreen.java`
- `addons/underworld/src/main/java/panetina/elarion/addons/underworld/client/GraveRecoveryScreen.java`
- `docs/reports/SEMANTIC_UI_COMPONENT_AUDIT.md`
- `docs/ui/COMPONENT_REFERENCE.md`

Decisions made:

- Extract only row viewport geometry: visible capacity, clamped first row,
  visible count, maximum first row, row Y positions, and row-only hit testing.
- Migrate NPC Trade catalog because it had repeated manual render/click/scroll
  range math and no packet/model changes were required.
- Keep `ElarionVirtualList` as list-state infrastructure; the new helper
  complements it with reusable viewport geometry instead of replacing it.
- Leave row contents, quote/purchase behavior, stock, paging, persistence, and
  server authority with NPC/Economy.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionScrollViewportLayout.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ui/ElarionScrollViewportLayoutTest.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcTradeScreen.java`
- `docs/ui/COMPONENT_REFERENCE.md`
- `docs/reports/SEMANTIC_UI_COMPONENT_AUDIT.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/systems/GUI.md`
- `PLAN.md`
- `TODO.md`
- `INDEX.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionScrollViewportLayoutTest :platform:core:compileJava :addons:npcs:compileJava`.

Unresolved risks:

- Notification details/list, Grave Recovery grids, Shrine panels, Admin Panel
  lists, Character Menu tabs, and Government archives still have local
  scroll/viewport calculations. Migrate only after each screen shape is
  audited.
- No live screenshot QA was run; this was a geometry-only extraction and the
  user is conserving QA credits.

Precise recommended next slice:

- Phase 5 Slice 17 - Shared Multiline Text/Input Viewport Audit and Prototype.
- Recommended model: `Medium`; use `High` only if this expands into reusable
  editor widgets, server-authored form schemas, packet changes, or broad
  character/Admin/Shrine input rewrites.

## 2026-07-10 - Phase 5 Slice 17 Text Viewport Layout

Objective:

- Add a shared Core multiline text viewport layout helper and migrate one
  low-risk text viewport family.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/client/CharacterCreationScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionTextInput.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionUiTypography.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionNotificationHud.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionCollectionScreen.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcDialogueScreen.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcTradeScreen.java`
- `addons/offerings/src/main/java/panetina/elarion/addons/offerings/client/ShrineOfFoundationScreen.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/client/CivicForumScreen.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/client/seat/SeatOfRuleScreen.java`
- `addons/underworld/src/main/java/panetina/elarion/addons/underworld/client/GraveRecoveryScreen.java`
- `docs/reports/SEMANTIC_UI_COMPONENT_AUDIT.md`
- `docs/ui/COMPONENT_REFERENCE.md`

Decisions made:

- Extract only visible-line viewport geometry: visible capacity, clamped first
  line, visible count, maximum first line, line Y positions,
  absolute/visible-line mapping, and scroll hint state.
- Migrate Character Creation biography because it had repeated manual
  visible-line math in scroll, render, caret, and scroll-hint logic.
- Keep `ElarionTextInput` as the text ownership/editing model; the new helper
  does not own typing, focus, validation, max length, caret blinking, or
  submission.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionTextViewportLayout.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ui/ElarionTextViewportLayoutTest.java`
- `platform/core/src/main/java/panetina/elarion/core/client/CharacterCreationScreen.java`
- `docs/ui/COMPONENT_REFERENCE.md`
- `docs/reports/SEMANTIC_UI_COMPONENT_AUDIT.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/systems/GUI.md`
- `PLAN.md`
- `TODO.md`
- `INDEX.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionTextViewportLayoutTest :platform:core:compileJava`.

Unresolved risks:

- Admin Panel body/config text, Notification detail body, Shrine detail/history
  text, NPC prompt text, Portal status text, and Grave status text still have
  local wrapped text viewport math and should be migrated only after each
  shape is audited.
- No live screenshot QA was run; this was a geometry-only extraction and the
  user is conserving QA credits.

Precise recommended next slice:

- Phase 5 Slice 18 - Shared Tooltip Shell Audit and Prototype.
- Recommended model: `Medium`; use `High` only if this expands into
  server-authored tooltip schemas, item-component inspection, packet changes,
  or cross-screen visibility rules.

## 2026-07-10 - Phase 5 Slice 18 Tooltip Shell Layout

Objective:

- Add a shared Core custom tooltip shell layout helper and migrate one
  low-risk tooltip shell family.

What was inspected:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcDialogueScreen.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcTradeScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionNotificationHud.java`
- `addons/offerings/src/main/java/panetina/elarion/addons/offerings/client/ShrineOfFoundationScreen.java`
- `addons/portals/src/main/java/panetina/elarion/addons/portals/client/PortalStatusHud.java`
- `addons/underworld/src/main/java/panetina/elarion/addons/underworld/client/GraveRecoveryScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionUiTypography.java`
- `docs/reports/SEMANTIC_UI_COMPONENT_AUDIT.md`
- `docs/ui/COMPONENT_REFERENCE.md`

Decisions made:

- Extract only custom tooltip shell geometry: screen-edge-aware placement,
  shell bounds, and padded content bounds.
- Migrate NPC relationship hover hints because they used a custom string
  tooltip and do not require native item component rendering.
- Keep native item tooltips on `drawItemTooltip` for item stacks, enchantments,
  and lore.
- Leave tooltip text, visibility rules, hover trigger, and server-authored data
  with the owning screen/service.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionTooltipShellLayout.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ui/ElarionTooltipShellLayoutTest.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcDialogueScreen.java`
- `docs/ui/COMPONENT_REFERENCE.md`
- `docs/reports/SEMANTIC_UI_COMPONENT_AUDIT.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/systems/GUI.md`
- `PLAN.md`
- `TODO.md`
- `INDEX.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionTooltipShellLayoutTest :platform:core:compileJava :addons:npcs:compileJava`.

Unresolved risks:

- Admin Panel hints, Character Menu badge hints, Shrine/Portal status hints,
  and non-item Notification fallbacks still use native/simple tooltip paths or
  local body text. Migrate only after each tooltip shape is audited.
- No live screenshot QA was run; this was a geometry-only extraction and the
  user is conserving QA credits.

Precise recommended next slice:

- Phase 5 Slice 19 - Shared Icon/Label Line Layout Audit and Prototype.
- Recommended model: `Medium`; use `High` only if this expands into semantic
  data formatting, profile aggregation, config schemas, packets, or broad
  screen rewrites.

## 2026-07-10 - Phase 5 Slice 19 Icon/Label Line Layout

Objective:

- Add a shared Core compact icon + label/value line layout helper and migrate
  one low-risk line family.

What was inspected:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcBankScreen.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcTradeScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionCollectionScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `addons/offerings/src/main/java/panetina/elarion/addons/offerings/client/ShrineOfFoundationScreen.java`
- `addons/portals/src/main/java/panetina/elarion/addons/portals/client/PortalConfirmationScreen.java`
- `addons/underworld/src/main/java/panetina/elarion/addons/underworld/client/GraveRecoveryScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionMoneySummary.java`
- `docs/reports/SEMANTIC_UI_COMPONENT_AUDIT.md`
- `docs/ui/COMPONENT_REFERENCE.md`

Decisions made:

- Extract only compact label + icon + value line geometry.
- Migrate NPC Bank Fee/Total because the line had local label-width plus icon
  offset arithmetic and no server behavior changes were needed.
- Leave quote values, fee/tax policy, currency semantics, formatting,
  validation, packets, and persistence outside the helper.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionIconLabelLineLayout.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ui/ElarionIconLabelLineLayoutTest.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcBankScreen.java`
- `docs/ui/COMPONENT_REFERENCE.md`
- `docs/reports/SEMANTIC_UI_COMPONENT_AUDIT.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/systems/GUI.md`
- `PLAN.md`
- `TODO.md`
- `INDEX.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionIconLabelLineLayoutTest :platform:core:compileJava :addons:npcs:compileJava`.

Unresolved risks:

- Character Menu profile facts, Admin Panel detail facts, Shrine requirements,
  Portal prompt cost facts, Grave status facts, and service summaries still
  have local icon/text/value geometry. Migrate only after each shape is
  audited.
- No live screenshot QA was run; this was a geometry-only extraction and the
  user is conserving QA credits.

Precise recommended next slice:

- Phase 5 Slice 20 - Shared Status/Feedback Line Layout Audit and Prototype.
- Recommended model: `Medium`; use `High` only if this expands into
  server-authored feedback schemas, notification events, packet changes, or
  broad status presentation rewrites.

## 2026-07-10 - Phase 5 Slice 20 Status Line Layout

Objective:

- Add a shared Core one-line status/feedback layout helper and migrate one
  low-risk status message family.

What was inspected:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcBankScreen.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcTradeScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionIconLabelLineLayout.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ui/ElarionIconLabelLineLayoutTest.java`
- `docs/reports/SEMANTIC_UI_COMPONENT_AUDIT.md`
- `docs/ui/COMPONENT_REFERENCE.md`
- `docs/systems/GUI.md`
- `docs/systems/UI_JOURNAL.md`

Decisions made:

- Extract only bounded one-line status geometry: bounds, text position, and
  text maximum width.
- Migrate NPC Bank invalid quote messages and dialogue feedback because both
  were local fixed-coordinate one-line status messages.
- Leave message text, severity, quote validity, tax policy, validation,
  notifications, packets, persistence, and server authority outside the helper.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionStatusLineLayout.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ui/ElarionStatusLineLayoutTest.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcBankScreen.java`
- `docs/ui/COMPONENT_REFERENCE.md`
- `docs/reports/SEMANTIC_UI_COMPONENT_AUDIT.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/systems/GUI.md`
- `PLAN.md`
- `TODO.md`
- `INDEX.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionStatusLineLayoutTest :platform:core:compileJava :addons:npcs:compileJava`.

Unresolved risks:

- Admin Panel validation text, Shrine contribution outcomes, Portal
  blocked/allowed hints, Grave status messages, and Character Creation
  validation messages still use local status/body geometry. Migrate only after
  each shape is audited.
- No live screenshot QA was run; this was a geometry-only extraction and the
  user is conserving QA credits.

Precise recommended next slice:

- Phase 5 Slice 21 - Shared Section Header/Subheader Layout Audit and
  Prototype.
- Recommended model: `Medium`; use `High` only if this expands into broad
  screen-chrome rewrites, title typography changes across many screens, or
  live screenshot QA.

## 2026-07-10 - Phase 5 Slice 21 Section Header Layout

Objective:

- Add a shared Core centered section-header layout helper and migrate one
  low-risk repeated header family.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/client/CharacterCreationScreen.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcBankScreen.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcTradeScreen.java`
- `addons/offerings/src/main/java/panetina/elarion/addons/offerings/client/ShrineOfFoundationScreen.java`
- `addons/underworld/src/main/java/panetina/elarion/addons/underworld/client/GraveRecoveryScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionCollectionScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionDetailCardLayout.java`
- `docs/reports/SEMANTIC_UI_COMPONENT_AUDIT.md`
- `docs/ui/COMPONENT_REFERENCE.md`
- `docs/systems/GUI.md`
- `docs/systems/UI_JOURNAL.md`

Decisions made:

- Extract only compact centered icon/title/divider section-header geometry.
- Migrate Character Creation `IDENTITY` and `BIOGRAPHY` panel headers because
  they share the exact same repeated icon/title/divider shape.
- Do not force left-aligned `headerShell` panel titles through this helper;
  they are a distinct shape for the next slice.
- Leave labels, icon ids, onboarding state, input validation, Realm placement
  flow, packets, persistence, and server authority outside the helper.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionSectionHeaderLayout.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ui/ElarionSectionHeaderLayoutTest.java`
- `platform/core/src/main/java/panetina/elarion/core/client/CharacterCreationScreen.java`
- `docs/ui/COMPONENT_REFERENCE.md`
- `docs/reports/SEMANTIC_UI_COMPONENT_AUDIT.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/systems/GUI.md`
- `PLAN.md`
- `TODO.md`
- `INDEX.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionSectionHeaderLayoutTest :platform:core:compileJava`.

Unresolved risks:

- NPC Bank/Trade, Admin Panel, Character Menu, Shrine, Portal, and Grave
  screens still have local left-aligned `headerShell` title/divider geometry.
  Audit that as a separate helper shape instead of broad title rewrites.
- No live screenshot QA was run; this was a geometry-only extraction.

Precise recommended next slice:

- Phase 5 Slice 22 - Shared Left Panel Header Layout Audit and Prototype.
- Recommended model: `Medium`; use `High` only if this expands into broad
  screen-chrome rewrites, title typography changes across many screens, or
  live screenshot QA.

## 2026-07-10 - Phase 5 Slice 22 Panel Header Layout

Objective:

- Add a shared Core left-aligned panel-header layout helper and migrate one
  low-risk repeated `headerShell` title/divider family.

What was inspected:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcBankScreen.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcTradeScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionSectionHeaderLayout.java`
- `docs/reports/SEMANTIC_UI_COMPONENT_AUDIT.md`
- `docs/ui/COMPONENT_REFERENCE.md`
- `docs/systems/GUI.md`
- `docs/systems/UI_JOURNAL.md`

Decisions made:

- Extract only left-aligned `headerShell` panel geometry: panel bounds, header
  height, title origin/max width, divider rectangle, and body start Y.
- Migrate NPC Bank amount-panel and NPC Trade catalog-panel headers because
  they share the same title/divider/body-start shape.
- Do not force centered section headers or full service-screen portrait/title
  headers through this helper.
- Leave panel labels, quote values, stock, trade offers, validation, packets,
  persistence, and server authority outside the helper.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionPanelHeaderLayout.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ui/ElarionPanelHeaderLayoutTest.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcBankScreen.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcTradeScreen.java`
- `docs/ui/COMPONENT_REFERENCE.md`
- `docs/reports/SEMANTIC_UI_COMPONENT_AUDIT.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/systems/GUI.md`
- `PLAN.md`
- `TODO.md`
- `INDEX.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionPanelHeaderLayoutTest :platform:core:compileJava :addons:npcs:compileJava`.
- Live screenshot QA harness was exercised through
  `build/ui-qa/slice-22-panel-header/trader-dialogue-open-2.png`, which
  confirmed the local server/client path and NPC dialogue opening. The migrated
  panel headers are geometry-only and covered by focused layout tests.

Unresolved risks:

- Admin Panel, Character Menu, Shrine, Portal, Grave, and Notification detail
  surfaces still contain local panel/header geometry. Migrate only after each
  shape is audited.
- Full service-screen headers with portrait/title/subtitle/badge/close
  geometry are a separate shape and should not be folded into
  `ElarionPanelHeaderLayout`.

Precise recommended next slice:

- Phase 5 Slice 23 - Shared Service Screen Header Layout Audit and Prototype.
- Recommended model: `Medium`; use `High` only if this expands into broad
  screen-chrome rewrites, portrait/title/balance rewrites across many screens,
  or live screenshot QA.

## 2026-07-10 - Phase 5 Slice 23 Service Header Layout

Objective:

- Add a shared Core service-screen header layout helper and migrate one
  low-risk repeated portrait/title/subtitle/badge/close family.

What was inspected:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcBankScreen.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcTradeScreen.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcDialogueScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionCollectionScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionPanelHeaderLayout.java`
- `docs/reports/SEMANTIC_UI_COMPONENT_AUDIT.md`
- `docs/ui/COMPONENT_REFERENCE.md`
- `docs/systems/GUI.md`
- `docs/systems/UI_JOURNAL.md`

Decisions made:

- Extract only service-screen header geometry: portrait rectangle, title
  origin/max width, subtitle baseline, currency badge rectangle, and close
  rectangle.
- Migrate NPC Bank and NPC Trade headers because they share the same structure.
- Keep NPC Dialogue out of this first migration because its relationship-heart
  header is related but not identical.
- Leave NPC identity, service labels, balance values, close behavior,
  relationship behavior, packets, persistence, and server authority outside the
  helper.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionServiceHeaderLayout.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ui/ElarionServiceHeaderLayoutTest.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcBankScreen.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcTradeScreen.java`
- `docs/ui/COMPONENT_REFERENCE.md`
- `docs/reports/SEMANTIC_UI_COMPONENT_AUDIT.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/systems/GUI.md`
- `PLAN.md`
- `TODO.md`
- `INDEX.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionServiceHeaderLayoutTest :platform:core:compileJava :addons:npcs:compileJava`.

Unresolved risks:

- NPC Dialogue relationship hearts, Admin Panel title chrome, Ledger profile
  headers, Shrine, Portal, and Grave headers still use local geometry. Migrate
  each only after its shape is audited.
- No live screenshot QA was run in this Medium slice.

Precise recommended next slice:

- Phase 5 Slice 24 - Shared Paired Service Mode Button Layout Audit and
  Prototype.
- Recommended model: `Medium`; use `High` only if this expands into broad
  all-screen tab rewrites, active-state behavior changes, or live screenshot
  QA.

## 2026-07-10 - Phase 5 Slice 24 Paired Button Layout

Objective:

- Add a shared Core paired service button-row layout helper and migrate one
  low-risk repeated two-option mode/action row family.

What was inspected:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcBankScreen.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcTradeScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionActionBandLayout.java`
- `docs/reports/SEMANTIC_UI_COMPONENT_AUDIT.md`
- `docs/ui/COMPONENT_REFERENCE.md`
- `docs/systems/GUI.md`
- `docs/systems/UI_JOURNAL.md`

Decisions made:

- Extract only paired button rectangles: left button, right button, gap, and
  combined bounds.
- Preserve the current NPC Bank uneven button widths and NPC Trade equal button
  widths by passing explicit dimensions into the helper.
- Use the same layout records for both rendering and click hitboxes.
- Leave labels, roles, selected state, enabled state, service switching,
  packets, persistence, and server authority outside the helper.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionPairedButtonLayout.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ui/ElarionPairedButtonLayoutTest.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcBankScreen.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcTradeScreen.java`
- `docs/ui/COMPONENT_REFERENCE.md`
- `docs/reports/SEMANTIC_UI_COMPONENT_AUDIT.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/systems/GUI.md`
- `PLAN.md`
- `TODO.md`
- `INDEX.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionPairedButtonLayoutTest :platform:core:compileJava :addons:npcs:compileJava`.

Unresolved risks:

- Admin Panel, Portal, Grave, Shrine, and Government may have related paired
  action rows, but they were not migrated in this slice.
- No live screenshot QA was run in this Medium slice.

Precise recommended next slice:

- Phase 5 Slice 25 - Shared Service Footer Action Layout Audit and Prototype.
- Recommended model: `Medium`; use `High` only if this expands into broad
  all-screen footer/action rewrites, behavior changes, or live screenshot QA.

## 2026-07-10 - Phase 5 Slice 25 Footer Action Layout

Objective:

- Add a shared Core single footer-action layout helper and migrate one low-risk
  repeated footer/back-action family.

What was inspected:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcBankScreen.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcTradeScreen.java`
- `addons/portals/src/main/java/panetina/elarion/addons/portals/client/PortalConfirmationScreen.java`
- `addons/underworld/src/main/java/panetina/elarion/addons/underworld/client/GraveRecoveryScreen.java`
- `addons/offerings/src/main/java/panetina/elarion/addons/offerings/client/ShrineOfFoundationScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `docs/reports/SEMANTIC_UI_COMPONENT_AUDIT.md`
- `docs/ui/COMPONENT_REFERENCE.md`
- `docs/systems/GUI.md`
- `docs/systems/UI_JOURNAL.md`

Decisions made:

- Extract only single footer action button geometry.
- Migrate NPC Bank and NPC Trade `Back to Conversation` buttons because they
  share the same single-button footer role, even though each screen has its own
  Y/width.
- Use the same layout record for render and click hitboxes.
- Leave labels, role lookup, fallback close behavior, packets, persistence,
  and server authority outside the helper.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionFooterActionLayout.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ui/ElarionFooterActionLayoutTest.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcBankScreen.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcTradeScreen.java`
- `docs/ui/COMPONENT_REFERENCE.md`
- `docs/reports/SEMANTIC_UI_COMPONENT_AUDIT.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/systems/GUI.md`
- `PLAN.md`
- `TODO.md`
- `INDEX.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionFooterActionLayoutTest :platform:core:compileJava :addons:npcs:compileJava`.

Unresolved risks:

- Portal, Grave, Shrine, Admin Panel, Notification, and Government footer
  actions may have related shapes, but they were not migrated in this slice.
- No live screenshot QA was run in this Medium slice.

Precise recommended next slice:

- Phase 5 Slice 26 - Shared Compact Preset Button Row Layout Audit and
  Prototype.
- Recommended model: `Medium`; use `High` only if this expands into broad
  action-band rewrites, behavior changes, or live screenshot QA.

## 2026-07-10 - Phase 5 Slices 26-29 NPC Service Layout Tightening

Objective:

- Continue narrow shared UI extraction through the remaining NPC Bank service
  amount-panel geometry and close hitbox consistency.

What was inspected:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcBankScreen.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcTradeScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionActionBandLayout.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionServiceHeaderLayout.java`
- `docs/reports/SEMANTIC_UI_COMPONENT_AUDIT.md`
- `docs/ui/COMPONENT_REFERENCE.md`
- `docs/systems/GUI.md`
- `docs/systems/UI_JOURNAL.md`

Decisions made:

- Slice 26: add `ElarionPresetButtonRowLayout` for compact equal-width preset
  rows and route NPC Bank amount preset render/click geometry through it.
- Slice 27: extend the preset helper with a preset-plus-confirm row and route
  NPC Bank Confirm action render/click geometry through it.
- Slice 28: add `ElarionSplitSummaryLayout` for divider plus left/right
  summary origins and route NPC Bank Fee/Total placement through it.
- Slice 29: reuse the existing `ElarionServiceHeaderLayout` close rectangle
  for NPC Bank and NPC Trade close hitboxes, removing separate close math.
- Keep preset amounts, labels, quote validation, submit behavior, close
  behavior, packets, persistence, and server authority outside the helpers.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionPresetButtonRowLayout.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ui/ElarionPresetButtonRowLayoutTest.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionSplitSummaryLayout.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ui/ElarionSplitSummaryLayoutTest.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcBankScreen.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcTradeScreen.java`
- `docs/ui/COMPONENT_REFERENCE.md`
- `docs/reports/SEMANTIC_UI_COMPONENT_AUDIT.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/systems/GUI.md`
- `PLAN.md`
- `TODO.md`
- `INDEX.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionPresetButtonRowLayoutTest :platform:core:compileJava :addons:npcs:compileJava`.
- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionSplitSummaryLayoutTest :platform:core:compileJava :addons:npcs:compileJava`.
- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionPresetButtonRowLayoutTest --tests panetina.elarion.core.client.ui.ElarionSplitSummaryLayoutTest --tests panetina.elarion.core.client.ui.ElarionServiceHeaderLayoutTest :platform:core:compileJava :addons:npcs:compileJava`.

Unresolved risks:

- No live screenshot QA was run in these Medium slices.
- The newer service helpers are documented but not yet shown in the dev UI
  gallery.
- The next non-NPC screen family still needs to be selected for narrow
  migration.

Precise recommended next slice:

- Phase 5 Slice 30 - Shared Service Layout Gallery/Reference Pass.
- Recommended model: `Medium`; use `High` only if this expands into live
  screenshot QA or broad all-screen visual rewrites.

## 2026-07-10 - Phase 5 Slice 30 Service Helper Gallery Coverage

Objective:

- Add dev-gallery/reference coverage for the newly extracted NPC service-screen
  helpers before closing Phase 5.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionUiComponentGalleryScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionServiceHeaderLayout.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionPairedButtonLayout.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionFooterActionLayout.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionPresetButtonRowLayout.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionSplitSummaryLayout.java`
- `docs/reports/SEMANTIC_UI_COMPONENT_AUDIT.md`
- `docs/ui/COMPONENT_REFERENCE.md`
- `docs/systems/UI_JOURNAL.md`
- `PLAN.md`
- `TODO.md`

Decisions made:

- Keep the dev gallery reference-only and local-client only.
- Show the newer service helpers in one compact section instead of rewriting a
  gameplay screen during this slice.
- Use a compact hand-drawn service badge in the gallery rather than forcing the
  production fixed-width currency badge into a narrow header slot.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionUiComponentGalleryScreen.java`
- `docs/ui/COMPONENT_REFERENCE.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/reports/SEMANTIC_UI_COMPONENT_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionPresetButtonRowLayoutTest --tests panetina.elarion.core.client.ui.ElarionSplitSummaryLayoutTest --tests panetina.elarion.core.client.ui.ElarionServiceHeaderLayoutTest :platform:core:compileJava`.

Unresolved risks:

- No live screenshot QA was run in this Medium slice.
- Remaining per-screen migrations should move into Phase 6/7 instead of
  continuing open-ended Phase 5 extraction.

Precise recommended next slice:

- Phase 5 Closure Audit and Phase 6 handoff.
- Recommended model: `Light`; use `Medium` for the first Phase 6
  implementation slice and `High` only if live screenshot QA or broad
  Government visual rewrites are needed.

## 2026-07-10 - Phase 5 Closure and Phase 6 Handoff

Objective:

- Close Phase 5 as a shared semantic UI component-foundation phase and select
  the next active phase/slice.

What was inspected:

- `docs/reports/SEMANTIC_UI_COMPONENT_AUDIT.md`
- `docs/systems/UI_JOURNAL.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Decisions made:

- Phase 5 is complete enough to close. It delivered shared, tested layout
  helpers plus dev-gallery/reference coverage.
- Remaining per-screen migrations are Phase 6/7 work, not Phase 5 blockers.
- The next active implementation slice is Phase 6 Slice 1: Government
  Archive/History Row and Detail Action Migration.

Exact files changed:

- `docs/reports/SEMANTIC_UI_COMPONENT_AUDIT.md`
- `docs/systems/UI_JOURNAL.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Documentation-only closure. The preceding code slice passed focused Core UI
  tests and `:platform:core:compileJava`.

Unresolved risks:

- Government archive prose is still not Chronicle-grade; keep the first Phase
  6 slice layout-only unless a separate readability/Chronicle slice is
  approved.
- Remaining Shrine, Portal, Character Menu, Admin Panel, Notification, Grave,
  and NPC Dialogue migrations still need one-family-at-a-time slices.

Precise recommended next slice:

- Phase 6 Slice 1 - Government Archive/History Row and Detail Action
  Migration.
- Recommended model: `Medium`; use `High` only if adding live screenshot QA or
  archive prose/Chronicle changes.

## 2026-07-10 - Phase 6 Slice 1 Government Archive/History Row and Chronicle Projection

Objective:

- Make Government archive/history rows more readable using structured
  public-history metadata while keeping Core as the durable history owner and
  Government as the domain projection owner.

What was inspected:

- `docs/systems/GUI.md`
- `docs/systems/Government.md`
- `docs/systems/Chronicles.md`
- `platform/core/src/main/java/panetina/elarion/core/model/HistoryEvent.java`
- `platform/core/src/main/java/panetina/elarion/core/model/HistoryIndexEntry.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ChronicleEntry.java`
- `platform/core/src/main/java/panetina/elarion/core/model/PublicHistoryEntry.java`
- `platform/core/src/main/java/panetina/elarion/core/service/HistoryService.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/GovernmentBlockInteractions.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/service/GovernmentStateService.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/client/CivicForumScreen.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/client/seat/SeatOfRuleScreen.java`
- focused Core/Government tests around public history, row layout, and block
  interactions.

Decisions made:

- Core public-history projections now carry bounded metadata from
  `HistoryEvent` through live indexes, Chronicle archives, and
  `PublicHistoryEntry`. Old records default to empty metadata maps.
- Government owns the first readable archive projection through
  `GovernmentChronicleText`; it is a reference consumer, not the final Core
  Chronicle variant framework.
- Private `vote-cast` events are hidden from Civic Forum History and Seat of
  Rule Archive lists.
- Government archive rows display a detail label plus one age/time line rather
  than duplicated timestamp columns.
- Compact record rows use centered text-block geometry for better vertical
  alignment.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/model/HistoryIndexEntry.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ChronicleEntry.java`
- `platform/core/src/main/java/panetina/elarion/core/model/PublicHistoryEntry.java`
- `platform/core/src/main/java/panetina/elarion/core/service/HistoryService.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionSemanticRowLayout.java`
- `platform/core/src/test/java/panetina/elarion/core/model/PublicHistoryEntryTest.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ui/ElarionSemanticRowLayoutTest.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/GovernmentChronicleText.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/GovernmentBlockInteractions.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/client/CivicForumScreen.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/client/seat/SeatOfRuleScreen.java`
- `addons/government/src/test/java/panetina/elarion/addons/government/GovernmentBlockInteractionsTest.java`
- `docs/systems/Chronicles.md`
- `docs/systems/Government.md`
- `docs/systems/GUI.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/api.md`
- `INDEX.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.model.PublicHistoryEntryTest --tests panetina.elarion.core.client.ui.ElarionSemanticRowLayoutTest :addons:government:test --tests panetina.elarion.addons.government.GovernmentBlockInteractionsTest --tests panetina.elarion.addons.government.client.GovernmentUiScreenLayoutTest :platform:core:compileJava :addons:government:compileJava`.

Unresolved risks:

- No live screenshot QA was run by request.
- This does not implement selected Chronicle variant-id persistence yet.
- Existing event producers still need a broader metadata/variant audit before
  Chronicle prose is promoted project-wide.

Precise recommended next slice:

- Phase 6 Slice 2 - Chronicle Variant Contract Proposal and Narrow Prototype.
- Recommended model: `Medium` for docs/contracts/tests; use `High` if adding
  persisted variant-id storage, migration behavior, or live screenshot QA.

## 2026-07-10 - Phase 6 Closure Chronicle Contract and Government Notifications

Objective:

- Finish the remaining Phase 6 work that fits a Medium no-QA pass: reusable
  Chronicle renderer/provider contract, Government renderer registration, and
  low-risk Government notification standardization.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/service/HistoryService.java`
- `platform/core/src/main/java/panetina/elarion/core/api/ElarionPublicHistoryApi.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionNotificationService.java`
- `platform/core/src/main/java/panetina/elarion/core/service/RealmDeliveryService.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/ElarionGovernmentAddon.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/GovernmentChronicleText.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/GovernmentBlockInteractions.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/service/GovernmentStateService.java`

Decisions made:

- Core owns renderer registration and fallback Chronicle projection.
- Addons own domain-specific wording through `ChronicleRenderer`.
- Selected variant IDs use `chronicle.variant` metadata when present and a
  deterministic default family otherwise. This avoids a broad migration of
  old history records.
- Government registers `GovernmentChronicleText` through
  `api.publicHistory().registerRenderer(...)`; Government UI asks
  `api.publicHistory().project(...)` for archive row wording.
- Government notifications now use centralized `elarion_government` source,
  semantic Realm/Government icon, and `Open Forum` plus `Dismiss` when
  notification metadata contains `realmId`. The action still revalidates
  server-side Realm membership.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/model/ChronicleProjection.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ChronicleRenderContext.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ChronicleRenderer.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ChronicleRendererRegistry.java`
- `platform/core/src/main/java/panetina/elarion/core/service/HistoryService.java`
- `platform/core/src/main/java/panetina/elarion/core/api/ElarionPublicHistoryApi.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ChronicleRendererRegistryTest.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/ElarionGovernmentAddon.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/GovernmentChronicleText.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/GovernmentBlockInteractions.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/service/GovernmentStateService.java`
- `addons/government/src/test/java/panetina/elarion/addons/government/GovernmentBlockInteractionsTest.java`
- `docs/systems/Chronicles.md`
- `docs/systems/Government.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/addons/government.md`
- `docs/api.md`
- `INDEX.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ChronicleRendererRegistryTest --tests panetina.elarion.core.model.PublicHistoryEntryTest --tests panetina.elarion.core.client.ui.ElarionSemanticRowLayoutTest :addons:government:test --tests panetina.elarion.addons.government.GovernmentBlockInteractionsTest --tests panetina.elarion.addons.government.client.GovernmentUiScreenLayoutTest :platform:core:compileJava :addons:government:compileJava`.

Unresolved risks:

- No live screenshot QA was run by request.
- The contract exists, but most non-Government event families still use
  fallback wording until their owning systems register renderers.
- Persisted non-default variant IDs require event producers to write
  `chronicle.variant`; this slice did not migrate producers.

Precise recommended next slice:

- Phase 7 Slice 1 - Player-Facing UI Migration Inventory Refresh.
- Recommended model: `Light` for docs/audit only, `Medium` if fixing one small
  non-QA issue, `High` only for live screenshot QA or a full screen-family
  migration.

## 2026-07-10 - Phase 7 Slice 2 Shrine Completed-State Projection

Objective:

- Fix the Shrine UI contradiction where a completed project could show
  incomplete `0 / required` progress rows.

What was inspected:

- `docs/systems/GUI.md`
- `docs/addons/offerings.md`
- `docs/reports/UI_FAMILY_INVENTORY.md`
- `addons/offerings/src/main/java/panetina/elarion/addons/offerings/ElarionOfferingsAddon.java`
- `addons/offerings/src/main/java/panetina/elarion/addons/offerings/service/OfferingService.java`
- `addons/offerings/src/main/java/panetina/elarion/addons/offerings/model/OfferingInstance.java`
- `addons/offerings/src/main/java/panetina/elarion/addons/offerings/model/OfferingProgress.java`
- `addons/offerings/src/main/java/panetina/elarion/addons/offerings/network/ShrineUiOpenPayload.java`
- focused Offering tests.

Decisions made:

- Keep Offering runtime progress as the contribution audit trail.
- Normalize only the Shrine UI snapshot when the project is already completed.
- Completed snapshots now send every requirement row as visually complete and
  use required values for missing/partial progress totals.
- Incomplete snapshots still send actual stored progress.
- No storage, donation, economy, reward, milestone, packet ID, or contribution
  behavior changed.

Exact files changed:

- `addons/offerings/src/main/java/panetina/elarion/addons/offerings/ElarionOfferingsAddon.java`
- `addons/offerings/src/test/java/panetina/elarion/addons/offerings/ElarionOfferingsAddonTest.java`
- `docs/addons/offerings.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/reports/UI_FAMILY_INVENTORY.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :addons:offerings:test --tests panetina.elarion.addons.offerings.ElarionOfferingsAddonTest :addons:offerings:compileJava`.

Unresolved risks:

- No live screenshot QA was run.
- Reward grid visibility and complete/incomplete Shrine screenshots still need
  a later manual or High-model pass.

Precise recommended next slice:

- Phase 7 Slice 3 - Portal Confirmation QA / Follow-up.
- Recommended model: `Light` for manual QA checklist only, `Medium` for one
  contained Portal prompt fix plus focused tests, and `High` for live
  screenshot QA.

## 2026-07-10 - Phase 7 Slice 3 Portal Confirmation QA / Follow-up

Objective:

- Verify Portal Confirmation prompt states visually and fix only contained
  prompt presentation issues.

What was inspected:

- `docs/addons/portals.md`
- `docs/systems/GUI.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/reports/UI_FAMILY_INVENTORY.md`
- Portal command, prompt screen, payload, config-default, and focused test
  classes under `addons/portals/src`.
- Live dev server/client Portal prompt states through `/e portal preview`.

Decisions made:

- Add an OP4-only preview command for visual QA rather than requiring real
  route/fee/ticket setup for every prompt state.
- Keep previews presentation-only; confirming still sends the ordinary
  server-validated travel request and does not bypass route authority.
- Wrap long Portal prompt message text inside the prompt card so Nether/End
  ticket explanations do not truncate.
- Keep free/ticket/fee/blocked payment-slot behavior driven by
  `PortalTravelPromptPayload.costKind`.

Exact files changed:

- `addons/portals/src/main/java/panetina/elarion/addons/portals/command/PortalCommands.java`
- `addons/portals/src/main/java/panetina/elarion/addons/portals/client/PortalConfirmationScreen.java`
- `addons/portals/src/test/java/panetina/elarion/addons/portals/command/PortalCommandsPreviewTest.java`
- `docs/addons/portals.md`
- `docs/commands.md`
- `docs/test-commands.md`
- `docs/systems/GUI.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/reports/UI_FAMILY_INVENTORY.md`
- `wiki/admin/commands.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :addons:portals:test --tests panetina.elarion.addons.portals.command.PortalCommandsPreviewTest --tests panetina.elarion.addons.portals.client.PortalConfirmationScreenLayoutTest --tests panetina.elarion.addons.portals.network.PortalTravelPromptPayloadTest --tests panetina.elarion.addons.portals.config.PortalConfigDefaultsTest :addons:portals:compileJava`.
- Live screenshot QA passed for
  `build/ui-qa/portal-phase7/final/prompt-neutral.png`,
  `prompt-nether.png`, `prompt-end.png`, `prompt-fee.png`,
  `prompt-blocked.png`, and `prompt-return.png`.
- QA server/client were stopped after capture.

Unresolved risks:

- Preview prompts do not prove a real configured route will accept travel;
  real-route regression checks are still useful after route/config changes.
- Portal HUD route-slot QA remains separate from the confirmation prompt.

Precise recommended next slice:

- Phase 7 Slice 4 - Grave Recovery QA / Follow-up.
- Recommended model: `Light` for manual QA checklist only, `Medium` for one
  contained Grave UI fix plus focused tests, and `High` only if live screenshot
  QA is needed.

## 2026-07-10 - Phase 7 Slice 4 Grave Recovery Slot Tooltip Polish

Objective:

- Enforce the Grave Recovery slot-only tooltip contract with a contained
  client-layout fix and focused tests.

What was inspected:

- `RULES.md`
- `CODEX.md`
- `docs/systems/Underworld.md`
- `docs/systems/GUI.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/reports/UI_FAMILY_INVENTORY.md`
- `addons/underworld/src/main/java/panetina/elarion/addons/underworld/client/GraveRecoveryScreen.java`
- `addons/underworld/src/main/java/panetina/elarion/addons/underworld/network/GraveOpenPayload.java`
- `addons/underworld/src/main/java/panetina/elarion/addons/underworld/network/GraveRecoverPayload.java`
- `addons/underworld/src/test/java/panetina/elarion/addons/underworld/client/GraveRecoveryScreenLayoutTest.java`
- Core `ElarionItemSlotLayout` and its tests.

Decisions made:

- Use the existing Core `ElarionItemSlotLayout` helper instead of adding
  screen-local slot math.
- Keep the 26x26 civic slot frame, but draw the native item and trigger native
  item tooltip from the shared 16x16 item rectangle.
- Do not change Underworld recovery packets, server validation, corpse storage,
  vault behavior, inventory mutation, ownership/access checks, or live QA
  commands.

Exact files changed:

- `addons/underworld/src/main/java/panetina/elarion/addons/underworld/client/GraveRecoveryScreen.java`
- `addons/underworld/src/test/java/panetina/elarion/addons/underworld/client/GraveRecoveryScreenLayoutTest.java`
- `docs/systems/Underworld.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/reports/UI_FAMILY_INVENTORY.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :addons:underworld:test --tests panetina.elarion.addons.underworld.client.GraveRecoveryScreenLayoutTest --tests panetina.elarion.addons.underworld.network.GraveOpenPayloadTest :addons:underworld:compileJava`.

Unresolved risks:

- No live screenshot QA was run in this Medium slice.
- Grave Recovery still needs visual acceptance for empty, populated, scroll,
  full-inventory, disabled/enabled recover, slot tooltip, and close states.

Precise recommended next slice:

- Phase 7 Slice 5 - Character Menu backend-summary proposal.
- Recommended model: `Light` for docs/proposal only, `Medium` if adding one
  bounded owner-summary contract or focused test, and `High` only if live UI QA
  is required.

## 2026-07-10 - Character Menu Mount Preview QA / Polish

Objective:

- Re-run live Mounts-tab screenshot QA and correct selected-mount preview
  centering/scale for all seven V1 mounts.

What was inspected:

- `addons/mounts/src/main/java/panetina/elarion/addons/mounts/client/ElarionMountCollectionPreviewRenderer.java`
- `addons/mounts/src/test/java/panetina/elarion/addons/mounts/client/ElarionMountCollectionPreviewRendererTest.java`
- `addons/mounts/src/main/resources/assets/elarion_mounts/geo/mount_wyvern.geo.json`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionMenuEntityPreviewRenderer.java`
- Live screenshots under `build/ui-qa/mount-preview-20260710-redo/`.

Decisions made:

- Keep bounds-aware preview sizing for normal mounts.
- Treat Chinese Dragon, Sci-Fi Bike, and Wyvern as explicitly calibrated
  converted long models because raw geometry bounds do not match the live
  visible camera footprint.
- Normalize Wyvern geo `format_version` to `1.12.0` so GeckoLib no longer
  enters the unsupported 1.21 geometry warning path.
- Use live screenshots as the final acceptance signal for the converted model
  calibration, with focused tests protecting the helper math.

Exact files changed:

- `addons/mounts/src/main/java/panetina/elarion/addons/mounts/client/ElarionMountCollectionPreviewRenderer.java`
- `addons/mounts/src/test/java/panetina/elarion/addons/mounts/client/ElarionMountCollectionPreviewRendererTest.java`
- `addons/mounts/src/main/resources/assets/elarion_mounts/geo/mount_wyvern.geo.json`
- `docs/addons/mounts.md`
- `docs/systems/UI_JOURNAL.md`
- `PLAN.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :addons:mounts:test --tests panetina.elarion.addons.mounts.client.ElarionMountCollectionPreviewRendererTest`.
- Live screenshot QA used the real local multiplayer path and captured final
  mount previews under `build/ui-qa/mount-preview-20260710-redo/`.
- Final reviewed screenshots: `mount-airship-corrected.png`,
  `mount-bee-corrected.png`, `mount-ghast-corrected.png`,
  `mount-hot-air-balloon-corrected.png`, `mount-scifi-bike-corrected.png`,
  `mount-wyvern-final.png`, and `mount-chinese-dragon-final3.png`.

Unresolved risks:

- Future mount geo replacements can invalidate visual calibration; rerun the
  same live screenshot pass whenever a mount model, scale, anchor, or animation
  asset changes.

Precise recommended next slice:

- Phase 8 Slice 2 - Core Chronicle Template Library Skeleton.
- Recommended model: `Medium`.

## 2026-07-11 - Phase 8 Slice 2 Core Chronicle Template Library Skeleton

Objective:

- Add reusable Core Chronicle template-family and stable variant-selection
  contracts without migrating event producers or persistence.

What was inspected:

- `docs/reports/CHRONICLE_VARIANT_FRAMEWORK_PROPOSAL.md`
- `docs/systems/Chronicles.md`
- `platform/core/src/main/java/panetina/elarion/core/service/ChronicleRendererRegistry.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ChronicleProjection.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ChronicleRenderer.java`
- `platform/core/src/main/java/panetina/elarion/core/model/PublicHistoryEntry.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ChronicleRendererRegistryTest.java`

Decisions made:

- Core owns the generic template skeleton and stable selector.
- Persisted `chronicle.variant` metadata remains authoritative.
- Without persisted metadata, variant selection is deterministic from event id
  plus family id.
- A family is library-ready only when it has at least 10 authored templates.
- Missing required metadata uses a family-owned fallback body.
- This slice does not stamp variant ids into history records and does not
  migrate Government or addon renderer families.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/model/ChronicleTemplate.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ChronicleTemplateFamily.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ChronicleTemplateLibrary.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ChronicleVariantSelector.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ChronicleTemplateRenderer.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ChronicleTemplateLibraryTest.java`
- `docs/systems/Chronicles.md`
- `INDEX.md`
- `TODO.md`
- `PLAN.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ChronicleTemplateLibraryTest --tests panetina.elarion.core.service.ChronicleRendererRegistryTest --console=plain`.

Unresolved risks:

- Existing Chronicle text still mostly uses registered renderers or fallback
  text until families are migrated.
- Variant ids are selected deterministically but are not yet persisted for new
  events.
- No in-game library UI or live QA was part of this slice.

Precise recommended next slice:

- Phase 8 Slice 3 - Government Chronicle template pilot.
- Recommended model: `Medium`.
- Start with one narrow Government family, preferably proposal/law outcomes,
  and author at least 10 variants before marking it library-ready.

## 2026-07-11 - Phase 8 Slice 3 Government Chronicle Template Pilot

Objective:

- Migrate one narrow Government Chronicle family onto the Core template
  library with 10 authored variants and focused tests.

What was inspected:

- `addons/government/src/main/java/panetina/elarion/addons/government/GovernmentChronicleText.java`
- `addons/government/src/test/java/panetina/elarion/addons/government/GovernmentBlockInteractionsTest.java`
- `docs/systems/Government.md`
- `docs/addons/government.md`
- `docs/systems/Chronicles.md`

Decisions made:

- Start with `proposal-approved` because it is a common archive/proposal
  outcome and already carries structured `title`/`category` metadata.
- Keep the rest of `GovernmentChronicleText` switch-rendered until future
  slices migrate each family deliberately.
- Require `title` metadata for authored proposal-approved prose; if absent,
  use the old safe fallback sentence.
- Keep `category` optional so older proposal-approved records still render.
- Do not stamp selected variants into persisted history in this slice.

Exact files changed:

- `addons/government/src/main/java/panetina/elarion/addons/government/GovernmentChronicleText.java`
- `addons/government/src/test/java/panetina/elarion/addons/government/GovernmentBlockInteractionsTest.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ChronicleTemplate.java`
- `docs/systems/Government.md`
- `docs/addons/government.md`
- `docs/systems/Chronicles.md`
- `TODO.md`
- `PLAN.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :addons:government:test --tests panetina.elarion.addons.government.GovernmentBlockInteractionsTest :platform:core:test --tests panetina.elarion.core.service.ChronicleTemplateLibraryTest --tests panetina.elarion.core.service.ChronicleRendererRegistryTest --console=plain`.

Unresolved risks:

- Most Government Chronicle families still use the older switch path.
- Variant ids are deterministic but not persisted for newly emitted events.
- No live screenshot QA was run because this was backend text projection only.

Precise recommended next slice:

- Phase 8 Slice 4 - Government Chronicle family expansion.
- Recommended model: `Medium`.
- Migrate `proposal-rejected` and/or `civic-record-created` with 10 variants
  each and focused tests.

## 2026-07-11 - Phase 8 Closure Chronicle Variant Framework

Objective:

- Finish the Phase 8 Chronicle variant framework within the current safe scope:
  shared Core template families, deterministic selection, several
  library-ready event families, and a bounded future in-game library query
  helper.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/api/ElarionPublicHistoryApi.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`
- `platform/core/src/main/java/panetina/elarion/core/service/CoreChronicleText.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/GovernmentChronicleText.java`
- `addons/portals/src/main/java/panetina/elarion/addons/portals/ElarionPortalsAddon.java`
- `addons/portals/src/main/java/panetina/elarion/addons/portals/PortalChronicleText.java`
- `addons/offerings/src/main/java/panetina/elarion/addons/offerings/ElarionOfferingsAddon.java`
- `addons/offerings/src/main/java/panetina/elarion/addons/offerings/OfferingChronicleText.java`
- Focused Core/Government/Portals/Offerings Chronicle tests.
- Chronicle, addon, API, index, plan, TODO, and handoff docs.

Decisions made:

- Existing `chronicle.variant` metadata remains authoritative.
- Records without persisted variant metadata choose a stable variant from event
  id plus family id. This gives stable prose without rewriting old history.
- Do not add first-render write-back or broad event-emitter stamping in this
  phase; that needs a separate persistence/migration proposal.
- `chronicleLibrary(realmId, limit)` is only a bounded public-history query
  helper for future in-game library views. It does not expose raw history files
  or create a new UI.
- Promoted families are considered library-ready only when they have at least
  10 authored variants and focused tests.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/model/ChronicleTemplate.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ChronicleTemplateFamily.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ChronicleTemplateLibrary.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ChronicleVariantSelector.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ChronicleTemplateRenderer.java`
- `platform/core/src/main/java/panetina/elarion/core/service/CoreChronicleText.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`
- `platform/core/src/main/java/panetina/elarion/core/api/ElarionPublicHistoryApi.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ChronicleTemplateLibraryTest.java`
- `platform/core/src/test/java/panetina/elarion/core/service/CoreChronicleTextTest.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/GovernmentChronicleText.java`
- `addons/government/src/test/java/panetina/elarion/addons/government/GovernmentBlockInteractionsTest.java`
- `addons/portals/src/main/java/panetina/elarion/addons/portals/ElarionPortalsAddon.java`
- `addons/portals/src/main/java/panetina/elarion/addons/portals/PortalChronicleText.java`
- `addons/portals/src/test/java/panetina/elarion/addons/portals/PortalChronicleTextTest.java`
- `addons/offerings/src/main/java/panetina/elarion/addons/offerings/ElarionOfferingsAddon.java`
- `addons/offerings/src/main/java/panetina/elarion/addons/offerings/OfferingChronicleText.java`
- `addons/offerings/src/test/java/panetina/elarion/addons/offerings/OfferingChronicleTextTest.java`
- `docs/systems/Chronicles.md`
- `docs/systems/Government.md`
- `docs/addons/government.md`
- `docs/addons/portals.md`
- `docs/addons/offerings.md`
- `docs/addons/core.md`
- `docs/api.md`
- `INDEX.md`
- `TODO.md`
- `PLAN.md`
- `docs/ai/CURRENT_STATUS.md`

Library-ready families completed:

- Government: `proposal-approved`, `proposal-rejected`,
  `civic-record-created`.
- Portals: `route-unlocked`.
- Offerings: `project-completed`, `project-force-completed`.
- Core: `title.progression-unlocked`.

Verification:

- Passed focused tests during implementation:
  `.\gradlew.bat :addons:government:test --tests panetina.elarion.addons.government.GovernmentBlockInteractionsTest --console=plain`.
- Passed focused cross-module Chronicle tests:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.CoreChronicleTextTest --tests panetina.elarion.core.service.ChronicleTemplateLibraryTest :addons:government:test --tests panetina.elarion.addons.government.GovernmentBlockInteractionsTest :addons:portals:test --tests panetina.elarion.addons.portals.PortalChronicleTextTest :addons:offerings:test --tests panetina.elarion.addons.offerings.OfferingChronicleTextTest --console=plain`.

Unresolved risks:

- Many event families still use fallback/switch wording until intentionally
  promoted.
- No persisted selected-variant stamping is implemented yet.
- No in-game library UI was built in Phase 8.
- No live screenshot QA was needed because this phase is backend text
  projection/API work.

Precise recommended next slice:

- Phase 9 Slice 1 - NPC narrative readiness audit and bounded proposal.
- Recommended model: `Medium`; `Light` is acceptable only for a shorter
  docs-only audit.
- Inspect NPC definitions, dialogue nodes/options, placement storage,
  validation, action routing, relationship readiness, and quest/economy/trade
  integration points before implementing any rewrite.

## 2026-07-11 - Phase 8 Addendum Death And Global Chronicle Families

Objective:

- Add 10-variant Chronicle families for death records and Realm global-access
  changes, and make the 10-variant requirement a permanent project rule for
  future public-history features.

What was inspected:

- Underworld death classification and domain-event emission in
  `UnderworldService`.
- Offering global-access flag mutation in `OfferingService`.
- Existing Chronicle template family pattern and tests.
- `RULES.md`, Chronicle docs, addon docs, plan, TODO, and index.

Decisions made:

- Underworld owns death prose because it owns death classification and True
  Death state.
- Living-world death capture now records a durable `underworld` Chronicle entry
  after corpse/session state is created. This keeps death visible to future
  library views without reading Underworld storage.
- Self-inflicted player damage is classified as `SUICIDE` when no recent combat
  tag makes another player authoritative.
- Offering owns Realm global-access wording because it owns the current global
  access flag.
- Revolution, war, peace, story quest, seasonal event, and future political
  families are documented as future promoted families; they were not invented
  before their source events exist.

Exact files changed:

- `addons/underworld/src/main/java/panetina/elarion/addons/underworld/UnderworldChronicleText.java`
- `addons/underworld/src/main/java/panetina/elarion/addons/underworld/ElarionUnderworldAddon.java`
- `addons/underworld/src/main/java/panetina/elarion/addons/underworld/model/ElarionDeathType.java`
- `addons/underworld/src/main/java/panetina/elarion/addons/underworld/service/UnderworldService.java`
- `addons/underworld/src/test/java/panetina/elarion/addons/underworld/UnderworldChronicleTextTest.java`
- `addons/offerings/src/main/java/panetina/elarion/addons/offerings/OfferingChronicleText.java`
- `addons/offerings/src/main/java/panetina/elarion/addons/offerings/service/OfferingService.java`
- `addons/offerings/src/test/java/panetina/elarion/addons/offerings/OfferingChronicleTextTest.java`
- `RULES.md`
- `docs/systems/Chronicles.md`
- `docs/addons/underworld.md`
- `docs/addons/offerings.md`
- `INDEX.md`
- `TODO.md`
- `PLAN.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed `.\gradlew.bat :addons:underworld:test :addons:offerings:test :platform:core:test --console=plain`.

Unresolved risks:

- Future war, peace, revolution, story quest, seasonal event, NPC relationship,
  mount, pet, and other event families still need their own source events and
  10-variant family slices when promoted.
- This addendum does not add a Chronicle UI or rewrite existing history.

Precise recommended next slice:

- Phase 9 Slice 1 - NPC narrative readiness audit and bounded proposal.
- Recommended model: `Medium`; `Light` only for a shorter docs-only audit.

## 2026-07-11 - Phase 9 Slice 1 NPC Narrative Readiness Audit

Objective:

- Start Phase 9 by auditing NPC narrative readiness and producing a bounded
  next-slice proposal. No production code, resources, configs, or persistence
  formats were changed.

What was inspected:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/api/ElarionNpcApi.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/ElarionNpcsAddon.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfigLoader.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfigValidator.java`
- NPC dialogue model records: `DialogueDefinition`, `DialogueNode`,
  `DialogueOption`, `DialogueCondition`, `DialogueAction`,
  `DialogueTextVariant`.
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/model/PlacedNpcRecord.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/service/NpcInteractionService.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/storage/NpcPlacementStorage.java`
- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/config/NpcConfigValidatorTest.java`
- Quest actor binding/state references enough to confirm that Quest state stays
  quest-owned and uses NPC public APIs.
- `docs/systems/NPCs.md` and `docs/addons/npcs.md`.

Decisions made:

- NPCs already has a data-driven, server-authoritative foundation. The next
  slice should harden graph validation instead of starting with relationship
  storage or story flags.
- NPC relationship state must remain NPC-owned. Aggregate NPC/Realm reputation
  belongs in the Character Menu, while NPC screens may later show only that
  specific NPC relationship.
- `DialogueAction.historyWorthy` is parsed but not yet a public-history
  contract. It should not emit player-facing history until metadata,
  visibility, and 10-variant Chronicle family rules are designed.
- `DialogueOption.close` is parsed, but current option handling reopens/advances
  nodes. Config-authored close behavior needs a separate narrow behavior slice
  if still desired.

Exact files changed:

- `docs/reports/NPC_NARRATIVE_READINESS_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `INDEX.md`
- `docs/systems/NPCs.md`
- `docs/addons/npcs.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Documentation/audit-only slice. No Java tests were run because no production
  code changed.

Unresolved risks:

- No NPC-owned relationship persistence/service exists yet.
- No durable story flags, one-time choices, endings, or re-entry state exist
  yet.
- Dialogue graph validation lacks reachability, unreachable-node, duplicate
  option/variant id, and terminal/service-exit checks.
- YAML duplicate node-key detection may require parser-level support and should
  not be bundled into the first validator slice unless easy.

Precise recommended next slice:

- Phase 9 Slice 2 - NPC Graph Validation V1.
- Recommended model: `Medium`.
- Add a pure validation helper and focused tests for root reachability,
  unreachable nodes, duplicate option ids, duplicate variant ids, no-exit or
  terminal structure where detectable, and service-node exit safety.
- Keep runtime behavior, packets, persistence, UI geometry, and
  relationship/story-state design unchanged.

## 2026-07-11 - Phase 9 Slice 2 NPC Graph Validation V1

Objective:

- Harden NPC dialogue graph validation without changing runtime dialogue
  behavior, packets, persistence, UI geometry, relationship state, or story
  state.

What was inspected:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfigValidator.java`
- NPC dialogue model records under
  `addons/npcs/src/main/java/panetina/elarion/addons/npcs/model/`.
- Existing validator coverage in
  `addons/npcs/src/test/java/panetina/elarion/addons/npcs/config/NpcConfigValidatorTest.java`.
- Phase 9 audit report and NPC system/addon docs.

Decisions made:

- Keep validation inside `NpcConfigValidator`; do not introduce a second graph
  manager or runtime graph service.
- Treat normal dialogue nodes with no options as valid terminal nodes.
- Treat non-dialogue service presentation nodes (`bank`, `trade`, future
  service surfaces) as invalid if all options loop to the same node and none
  close, because players need a data-authored exit back to conversation or
  close behavior.
- Do not attempt parser-level duplicate YAML node-key detection in this slice;
  current map parsing may already overwrite duplicate node keys before
  validation.

Exact files changed:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfigValidator.java`
- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/config/NpcConfigValidatorTest.java`
- `docs/reports/NPC_NARRATIVE_READINESS_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/systems/NPCs.md`
- `docs/ai/CURRENT_STATUS.md`

Behavior added:

- Validates root reachability and reports nodes unreachable from the root.
- Validates blank node ids, blank option ids, and blank variant ids.
- Validates duplicate option ids within a node.
- Validates duplicate text-variant ids within a node.
- Validates service presentation nodes have an exit option.

Verification:

- Passed `.\gradlew.bat :addons:npcs:test --tests panetina.elarion.addons.npcs.config.NpcConfigValidatorTest --console=plain`.
- Passed `.\gradlew.bat :addons:npcs:test --console=plain`.

Unresolved risks:

- No NPC-owned relationship persistence/service exists yet.
- No durable story flags, one-time choices, endings, or re-entry state exist
  yet.
- Circular branches that prevent completion are not detected yet.
- Parser-level duplicate YAML node-key detection remains future work if the
  parser exposes it cleanly.

Precise recommended next slice:

- Phase 9 Slice 3 - NPC Relationship State Design.
- Recommended model: `Medium`; `Light` only for a short proposal, `High` if
  implementing storage/actions/conditions in the same slice.
- Define NPC-owned per-player/per-NPC relationship records, visibility,
  mutation API, future action/condition names, Character Menu projection
  boundary, and history/Chronicle emission rules. Do not implement broad Realm
  reputation in NPCs.

## 2026-07-11 - Phase 9 Slices 3-4 NPC Relationship State Design And V1

Objective:

- Define and implement the first NPC-owned relationship foundation without
  adding broad Realm/faction reputation, UI rendering, profile summaries,
  notifications, or Chronicle emission.

What was inspected:

- Core registry contracts: `ActionContext`, `ConditionContext`,
  `RegistryExecutionContext`, and `ElarionRegistries`.
- Existing NPC dialogue execution metadata in `NpcInteractionService`.
- Existing NPC storage/service patterns for placement and trade stock.
- Core profile contributor boundaries and `CitizenProfileSummaryFields`.

Decisions made:

- Relationship state is keyed by player UUID plus placed NPC UUID, not display
  name, NPC definition id, Realm, or Core citizen fields.
- NPCs owns storage and mutation. Core only provides registry/profile/history
  infrastructure.
- V1 uses a bounded integer score from `-10000` to `10000`.
- V1 relationship mutations are silent: no history, Chronicle, notification, UI
  relation label, or Character Menu summary yet.
- Registry handlers default to the current dialogue NPC through
  `RegistryExecutionContext.metadata().npcId`.

Exact files changed:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/model/NpcRelationshipRecord.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/storage/NpcRelationshipStorage.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/service/NpcRelationshipService.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/service/NpcRelationshipRegistryHandlers.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/ElarionNpcsAddon.java`
- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/storage/NpcRelationshipStorageTest.java`
- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/service/NpcRelationshipServiceTest.java`
- `docs/reports/NPC_RELATIONSHIP_STATE_DESIGN.md`
- `docs/reports/NPC_NARRATIVE_READINESS_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `INDEX.md`
- `docs/systems/NPCs.md`
- `docs/addons/npcs.md`
- `docs/ai/CURRENT_STATUS.md`

APIs/registry ids added:

- `elarion_npcs:set_relationship`
- `elarion_npcs:add_relationship`
- `elarion_npcs:relationship_at_least`

Persistence:

- Added NPC-owned state file
  `world/elarion/addon-state/npcs/relationships.json`.
- Schema version: `1`.
- Records store player UUID, placed NPC UUID, score, and update timestamp.

Verification:

- Passed `.\gradlew.bat :addons:npcs:test --tests panetina.elarion.addons.npcs.storage.NpcRelationshipStorageTest --tests panetina.elarion.addons.npcs.service.NpcRelationshipServiceTest --console=plain`.
- Passed `.\gradlew.bat :addons:npcs:test --console=plain`.

Unresolved risks:

- No NPC UI relation label/value is wired to relationship scores yet.
- No Character Menu `npcs/reputation` contributor exists yet.
- No relationship milestone history/Chronicle families exist yet.
- No story flags, one-time choices, endings, or re-entry state exist yet.

Precise recommended next slice:

- Phase 9 Slice 5 - Story Flags And One-Time Choices Proposal.
- Recommended model: `Medium`; `Light` only for a short proposal, `High` if
  implementing storage/actions/conditions in the same slice.
- Design durable per-player/per-placed-NPC story flags, option-use state,
  repeatable/one-time choice semantics, ending markers, and re-entry behavior.

## Dev Launch And Client Resource Repair - 2026-07-11

Objective:

- Restore reliable server/client startup after a stale Government class output,
  an invalid dev config title reference, locked Fabric remap output, and invalid
  Minecraft 1.21.1 resource-pack model rotations.

Changes:

- Rebuilt `addons:government` from clean output; the missing
  `GovernmentProposalDecisionPolicy.class` was stale generated build state, not
  a Java source defect.
- Restored `dev/run/config/elarion/core/citizens-defaults.yml` to stable title
  id `citizen`; the player-facing title display remains `Ember`.
- Updated `dev/log4j-elarion-dev.xml` to suppress VanillaBackport's known-empty
  `frog_variant` and `cat_variant` registry diagnostics alongside its existing
  known-empty registry allowlist.
- Corrected all invalid element rotations in the enabled client-one
  `Elarion Excalibured v1.zip` resource pack to Minecraft 1.21.1-supported
  angles. The affected models were three wizard hats, one chef hat, and two
  signal-campfire models.
- The Fabric remap lock was caused by launching a second dev server while the
  first still owned `dev/run/.fabric/processedMods`; generated locks were
  cleared only after stopping the owning dev process.

Verification:

- Passed `:addons:government:clean :addons:government:compileJava`.
- Passed `:addons:government:test :addons:npcs:test :dev:classes`.
- Dedicated server reached `Done (2.655s)` with zero visible `ERROR` lines and
  opened every managed Elarion world.
- Client completed resource reload with zero `ERROR` lines and zero failed
  models. The remaining Iris shader-option/type warnings belong to the selected
  external shader pack.

Plan continuity:

- At the time of this repair, Phase 9 Slice 5 was the next planned slice; the
  completion handoff below supersedes that status.

## 2026-07-11 - Phase 9 Slices 5-6 Story State And Chronicle Completion

Objective:

- Finish Phase 9 with NPC-owned durable narrative state, one-time choices,
  endings/re-entry, and meaningful structured NPC Chronicle outcomes.

Architecture and decisions:

- NPC story state is keyed by player UUID plus placed NPC UUID and stored in
  `world/elarion/addon-state/npcs/story-state.json` schema 1.
- State stores flags, stable used-choice keys, ending id, re-entry node id, and
  update timestamp. It does not duplicate Core citizen or Quest state.
- `one-time: true` options are hidden/rejected server-side after successful use
  and are marked only after all actions succeed.
- Re-entry is explicit through `set_reentry_node`; ordinary dialogue still
  starts at root. Removed/invalid stored nodes fall back to root.
- `close: true` now closes the server-authoritative session after successful
  actions.
- Ordinary choices, relationship changes, bank use, and trades remain silent.
  Only actions explicitly marked `history-worthy: true` with a nonblank
  `history-outcome` emit history.
- NPC story outcomes use category/type `npc/story-outcome`, structured metadata,
  and a persisted deterministic `chronicle.variant` id. `NpcChronicleText`
  provides ten authored variants plus missing-context fallback.
- Core's default Chronicle categories and the active dev config include `npc`.
- No NPC UI geometry changed. Relationship labels and aggregate Character Menu
  reputation remain deferred.

Registry ids added:

- `elarion_npcs:set_story_flag`
- `elarion_npcs:clear_story_flag`
- `elarion_npcs:story_flag_set`
- `elarion_npcs:set_ending`
- `elarion_npcs:ending_is`
- `elarion_npcs:set_reentry_node`

Primary files added:

- `NpcStoryStateRecord`, `NpcStoryStateStorage`, `NpcStoryStateService`
- `NpcStoryRegistryHandlers`, `NpcHistoryService`, `NpcChronicleText`
- Story storage/service/history/Chronicle tests
- `docs/reports/NPC_STORY_STATE_DESIGN.md`

Primary files changed:

- `DialogueOption`, `NpcConfigLoader`, `NpcConfigValidator`
- `NpcInteractionService`, `ElarionNpcsAddon`
- Core history defaults/descriptors and dev `history.yml`
- NPC/config/UI journal/audit documentation, `PLAN.md`, `TODO.md`, and `INDEX.md`

Verification:

- Passed full `:addons:npcs:test` after implementation.
- Passed focused story storage/service, history, Chronicle, and config-validator
  tests.
- Passed affected Core config manager/registry tests.

Deferred work:

- NPC-screen relationship tier/label presentation.
- Character Menu aggregate NPC/Realm reputation contributor.
- Persistent resume of an actively open interrupted conversation.
- Quest-specific parameter validation, localization-key validation, and graph
  visualization.
- Relationship milestone Chronicle families until explicitly promoted with ten
  authored variants.

Phase status and next slice:

- Phase 9 is complete.
- Next: Phase 10 Slice 1, Placeholder Consolidation Audit.
- Recommended model: `Medium`; this should be audit/proposal only before shared
  registry implementation.
