# Phase 14 Optional-Addon Absence QA

Date: 2026-07-18

## Scope

This slice verified documented loader-level optional-addon behavior without
removing jars from `dev/run/**` or `build/export/**`. Each case runs through an
otherwise-disabled per-module Loom server task in an isolated directory under
`build/qa/optional-addon/`.

## Classified Contracts

| Consumer | Provider absent | Contract | Evidence |
| --- | --- | --- | --- |
| Core | Every Elarion addon | Supported | `:platform:core:runServer` reached `Done`, stabilized, and stopped with no addon initialization markers |
| NPCs | Economy | Supported; declared by `suggests` | `:addons:npcs:runServer` loaded shipped NPC/dialogue/trade definitions, reached `Done`, stabilized, and stopped without Economy |
| Voice Chat Hooks | Third-party voice chat | Supported shell behavior | `:addons:voicechat-hooks:runServer` reached `Done` without a voice-chat provider |

Government and Quests are not Economy/Portal absence cases. Government hard
depends on Offerings and Groups; those dependencies require Economy and/or
Portals. Quests hard depends on Offerings. Their Gradle `runtimeOnly` entries
support development runtime composition but do not override Fabric's hard
transitive dependency metadata.

## Confirmed Defect And Correction

The first NPC-without-Economy run reached `Done` and then crashed while loading
the shipped banker dialogue. NPC config validation rejected the two
Economy-owned bank prompt IDs because their provider registry was absent.

`NpcConfigLoader` now recognizes exactly those two shipped prompt IDs as
optional-provider config contracts. It does not register an action handler or
mutate Economy state. Without Economy, the existing unavailable quote,
purchase, sale, and bank providers keep service operations disabled; a forged
execution still cannot resolve an Economy action handler.

## Harness

Run every case:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File dev/tools/optional-addon-qa.ps1
```

Run one case with a custom timeout:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File dev/tools/optional-addon-qa.ps1 `
  -Case npcs-without-economy -StartupTimeoutSeconds 180
```

The harness:

- refuses cleanup outside `build/qa/optional-addon/`;
- creates isolated EULA, server properties, config, world, and logs;
- starts only the selected module and its declared Gradle dependencies;
- requires `Done`, a stabilization window, expected initialization, provider
  absence, no `ERROR`/`FATAL`, and a clean stop;
- writes `optional-addon-qa.log` inside each case directory.

The explicit `-PoptionalAddonQa=true` property enables per-module server tasks
and redirects them into the isolated QA root. Ordinary Gradle runs remain
unchanged.

## Verification

- `:addons:npcs:test`: PASS.
- Core-only isolated startup: PASS.
- NPCs without Economy isolated startup: PASS after the bounded correction.
- Voice Chat Hooks without a provider isolated startup: PASS.
- Combined three-case harness: PASS.
- Full `build`: PASS, 189 actionable tasks.
- `verifyAiContext`: PASS, 12/12 cases at 95.97% aggregate savings.

No persistence format, network payload, command, config file, or public API
changed.

## Files Changed

- `build.gradle`
- `dev/tools/optional-addon-qa.ps1`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfigLoader.java`
- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/config/NpcBankPresentationMigrationTest.java`
- `PLAN.md`
- `TODO.md`
- `INDEX.md`
- `docs/ai/CURRENT_STATUS.md`
- `docs/architecture/DEPENDENCY_GRAPH.md`
- `docs/architecture/REVAMP_REMAINING_ROADMAP.md`
- `docs/addons/npcs.md`
- `docs/addons/voicechat-hooks.md`
- `docs/reports/PHASE_14_VERIFICATION_MATRIX.md`
- `docs/reports/PHASE_14_OPTIONAL_ADDON_ABSENCE_QA.md`

No public API, config key, packet, command, or persistent schema was added or
changed. `OPTIMIZATION_TRACKER.md` and the UI journal were deliberately not
changed because this slice found no performance issue and changed no UI.

## Remaining Limits

- The absence matrix proves loader initialization, config loading, bounded
  fallback construction, stabilization, and shutdown. It does not replace a
  future client interaction test for disabled NPC bank/trade controls.
- Voice Chat Hooks remains an honest shell; no third-party adapter behavior
  exists to verify yet.
- New optional provider/consumer edges must declare loader metadata, expose an
  unavailable server-side fallback, and add a harness case before being
  documented as supported.

## Subsequent Slice

Phase 14 Slice 7 representative UI/resource QA subsequently completed; see
`PHASE_14_UI_RESOURCE_QA.md`.
