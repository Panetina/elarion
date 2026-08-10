package panetina.elarion.addons.backpacks;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.List;

public final class BackpackRecipePolicy {
    public static final String BACKPACK_NAMESPACE = "yyzsbackpack";

    private BackpackRecipePolicy() {
    }

    public static boolean blocksRecipe(Identifier recipeId) {
        return BACKPACK_NAMESPACE.equals(recipeId.getNamespace());
    }

    public static boolean containsBackpack(List<ItemStack> stacks) {
        return stacks.stream().anyMatch(BackpackRecipePolicy::isBackpack);
    }

    public static boolean isBackpack(ItemStack stack) {
        return !stack.isEmpty() && isBackpackId(Registries.ITEM.getId(stack.getItem()));
    }

    public static boolean isBackpackId(Identifier itemId) {
        return BACKPACK_NAMESPACE.equals(itemId.getNamespace())
                && itemId.getPath().endsWith("_backpack");
    }
}
