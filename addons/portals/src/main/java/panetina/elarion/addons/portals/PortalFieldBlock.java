package panetina.elarion.addons.portals;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public final class PortalFieldBlock extends Block {
    public static final EnumProperty<Direction.Axis> AXIS = EnumProperty.of("axis", Direction.Axis.class);
    private static final VoxelShape X_SHAPE = Block.createCuboidShape(6, 0, 0, 10, 16, 16);
    private static final VoxelShape Y_SHAPE = Block.createCuboidShape(0, 6, 0, 16, 10, 16);
    private static final VoxelShape Z_SHAPE = Block.createCuboidShape(0, 0, 6, 16, 16, 10);

    public PortalFieldBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(AXIS, Direction.Axis.X));
    }

    @Override
    protected MapCodec<? extends Block> getCodec() {
        return createCodec(PortalFieldBlock::new);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    protected VoxelShape getOutlineShape(
            BlockState state, BlockView world, BlockPos pos, ShapeContext context
    ) {
        return switch (state.get(AXIS)) {
            case X -> X_SHAPE;
            case Y -> Y_SHAPE;
            case Z -> Z_SHAPE;
        };
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state, BlockView world, BlockPos pos, ShapeContext context
    ) {
        return VoxelShapes.empty();
    }
}
