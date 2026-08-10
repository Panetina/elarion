package panetina.elarion.addons.guilds.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

/** Guild-owned aggregate of physical-Sigil donations; Economy owns the payment ledger. */
public record GuildProgression(long totalContributed, Map<UUID, Long> memberContributions,
                               List<GuildContributionReceipt> recentReceipts) {
    public GuildProgression {
        totalContributed = Math.max(0L, totalContributed);
        LinkedHashMap<UUID, Long> normalized = new LinkedHashMap<>();
        if (memberContributions != null) {
            memberContributions.forEach((member, amount) -> {
                if (member != null && amount != null && amount > 0L) normalized.put(member, amount);
            });
        }
        memberContributions = Map.copyOf(normalized);
        recentReceipts = recentReceipts == null ? List.of() : List.copyOf(recentReceipts.stream().limit(128).toList());
    }

    public GuildProgression contribute(UUID operationId, UUID memberId, long amount) {
        if (memberId == null || amount <= 0L) throw new IllegalArgumentException("Contribution must be positive.");
        GuildContributionReceipt existing = receipt(operationId);
        if (existing != null) {
            if (!existing.contributorId().equals(memberId) || existing.amount() != amount) {
                throw new IllegalArgumentException("Contribution operation does not match its original request.");
            }
            return this;
        }
        LinkedHashMap<UUID, Long> updated = new LinkedHashMap<>(memberContributions);
        updated.merge(memberId, amount, Math::addExact);
        ArrayList<GuildContributionReceipt> receipts = new ArrayList<>(recentReceipts);
        receipts.add(0, new GuildContributionReceipt(operationId, memberId, amount, System.currentTimeMillis()));
        if (receipts.size() > 128) receipts.subList(128, receipts.size()).clear();
        return new GuildProgression(Math.addExact(totalContributed, amount), updated, receipts);
    }

    public GuildContributionReceipt receipt(UUID operationId) {
        if (operationId == null) return null;
        return recentReceipts.stream().filter(receipt -> receipt.operationId().equals(operationId)).findFirst().orElse(null);
    }

    public static GuildProgression empty() {
        return new GuildProgression(0L, Map.of(), List.of());
    }
}
