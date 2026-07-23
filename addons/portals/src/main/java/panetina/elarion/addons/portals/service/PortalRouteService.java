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
import panetina.elarion.core.model.ElarionDomainEvent;
import panetina.elarion.core.service.ElarionPerformanceMonitor;
import panetina.elarion.core.service.PlayerRestrictionService;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.concurrent.CopyOnWriteArrayList;

public final class PortalRouteService {
    public static final String PORTAL_JOURNEYS_STAT = "portal_journeys";
    private static final int PLAYER_CHECK_INTERVAL = 5;
    private static final int SCHEDULE_CHECK_INTERVAL = 20;
    private final Logger logger;
    private final ElarionApi api;
    private final PortalDefinitionService definitions;
    private final PortalStorage storage;
    private final PortalEndpointIndex endpointIndex = new PortalEndpointIndex();
    private final PortalFieldController fields;
    private final PortalWorldTravelGuard worldTravelGuard;
    private final PortalScheduleReconciler scheduleReconciler;
    private final PortalPlayerPromptDetector promptDetector;
    private final PortalTravelExecutor<ServerPlayerEntity, ServerWorld> travelExecutor;
    private final PortalRouteAdminMutator adminMutator;
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
        this.worldTravelGuard = new PortalWorldTravelGuard(logger, definitions, this::world);
        this.scheduleReconciler = new PortalScheduleReconciler(logger, definitions, fields,
                new PortalScheduleReconciler.Effects() {
                    @Override
                    public void markDirty() {
                        dirty = true;
                    }

                    @Override
                    public void syncVisuals() {
                        visualSync.run();
                    }

                    @Override
                    public void record(String type, String routeId) {
                        PortalRouteService.this.record(type, null, routeId, Map.of());
                    }

                    @Override
                    public void announce(String message) {
                        if (server != null) {
                            server.getPlayerManager().broadcast(Text.literal(message), false);
                        }
                    }

                    @Override
                    public String displayName(PortalRouteDefinition definition) {
                        return routeDisplayName(definition);
                    }

                    @Override
                    public PortalRouteSnapshot snapshot(String routeId, Instant occurredAt) {
                        return PortalRouteService.this.snapshot(routeId, occurredAt);
                    }

                    @Override
                    public void publish(PortalRouteStateEvent event) {
                        publishStateEvent(event);
                    }

                    @Override
                    public void emitDomainEvent(
                            String eventType,
                            PortalRouteDefinition definition,
                            Map<String, String> metadata
                    ) {
                        emitRouteDomainEvent(eventType, definition, null, metadata);
                    }
                });
        this.promptDetector = new PortalPlayerPromptDetector(api, definitions, endpointIndex,
                new PortalPlayerPromptDetector.Effects() {
                    @Override
                    public PortalRouteState route(String routeId) {
                        return PortalRouteService.this.route(routeId);
                    }

                    @Override
                    public void rememberPosition(UUID playerId, PortalArrival arrival) {
                        worldTravelGuard.rememberPosition(playerId, arrival);
                    }

                    @Override
                    public boolean hasEntitlement(UUID playerId, String routeId) {
                        return PortalRouteService.this.hasEntitlement(playerId, routeId);
                    }

                    @Override
                    public PortalFreePassageState freePassageState(UUID playerId, String routeId) {
                        return state.freePassages.get(entitlementKey(playerId, routeId));
                    }

                    @Override
                    public boolean isFreePassage(
                            UUID playerId,
                            PortalRouteDefinition definition,
                            PortalTravelDirection direction
                    ) {
                        return PortalRouteService.this.isFreePassage(playerId, definition, direction);
                    }

                    @Override
                    public boolean hasTicket(ServerPlayerEntity player, String ticketId) {
                        return ticketSlot(player, ticketId) >= 0;
                    }

                    @Override
                    public String displayName(PortalRouteDefinition definition) {
                        return routeDisplayName(definition);
                    }

                    @Override
                    public String description(PortalRouteDefinition definition) {
                        return routeDescription(definition);
                    }

                    @Override
                    public void sendPrompt(TravelPrompt prompt) {
                        promptSender.accept(prompt);
                    }
                });
        this.travelExecutor = new PortalTravelExecutor<>(
                new PortalTravelExecutor.Effects<>() {
                    @Override
                    public String restriction(ServerPlayerEntity player) {
                        return api.system().restrictions()
                                .restriction(player, PlayerRestrictionService.PORTAL_TRAVEL)
                                .map(PlayerRestrictionService.PlayerRestriction::message)
                                .orElse(null);
                    }

                    @Override
                    public UUID playerId(ServerPlayerEntity player) {
                        return player.getUuid();
                    }

                    @Override
                    public String playerWorld(ServerPlayerEntity player) {
                        return world(player);
                    }

                    @Override
                    public BlockPos playerPosition(ServerPlayerEntity player) {
                        return player.getBlockPos();
                    }

                    @Override
                    public Instant now() {
                        return Instant.now();
                    }

                    @Override
                    public ServerWorld destination(String worldId) {
                        return world(worldId);
                    }

                    @Override
                    public boolean teleport(
                            ServerPlayerEntity player,
                            ServerWorld destination,
                            PortalArrival arrival
                    ) {
                        return worldTravelGuard.teleport(player, destination, arrival);
                    }

                    @Override
                    public int ticketSlot(ServerPlayerEntity player, String ticketId) {
                        return PortalRouteService.this.ticketSlot(player, ticketId);
                    }

                    @Override
                    public void removeTicket(ServerPlayerEntity player, int slot) {
                        player.getInventory().removeStack(slot, 1);
                    }

                    @Override
                    public void restoreTicket(
                            ServerPlayerEntity player,
                            PortalRouteDefinition definition
                    ) {
                        player.getInventory().offerOrDrop(PortalContent.TICKET.create(
                                definition.ticketId(),
                                definition.ticketName(),
                                definition.ticketLore()));
                    }

                    @Override
                    public long servicePrice(String priceKey) {
                        return ElarionEconomyApi.get().servicePrice(priceKey);
                    }

                    @Override
                    public PortalTravelExecutor.PaymentAttempt pay(
                            ServerPlayerEntity player,
                            long price
                    ) {
                        EconomyMixedPayment payment = ElarionEconomyApi.get().payPhysicalOnly(
                                player, price, "Ancient Gate passage", "elarion:portals");
                        if (!payment.successful()) {
                            return PortalTravelExecutor.PaymentAttempt.failure(payment.message());
                        }
                        return PortalTravelExecutor.PaymentAttempt.success(() ->
                                ElarionEconomyApi.get().refundMixedPayment(
                                        player,
                                        payment,
                                        "Ancient Gate failed-travel refund",
                                        "elarion:portals"));
                    }

                    @Override
                    public void markDirty() {
                        dirty = true;
                    }

                    @Override
                    public void save() {
                        PortalRouteService.this.save();
                    }

                    @Override
                    public void clearOccupation(UUID playerId) {
                        promptDetector.clearOccupation(playerId);
                    }

                    @Override
                    public void record(
                            String type,
                            UUID actor,
                            String routeId,
                            Map<String, String> details
                    ) {
                        PortalRouteService.this.record(type, actor, routeId, details);
                    }

                    @Override
                    public void incrementJourney(UUID playerId) {
                        incrementPortalJourneys(playerId);
                    }
                });
        this.adminMutator = new PortalRouteAdminMutator(api, definitions, fields,
                new PortalRouteAdminMutator.Effects() {
                    @Override
                    public void persist(boolean rebuildIndex) {
                        dirty = true;
                        save();
                        if (rebuildIndex) rebuildIndex();
                    }

                    @Override
                    public void reconcile(String routeId) {
                        reconcileRoute(routeId);
                    }

                    @Override
                    public void syncVisuals() {
                        visualSync.run();
                    }

                    @Override
                    public void emitStateChange(
                            PortalRouteDefinition definition,
                            PortalRouteStateEvent.Type type,
                            Instant occurredAt
                    ) {
                        scheduleReconciler.emitStateChange(definition, type, occurredAt);
                    }

                    @Override
                    public void emitDomainEvent(
                            String eventType,
                            PortalRouteDefinition definition,
                            UUID actor,
                            Map<String, String> metadata
                    ) {
                        emitRouteDomainEvent(eventType, definition, actor, metadata);
                    }

                    @Override
                    public void record(String type, UUID actor, String routeId, Map<String, String> details) {
                        PortalRouteService.this.record(type, actor, routeId, details);
                    }

                    @Override
                    public String displayName(PortalRouteDefinition definition) {
                        return routeDisplayName(definition);
                    }

                    @Override
                    public String description(PortalRouteDefinition definition) {
                        return routeDescription(definition);
                    }
                });
    }

    public void bind(MinecraftServer server) {
        this.server = server;
        state = storage.load(server);
        PortalStateMigration.migrateLegacyRouteIds(state);
        definitions.all().forEach(definition ->
                state.routes.computeIfAbsent(definition.id(), PortalRouteState::new));
        dirty = true;
        save();
        rebuildIndex();
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
        return new PortalRouteSnapshot(routeId, routeDisplayName(definition), definition.mode(),
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
        adminMutator.setEndpoint(state, routeId, role, selection, actor);
    }

    public void setAGate(String routeId, PortalSelection selection, ServerPlayerEntity actor) {
        setEndpoint(routeId, PortalEndpointRole.SOURCE, selection, actor);
    }

    public void setBGate(String routeId, PortalSelection selection, ServerPlayerEntity actor) {
        setEndpoint(routeId, PortalEndpointRole.RETURN, selection, actor);
    }

    public void setArrival(String routeId, PortalArrivalRole role, ServerPlayerEntity actor) {
        adminMutator.setArrival(state, routeId, role, actor);
    }

    public void setAArrival(String routeId, ServerPlayerEntity actor) {
        setArrival(routeId, PortalArrivalRole.RETURN, actor);
    }

    public void setBArrival(String routeId, ServerPlayerEntity actor) {
        setArrival(routeId, PortalArrivalRole.OUTBOUND, actor);
    }

    public void remove(String routeId, UUID actor) {
        adminMutator.remove(state, routeId, actor);
    }

    public void unlock(String routeId, UUID actor) {
        adminMutator.unlock(state, routeId, actor);
    }

    public void lock(String routeId, UUID actor) {
        adminMutator.lock(state, routeId, actor);
    }

    public void forceOpen(String routeId, Duration duration, UUID actor) {
        adminMutator.forceOpen(state, routeId, duration, actor);
    }

    public void forceClose(String routeId, UUID actor) {
        adminMutator.forceClose(state, routeId, actor);
    }

    public void tick() {
        if (server == null) return;
        ticks++;
        if (ticks % SCHEDULE_CHECK_INTERVAL == 0) scheduleReconciler.tick(state);
        if (ticks % PLAYER_CHECK_INTERVAL == 0) {
            promptDetector.tick(server.getPlayerManager().getPlayerList());
        }
        if (dirty && ticks % 100 == 0) save();
    }

    public TravelResult travel(ServerPlayerEntity player, String routeId, PortalTravelDirection direction) {
        long started = System.nanoTime();
        try {
            PortalRouteDefinition definition = definitions.require(routeId);
            PortalRouteState route = route(routeId);
            return travelExecutor.travel(state, player, definition, route, direction);
        } finally {
            ElarionPerformanceMonitor.record("portal-travel", System.nanoTime() - started);
        }
    }

    private void incrementPortalJourneys(UUID playerId) {
        api.playerStats().increment(playerId, PORTAL_JOURNEYS_STAT, 1L);
    }

    public boolean hasEntitlement(UUID playerId, String routeId) {
        return state.entitlements.containsKey(entitlementKey(playerId, routeId));
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
        worldTravelGuard.enterSetupDestination(player, routeId, requestedPosition);
    }

    public void returnFromSetup(ServerPlayerEntity player) {
        worldTravelGuard.returnFromSetup(player);
    }

    public void clearSetupOrigin(UUID playerId) {
        worldTravelGuard.clearSetupOrigin(playerId);
    }

    public boolean consumeAuthorizedWorldChange(UUID playerId) {
        return worldTravelGuard.consumeAuthorizedWorldChange(playerId);
    }

    public void rejectUnauthorizedWorldChange(
            ServerPlayerEntity player, ServerWorld origin, ServerWorld destination
    ) {
        worldTravelGuard.rejectUnauthorizedWorldChange(player, origin, destination);
    }

    public void save() {
        if (server == null || !dirty) return;
        storage.save(server, state);
        dirty = false;
    }

    public int resetAllPlayerState() {
        int changed = state.entitlements.size() + state.freePassages.size();
        state.entitlements.clear();
        state.freePassages.clear();
        dirty = true;
        save();
        return changed;
    }

    public synchronized int deleteWorldEndpoints(String worldId) {
        int changed = 0;
        for (PortalRouteState route : state.routes.values()) {
            boolean matches = (route.source != null && worldId.equals(route.source.worldId()))
                    || (route.returnEndpoint != null && worldId.equals(route.returnEndpoint.worldId()));
            if (!matches) continue;
            route.source = null;
            route.returnEndpoint = null;
            route.outboundArrival = null;
            route.returnArrival = null;
            route.unlocked = false;
            route.forcedOpenUntil = null;
            route.forcedClosedUntil = null;
            changed++;
        }
        if (changed > 0) { dirty = true; endpointIndex.rebuild(state.routes); save(); }
        return changed;
    }

    public void reconcileFields() {
        if (server == null) return;
        scheduleReconciler.reconcileAll(state);
    }

    public void reconcileRoute(String routeId) {
        scheduleReconciler.reconcile(state, routeId);
    }

    private boolean isActive(PortalRouteDefinition definition, PortalRouteState route, Instant now) {
        return PortalScheduleReconciler.isActive(definition, route, now);
    }

    private static PortalScheduleDefinition.Window window(
            PortalRouteDefinition definition, PortalRouteState route, Instant now
    ) {
        return PortalScheduleReconciler.window(definition, route, now);
    }

    private int ticketSlot(ServerPlayerEntity player, String ticketId) {
        for (int slot = 0; slot < player.getInventory().size(); slot++) {
            if (PortalContent.TICKET.matches(player.getInventory().getStack(slot), ticketId)) return slot;
        }
        return -1;
    }

    private void publishStateEvent(PortalRouteStateEvent event) {
        stateListeners.forEach(listener -> {
            try {
                listener.accept(event);
            } catch (RuntimeException exception) {
                logger.error("Portal route-state listener failed for {}", event.routeId(), exception);
            }
        });
    }

    private String routeDisplayName(PortalRouteDefinition definition) {
        return formatRealmText(definition, definition.displayName());
    }

    private String routeDescription(PortalRouteDefinition definition) {
        return formatRealmText(definition, definition.description());
    }

    private String formatRealmText(PortalRouteDefinition definition, String raw) {
        return api.realms().find(definition.id())
                .map(api.realms()::presentation)
                .map(presentation -> PortalRealmText.format(raw, presentation, api.system().placeholders()))
                .orElse(raw == null ? "" : raw);
    }

    private void emitRouteDomainEvent(
            String eventType,
            PortalRouteDefinition definition,
            UUID actor,
            Map<String, String> stateMetadata
    ) {
        Map<String, String> metadata = new LinkedHashMap<>(stateMetadata);
        metadata.put("displayName", routeDisplayName(definition));
        metadata.put("mode", definition.mode().configId());
        String realmId = api.realms().find(definition.id()).map(realm -> realm.id()).orElse("");
        api.system().events().emitDomainEvent(ElarionDomainEvent.of(
                "elarion_portals",
                eventType,
                actor,
                realmId,
                "portal-route",
                definition.id(),
                metadata));
    }

    private ServerWorld world(String raw) {
        if (server == null) return null;
        Identifier id = Identifier.tryParse(raw);
        return id == null ? null : server.getWorld(RegistryKey.of(RegistryKeys.WORLD, id));
    }

    private static String world(ServerPlayerEntity player) {
        return player.getWorld().getRegistryKey().getValue().toString();
    }

    private static boolean unrestrictedSource(PortalRouteDefinition definition) {
        return "*".equals(definition.sourceDimension());
    }

    private static String entitlementKey(UUID playerId, String routeId) {
        return PortalTravelExecutor.entitlementKey(playerId, routeId);
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
            String costKind,
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
