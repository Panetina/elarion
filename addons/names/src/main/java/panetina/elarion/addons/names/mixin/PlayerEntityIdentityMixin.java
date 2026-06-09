package panetina.elarion.addons.names.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import panetina.elarion.core.api.ElarionApi;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityIdentityMixin {
    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void elarion$serverIdentityDisplayName(CallbackInfoReturnable<Text> cir) {
        if ((Object) this instanceof ServerPlayerEntity player) {
            cir.setReturnValue(ElarionApi.get().identities().resolve(player).displayName());
        }
    }
}
