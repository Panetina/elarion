package panetina.elarion.addons.guilds.model;

import java.util.UUID;

/** Bounded replay receipt; Economy retains the canonical financial operation receipt. */
public record GuildContributionReceipt(UUID operationId, UUID contributorId, long amount, long createdAt) {
    public GuildContributionReceipt {
        if (operationId == null || contributorId == null || amount < 1L) {
            throw new IllegalArgumentException("A Guild contribution receipt is invalid.");
        }
        createdAt = createdAt <= 0L ? System.currentTimeMillis() : createdAt;
    }
}
