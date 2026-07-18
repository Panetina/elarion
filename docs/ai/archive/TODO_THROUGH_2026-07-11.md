# TODO

Active implementation work only. Keep this file current and avoid long-term design
notes here.

Current UI QA source of truth: `docs/reports/UI_FAMILY_INVENTORY.md`.
Use that inventory before starting another visual polish slice.
Current semantic UI component audit:
`docs/reports/SEMANTIC_UI_COMPONENT_AUDIT.md`.

## High Priority

- Phase 10 Slice 1: Placeholder Consolidation Audit. Recommended model:
  `Medium`. Inventory existing placeholder/token systems, owners, contexts,
  visibility rules, missing-data behavior, and compatibility-sensitive keys
  before introducing a shared registry.

- Phase 9 follow-up backlog after relationship V1: map NPC relationship scores
  to server-authored NPC-screen labels/values, then add a Character Menu
  `npcs/reputation` contributor only after owner-maintained summaries exist.
  Aggregate NPC/Realm reputation remains Character Menu-only; the NPC screen may
  show only that specific NPC relationship.

- Phase 8 follow-up backlog: migrate additional event families only when a
  player-facing Chronicle/library surface needs them. Remaining likely families
  include Realm membership, offices, groups, questline completion, NPC
  relationship milestones, mount unlocks, future pets, revolution, war, peace,
  imperial succession, and story/seasonal events. Each promoted family needs 10
  authored stable variants, metadata checks, tests, and no raw history scans.

- Remaining UI QA/backlog after Phase 7 closure: Shrine/Offering visual
  follow-up if manual QA finds a new issue, Portal prompt regression after
  route changes, Character Menu final screenshot pass, Admin Panel detail/action
  panes, Notification detail surfaces, Grave Recovery live screenshots, and NPC
  dialogue refinements. Migrate or QA one family per approved slice.

- Produce and promote the first curated Character Menu runtime art batch after
  the shared asset-atlas contract is chosen. Profile, Mounts, Pets, and Titles
  now use the approved Option A layout with existing player/vanilla/addon
  textures; custom rewards, achievements, profile categories, badges, chrome,
  and semantic 32x32 art remain future asset work.
- Add owner-maintained summary APIs before exposing NPC reputation or Chronicle
  recent-summary. NPC relation is placeholder Neutral/0. Underworld lifetime
  deaths, Offering score, completed quest count, and portal journeys are now
  contributed for self/admin viewers from owner-maintained Core player-stat
  keys.
- Add a shared player-link UI contract for History, Chronicle, Government,
  notifications, and future menus: double-clicking a server-authored player
  nickname/name span should request that player's Citizen Profile through the
  existing profile service. Do not implement this with client-side name lookup
  or raw text parsing; the server snapshot must carry the target citizen/player
  UUID and visibility must remain server-filtered.
- Add Mounts/Pets reward integration as a separate owner-approved slice:
  Mounts should expose a stable reward/action hook for unlocking a specific
  mount, and the future Pets addon should expose the equivalent pet unlock
  hook. The three Realm baseline mounts/pets stay Common; Sci-Fi Bike is
  reserved as Legendary for the future 100% advancements route.
- Split the approved Option A migration into independently testable screen
  families: player hub, character creation, balanced Realm placement,
  dedicated bank service, trader entry, dedicated trade, Portal
  pop-ups, Grave Recovery, Admin Panel, and generic event feedback. Do not
  expand banker or trader features during their visual migrations. The simple
  quest NPC visual migration is complete; the dedicated bank screen is
  implemented and passed live screenshot QA under
  `build/ui-qa/slice-19-npc-bank/`.
- NPC Sell/buyback V1 is implemented through server-side inventory escrow,
  idempotent Economy wallet payout, sale replay storage, client Sell rows, and
  explicit `destination-offer` routing for `stock-destination: placed_npc`.
  The shipped legacy `worldheart_trader.cobblestone_buyback` route has an
  in-memory compatibility bridge to `destination-offer: cobblestone`; custom
  trader catalogs remain strict. Follow-up work should focus on live QA and
  richer stock/dynamic pricing policy. Do not add wildcard buyback or broad
  item-class selling without a new audit/proposal.
- Economy/NPC trades: future dynamic pricing should use the NPC trade
  `price-key` metadata and Economy-owned policy. Do not duplicate price,
  inflation, tax, or treasury logic in NPC UI.
- The NPC purchase foundation is recorded in
  `docs/reports/NPC_TRADE_PURCHASE_FOUNDATION_PROPOSAL.md`: Economy idempotent
  receipts, NPC Realm/world jurisdiction registration, server tax quotes plus
  quantity UI, Worldheart authority/treasury routing, unlimited BUY journaling,
  and finite placed-NPC stock are complete. Approved
  policy routes Realm revenue to Realm treasuries, non-Realm activity to the
  Worldheart treasury, and uses physical Sigils for shop purchases. Future
  Worldheart control blocks must allow
  Core-authorized OP4/admins or the current Worldheart ruler through
  `WorldheartGovernanceService`, not direct UUID/string checks.
- Bank UI: server-authored bank quote transport is implemented. Follow-up live
  QA should verify Deposit/Withdraw fee/total preview, invalid insufficient
  physical/banked currency states, and post-submit feedback with a nonzero
  `bank.withdrawal-tax-basis-points` config. Do not reintroduce client-side
  withdrawal tax or total math.
- Bank UI live QA is currently blocked before quote capture by NPC dialogue
  navigation/tooling: Slice 35 could place and open the banker root dialogue on
  a clean temporary platform, but synthetic clicks entered the Sigils lore node
  and the Back/close flow did not reliably return to the root before the client
  disconnected to the multiplayer screen. Preserve the screenshots under
  `build/ui-qa/slice-35-bank-quote/` and investigate the dialogue option
  activation/dismiss path before rerunning the taxed Withdraw screenshot.
  Slice 36 cleaned the stale NPC art/config and made the dev-run banker
  dialogue explicit, so the rerun should start from a full restart and first
  confirm the server reports 2 NPC definitions, 2 skin profiles, 2 portrait
  profiles, 2 dialogues, and 1 trade catalog. Slice 38 fixed the client to
  respect `typing-click-completes`; QA scripts should wait until the dialogue
  reaches input state before clicking a final option unless intentionally
  testing typewriter skip behavior. Slice 39 added OP-only `/e npc open
  <npcId>` and upgraded `dev/tools/npc-trade-qa.ps1` with `open-bank` and
  `capture-bank`, so the rerun should use command-opened conversations instead
  of world right-click setup.
- Manual QA later verified bank Deposit/Withdraw quotes and flow. Recheck the
  latest visual polish only: portrait bottom gutter, bank Fee/Total compact
  grouping, balance badge placement, trader range text/arrows, quantity value
  centering, Total/Payout under Confirm, left-aligned stock labels, smaller row
  Sigil icon, paid Portal prompt Sigil icon, and Nether/End ticket tooltip
  wording.
- Add real NPC-owned per-NPC relationship state before rendering relationship
  meters in NPC screens. Aggregate NPC/Realm reputation remains a Citizen
  Ledger-only projection and needs bounded owner-maintained summary APIs before
  exposure.
- Continue curating the Option A runtime art bank through the shared
  `ElarionUiIcons` catalog. The first large library is promoted under
  `assets/elarion_core/textures/gui/library/` and wired into the previously
  revamped Government, Notification, Character Menu, onboarding, Shrine, and
  Portal surfaces. Future screen families still need their own semantic ids and
  chrome assets promoted only when consumed.

- Continue live Admin Panel screenshot QA with the dev-server path and
  `dev/tools/minecraft-qa.ps1` or `dev/tools/capture-minecraft-window.ps1`.
  Overview, Config summary rows, scoped Config category rows, Players tab Set
  Realm, Set Realm Tab completion, font-scale Apply/reflow/restore, disabled
  Apply on non-applier rows, action-list scrolling, and Mount grant Tab
  completion were captured on 2026-07-06. Danger row selection, action modal
  geometry, config edit shell geometry, and centered action overflow markers
  have since been code-polished and test-backed; refreshed screenshots remain
  pending.
- Character Menu live QA evidence is under
  `build/ui-qa/slice-17k-unlockables/`: large Mount preview, designed Pets empty
  state, Titles list/detail, clean icon scaling, title Set Active refresh, and
  restored Monarch state. Keep hidden-scroll and action regression coverage as
  future providers add larger collections.
- Manually verify the 2026-07-08 civic UI polish pass without screenshot
  automation: Shrine two-row reward visibility and Close/Offer/Cancel button
  centering, Shrine tab text/icon baseline, Civic Forum and Seat of Rule title
  size/position/icons, notification Quest/World rail icons, Character Creation
  `Continue` -> Realm Placement -> Confirm teleport flow, Character Creation
  player-head identity preview, Character Menu Profile default from C, hidden
  `/charactermenu` command recommendations, Monarch/title overhead
  color, Pets icon, and Mount preview centering, especially Sci-Fi Bike.
- Character Menu rank/title QA evidence is under
  `build/ui-qa/slice-17m-ledger-rank-qa/`: Mount rank badges, Profile dossier,
  and Titles live-player preview were captured before the shared-rank palette
  polish. A final post-patch screenshot was blocked by the client returning
  `Invalid Session` while reconnecting; rerun this QA after the next clean
  client launch.
- Continue Portal Confirmation and Grave Recovery screenshot QA. The allowed
  Neutral Gate confirmation was captured live on 2026-07-06. Its gate icon
  frame rendered empty in that old capture; the source now routes prompt slots
  from server-authored `costKind` and has test-backed free/ticket/fee icon
  decisions. Blocked portal state and live Grave Recovery screenshots remain
  pending. Capture with `dev/tools/capture-minecraft-window.ps1`. Verify gate
  icon resolution, allowed/blocked button states, grave status chip, item-slot
  grid, slot-only item tooltips, centered row range marker, and recovery/close
  buttons.
- Keep `build/ui-qa/slice-19-npc-bank/` as the banker regression evidence:
  normal conversation opens first, `Open Bank` transitions to the dedicated bank
  screen, and Back to Conversation returns to the normal NPC screen. Follow-up
  evidence under `build/ui-qa/npc-polish-live-2/` verifies Sigil icon/caret
  polish, single banker/trader portraits, both in-world skins, the dedicated
  trade shell, and native enchantment/lore tooltips. Numeric success/error
  feedback after a real deposit/withdraw remains future QA.
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
- Add a dedicated follow-up for title activation if needed when the Citizen
  Ledger shell starts. Slice 17C captured the Titles tab selection/detail path,
  but the live client disconnected before a title activation click could be
  performed.
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
- Add a deterministic NPC live-QA setup script for banker/trader placement and
  UI opening. Slice 26 live QA verified conversation-first trader entry,
  icon-only tooltips, Buy/Sell tabs, successful ticket purchase, and visible
  stock refresh after the server re-sends `NpcTradeSnapshotPayload`, but the
  current QA still requires manual placement/opening in a local dev world.
- Continue after Slice 29 with a dev UI gallery/reference pass for the newer
  service helpers, then pick the next non-NPC screen family for narrow
  migration.
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
