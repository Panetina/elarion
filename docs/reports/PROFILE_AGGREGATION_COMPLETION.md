# Profile Aggregation Completion

Phase 11 completed the bounded server-authoritative Character Menu profile
boundary.

## Ownership And Queries

- Core owns profile aggregation, visibility filtering, wire limits, and the
  Character Menu shell. It does not copy addon state.
- NPCs owns personal NPC relationships and constant-time per-player/per-faction
  reputation summaries. Personal relationship appears in NPC dialogue;
  faction totals use the dedicated Reputation tab.
- Government owns office terms and maintains an in-memory holder index. Profile
  reads are capped at 32 terms and do not query Chronicle/history archives.
- Groups uses its existing constant-time player-to-group index.
- Mounts contributes public unlock counts from its owner-maintained player map.
  Read methods no longer create empty collection records.
- Core owns identity, Realm, active title, title count, and progression fields.

## Visibility

Core filters every section, field, and card before synchronization. Public
identity, Realm, titles, current/former public offices, Groups, and Mount counts
are public. NPC personal relationships and faction internals remain NPC-owned;
only the player's own Reputation tab is sent as part of their Character Menu.
Existing self/admin fields remain filtered by `ProfileVisibility`.

## Player Links

`CitizenProfileRequestPayload` carries a typed target UUID. The server resolves
the citizen and applies public/self/admin filtering. Non-self linked requests
return one `CitizenProfileOpenPayload` containing both the Character Menu and
the filtered target profile, preventing the previous self-request race.
`CitizenProfileClientRequests.open(UUID)` is the client extension point for
history, Chronicle, and menu rows that already possess a server-authored UUID.
Consumers must never parse names or display text into identity.

## Bounds

- Core caps sections, fields, cards, and packet strings.
- Government term reads are indexed by holder and capped.
- Group and Mount reads are direct map lookups.
- NPC faction reads are direct per-player map lookups.
- Profile opening performs no filesystem IO, world scans, history scans, or
  state mutation.

## Verification

Focused tests cover Government holder bounds/order, Group membership, Mount
counts/non-mutating reads, and combined profile-open packet round trips. The
full `build verifyAiContext` graph passed: 190 tasks; 12/12 context cases and
95.94% aggregate reduction.

