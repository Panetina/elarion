package panetina.elarion.addons.underworld.block;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public final class UnderworldBlocks {
    public static final Identifier TOMB_ID = Identifier.of("elarion_underworld", "tomb");
    public static final Identifier TOMB_ENTITY_ID = Identifier.of("elarion_underworld", "tomb");

    public static final UnderworldTombBlock TOMB = new UnderworldTombBlock(AbstractBlock.Settings.create()
            .strength(-1.0F, 3600000.0F)
            .nonOpaque()
            .dropsNothing());
    public static final Item TOMB_ITEM = new BlockItem(TOMB, new Item.Settings().maxCount(16));
    public static BlockEntityType<UnderworldTombBlockEntity> TOMB_ENTITY;
    private static boolean registered;

    private UnderworldBlocks() {
    }

    public static synchronized void register() {
        if (registered) return;
        Registry.register(Registries.BLOCK, TOMB_ID, TOMB);
        Registry.register(Registries.ITEM, TOMB_ID, TOMB_ITEM);
        TOMB_ENTITY = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                TOMB_ENTITY_ID,
                FabricBlockEntityTypeBuilder.create(UnderworldTombBlockEntity::new, TOMB).build()
        );
        registered = true;
    }

    public static BlockState tombState(int partY, Direction facing, TombstoneVariant variant) {
        return TOMB.getDefaultState()
                .with(UnderworldTombBlock.PART_Y, partY)
                .with(UnderworldTombBlock.FACING, facing)
                .with(UnderworldTombBlock.VARIANT, variant.blockStateId());
    }
}
