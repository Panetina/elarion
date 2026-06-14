package panetina.elarion.addons.offerings;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;
import net.minecraft.util.Identifier;

public final class OfferingsBlocks {
    public static final Identifier SHRINE_OF_FOUNDATION_ID =
            Identifier.of("elarion", "shrine_of_foundation");
    public static final Identifier SHRINE_OF_FOUNDATION_ITEM_ID =
            Identifier.of("elarion", "shrine_of_foundation");
    public static final Identifier ITEM_GROUP_ID = Identifier.of("elarion", "shrines");

    public static final ShrineOfFoundationBlock SHRINE_OF_FOUNDATION =
            new ShrineOfFoundationBlock(AbstractBlock.Settings.create()
                    .strength(3.0F, 6.0F)
                    .nonOpaque());
    public static final Item SHRINE_OF_FOUNDATION_ITEM =
            new ShrineOfFoundationItem(SHRINE_OF_FOUNDATION, new Item.Settings().maxCount(16));
    public static ItemGroup ITEM_GROUP;
    private static boolean registered;

    private OfferingsBlocks() {
    }

    public static synchronized void register() {
        if (registered) return;
        Registry.register(Registries.BLOCK, SHRINE_OF_FOUNDATION_ID, SHRINE_OF_FOUNDATION);
        Registry.register(Registries.ITEM, SHRINE_OF_FOUNDATION_ITEM_ID, SHRINE_OF_FOUNDATION_ITEM);
        ITEM_GROUP = Registry.register(
                Registries.ITEM_GROUP,
                ITEM_GROUP_ID,
                FabricItemGroup.builder()
                        .displayName(Text.translatable("itemGroup.elarion.shrines"))
                        .icon(() -> new ItemStack(SHRINE_OF_FOUNDATION_ITEM))
                        .entries((context, entries) -> entries.add(SHRINE_OF_FOUNDATION_ITEM))
                        .build()
        );
        registered = true;
    }

    public static BlockState partState(int partX, int partY, int partZ, Direction facing) {
        return SHRINE_OF_FOUNDATION.getDefaultState()
                .with(ShrineOfFoundationBlock.PART_X, partX)
                .with(ShrineOfFoundationBlock.PART_Y, partY)
                .with(ShrineOfFoundationBlock.PART_Z, partZ)
                .with(ShrineOfFoundationBlock.FACING, facing);
    }
}
