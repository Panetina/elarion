package panetina.elarion.addons.mounts.mixin;

import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.Perspective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import panetina.elarion.addons.mounts.client.ElarionMountCamera;

@Mixin(GameOptions.class)
public abstract class GameOptionsMixin {
    @ModifyVariable(method = "setPerspective", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private Perspective elarionMounts$skipFirstPersonWhileMounted(Perspective perspective) {
        return ElarionMountCamera.mountedPerspective(perspective);
    }
}
