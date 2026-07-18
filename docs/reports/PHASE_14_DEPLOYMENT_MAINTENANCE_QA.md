# Phase 14 Deployment And Maintenance QA

Date: 2026-07-18

## Deployment Plan Gate

`dev/tools/deploy-live-server.ps1` now supports `-PlanOnly`. It creates the
same SHA-256 manifest and SFTP stage/commit/rollback batches as a release but
returns before opening SFTP.

Final dry-run evidence (`build/deploy-live-server/20260718-005521/`):

- 42 server jars in the manifest.
- 43 stage uploads: 42 jars plus the manifest.
- Stage commands target only the timestamped `.elarion-staging` release.
- Commit commands rename live `mods` into a timestamped backup before promoting
  staged `mods`.
- Rollback commands restore the backup to live `mods`.
- The corrected plan-only run completed without SFTP output or remote changes.
- Running `gradlew deployLiveServerMods` without both safety properties failed
  before release preparation or script execution.

## Defect Found During Dry Run

The first `-PlanOnly` implementation placed its guard after the stage call. Two
verification attempts created isolated remote staging releases
`20260718-005248` and `20260718-005342`; neither executed commit/promotion and
the live `mods` directory was not changed. The guard is now before every SFTP
invocation. The remote staging directories were deliberately not deleted
without owner approval.

## Maintenance Guide

`docs/systems/EXTENSION_GUIDE.md` now documents the current source-backed
workflow for adding:

- config domains and safe runtime appliers
- Admin Panel providers/actions
- shared UI components
- placeholders
- profile sections
- Chronicle events/renderers
- NPC actions/conditions
- notifications
- addon integrations

Each section preserves canonical ownership, server authority, bounded read
paths, compatibility, focused tests, and documentation obligations.

## Result

PASS for Phase 14 Slice 8. Safe local release planning and the guarded live
promotion path are both documented and verified. Actual live promotion remains
an explicit owner-approved operation performed only while the live server is
stopped.

