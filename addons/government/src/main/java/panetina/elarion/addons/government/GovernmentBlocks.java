package panetina.elarion.addons.government;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class GovernmentBlocks {
    public static final Identifier CIVIC_FORUM_ID = Identifier.of("elarion", "civic_forum");
    public static final Identifier SEAT_OF_RULE_ID = Identifier.of("elarion", "seat_of_rule");
    public static final Identifier ITEM_GROUP_ID = Identifier.of("elarion", "government");

    public static final Block CIVIC_FORUM = new Block(AbstractBlock.Settings.create()
            .strength(3.0F, 6.0F)
            .requiresTool());
    public static final Block SEAT_OF_RULE = new Block(AbstractBlock.Settings.create()
            .strength(3.0F, 6.0F)
            .requiresTool());

    public static final Item CIVIC_FORUM_ITEM =
            new GovernmentBlockItem(CIVIC_FORUM, new Item.Settings().maxCount(16));
    public static final Item SEAT_OF_RULE_ITEM =
            new GovernmentBlockItem(SEAT_OF_RULE, new Item.Settings().maxCount(16));

    public static ItemGroup ITEM_GROUP;
    private static boolean registered;

    private GovernmentBlocks() {
    }

    public static synchronized void register() {
        if (registered) return;
        Registry.register(Registries.BLOCK, CIVIC_FORUM_ID, CIVIC_FORUM);
        Registry.register(Registries.ITEM, CIVIC_FORUM_ID, CIVIC_FORUM_ITEM);
        Registry.register(Registries.BLOCK, SEAT_OF_RULE_ID, SEAT_OF_RULE);
        Registry.register(Registries.ITEM, SEAT_OF_RULE_ID, SEAT_OF_RULE_ITEM);
        ITEM_GROUP = Registry.register(
                Registries.ITEM_GROUP,
                ITEM_GROUP_ID,
                FabricItemGroup.builder()
                        .displayName(Text.translatable("itemGroup.elarion.government"))
                        .icon(() -> new ItemStack(CIVIC_FORUM_ITEM))
                        .entries((context, entries) -> {
                            entries.add(CIVIC_FORUM_ITEM);
                            entries.add(SEAT_OF_RULE_ITEM);
                        })
                        .build()
        );
        registered = true;
    }
}
