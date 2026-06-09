package panetina.elarion.addons.titles.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import panetina.elarion.core.client.ClientIdentityCache;

@Environment(EnvType.CLIENT)
@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin
        extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

    protected PlayerEntityRendererMixin(
            EntityRendererFactory.Context context,
            PlayerEntityModel<AbstractClientPlayerEntity> model,
            float shadowRadius
    ) {
        super(context, model, shadowRadius);
    }

    @Inject(
            method = "renderLabelIfPresent",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;renderLabelIfPresent(Lnet/minecraft/entity/Entity;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IF)V",
                    ordinal = 1
            )
    )
    private void elarion$renderTitle(
            AbstractClientPlayerEntity player,
            Text text,
            MatrixStack matrices,
            VertexConsumerProvider vertices,
            int light,
            float tickDelta,
            CallbackInfo ci
    ) {
        ClientIdentityCache.find(player.getUuid())
                .filter(identity -> identity.visible() && !identity.title().isBlank())
                .ifPresent(identity -> {
                    matrices.push();
                    matrices.scale(0.75f, 0.75f, 0.75f);
                    matrices.translate(0.0f, 0.6f, 0.0f);
                    super.renderLabelIfPresent(
                            player, identity.titleText(), matrices, vertices, light, tickDelta);
                    matrices.pop();
                    matrices.translate(0.0d, 0.1225d, 0.0d);
                });
    }
}
