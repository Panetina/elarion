# Realm System

Purpose: represent canonical Realm identity, membership, leaders, relationships, visibility, and Realm-scoped player identity.

Main classes: `CitizenService`, `RealmService`, `RealmRelationshipService`, `RealmSpawnService`, `RealmDeliveryService`, `RealmProtectionService`.

Entry points: Core initialization and `addons/realms`.

Commands: `/e realm ...`, `/e citizen ...`, Realm chat commands.

Network packets: identity sync payloads.

GUI/screens: none central yet; future Government/Ledger/Atlas views should consume Core Realm APIs.

Storage/persistence: `config/elarion/core/realms.yml`, `world/elarion/citizens`,
`world/elarion/addon-state/realms/realm-state.json`, and Realm delivery storage.
Realm runtime loading treats null collections as empty, removes unusable
relationship/decision/vote rows, normalizes hidden Realm IDs, and retains valid
adjacent relationships, visibility state, decisions, and votes. The JSON shape
and Core ownership remain unchanged.

Persisted Realm decisions remain canonical. Core rebuilds a runtime-only
pending-decision projection on bind, maintained on propose, vote, and expiry.
Pending Realm queries use the relevant Realm subset; deadline expiry visits only
due decisions, preserving existing `createdAt` presentation ordering and
canonical order for simultaneous expirations.

Realm spawn defaults:

| Realm | Spawn world | Spawn |
| --- | --- | --- |
| `realm1` | `elarion:realm_world_1` | `-367, 75, 138` |
| `realm2` | `elarion:realm_world_2` | `3000, 128, 3920` |
| `realm3` | `elarion:realm_world_3` | `6061, 84, 5122` |

Core owns these Realm spawn destinations. The Worlds addon owns the matching
managed-world seeds, borders, and generation settings.

Dependencies: Core config, Core history, ability system, messaging.

Related systems: Government, Offerings, Economy treasuries, NPCs, Atlas, Chronicle.

Extension points: Core APIs, abilities, registry conditions/actions, public history.

Risks: addons copying Realm membership or relationship truth; direct file reads instead of Core APIs.

Do not duplicate this system by creating: addon-local Realm records, separate team managers, or parallel membership storage.
