package panetina.elarion.addons.mounts.mixin;

import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import panetina.elarion.addons.mounts.entity.ElarionMountEntity;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Inject(method = "shouldDismount", at = @At("HEAD"), cancellable = true)
    private void elarionMounts$shiftIsMountInput(CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (player.getVehicle() instanceof ElarionMountEntity) {
            cir.setReturnValue(false);
        }
    }
}
