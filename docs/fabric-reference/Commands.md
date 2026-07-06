# Fabric Commands

## Purpose

Register Brigadier commands for player and OP/admin workflows.

## Core Minecraft Classes

- `net.minecraft.server.command.CommandManager`
- `net.minecraft.server.command.ServerCommandSource`
- `com.mojang.brigadier.builder.LiteralArgumentBuilder`
- `com.mojang.brigadier.arguments.*`

## Core Fabric API Classes

- `net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback`

## Common Patterns

- Register commands once at startup.
- Gate admin commands with `requires(source -> source.hasPermissionLevel(4))`.
- Use suggestions for IDs such as players, Realms, NPCs, projects, titles, abilities, and rewards.
- Use shared output helpers for readable diagnostics.

## Anti-patterns

- Adding a new root command for every addon when `/e <system>` fits.
- Hiding permission checks in handler code only.
- Returning dense, unreadable status strings.
- Mutating state without persistence tests.

## Example Source Locations

- Core root commands: `platform/core/src/main/java/panetina/elarion/core/command`
- NPC commands: `addons/npcs/src/main/java/panetina/elarion/addons/npcs/command/NpcCommands.java`
- Offering commands: `addons/offerings/src/main/java/panetina/elarion/addons/offerings/command/OfferingCommands.java`
- Fabric API: `external/fabric-api/fabric-command-api-v2`

## Elarion Use Cases

- `/e npc ...`
- `/e offerings ...`
- `/e economy ...`
- `/e perf ...`
- Player chat commands such as `/rc`, `/ac`, `/pm`, `/w`, and `/yell`.
