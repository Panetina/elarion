package panetina.elarion.addons.economy.mixin;

import net.minecraft.inventory.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import panetina.elarion.addons.economy.EconomyItems;

@Mixin(Inventory.class)
public interface InventoryMaxStackCountMixin {
    @Inject(method = "getMaxCountPerStack", at = @At("HEAD"), cancellable = true)
    private void elarion$raiseInventoryStackCeiling(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(EconomyItems.CURRENCY_MAX_STACK_SIZE);
    }
}
