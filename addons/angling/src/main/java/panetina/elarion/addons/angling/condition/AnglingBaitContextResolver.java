package panetina.elarion.addons.angling.condition;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import panetina.elarion.addons.angling.AnglingItems;

import java.util.Objects;
import java.util.Optional;

public final class AnglingBaitContextResolver {
    private AnglingBaitContextResolver() {
    }

    public static Optional<Identifier> resolve(ServerPlayerEntity player) {
        Objects.requireNonNull(player, "player");
        return resolve(
                Registries.ITEM.getId(player.getOffHandStack().getItem()),
                Registries.ITEM.getId(player.getMainHandStack().getItem()));
    }

    public static Optional<Identifier> resolve(
            Identifier offHandItemId,
            Identifier mainHandItemId
    ) {
        if (AnglingItems.PLACEHOLDER_BAIT_ITEM_ID.equals(offHandItemId)) {
            return Optional.of(AnglingItems.PLACEHOLDER_BAIT_ITEM_ID);
        }
        if (AnglingItems.PLACEHOLDER_BAIT_ITEM_ID.equals(mainHandItemId)) {
            return Optional.of(AnglingItems.PLACEHOLDER_BAIT_ITEM_ID);
        }
        return Optional.empty();
    }

    public static boolean consumeOne(ServerPlayerEntity player, Identifier baitId) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(baitId, "baitId");
        if (player.getAbilities().creativeMode) return true;
        if (consumeOne(player.getOffHandStack(), baitId)) return true;
        return consumeOne(player.getMainHandStack(), baitId);
    }

    private static boolean consumeOne(ItemStack stack, Identifier baitId) {
        if (stack.isEmpty()) return false;
        if (!baitId.equals(Registries.ITEM.getId(stack.getItem()))) return false;
        stack.decrement(1);
        return true;
    }
}
