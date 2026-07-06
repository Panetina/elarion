# UI Journal

Purpose: preserve concrete visual direction for Elarion screens so future UI
work can converge instead of drifting between one-off styles.

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
