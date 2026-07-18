package panetina.elarion.addons.portals.service;

import net.minecraft.util.math.BlockPos;
import panetina.elarion.addons.portals.model.PortalArrival;
import panetina.elarion.addons.portals.model.PortalEndpoint;
import panetina.elarion.addons.portals.model.PortalFreePassageState;
import panetina.elarion.addons.portals.model.PortalReturnEntitlement;
import panetina.elarion.addons.portals.model.PortalRouteDefinition;
import panetina.elarion.addons.portals.model.PortalRouteState;
import panetina.elarion.addons.portals.model.PortalTravelDirection;
import panetina.elarion.addons.portals.storage.PortalState;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

final class PortalTravelExecutor<P, D> {
    private final Effects<P, D> effects;

    PortalTravelExecutor(Effects<P, D> effects) {
        this.effects = effects;
    }

    PortalRouteService.TravelResult travel(
            PortalState state,
            P player,
            PortalRouteDefinition definition,
            PortalRouteState route,
            PortalTravelDirection direction
    ) {
        String restriction = effects.restriction(player);
        if (restriction != null) {
            return PortalRouteService.TravelResult.fail(restriction.isBlank()
                    ? "You cannot use portals right now."
                    : restriction);
        }

        Instant now = effects.now();
        if (!PortalScheduleReconciler.isActive(definition, route, now)) {
            return PortalRouteService.TravelResult.fail("This gate is closed.");
        }
        PortalEndpoint expected = direction == PortalTravelDirection.OUTBOUND
                ? route.source : route.returnEndpoint;
        if (expected == null || !expected.worldId().equals(effects.playerWorld(player))
                || !expected.bounds().contains(effects.playerPosition(player))) {
            return PortalRouteService.TravelResult.fail("You are no longer inside the linked gate.");
        }
        PortalArrival arrival = direction == PortalTravelDirection.OUTBOUND
                ? route.outboundArrival : route.returnArrival;
        D destination = effects.destination(arrival.worldId());
        if (destination == null) {
            return PortalRouteService.TravelResult.fail("The destination world is unavailable.");
        }

        PortalRouteService.TravelResult result;
        if (definition.mode().chargesPassage()) {
            result = travelFeePassage(state, player, definition, direction, destination, arrival);
        } else if (!definition.mode().requiresTicket()) {
            result = effects.teleport(player, destination, arrival)
                    ? PortalRouteService.TravelResult.ok()
                    : PortalRouteService.TravelResult.fail("Travel failed.");
        } else if (direction == PortalTravelDirection.OUTBOUND) {
            result = travelTicketOutbound(state, player, definition, route, destination, arrival, now);
        } else {
            result = travelTicketReturn(state, player, definition, destination, arrival);
        }
        if (!result.success()) return result;

        effects.save();
        UUID playerId = effects.playerId(player);
        effects.clearOccupation(playerId);
        effects.record(
                direction == PortalTravelDirection.OUTBOUND ? "travel-outbound" : "travel-return",
                playerId,
                definition.id(),
                Map.of());
        effects.incrementJourney(playerId);
        return PortalRouteService.TravelResult.ok();
    }

    private PortalRouteService.TravelResult travelTicketOutbound(
            PortalState state,
            P player,
            PortalRouteDefinition definition,
            PortalRouteState route,
            D destination,
            PortalArrival arrival,
            Instant now
    ) {
        int slot = effects.ticketSlot(player, definition.ticketId());
        if (slot < 0) {
            return PortalRouteService.TravelResult.fail("You need a " + definition.ticketName() + ".");
        }
        UUID playerId = effects.playerId(player);
        String key = entitlementKey(playerId, definition.id());
        PortalReturnEntitlement previous = state.entitlements.get(key);
        effects.removeTicket(player, slot);
        state.entitlements.put(key, new PortalReturnEntitlement(
                playerId,
                definition.id(),
                now.toEpochMilli(),
                PortalScheduleReconciler.window(definition, route, now).start().toEpochMilli()));
        effects.markDirty();
        effects.save();

        if (effects.teleport(player, destination, arrival)) {
            return PortalRouteService.TravelResult.ok();
        }

        effects.restoreTicket(player, definition);
        if (previous == null) state.entitlements.remove(key);
        else state.entitlements.put(key, previous);
        effects.markDirty();
        effects.save();
        return PortalRouteService.TravelResult.fail("Travel failed; your ticket was restored.");
    }

    private PortalRouteService.TravelResult travelTicketReturn(
            PortalState state,
            P player,
            PortalRouteDefinition definition,
            D destination,
            PortalArrival arrival
    ) {
        UUID playerId = effects.playerId(player);
        String key = entitlementKey(playerId, definition.id());
        if (!state.entitlements.containsKey(key)) {
            return PortalRouteService.TravelResult.fail(
                    "You do not have a return passage for this route.");
        }
        if (!effects.teleport(player, destination, arrival)) {
            return PortalRouteService.TravelResult.fail("Return travel failed.");
        }
        state.entitlements.remove(key);
        effects.markDirty();
        return PortalRouteService.TravelResult.ok();
    }

    private PortalRouteService.TravelResult travelFeePassage(
            PortalState state,
            P player,
            PortalRouteDefinition definition,
            PortalTravelDirection direction,
            D destination,
            PortalArrival arrival
    ) {
        UUID playerId = effects.playerId(player);
        String key = entitlementKey(playerId, definition.id());
        PortalFreePassageState freeState = state.freePassages.get(key);
        boolean free = PortalFreePassagePolicy.isFree(
                definition.firstRoundTripFree(), freeState, direction);
        boolean storedReturnPassage = direction == PortalTravelDirection.RETURN
                && freeState == PortalFreePassageState.RETURN_AVAILABLE;
        if (direction == PortalTravelDirection.RETURN && !storedReturnPassage) {
            return PortalRouteService.TravelResult.fail(
                    "You do not have a stored return passage for this route.");
        }

        long price = free || storedReturnPassage
                ? 0L
                : effects.servicePrice(definition.passagePriceKey());
        PaymentAttempt payment = price > 0
                ? effects.pay(player, price)
                : PaymentAttempt.success(() -> {});
        if (!payment.successful()) {
            return PortalRouteService.TravelResult.fail(payment.message());
        }
        if (!effects.teleport(player, destination, arrival)) {
            payment.refund().run();
            return PortalRouteService.TravelResult.fail(
                    "Travel failed" + (price > 0 ? "; your payment was refunded." : "."));
        }

        if (free || direction == PortalTravelDirection.OUTBOUND || storedReturnPassage) {
            state.freePassages.put(key,
                    PortalFreePassagePolicy.afterSuccessfulTravel(freeState, direction));
            effects.markDirty();
        }
        if (price > 0) {
            effects.record(
                    "passage-fee-paid",
                    playerId,
                    definition.id(),
                    Map.of(
                            "price", Long.toString(price),
                            "direction", direction.name().toLowerCase()));
        }
        return PortalRouteService.TravelResult.ok();
    }

    static String entitlementKey(UUID playerId, String routeId) {
        return playerId + "|" + routeId;
    }

    record PaymentAttempt(boolean successful, String message, Runnable refund) {
        PaymentAttempt {
            message = message == null ? "" : message;
            refund = refund == null ? () -> {} : refund;
        }

        static PaymentAttempt success(Runnable refund) {
            return new PaymentAttempt(true, "Payment completed.", refund);
        }

        static PaymentAttempt failure(String message) {
            return new PaymentAttempt(false, message, () -> {});
        }
    }

    interface Effects<P, D> {
        String restriction(P player);

        UUID playerId(P player);

        String playerWorld(P player);

        BlockPos playerPosition(P player);

        Instant now();

        D destination(String worldId);

        boolean teleport(P player, D destination, PortalArrival arrival);

        int ticketSlot(P player, String ticketId);

        void removeTicket(P player, int slot);

        void restoreTicket(P player, PortalRouteDefinition definition);

        long servicePrice(String priceKey);

        PaymentAttempt pay(P player, long price);

        void markDirty();

        void save();

        void clearOccupation(UUID playerId);

        void record(String type, UUID actor, String routeId, Map<String, String> details);

        void incrementJourney(UUID playerId);
    }
}
