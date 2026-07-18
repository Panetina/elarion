package panetina.elarion.core.integration.minecraft;

import org.slf4j.Logger;
import panetina.elarion.core.integration.minecraft.MinecraftProjectionProtocol.Visibility;
import panetina.elarion.core.model.CitizenRecord;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class AdvancementLeaderboardProjection {
    public static final int MAX_ENTRIES = 10;
    private final MinecraftProjectionPublisher projections;
    private final AdvancementLeaderboardStorage storage;
    private final Map<UUID, Entry> entries = new LinkedHashMap<>();
    private Path root;

    public AdvancementLeaderboardProjection(Logger logger, MinecraftProjectionPublisher projections) {
        this.projections = projections;
        this.storage = new AdvancementLeaderboardStorage(logger);
    }

    public synchronized void bind(Path elarionRoot) {
        root = elarionRoot;
        entries.clear();
        entries.putAll(storage.load(elarionRoot));
        publish();
    }

    public synchronized void update(CitizenRecord citizen, long completed) {
        if (citizen == null || root == null) return;
        entries.put(citizen.uuid(), new Entry(displayName(citizen), citizen.realmId(), Math.max(0L, completed)));
        storage.save(root, entries);
        publish();
    }

    List<Entry> leaders() {
        return entries.values().stream()
                .filter(entry -> entry.completed() > 0)
                .sorted(Comparator.comparingLong(Entry::completed).reversed()
                        .thenComparing(Entry::name, String.CASE_INSENSITIVE_ORDER))
                .limit(MAX_ENTRIES)
                .toList();
    }

    private void publish() {
        List<Entry> leaders = leaders();
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("label", "Advancement leaders");
        payload.put("value", leaders.isEmpty() ? "0" : Long.toString(leaders.getFirst().completed()));
        payload.put("displayValue", leaders.isEmpty() ? "Awaiting records"
                : leaders.getFirst().completed() + " completed");
        payload.put("description", "The highest verified advancement totals recorded by Fabric Core.");
        payload.put("entryCount", Integer.toString(leaders.size()));
        for (int index = 0; index < leaders.size(); index++) {
            Entry entry = leaders.get(index);
            String prefix = "rank" + (index + 1);
            payload.put(prefix + "Name", entry.name());
            payload.put(prefix + "Realm", entry.realmId());
            payload.put(prefix + "Value", Long.toString(entry.completed()));
        }
        projections.publishState("metric.advancement-leaderboard", "global", "", Visibility.PUBLIC, payload);
    }

    private static String displayName(CitizenRecord citizen) {
        String nickname = citizen.nickname();
        return nickname == null || nickname.isBlank() ? citizen.lastKnownUsername() : nickname;
    }

    public record Entry(String name, String realmId, long completed) {
        public Entry {
            name = name == null || name.isBlank() ? "Unknown citizen" : name.substring(0, Math.min(64, name.length()));
            realmId = realmId == null ? "" : realmId.substring(0, Math.min(64, realmId.length()));
            completed = Math.max(0L, completed);
        }

        Entry normalized() {
            return new Entry(name, realmId, completed);
        }
    }
}
