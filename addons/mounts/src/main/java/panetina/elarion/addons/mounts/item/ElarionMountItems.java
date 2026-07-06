package panetina.elarion.addons.mounts.item;

import java.util.EnumMap;
import java.util.Map;

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
import panetina.elarion.addons.mounts.entity.ElarionMountType;

public final class ElarionMountItems {
    public static final Identifier ITEM_GROUP_ID = Identifier.of("elarion_mounts", "mounts");
    public static final RegistryKey<ItemGroup> ITEM_GROUP_KEY =
            RegistryKey.of(RegistryKeys.ITEM_GROUP, ITEM_GROUP_ID);

    private static final Map<ElarionMountType, Item> WHISTLES = new EnumMap<>(ElarionMountType.class);
    private static boolean registered;

    private ElarionMountItems() {
    }

    public static synchronized void register() {
        if (registered) return;
        for (ElarionMountType type : ElarionMountType.values()) {
            WHISTLES.put(type, Registry.register(
                    Registries.ITEM,
                    Identifier.of("elarion_mounts", type.itemId()),
                    new MountWhistleItem(type, new Item.Settings().maxCount(16))));
        }

        Registry.register(
                Registries.ITEM_GROUP,
                ITEM_GROUP_ID,
                FabricItemGroup.builder()
                        .displayName(Text.translatable("itemGroup.elarion_mounts.mounts"))
                        .icon(() -> new ItemStack(WHISTLES.get(ElarionMountType.WYVERN)))
                        .entries((context, entries) -> {
                            for (ElarionMountType type : ElarionMountType.values()) {
                                entries.add(WHISTLES.get(type));
                            }
                        })
                        .build()
        );
        registered = true;
    }

    public static Item whistle(ElarionMountType type) {
        return WHISTLES.get(type);
    }
}
