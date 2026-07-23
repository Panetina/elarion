package panetina.elarion.addons.angling.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import panetina.elarion.addons.angling.component.AnglingDataComponents;
import panetina.elarion.addons.angling.component.AnglingSingleStackComponent;
import panetina.elarion.addons.angling.fishing.AnglingFishingRuntime;

/** Fabric rod item; all mutable authority is delegated to the server-bound fishing service. */
public final class AnglingFishingRodItem extends Item {
    public AnglingFishingRodItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack rod = user.getStackInHand(hand);
        if (rod.getOrDefault(AnglingDataComponents.HOOK, AnglingSingleStackComponent.EMPTY).stack().isEmpty()
                || rod.getOrDefault(AnglingDataComponents.BOBBER, AnglingSingleStackComponent.EMPTY).stack().isEmpty()) {
            if (!world.isClient()) user.sendMessage(Text.translatable("gui.elarion_angling.no_hook_or_bobber"), true);
            return TypedActionResult.fail(rod);
        }
        if (world.isClient()) return TypedActionResult.success(rod, true);
        if (!(user instanceof ServerPlayerEntity player) || !AnglingFishingRuntime.use(player, hand, rod)) {
            return TypedActionResult.fail(rod);
        }
        return TypedActionResult.success(rod, false);
    }

    @Override
    public boolean hasRecipeRemainder() {
        return true;
    }

    @Override
    public ItemStack getRecipeRemainder(ItemStack stack) {
        return stack.copy();
    }
}
