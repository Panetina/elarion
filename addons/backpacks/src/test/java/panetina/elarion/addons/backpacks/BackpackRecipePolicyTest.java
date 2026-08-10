package panetina.elarion.addons.backpacks;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BackpackRecipePolicyTest {
    @Test
    void blocksEveryRecipeOwnedByTheUpstreamBackpackNamespace() {
        assertTrue(BackpackRecipePolicy.blocksRecipe(Identifier.of("yyzsbackpack", "iron_backpack")));
        assertTrue(BackpackRecipePolicy.blocksRecipe(Identifier.of("yyzsbackpack", "netherite_backpack_smithing")));
        assertFalse(BackpackRecipePolicy.blocksRecipe(Identifier.of("minecraft", "chest")));
    }

    @Test
    void recognizesOnlyYyzBackpackItemsForSpecialDyeRecipeBlocking() {
        assertTrue(BackpackRecipePolicy.isBackpackId(Identifier.of("yyzsbackpack", "iron_backpack")));
        assertTrue(BackpackRecipePolicy.isBackpackId(Identifier.of("yyzsbackpack", "netherite_backpack")));
        assertFalse(BackpackRecipePolicy.isBackpackId(Identifier.of("yyzsbackpack", "upgrade")));
        assertFalse(BackpackRecipePolicy.isBackpackId(Identifier.of("minecraft", "bundle")));
    }
}
