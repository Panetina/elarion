# Elarion Test Commands

Development-only command contract for resetting and advancing progression tests.

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
powershell -NoProfile -ExecutionPolicy Bypass -File .\dev\tools\minecraft-qa.ps1 -Action scroll -X 655 -Y 375 -Wheel -1 -Count 4
powershell -NoProfile -ExecutionPolicy Bypass -File .\dev\tools\minecraft-qa.ps1 -Action capture -Output build\ui-qa\admin-panel.png
```

The script captures the Minecraft window contents by handle, so another
overlapping desktop window should not appear in the output. If the graphics
driver returns a black image, make the Minecraft window visible and retry with
`-ScreenCapture`.
Use `minecraft-qa.ps1` for faster repeated checks. It wraps focus/maximize,
slash-command sending, client-area clicks, mouse-wheel scrolling, and
screenshot capture around the same Minecraft window target.

Screen entry points for QA:

- `/e panel`: Admin Panel and config edit shell.
- `/collection`: Collection menu.
- HUD notification rail: notification drawer and detail/action states.
- Linked Shrine block interaction: Shrine UI.
- Civic Forum or Seat of Rule block interaction: Government UI.
- Placed NPC interaction: NPC Dialogue.
- Portal-route interaction: Portal Confirmation.
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
- Theocracy faith identity
- active government forms
- office holders
- votes
- citizen proposals
- civic records/laws
- Government pending proposal/civic record references

It also releases Confederation group locks created by delegate seats.

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
  session was already cleared.
- `/e test death reset-state` clears all Underworld sessions, Soul Fractures,
  corpses, grave markers, and combat tags.
- `/e test character force-active <player>` is a local repair escape hatch
  after manual citizen edits. It marks the lifecycle active but does not restore
  archived character state or undo addon cleanup.
- Test commands must remain explicit and isolated from normal admin operations.
