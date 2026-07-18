# UI Journal

Purpose: preserve concrete visual direction for Elarion screens so future UI
work can converge instead of drifting between one-off styles.

## Current UI Inventory

The current cross-screen status and manual QA order lives in
`docs/reports/UI_FAMILY_INVENTORY.md`. Use that report before starting another
visual polish slice so accepted screens are not reworked unnecessarily.

## Government Reference Set

The current canonical Government UI references are saved under
`docs/ui/government/`.

- `CF-01-civic-forum-states.png`: Civic Forum current votes, founding vote,
  proposals, and create-proposal modal.
- `SR-01-seat-of-rule-states.png`: Seat of Rule review, records, offices, and
  finalize-record modal.
- `IF-01-government-inner-flows.png`: inner flows for Realm identity, Realm
  color, government form, leader election, citizen proposal, and official text.

These three images are implementation targets for Government UI polish. Older
generated variants are not canonical unless explicitly promoted later.

Government scrollable row panels use one compact centered range marker when
their content overflows: a tiny down arrow, `Rows first-last / total`, and a
matching tiny up arrow. Use `GovernmentUiGlyphs.rowRange(...)`; do not restore
right-aligned bare numeric ranges in Civic Forum or Seat of Rule.

## Visual Rules

- Root surfaces should be warm dark brown or charcoal-brown, not flat gray.
- Header metadata must be inside framed cards. Avoid floating labels without
  a visible background.
- Use gold for normal borders, dividers, and tab frames.
- Use the active-mount green family for selected rows, selected votes, and
  primary approve/submit actions.
- Use warm red for reject/cancel/destructive actions.
- Keep icons or placeholder glyphs directly beside labels with consistent
  padding.
- Prefer texture assets for semantic icons over code-drawn glyphs. Code drawing
  is acceptable for simple dividers, bars, checkmarks, and tiny state marks, but
  primary tab/category/accessory icons should live as PNG assets so NPC, Portal,
  Notification, Government, and future screens can share a consistent pixel-art
  language.
- Government UI icons are texture assets, not ad hoc scaled drawings. Use exact
  `16 x 16` PNGs under
  `assets/elarion_government/textures/gui/icons/{icon_id}.png`; color swatches
  use the exact Realm color id as the filename, for example `dark_green.png`.
- Keep row text short and clipped inside bounds. Do not let bars, buttons, or
  tags cross panel borders.

## Notification Reference Contract

The notification drawer follows the same compact civic UI language as the
reference mockup: one attached dark-brown and gold shell, a left category rail,
a centered `NOTIFICATIONS` header with small ornamentation, compact rows, and a
bounded contextual action band at the bottom.

- Keep Personal, Realm, Quest, and World category icons as the rail selectors.
  Mail and Realm keep their read/unread assets; Quest and World keep distinct
  `32 x 32` texture assets with unread variants.
- Row selection must not expand the row. It marks the entry read, applies a
  neutral brown selected fill, keeps the semantic left accent, and shows actions
  in the footer.
- Do not use a large green selected block for notification rows. Reserve green
  for positive/claim/accept actions or explicit success state.
- The footer action band is local to the selected entry. It shows the local
  `View` command plus server-authored actions, up to four per row, wrapping into
  a second bounded row when needed.
- Detail view stays inside the same drawer and contains a back arrow, larger
  icon, title, status, wrapped body, reward preview, and server actions. Long
  text scrolls inside the detail viewport.
- Empty categories are content-bounded and do not render an empty action
  footer.
- Portal Nether/End route slots remain HUD accessories below the notification
  categories, not extra notification categories. Their route icons are also
  `32 x 32` texture assets and should not be redrawn in Java.

## Shared Civic UI Contract

Use this contract when remaking NPC, Portal, Shrine, Notification, Admin, and
other Elarion screens so they converge with the Civic Forum/Seat of Rule visual
direction instead of becoming separate themes.

Implementation note: the Phase 4 UI audit is tracked in
`docs/reports/UI_SYSTEM_AUDIT.md`. Generic civic colors, row/action surfaces,
thin attached shells, dividers, status chips, and scaled control metrics now
belong to Core UI helpers: `ElarionCivicColors`, `ElarionCivicUi`, and
`ElarionUiMetrics`. Government-specific semantic components may remain in the
Government addon, but future screens should not copy Government helper classes
or local Notification/Admin color constants.

- One attached shell: compose a compact framed header and framed body as joined
  surfaces. Avoid floating disconnected panels unless the reference shows a
  modal or repeated item card.
- Header first: put title, close button, and compact metadata in the header.
  Body sections should start below clear gold dividers, not with oversized
  explanatory text.
- Brown/gold baseline: root black-brown, card brown, subdued gold borders, warm
  bevel shadow, active green for selected/positive states, and warm red for
  destructive/reject states.
- Pixel assets: use fixed-source PNG icons for primary tabs, row types, route
  states, NPC roles, and large detail headers. Keep procedural glyphs for tiny
  generic UI marks only.
- Compact rows: reserve bounded zones for icon, title, tag/status, right metric,
  and optional secondary metric. Long titles ellipsize before the metric; time
  labels are right-aligned or moved to a separate line, never hard-coded at a
  fixed offset.
- Detail panels: start with icon/title/tag/actor or source, then wrapped body,
  then actions/status. Do not expand list rows into full detail cards.
- Action bands: keep contextual actions grouped at the bottom or in a dedicated
  detail section. Use short labels with familiar icons and keep disabled actions
  visible but muted when the server sends them.
- Empty states: draw a bounded message panel that still aligns with the active
  rail/tab pointer. Empty content should not leave a pointer aimed at blank
  background.
- Font scaling: all Elarion-authored text uses Core typography helpers; hitboxes
  and clipping must use the same scaled metrics as rendering.

For compact civic-style screens, prefer an integrated header: crest, title,
then inline metadata with small glyphs and thin vertical separators. Avoid
three or more large header rectangles when the reference uses one continuous
bar. Grid texture should be subdued and secondary; rows and detail panels must
carry the hierarchy, not the background pattern.

For the latest Civic reference pass, the top-left crest is rendered naturally
with no square frame or artificial background. The root surface is two attached
rectangles: a compact framed header glued to a framed body at a shared seam.
Use thin 1 px borders with a dark bevel shadow; avoid thick nested frames unless
the reference shows a framed card or icon slot.

## Conversation-First NPC Services

2026-07-08: Bankers and future service NPCs should not replace the normal NPC
conversation screen. Every NPC opens through the compact civic conversation
surface first. Service access appears as a normal server-authored choice such
as `Open Bank`, `Trade`, or a quest entry. Selecting that choice may move the
same validated session into a dedicated presentation screen.

- Bank presentation uses a separate compact service window with NPC portrait,
  deposited balance, Deposit/Withdraw modes, bounded amount input, presets,
  fee/total summary, authoritative feedback, and Back to Conversation.
- Trader presentation uses the approved compact Buy/Sell reference
  `docs/ui/revamp-option-a/09-npc-trade-screen-v2.png` as a dedicated shell;
  do not implement shop controls inside the generic dialogue screen. The first
  implementation is intentionally non-mutating and shows a pending-catalog
  state until a trade owner provides stock, price, inventory, and mutation
  contracts.
- Service screens may show only data owned by the relevant system. Economy
  owns wallet/bank state; NPCs own dialogue/session/presentation; future trade
  systems own stock/prices.
- Aggregate NPC/Realm reputation belongs in Character Menu. NPC screens may
  show only the relationship with the currently interacted NPC after NPC-owned
  relationship data exists.
- Dialogue typewriter behavior is server-authored by the NPC UI config and
  synchronized in the dialogue open payload. Screens must respect
  `typing-click-completes`; live QA automation should either wait for the
  dialogue to reach input state or explicitly test the configured click-to-skip
  behavior instead of assuming every option click submits immediately.

## Government Measured Contract

Use these measurements when bringing Civic Forum, Seat of Rule, or later civic
screens closer to the reference images.

- Logical frame: `760 x 500`, centered through `ElarionScaledLayout`.
- Minimum readable scale: `75%`. Below that, render a themed fallback instead
  of shrinking Government text into unreadable pixels.
- Header hierarchy: outer root frame, compact top header, three framed metadata
  items, tab row, two main panels, bottom status band.
- Attached shell hierarchy: Civic Forum uses a header panel of about `50px`,
  with the body panel starting on the same seam. Header metadata is inline,
  separated by short gold dividers, not placed in large standalone boxes.
- Corners: square pixel bevels only. The apparent rounding is a 1-2 px stepped
  highlight/shadow illusion, not actual rounded corners.
- Borders: 1 px dark bevel outside, 1 px gold or green state border inside.
  Selected vote/active states replace gold with active green.
- Row density: current-vote rows remain compact enough for founding decisions
  plus several active/recent rows; module/review rows use taller cards with
  icon, title, colored tag, actor/summary, count, and state/timer.
- Small row/header icons render naturally without boxed square frames. Reserve
  framed thumbnails for large detail cards or content previews only.
- Government icon files are exact `16x16` textures and must always be sampled
  from a fixed `16x16` source region when displayed larger. Sampling the
  display size repeats texture pixels.
- Semantic Government icons use distinct silhouettes; automated tests reject
  repeated placeholder artwork across tabs and actions.
- Civic post-founding tabs are `Current Votes`, `Proposals`, `Laws`,
  `Projects`, `Offices`, and `History`. Seat tabs are `Review`, `Laws`,
  `Projects`, `Offices`, and `Archive`.
- During unfinished founding, keep the post-founding Civic tabs visible but
  disabled. Route stale clicks back to Current Votes and show a compact Civic
  notice in the bottom band naming the missing requirement.
- Current Votes contains only real active votes and 24h recent outcomes after
  founding. Do not place module links, category navigation rows, or
  `Founding Complete` placeholders there.
- Pending proposals and office rows are informational. Draw Yes/No controls
  only for citizen-ratification proposal rows, and draw thresholds as progress
  summaries rather than selectable choices.
- Selected rows use a neutral dark fill with a thin green border/left rail.
  Avoid a full green wash over the row.
- Header identity: one composed label such as `Republic of Oak`, followed by a
  server-authored primary authority summary such as `President Biggus
  Testerus`, then role and Realm color inline metadata. Reserve a compact but
  complete color slot so names such as `Dark Green` are not clipped. Do not
  split identity into `Realm of Oak` plus a separate form label.
- Civic Forum and Seat of Rule share the same Government chrome contract:
  compact `46px` attached header, four equal-width metadata cells, and tabs
  spanning exactly across the combined left/right content panel width. Do not
  hand-size Seat tabs or header cards separately from Civic.
- Two-line vote rows reserve separate bounded areas for title, category tag,
  and right-aligned metrics. Tags are clipped inside the row; active rows show
  vote count and timer/state, while recent rows show only the final outcome.
  No text may touch or cross the row frame.
- Category/status tags are quiet scan markers, not primary buttons: keep them
  short, `10-12px` high, and use a small color accent instead of a large boxed
  badge.
- Scrollable lists reserve a footer strip for the visible range. Pagination
  text must never overlap the final row.
- Empty choice panels must still render their server-authored primary action.
  This is required for first nominations and other workflows where an action
  creates the first row.
- Modal hierarchy: dim root, centered bordered card, title/icon row, input
  fields, category/status line, Cancel and Submit/Publish buttons.

Palette roles extracted from the references:

- Root black-brown: `0xFF0C0805`.
- Card brown: `0xFF120C07`.
- Hover/raised brown: `0xFF26190D`.
- Gold border/highlight: existing Elarion theme border/title gold.
- Dark bevel: warm dark brown shadow, never neutral gray.
- Active green: active-mount green family for selected rows, selected votes,
  approve/submit actions, and settled states.
- Reject red: warm red for reject/cancel/destructive state.
- Muted text: subdued tan/gray-brown only for secondary descriptions.

## Government Three-Pass QA

When touching the Government screens, repeat this loop before handoff:

1. Structure pass: compare live Civic and Seat screenshots against `CF-01` and
   `SR-01` for header height, tab placement, panel bounds, row density, bottom
   band, and modal bounds.
2. Color/component pass: compare root fill, row fill, border warmth, selected
   green, reject red, tags, vote bars, buttons, and icon placement. Remove
   flat gray areas.
3. Interaction/resize pass: verify Civic tabs, founding options, proposal
   modal, leader nomination, Seat review/finalize actions, hidden scrolling,
   and the 75% readable-size fallback.

## Live Screenshot Capture

For live UI QA on the Windows development workstation:

```text
.\gradlew.bat runServer
.\gradlew.bat runClientOne
powershell -NoProfile -ExecutionPolicy Bypass -File .\dev\tools\capture-minecraft-window.ps1 -Output build\ui-qa\<screen-name>.png
```

Open the target Elarion screen on the live dev server before capturing. The
normal test route is `runServer`, `runClientOne`, Multiplayer, saved
`localhost` server. The capture script uses the Minecraft window handle and
`PrintWindow`, so other overlapping desktop windows should not pollute the PNG.
Use `-ScreenCapture` only as a fallback when `PrintWindow` returns a black
image on a driver setup.

Each UI polish slice should save the current live capture under `build/ui-qa/`
and inspect it before making visual corrections. Commit only source and docs;
`build/ui-qa/` remains disposable local evidence.

## Civic Forum Reference Pass V3

The Civic Forum implementation should use these V3 renderer rules before
adding more Government features:

- Keep the logical frame at `760 x 500`, but use a compact `46px` header and
  attached body seam. The header is not a hero/banner; it is a thin civic
  status strip.
- Header metadata remains inline: composed Realm identity, primary authority
  holder, citizen role, and compact Realm color swatch. Long text ellipsizes
  inside its slot; `Dark Green` must fit without crossing the close button.
- Six Civic tabs are supported: `Current Votes`, `Proposals`, `Laws`,
  `Projects`, `Offices`, and `History`. Fit them with tighter widths and gaps
  instead of dropping `Projects`.
- Current-vote rows reserve fixed internal zones: icon, title, category tag,
  right-aligned count/state, and optional timer. Tags and metrics must never
  share the same pixels.
- Recent rows show only the final outcome in the right metric zone. Do not use
  placeholder labels such as `Recent res...`.
- Use neutral selected fills with a thin green rail/border. Do not paint the
  whole selected row bright green.
- Vote progress rows draw a dark track plus colored fill directly. Do not
  layer progress bars on top of beveled boxes, because that creates the yellow
  under-fill artifact seen in earlier screenshots.
- Small row and header icons render as natural `16x16` textures. Framed icon
  slots are reserved for large detail thumbnails and color/choice cards where
  the reference shows a card-like preview.

## Seat Of Rule Reference Pass V1

Seat of Rule follows the same Government chrome as Civic Forum. Its top tabs
are the only navigation; the Review tab must never render internal module
shortcut rows such as `Review`, `Laws`, `Projects`, `Offices`, or `Archive`.
The default Review content is the bounded authority-review proposal list, or a
clean empty state when there is no pending work.

Seat detail panels use the same compact tags, row selection, and progress bars
as Civic. Approve/reject choices may have checkboxes only when clickable;
threshold and decision-state summaries are informational rows on a dark track
with no gold under-fill. Actor text in Seat and Civic is player-facing through
Core identity resolution and must not display raw UUIDs.

## Government Shared Component Pass V4

Civic Forum and Seat of Rule share Government-specific UI components for
record rows, detail headers, vote option rows, timer blocks, and section
titles. Future Government work should extend those components before adding
screen-local drawing logic.

- Record rows reserve fixed zones for icon, title, compact tag, right metric,
  and optional secondary metric. Office rows use tenure/seats, not vote labels.
- Detail panels start with the same large icon/title/tag/actor block, then body
  text, a thin divider, and a section-specific progress/details area.
- Vote option rows use `29px` dark framed cards with optional checkboxes,
  colored fill only on a dark track, and right-aligned counts/percentages.
- Tags are secondary scan markers: `10px` high, translucent fill, narrow left
  accent, and clipped text. They must not visually compete with row titles.
- Government semantic icons remain exact `16x16` PNGs. The core set now has
  distinct silhouettes for proposals, laws, offices, people, timer, history,
  archive, project, notice, approve, reject, and current votes.

## Government Pixel-Art Pass V5

Government icons now use hand-authored `16x16` matrix artwork exported by
`dev/tools/generate_government_icons.py`. The script is an export/validation
tool only: icon shapes are explicit pixel rows with a fixed Government palette,
not procedurally invented rectangles.

- Pixel-art rules: readable silhouette first, then base material color, dark
  outline/shadow, and 1-pixel highlight polish.
- Core civic icons use warm gold/parchment with dark brown outlines; projects
  use blue accents, approvals use green, rejects use red, and notices use
  purple.
- Realm color UI uses the actual chosen/proposed color swatch as the visible
  indicator. The generic `realm_color` icon is only a fallback concept marker,
  not the normal color display.
- The top-left Government crest must render naturally with transparent
  outside pixels; do not place it in a generic square frame.
- Contact sheets generated under `build/tmp/government-icons-contact-v*.png`
  are visual QA artifacts only and are not committed.
- Tests assert icon dimensions, unique semantic artwork, and enough visual
  pixel mass/palette depth to prevent a return to thin placeholder glyphs.

## Government Naming

Government screens show one composed civic identity:

- `Republic of Oak`
- `Monarchy of Oak`
- `Confederation of Oak`
- `Theocracy of Oak`

Do not show redundant separate header text such as `Realm of Oak` plus
`Republic`. If Core or Government provides `Realm of Oak`, strip the `Realm of`
prefix before composing the display label.

## Tag Palette

Use colored tags as quick scanning anchors:

- Citizen Proposal / Active / Settled: green.
- Law / Rule / Economy / Pending: gold.
- Security / Reject / Rejected: red.
- Infrastructure / Project / Government Proposal: blue.
- Culture / Notice / Faith: purple.
- Locked / Waiting: muted gray-brown.
- Unknown or generic categories: teal.

## Texture Icons

Government icon ids resolve through `GovernmentUiIcons`. Normal Government UI
must use the texture resolver first and keep procedural glyphs only as the
unknown-id fallback. The first required icon set is:

- `civic_crest`, `realm_name`, `realm_color`, `government_form`,
  `leader_election`, `people`, `current_votes`, `timer`, `settled`, `reject`.
- `proposal`, `law`, `office`, `history`, `archive`, `project`, `notice`.
- Realm color swatches: `dark_red`, `red`, `gold`, `yellow`, `dark_green`,
  `green`, `aqua`, `dark_aqua`, `dark_blue`, `blue`, `light_purple`,
  `dark_purple`, `white`, `gray`, `dark_gray`, `black`.

Until final custom art exists, use reusable Government texture placeholders:

- Realm/name proposal: crest or scroll.
- Realm color: banner/swatch.
- Government form: pillar.
- Leader/player election: crown or people.
- Proposal/law/record: scroll or book.
- Office: crown or pillar.

Do not block UI implementation on custom art. Replace placeholders later through
the icon mapping without changing screen layout.

## Migration Checklist

When updating an older Elarion screen:

1. Save or identify a reference image before code changes.
2. Replace gray root panels with warm themed surfaces.
3. Convert floating metadata into framed cards.
4. Use tags for record type/status/category.
5. Keep all panels within the logical layout bounds.
6. Add a layout test for any new fixed panel geometry.
7. Capture a live screenshot and compare it against the reference image before
   handoff.

## Live QA Pass - 2026-07-06

The first server-backed screenshot pass used `runServer`, `runClientOne`, the
saved `localhost` entry, and `dev/tools/capture-minecraft-window.ps1`.

- Realm Notifications: empty-state shell, selected rail, header ornaments, and
  close button rendered without overlap. Populated rows/actions/detail remain
  unverified.
- Civic Forum: History and Create Civic Proposal rendered at a maximized
  `1936 x 1048` window. At `870 x 519`, Civic correctly used its bounded
  too-small fallback instead of clipping controls.
- Shrine: the shell rendered, but the snapshot was internally inconsistent:
  `Complete` coexisted with zero progress and zero requirement totals.
- Neutral Gate: the allowed confirmation shell and buttons rendered, but its
  32px gate icon frame was empty.
- No custom-payload decode or disconnect error appeared in client/server logs.

Evidence remains disposable under `build/ui-qa/requested-*.png`. Promote only
approved comparison images into `docs/ui/`.

## 2026-07-07 - Option A Civic Ledger Direction Approved

The user selected the first generated direction, Option A, as the visual basis
for custom screens that still need a full redesign. The indexed reference set
is `docs/ui/revamp-option-a/README.md` and contains detailed `1536x1024` boards
for the future player hub, onboarding, Shrine, NPC roles, trade, Portal
pop-ups, Grave Recovery, Admin Panel, and generic event feedback.

Behavioral decisions captured with the art:

- Generalize Collection into Profile plus Unlockables; `Character Menu` is a
  candidate name, not yet a compatibility-breaking rename.
- Character creation precedes server-enforced population-balanced Realm
  placement.
- Portals use three compact pop-up families and do not gain a menu.
- Grave Recovery remains a separate menu.
- Quest NPC, banker, trader entry, and trade screens are intentionally
  distinct.
- Generic event pop-ups project existing authoritative events/notifications
  and do not create another inbox.

The boards are composition references. Implementation must rebuild their
language with shared civic primitives, real assets, semantic presentation
models, font scaling, bounded scrolling, matching hitboxes, and live
screenshot QA.

### Simplification correction

After reviewing the first detailed boards, the Shrine, quest NPC, and banker
references were replaced:

- Shrine preserves the current live layout and receives art/control polish
  only.
- Quest NPC is a compact conversation window without history, relationship,
  biography, schedule, location, or session dashboards.
- Banker is a compact Deposit/Withdraw window without treasury, account,
  security, or administrative sidebars.

Future implementation must treat those exclusions as scope boundaries rather
than omitted details to restore.

## Admin Panel Payload QA - 2026-07-06

After bounding Admin Panel snapshots and scoping Config rows, the patched
client/server opened `/e panel`, clicked Config, clicked `Core: UI Theme`, and
opened the Players tab without a custom-payload disconnect.

- `build/ui-qa/admin-config-overview-fixed.png`: patched Admin Panel overview.
- `build/ui-qa/admin-config-tab-fixed.png`: Config tab domain/category summary
  rows only.
- `build/ui-qa/admin-config-category-fixed.png`: scoped UI Theme category entry
  rows loaded after selecting the category.
- `build/ui-qa/admin-players-set-realm-fixed.png`: Players tab includes the
  Core-owned `Set Realm` action.
- `build/ui-qa/admin-set-realm-tab-complete.png`: Set Realm modal shows
  server-authored suggestions and Tab-completes a Realm id.

Use `dev/tools/minecraft-qa.ps1` for repeat checks that need command sending,
client-area clicks, and screenshots.

## Admin Config Apply QA - 2026-07-06

The live Config editor now has verified behavior for both editable and
read-only targets:

- `font-apply-fixed-applied-125.png`: font scale applied from `100` to `125`
  and the Admin Panel reflowed using the synchronized typography metrics.
- `font-apply-restored-100.png`: the same path restored the dev config to
  `100`.
- `config-non-applier-apply-disabled.png`: Logical Width displayed the
  no-applier reason and ignored Apply.
- `admin-players-mount-actions.png`: mouse-wheel QA reached provider-contributed
  Mount actions in the bounded player action list.
- `admin-grant-mount-tab-complete.png`: Grant Mount Tab-completed `airship`
  from Mount-owned suggestions.

The QA helper now supports `-Action scroll` with `-Wheel` and `-Count`.
Screenshots remain under `build/ui-qa/` until an approved reference comparison
is promoted into `docs/ui/`.

## Populated Notification QA - 2026-07-06

The populated drawer was exercised through real `runServer`/`runClientOne`
snapshots. Existing Realm mail supplied mixed read/unread rows, and
`/e realm reward realm1 council_hall_blessing` supplied a claimable Personal
reward with currency, item counts, and an enchanted sword preview.

- `notification-realm-selected-unread.png`: compact Realm rows, visible
  selection, unread marker, neutral View, and destructive Dismiss.
- `notification-personal-selected-patched.png`: Personal reward row with
  neutral View and green Claim.
- `notification-reward-detail-patched.png`: content-bounded reward detail.
- `notification-reward-tooltip-patched.png`: server-authored `Sharpness III`
  tooltip rendered above the reward slot.
- `notification-world-empty-final.png`: content-bounded World empty state with
  no pointer leading into empty drawer space.

Short details now fit their content and long details keep the existing bounded
scroll viewport. Empty lower categories hide only the pointer that cannot meet
the drawer; the selected rail slot remains green. The QA helper preserves
maximization and supports `-Action move -X <client-x> -Y <client-y>` for native
cursor tooltip checks.

## Collection Baseline QA - 2026-07-07

The current Collection menu was captured live before Character Menu shell work.
Evidence is local and disposable under `build/ui-qa/slice-17c/`.

- `11-collection-mounts.png`: Mounts tab, Airship selected, live model preview
  visible.
- `12-collection-mount-active.png`: Set Active refreshed the server-authored
  row/detail state.
- `13-collection-mount-scroll.png`: hidden mount-list scrolling reached later
  mount rows without losing the selected detail.
- `14-collection-pets.png`: empty Pets tab renders.
- `15-collection-titles.png`: Titles tab selection/detail renders.

No Elarion Collection/custom-payload encode/decode crash occurred. This proves
the current unlockables flow is stable enough to preserve as a behavioral
baseline, not that the visuals are final. Future Character Menu work should
replace the sparse old shell/detail language with the approved Option A
Profile/Unlockables boards while preserving server-authoritative actions.

Live QA note: if `runClientOne` starts with a tiny/white framebuffer or blank
main menu, focus the Minecraft window and toggle `F11` once before restarting
or changing shader state.

## Option A Runtime Asset Bank

The approved Option A art direction needs a curated runtime asset bank, not
untracked concept-board crops. Track the planned asset families, sizes, and
promotion rules in `docs/ui/revamp-option-a/ASSET_PLAN.md`. Generate assets in
screen-family batches and promote final PNGs into module resources only when a
slice consumes them.

## Character Onboarding Option A Migration - 2026-07-07

Character Creation and Realm Assignment now use the approved Option A civic
onboarding language while preserving the same server-authoritative flow.
Character Creation renders a step strip, identity preview, bounded biography
panel, readiness row, and bottom action band. Realm Assignment renders an
assigned Realm summary, up to three vertical Realm rows, assigned-row
highlight, server-provided population values, and a bottom confirmation band.

No payload shape, lifecycle storage, cooldown rule, validation rule, or Realm
assignment algorithm changed in this pass.

Final live QA on 2026-07-07 joined the saved localhost server, reset the test
character through supported commands, and captured both screens under
`build/ui-qa/slice-17n-onboarding-live-qa-5-footer-centered/`. Both action
buttons are vertically centered inside their footer bands with balanced logical
spacing, labels receive the shared one-pixel optical correction, and draw/hit
bounds match. `Confirm Placement` closed successfully. Character/citizen state
was restored byte-for-byte from the pre-QA backup and all QA processes stopped.

## Shrine And Offerings Civic Reskin - 2026-07-07

`ShrineOfFoundationScreen` now follows the approved Option A Shrine reference
without changing its information architecture or server contract. The stable
`SHRINE OF FOUNDATION` title, ornamental header, bordered progress band,
green-bordered selected tab, compact requirement/history rows, framed project
summary, bounded reward grid, civic contribution modal, and footer Close action
all reuse Core civic primitives.

The project title remains server-authored and is displayed in the summary, so
quest-projected memorial names still work. Reward items retain native Minecraft
tooltips, including enchantments. Live evidence under
`build/ui-qa/slice-17o-shrine-civic-reskin/` covers completed and incomplete
states, all six configured reward slots, an enchanted reward tooltip, History,
and the amount modal. The temporary reset used for incomplete-state QA was
restored with matching hashes and no gameplay state was retained.

## Shared Runtime Icon Catalog - 2026-07-07

The Core runtime icon catalog now maps semantic ids to the curated art library
under `platform/core/src/main/resources/assets/elarion_core/textures/gui/library/`.
Government, notifications, Character Menu/profile, character onboarding, Realm
assignment, Shrine, Portal confirmation, and Portal HUD route slots should use
`ElarionUiIcons` instead of one-off placeholder textures.

Mail and Realm notification rail read/unread assets are intentionally preserved.
Rows, details, action glyphs, profile panels, tabs, Shrine summary/tabs, and
Portal ticket/status art now prefer semantic library icons. Free/neutral Portal
confirmation prompts do not draw the payment/item slot; ticketed and fee
prompts keep the framed slot and render ticket/currency/gate art.

No live screenshot QA was run for this pass at the user's request. The next
visual check should manually inspect the previously revamped screens together:
Government, notification drawer, Character Menu/profile and unlockables,
Character Creation, Realm Assignment, Shrine, neutral/free Portal prompt,
ticket/fee Portal prompt, and Nether/End route HUD slots.

## Civic UI Polish And Onboarding Confirm Packet - 2026-07-08

Follow-up visual fixes centered the shared civic action-button text baseline
through `ElarionCivicUi.centeredTextY`, moved older Government tab/action
buttons to metric-based centering, centered Shrine reward slots within their
bounded reward panel, and made Civic Forum / Seat of Rule titles larger and
lower inside their headers.

The semantic icon catalog was retuned so generic profile, identity, people,
civic, quest, world, Nether, End, project, and pet icons use non-portrait
library art. Portrait assets remain available for actual player/NPC portrait
slots only.

Character Creation now labels the footer action `Continue`. After server
acceptance, Core sends the Realm placement panel before closing creation, and
teleporting is triggered only by the placement screen's
`CharacterRealmAssignmentConfirmPayload`. The Character Menu opens on the
Profile tab by default, while Mounts/Pets/Titles remain selectable. Mount
preview calibration was adjusted for the Sci-Fi Bike and kept bounded by the
mount preview tests.

No live screenshot QA was run in this pass per user request. Manual inspection
should verify Shrine reward/button alignment, Government title/icon choices,
Character Creation -> Realm Placement -> Confirm flow, Character Menu Profile
default, Pets tab icon, and Mount preview centering.

## Civic Follow-Up Polish - 2026-07-08

Manual screenshots showed the completed Shrine summary could hide the second
reward row and that shared civic button/title text still sat slightly high.
The follow-up pass moved the shared civic compact-control baseline down one
pixel, removed Shrine tab-local upward offsets, lowered the Shrine header title
by one pixel, and changed the Shrine summary reward panel to reserve height
from reward count with a three-column cap before drawing.

Quest and World notification rail slots now render through the shared semantic
icon catalog instead of the old dedicated feather/world textures. Unread state
for those semantic rail icons is represented by the same small marker overlay
while Mail and Realm keep their existing read/unread icon assets.

Civic Forum and Seat of Rule header crests now draw at source-square 32 px
inside the header rather than scaling to 36 px. The shared Civic crest semantic
icon now points to shield art to avoid tall building artwork reading as
stretched in the header.

## Quest NPC Dialogue Option A Migration - 2026-07-08

The simple NPC conversation surface now follows the approved compact quest
dialogue reference without changing NPC authority or packet contracts. It uses
a larger title, true NPC portrait, one active conversation body, a three-choice
visible cap with virtual overflow scrolling, a header close control, and the
existing bounded card strip below the choices.

Player-response and NPC-response typing phases render in the same conversation
body. Banker, trader, trade, relationship-dashboard, biography, and quest-system
features remain separate future screen families. The current payload does not
carry semantic option-icon or quest-strip metadata, so the client deliberately
does not infer those concepts from localized option text.

## Banker And Trader Polish - 2026-07-08

The dedicated bank screen now uses the shared Sigil currency texture in the
balance badge, amount input, fee/total row, and related controls. The quick
amount buttons line up with the amount field instead of floating across the
footer, and the amount field renders a blinking caret even before a value is
typed.

`worldheart_banker` and `worldheart_trader` now use explicit 64x64 texture
skins plus curated 32x32 portrait library images. The generated skin atlases
keep standard Minecraft UV spacing with transparent unused areas; dark seams
inside active body faces should be treated as asset defects, not as required UV
padding.

The trade shell originally rendered a static, non-mutating preview catalog.
That path has been replaced by the server-authored catalog snapshot described
below. Real stock, prices, inventory checks, and buy/sell mutation packets
still require the separate trade-owner boundary slice.

## Banker And Trader Live QA Follow-Up - 2026-07-08

Live QA under `build/ui-qa/npc-polish-live-2/` found and corrected two source
sampling defects: 32x32 NPC portraits were treated as 64x64 sources and tiled,
and the shared 16x16 Sigil icon relied on an ambiguous texture overload when
scaled. Full-image assets now declare their source dimensions explicitly.

The bank amount field now follows one metric contract: icon at the fixed field
origin, text immediately after it, and caret at `text origin + rendered amount
width`. The empty caret begins at the input origin; every digit and backspace
moves it with the visible value. Fee and Total use one icon/value pair each.

Trader rows preserve their actual `ItemStack` and render the native tooltip
after leaving the scaled matrix. Live captures verify `Protection IV`, armor
attributes, a custom item name, and lore. Front-facing world QA also verifies
the banker/trader 64x64 skin faces, torsos, sleeves, legs, hat band, tie, tunic,
and belt without atlas seams. Overlapping NPC nameplates are a placement and
readability concern, not part of skin rendering.

Reusable rule: never infer full-texture source size from destination size, and
never replace an item stack with display-only strings when native tooltips are
expected.

## NPC Trade Owner Audit - 2026-07-08

The trade UI remains intentionally non-mutating. The architecture audit in
`docs/reports/NPC_TRADE_OWNER_AUDIT.md` found that its current rows are
client-built examples and therefore cannot provide authoritative labels,
prices, stock, item components, or availability.

The first trade implementation slice replaced those rows with a bounded
server-authored snapshot while preserving the civic shell and native item
tooltips. Buy/Sell actions stay disabled. Later mutation work requires a
session nonce, catalog revision, server-resolved price, idempotent Economy
receipt, restart-safe reward delivery, and NPC-owned purchase reconciliation.

UI rule: disabled actions must show a server-authored reason. The client must
never infer availability from labels, price text, stock text, or item lore.

## NPC Server-Authored Trade Catalog - 2026-07-08

The trade shell now renders server-authored rows from `NpcTradeSnapshotPayload`
instead of constructing its goods client-side. `trades.yml` owns offer labels,
subtitles, item IDs, counts, custom model data, names, lore, enchantments,
prices, and enabled flags. The client only lays out the snapshot, renders the
provided `ItemStack`, and shows native tooltips.

Buy/Sell remains visually present but non-mutating. Offer rows are display-only
until the later payment, stock, and purchase-journal slices exist. Keep the
Sigil price icon directly paired with the right-aligned price text; do not
scatter separate loose currency icons across the row.

Nether and End tickets use the same Portal ticket item with custom model data
rather than separate fake icons: Nether uses the crimson stele asset, and End
uses the blue stele asset. Portal confirmation prompts route ticket semantics
to those same icons. Portal confirmation now uses the server-authored
`PortalTravelPromptPayload.costKind` (`free`, `ticket`, or `fee`) for
payment-slot visibility and ticket/currency art; it must not infer free/no-fee
state from localized requirement text.
The item models now point at portals-owned `textures/item` copies of the stele
icons; GUI-library paths remain valid for UI semantic icons, but inventory item
models must use item texture namespace paths.

Follow-up regression fix: the shipped `worldheart_trader` gets an in-memory
catalog compatibility bridge when older `npcs.yml` files omit or blank
`trade-catalog`, and the bank screen remembers Deposit/Withdraw mode per NPC
across server feedback refreshes. Trade offers also expose `price-key` as a
future dynamic Economy pricing hook while the current UI remains read-only and
displays fixed `price`.

## Trader Row Polish Follow-Up - 2026-07-08

Trader prices now use a fixed Sigil icon column with the amount drawn directly
after the icon, so one-digit and multi-digit prices align without drifting
across rows. Native item tooltips now open only when the cursor is over the
actual 16x16 item icon; row hover still highlights the row but does not show
enchantments/lore by itself.

Trade preview custom names/lore and real Portal tickets now explicitly disable
Minecraft's default italic custom-name styling. Keep this rule for future
server-authored item previews: item names and lore should be component-styled
server-side before the stack crosses the packet boundary.

Live QA evidence for this follow-up is complete under
`build/ui-qa/trade-fixes-20260708-stack999/`. The earlier `Invalid Session`
capture was caused by using full-window screenshot Y coordinates with
`minecraft-qa.ps1`, which clicks client-area coordinates; the accidental click
hit Realms instead of Multiplayer. Corrected QA verified one-slot 999 Sigils,
stable Sigil price columns, row hover without native tooltips, icon-only native
tooltips, and non-italic Nether ticket text.

Economy now owns the necessary runtime support for physical 999-count Sigil
stacks: the item max count, the inventory slot ceiling, and the serialized
ItemStack count codec must stay aligned. Do not raise stack counts for
currency-like items by changing only `Item.Settings.maxCount`.

## NPC Trade Quantity And Tax Proposal - 2026-07-08

Future BUY interaction keeps catalog rows compact. Selecting a row opens one
bounded detail/action area inside the trader shell with minus, numeric quantity,
plus, Max, and server-authored Subtotal, Tax, and Total. Native tooltips remain
icon-only. Quantity counts offer units, not raw item count, and controls use
shared typography metrics at every supported font scale. Confirm remains
disabled until crash-safe Economy receipts and the NPC purchase journal exist.
See `docs/reports/NPC_TRADE_PURCHASE_FOUNDATION_PROPOSAL.md`.

## NPC Trade Jurisdiction Foundation - 2026-07-08

No visible trader layout changed in Slice 23B. The server now resolves and
persists each placed NPC's Realm/world jurisdiction before the future quote
panel renders tax. The next UI slice may display only server-authored authority,
rate, Subtotal, Tax, and Total; it must not infer jurisdiction from client world
state or NPC labels.

## Trader Quantity And Tax Quote Panel - 2026-07-08

- Catalog rows remain compact; selection uses the active green border and
  native tooltips remain restricted to the item icon.
- The fourth-row area is a bounded panel with minus/plus/Max, quantity, subtotal,
  authority tax, total, and disabled Confirm.
- Quantity changes request server quotes. The UI does not derive tax,
  jurisdiction, totals, stock, or eligibility.

## Worldheart Control UI Boundary - 2026-07-09

- No UI changed in this infrastructure slice.
- Future Worldheart blocks, Admin pages, and Seat of Rule/owner tools should
  ask Core's Worldheart governance service whether the player is OP4/admin or
  the current Worldheart ruler. Do not hard-code `Hollow Emperor`, fake player
  accounts, or direct UUID checks in screens.

## Trader Unlimited BUY Confirm - 2026-07-09

- Confirm is enabled only for a valid server-authored quote.
- Clicking Confirm sends a purchase request with a client-generated purchase
  ID. The server revalidates session, range, catalog revision, offer, item
  availability, and quote before charging.
- Result feedback renders in the selected-offer band. The client still never
  computes subtotal, tax, total, treasury destination, or delivery.
- Finite BUY stock is now server-authored row metadata. Compact trader rows
  show `Stock N` only for finite offers; unlimited offers keep their authored
  subtitle. Native item/enchantment/lore tooltips still appear only when
  hovering the item icon. Selling and dynamic price-key behavior remain future
  UI work.

## NPC Trade Live QA - 2026-07-09

- Live QA evidence lives under `build/ui-qa/slice-26-npc-trade-live/`.
- `08-trader-dialogue.png` verifies the conversation-first trader surface with
  curated portrait and explicit `Trade` option.
- `09-trade-buy.png` verifies the Buy catalog with Nether/End ticket icons,
  stock labels, fixed Sigil price columns, and server-authored quote panel.
- `10-hover-nether-icon.png` and `11-hover-nether-text.png` verify native item
  tooltips only appear over the actual item icon hitbox.
- `12-trade-sell.png` verifies Sell mode, cobblestone buyback presentation,
  Sigil payout columns, and disabled Confirm when no matching item exists.
- `18-after-refresh-fix-stock10.png` verifies the live stock refresh fix:
  successful Buy now returns a fresh server snapshot, and visible Nether ticket
  stock updates from `Stock 11` to `Stock 10` without reopening the trader.

## NPC Trade Action Band Polish - 2026-07-09

- The selected-offer action area must stay inside the catalog shell. The old
  shallow band extended below its parent frame, making amount, status, Confirm,
  and totals feel like mixed UI layers.
- The trade screen now uses a taller bounded action panel inside the catalog
  shell. Amount controls sit in the left control group, status/error text sits
  in the center, Confirm uses a larger right-side button, and Subtotal/Tax/Total
  are separated into a lower totals row.
- Follow-up polish increased the logical screen height, shows four catalog rows
  above the selected-offer band, moves the page counter out of the action band,
  and keeps visual controls and click hitboxes on the same coordinates.
- Keep this pattern for future trader/shop UIs: row selection opens one clear
  bounded action band, never a floating footer that crosses frame boundaries.
- QA note: the first live capture before the follow-up layout expansion is
  `build/ui-qa/slice-27-trade-action-band/06-trade-action-band.png`. The
  post-expansion rerun started `runServer`/`runClientOne` successfully, but the
  client hit Minecraft's `Invalid Session` dialog before reaching the server
  list; see `build/ui-qa/slice-27-trade-action-band/10-multiplayer-rerun.png`.

## NPC Trade QA Helper And Final Band Layout - 2026-07-09

- The final trader action band uses a four-row catalog above a bounded selected
  offer panel. The top strip contains `Qty`, `-`, fixed-width quantity well,
  `+`, `Max`, status text, and a right-aligned `Confirm` button. The lower
  strip contains `Subtotal`, jurisdiction tax, and `Total/Payout`.
- The panel and footer now fit inside the outer shell with bottom breathing
  room; do not move the footer to the panel edge or let the action band cross
  the catalog shell.
- Follow-up readability polish keeps compact rows to icon, title, finite stock,
  and price. Do not reintroduce row subtitles when the title already identifies
  the offer; detailed/lore text belongs to native item tooltips or a dedicated
  detail view.
- Total cells should keep label, Sigil icon, and amount close together. Avoid
  spreading subtotal, tax, and total across the full panel width because it
  reads as unrelated data instead of one purchase summary.
- NPC portrait images now draw closer to the frame edge to remove the visible
  bottom gutter seen under configured 32x32 portrait assets.
- `dev/tools/npc-trade-qa.ps1` rebuilds the default banker/trader scene through
  normal `/e npc` commands after the dev client is already joined. It assumes
  world view by default; `-CloseScreens` is opt-in because automatic ESC can
  disrupt the connection state.
- Live QA evidence:
  `build/ui-qa/slice-28-npc-trade-layout-qa/05-trade-buy-layout.png`.
- Follow-up live QA attempt for this polish was blocked by the dev client
  showing Minecraft's `Invalid Session` dialog before the server list:
  `build/ui-qa/slice-29-npc-trade-readable-polish/02-menu-fullscreen.png`.

## Grave Recovery Shell Polish - 2026-07-09

- Grave Recovery remains a separate menu, not a Portal/NPC/ledger tab.
- The screen now follows the civic popup pattern: attached shell, centered
  ornamented title, framed status/body panel, framed contents grid, and a
  bounded bottom action band.
- Render, scroll, and click math come from one internal layout snapshot. Do not
  reintroduce separate hard-coded button/grid positions for visual and hitbox
  paths.
- Native item tooltips remain item-slot-only. Hovering the panel or row area
  should not expose item lore/enchantments unless the cursor is over the actual
  item slot.
- No recovery state changed in this slice; the client still sends only the
  typed recover request and the server revalidates corpse id, ownership/access,
  world, range, and inventory mutation.

## Admin Panel Danger Modal Polish - 2026-07-09

- Danger rows now keep destructive colors while still showing hover and selected
  state. Selected danger rows use the same active-green left marker pattern as
  other rows so the chosen row is visible.
- Generic Admin Panel action confirmation/input modals now use one
  `ActionModalLayout` metric record for render and click geometry. Do not add
  separate hard-coded modal button coordinates in render and mouse handlers.
- Confirmation bodies render inside a bounded message panel; danger actions use
  the destructive accent. Input modals keep the text field and Tab suggestions
  between the body panel and the footer action band.
- This did not change Admin Panel packets, permissions, provider actions,
  runtime reset behavior, or config edit shell behavior.

## Bank And Economy UI Audit - 2026-07-09

- The dedicated bank screen is visually NPC-owned, but all money authority
  remains Economy-owned.
- The current Fee row is not authoritative for nonzero withdrawal-tax configs:
  `NpcBankScreen` renders `0` locally while Economy applies withdrawal tax on
  submit through `EconomyInventoryService.withdraw(...)`.
- Do not fix this with client-side fee math. The next bank UI slice should
  follow the trade screen pattern: request a server-authored quote for the
  selected mode and amount, render Fee/Total/validity from that quote, then
  keep mutation on the existing server-validated submit path.
- Keep bank interest, tax editing, transaction history, and direct bank spending
  for Portal/Shrine/trader services out of the visual polish slice.

## Bank Quote Transport - 2026-07-09

- Economy now exposes `ElarionEconomyApi.quoteBank(...)` and
  `EconomyInventoryService.quoteBank(...)` for Deposit/Withdraw previews.
  Quotes are pure read models and do not mutate wallets, inventory, journals,
  receipts, or treasuries.
- NPCs mirrors the trade quote pattern with `NpcBankQuoteRequestPayload` and
  `NpcBankQuotePayload`. The server revalidates active NPC session, range,
  bank node, and visible mode option before sending an Economy-authored quote.
- `NpcBankScreen` clears stale quotes on mode/amount changes, ignores replies
  that do not match the current mode and amount, and disables Confirm until the
  current quote is valid.
- Do not reintroduce client-side Fee/Total math. Bank interest, transaction
  history, and tax editing stay outside this compact NPC service screen.

## Bank Quote Live QA Blocker - 2026-07-09

- Dev QA added a visible local-only `bank.withdrawal-tax-basis-points: 250` to
  `dev/run/config/elarion/addons/economy/economy.yml`.
- `build/ui-qa/slice-35-bank-quote/06-clean-platform-banker.png` and
  `07-banker-dialogue-clean.png` show the deterministic clean-platform banker
  setup and root dialogue.
- The live click path did not reach the dedicated bank screen: after entering
  the Sigils lore node, Back/close did not reliably return to root, and the
  client eventually returned to the multiplayer screen. Treat this as a
  dialogue activation/dismiss QA blocker before judging bank Fee/Total visuals.
- Keep the server-authored quote invariant: fix the flow/tooling first, then
  capture Deposit and Withdraw states. Do not add local Fee/Total math to the
  bank UI.

## NPC Service Art Cleanup - 2026-07-09

- The obsolete `dunk_banker.png` texture is no longer part of the active NPC
  set. Active dev-run NPC visual profiles are only `worldheart_banker` and
  `worldheart_trader`.
- `ElarionNpcEntityRenderer` now falls back to the current
  `worldheart_banker.png` texture instead of the deleted legacy file.
- Generated NPC defaults also reference only current service textures for the
  banker/trader paths. The dev-run banker dialogue file was updated to the
  explicit conversation-first `Open Bank` route so live QA no longer depends on
  the legacy root-prompt migration bridge.
- Restart verification reported exactly 2 NPC definitions, 2 skin profiles, 2
  portrait profiles, 2 dialogues, and 1 trade catalog. The remaining visual QA
  check is client-side: confirm no missing-texture warning after the next clean
  client start.

## NPC Service QA Open Command - 2026-07-09

- `/e npc open <npcId>` is an OP-only QA/admin convenience that opens the
  normal conversation for a nearby placed NPC through `NpcInteractionService`.
  It does not bypass range, definition availability, permission checks,
  dialogue validity, or the normal session path.
- `dev/tools/npc-trade-qa.ps1` now uses this command to avoid brittle
  right-click setup for banker/trader root conversations. The helper still
  reaches Bank/Trade by clicking the server-authored `Open Bank` or `Trade`
  option, so service screens keep the conversation-first contract.
- Use `capture-bank` from a clean client/server start for the next bank
  Fee/Total screenshot pass.

## NPC Bank And Trader Manual QA Polish - 2026-07-09

- Configured NPC portraits should visually meet the lower portrait frame edge;
  a one-pixel black gutter below a 32x32 portrait reads as bad alignment.
- Bank Fee/Total rows should be compact groups: label, Sigil icon, amount.
  Do not right-align the amount far away from its label in compact service
  panels.
- Trader stock labels are left-aligned and vertically centered so the `S` in
  `Stock N` stays stable as counts change.
- Trader row range text must not sit on a row border. Keep it between the row
  stack and the selected-offer panel.
- Quantity controls must have even spacing around the value well. Total/Payout
  should be visually stronger than Subtotal and Tax, while all three remain
  server-authored quote values.
- Compact row Sigil icons may be slightly smaller than standalone currency
  badges; keep the price icon column aligned but avoid oversized row icons.
- Ticket tooltip lore must not imply one-way travel. Use neutral wording such
  as `Grants one Nether Gate passage.`

## NPC Service Default Guardrails - 2026-07-09

- Generated service NPC defaults now have focused test coverage preventing the
  deleted `dunk_banker` texture id from returning.
- Generated banker dialogue is also covered for the explicit
  conversation-first `Open Bank` route and dedicated `presentation: bank` node.
  This keeps future generated configs aligned with the dedicated bank screen
  instead of falling back to legacy root-level deposit/withdraw prompt shape.

## NPC Trade Final Action-Band Polish - 2026-07-09

- Trade page range text should sit in the clear space between catalog rows and
  the selected-offer band. When the list scrolls, use small left/right range
  markers (`v` and `^`) rather than large paging controls.
- Quantity value wells must use the same centered text baseline as civic
  buttons. Do not hard-code `y + 6` for compact controls.
- Compact trade row titles must also use shared centered text metrics. Do not
  hard-code a fixed `y + 3` title baseline beside 16x16 item icons.
- Subtotal and Realm/Worldheart tax stay grouped on the left/middle of the
  selected-offer band. Total/Payout belongs under the Confirm button and uses
  stronger value color so it reads as the final settlement amount.
- Paid Portal confirmation prompts render the shared Sigil currency texture
  inside the payment slot. Ticket prompts render ticket art, and free prompts
  omit the slot.

## Portal And Grave QA Polish - 2026-07-09

- Portal confirmation now keeps payment-slot visibility on the server-authored
  `costKind`: free prompts omit the slot, ticket prompts use route-specific
  ticket art, and fee prompts keep the framed Sigil payment slot. Do not infer
  the slot from requirement text.
- Portal confirmation title and Yes/No buttons use shared centered metrics.
  `PortalConfirmationScreen.buttonLayout(...)` owns the paired button
  positions for both render and click handling.
- Grave Recovery layout is now exposed through
  `GraveRecoveryScreen.calculateLayout(...)` so render, scroll, button hitbox,
  and future tests share one geometry source.
- Grave Recovery scroll markers use the compact centered `Rows first-last /
  total` pattern with matching tiny down/up arrows instead of right-aligned
  bare range text in the grid header.
- Native grave item tooltips remain slot-only. The recovery button still sends
  the same typed recover request and server authority did not change.

## Admin Panel Final Config UX Polish - 2026-07-09

- The Admin Panel config edit shell now uses one `ConfigEditLayout` record for
  render, click, and layout tests. Avoid reintroducing duplicated modal
  coordinates for config controls.
- The config edit shell is slightly wider/taller, centers its title with shared
  text metrics, and keeps Validate/Apply/Close in a bounded footer. This is a
  visual/layout change only; it does not add new config mutation powers.
- Detail action overflow now uses the centered `Rows first-last / total` marker
  with tiny down/up arrows instead of right-aligned raw range text.
- Danger rows keep destructive coloring while selected rows receive the same
  visible active rail treatment as other selected civic rows.

## Shared List Range Marker - 2026-07-10

- Core now owns `ElarionListRangeMarker` for compact centered
  `Rows first-last / total` markers with tiny previous/next arrows.
- Government keeps its existing `GovernmentUiGlyphs.rowRange(...)` call sites,
  but that wrapper now delegates to the Core helper.
- Grave Recovery uses the Core helper directly in its grid header.
- This slice did not alter packets, server state, Grave recovery authority,
  Government row payloads, or row visuals beyond sharing marker math/drawing.
- Future list-like screens should reuse `ElarionListRangeMarker` instead of
  local `Rows ...` strings or local arrow drawing.

## Shared Money Summary Cell - 2026-07-10

- Core now owns `ElarionMoneySummary` for compact settlement cells composed of
  label, shared Sigil icon, and amount.
- NPC Trade selected-offer Subtotal, jurisdiction tax, and Total/Payout cells
  now render through the Core helper.
- This is presentation-only. NPC Trade still receives subtotal, tax, total,
  payout, tax authority label, validity, and messages from server-authored
  quote snapshots; the client does not compute settlement values.
- Catalog row prices remain local to NPC Trade for now because fixed price
  columns are a different row-layout pattern from selected-offer settlement
  summaries.
- Future Bank, Portal fee, Treasury, and Seat of Rule money summaries should
  reuse `ElarionMoneySummary` or extend it narrowly instead of adding local
  label/icon/value grouping.

## Shared Action Band Layout - 2026-07-10

- Core now owns `ElarionActionBandLayout` for shared bounded action-band
  rectangles and hitboxes.
- NPC Trade selected-offer quantity controls, status slot, confirm button, and
  click hitboxes now use the Core helper.
- This is layout-only. NPC Trade still owns labels, tones, quote rendering, and
  server-authoritative purchase requests.
- Future Bank, Portal, Grave, Notification, Admin Panel, and Seat of Rule
  action bands should reuse or narrowly extend this layout approach when they
  have repeated button/status/footer geometry.
- Do not turn the helper into a domain widget. Server-authored actions, enabled
  state, tax/payment values, and mutation requests stay with the owning screen
  and service contracts.

## Shared Semantic Row Layout - 2026-07-10

- Core now owns `ElarionSemanticRowLayout` for compact row rectangles,
  item-icon bounds, title/meta baselines, and price icon placement.
- NPC Trade catalog item-price rows now use the Core helper.
- Native item tooltips remain item-slot-only. Hovering row text or empty row
  space should highlight/select the row but must not expose item lore,
  enchantments, or other native item tooltip data.
- This is layout-only. NPC Trade still owns row labels, stock text, price
  values, selected offer state, quote requests, and trade mutation behavior.
- Future Government archive, Notification, Admin Panel, and Character Menu row
  work should extend the helper narrowly for their row shape instead of adding
  new local coordinate clusters.

## Government Record Row Layout - 2026-07-10

- `ElarionSemanticRowLayout` now also exposes compact record-row geometry for
  non-item rows.
- `GovernmentUiComponents.recordRow(...)` uses the Core helper for row bounds,
  icon placement, title/tag baselines, metric columns, and secondary metric
  baseline.
- This covers Civic Forum and Seat of Rule rows, including history/archive
  rows, because both screens already route through the shared Government
  component.
- Government row payloads, archive semantics, selection behavior, detail
  panels, and server authority did not change.

## Shared UI Component Reference And Gallery - 2026-07-10

- `docs/ui/COMPONENT_REFERENCE.md` is the indexed reference for the first
  extracted Core UI helpers.
- The reference documents `ElarionListRangeMarker`, `ElarionMoneySummary`,
  `ElarionActionBandLayout`, and `ElarionSemanticRowLayout`.
- Development clients can open `/elarion-ui-gallery` to view a static local
  screen that renders the current helper shapes.
- The gallery command is intercepted client-side only when Fabric reports a
  development environment. It is not a server command, not synchronized, and
  not a production player feature.

## Detail Header Layout - 2026-07-10

- Core now owns `ElarionDetailCardLayout` for detail/identity header geometry.
- `GovernmentUiComponents.detailHeader(...)` uses the Core helper for the
  icon slot, text column, title baseline, tag baseline, and subtitle/actor
  baseline.
- Civic Forum and Seat of Rule detail headers are covered through that shared
  Government component.
- Government still owns icon frame rendering, tags, row payloads, detail
  semantics, text colors, and server authority.
- The dev-only `/elarion-ui-gallery` screen and
  `docs/ui/COMPONENT_REFERENCE.md` include the detail header shape.

## Detail Body Layout - 2026-07-10

- Core now owns `ElarionDetailBodyLayout` for section-title, bounded body text,
  and simple key/value row geometry below detail headers.
- `GovernmentUiComponents.sectionTitle(...)` and
  `GovernmentUiComponents.bodyText(...)` use the Core helper, covering Civic
  Forum and Seat of Rule detail body areas through the existing shared
  Government component.
- Government still owns icon choices, text colors, wrapped strings, detail
  semantics, and server authority.
- Future Notification, Character Menu, Shrine, NPC, Admin Panel, and Portal
  detail panels should reuse or narrowly extend this helper instead of adding
  new local body text and key/value coordinate clusters.

## Badge Layout - 2026-07-10

- Core now owns `ElarionBadgeLayout` for compact civic chip/tag geometry:
  width clamping, accent strip, top line, and text inset.
- Core `ElarionCivicUi.statusChip(...)` and Government
  `GovernmentUiGlyphs.tag(...)` now share that geometry while preserving their
  own colors, tones, active state, and semantic labels.
- This is presentation/layout only. It does not define Government row states,
  Admin permissions, notification severity, profile ranks, or any server
  behavior.
- Future small badges in Notification, Character Menu, Shrine, NPC, Admin
  Panel, Portal, and Grave screens should use this helper instead of local
  chip width formulas.

## Progress Track Layout - 2026-07-10

- Core now owns `ElarionProgressTrackLayout` for bounded progress track and
  inner-fill rectangles.
- Government vote-option progress bars now use the Core helper for the track,
  top line, and fill bounds while preserving Government-owned vote values,
  ratio calculation, labels, colors, selection state, and semantics.
- Future Shrine, Quest, Economy, Admin Panel, Notification, and Ledger progress
  indicators should use this layout helper when they need a civic progress
  track, while keeping server-authored totals and domain formatting in the
  owning addon/service.

## Empty State Layout - 2026-07-10

- Core now owns `ElarionEmptyStateLayout` for compact bounded empty-state panel
  geometry with separate title and wrapped-body rectangles.
- Notification drawer empty categories now use the Core helper while preserving
  category-specific empty titles and body text.
- The helper is layout-only. Empty/error/loading conditions, messages, colors,
  and visibility remain owned by the calling screen or server snapshot.
- Future Character Menu, Admin Panel, Shrine, Portal, Grave, NPC, and
  Government empty states should use or narrowly extend this helper instead of
  adding local title/body offsets.

## Modal Layout - 2026-07-10

- Core now owns `ElarionModalLayout` for centered two-button modal geometry:
  shell position, body rectangle, optional input field position, and footer
  button placement.
- Admin Panel action confirmation/input modals now route their layout factory
  through the Core helper while preserving existing rendering, click handling,
  autocomplete, validation, permissions, packets, and server authority.
- This helper is layout-only. It must not own config apply behavior, action
  parameters, confirmation semantics, input validation, or suggestion cycling.
- Future Admin, NPC, Bank, Character, Portal, Grave, and Government modals
  should reuse or narrowly extend this helper when they share centered
  two-button shell geometry.

## Input Field Layout - 2026-07-10

- Core now owns `ElarionInputFieldLayout` for single-line input geometry:
  optional icon rectangle, text X/max width, centered text baseline, and caret
  max X.
- NPC Bank amount input now uses this helper for the Sigil icon, amount text,
  placeholder text, and blinking caret position.
- This is layout-only. Input ownership, max digits, validation, quote
  requests, suggestions, permissions, and submit packets remain with the owning
  screen/service.
- Future Admin Panel filter/action/config inputs, character name input, Shrine
  amount input, and NPC prompt inputs should reuse or narrowly extend this
  helper when they are single-line fields.

## Item Slot Layout - 2026-07-10

- Core now owns `ElarionItemSlotLayout` for square item-slot geometry: outer
  slot bounds, inner item draw bounds, grid placement, and slot-only hover
  detection.
- Notification reward previews now use the helper for slot frames, native item
  draw origins, count overlays, texture/icon fallback centering, and tooltip
  hitboxes.
- Tooltip contents remain owned by the caller and its server-authored preview
  data. The helper must not inspect storage, invent enchantment/lore text, or
  decide item visibility.
- Future Shrine rewards, Grave Recovery slots, NPC Trade item icons, Portal
  ticket prompts, and Character Menu unlock previews should reuse or narrowly
  extend this helper when their native item tooltip hitbox needs to match the
  rendered slot.

## Scroll Viewport Layout - 2026-07-10

- Core now owns `ElarionScrollViewportLayout` for bounded row-list viewport
  geometry: visible capacity, clamped first row, visible count, maximum first
  row, row Y positions, and row-only hit testing.
- NPC Trade catalog rows now use the helper for render range, click selection,
  scroll bounds, and range text input values.
- This is layout-only. Row contents, selected offer state, quotes, stock,
  purchase/sell behavior, server paging, persistence, and packets remain owned
  by NPC/Economy services and their screen.
- Future Notification lists/details, Grave Recovery grids, Shrine panels,
  Admin Panel lists, Character Menu tabs, and Government archives should reuse
  or narrowly extend this helper where their render/click/scroll math is a
  row-based viewport.

## Text Viewport Layout - 2026-07-10

- Core now owns `ElarionTextViewportLayout` for bounded multiline text
  viewport geometry: visible line capacity, clamped first line, visible count,
  maximum first line, line Y positions, absolute/visible line mapping, and
  scroll hint state.
- Character Creation biography rendering now uses the helper for scroll range,
  visible wrapped lines, caret-line visibility, and up/down hint state.
- This is layout-only. Text wrapping, input focus, keyboard editing, max
  length, validation, submit packets, and Realm-placement flow remain owned by
  Character Creation and Core services.
- Future Admin Panel text bodies, Notification details, Shrine history/detail
  text, NPC prompt text, Portal status text, and Grave status text should reuse
  or narrowly extend this helper when they need bounded multiline text
  viewport geometry.

## Tooltip Shell Layout - 2026-07-10

- Core now owns `ElarionTooltipShellLayout` for custom civic tooltip shell
  geometry: screen-edge-aware placement, shell rectangle, and padded content
  rectangle.
- NPC relationship hover hints now use the helper for placement and civic
  shell bounds while preserving NPC-owned relationship labels and hover
  triggers.
- This helper is not a replacement for Minecraft native item tooltips. Item
  components, enchantments, lore, and native item stack presentation should
  continue to use `drawItemTooltip`.
- Future Admin Panel hints, Character Menu badges, Shrine/Portal status hints,
  and non-item Notification fallbacks should reuse or narrowly extend this
  helper when custom civic tooltip chrome is required.

## Icon/Label Line Layout - 2026-07-10

- Core now owns `ElarionIconLabelLineLayout` for compact one-line label + icon
  + value geometry.
- NPC Bank Fee/Total quote lines now use the helper for label position, Sigil
  icon placement, and value position.
- This is layout-only. Fee/total values, bank quote validity, tax policy,
  Economy ownership, and server authority remain owned by NPC/Economy services.
- Future Character Menu profile facts, Admin Panel detail facts, Shrine
  requirement facts, Portal prompt cost facts, Grave status facts, and service
  summaries should reuse or narrowly extend this helper when they share the
  compact icon/value line shape.

## Status Line Layout - 2026-07-10

- Core now owns `ElarionStatusLineLayout` for bounded one-line status and
  feedback geometry: bounds, text position, and text maximum width.
- NPC Bank invalid quote messages and dialogue feedback now use the helper for
  placement while preserving server-authored message text, colors, packets, and
  NPC/Economy service ownership.
- This is layout-only. Validation, status semantics, notifications, quote
  generation, tax policy, and persistence remain outside Core UI.
- Future Admin Panel validation text, Shrine contribution outcomes, Portal
  blocked/allowed hints, Grave status messages, and Character Creation
  validation messages should reuse or narrowly extend this helper when they
  share the same one-line status shape.

## Section Header Layout - 2026-07-10

- Core now owns `ElarionSectionHeaderLayout` for compact centered section
  header geometry: bounds, icon rectangle, centered title position, and divider
  rectangle.
- Character Creation `IDENTITY` and `BIOGRAPHY` panel headers now use the
  helper for icon/title/divider placement while preserving onboarding content,
  input validation, Realm placement flow, packets, and server authority.
- This is layout-only. Panel labels, icon ids, validation, input state, and
  submission behavior remain owned by Character Creation.
- Left-aligned `headerShell` panel titles in NPC Bank/Trade, Admin Panel,
  Shrine, Portal, Grave, and Ledger screens are a distinct shape and should be
  audited separately instead of forced through this centered helper.

## Panel Header Layout - 2026-07-10

- Core now owns `ElarionPanelHeaderLayout` for left-aligned `headerShell`
  panel geometry: shell bounds, title origin/max width, divider rectangle, and
  body start Y.
- NPC Bank amount panels and NPC Trade catalog panels now use the helper for
  title/divider/body-start placement while preserving values, quote state,
  trade offers, stock, packets, and server authority.
- This is layout-only. Panel labels, money/tax formatting, offer rows,
  validation, interaction behavior, and persistence remain owned by the
  calling screen/service.
- Future Admin Panel detail panes, Character Menu panels, Shrine panels,
  Portal prompts, Grave Recovery panes, and Notification details should reuse
  or narrowly extend this helper only when they share left-title panel geometry.

## Service Header Layout - 2026-07-10

- Core now owns `ElarionServiceHeaderLayout` for service-screen header
  geometry: portrait rectangle, title origin/max width, subtitle baseline,
  currency badge rectangle, and close-button rectangle.
- NPC Bank and NPC Trade headers now use the helper for portrait/title/
  subtitle/badge/close placement while preserving NPC identity, service
  labels, balance values, packets, and server authority.
- This is layout-only. It does not own NPC relationship hearts, dialogue
  state, balance calculation, service availability, close behavior, or
  persistence.
- NPC Dialogue's relationship-heart header is related but not identical; audit
  it separately before trying to migrate it.

## Paired Button Layout - 2026-07-10

- Core now owns `ElarionPairedButtonLayout` for paired button-row geometry:
  left button rectangle, right button rectangle, gap, and combined bounds.
- NPC Bank Deposit/Withdraw and NPC Trade Buy/Sell rows now use the helper for
  both rendering and click hitboxes.
- This is layout-only. Button labels, roles, selected state, enabled state,
  service switching, packets, and server authority remain owned by the NPC
  screens/services.
- Future Admin Panel, Portal, Grave, Shrine, and Government paired action rows
  should reuse or narrowly extend this helper only when they share this
  left/right button geometry.

## Footer Action Layout - 2026-07-10

- Core now owns `ElarionFooterActionLayout` for single footer action button
  geometry.
- NPC Bank and NPC Trade `Back to Conversation` buttons now use the helper for
  both rendering and click hitboxes.
- This is layout-only. Button labels, back-role lookup, fallback close
  behavior, packets, and server authority remain owned by the NPC screens.
- Future Portal, Grave, Shrine, Admin Panel, Notification, and Government
  footer actions should reuse or narrowly extend this helper only when they
  share single footer-action geometry.

## Preset Button Row Layout - 2026-07-10

- Core now owns `ElarionPresetButtonRowLayout` for compact equal-width preset
  rows and a preset-plus-confirm variant.
- NPC Bank amount presets and Confirm action now use this helper for render
  and click hitboxes while preserving preset amounts, quote validation,
  submit behavior, packets, and server authority.
- This is layout-only. Preset labels, amounts, enabled state, and submit
  actions remain owned by the calling screen/service.

## Split Summary Layout - 2026-07-10

- Core now owns `ElarionSplitSummaryLayout` for a divider plus left/right
  summary origins.
- NPC Bank Fee/Total summary placement now uses this helper while preserving
  server-authored quote values, colors, and Economy tax policy.
- This is layout-only. It does not format money, calculate tax, validate
  quotes, or decide whether a transaction can submit.

## Service Header Close Hitboxes - 2026-07-10

- NPC Bank and NPC Trade now use the existing `ElarionServiceHeaderLayout`
  close rectangle for mouse hitboxes as well as rendering.
- This removes separate `closeX()` math from those service screens and keeps
  close-button render/click geometry tied to one layout record.

## Service Helper Gallery Coverage - 2026-07-10

- The dev-only `/elarion-ui-gallery` now includes compact examples of the
  newer service-screen helpers: service header, paired mode buttons, compact
  preset/confirm row, split summary row, and footer action.
- This is reference-only. It does not add production UI behavior, packets,
  persistence, or new admin/player commands.
- Future shared UI helpers should be added to the gallery in the same slice
  that introduces or promotes them, so repeated alignment bugs can be checked
  without opening every gameplay menu.

## Phase 5 Semantic UI Foundation Closure - 2026-07-10

- Phase 5 is closed as a shared component-foundation phase. The current helper
  set, focused tests, component reference, and dev gallery are sufficient to
  move into Phase 6 Government work.
- Remaining screen-family migrations are not Phase 5 blockers. They belong to
  Phase 6/7 slices and should be handled one UI family at a time.
- Any future promoted helper must still keep render/hitbox geometry in one
  layout record, support font-scale calculations, include focused tests, and
  update the component reference, UI journal, and dev gallery.

## Government Archive Chronicle Projection - 2026-07-10

- Civic Forum History and Seat of Rule Archive now render Government-owned
  readable projections from Core `PublicHistoryEntry` snapshots instead of raw
  event-type labels.
- Archive rows hide private `vote-cast` events, use structured metadata for
  proposal/record/election/office wording, and show one detail label plus one
  age/time line instead of duplicate timestamp columns.
- Shared compact record row geometry now centers the title/tag and metric/time
  text block inside each row, matching the cleaner row treatment used in newer
  service UI.
- No live screenshot QA was run for this slice by request. Focused Core and
  Government tests covered metadata flow, filtering, projection, and row
  layout bounds.

## Phase 6 Closure - Chronicle Contract And Government Notifications - 2026-07-10

- Core now owns a reusable Chronicle renderer/provider contract:
  `ChronicleRenderer`, `ChronicleRenderContext`, `ChronicleProjection`, and
  `ChronicleRendererRegistry`.
- Government registers `GovernmentChronicleText` through the public-history API
  rather than calling a screen-local formatter directly. This makes Government
  the reference renderer for future Chronicle/news/ledger projections.
- The selected wording contract uses the metadata key `chronicle.variant` when
  present and otherwise derives a deterministic default variant family. This is
  live-safe for old history records because missing metadata remains empty.
- Government notification helpers now use a centralized source/action/icon
  shape: `elarion_government`, semantic Realm/Government icon, and
  server-validated `Open Forum` plus `Dismiss` actions for Realm-scoped
  notifications.
- No live screenshot QA was run in this Medium slice.

## Shrine Completed-State Projection - 2026-07-10

- Shrine UI snapshots now normalize completed instances so requirement rows and
  progress totals render as complete even when an admin/forced completion did
  not fill the stored progress map.
- This is presentation-only. Offerings runtime storage still keeps the actual
  contribution progress, donation records, milestone state, and completion
  timestamp separately.
- The fix prevents contradictory Shrine states such as `Complete` with
  `0 / required` rows while preserving server authority and storage semantics.

## Portal Confirmation Live QA - 2026-07-10

- Portal Confirmation now has an OP4-only preview command for visual QA:
  `/e portal preview <neutral|nether|end|fee|blocked|return>`.
- Preview prompts are presentation-only. They do not mutate route state, grant
  entitlements, or bypass the normal server-validated travel confirmation.
- Accepted prompt states are captured under
  `build/ui-qa/portal-phase7/final/`: neutral/free without a payment slot,
  Nether and End ticket prompts with ticket art and wrapped body text, paid
  Ancient Gate with Sigil currency art, blocked travel with muted `Yes`, and
  prepaid return with no payment slot.
- Long Portal prompt body text should wrap inside the card instead of relying
  on one-line ellipsis. Keep payment-slot visibility driven by server-authored
  `PortalTravelPromptPayload.costKind`.

## Grave Recovery Slot Tooltip Contract - 2026-07-10

- Grave Recovery item slots now use `ElarionItemSlotLayout` for frame bounds,
  native item draw origin, and tooltip hitbox geometry.
- Item lore/enchantments should appear only when hovering the actual 16x16 item
  icon area, not the whole 26x26 civic slot frame or surrounding contents grid.
- This is client layout only. Recovery still sends the same
  `GraveRecoverPayload`, and Underworld service logic still revalidates corpse
  id, access, location, ownership, and inventory mutation server-side.
- No live screenshot QA was run in this Medium slice. A future High pass should
  capture empty, populated, scroll, full-inventory, disabled/enabled recover,
  slot tooltip, and close states.

## Character Menu Profile Summary Field Contract - 2026-07-10

- Core now owns `CitizenProfileSummaryFields`, the stable source/field-id
  contract for Ledger dossier summary slots.
- The existing Progression and Government contributors now use the same
  constants as the Character Menu client lookups. This prevents future addon
  contributors from inventing duplicate IDs for the same profile facts.
- This is a contract/test slice only. It does not add completed quest,
  Offering score, NPC reputation, lifetime death, portal journey, milestone,
  or Chronicle summary data.
- Future contributors must expose bounded owner-maintained summaries with
  explicit visibility before the Ledger renders real values for those slots.

## Character Menu Underworld Death Summary - 2026-07-10

- Underworld now contributes the Ledger `underworld/deaths` summary with
  `SELF` visibility through `UnderworldProfileContributor`.
- The value is updated by `UnderworldService` at the authoritative living-world
  death capture and repeat Underworld death capture paths using the Core
  player-stat key `underworld_lifetime_deaths`.
- The Ledger UI did not change in this slice; it already reads the reserved
  `underworld/deaths` slot and will now show the server-filtered value for
  self/admin profile snapshots.
- No live screenshot QA was run in this Medium slice.

## Character Menu Offering Score Summary - 2026-07-10

- Offerings now contributes the Ledger `offerings/offering-score` summary with
  `SELF` visibility through `OfferingProfileContributor`.
- The value is updated by `OfferingService` after successful direct item or
  currency player Shrine contributions using the Core player-stat key
  `offerings_score`.
- Event/admin progress injections do not count toward the personal Offering
  score, and old contributions are not backfilled.
- The Ledger UI did not change in this slice; it already reads the reserved
  `offerings/offering-score` slot and will now show the server-filtered value
  for self/admin profile snapshots.
- No live screenshot QA was run in this Medium slice.

## Character Menu Quest Completion Summary - 2026-07-10

- Quests now contributes the Ledger `quests/quests-completed` summary with
  `SELF` visibility through `QuestProfileContributor`.
- The value is updated by `QuestStateService` when a player actor locks a quest
  ending on a scope that did not already have one, using the Core player-stat
  key `quests_completed`.
- Existing quest endings are not backfilled, and repeated locks on the same
  active scope do not increase the counter.
- The Ledger UI did not change in this slice; it already reads the reserved
  `quests/quests-completed` slot and will now show the server-filtered value
  for self/admin profile snapshots.
- No live screenshot QA was run in this Medium slice.

## Character Menu Portal Journey Summary - 2026-07-10

- Portals now contributes the Ledger `portals/journeys` summary with `SELF`
  visibility through `PortalProfileContributor`.
- The value is updated by `PortalRouteService` after successful
  server-authoritative travel using the Core player-stat key
  `portal_journeys`.
- Existing travel history is not backfilled.
- The Ledger UI did not change in this slice; it already reads the reserved
  `portals/journeys` slot and will now show the server-filtered value for
  self/admin profile snapshots.
- No live screenshot QA was run in this Medium slice.

## Character Menu Mount Preview QA - 2026-07-10

- Re-ran live screenshot QA for the Character Menu Mounts tab through the real
  local multiplayer path: main menu -> Multiplayer -> saved localhost server ->
  `/charactermenu` -> Mounts.
- Corrected a QA automation mistake where unmaximized/client-coordinate clicks
  could hit the Realms button instead of Multiplayer.
- Tuned Mount preview calibration so all seven V1 mounts are visible in the
  detail frame. Normal mounts use bounds-aware margins; Chinese Dragon,
  Sci-Fi Bike, and Wyvern use explicit visual calibration because their
  converted model bounds do not match the live camera footprint.
- Wyvern geometry now declares GeckoLib-compatible `format_version` `1.12.0`.
- Final useful screenshots:
  `build/ui-qa/mount-preview-20260710-redo/mount-airship-corrected.png`,
  `mount-bee-corrected.png`, `mount-ghast-corrected.png`,
  `mount-hot-air-balloon-corrected.png`, `mount-scifi-bike-corrected.png`,
  `mount-wyvern-final.png`, and `mount-chinese-dragon-final3.png`.

## Character Menu Mount Preview Finalization - 2026-07-11

- Captured all seven mounts from the live Ledger and calibrated visible artwork
  rather than relying on raw converted geometry centers.
- Minecraft GUI scale doubles screen-space movement relative to renderer offset
  units; final calibration uses half the measured screenshot delta.
- Final horizontal art offsets: Airship `-17`, Bee `17`, Chinese Dragon `37`,
  Sci-Fi Bike `85`, and Wyvern `-23`. Ghast and Hot Air Balloon remain default.
- Final vertical art offsets: Airship `14`, Bee `-24`, Chinese Dragon `34`, and
  Wyvern `32`.
- Removed the Wyvern model's invalid textured `shadow` helper plane and froze
  only its Ledger preview pose to prevent animated wing ghosting. The canonical
  Wyvern texture remains unchanged; its detached-looking islands are valid UVs.
- Preview tests now enforce positive frames, viewport intersection, bounded
  offsets, and explicit converted-model calibration instead of incorrectly
  requiring raw geometry symmetry.
- `dev/tools/minecraft-qa.ps1 -Action key -Keys c` now posts the key directly to
  Minecraft, avoiding focus theft by visible QA terminals.
- Final Wyvern evidence:
  `build/ui-qa/mount-preview-20260711-final/wyvern-verified.png`.

## Character Menu Title Preview - 2026-07-11

- Title detail previews use the synchronized Core nickname, falling back to the
  account username only when no nickname is available.
- The local player entity rotation is temporarily normalized and restored, so
  the portrait always faces the preview camera without changing gameplay pose.
- The portrait uses a larger shoulder-and-head crop beneath the title/name
  labels instead of displaying the full player body at a distance.

## Ember Terminology And Character Menu Commands - 2026-07-11

- Player-facing `Citizen`/`Citizens` terminology is now `Ember`/`Embers`.
  `Citizenship` remains the civic-status label by explicit design.
- The former Citizen Ledger shell is now **Character Menu** and defaults to
  Profile when opened with `C`.
- Hidden client `/charactermenu` is the sole manual command. The old `/ledger`
  and `/collection` aliases were removed and remain absent from suggestions.
- The server identity command is `/e ember`.
- Technical compatibility identifiers remain unchanged: Citizen model/API and
  payload class names, citizen storage paths, the `citizens` config key, and
  stable default title ID `citizen`. Only their rendered labels are Ember.

## Character Menu Title Preview Colors - 2026-07-11

- Title preview labels use the selected title entry's configured ARGB color.
- The nickname uses the synchronized Realm/name formatting color exposed by
  `ClientIdentity`, matching project-wide identity rendering.
- Missing identity snapshots fall back to the current UI body-text color.

## Phase 9 NPC Narrative Backend - 2026-07-11

- Phase 9 added server-authoritative NPC relationship and story state without
  changing NPC dialogue, bank, or trader geometry.
- One-time options are removed from server-authored option snapshots after
  successful use; the client does not infer or persist narrative state.
- NPC relationship labels and Character Menu reputation remain deliberately
  deferred, so no placeholder relation bar data was invented in this phase.

## Phase 14 Fresh Onboarding QA - 2026-07-18

- Live generic-client QA found that generated `Player###` account usernames
  were prefilled into Character Name even though the configured roleplay-name
  policy rejects digits.
- Core now sends only a policy-valid normalized prefill. Invalid account names
  render the existing blank field and `Unnamed Ember` preview.
- The verified flow is Character Creation -> server-authored Realm Placement ->
  Confirm Placement -> living-world teleport. Continue does not teleport.
- Evidence is indexed in
  `docs/reports/PHASE_14_CLIENT_AUTHORITY_QA.md`; no layout geometry changed.

## Phase 14 Representative UI And Resource QA - 2026-07-18

- Replaced the incomplete runtime Excalibured archive with the complete pack
  and split its four Minecraft font textures into a separate font pack through
  `dev/tools/split-elarion-resource-pack.ps1`.
- Runtime pack activation is now idempotent for generic and both stable clients;
  repeated sync no longer duplicates pack IDs.
- Fresh screenshots cover Character Profile/Reputation/Mounts/Titles, Admin
  Overview/Config, Realm notification list/actions/detail, and the free Neutral
  Portal prompt under `build/ui-qa/phase14-final/`.
- Config discovery opens without the former custom-payload crash. Notification
  selected rows remain compact, View/Dismiss labels are centered, and the free
  Portal prompt has no item/currency slot.
- Full evidence and reused accepted family captures are indexed in
  `docs/reports/PHASE_14_UI_RESOURCE_QA.md`.
