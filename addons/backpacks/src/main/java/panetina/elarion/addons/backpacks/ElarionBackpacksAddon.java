package panetina.elarion.addons.backpacks;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.api.ElarionAddon;
import panetina.elarion.core.api.ElarionApi;

import java.util.List;

public final class ElarionBackpacksAddon implements ElarionAddon {
    private static final Logger LOGGER = LoggerFactory.getLogger("elarion_backpacks");

    @Override
    public void initialize(ElarionApi api) {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> removeBackpackRecipes(server.getRecipeManager()));
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            if (success) {
                removeBackpackRecipes(server.getRecipeManager());
            }
        });
        LOGGER.info("Elarion Backpacks initialized; Yyz backpack crafting is disabled");
    }

    static int removeBackpackRecipes(RecipeManager recipes) {
        List<RecipeEntry<?>> retained = recipes.values().stream()
                .filter(entry -> !BackpackRecipePolicy.blocksRecipe(entry.id()))
                .toList();
        int removed = recipes.values().size() - retained.size();
        if (removed > 0) {
            recipes.setRecipes(retained);
            LOGGER.info("Disabled {} Yyz backpack crafting and smithing recipes", removed);
        }
        return removed;
    }
}
