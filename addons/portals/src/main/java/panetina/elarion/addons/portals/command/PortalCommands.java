package panetina.elarion.addons.portals.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import panetina.elarion.addons.portals.PortalContent;
import panetina.elarion.addons.portals.model.PortalArrival;
import panetina.elarion.addons.portals.model.PortalEndpoint;
import panetina.elarion.addons.portals.model.PortalTravelDirection;
import panetina.elarion.addons.portals.model.PortalUiConfig;
import panetina.elarion.addons.portals.network.PortalTravelPromptPayload;
import panetina.elarion.addons.portals.service.PortalDefinitionService;
import panetina.elarion.addons.portals.service.PortalRouteService;
import panetina.elarion.addons.portals.service.PortalSelectionService;
import panetina.elarion.core.command.CommandOutput;

import java.time.Duration;
import java.util.UUID;

public final class PortalCommands {
    static final String[] PREVIEW_STATES = {
            "neutral", "nether", "end", "fee", "blocked", "return"
    };

    private PortalCommands() {
    }

    public static LiteralArgumentBuilder<ServerCommandSource> create(
            PortalDefinitionService definitions,
            PortalRouteService routes,
            PortalSelectionService selections
    ) {
        return CommandManager.literal("portal")
                .requires(source -> source.hasPermissionLevel(4))
                .then(CommandManager.literal("reload").executes(ctx -> run(ctx, () -> {
                    routes.reload();
                    CommandOutput.success(ctx.getSource(), "Portal definitions reloaded.", false);
                })))
                .then(CommandManager.literal("wand").executes(ctx -> run(ctx, () -> {
                    ServerPlayerEntity player = player(ctx);
                    if (!player.getInventory().insertStack(PortalContent.SURVEYOR.getDefaultStack())) {
                        player.dropItem(PortalContent.SURVEYOR.getDefaultStack(), false);
                    }
                    CommandOutput.success(ctx.getSource(), "Portal Surveyor granted.", false);
                })))
                .then(CommandManager.literal("list").executes(ctx -> run(ctx, () -> {
                    CommandOutput.header(ctx.getSource(), "Portal Routes");
                    routes.snapshots().forEach(route -> CommandOutput.bullet(ctx.getSource(),
                            route.routeId() + " - " + route.displayName() + " ["
                                    + (!route.complete() ? "incomplete"
                                    : route.active() ? "open" : route.unlocked() ? "closed" : "locked") + "]"));
                })))
                .then(CommandManager.literal("inspect")
                        .then(routeArgument(definitions).executes(ctx ->
                                run(ctx, () -> inspect(ctx, definitions, routes)))))
                .then(CommandManager.literal("guide")
                        .then(routeArgument(definitions).executes(ctx ->
                                run(ctx, () -> guide(ctx, definitions, routes)))))
                .then(CommandManager.literal("preview")
                        .then(CommandManager.argument("state", StringArgumentType.word())
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(PREVIEW_STATES, builder))
                                .executes(ctx -> run(ctx, () -> preview(ctx, definitions)))))
                .then(CommandManager.literal("endpoint")
                        .then(CommandManager.literal("set")
                                .then(routeArgument(definitions)
                                        .then(CommandManager.argument("role", StringArgumentType.word())
                                                .suggests((ctx, builder) -> CommandSource.suggestMatching(
                                                        new String[]{"a_gate", "a_arrival", "b_gate", "b_arrival"}, builder))
                                                .executes(ctx -> run(ctx, () -> {
                                                    ServerPlayerEntity player = player(ctx);
                                                    String route = StringArgumentType.getString(ctx, "route");
                                                    EndpointSlot role = EndpointSlot.parse(
                                                            StringArgumentType.getString(ctx, "role"));
                                                    switch (role) {
                                                        case A_GATE -> {
                                                            routes.setAGate(route, selections.require(player), player);
                                                            selections.clear(player);
                                                            CommandOutput.success(ctx.getSource(),
                                                                    "Set a_gate for " + route + ".", false);
                                                        }
                                                        case B_GATE -> {
                                                            routes.setBGate(route, selections.require(player), player);
                                                            selections.clear(player);
                                                            CommandOutput.success(ctx.getSource(),
                                                                    "Set b_gate for " + route + ".", false);
                                                        }
                                                        case A_ARRIVAL -> {
                                                            routes.setAArrival(route, player);
                                                            CommandOutput.success(ctx.getSource(),
                                                                    "Set a_arrival for " + route + ".", false);
                                                        }
                                                        case B_ARRIVAL -> {
                                                            routes.setBArrival(route, player);
                                                            CommandOutput.success(ctx.getSource(),
                                                                    "Set b_arrival for " + route + ".", false);
                                                        }
                                                    }
                                                }))))))
                .then(CommandManager.literal("setup")
                        .then(CommandManager.literal("enter")
                                .then(routeArgument(definitions)
                                        .executes(ctx -> run(ctx, () -> {
                                            routes.enterSetupDestination(player(ctx),
                                                    StringArgumentType.getString(ctx, "route"), null);
                                            CommandOutput.success(ctx.getSource(),
                                                    "Entered the destination world in portal setup mode.", false);
                                        }))
                                        .then(CommandManager.argument("position", Vec3ArgumentType.vec3())
                                                .executes(ctx -> run(ctx, () -> {
                                                    routes.enterSetupDestination(player(ctx),
                                                            StringArgumentType.getString(ctx, "route"),
                                                            Vec3ArgumentType.getVec3(ctx, "position"));
                                                    CommandOutput.success(ctx.getSource(),
                                                            "Entered the destination world at the requested position.",
                                                            false);
                                                })))))
                        .then(CommandManager.literal("return").executes(ctx -> run(ctx, () -> {
                            routes.returnFromSetup(player(ctx));
                            CommandOutput.success(ctx.getSource(),
                                    "Returned to your stored portal setup origin.", false);
                        }))))
                .then(CommandManager.literal("unlock")
                        .then(routeArgument(definitions).executes(ctx -> run(ctx, () -> {
                            routes.unlock(StringArgumentType.getString(ctx, "route"), actor(ctx));
                            CommandOutput.success(ctx.getSource(), "Portal route unlocked.", false);
                        }))))
                .then(CommandManager.literal("lock")
                        .then(routeArgument(definitions).executes(ctx -> run(ctx, () -> {
                            routes.lock(StringArgumentType.getString(ctx, "route"), actor(ctx));
                            CommandOutput.success(ctx.getSource(), "Portal route locked.", false);
                        }))))
                .then(CommandManager.literal("remove")
                        .then(routeArgument(definitions).executes(ctx -> run(ctx, () -> {
                            routes.remove(StringArgumentType.getString(ctx, "route"), actor(ctx));
                            CommandOutput.success(ctx.getSource(), "Portal linkage removed.", false);
                        }))))
                .then(CommandManager.literal("repair")
                        .then(routeArgument(definitions).executes(ctx -> run(ctx, () -> {
                            routes.reconcileRoute(StringArgumentType.getString(ctx, "route"));
                            CommandOutput.success(ctx.getSource(), "Portal route reconciled.", false);
                        })))
                        .then(CommandManager.literal("all").executes(ctx -> run(ctx, () -> {
                            routes.reconcileFields();
                            CommandOutput.success(ctx.getSource(), "All portal routes reconciled.", false);
                        }))))
                .then(CommandManager.literal("window")
                        .then(CommandManager.literal("open")
                                .then(routeArgument(definitions)
                                        .then(CommandManager.argument("duration", StringArgumentType.word())
                                                .executes(ctx -> run(ctx, () -> {
                                                    routes.forceOpen(StringArgumentType.getString(ctx, "route"),
                                                            duration(StringArgumentType.getString(ctx, "duration")),
                                                            actor(ctx));
                                                    CommandOutput.success(ctx.getSource(),
                                                            "Portal window forced open.", false);
                                                })))))
                        .then(CommandManager.literal("close")
                                .then(routeArgument(definitions).executes(ctx -> run(ctx, () -> {
                                    routes.forceClose(StringArgumentType.getString(ctx, "route"), actor(ctx));
                                    CommandOutput.success(ctx.getSource(), "Portal window closed.", false);
                                })))))
                .then(CommandManager.literal("entitlement")
                        .then(CommandManager.literal("inspect")
                                .then(CommandManager.argument("player", EntityArgumentType.player())
                                        .then(routeArgument(definitions).executes(ctx -> run(ctx, () -> {
                                            ServerPlayerEntity target = target(ctx);
                                            CommandOutput.kv(ctx.getSource(), "Return entitlement",
                                                    routes.hasEntitlement(target.getUuid(),
                                                            StringArgumentType.getString(ctx, "route")));
                                        })))))
                        .then(CommandManager.literal("grant")
                                .then(CommandManager.argument("player", EntityArgumentType.player())
                                        .then(routeArgument(definitions).executes(ctx -> run(ctx, () -> {
                                            ServerPlayerEntity target = target(ctx);
                                            routes.grantEntitlement(target.getUuid(),
                                                    StringArgumentType.getString(ctx, "route"));
                                            CommandOutput.success(ctx.getSource(), "Return entitlement granted.", false);
                                        })))))
                        .then(CommandManager.literal("clear")
                                .then(CommandManager.argument("player", EntityArgumentType.player())
                                        .then(routeArgument(definitions).executes(ctx -> run(ctx, () -> {
                                            ServerPlayerEntity target = target(ctx);
                                            routes.clearEntitlement(target.getUuid(),
                                                    StringArgumentType.getString(ctx, "route"));
                                            CommandOutput.success(ctx.getSource(), "Return entitlement cleared.", false);
                                        }))))));
    }

    private static RequiredArgumentBuilder<ServerCommandSource, String> routeArgument(
            PortalDefinitionService definitions
    ) {
        return CommandManager.argument("route", StringArgumentType.word())
                .suggests((ctx, builder) -> CommandSource.suggestMatching(
                        definitions.all().stream().map(definition -> definition.id()), builder));
    }

    private static void inspect(
            CommandContext<ServerCommandSource> ctx,
            PortalDefinitionService definitions,
            PortalRouteService routes
    ) {
        String routeId = StringArgumentType.getString(ctx, "route");
        var definition = definitions.require(routeId);
        var snapshot = routes.snapshot(routeId);
        var state = routes.route(routeId);
        CommandOutput.header(ctx.getSource(), snapshot.displayName());
        CommandOutput.kv(ctx.getSource(), "ID", snapshot.routeId());
        CommandOutput.kv(ctx.getSource(), "Mode", definition.mode().configId());
        CommandOutput.kv(ctx.getSource(), "Source world", definition.sourceDimension());
        CommandOutput.kv(ctx.getSource(), "Destination world", destinationLabel(definition.destinationDimension()));
        CommandOutput.kv(ctx.getSource(), "Unlocked", snapshot.unlocked());
        CommandOutput.kv(ctx.getSource(), "Complete", snapshot.complete());
        CommandOutput.kv(ctx.getSource(), "Active", snapshot.active());
        if (definition.mode().usesSchedule()) {
            CommandOutput.kv(ctx.getSource(), "Window start", snapshot.windowStart());
            CommandOutput.kv(ctx.getSource(), "Window end", snapshot.windowEnd());
        }
        if (definition.mode().requiresTicket()) {
            CommandOutput.kv(ctx.getSource(), "Required ticket", definition.ticketName());
            CommandOutput.kv(ctx.getSource(), "Economy price key", definition.ticketPriceKey());
        }
        if (definition.mode().chargesPassage()) {
            CommandOutput.kv(ctx.getSource(), "Economy price key", definition.passagePriceKey());
            CommandOutput.kv(ctx.getSource(), "First round trip free", definition.firstRoundTripFree());
        }
        endpoint(ctx, "A gate", state.source);
        endpoint(ctx, "B gate", state.returnEndpoint);
        arrival(ctx, "B arrival", state.outboundArrival);
        arrival(ctx, "A arrival", state.returnArrival);
        CommandOutput.kv(ctx.getSource(), "Color", String.format("#%06X", snapshot.visual().rgb()));
        CommandOutput.kv(ctx.getSource(), "Runtime locations",
                "world/elarion/addon-state/portals/state.json");
    }

    private static void guide(
            CommandContext<ServerCommandSource> ctx,
            PortalDefinitionService definitions,
            PortalRouteService routes
    ) {
        String routeId = StringArgumentType.getString(ctx, "route");
        var definition = definitions.require(routeId);
        var state = routes.route(routeId);
        CommandOutput.header(ctx.getSource(), definition.displayName() + " Setup");
        CommandOutput.kv(ctx.getSource(), "Mode", definition.mode().configId());
        CommandOutput.kv(ctx.getSource(), "Source world", definition.sourceDimension());
        CommandOutput.kv(ctx.getSource(), "Destination world", destinationLabel(definition.destinationDimension()));
        CommandOutput.bullet(ctx.getSource(), state.source == null
                ? "1. Select portal frame A, then: /e portal endpoint set " + routeId + " a_gate"
                : "1. a_gate is configured.");
        CommandOutput.bullet(ctx.getSource(), "*".equals(definition.destinationDimension())
                ? "2. Travel to side B using admin/world tools."
                : "2. Enter side B world: /e portal setup enter " + routeId + " [x y z]");
        CommandOutput.bullet(ctx.getSource(), state.outboundArrival == null
                ? "3. Stand where players should appear on side B: /e portal endpoint set "
                + routeId + " b_arrival"
                : "3. b_arrival is configured.");
        CommandOutput.bullet(ctx.getSource(), state.returnEndpoint == null
                ? "4. Select portal frame B, then: /e portal endpoint set "
                + routeId + " b_gate"
                : "4. b_gate is configured.");
        CommandOutput.bullet(ctx.getSource(), "5. Return to setup origin: /e portal setup return");
        CommandOutput.bullet(ctx.getSource(), state.returnArrival == null
                ? "6. Stand where players should appear on side A: /e portal endpoint set "
                + routeId + " a_arrival"
                : "6. a_arrival is configured.");
        CommandOutput.bullet(ctx.getSource(), definition.mode().requiresUnlock()
                ? "7. Unlock when complete: /e portal unlock " + routeId
                : "7. This route activates automatically when all four locations are configured.");
    }

    private static void preview(CommandContext<ServerCommandSource> ctx, PortalDefinitionService definitions) {
        ServerPlayerEntity player = player(ctx);
        String state = StringArgumentType.getString(ctx, "state");
        ServerPlayNetworking.send(player, previewPayload(state, definitions.ui()));
        CommandOutput.success(ctx.getSource(), "Opened portal prompt preview: " + state + ".", false);
    }

    static PortalTravelPromptPayload previewPayload(String rawState, PortalUiConfig ui) {
        String state = rawState == null ? "" : rawState.toLowerCase(java.util.Locale.ROOT);
        long scheduledClose = System.currentTimeMillis() + Duration.ofMinutes(42).toMillis();
        return switch (state) {
            case "neutral" -> payload(
                    "neutral", "Neutral Gate", PortalTravelDirection.OUTBOUND, 0L, "",
                    PortalTravelPromptPayload.COST_FREE, "No ticket or fee required.", 0,
                    true, "", ui);
            case "nether" -> payload(
                    "nether", "Nether Gate", PortalTravelDirection.OUTBOUND, scheduledClose,
                    "elarion:portal_ticket", PortalTravelPromptPayload.COST_TICKET,
                    "You need a Nether Ticket.", 0xFFFFD37A, true,
                    "A ticket grants one passage and stores one return.", ui);
            case "end" -> payload(
                    "end", "End Gate", PortalTravelDirection.OUTBOUND, scheduledClose,
                    "elarion:portal_ticket", PortalTravelPromptPayload.COST_TICKET,
                    "You need an End Ticket.", 0xFFD8C2FF, true,
                    "A ticket grants one passage and stores one return.", ui);
            case "fee" -> payload(
                    "realm1", "Ancient Gate", PortalTravelDirection.OUTBOUND, 0L,
                    "elarion:currency", PortalTravelPromptPayload.COST_FEE,
                    "You need 25 Sigils.", 0xFF9696D1, true,
                    "Withdraw banked Sigils before using this gate.", ui);
            case "blocked" -> payload(
                    "realm1", "Ancient Gate", PortalTravelDirection.OUTBOUND, 0L,
                    "elarion:currency", PortalTravelPromptPayload.COST_FEE,
                    "Portal travel is temporarily blocked.", 0xFFFF6B6B, false,
                    "You cannot use portals right now.", ui);
            case "return" -> payload(
                    "realm1", "Ancient Gate", PortalTravelDirection.RETURN, 0L,
                    "", PortalTravelPromptPayload.COST_FREE,
                    "Your return passage is already paid.", 0xFF8FE18F, true,
                    "This return completes your current round trip.", ui);
            default -> throw new IllegalArgumentException(
                    "Preview state must be neutral, nether, end, fee, blocked, or return.");
        };
    }

    private static PortalTravelPromptPayload payload(
            String routeId,
            String gateName,
            PortalTravelDirection direction,
            long closesAt,
            String iconItem,
            String costKind,
            String requirement,
            int requirementColor,
            boolean allowed,
            String message,
            PortalUiConfig ui
    ) {
        PortalUiConfig safeUi = ui == null ? PortalUiConfig.defaults() : ui;
        return new PortalTravelPromptPayload(
                routeId, gateName, direction, closesAt, iconItem, costKind, requirement,
                requirementColor, allowed, message, safeUi.themeVariant(), safeUi.logicalWidth(),
                safeUi.logicalHeight(), safeUi.minimumScalePercent(), safeUi.confirmButtonWidth(),
                safeUi.closeButtonWidth());
    }

    private static void endpoint(
            CommandContext<ServerCommandSource> ctx, String label, PortalEndpoint endpoint
    ) {
        if (endpoint == null) {
            CommandOutput.kv(ctx.getSource(), label, "-");
            return;
        }
        var b = endpoint.bounds();
        CommandOutput.kv(ctx.getSource(), label, endpoint.worldId() + " "
                + b.minX() + "," + b.minY() + "," + b.minZ() + " -> "
                + b.maxX() + "," + b.maxY() + "," + b.maxZ() + " axis=" + b.axis());
    }

    private static void arrival(
            CommandContext<ServerCommandSource> ctx, String label, PortalArrival arrival
    ) {
        CommandOutput.kv(ctx.getSource(), label, arrival == null ? "-" : arrival.worldId() + " "
                + String.format(java.util.Locale.ROOT, "%.1f %.1f %.1f yaw=%.1f",
                arrival.x(), arrival.y(), arrival.z(), arrival.yaw()));
    }

    private static String destinationLabel(String destinationDimension) {
        return "*".equals(destinationDimension) ? "Any configured world" : destinationDimension;
    }

    private static int run(CommandContext<ServerCommandSource> ctx, Runnable action) {
        try {
            action.run();
            return Command.SINGLE_SUCCESS;
        } catch (Exception exception) {
            ctx.getSource().sendError(Text.literal(exception.getMessage() == null
                    ? exception.getClass().getSimpleName() : exception.getMessage()));
            return 0;
        }
    }

    private static ServerPlayerEntity player(CommandContext<ServerCommandSource> ctx) {
        try {
            return ctx.getSource().getPlayerOrThrow();
        } catch (Exception exception) {
            throw new IllegalArgumentException("This command requires a player.");
        }
    }

    private static UUID actor(CommandContext<ServerCommandSource> ctx) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        return player == null ? null : player.getUuid();
    }

    private static ServerPlayerEntity target(CommandContext<ServerCommandSource> ctx) {
        try {
            return EntityArgumentType.getPlayer(ctx, "player");
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException exception) {
            throw new IllegalArgumentException(exception.getMessage());
        }
    }

    private static Duration duration(String raw) {
        if (!raw.matches("[1-9][0-9]*[smhd]")) {
            throw new IllegalArgumentException("Duration must look like 30m, 4h, or 2d.");
        }
        long amount = Long.parseLong(raw.substring(0, raw.length() - 1));
        return switch (raw.charAt(raw.length() - 1)) {
            case 's' -> Duration.ofSeconds(amount);
            case 'm' -> Duration.ofMinutes(amount);
            case 'h' -> Duration.ofHours(amount);
            case 'd' -> Duration.ofDays(amount);
            default -> throw new IllegalArgumentException("Unsupported duration.");
        };
    }

    private enum EndpointSlot {
        A_GATE,
        A_ARRIVAL,
        B_GATE,
        B_ARRIVAL;

        static EndpointSlot parse(String value) {
            return switch (value.toLowerCase(java.util.Locale.ROOT)) {
                case "a_gate" -> A_GATE;
                case "a_arrival" -> A_ARRIVAL;
                case "b_gate" -> B_GATE;
                case "b_arrival" -> B_ARRIVAL;
                default -> throw new IllegalArgumentException(
                        "Endpoint role must be a_gate, a_arrival, b_gate, or b_arrival.");
            };
        }
    }
}
