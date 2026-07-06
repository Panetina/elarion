package panetina.elarion.addons.government.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public record GovernmentProposalRecord(
        String id,
        String realmId,
        UUID authorId,
        String category,
        String title,
        String body,
        String finalTitle,
        String finalBody,
        GovernmentProposalStatus status,
        long createdAt,
        long resolvedAt,
        UUID resolvedBy,
        UUID sponsorId,
        Map<UUID, Boolean> reviewVotes,
        Map<UUID, Boolean> citizenVotes
) {
    public GovernmentProposalRecord {
        id = clean(id);
        realmId = clean(realmId);
        category = clean(category);
        title = title == null ? "" : title.trim();
        body = body == null ? "" : body.trim();
        finalTitle = finalTitle == null ? "" : finalTitle.trim();
        finalBody = finalBody == null ? "" : finalBody.trim();
        status = status == null ? GovernmentProposalStatus.PENDING : status;
        reviewVotes = reviewVotes == null ? Map.of() : Map.copyOf(new LinkedHashMap<>(reviewVotes));
        citizenVotes = citizenVotes == null ? Map.of() : Map.copyOf(new LinkedHashMap<>(citizenVotes));
    }

    public static GovernmentProposalRecord create(
            String id,
            String realmId,
            UUID authorId,
            String category,
            String title,
            String body,
            long createdAt
    ) {
        return new GovernmentProposalRecord(id, realmId, authorId, category, title, body,
                "", "", GovernmentProposalStatus.PENDING, createdAt, 0L, null, null, Map.of(), Map.of());
    }

    public GovernmentProposalRecord withVote(UUID voter, boolean approve) {
        Map<UUID, Boolean> votes = new LinkedHashMap<>(reviewVotes);
        if (voter != null) votes.put(voter, approve);
        return new GovernmentProposalRecord(id, realmId, authorId, category, title, body, finalTitle, finalBody, status,
                createdAt, resolvedAt, resolvedBy, sponsorId, votes, citizenVotes);
    }

    public GovernmentProposalRecord withCitizenVote(UUID voter, boolean approve) {
        Map<UUID, Boolean> votes = new LinkedHashMap<>(citizenVotes);
        if (voter != null) votes.put(voter, approve);
        return new GovernmentProposalRecord(id, realmId, authorId, category, title, body, finalTitle, finalBody, status,
                createdAt, resolvedAt, resolvedBy, sponsorId, reviewVotes, votes);
    }

    public GovernmentProposalRecord withStatus(GovernmentProposalStatus next, UUID resolver, long resolvedAt) {
        return new GovernmentProposalRecord(id, realmId, authorId, category, title, body, finalTitle, finalBody, next,
                createdAt, resolvedAt, resolver, sponsorId, reviewVotes, citizenVotes);
    }

    public GovernmentProposalRecord withStatusAndSponsor(
            GovernmentProposalStatus next,
            UUID resolver,
            UUID sponsor,
            long resolvedAt
    ) {
        return new GovernmentProposalRecord(id, realmId, authorId, category, title, body, finalTitle, finalBody, next,
                createdAt, resolvedAt, resolver, sponsor, reviewVotes, citizenVotes);
    }

    public GovernmentProposalRecord withStatusAndClearedReview(
            GovernmentProposalStatus next,
            UUID resolver,
            long resolvedAt
    ) {
        return new GovernmentProposalRecord(id, realmId, authorId, category, title, body, finalTitle, finalBody, next,
                createdAt, resolvedAt, resolver, sponsorId, Map.of(), citizenVotes);
    }

    public GovernmentProposalRecord withFinalText(String nextTitle, String nextBody, UUID resolver, long resolvedAt) {
        return new GovernmentProposalRecord(id, realmId, authorId, category, title, body, nextTitle, nextBody, status,
                createdAt, resolvedAt, resolver, sponsorId, Map.of(), citizenVotes);
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
