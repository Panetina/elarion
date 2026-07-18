package panetina.elarion.addons.portals.service;

import org.slf4j.Logger;
import panetina.elarion.addons.portals.model.PortalRouteDefinition;
import panetina.elarion.addons.portals.model.PortalRouteSnapshot;
import panetina.elarion.addons.portals.model.PortalRouteState;
import panetina.elarion.addons.portals.model.PortalRouteStateEvent;
import panetina.elarion.addons.portals.model.PortalScheduleDefinition;
import panetina.elarion.addons.portals.storage.PortalState;
import panetina.elarion.core.service.ElarionPerformanceMonitor;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final class PortalScheduleReconciler {
    private final Logger logger;
    private final PortalDefinitionService definitions;
    private final PortalFieldController fields;
    private final Effects effects;
    private final Set<String> obstructedRoutes = new HashSet<>();
    private final Map<String, Boolean> publishedActivity = new HashMap<>();

    PortalScheduleReconciler(
            Logger logger,
            PortalDefinitionService definitions,
            PortalFieldController fields,
            Effects effects
    ) {
        this.logger = logger;
        this.definitions = definitions;
        this.fields = fields;
        this.effects = effects;
    }

    void tick(PortalState state) {
        Instant now = Instant.now();
        boolean syncNeeded = false;
        for (PortalRouteDefinition definition : definitions.all()) {
            PortalRouteState route = route(state, definition.id());
            PortalScheduleDefinition.Window window = window(definition, route, now);
            boolean shouldBeActive = isActive(definition, route, now);
            boolean hasFields = fields.isActive(definition.id());
            try {
                if (shouldBeActive && !hasFields) {
                    fields.activate(definition.id(), route);
                    obstructedRoutes.remove(definition.id());
                    emitStateChange(definition, PortalRouteStateEvent.Type.OPENED, now);
                    syncNeeded = true;
                    if (definition.mode().usesSchedule()
                            && route.lastOpenedWindow != window.start().toEpochMilli()) {
                        route.lastOpenedWindow = window.start().toEpochMilli();
                        effects.record("window-opened", definition.id());
                        effects.markDirty();
                    }
                } else if (!shouldBeActive && hasFields) {
                    fields.deactivate(definition.id(), route);
                    emitStateChange(definition, PortalRouteStateEvent.Type.CLOSED, now);
                    syncNeeded = true;
                    if (definition.mode().usesSchedule()
                            && route.lastClosedWindow != window.start().toEpochMilli()) {
                        route.lastClosedWindow = window.start().toEpochMilli();
                        effects.record("window-closed", definition.id());
                        effects.markDirty();
                    }
                }
            } catch (RuntimeException exception) {
                reportTransitionFailure(definition, exception);
            }
        }
        if (syncNeeded) effects.syncVisuals();
    }

    void reconcileAll(PortalState state) {
        definitions.all().forEach(definition -> reconcile(state, definition.id()));
    }

    void reconcile(PortalState state, String routeId) {
        PortalRouteDefinition definition = definitions.require(routeId);
        PortalRouteState route = route(state, routeId);
        Instant now = Instant.now();
        try {
            if (isActive(definition, route, now)) {
                fields.activate(routeId, route);
                obstructedRoutes.remove(routeId);
                emitStateChange(definition, PortalRouteStateEvent.Type.OPENED, now);
            } else {
                fields.deactivate(routeId, route);
                emitStateChange(definition, PortalRouteStateEvent.Type.CLOSED, now);
            }
            effects.syncVisuals();
        } catch (RuntimeException exception) {
            reportTransitionFailure(definition, exception);
        }
    }

    void emitStateChange(
            PortalRouteDefinition definition,
            PortalRouteStateEvent.Type type,
            Instant occurredAt
    ) {
        boolean active = type == PortalRouteStateEvent.Type.OPENED;
        Boolean previous = publishedActivity.put(definition.id(), active);
        if ((previous == null && !active) || java.util.Objects.equals(previous, active)) return;

        effects.publish(new PortalRouteStateEvent(
                definition.id(),
                effects.displayName(definition),
                type,
                occurredAt,
                effects.snapshot(definition.id(), occurredAt)));
        if (definition.mode().usesSchedule()) {
            effects.emitDomainEvent(
                    active ? "portal-window-opened" : "portal-window-closed",
                    definition,
                    Map.of(
                            "active", Boolean.toString(active),
                            "occurredAt", Long.toString(occurredAt.toEpochMilli())));
        }
    }

    static boolean isActive(PortalRouteDefinition definition, PortalRouteState route, Instant now) {
        if (!definition.enabled() || !route.complete()) return false;
        if (!definition.mode().requiresUnlock()) return true;
        return route.unlocked && window(definition, route, now).active();
    }

    static PortalScheduleDefinition.Window window(
            PortalRouteDefinition definition, PortalRouteState route, Instant now
    ) {
        if (route.forcedOpenUntil != null && route.forcedOpenUntil > now.toEpochMilli()) {
            return new PortalScheduleDefinition.Window(now, Instant.ofEpochMilli(route.forcedOpenUntil), true);
        }
        if (route.forcedClosedUntil != null && route.forcedClosedUntil > now.toEpochMilli()) {
            PortalScheduleDefinition.Window scheduled = definition.schedule().windowAt(now);
            return new PortalScheduleDefinition.Window(scheduled.start(), scheduled.end(), false);
        }
        return definition.schedule().windowAt(now);
    }

    private void reportTransitionFailure(PortalRouteDefinition definition, RuntimeException exception) {
        if (!obstructedRoutes.add(definition.id())) return;
        ElarionPerformanceMonitor.record("portal-field-obstructed", 0L);
        String message = exception.getMessage();
        if (message != null && message.startsWith("Unknown or unloaded world ")) {
            logger.warn("Portal route {} is inactive because a linked world is unavailable: {}",
                    definition.id(), message.substring("Unknown or unloaded world ".length()));
            return;
        }
        logger.error("Portal route {} could not transition: {}", definition.id(), message);
        effects.announce(definition.displayName()
                + " could not open because its portal interior is obstructed.");
    }

    private static PortalRouteState route(PortalState state, String routeId) {
        return state.routes.computeIfAbsent(routeId, PortalRouteState::new);
    }

    interface Effects {
        void markDirty();

        void syncVisuals();

        void record(String type, String routeId);

        void announce(String message);

        String displayName(PortalRouteDefinition definition);

        PortalRouteSnapshot snapshot(String routeId, Instant occurredAt);

        void publish(PortalRouteStateEvent event);

        void emitDomainEvent(
                String eventType,
                PortalRouteDefinition definition,
                Map<String, String> metadata
        );
    }
}
