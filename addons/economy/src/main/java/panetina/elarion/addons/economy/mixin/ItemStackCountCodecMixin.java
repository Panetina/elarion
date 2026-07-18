package panetina.elarion.addons.economy.mixin;

import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import panetina.elarion.addons.economy.EconomyItems;

@Mixin(ItemStack.class)
public abstract class ItemStackCountCodecMixin {
    @ModifyConstant(method = "method_57371", constant = @Constant(intValue = 99))
    private static int elarion$raiseSerializedStackCountLimit(int original) {
        return Math.max(original, EconomyItems.CURRENCY_MAX_STACK_SIZE);
    }
}
