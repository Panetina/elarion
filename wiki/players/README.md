# Player Guides

Player-facing wiki entry point.

[Home](../README.md) | [Admin](../admin/README.md) | [Addons](../addons/README.md)

## Status

`Future`

The first wiki pass is admin-first. Player pages should be written after the current Shrine, Portal, NPC, and Economy loops are manually verified on the real client modpack.

Planned pages:

- Getting started
- Realms and chat
- Sigils and banking
- Shrines and Offerings
- Ancient Gates and Portal tickets
- NPCs and Worldheart services
- Notifications and quest reminders
- Character creation, death, graves, and the Underworld

## Current HUD Notes

The left-side HUD rail is one dark, gold-framed component with Personal, Realm,
Quest, and World icons. Clicking an icon opens a compact civic-style
notification drawer, marks the selected category with a gold pointer, and
switches the drawer to that category. Empty drawers shrink to their content
instead of occupying a full-height panel, and Quest/World have their own glyphs
rather than reusing the Mail or Realm icons.

Clicking a message selects it and marks it read while keeping the compact list
row the same height. The bottom action band shows `View` plus any available
server actions such as Claim, Go To, Accept, Decline, or Dismiss. `View` opens
the message detail in the same drawer with a back arrow, full body text, reward
preview when present, and the same server-validated actions.

- Personal Mail: direct messages, personal results, and reward claims. This
  view uses mail/reward card styling instead of showing Realm, World, or Quest
  card language.
- Realm: Realm-wide notices, votes, events, and public Realm news.
- World: global-stage events such as Nether/End unlocks and unique title
  claims. This icon appears only after your Realm awakens its Ancient Gate.
- Quests: questline notices, reminders, and authored quest outcomes.

Unlocked Nether and End routes also appear as small status icons in the
reserved accessory space below the notification categories. Each scheduled
route has its own route-colored slot; closed routes are dimmed and open routes
show progress. Hovering shows the local opening or closing countdown while a
cursor screen, such as chat or the notification drawer, is open.

Tablist visibility is staged. Before your Realm reaches global access, the
tablist shows your own Realm citizens and neutral players. Other Realm groups
appear after both Realms are global-stage eligible. Overhead names and titles
remain visible in-world.

Rewards from Realm admin grants and Shrine completions appear as claimable
Personal notifications. Items or Sigils are delivered only after pressing Claim.
If the inventory is full, the reward stays pending.

Some notifications contain actions. Group invitations can be accepted or
declined, active civic notices can open the Civic Forum, and Realm relationship
decisions can be approved or rejected by eligible citizens. Actions are
validated by the server; the drawer does not own gameplay state.

Admins can raise Elarion custom UI text size server-wide through
`defaults.font-scale-percent` in `config/elarion/core/ui_theme.yml`. The setting
affects custom Elarion screens and HUD text, not vanilla Minecraft menus, chat,
inventory, tablist, or overhead player names.

## Character And Death Foundation

Existing citizens confirm their current identity once. New players create a
name and short character biography before entering normal play. This mandatory
screen waits for other mod screens to close instead of replacing them. If a
name is rejected, the biography text stays in place so the player can correct
only the name. The biography field supports multiple lines and scrolls inside
the box. After creation, new Realm-less players are automatically placed into
the least populated starter Realm. A Realm-choice panel is shown as a future
feature, but manual choosing is disabled for now.

Death leaves a protected Elarion grave and sends the soul to the Underworld.
Interact with the grave after returning to open its recovery screen. Items that
do not fit remain protected; expired graves move their contents to admin-assisted
recovery storage. True Death archives the current character and unlocks fresh
character creation after 24 real hours.
