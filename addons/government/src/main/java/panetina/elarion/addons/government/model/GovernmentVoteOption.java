package panetina.elarion.addons.government.model;

import java.util.UUID;

public final class GovernmentVoteOption {
    public String id = "";
    public String title = "";
    public String body = "";
    public String tag = "";
    public String formId = "";
    public String officeId = "";
    public String groupId = "";
    public UUID candidateId;
    public UUID proposedBy;
    public long createdAt;

    public GovernmentVoteOption() {
    }

    public static GovernmentVoteOption realmName(
            String id,
            String displayName,
            String tag,
            UUID proposedBy
    ) {
        GovernmentVoteOption option = new GovernmentVoteOption();
        option.id = id;
        option.title = displayName;
        option.body = "Public tag: " + tag;
        option.tag = tag;
        option.proposedBy = proposedBy;
        option.createdAt = System.currentTimeMillis();
        return option;
    }

    public static GovernmentVoteOption governmentForm(String id, String title, String body) {
        GovernmentVoteOption option = new GovernmentVoteOption();
        option.id = id;
        option.title = title;
        option.body = body;
        option.formId = id;
        option.createdAt = System.currentTimeMillis();
        return option;
    }

    public static GovernmentVoteOption candidate(
            String id,
            String title,
            String body,
            String officeId,
            UUID candidateId,
            String groupId
    ) {
        GovernmentVoteOption option = new GovernmentVoteOption();
        option.id = id;
        option.title = title;
        option.body = body;
        option.officeId = officeId;
        option.candidateId = candidateId;
        option.groupId = groupId == null ? "" : groupId;
        option.proposedBy = candidateId;
        option.createdAt = System.currentTimeMillis();
        return option;
    }

    public String officeId() {
        return officeId;
    }

    public String id() {
        return id;
    }

    public String formId() {
        return formId;
    }

    public String groupId() {
        return groupId;
    }

    public UUID candidateId() {
        return candidateId;
    }
}
