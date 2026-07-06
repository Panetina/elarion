# Persistent Data

## Purpose

Store durable Elarion state across reloads and restarts without blocking hot gameplay paths or duplicating ownership.

## Core Minecraft Classes

- `net.minecraft.server.MinecraftServer`
- `net.minecraft.world.PersistentState`
- `net.minecraft.nbt.NbtCompound`

## Core Fabric API Classes

- Fabric Data Attachment API for local attached state.
- Fabric lifecycle events for load/save hooks.

## Common Patterns

- Editable definitions: `config/elarion/...`
- Runtime state: `world/elarion/...`
- Use atomic writes for JSON/YAML snapshots.
- Use JSONL for append-heavy audit logs.
- Use indexes/summaries for player-facing history and search.

## Anti-patterns

- Parsing config during player interaction.
- Loading every history/ledger file for one GUI.
- Storing large histories in block entities.
- Using client state as persistence.

## Example Source Locations

- Core JSON storage: `platform/core/src/main/java/panetina/elarion/core/storage/JsonStateStorage.java`
- Core history: `platform/core/src/main/java/panetina/elarion/core/storage/HistoryStorage.java`
- Economy storage: `addons/economy/src/main/java/panetina/elarion/addons/economy/storage`
- Offering storage: `addons/offerings/src/main/java/panetina/elarion/addons/offerings/storage`

## Elarion Use Cases

- Citizens, identities, titles, rewards, deferred grants.
- Economy balances and transaction state.
- Offering project instances and donation records.
- NPC placements.
- Chronicle indexes and archives.
