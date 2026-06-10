package panetina.elarion.core.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import panetina.elarion.core.api.ElarionApi;

@Mixin(net.minecraft.screen.slot.CraftingResultSlot.class)
public abstract class CraftingResultSlotMixin extends Slot {
    private CraftingResultSlotMixin(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Inject(method = "onTakeItem", at = @At("TAIL"))
    private void elarion$recordCraft(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        if (!(player instanceof ServerPlayerEntity serverPlayer) || stack.isEmpty()) return;
        RecipeEntry<?> recipe = inventory instanceof CraftingResultInventory result
                ? result.getLastRecipe()
                : null;
        ElarionApi.get().progression().recordCraft(
                serverPlayer,
                stack.copy(),
                recipe == null ? null : recipe.id()
        );
    }
}
