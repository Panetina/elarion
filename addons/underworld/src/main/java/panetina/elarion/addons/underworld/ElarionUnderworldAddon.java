package panetina.elarion.addons.underworld;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panetina.elarion.addons.underworld.block.UnderworldBlocks;
import panetina.elarion.addons.underworld.command.DeathCommands;
import panetina.elarion.addons.underworld.command.BanishCommands;
import panetina.elarion.addons.underworld.config.UnderworldConfig;
import panetina.elarion.addons.underworld.config.UnderworldConfigDescriptors;
import panetina.elarion.addons.underworld.config.UnderworldConfigLoader;
import panetina.elarion.addons.underworld.network.UnderworldStatusSyncPayload;
import panetina.elarion.addons.underworld.network.BanishmentAppearanceSyncPayload;
import panetina.elarion.addons.underworld.network.GraveOpenPayload;
import panetina.elarion.addons.underworld.network.GraveRecoverPayload;
import panetina.elarion.addons.underworld.service.UnderworldAdminPanelProvider;
import panetina.elarion.addons.underworld.service.UnderworldProfileContributor;
import panetina.elarion.addons.underworld.service.UnderworldService;
import panetina.elarion.addons.underworld.storage.UnderworldStorage;
import panetina.elarion.core.api.ElarionAddon;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.api.reset.PlayerResetHandler;
import panetina.elarion.core.api.reset.PlayerResetResult;
import panetina.elarion.core.storage.JsonStateStorage;

public final class ElarionUnderworldAddon implements ElarionAddon {
    private static final Logger LOGGER = LoggerFactory.getLogger("elarion_underworld");

    @Override
    public void initialize(ElarionApi api) {
        UnderworldBlocks.register();
        PayloadTypeRegistry.playS2C().register(UnderworldStatusSyncPayload.ID, UnderworldStatusSyncPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(
                BanishmentAppearanceSyncPayload.ID, BanishmentAppearanceSyncPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(GraveOpenPayload.ID, GraveOpenPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(GraveRecoverPayload.ID, GraveRecoverPayload.CODEC);
        UnderworldConfig config = UnderworldConfigLoader.load(LOGGER);
        UnderworldService service = new UnderworldService(
                LOGGER, api, new UnderworldStorage(LOGGER), config);
        UnderworldConfigDescriptors.register(api.system().configs(), service::config);
        api.publicHistory().registerRenderer(UnderworldChronicleText.INSTANCE);
        api.system().adminPanel().registerProvider(new UnderworldAdminPanelProvider(service));
        api.system().profiles().registerContributor(new UnderworldProfileContributor(api.playerStats()));
        api.system().abilities().register("elarion.underworld.manage");
        api.system().playerResets().register(new PlayerResetHandler() {
            @Override public String id() { return "elarion_underworld"; }
            @Override public java.util.Map<String, Long> preview(net.minecraft.server.MinecraftServer server) {
                return service.resetPreview();
            }
            @Override public java.util.List<java.nio.file.Path> backupTargets(net.minecraft.server.MinecraftServer server) {
                return java.util.List.of(JsonStateStorage.addonStateRoot(server, "underworld").resolve("state.json"));
            }
            @Override public PlayerResetResult reset(panetina.elarion.core.api.reset.PlayerResetContext context) {
                java.util.Map<String, Long> counts = service.resetPreview();
                service.resetAll();
                return new PlayerResetResult(counts);
            }
        });
        api.system().commands().registerAdminSubcommand(() -> DeathCommands.create(service));
        api.system().commands().registerRootCommand(() -> BanishCommands.banish(service));
        api.system().commands().registerRootCommand(() -> BanishCommands.unbanish(service));
        api.system().commands().registerTestSubcommand(() -> DeathCommands.createTest(service));
        api.system().commands().registerHelpDescription(
                "/e death ...", "Inspect and manage Underworld sessions, corpses, and soul fractures.");
        api.system().commands().registerHelpDescription(
                "/e test death ...", "Development-only Underworld test commands.");
        api.system().commands().registerHelpDescription(
                "/banish <player> <minutes|permanent> <reason>", "Confine a player to the Underworld.");
        api.system().commands().registerHelpDescription(
                "/unbanish <player>", "Lift an active Underworld banishment.");
        service.registerEvents();
        ServerPlayNetworking.registerGlobalReceiver(GraveRecoverPayload.ID, (payload, context) ->
                context.server().execute(() -> service.recoverFromUi(context.player(), payload.corpseId())));
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient() || !(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;
            return service.handleTombInteraction(serverPlayer, hitResult.getBlockPos(), hand)
                    ? ActionResult.SUCCESS
                    : ActionResult.PASS;
        });
        ServerLifecycleEvents.SERVER_STARTED.register(service::bind);
        ServerTickEvents.END_SERVER_TICK.register(server -> service.tick());
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> service.shutdown());
        LOGGER.info("Elarion Underworld initialized");
    }
}
