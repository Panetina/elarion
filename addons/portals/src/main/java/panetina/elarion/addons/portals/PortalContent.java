package panetina.elarion.addons.portals;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import panetina.elarion.addons.economy.EconomyItems;

public final class PortalContent {
    public static final Identifier FIELD_ID = Identifier.of("elarion", "portal_field");
    public static final Identifier SURVEYOR_ID = Identifier.of("elarion", "portal_surveyor");
    public static final Identifier TICKET_ID = Identifier.of("elarion", "portal_ticket");

    public static final PortalFieldBlock FIELD = new PortalFieldBlock(AbstractBlock.Settings.create()
            .noCollision()
            .nonOpaque()
            .dropsNothing()
            .sounds(BlockSoundGroup.GLASS)
            .luminance(state -> 11)
            .strength(-1.0F, 3_600_000.0F));
    public static final PortalSurveyorItem SURVEYOR = new PortalSurveyorItem(new Item.Settings().maxCount(1));
    public static final PortalTicketItem TICKET = new PortalTicketItem(new Item.Settings().maxCount(64));
    private static volatile java.util.List<panetina.elarion.addons.portals.model.PortalRouteDefinition>
            ticketDefinitions = java.util.List.of();
    private static boolean registered;

    private PortalContent() {
    }

    public static synchronized void register() {
        if (registered) return;
        Registry.register(Registries.BLOCK, FIELD_ID, FIELD);
        Registry.register(Registries.ITEM, SURVEYOR_ID, SURVEYOR);
        Registry.register(Registries.ITEM, TICKET_ID, TICKET);
        ItemGroupEvents.modifyEntriesEvent(EconomyItems.ITEM_GROUP_KEY).register(entries -> {
            if (ticketDefinitions.isEmpty()) {
                entries.add(TICKET);
            } else {
                ticketDefinitions.forEach(definition -> entries.add(TICKET.create(
                        definition.ticketId(), definition.ticketName(), definition.ticketLore())));
            }
        });
        registered = true;
    }

    public static void configureTickets(
            java.util.Collection<panetina.elarion.addons.portals.model.PortalRouteDefinition> definitions
    ) {
        ticketDefinitions = definitions.stream()
                .filter(panetina.elarion.addons.portals.model.PortalRouteDefinition::enabled)
                .filter(definition -> definition.mode().requiresTicket())
                .toList();
    }
}
