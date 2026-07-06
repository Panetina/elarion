package panetina.elarion.addons.government.model;

import java.util.UUID;

public record GovernmentOfficeTermRecord(
        String realmId,
        String officeId,
        UUID holderId,
        long chosenAt,
        long removedAt,
        long approvedCount,
        long rejectedCount
) {
    public GovernmentOfficeTermRecord {
        realmId = clean(realmId);
        officeId = clean(officeId);
        chosenAt = Math.max(0L, chosenAt);
        removedAt = Math.max(0L, removedAt);
        approvedCount = Math.max(0L, approvedCount);
        rejectedCount = Math.max(0L, rejectedCount);
    }

    public static GovernmentOfficeTermRecord active(String realmId, String officeId, UUID holderId, long chosenAt) {
        return new GovernmentOfficeTermRecord(realmId, officeId, holderId, chosenAt, 0L, 0L, 0L);
    }

    public GovernmentOfficeTermRecord withRemovedAt(long time) {
        return new GovernmentOfficeTermRecord(realmId, officeId, holderId, chosenAt, time,
                approvedCount, rejectedCount);
    }

    public GovernmentOfficeTermRecord withDecision(boolean approved) {
        return new GovernmentOfficeTermRecord(realmId, officeId, holderId, chosenAt, removedAt,
                approved ? approvedCount + 1L : approvedCount,
                approved ? rejectedCount : rejectedCount + 1L);
    }

    public boolean active() {
        return removedAt <= 0L;
    }

    public String key() {
        return key(realmId, officeId, holderId, chosenAt);
    }

    public static String key(String realmId, String officeId, UUID holderId, long chosenAt) {
        return clean(realmId) + "|" + clean(officeId) + "|" + (holderId == null ? "" : holderId)
                + "|" + Math.max(0L, chosenAt);
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
