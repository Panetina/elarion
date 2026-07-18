# Codex Project Context

Compact command and navigation reference. Policy lives in `RULES.md`; task
routing lives in `docs/ai/routes.json`.

## Primary Paths

- Core: `platform/core`.
- Addons: `addons/*`.
- GameTests: `tests/gametest`.
- Technical contracts: `docs/systems`, `docs/addons`, and `docs/architecture`.
- Current handoff: `docs/ai/CURRENT_STATUS.md`.
- Remaining revamp roadmap: `docs/architecture/REVAMP_REMAINING_ROADMAP.md`.
- AI context command: `dev/tools/ai-context.ps1`.
- Config definitions: `config/elarion`; runtime state: `world/elarion`.

## Context Command

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\dev\tools\ai-context.ps1 -Task "trace NPC dialogue persistence" -Mode explore -BudgetTokens 6000 -Format markdown
```

Use `-Format json` for agent/tool integration. The command prioritizes current
dirty files, selects task authority docs, returns full pivot files and compact
supporting outlines, and refuses unsafe low-context results.

## Focused Verification

```text
.\gradlew.bat :platform:core:compileJava
.\gradlew.bat :platform:core:test
.\gradlew.bat :addons:npcs:test
.\gradlew.bat :addons:economy:test
.\gradlew.bat :addons:offerings:test
.\gradlew.bat :addons:quests:test
.\gradlew.bat :addons:government:test
.\gradlew.bat :tests:gametest:runGameTest
.\gradlew.bat verifyAiContext
```

Full verification and exports:

```text
.\gradlew.bat build
.\gradlew.bat exportMods
.\gradlew.bat rebuildExportMods
```

Safe live-release planning without SFTP or remote mutation:

```text
powershell -NoProfile -ExecutionPolicy Bypass -File .\dev\tools\deploy-live-server.ps1 -PlanOnly
```

See `docs/systems/LiveDeployment.md`; actual promotion still requires the
stopped-server Gradle guard and explicit owner approval.

Phase 14 evidence and ordered runtime checks are tracked in
`docs/reports/PHASE_14_VERIFICATION_MATRIX.md`. Do not treat `build` as proof of
GameTest, dedicated startup, restart, optional-addon, multiplayer, or UI status.

## Live UI QA

Build changed modules, stop stale dev processes, start `runServer`, then
`runClientOne`, join the saved localhost server, and use:

```text
powershell -NoProfile -ExecutionPolicy Bypass -File .\dev\tools\minecraft-qa.ps1 -Action capture -Output build\ui-qa\capture.png
powershell -NoProfile -ExecutionPolicy Bypass -File .\dev\tools\minecraft-qa.ps1 -Action command -Command '/e panel'
```

Do not rebuild shared dev jars while a server/client still has them open. If
Fabric outputs or logs are locked, stop owning processes, run Gradle `--stop`,
then `cleanDevRunLocks`.

## Fast Discovery

- Networking: Core/addon `network` packages and `docs/systems/Networking.md`.
- UI: Core `client/ui`, addon `client` packages, and `docs/systems/GUI.md`.
- Persistence: Core/addon `storage` packages and
  `docs/systems/Persistence.md`.
- Commands: Core/addon `command` packages and `docs/commands.md`.
- Config: `*Config*` classes, descriptors, and `docs/config.md`.
- History/Chronicle: `docs/systems/Chronicles.md` and Core public-history APIs.
- Placeholders: use the `placeholders` route in `docs/ai/routes.json`; audit
  owners and runtime costs before adding registry contracts or migrations.

## Reference Rule

`external/**` and `addons/angling/reference/**` are excluded from ordinary
search, build-quality decisions, and context capsules. Use them only for an
explicit upstream comparison or Angling port.
