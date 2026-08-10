package panetina.elarion.addons.backpacks.mixin;

import net.minecraft.recipe.ArmorDyeRecipe;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import panetina.elarion.addons.backpacks.BackpackRecipePolicy;

@Mixin(ArmorDyeRecipe.class)
public abstract class ArmorDyeRecipeMixin {
    @Inject(method = "matches(Lnet/minecraft/recipe/input/CraftingRecipeInput;Lnet/minecraft/world/World;)Z",
            at = @At("HEAD"), cancellable = true)
    private void elarion$rejectBackpackDyeing(CraftingRecipeInput input, World world,
                                              CallbackInfoReturnable<Boolean> result) {
        if (BackpackRecipePolicy.containsBackpack(input.getStacks())) {
            result.setReturnValue(false);
        }
    }
}
