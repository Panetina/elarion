package panetina.elarion.addons.npcs.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import panetina.elarion.addons.npcs.entity.ElarionNpcEntities;
import panetina.elarion.addons.npcs.network.NpcDialogueClosePayload;
import panetina.elarion.addons.npcs.network.NpcDialogueOpenPayload;
import panetina.elarion.addons.npcs.network.NpcVisualSyncPayload;

public final class ElarionNpcsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ElarionNpcEntities.NPC, ElarionNpcEntityRenderer::new);
        ClientPlayNetworking.registerGlobalReceiver(NpcDialogueOpenPayload.ID, (payload, context) ->
                context.client().execute(() -> {
                    if (context.client().currentScreen instanceof NpcDialogueScreen screen
                            && screen.belongsTo(payload)) {
                        screen.updateDialogue(payload);
                    } else {
                        context.client().setScreen(new NpcDialogueScreen(payload));
                    }
                }));
        ClientPlayNetworking.registerGlobalReceiver(NpcDialogueClosePayload.ID, (payload, context) ->
                context.client().execute(() -> {
                    if (context.client().currentScreen instanceof NpcDialogueScreen) {
                        context.client().setScreen(null);
                    }
                }));
        ClientPlayNetworking.registerGlobalReceiver(NpcVisualSyncPayload.ID, (payload, context) ->
                context.client().execute(() -> NpcClientVisuals.replace(payload)));
    }
}
