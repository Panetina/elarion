# Elarion Test Commands

Development-only command contract for resetting and advancing progression tests.

Administrative world reset is separate from the test namespace: `/e reset world
<world>` previews and confirms regeneration of a managed world, removes its
placed NPCs, Shrine/offering records, and portal endpoints, then recreates the
world from its existing definition. Its world argument uses server-authored
managed-world completion. It does not delete definitions or configs. Each reset
backup includes the Fantasy persistent-dimension files, world-scoped addon
state, and an atomic, backup-relative `manifest.json` recovery inventory. If
regeneration or a world-scoped handler fails, Core restores those files,
reopens the managed world, and asks each state owner to reload before reporting
the original reset failure.

Administrative player reset is also separate from the test namespace:
`/e reset players` backs up and removes all player-owned vanilla and Elarion
state, plus `ops.json`, `whitelist.json`, and both layers of `usercache.json`.
Its preview explicitly warns that console or bridge access is required to
re-whitelist players and restore operators after confirmation. It never deletes
the whole `world/elarion` tree or shared world/Realm/NPC infrastructure.

These commands are OP level 4 only and live under `/e test`. They are not
player-facing gameplay commands. Use them to reset Shrine/Foundation and
Government state during local verification without deleting Portal coordinates,
NPC placements, or other coordinate-based setup.

The OP-only `/e panel` Admin Panel exposes common reset and repair actions
through a themed GUI. Its Danger Zone `Reset Everything` button is
runtime/progression only and follows the same preservation rules as these test
commands.

## Live UI Screenshot QA

Launch the full dev client as the admin test player:

```text
.\gradlew.bat runServer
.\gradlew.bat runClientOne
```

Join Multiplayer using the saved `localhost` server entry. After opening the
target Elarion UI in Minecraft, capture the window:

```text
powershell -NoProfile -ExecutionPolicy Bypass -File .\dev\tools\capture-minecraft-window.ps1 -Output build\ui-qa\<screen-name>.png
powershell -NoProfile -ExecutionPolicy Bypass -File .\dev\tools\minecraft-qa.ps1 -Action command -Command '/e panel'
powershell -NoProfile -ExecutionPolicy Bypass -File .\dev\tools\minecraft-qa.ps1 -Action click -X 490 -Y 88
powershell -NoProfile -ExecutionPolicy Bypass -File .\dev\tools\minecraft-qa.ps1 -Action click -Button right -X 960 -Y 520
powershell -NoProfile -ExecutionPolicy Bypass -File .\dev\tools\minecraft-qa.ps1 -Action key -Keys '{ENTER}'
powershell -NoProfile -ExecutionPolicy Bypass -File .\dev\tools\minecraft-qa.ps1 -Action move -X 318 -Y 250
powershell -NoProfile -ExecutionPolicy Bypass -File .\dev\tools\minecraft-qa.ps1 -Action scroll -X 655 -Y 375 -Wheel -1 -Count 4
powershell -NoProfile -ExecutionPolicy Bypass -File .\dev\tools\minecraft-qa.ps1 -Action capture -Output build\ui-qa\admin-panel.png
powershell -NoProfile -ExecutionPolicy Bypass -File .\dev\tools\npc-trade-qa.ps1 -Action setup -World minecraft:overworld -ClickX 960 -ClickY 540 -TradeButtonX 960 -TradeButtonY 503 -OutputDir build\ui-qa\npc-trade-qa
powershell -NoProfile -ExecutionPolicy Bypass -File .\dev\tools\npc-trade-qa.ps1 -Action capture-trader -World minecraft:overworld -ClickX 960 -ClickY 540 -TradeButtonX 960 -TradeButtonY 503 -OutputDir build\ui-qa\npc-trade-qa
```

The script captures the Minecraft window contents by handle, so another
overlapping desktop window should not appear in the output. If the graphics
driver returns a black image, make the Minecraft window visible and retry with
`-ScreenCapture`.
If the client starts with a tiny/white framebuffer or the main menu appears
blank, focus Minecraft and toggle `F11` once to force a window/framebuffer
reset before restarting or changing shader state.
Use `minecraft-qa.ps1` for faster repeated checks. It wraps focus/maximize,
slash-command sending, native-cursor movement for hover/tooltips, client-area
left/right clicks, keyboard actions, mouse-wheel scrolling, and screenshot
capture around the same Minecraft window target. Later actions preserve an
already maximized window.

Use `npc-trade-qa.ps1` after joining the dev server to rebuild the default
banker/trader test pair through normal OP `/e npc` commands. It is a QA wrapper,
not a production shortcut. The helper assumes the client is in-world; pass
`-CloseScreens` only when deliberately clearing an open UI before setup.

Screen entry points for QA:

- `/e panel`: Admin Panel and config edit shell.
- `C` keybind: Character Menu / unlockables menu. `/charactermenu`
  are hidden client aliases for manual use and are not server command
  recommendations.
- HUD notification rail: notification drawer and detail/action states.
- Linked Shrine block interaction: Shrine UI.
- Civic Forum or Seat of Rule block interaction: Government UI.
- Placed NPC interaction: NPC Dialogue.
- Portal-route interaction: Portal Confirmation. For prompt-only visual QA use
  `/e portal preview <neutral|nether|end|fee|blocked|return>`.
- Grave/tomb interaction: Grave Recovery.

## Commands

```text
/e test shrine reset
/e test shrine reset <realm>
/e test realm global <realm> on|off
/e test government reset
/e test government reset <realm>
/e test government advance <realm>
/e test death send <player> <minutes>
/e test death return <player>
/e test death fracture <player>
/e test death fracture add <player>
/e test death fracture remove <player>
/e test death clear <player>
/e test death reset-state
/e test character finish-cooldown <player>
/e test character trigger-true-death <player>
/e test character reset <player>
/e test character force-active <player>
/e test mounts summon <type>
/e test mounts debug
/e test mounts clear-nearby
```

## Shrine Reset

`/e test shrine reset` clears:

- Offering instance progress
- Offering donation history
- Offering-owned Realm flags such as `foundation_i`, `foundation_ii`,
  `foundation_iii`, and global-access flags
- Shrine display-name overrides such as quest memorial names

It preserves:

- Shrine blocks
- Shrine links/anchors
- Offering instance IDs
- Portal route definitions and endpoint coordinates
- NPC placements
- Government state
- Quest state

Shrine reset also reverses project-owned milestone side effects. A route opened
by a Shrine `elarion:portal_unlock` milestone is locked again, but its endpoint
coordinates are not deleted.

`/e test shrine reset <realm>` limits the reset to Realm-scoped Offering
instances and flags for that Realm.

## Realm Global Access

`/e test realm global <realm> on|off` manually toggles the Offering-owned
global-access Realm flag. Use it to test World notifications, global-stage
tablist visibility, title announcements, and portal/global access behavior
without completing the Shrine progression again.

This command does not alter Shrine progress, Government state, Portal
coordinates, NPC placements, or Realm identity.

## Government Reset

`/e test government reset` clears:

- voted Realm name/tag/color overlays
- active government forms
- office holders
- votes
- audience requests
- civic records/laws
- Government pending proposal/civic record references

It does not touch Portal routes, NPC placements, Shrine blocks, or Offering
progress.

It preserves:

- Civic Forum and Seat of Rule blocks
- Shrine progress and Foundation flags
- Portal routes and endpoint coordinates
- NPC placements
- Group membership and group identity

`/e test government reset <realm>` limits the reset to one Realm.

## Government Advance

`/e test government advance <realm>` advances the current Civic Forum timing
window for that Realm:

- proposal/nomination phase -> vote phase
- active vote phase -> resolution
- empty expired vote -> reopened stage

Use it only for local verification of name, color, government form, faith, and
founding election flows.

## Ownership

- Core owns the `/e test` root command extension point.
- Offerings contributes `shrine` test commands.
- Government contributes `government` test commands.
- Underworld contributes `death` test commands for local death-loop
  verification.
- Core contributes `character` test commands for local character lifecycle and
  True Death cooldown verification.
- Mounts contributes `mounts summon`, `mounts debug`, and
  `mounts clear-nearby` for local mount spawning, input/grounding diagnostics,
  and cleanup.
- Valid mount test types are `airship`, `bee`, `chinese_dragon`, `ghast`,
  `hot_air_balloon`, `scifi_bike`, and `wyvern`.
- `/e test death return <player>` force-returns the player even if the active
  death session was already cleared, but never bypasses an active moderation
  banishment; use `/unbanish <player>` explicitly.
- `/e test death reset-state` clears all Underworld sessions, Soul Fractures,
  corpses, grave markers, and combat tags.
- `/e test character force-active <player>` is a local repair escape hatch
  after manual citizen edits. It marks the lifecycle active but does not restore
  archived character state or undo addon cleanup.
- Test commands must remain explicit and isolated from normal admin operations.
