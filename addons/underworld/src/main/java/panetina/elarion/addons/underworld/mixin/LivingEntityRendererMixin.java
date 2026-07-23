package panetina.elarion.addons.underworld.mixin;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import panetina.elarion.addons.underworld.client.UnderworldSoulSight;
import panetina.elarion.addons.underworld.client.UnderworldSpectralTextures;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @Unique private boolean elarionUnderworld$shadowPass;
    @Unique private boolean elarionUnderworld$banishedPass;
    @Unique private VertexConsumerProvider elarionUnderworld$vertices;

    @Inject(
            method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD")
    )
    private void elarionUnderworld$beginShadowPass(
            LivingEntity entity,
            float yaw,
            float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider vertices,
            int light,
            CallbackInfo ci
    ) {
        UnderworldSoulSight.PlayerAppearance appearance = entity instanceof AbstractClientPlayerEntity player
                ? UnderworldSoulSight.appearance(player)
                : UnderworldSoulSight.PlayerAppearance.NORMAL;
        elarionUnderworld$shadowPass = appearance == UnderworldSoulSight.PlayerAppearance.SHADOW;
        elarionUnderworld$banishedPass = appearance == UnderworldSoulSight.PlayerAppearance.BANISHED;
        elarionUnderworld$vertices = vertices;
    }

    @ModifyArg(
            method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V"),
            index = 1
    )
    private VertexConsumer elarionUnderworld$replacePlayerTexture(VertexConsumer original) {
        if ((!elarionUnderworld$shadowPass && !elarionUnderworld$banishedPass)
                || elarionUnderworld$vertices == null) {
            return original;
        }
        return elarionUnderworld$vertices.getBuffer(
                RenderLayer.getEntityCutoutNoCull(
                        UnderworldSpectralTextures.modelTexture(elarionUnderworld$banishedPass)));
    }

    @ModifyArg(
            method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V"),
            index = 4
    )
    private int elarionUnderworld$blackenPlayerModel(int color) {
        return elarionUnderworld$shadowPass || elarionUnderworld$banishedPass ? 0xFFFFFFFF : color;
    }

    @ModifyArg(
            method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V"),
            index = 2
    )
    private int elarionUnderworld$lightBanishedPlayer(int light) {
        return elarionUnderworld$shadowPass || elarionUnderworld$banishedPass
                ? LightmapTextureManager.MAX_LIGHT_COORDINATE : light;
    }

    @Redirect(
            method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;")
    )
    private Iterator<FeatureRenderer<?, ?>> elarionUnderworld$hidePlayerFeatures(List<FeatureRenderer<?, ?>> features) {
        return elarionUnderworld$shadowPass || elarionUnderworld$banishedPass
                ? Collections.emptyIterator() : features.iterator();
    }

    @Inject(
            method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("RETURN")
    )
    private void elarionUnderworld$endShadowPass(
            LivingEntity entity,
            float yaw,
            float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider vertices,
            int light,
            CallbackInfo ci
    ) {
        elarionUnderworld$shadowPass = false;
        elarionUnderworld$banishedPass = false;
        elarionUnderworld$vertices = null;
    }
}
