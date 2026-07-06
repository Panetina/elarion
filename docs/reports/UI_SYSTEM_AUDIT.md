# UI System Audit

## Phase 4, Slice 1 - Shared UI Token And Component Proposal

Status: completed proposal.

Objective:

- Inspect the current Core UI primitives, Government civic UI helpers, and
  notification/Admin styling duplication to define the first safe UI-token
  consolidation target.

Production code changes:

- None.

Files inspected:

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

Verified facts:

- Core already owns shared UI primitives through `ElarionUiStyle`,
  `ElarionUiRenderer`, `ElarionUiTypography`, `ElarionUiThemes`,
  `ElarionScaledLayout`, `ElarionScreen`, and `ElarionVirtualList`.
- `ElarionUiTypography` already supports server-synchronized font scaling,
  scaled width metrics, font height, line height, centering, right alignment,
  ellipsis, wrapping, and clipped wrapped drawing.
- Core renderer already provides basic civic-compatible primitives such as
  panels, bordered boxes, beveled boxes, compact buttons, tabs, progress bars,
  scrollbars, dialogue boxes, portrait frames, currency badges, icon frames,
  cards, and wrapper text clipping.
- The strongest current civic styling lives in Government-specific helpers:
  `GovernmentUiGlyphs`, `GovernmentUiComponents`, and
  `GovernmentScreenChrome`.
- Notification HUD has its own duplicated civic color constants, rail/drawer
  drawing, row drawing, close button drawing, and action-button drawing.
- Admin Panel uses Core primitives but still has local danger colors, modal
  overlays, fixed control metrics, and row/action drawing logic.
- The UI journal already defines the desired brown/gold civic direction and
  notification behavior. The gap is implementation consolidation, not missing
  design direction.

Findings:

1. Core style is theme-backed but not semantic enough.
   `ElarionUiStyle` exposes base colors, but repeated civic concepts such as
   root surface, warm card, selected row fill, selected rail, destructive
   action, positive action, gold shadow, and disabled action are still copied
   into Government and Notification classes.

2. Reusable visual components are split by module.
   Government has the most complete row, tab, tag, section, metric, action, and
   attached-shell helpers, but those helpers cannot be reused by Core
   Notification, Admin, NPC, Portal, Shrine, or Collection screens without
   importing Government UI classes.

3. Notification and Government already converged visually but not structurally.
   Both use the same warm surface, gold frame, green selected/positive state,
   red destructive state, row fill, and compact action concepts. The duplicated
   constants should move into Core before more screens are migrated.

4. Layout tokens are implicit.
   Button height, row height, header height, icon size, tab gaps, shell padding,
   and metric columns are currently local constants. Font scaling exists, but
   controls still need shared sizing helpers so hitboxes and clipping grow with
   text consistently.

5. The first implementation slice should not migrate a full screen.
   Moving all Government helpers to Core or rewriting Notification/Admin in one
   pass would be too large and likely to destabilize already tested behavior.

Recommended Phase 4, Slice 2:

- Add Core-owned civic UI token and primitive contracts, then migrate no
  complete screen yet.

Scope classification:

- MEDIUM.

Objective:

- Introduce a reusable Core civic UI layer that captures the existing
  Government/Notification visual language without changing screen behavior.

Expected files:

- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionCivicColors.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionCivicUi.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionUiMetrics.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ui/ElarionUiMetricsTest.java`
- `docs/systems/GUI.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `INDEX.md` only if navigation changes.
- `docs/ai/CURRENT_STATUS.md`

Approved boundaries for the next slice:

- Add Core reusable civic constants and helpers for:
  - warm root/card/header/button surfaces.
  - gold border/highlight/shadow.
  - positive, destructive, selected, disabled, muted, and informational states.
  - thin attached shell/frame.
  - row surface with selected rail.
  - compact action button with primary/destructive/disabled states.
  - section divider.
  - status/tag chip.
  - font-scale-aware row/control height metrics.
- Add focused tests for metric calculations.
- Document how new screens and touched screens should use the Core civic
  helpers.

Explicit exclusions for the next slice:

- Do not migrate Civic Forum or Seat of Rule wholesale.
- Do not rewrite Notification HUD layout.
- Do not redesign Admin Panel.
- Do not add a UI gallery yet.
- Do not change packets, persistence, config formats, or server behavior.

Architecture impact:

- Ownership improves because Core will own shared UI visual tokens and generic
  civic primitives.
- Dependency direction improves because future Core/addon screens can reuse
  Core UI helpers instead of copying Government-specific helper classes.
- No persistence, networking, command, or server-authority changes are needed.

Compatibility risks:

- Low if helpers are additive and screen behavior is not changed.
- The main risk is accidental visual drift if constants differ from current
  Government/Notification values; use the current literals as the initial
  source values.

Verification:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionUiMetricsTest`
- `.\gradlew.bat :platform:core:test`
- `git diff --check`

Deferred work:

- Migrate Notification action band and row drawing to the shared helpers.
- Migrate Admin Panel row/action/modal drawing to the shared helpers.
- Gradually move Government generic helpers into Core while keeping
  Government-specific semantic components in the Government addon.
- Add a dev-only UI gallery after the first component set has at least one
  adopted caller.

## Phase 4, Slice 2 - Core Civic UI Helper Foundation

Status: completed.

Objective:

- Add additive Core helpers for the civic brown/gold visual language without
  changing screen behavior.

Files changed:

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

Implementation notes:

- `ElarionCivicColors` centralizes the current Government/Notification civic
  palette: warm root/card/header/button surfaces, gold frame colors, active
  green, destructive red, info blue, muted borders, and small gloss/shadow
  overlays.
- `ElarionCivicUi` provides additive generic drawing helpers for thin boxes,
  attached shells, row surfaces with selected rails, compact action buttons,
  dividers, and status chips.
- `ElarionUiMetrics` provides font-scale-aware row/control heights and bounded
  list row calculations.
- No existing screen was migrated in this slice.

Verification:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionUiMetricsTest`
- `.\gradlew.bat :platform:core:test`

Recommended Phase 4, Slice 3:

- Migrate one small existing caller to prove the Core helpers.
- Suggested first caller: Notification HUD row/action drawing, because it
  currently duplicates the same civic constants and the user-facing
  notification panel still needs visual polish.
- Keep the slice to generic helper adoption only; do not redesign drawer
  layout, packet behavior, notification storage, or category filtering.

## Phase 4, Slice 3 - Notification HUD Helper Adoption

Status: completed.

Objective:

- Migrate Notification HUD row/action drawing to the new Core civic helpers
  without changing notification packets, storage, filtering, or action
  semantics.

Files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionNotificationHud.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionCivicUi.java`
- `docs/systems/GUI.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Implementation notes:

- Notification rail slots and notification rows now use
  `ElarionCivicUi.rowSurface`.
- Notification thin boxes now delegate to `ElarionCivicUi.thinBox`.
- Notification action buttons now use
  `ElarionCivicUi.compactActionButtonFrame` while preserving the HUD's existing
  icon-plus-label centering and glyph drawing.
- Notification action height now uses `ElarionUiMetrics.controlHeight`.
- Notification local civic color names now alias `ElarionCivicColors` so the
  Core token class is the palette source.
- No packet, persistence, storage, filtering, action semantics, or drawer
  layout was changed.

Verification:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionUiMetricsTest`
- `.\gradlew.bat :platform:core:test`

Recommended Phase 4, Slice 4:

- Continue Notification HUD visual cleanup by migrating shell/header/ornament
  drawing and any remaining duplicated civic literals to Core helpers, then
  perform live screenshot QA of the notification drawer.
- Classification: MEDIUM.
- Exclude notification packet/storage/action changes.

## Phase 4, Slice 4 - Notification HUD Shell Helper Adoption

Status: code completed; live screenshot QA pending.

Objective:

- Continue Notification HUD helper adoption by moving shell/header/ornament
  drawing and remaining reusable civic literals into Core helpers.

Files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionNotificationHud.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionCivicColors.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionCivicUi.java`
- `docs/systems/GUI.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Implementation notes:

- Added Core civic tokens for rail inset/marks, message-body surfaces,
  hover gloss, divider, destructive text/border, and info button states.
- Added Core civic helpers for header shells, rail shells, message bodies,
  header ornaments, close buttons, and reusable dimming.
- Notification HUD now delegates rail shell, drawer shell, header ornaments,
  close button, divider, message body, row surfaces, action button frames, thin
  boxes, and action height to Core civic helpers.
- Notification packet handling, storage, filtering, action semantics, and
  category behavior were not changed.

Verification:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionUiMetricsTest`
- `.\gradlew.bat :platform:core:test`

Not verified:

- Live screenshot QA of the Notification drawer was not run in this slice
  because no active Minecraft client/screenshot control path was available in
  the current turn.

Recommended Phase 4, Slice 5:

- Run live Notification drawer screenshot QA and apply only small visual
  corrections found by that QA.
- Classification: SMALL.
- Verify rail icons, drawer shell, header title/ornaments, centered close X,
  selected row, unread marker, action footer, empty state, and detail state.
- Exclude notification packets, storage, filtering, and action semantics.

## Phase 4, Slice 5 - Notification HUD Layout Contract

Status: completed automated coverage; live screenshot QA still pending.

Objective:

- Attempt the next Notification HUD QA slice. No active Minecraft
  client/screenshot control path was available, so this slice added bounded
  automated layout coverage instead of pretending visual QA was complete.

Files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionNotificationHud.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionNotificationHudLayout.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionNotificationHudLayoutTest.java`
- `docs/systems/GUI.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Implementation notes:

- Added bootstrap-free `ElarionNotificationHudLayout` for Notification HUD
  geometry constants and scaled metrics.
- `ElarionNotificationHud` now aliases its tested layout constants and metric
  helpers from `ElarionNotificationHudLayout`.
- Added `ElarionNotificationHudLayoutTest` covering:
  - close button centered on the drawer header and inside the panel bounds.
  - list band staying inside the drawer panel.
  - row, action-header, and action-button heights growing with font scale.
- The first test attempt directly initialized `ElarionNotificationHud` and
  failed because `ItemStack.EMPTY` requires Minecraft registry bootstrap in a
  plain JVM unit test. The final test avoids that by using the bootstrap-free
  layout helper.

Verification:

- Initial direct-HUD test attempt failed with Minecraft bootstrap initialization
  (`ItemStack.EMPTY` / registry not bootstrapped); this was fixed by extracting
  layout metrics.
- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionNotificationHudLayoutTest`
- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionUiMetricsTest`
- `.\gradlew.bat :platform:core:test`

Not verified:

- Live screenshot QA of the Notification drawer remains pending because there
  was no active Minecraft client/screenshot control path in this turn.

Recommended Phase 4, Slice 6:

- Run actual dev-client Notification drawer screenshot QA and apply only small
  visual corrections found in that live pass.
- Classification: SMALL.
- Verify rail icons, drawer shell, header title/ornaments, centered close X,
  selected row, unread marker, action footer, empty state, and detail state.
- Exclude notification packets, storage, filtering, and action semantics.

## Phase 4, Slice 6 - Notification HUD QA Follow-Up

Status: limited code cleanup completed; live screenshot QA still blocked.

Objective:

- Attempt actual dev-client Notification drawer screenshot QA and make small
  visual corrections only.

Result:

- No attached Minecraft client, app terminal, or screenshot-control path was
  available in this turn, so live visual QA could not be performed.
- The only code correction made was to route the Notification action-footer
  divider through `ElarionCivicUi.divider`, removing one more local divider
  draw from the HUD.

Files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionNotificationHud.java`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionNotificationHudLayoutTest`
- `.\gradlew.bat :platform:core:test`

Not verified:

- Live screenshot QA of the Notification drawer.

Recommended next work:

- If a dev client/screenshot path is available: run Notification drawer live QA
  and make only small visual corrections.
- If live QA remains unavailable: move to the next Phase 4 helper-adoption
  target, likely Admin Panel row/action/modal drawing, while keeping
  Notification screenshot QA as a manual high-priority task.

## Phase 4, Slice 7 - Admin Panel Helper Adoption

Status: completed.

Objective:

- Adopt shared Core civic helpers in Admin Panel row/action/modal surfaces
  without changing Admin config packets, permissions, validation, apply
  behavior, storage, provider actions, or server authority.

Files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `docs/systems/GUI.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Implementation notes:

- Non-danger Admin rows now use `ElarionCivicUi.rowSurface`.
- Danger rows use Core civic destructive colors.
- Detail action buttons now use `ElarionCivicUi.compactActionButton`.
- Confirmation modal and config edit shell overlays now use
  `ElarionCivicColors.MODAL_OVERLAY`.
- Confirmation/config modal buttons now use `ElarionCivicUi.compactActionButton`
  with normal, primary, or destructive tones.
- Admin click targets, config validation/apply predicates, packet sends,
  permissions, storage, and provider action semantics were not changed.

Verification:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionAdminPanelScreenLayoutTest --tests panetina.elarion.core.client.ElarionConfigEditClientStateTest`
- `.\gradlew.bat :platform:core:test`

Not verified:

- Live Admin Panel screenshot QA.
- Live Notification drawer screenshot QA.

Recommended next work:

- If a dev client/screenshot path is available: run Notification drawer and
  Admin Panel live QA, then apply only small visual corrections.
- If live QA remains unavailable: continue Phase 4 helper adoption with Admin
  Panel shell/detail panels or another low-risk Core screen.
- Classification: MEDIUM for another helper-adoption slice.
- Exclude Admin config packet/edit behavior, permissions, storage, and config
  apply semantics.

## Phase 4, Slice 8 - Admin Panel Shell And Detail Helper Adoption

Status: completed.

Objective:

- Continue Admin Panel helper adoption by routing the remaining main shell,
  header, list/detail frame, filter/input, and config edit result surfaces
  through shared Core civic helpers.

Files changed:

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
- Admin click targets, list/detail geometry constants, config validation/apply
  predicates, packet sends, permissions, storage, and provider action
  semantics were not changed.

Verification:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionAdminPanelScreenLayoutTest --tests panetina.elarion.core.client.ElarionConfigEditClientStateTest`
- `.\gradlew.bat :platform:core:test`

Not verified:

- Live Admin Panel screenshot QA.
- Live Notification drawer screenshot QA.

Recommended next work:

- If a dev client/screenshot path is available: run Notification drawer and
  Admin Panel live QA, then apply only small visual corrections.
- If live QA remains unavailable: move to the next low-risk Phase 4 helper
  adoption target, likely Collection shell/list/detail surfaces.
- Classification: MEDIUM for another helper-adoption slice.
- Exclude Collection packet/action/provider behavior, unlock state, title
  activation, mount preview behavior, storage, and server authority.

## Phase 4, Slice 9 - Collection Shell/List/Detail Helper Adoption

Status: completed.

Objective:

- Adopt shared Core civic helpers in Collection shell/list/detail visual
  surfaces without changing Collection packets, actions, provider behavior,
  unlock state, title activation, mount preview behavior, storage, or server
  authority.

Files changed:

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
- Collection-specific active/selected row rendering and icon-frame rendering
  were left intact because they encode existing Collection visual states.
- Click targets, layout constants, packet sends, provider actions, unlock
  state, title activation, mount preview rendering, storage, and server
  authority were not changed.

Verification:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionCollectionScreenLayoutTest --tests panetina.elarion.core.network.CollectionPayloadTest --tests panetina.elarion.core.service.ElarionCollectionServiceTest`
- `.\gradlew.bat :platform:core:test`

Not verified:

- Live Collection screenshot QA.
- Live Admin Panel screenshot QA.
- Live Notification drawer screenshot QA.

Recommended next work:

- If a dev client/screenshot path is available: run Notification drawer,
  Admin Panel, and Collection live QA, then apply only small visual
  corrections.
- If live QA remains unavailable: continue Phase 4 helper adoption with
  Character Creation and Realm Assignment shell/input/choice surfaces.
- Classification: MEDIUM for another helper-adoption slice.
- Exclude character lifecycle behavior, Realm membership behavior, packets,
  persistence, commands, and server authority.

## Phase 4, Slice 10 - Character Creation And Realm Assignment Helper Adoption

Status: completed.

Objective:

- Adopt shared Core civic helpers in Character Creation and Realm Assignment
  shell/input/choice/button visual surfaces without changing character
  lifecycle behavior, Realm membership behavior, packets, persistence,
  commands, or server authority.

Files changed:

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
- Click targets, input focus behavior, submit/close behavior, payload sends,
  lifecycle state, Realm assignment state, persistence, commands, and server
  authority were not changed.

Verification:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.network.CharacterCreationPayloadTest --tests panetina.elarion.core.service.CharacterLifecycleServiceTest`
- `.\gradlew.bat :platform:core:test`

Not verified:

- Live Character Creation screenshot QA.
- Live Realm Assignment screenshot QA.
- Live Collection screenshot QA.
- Live Admin Panel screenshot QA.
- Live Notification drawer screenshot QA.

Recommended next work:

- If a dev client/screenshot path is available: run live QA for Notification,
  Admin Panel, Collection, Character Creation, and Realm Assignment, then apply
  only small visual corrections.
- If live QA remains unavailable: continue Phase 4 helper adoption with a
  small addon screen pair such as Portal Confirmation and Grave Recovery shell
  surfaces.
- Classification: MEDIUM for another helper-adoption slice.
- Exclude portal travel behavior, grave recovery behavior, packets,
  persistence, commands, inventory mutation, and server authority.

## Phase 4, Slice 11 - Portal Confirmation And Grave Recovery Helper Adoption

Status: completed.

Objective:

- Adopt shared Core civic helpers in Portal Confirmation and Grave Recovery
  shell/body/status/slot/action visual surfaces without changing portal travel
  behavior, grave recovery behavior, packets, persistence, commands, inventory
  mutation, or server authority.

Files changed:

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
- Portal and Grave click targets, packet payloads, travel/recovery validation,
  persistence, inventory mutation, commands, and server authority were not
  changed.

Verification:

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

Recommended next work:

- If a dev client/screenshot path is available: run live QA for the migrated
  Core/addon screens and apply only small visual corrections.
- If live QA remains unavailable: continue Phase 4 helper adoption with another
  bounded addon screen target, likely NPC Dialogue or Shrine UI shell/action
  surfaces after a short proposal/audit of their local drawing complexity.
- Classification: MEDIUM for another helper-adoption slice.
- Exclude NPC dialogue condition/action evaluation, Offering donation or
  milestone behavior, packets, persistence, commands, inventory mutation, and
  server authority.

## Phase 4, Slice 12 - NPC Dialogue And Shrine UI Helper-Adoption Proposal

Status: completed proposal.

Objective:

- Inspect NPC Dialogue and Shrine UI local drawing complexity and choose the
  next bounded helper-adoption target without changing production code.

Files inspected:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcDialogueScreen.java`
- `addons/offerings/src/main/java/panetina/elarion/addons/offerings/client/ShrineOfFoundationScreen.java`
- `docs/addons/npcs.md`
- `docs/addons/offerings.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`

Findings:

- NPC Dialogue already uses Core primitives heavily: scaled `ElarionScreen`,
  `ElarionUiTypography`, `ElarionUiRenderer.dialogueBox`, portrait frames,
  cards, compact buttons, virtual lists, scrollbars, and numeric prompts.
- NPC Dialogue still has direct panel/header drawing plus local compact-button
  usage for options, footer Close, and numeric prompt controls.
- NPC Dialogue is behavior-sensitive, but a visual-only helper pass can avoid
  the conversation controller, condition/action evaluation, packet sends,
  prompt submission, portraits, cards, relationship hearts, sounds, and scroll
  state.
- Shrine UI has broader local drawing density: project summary, progress,
  tabs, requirement rows, history rows, numeric prompt, rewards summary,
  reward slots, tooltips, row icons, scrollbars, and contribution messages.
- Shrine UI is a valid future target, but migrating it safely should follow a
  narrower NPC pass because Shrine has more compressed layout and more row
  variants in one screen.

Decision:

- Next implementation slice should target NPC Dialogue shell/options/footer/
  prompt helper adoption.
- Shrine helper adoption is deferred until after NPC or until live QA identifies
  a higher-priority Shrine visual issue.

Recommended Phase 4, Slice 13:

- Adopt shared Core civic helpers in NPC Dialogue shell/header/options/footer
  and numeric prompt surfaces.
- Keep NPC portrait rendering, dialogue boxes, relation hearts, currency badge,
  cards, typing phases, sounds, option selection state, virtual-list scroll
  behavior, prompt validation, packet sends, condition/action evaluation,
  persistence, commands, and server authority unchanged.

Expected files:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcDialogueScreen.java`
- `docs/addons/npcs.md`
- `docs/systems/GUI.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- `.\gradlew.bat :addons:npcs:test --tests panetina.elarion.addons.npcs.client.ui.ElarionConversationControllerTest --tests panetina.elarion.addons.npcs.network.NpcDialoguePromptSubmitPayloadTest`
- `.\gradlew.bat :addons:npcs:test`

Scope classification:

- MEDIUM.

Explicit exclusions:

- Do not change NPC dialogue packet schemas or sends.
- Do not change server-side condition/action evaluation.
- Do not change Economy/NPC prompt semantics.
- Do not change portrait/profile rendering.
- Do not change typing phase logic, sound playback, saved scroll/selection, or
  relationship display semantics.
- Do not touch Shrine UI in the same implementation slice.

## Phase 4, Slice 13 - NPC Dialogue Helper Adoption

Status: completed.

Objective:

- Adopt shared Core civic helpers in NPC Dialogue shell/options/footer/prompt
  visual surfaces without changing dialogue packets, server validation,
  condition/action evaluation, portrait rendering, typing phases, sounds, saved
  scroll state, prompt semantics, persistence, commands, or server authority.

Files changed:

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
- NPC portrait rendering, dialogue boxes, relation hearts, currency badge,
  cards, typing phases, sounds, option selection state, virtual-list scroll
  behavior, prompt validation, packet sends, condition/action evaluation,
  persistence, commands, and server authority were not changed.
- Shrine UI was not changed.

Verification:

- `.\gradlew.bat :addons:npcs:test --tests panetina.elarion.addons.npcs.client.ui.ElarionConversationControllerTest --tests panetina.elarion.addons.npcs.network.NpcDialoguePromptSubmitPayloadTest`
- `.\gradlew.bat :addons:npcs:test`

Not verified:

- Live NPC Dialogue screenshot QA.
- Live migrated-screen screenshot QA for previous Phase 4 screens remains
  pending.

Recommended next work:

- If a dev client/screenshot path is available: run live QA for the migrated
  Core/addon screens and apply only small visual corrections.
- If live QA remains unavailable: continue Phase 4 helper adoption with a
  bounded Shrine UI proposal or first implementation slice focused on
  shell/header/close/numeric prompt surfaces only.
- Classification: MEDIUM for another helper-adoption slice.
- Exclude Offering donation behavior, milestone behavior, reward rendering
  semantics, row icon semantics, packets, persistence, commands, inventory
  mutation, and server authority.

## Phase 4, Slice 14 - Live Screenshot QA Capture Path

Status: completed.

Objective:

- Establish a repeatable Windows dev-client screenshot capture path before
  continuing visual helper adoption.

Findings:

- `.\gradlew.bat runClientOne` launches the full dev client as
  `ElarionAdmin` from the `dev` module.
- The dev client reached the Minecraft main menu with the full Elarion mod set
  loaded.
- Raw `CopyFromScreen` captures can include unrelated overlapping desktop
  windows.
- `PrintWindow` capture by Minecraft window handle produced a clean Minecraft
  PNG even with another window overlapping the same desktop area.

Files changed:

- `dev/tools/capture-minecraft-window.ps1`
- `CODEX.md`
- `docs/systems/GUI.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/test-commands.md`
- `INDEX.md`
- `TODO.md`
- `PLAN.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Launched `.\gradlew.bat runClientOne --console=plain` through a detached
  wrapper.
- Confirmed a `Minecraft* 1.21.1` window existed.
- Captured a clean Minecraft main-menu image through `PrintWindow` to
  `build/ui-qa/script-verified-main-menu.png` using:
  `powershell -NoProfile -ExecutionPolicy Bypass -File .\dev\tools\capture-minecraft-window.ps1 -Output build\ui-qa\script-verified-main-menu.png`.

Limitations:

- This establishes capture, not full UI scenario automation. Opening specific
  Elarion screens still requires manual/dev-world setup or future dev-only UI
  gallery/test hooks.
- Direct `.\dev\tools\capture-minecraft-window.ps1` execution can be blocked
  by local PowerShell policy. Use the documented `powershell -NoProfile
  -ExecutionPolicy Bypass -File ...` form.
- `build/ui-qa/` screenshots are disposable local QA evidence and should not
  be committed unless intentionally promoted to `docs/ui/` as a reference.

Live server QA addendum:

- Real screen-state QA must start `.\gradlew.bat runServer`, then
  `.\gradlew.bat runClientOne`, then connect through Multiplayer using the
  saved `localhost` server entry.
- Direct command-open screens: `/e panel` for Admin Panel and `/collection`
  for Collection.
- Server-context screens: Notification drawer opens from the HUD rail; Shrine,
  Government, NPC Dialogue, Portal Confirmation, and Grave Recovery open from
  their linked block/entity/route/grave interactions.
- Do not test action behavior by constructing client screens directly; those
  flows require server-issued sessions or authoritative snapshots.

## Phase 4, Slice 15 - First Server-Backed Screenshot Pass

Status: completed on 2026-07-06.

Live states captured:

- Realm notification empty state.
- Civic Forum History with structured archive records.
- Create Civic Proposal modal in the Proposals tab.
- Linked Shrine of Foundation screen.
- Allowed Neutral Gate confirmation.

Verified behavior:

- Government screens opened through a real block-backed server session.
- Portal confirmation opened from a configured route field after a temporary
  admin unlock and was not constructed directly on the client.
- The small Civic window used its explicit too-small fallback; maximization
  produced the full layout.
- No custom-payload decode/disconnect error appeared during these flows.
- The dedicated server shut down cleanly and temporary QA state was restored.

Findings:

- Realm notification populated rows, unread markers, action footer, and detail
  view still need seeded-data QA.
- Neutral Gate rendered an empty gate icon frame.
- Shrine completion presentation was contradictory: complete status and body
  text appeared with zero aggregate and requirement progress.

No production rendering, packet, config, command, or persistence code changed
in this pass.
