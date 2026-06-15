package panetina.elarion.addons.portals.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.render.RenderLayer;
import panetina.elarion.addons.portals.PortalContent;
import panetina.elarion.addons.portals.network.PortalScreenClosePayload;
import panetina.elarion.addons.portals.network.PortalRouteStatusSyncPayload;
import panetina.elarion.addons.portals.network.PortalTravelPromptPayload;
import panetina.elarion.addons.portals.network.PortalVisualSyncPayload;

public final class ElarionPortalsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(PortalContent.FIELD, RenderLayer.getTranslucent());
        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) ->
                PortalClientVisuals.color(pos), PortalContent.FIELD);
        HudRenderCallback.EVENT.register((context, tickCounter) -> PortalStatusHud.render(context));
        ClientPlayNetworking.registerGlobalReceiver(PortalVisualSyncPayload.ID, (payload, context) ->
                context.client().execute(() -> PortalClientVisuals.replace(payload)));
        ClientPlayNetworking.registerGlobalReceiver(PortalRouteStatusSyncPayload.ID, (payload, context) ->
                context.client().execute(() -> PortalClientRouteStatus.replace(payload)));
        ClientPlayNetworking.registerGlobalReceiver(PortalTravelPromptPayload.ID, (payload, context) ->
                context.client().execute(() -> context.client().setScreen(new PortalConfirmationScreen(payload))));
        ClientPlayNetworking.registerGlobalReceiver(PortalScreenClosePayload.ID, (payload, context) ->
                context.client().execute(() -> {
                    if (context.client().currentScreen instanceof PortalConfirmationScreen) {
                        context.client().setScreen(null);
                    }
                }));
    }
}
