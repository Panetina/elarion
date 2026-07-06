package panetina.elarion.addons.mounts.mixin;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import panetina.elarion.addons.mounts.client.ElarionMountRiderRenderContext;
import panetina.elarion.addons.mounts.entity.ElarionMountEntity;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private <E extends Entity> void elarionMounts$hideVanillaMountedRider(
            E entity,
            double x,
            double y,
            double z,
            float yaw,
            float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            CallbackInfo ci
    ) {
        if (entity.getVehicle() instanceof ElarionMountEntity
                && !ElarionMountRiderRenderContext.renderingMountedRider()) {
            ci.cancel();
        }
    }
}
