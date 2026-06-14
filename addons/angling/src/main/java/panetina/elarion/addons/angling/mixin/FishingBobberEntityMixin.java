package panetina.elarion.addons.angling.mixin;

import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import panetina.elarion.addons.angling.integration.VanillaFishingHooks;

@Mixin(FishingBobberEntity.class)
public abstract class FishingBobberEntityMixin {
    @Unique
    private boolean elarionAngling$selectionAttempted;

    @Inject(method = "tickFishingLogic", at = @At("HEAD"))
    private void elarionAngling$onFishingTick(BlockPos pos, CallbackInfo ci) {
        if (elarionAngling$selectionAttempted) return;
        elarionAngling$selectionAttempted = true;
        FishingBobberEntity bobber = (FishingBobberEntity) (Object) this;
        VanillaFishingHooks.onFishingTick(bobber, (ServerWorld) bobber.getWorld(), pos);
    }

    @Inject(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/loot/LootTable;generateLoot(Lnet/minecraft/loot/context/LootContextParameterSet;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;",
                    shift = At.Shift.BEFORE),
            cancellable = true)
    private void elarionAngling$beforeVanillaFishingLoot(
            ItemStack usedItem,
            CallbackInfoReturnable<Integer> cir
    ) {
        if (!VanillaFishingHooks.beforeVanillaFishingLoot(
                (FishingBobberEntity) (Object) this)) {
            cir.setReturnValue(0);
        }
    }
}
