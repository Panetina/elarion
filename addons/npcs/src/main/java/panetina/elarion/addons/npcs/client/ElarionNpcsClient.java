package panetina.elarion.addons.npcs.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import panetina.elarion.addons.npcs.entity.ElarionNpcEntities;
import panetina.elarion.addons.npcs.network.NpcDialogueClosePayload;
import panetina.elarion.addons.npcs.network.NpcDialogueOpenPayload;
import panetina.elarion.addons.npcs.network.NpcBankQuotePayload;
import panetina.elarion.addons.npcs.network.NpcVisualSyncPayload;
import panetina.elarion.addons.npcs.network.NpcTradeSnapshotPayload;
import panetina.elarion.addons.npcs.network.NpcTradeQuotePayload;
import panetina.elarion.addons.npcs.network.NpcTradePurchaseResultPayload;

public final class ElarionNpcsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ElarionNpcEntities.NPC, ElarionNpcEntityRenderer::new);
        ClientPlayNetworking.registerGlobalReceiver(NpcDialogueOpenPayload.ID, (payload, context) ->
                context.client().execute(() -> {
                    if ("bank".equals(payload.presentationKind())) {
                        if (context.client().currentScreen instanceof NpcBankScreen bank
                                && bank.belongsTo(payload)) {
                            bank.updateDialogue(payload);
                        } else {
                            context.client().setScreen(new NpcBankScreen(payload));
                        }
                    } else if ("trade".equals(payload.presentationKind())) {
                        if (context.client().currentScreen instanceof NpcTradeScreen trade
                                && trade.belongsTo(payload)) {
                            trade.updateDialogue(payload);
                        } else {
                            context.client().setScreen(new NpcTradeScreen(payload));
                        }
                    } else if (context.client().currentScreen instanceof NpcDialogueScreen dialogue
                            && dialogue.belongsTo(payload)) {
                        dialogue.updateDialogue(payload);
                    } else {
                        context.client().setScreen(new NpcDialogueScreen(payload));
                    }
                }));
        ClientPlayNetworking.registerGlobalReceiver(NpcDialogueClosePayload.ID, (payload, context) ->
                context.client().execute(() -> {
                    if (context.client().currentScreen instanceof NpcDialogueScreen
                            || context.client().currentScreen instanceof NpcBankScreen
                            || context.client().currentScreen instanceof NpcTradeScreen) {
                        context.client().setScreen(null);
                    }
                }));
        ClientPlayNetworking.registerGlobalReceiver(NpcBankQuotePayload.ID, (payload, context) ->
                context.client().execute(() -> {
                    if (context.client().currentScreen instanceof NpcBankScreen bank
                            && bank.belongsTo(payload)) {
                        bank.updateQuote(payload);
                    }
                }));
        ClientPlayNetworking.registerGlobalReceiver(NpcTradeSnapshotPayload.ID, (payload, context) ->
                context.client().execute(() -> {
                    if (context.client().currentScreen instanceof NpcTradeScreen trade
                            && trade.belongsTo(payload)) {
                        trade.updateSnapshot(payload);
                    }
                }));
        ClientPlayNetworking.registerGlobalReceiver(NpcTradeQuotePayload.ID, (payload, context) ->
                context.client().execute(() -> {
                    if (context.client().currentScreen instanceof NpcTradeScreen trade
                            && trade.belongsTo(payload)) {
                        trade.updateQuote(payload);
                    }
                }));
        ClientPlayNetworking.registerGlobalReceiver(NpcTradePurchaseResultPayload.ID, (payload, context) ->
                context.client().execute(() -> {
                    if (context.client().currentScreen instanceof NpcTradeScreen trade
                            && trade.belongsTo(payload)) {
                        trade.updatePurchaseResult(payload);
                    }
                }));
        ClientPlayNetworking.registerGlobalReceiver(NpcVisualSyncPayload.ID, (payload, context) ->
                context.client().execute(() -> NpcClientVisuals.replace(payload)));
    }
}
