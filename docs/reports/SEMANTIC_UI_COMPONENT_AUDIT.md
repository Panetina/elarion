# Semantic UI Component Audit

Date: 2026-07-10

Scope: Phase 5 Slice 1. Documentation-only audit of repeated custom Elarion UI
patterns after the Notification, Government, Character Menu, Shrine, Portal,
Grave, Admin Panel, NPC Bank, and NPC Trade civic revamps. No production code
was changed.

## Objective

Identify which repeated screen patterns should become reusable semantic Core UI
components before more UI families are rebuilt. This audit intentionally avoids
Chronicle, placeholder, profile-contributor, and persistence implementation
work.

## Existing Shared Foundation

Core already owns the low-level civic UI foundation:

- `ElarionCivicUi`: shells, thin boxes, row surfaces, message bodies, compact
  action buttons, close button, dividers, chips, and centered text baseline.
- `ElarionUiMetrics`: font-scale-aware row/button/control height and visible
  row calculations.
- `ElarionUiTypography`: scaled width, font height, wrapping, clipping,
  ellipsis, and drawing helpers.
- `ElarionVirtualList`: bounded scrolling and selection movement.
- `ElarionUiIcons`: semantic icon catalog backed by curated runtime PNGs.

The next step is not another color/button primitive pass. The gap is semantic
composition: repeated row ranges, list bands, item/money summaries, action
bands, and profile/detail cards still live inside individual screens.

## Repeated Patterns Found

### 1. Bounded List With Centered Range Marker

Current repeated implementations:

- Government uses `GovernmentUiGlyphs.rowRange(...)`.
- Grave Recovery has its own `Rows first-last / total` drawing.
- Admin Panel action overflow now has its own centered range marker.
- NPC Trade uses tiny down/up markers around `Rows first-last / total`.
- Collection/Admin/Government screens separately compute visible row counts.

Target component:

```text
ElarionListRangeMarker
ElarionListFrame
ElarionListLayout
```

Inputs should be semantic and layout-safe:

- first visible row, last visible row, total rows
- whether previous/next rows exist
- preferred marker style: compact text, tiny arrows, none
- x/y/width or centered anchor
- text style and muted/active tone

Why first:

- It directly addresses recurring polish bugs: text sitting on borders,
  mismatched arrows, row-count alignment, and list footer collisions.
- It can be extracted without changing server packets or domain models.
- It has existing examples across Core and addons.

### 2. Row Surface Plus Icon/Title/Meta Layout

Current repeated implementations:

- Notification rows: icon, title, subtitle/status, age, unread state, accent.
- Government rows: icon, title, tag/status, metadata, selected state.
- Admin Panel rows: icon/kind/state/actions with selected/danger state.
- NPC Trade rows: item icon, title, stock, Sigil price.
- Character Menu rows: icon, title, rank/status, active state.

Target component:

```text
ElarionSemanticRow
ElarionSemanticRowLayout
ElarionSemanticRowRenderer
```

Inputs:

- icon kind: semantic UI icon or native item stack
- title/body/subtitle/status/timestamp fields
- optional left accent color
- selected/hovered/disabled/danger/unread flags
- optional right columns: time, stock, count, price, action hint
- row height derived from font scale and content role

Rules:

- Native item tooltips must be item-slot-only.
- Portrait images remain portrait-only, not generic row icons.
- Hitboxes must come from the same layout record used to render.

Why not first:

- It touches more screens and can become too broad. Extract after range/action
  band helpers prove stable.

### 3. Contextual Action Band

Current repeated implementations:

- Notification drawer footer: `View`, `Claim`, `Go To`, `Dismiss`, etc.
- Admin Panel row/detail actions and modal footer actions.
- NPC Trade selected-offer band: quantity controls, status, confirm, totals.
- Bank screen: presets, confirm, fee/total quote.
- Portal/Grave confirmation footers: primary/secondary decisions.

Target component:

```text
ElarionActionBand
ElarionActionButtonSpec
ElarionActionBandLayout
```

Inputs:

- action specs: id, label, icon, tone, enabled, tooltip/status
- max actions per row and wrap behavior
- optional left/middle/right zones
- optional status text
- optional quantity controls or numeric input slot

Rules:

- The band must be bounded inside its parent panel.
- Disabled actions stay visible but muted.
- Text must use `ElarionCivicUi.centeredTextY`.
- Server-authoritative actions remain server-authored; the component is
  presentation and hitbox only.

Why second:

- It removes a major source of recurring button-baseline and footer-spacing
  bugs after the smaller range marker helper lands.

### 4. Money / Price / Tax Summary

Current repeated implementations:

- NPC Trade quote rows: Subtotal, Realm/Worldheart tax, Total/Payout.
- Bank quote rows: Fee and Total.
- Portal fee prompts: Sigil payment slot.
- Future Treasury/Seat of Rule tax views will need the same vocabulary.

Target component:

```text
ElarionMoneyAmount
ElarionSettlementSummary
ElarionCurrencyIcon
```

Inputs:

- label, amount, currency id/icon
- semantic kind: subtotal, fee/tax, total, payout, balance
- compact or expanded placement
- positive/neutral/destructive tone

Rules:

- The client must never compute tax, fee, total, payout, or treasury routing.
- Values come from server-authored quotes/snapshots.
- Sigil icon, amount, and label should stay visually grouped.
- Total/Payout should be visually stronger than intermediate values.

Why not first:

- It should follow action-band extraction because it often lives inside the
  selected-offer/action footer.

### 5. Detail / Dossier Card

Current repeated implementations:

- Character Menu profile sheet.
- Notification detail view.
- Government selected row/detail panel.
- Shrine project summary.
- NPC conversation body and service header.
- Admin Panel detail card.

Target component:

```text
ElarionDetailCard
ElarionIdentityHeader
ElarionKeyValueGrid
```

Inputs:

- title, subtitle, portrait/icon, body lines
- badges/chips
- key/value rows
- optional native item/reward slots
- optional scroll viewport

Rules:

- Player head/portrait/identity rendering must be explicit; do not infer
  portrait assets from generic icon ids.
- Long text scrolls or wraps within a bounded viewport.

Why later:

- This overlaps future profile and Chronicle work. Keep it after smaller
  extraction wins.

## Implemented Slice

Phase 5 Slice 2 implemented the first narrow component extraction:

- Added Core `ElarionListRangeMarker`.
- Added focused Core tests for clamped ranges, labels, hidden all-visible
  state, and empty ranges.
- Migrated Government range markers through the Core helper while retaining
  the Government wrapper for existing screen call sites.
- Migrated Grave Recovery range markers directly to the Core helper.
- Did not change packets, server models, persistence, Chronicle, profile,
  placeholder, or row renderer behavior.

Verification:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionListRangeMarkerTest`
- `.\gradlew.bat :addons:government:compileJava :addons:underworld:compileJava`
- `.\gradlew.bat :addons:underworld:test --tests panetina.elarion.addons.underworld.client.GraveRecoveryScreenLayoutTest`

## Next Recommended Implementation Slice

Phase 5 Slice 3 implemented the first money-summary prototype:

- Added Core `ElarionMoneySummary` for compact label + Sigil icon + amount
  cells.
- Added focused Core tests for grouped icon/value layout, invalid size/gap
  clamping, and label/emphasis normalization.
- Migrated NPC Trade selected-offer `Subtotal`, jurisdiction tax, and
  `Total/Payout` cells through the Core helper.
- Left NPC Trade catalog row prices unchanged because those are a separate
  compact row-column pattern.
- Did not change server-authored quote values, tax calculation, treasury
  routing, packets, persistence, or purchase/sale authority.

Verification:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionMoneySummaryTest :addons:npcs:compileJava`

## Implemented Slice

Phase 5 Slice 4 implemented the first action-band layout prototype:

- Added Core `ElarionActionBandLayout` for bounded action-band rectangles and
  matching hitboxes.
- Added focused Core tests for compact quantity/confirm geometry, render-bound
  containment, and invalid-size clamping.
- Migrated NPC Trade selected-offer quantity controls, status slot, confirm
  button, and click hitboxes through the Core helper.
- Kept NPC Trade labels, tones, quote rendering, and server-authoritative
  purchase request handling in the screen.
- Did not change packets, server models, persistence, tax logic, Chronicle,
  profile contributors, placeholder registry, or UI gallery.

Verification:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionActionBandLayoutTest :addons:npcs:compileJava`

## Implemented Slice

Phase 5 Slice 5 implemented the first semantic row layout prototype:

- Added Core `ElarionSemanticRowLayout` for compact row rectangles, item icon
  bounds, text baselines, optional meta columns, and price icon placement.
- Added focused Core tests for NPC Trade catalog geometry, item-icon-only
  tooltip bounds, and invalid-size clamping.
- Migrated NPC Trade catalog item-price rows through the Core helper.
- Kept NPC Trade row payloads, offer labels, stock, prices, selection behavior,
  and native item tooltips screen/server-owned.
- Did not change packets, server models, persistence, tax logic, Chronicle,
  profile contributors, placeholder registry, or UI gallery.

Verification:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionSemanticRowLayoutTest :addons:npcs:compileJava`

## Implemented Slice

Phase 5 Slice 6 implemented the first non-item row-layout migration:

- Extended Core `ElarionSemanticRowLayout` with compact record-row geometry for
  icon, title, tag, metric, and secondary metric placement.
- Added focused Core tests for Government-style record rows and small-row
  baseline behavior.
- Routed `GovernmentUiComponents.recordRow(...)` through the Core helper,
  covering Civic Forum and Seat of Rule rows, including history/archive rows.
- Did not change Government row payloads, archive semantics, server models,
  persistence, tax logic, Chronicle, profile contributors, placeholder
  registry, or UI gallery.

Verification:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionSemanticRowLayoutTest :addons:government:compileJava :addons:government:test --tests panetina.elarion.addons.government.client.GovernmentUiScreenLayoutTest`

## Implemented Slice

Phase 5 Slice 7 documented the first extracted helper set:

- Added `docs/ui/COMPONENT_REFERENCE.md`.
- Documented intended use, semantic inputs, states, current users,
  anti-patterns, and extension rules for:
  - `ElarionListRangeMarker`
  - `ElarionMoneySummary`
  - `ElarionActionBandLayout`
  - `ElarionSemanticRowLayout`
- Added a development-only local client gallery screen opened with
  `/elarion-ui-gallery`.
- The gallery command is intercepted client-side only in Fabric development
  environments. It is not a server command, production feature, packet, or
  player-facing UI surface.
- Did not change packets, server models, persistence, tax logic, Chronicle,
  profile contributors, placeholder registry, or player-facing UI behavior.

Verification:

- `.\gradlew.bat :platform:core:compileJava`

## Implemented Slice

Phase 5 Slice 8 implemented the first detail/dossier header prototype:

- Added Core `ElarionDetailCardLayout` for identity/detail header geometry.
- Added focused Core tests for Government-style detail header geometry and
  invalid-size clamping.
- Routed `GovernmentUiComponents.detailHeader(...)` through the Core helper,
  covering Civic Forum and Seat of Rule detail headers.
- Updated the shared component reference and dev-only gallery to include the
  detail header shape.
- Kept Government icon frames, tags, text colors, row payloads, detail
  semantics, and server authority in Government.
- Did not change packets, server models, persistence, tax logic, Chronicle,
  profile contributors, placeholder registry, or player-facing behavior.

Verification:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionDetailCardLayoutTest :addons:government:compileJava :addons:government:test --tests panetina.elarion.addons.government.client.GovernmentUiScreenLayoutTest`

## Implemented Slice

Phase 5 Slice 9 implemented the first detail body/key-value helper:

- Added Core `ElarionDetailBodyLayout` for section-title, bounded body text,
  and simple key/value row geometry.
- Added focused Core tests for Government-style section-title offsets, bounded
  body clamping, and key/value width behavior.
- Routed `GovernmentUiComponents.sectionTitle(...)` and
  `GovernmentUiComponents.bodyText(...)` through the Core helper, covering
  Civic Forum and Seat of Rule detail body areas without changing their
  semantic payloads.
- Updated the shared component reference and dev-only gallery to include the
  detail-body shape.
- Did not change packets, server models, persistence, tax logic, Chronicle,
  profile contributors, placeholder registry, or player-facing behavior.

Verification:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionDetailBodyLayoutTest :platform:core:compileJava :addons:government:compileJava :addons:government:test --tests panetina.elarion.addons.government.client.GovernmentUiScreenLayoutTest`

## Implemented Slice

Phase 5 Slice 10 implemented the first shared badge/tag geometry helper:

- Added Core `ElarionBadgeLayout` for compact badge width, accent strip, top
  line, and text inset geometry.
- Added focused Core tests for text max width, minimum width, fixed insets, and
  maximum-width clamping.
- Routed Core `ElarionCivicUi.statusChip(...)` and Government
  `GovernmentUiGlyphs.tag(...)` through the helper.
- Preserved caller-owned colors, tones, active state, labels, and domain
  semantics.
- Did not change packets, server models, persistence, tax logic, Chronicle,
  profile contributors, placeholder registry, or player-facing behavior.

Verification:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionBadgeLayoutTest :platform:core:compileJava :addons:government:compileJava :addons:government:test --tests panetina.elarion.addons.government.client.GovernmentUiScreenLayoutTest`

## Implemented Slice

Phase 5 Slice 11 implemented the first shared progress-track helper:

- Added Core `ElarionProgressTrackLayout` for bounded progress track, top-line,
  and inner-fill rectangles.
- Added focused Core tests for empty, partial, full, and invalid-size progress
  geometry.
- Routed Government vote-option progress tracks through the helper.
- Preserved Government-owned vote values, ratio calculation, labels, colors,
  selection state, and semantics.
- Did not change packets, server models, persistence, tax logic, Chronicle,
  profile contributors, placeholder registry, or player-facing behavior.

Verification:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionProgressTrackLayoutTest :platform:core:compileJava :addons:government:compileJava :addons:government:test --tests panetina.elarion.addons.government.client.GovernmentUiScreenLayoutTest`

## Implemented Slice

Phase 5 Slice 12 implemented the first shared empty-state layout helper:

- Added Core `ElarionEmptyStateLayout` for bounded empty panel, title, and
  wrapped-body rectangles.
- Added focused Core tests for Notification-style empty-state offsets and
  invalid-size clamping.
- Routed Notification drawer empty categories through the helper.
- Preserved category-specific empty titles and body text in the notification
  HUD.
- Did not change packets, server models, persistence, tax logic, Chronicle,
  profile contributors, placeholder registry, or player-facing behavior beyond
  shared layout math.

Verification:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionEmptyStateLayoutTest :platform:core:compileJava`

## Implemented Slice

Phase 5 Slice 13 implemented the first shared modal layout helper:

- Added Core `ElarionModalLayout` for centered two-button modal geometry,
  including body rectangle, optional input placement, and footer button
  positions.
- Added focused Core tests for the existing Admin Panel confirmation/input
  modal geometry and invalid spec clamping.
- Routed Admin Panel action modal layout creation through the Core helper while
  preserving the existing package-local layout record and tests.
- Preserved Admin Panel rendering, click handling, autocomplete, validation,
  permissions, packets, config apply behavior, and server authority.

Verification:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionModalLayoutTest --tests panetina.elarion.core.client.ElarionAdminPanelScreenLayoutTest :platform:core:compileJava`

## Implemented Slice

Phase 5 Slice 14 implemented the first shared input-field layout helper:

- Added Core `ElarionInputFieldLayout` for single-line input geometry:
  optional icon rectangle, text bounds, centered text baseline, and caret max
  X.
- Added focused Core tests for NPC Bank amount geometry, no-icon fields, and
  invalid-size clamping.
- Routed NPC Bank amount input icon, placeholder/value text, and caret through
  the helper.
- Preserved input ownership, max digits, quote requests, validation,
  suggestions, permissions, packets, and server authority in the owning screen
  and services.

Verification:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionInputFieldLayoutTest :platform:core:compileJava :addons:npcs:compileJava`

## Implemented Slice

Phase 5 Slice 15 implemented the first shared native item-slot layout helper:

- Added Core `ElarionItemSlotLayout` for square slot bounds, centered native
  item draw bounds, grid placement, and slot-only hover detection.
- Added focused Core tests for 18x18 native item slots, grid positioning,
  hover bounds, and invalid-size clamping.
- Routed Notification reward previews through the helper while preserving
  server-authored reward labels, detail lines, and Minecraft native item
  tooltips.
- Preserved notification packets, reward preview data, selection behavior,
  claim/dismiss actions, persistence, and server authority.

Verification:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionItemSlotLayoutTest :platform:core:compileJava`

## Implemented Slice

Phase 5 Slice 16 implemented the first shared row viewport layout helper:

- Added Core `ElarionScrollViewportLayout` for bounded row-list viewport
  geometry: visible capacity, clamped first row, visible count, maximum first
  row, row Y positions, and row-only hit testing.
- Added focused Core tests for NPC Trade catalog geometry, first-row clamping,
  gap-aware hit testing, and empty viewport safety.
- Routed NPC Trade catalog render range, click selection, scroll bounds, and
  range text values through the helper.
- Preserved NPC/Economy-owned offers, quotes, stock, purchase/sell behavior,
  packets, persistence, and server authority.

Verification:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionScrollViewportLayoutTest :platform:core:compileJava :addons:npcs:compileJava`

## Implemented Slice

Phase 5 Slice 17 implemented the first shared multiline text viewport helper:

- Added Core `ElarionTextViewportLayout` for visible line capacity, clamped
  first line, visible count, maximum first line, line Y positions,
  absolute/visible line mapping, and scroll hint state.
- Added focused Core tests for Character Creation biography geometry, first
  line clamping, caret-line visibility mapping, and invalid input safety.
- Routed Character Creation biography scroll range, visible wrapped lines,
  caret-line visibility, and scroll hints through the helper.
- Preserved text wrapping, input ownership, max length, validation, submit
  packets, Realm-placement flow, persistence, and server authority.

Verification:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionTextViewportLayoutTest :platform:core:compileJava`

## Implemented Slice

Phase 5 Slice 18 implemented the first shared custom tooltip shell layout:

- Added Core `ElarionTooltipShellLayout` for screen-edge-aware shell placement
  and padded content bounds.
- Added focused Core tests for down/right placement, left/up flipping,
  oversized-content clamping, and invalid-size safety.
- Routed NPC relationship hover hints through the helper with civic shell
  chrome.
- Preserved NPC-owned relationship labels, hover triggers, visibility,
  packets, persistence, and server authority.
- Preserved native Minecraft item tooltip behavior for NPC Trade, Shrine,
  Grave, Notification item rewards, and Portal ticket/item previews.

Verification:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionTooltipShellLayoutTest :platform:core:compileJava :addons:npcs:compileJava`

## Implemented Slice

Phase 5 Slice 19 implemented the first shared compact icon/label line helper:

- Added Core `ElarionIconLabelLineLayout` for compact label + icon + value
  geometry.
- Added focused Core tests for NPC Bank Fee/Total geometry, custom gaps, and
  invalid-size safety.
- Routed NPC Bank Fee/Total quote lines through the helper.
- Preserved server-authored quote values, quote validity, bank tax policy,
  Economy ownership, packets, persistence, and server authority.

Verification:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionIconLabelLineLayoutTest :platform:core:compileJava :addons:npcs:compileJava`

## Implemented Slice

Phase 5 Slice 20 implemented the first shared status/feedback line helper:

- Added Core `ElarionStatusLineLayout` for bounded one-line status/feedback
  geometry.
- Added focused Core tests for NPC Bank feedback geometry, vertical centering
  inside taller status areas, and invalid-size safety.
- Routed NPC Bank invalid quote messages and dialogue feedback through the
  helper.
- Preserved server-authored messages, feedback severity, quote validity,
  bank tax policy, Economy/NPC ownership, packets, persistence, and server
  authority.

Verification:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionStatusLineLayoutTest :platform:core:compileJava :addons:npcs:compileJava`

## Implemented Slice

Phase 5 Slice 21 implemented the first shared centered section-header helper:

- Added Core `ElarionSectionHeaderLayout` for compact centered icon/title/
  divider section header geometry.
- Added focused Core tests for Character Creation `IDENTITY` and `BIOGRAPHY`
  panel header geometry and invalid-size safety.
- Routed the two Character Creation panel headers through the helper.
- Preserved onboarding content, icons, labels, input validation, Realm
  placement flow, packets, persistence, and server authority.

Verification:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionSectionHeaderLayoutTest :platform:core:compileJava`

## Implemented Slice

Phase 5 Slice 22 implemented the first shared left panel-header helper:

- Added Core `ElarionPanelHeaderLayout` for left-aligned `headerShell` title,
  divider, and body-start geometry.
- Added focused Core tests for NPC Trade catalog panel geometry, NPC Bank
  amount panel geometry, and invalid-size safety.
- Routed NPC Bank amount-panel and NPC Trade catalog-panel headers through the
  helper.
- Preserved server-authored values, quotes, stock, trade offers, packets,
  persistence, and server authority.

Verification:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionPanelHeaderLayoutTest :platform:core:compileJava :addons:npcs:compileJava`
- Live screenshot QA harness was exercised successfully through
  `build/ui-qa/slice-22-panel-header/trader-dialogue-open-2.png`; this capture
  verified the local client/server path and NPC dialogue opening. The migrated
  panel headers are geometry-only and were covered by focused tests.

## Implemented Slice

Phase 5 Slice 23 implemented the first shared service-screen header helper:

- Added Core `ElarionServiceHeaderLayout` for portrait/title/subtitle/
  currency-badge/close-button geometry.
- Added focused Core tests for NPC Bank service header geometry, NPC Trade
  service header geometry, and invalid-size safety.
- Routed NPC Bank and NPC Trade service headers through the helper.
- Preserved NPC identity, service labels, balance values, close behavior,
  packets, persistence, and server authority.

Verification:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionServiceHeaderLayoutTest :platform:core:compileJava :addons:npcs:compileJava`

## Implemented Slice

Phase 5 Slice 24 implemented the first shared paired button-row helper:

- Added Core `ElarionPairedButtonLayout` for left/right button rectangles,
  gap, and combined bounds.
- Added focused Core tests for NPC Bank Deposit/Withdraw geometry, NPC Trade
  Buy/Sell geometry, hitbox behavior, and invalid-size safety.
- Routed NPC Bank Deposit/Withdraw and NPC Trade Buy/Sell render/click
  geometry through the helper.
- Preserved labels, roles, selected state, enabled state, packets,
  persistence, and server authority.

Verification:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionPairedButtonLayoutTest :platform:core:compileJava :addons:npcs:compileJava`

## Implemented Slice

Phase 5 Slice 25 implemented the first shared single footer-action helper:

- Added Core `ElarionFooterActionLayout` for footer action button geometry.
- Added focused Core tests for NPC Bank and NPC Trade footer button geometry,
  hitbox behavior, and invalid-size safety.
- Routed NPC Bank and NPC Trade `Back to Conversation` render/click geometry
  through the helper.
- Preserved labels, back-role lookup, fallback close behavior, packets,
  persistence, and server authority.

Verification:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionFooterActionLayoutTest :platform:core:compileJava :addons:npcs:compileJava`

## Implemented Slices

Phase 5 Slices 26-29 tightened the remaining NPC service-screen local geometry:

- Slice 26 added Core `ElarionPresetButtonRowLayout` and routed NPC Bank
  amount preset render/click geometry through it.
- Slice 27 extended that helper with a preset-plus-confirm row and routed NPC
  Bank Confirm action render/click geometry through it.
- Slice 28 added Core `ElarionSplitSummaryLayout` and routed NPC Bank
  Fee/Total divider and summary origins through it.
- Slice 29 moved NPC Bank and NPC Trade close-button hitboxes onto the existing
  `ElarionServiceHeaderLayout` close rectangle.
- Preserved preset amounts, labels, quote validation, submit behavior, close
  behavior, packets, persistence, and server authority.

Verification:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionPresetButtonRowLayoutTest :platform:core:compileJava :addons:npcs:compileJava`
- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionSplitSummaryLayoutTest :platform:core:compileJava :addons:npcs:compileJava`
- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionPresetButtonRowLayoutTest --tests panetina.elarion.core.client.ui.ElarionSplitSummaryLayoutTest --tests panetina.elarion.core.client.ui.ElarionServiceHeaderLayoutTest :platform:core:compileJava :addons:npcs:compileJava`

## Implemented Slice

Phase 5 Slice 30 added shared service-helper coverage to the dev-only UI
gallery:

- The gallery now includes compact examples for service header, paired service
  buttons, preset/confirm row, split summary, and footer action helpers.
- The gallery remains development-only and local-client only through
  `/elarion-ui-gallery`.
- No production gameplay screen, packet, persistence, server model, tax policy,
  NPC dialogue behavior, or authority rule changed.

Verification:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionPresetButtonRowLayoutTest --tests panetina.elarion.core.client.ui.ElarionSplitSummaryLayoutTest --tests panetina.elarion.core.client.ui.ElarionServiceHeaderLayoutTest :platform:core:compileJava`

## Phase 5 Closure

Phase 5's shared semantic UI foundation is complete enough to close this phase
and move to the screen-specific phases.

What Phase 5 delivered:

- Shared, tested layout helpers for range markers, money summaries, action
  bands, semantic rows, detail cards, body/key-value sections, badges,
  progress tracks, empty states, modal shells, input fields, native item-slot
  hover boundaries, row viewports, text viewports, tooltip shells, icon/label
  lines, status lines, section headers, panel headers, service headers, paired
  buttons, footer actions, preset/confirm rows, and split summaries.
- First migrations through those helpers in Government, Grave Recovery, NPC
  Bank, NPC Trade, Collection/Profile, and other narrow touched surfaces.
- Focused tests for each new geometry helper.
- Dev-only `/elarion-ui-gallery` coverage for the current helper families.
- Documentation in `docs/ui/COMPONENT_REFERENCE.md` and
  `docs/systems/UI_JOURNAL.md`.

What Phase 5 did not and should not finish:

- It did not migrate every custom screen. Shrine, Portal, Character Menu,
  Admin Panel, Notification details, Grave Recovery follow-ups, and NPC
  dialogue refinements remain screen-family work.
- It did not build Chronicle, placeholder, profile aggregation, or advanced
  owner-maintained summary systems.
- It did not change packets, persistence, server authority, tax policy, or
  domain behavior except where earlier approved non-Phase-5 slices did so.

Closure decision:

- Treat remaining migrations as Phase 6/7 work, not Phase 5 blockers.
- New UI helpers must still update descriptor docs, component reference, UI
  journal, tests, and the dev gallery when they are promoted.

## Next Recommended Implementation Slice

Phase 6 Slice 1: Government Archive/History Row and Detail Action Migration.

Recommended model: `Medium`.

Use `High` if the slice includes live screenshot QA or archive prose/Chronicle
rewrites. Keep it `Medium` if it is layout-only.

Recommended scope:

- Reuse the shared row/range/action helpers to center Government archive rows,
  standardize range markers, and align detail actions.
- Keep structured archive data and history prose unchanged unless a separate
  Chronicle/readability slice is explicitly approved.
- Update Government UI docs, UI journal, component reference if helpers are
  extended, and focused tests.

Explicit exclusions:

- Do not implement Chronicle variant rendering in this slice.
- Do not rewrite all Government screens.
- Do not change persistence, networking, tax, authority, or public commands.

## Risks

- Too-generic renderers could erase domain-specific layout needs. Keep helpers
  narrow and migrate only one or two screen families at a time.
- If hitbox and render coordinates are not exposed from one layout object, the
  old drift bugs will return.
- Font scale must remain part of every calculation; no fixed `y + 3` or
  `y + 6` baselines should be introduced.

## Deferred Work

- Additional migrations for Shrine, Grave, Admin Panel, Character Menu,
  Portal, and Notification detail surfaces.
- Chronicle/profile/placeholder-driven text components.
