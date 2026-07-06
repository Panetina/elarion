# GUI And Shared UI System

Purpose: provide reusable visual primitives and theme consistency for Elarion screens.

Main classes: `ElarionScreen`, `ElarionUiRenderer`, `ElarionUiTypography`,
`ElarionCivicColors`, `ElarionCivicUi`, `ElarionUiMetrics`,
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
Collection menu, future shops/market/quests/Atlas.

Storage/persistence: `config/elarion/core/ui_theme.yml`.

Dependencies: Core client UI package and addon payloads.

Related systems: NPCs, Offerings, Economy, Government, future Quest/Market screens.

Extension points: `ElarionScreen`, primitives, typography helpers, cards,
virtual lists, numeric prompt, theme variants, collection tab providers.

Risks: one-off buttons/panels per screen; duplicated colors; unbounded list rendering; client-owned mutation.

Current Phase 4 status: `docs/reports/UI_SYSTEM_AUDIT.md` identifies the UI
consolidation path. Core now owns additive civic color tokens, primitive
helpers, and font-scale-aware control metrics through `ElarionCivicColors`,
`ElarionCivicUi`, and `ElarionUiMetrics`. New or touched custom Elarion
screens should use those helpers for generic brown/gold shells, rows, action
buttons, dividers, chips, and scaled control sizing before adding local drawing
code. Government-specific semantic components may stay in Government. The
Notification HUD row surfaces, rail slots, action button frames, thin boxes,
action heights, rail shell, drawer shell, header ornaments, close button, and
message body now use the shared Core civic helpers. Notification layout
contract tests cover close-button/header centering, list bounds, and scaled
row/action heights. Live screenshot QA for the notification drawer is still
pending. Admin Panel row surfaces, action buttons, confirmation/config modal
buttons, modal overlays, main shell, header ornament, list/detail frames,
filter input frame, and config edit result/input surfaces now use shared Core
civic helpers. Collection shell, header ornament, content header, list frame,
detail frame, preview frame, preview body, and action buttons also use shared
Core civic helpers. Character Creation and Realm Assignment shell, header,
input/choice, and primary button surfaces now use shared Core civic helpers.
Portal Confirmation and Grave Recovery shell/body/status/slot/action surfaces
now use shared Core civic helpers. NPC Dialogue shell/options/footer/prompt
surfaces now use shared Core civic helpers while NPC-specific portraits,
dialogue boxes, relation hearts, cards, typing, sounds, prompts, and
server-authoritative option behavior remain NPC-owned. Admin Panel, Collection,
Character Creation, Realm Assignment, Portal Confirmation, Grave Recovery, NPC
Dialogue, and Notification still need live screenshot QA.

Live screenshot QA is now available on this Windows workstation:

```text
.\gradlew.bat runServer
.\gradlew.bat runClientOne
powershell -NoProfile -ExecutionPolicy Bypass -File .\dev\tools\capture-minecraft-window.ps1 -Output build\ui-qa\<screen-name>.png
powershell -NoProfile -ExecutionPolicy Bypass -File .\dev\tools\minecraft-qa.ps1 -Action command -Command '/e panel'
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
`dev/tools/minecraft-qa.ps1` is the faster wrapper for repeated checks: it can
focus/maximize Minecraft, send a slash command, post client-area clicks and
mouse-wheel scrolls, and capture screenshots through the same window matcher.

Manual UI entry map:

- Admin Panel opens with `/e panel`.
- Collection opens with `/collection` or the Collection keybind.
- Notification drawer opens from the HUD rail; notification data is seeded by
  normal domain actions and admin/test commands rather than a separate inbox
  command.
- Shrine UI opens by right-clicking a linked Shrine of Foundation block.
- Civic Forum and Seat of Rule open by right-clicking their Government blocks;
  their action packets require the server-issued block session.
- NPC Dialogue opens by interacting with placed NPCs.
- Portal Confirmation opens from portal-route interaction when the server
  route state allows prompting.
- Grave Recovery opens by interacting with an Underworld grave/tomb.

Visual reference journal: `docs/systems/UI_JOURNAL.md`. Government's current
canonical reference images live in `docs/ui/government/` and should be used
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
Decline, or Dismiss. The action band wraps after four actions and keeps
disabled actions visible but muted. `View` opens an in-drawer detail state with
a back arrow, larger icon, title, status, wrapped body, reward previews, and
the same server-authored action band. Returning to the list preserves the
selection and list scroll position. Empty categories stay content-bounded and
do not draw an empty footer. Selected rows use a neutral dark surface with a
narrow category-colored accent rather than a full green selected fill.
Notification cards are ordered newest-first regardless of read state; unread
cards use a small marker in addition to the category rail new-message icon.

Nether/End route-state icons are Portal-owned HUD accessories, not notification
categories. Each unlocked scheduled route receives a compact route-colored slot
below the category separator. Slots are generated from the route-status
snapshot, so future routes use the same bounded repeated layout without adding
hard-coded HUD categories. They render from
`ElarionNotificationHud.accessoryAnchor()` through `ElarionHudOverlayRegistry`
and compute countdowns locally.

Government owns two separate themed screens: `CivicForumScreen` for citizen
founding/proposals and `SeatOfRuleScreen` for authority review/records. They
share low-level Core UI primitives and the `GovernmentUiOpenPayload` packet,
but they do not share one generic dashboard screen. Government UI rows carry
explicit row intent in their network snapshot: static, navigation, choice,
expandable, or action detail. Voting rows also carry server-authored selection
and vote-count metadata; clients must not infer the player's selected option
from labels such as "Your vote". Civic Forum and Seat of Rule root screens show
Close, while module switches remain server-authoritative. Do not infer row
behavior from labels such as "Rules" or "Monarch"; the payload row kind and
semantic metadata are the contract.

Government proposal and civic-record overlays use a compact title field plus a
wider multiline body area for proposal text, official law text, rules, notices,
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
Core owns the `C` key opener, `/collection`, generic collection packets,
`ElarionCollectionService`, the themed `ElarionCollectionScreen`, and the
client-side `ElarionCollectionPreviewRegistry`. Pressing `C` while Collection
is already open closes it. Addons register tab providers through
`ElarionApi.system().collections()` and remain authoritative for their own
state and actions. The client renders only the server snapshot and sends
tab/entry/action ids; the server validates all mutations before refreshing the
snapshot.

Collection uses top tabs and currently exposes Mounts, a Core-owned `Pets`
placeholder tab until the future Pets addon contributes real entries, and a
Core-owned Titles tab backed by `TitleService`. The list and detail panels
share one vertical content band. The list uses hidden scrolling: mouse wheel,
keyboard, and page movement remain available for future long tabs, but no
visible scrollbar is drawn. Current Mounts entries use a balanced landing list
with six visible rows and hidden overflow scrolling for the seventh or future
entries. Collection rows use visual state instead of status text: locked
entries are muted, selected entries use the standard gold frame, and the active
entry replaces that frame with green border/highlight/shadow colors instead of
drawing a second outline. The detail panel owns the selected-entry preview
frame and action button. Addons may register client-only preview providers for
that frame; Core's `ElarionMenuEntityPreviewRenderer` provides the reusable
entity rendering/clipping path, flushes already-drawn UI before enabling the
preview scissor, and clips only the live preview render. Mounts uses this for
bounds-aware model previews, and future Pets should use the same registry
instead of adding pet-specific code to Core. Addon-provided text should stay
short and theme-appropriate.

Do not duplicate this system by creating: separate UI frameworks per addon.

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
