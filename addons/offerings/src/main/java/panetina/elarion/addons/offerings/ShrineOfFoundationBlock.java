package panetina.elarion.addons.offerings;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public final class ShrineOfFoundationBlock extends Block {
    public static final int WIDTH_BLOCKS = 2;
    public static final int DEPTH_BLOCKS = 2;
    public static final int HEIGHT_BLOCKS = 5;
    public static final int WIDTH_PIXELS = WIDTH_BLOCKS * 16;
    public static final int DEPTH_PIXELS = DEPTH_BLOCKS * 16;
    public static final int HEIGHT_PIXELS = HEIGHT_BLOCKS * 16;

    public static final IntProperty PART_X = IntProperty.of("part_x", 0, WIDTH_BLOCKS - 1);
    public static final IntProperty PART_Y = IntProperty.of("part_y", 0, HEIGHT_BLOCKS - 1);
    public static final IntProperty PART_Z = IntProperty.of("part_z", 0, DEPTH_BLOCKS - 1);
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;

    public ShrineOfFoundationBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState()
                .with(PART_X, 0)
                .with(PART_Y, 0)
                .with(PART_Z, 0)
                .with(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends Block> getCodec() {
        return createCodec(ShrineOfFoundationBlock::new);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(PART_X, PART_Y, PART_Z, FACING);
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient) {
            BlockPos origin = origin(pos, state);
            removeStructure(world, origin, pos);
        }
        return super.onBreak(world, pos, state, player);
    }

    public static BlockPos origin(BlockPos pos, BlockState state) {
        return pos.add(
                -state.get(PART_X),
                -state.get(PART_Y),
                -state.get(PART_Z)
        );
    }

    public static boolean canPlaceStructure(World world, BlockPos origin) {
        for (int y = 0; y < HEIGHT_BLOCKS; y++) {
            for (int x = 0; x < WIDTH_BLOCKS; x++) {
                for (int z = 0; z < DEPTH_BLOCKS; z++) {
                    BlockPos part = origin.add(x, y, z);
                    if (!world.getBlockState(part).isAir()) return false;
                }
            }
        }
        return true;
    }

    public static void placeStructure(World world, BlockPos origin, Direction facing) {
        for (int y = 0; y < HEIGHT_BLOCKS; y++) {
            for (int x = 0; x < WIDTH_BLOCKS; x++) {
                for (int z = 0; z < DEPTH_BLOCKS; z++) {
                    world.setBlockState(origin.add(x, y, z), OfferingsBlocks.partState(x, y, z, facing),
                            Block.NOTIFY_ALL);
                }
            }
        }
    }

    public static void removeStructure(World world, BlockPos origin) {
        removeStructure(world, origin, null);
    }

    private static void removeStructure(World world, BlockPos origin, BlockPos brokenPos) {
        for (int y = 0; y < HEIGHT_BLOCKS; y++) {
            for (int x = 0; x < WIDTH_BLOCKS; x++) {
                for (int z = 0; z < DEPTH_BLOCKS; z++) {
                    BlockPos part = origin.add(x, y, z);
                    if (part.equals(brokenPos)) continue;
                    if (world.getBlockState(part).isOf(OfferingsBlocks.SHRINE_OF_FOUNDATION)) {
                        world.removeBlock(part, false);
                    }
                }
            }
        }
    }
}
