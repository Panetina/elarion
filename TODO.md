# TODO

Active implementation work only. Keep this file current and avoid long-term design
notes here.

## High Priority

- Continue manual Phase 3 Admin config edit verification in a dev client:
  `/e panel` now opens, Config/category clicks no longer disconnect, and the
  Set Realm modal Tab-completes server-authored suggestions. Next verify
  `core:ui_theme:defaults.font-scale-percent` validate/apply, confirm UI theme
  sync/reflow, and confirm other config entries keep Apply disabled. Prefer
  `dev/tools/minecraft-qa.ps1` for command sending, tab clicks, and screenshots.
- Continue live Notification drawer screenshot QA with populated data. The
  Realm empty state, selected rail, shell, header, ornaments, and close button
  were captured on 2026-07-06; selected rows, unread markers, action footer,
  and detail state still require seeded Realm notifications. Use
  the dev-server path: `runServer`, `runClientOne`, Multiplayer saved
  `localhost`, then `dev/tools/capture-minecraft-window.ps1`. Verify rail icons, drawer shell, header title/ornaments,
  centered close X, selected row, unread marker, action footer, empty state,
  and detail state. Do not change notification packets, storage, filtering, or
  action semantics.
- Continue live Admin Panel screenshot QA with the dev-server path and
  `dev/tools/minecraft-qa.ps1` or `dev/tools/capture-minecraft-window.ps1`.
  Overview, Config summary rows, scoped Config category rows, Players tab Set
  Realm, Set Realm Tab completion, font-scale Apply/reflow/restore, disabled
  Apply on non-applier rows, action-list scrolling, and Mount grant Tab
  completion were captured on 2026-07-06. Danger rows and confirmation-modal
  polish remain pending.
- Run live Collection screenshot QA with
  the dev-server path and `dev/tools/capture-minecraft-window.ps1`. Verify shell/header, tab row, list frame, active/selected rows,
  icon frames, preview frame/body, action buttons, hidden scrolling, title
  activation, and mount preview rendering.
- Run live Character Creation and Realm Assignment screenshot QA when a
  target state is open in the dev client. Capture with
  `dev/tools/capture-minecraft-window.ps1`. Verify shell/header, text inputs,
  biography clipping/scroll hints, cooldown state, Realm option rows, and
  primary buttons.
- Continue Portal Confirmation and Grave Recovery screenshot QA. The allowed
  Neutral Gate confirmation was captured live on 2026-07-06. Its gate icon
  frame rendered empty and needs a focused asset/id investigation; the blocked
  portal state and all Grave Recovery states remain pending. Capture with
  `dev/tools/capture-minecraft-window.ps1`. Verify gate icon resolution,
  allowed/blocked button states, grave status chip, item-slot
  grid, item tooltips, scroll indicator, and recovery/close buttons.
- Run live NPC Dialogue screenshot QA with
  `dev/tools/capture-minecraft-window.ps1`. Verify shell/header, NPC/player dialogue rows, option rows,
  numeric prompt, relation hearts, currency badge, cards, scrollbars, and
  footer Close.
- Continue Phase 4 helper adoption after an initial live screenshot QA pass.
  Recommended implementation target remains Shrine UI shell/header/close/
  numeric prompt surfaces only, without changing Offering behavior or server
  authority.
- Investigate the Shrine completion snapshot captured on 2026-07-06: the UI
  showed `Complete` and "This project is complete" while its progress bar and
  requirements showed `0 / 356`, `0 / 256`, and `0 / 100`. Establish whether
  reset-generation state or UI projection is stale before changing rendering.
- Track Phase 0 audit findings from `docs/reports/PROJECT_REVAMP_AUDIT.md`:
  missing centralized placeholder registry, missing Citizen Profile aggregation
  API, Admin Panel row/action contract not yet sufficient for long-term config
  discovery, and Chronicle variant renderer not yet represented as a reusable
  contract.
- Track Config/Admin audit findings from `docs/reports/CONFIG_ADMIN_AUDIT.md`:
  addon reload safety is inconsistent, `/e reload` covers Core only, the
  Admin Panel Config tab can show read-only domain/category summaries but not typed controls,
  and config test coverage is uneven across addons.
- Manually verify the paused Collection UI pass: selected unlocked mount models
  render in the right preview frame after the bounds-aware preview pass, icon
  frames have no black corner pixels, only Mounts/Pets/Titles tabs show, and
  the Titles tab sets active Core titles through existing title state.
- Continue adding in-game Government/GameTest coverage for full Civic Forum and
  Seat UI action flows. Focused tests now cover vote timing, runoff state,
  Realm color persistence, office state mutation, Confederation delegate group
  metadata, Theocracy faith identity persistence, proposal/civic-record
  persistence, tablist visibility/name behavior, Government UI session rejection,
  screen-to-block session mapping, and Government command surfaces.
- Manually verify Government civic-record V1 in-game: citizen proposal creation,
  authority approval/rejection, `Finalize` official wording, typed record
  creation for law/notice/rule/project categories, direct Monarchy record
  creation, High Priest doctrine submission to Synod, archive/restore buttons,
  Republic President wording -> Council review -> citizen law ratification,
  Confederation two-delegate law approval, and notifications.
- Manually verify `/e test shrine reset [realm]` and `/e test government reset
  [realm]` before the next Shrine/Government progression pass. Confirm Shrine
  links/blocks, Portal routes, and NPC placements are preserved.
- Add manual/client verification for true Realm header rows in the tablist at
  different GUI scales and player counts. Current grouped rendering falls back
  to vanilla when a scoreboard objective is active.
- Manually verify Underworld V1: PvE death creates owner-only corpse recovery,
  PvP death creates limited killer loot, souls are sent to the configured
  Underworld world, chat/private/group chat and portals are blocked while dead,
  timers survive logout/restart, and Soul Fractures trigger True Death events.
- Manually verify Character Lifecycle: existing citizens confirm their preserved
  generation-one identity once; new accounts create a fresh neutral character;
  True Death performs restart-safe addon cleanup, waits 24 real hours, and then
  opens the same creation UI without replacing another mod's active screen.
- Manually verify grave recovery with enchanted/component-bearing items, a full
  inventory, restart reconciliation, and expired-corpse recovery vaults.

## Medium Priority

- Audit each active addon when next touched and document its domain events,
  player-facing notification projections, silent/noise exclusions, and future
  integration hooks. Do not add notifications merely to satisfy a quota.
- Build the future Quest addon against the reserved Core Quest notification
  category; do not create another HUD or inbox.
- Add/finish command and GameTest coverage for features that mutate persistence.
- Keep NPC, Offering, Government, Portal, and Groups docs synchronized with
  source changes.
- Keep `AGENTS.md`, `INDEX.md`, `CODEX.md`, `docs/systems/README.md`, and
  `wiki/addons/README.md` synchronized when addons or root Markdown files are
  added, removed, or promoted.
- Add a focused web/bridge architecture document before website integration
  starts.
- Split oversized services/screens only along existing ownership boundaries:
  Government vote logic, Portal travel/schedule logic, Offering contribution
  flow, and large UI screens are the main candidates.

## Low Priority

- Expand system docs only when a subsystem has grown enough to justify it.
- Add source line references to docs during the next code change that touches
  each system.

## Future Ideas

- Atlas / political map system.
- Quest system should publish accepted, assigned, random, timed, and abortable
  quest reminders through Core notifications instead of adding a separate HUD.
- Rich Chronicle, newspaper, ledger, and rumor read models.
- More civic and authority UI modules.
- Add stables, mount progression, NPC sales, permissions, and richer mount
  collection rules after higher-priority Government, Underworld, and Character
  Lifecycle verification work is cleared.

## Technical Debt

- Add a decimal config descriptor codec before typed editing is introduced.
  NPC interaction ranges, Underworld decimal values, and Optimization timing
  thresholds are currently read-only string descriptors.
- Audit whether Core ability, Jail, and Security descriptors need real typed
  runtime models first. Do not expose generated YAML defaults as active current
  values unless they are parsed into typed snapshots.
- Jail and Security generate placeholder YAML but do not parse it into runtime
  models. Add typed loaders before registering those domains; do not display
  generated defaults as active current values.
- Keep the root documentation split small and deliberate.
- Keep future work sliced narrowly to reduce token/credit cost without lowering
  correctness: subsystem first, focused reads, focused tests, then full build
  only when needed.
- Avoid reintroducing duplicate managers, duplicate state owners, or alternate
  networking stacks.
- Do not let local reference notes drift away from the source tree.
- Keep shell addons clearly marked as shells until they own real behavior.
- Ignore Angling reference cleanup for now; it is not part of the active
  Government/Shrine/Portal path.
- Government UI session validation and screen-to-block routing have focused
  unit coverage; full packet-level and multi-player GameTest coverage is still
  pending.
- Add GameTests for dedicated grave reconciliation, recovery authorization,
  full-inventory retention, expiry-vault transfer, and duplicate packet replay.
- Notification persistence, category filtering, reward-provider composition,
  Government/Realm actions, Group invites, Offering milestone notices, title
  notices, gated World visibility, and Portal unlock/status notices have
  focused unit coverage. Full in-game action/GameTest coverage remains future
  work.
- Core now exposes `ElarionDomainEvent` for future cross-addon consumers.
  Titles, Portal route/window lifecycle, Offering-owned Realm global-access
  changes, and Government civic lifecycle changes now emit stable events.
  Remaining addon events should migrate incrementally when their owning feature
  is next changed; do not perform a broad behavior-changing retrofit.
- When an addon changes status, update `AGENTS.md`, `INDEX.md`, `CODEX.md`,
  `docs/addons/README.md`, `wiki/addons/README.md`, and any relevant admin wiki
  page in the same pass.

## Unknown / Needs Investigation

- Whether Confederation needs a dedicated delegate-management UI before civic
  records gain gameplay effects, or whether command-backed cleanup is enough
  until the next civic feature pass.
- Whether Theocracy succession crisis should be implemented before general laws
  and proposals. Current Theocracy has faith identity plus High Priest/Synod
  founding elections, but no doctrine/ritual/succession UI yet.
