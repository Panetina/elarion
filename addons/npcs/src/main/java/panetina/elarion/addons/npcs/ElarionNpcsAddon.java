package panetina.elarion.addons.npcs;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panetina.elarion.addons.npcs.api.ElarionNpcApi;
import panetina.elarion.addons.npcs.command.NpcCommands;
import panetina.elarion.addons.npcs.config.NpcConfigDescriptors;
import panetina.elarion.addons.npcs.config.NpcConfigLoader;
import panetina.elarion.addons.npcs.entity.ElarionNpcEntities;
import panetina.elarion.addons.npcs.entity.ElarionNpcEntity;
import panetina.elarion.addons.npcs.network.NpcDialogueClosePayload;
import panetina.elarion.addons.npcs.network.NpcDialogueDismissPayload;
import panetina.elarion.addons.npcs.network.NpcDialogueOpenPayload;
import panetina.elarion.addons.npcs.network.NpcDialoguePromptSubmitPayload;
import panetina.elarion.addons.npcs.network.NpcDialogueSelectPayload;
import panetina.elarion.addons.npcs.network.NpcBankQuotePayload;
import panetina.elarion.addons.npcs.network.NpcBankQuoteRequestPayload;
import panetina.elarion.addons.npcs.network.NpcVisualSyncPayload;
import panetina.elarion.addons.npcs.network.NpcTradeSnapshotPayload;
import panetina.elarion.addons.npcs.network.NpcTradeQuotePayload;
import panetina.elarion.addons.npcs.network.NpcTradeQuoteRequestPayload;
import panetina.elarion.addons.npcs.network.NpcTradePurchaseRequestPayload;
import panetina.elarion.addons.npcs.network.NpcTradePurchaseResultPayload;
import panetina.elarion.addons.npcs.service.NpcDefinitionService;
import panetina.elarion.addons.npcs.service.NpcInteractionService;
import panetina.elarion.addons.npcs.service.NpcPlacementService;
import panetina.elarion.addons.npcs.service.NpcReputationTabProvider;
import panetina.elarion.addons.npcs.service.NpcBankQuoteProvider;
import panetina.elarion.addons.npcs.service.NpcRelationshipRegistryHandlers;
import panetina.elarion.addons.npcs.service.NpcRelationshipService;
import panetina.elarion.addons.npcs.service.NpcHistoryService;
import panetina.elarion.addons.npcs.service.NpcStoryRegistryHandlers;
import panetina.elarion.addons.npcs.service.NpcStoryStateService;
import panetina.elarion.addons.npcs.service.NpcTradeSnapshotService;
import panetina.elarion.addons.npcs.service.NpcTradeQuoteProvider;
import panetina.elarion.addons.npcs.service.NpcTradePurchaseProvider;
import panetina.elarion.addons.npcs.service.NpcTradePurchaseService;
import panetina.elarion.addons.npcs.service.NpcTradeSaleProvider;
import panetina.elarion.addons.npcs.service.NpcTradeStockService;
import panetina.elarion.addons.npcs.service.NpcTaxJurisdictionResolver;
import panetina.elarion.addons.npcs.storage.NpcPlacementStorage;
import panetina.elarion.addons.npcs.storage.NpcRelationshipStorage;
import panetina.elarion.addons.npcs.storage.NpcStoryStateStorage;
import panetina.elarion.addons.npcs.storage.NpcTradePurchaseStorage;
import panetina.elarion.addons.npcs.storage.NpcTradeSaleStorage;
import panetina.elarion.addons.npcs.storage.NpcTradeStockStorage;
import panetina.elarion.core.api.ElarionAddon;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.api.reset.PlayerResetHandler;
import panetina.elarion.core.api.reset.PlayerResetResult;
import panetina.elarion.core.api.reset.WorldResetHandler;
import panetina.elarion.core.api.reset.WorldResetResult;

import java.util.concurrent.atomic.AtomicBoolean;

public final class ElarionNpcsAddon implements ElarionAddon {
    private static final Logger LOGGER = LoggerFactory.getLogger("elarion_npcs");

    @Override
    public void initialize(ElarionApi api) {
        ElarionNpcEntities.initialize();
        PayloadTypeRegistry.playS2C().register(NpcDialogueOpenPayload.ID, NpcDialogueOpenPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(NpcDialogueClosePayload.ID, NpcDialogueClosePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(NpcVisualSyncPayload.ID, NpcVisualSyncPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(NpcBankQuotePayload.ID, NpcBankQuotePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(NpcTradeSnapshotPayload.ID, NpcTradeSnapshotPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(NpcTradeQuotePayload.ID, NpcTradeQuotePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(NpcTradePurchaseResultPayload.ID, NpcTradePurchaseResultPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(NpcDialogueSelectPayload.ID, NpcDialogueSelectPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(NpcDialoguePromptSubmitPayload.ID, NpcDialoguePromptSubmitPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(NpcDialogueDismissPayload.ID, NpcDialogueDismissPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(NpcBankQuoteRequestPayload.ID, NpcBankQuoteRequestPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(NpcTradeQuoteRequestPayload.ID, NpcTradeQuoteRequestPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(NpcTradePurchaseRequestPayload.ID, NpcTradePurchaseRequestPayload.CODEC);

        NpcDefinitionService definitions = new NpcDefinitionService(LOGGER, new NpcConfigLoader(LOGGER));
        AtomicBoolean configDomainRegistered = new AtomicBoolean();
        NpcStoryStateService stories = new NpcStoryStateService(
                LOGGER, new NpcStoryStateStorage(LOGGER));
        NpcPlacementService placements = new NpcPlacementService(
                LOGGER, definitions, new NpcPlacementStorage(LOGGER),
                new NpcTaxJurisdictionResolver(worldId -> api.realms().ownerForWorld(worldId)
                        .map(realm -> realm.id())));
        NpcRelationshipService relationships = new NpcRelationshipService(
                LOGGER, new NpcRelationshipStorage(LOGGER), placedNpcId -> placements.find(placedNpcId)
                .flatMap(placed -> definitions.npc(placed.definitionId()))
                .map(npc -> npc.faction())
                .orElse("unaffiliated"));
        api.system().collections().registerTab(new NpcReputationTabProvider(api, definitions, relationships));
        NpcTradeQuoteProvider quoteProvider = tradeQuoteProvider();
        NpcBankQuoteProvider bankQuoteProvider = bankQuoteProvider();
        NpcTradeStockService tradeStocks = new NpcTradeStockService(LOGGER, new NpcTradeStockStorage(LOGGER));
        NpcTradePurchaseService tradePurchases = new NpcTradePurchaseService(
                LOGGER, definitions, quoteProvider, tradePurchaseProvider(), tradeSaleProvider(),
                tradeStocks, new NpcTradePurchaseStorage(LOGGER), new NpcTradeSaleStorage(LOGGER));
        NpcTradeSnapshotService tradeSnapshots = new NpcTradeSnapshotService(definitions, quoteProvider, tradeStocks);
        NpcInteractionService interactions = new NpcInteractionService(
                api, definitions, placements, bankQuoteProvider, tradeSnapshots, tradePurchases,
                stories, relationships, new NpcHistoryService(LOGGER, api));
        api.system().playerResets().register(new PlayerResetHandler() {
            @Override public String id() { return "elarion_npcs"; }
            @Override public java.util.Map<String, Long> preview(net.minecraft.server.MinecraftServer server) {
                return java.util.Map.of();
            }
            @Override public java.util.List<java.nio.file.Path> backupTargets(net.minecraft.server.MinecraftServer server) {
                java.nio.file.Path root = panetina.elarion.core.storage.JsonStateStorage.addonStateRoot(server, "npcs");
                return java.util.List.of(root.resolve("relationships.json"), root.resolve("story-state.json"),
                        root.resolve("trade-purchases.json"), root.resolve("trade-sales.json"));
            }
            @Override public PlayerResetResult reset(panetina.elarion.core.api.reset.PlayerResetContext context) {
                java.util.Map<String, Long> changed = new java.util.LinkedHashMap<>();
                changed.put("npcRelationships", (long) relationships.resetAllPlayerState());
                changed.put("npcStoryState", (long) stories.resetAllPlayerState());
                changed.put("npcTradeJournal", (long) tradePurchases.resetAllPlayerState());
                return new PlayerResetResult(changed);
            }
        });
        api.system().worldResets().register(new WorldResetHandler() {
            @Override public String id() { return "elarion_npcs"; }
            @Override public java.util.Map<String, Long> preview(net.minecraft.server.MinecraftServer server, String worldId) {
                return java.util.Map.of("placedNpcs", placements.all().stream().filter(npc -> worldId.equals(npc.worldId())).count());
            }
            @Override public java.util.List<java.nio.file.Path> backupTargets(net.minecraft.server.MinecraftServer server, String worldId) {
                return java.util.List.of(panetina.elarion.core.storage.JsonStateStorage.addonStateRoot(server, "npcs").resolve("placed-npcs.json"));
            }
            @Override public WorldResetResult reset(panetina.elarion.core.api.reset.WorldResetContext context) {
                return WorldResetResult.of("placedNpcs", placements.removeWorld(context.worldId()));
            }
        });
        placements.onRemoved(interactions::closeNpcSessions);
        new ElarionNpcApi(api, definitions, placements, interactions, relationships);
        NpcRelationshipRegistryHandlers.register(api, relationships);
        NpcStoryRegistryHandlers.register(api, stories);
        api.publicHistory().registerRenderer(NpcChronicleText.INSTANCE);

        api.system().abilities().register("elarion.npc.admin");
        api.system().commands().registerAdminSubcommand(() ->
                NpcCommands.create(api, definitions, placements, interactions));
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
        ServerPlayNetworking.registerGlobalReceiver(NpcBankQuoteRequestPayload.ID,
                (payload, context) -> context.server().execute(() ->
                        interactions.quoteBank(context.player(), payload)));
        ServerPlayNetworking.registerGlobalReceiver(NpcTradeQuoteRequestPayload.ID,
                (payload, context) -> context.server().execute(() ->
                        interactions.quoteTrade(context.player(), payload)));
        ServerPlayNetworking.registerGlobalReceiver(NpcTradePurchaseRequestPayload.ID,
                (payload, context) -> context.server().execute(() ->
                        interactions.purchaseTrade(context.player(), payload)));
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
            stories.bind(server);
            if (configDomainRegistered.compareAndSet(false, true)) {
                NpcConfigDescriptors.register(
                        api.system().configs(),
                        definitions::npcs,
                        definitions::skins,
                        definitions::portraits,
                        definitions::dialogues,
                        definitions::trades,
                        definitions::ui);
            }
            placements.bind(server);
            relationships.bind(server);
            tradeStocks.bind(server);
            tradePurchases.bind(server);
            // Managed worlds open in other SERVER_STARTED handlers. Reconcile
            // on the first server-queue drain so their NPCs can spawn.
            if (!api.tasks().enqueueServer("npc-startup-reconcile", placements::respawnAll)) {
                LOGGER.error("Could not queue startup NPC reconciliation");
            }
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayNetworking.send(handler.player, placements.visualSyncPayload());
            tradePurchases.reconcilePlayer(handler.player);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                interactions.dismissPlayer(handler.player.getUuid()));
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            placements.shutdown();
            relationships.shutdown();
            stories.shutdown();
            tradeStocks.shutdown();
            tradePurchases.shutdown();
        });

        LOGGER.info("Elarion NPCs initialized");
    }

    private static NpcTradeQuoteProvider tradeQuoteProvider() {
        if (!FabricLoader.getInstance().isModLoaded("elarion_economy")) {
            return NpcTradeQuoteProvider.unavailable();
        }
        try {
            Class<?> type = Class.forName(
                    "panetina.elarion.addons.npcs.integration.EconomyNpcTradeQuoteProvider");
            return (NpcTradeQuoteProvider) type.getConstructor().newInstance();
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to initialize NPC Economy quote integration", exception);
        }
    }

    private static NpcBankQuoteProvider bankQuoteProvider() {
        if (!FabricLoader.getInstance().isModLoaded("elarion_economy")) {
            return NpcBankQuoteProvider.unavailable();
        }
        try {
            Class<?> type = Class.forName(
                    "panetina.elarion.addons.npcs.integration.EconomyNpcBankQuoteProvider");
            return (NpcBankQuoteProvider) type.getConstructor().newInstance();
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to initialize NPC Economy bank quote integration", exception);
        }
    }

    private static NpcTradePurchaseProvider tradePurchaseProvider() {
        if (!FabricLoader.getInstance().isModLoaded("elarion_economy")) {
            return NpcTradePurchaseProvider.unavailable();
        }
        try {
            Class<?> type = Class.forName(
                    "panetina.elarion.addons.npcs.integration.EconomyNpcTradePurchaseProvider");
            return (NpcTradePurchaseProvider) type.getConstructor().newInstance();
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to initialize NPC Economy purchase integration", exception);
        }
    }

    private static NpcTradeSaleProvider tradeSaleProvider() {
        if (!FabricLoader.getInstance().isModLoaded("elarion_economy")) {
            return NpcTradeSaleProvider.unavailable();
        }
        try {
            Class<?> type = Class.forName(
                    "panetina.elarion.addons.npcs.integration.EconomyNpcTradeSaleProvider");
            return (NpcTradeSaleProvider) type.getConstructor().newInstance();
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to initialize NPC Economy sale integration", exception);
        }
    }
}
