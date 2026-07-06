package panetina.elarion.addons.mounts.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import panetina.elarion.addons.mounts.client.ElarionMountsClient;
import panetina.elarion.addons.mounts.entity.ElarionMountEntity;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {
    @Unique
    private boolean elarionMounts$restoreInput;
    @Unique
    private boolean elarionMounts$savedJumping;
    @Unique
    private boolean elarionMounts$savedSneaking;

    @Inject(method = "dismountVehicle", at = @At("HEAD"), cancellable = true)
    private void elarionMounts$keepSneakAsMountInput(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        if (player.getVehicle() instanceof ElarionMountEntity && !ElarionMountsClient.allowClientDismount()) {
            ci.cancel();
        }
    }

    @Inject(method = "sendMovementPackets", at = @At("HEAD"))
    private void elarionMounts$suppressVanillaMountedInput(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        if (player.getVehicle() instanceof ElarionMountEntity && player.input != null) {
            elarionMounts$restoreInput = true;
            elarionMounts$savedJumping = player.input.jumping;
            elarionMounts$savedSneaking = player.input.sneaking;
            player.input.jumping = false;
            player.input.sneaking = false;
        }
    }

    @Inject(method = "sendMovementPackets", at = @At("RETURN"))
    private void elarionMounts$restoreVanillaMountedInput(CallbackInfo ci) {
        if (!elarionMounts$restoreInput) {
            return;
        }
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        if (player.input != null) {
            player.input.jumping = elarionMounts$savedJumping;
            player.input.sneaking = elarionMounts$savedSneaking;
        }
        elarionMounts$restoreInput = false;
    }
}
