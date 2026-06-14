package panetina.elarion.addons.npcs;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panetina.elarion.addons.npcs.api.ElarionNpcApi;
import panetina.elarion.addons.npcs.command.NpcCommands;
import panetina.elarion.addons.npcs.config.NpcConfigLoader;
import panetina.elarion.addons.npcs.entity.ElarionNpcEntities;
import panetina.elarion.addons.npcs.entity.ElarionNpcEntity;
import panetina.elarion.addons.npcs.network.NpcDialogueClosePayload;
import panetina.elarion.addons.npcs.network.NpcDialogueDismissPayload;
import panetina.elarion.addons.npcs.network.NpcDialogueOpenPayload;
import panetina.elarion.addons.npcs.network.NpcDialoguePromptSubmitPayload;
import panetina.elarion.addons.npcs.network.NpcDialogueSelectPayload;
import panetina.elarion.addons.npcs.network.NpcVisualSyncPayload;
import panetina.elarion.addons.npcs.service.NpcDefinitionService;
import panetina.elarion.addons.npcs.service.NpcInteractionService;
import panetina.elarion.addons.npcs.service.NpcPlacementService;
import panetina.elarion.addons.npcs.storage.NpcPlacementStorage;
import panetina.elarion.core.api.ElarionAddon;
import panetina.elarion.core.api.ElarionApi;

public final class ElarionNpcsAddon implements ElarionAddon {
    private static final Logger LOGGER = LoggerFactory.getLogger("elarion_npcs");

    @Override
    public void initialize(ElarionApi api) {
        ElarionNpcEntities.initialize();
        PayloadTypeRegistry.playS2C().register(NpcDialogueOpenPayload.ID, NpcDialogueOpenPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(NpcDialogueClosePayload.ID, NpcDialogueClosePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(NpcVisualSyncPayload.ID, NpcVisualSyncPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(NpcDialogueSelectPayload.ID, NpcDialogueSelectPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(NpcDialoguePromptSubmitPayload.ID, NpcDialoguePromptSubmitPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(NpcDialogueDismissPayload.ID, NpcDialogueDismissPayload.CODEC);

        NpcDefinitionService definitions = new NpcDefinitionService(LOGGER, new NpcConfigLoader(LOGGER));
        NpcPlacementService placements = new NpcPlacementService(
                LOGGER, definitions, new NpcPlacementStorage(LOGGER));
        NpcInteractionService interactions = new NpcInteractionService(api, definitions, placements);
        placements.onRemoved(interactions::closeNpcSessions);
        new ElarionNpcApi(api, definitions, placements, interactions);

        api.system().abilities().register("elarion.npc.admin");
        api.system().commands().registerAdminSubcommand(() -> NpcCommands.create(api, definitions, placements));
        api.system().commands().registerHelpDescription("/e npc ...", "Manage static Elarion NPCs.");

        ServerPlayNetworking.registerGlobalReceiver(NpcDialogueSelectPayload.ID,
                (payload, context) -> context.server().execute(() ->
                        interactions.select(context.player(), payload)));
        ServerPlayNetworking.registerGlobalReceiver(NpcDialoguePromptSubmitPayload.ID,
                (payload, context) -> context.server().execute(() ->
                        interactions.submitPrompt(context.player(), payload)));
        ServerPlayNetworking.registerGlobalReceiver(NpcDialogueDismissPayload.ID,
                (payload, context) -> context.server().execute(() ->
                        interactions.dismiss(context.player(), payload.npcId())));
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient() || !(player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer)) {
                return ActionResult.PASS;
            }
            return placements.byEntity(entity.getUuid())
                    .map(record -> {
                        interactions.open(serverPlayer, record.id());
                        return ActionResult.SUCCESS;
                    })
                    .orElse(ActionResult.PASS);
        });
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof ElarionNpcEntity npc) {
                placements.onEntityLoaded(npc);
            }
        });
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            definitions.load(api);
            placements.bind(server);
            // Managed worlds open in other SERVER_STARTED handlers. Reconcile
            // on the first server-queue drain so their NPCs can spawn.
            if (!api.tasks().enqueueServer("npc-startup-reconcile", placements::respawnAll)) {
                LOGGER.error("Could not queue startup NPC reconciliation");
            }
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                ServerPlayNetworking.send(handler.player, placements.visualSyncPayload()));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                interactions.dismissPlayer(handler.player.getUuid()));
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> placements.shutdown());

        LOGGER.info("Elarion NPCs initialized");
    }
}
