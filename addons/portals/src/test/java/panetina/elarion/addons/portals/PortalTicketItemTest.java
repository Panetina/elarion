package panetina.elarion.addons.portals;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PortalTicketItemTest {
    @Test
    void ticketIdsCarryDimensionModelData() {
        assertEquals(1, PortalTicketItem.modelData("nether"));
        assertEquals(2, PortalTicketItem.modelData("end"));
        assertEquals(0, PortalTicketItem.modelData("ancient"));
    }
}
