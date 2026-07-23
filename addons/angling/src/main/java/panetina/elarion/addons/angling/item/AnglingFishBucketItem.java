package panetina.elarion.addons.angling.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.EntityBucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import panetina.elarion.addons.angling.component.AnglingDataComponents;
import panetina.elarion.addons.angling.component.AnglingSingleStackComponent;
import panetina.elarion.addons.angling.fishing.AnglingFishEntity;
import panetina.elarion.addons.angling.registry.AnglingEntities;

/** Water bucket that restores the exact caught fish item and its quality components. */
public final class AnglingFishBucketItem extends EntityBucketItem {
    public AnglingFishBucketItem(Settings settings) {
        super(AnglingEntities.FISH, Fluids.WATER, SoundEvents.ITEM_BUCKET_EMPTY_FISH, settings);
    }

    @Override
    public void onEmptied(PlayerEntity player, World world, ItemStack bucket, BlockPos pos) {
        if (!(world instanceof ServerWorld serverWorld)) return;
        AnglingSingleStackComponent stored = bucket.getOrDefault(
                AnglingDataComponents.BUCKETED_FISH, AnglingSingleStackComponent.EMPTY);
        if (stored.isEmpty()) return;
        AnglingFishEntity fish = AnglingEntities.FISH.create(serverWorld);
        if (fish == null) return;
        fish.setFish(stored.stack());
        fish.refreshPositionAndAngles(pos.toCenterPos(), 0.0F, 0.0F);
        serverWorld.spawnEntity(fish);
    }
}
