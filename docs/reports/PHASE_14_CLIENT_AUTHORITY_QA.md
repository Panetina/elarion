# Phase 14 Client Parity, Onboarding, And Authority QA

Date: 2026-07-18

## Scope

Phase 14 Slice 5 verified the synchronized development server, generic client,
Client One, and Client Two without changing persisted schemas or deployment
state. Runtime evidence is under
`build/phase14/client-authority-20260718-013346/`.

## Results

### Runtime parity: PASS

- `syncDevRuntimeMods` completed before launch.
- `runtime-parity.json` records 21 server jars, 20 generic jars, 24 Client One
  jars, and 20 Client Two jars.
- Every shared gameplay jar has the same hash. Generic and Client Two are
  identical.
- Expected differences are limited to server/admin Axiom and WorldEdit,
  client-only Mod Menu, and Client One's Axiom, WorldEdit, Iris, and Sodium QA
  additions.
- All tested clients joined without registry or custom-payload mismatch.

### Fresh onboarding: PASS after one bounded fix

The first generated account was `Player903`. Character Creation prefilled that
Minecraft username even though the configured roleplay-name policy rejects
digits, so Continue could not succeed. Core now passes any proposed prefill
through `NicknameService.safePrefill`; invalid account names produce an empty
field while valid existing nicknames remain normalized.

The post-fix generic account `Player745` completed this flow:

1. Character Creation opened with a blank name and `Unnamed Ember` preview.
2. `Phase Scout` plus a biography passed server validation.
3. Continue opened Realm Placement without teleporting.
4. Core assigned the least-populated eligible Realm, `Wilderness II`.
5. Confirm Placement alone performed living-world teleport.

Evidence:

- `fresh-onboarding-empty-prefill.png`
- `fresh-realm-assignment.png`
- `fresh-living-world.png`
- `generic-after-fix-latest.log`

### Server authority: PASS for rejection and authorized resync

- Client Two (`ElarionPlayer1`, character `Second Ember`) did not receive the
  `/e` command tree. `/e panel` remained an unknown command at position 1.
- Client One (`ElarionAdmin`) successfully granted and activated the temporary
  `news_reporter` title through `/e title set`.
- The Character Menu immediately displayed `News Reporter` as the active title,
  proving the authoritative mutation was synchronized back to the client.
- Cleanup restored `government_monarch` and revoked `news_reporter`; the citizen
  file contains only the original titles after cleanup.

Evidence:

- `client-two-command-not-exposed.png`
- `client-one-authorized-title-result.png`
- `client-one-title-resynced-character-menu.png`
- `client-one-live-latest.log`
- `server-live-latest.log`

### Simultaneous two-window observation: PARTIAL

Both attempts to keep Client One and Client Two rendering concurrently ended in
the same native `EXCEPTION_ACCESS_VIOLATION` inside `glfw.dll`. No Elarion Java
frame, packet error, registry mismatch, or server exception preceded either
failure. Each stable client ran correctly by itself. The raw JVM reports are
preserved as `client-one-glfw-crash.log` and `client-two-glfw-crash.log`.

This is a QA-host limitation for this slice. It prevents claiming a simultaneous
two-window screenshot pass, but it does not invalidate the separately observed
permission rejection or server-to-client title synchronization.

## Log Classification

- Server startup and shutdown contained no `ERROR` entries and saved every
  managed dimension.
- The two `moved too quickly` warnings occurred during server-authored Realm
  teleport and did not disconnect either player.
- Missing JEI/Sodium compatibility-target classes are optional third-party
  mixin probes; those mods are not in the affected runtime.
- No `Received ... unknown registry entries`, custom-payload mismatch, or
  connection-loss contract error was found.

## Files Changed

- `platform/core/src/main/java/panetina/elarion/core/service/NicknameService.java`
- `platform/core/src/main/java/panetina/elarion/core/service/CharacterLifecycleService.java`
- `platform/core/src/test/java/panetina/elarion/core/service/NicknameServiceTest.java`
- Phase 14 planning, verification, UI journal/audit, index, and handoff docs.

## Verification

- `:platform:core:test` (including the new nickname prefill regression)
- `verifyAiContext` (12/12 cases, 95.96% aggregate savings)
- Live server plus generic, Client One, and Client Two launches
- Controlled `/stop` with all dimensions saved
- Runtime jar hash manifest and targeted server/client log scans

## Deferred

- `ElarionTextInput` supports bounded entry, append, backspace, paste, and
  scrolling, but not selection or arbitrary caret navigation. This did not
  block the corrected flow and remains a separate UI-input enhancement.
- Simultaneous dual-window QA should be retried on a host/runtime that can keep
  two GLFW clients stable. Do not weaken authority checks to accommodate the QA
  environment.
