# Elarion Networking System

Purpose: move authoritative snapshots and bounded client intents between server and client.

Main classes: payload records under Core and addons,
`platform/core/.../network/ElarionPacketCodecs.java`, and addon initializers
registering payloads.

Entry points: `PayloadTypeRegistry`, `ServerPlayNetworking`, `ClientPlayNetworking`.

Commands: none.

Network packets: Core identity/theme/character/admin/collection/notification
payloads, NPC dialogue/visual, Shrine UI/contribution, Government UI,
Portal prompt/status/visual, Underworld grave/status, and Mount input.

GUI/screens: NPC and Shrine screens consume network snapshots.

Storage/persistence: none directly; packets refresh from service-owned state.

Dependencies: Fabric networking API, Core/addon services.

Related systems: GUI, NPCs, Offerings, Identity, future Market/Atlas/Government.

Extension points: new typed payload records and server receivers.

Risks: client-trusted mutation, oversized snapshots, repeated packet frameworks.

Do not duplicate this system by creating: custom packet buses when Fabric typed payloads and existing Elarion patterns are enough.

Cross-addon server integrations use `ElarionApi.system().events()` and
`ElarionDomainEvent`; they do not require client packets. Addons emit compact
events after authoritative mutations. Player-facing notification projections
use `ElarionApi.notifications()`, while Core handles persistence and sync.
Events must not carry large snapshots or become a substitute for owner APIs.

## Packet Safety

All Elarion payload string fields must use `ElarionPacketCodecs.writeString`
and `ElarionPacketCodecs.readString` with an explicit maximum length. Server
snapshots are still server-authored, but the codec boundary strips unsafe
control/format characters and clamps strings before decode can crash a client.

Payload list counts must be read through `ElarionPacketCodecs.readBoundedCount`
or equivalent explicit clamping. Client intent packets must carry bounded IDs,
enums, and numeric values only; the server revalidates ownership, distance,
permissions, session state, and current world state before mutating anything.
