package panetina.elarion.addons.portals.service;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import panetina.elarion.addons.economy.api.ElarionEconomyApi;
import panetina.elarion.addons.portals.model.PortalArrival;
import panetina.elarion.addons.portals.model.PortalFreePassageState;
import panetina.elarion.addons.portals.model.PortalRouteDefinition;
import panetina.elarion.addons.portals.model.PortalRouteState;
import panetina.elarion.addons.portals.model.PortalScheduleDefinition;
import panetina.elarion.addons.portals.model.PortalTravelDirection;
import panetina.elarion.addons.portals.network.PortalTravelPromptPayload;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.service.PlayerRestrictionService;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

final class PortalPlayerPromptDetector {
    private final ElarionApi api;
    private final PortalDefinitionService definitions;
    private final PortalEndpointIndex endpointIndex;
    private final Effects effects;
    private final Map<UUID, Set<String>> occupiedEndpoints = new HashMap<>();

    PortalPlayerPromptDetector(
            ElarionApi api,
            PortalDefinitionService definitions,
            PortalEndpointIndex endpointIndex,
            Effects effects
    ) {
        this.api = api;
        this.definitions = definitions;
        this.endpointIndex = endpointIndex;
        this.effects = effects;
    }

    void tick(Collection<ServerPlayerEntity> players) {
        Instant now = Instant.now();
        for (ServerPlayerEntity player : players) {
            String world = player.getWorld().getRegistryKey().getValue().toString();
            effects.rememberPosition(player.getUuid(), new PortalArrival(
                    world, player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch()));
            BlockPos pos = player.getBlockPos();
            Set<String> current = new HashSet<>();
            Set<String> previous = occupiedEndpoints.getOrDefault(player.getUuid(), Set.of());
            for (PortalEndpointIndex.Entry indexed : endpointIndex.nearby(world, pos)) {
                PortalRouteDefinition definition = definitions.require(indexed.routeId());
                PortalRouteState route = effects.route(indexed.routeId());
                if (!PortalScheduleReconciler.isActive(definition, route, now)) continue;
                if (!indexed.endpoint().bounds().contains(pos)) continue;
                PortalTravelDirection direction = indexed.direction();
                String key = definition.id() + ":" + direction;
                current.add(key);
                if (!previous.contains(key)) {
                    effects.sendPrompt(prompt(player, definition, route, direction, now));
                }
            }
            if (current.isEmpty()) occupiedEndpoints.remove(player.getUuid());
            else occupiedEndpoints.put(player.getUuid(), current);
        }
    }

    void clearOccupation(UUID playerId) {
        occupiedEndpoints.remove(playerId);
    }

    private PortalRouteService.TravelPrompt prompt(
            ServerPlayerEntity player,
            PortalRouteDefinition definition,
            PortalRouteState route,
            PortalTravelDirection direction,
            Instant now
    ) {
        Optional<PlayerRestrictionService.PlayerRestriction> restriction =
                api.system().restrictions().restriction(player, PlayerRestrictionService.PORTAL_TRAVEL);
        boolean restricted = restriction.isPresent();
        String restrictionMessage = restriction
                .map(PlayerRestrictionService.PlayerRestriction::message)
                .filter(message -> !message.isBlank())
                .orElse("You cannot use portals right now.");
        PortalScheduleDefinition.Window window = PortalScheduleReconciler.window(definition, route, now);
        boolean entitled = effects.hasEntitlement(player.getUuid(), definition.id());
        boolean ticketed = definition.mode().requiresTicket();
        boolean feePassage = definition.mode().chargesPassage();
        boolean storedReturnPassage = feePassage && direction == PortalTravelDirection.RETURN
                && effects.freePassageState(player.getUuid(), definition.id())
                == PortalFreePassageState.RETURN_AVAILABLE;
        boolean freePassage = feePassage && (effects.isFreePassage(player.getUuid(), definition, direction)
                || storedReturnPassage);
        boolean feeReturnWithoutStoredPassage = feePassage && direction == PortalTravelDirection.RETURN
                && !storedReturnPassage;
        ElarionEconomyApi economy = ElarionEconomyApi.get();
        long passagePrice = feePassage ? economy.servicePrice(definition.passagePriceKey()) : 0L;
        boolean hasTicket = !ticketed || direction != PortalTravelDirection.OUTBOUND
                || effects.hasTicket(player, definition.ticketId());
        boolean canPay = !feeReturnWithoutStoredPassage && (!feePassage || freePassage
                || economy.physicalCurrency(player) >= passagePrice);
        String requirement = restricted
                ? restrictionMessage
                : ticketed && direction == PortalTravelDirection.OUTBOUND
                ? "You need a " + definition.ticketName() + "."
                : ticketed
                ? "Uses your stored return passage."
                : feeReturnWithoutStoredPassage
                ? "You do not have a stored return passage."
                : storedReturnPassage
                ? "Your return passage is already paid."
                : feePassage && freePassage
                ? "Your first round trip is free."
                : feePassage
                ? "You need " + api.serverIdentity().currencyAmount(passagePrice) + "."
                : "No ticket or fee required.";
        String message = restricted ? restrictionMessage
                : !hasTicket ? "The required ticket is not in your inventory."
                : feeReturnWithoutStoredPassage
                ? "Enter from the Realm side first to store a return passage."
                : !canPay ? "You do not have enough carried physical "
                + api.serverIdentity().currencyPlural() + "."
                : ticketed && direction == PortalTravelDirection.RETURN && !entitled
                ? "You do not have a return passage for this route."
                : storedReturnPassage
                ? "This return completes your current round trip."
                : feePassage && freePassage
                ? "Later passages cost " + api.serverIdentity().currencyAmount(passagePrice) + "."
                : feePassage
                ? "Withdraw banked " + api.serverIdentity().currencyPlural()
                + " before using this gate." : "";
        String costKind = ticketed && direction == PortalTravelDirection.OUTBOUND
                ? PortalTravelPromptPayload.COST_TICKET
                : feePassage && !freePassage && !storedReturnPassage
                ? PortalTravelPromptPayload.COST_FEE
                : PortalTravelPromptPayload.COST_FREE;
        return new PortalRouteService.TravelPrompt(
                player,
                definition.id(),
                effects.displayName(definition),
                effects.description(definition),
                direction,
                definition.mode().usesSchedule() ? window.end().toEpochMilli() : 0L,
                ticketed && direction == PortalTravelDirection.OUTBOUND,
                definition.ticketName(),
                definition.visual().iconItem(),
                costKind,
                requirement,
                definition.visual().promptAccentColor(),
                !restricted && hasTicket && canPay
                        && (!ticketed || direction == PortalTravelDirection.OUTBOUND || entitled),
                message);
    }

    interface Effects {
        PortalRouteState route(String routeId);

        void rememberPosition(UUID playerId, PortalArrival arrival);

        boolean hasEntitlement(UUID playerId, String routeId);

        PortalFreePassageState freePassageState(UUID playerId, String routeId);

        boolean isFreePassage(
                UUID playerId,
                PortalRouteDefinition definition,
                PortalTravelDirection direction
        );

        boolean hasTicket(ServerPlayerEntity player, String ticketId);

        String displayName(PortalRouteDefinition definition);

        String description(PortalRouteDefinition definition);

        void sendPrompt(PortalRouteService.TravelPrompt prompt);
    }
}
