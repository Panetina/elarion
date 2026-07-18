# Phase 14 Runtime Baseline

Date: 2026-07-18

## Scope

This report records Phase 14 Slices 2 and 3: the headless GameTest baseline
and one non-destructive dedicated-server startup/shutdown audit. It does not
claim restart persistence, multiplayer, optional-addon, or UI verification.

## GameTest

Command:

```powershell
.\gradlew.bat runGameTests --console=plain
```

Result: `PASS`. Fabric reported `All 1 required tests passed` and Gradle
completed successfully.

The single registered test is a broad server-context integration scenario. It
checks Core services, Economy transactions, indexed History and Chronicle
generation, managed worlds, command registration/removal/permissions,
Offerings lifecycle commands, NPC definitions and admin commands, Portal
commands, Groups commands, Government definitions/state/founding/reset, and
world-border isolation.

Harness corrections required before the baseline could pass:

- Added the required Portals addon to the GameTest runtime because Government
  loads Offerings and Offerings declares Portals as required.
- Replaced the hard-coded `realm1` fixture with the first configured Realm ID.
- Distinguished commands that successfully return an empty result (`0`) from
  Brigadier syntax/permission failures.
- Registered a real Realm Ember before testing Government office assignment.

These changes affect test configuration and fixtures only. No production
behavior or persisted schema changed.

## Dedicated Server

Procedure: launched `runServer` against the existing dev runtime, waited for
Minecraft readiness, sent `stop`, and inspected `dev/run/logs/latest.log`.
The dev world was not reset or deleted.

Result: `PASS`.

- Minecraft reached `Done (1.770s)`.
- Core and every configured Elarion addon initialized.
- All six managed worlds opened: three Realm worlds, lobby, Underworld, and
  Worldheart.
- Shutdown completed and `All dimensions are saved` was logged.
- The final log contained `0` error-level entries.

The final log contained 12 non-fatal warnings:

- one Forge Config API recommendation for client-only Mod Menu on the server;
- two development remapper warnings from third-party mods;
- three missing development refmap warnings from third-party mods;
- one Windows PerfOS registry warning;
- four expected offline-mode warnings in the local dev runtime;
- one startup `Can't keep up` warning while managed worlds were opening.

None is an Elarion contract violation or startup failure. RoadWeaver also emits
garbled localized `INFO` text in the Windows console; that is upstream display
noise, not an error.

## Remaining Boundary

The next slice is the controlled restart and persistence smoke. It must use a
backed-up runtime and representative owner state, so its recommended model is
High. Client parity, multiplayer authority, optional-addon absence, and live UI
QA remain separate later slices.
