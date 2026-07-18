# UI Family Inventory And QA Queue

Date: 2026-07-18

Scope: Phase 7 Slice 1 refresh. This is a documentation-only inventory based on
`TODO.md`, `docs/systems/UI_JOURNAL.md`, `docs/systems/GUI.md`,
`docs/ai/CURRENT_STATUS.md`, the Phase 6 Chronicle/Government work, and
existing UI reports. No production code was changed.

## Purpose

Keep the remaining UI revamp work bounded. Future slices should start from this
file instead of rereading the full UI journal unless a live bug report or source
change makes that necessary.

Phase 14 representative UI/resource QA is complete. Current evidence and the
remaining non-blocking feature-flow checks are recorded in
`PHASE_14_UI_RESOURCE_QA.md`.

## Status Legend

- `Accepted`: implemented and either manually accepted or covered by live QA
  evidence enough for normal continuation.
- `Needs QA`: implementation exists, but the next work is visual/manual QA or a
  focused screenshot rerun.
- `Needs polish`: implementation exists, but documented issues remain.
- `Pending`: not yet rebuilt on the approved civic UI direction.
- `Backend blocked`: UI should wait for owner-maintained server data or
  architecture.

## Accepted Or Mostly Accepted

| UI family | Current state | Evidence / note |
| --- | --- | --- |
| Notification drawer | Needs only targeted regression QA unless new issues are reported. Compact drawer, rail, action band, semantic icons, and detail flow are implemented. | `UI_JOURNAL.md` Notification Reference Contract and populated QA notes. |
| Civic Forum / Seat of Rule chrome | Mostly accepted. Latest micro-slice centered Government overflow markers as `Rows first-last / total` with tiny arrows. | `GovernmentUiGlyphs.rowRange(...)`; `:addons:government:compileJava` passed. |
| Character Menu shell/profile/unlockables | Mostly accepted. Opens Profile first, has rank/title/mount polish, C keybind conflict is handled, and commands remain hidden. | `build/ui-qa/slice-17k-unlockables/`, `slice-17m-ledger-rank-qa/`, `GUI.md`. |
| Character Creation / Realm Assignment | Mostly accepted. `Continue` opens Realm placement before teleport; confirm uses a dedicated payload. | `UI_JOURNAL.md` Character Onboarding Option A Migration. |
| Shrine / Offerings screen | Mostly accepted visually. Completed instances now project requirement rows/totals as complete even when forced/admin completion did not mutate stored progress. | Phase 7 Slice 2 Offering tests passed; still use manual QA for reward grid visibility. |
| Simple NPC conversation | Accepted for quest/basic NPC shell. Relationship is intentionally not shown until NPC-owned state exists. | `UI_JOURNAL.md` Quest NPC Dialogue Option A Migration. |
| Dedicated bank screen | Accepted after manual QA for Deposit/Withdraw quote flow, Back/ESC, caret, presets, and fee/total behavior. | `TODO.md` manual QA notes and `UI_JOURNAL.md` bank quote entries. |
| Dedicated trade screen | Accepted after final manual polish for row title centering, range marker, quantity controls, stock, tooltips, Sigil icon, and totals layout. | `UI_JOURNAL.md` NPC Trade Final Action-Band Polish. |

## Needs QA

| UI family | What to check next | Recommended model |
| --- | --- | --- |
| Government screens | Live visual pass for centered range markers, archive list row alignment, title/header height, and no footer overlap. | Light if QA only; Medium if code polish is needed. |
| Admin Panel | Code-side layout polish is complete; refresh Config tab/detail, Danger Zone, disabled actions, and action overflow screenshots when QA is convenient. | Light if QA only; Medium if code polish is needed. |
| Portal Confirmation | Accepted in Phase 7 Slice 3. Live QA covered neutral/free, Nether ticket, End ticket, paid Sigil, blocked, and return states. Screenshots: `build/ui-qa/portal-phase7/final/`. Remaining risk is only real-route regression QA after route/config changes. | Light only if rechecking after route changes; Medium if a regression is found. |
| Grave Recovery | Code-side slot-only tooltip polish is complete in Phase 7 Slice 4; item rendering and tooltip hitboxes now share `ElarionItemSlotLayout`. Still needs live screenshots for empty, populated, scroll, full-inventory, recover disabled/enabled, and close states. | Light if manual QA only; High for screenshot acceptance; Medium if a new defect appears. |
| Notification drawer | Recheck populated Personal/Realm/Quest/World categories after current icon art and row range conventions. | Light to Medium. |
| Character Menu | Final screenshot after rank palette/title preview polish if a clean client session is already running. | Light. |

## Needs Polish Or Investigation

| UI family | Issue | Next action |
| --- | --- | --- |
| Shrine / Offerings | Reward grid visibility and full visual pass still need manual or screenshot QA. | State projection issue fixed in Phase 7 Slice 2; do not reopen unless live QA shows a new contradiction. |
| Government archive/history | Needs readable archive records backed by structured data. Current visual polish is separate from the larger Chronicle/history problem. | Phase 6 audit before implementation. |
| Admin Panel config editor | Font-scale Apply works for the first safe production applier; most config entries are still read-only or validation-only. | Do not add new mutation powers until owner appliers exist. |
| Character Menu profile data | Stable source/field IDs are centralized in Core `CitizenProfileSummaryFields`; `underworld/deaths`, `offerings/offering-score`, `quests/quests-completed`, and `portals/journeys` are now backed by owner-maintained self/admin contributors. NPC reputation, milestones, and recent Chronicle summary still need bounded owner summaries before display. | Add one owner summary/contributor per slice; do not scan storage from the Ledger UI. |
| NPC relationship display | Per-NPC relationship belongs to NPCs, while aggregate reputation belongs in Character Menu. | Add NPC-owned relationship state before rendering meters. |

## Pending Screen Families

| UI family | Status | Notes |
| --- | --- | --- |
| Portal HUD route slots | Partially integrated; still needs visual QA for Nether/End icons and route states. | Keep as notification-rail accessories, not notification categories. |
| Underworld / Grave extended states | Grave shell is polished, but full Underworld flow QA remains pending. | Include enchanted/component items, full inventory, expired vaults, and restart reconciliation. |
| Economy/bank broader UI | Dedicated NPC bank is done; broader bank/treasury/transaction history UI is not in scope yet. | Requires Economy/Admin/Government policy slices. |
| Marketplace/shop expansion | Trader V1 exists; broader dynamic pricing, stock policy, buyback UX, and inflation integration remain future Economy/NPC work. | Use NPC `price-key` and Economy-owned policies. |
| NPC narrative/quest-rich dialogue | Basic conversation exists; branching graph validation, relationships, and richer quest integration remain future slices. | Phase 9. |
| Chronicle/newspaper/history reader | Not implemented as a reusable rendering framework yet. | Phase 8. |
| Placeholder-driven UI text | No centralized placeholder registry yet. | Phase 10. |
| Dev-only UI gallery | Planned after component primitives stabilize; not yet built. | Phase 4/5 later slice. |

## Manual QA Order

Use this order when saving credits and doing manual checks:

1. Government: Civic Forum History/Archive and Seat of Rule Archive after the
   row-range marker change.
2. Grave Recovery: populated items, tooltip-only-on-slot, recover action,
   scrolling, full inventory.
3. Admin Panel: Config tab, one config detail, Danger Zone modal, disabled
   actions, Tab completion where available.
4. Notification drawer: populated Personal/Realm/Quest/World, selected rows,
   footer action band, empty categories.
5. Character Menu: Profile first from C, Titles active preview, Mount/Pets
   alignment, no command recommendations for hidden aliases.
6. Character Creation / Realm Assignment: new player `Continue`, Realm
   placement, Confirm teleport.
7. Shrine: reward grid visibility plus one complete and one incomplete state.

## Semantic Component Audit Result

Phase 5 Slice 1 is complete in
`docs/reports/SEMANTIC_UI_COMPONENT_AUDIT.md`.

Main finding: the first reusable UI component should be a narrow shared list
range marker/list bounds helper before a broad semantic row renderer. Phase 5
Slice 2 implemented that first extraction for Government and Grave Recovery.

## Recommended Next Slice

Phase 7 Slice 6 completed the first owner-maintained Ledger summary:
Underworld now updates and contributes self/admin lifetime death count through
`underworld/deaths`. Phase 7 Slice 7 added the second one: Offerings now
updates and contributes self/admin direct player Offering score through
`offerings/offering-score`. Phase 7 Slice 8 added Quests completed count for
new player-authored ending locks through `quests/quests-completed`. Phase 7
Slice 9 added Portal journey count for successful authoritative travel through
`portals/journeys`.

Recommended model:

- `Light`: notification/Government/Grave manual-QA checklist or docs-only planning.
- `Medium`: one bounded owner-summary contract/test if the safe owner is clear.
- `High`: live Ledger UI screenshot QA only after backend summaries are ready.

Reason: Ledger Profile now has stable field IDs, but most desired facts are
still not backed by bounded owner-owned summaries. Add one safe source at a
time.

Next Phase 7 candidates:

1. Phase 8 Chronicle variant framework audit/proposal. Recommended model:
   `Medium`; use `High` only for broad implementation or screenshot-heavy QA.
2. Notification drawer regression check after the current icon/art and row
   conventions.
3. Character Creation / Realm Assignment final visual pass.
4. Grave Recovery High screenshot acceptance pass if manual QA finds any
   visual issue or if final evidence is needed.

Explicit exclusions:

- Do not migrate every Phase 7 UI family in one slice.
- Do not introduce Chronicle/profile/placeholder systems during Shrine polish.
- Do not run broad builds.
