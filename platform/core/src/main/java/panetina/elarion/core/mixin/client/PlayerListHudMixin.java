package panetina.elarion.core.mixin.client;

import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import panetina.elarion.core.client.ClientIdentityCache;

import java.util.ArrayList;
import java.util.List;

@Mixin(PlayerListHud.class)
public abstract class PlayerListHudMixin {
    @Inject(method = "collectPlayerEntries", at = @At("RETURN"), cancellable = true)
    private void elarion$filterPlayerEntries(CallbackInfoReturnable<List<PlayerListEntry>> cir) {
        List<PlayerListEntry> visible = new ArrayList<>(cir.getReturnValue());
        visible.removeIf(entry -> ClientIdentityCache.isKnownHidden(entry.getProfile().getId()));
        cir.setReturnValue(visible);
    }

}
