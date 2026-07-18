package panetina.elarion.addons.portals.service;

import net.minecraft.server.network.ServerPlayerEntity;
import panetina.elarion.addons.portals.model.PortalArrival;
import panetina.elarion.addons.portals.model.PortalArrivalRole;
import panetina.elarion.addons.portals.model.PortalBounds;
import panetina.elarion.addons.portals.model.PortalEndpoint;
import panetina.elarion.addons.portals.model.PortalEndpointRole;
import panetina.elarion.addons.portals.model.PortalRouteDefinition;
import panetina.elarion.addons.portals.model.PortalRouteState;
import panetina.elarion.addons.portals.model.PortalRouteStateEvent;
import panetina.elarion.addons.portals.model.PortalScheduleDefinition;
import panetina.elarion.addons.portals.model.PortalSelection;
import panetina.elarion.addons.portals.storage.PortalState;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.model.ElarionNotificationAction;
import panetina.elarion.core.model.ElarionNotificationCategory;
import panetina.elarion.core.service.ElarionNotificationService;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

final class PortalRouteAdminMutator {
    private final ElarionApi api;
    private final PortalDefinitionService definitions;
    private final PortalFieldController fields;
    private final Effects effects;

    PortalRouteAdminMutator(
            ElarionApi api,
            PortalDefinitionService definitions,
            PortalFieldController fields,
            Effects effects
    ) {
        this.api = api;
        this.definitions = definitions;
        this.fields = fields;
        this.effects = effects;
    }

    void setEndpoint(
            PortalState state,
            String routeId,
            PortalEndpointRole role,
            PortalSelection selection,
            ServerPlayerEntity actor
    ) {
        PortalRouteState route = route(state, routeId);
        PortalRouteDefinition definition = definitions.require(routeId);
        if (role == PortalEndpointRole.SOURCE
                && !unrestrictedSource(definition)
                && !definition.sourceDimension().equals(selection.worldId())) {
            throw new IllegalArgumentException("a_gate must be inside "
                    + definition.sourceDimension() + ".");
        }
        if (role == PortalEndpointRole.RETURN
                && !unrestrictedDestination(definition)
                && !definition.destinationDimension().equals(selection.worldId())) {
            throw new IllegalArgumentException("b_gate must be inside "
                    + definition.destinationDimension() + ".");
        }
        PortalBounds bounds = PortalBounds.between(selection.first(), selection.second());
        PortalEndpoint endpoint = new PortalEndpoint(selection.worldId(), bounds);
        ensureNoOverlap(state, routeId, role, endpoint);
        fields.validateInterior(endpoint, false);
        if (role == PortalEndpointRole.SOURCE) route.source = endpoint;
        else route.returnEndpoint = endpoint;
        effects.persist(true);
        if (route.complete()) effects.reconcile(routeId);
        effects.syncVisuals();
        effects.record("endpoint-set", actor.getUuid(), routeId,
                Map.of("role", role.name().toLowerCase()));
    }

    void setArrival(
            PortalState state,
            String routeId,
            PortalArrivalRole role,
            ServerPlayerEntity actor
    ) {
        PortalRouteDefinition definition = definitions.require(routeId);
        String actorWorld = actor.getWorld().getRegistryKey().getValue().toString();
        if (role == PortalArrivalRole.OUTBOUND && !unrestrictedDestination(definition)
                && !definition.destinationDimension().equals(actorWorld)) {
            throw new IllegalArgumentException("b_arrival must be inside "
                    + definition.destinationDimension() + ".");
        }
        PortalRouteState route = route(state, routeId);
        if (role == PortalArrivalRole.RETURN && route.source != null
                && !unrestrictedSource(definition)
                && !definition.sourceDimension().equals(actorWorld)) {
            throw new IllegalArgumentException("a_arrival must be in the a_gate world.");
        }
        PortalArrival arrival = new PortalArrival(
                actorWorld,
                actor.getX(), actor.getY(), actor.getZ(), actor.getYaw(), actor.getPitch());
        if (role == PortalArrivalRole.OUTBOUND) route.outboundArrival = arrival;
        else route.returnArrival = arrival;
        effects.persist(false);
        if (route.complete()) effects.reconcile(routeId);
        effects.record("arrival-set", actor.getUuid(), routeId,
                Map.of("role", role.name().toLowerCase()));
    }

    void remove(PortalState state, String routeId, UUID actor) {
        PortalRouteDefinition definition = definitions.require(routeId);
        PortalRouteState route = route(state, routeId);
        fields.deactivate(routeId, route);
        effects.emitStateChange(definition, PortalRouteStateEvent.Type.CLOSED, Instant.now());
        route.source = null;
        route.returnEndpoint = null;
        route.outboundArrival = null;
        route.returnArrival = null;
        route.unlocked = false;
        route.forcedOpenUntil = null;
        route.forcedClosedUntil = null;
        state.entitlements.entrySet().removeIf(entry -> entry.getValue().routeId().equals(routeId));
        state.freePassages.keySet().removeIf(key -> key.endsWith("|" + routeId));
        effects.persist(true);
        effects.syncVisuals();
        effects.record("route-removed", actor, routeId, Map.of());
    }

    void unlock(PortalState state, String routeId, UUID actor) {
        PortalRouteDefinition definition = definitions.require(routeId);
        if (!definition.mode().requiresUnlock()) {
            throw new IllegalArgumentException(
                    definition.displayName() + " is always open and does not use locks.");
        }
        if (!definition.enabled()) {
            throw new IllegalArgumentException("Portal route " + routeId + " is disabled in routes.yml.");
        }
        PortalRouteState route = route(state, routeId);
        if (!route.complete()) {
            throw new IllegalArgumentException("Portal route " + routeId + " is not fully linked.");
        }
        if (route.unlocked) return;
        route.unlocked = true;
        effects.persist(false);

        List<ElarionNotificationAction> actions = List.of(new ElarionNotificationAction(
                ElarionNotificationService.DISMISS, "Dismiss", true));
        String body = effects.description(definition).isBlank()
                ? "A new route has opened."
                : effects.description(definition);
        String displayName = effects.displayName(definition);
        if (definition.mode().chargesPassage()) {
            api.notifications().publishRealm(
                    routeId,
                    ElarionNotificationCategory.REALM,
                    "elarion_portals",
                    "ancient-gate-unlocked",
                    "portal-unlocked:" + routeId,
                    displayName + " Unlocked",
                    body,
                    "Realm Gate",
                    "item:minecraft:ender_eye",
                    actions,
                    Map.of("routeId", routeId),
                    api.notifications().defaultExpiry());
        } else {
            api.notifications().publishWorld(
                    "elarion_portals",
                    "route-unlocked",
                    "portal-unlocked:" + routeId,
                    displayName + " Unlocked",
                    body,
                    "World Gate",
                    "item:minecraft:ender_eye",
                    actions,
                    Map.of("routeId", routeId),
                    api.notifications().defaultExpiry());
        }
        effects.emitDomainEvent("portal-route-unlocked", definition, actor,
                Map.of("unlocked", "true"));
        effects.record("route-unlocked", actor, routeId, Map.of());
        effects.reconcile(routeId);
    }

    void lock(PortalState state, String routeId, UUID actor) {
        PortalRouteDefinition definition = definitions.require(routeId);
        if (!definition.mode().requiresUnlock()) {
            throw new IllegalArgumentException(
                    definition.displayName() + " is always open and cannot be locked.");
        }
        PortalRouteState route = route(state, routeId);
        if (!route.unlocked) return;
        route.unlocked = false;
        route.forcedOpenUntil = null;
        route.forcedClosedUntil = null;
        fields.deactivate(routeId, route);
        effects.emitStateChange(definition, PortalRouteStateEvent.Type.CLOSED, Instant.now());
        effects.persist(false);
        effects.syncVisuals();
        api.notifications().resolveByMetadata("elarion_portals", "routeId", routeId);
        effects.emitDomainEvent("portal-route-locked", definition, actor,
                Map.of("unlocked", "false"));
        effects.record("route-locked", actor, routeId, Map.of());
    }

    void forceOpen(PortalState state, String routeId, Duration duration, UUID actor) {
        PortalRouteDefinition definition = definitions.require(routeId);
        if (!definition.mode().usesSchedule()) {
            throw new IllegalArgumentException(
                    definition.displayName() + " is always open and has no schedule window.");
        }
        PortalRouteState route = route(state, routeId);
        if (!route.unlocked || !route.complete()) {
            throw new IllegalArgumentException("Portal route must be unlocked and fully linked.");
        }
        route.forcedOpenUntil = Instant.now().plus(duration).toEpochMilli();
        route.forcedClosedUntil = null;
        effects.persist(false);
        effects.reconcile(routeId);
        effects.record("window-force-opened", actor, routeId,
                Map.of("durationSeconds", Long.toString(duration.toSeconds())));
    }

    void forceClose(PortalState state, String routeId, UUID actor) {
        PortalRouteDefinition definition = definitions.require(routeId);
        if (!definition.mode().usesSchedule()) {
            throw new IllegalArgumentException(
                    definition.displayName() + " is always open and has no schedule window.");
        }
        PortalRouteState route = route(state, routeId);
        PortalScheduleDefinition.Window scheduled = definition.schedule().windowAt(Instant.now());
        route.forcedOpenUntil = null;
        route.forcedClosedUntil = scheduled.active()
                ? scheduled.end().toEpochMilli()
                : scheduled.start().toEpochMilli();
        fields.deactivate(routeId, route);
        effects.emitStateChange(definition, PortalRouteStateEvent.Type.CLOSED, Instant.now());
        effects.persist(false);
        effects.syncVisuals();
        effects.record("window-force-closed", actor, routeId, Map.of());
    }

    static void ensureNoOverlap(
            PortalState state,
            String routeId,
            PortalEndpointRole role,
            PortalEndpoint endpoint
    ) {
        for (Map.Entry<String, PortalRouteState> entry : state.routes.entrySet()) {
            for (PortalEndpoint existing : PortalRouteService.endpoints(entry.getValue())) {
                if (!existing.worldId().equals(endpoint.worldId())) continue;
                boolean replacingSame = entry.getKey().equals(routeId)
                        && (role == PortalEndpointRole.SOURCE && existing == entry.getValue().source
                        || role == PortalEndpointRole.RETURN && existing == entry.getValue().returnEndpoint);
                if (!replacingSame && existing.bounds().intersects(endpoint.bounds())) {
                    throw new IllegalArgumentException("Portal region overlaps route " + entry.getKey() + ".");
                }
            }
        }
    }

    private PortalRouteState route(PortalState state, String routeId) {
        definitions.require(routeId);
        return state.routes.computeIfAbsent(routeId, PortalRouteState::new);
    }

    private static boolean unrestrictedDestination(PortalRouteDefinition definition) {
        return "*".equals(definition.destinationDimension());
    }

    private static boolean unrestrictedSource(PortalRouteDefinition definition) {
        return "*".equals(definition.sourceDimension());
    }

    interface Effects {
        void persist(boolean rebuildIndex);

        void reconcile(String routeId);

        void syncVisuals();

        void emitStateChange(
                PortalRouteDefinition definition,
                PortalRouteStateEvent.Type type,
                Instant occurredAt
        );

        void emitDomainEvent(
                String eventType,
                PortalRouteDefinition definition,
                UUID actor,
                Map<String, String> metadata
        );

        void record(String type, UUID actor, String routeId, Map<String, String> details);

        String displayName(PortalRouteDefinition definition);

        String description(PortalRouteDefinition definition);
    }
}
