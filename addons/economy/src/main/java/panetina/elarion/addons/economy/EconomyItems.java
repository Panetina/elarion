package panetina.elarion.addons.economy;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class EconomyItems {
    public static final Identifier CURRENCY_ID = Identifier.of("elarion", "currency");
    public static final Identifier ITEM_GROUP_ID = Identifier.of("elarion", "economy");
    public static final RegistryKey<ItemGroup> ITEM_GROUP_KEY =
            RegistryKey.of(RegistryKeys.ITEM_GROUP, ITEM_GROUP_ID);
    public static final int CURRENCY_MAX_STACK_SIZE = 999;
    public static final Item CURRENCY = new Item(new Item.Settings().maxCount(CURRENCY_MAX_STACK_SIZE));
    public static ItemGroup ITEM_GROUP;
    private static boolean registered;

    private EconomyItems() {
    }

    public static synchronized void register() {
        if (registered) return;
        Registry.register(Registries.ITEM, CURRENCY_ID, CURRENCY);
        ITEM_GROUP = Registry.register(
                Registries.ITEM_GROUP,
                ITEM_GROUP_ID,
                FabricItemGroup.builder()
                        .displayName(Text.translatable("itemGroup.elarion.economy"))
                        .icon(() -> new ItemStack(CURRENCY))
                        .entries((context, entries) -> entries.add(CURRENCY))
                        .build()
        );
        registered = true;
    }
}
