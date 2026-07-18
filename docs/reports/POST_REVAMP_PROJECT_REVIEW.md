# Post-Revamp Project Review

Date: 2026-07-18

## Scope

Final source-backed review after completion of Phases 0-14. This review used
the current dirty worktree, final verification evidence, persistence and
networking implementations, known-risk documents, and focused static searches.
No production source was changed.

## Remediation Status

All findings were addressed in the 2026-07-18 post-review hardening
checkpoint:

- intentional source, tests, documentation, assets, and canonical development
  resource packs are included in a reproducible Git checkpoint;
- malformed shared JSON state is quarantined before fallback and atomic save
  failures propagate to owner services;
- Government, Quest, and Underworld runtime states persist schema version `1`
  and reject unsupported future versions;
- profile requests are limited to four per player per second and failed
  contributors produce bounded diagnostics;
- Underworld display and expiry work uses deduplicated, capped runtime queues;
- all identified `A Ember`/`a Ember` player-facing grammar was corrected.

Focused tests cover each new contract. Broad build and GameTest evidence is
recorded in the final handoff for this checkpoint.

## Findings

### High - The current implementation is not recoverable from Git

The worktree contains 526 status entries, including 164 modified tracked Java
files, 282 untracked Java files, and roughly 9,700 untracked files overall.
About 9,300 of those files are the Core GUI texture library; the remaining
untracked files include important Economy, profile, UI, bridge, test, and
deployment source. The last commit predates most of the revamp.

A fresh clone, branch switch, workstation loss, or incomplete manual copy would
not reproduce the build that passed Phase 14. Before further feature work or a
live release, create a reviewed checkpoint commit or an equivalent verified
backup that includes intentional source, tests, docs, and assets while
excluding runtime/build output and secrets.

### High - Corrupt JSON state can be replaced by an empty state

`JsonStateStorage.read` logs malformed JSON and returns the same fallback used
for a missing file. Callers cannot distinguish first startup from corruption.
Government and Underworld bind their fallback state and immediately save it,
which can overwrite the original corrupt file with an empty/repaired state.
No pre-replacement backup is made.

`JsonStateStorage.writeAtomic` also catches save errors and returns `void`.
Government mutations then continue to emit history, notifications, and success
results even when persistence failed. This creates a false-success and
restart-loss path.

The shared storage contract should return explicit load/save results, preserve
or quarantine corrupt files, and prevent success publication after failed
durability. This requires its own migration-safe hardening slice.

### Medium - Several canonical state files have no explicit schema version

Government, Quest, and Underworld state records are representative examples of
JSON state that relies on Gson defaults and service-side null repair without a
persisted schema version. This works for additive changes but cannot reliably
distinguish old, unsupported, partially migrated, or future formats.

Do not retrofit every store at once. Add versions only when each owner next
changes its persisted format, with backup, migration, unsupported-version
behavior, and old-data tests.

### Medium - Public profile requests have no request throttling

Any connected client can repeatedly send `CitizenProfileRequestPayload`.
Every request runs aggregation on the server thread, invokes all registered
contributors, builds a Collection snapshot, and sends a bounded response.
Payload sizes and section counts are bounded, but request frequency is not.

Add a reusable per-player request budget or cooldown before this profile-open
path is exposed broadly through double-click links. Contributor exceptions are
currently swallowed without bounded diagnostics, which also obscures broken
addon projections.

### Medium - Underworld performs whole-corpse periodic scans

Every second, Underworld walks every active corpse to refresh tomb displays;
every ten seconds it walks every corpse again for expiration. The collection is
eventually reduced by expiry, but the work scales with total active deaths and
performs world/block-entity lookups on the server thread.

This is acceptable for the current prototype population but should move to
dirty tomb updates plus an expiry queue before a high-population live launch.

### Low - Ember terminology has player-facing grammar regressions

Government Chronicle fallback text contains four `A Ember` strings, and the
private-message help text contains `a Ember`. These should use `An Ember`/`an
Ember`. Stable technical `citizen` identifiers and Java type names are
intentional compatibility contracts and are not part of this finding.

## Verification State

The current implementation compiles and its automated baseline is strong:

- full Gradle build passed;
- all required GameTests passed;
- dedicated startup and controlled restart passed;
- optional-addon absence cases passed;
- release export composition and deployment planning passed;
- representative UI and resource-pack QA passed;
- AI context verification passed 12/12 cases.

The final matrix still truthfully marks command integration, live malformed
config application, and simultaneous multiplayer authority observation as
partial. Actual live deployment remains deliberately unexecuted.

## Original Readiness Assessment

Architecture and feature foundations are substantially better than the
pre-revamp project and are suitable for continued bounded development. The
project is not yet ready to be treated as a recoverable production release
until the Git checkpoint and corrupt-state/save-failure finding are addressed.
The remaining medium and low findings can be handled as owner-specific
hardening work when development resumes.
