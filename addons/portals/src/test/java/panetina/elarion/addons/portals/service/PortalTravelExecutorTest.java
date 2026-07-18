package panetina.elarion.addons.portals.service;

import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;
import panetina.elarion.addons.portals.model.PortalArrival;
import panetina.elarion.addons.portals.model.PortalBounds;
import panetina.elarion.addons.portals.model.PortalEndpoint;
import panetina.elarion.addons.portals.model.PortalFreePassageState;
import panetina.elarion.addons.portals.model.PortalReturnEntitlement;
import panetina.elarion.addons.portals.model.PortalRouteDefinition;
import panetina.elarion.addons.portals.model.PortalRouteMode;
import panetina.elarion.addons.portals.model.PortalRouteState;
import panetina.elarion.addons.portals.model.PortalScheduleDefinition;
import panetina.elarion.addons.portals.model.PortalTravelDirection;
import panetina.elarion.addons.portals.storage.PortalState;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortalTravelExecutorTest {
    private static final UUID PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000000123");
    private static final String ROUTE_ID = "route";

    @Test
    void failedTicketTeleportRestoresTicketAndPreviousEntitlement() {
        Fixture fixture = new Fixture();
        fixture.teleportSucceeds = false;
        PortalState state = new PortalState();
        PortalRouteState route = completeRoute(true);
        PortalReturnEntitlement previous = new PortalReturnEntitlement(PLAYER_ID, ROUTE_ID, 1L, 2L);
        state.entitlements.put(key(), previous);

        var result = fixture.executor().travel(
                state, "player", definition(PortalRouteMode.SCHEDULED_TICKETED, false), route,
                PortalTravelDirection.OUTBOUND);

        assertFalse(result.success());
        assertEquals(1, fixture.removedTickets);
        assertEquals(1, fixture.restoredTickets);
        assertSame(previous, state.entitlements.get(key()));
        assertEquals(2, fixture.saves);
        assertEquals(0, fixture.journeys);
        assertTrue(fixture.records.isEmpty());
    }

    @Test
    void successfulTicketRoundTripGrantsThenConsumesReturnEntitlement() {
        Fixture fixture = new Fixture();
        PortalState state = new PortalState();
        PortalRouteState route = completeRoute(true);
        PortalRouteDefinition definition = definition(PortalRouteMode.SCHEDULED_TICKETED, false);

        var outbound = fixture.executor().travel(
                state, "player", definition, route, PortalTravelDirection.OUTBOUND);

        assertTrue(outbound.success());
        assertEquals(1, fixture.removedTickets);
        assertTrue(state.entitlements.containsKey(key()));
        assertEquals(1, fixture.journeys);

        fixture.world = "minecraft:the_nether";
        var returned = fixture.executor().travel(
                state, "player", definition, route, PortalTravelDirection.RETURN);

        assertTrue(returned.success());
        assertFalse(state.entitlements.containsKey(key()));
        assertEquals(2, fixture.journeys);
        assertEquals(List.of("travel-outbound", "travel-return"), fixture.records);
    }

    @Test
    void failedPaidPassageRefundsWithoutProgressOrJourneyAccounting() {
        Fixture fixture = new Fixture();
        fixture.teleportSucceeds = false;
        fixture.price = 25L;
        PortalState state = new PortalState();

        var result = fixture.executor().travel(
                state, "player", definition(PortalRouteMode.FEE_PASSAGE, false), completeRoute(true),
                PortalTravelDirection.OUTBOUND);

        assertFalse(result.success());
        assertEquals(1, fixture.payments);
        assertEquals(1, fixture.refunds);
        assertNull(state.freePassages.get(key()));
        assertEquals(0, fixture.journeys);
        assertTrue(fixture.records.isEmpty());
    }

    @Test
    void successfulPaidPassageStoresReturnAndRecordsFeeBeforeJourney() {
        Fixture fixture = new Fixture();
        fixture.price = 25L;
        PortalState state = new PortalState();

        var result = fixture.executor().travel(
                state, "player", definition(PortalRouteMode.FEE_PASSAGE, false), completeRoute(true),
                PortalTravelDirection.OUTBOUND);

        assertTrue(result.success());
        assertEquals(PortalFreePassageState.RETURN_AVAILABLE, state.freePassages.get(key()));
        assertEquals(1, fixture.payments);
        assertEquals(0, fixture.refunds);
        assertEquals(List.of("passage-fee-paid", "travel-outbound"), fixture.records);
        assertEquals(1, fixture.journeys);
        assertEquals(1, fixture.saves);
    }

    @Test
    void paymentFailureDoesNotTeleportOrMutateState() {
        Fixture fixture = new Fixture();
        fixture.price = 25L;
        fixture.paymentSucceeds = false;
        PortalState state = new PortalState();

        var result = fixture.executor().travel(
                state, "player", definition(PortalRouteMode.FEE_PASSAGE, false), completeRoute(true),
                PortalTravelDirection.OUTBOUND);

        assertFalse(result.success());
        assertEquals("Not enough carried Sigils.", result.message());
        assertEquals(0, fixture.teleports);
        assertEquals(0, fixture.refunds);
        assertTrue(state.freePassages.isEmpty());
    }

    @Test
    void restrictionStopsAllTravelSideEffects() {
        Fixture fixture = new Fixture();
        fixture.restriction = "Portal travel is restricted.";

        var result = fixture.executor().travel(
                new PortalState(), "player", definition(PortalRouteMode.ALWAYS_OPEN, false),
                completeRoute(false), PortalTravelDirection.OUTBOUND);

        assertFalse(result.success());
        assertEquals(0, fixture.teleports);
        assertEquals(0, fixture.saves);
        assertEquals(0, fixture.journeys);
    }

    private static String key() {
        return PortalTravelExecutor.entitlementKey(PLAYER_ID, ROUTE_ID);
    }

    private static PortalRouteState completeRoute(boolean unlocked) {
        PortalRouteState route = new PortalRouteState(ROUTE_ID);
        PortalBounds bounds = PortalBounds.between(new BlockPos(0, 60, 0), new BlockPos(2, 64, 0));
        route.source = new PortalEndpoint("minecraft:overworld", bounds);
        route.returnEndpoint = new PortalEndpoint("minecraft:the_nether", bounds);
        route.outboundArrival = new PortalArrival("minecraft:the_nether", 1, 62, 0, 0, 0);
        route.returnArrival = new PortalArrival("minecraft:overworld", 1, 62, 0, 0, 0);
        route.unlocked = unlocked;
        return route;
    }

    private static PortalRouteDefinition definition(PortalRouteMode mode, boolean firstRoundTripFree) {
        return new PortalRouteDefinition(
                ROUTE_ID, "Route", "", "minecraft:overworld", "minecraft:the_nether",
                true, mode, "ticket", "Gate Ticket", "", "ticket", "passage",
                firstRoundTripFree, PortalScheduleDefinition.alwaysOpenSchedule(), null);
    }

    private static final class Fixture implements PortalTravelExecutor.Effects<String, String> {
        String restriction;
        String world = "minecraft:overworld";
        boolean destinationAvailable = true;
        boolean teleportSucceeds = true;
        boolean paymentSucceeds = true;
        int ticketSlot = 0;
        long price;
        int teleports;
        int removedTickets;
        int restoredTickets;
        int payments;
        int refunds;
        int saves;
        int journeys;
        final List<String> records = new ArrayList<>();

        PortalTravelExecutor<String, String> executor() {
            return new PortalTravelExecutor<>(this);
        }

        @Override
        public String restriction(String player) {
            return restriction;
        }

        @Override
        public UUID playerId(String player) {
            return PLAYER_ID;
        }

        @Override
        public String playerWorld(String player) {
            return world;
        }

        @Override
        public BlockPos playerPosition(String player) {
            return new BlockPos(1, 62, 0);
        }

        @Override
        public Instant now() {
            return Instant.parse("2026-07-18T12:00:00Z");
        }

        @Override
        public String destination(String worldId) {
            return destinationAvailable ? worldId : null;
        }

        @Override
        public boolean teleport(String player, String destination, PortalArrival arrival) {
            teleports++;
            return teleportSucceeds;
        }

        @Override
        public int ticketSlot(String player, String ticketId) {
            return ticketSlot;
        }

        @Override
        public void removeTicket(String player, int slot) {
            removedTickets++;
        }

        @Override
        public void restoreTicket(String player, PortalRouteDefinition definition) {
            restoredTickets++;
        }

        @Override
        public long servicePrice(String priceKey) {
            return price;
        }

        @Override
        public PortalTravelExecutor.PaymentAttempt pay(String player, long price) {
            payments++;
            return paymentSucceeds
                    ? PortalTravelExecutor.PaymentAttempt.success(() -> refunds++)
                    : PortalTravelExecutor.PaymentAttempt.failure("Not enough carried Sigils.");
        }

        @Override
        public void markDirty() {
        }

        @Override
        public void save() {
            saves++;
        }

        @Override
        public void clearOccupation(UUID playerId) {
        }

        @Override
        public void record(String type, UUID actor, String routeId, Map<String, String> details) {
            records.add(type);
        }

        @Override
        public void incrementJourney(UUID playerId) {
            journeys++;
        }
    }
}
