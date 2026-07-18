package panetina.elarion.addons.portals.service;

import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;
import panetina.elarion.addons.portals.model.PortalArrival;
import panetina.elarion.addons.portals.model.PortalBounds;
import panetina.elarion.addons.portals.model.PortalEndpoint;
import panetina.elarion.addons.portals.model.PortalRouteDefinition;
import panetina.elarion.addons.portals.model.PortalRouteMode;
import panetina.elarion.addons.portals.model.PortalRouteState;
import panetina.elarion.addons.portals.model.PortalScheduleDefinition;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortalScheduleReconcilerTest {
    private static final Instant NOW = Instant.parse("2026-07-18T12:00:00Z");

    @Test
    void alwaysOpenRouteDoesNotRequireAnUnlock() {
        PortalRouteState state = completeState();

        assertTrue(PortalScheduleReconciler.isActive(
                definition(PortalRouteMode.ALWAYS_OPEN, inactiveSchedule()), state, NOW));
    }

    @Test
    void scheduledRouteRequiresUnlockAndAnActiveWindow() {
        PortalRouteState state = completeState();
        PortalRouteDefinition definition = definition(PortalRouteMode.SCHEDULED_TICKETED, activeSchedule());

        assertFalse(PortalScheduleReconciler.isActive(definition, state, NOW));
        state.unlocked = true;
        assertTrue(PortalScheduleReconciler.isActive(definition, state, NOW));
    }

    @Test
    void forcedStateOverridesTheConfiguredWindow() {
        PortalRouteState state = completeState();
        state.unlocked = true;
        PortalRouteDefinition definition = definition(PortalRouteMode.SCHEDULED_TICKETED, inactiveSchedule());

        state.forcedOpenUntil = NOW.plusSeconds(60).toEpochMilli();
        assertTrue(PortalScheduleReconciler.isActive(definition, state, NOW));

        state.forcedOpenUntil = null;
        state.forcedClosedUntil = NOW.plusSeconds(60).toEpochMilli();
        assertFalse(PortalScheduleReconciler.isActive(
                definition(PortalRouteMode.SCHEDULED_TICKETED, activeSchedule()), state, NOW));
    }

    private static PortalRouteState completeState() {
        PortalRouteState state = new PortalRouteState("route");
        PortalBounds bounds = PortalBounds.between(new BlockPos(0, 0, 0), new BlockPos(2, 3, 0));
        state.source = new PortalEndpoint("minecraft:overworld", bounds);
        state.returnEndpoint = new PortalEndpoint("minecraft:the_nether", bounds);
        state.outboundArrival = new PortalArrival("minecraft:the_nether", 0, 64, 0, 0, 0);
        state.returnArrival = new PortalArrival("minecraft:overworld", 0, 64, 0, 0, 0);
        return state;
    }

    private static PortalRouteDefinition definition(
            PortalRouteMode mode,
            PortalScheduleDefinition schedule
    ) {
        return new PortalRouteDefinition(
                "route", "Route", "", "minecraft:overworld", "minecraft:the_nether",
                true, mode, "ticket", "Ticket", "", "ticket", "passage", false,
                schedule, null);
    }

    private static PortalScheduleDefinition activeSchedule() {
        return new PortalScheduleDefinition(
                ZoneId.of("UTC"), NOW.minusSeconds(60), Duration.ofHours(1),
                Duration.ofMinutes(30), List.of());
    }

    private static PortalScheduleDefinition inactiveSchedule() {
        return new PortalScheduleDefinition(
                ZoneId.of("UTC"), NOW.minusSeconds(40 * 60), Duration.ofHours(1),
                Duration.ofMinutes(10), List.of());
    }
}
