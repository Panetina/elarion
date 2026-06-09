package panetina.elarion.core.mixin.client;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import panetina.elarion.core.client.ClientIdentityCache;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererVisibilityMixin {
    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"), cancellable = true)
    private void elarion$hideRestrictedNametag(
            AbstractClientPlayerEntity player,
            Text text,
            MatrixStack matrices,
            VertexConsumerProvider vertices,
            int light,
            float tickDelta,
            CallbackInfo ci
    ) {
        if (ClientIdentityCache.isKnownHidden(player.getUuid())) ci.cancel();
    }
}
