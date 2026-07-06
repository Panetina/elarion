# Fabric Networking

## Purpose

Synchronize server-authoritative gameplay state and client UI intent through typed payloads.

## Core Minecraft Classes

- `net.minecraft.network.packet.CustomPayload`
- `net.minecraft.network.PacketByteBuf`
- `net.minecraft.network.codec.PacketCodec`
- `net.minecraft.server.network.ServerPlayerEntity`

## Core Fabric API Classes

- `net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry`
- `net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking`
- `net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking`

## Common Patterns

- Register S2C/C2S payload types during initialization.
- Client sends compact intent: IDs, selected option, amount, or request.
- Server validates current state, range, permissions, and ownership.
- Server sends an authoritative snapshot after mutation.

## Anti-patterns

- Trusting client-side amounts, position, completion state, or permissions.
- Sending entire config definitions every interaction.
- Mutating gameplay state on client packet decode without server-thread execution.
- Creating one-off packet frameworks per addon.

## Example Source Locations

- Elarion Core: `platform/core/src/main/java/panetina/elarion/core/network`
- Elarion NPCs: `addons/npcs/src/main/java/panetina/elarion/addons/npcs/network`
- Elarion Offerings: `addons/offerings/src/main/java/panetina/elarion/addons/offerings/network`
- Fabric API: `external/fabric-api/fabric-networking-api-v1`

## Elarion Use Cases

- Identity sync.
- UI theme sync.
- NPC dialogue and prompt submissions.
- Shrine snapshots and Offering submissions.
- Future market, quest, Atlas, Government, and Ledger screens.
