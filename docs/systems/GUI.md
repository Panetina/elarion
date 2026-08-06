# GUI And Shared UI System

Purpose: provide reusable visual primitives and theme consistency for Elarion screens.

Main classes: `ElarionScreen`, `ElarionUiRenderer`, `ElarionUiTypography`,
`ElarionCivicColors`, `ElarionCivicUi`, `ElarionUiIcons`, `ElarionUiMetrics`,
`ElarionUiThemes`, `ElarionScaledLayout`, `ElarionVirtualList`,
`ElarionNumericInput`, `ElarionUiThemeService`, `ElarionNotificationHud`,
`ElarionCollectionScreen`, `ElarionCollectionService`,
`ElarionAdminPanelScreen`, `ElarionAdminPanelService`.

Entry points: Core client initializer, UI theme sync, addon client screens.

Commands: none.

Network packets: `UiThemeSyncPayload`, `NotificationSnapshotPayload`,
`NotificationActionPayload`, `CollectionOpenPayload`,
`CollectionOpenRequestPayload`, `CollectionActionPayload`,
`AdminPanelOpenPayload`, `AdminPanelOpenRequestPayload`,
`AdminPanelActionPayload`; legacy reward-claim compatibility payloads and
addon-specific screen snapshots.

GUI/screens: NPC dialogue, Shrine UI, Government UI, notification drawer,
Collection menu, Atlas placeholder shell, and future shops/market/quests/full
Atlas.

Storage/persistence: `config/elarion/core/ui_theme.yml`.

Dependencies: Core client UI package and addon payloads.

Related systems: NPCs, Offerings, Economy, Government, future Quest/Market screens.

Extension points: `ElarionScreen`, primitives, typography helpers, cards,
virtual lists, numeric prompt, theme variants, collection tab providers.

The Atlas shell registers `M` in the Elarion key category and opens a static
`AtlasPlaceholderScreen` built from shared civic primitives. It sends no
packet, loads no map data, and exposes only disabled future feature labels.

Risks: one-off buttons/panels per screen; duplicated colors; unbounded list rendering; client-owned mutation.

Core now owns additive civic color tokens, primitive
helpers, and font-scale-aware control metrics through `ElarionCivicColors`,
`ElarionCivicUi`, `ElarionUiIcons`, and `ElarionUiMetrics`. New or touched custom Elarion
screens should use those helpers for generic brown/gold shells, rows, action
buttons, dividers, chips, semantic icons, and scaled control sizing before adding local drawing
code. Custom Elarion menus must use `ElarionUiTypography` for scaled text
width, font height, centering, wrapping, clipping, and control sizing instead
of hard-coded `8px`/`9px` baselines. Main screen titles may use a larger header
treatment where hierarchy requires it; compact rows and buttons should stay
metric-centered inside their hitboxes through `ElarionCivicUi.centeredTextY`
instead of local `-1`/`+1` offsets. Bounded reward/item grids must reserve
height from the expected row count before drawing so later rows are not hidden
by footer/status bands. NPC/player portrait art is reserved for
actual portrait slots and must not be used as generic profile, identity,
people, civic, tab, or ledger icons. Government-specific semantic components
may stay in Government. The
Notification HUD row surfaces, rail slots, action button frames, thin boxes,
action heights, rail shell, drawer shell, header ornaments, close button, and
message body now use the shared Core civic helpers. Notification layout
contract tests cover close-button/header centering, list bounds, and scaled
row/action heights. Live screenshot QA for the notification drawer is still
pending. Admin Panel row surfaces, action buttons, confirmation/config modal
buttons, modal overlays, main shell, header ornament, list/detail frames,
filter input frame, and config edit result/input surfaces now use shared Core
civic helpers. Generic Admin Panel action confirmation/input modals derive
render and click positions from one `ActionModalLayout` metric record, and
danger rows preserve hover and selected state while keeping destructive visual
language. Collection shell, header ornament, content header, list frame,
detail frame, preview frame, preview body, and action buttons also use shared
Core civic helpers. Character Creation and Realm Assignment shell, header,
input/choice, and primary button surfaces now use shared Core civic helpers.
Character Creation's primary footer action is `Continue`; after acceptance, it
opens the server-authored Realm placement panel. Realm placement confirmation
sends a dedicated C2S confirm packet before teleporting to the assigned Realm.
Portal Confirmation and Grave Recovery shell/body/status/slot/action surfaces
now use shared Core civic helpers. Simple quest NPC Dialogue uses the approved
compact Option A hierarchy: enlarged title, true NPC portrait, one current
conversation body, up to three visible choices, bounded overflow scrolling,
and an optional metadata/card strip. Its shell/options/header-close/prompt
surfaces use shared Core civic helpers while NPC-specific portraits, relation
hearts, cards, typing, sounds, prompts, and server-authoritative option behavior
remain NPC-owned. Service NPCs remain conversation-first: bankers, traders, and
future service actors open the same NPC conversation surface first, then use a
server-authored option such as `Open Bank` to transition into a dedicated
service presentation. The bank presentation uses its own compact screen with
Deposit/Withdraw modes, amount presets, server-authored fee/total quotes,
feedback, and a Back to Conversation action. Faction reputation belongs in the
Character Menu Reputation tab; NPC screens show only the authoritative
relationship with that specific NPC. Trade presentation now has a dedicated
Buy/Sell shell with server-authored quotes, stock, and mutation results.
Collection now has a
live current-state baseline under `build/ui-qa/slice-17c/`; it still needs the
future Character Menu redesign. Admin Panel follow-up states, Character
Creation, Realm Assignment, Portal Confirmation blocked state, Grave Recovery,
and Notification still need additional live screenshot QA. The banker NPC
conversation -> bank -> back flow passed live QA under
`build/ui-qa/slice-19-npc-bank/`; future NPC prompt states and the trade shell
still need their own live QA.

Runtime art icons: Core owns `ElarionUiIcons`, a semantic icon catalog backed
by curated PNGs in
`platform/core/src/main/resources/assets/elarion_core/textures/gui/library/`.
Use semantic ids such as `profile`, `titles`, `mail`, `realm`, `quest`,
`world`, `proposal`, `law`, `office`, `shrine`, `reward`, `portal_ticket`,
`portal_free`, `portal_fee`, `nether_gate`, and `end_gate` instead of adding
screen-local placeholder textures. Server-authored item rewards and costs may
still render real Minecraft item stacks for native item tooltips.

Core now owns
`ElarionListRangeMarker` for centered `Rows first-last / total` text with
consistent tiny previous/next arrows; Government and Grave Recovery route their
range markers through it. Core also owns `ElarionMoneySummary` for compact
label + Sigil icon + amount cells; NPC Trade settlement cells use it while
server quote/tax authority remains unchanged. Core now owns
`ElarionActionBandLayout` for shared bounded action-band rectangles; NPC Trade
uses it for selected-offer quantity controls, status slot, confirm button, and
matching click hitboxes. Core now owns `ElarionSemanticRowLayout` for shared
compact row rectangles; NPC Trade catalog item-price rows use it for row bounds,
item-icon tooltip bounds, title/meta baselines, and price icon placement.
Government shared record rows use it for row bounds, icon placement,
title/tag baselines, and metric columns, covering Civic Forum and Seat of Rule
history/archive rows through `GovernmentUiComponents.recordRow(...)`. Compact
record rows use centered text-block geometry so the title, tag, metric, and
secondary line sit visually centered inside the row at supported font scales.
Core also owns `ElarionDetailCardLayout` for detail/identity header geometry;
Government detail headers route through it while keeping Government-owned icon
frames, tags, and row payload semantics.
Core owns `ElarionItemSlotLayout` for native item-slot bounds, centered item
draw origins, and slot-only hover hitboxes; Notification reward previews use
it while preserving server-authored reward text and Minecraft native item
tooltip rendering.
Core owns `ElarionScrollViewportLayout` for bounded row-viewport geometry:
visible capacity, clamped first row, visible count, row Y positions, and
row-only hit testing; NPC Trade catalog render/click/scroll range geometry
uses it while preserving server-authored offers, quotes, stock, and purchase
behavior.
Core owns `ElarionTextViewportLayout` for bounded multiline text viewport
geometry: visible line capacity, clamped first line, visible line count, line Y
positions, caret-line visibility, and scroll hint state; Character Creation
biography rendering uses it while preserving text input, validation, submit
packets, and server authority.
Core owns `ElarionTooltipShellLayout` for custom civic tooltip shell geometry:
screen-edge-aware placement and padded content bounds; NPC relationship hover
hints use it while preserving NPC-owned relationship labels and hover triggers.
Native Minecraft item tooltips still use `drawItemTooltip`.
Core owns `ElarionIconLabelLineLayout` for compact icon + label/value line
geometry; NPC Bank Fee/Total currency pairs use it while preserving
server-authored quote values, bank tax policy, and Economy ownership.
Core owns `ElarionStatusLineLayout` for bounded one-line status/feedback
geometry; NPC Bank quote errors and dialogue feedback use it while preserving
server-authored messages, colors, packets, and Economy/NPC ownership.
Core owns `ElarionSectionHeaderLayout` for centered icon/title/divider section
header geometry; Character Creation `IDENTITY` and `BIOGRAPHY` panel headers
use it while preserving onboarding flow, icons, labels, validation, packets,
and server authority.
Core owns `ElarionPanelHeaderLayout` for left-aligned `headerShell` panel
title/divider/body-start geometry; NPC Bank amount panels and NPC Trade
catalog panels use it while preserving values, quotes, stock, packets, and
server authority.
Core owns `ElarionServiceHeaderLayout` for service-screen portrait/title/
subtitle/currency-badge/close-button geometry; NPC Bank and NPC Trade headers
use it while preserving NPC identity, service labels, currency values, close
behavior, packets, and server authority.
Core owns `ElarionPairedButtonLayout` for paired service mode/action button
rectangles; NPC Bank Deposit/Withdraw and NPC Trade Buy/Sell render/click
geometry use it while preserving roles, selected state, enabled state, packets,
and server authority.
Core owns `ElarionFooterActionLayout` for single footer action button
rectangles; NPC Bank and NPC Trade `Back to Conversation` render/click geometry
use it while preserving role lookup, fallback close behavior, packets, and
server authority.
Core owns `ElarionPresetButtonRowLayout` for compact preset/action rows and
`ElarionSplitSummaryLayout` for divider plus left/right summary origins; NPC
Bank amount presets, Confirm action, and Fee/Total summary use them while
preserving quote values, validation, packets, and Economy/NPC authority.
Prefer similarly narrow helpers before creating a broad generic row/detail
renderer. Keep each extraction narrow and migrate one or two screens at a time.

Shared component reference: `docs/ui/COMPONENT_REFERENCE.md`. In development
clients only, `/elarion-ui-gallery` opens a static local gallery screen for the
current shared helper shapes. The gallery command is client-intercepted and is
not a server command or production player-facing feature.

Live screenshot QA is now available on this Windows workstation:

```text
.\gradlew.bat runServer
.\gradlew.bat runClientOne
powershell -NoProfile -ExecutionPolicy Bypass -File .\dev\tools\capture-minecraft-window.ps1 -Output build\ui-qa\<screen-name>.png
powershell -NoProfile -ExecutionPolicy Bypass -File .\dev\tools\minecraft-qa.ps1 -Action command -Command '/e panel'
powershell -NoProfile -ExecutionPolicy Bypass -File .\dev\tools\minecraft-qa.ps1 -Action move -X 318 -Y 250
powershell -NoProfile -ExecutionPolicy Bypass -File .\dev\tools\minecraft-qa.ps1 -Action scroll -X 655 -Y 375 -Wheel -1 -Count 4
powershell -NoProfile -ExecutionPolicy Bypass -File .\dev\tools\minecraft-qa.ps1 -Action capture -Output build\ui-qa\<screen-name>.png
```

For real Elarion UI QA, run the dev server first, then open `runClientOne`,
join Multiplayer using the saved `localhost` server entry, and use in-game
commands/interactions to open the target screen. After the target screen is
open in Minecraft, capture the window with
`dev/tools/capture-minecraft-window.ps1`. The default path uses `PrintWindow`,
which captures the Minecraft window contents even if another desktop window is
overlapping it. If a local graphics driver returns a black image, make
Minecraft visible and retry with `-ScreenCapture`.
If the client starts with a tiny/white framebuffer or blank main menu, focus
Minecraft and toggle `F11` once before restarting or changing shader state.
`dev/tools/minecraft-qa.ps1` is the faster wrapper for repeated checks: it can
focus/maximize Minecraft, send a slash command, move the native cursor for
hover/tooltips, post client-area clicks and mouse-wheel scrolls, and capture
screenshots through the same window matcher. Focus and input actions preserve
an already maximized window.

Manual UI entry map:

- Admin Panel opens with `/e panel`.
- Character Menu / unlockables opens from the default `C` keybind. Core clears
  vanilla's default Save Hotbar Activator binding on the first safe client tick
  when it still owns `C`, so fresh clients avoid both the key conflict and
  entrypoint access to uninitialized options. `/charactermenu` remains hidden
  client aliases for manual use, but are not registered in the server command
  tree, `/help`, or slash recommendations.
- Notification drawer opens from the HUD rail; notification data is seeded by
  normal domain actions and admin/test commands rather than a separate inbox
  command.
- Shrine UI opens by right-clicking a linked Shrine of Foundation block.
- Civic Forum and Seat of Rule open by right-clicking their Government blocks;
  their action packets require the server-issued block session.
- NPC Dialogue opens by interacting with placed NPCs.
- Portal Confirmation opens from portal-route interaction when the server
  route state allows prompting. OP4 administrators can also use
  `/e portal preview <neutral|nether|end|fee|blocked|return>` for visual QA;
  this opens representative prompts only and does not alter route state or
  bypass server-side travel validation.
- Grave Recovery opens by interacting with an Underworld grave/tomb.

Government's current canonical reference images live in `docs/ui/government/`
and should be used
when polishing Civic Forum, Seat of Rule, and their inner modal flows.

Notification HUD: Core owns the left icon rail, slideout drawer, and the
reserved accessory anchor below the notification category icons. The left icons
are the category tabs for Personal, Realm, Quests, and World; there is no top
text tab row. The rail is one attached dark component with gold pixel bevels,
fixed-size slots, and a selected-tab pointer matching the notification visual
reference. Mail and Realm preserve their read/unread assets, while Quest and
World use distinct category glyphs with unread markers instead of reusing the
envelope or Realm shield. The drawer
height is content-bounded, so empty categories do not leave a full-height black
panel. Personal opens as Personal Mail and renders only direct personal
messages, private outcomes, and reward claims with mail/reward visual language,
not Realm, Quest, or World card styling. Realm includes admin mail, Realm
announcements, Government results, and Offering/Shrine level notices; World
contains global-stage events such as scheduled Portal unlocks and unique title
claims, and is hidden until the player's Realm unlocks global access; Quests
contains explicit questline notifications published by the Quests addon.
Reward entries are claimable through notification actions. Reward preview
tooltips may include bounded server-authored detail lines such as configured
enchantments; the client does not inspect reward storage to invent them. Realm
and Offering systems should not insert rewards directly into inventory. Clicking a compact
notification row selects it and marks it read without changing row height. The
drawer footer shows the contextual action band for the selected row: a local
`View` command plus server-authored actions such as Claim, Go To, Accept,
Decline, or Dismiss. `View` and `Go To` use the neutral action tone; Claim,
Accept, and Approve use the green primary tone. The action band wraps after four actions and keeps
disabled actions visible but muted. `View` opens an in-drawer detail state with
a back arrow, larger icon, title, status, wrapped body, reward previews, and
the same server-authored action band. Short details are content-bounded while
long details retain the maximum bounded scrolling viewport. Returning to the
list preserves selection and scroll position. Empty categories stay
content-bounded and do not draw an empty footer. A lower selected rail slot
hides its pointer when that pointer would land below the short drawer. Selected rows use the shared dark green selected
surface plus a narrow category-colored accent, keeping the state visible
without changing row height.
Notification cards are ordered newest-first regardless of read state; unread
cards use a small marker in addition to the category rail new-message icon.
Rail unread markers are drawn after their category glyph at the icon's upper
edge, so the glyph cannot cover the exclamation marker.

Chat composer: the compact channel control above the `T` input lists only the
server-projected channels the player may currently attempt to use. Selection
persists for the connection. Tab/Shift+Tab cycles channels for ordinary text;
slash-command completion remains vanilla. PM opens a bounded list of eligible
nicknames but sends the selected stable UUID. All delivery, membership,
restriction, distance, visibility, and rate checks remain server-owned.

Nether/End route-state icons are Portal-owned HUD accessories, not notification
categories. Each unlocked scheduled route receives a compact route-colored slot
below the category separator. Slots are generated from the route-status
snapshot, so future routes use the same bounded repeated layout without adding
hard-coded HUD categories. They render from
`ElarionNotificationHud.accessoryAnchor()` through `ElarionHudOverlayRegistry`
and compute countdowns locally.

Government owns two separate themed screens: `CivicForumScreen` for citizen
founding/audience requests and `SeatOfRuleScreen` for authority review/records. They
share low-level Core UI primitives and the `GovernmentUiOpenPayload` packet,
but they do not share one generic dashboard screen. Government UI rows carry
explicit row intent in their network snapshot: static, navigation, choice,
expandable, or action detail. Voting rows also carry server-authored selection
and vote-count metadata; clients must not infer the player's selected option
from labels such as "Your vote". Civic Forum and Seat of Rule root screens show
Close, while module switches remain server-authoritative. Do not infer row
behavior from labels such as "Rules" or "Monarch"; the payload row kind and
semantic metadata are the contract.

Government audience/vote and civic-record overlays use a compact title field plus a
wider multiline body area for audience text, official law text, rules, notices,
and project records. Long body text should be authored in that body area, not
compressed into titles or single-line inputs.

Government screen polish follows the UI journal reference set: warm dark root
surfaces, gold frame language, green selected-vote/action states, colorful
category tags, framed header metadata, and one composed civic identity such as
`Republic of Oak` instead of separate `Realm of Oak` and `Republic` labels.

Addon integrations publish through `ElarionApi.notifications()`. Addons own
the meaning and validation of their actions, while Core owns storage,
recipient snapshots, filtering, persistence, synchronization, and the shared
drawer. Do not add addon-specific HUD rails or duplicate notification stores.

HUD elements that share space or ordering with notifications use
`ElarionHudOverlayRegistry` instead of independent `HudRenderCallback`
registrations. Status icons render before the notification drawer; tooltips
and other foreground overlays render afterward. Core draws the combined stack
above chat text, including while `ChatScreen` is open. Renderer failures are
isolated so one addon cannot prevent the remaining HUD layers from drawing.

Custom full-screen Elarion GUIs must extend `ElarionScreen` instead of vanilla
`Screen`. The base class centralizes the non-pausing behavior and disables both
Minecraft menu blur hooks (`blur()` and `applyBlur(float)`), so addon screens
stay sharp without repeating per-screen no-op overrides.

All Elarion-authored custom UI text must use `ElarionUiTypography` for width
metrics, font height, centering, ellipsis, wrapping, drawing, clipping, line
spacing, caret placement, and tooltip text. The server-synchronized
`defaults.font-scale-percent` value in `ui_theme.yml` scales text independently
from the fixed outer panel layout. Text-bearing rows, buttons, fields, modals,
and lists must calculate hitboxes and clipping from the same scaled metrics
they render with; larger scale values should reduce visible list rows and keep
scrolling bounded instead of letting text cross frames. Vanilla menus, chat,
inventory, tablist, player nameplates, and title text around player names are
outside this Elarion typography contract.

The Core Collection menu is the shared modular shell for player unlockables.
It presents itself to players as **Character Menu** while preserving Collection
internals. Core owns the `C` key opener, the hidden client-side `/charactermenu`
command, generic collection packets, `ElarionCollectionService`,
the themed `ElarionCollectionScreen`, and the client-side
`ElarionCollectionPreviewRegistry`. Pressing `C` while the screen is already
open closes it. Addons register tab providers through
`ElarionApi.system().collections()` and remain authoritative for their own
state and actions. The client renders only the server snapshot and sends
tab/entry/action ids; the server validates all mutations before refreshing the
snapshot.

Collection snapshots are bounded before transport to 32 tabs, 512 entries per
tab, and 16 actions per entry. Blank, unsafe, oversized, or duplicate
actionable IDs are omitted, display strings use the shared bounded string
codec, and selection falls back to the first transmitted tab. Addon providers
must still keep snapshot generation itself bounded; packet caps prevent a
disconnect but do not make an expensive provider query acceptable.
Collection entries also carry optional `accentColor`, `rankLabel`, and
`rankColor` presentation metadata. Providers own the meaning of those values,
but shared rarity/standing ranks must use Core `ElarionCollectionRank` colors
so `COMMON`, `UNCOMMON`, `RARE`, `EPIC`, `LEGENDARY`, and civic ranks look the
same project-wide. Core bounds, transports, and renders these values as
row/detail frame accents and small rank badges. Do not encode rarity or
ownership tier only in long body text when the provider can supply these
metadata fields.

Collection uses top tabs and currently exposes Mounts, a Core-owned `Pets`
placeholder tab until the future Pets addon contributes real entries, and a
Core-owned Titles tab backed by `TitleService`. The list and detail panels
share one vertical content band in the Option A civic ledger layout. Icon tabs
lead into a compact collection list and a similarly weighted showcase panel;
the header reports bounded unlocked/total completion. Rows show explicit
`ACTIVE`, `OWNED`, or `LOCKED` presentation, while selection remains a visual
frame state. The list uses hidden scrolling: mouse wheel,
keyboard, and page movement remain available for future long tabs, but no
visible scrollbar is drawn. Current Mounts entries use a balanced landing list
with six visible rows and hidden overflow scrolling for the seventh or future
entries. Locked entries are muted, selected entries use the standard gold
frame, and the active entry replaces that frame with green
border/highlight/shadow colors instead of drawing a second outline. Empty tabs
render a centered semantic icon, title, and provider subtitle in both bounded
panels. The detail panel owns a large selected-entry preview, collection or
unlock record, rank badge, and action button. Addons may register client-only
preview providers for that frame; Core's `ElarionMenuEntityPreviewRenderer` provides the reusable
entity rendering/clipping path, flushes already-drawn UI before enabling the
preview scissor, and clips only the live preview render. Mounts uses this for
bounds-aware model previews, and future Pets should use the same registry
instead of adding pet-specific code to Core. Addon-provided text should stay
short and theme-appropriate. Raw 16x16 icon textures are matrix-scaled as one
complete sprite; requesting an oversized texture region causes visible tiling
and is forbidden.

The Titles tab uses Core title definition colors for row/detail accenting,
identity title rendering, and the unlocked title preview above the live player
model and username. Built-in titles also provide rank badges through
`ElarionCollectionRank`: Sovereign, Heir, Council, Officer, Trusted,
Common, Rare, and Legendary. Mounts provides Common/Uncommon/Legendary badges
through the same Core rank palette. The Core Pets placeholder remains empty
until a Pets addon owns real entries and can provide equivalent metadata.

Do not duplicate this system by creating: separate UI frameworks per addon.

## Shared Icon And Portrait Library

Core owns the project-wide, read-only UI art library under
`assets/elarion_core/textures/gui/library/`. It contains categorized pixel-art
icons, portraits, and a generated `manifest.json`. Addons may reference these
textures but must not copy them into addon namespaces.

Each manifest record represents one logical image and lists every available
dimension. Source packs whose size folders repeat the same logical filename
are grouped into one record; Pack 2's `faN`, `fbN`, and `fcN` filenames map to
the 16, 32, and 64 pixel variants of logical image `N`. The stable record ID
retains source-pack identity so same-named or visually similar assets from
different packs never collide. The `labelSource` field distinguishes labels
from source filenames, manual contact-sheet labels, and contact-sheet bucket
labels. SHA-256 values retain exact source identity.

The manifest is a developer lookup catalog, not a player-facing runtime query
engine. UI implementations should select a bounded set of known texture paths
at definition/load time and cache `ResourceLocation` values. Do not scan or
parse the full catalog during screen rendering.

Regenerate the library deterministically with:

```text
<python> dev/tools/import-icon-library.py <source-icons-folder> platform/core/src/main/resources/assets/elarion_core/textures/gui/library
```

After import, refine filenames inside the generated project library with:

```text
<python> dev/tools/refine-generated-icon-library.py platform/core/src/main/resources/assets/elarion_core/textures/gui/library
```

Refinement passes operate on the generated library and manifest only. Do not
re-read the original Desktop source packs when the goal is to polish project
resource filenames.

The Core Admin Panel is the OP-only dashboard opened with `/e panel`. Core owns
the shell, packets, screen, and provider registry; addons register provider
rows/actions and keep ownership of their own mutations. The client renders only
server-authored rows/actions and sends provider/action/target ids plus optional
single-field text parameters. Destructive actions show a click-confirm modal.
The Danger Zone `Reset Everything` action runs registered runtime-reset
providers only and must not delete config files, world files, placed setup
objects, portal endpoints, NPC placements, or inventories.

The Config tab displays config-domain, category, and entry rows from the Core
config descriptor registry. Rows summarize registered domains, source files,
reload commands, categories, entries, current/default display values, bounds,
choices, permissions, validation state, and reload/restart markers. Config
entry rows can open the Core config edit shell. Validation and apply requests
use dedicated config edit payloads; only explicitly registered applier targets
can enable Apply. The current production Apply path is limited to
`core:ui_theme:defaults.font-scale-percent`; all other entries remain
read-only/validation-only until their owning domain registers a safe applier.
The Config tab is intentionally scoped for packet safety: opening the tab sends
domain/category summary rows only, and selecting a category requests that
category's entry rows from the server. Admin Panel open payloads also cap tab,
row, action, and suggestion counts before serialization so an oversized
provider snapshot cannot desynchronize the custom payload stream.

Admin Panel single-field actions may include server-authored autocomplete
suggestions. The client only cycles suggestions with Tab inside the modal and
does not invent IDs. Current Core suggestions cover Realm assignment, titles,
and registered abilities; addon providers can attach their own scoped
suggestions, such as Mount IDs for mount grant/revoke/active actions. Player
Realm assignment is an OP-only player action that calls Core `RealmService`
on the server and then resyncs identity.

The approved future reference set for the broader player hub, onboarding,
Shrine, role-specific NPC interfaces, Portal pop-ups, Grave Recovery, Admin
Panel, and generic event feedback lives in
`docs/ui/revamp-option-a/README.md`. These are planned visual targets, not
implemented contracts. `Character Menu` is implemented as the player-facing
label and hidden `/charactermenu` alias for the current Collection unlockables surface. It
now includes a Core-only Profile tab that requests a server-authored profile
snapshot, reads `CitizenProfileClientState`, and renders visible identity,
Realm, active-title, citizenship, and Core-owned collection summaries as one
portrait-led civic dossier. The Profile tab must not grow nested section menus
or button-like in-profile navigation.
Addon profile data must be aggregated through server-authorized addon
contributors with bounded owner summaries rather than copied into Core;
until then, those areas render subdued `Not recorded` or source-not-contributed
states rather than invented values. Dossier summary slots use the stable
source/field ids in `CitizenProfileSummaryFields`: `offering-score` from
`offerings`, `quests-completed` from `quests`,
`memberships` from `groups`, `office-history` from `government`, `deaths`
from `underworld`, `journeys` from `portals`, `milestones` from
`progression`, and `recent-summary` from `history`. Addon contributors must
use those constants only when they can supply bounded, server-authorized
values with explicit visibility.
Faction reputation is not a Profile summary field. NPCs registers the separate
`reputation` Character Menu tab and supplies bounded per-faction rows.
Reputation is a full-width standing ledger rather than an unlock collection:
there is no selected-item detail panel or owned state. Each row shows faction
identity, known contacts, one of five standing labels, and tier-local
`current/120` progress. The five top tabs divide the available shell width
instead of relying on fixed offsets.
Player-linked profile opening uses a typed server-authored UUID through
`CitizenProfileClientRequests.open`. Core returns the filtered profile and
Character Menu snapshot atomically. History, Chronicle, and addon rows must
never parse display text to discover player identity.
`underworld/deaths` is currently backed by `UnderworldProfileContributor` for
self/admin viewers through the Core player-stat key
`underworld_lifetime_deaths`; it is not public profile data.
`offerings/offering-score` is currently backed by
`OfferingProfileContributor` for self/admin viewers through the Core
player-stat key `offerings_score`, incremented only after successful direct
player Shrine contributions.
`quests/quests-completed` is currently backed by `QuestProfileContributor` for
self/admin viewers through the Core player-stat key `quests_completed`,
incremented only when a player actor locks a quest ending on a scope that did
not already have one.
`portals/journeys` is currently backed by `PortalProfileContributor` for
self/admin viewers through the Core player-stat key `portal_journeys`,
incremented only after successful server-authoritative portal travel.

The shell uses `Character Menu` as its player-facing name while preserving the
existing Collection API, packets,
config/runtime filenames, and provider contracts as the internal Unlockables
subsystem. Profile uses separate read-only model records, contributor
contracts, server-side visibility, and bounded section/field/card caps.
The Profile Advancement count is synchronized from visible completed Minecraft
advancements only; hidden/internal advancement records are not counted.
The Character Menu opens the Profile tab by default from the `C` keybind,
or from the hidden client `/charactermenu` command. Mounts, Pets, Titles, and
future unlockable tabs remain selectable from the tab row.

Core shared UI now includes reusable layout helpers for list range markers,
money summaries, action bands, compact semantic rows, detail headers, detail
body/key-value areas, compact badges/chips, progress tracks, empty-state
panels, centered modal shells, and single-line input fields. These helpers are
geometry and presentation contracts only; owning screens still provide
server-authored payloads, labels, permissions, actions, colors, values, totals,
empty-state conditions, input validation, suggestions, and mutation requests.
New custom UI should reuse or narrowly extend these helpers before adding
local coordinate clusters.

## Development Resource Packs

The canonical development source archive is split into art and font packs so
font activation remains independently controllable:

```text
powershell -NoProfile -ExecutionPolicy Bypass -File .\dev\tools\split-elarion-resource-pack.ps1 -SourcePack "<complete Elarion Excalibured v1.zip>" -OutputDirectory .\dev\resourcepacks
.\gradlew.bat syncDevRuntimeMods --rerun-tasks
```

The script produces `Elarion Excalibured v1.zip` without Minecraft font
textures and `Elarion Font v1.zip` containing only those fonts and pack
metadata. Runtime sync activates each pack exactly once for generic and stable
clients. `dev/resourcepacks/` is the tracked canonical runtime input;
`dev/run/**/resourcepacks/` contains generated copies only. Do not hand-copy a
partial archive into an individual client profile.
