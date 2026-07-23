package panetina.elarion.addons.angling.component;

import com.mojang.serialization.Codec;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;

import java.util.Objects;

/** Immutable copy boundary for a single item stored inside another stack. */
public final class AnglingSingleStackComponent {
    public static final Codec<AnglingSingleStackComponent> CODEC = ItemStack.OPTIONAL_CODEC.xmap(
            AnglingSingleStackComponent::new,
            AnglingSingleStackComponent::stack
    );
    public static final PacketCodec<RegistryByteBuf, AnglingSingleStackComponent> PACKET_CODEC =
            ItemStack.OPTIONAL_PACKET_CODEC.xmap(AnglingSingleStackComponent::new, AnglingSingleStackComponent::stack);
    public static final AnglingSingleStackComponent EMPTY = new AnglingSingleStackComponent(ItemStack.EMPTY);

    private final ItemStack stack;

    public AnglingSingleStackComponent(ItemStack stack) {
        this.stack = Objects.requireNonNull(stack, "stack").copy();
    }

    public ItemStack stack() {
        return stack.copy();
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof AnglingSingleStackComponent component
                && ItemStack.areEqual(stack, component.stack);
    }

    @Override
    public int hashCode() {
        return ItemStack.hashCode(stack);
    }
}
