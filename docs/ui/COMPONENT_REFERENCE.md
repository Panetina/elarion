# Elarion UI Component Reference

Date: 2026-07-10

Purpose: keep custom Elarion UI screens on one civic brown/gold component
language while preserving server authority and addon ownership.

This reference covers shared Core helpers that currently exist. It is not a
promise that every screen has already migrated.

## Rules

- Use shared helpers before adding local coordinate clusters.
- Keep render rectangles and hitboxes from the same layout object.
- Keep server-authored values server-authored. UI helpers must not calculate
  tax, price, permission, stock, vote outcome, rewards, or mutation validity.
- Native item tooltips belong only to native item/icon bounds, not the whole
  row.
- Extend helpers narrowly for a real repeated shape. Do not create a broad
  generic renderer that hides domain semantics.
- For new UI work, update this reference when a helper is added or its contract
  changes.

## `ElarionListRangeMarker`

Source:
`platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionListRangeMarker.java`

Intended use:

- Compact centered list/page range markers.
- Use when a bounded list has hidden rows above or below the viewport.

Semantic inputs:

- first visible row index
- visible row count
- total rows
- center X, Y, color

States:

- hidden when every row is visible
- tiny down/up arrows when previous/next rows exist
- clamped safely for empty and invalid counts

Current users:

- Government row lists through `GovernmentUiGlyphs.rowRange(...)`
- Grave Recovery grid header

Anti-patterns:

- Local `Rows ...` strings.
- Different arrow symbols per screen.
- Drawing the marker on top of row borders.

## `ElarionMoneySummary`

Source:
`platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionMoneySummary.java`

Intended use:

- Compact money/settlement cells: label, Sigil icon, amount.
- Use for Fee, Tax, Subtotal, Total, Payout, and similar summaries.

Semantic inputs:

- label
- amount
- emphasis flag
- icon size and style

States:

- neutral summary
- emphasized final value

Current users:

- NPC Trade selected-offer Subtotal, tax, Total/Payout cells

Anti-patterns:

- Placing the Sigil icon far away from its value.
- Computing tax/total/payout on the client.
- Replacing server-authored quote values with local UI math.

## `ElarionActionBandLayout`

Source:
`platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionActionBandLayout.java`

Intended use:

- Bounded action/footer geometry where controls and hitboxes must stay aligned.
- Current shape supports quantity, minus/plus/max, status, confirm, divider,
  and summary row positions.

Semantic inputs:

- panel rectangle
- label width
- button/value/max/confirm dimensions

States:

- enabled/disabled and tone remain owned by the caller
- status text remains owned by the caller
- server action dispatch remains owned by the caller

Current users:

- NPC Trade selected-offer quantity controls and Confirm action

Anti-patterns:

- Separate render and click coordinates.
- Floating action footers that cross parent frame borders.
- Turning this helper into a trade, bank, or portal domain widget.

## `ElarionSemanticRowLayout`

Source:
`platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionSemanticRowLayout.java`

Intended use:

- Shared compact row geometry for repeated row shapes.
- Current shapes:
  - item-price rows
  - record rows

Semantic inputs:

- row rectangle
- icon inset/size
- title and metadata offsets
- price or metric column offsets

States:

- selected/hovered/disabled visuals remain owned by the caller
- row payloads remain owned by the screen/domain
- native item tooltip bounds are explicit

Current users:

- NPC Trade catalog item-price rows
- Government shared record rows through `GovernmentUiComponents.recordRow(...)`

Anti-patterns:

- Using generic portrait art for non-portrait rows.
- Making the whole row show native item tooltip data.
- Moving row baselines with local `y + 3` or `y + 6` patches instead of the
  layout helper.
- Migrating all row types before one row shape is proven.

## `ElarionDetailCardLayout`

Source:
`platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionDetailCardLayout.java`

Intended use:

- Shared detail/dossier header geometry.
- Use where a framed icon or portrait sits beside title, tag, and subtitle or
  actor text.

Semantic inputs:

- header rectangle origin and width
- icon size
- icon-to-text gap
- title, tag, and subtitle offsets

States:

- drawing, icon style, tag tone, and text color remain owned by the caller
- body text and action buttons remain separate components
- domain payloads remain owned by the screen/domain

Current users:

- Government shared detail headers through `GovernmentUiComponents.detailHeader(...)`

Anti-patterns:

- Recomputing `icon size + gap` in each screen.
- Letting title, tag, and subtitle offsets drift separately.
- Treating this helper as a complete Government/Notification/Profile card
  renderer.

## `ElarionDetailBodyLayout`

Source:
`platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionDetailBodyLayout.java`

Intended use:

- Shared geometry for detail section titles, bounded body text, and simple
  key/value rows below a detail header.
- Use when a screen needs wrapped text or metadata rows to stay inside a fixed
  detail viewport.

Semantic inputs:

- section title origin, icon size, icon-to-text gap, and icon Y offset
- body rectangle
- key/value row width, label width, and label-to-value gap

States:

- text colors, icon choice, wrapping content, and visibility remain owned by
  the caller
- clipping and hitboxes should use the returned rectangles
- this helper does not format domain values

Current users:

- Government shared section titles and body text through
  `GovernmentUiComponents.sectionTitle(...)` and
  `GovernmentUiComponents.bodyText(...)`

Anti-patterns:

- Local section-title `x + icon + gap` math in every screen.
- Wrapped body text without a bounded viewport.
- Using key/value rows to duplicate domain formatting or authorization.

## `ElarionBadgeLayout`

Source:
`platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionBadgeLayout.java`

Intended use:

- Shared geometry for compact civic chips, tags, badges, and small state labels.
- Use when a caller already owns the semantic label and tone/color but needs
  consistent badge width, accent strip, top line, and text inset.

Semantic inputs:

- origin
- maximum width
- measured visible-text width

States:

- color, tone, active/disabled state, and label semantics remain owned by the
  caller
- text should be ellipsized with `textMaxWidth(...)` before layout width is
  computed

Current users:

- Core `ElarionCivicUi.statusChip(...)`
- Government shared tags through `GovernmentUiGlyphs.tag(...)`

Anti-patterns:

- Local badge width formulas.
- Different text insets for the same civic chip shape.
- Encoding domain states inside the layout helper.

## `ElarionProgressTrackLayout`

Source:
`platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionProgressTrackLayout.java`

Intended use:

- Shared geometry for bounded progress tracks and inner fill rectangles.
- Use when a caller already owns the progress value, total, label, colors, and
  domain meaning.

Semantic inputs:

- track origin
- track width and height
- normalized progress ratio

States:

- ratio is clamped to `0.0-1.0`
- fill is drawn inside the track border
- caller owns text, selected state, disabled state, and colors

Current users:

- Government vote-option tracks through `GovernmentUiGlyphs.progressRow(...)`

Anti-patterns:

- Putting vote, project, shrine, economy, or quest semantics in the layout
  helper.
- Local progress fill math that draws over the track border.
- Client-side calculation of server-authoritative progress totals.

## `ElarionEmptyStateLayout`

Source:
`platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionEmptyStateLayout.java`

Intended use:

- Shared geometry for compact bounded empty-state panels.
- Use when a list/detail area has no rows and needs a title plus wrapped body
  without an action footer.

Semantic inputs:

- panel origin and size
- current line height

States:

- caller owns empty-state condition, title, body copy, colors, and whether the
  panel is visible
- helper returns panel, title, and body rectangles only

Current users:

- Notification drawer empty categories

Anti-patterns:

- Empty states that expand the drawer or show unrelated action footers.
- Local title/body offsets that drift per category.
- Moving loading/error/empty conditions into the UI layout helper.

## `ElarionModalLayout`

Source:
`platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionModalLayout.java`

Intended use:

- Shared geometry for centered modal shells with body text, optional input
  field placement, and two footer buttons.
- Use when a caller already owns modal content, validation, permissions,
  suggestions, submit requests, and close behavior.

Semantic inputs:

- parent logical size
- modal size
- body inset/top/height
- input gap
- footer button dimensions and gap

States:

- helper returns geometry only
- caller owns title/body text, tone, button labels, enabled state, input value,
  validation, autocomplete, and network requests

Current users:

- Admin Panel action confirmation/input modal layout factory

Anti-patterns:

- Adding config apply semantics, validation, or suggestions to the layout
  helper.
- Recomputing centered modal/button positions in each screen.
- Sharing a modal layout while letting render and click hitboxes drift.

## `ElarionInputFieldLayout`

Source:
`platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionInputFieldLayout.java`

Intended use:

- Shared geometry for single-line civic input fields, including optional
  leading icon, text bounds, centered text baseline, and caret maximum.
- Use when a screen already owns the input model and needs render/caret
  rectangles to stay aligned.

Semantic inputs:

- field origin and size
- horizontal inset
- optional icon size and gap

States:

- caller owns focus, placeholder text, value, max length, validation,
  autocomplete, and submit behavior
- helper computes rectangles and text/caret positions only

Current users:

- NPC Bank amount input field

Anti-patterns:

- Local icon/text/caret math that drifts after visual polish.
- Moving text editing, validation, permissions, or network requests into the
  layout helper.
- Using the single-line helper for wrapped/multiline input.

## `ElarionItemSlotLayout`

Source:
`platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionItemSlotLayout.java`

Intended use:

- Shared geometry for square native item slots, centered item draw origins, and
  slot-only hover bounds.
- Use when a screen renders a Minecraft `ItemStack` or item-like preview and
  needs the draw position and tooltip hitbox to stay aligned.

Semantic inputs:

- slot origin and size
- native item inset
- optional grid index, column count, and gap

States:

- caller owns the item stack, count overlay, custom tooltip text, native
  tooltip call, enabled/disabled state, and server-authored visibility
- helper computes the slot rectangle and inner item rectangle only

Current users:

- Notification reward preview grid

Anti-patterns:

- Hovering the whole row or detail panel when only the item icon should show a
  native item tooltip.
- Local slot/item offsets that cause the icon and tooltip hitbox to drift.
- Moving reward contents, enchantment/lore visibility, storage lookup, or
  server-authored preview data into the layout helper.

## `ElarionScrollViewportLayout`

Source:
`platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionScrollViewportLayout.java`

Intended use:

- Shared geometry for bounded row viewports.
- Use when a screen renders a row list with a fixed row height/gap and needs
  render range, click hit testing, and scroll clamping to use the same math.

Semantic inputs:

- viewport origin and width
- available height
- row height and row gap
- item count
- preferred first visible row

States:

- helper returns visible capacity, clamped first row, visible row count,
  maximum first row, row Y positions, and row-only hit testing
- caller owns row contents, selection, keyboard behavior, scrollbar visuals,
  server paging, virtualization state, and network requests

Current users:

- NPC Trade catalog rows

Anti-patterns:

- Recomputing row click bounds separately from render row Y positions.
- Letting hover/click hitboxes include the gap between rows.
- Moving server-authored list data, purchase behavior, paging, or persistence
  into the layout helper.

## `ElarionTextViewportLayout`

Source:
`platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionTextViewportLayout.java`

Intended use:

- Shared geometry for bounded multiline text viewports.
- Use when a screen already has wrapped lines or an input model and needs
  visible-line range, line Y positions, caret-line visibility, and scroll hints
  to share the same math.

Semantic inputs:

- viewport origin and size
- line height
- wrapped line count
- preferred first visible line

States:

- helper returns visible line capacity, clamped first line, visible count,
  maximum first line, line Y positions, and visible/absolute line mapping
- caller owns text wrapping, text editing, focus, caret blinking, validation,
  submit behavior, and network requests

Current users:

- Character Creation biography text area

Anti-patterns:

- Recomputing max visible lines separately for scroll, render, caret, and
  scroll-hint logic.
- Moving text validation, keyboard editing, server-authored forms, or submit
  packets into the layout helper.
- Using fixed line counts that ignore configured font scale.

## `ElarionTooltipShellLayout`

Source:
`platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionTooltipShellLayout.java`

Intended use:

- Shared geometry for custom civic tooltip shells.
- Use when a screen needs Elarion-styled tooltip chrome instead of Minecraft's
  native item tooltip renderer.

Semantic inputs:

- mouse position
- screen size
- content size
- padding and cursor offset

States:

- helper returns screen-edge-aware shell bounds and padded content bounds
- caller owns tooltip text, line wrapping, iconography, colors, hover trigger,
  visibility rules, and server-authored data

Current users:

- NPC relationship hover hint

Anti-patterns:

- Replacing native item tooltips with custom shells when item components,
  enchantments, or lore should be rendered by Minecraft.
- Moving tooltip content, visibility, server schemas, or item inspection into
  the layout helper.
- Drawing custom tooltips without screen-edge clamping.

## `ElarionIconLabelLineLayout`

Source:
`platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionIconLabelLineLayout.java`

Intended use:

- Shared geometry for compact icon + label/value lines.
- Use when a screen needs a short label, a small icon, and a short value to
  stay aligned as one line.

Semantic inputs:

- line origin
- label width
- icon size
- label-to-icon and icon-to-value gaps

States:

- helper returns label position, icon rectangle, and value position
- caller owns label text, value text, icon drawing, colors, formatting,
  server-authored values, and interaction behavior

Current users:

- NPC Bank Fee/Total currency pairs

Anti-patterns:

- Moving money/tax formatting, profile data, config values, or domain labels
  into the layout helper.
- Local `label width + icon gap + value gap` arithmetic for the same compact
  line shape.
- Using this helper for multi-line facts or rows that need selection/hover
  bounds.

## `ElarionStatusLineLayout`

Source:
`platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionStatusLineLayout.java`

Intended use:

- Shared geometry for bounded one-line status and feedback messages.
- Use when a screen needs server-authored success/error/help text to share a
  stable text origin and maximum width.

Semantic inputs:

- line origin
- status area width and height
- rendered line height

States:

- helper returns the status bounds, text position, and text maximum width
- caller owns message text, ellipsizing/wrapping decision, color, severity,
  visibility, server-authored status meaning, and interaction behavior

Current users:

- NPC Bank invalid quote messages
- NPC Bank dialogue feedback messages

Anti-patterns:

- Moving status semantics, notification events, validation logic, packets, or
  domain-specific wording into the layout helper.
- Scattering fixed status text coordinates where the same one-line shape is
  reused.
- Using this helper for multi-line detail bodies or clickable action bands.

## `ElarionSectionHeaderLayout`

Source:
`platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionSectionHeaderLayout.java`

Intended use:

- Shared geometry for compact centered section headers with an icon, title, and
  divider.
- Use when a contained panel needs a stable icon/title/divider header without
  becoming a full screen chrome/header.

Semantic inputs:

- section bounds origin and size
- icon inset, offset, and size
- title vertical offset
- divider inset and vertical offset

States:

- helper returns section bounds, icon rectangle, title center point, title Y,
  and divider rectangle
- caller owns title text, icon id, colors, shell drawing, labels, validation,
  server-authored data, and interaction behavior

Current users:

- Character Creation `IDENTITY` panel header
- Character Creation `BIOGRAPHY` panel header

Anti-patterns:

- Moving onboarding state, validation, packets, or identity semantics into the
  layout helper.
- Using this helper for full screen headers, modal headers, or left-aligned
  `headerShell` panels with different geometry.
- Reintroducing local icon/title/divider coordinates for the same centered
  panel-header shape.

## `ElarionPanelHeaderLayout`

Source:
`platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionPanelHeaderLayout.java`

Intended use:

- Shared geometry for left-aligned `headerShell` panels with a title, divider,
  and content body start.
- Use when a contained panel needs stable title/divider placement without
  becoming a full screen chrome/header.

Semantic inputs:

- panel bounds origin and size
- header height
- title inset and vertical offset
- divider inset and vertical offset

States:

- helper returns panel bounds, header height, title position and maximum width,
  divider rectangle, and body start Y
- caller owns title text, colors, shell drawing, rows, domain values,
  validation, scrolling, and interaction behavior

Current users:

- NPC Bank amount panel
- NPC Trade catalog panel

Anti-patterns:

- Moving tax, quote, stock, trade, validation, packet, or persistence behavior
  into the layout helper.
- Forcing centered section headers, modal headers, or service-screen
  portrait/title headers through this left-title panel helper.
- Reintroducing local left-title/divider coordinates for panels that share this
  shape.

## `ElarionServiceHeaderLayout`

Source:
`platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionServiceHeaderLayout.java`

Intended use:

- Shared geometry for service-screen headers with a portrait, large title,
  subtitle, optional currency badge, and close button.
- Use when a service screen needs the same header structure while keeping
  domain data and interactions owned by the screen/service.

Semantic inputs:

- screen/header bounds
- padding
- portrait offset and size
- title/subtitle offsets
- close size
- badge width, right gap, and vertical offset

States:

- helper returns header bounds, portrait rectangle, title position/max width,
  subtitle Y, close rectangle, and badge rectangle
- caller owns title text, subtitle text, portrait id, currency values, close
  behavior, colors, packets, and service availability

Current users:

- NPC Bank header
- NPC Trade header

Anti-patterns:

- Moving NPC identity, relationship, bank/trade balance, service availability,
  permission, packet, or persistence behavior into the layout helper.
- Forcing relationship-heart NPC dialogue headers through this helper before
  their distinct geometry is audited.
- Reintroducing local portrait/title/badge/close coordinates for service
  screens that share this shape.

## `ElarionPairedButtonLayout`

Source:
`platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionPairedButtonLayout.java`

Intended use:

- Shared geometry for paired service mode/action buttons.
- Use when two buttons must share one row and render/click hitboxes must stay
  identical.

Semantic inputs:

- row origin
- left width
- right width
- gap
- height

States:

- helper returns left button rectangle, right button rectangle, gap, and
  combined bounds
- caller owns labels, roles, selected state, hover state, enabled state,
  colors/tones, packets, and behavior

Current users:

- NPC Bank Deposit/Withdraw row
- NPC Trade Buy/Sell row

Anti-patterns:

- Moving service mode state, action role selection, validation, permissions,
  packet sends, or persistence into the layout helper.
- Using separate hard-coded rectangles for render and click handling.
- Forcing multi-button action bands or tab bars through this helper before
  their different geometry is audited.

## `ElarionFooterActionLayout`

Source:
`platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionFooterActionLayout.java`

Intended use:

- Shared geometry for a single footer action button.
- Use when a footer/back/action button needs one shared render/click rectangle.

Semantic inputs:

- button origin
- button width
- button height

States:

- helper returns the button rectangle
- caller owns labels, roles, hover state, enabled state, button tone, packets,
  fallback behavior, and permissions

Current users:

- NPC Bank `Back to Conversation`
- NPC Trade `Back to Conversation`

Anti-patterns:

- Moving back-navigation, close fallback, permissions, packet sends, or service
  state into the layout helper.
- Using separate hard-coded footer rectangles for render and click handling.
- Forcing multi-button footers through this single-button helper before their
  different geometry is audited.

## `ElarionPresetButtonRowLayout`

Source:
`platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionPresetButtonRowLayout.java`

Intended use:

- Shared geometry for compact preset/action button rows.
- Use when equal-width preset buttons and an optional confirm button must share
  one render/click geometry source.

Semantic inputs:

- row origin
- preset button width and height
- preset gap and count
- optional confirm gap and confirm width

States:

- helper returns preset button rectangles, row bounds, hit index lookup, and
  optional confirm rectangle
- caller owns labels, preset values, enabled state, clicked action, validation,
  packets, and behavior

Current users:

- NPC Bank amount preset buttons
- NPC Bank Confirm action

Anti-patterns:

- Moving amount values, validation, quote requests, submit behavior, or
  permissions into the layout helper.
- Using separate hard-coded preset rectangles for render and click handling.

## `ElarionSplitSummaryLayout`

Source:
`platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionSplitSummaryLayout.java`

Intended use:

- Shared geometry for a divider plus left/right summary positions.
- Use when a compact panel has two summary cells beneath a divider.

Semantic inputs:

- divider origin and width
- summary Y
- right summary X

States:

- helper returns divider rectangle plus left and right summary origins
- caller owns labels, values, colors, formatting, validation, and behavior

Current users:

- NPC Bank Fee/Total summary row

Anti-patterns:

- Moving money formatting, tax calculation, quote validity, or transaction
  behavior into the layout helper.
- Using this helper for dense tables or multi-row summaries before their
  geometry is audited.

## Development Gallery

In a development client only, type:

```text
/elarion-ui-gallery
```

This opens a static screen that renders the current shared helper shapes. The
command is intercepted client-side and is not a server command, not a production
feature, and not a player-facing UI surface.

The gallery currently includes compact references for list ranges, money
summaries, action bands, semantic rows, detail headers/body text, service
headers, paired service buttons, compact preset/confirm rows, split summaries,
and footer actions. When a new reusable layout helper is added, update this
gallery in the same slice so future UI work can compare geometry without
opening every gameplay screen.
