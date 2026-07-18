# Option A Asset Bank Plan

Status: active. A large curated runtime icon library has been promoted into
`platform/core/src/main/resources/assets/elarion_core/textures/gui/library/`
and the first semantic runtime catalog is `ElarionUiIcons`. The atlas/chrome
contract is still open; promote additional screen-family assets only when a
slice consumes them.

This file tracks the future art-production program for the approved Option A
civic brown/gold UI direction. The goal is to create a large reusable asset
bank for Elarion's SMP UI without producing unindexed, wrong-sized concept art.

## Rules

- Generate and curate assets in screen-sized batches, not as one unreviewed
  dump.
- Keep source/reference art under `docs/ui/revamp-option-a/` until a screen
  slice consumes it.
- Promote final PNG assets into module resources only when implementation code
  uses them. Semantic runtime icons should be exposed through `ElarionUiIcons`
  rather than hard-coded directly in screen classes.
- Keep source sizes fixed and pixel-readable. Do not scale fuzzy concept-board
  crops into runtime assets.
- Prefer shared assets for chrome, controls, badges, dividers, and semantic
  states so screens stay visually consistent.
- Preserve domain ownership: Portal, NPC, Shrine, Admin, Character Menu, and
  Grave screens may have role-specific art, but they should still use the same
  civic UI token language.

## Initial Runtime Asset Sizes

| Asset family | Source size | Notes |
| --- | ---: | --- |
| Semantic tab/category icons | `32x32` | Mail, Realm, Quest, World, Nether Gate, End Gate, Profile, Unlockables, Shrine, Portal, Grave, Admin |
| Small control icons | `16x16` | Close, back, search, view, accept, decline, claim, dismiss, warning, locked |
| Medium control icons | `24x24` | Buttons that need stronger readability at high GUI scale |
| Row/status icons | `32x32` | Notification rows, archive rows, reward rows, quest states, trade rows |
| Popup/ticket stamps | `64x64` | Scheduled, neutral/no-fee, fee/currency, unavailable, confirmed |
| Portrait/role art | `128x128` or `192x192` | Only for approved NPC/player role surfaces; do not force portraits into compact screens |
| Panel/chrome slices | tileable or `9-slice` source | Borders, bevels, inset panels, dividers, scrollbars, selected rows |
| Reference boards | `1536x1024` | Design QA only, not runtime textures |

## Priority Batches

1. Character Menu / Unlockables:
   Profile, unlockables, mounts, titles, rewards, achievements, quests, Realm,
   reputation, offerings, deaths, history, locked, active, selected, empty.
2. Portal pop-ups:
   scheduled/gated ticket, neutral no-fee passage, fee/currency ticket, blocked
   route, Nether Gate, End Gate, return entitlement, countdown.
3. Shrine/Offering reskin:
   Offering project, requirement, fulfilled/blocked requirement, reward, stage,
   contribution, history, completion.
4. NPC role surfaces:
   quest dialogue, banker, trader entry, trade, choice, disabled choice, reward,
   cost, relationship status.
5. Grave Recovery:
   grave, recover, inventory warning, expired vault, owner-only, item-slot
   chrome, component/enchantment indicator.
6. Admin Panel:
   config, players, realms, systems, danger, validation, apply, rollback,
   permission, restart-required, reloadable.
7. Generic event feedback:
   mount unlocked, title unlocked, quest complete, reputation changed, reward
   available, warning, success, failure.

## Open Decisions

- `Character Menu` is now the player-facing shell label and `/charactermenu` command
  alias. Collection remains the internal API/packet/runtime name for the
  unlockables subsystem.
- Exact runtime atlas/chrome layout is still open. The current promoted library
  is a direct texture catalog, not a packed atlas.
- Portrait use remains optional and should not make compact NPC/banker screens
  heavier than their gameplay flow needs.
