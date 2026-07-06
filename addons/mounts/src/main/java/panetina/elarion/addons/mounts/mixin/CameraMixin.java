package panetina.elarion.addons.mounts.mixin;

import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import panetina.elarion.addons.mounts.client.ElarionMountCamera;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @ModifyVariable(method = "clipToSpace", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float elarionMounts$useMountedCameraDistance(float desiredCameraDistance) {
        return ElarionMountCamera.mountedDistance(desiredCameraDistance);
    }
}
