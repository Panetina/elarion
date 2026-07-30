package panetina.elarion.core.service;

import com.google.gson.Gson;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.ElarionNotificationAction;
import panetina.elarion.core.model.ElarionNotificationCategory;
import panetina.elarion.core.model.ElarionNotificationEntry;
import panetina.elarion.core.model.ElarionNotificationSnapshot;
import panetina.elarion.core.model.ElarionStoredNotification;
import panetina.elarion.core.network.NotificationSnapshotPayload;
import panetina.elarion.core.integration.minecraft.MinecraftProjectionPublisher;
import panetina.elarion.core.integration.minecraft.MinecraftProjectionProtocol.Visibility;
import panetina.elarion.core.storage.NotificationStorage;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

public final class ElarionNotificationService {
    public static final String DISMISS = "elarion_core:dismiss";
    public static final String MARK_READ = "elarion_core:mark_read";
    private static final long DEFAULT_EXPIRY_MILLIS = Duration.ofDays(30).toMillis();
    private static final int MAX_PER_CATEGORY = 100;
    private static final int MAX_LAUNCHER_ENTRIES = 5;
    private static final int MAX_LAUNCHER_TEXT = 240;
    private static final Gson GSON = new Gson();

    private final NotificationStorage storage;
    private final CitizenService citizens;
    private final MinecraftProjectionPublisher webProjections;
    private final Map<String, ElarionStoredNotification> notifications = new LinkedHashMap<>();
    private final Map<UUID, LinkedHashSet<String>> notificationIdsByRecipient = new LinkedHashMap<>();
    private final Map<String, NotificationActionHandler> actionHandlers = new LinkedHashMap<>();
    private final List<Function<UUID, List<ElarionNotificationEntry>>> providers = new CopyOnWriteArrayList<>();
    private final Set<String> worldEligibleRealms = new LinkedHashSet<>();
    private MinecraftServer server;

    public ElarionNotificationService(NotificationStorage storage, CitizenService citizens,
                                      MinecraftProjectionPublisher webProjections) {
        this.storage = storage;
        this.citizens = citizens;
        this.webProjections = webProjections;
    }

    public synchronized void bind(MinecraftServer server) {
        this.server = server;
        notifications.clear();
        notificationIdsByRecipient.clear();
        for (ElarionStoredNotification notification : storage.load(server)) {
            if (notification != null && !notification.id().isBlank()) {
                notifications.put(notification.id(), notification);
                index(notification);
            }
        }
        prune(System.currentTimeMillis());
        publishLauncherSnapshots();
    }

    public void registerProvider(Function<UUID, List<ElarionNotificationEntry>> provider) {
        if (provider != null) providers.add(provider);
    }

    public synchronized void replaceWorldEligibleRealms(Collection<String> realmIds) {
        worldEligibleRealms.clear();
        if (realmIds != null) {
            realmIds.stream()
                    .filter(realmId -> realmId != null && !realmId.isBlank())
                    .map(String::trim)
                    .forEach(worldEligibleRealms::add);
        }
        publishLauncherSnapshots();
        syncAll();
    }

    public synchronized void setWorldRealmEligible(String realmId, boolean eligible) {
        String normalized = clean(realmId);
        if (normalized.isBlank()) return;
        if (eligible) worldEligibleRealms.add(normalized);
        else worldEligibleRealms.remove(normalized);
        syncRealm(normalized);
    }

    public synchronized boolean isWorldRealmEligible(String realmId) {
        return worldEligibleRealms.contains(clean(realmId));
    }

    public synchronized void registerAction(String actionId, NotificationActionHandler handler) {
        String normalized = clean(actionId);
        if (normalized.isBlank() || handler == null) throw new IllegalArgumentException("Notification action is required.");
        if (actionHandlers.putIfAbsent(normalized, handler) != null) {
            throw new IllegalStateException("Notification action already registered: " + normalized);
        }
    }

    public String publishPersonal(
            UUID recipientId,
            ElarionNotificationCategory category,
            String sourceSystem,
            String eventType,
            String deduplicationKey,
            String title,
            String body,
            String status,
            String icon,
            List<ElarionNotificationAction> actions,
            Map<String, String> metadata,
            long expiresAt
    ) {
        if (recipientId == null) return "";
        String id = publish(recipientId, category, sourceSystem, eventType, deduplicationKey,
                title, body, status, icon, actions, metadata, expiresAt);
        syncOnline(recipientId);
        return id;
    }

    public int publishRealm(
            String realmId,
            ElarionNotificationCategory category,
            String sourceSystem,
            String eventType,
            String deduplicationKey,
            String title,
            String body,
            String status,
            String icon,
            List<ElarionNotificationAction> actions,
            Map<String, String> metadata,
            long expiresAt
    ) {
        int published = 0;
        for (UUID citizenId : citizens.citizenIdsInRealm(realmId)) {
            String recipientKey = deduplicationKey.isBlank() ? "" : deduplicationKey + ":" + citizenId;
            publish(citizenId, category, sourceSystem, eventType, recipientKey,
                    title, body, status, icon, actions, metadata, expiresAt);
            syncOnline(citizenId);
            published++;
        }
        return published;
    }

    public int publishWorld(
            String sourceSystem,
            String eventType,
            String deduplicationKey,
            String title,
            String body,
            String status,
            String icon,
            List<ElarionNotificationAction> actions,
            Map<String, String> metadata,
            long expiresAt
    ) {
        int published = 0;
        for (UUID citizenId : citizens.citizenIdsInRealms(worldEligibleRealms)) {
            String recipientKey = deduplicationKey.isBlank() ? "" : deduplicationKey + ":" + citizenId;
            publish(citizenId, ElarionNotificationCategory.WORLD, sourceSystem, eventType, recipientKey,
                    title, body, status, icon, actions, metadata, expiresAt);
            syncOnline(citizenId);
            published++;
        }
        return published;
    }

    public synchronized ActionResult act(ServerPlayerEntity player, String notificationId, String actionId) {
        if (player == null) return ActionResult.failure("Player is unavailable.");
        ElarionStoredNotification notification = notifications.get(clean(notificationId));
        if (notification == null || notification.resolved() || !player.getUuid().equals(notification.recipientId())) {
            sync(player);
            return ActionResult.failure("Notification is no longer available.");
        }
        long now = System.currentTimeMillis();
        if (notification.expired(now)) {
            notifications.put(notification.id(), notification.resolve());
            save();
            sync(player);
            return ActionResult.failure("Notification expired.");
        }
        String normalizedAction = clean(actionId);
        if (MARK_READ.equals(normalizedAction)) {
            notifications.put(notification.id(), notification.read());
            save();
            sync(player);
            return ActionResult.success("", false);
        }
        if (DISMISS.equals(normalizedAction)) {
            notifications.put(notification.id(), notification.resolve());
            save();
            sync(player);
            return ActionResult.success("Dismissed.", true);
        }
        boolean offered = notification.actions().stream()
                .anyMatch(action -> normalizedAction.equals(action.id()) && action.enabled());
        NotificationActionHandler handler = actionHandlers.get(normalizedAction);
        if (!offered || handler == null) return ActionResult.failure("Action is unavailable.");
        ActionResult result = handler.handle(new ActionContext(player, notification, normalizedAction));
        ElarionStoredNotification current = notifications.get(notification.id());
        if (current != null) {
            if (result.resolve()) current = current.resolve();
            else current = current.read().withStatus(result.message());
            notifications.put(current.id(), current);
            save();
        }
        sync(player);
        return result;
    }

    public synchronized ElarionNotificationSnapshot snapshot(UUID recipientId) {
        if (recipientId == null) return ElarionNotificationSnapshot.EMPTY;
        long now = System.currentTimeMillis();
        prune(now);
        List<ElarionNotificationEntry> entries = new ArrayList<>();
        notificationIdsByRecipient.getOrDefault(recipientId, new LinkedHashSet<>()).stream()
                .map(notifications::get)
                .filter(java.util.Objects::nonNull)
                .filter(notification -> !notification.resolved() && !notification.expired(now))
                .filter(notification -> notification.category() != ElarionNotificationCategory.WORLD
                        || worldEligible(recipientId))
                .map(this::entry)
                .forEach(entries::add);
        for (Function<UUID, List<ElarionNotificationEntry>> provider : providers) {
            List<ElarionNotificationEntry> supplied = provider.apply(recipientId);
            if (supplied != null) entries.addAll(supplied);
        }
        return new ElarionNotificationSnapshot(entries, worldEligible(recipientId));
    }

    public synchronized void sync(ServerPlayerEntity player) {
        if (server == null || player == null) return;
        // A join/sync is the bounded recovery point for notifications that
        // existed before the bridge was enabled or before a server restart.
        // Keep the website read model recipient-scoped and current without
        // scanning global notification history.
        publishLauncherSnapshot(player.getUuid());
        ServerPlayNetworking.send(player, new NotificationSnapshotPayload(snapshot(player.getUuid())));
    }

    public synchronized void save() {
        if (server != null) storage.save(server, List.copyOf(notifications.values()));
    }

    public synchronized int resetAllPlayerState() {
        int count = notifications.size();
        notifications.clear();
        notificationIdsByRecipient.clear();
        save();
        return count;
    }

    public synchronized void syncRealm(String realmId) {
        if (server == null || realmId == null || realmId.isBlank()) return;
        citizens.citizenIdsInRealm(realmId).forEach(this::syncOnline);
    }

    public synchronized void syncAll() {
        if (server == null) return;
        server.getPlayerManager().getPlayerList().forEach(this::sync);
    }

    public synchronized int resolveByMetadata(String sourceSystem, String key, String value) {
        List<UUID> recipients = new ArrayList<>();
        int resolved = 0;
        for (Map.Entry<String, ElarionStoredNotification> entry : List.copyOf(notifications.entrySet())) {
            ElarionStoredNotification notification = entry.getValue();
            if (!clean(sourceSystem).equals(notification.sourceSystem())) continue;
            if (!clean(value).equals(notification.metadata().getOrDefault(key, ""))) continue;
            if (notification.resolved()) continue;
            notifications.put(entry.getKey(), notification.resolve());
            recipients.add(notification.recipientId());
            resolved++;
        }
        if (resolved > 0) {
            save();
            recipients.stream().distinct().forEach(this::syncOnline);
        }
        return resolved;
    }

    public long defaultExpiry() {
        return System.currentTimeMillis() + DEFAULT_EXPIRY_MILLIS;
    }

    private synchronized String publish(
            UUID recipientId,
            ElarionNotificationCategory category,
            String sourceSystem,
            String eventType,
            String deduplicationKey,
            String title,
            String body,
            String status,
            String icon,
            List<ElarionNotificationAction> actions,
            Map<String, String> metadata,
            long expiresAt
    ) {
        String source = clean(sourceSystem);
        String dedupe = clean(deduplicationKey);
        String id = dedupe.isBlank()
                ? UUID.randomUUID().toString()
                : UUID.nameUUIDFromBytes((source + "|" + dedupe).getBytes(StandardCharsets.UTF_8)).toString();
        ElarionStoredNotification existing = notifications.get(id);
        if (existing != null && !existing.resolved()) return id;
        List<ElarionNotificationAction> safeActions = actions == null ? List.of() : List.copyOf(actions);
        long effectiveExpiry = expiresAt;
        if (effectiveExpiry < 0L) effectiveExpiry = 0L;
        if (effectiveExpiry == 0L && safeActions.stream().allMatch(action -> DISMISS.equals(action.id()))) {
            effectiveExpiry = System.currentTimeMillis() + DEFAULT_EXPIRY_MILLIS;
        }
        notifications.put(id, new ElarionStoredNotification(
                id, recipientId, category, source, eventType, dedupe, title, body, status, icon,
                true, false, System.currentTimeMillis(), effectiveExpiry, safeActions, metadata));
        index(notifications.get(id));
        trim(recipientId, category);
        save();
        return id;
    }

    private ElarionNotificationEntry entry(ElarionStoredNotification notification) {
        return new ElarionNotificationEntry(
                notification.id(), notification.category(), notification.title(), notification.body(),
                notification.status(), notification.icon(), notification.unread(), notification.actions(),
                List.of(), notification.createdAt());
    }

    private void trim(UUID recipientId, ElarionNotificationCategory category) {
        List<ElarionStoredNotification> matches = notifications.values().stream()
                .filter(notification -> recipientId.equals(notification.recipientId()))
                .filter(notification -> category == notification.category())
                .sorted(Comparator.comparingLong(ElarionStoredNotification::createdAt))
                .toList();
        int remove = matches.size() - MAX_PER_CATEGORY;
        for (int index = 0; index < remove; index++) remove(matches.get(index).id());
    }

    private void prune(long now) {
        List<String> removed = notifications.values().stream()
                .filter(notification -> notification.resolved() || notification.expired(now))
                .map(ElarionStoredNotification::id)
                .toList();
        removed.forEach(this::remove);
        boolean changed = !removed.isEmpty();
        if (changed) save();
    }

    private void syncOnline(UUID recipientId) {
        if (server == null) return;
        ServerPlayerEntity player = server.getPlayerManager().getPlayer(recipientId);
        if (player != null) sync(player);
        else publishLauncherSnapshot(recipientId);
    }

    private boolean worldEligible(UUID recipientId) {
        return citizens.find(recipientId).map(this::worldEligible).orElse(false);
    }

    private boolean worldEligible(CitizenRecord citizen) {
        return citizen != null
                && !citizen.realmId().isBlank()
                && worldEligibleRealms.contains(citizen.realmId());
    }

    private void publishLauncherSnapshot(UUID recipientId) {
        if (recipientId == null || webProjections == null) return;
        List<Map<String, String>> entries = snapshot(recipientId).entries().stream()
                .filter(ElarionNotificationEntry::unread)
                .sorted(Comparator.comparingLong(ElarionNotificationEntry::createdAt).reversed()
                        .thenComparing(ElarionNotificationEntry::id))
                .limit(MAX_LAUNCHER_ENTRIES)
                .map(entry -> Map.of(
                        "id", clean(entry.id()),
                        "category", entry.category().name(),
                        "sourceSystem", "game",
                        "title", truncate(entry.title()),
                        "body", truncate(entry.body()),
                        "createdAt", Long.toString(entry.createdAt())))
                .toList();
        String realmId = citizens.find(recipientId).map(CitizenRecord::realmId).orElse("");
        webProjections.publishState("citizen.notifications", recipientId.toString(), realmId,
                Visibility.WHITELISTED, Map.of("entriesJson", GSON.toJson(entries)));
    }

    /** Startup/config recovery publishes only each recipient's bounded local inbox. */
    private void publishLauncherSnapshots() {
        new ArrayList<>(notificationIdsByRecipient.keySet()).forEach(this::publishLauncherSnapshot);
    }

    private void index(ElarionStoredNotification notification) {
        if (notification == null || notification.recipientId() == null) return;
        notificationIdsByRecipient.computeIfAbsent(notification.recipientId(), ignored -> new LinkedHashSet<>())
                .add(notification.id());
    }

    private void remove(String notificationId) {
        ElarionStoredNotification removed = notifications.remove(notificationId);
        if (removed == null) return;
        LinkedHashSet<String> ids = notificationIdsByRecipient.get(removed.recipientId());
        if (ids == null) return;
        ids.remove(notificationId);
        if (ids.isEmpty()) notificationIdsByRecipient.remove(removed.recipientId());
    }

    private static String truncate(String value) {
        String clean = clean(value);
        return clean.substring(0, Math.min(clean.length(), MAX_LAUNCHER_TEXT));
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    @FunctionalInterface
    public interface NotificationActionHandler {
        ActionResult handle(ActionContext context);
    }

    public record ActionContext(
            ServerPlayerEntity player,
            ElarionStoredNotification notification,
            String actionId
    ) {}

    public record ActionResult(boolean success, String message, boolean resolve) {
        public ActionResult {
            message = message == null ? "" : message;
        }

        public static ActionResult success(String message, boolean resolve) {
            return new ActionResult(true, message, resolve);
        }

        public static ActionResult failure(String message) {
            return new ActionResult(false, message, false);
        }
    }
}
