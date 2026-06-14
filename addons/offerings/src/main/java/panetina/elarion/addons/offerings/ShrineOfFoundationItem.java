package panetina.elarion.addons.offerings;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public final class ShrineOfFoundationItem extends Item {
    private final Block block;

    public ShrineOfFoundationItem(Block block, Settings settings) {
        super(settings);
        this.block = block;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        if (!world.isClient && (!(context.getPlayer() instanceof ServerPlayerEntity player)
                || !player.hasPermissionLevel(4))) {
            if (context.getPlayer() instanceof ServerPlayerEntity player) {
                player.sendMessage(Text.literal("Only OP level 4 can place Shrines of Foundation."), false);
            }
            return ActionResult.FAIL;
        }
        BlockPos origin = world.getBlockState(context.getBlockPos()).isAir()
                ? context.getBlockPos()
                : context.getBlockPos().offset(context.getSide());
        if (!ShrineOfFoundationBlock.canPlaceStructure(world, origin)) {
            if (!world.isClient && context.getPlayer() instanceof ServerPlayerEntity player) {
                player.sendMessage(Text.literal("A 2x2x5 clear space is required for this Shrine."), false);
            }
            return ActionResult.FAIL;
        }
        if (!world.isClient) {
            Direction facing = context.getHorizontalPlayerFacing().getOpposite();
            ShrineOfFoundationBlock.placeStructure(world, origin, facing);
            if (context.getPlayer() == null || !context.getPlayer().isCreative()) {
                context.getStack().decrement(1);
            }
        }
        return ActionResult.success(world.isClient);
    }
}
