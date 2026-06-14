package panetina.elarion.addons.portals;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panetina.elarion.addons.portals.api.ElarionPortalsApi;
import panetina.elarion.addons.portals.command.PortalCommands;
import panetina.elarion.addons.portals.model.PortalTravelDirection;
import panetina.elarion.addons.portals.network.PortalScreenClosePayload;
import panetina.elarion.addons.portals.network.PortalRouteStatusSyncPayload;
import panetina.elarion.addons.portals.network.PortalTravelConfirmPayload;
import panetina.elarion.addons.portals.network.PortalTravelPromptPayload;
import panetina.elarion.addons.portals.network.PortalVisualSyncPayload;
import panetina.elarion.addons.portals.registry.PortalActions;
import panetina.elarion.addons.portals.service.PortalDefinitionService;
import panetina.elarion.addons.portals.service.PortalRouteService;
import panetina.elarion.addons.portals.service.PortalSelectionService;
import panetina.elarion.addons.portals.storage.PortalStorage;
import panetina.elarion.core.api.ElarionAddon;
import panetina.elarion.core.api.ElarionApi;

public final class ElarionPortalsAddon implements ElarionAddon {
    private static final Logger LOGGER = LoggerFactory.getLogger("elarion_portals");

    @Override
    public void initialize(ElarionApi api) {
        PayloadTypeRegistry.playS2C().register(PortalTravelPromptPayload.ID, PortalTravelPromptPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(PortalScreenClosePayload.ID, PortalScreenClosePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(PortalVisualSyncPayload.ID, PortalVisualSyncPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(
                PortalRouteStatusSyncPayload.ID, PortalRouteStatusSyncPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(PortalTravelConfirmPayload.ID, PortalTravelConfirmPayload.CODEC);
        PortalDefinitionService definitions = new PortalDefinitionService(api);
        definitions.load();
        PortalContent.configureTickets(definitions.all());
        PortalContent.register();
        PortalSelectionService selections = new PortalSelectionService();
        PortalRouteService routes = new PortalRouteService(
                LOGGER, api, definitions, new PortalStorage(LOGGER));
        new ElarionPortalsApi(definitions, routes);
        PortalActions.register(api, definitions, routes);

        api.system().abilities().register("elarion.portal.manage");
        api.system().commands().registerAdminSubcommand(
                () -> PortalCommands.create(definitions, routes, selections));
        api.system().commands().registerHelpDescription(
                "/e portal ...", "Link, inspect, schedule, and repair Elarion portal routes.");

        routes.setPromptSender(prompt -> {
            var ui = definitions.ui();
            ServerPlayNetworking.send(prompt.player(), new PortalTravelPromptPayload(
                    prompt.routeId(), prompt.title(), prompt.direction(),
                    prompt.closesAt(), prompt.iconItem(), prompt.requirement(),
                    prompt.requirementColor(),
                    prompt.allowed(), prompt.message(),
                    ui.themeVariant(), ui.logicalWidth(), ui.logicalHeight(), ui.minimumScalePercent(),
                    ui.confirmButtonWidth(), ui.closeButtonWidth()));
        });
        routes.setVisualSync(() -> {
            if (routes.snapshots().isEmpty()) return;
            PortalVisualSyncPayload payload = PortalVisualSyncPayload.from(routes.snapshots());
            PortalRouteStatusSyncPayload status = PortalRouteStatusSyncPayload.from(routes.snapshots());
            routes.snapshots().stream().findFirst().ifPresent(ignored -> {
                try {
                    var server = api.citizens().server();
                    if (server != null) server.getPlayerManager().getPlayerList()
                            .forEach(player -> {
                                ServerPlayNetworking.send(player, payload);
                                ServerPlayNetworking.send(player, status);
                            });
                } catch (RuntimeException exception) {
                    LOGGER.debug("Portal visual sync deferred until server bind");
                }
            });
        });

        registerSelection(selections);
        ServerPlayNetworking.registerGlobalReceiver(PortalTravelConfirmPayload.ID, (payload, context) ->
                context.server().execute(() -> {
                    var result = routes.travel(context.player(), payload.routeId(), payload.direction());
                    if (result.success()) {
                        LOGGER.info("Portal travel completed for {} on route {} ({})",
                                context.player().getGameProfile().getName(),
                                payload.routeId(),
                                payload.direction().name().toLowerCase(java.util.Locale.ROOT));
                    } else {
                        context.player().sendMessage(Text.literal(result.message()), false);
                    }
                    ServerPlayNetworking.send(context.player(), PortalScreenClosePayload.INSTANCE);
                }));
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            routes.bind(server);
            if (!api.tasks().enqueueServer("portal-startup-reconcile", routes::reconcileFields)) {
                LOGGER.error("Could not queue startup portal reconciliation");
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> routes.tick());
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> routes.save());
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayNetworking.send(handler.player, PortalVisualSyncPayload.from(routes.snapshots()));
            ServerPlayNetworking.send(handler.player, PortalRouteStatusSyncPayload.from(routes.snapshots()));
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                routes.clearSetupOrigin(handler.player.getUuid()));
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(routes::rejectUnauthorizedWorldChange);
        LOGGER.info("Elarion Portals initialized with {} route definitions", definitions.all().size());
    }

    private static void registerSelection(PortalSelectionService selections) {
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (world.isClient || !(player instanceof ServerPlayerEntity serverPlayer)
                    || !player.getStackInHand(hand).isOf(PortalContent.SURVEYOR)) return ActionResult.PASS;
            if (!serverPlayer.hasPermissionLevel(4)) return ActionResult.FAIL;
            try {
                selections.first(serverPlayer, pos.offset(direction));
            } catch (IllegalArgumentException exception) {
                LOGGER.info("Portal Surveyor selection rejected for {}: {}",
                        serverPlayer.getGameProfile().getName(), exception.getMessage());
                serverPlayer.sendMessage(Text.literal(exception.getMessage()), true);
            }
            return ActionResult.SUCCESS;
        });
        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            if (world.isClient || !(player instanceof ServerPlayerEntity serverPlayer)
                    || !player.getStackInHand(hand).isOf(PortalContent.SURVEYOR)) return ActionResult.PASS;
            if (!serverPlayer.hasPermissionLevel(4)) return ActionResult.FAIL;
            try {
                selections.second(serverPlayer, hit.getBlockPos().offset(hit.getSide()));
            } catch (IllegalArgumentException exception) {
                LOGGER.info("Portal Surveyor selection rejected for {}: {}",
                        serverPlayer.getGameProfile().getName(), exception.getMessage());
                serverPlayer.sendMessage(Text.literal(exception.getMessage()), true);
            }
            return ActionResult.SUCCESS;
        });
    }
}
