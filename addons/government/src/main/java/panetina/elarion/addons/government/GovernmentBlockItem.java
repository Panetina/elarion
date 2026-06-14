package panetina.elarion.addons.government;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;

public final class GovernmentBlockItem extends BlockItem {
    public GovernmentBlockItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (!context.getWorld().isClient
                && (!(context.getPlayer() instanceof ServerPlayerEntity player)
                || !player.hasPermissionLevel(4))) {
            if (context.getPlayer() instanceof ServerPlayerEntity player) {
                player.sendMessage(Text.literal("Only OP level 4 can place Government blocks."), false);
            }
            return ActionResult.FAIL;
        }
        return super.useOnBlock(context);
    }
}
