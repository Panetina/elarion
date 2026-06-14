package panetina.elarion.addons.offerings.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import panetina.elarion.addons.offerings.OfferingsBlocks;
import panetina.elarion.addons.offerings.ShrineOfFoundationBlock;
import panetina.elarion.addons.offerings.model.OfferingInstance;
import panetina.elarion.addons.offerings.model.OfferingProjectDefinition;
import panetina.elarion.addons.offerings.service.OfferingDefinitionService;
import panetina.elarion.addons.offerings.service.OfferingService;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.command.CommandOutput;

import java.util.Locale;
import java.util.function.Supplier;

public final class OfferingCommands {
    private OfferingCommands() {
    }

    public static LiteralArgumentBuilder<ServerCommandSource> create(
            ElarionApi api,
            OfferingDefinitionService definitions,
            OfferingService service
    ) {
        return CommandManager.literal("offerings")
                .requires(source -> source.hasPermissionLevel(4))
                .then(CommandManager.literal("reload").executes(ctx -> run(ctx, () -> {
                    definitions.load();
                    CommandOutput.success(ctx.getSource(), identity().offeringSingular()
                            + " definitions reloaded.", false);
                })))
                .then(CommandManager.literal("projects").executes(ctx -> run(ctx, () -> listProjects(ctx, definitions))))
                .then(CommandManager.literal("inspect")
                        .then(CommandManager.argument("project", StringArgumentType.word())
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(
                                        definitions.all().stream().map(OfferingProjectDefinition::id), builder))
                                .executes(ctx -> run(ctx, () -> inspectProject(ctx, definitions)))))
                .then(CommandManager.literal("instances").executes(ctx -> run(ctx, () -> listInstances(ctx, service))))
                .then(CommandManager.literal("state")
                        .then(CommandManager.argument("instance", StringArgumentType.word())
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(
                                        service.instances().stream().map(OfferingInstance::id), builder))
                                .executes(ctx -> run(ctx, () -> state(ctx, service)))))
                .then(CommandManager.literal("start")
                        .then(CommandManager.literal("realm")
                                .then(CommandManager.argument("realm", StringArgumentType.word())
                                        .suggests((ctx, builder) -> CommandSource.suggestMatching(
                                                api.realms().all().stream().map(realm -> realm.id()), builder))
                                        .then(CommandManager.argument("project", StringArgumentType.word())
                                                .suggests((ctx, builder) -> CommandSource.suggestMatching(
                                                        definitions.all().stream().map(OfferingProjectDefinition::id), builder))
                                                .executes(ctx -> run(ctx, () -> {
                                                    var instance = service.startRealm(
                                                            StringArgumentType.getString(ctx, "realm"),
                                                            StringArgumentType.getString(ctx, "project"),
                                                            playerOrNull(ctx));
                                                    CommandOutput.success(ctx.getSource(),
                                                            "Started " + identity().realmSingular().toLowerCase(Locale.ROOT)
                                                                    + " " + identity().offeringSingular().toLowerCase(Locale.ROOT)
                                                                    + " " + instance.id(), false);
                                                })))))
                        .then(CommandManager.literal("global")
                                .then(CommandManager.argument("project", StringArgumentType.word())
                                        .suggests((ctx, builder) -> CommandSource.suggestMatching(
                                                definitions.all().stream().map(OfferingProjectDefinition::id), builder))
                                        .executes(ctx -> run(ctx, () -> {
                                            var instance = service.startGlobal(
                                                    StringArgumentType.getString(ctx, "project"), playerOrNull(ctx));
                                            CommandOutput.success(ctx.getSource(),
                                                            "Started global " + identity().offeringSingular().toLowerCase(Locale.ROOT)
                                                                    + " " + instance.id(), false);
                                        }))))
                        .then(CommandManager.literal("location")
                                .then(CommandManager.argument("project", StringArgumentType.word())
                                        .suggests((ctx, builder) -> CommandSource.suggestMatching(
                                                definitions.all().stream().map(OfferingProjectDefinition::id), builder))
                                        .executes(ctx -> run(ctx, () -> {
                                            var instance = service.startLocation(
                                                    StringArgumentType.getString(ctx, "project"),
                                                    requirePlayer(ctx));
                                            CommandOutput.success(ctx.getSource(),
                                                    "Started location offering " + instance.id(), false);
                                        })))))
                .then(CommandManager.literal("shrine")
                        .then(CommandManager.literal("link")
                                .then(CommandManager.argument("instance", StringArgumentType.word())
                                        .suggests((ctx, builder) -> CommandSource.suggestMatching(
                                                service.instances().stream().map(OfferingInstance::id), builder))
                                        .executes(ctx -> run(ctx, () -> shrineLink(ctx, service)))))
                        .then(CommandManager.literal("unlink")
                                .executes(ctx -> run(ctx, () -> shrineUnlink(ctx, service))))
                        .then(CommandManager.literal("inspect")
                                .executes(ctx -> run(ctx, () -> shrineInspect(ctx, service))))
                        .then(CommandManager.literal("remove")
                                .executes(ctx -> run(ctx, () -> shrineRemove(ctx, service))))
                        .then(CommandManager.literal("repair")
                                .executes(ctx -> run(ctx, () -> shrineRepair(ctx, service)))))
                .then(CommandManager.literal("delete")
                        .then(CommandManager.argument("instance", StringArgumentType.word())
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(
                                        service.instances().stream().map(OfferingInstance::id), builder))
                                .executes(ctx -> run(ctx, () -> {
                                    String instanceId = StringArgumentType.getString(ctx, "instance");
                                    service.deleteInstance(instanceId, playerOrNull(ctx));
                                    CommandOutput.success(ctx.getSource(), "Deleted "
                                            + identity().offeringSingular().toLowerCase(Locale.ROOT)
                                            + " instance " + instanceId + ".", false);
                                }))))
                .then(CommandManager.literal("reset")
                        .then(CommandManager.argument("instance", StringArgumentType.word())
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(
                                        service.instances().stream().map(OfferingInstance::id), builder))
                                .executes(ctx -> run(ctx, () -> {
                                    var instance = service.reset(StringArgumentType.getString(ctx, "instance"),
                                            playerOrNull(ctx));
                                    CommandOutput.success(ctx.getSource(), "Reset " + instance.id(), false);
                                }))))
                .then(CommandManager.literal("complete")
                        .then(CommandManager.argument("instance", StringArgumentType.word())
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(
                                        service.instances().stream().map(OfferingInstance::id), builder))
                                .executes(ctx -> run(ctx, () -> {
                                    var instance = service.complete(StringArgumentType.getString(ctx, "instance"),
                                            playerOrNull(ctx), true);
                                    CommandOutput.success(ctx.getSource(), "Completed " + instance.id(), false);
                                }))));
    }

    private static void listProjects(
            CommandContext<ServerCommandSource> ctx,
            OfferingDefinitionService definitions
    ) {
        CommandOutput.header(ctx.getSource(), identity().offeringSingular() + " Projects");
        for (var project : definitions.all()) {
            CommandOutput.bullet(ctx.getSource(), project.id() + " - " + project.displayName()
                    + " (" + project.scope().name().toLowerCase(Locale.ROOT) + ")");
        }
    }

    private static void inspectProject(
            CommandContext<ServerCommandSource> ctx,
            OfferingDefinitionService definitions
    ) {
        String id = StringArgumentType.getString(ctx, "project");
        var project = definitions.find(id).orElseThrow(() -> new IllegalArgumentException("Unknown project " + id));
        CommandOutput.header(ctx.getSource(), "Project " + project.id());
        CommandOutput.kv(ctx.getSource(), "Name", project.displayName());
        CommandOutput.kv(ctx.getSource(), "Scope", project.scope());
        CommandOutput.kv(ctx.getSource(), "Repeatable", project.repeatable());
        CommandOutput.kv(ctx.getSource(), "Multiple Instances", project.allowMultipleInstances());
        CommandOutput.kv(ctx.getSource(), "Levels", project.levels().size());
        for (var level : project.levels()) {
            CommandOutput.section(ctx.getSource(), level.displayName() + " (" + level.id() + ")");
            for (var requirement : level.requirements()) {
                CommandOutput.bullet(ctx.getSource(), requirement.key() + " " + requirement.count());
            }
            for (var milestone : level.milestones()) {
                CommandOutput.bullet(ctx.getSource(), "milestone " + milestone.id() + " -> " + milestone.type());
            }
        }
    }

    private static void listInstances(CommandContext<ServerCommandSource> ctx, OfferingService service) {
        CommandOutput.header(ctx.getSource(), identity().offeringSingular() + " Instances");
        if (service.instances().isEmpty()) {
            CommandOutput.empty(ctx.getSource(), "No " + identity().offeringSingular().toLowerCase(Locale.ROOT)
                    + " instances.");
            return;
        }
        for (var instance : service.instances()) {
            CommandOutput.bullet(ctx.getSource(), instance.id() + " project=" + instance.projectId()
                    + " level=" + (instance.activeLevelId().isBlank() ? "-" : instance.activeLevelId())
                    + " scope=" + instance.scope().name().toLowerCase(Locale.ROOT)
                    + (instance.realmId().isBlank() ? "" : " realm=" + instance.realmId())
                    + (instance.completed() ? " completed" : ""));
        }
    }

    private static void state(CommandContext<ServerCommandSource> ctx, OfferingService service) {
        String id = StringArgumentType.getString(ctx, "instance");
        var instance = service.findInstance(id).orElseThrow(() -> new IllegalArgumentException("Unknown instance " + id));
        var progress = service.progress(id);
        CommandOutput.header(ctx.getSource(), identity().offeringSingular() + " State " + id);
        CommandOutput.kv(ctx.getSource(), "Project", instance.projectId());
        CommandOutput.kv(ctx.getSource(), "Level", instance.activeLevelId().isBlank() ? "-" : instance.activeLevelId());
        CommandOutput.kv(ctx.getSource(), "Scope", instance.scope());
        CommandOutput.kv(ctx.getSource(), identity().realmSingular(), instance.realmId().isBlank() ? "-" : instance.realmId());
        CommandOutput.kv(ctx.getSource(), "Anchor", instance.anchorId().isBlank() ? "-" : instance.anchorId());
        CommandOutput.kv(ctx.getSource(), "Complete", progress.complete());
        CommandOutput.section(ctx.getSource(), "Progress");
        for (var row : progress.rows()) {
            CommandOutput.bullet(ctx.getSource(), row.key() + ": " + row.current() + " / " + row.required());
        }
    }

    private static int run(CommandContext<ServerCommandSource> ctx, Runnable runnable) {
        try {
            runnable.run();
            return Command.SINGLE_SUCCESS;
        } catch (Exception exception) {
            ctx.getSource().sendError(net.minecraft.text.Text.literal(exception.getMessage()));
            return 0;
        }
    }

    private static void shrineLink(CommandContext<ServerCommandSource> ctx, OfferingService service) {
        ServerPlayerEntity player = requirePlayer(ctx);
        BlockPos origin = shrineOrigin(player);
        var anchor = service.linkAnchorAt(StringArgumentType.getString(ctx, "instance"),
                player.getWorld().getRegistryKey().getValue().toString(), origin, player);
        CommandOutput.success(ctx.getSource(), "Linked " + identity().shrineOfFoundation()
                + " to " + anchor.instanceId(), false);
    }

    private static void shrineUnlink(CommandContext<ServerCommandSource> ctx, OfferingService service) {
        ServerPlayerEntity player = requirePlayer(ctx);
        BlockPos origin = shrineOrigin(player);
        service.unlinkAnchorAt(player.getWorld().getRegistryKey().getValue().toString(), origin, player);
        CommandOutput.success(ctx.getSource(), "Unlinked " + identity().shrineOfFoundation() + ".", false);
    }

    private static void shrineInspect(CommandContext<ServerCommandSource> ctx, OfferingService service) {
        ServerPlayerEntity player = requirePlayer(ctx);
        BlockPos origin = shrineOrigin(player);
        String worldId = player.getWorld().getRegistryKey().getValue().toString();
        CommandOutput.header(ctx.getSource(), identity().shrineOfFoundation());
        CommandOutput.kv(ctx.getSource(), "World", worldId);
        CommandOutput.kv(ctx.getSource(), "Origin", origin.getX() + " " + origin.getY() + " " + origin.getZ());
        var anchor = service.findAnchorAt(worldId, origin);
        if (anchor.isEmpty()) {
            CommandOutput.kv(ctx.getSource(), "Linked " + identity().offeringSingular(), "-");
            return;
        }
        CommandOutput.kv(ctx.getSource(), "Linked " + identity().offeringSingular(), anchor.get().instanceId());
    }

    private static void shrineRemove(CommandContext<ServerCommandSource> ctx, OfferingService service) {
        ServerPlayerEntity player = requirePlayer(ctx);
        BlockPos origin = shrineOrigin(player);
        String worldId = player.getWorld().getRegistryKey().getValue().toString();
        var deleted = service.deleteLinkedInstanceAt(worldId, origin, player);
        ShrineOfFoundationBlock.removeStructure(player.getWorld(), origin);
        CommandOutput.success(ctx.getSource(), "Removed " + identity().shrineOfFoundation()
                + deleted.map(instance -> " and deleted " + instance.id() + ".").orElse("."), false);
    }

    private static void shrineRepair(CommandContext<ServerCommandSource> ctx, OfferingService service) {
        ServerPlayerEntity player = requirePlayer(ctx);
        BlockPos origin = shrineOrigin(player);
        var state = player.getWorld().getBlockState(origin);
        if (!state.isOf(OfferingsBlocks.SHRINE_OF_FOUNDATION) || !state.get(ShrineOfFoundationBlock.PART_X).equals(0)
                || !state.get(ShrineOfFoundationBlock.PART_Y).equals(0)
                || !state.get(ShrineOfFoundationBlock.PART_Z).equals(0)) {
            throw new IllegalArgumentException("Look at an existing " + identity().shrineOfFoundation() + ".");
        }
        ShrineOfFoundationBlock.placeStructure(player.getWorld(), origin, state.get(ShrineOfFoundationBlock.FACING));
        CommandOutput.success(ctx.getSource(), "Repaired " + identity().shrineOfFoundation() + " parts.", false);
    }

    private static BlockPos shrineOrigin(ServerPlayerEntity player) {
        HitResult hit = player.raycast(6.0D, 0.0F, false);
        if (!(hit instanceof BlockHitResult blockHit)) {
            throw new IllegalArgumentException("Look at a " + identity().shrineOfFoundation() + ".");
        }
        var state = player.getWorld().getBlockState(blockHit.getBlockPos());
        if (!state.isOf(OfferingsBlocks.SHRINE_OF_FOUNDATION)) {
            throw new IllegalArgumentException("Look at a " + identity().shrineOfFoundation() + ".");
        }
        return ShrineOfFoundationBlock.origin(blockHit.getBlockPos(), state);
    }

    private static ServerPlayerEntity playerOrNull(CommandContext<ServerCommandSource> ctx) {
        try {
            return ctx.getSource().getPlayer();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static ServerPlayerEntity requirePlayer(CommandContext<ServerCommandSource> ctx) {
        ServerPlayerEntity player = playerOrNull(ctx);
        if (player == null) throw new IllegalArgumentException("This command requires an in-game player.");
        return player;
    }

    private static panetina.elarion.core.model.ServerIdentityConfig identity() {
        return ElarionApi.get().serverIdentity();
    }

}
