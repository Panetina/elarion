# Elarion Systems Notes

This folder contains focused system notes. `INDEX.md` is the navigation entry
point; use this folder only after choosing a system.

For a new AI/new PC handoff, read `../ai/CURRENT_STATUS.md` before drilling
into these system notes.

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
- `Groups.md`
- `Government.md`
- `Underworld.md`
- `Characters.md`
- `Realms.md`
- `Permissions.md`

## Future / Partly Speculative Docs

- `Maps.md`
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
