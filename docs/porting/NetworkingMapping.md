# Networking Mapping

NeoForge feature: custom payload/channel registration and packet distributors.

Fabric equivalent: typed `CustomPayload` records with `PayloadTypeRegistry`, `ServerPlayNetworking`, and `ClientPlayNetworking`.

Minecraft classes: `CustomPayload`, `PacketCodec`, `PacketByteBuf`, `ServerPlayerEntity`.

Fabric API classes: `PayloadTypeRegistry`, `ServerPlayNetworking`, `ClientPlayNetworking`.

Porting difficulty: Medium.

Notes:

- Convert NeoForge packets into typed Fabric payload records.
- Keep client-to-server packets as intent only.
- Send authoritative S2C snapshots after server mutation.

Example source locations:

- NeoForge: `external/neoforge/src/main/java/net/neoforged/neoforge/network`
- Fabric: `external/fabric-api/fabric-networking-api-v1`
- Elarion: NPC and Offering network packages.
