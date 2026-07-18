# Character Lifecycle System

Status: Implemented foundation; manual integration verification required.

## Purpose

Core owns the account-to-character lifecycle. Existing citizens complete one
migration confirmation, new accounts create a character, and dead characters
are archived before restart-safe True Death cleanup. A new character becomes
available after a 24-hour real-time cooldown.

## Main Classes

- `platform/core/.../service/CharacterLifecycleService.java`
- `platform/core/.../model/CharacterLifecycleRecord.java`
- `platform/core/.../model/CharacterArchiveRecord.java`
- `platform/core/.../storage/CharacterLifecycleStorage.java`
- `platform/core/.../client/CharacterCreationFlow.java`
- `platform/core/.../client/CharacterCreationScreen.java`
- `platform/core/.../client/CharacterRealmAssignmentScreen.java`
- `platform/core/.../client/ui/ElarionTextInput.java`
- `platform/core/.../service/CharacterRealmAssignmentPlanner.java`

## Storage And Networking

- Runtime: `world/elarion/core/characters/state.json`
- S2C: `CharacterCreationRequirementPayload`
- S2C: `CharacterRealmAssignmentPayload`
- C2S: `CharacterCreationStatusRequestPayload`
- C2S: `CharacterCreationSubmitPayload`
- C2S: `CharacterRealmAssignmentConfirmPayload`

On client join, Core requests the current character-creation status once the
client world/player are available. The client queues the mandatory screen and
opens it immediately when no other screen is active. It never replaces another
mod's screen, but it no longer waits for player movement or combat input before
showing the required character flow. Validation and lifecycle state remain
server-authoritative.

During `TRUE_DEAD_COOLDOWN`, the same mandatory client flow opens in a read-only
cooldown state. Gameplay restrictions remain active until the cooldown expires
or an admin runs the recreate-now command.

Character creation uses the shared Core `ElarionTextInput` helper for the name
and biography fields. Validation errors keep the local field contents intact.
Core only sends a character-name prefill when the proposed account name or
existing nickname already satisfies the configured name character, structure,
and length rules. Invalid generated/test account names such as `Player745`
produce an empty field instead of a prefilled value the server will reject.
The biography field is multiline, bounded to 500 characters, and scrolls inside
the input area instead of growing the screen. The client screen uses the
approved Option A civic onboarding layout: step strip, identity preview,
bounded biography panel, biography readiness check, and bottom action band.

After a new character is accepted, Core automatically assigns Realm-less new
players to one of `realm1`, `realm2`, or `realm3`. The selected Realm is chosen
from the least-populated Realm set with random tie-breaking. Existing migrated
citizens keep their current Realm. The follow-up Realm assignment panel is
presentation only; future manual Realm choosing is shown disabled and does not
own membership state. The current placement screen renders up to three vertical
Realm rows, highlights the assigned Realm, shows server-provided population
counts, and owns the final continue action. Character creation sends the Realm
assignment panel before closing the creation screen. `Confirm Placement` sends
`CharacterRealmAssignmentConfirmPayload`; Core teleports the player only from
that confirm packet, not directly from the `realm-assigned` citizen-change
event.

## Reset Contract

Addon cleanup uses stable, idempotent reset-handler IDs. Each completed step is
persisted before the next step, so a crash resumes without duplicating estate
transfers or leaving partial authority state.

- Core clears character-scoped identity, titles, abilities, progression, and stats.
- Economy transfers bank balance to the former Realm treasury or burns neutral estates.
- Groups removes membership and disbands a group led by the dead character.
- Government vacates offices and promotes a monarchy heir when present.
- Underworld clears sessions, Soul state, graves, and recovery-vault state.

Globally unique titles are retired permanently. Dead RP names remain reserved.

## Commands

```text
/e character inspect <player>
/e character recreate-now <player>
/e character archive <player>
/e test character finish-cooldown <player>
/e test character trigger-true-death <player>
/e test character reset <player>
/e test character force-active <player>
```

`/e character recreate-now <player>` skips the True Death cooldown and makes the
official character creation flow available immediately. It does not restore the
dead character. `/e test character force-active <player>` is dev-only repair for
manual tests after `/e citizen` edits; it must not be used as a production
restore path.

## Do Not Duplicate

Do not create addon-owned character IDs, creation screens, True Death reset
coordinators, dead-name registries, or character archives. Register an
idempotent cleanup handler through `ElarionApi.characters()` instead.
