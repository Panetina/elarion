package panetina.elarion.core.service;

import net.minecraft.server.MinecraftServer;
import panetina.elarion.core.event.ElarionEventBus;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.HistoryEvent;
import panetina.elarion.core.storage.HistoryStorage;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

public final class HistoryService {
    private final HistoryStorage storage;
    private MinecraftServer server;

    public HistoryService(HistoryStorage storage, ElarionEventBus events) {
        this.storage = storage;
        events.onCitizenChanged(this::recordCitizenChange);
        events.onProgression(this::recordProgression);
    }

    public void bind(MinecraftServer server) {
        this.server = server;
    }

    public HistoryEvent record(HistoryEvent event) {
        if (server == null) throw new IllegalStateException("HistoryService is not bound to a server");
        storage.append(server, event);
        return event;
    }

    public HistoryEvent record(
            String category,
            String type,
            UUID actorId,
            String subjectType,
            String subjectId,
            String communityId,
            Map<String, String> metadata
    ) {
        return record(HistoryEvent.create(
                category, type, actorId, subjectType, subjectId, communityId, metadata));
    }

    public List<HistoryEvent> recent(int limit) {
        return query(event -> true, limit);
    }

    public List<HistoryEvent> forPlayer(UUID playerId, int limit) {
        String id = playerId.toString();
        return query(event -> playerId.equals(event.actorId())
                || event.subjectType().equals("player") && event.subjectId().equals(id), limit);
    }

    public List<HistoryEvent> forCommunity(String communityId, int limit) {
        return query(event -> event.communityId().equalsIgnoreCase(communityId), limit);
    }

    public List<HistoryEvent> forCategory(String category, int limit) {
        return query(event -> event.category().equalsIgnoreCase(category), limit);
    }

    public List<HistoryEvent> query(Predicate<HistoryEvent> filter, int limit) {
        if (server == null) throw new IllegalStateException("HistoryService is not bound to a server");
        int safeLimit = Math.max(1, Math.min(limit, 100));
        return storage.loadAll(server).stream().filter(filter).limit(safeLimit).toList();
    }

    private void recordCitizenChange(ElarionEventBus.CitizenChanged event) {
        if (server == null) return;
        CitizenRecord citizen = event.citizen();
        Map<String, String> metadata = new LinkedHashMap<>();
        put(metadata, "username", citizen.lastKnownUsername());
        put(metadata, "nickname", citizen.nickname());
        put(metadata, "title", citizen.titleId());
        put(metadata, "status", citizen.status().name());
        record("citizen", normalize(event.reason()), null, "player",
                event.citizenId().toString(), citizen.communityId(), metadata);
    }

    private void recordProgression(ElarionEventBus.ProgressionEvent event) {
        if (server == null) return;
        record("progression", normalize(event.eventId()), event.actorId(), "progression",
                event.subjectId(), "", Map.of("event", event.eventId()));
    }

    private static void put(Map<String, String> values, String key, String value) {
        if (value != null && !value.isBlank()) values.put(key, value);
    }

    private static String normalize(String value) {
        return value == null ? "event" : value.trim().toLowerCase(Locale.ROOT).replace('_', '-');
    }
}
