package panetina.elarion.addons.names.mixin;

import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import panetina.elarion.core.client.ClientIdentityCache;

@Mixin(PlayerListHud.class)
public abstract class PlayerListHudMixin {
    @Inject(method = "getPlayerName", at = @At("RETURN"), cancellable = true)
    private void elarion$identityTabName(
            PlayerListEntry entry,
            CallbackInfoReturnable<Text> cir
    ) {
        ClientIdentityCache.find(entry.getProfile().getId())
                .filter(identity -> identity.visible())
                .ifPresent(identity -> cir.setReturnValue(identity.tabName()));
    }
}
