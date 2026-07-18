# Phase 14 UI And Resource QA

Date: 2026-07-18

## Scope

Representative live-client QA for the canonical civic UI, shared resource
packs, Admin configuration discovery, notification interactions, Character
Menu projections, and Portal confirmation. This pass reused accepted family
evidence where no relevant source contract changed instead of recreating every
historical screenshot.

## Resource Pack Repair

- The runtime `Elarion Excalibured v1.zip` was an incomplete 196-texture copy.
  The complete source pack contains 4,056 textures and rendered correctly after
  replacement.
- `dev/tools/split-elarion-resource-pack.ps1` now deterministically produces:
  - `Elarion Excalibured v1.zip`, excluding the four Minecraft font textures.
  - `Elarion Font v1.zip`, containing only those font textures plus pack
    metadata/art.
- `syncDevRuntimeMods` copies both packs to generic, Client One, and Client Two
  and activates them after `vanilla`/`fabric` where present.
- Pack activation is idempotent. The Gradle helper normalizes interpolated IDs
  to strings and removes existing duplicates before appending active IDs.
- The final Client One log contained zero `ERROR`, `FATAL`, missing-texture, or
  unable-to-load matches.

## Fresh Evidence

Evidence directory: `build/ui-qa/phase14-final/`.

| Evidence | Result |
| --- | --- |
| `00-resource-pack-world.png` | Complete Excalibured assets and separate font render in world |
| `01-character-profile.png` | Character Menu opens Profile first; bounded profile layout is readable |
| `02-character-reputation.png` | Full-width faction ledger, five standings, values and progress tracks fit |
| `03-character-mounts.png` | Mount list/detail and 3D preview render without blank assets or clipping |
| `04-character-titles.png` | Active title color, nickname, and shoulder/head preview render correctly |
| `05-admin-overview.png` | Admin shell, navigation, rows, detail pane, and empty action area fit |
| `06-admin-config.png` | Config discovery opens without payload crash and shows descriptor counts |
| `07-notification-realm-list.png` | Compact Realm rows, icons, age, unread state, and rail pointer render |
| `08-notification-actions.png` | Selected row stays compact; View/Dismiss action band is centered |
| `09-notification-detail.png` | Back, icon, wrapped body, and bounded detail action state render |
| `10-portal-neutral.png` | Neutral/free gate has no item slot or currency and uses centered actions |

The live capture used the current 856x512 development window and configured
Minecraft GUI scale. Automated typography/config tests cover the supported
100-150% server font-scale contract, including 150% notification bounds and
invalid-reload rollback.

## Reused Accepted Evidence

- Portal paid, Nether, End, blocked, and return states:
  `build/ui-qa/portal-phase7/final/`.
- Shrine incomplete/complete requirements, six rewards, reward tooltip,
  history, and contribution modal:
  `build/ui-qa/slice-17o-shrine-civic-reskin/`.
- Character creation and Realm assignment:
  `build/ui-qa/slice-17n-onboarding-live-qa-5-footer-centered/`, plus the fresh
  Phase 14 onboarding flow in `PHASE_14_CLIENT_AUTHORITY_QA.md`.
- Bank quote/conversation states: `build/ui-qa/slice-35-bank-quote/`.
- NPC trade catalog, tooltip-only-on-icon, stock, buy/sell, and restart state:
  `build/ui-qa/slice-26-npc-trade-live/` and
  `build/ui-qa/trade-fixes-20260708-stack999/`.
- All seven mount preview calibrations and final Wyvern artifact removal:
  `build/ui-qa/mount-preview-20260711-final/`.

## Result

PASS for the representative Phase 14 UI/resource gate. No new overlap,
clipping, blank texture, inaccessible action, resource-pack error, or Admin
Config payload crash was observed. Grave Recovery extended inventory states
remain a future feature-flow QA item, not a blocker for the shared UI/resource
release gate.

