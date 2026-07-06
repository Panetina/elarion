package panetina.elarion.addons.mounts.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import panetina.elarion.addons.mounts.entity.ElarionMountType;

public final class MountWhistleItem extends Item {
    private final ElarionMountType mountType;

    public MountWhistleItem(ElarionMountType mountType, Settings settings) {
        super(settings);
        this.mountType = mountType;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient()) {
            user.sendMessage(Text.literal("Mount whistles are deprecated. Open Collection with C and press R to summon your active mount."), false);
        }
        return TypedActionResult.success(stack, world.isClient());
    }
}
