package panetina.elarion.core.service;

import panetina.elarion.core.model.CharacterLifecycleRecord;
import panetina.elarion.core.model.CharacterLifecycleStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/** Runtime-only projection of lifecycle records that require periodic work. */
final class CharacterLifecycleWorkIndex {
    private final Map<String, CharacterLifecycleRecord> pendingResets = new LinkedHashMap<>();
    private final NavigableMap<Long, LinkedHashMap<String, CharacterLifecycleRecord>> cooldowns = new TreeMap<>();
    private final Map<String, Long> cooldownDeadlineByAccount = new LinkedHashMap<>();

    void rebuild(Collection<CharacterLifecycleRecord> records) {
        clear();
        if (records != null) records.forEach(this::update);
    }

    void update(CharacterLifecycleRecord record) {
        if (record == null || record.accountId == null || record.accountId.isBlank()) return;
        remove(record.accountId);
        if (record.status == CharacterLifecycleStatus.RESETTING) {
            pendingResets.put(record.accountId, record);
        } else if (record.status == CharacterLifecycleStatus.TRUE_DEAD_COOLDOWN) {
            cooldownDeadlineByAccount.put(record.accountId, record.eligibleAt);
            cooldowns.computeIfAbsent(record.eligibleAt, ignored -> new LinkedHashMap<>())
                    .put(record.accountId, record);
        }
    }

    List<CharacterLifecycleRecord> pendingResets() {
        return List.copyOf(pendingResets.values());
    }

    List<CharacterLifecycleRecord> pollDueCooldowns(long now) {
        List<CharacterLifecycleRecord> due = new ArrayList<>();
        while (!cooldowns.isEmpty() && cooldowns.firstKey() <= now) {
            Map.Entry<Long, LinkedHashMap<String, CharacterLifecycleRecord>> entry = cooldowns.pollFirstEntry();
            entry.getValue().forEach((accountId, record) -> {
                Long deadline = cooldownDeadlineByAccount.get(accountId);
                if (deadline != null && deadline.equals(entry.getKey())) {
                    cooldownDeadlineByAccount.remove(accountId);
                    due.add(record);
                }
            });
        }
        return List.copyOf(due);
    }

    void remove(String accountId) {
        if (accountId == null || accountId.isBlank()) return;
        pendingResets.remove(accountId);
        Long deadline = cooldownDeadlineByAccount.remove(accountId);
        if (deadline == null) return;
        LinkedHashMap<String, CharacterLifecycleRecord> bucket = cooldowns.get(deadline);
        if (bucket == null) return;
        bucket.remove(accountId);
        if (bucket.isEmpty()) cooldowns.remove(deadline);
    }

    void clear() {
        pendingResets.clear();
        cooldowns.clear();
        cooldownDeadlineByAccount.clear();
    }

    int pendingResetCount() {
        return pendingResets.size();
    }

    int scheduledCooldownCount() {
        return cooldownDeadlineByAccount.size();
    }
}
