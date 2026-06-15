# TODO

Active implementation work only. Keep this file current and avoid long-term design
notes here.

## High Priority

- Add Government command/GameTest coverage for full in-game Civic Forum / Seat
  actions, vote resolution, runoff behavior, and election result persistence.
- Finish Government vote lifecycle polish around proposal-window handoff and
  clear player-facing messaging for empty or expired votes.

## Medium Priority

- Audit each active addon when next touched and document its domain events,
  player-facing notification projections, silent/noise exclusions, and future
  integration hooks. Do not add notifications merely to satisfy a quota.
- Build the future Quest addon against the reserved Core Quest notification
  category; do not create another HUD or inbox.
- Add/finish command and GameTest coverage for features that mutate persistence.
- Keep NPC, Offering, Government, Portal, and Groups docs synchronized with
  source changes.
- Keep `AGENTS.md`, `INDEX.md`, `CODEX.md`, `docs/systems/README.md`, and
  `wiki/addons/README.md` synchronized when addons or root Markdown files are
  added, removed, or promoted.
- Add a focused web/bridge architecture document before website integration
  starts.
- Split oversized services/screens only along existing ownership boundaries:
  Government vote logic, Portal travel/schedule logic, Offering contribution
  flow, and large UI screens are the main candidates.

## Low Priority

- Expand system docs only when a subsystem has grown enough to justify it.
- Add source line references to docs during the next code change that touches
  each system.

## Future Ideas

- Atlas / political map system.
- Quest system should publish accepted, assigned, random, timed, and abortable
  quest reminders through Core notifications instead of adding a separate HUD.
- Rich Chronicle, newspaper, ledger, and rumor read models.
- More civic and authority UI modules.

## Technical Debt

- Keep the root documentation split small and deliberate.
- Keep future work sliced narrowly to reduce token/credit cost without lowering
  correctness: subsystem first, focused reads, focused tests, then full build
  only when needed.
- Avoid reintroducing duplicate managers, duplicate state owners, or alternate
  networking stacks.
- Do not let local reference notes drift away from the source tree.
- Keep shell addons clearly marked as shells until they own real behavior.
- Ignore Angling reference cleanup for now; it is not part of the active
  Government/Shrine/Portal path.
- Government UI session validation has focused unit coverage; full command and
  GameTest coverage is still pending.
- Notification persistence, category filtering, reward-provider composition,
  Government/Realm actions, Group invites, Offering milestone notices, title
  notices, gated World visibility, and Portal unlock/status notices have
  focused unit coverage. Full in-game action/GameTest coverage remains future
  work.
- Core now exposes `ElarionDomainEvent` for future cross-addon consumers.
  Existing addon events should migrate incrementally when their owning feature
  is next changed; do not perform a broad behavior-changing retrofit.
- When an addon changes status, update `AGENTS.md`, `INDEX.md`, `CODEX.md`,
  `docs/addons/README.md`, `wiki/addons/README.md`, and any relevant admin wiki
  page in the same pass.

## Unknown / Needs Investigation

- Which remaining Government UI screens should become real gameplay in the next
  slice.
- Whether proposal-only Government windows with no ballots should auto-expire,
  auto-extend, or require admin/test intervention.
