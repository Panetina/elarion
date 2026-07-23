package panetina.elarion.addons.angling.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Objects;

/** Bounded portable contents for tackle-box fish storage. */
public final class AnglingTackleBoxComponent {
    public static final int MAX_FISH_STACKS = 900;
    public static final Codec<AnglingTackleBoxComponent> CODEC = ItemStack.OPTIONAL_CODEC.listOf()
            .validate(values -> values.size() <= MAX_FISH_STACKS
                    ? DataResult.success(values)
                    : DataResult.error(() -> "tackle box exceeds " + MAX_FISH_STACKS + " stored stacks"))
            .xmap(AnglingTackleBoxComponent::new, AnglingTackleBoxComponent::stacks);
    public static final AnglingTackleBoxComponent EMPTY = new AnglingTackleBoxComponent(List.of());

    private final List<ItemStack> stacks;

    public AnglingTackleBoxComponent(List<ItemStack> stacks) {
        Objects.requireNonNull(stacks, "stacks");
        if (stacks.size() > MAX_FISH_STACKS || stacks.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Invalid tackle-box contents");
        }
        this.stacks = stacks.stream().map(ItemStack::copy).toList();
    }

    public List<ItemStack> stacks() {
        return stacks.stream().map(ItemStack::copy).toList();
    }

    public int size() {
        return stacks.size();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof AnglingTackleBoxComponent component) || stacks.size() != component.stacks.size()) {
            return false;
        }
        for (int index = 0; index < stacks.size(); index++) {
            if (!ItemStack.areEqual(stacks.get(index), component.stacks.get(index))) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 1;
        for (ItemStack stack : stacks) result = 31 * result + ItemStack.hashCode(stack);
        return result;
    }
}
