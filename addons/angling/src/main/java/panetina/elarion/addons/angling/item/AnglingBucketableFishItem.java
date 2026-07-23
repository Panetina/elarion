package panetina.elarion.addons.angling.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import panetina.elarion.addons.angling.fishing.AnglingFishEntity;
import panetina.elarion.addons.angling.registry.AnglingEntities;

/** Frozen creative placement behavior for the 48 bucketable native fish items. */
public final class AnglingBucketableFishItem extends Item {
    public AnglingBucketableFishItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getPlayer() == null || !context.getPlayer().isCreative()) return ActionResult.PASS;
        if (!(context.getWorld() instanceof ServerWorld world)) return ActionResult.SUCCESS;
        AnglingFishEntity fish = AnglingEntities.FISH.create(world);
        if (fish == null) return ActionResult.FAIL;
        fish.setFish(context.getStack());
        fish.refreshPositionAndAngles(
                context.getBlockPos().offset(context.getSide()).toCenterPos(), 0.0F, 0.0F);
        if (!world.spawnEntity(fish)) return ActionResult.FAIL;
        context.getStack().decrement(1);
        return ActionResult.CONSUME;
    }
}
