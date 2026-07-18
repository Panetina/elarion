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
- Direct command-open screens: `/e panel` for Admin Panel and `/charactermenu`
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

## Phase 4, Slice 16 - Populated Notification Drawer Live QA

Status: completed on 2026-07-06.

Live findings and corrections:

- Populated Realm rows, mixed read/unread state, selection, action footer, and
  detail navigation rendered without overlap.
- Claimable reward previews rendered their counts and server-authored
  enchantment tooltip (`Sharpness III`).
- Local `View` and navigation-style `Go To` actions were incorrectly using the
  same green primary tone as Claim; they now use the neutral tone.
- Short details always consumed the maximum drawer height; they now use tested
  content-bounded height while long details remain capped and scrollable.
- Empty World extended the shell to its lower rail pointer, leaving a dead
  band. Empty drawers now remain content-bounded and suppress only pointers
  that cannot intersect the drawer.

Verification:

- `.\gradlew.bat :platform:core:test`
- Live screenshots under `build/ui-qa/`, including
  `notification-reward-tooltip-patched.png` and
  `notification-world-empty-final.png`.
- No Elarion custom-payload encode/decode error occurred. One vanilla
  `clientbound/minecraft:disconnect` encoder warning was caused by the QA
  double-click opening two connection attempts and is not an Elarion payload.
- Seeded notification/reward/history runtime files were restored after QA.

Recommended next slice:

- Phase 4, Slice 17: Collection live screenshot QA and bounded visual
  corrections. Classification: MEDIUM.

## Option A Reference Expansion - 2026-07-07

Status: design/reference slice completed; no production code changed.

The first generated Civic Ledger direction was approved and expanded into the
indexed set at `docs/ui/revamp-option-a/README.md`. The set covers the future
general player hub, character creation, balanced Realm placement,
Shrine/Offerings, quest dialogue, banker accounts, trader entry, dedicated
trade, three Portal ticket pop-ups, Grave Recovery, Admin Panel, and generic
event feedback.

Architecture findings:

- The player hub cannot become a second owner of quest, relationship,
  Offering, Government, Economy, Mount, or Underworld state. It requires a
  conservative Core aggregation boundary and addon contributors.
- Renaming Collection affects commands, keybind labels, payload/class names,
  docs, and compatibility. Audit those surfaces before choosing whether the
  rename is display-only or structural.
- A universal NPC screen would weaken the current ownership model. Quest,
  banker, trader-entry, and trade surfaces should share primitives while
  retaining role-specific presentation and server contracts.
- Portal confirmation should remain interaction-scoped. No route browser or
  client-owned price/schedule decision is introduced.

Recommended next slice:

- Phase 4, Slice 17A: current Collection and future player-hub architecture
  audit, including naming/compatibility impact, provider/action contracts,
  profile data owners, visibility, and bounded query requirements.
- Classification: MEDIUM, documentation-only.

## Phase 4, Slice 17B - Collection Wire-Boundary Hardening

Status: completed on 2026-07-07.

- Collection snapshots now apply matching outbound/inbound limits before
  encoding: 32 tabs, 512 entries per tab, and 16 actions per entry.
- Invalid, oversized, unsafe, and duplicate actionable IDs are omitted instead
  of being truncated into client-visible action targets.
- Selection falls back to the first valid transmitted tab.
- Valid provider snapshots, actions, packet identifiers, persistence, config,
  and screen behavior remain unchanged.
- Focused Collection payload/service tests and full Core tests passed.

Recommended next slice:

- Phase 4, Slice 17C: live Collection regression baseline against the current
  implementation before Character Menu shell work.
- Capture Mounts/Pets/Titles, selected/active/locked rows, model preview,
  scrolling, title activation, and server-authoritative action refresh.
- Classification: MEDIUM.

No production rendering, packet, config, command, or persistence code changed
in this pass.

## Phase 4, Slice 17C - Collection Live Regression Baseline

Status: completed on 2026-07-07; live QA and documentation only.

- Started the dev server/client, joined the saved `localhost` multiplayer
  entry as `ElarionAdmin`, and opened `/charactermenu`.
- Captured live screenshots in `build/ui-qa/slice-17c/` for Mounts selected,
  Mounts active refresh, hidden mount scrolling, empty Pets, and Titles detail.
- Verified that the current Collection unlockables flow renders and refreshes
  without an Elarion custom-payload encode/decode crash.
- Verified the Airship model preview path in the live menu.
- The baseline confirms behavior stability, not final visual quality. The
  future Character Menu shell still needs a dedicated redesign pass against
  `docs/ui/revamp-option-a/01-citizen-ledger-profile.png` and
  `docs/ui/revamp-option-a/02-citizen-ledger-unlockables.png`.
- Runtime QA side effect: the test player's active mount was set to `airship`;
  no pre-click state backup existed.
- Live QA note: a white/tiny framebuffer on startup was resolved by toggling
  Minecraft with `F11`; the issue was not shader-related in this run, and Iris
  state was restored after the temporary check.

Recommended next slice:

- Phase 4, Slice 17D: introduce the `Character Menu` user-facing shell label
  and `/charactermenu` alias while preserving Collection internals and `/charactermenu`
  compatibility.
- Classification: SMALL.

## Phase 4, Slice 17D - Character Menu Label And Command Alias

Status: completed on 2026-07-07.

- The current Collection unlockables shell now presents itself as `Citizen
  Ledger` in the server snapshot title and client screen title.
- `/charactermenu` opens the same server-authoritative Collection snapshot.
- `/charactermenu` remains available as a compatibility alias.
- The keybind label now reads `Open Character Menu`.
- No Collection packet identifiers, provider/action contracts, config paths,
  runtime files, storage, resource paths, unlock behavior, profile fields, or
  visual redesign were changed.
- Focused Collection service, payload, and screen layout tests passed.

Recommended next slice:

- Define the Core profile aggregation/presentation boundary with conservative
  server-side visibility before adding Profile rows to the Character Menu.
- Classification: MEDIUM, likely design plus small Core model/API scaffolding.

## Phase 4, Slice 17E - Core Profile Boundary

Status: completed on 2026-07-07.

- Added `CitizenProfileService` plus profile presentation records for request
  context, snapshots, sections, fields, cards, contributor contracts, and
  visibility.
- Exposed the service through `ElarionApi.profiles()` and
  `ElarionApi.system().profiles()`.
- Added Core-only identity, Realm, and active-title profile sections with
  server-side `PUBLIC`, `SELF`, and `ADMIN` filtering.
- Enforced bounded aggregation output: 16 sections, 24 fields per section, and
  8 cards per section.
- Contributor registration is explicit and rejects duplicate IDs, but no addon
  contributors are registered yet.
- No Profile tab rendering, profile packets, Collection UI changes, art assets,
  or addon state changes were added.
- Focused `CitizenProfileServiceTest` passed.

Recommended next slice:

- Add bounded profile request/snapshot packet records and round-trip tests
  before rendering the Profile tab.
- Classification: MEDIUM.

## Phase 4, Slice 17F - Bounded Profile Transport Records

Status: completed on 2026-07-07.

- Added unregistered Character Menu profile payload records and codecs:
  `CitizenProfileRequestPayload` and `CitizenProfileSnapshotPayload`.
- Request payloads carry a target citizen UUID and optional section id.
- Snapshot payloads carry one bounded `CitizenProfileSnapshot`.
- Snapshot encoding enforces 16 sections, 24 fields per section, and 8 cards
  per section, matching the Core profile service caps.
- No payload type registration, receivers, client cache, Profile tab rendering,
  addon contributor, Collection behavior, persistence, config, command, or art
  asset changed.
- Focused profile payload/service tests passed.

Superseded next slice:

- Register profile payload types and add server-authoritative request/response
  dispatch only after confirming the receiver scope.
- Classification: SMALL to MEDIUM. Completed by Slice 17G.

## Phase 4, Slice 17G - Profile Transport Registration And Cache

Status: completed on 2026-07-07.

- Registered Character Menu profile request/snapshot payload types.
- Added server-authoritative profile request handling through
  `CitizenProfileService`, including zero-target self requests and optional
  visible section narrowing.
- Added `CitizenProfileClientState` plus the client receiver/cache lifecycle.
- No Character Menu Profile tab rendering, addon profile contributors, raw
  storage scans, broad shell redesign, or art asset promotion changed.
- Focused profile service/payload/client-state tests passed.

Recommended next slice:

- Render a Core-only Profile tab in Character Menu from the live profile
  request/cache path before adding addon contributors.
- Classification: MEDIUM.

## Phase 4, Slice 17H - Core Profile Tab Surface

Status: completed on 2026-07-07.

- Added a fixed Core Profile tab to Character Menu while preserving Mounts as
  the default selected unlockables tab.
- The Profile tab requests the server profile snapshot, reads
  `CitizenProfileClientState`, and renders visible Core identity, Realm, and
  active-title sections.
- The first Profile rendering used a section-list/detail-panel pattern; live
  review rejected it as too menu-like.
- The corrected Profile rendering uses one composed civic profile sheet with a
  name/identity strip plus Realm, office/title, and record panels.
- No addon profile contributors, completed quest projection, Offering score,
  NPC reputation, lifetime death total, packet schema, art asset, or broad
  Option A redesign changed.
- Focused Collection service/screen-layout/profile-client-state tests passed.

Recommended next slice:

- Phase 4, Slice 17I live QA captured the corrected `/charactermenu` Profile sheet at
  `build/ui-qa/slice-17j-profile-sheet/11-ledger-profile-sheet.png`.
- No custom payload crash occurred while opening `/charactermenu` and selecting the
  Profile tab in the dev client.
- The broader Character Menu shell and unlockable tabs still need the approved
  Option A art/component migration.
- Classification: MEDIUM.

## Phase 4, Slice 18 - Conversation-First NPC Services And Dedicated Bank

Status: completed on 2026-07-08.

- Added an explicit NPC presentation boundary. Dialogue nodes now carry
  `dialogue`, `bank`, or reserved `trade` presentation kinds, and dialogue
  options carry stable presentation roles such as `open_bank`, `deposit`,
  `withdraw`, and `back`.
- Normal NPC conversation remains the first surface for all NPCs. Bankers open
  the generic conversation screen first and expose `Open Bank`; selecting it
  transitions the same validated session into a dedicated bank presentation.
- Added a dedicated compact bank screen with NPC portrait, deposited balance,
  Deposit/Withdraw modes, bounded numeric amount input, presets, fee/total
  display, feedback, and Back to Conversation.
- Legacy root-level Economy deposit/withdraw banker prompt actions are
  projected into an in-memory `bank_service` node, preserving existing
  administrator files while moving service controls out of the root
  conversation UI.
- Removed the hard-coded `Neutral/0` relationship projection from NPC
  snapshots. Aggregate NPC/Realm reputation belongs in Character Menu; NPC
  screens may show only the relationship with the currently interacted NPC once
  NPC-owned authoritative relationship data exists.
- Trade presentation is reserved by the model but rejected by validation until
  its dedicated screen contract is implemented.

Files changed:

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
- NPC descriptor, migration, and packet tests under `addons/npcs/src/test/java/`.
- Docs under `docs/addons/`, `docs/systems/`, `docs/config.md`, and this audit.

Verification:

- `.\gradlew.bat :addons:npcs:test :addons:economy:test`
- Live screenshot QA under `build/ui-qa/slice-19-npc-bank/`:
  - `12-after-right-click.png`: conversation-first banker UI with `Open Bank`.
  - `13-bank-screen.png`: dedicated bank presentation.
  - `14-back-to-conversation.png`: return to conversation.

Recommended next slice:

- Implement the dedicated trade presentation contract against the approved
  compact Buy/Sell reference, starting with model/payload/config validation and
  a non-mutating screen shell before adding stock/price behavior.
- Classification: MEDIUM.

## Phase 4, Slice 20 - Dedicated Trade Presentation Shell

Status: completed on 2026-07-08.

- Enabled `presentation: trade` as a validated NPC node presentation.
- Added `NpcTradeScreen`, a dedicated compact Buy/Sell shell that opens from
  server-authored dialogue metadata and remains separate from generic
  conversation and bank presentation.
- The trade shell is intentionally non-mutating. Config validation rejects
  prompts and executable actions on options inside `trade` nodes until a trade
  owner provides bounded stock, price, inventory, and mutation contracts.
- Client dispatch now routes `presentationKind=trade` to `NpcTradeScreen` and
  closes it through the existing NPC close payload.
- Descriptor tests cover `dialogue, trade` presentation reporting so Admin
  config discovery reflects the new parsed content shape.

Files changed:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/ElarionNpcsClient.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcTradeScreen.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfigValidator.java`
- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/config/NpcConfigValidatorTest.java`
- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/config/NpcConfigDescriptorsTest.java`
- NPC config/network/UI docs and handoff files.

Verification:

- `.\gradlew.bat :addons:npcs:compileJava :addons:npcs:compileTestJava`
- `.\gradlew.bat :addons:npcs:test`

Recommended next slice:

- Define the real trade owner boundary before enabling buy/sell mutations:
  stock provider, price provider, inventory checks, server-authoritative
  request/result packets, and persistence ownership. Classification: MEDIUM
  audit/proposal before implementation.

## Phase 4 Follow-Up - Default Trader Content

Status: completed on 2026-07-08.

Changes:

- Added `worldheart_trader` to generated NPC defaults and the local dev-run NPC
  config.
- Added `dialogues/worldheart_trader.yml` as the default route into the
  non-mutating `NpcTradeScreen`.
- Added `worldheart_trader_1` to the dev lobby placement state, two blocks to
  the visual left of `worldheart_banker_1`. Its `entityId` is intentionally
  null so the NPC placement reconciler owns spawning.

Verification:

- Passed `.\gradlew.bat :addons:npcs:compileJava :addons:npcs:compileTestJava`.
- Passed `.\gradlew.bat :addons:npcs:test`.

Next QA:

- Live QA was attempted under `build/ui-qa/slice-trader-placement/`. The server
  loaded `2 NPC definitions, 3 skin profiles, 2 portrait profiles, and 2
  dialogues`, confirming the trader config/default path. The client reached the
  main menu, but joining was blocked by Minecraft's `Invalid Session` screen
  before the local server received a player login. Captures:
  `01-client-current.png` and `02-multiplayer.png`.
- Next live pass should right-click `worldheart_trader_1`, verify normal
  conversation opens first, then `Trade` opens the dedicated trade shell.

## Phase 4 Follow-Up - Banker And Trader Visual Polish

Status: completed on 2026-07-08.

Changes:

- Replaced the default banker/trader placeholder visuals with explicit
  `worldheart_banker.png` and `worldheart_trader.png` 64x64 NPC skin textures.
- Wired the banker to
  `portrait_character_portrait_icons_03_icons_03.png` and the trader to
  `portrait_character_portrait_icons_27_icons_27.png` through the existing
  portrait profile system.
- Added 32x32 texture-size handling to NPC portrait rendering so curated
  library portrait icons draw without being sampled as 64x64 textures.
- Aligned the bank `+100`, `+1K`, and `+10K` buttons with the amount field,
  added a blinking amount caret, and used the shared Sigil currency icon across
  the balance badge, input, fee/total row, and trader price rows.
- Added display-only trader preview rows for two Nether Gate Tickets, one
  Cobblestone, a Protection IV chestplate, and a named/lore Protection IV
  chestplate. These rows do not buy, sell, reserve, or mutate inventory.

Verification:

- Passed `.\gradlew.bat :platform:core:compileJava :addons:npcs:compileJava :addons:npcs:compileTestJava`.
- Passed `.\gradlew.bat :addons:npcs:test`.
- No live screenshot QA was run at the user's request.

Unresolved:

- Real trade ownership remains undefined. Do not convert the preview catalog
  into gameplay until stock providers, price providers, inventory checks,
  request/result packets, persistence ownership, and Economy formatting are
  approved.

## Phase 4 Follow-Up - Banker And Trader Live QA

Status: verified on 2026-07-08.

- Banker and trader portraits render once at their intended 32x32 source
  bounds.
- The bank amount caret starts at the numeric input origin and follows the
  measured final digit; preset `100` was captured with the caret immediately
  after the value and with the blink-off state in the paired capture.
- Balance, input, Fee, Total, and trader prices use the same Sigil texture with
  explicit source sizing.
- Trader armor rows show native enchantment, lore, and attribute tooltips.
- Both standard 64x64 skins render valid body regions in-world.

Evidence directory: `build/ui-qa/npc-polish-live-2/`.

Remaining risk: the catalog is presentation-only. Buy/sell remains blocked on
the approved trade-owner architecture slice.

## Phase 4, Slice 21 - NPC Trade Owner Audit

Status: documentation-only architecture audit completed on 2026-07-08.

- Verified that all current catalog rows, prices, and stock labels are client
  examples and cannot be used for mutation.
- Selected a dedicated bounded server snapshot instead of expanding the large
  dialogue payload or parsing localized labels.
- Kept the civic trade shell, real item-stack tooltips, and virtualized list
  direction.
- Deferred enabled Buy/Sell controls until payment idempotency, purchase
  recovery, stock ownership, and server request validation are implemented.

Full contract: `docs/reports/NPC_TRADE_OWNER_AUDIT.md`.

## Phase 4, Slice 22 - Server-Authored NPC Trade Catalogs

Status: completed on 2026-07-08.

- Added parsed NPC trade catalogs under `config/elarion/addons/npcs/trades.yml`.
- NPC definitions can reference a catalog with `trade-catalog`.
- Added trade models, loader parsing, validation, generated defaults, and
  read-only Admin/config descriptors.
- Added bounded `NpcTradeSnapshotPayload`/offer payload transport. The server
  builds `ItemStack` previews so enchantments, custom names, lore, and native
  tooltips survive the packet boundary.
- Updated the trade UI to render server-authored rows instead of hard-coded
  client preview rows. Buy/Sell mutation remains disabled.
- Added Nether/End ticket art through the real Portal ticket stack path:
  crimson stele for Nether/default and blue stele for End custom model data.
  Portal confirmation prompt ticket icons use the same semantic assets. Item
  models now use portals-owned item texture copies to avoid missing inventory
  textures from GUI-library model references.
- Follow-up fixes preserve the selected banker Deposit/Withdraw mode through
  server feedback refreshes, bridge legacy `worldheart_trader` configs to the
  generated catalog in memory, and add `price-key` metadata for future dynamic
  Economy pricing.
- Kept Economy dependency, payment, inventory mutation, finite stock, purchase
  packets, and trade persistence excluded.

Verification:

- Passed `.\gradlew.bat :platform:core:compileJava :addons:npcs:compileJava :addons:npcs:compileTestJava`.
- Passed `.\gradlew.bat :addons:npcs:test --tests panetina.elarion.addons.npcs.config.NpcConfigDescriptorsTest --tests panetina.elarion.addons.npcs.config.NpcConfigValidatorTest`.

Next:

- Phase 4, Slice 23: propose/add the Economy idempotent operation receipt
  foundation needed before NPC trade purchases can become crash-safe. Keep NPC
  purchase packets and stock persistence out unless that proposal is approved.

## Phase 4 Follow-Up - Trader Row Polish And 999 Sigil QA

Status: completed with live in-world QA on 2026-07-08.

- Trader row item tooltips now require hovering the actual 16x16 item icon;
  full-row hover only highlights selection.
- Trader prices now use a stable Sigil icon column with the value immediately
  after the icon.
- NPC trade preview names/lore and real Portal tickets now disable default
  italic custom-name styling.
- `runClientOne`/`runClientTwo` now pass stable local dev UUID/access-token
  values.
- Economy now raises the physical Sigil stack path to 999 through the item
  max, inventory slot ceiling, and ItemStack serialization codec.

Verification:

- Passed focused NPC/Portal/Economy compile plus `EconomyItemsTest`.
- Dev server reached ready state and loaded `1 trade catalogs`.
- Live QA evidence under `build/ui-qa/trade-fixes-20260708-stack999/` verified
  the 999 Sigil hotbar stack, fixed price column, no tooltip on row-text hover,
  native tooltip on item-icon hover, and non-italic Nether ticket tooltip.
- The earlier `Invalid Session` evidence was an automation-coordinate error:
  `minecraft-qa.ps1` uses client-area coordinates while captures include the
  Windows title bar.

## Phase 4, Slice 23B - NPC Tax Jurisdiction Foundation

Status: backend/config/persistence complete on 2026-07-08; no visual change.

- NPC definitions now expose `tax-jurisdiction` in the descriptor system.
- Placement schema v2 stores resolved Realm/world jurisdiction.
- The future quantity panel must render server-authored quote values only.
- Live screenshot QA was not required because this slice changed no rendering.

## Phase 4, Slice 23C - Trader Quote Layout

The trader uses a fixed three-row catalog viewport plus one selected-offer
quote band. Selection and hover follow the civic green-border state. Totals are
server-authored and Confirm remains disabled. Live QA is deferred until the
purchase action exists.

## Phase 4, Slice 23C.1 - Worldheart Authority Boundary

Status: backend architecture complete on 2026-07-09; no visual change.

- Core now owns persistent Worldheart governing authority and central role
  checks for OP4/admin versus current Worldheart ruler.
- Economy now owns a dedicated Worldheart treasury route; the treasury is not a
  player wallet and does not move when authority changes.
- Future Worldheart control blocks, Seat of Rule tax UI, or owner Admin tools
  should render only server-authored permission state from this service.

## Phase 4, Slice 23D - Trader Unlimited BUY

Status: backend/network/UI activation complete on 2026-07-09.

- The existing selected-offer panel now enables Confirm when the current
  server-authored quote is valid.
- Confirm sends a purchase ID and bounded quantity; result feedback is displayed
  inside the selected-offer panel.
- The server owns payment, treasury routing, purchase journaling, and delivery.
  The client does not compute tax, total, or destination.
- No live screenshot QA was run in this slice. Finite stock and Sell UI remain
  future work.

## Phase 4, Slice 23E - Finite NPC Trade Stock

Status: backend/network/UI metadata complete on 2026-07-09.

- Trader rows now receive server-authored finite stock metadata from the
  snapshot. Rows display `Stock N` beside the authored subtitle only for finite
  offers; unlimited offers keep their normal subtitle.
- Quote maximums are clamped by placed-NPC stock, and stale/over-large purchase
  requests are rejected server-side.
- Stock is gameplay state, not UI state. The screen does not calculate
  remaining stock, restock intervals, taxes, or settlement.
- Native item stack tooltips remain limited to the item icon hitbox.
- No live screenshot QA was run in this slice. Sell/buyback UI remains future
  work.

## Phase 9 NPC Narrative Note - 2026-07-11

Phase 9 changed NPC backend narrative contracts only. Dialogue option snapshots
now omit consumed one-time choices, but no panel geometry, typography, icons,
hitboxes, or service-screen layouts changed. No screenshot QA was required.

## Phase 14 Onboarding Runtime Audit - 2026-07-18

- Generic-client live QA verified the current Character Creation and Realm
  Placement visual sequence through the saved localhost multiplayer server.
- One behavioral UI defect was confirmed: a generated account name containing
  digits could be rendered as a prefill even though server validation rejected
  it. Core now filters the prefill through the canonical nickname policy.
- Blank identity state, populated biography state, assigned Realm highlight,
  Confirm Placement boundary, and living-world entry all rendered without
  overlap or inaccessible controls at the tested GUI scale.
- This was a targeted flow audit; the representative Phase 14 pass is recorded
  in the closure section below.

## Phase 14 Representative UI/Resource Closure - 2026-07-18

- Representative live screenshots now cover the Character Menu, Admin Config,
  notification list/action/detail flow, and Neutral Portal prompt; accepted
  Portal, Shrine, onboarding, bank, trader, and mount evidence was re-indexed.
- The incomplete Excalibured runtime archive was the cause of missing-resource
  noise. The complete art pack plus separate font pack now load with zero
  client error/missing-texture matches.
- Shared pack synchronization is idempotent across generic and stable clients.
- See `PHASE_14_UI_RESOURCE_QA.md`; the Phase 14 shared UI/resource gate passes.
