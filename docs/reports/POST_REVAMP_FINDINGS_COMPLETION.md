# Post-Revamp Findings Completion

Date: 2026-07-18

## Result

All findings from `POST_REVAMP_PROJECT_REVIEW.md` are resolved in the final
hardening checkpoint.

## Changes

- `JsonStateStorage` quarantines unreadable JSON before fallback and propagates
  atomic write failures.
- Government, Quest, and Underworld runtime states persist schema version `1`,
  normalize versionless legacy state, and reject unsupported future versions.
- Character Menu profile requests are limited to four requests per player per
  second. Contributor failure logging is bounded until recovery.
- Underworld tomb display and corpse expiry processing use deduplicated,
  bounded queues instead of recurring whole-corpse scans.
- Player-facing `A Ember` grammar was corrected.
- Canonical Excalibured art/font runtime packs moved from ignored `dev/run` to
  tracked `dev/resourcepacks`; runtime synchronization copies from that source.
- Intentional source, tests, documentation, references, and assets are included
  in the final Git checkpoint. Runtime output, caches, backups, local deployment
  configuration, and secrets remain ignored.

## Verification

- Focused Core persistence, profile, and request-limiter tests passed.
- Focused Government, Quest, and Underworld schema/queue tests passed.
- `syncDevRuntimeMods --rerun-tasks` passed; both resource packs matched their
  generic and stable-client copies by SHA-256.
- Full `build` passed: 189 actionable tasks.
- `verifyAiContext` passed all 12 cases with 95.97% aggregate context savings.
- `runGameTests` passed all required tests.

## Remaining Risk

No review finding remains open. Existing feature-specific manual QA and future
live deployment work remain in `TODO.md`; they are not regressions introduced
by this hardening checkpoint.
