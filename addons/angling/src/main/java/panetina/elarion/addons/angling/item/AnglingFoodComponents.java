package panetina.elarion.addons.angling.item;

import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.ItemConvertible;

import java.util.Objects;

/** Exact immutable food definitions from the frozen Angling reference. */
public final class AnglingFoodComponents {
    private AnglingFoodComponents() {
    }

    public static FoodComponent rawFish(ItemConvertible remainder) {
        return new FoodComponent.Builder()
                .nutrition(2)
                .saturationModifier(0.1F)
                .usingConvertsTo(Objects.requireNonNull(remainder, "remainder"))
                .alwaysEdible()
                .build();
    }

    public static FoodComponent cookedFish(ItemConvertible remainder) {
        return new FoodComponent.Builder()
                .nutrition(6)
                .saturationModifier(2.0F)
                .usingConvertsTo(Objects.requireNonNull(remainder, "remainder"))
                .alwaysEdible()
                .build();
    }
}
