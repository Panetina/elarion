package panetina.elarion.addons.underworld.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class UnderworldTombBlock extends BlockWithEntity {
    public static final int WIDTH_BLOCKS = 1;
    public static final int HEIGHT_BLOCKS = 2;
    public static final int DEPTH_BLOCKS = 1;

    public static final IntProperty PART_Y = IntProperty.of("part_y", 0, HEIGHT_BLOCKS - 1);
    public static final IntProperty VARIANT = IntProperty.of("variant", 0, 2);
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;

    private static final VoxelShape[] NORTH_SHAPES = new VoxelShape[]{
            northShape(TombstoneVariant.TOMBSTONE_1),
            northShape(TombstoneVariant.TOMBSTONE_2),
            northShape(TombstoneVariant.TOMBSTONE_3)
    };
    private static final VoxelShape[] SOUTH_SHAPES = new VoxelShape[]{
            southShape(TombstoneVariant.TOMBSTONE_1),
            southShape(TombstoneVariant.TOMBSTONE_2),
            southShape(TombstoneVariant.TOMBSTONE_3)
    };
    private static final VoxelShape[] EAST_SHAPES = new VoxelShape[]{
            eastShape(TombstoneVariant.TOMBSTONE_1),
            eastShape(TombstoneVariant.TOMBSTONE_2),
            eastShape(TombstoneVariant.TOMBSTONE_3)
    };
    private static final VoxelShape[] WEST_SHAPES = new VoxelShape[]{
            westShape(TombstoneVariant.TOMBSTONE_1),
            westShape(TombstoneVariant.TOMBSTONE_2),
            westShape(TombstoneVariant.TOMBSTONE_3)
    };

    public UnderworldTombBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState()
                .with(PART_Y, 0)
                .with(VARIANT, 0)
                .with(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return createCodec(UnderworldTombBlock::new);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(PART_Y, VARIANT, FACING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return getDefaultState().with(FACING, context.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return state.get(PART_Y) == 0 ? new UnderworldTombBlockEntity(pos, state) : null;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return shapeForState(state);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return shapeForState(state);
    }

    public static VoxelShape shapeForState(BlockState state) {
        return state.get(PART_Y) == 0 ? visualShape(TombstoneVariant.byBlockStateId(state.get(VARIANT)),
                state.get(FACING)) : VoxelShapes.empty();
    }

    public static VoxelShape visualShape(TombstoneVariant variant, Direction facing) {
        int index = variant.blockStateId();
        return switch (facing) {
            case SOUTH -> SOUTH_SHAPES[index];
            case EAST -> EAST_SHAPES[index];
            case WEST -> WEST_SHAPES[index];
            default -> NORTH_SHAPES[index];
        };
    }

    private static VoxelShape northShape(TombstoneVariant variant) {
        return Block.createCuboidShape(variant.minX(), variant.minY(), variant.minZ(),
                variant.maxX(), variant.maxY(), variant.maxZ());
    }

    private static VoxelShape southShape(TombstoneVariant variant) {
        return Block.createCuboidShape(variant.minX(), variant.minY(), 16.0D - variant.maxZ(),
                variant.maxX(), variant.maxY(), 16.0D - variant.minZ());
    }

    private static VoxelShape eastShape(TombstoneVariant variant) {
        return Block.createCuboidShape(16.0D - variant.maxZ(), variant.minY(), variant.minX(),
                16.0D - variant.minZ(), variant.maxY(), variant.maxX());
    }

    private static VoxelShape westShape(TombstoneVariant variant) {
        return Block.createCuboidShape(variant.minZ(), variant.minY(), variant.minX(),
                variant.maxZ(), variant.maxY(), variant.maxX());
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient) {
            removeStructure(world, origin(pos, state), pos);
        }
        return super.onBreak(world, pos, state, player);
    }

    public static BlockPos origin(BlockPos pos, BlockState state) {
        return pos.down(state.get(PART_Y));
    }

    public static boolean canPlaceStructure(World world, BlockPos origin) {
        for (int y = 0; y < HEIGHT_BLOCKS; y++) {
            BlockPos part = origin.up(y);
            if (!world.isInBuildLimit(part) || !world.getBlockState(part).isAir()) return false;
        }
        return true;
    }

    public static void placeStructure(World world, BlockPos origin, Direction facing, TombstoneVariant variant, String corpseId) {
        for (int y = 0; y < HEIGHT_BLOCKS; y++) {
            world.setBlockState(origin.up(y), UnderworldBlocks.tombState(y, facing, variant), Block.NOTIFY_ALL);
        }
        if (world.getBlockEntity(origin) instanceof UnderworldTombBlockEntity tomb) {
            tomb.setCorpseId(corpseId);
        }
    }

    public static void removeStructure(World world, BlockPos origin) {
        removeStructure(world, origin, null);
    }

    private static void removeStructure(World world, BlockPos origin, @Nullable BlockPos brokenPos) {
        for (int y = 0; y < HEIGHT_BLOCKS; y++) {
            BlockPos part = origin.up(y);
            if (part.equals(brokenPos)) continue;
            if (world.getBlockState(part).isOf(UnderworldBlocks.TOMB)) {
                world.removeBlock(part, false);
            }
        }
    }
}
