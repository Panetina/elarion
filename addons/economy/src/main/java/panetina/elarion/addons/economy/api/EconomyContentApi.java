package panetina.elarion.addons.economy.api;

import net.minecraft.item.ItemGroup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

/**
 * Stable Economy-owned content identities for addons that need to reference
 * an Economy item or contribute an entry to its item group.
 */
public final class EconomyContentApi {
    private static final Identifier CURRENCY_ITEM_ID = Identifier.of("elarion", "currency");
    private static final Identifier ITEM_GROUP_ID = Identifier.of("elarion", "economy");
    private static final RegistryKey<ItemGroup> ITEM_GROUP_KEY =
            RegistryKey.of(RegistryKeys.ITEM_GROUP, ITEM_GROUP_ID);

    private EconomyContentApi() {
    }

    public static Identifier currencyItemId() {
        return CURRENCY_ITEM_ID;
    }

    public static RegistryKey<ItemGroup> itemGroupKey() {
        return ITEM_GROUP_KEY;
    }
}
