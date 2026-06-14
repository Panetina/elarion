package panetina.elarion.addons.portals;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

public final class PortalTicketItem extends Item {
    private static final String TICKET_KEY = "ElarionPortalTicket";

    public PortalTicketItem(Settings settings) {
        super(settings);
    }

    public ItemStack create(String ticketId, String displayName, String lore) {
        ItemStack stack = new ItemStack(this);
        NbtCompound nbt = new NbtCompound();
        nbt.putString(TICKET_KEY, ticketId);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(displayName));
        if (lore != null && !lore.isBlank()) {
            stack.set(DataComponentTypes.LORE, new LoreComponent(java.util.List.of(Text.literal(lore))));
        }
        return stack;
    }

    public String ticketId(ItemStack stack) {
        NbtComponent data = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        return data.copyNbt().getString(TICKET_KEY);
    }

    public boolean matches(ItemStack stack, String ticketId) {
        return stack.isOf(this) && ticketId.equals(ticketId(stack));
    }
}
