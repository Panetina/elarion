package panetina.elarion.addons.portals.service;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import panetina.elarion.addons.economy.api.ElarionEconomyApi;
import panetina.elarion.addons.economy.model.EconomyMixedPayment;
import panetina.elarion.addons.portals.PortalContent;
import panetina.elarion.addons.portals.model.PortalArrival;
import panetina.elarion.addons.portals.model.PortalArrivalRole;
import panetina.elarion.addons.portals.model.PortalEndpoint;
import panetina.elarion.addons.portals.model.PortalEndpointRole;
import panetina.elarion.addons.portals.model.PortalFreePassageState;
import panetina.elarion.addons.portals.model.PortalReturnEntitlement;
import panetina.elarion.addons.portals.model.PortalRouteDefinition;
import panetina.elarion.addons.portals.model.PortalRouteSnapshot;
import panetina.elarion.addons.portals.model.PortalRouteState;
import panetina.elarion.addons.portals.model.PortalRouteStateEvent;
import panetina.elarion.addons.portals.model.PortalScheduleDefinition;
import panetina.elarion.addons.portals.model.PortalSelection;
import panetina.elarion.addons.portals.model.PortalTravelDirection;
import panetina.elarion.addons.portals.storage.PortalState;
import panetina.elarion.addons.portals.storage.PortalStorage;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.model.ElarionNotificationAction;
import panetina.elarion.core.model.ElarionNotificationCategory;
import panetina.elarion.core.service.ElarionNotificationService;
import panetina.elarion.core.service.ElarionPerformanceMonitor;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.concurrent.CopyOnWriteArrayList;

public final class PortalRouteService {
    private static final int PLAYER_CHECK_INTERVAL = 5;
    private static final int SCHEDULE_CHECK_INTERVAL = 20;
    private final Logger logger;
    private final ElarionApi api;
    private final PortalDefinitionService definitions;
    private final PortalStorage storage;
    private final Map<UUID, Set<String>> occupiedEndpoints = new HashMap<>();
    private final Set<UUID> authorizedWorldChanges = new HashSet<>();
    private final Map<UUID, PortalArrival> lastKnownPositions = new HashMap<>();
    private final Map<UUID, PortalArrival> setupOrigins = new HashMap<>();
    private final PortalEndpointIndex endpointIndex = new PortalEndpointIndex();
    private final PortalFieldController fields;
    private final Set<String> obstructedRoutes = new HashSet<>();
    private final Map<String, Boolean> publishedActivity = new HashMap<>();
    private PortalState state = new PortalState();
    private MinecraftServer server;
    private long ticks;
    private boolean dirty;
    private Consumer<TravelPrompt> promptSender = ignored -> {};
    private Runnable visualSync = () -> {};
    private final List<Consumer<PortalRouteStateEvent>> stateListeners = new CopyOnWriteArrayList<>();

    public PortalRouteService(
            Logger logger, ElarionApi api, PortalDefinitionService definitions, PortalStorage storage
    ) {
        this.logger = logger;
        this.api = api;
        this.definitions = definitions;
        this.storage = storage;
        this.fields = new PortalFieldController(logger, api, this::world);
    }

    public void bind(MinecraftServer server) {
        this.server = server;
        state = storage.load(server);
        migrateLegacyRouteIds();
        definitions.all().forEach(definition ->
                state.routes.computeIfAbsent(definition.id(), PortalRouteState::new));
        dirty = true;
        save();
        rebuildIndex();
    }

    private void migrateLegacyRouteIds() {
        migrateLegacyRouteId("ancient_oak", "realm1");
        migrateLegacyRouteId("ancient_sky", "realm2");
        migrateLegacyRouteId("ancient_earth", "realm3");
    }

    private void migrateLegacyRouteId(String legacyId, String routeId) {
        PortalRouteState legacy = state.routes.remove(legacyId);
        if (legacy != null && !state.routes.containsKey(routeId)) {
            legacy.routeId = routeId;
            state.routes.put(routeId, legacy);
        }
        Map<String, PortalReturnEntitlement> migratedEntitlements = new LinkedHashMap<>();
        state.entitlements.entrySet().removeIf(entry -> {
            PortalReturnEntitlement entitlement = entry.getValue();
            if (!legacyId.equals(entitlement.routeId())) return false;
            PortalReturnEntitlement migrated = new PortalReturnEntitlement(
                    entitlement.playerId(), routeId, entitlement.createdAt(), entitlement.sourceWindowStart());
            migratedEntitlements.put(entitlementKey(entitlement.playerId(), routeId), migrated);
            return true;
        });
        state.entitlements.putAll(migratedEntitlements);

        Map<String, PortalFreePassageState> migratedPassages = new LinkedHashMap<>();
        state.freePassages.entrySet().removeIf(entry -> {
            if (!entry.getKey().endsWith("|" + legacyId)) return false;
            String migratedKey = entry.getKey().substring(
                    0, entry.getKey().length() - legacyId.length()) + routeId;
            migratedPassages.put(migratedKey, entry.getValue());
            return true;
        });
        state.freePassages.putAll(migratedPassages);
    }

    public void reload() {
        definitions.load();
        PortalContent.configureTickets(definitions.all());
        definitions.all().forEach(definition ->
                state.routes.computeIfAbsent(definition.id(), PortalRouteState::new));
        dirty = true;
        rebuildIndex();
        reconcileFields();
        visualSync.run();
    }

    public void setPromptSender(Consumer<TravelPrompt> promptSender) {
        this.promptSender = promptSender == null ? ignored -> {} : promptSender;
    }

    public void setVisualSync(Runnable visualSync) {
        this.visualSync = visualSync == null ? () -> {} : visualSync;
    }

    public AutoCloseable onRouteStateChanged(Consumer<PortalRouteStateEvent> listener) {
        stateListeners.add(listener);
        return () -> stateListeners.remove(listener);
    }

    public Collection<PortalRouteSnapshot> snapshots() {
        Instant now = Instant.now();
        return definitions.all().stream().map(definition -> snapshot(definition.id(), now)).toList();
    }

    public PortalRouteSnapshot snapshot(String routeId) {
        return snapshot(routeId, Instant.now());
    }

    private PortalRouteSnapshot snapshot(String routeId, Instant now) {
        PortalRouteDefinition definition = definitions.require(routeId);
        PortalRouteState route = route(routeId);
        PortalScheduleDefinition.Window window = window(definition, route, now);
        boolean unlocked = !definition.mode().requiresUnlock() || route.unlocked;
        return new PortalRouteSnapshot(routeId, definition.displayName(), definition.mode(),
                unlocked, route.complete(),
                isActive(definition, route, now),
                window.start(), window.end(),
                route.source, route.returnEndpoint, definition.visual());
    }

    public PortalRouteState route(String routeId) {
        definitions.require(routeId);
        return state.routes.computeIfAbsent(routeId, PortalRouteState::new);
    }

    public void setEndpoint(
            String routeId, PortalEndpointRole role, PortalSelection selection, ServerPlayerEntity actor
    ) {
        PortalRouteState route = route(routeId);
        PortalRouteDefinition definition = definitions.require(routeId);
        if (role == PortalEndpointRole.SOURCE
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
        var bounds = panetina.elarion.addons.portals.model.PortalBounds.between(
                selection.first(), selection.second());
        PortalEndpoint endpoint = new PortalEndpoint(selection.worldId(), bounds);
        ensureNoOverlap(routeId, role, endpoint);
        fields.validateInterior(endpoint, false);
        if (role == PortalEndpointRole.SOURCE) route.source = endpoint;
        else route.returnEndpoint = endpoint;
        dirty = true;
        save();
        rebuildIndex();
        if (route.complete()) reconcileRoute(routeId);
        visualSync.run();
        record("endpoint-set", actor.getUuid(), routeId, Map.of("role", role.name().toLowerCase()));
    }

    public void setAGate(String routeId, PortalSelection selection, ServerPlayerEntity actor) {
        setEndpoint(routeId, PortalEndpointRole.SOURCE, selection, actor);
    }

    public void setBGate(String routeId, PortalSelection selection, ServerPlayerEntity actor) {
        setEndpoint(routeId, PortalEndpointRole.RETURN, selection, actor);
    }

    public void setArrival(String routeId, PortalArrivalRole role, ServerPlayerEntity actor) {
        PortalRouteDefinition definition = definitions.require(routeId);
        String actorWorld = actor.getWorld().getRegistryKey().getValue().toString();
        if (role == PortalArrivalRole.OUTBOUND && !unrestrictedDestination(definition)
                && !definition.destinationDimension().equals(actorWorld)) {
            throw new IllegalArgumentException("b_arrival must be inside "
                    + definition.destinationDimension() + ".");
        }
        PortalRouteState route = route(routeId);
        if (role == PortalArrivalRole.RETURN && route.source != null
                && !definition.sourceDimension().equals(actorWorld)) {
            throw new IllegalArgumentException("a_arrival must be in the a_gate world.");
        }
        PortalArrival arrival = new PortalArrival(
                actorWorld,
                actor.getX(), actor.getY(), actor.getZ(), actor.getYaw(), actor.getPitch());
        if (role == PortalArrivalRole.OUTBOUND) route.outboundArrival = arrival;
        else route.returnArrival = arrival;
        dirty = true;
        save();
        if (route.complete()) reconcileRoute(routeId);
        record("arrival-set", actor.getUuid(), routeId, Map.of("role", role.name().toLowerCase()));
    }

    public void setAArrival(String routeId, ServerPlayerEntity actor) {
        setArrival(routeId, PortalArrivalRole.RETURN, actor);
    }

    public void setBArrival(String routeId, ServerPlayerEntity actor) {
        setArrival(routeId, PortalArrivalRole.OUTBOUND, actor);
    }

    public void remove(String routeId, UUID actor) {
        PortalRouteDefinition definition = definitions.require(routeId);
        PortalRouteState route = route(routeId);
        fields.deactivate(routeId, route);
        emitStateChange(definition, PortalRouteStateEvent.Type.CLOSED, Instant.now());
        route.source = null;
        route.returnEndpoint = null;
        route.outboundArrival = null;
        route.returnArrival = null;
        route.unlocked = false;
        route.forcedOpenUntil = null;
        route.forcedClosedUntil = null;
        state.entitlements.entrySet().removeIf(entry -> entry.getValue().routeId().equals(routeId));
        state.freePassages.keySet().removeIf(key -> key.endsWith("|" + routeId));
        dirty = true;
        save();
        rebuildIndex();
        visualSync.run();
        record("route-removed", actor, routeId, Map.of());
    }

    public void unlock(String routeId, UUID actor) {
        PortalRouteDefinition definition = definitions.require(routeId);
        if (!definition.mode().requiresUnlock()) {
            throw new IllegalArgumentException(
                    definition.displayName() + " is always open and does not use locks.");
        }
        if (!definition.enabled()) {
            throw new IllegalArgumentException("Portal route " + routeId + " is disabled in routes.yml.");
        }
        PortalRouteState route = route(routeId);
        if (!route.complete()) throw new IllegalArgumentException("Portal route " + routeId + " is not fully linked.");
        if (route.unlocked) return;
        route.unlocked = true;
        dirty = true;
        save();
        List<ElarionNotificationAction> actions = List.of(new ElarionNotificationAction(
                ElarionNotificationService.DISMISS, "Dismiss", true));
        String body = definition.description().isBlank()
                ? "A new route has opened."
                : definition.description();
        if (definition.mode().chargesPassage()) {
            api.notifications().publishRealm(
                    routeId,
                    ElarionNotificationCategory.REALM,
                    "elarion_portals",
                    "ancient-gate-unlocked",
                    "portal-unlocked:" + routeId,
                    definition.displayName() + " Unlocked",
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
                    definition.displayName() + " Unlocked",
                    body,
                    "World Gate",
                    "item:minecraft:ender_eye",
                    actions,
                    Map.of("routeId", routeId),
                    api.notifications().defaultExpiry());
        }
        record("route-unlocked", actor, routeId, Map.of());
        reconcileRoute(routeId);
    }

    public void lock(String routeId, UUID actor) {
        PortalRouteDefinition definition = definitions.require(routeId);
        if (!definition.mode().requiresUnlock()) {
            throw new IllegalArgumentException(
                    definition.displayName() + " is always open and cannot be locked.");
        }
        PortalRouteState route = route(routeId);
        route.unlocked = false;
        route.forcedOpenUntil = null;
        route.forcedClosedUntil = null;
        fields.deactivate(routeId, route);
        emitStateChange(definition, PortalRouteStateEvent.Type.CLOSED, Instant.now());
        dirty = true;
        save();
        visualSync.run();
        record("route-locked", actor, routeId, Map.of());
    }

    public void forceOpen(String routeId, Duration duration, UUID actor) {
        PortalRouteDefinition definition = definitions.require(routeId);
        if (!definition.mode().usesSchedule()) {
            throw new IllegalArgumentException(
                    definition.displayName() + " is always open and has no schedule window.");
        }
        PortalRouteState route = route(routeId);
        if (!route.unlocked || !route.complete()) {
            throw new IllegalArgumentException("Portal route must be unlocked and fully linked.");
        }
        route.forcedOpenUntil = Instant.now().plus(duration).toEpochMilli();
        route.forcedClosedUntil = null;
        dirty = true;
        save();
        reconcileRoute(routeId);
        record("window-force-opened", actor, routeId, Map.of("durationSeconds", Long.toString(duration.toSeconds())));
    }

    public void forceClose(String routeId, UUID actor) {
        PortalRouteDefinition definition = definitions.require(routeId);
        if (!definition.mode().usesSchedule()) {
            throw new IllegalArgumentException(
                    definition.displayName() + " is always open and has no schedule window.");
        }
        PortalRouteState route = route(routeId);
        PortalScheduleDefinition.Window scheduled = definition.schedule().windowAt(Instant.now());
        route.forcedOpenUntil = null;
        route.forcedClosedUntil = scheduled.active() ? scheduled.end().toEpochMilli() : scheduled.start().toEpochMilli();
        fields.deactivate(routeId, route);
        emitStateChange(definition, PortalRouteStateEvent.Type.CLOSED, Instant.now());
        dirty = true;
        save();
        visualSync.run();
        record("window-force-closed", actor, routeId, Map.of());
    }

    public void tick() {
        if (server == null) return;
        ticks++;
        if (ticks % SCHEDULE_CHECK_INTERVAL == 0) tickSchedules();
        if (ticks % PLAYER_CHECK_INTERVAL == 0) tickPlayers();
        if (dirty && ticks % 100 == 0) save();
    }

    private void tickSchedules() {
        Instant now = Instant.now();
        boolean syncNeeded = false;
        for (PortalRouteDefinition definition : definitions.all()) {
            PortalRouteState route = route(definition.id());
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
                        record("window-opened", null, definition.id(), Map.of());
                        dirty = true;
                    }
                } else if (!shouldBeActive && hasFields) {
                    fields.deactivate(definition.id(), route);
                    emitStateChange(definition, PortalRouteStateEvent.Type.CLOSED, now);
                    syncNeeded = true;
                    if (definition.mode().usesSchedule()
                            && route.lastClosedWindow != window.start().toEpochMilli()) {
                        route.lastClosedWindow = window.start().toEpochMilli();
                        record("window-closed", null, definition.id(), Map.of());
                        dirty = true;
                    }
                }
            } catch (RuntimeException exception) {
                if (obstructedRoutes.add(definition.id())) {
                    metric("portal-field-obstructed");
                    logTransitionFailure(definition, exception);
                }
            }
        }
        if (syncNeeded) visualSync.run();
    }

    private void tickPlayers() {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            String world = player.getWorld().getRegistryKey().getValue().toString();
            lastKnownPositions.put(player.getUuid(), new PortalArrival(
                    world, player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch()));
            BlockPos pos = player.getBlockPos();
            Set<String> current = new HashSet<>();
            for (PortalEndpointIndex.Entry indexed : endpointIndex.nearby(world, pos)) {
            PortalRouteDefinition definition = definitions.require(indexed.routeId());
                PortalRouteState route = route(indexed.routeId());
                if (!isActive(definition, route, Instant.now())) continue;
                if (!indexed.endpoint().bounds().contains(pos)) continue;
                PortalTravelDirection direction = indexed.direction();
                String key = definition.id() + ":" + direction;
                current.add(key);
                Set<String> previous = occupiedEndpoints.getOrDefault(player.getUuid(), Set.of());
                if (!previous.contains(key)) {
                    promptSender.accept(prompt(player, definition, route, direction));
                }
            }
            if (current.isEmpty()) occupiedEndpoints.remove(player.getUuid());
            else occupiedEndpoints.put(player.getUuid(), current);
        }
    }

    private TravelPrompt prompt(
            ServerPlayerEntity player,
            PortalRouteDefinition definition,
            PortalRouteState route,
            PortalTravelDirection direction
    ) {
        PortalScheduleDefinition.Window window = window(definition, route, Instant.now());
        boolean entitled = hasEntitlement(player.getUuid(), definition.id());
        boolean ticketed = definition.mode().requiresTicket();
        boolean feePassage = definition.mode().chargesPassage();
        boolean storedReturnPassage = feePassage && direction == PortalTravelDirection.RETURN
                && state.freePassages.get(entitlementKey(player.getUuid(), definition.id()))
                == PortalFreePassageState.RETURN_AVAILABLE;
        boolean freePassage = feePassage && (isFreePassage(player.getUuid(), definition, direction)
                || storedReturnPassage);
        boolean feeReturnWithoutStoredPassage = feePassage && direction == PortalTravelDirection.RETURN
                && !storedReturnPassage;
        ElarionEconomyApi economy = ElarionEconomyApi.get();
        long passagePrice = feePassage ? economy.servicePrice(definition.passagePriceKey()) : 0L;
        boolean hasTicket = !ticketed || direction != PortalTravelDirection.OUTBOUND
                || ticketSlot(player, definition.ticketId()) >= 0;
        boolean canPay = !feeReturnWithoutStoredPassage && (!feePassage || freePassage
                || economy.physicalCurrency(player) + economy.wallet(player.getUuid()) >= passagePrice);
        String requirement = ticketed && direction == PortalTravelDirection.OUTBOUND
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
        String message = !hasTicket ? "The required ticket is not in your inventory."
                : feeReturnWithoutStoredPassage
                ? "Enter from the Realm side first to store a return passage."
                : !canPay ? "You do not have enough physical or banked "
                + api.serverIdentity().currencyPlural() + "."
                : ticketed && direction == PortalTravelDirection.RETURN && !entitled
                ? "You do not have a return passage for this route."
                : storedReturnPassage
                ? "This return completes your current round trip."
                : feePassage && freePassage
                ? "Later passages cost " + api.serverIdentity().currencyAmount(passagePrice) + "."
                : feePassage
                ? "Physical " + api.serverIdentity().currencyPlural()
                + " are used before your bank balance." : "";
        return new TravelPrompt(player, definition.id(), definition.displayName(), definition.description(), direction,
                definition.mode().usesSchedule() ? window.end().toEpochMilli() : 0L,
                ticketed && direction == PortalTravelDirection.OUTBOUND,
                definition.ticketName(),
                definition.visual().iconItem(),
                requirement,
                definition.visual().promptAccentColor(),
                hasTicket && canPay && (!ticketed || direction == PortalTravelDirection.OUTBOUND || entitled),
                message);
    }

    public TravelResult travel(ServerPlayerEntity player, String routeId, PortalTravelDirection direction) {
        long started = System.nanoTime();
        try {
            PortalRouteDefinition definition = definitions.require(routeId);
            PortalRouteState route = route(routeId);
            if (!isActive(definition, route, Instant.now())) return TravelResult.fail("This gate is closed.");
            PortalEndpoint expected = direction == PortalTravelDirection.OUTBOUND ? route.source : route.returnEndpoint;
            if (expected == null || !expected.worldId().equals(world(player))
                    || !expected.bounds().contains(player.getBlockPos())) {
                return TravelResult.fail("You are no longer inside the linked gate.");
            }
            PortalArrival arrival = direction == PortalTravelDirection.OUTBOUND
                    ? route.outboundArrival : route.returnArrival;
            ServerWorld destination = world(arrival.worldId());
            if (destination == null) return TravelResult.fail("The destination world is unavailable.");
            if (definition.mode().chargesPassage()) {
                TravelResult result = travelFeePassage(player, definition, direction, destination, arrival);
                if (!result.success()) return result;
            } else if (!definition.mode().requiresTicket()) {
                if (!teleport(player, destination, arrival)) {
                    return TravelResult.fail("Travel failed.");
                }
            } else if (direction == PortalTravelDirection.OUTBOUND) {
                int slot = ticketSlot(player, definition.ticketId());
                if (slot < 0) return TravelResult.fail("You need a " + definition.ticketName() + ".");
                player.getInventory().removeStack(slot, 1);
                PortalReturnEntitlement previous = entitlement(player.getUuid(), routeId).orElse(null);
                state.entitlements.put(entitlementKey(player.getUuid(), routeId),
                        new PortalReturnEntitlement(player.getUuid(), routeId, System.currentTimeMillis(),
                                window(definition, route, Instant.now()).start().toEpochMilli()));
                dirty = true;
                save();
                if (!teleport(player, destination, arrival)) {
                    player.getInventory().offerOrDrop(PortalContent.TICKET.create(
                            definition.ticketId(), definition.ticketName(), definition.ticketLore()));
                    if (previous == null) state.entitlements.remove(entitlementKey(player.getUuid(), routeId));
                    else state.entitlements.put(entitlementKey(player.getUuid(), routeId), previous);
                    dirty = true;
                    save();
                    return TravelResult.fail("Travel failed; your ticket was restored.");
                }
            } else {
                if (!hasEntitlement(player.getUuid(), routeId)) {
                    return TravelResult.fail("You do not have a return passage for this route.");
                }
                if (!teleport(player, destination, arrival)) return TravelResult.fail("Return travel failed.");
                state.entitlements.remove(entitlementKey(player.getUuid(), routeId));
                dirty = true;
            }
            save();
            occupiedEndpoints.remove(player.getUuid());
            record(direction == PortalTravelDirection.OUTBOUND ? "travel-outbound" : "travel-return",
                    player.getUuid(), routeId, Map.of());
            return TravelResult.ok();
        } finally {
            ElarionPerformanceMonitor.record("portal-travel", System.nanoTime() - started);
        }
    }

    public boolean hasEntitlement(UUID playerId, String routeId) {
        return state.entitlements.containsKey(entitlementKey(playerId, routeId));
    }

    private TravelResult travelFeePassage(
            ServerPlayerEntity player,
            PortalRouteDefinition definition,
            PortalTravelDirection direction,
            ServerWorld destination,
            PortalArrival arrival
    ) {
        String key = entitlementKey(player.getUuid(), definition.id());
        PortalFreePassageState freeState = state.freePassages.get(key);
        boolean free = isFreePassage(player.getUuid(), definition, direction);
        boolean storedReturnPassage = direction == PortalTravelDirection.RETURN
                && freeState == PortalFreePassageState.RETURN_AVAILABLE;
        if (direction == PortalTravelDirection.RETURN && !storedReturnPassage) {
            return TravelResult.fail("You do not have a stored return passage for this route.");
        }
        long price = free || storedReturnPassage ? 0L : ElarionEconomyApi.get().servicePrice(definition.passagePriceKey());
        EconomyMixedPayment payment = null;
        if (price > 0) {
            payment = ElarionEconomyApi.get().payPhysicalThenBank(
                    player, price,
                    "Ancient Gate passage", "elarion:portals");
            if (!payment.successful()) return TravelResult.fail(payment.message());
        }
        if (!teleport(player, destination, arrival)) {
            if (payment != null) {
                ElarionEconomyApi.get().refundMixedPayment(
                        player, payment, "Ancient Gate failed-travel refund", "elarion:portals");
            }
            return TravelResult.fail("Travel failed" + (price > 0 ? "; your payment was refunded." : "."));
        }
        if (free || direction == PortalTravelDirection.OUTBOUND || storedReturnPassage) {
            state.freePassages.put(key,
                    PortalFreePassagePolicy.afterSuccessfulTravel(freeState, direction));
            dirty = true;
        }
        if (price > 0) {
            record("passage-fee-paid", player.getUuid(), definition.id(),
                    Map.of("price", Long.toString(price), "direction", direction.name().toLowerCase()));
        }
        return TravelResult.ok();
    }

    private boolean isFreePassage(
            UUID playerId, PortalRouteDefinition definition, PortalTravelDirection direction
    ) {
        PortalFreePassageState state = this.state.freePassages.get(entitlementKey(playerId, definition.id()));
        return PortalFreePassagePolicy.isFree(definition.firstRoundTripFree(), state, direction);
    }

    public Optional<PortalReturnEntitlement> entitlement(UUID playerId, String routeId) {
        return Optional.ofNullable(state.entitlements.get(entitlementKey(playerId, routeId)));
    }

    public void grantEntitlement(UUID playerId, String routeId) {
        definitions.require(routeId);
        state.entitlements.put(entitlementKey(playerId, routeId),
                new PortalReturnEntitlement(playerId, routeId, System.currentTimeMillis(), 0));
        dirty = true;
        save();
    }

    public void clearEntitlement(UUID playerId, String routeId) {
        state.entitlements.remove(entitlementKey(playerId, routeId));
        dirty = true;
        save();
    }

    public void recordTicketPurchase(UUID playerId, String routeId, long price) {
        record("ticket-purchased", playerId, routeId, Map.of("price", Long.toString(price)));
    }

    public void enterSetupDestination(ServerPlayerEntity player, String routeId, Vec3d requestedPosition) {
        PortalRouteDefinition definition = definitions.require(routeId);
        if (unrestrictedDestination(definition)) {
            throw new IllegalArgumentException(
                    "This route accepts any destination world. Travel there normally, then set its endpoint and arrival.");
        }
        ServerWorld destination = world(definition.destinationDimension());
        if (destination == null) {
            throw new IllegalArgumentException(
                    "Destination world is unavailable: " + definition.destinationDimension());
        }
        setupOrigins.putIfAbsent(player.getUuid(), new PortalArrival(
                world(player), player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch()));
        Vec3d target = requestedPosition == null
                ? Vec3d.ofBottomCenter(destination.getSpawnPos()).add(0.0, 1.0, 0.0)
                : requestedPosition;
        PortalArrival arrival = new PortalArrival(
                definition.destinationDimension(), target.x, target.y, target.z, player.getYaw(), player.getPitch());
        if (!teleport(player, destination, arrival)) {
            throw new IllegalArgumentException("Could not enter the destination world for setup.");
        }
    }

    public void returnFromSetup(ServerPlayerEntity player) {
        PortalArrival origin = setupOrigins.get(player.getUuid());
        if (origin == null) {
            throw new IllegalArgumentException("No portal setup origin is stored for you.");
        }
        ServerWorld destination = world(origin.worldId());
        if (destination == null || !teleport(player, destination, origin)) {
            throw new IllegalArgumentException("Could not return to the stored setup origin.");
        }
        setupOrigins.remove(player.getUuid());
    }

    public void clearSetupOrigin(UUID playerId) {
        setupOrigins.remove(playerId);
    }

    public boolean consumeAuthorizedWorldChange(UUID playerId) {
        return authorizedWorldChanges.remove(playerId);
    }

    public void rejectUnauthorizedWorldChange(
            ServerPlayerEntity player, ServerWorld origin, ServerWorld destination
    ) {
        if (consumeAuthorizedWorldChange(player.getUuid())) return;
        String destinationId = destination.getRegistryKey().getValue().toString();
        String originId = origin.getRegistryKey().getValue().toString();
        if (!isRestrictedDimension(destinationId) && !isRestrictedDimension(originId)) return;
        PortalArrival previous = lastKnownPositions.get(player.getUuid());
        PortalArrival arrival = previous != null && previous.worldId().equals(originId)
                ? previous
                : new PortalArrival(originId, origin.getSpawnPos().getX() + 0.5,
                origin.getSpawnPos().getY() + 1.0, origin.getSpawnPos().getZ() + 0.5, 0, 0);
        authorizedWorldChanges.add(player.getUuid());
        teleport(player, origin, arrival);
        player.sendMessage(Text.literal("Unregistered dimension travel is blocked. Use a scheduled Elarion gate."),
                false);
    }

    public void save() {
        if (server == null || !dirty) return;
        storage.save(server, state);
        dirty = false;
    }

    public void reconcileFields() {
        if (server == null) return;
        definitions.all().forEach(definition -> reconcileRoute(definition.id()));
    }

    public void reconcileRoute(String routeId) {
        PortalRouteDefinition definition = definitions.require(routeId);
        PortalRouteState route = route(routeId);
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
            visualSync.run();
        } catch (RuntimeException exception) {
            if (obstructedRoutes.add(routeId)) {
                metric("portal-field-obstructed");
                logTransitionFailure(definition, exception);
            }
        }
    }

    private void logTransitionFailure(PortalRouteDefinition definition, RuntimeException exception) {
        String message = exception.getMessage();
        if (message != null && message.startsWith("Unknown or unloaded world ")) {
            logger.warn("Portal route {} is inactive because a linked world is unavailable: {}",
                    definition.id(), message.substring("Unknown or unloaded world ".length()));
            return;
        }
        logger.error("Portal route {} could not transition: {}", definition.id(), message);
        announce(definition.displayName() + " could not open because its portal interior is obstructed.");
    }

    private void ensureNoOverlap(String routeId, PortalEndpointRole role, PortalEndpoint endpoint) {
        for (Map.Entry<String, PortalRouteState> entry : state.routes.entrySet()) {
            for (PortalEndpoint existing : endpoints(entry.getValue())) {
                if (existing == null || !existing.worldId().equals(endpoint.worldId())) continue;
                boolean replacingSame = entry.getKey().equals(routeId)
                        && (role == PortalEndpointRole.SOURCE && existing == entry.getValue().source
                        || role == PortalEndpointRole.RETURN && existing == entry.getValue().returnEndpoint);
                if (!replacingSame && existing.bounds().intersects(endpoint.bounds())) {
                    throw new IllegalArgumentException("Portal region overlaps route " + entry.getKey() + ".");
                }
            }
        }
    }

    private boolean isActive(PortalRouteDefinition definition, PortalRouteState route, Instant now) {
        if (!definition.enabled() || !route.complete()) return false;
        if (!definition.mode().requiresUnlock()) return true;
        return route.unlocked && window(definition, route, now).active();
    }

    private static PortalScheduleDefinition.Window window(
            PortalRouteDefinition definition, PortalRouteState route, Instant now
    ) {
        if (route.forcedOpenUntil != null) {
            if (route.forcedOpenUntil > now.toEpochMilli()) {
                return new PortalScheduleDefinition.Window(now, Instant.ofEpochMilli(route.forcedOpenUntil), true);
            }
        }
        if (route.forcedClosedUntil != null && route.forcedClosedUntil > now.toEpochMilli()) {
            PortalScheduleDefinition.Window scheduled = definition.schedule().windowAt(now);
            return new PortalScheduleDefinition.Window(scheduled.start(), scheduled.end(), false);
        }
        return definition.schedule().windowAt(now);
    }

    private int ticketSlot(ServerPlayerEntity player, String ticketId) {
        for (int slot = 0; slot < player.getInventory().size(); slot++) {
            if (PortalContent.TICKET.matches(player.getInventory().getStack(slot), ticketId)) return slot;
        }
        return -1;
    }

    private void emitStateChange(
            PortalRouteDefinition definition,
            PortalRouteStateEvent.Type type,
            Instant occurredAt
    ) {
        boolean active = type == PortalRouteStateEvent.Type.OPENED;
        Boolean previous = publishedActivity.put(definition.id(), active);
        if ((previous == null && !active) || java.util.Objects.equals(previous, active)) return;
        PortalRouteStateEvent event = new PortalRouteStateEvent(
                definition.id(), definition.displayName(), type, occurredAt,
                snapshot(definition.id(), occurredAt));
        stateListeners.forEach(listener -> {
            try {
                listener.accept(event);
            } catch (RuntimeException exception) {
                logger.error("Portal route-state listener failed for {}", definition.id(), exception);
            }
        });
    }

    private boolean teleport(ServerPlayerEntity player, ServerWorld destination, PortalArrival arrival) {
        boolean changesWorld = player.getWorld() != destination;
        try {
            if (changesWorld) authorizedWorldChanges.add(player.getUuid());
            player.teleport(destination, arrival.x(), arrival.y(), arrival.z(),
                    Set.of(), arrival.yaw(), arrival.pitch());
            if (!changesWorld) authorizedWorldChanges.remove(player.getUuid());
            return true;
        } catch (RuntimeException exception) {
            authorizedWorldChanges.remove(player.getUuid());
            logger.error("Portal teleport failed for {}", player.getGameProfile().getName(), exception);
            return false;
        }
    }

    private ServerWorld world(String raw) {
        if (server == null) return null;
        Identifier id = Identifier.tryParse(raw);
        return id == null ? null : server.getWorld(RegistryKey.of(RegistryKeys.WORLD, id));
    }

    private static String world(ServerPlayerEntity player) {
        return player.getWorld().getRegistryKey().getValue().toString();
    }

    private static boolean unrestrictedDestination(PortalRouteDefinition definition) {
        return "*".equals(definition.destinationDimension());
    }

    private static boolean isRestrictedDimension(String worldId) {
        return "minecraft:the_nether".equals(worldId) || "minecraft:the_end".equals(worldId);
    }

    private static String entitlementKey(UUID playerId, String routeId) {
        return playerId + "|" + routeId;
    }

    static List<PortalEndpoint> endpoints(PortalRouteState route) {
        List<PortalEndpoint> result = new ArrayList<>(2);
        if (route.source != null) result.add(route.source);
        if (route.returnEndpoint != null) result.add(route.returnEndpoint);
        return result;
    }

    private void rebuildIndex() {
        endpointIndex.rebuild(state.routes);
    }

    private void announce(String message) {
        if (server != null) server.getPlayerManager().broadcast(Text.literal(message), false);
    }

    private void record(String type, UUID actor, String routeId, Map<String, String> details) {
        Map<String, String> metadata = new LinkedHashMap<>(details);
        metadata.put("routeId", routeId);
        api.history().recordChronicle("portal", type, actor, "portal_route", routeId, "", metadata,
                "The portal route " + routeId + " recorded " + type.replace('-', ' ') + ".");
    }

    private static void metric(String name) {
        ElarionPerformanceMonitor.record(name, 0L);
    }

    public record TravelPrompt(
            ServerPlayerEntity player,
            String routeId,
            String title,
            String description,
            PortalTravelDirection direction,
            long closesAt,
            boolean ticketRequired,
            String ticketName,
            String iconItem,
            String requirement,
            int requirementColor,
            boolean allowed,
            String message
    ) {
    }

    public record TravelResult(boolean success, String message) {
        public static TravelResult ok() {
            return new TravelResult(true, "Travel completed.");
        }

        public static TravelResult fail(String message) {
            return new TravelResult(false, message);
        }
    }
}
