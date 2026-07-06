package panetina.elarion.addons.government.model;

import java.util.UUID;

public record GovernmentLawRecord(
        String id,
        String realmId,
        String sourceProposalId,
        String category,
        String title,
        String body,
        UUID enactedBy,
        long enactedAt,
        boolean active,
        long archivedAt,
        UUID archivedBy
) {
    public GovernmentLawRecord {
        id = clean(id);
        realmId = clean(realmId);
        sourceProposalId = clean(sourceProposalId);
        category = clean(category);
        title = title == null ? "" : title.trim();
        body = body == null ? "" : body.trim();
    }

    public static GovernmentLawRecord enact(
            String id,
            GovernmentProposalRecord proposal,
            String title,
            String body,
            UUID enactedBy,
            long enactedAt
    ) {
        return new GovernmentLawRecord(id, proposal.realmId(), proposal.id(), proposal.category(),
                title, body, enactedBy, enactedAt, true, 0L, null);
    }

    public static GovernmentLawRecord direct(
            String id,
            String realmId,
            String category,
            String title,
            String body,
            UUID enactedBy,
            long enactedAt
    ) {
        return new GovernmentLawRecord(id, realmId, "", category, title, body, enactedBy, enactedAt,
                true, 0L, null);
    }

    public GovernmentLawRecord archived(UUID actor, long archivedAt) {
        return new GovernmentLawRecord(id, realmId, sourceProposalId, category, title, body,
                enactedBy, enactedAt, false, archivedAt, actor);
    }

    public GovernmentLawRecord restored() {
        return new GovernmentLawRecord(id, realmId, sourceProposalId, category, title, body,
                enactedBy, enactedAt, true, 0L, null);
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
