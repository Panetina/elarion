package panetina.elarion.addons.npcs.command;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import panetina.elarion.addons.npcs.config.NpcConfigException;
import panetina.elarion.addons.npcs.model.NpcDefinition;
import panetina.elarion.addons.npcs.model.PlacedNpcRecord;
import panetina.elarion.addons.npcs.service.NpcDefinitionService;
import panetina.elarion.addons.npcs.service.NpcPlacementService;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.command.CommandOutput;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class NpcCommands {
    private NpcCommands() {
    }

    public static LiteralArgumentBuilder<ServerCommandSource> create(
            ElarionApi api,
            NpcDefinitionService definitions,
            NpcPlacementService placements
    ) {
        return literal("npc")
                .requires(source -> source.hasPermissionLevel(4))
                .then(literal("reload").executes(context -> reload(context.getSource(), api, definitions, placements)))
                .then(literal("place")
                        .then(argument("definition", StringArgumentType.word())
                                .suggests((context, builder) -> CommandSource.suggestMatching(
                                        definitions.npcs().stream()
                                                .filter(NpcDefinition::enabled)
                                                .map(NpcDefinition::id), builder))
                                .executes(context -> place(
                                        context.getSource(),
                                        placements,
                                        StringArgumentType.getString(context, "definition"),
                                        null))
                                .then(literal("yaw")
                                        .then(argument("yaw", FloatArgumentType.floatArg(-180.0F, 180.0F))
                                                .executes(context -> place(
                                                        context.getSource(),
                                                        placements,
                                                        StringArgumentType.getString(context, "definition"),
                                                        FloatArgumentType.getFloat(context, "yaw")))))
                                .then(facingArgument()
                                        .executes(context -> place(
                                                context.getSource(),
                                                placements,
                                                StringArgumentType.getString(context, "definition"),
                                                yawFromWord(
                                                        StringArgumentType.getString(context, "facing"),
                                                        context.getSource().getPlayerOrThrow()))))))
                .then(literal("remove")
                        .then(literal("nearest")
                                .executes(context -> removeNearest(context.getSource(), placements)))
                        .then(idArgument("id", placements)
                                .executes(context -> remove(
                                        context.getSource(),
                                        placements,
                                        StringArgumentType.getString(context, "id")))))
                .then(literal("face")
                        .then(idArgument("id", placements)
                                .executes(context -> face(
                                        context.getSource(),
                                        placements,
                                        StringArgumentType.getString(context, "id")))))
                .then(literal("rotate")
                        .then(idArgument("id", placements)
                                .then(literal("yaw")
                                        .then(argument("yaw", FloatArgumentType.floatArg(-180.0F, 180.0F))
                                                .executes(context -> rotate(
                                                        context.getSource(),
                                                        placements,
                                                        StringArgumentType.getString(context, "id"),
                                                        FloatArgumentType.getFloat(context, "yaw")))))
                                .then(facingArgument()
                                        .executes(context -> rotate(
                                                context.getSource(),
                                                placements,
                                                StringArgumentType.getString(context, "id"),
                                                yawFromWord(
                                                        StringArgumentType.getString(context, "facing"),
                                                        context.getSource().getPlayerOrThrow()))))))
                .then(literal("tp")
                        .then(idArgument("id", placements)
                                .executes(context -> teleport(
                                        context.getSource(),
                                        definitions,
                                        placements,
                                        StringArgumentType.getString(context, "id")))))
                .then(literal("duplicate")
                        .then(idArgument("id", placements)
                                .executes(context -> duplicate(
                                        context.getSource(),
                                        placements,
                                        StringArgumentType.getString(context, "id"),
                                        null))
                                .then(literal("yaw")
                                        .then(argument("yaw", FloatArgumentType.floatArg(-180.0F, 180.0F))
                                                .executes(context -> duplicate(
                                                        context.getSource(),
                                                        placements,
                                                        StringArgumentType.getString(context, "id"),
                                                        FloatArgumentType.getFloat(context, "yaw")))))
                                .then(facingArgument()
                                        .executes(context -> duplicate(
                                                context.getSource(),
                                                placements,
                                                StringArgumentType.getString(context, "id"),
                                                yawFromWord(
                                                        StringArgumentType.getString(context, "facing"),
                                                        context.getSource().getPlayerOrThrow()))))))
                .then(literal("nearest")
                        .executes(context -> nearest(context.getSource(), definitions, placements)))
                .then(literal("repair")
                        .then(literal("all").executes(context -> repairAll(
                                context.getSource(), placements)))
                        .then(idArgument("id", placements)
                                .executes(context -> repair(
                                        context.getSource(),
                                        placements,
                                        StringArgumentType.getString(context, "id")))))
                .then(literal("list")
                        .executes(context -> list(context.getSource(), placements, "all"))
                        .then(literal("world").executes(context -> list(context.getSource(), placements, "world")))
                        .then(literal("near").executes(context -> list(context.getSource(), placements, "near")))
                        .then(literal("tag")
                                .then(argument("tag", StringArgumentType.word())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(
                                                definitions.npcs().stream()
                                                        .flatMap(definition -> definition.tags().stream())
                                                        .distinct()
                                                        .sorted(), builder))
                                        .executes(context -> list(
                                                context.getSource(),
                                                definitions,
                                                placements,
                                                StringArgumentType.getString(context, "tag"))))))
                .then(literal("inspect")
                        .then(literal("nearest")
                                .executes(context -> inspectNearest(context.getSource(), definitions, placements)))
                        .then(idArgument("id", placements)
                                .executes(context -> inspect(
                                        context.getSource(),
                                        definitions,
                                        placements,
                                        StringArgumentType.getString(context, "id")))))
                .then(literal("move")
                        .then(idArgument("id", placements)
                                .executes(context -> move(
                                        context.getSource(),
                                        placements,
                                        StringArgumentType.getString(context, "id")))))
                .then(literal("set")
                        .then(literal("name")
                                .then(idArgument("id", placements)
                                        .then(argument("name", StringArgumentType.greedyString())
                                                .executes(context -> update(
                                                        context.getSource(),
                                                        placements.rename(
                                                                StringArgumentType.getString(context, "id"),
                                                                StringArgumentType.getString(context, "name")))))))
                        .then(literal("skin")
                                .then(idArgument("id", placements)
                                        .then(argument("skin", StringArgumentType.word())
                                                .suggests((context, builder) -> CommandSource.suggestMatching(
                                                        definitions.skins().stream()
                                                                .map(panetina.elarion.addons.npcs.model
                                                                        .NpcSkinProfile::id), builder))
                                                .executes(context -> update(
                                                        context.getSource(),
                                                        placements.setSkin(
                                                                StringArgumentType.getString(context, "id"),
                                                                StringArgumentType.getString(context, "skin")))))))
                        .then(literal("portrait")
                                .then(idArgument("id", placements)
                                        .then(argument("portrait", StringArgumentType.word())
                                                .suggests((context, builder) -> CommandSource.suggestMatching(
                                                        definitions.portraits().stream()
                                                                .map(panetina.elarion.addons.npcs.model
                                                                        .NpcPortraitProfile::id), builder))
                                                .executes(context -> update(
                                                        context.getSource(),
                                                        placements.setPortrait(
                                                                StringArgumentType.getString(context, "id"),
                                                                StringArgumentType.getString(
                                                                        context, "portrait")))))))
                        .then(literal("dialogue")
                                .then(idArgument("id", placements)
                                        .then(argument("dialogue", StringArgumentType.word())
                                                .suggests((context, builder) -> CommandSource.suggestMatching(
                                                        definitions.dialogues().stream()
                                                                .map(panetina.elarion.addons.npcs.model
                                                                        .DialogueDefinition::id), builder))
                                                .executes(context -> update(
                                                        context.getSource(),
                                                        placements.setDialogue(
                                                                StringArgumentType.getString(context, "id"),
                                                                StringArgumentType.getString(
                                                                        context, "dialogue"))))))))
                .then(literal("dialogue")
                        .then(literal("inspect")
                                .then(argument("dialogue", StringArgumentType.word())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(
                                                definitions.dialogues().stream()
                                                        .map(panetina.elarion.addons.npcs.model
                                                                .DialogueDefinition::id), builder))
                                        .executes(context -> inspectDialogue(
                                                context.getSource(),
                                                definitions,
                                                StringArgumentType.getString(context, "dialogue"))))));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<ServerCommandSource, String> idArgument(
            String name,
            NpcPlacementService placements
    ) {
        return argument(name, StringArgumentType.word())
                .suggests((context, builder) -> CommandSource.suggestMatching(
                        placements.all().stream().map(PlacedNpcRecord::commandId), builder));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<ServerCommandSource, String> facingArgument() {
        return argument("facing", StringArgumentType.word())
                .suggests((context, builder) -> CommandSource.suggestMatching(
                        java.util.List.of("north", "east", "south", "west", "here"), builder));
    }

    private static int reload(
            ServerCommandSource source,
            ElarionApi api,
            NpcDefinitionService definitions,
            NpcPlacementService placements
    ) {
        try {
            definitions.reload(api);
            placements.respawnAll();
            CommandOutput.success(source, "NPC configuration reloaded.", true);
            return 1;
        } catch (NpcConfigException exception) {
            CommandOutput.header(source, "NPC Config Errors");
            exception.errors().forEach(error -> CommandOutput.bullet(source, error));
            return 0;
        } catch (RuntimeException exception) {
            source.sendError(net.minecraft.text.Text.literal(exception.getMessage()));
            return 0;
        }
    }

    private static int place(
            ServerCommandSource source,
            NpcPlacementService placements,
            String definition,
            Float yaw
    ) {
        try {
            ServerPlayerEntity player = source.getPlayerOrThrow();
            PlacedNpcRecord record = yaw == null
                    ? placements.place(player, definition)
                    : placements.place(player, definition, yaw);
            CommandOutput.success(source, "NPC placed.", true);
            CommandOutput.kv(source, "ID", record.commandId());
            CommandOutput.kv(source, "Definition", record.definitionId());
            CommandOutput.kv(source, "Facing yaw", record.yaw());
            CommandOutput.line(source, "Use /e npc inspect " + record.commandId() + " for details.");
            return 1;
        } catch (Exception exception) {
            source.sendError(net.minecraft.text.Text.literal(exception.getMessage()));
            return 0;
        }
    }

    private static int removeNearest(ServerCommandSource source, NpcPlacementService placements) {
        try {
            ServerPlayerEntity player = source.getPlayerOrThrow();
            PlacedNpcRecord nearest = placements.nearest(player, 24.0D).orElse(null);
            if (nearest == null) {
                source.sendError(net.minecraft.text.Text.literal("No placed NPC within 24 blocks."));
                return 0;
            }
            return remove(source, placements, nearest.commandId());
        } catch (Exception exception) {
            source.sendError(net.minecraft.text.Text.literal(exception.getMessage()));
            return 0;
        }
    }

    private static int remove(ServerCommandSource source, NpcPlacementService placements, String id) {
        if (!placements.remove(id)) {
            source.sendError(net.minecraft.text.Text.literal("Unknown placed NPC: " + id));
            return 0;
        }
        CommandOutput.success(source, "NPC removed.", true);
        return 1;
    }

    private static int list(ServerCommandSource source, NpcPlacementService placements, String mode) {
        CommandOutput.header(source, "NPCs");
        CommandOutput.kv(source, "Mode", mode);
        int shown = 0;
        for (PlacedNpcRecord record : placements.all()) {
            if ("world".equals(mode) && source.getWorld() != null
                    && !source.getWorld().getRegistryKey().getValue().toString().equals(record.worldId())) {
                continue;
            }
            if ("near".equals(mode) && source.getEntity() instanceof ServerPlayerEntity player
                    && player.squaredDistanceTo(record.x(), record.y(), record.z()) > 1024.0D) {
                continue;
            }
            CommandOutput.bullet(source, record.commandId() + " | " + record.definitionId() + " | " + record.worldId()
                    + " @ " + Math.round(record.x()) + " " + Math.round(record.y()) + " " + Math.round(record.z()));
            shown++;
        }
        if (shown == 0) CommandOutput.empty(source, "No placed NPCs matched.");
        return shown;
    }

    private static int list(
            ServerCommandSource source,
            NpcDefinitionService definitions,
            NpcPlacementService placements,
            String tag
    ) {
        CommandOutput.header(source, "NPCs");
        CommandOutput.kv(source, "Tag", tag);
        int shown = 0;
        for (PlacedNpcRecord record : placements.all()) {
            NpcDefinition definition = definitions.npc(record.definitionId()).orElse(null);
            if (definition == null || definition.tags().stream().noneMatch(tag::equalsIgnoreCase)) continue;
            CommandOutput.bullet(source, record.commandId() + " | " + record.definitionId() + " | " + record.worldId()
                    + " @ " + Math.round(record.x()) + " " + Math.round(record.y()) + " " + Math.round(record.z()));
            shown++;
        }
        if (shown == 0) CommandOutput.empty(source, "No placed NPCs matched tag " + tag + ".");
        return shown;
    }

    private static int inspect(
            ServerCommandSource source,
            NpcDefinitionService definitions,
            NpcPlacementService placements,
            String id
    ) {
        PlacedNpcRecord record = placements.find(id).orElse(null);
        if (record == null) {
            source.sendError(net.minecraft.text.Text.literal("Unknown placed NPC: " + id));
            return 0;
        }
        CommandOutput.header(source, "NPC");
        CommandOutput.kv(source, "ID", record.commandId());
        CommandOutput.kv(source, "UUID", record.id());
        CommandOutput.kv(source, "Definition", record.definitionId());
        CommandOutput.kv(source, "Entity", record.entityId());
        CommandOutput.kv(source, "World", record.worldId());
        CommandOutput.kv(source, "Position", record.x() + ", " + record.y() + ", " + record.z());
        CommandOutput.kv(source, "Facing yaw", record.yaw());
        definitions.npc(record.definitionId()).ifPresent(definition -> {
            CommandOutput.kv(source, "Name", record.displayName(definition));
            CommandOutput.kv(source, "Skin", record.skin(definition));
            CommandOutput.kv(source, "Portrait", record.portrait(definition));
            CommandOutput.kv(source, "Dialogue", record.dialogue(definition));
            CommandOutput.kv(source, "Tags", definition.tags().isEmpty() ? "(none)" : definition.tags());
            CommandOutput.kv(source, "Required ability",
                    definition.requiredAbility().isBlank() ? "(none)" : definition.requiredAbility());
            CommandOutput.kv(source, "Interaction range",
                    definition.interactionRangeBlocks() <= 0.0D ? "default" : definition.interactionRangeBlocks());
        });
        return 1;
    }

    private static int inspectNearest(
            ServerCommandSource source,
            NpcDefinitionService definitions,
            NpcPlacementService placements
    ) {
        try {
            ServerPlayerEntity player = source.getPlayerOrThrow();
            PlacedNpcRecord nearest = placements.nearest(player, 24.0D).orElse(null);
            if (nearest == null) {
                source.sendError(net.minecraft.text.Text.literal("No placed NPC within 24 blocks."));
                return 0;
            }
            return inspect(source, definitions, placements, nearest.commandId());
        } catch (Exception exception) {
            source.sendError(net.minecraft.text.Text.literal(exception.getMessage()));
            return 0;
        }
    }

    private static int move(ServerCommandSource source, NpcPlacementService placements, String id) {
        try {
            ServerPlayerEntity player = source.getPlayerOrThrow();
            return update(source, placements.move(id, player));
        } catch (Exception exception) {
            source.sendError(net.minecraft.text.Text.literal(exception.getMessage()));
            return 0;
        }
    }

    private static int face(ServerCommandSource source, NpcPlacementService placements, String id) {
        try {
            ServerPlayerEntity player = source.getPlayerOrThrow();
            java.util.Optional<PlacedNpcRecord> updated = placements.face(id, player);
            if (updated.isEmpty()) {
                source.sendError(net.minecraft.text.Text.literal("Unknown placed NPC: " + id));
                return 0;
            }
            CommandOutput.success(source, "NPC now faces you.", true);
            CommandOutput.kv(source, "ID", updated.get().commandId());
            CommandOutput.kv(source, "Facing yaw", updated.get().yaw());
            return 1;
        } catch (Exception exception) {
            source.sendError(net.minecraft.text.Text.literal(exception.getMessage()));
            return 0;
        }
    }

    private static int rotate(ServerCommandSource source, NpcPlacementService placements, String id, float yaw) {
        java.util.Optional<PlacedNpcRecord> updated = placements.rotate(id, yaw);
        if (updated.isEmpty()) {
            source.sendError(net.minecraft.text.Text.literal("Unknown placed NPC: " + id));
            return 0;
        }
        CommandOutput.success(source, "NPC rotated.", true);
        CommandOutput.kv(source, "ID", updated.get().commandId());
        CommandOutput.kv(source, "Facing yaw", updated.get().yaw());
        return 1;
    }

    private static int teleport(
            ServerCommandSource source,
            NpcDefinitionService definitions,
            NpcPlacementService placements,
            String id
    ) {
        try {
            ServerPlayerEntity player = source.getPlayerOrThrow();
            PlacedNpcRecord record = placements.find(id).orElse(null);
            if (record == null) {
                source.sendError(net.minecraft.text.Text.literal("Unknown placed NPC: " + id));
                return 0;
            }
            ServerWorld world = placements.server()
                    .map(server -> server.getWorld(net.minecraft.registry.RegistryKey.of(
                            net.minecraft.registry.RegistryKeys.WORLD,
                            net.minecraft.util.Identifier.of(record.worldId()))))
                    .orElse(null);
            if (world == null) {
                source.sendError(net.minecraft.text.Text.literal("NPC world is not loaded: " + record.worldId()));
                return 0;
            }
            player.teleportTo(new TeleportTarget(
                    world,
                    new Vec3d(record.x(), record.y(), record.z()),
                    Vec3d.ZERO,
                    record.yaw(),
                    0.0F,
                    TeleportTarget.NO_OP));
            CommandOutput.success(source, "Teleported to NPC.", true);
            CommandOutput.kv(source, "ID", record.commandId());
            definitions.npc(record.definitionId()).ifPresent(definition ->
                    CommandOutput.kv(source, "Name", record.displayName(definition)));
            return 1;
        } catch (Exception exception) {
            source.sendError(net.minecraft.text.Text.literal(exception.getMessage()));
            return 0;
        }
    }

    private static int duplicate(ServerCommandSource source, NpcPlacementService placements, String id, Float yaw) {
        try {
            ServerPlayerEntity player = source.getPlayerOrThrow();
            java.util.Optional<PlacedNpcRecord> copy = placements.duplicate(
                    id, player, yaw == null ? player.getYaw() : yaw);
            if (copy.isEmpty()) {
                source.sendError(net.minecraft.text.Text.literal("Unknown placed NPC: " + id));
                return 0;
            }
            CommandOutput.success(source, "NPC duplicated.", true);
            CommandOutput.kv(source, "ID", copy.get().commandId());
            CommandOutput.kv(source, "Definition", copy.get().definitionId());
            CommandOutput.kv(source, "Facing yaw", copy.get().yaw());
            return 1;
        } catch (Exception exception) {
            source.sendError(net.minecraft.text.Text.literal(exception.getMessage()));
            return 0;
        }
    }

    private static int nearest(
            ServerCommandSource source,
            NpcDefinitionService definitions,
            NpcPlacementService placements
    ) {
        return inspectNearest(source, definitions, placements);
    }

    private static int repair(ServerCommandSource source, NpcPlacementService placements, String id) {
        NpcPlacementService.RepairResult result = placements.repair(id);
        if (!result.found()) {
            source.sendError(net.minecraft.text.Text.literal("Unknown placed NPC: " + id));
            return 0;
        }
        return showRepair(source, result);
    }

    private static int repairAll(ServerCommandSource source, NpcPlacementService placements) {
        return showRepair(source, placements.repairAll());
    }

    private static int showRepair(ServerCommandSource source, NpcPlacementService.RepairResult result) {
        CommandOutput.header(source, "NPC Repair");
        CommandOutput.kv(source, "Checked", result.checked());
        CommandOutput.kv(source, "Reused", result.reused());
        CommandOutput.kv(source, "Respawned", result.respawned());
        CommandOutput.kv(source, "Removed stale/duplicate", result.removed());
        return result.checked();
    }

    private static int update(ServerCommandSource source, java.util.Optional<PlacedNpcRecord> record) {
        if (record.isEmpty()) {
            source.sendError(net.minecraft.text.Text.literal("Unknown placed NPC."));
            return 0;
        }
        CommandOutput.success(source, "NPC updated.", true);
        CommandOutput.kv(source, "ID", record.get().commandId());
        return 1;
    }

    private static int inspectDialogue(
            ServerCommandSource source,
            NpcDefinitionService definitions,
            String dialogueId
    ) {
        var dialogue = definitions.dialogue(dialogueId).orElse(null);
        if (dialogue == null) {
            source.sendError(net.minecraft.text.Text.literal("Unknown NPC dialogue: " + dialogueId));
            return 0;
        }
        CommandOutput.header(source, "NPC Dialogue");
        CommandOutput.kv(source, "ID", dialogue.id());
        CommandOutput.kv(source, "Root", dialogue.root());
        CommandOutput.kv(source, "Nodes", dialogue.nodes().size());
        dialogue.nodes().values().forEach(node -> {
            CommandOutput.section(source, node.id());
            CommandOutput.kv(source, "Options", node.options().size());
            node.options().forEach(option -> CommandOutput.bullet(source,
                    option.id()
                            + " -> " + (option.next().isBlank() ? node.id() : option.next())
                            + " | actions=" + option.actions().stream().map(action -> action.type()).toList()));
        });
        return dialogue.nodes().size();
    }

    private static float yawFromWord(String facing, ServerPlayerEntity player) {
        return switch (facing.toLowerCase(java.util.Locale.ROOT)) {
            case "north" -> 180.0F;
            case "east" -> -90.0F;
            case "south" -> 0.0F;
            case "west" -> 90.0F;
            case "here" -> player.getYaw();
            default -> throw new IllegalArgumentException("Facing must be north, east, south, west, here, or yaw <value>.");
        };
    }
}
