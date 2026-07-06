# Realm System

Purpose: represent canonical Realm identity, membership, leaders, relationships, visibility, and Realm-scoped player identity.

Main classes: `CitizenService`, `RealmService`, `RealmRelationshipService`, `RealmSpawnService`, `RealmDeliveryService`, `RealmProtectionService`.

Entry points: Core initialization and `addons/realms`.

Commands: `/e realm ...`, `/e citizen ...`, Realm chat commands.

Network packets: identity sync payloads.

GUI/screens: none central yet; future Government/Ledger/Atlas views should consume Core Realm APIs.

Storage/persistence: `config/elarion/core/realms.yml`, `world/elarion/citizens`, Realm runtime/delivery storage.

Dependencies: Core config, Core history, ability system, messaging.

Related systems: Government, Offerings, Economy treasuries, NPCs, Atlas, Chronicle.

Extension points: Core APIs, abilities, registry conditions/actions, public history.

Risks: addons copying Realm membership or relationship truth; direct file reads instead of Core APIs.

Do not duplicate this system by creating: addon-local Realm records, separate team managers, or parallel membership storage.
