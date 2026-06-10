package panetina.elarion.core.mixin;

import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import panetina.elarion.core.api.ElarionApi;

@Mixin(PlayerAdvancementTracker.class)
public abstract class PlayerAdvancementTrackerMixin {
    @Shadow private ServerPlayerEntity owner;

    @Inject(method = "grantCriterion", at = @At("RETURN"))
    private void elarion$recordAdvancement(
            AdvancementEntry advancement,
            String criterionName,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!cir.getReturnValueZ() || owner == null) return;
        if (!((PlayerAdvancementTracker) (Object) this).getProgress(advancement).isDone()) return;
        ElarionApi.get().progression().recordAdvancement(owner, advancement.id());
    }
}
