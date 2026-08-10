package panetina.elarion.addons.guilds.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Guild-owned aggregate of physical-Sigil donations; Economy owns the payment ledger. */
public record GuildProgression(long totalContributed, Map<UUID, Long> memberContributions) {
    public GuildProgression {
        totalContributed = Math.max(0L, totalContributed);
        LinkedHashMap<UUID, Long> normalized = new LinkedHashMap<>();
        if (memberContributions != null) {
            memberContributions.forEach((member, amount) -> {
                if (member != null && amount != null && amount > 0L) normalized.put(member, amount);
            });
        }
        memberContributions = Map.copyOf(normalized);
    }

    public GuildProgression contribute(UUID memberId, long amount) {
        if (memberId == null || amount <= 0L) throw new IllegalArgumentException("Contribution must be positive.");
        LinkedHashMap<UUID, Long> updated = new LinkedHashMap<>(memberContributions);
        updated.merge(memberId, amount, Math::addExact);
        return new GuildProgression(Math.addExact(totalContributed, amount), updated);
    }

    public static GuildProgression empty() {
        return new GuildProgression(0L, Map.of());
    }
}
