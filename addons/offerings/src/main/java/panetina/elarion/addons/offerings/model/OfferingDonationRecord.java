package panetina.elarion.addons.offerings.model;

import java.util.UUID;

public record OfferingDonationRecord(
        UUID contributorId,
        String contributorName,
        String levelId,
        String requirementKey,
        String type,
        long amount,
        long createdAt
) {
    public OfferingDonationRecord(
            UUID contributorId,
            String contributorName,
            String requirementKey,
            String type,
            long amount,
            long createdAt
    ) {
        this(contributorId, contributorName, "", requirementKey, type, amount, createdAt);
    }

    public OfferingDonationRecord {
        contributorName = contributorName == null ? "" : contributorName;
        levelId = levelId == null ? "" : levelId;
        requirementKey = requirementKey == null ? "" : requirementKey;
        type = type == null ? "" : type;
        createdAt = createdAt <= 0 ? System.currentTimeMillis() : createdAt;
    }
}
