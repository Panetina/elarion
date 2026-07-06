package panetina.elarion.addons.underworld.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import panetina.elarion.addons.underworld.block.UnderworldBlocks;
import panetina.elarion.addons.underworld.network.UnderworldStatusSyncPayload;
import panetina.elarion.addons.underworld.network.GraveOpenPayload;
import panetina.elarion.core.client.ClientIdentityDecorations;
import panetina.elarion.core.network.IdentitySyncRequestPayload;
import panetina.elarion.core.client.ui.ElarionHudOverlayRegistry;

import java.util.UUID;

public final class ElarionUnderworldClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(UnderworldBlocks.TOMB, RenderLayer.getCutout());
        BlockEntityRendererRegistry.register(UnderworldBlocks.TOMB_ENTITY, UnderworldTombBlockEntityRenderer::new);
        ElarionHudOverlayRegistry.registerBeforeNotifications(UnderworldStatusHud::render);
        ClientIdentityDecorations.registerTabSuffix(ElarionUnderworldClient::soulTabSuffix);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> UnderworldClientStatus.clear());
        ClientPlayNetworking.registerGlobalReceiver(UnderworldStatusSyncPayload.ID, (payload, context) ->
                context.client().execute(() -> {
                    boolean identityRelevant = UnderworldClientStatus.update(payload);
                    if (identityRelevant && context.client().getNetworkHandler() != null) {
                        ClientPlayNetworking.send(IdentitySyncRequestPayload.INSTANCE);
                    }
                }));
        ClientPlayNetworking.registerGlobalReceiver(GraveOpenPayload.ID, (payload, context) ->
                context.client().execute(() -> {
                    if (context.client().currentScreen instanceof GraveRecoveryScreen screen) screen.update(payload);
                    else context.client().setScreen(new GraveRecoveryScreen(payload));
                }));
    }

    private static Text soulTabSuffix(UUID uuid) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || !client.player.getUuid().equals(uuid)) {
            return Text.empty();
        }
        UnderworldStatusSyncPayload status = UnderworldClientStatus.current();
        if (status.fractures() <= 0 && !status.trueDeath()) {
            return Text.empty();
        }

        MutableText text = Text.literal("  Soul ").formatted(Formatting.DARK_PURPLE);
        int filled = Math.min(status.fractures(), status.maxFractures());
        for (int i = 0; i < status.maxFractures(); i++) {
            text.append(Text.literal(i < filled ? "◆" : "◇")
                    .formatted(status.trueDeath() ? Formatting.DARK_RED : Formatting.LIGHT_PURPLE));
        }
        return text;
    }
}
