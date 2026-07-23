# Elarion Systems Notes

This folder contains focused system notes. `INDEX.md` is the navigation entry
point; use this folder only after choosing a system.

For a new AI/new PC handoff, read the bounded `../ai/CURRENT_STATUS.md`, then
use `../ai/routes.json` or `dev/tools/ai-context.ps1` to select only the relevant
system notes.

## Current Verified System Docs

- `NPCs.md`
- `Quests.md`
- `CommunityContribution.md`
- `Treasury.md`
- `Chronicles.md`
- `GUI.md`
- `Networking.md`
- `Persistence.md`
- `CatchTelemetry.md`
- `Metrics.md`
- `Groups.md`
- `Government.md`
- `Underworld.md`
- `Characters.md`
- `Realms.md`
- `Permissions.md`
- `PLACEHOLDERS.md`
- `LiveDeployment.md`
- `EXTENSION_GUIDE.md`

## Future / Partly Speculative Docs

- `Atlas.md` (authoritative; only its placeholder shell is implemented)
- `Maps.md` (compatibility pointer to `Atlas.md`)
- `Teams.md`

## Reference Exclusion

`addons/angling/reference/**` is upstream reference material and should not be
used as Elarion source truth unless Angling porting work is explicitly active.

When a system doc describes future behavior, mark it as future or UNVERIFIED
instead of presenting it as implemented source truth.

## Maintenance Rule

Use the canonical maintenance matrix in `../../RULES.md`. Update system docs
when behavior crosses addon boundaries or changes canonical ownership,
networking, UI primitives, persistence, commands, permissions, events, or
notifications.
