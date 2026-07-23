package panetina.elarion.addons.underworld.mixin;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import panetina.elarion.addons.underworld.service.UnderworldService;

@Mixin(ItemEntity.class)
abstract class BanishmentItemPickupMixin {
    @Inject(method = "onPlayerCollision", at = @At("HEAD"), cancellable = true)
    private void elarion$blockBanishedPickup(PlayerEntity player, CallbackInfo callback) {
        if (player instanceof ServerPlayerEntity serverPlayer
                && UnderworldService.blocksBanishmentProgression(serverPlayer)) {
            callback.cancel();
        }
    }
}
