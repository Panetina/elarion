# Test Commands

Development-only reset and timing commands for local server testing.

[Home](../README.md) | [Admin](README.md) | [Commands](commands.md)

`Admin-only`, `Development-only`, `Manual verification needed`

## Live UI Screenshot QA

For UI checks that need real server state, start the dev server and admin
client, then join Multiplayer using the saved `localhost` server entry:

```text
.\gradlew.bat runServer
.\gradlew.bat runClientOne
```

After opening the target UI, capture the Minecraft window:

```text
powershell -NoProfile -ExecutionPolicy Bypass -File .\dev\tools\capture-minecraft-window.ps1 -Output build\ui-qa\<screen-name>.png
```

Common UI entry points are `/e panel`, `/charactermenu`, the notification HUD
rail, linked Shrine blocks, Civic Forum or Seat of Rule blocks, placed NPCs,
configured portal routes, and Underworld graves/tombs.

## Commands

```text
/e test shrine reset
/e test shrine reset realm1
/e test realm global realm1 on
/e test realm global realm1 off
/e test government reset
/e test government reset realm1
/e test government advance realm1
/e test death send Player544 5
/e test death return Player544
/e test death fracture Player544
/e test death fracture add Player544
/e test death fracture remove Player544
/e test death clear Player544
/e test death reset-state
/e test character finish-cooldown Player544
/e test character trigger-true-death Player544
/e test character reset Player544
/e test character force-active Player544
/e test mounts summon <type>
/e test mounts debug
/e test mounts clear-nearby
```

## Shrine Reset

Use this when you want to test Shrine/Foundation progression from zero without
rebuilding the Shrine or relinking it.

```text
/e test shrine reset realm1
```

Clears:

- Offering progress
- Offering donation history
- Foundation flags such as `foundation_i`, `foundation_ii`, `foundation_iii`
- Offering-owned global-access flags
- Shrine display-name overrides such as quest memorial names

Preserves:

- Shrine blocks
- Shrine links
- Portal route definitions and coordinates
- NPC placements
- Government state
- Quest state

Shrine reset also reverses project-owned milestone side effects. A gate opened
by a Shrine `elarion:portal_unlock` milestone is locked again, but its route
coordinates are not deleted.

## Realm Global Access

Use this when you need to test global-stage behavior without recompleting a
Shrine:

```text
/e test realm global realm1 on
/e test realm global realm1 off
```

This toggles the Offering-owned global-access flag used by World notifications,
global-stage portal/title behavior, and staged tablist visibility. It does not
reset Shrine progress or Government state.

## Government Reset

Use this when you want to test Civic Forum, Seat of Rule, Realm name voting,
Government form voting, elections, audience requests, law votes, and laws from
zero.

```text
/e test government reset realm1
```

Clears:

- voted Realm name/tag/color
- active Government form
- office holders
- votes
- audience/vote records
- civic records/laws

Preserves:

- Civic Forum and Seat blocks
- Shrine progress and Foundation flags
- Portal routes and coordinates
- NPC placements
- Guild membership and identity

Government reset does not touch Portal routes, NPC placements, Shrine blocks,
or Offering progress.

## Government Advance

Use this to avoid waiting 24 hours during local testing.

```text
/e test government advance realm1
```

It advances the current Government stage:

- proposal/nomination window to voting window
- voting window to resolution
- empty expired vote to reopened stage

## Underworld Test Commands

Use these to test death, corpse recovery, Underworld timers, Soul Fractures,
and True Death without waiting on normal gameplay.

```text
/e test death send Player544 5
/e test death return Player544
/e test death fracture Player544
/e test death fracture add Player544
/e test death fracture remove Player544
/e test death clear Player544
/e test death reset-state
```

- `send` binds the player to the Underworld for the provided minutes.
- `return` force-returns the player even if the active session was already
  cleared.
- `fracture` and `fracture add` add one Soul Fracture.
- `fracture remove` removes one Soul Fracture.
- `clear` clears the player's Soul Fractures.
- `reset-state` clears Underworld sessions, Soul Fractures, corpses, grave
  markers, and combat tags.

## Character Test Commands

Use these for character-creation and True Death verification.

```text
/e test character finish-cooldown Player544
/e test character trigger-true-death Player544
/e test character reset Player544
/e test character force-active Player544
```

- `finish-cooldown` clears the character creation cooldown.
- `trigger-true-death` moves the current character into the True Death flow.
- `reset` clears the current test character state so the creation UI appears
  again.
- `force-active` marks the lifecycle active after manual Ember edits. It does
  not restore archived character state or undo addon cleanup.

## Mount Tests

Use these while testing the native Fabric mount foundation:

```text
/e test mounts summon airship
/e test mounts summon bee
/e test mounts summon chinese_dragon
/e test mounts summon ghast
/e test mounts summon hot_air_balloon
/e test mounts summon scifi_bike
/e test mounts summon wyvern
/e test mounts debug
/e test mounts clear-nearby
```

- `summon` spawns the chosen mount type, assigns you as owner, and starts
  riding it. Valid types are `airship`, `bee`, `chinese_dragon`, `ghast`,
  `hot_air_balloon`, `scifi_bike`, and `wyvern`.
- `debug` reports the mount type, passenger count, owner, latest
  forward/side/jump/sneak values, speed, velocity, grounded state, effective
  grounded state, yaw, and seat mode.
- `clear-nearby` removes Elarion mounts within the local cleanup radius.
- While riding, `Space` ascends, `Shift` descends, and `R` safely dismisses the
  owned mount and despawns it. While unmounted, `R` summons/remounts the active
  unlocked mount selected in the Collection menu.

## Recommended Full Reset Order

To retest Shrine plus Government from zero while keeping placed blocks and
coordinates:

```text
/e test government reset realm1
/e test shrine reset realm1
```

Then complete Shrine levels again and walk through the Civic Forum flow.

## Source-Backed Notes

- Core test root: [../../platform/core/src/main/java/panetina/elarion/core/command/ElarionCommands.java](../../platform/core/src/main/java/panetina/elarion/core/command/ElarionCommands.java)
- Offering test command contributor: [../../addons/offerings/src/main/java/panetina/elarion/addons/offerings/command/OfferingCommands.java](../../addons/offerings/src/main/java/panetina/elarion/addons/offerings/command/OfferingCommands.java)
- Government test command contributor: [../../addons/government/src/main/java/panetina/elarion/addons/government/command/GovernmentCommands.java](../../addons/government/src/main/java/panetina/elarion/addons/government/command/GovernmentCommands.java)
- Underworld test command contributor: [../../addons/underworld/src/main/java/panetina/elarion/addons/underworld/command/DeathCommands.java](../../addons/underworld/src/main/java/panetina/elarion/addons/underworld/command/DeathCommands.java)
- Character test command contributor: [../../platform/core/src/main/java/panetina/elarion/core/command/CharacterCommands.java](../../platform/core/src/main/java/panetina/elarion/core/command/CharacterCommands.java)
- Mount test command contributor: [../../addons/mounts/src/main/java/panetina/elarion/addons/mounts/ElarionMountsAddon.java](../../addons/mounts/src/main/java/panetina/elarion/addons/mounts/ElarionMountsAddon.java)
