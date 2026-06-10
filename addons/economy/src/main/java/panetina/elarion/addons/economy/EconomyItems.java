package panetina.elarion.addons.economy;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class EconomyItems {
    public static final Identifier SIGIL_ID = Identifier.of("elarion", "sigil");
    public static final Identifier ITEM_GROUP_ID = Identifier.of("elarion", "economy");
    public static final Item SIGIL = new Item(new Item.Settings().maxCount(64));
    public static ItemGroup ITEM_GROUP;
    private static boolean registered;

    private EconomyItems() {
    }

    public static synchronized void register() {
        if (registered) return;
        Registry.register(Registries.ITEM, SIGIL_ID, SIGIL);
        ITEM_GROUP = Registry.register(
                Registries.ITEM_GROUP,
                ITEM_GROUP_ID,
                FabricItemGroup.builder()
                        .displayName(Text.translatable("itemGroup.elarion.economy"))
                        .icon(() -> new ItemStack(SIGIL))
                        .entries((context, entries) -> entries.add(SIGIL))
                        .build()
        );
        registered = true;
    }
}
