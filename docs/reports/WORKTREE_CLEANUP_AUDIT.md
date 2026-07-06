# Worktree Cleanup Audit

Date: 2026-06-23

This audit classifies the current dirty worktree without reverting or deleting
user work.

## Summary

- `git status --short` currently reports about 200 changed or untracked paths.
- No obvious generated cache/build output was identified in the tracked-source
  dirty set.
- Angling changes and `addons/angling/reference/**` remain intentionally out of
  ordinary cleanup scope unless Angling work resumes.
- The repository builds successfully with the current dirty tree after the
  optimization pass.

## Suggested Commit Batches

1. Core character, identity, notification, tablist, and UI foundation changes.
2. Government founding, voting, civic records, and Seat/Civic UI changes.
3. Portal route, HUD, Ancient Gate, schedule, and ticket behavior changes.
4. Underworld death/corpse/soul/True Death foundation changes.
5. Offerings/Shrine progression and reward integration changes.
6. Groups and Confederation support changes.
7. Documentation and wiki updates.
8. Angling work, only when deliberately resuming that addon.

## Cleanup Policy

- Do not revert broad modified files automatically.
- Do not delete untracked source/docs unless the owner confirms they are stale.
- Generated/cache files can be removed if they appear under build output,
  Gradle caches, IDE caches, or temporary folders.
- Keep `docs/`, `wiki/`, `INDEX.md`, `TODO.md`, `RULES.md`, `AGENTS.md`, and
  `CODEX.md` synchronized whenever behavior or ownership changes.

## Current Watch Items

- Several large feature sets are interleaved in the same worktree. Before a
  release branch, split them into reviewable batches.
- Add more command/GameTest coverage before expanding Government authority
  actions or Portal travel logic further.
- Keep Angling reference material out of normal source scans and architecture
  audits.
