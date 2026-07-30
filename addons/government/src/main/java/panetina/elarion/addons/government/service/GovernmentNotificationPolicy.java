package panetina.elarion.addons.government.service;

import panetina.elarion.core.model.ElarionNotificationAction;
import panetina.elarion.core.service.ElarionNotificationService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

final class GovernmentNotificationPolicy {
    private static final String OPEN_CIVIC_FORUM_ACTION = "elarion_government:open_civic_forum";

    private GovernmentNotificationPolicy() {
    }

    static List<ElarionNotificationAction> actions(Map<String, String> metadata) {
        if (metadata != null && !metadata.getOrDefault("realmId", "").isBlank()) {
            return List.of(
                    new ElarionNotificationAction(OPEN_CIVIC_FORUM_ACTION, "Open Forum", true),
                    new ElarionNotificationAction(ElarionNotificationService.DISMISS, "Dismiss", true));
        }
        return List.of(new ElarionNotificationAction(ElarionNotificationService.DISMISS, "Dismiss", true));
    }

    static Set<UUID> initialProposalReviewers(
            String formId,
            String category,
            Set<UUID> presidentHolders,
            Set<UUID> proposalDecisionMakers
    ) {
        Set<UUID> selected = "republic".equals(formId) && "law".equals(category)
                ? presidentHolders
                : proposalDecisionMakers;
        return selected == null ? Set.of() : Set.copyOf(selected);
    }

    static String voteStageDedupe(String realmId, String dedupe) {
        return realmId + ":" + dedupe;
    }

    static String authorityReviewDedupe(String realmId, String dedupe, UUID holderId) {
        return realmId + ":" + dedupe + ":" + holderId;
    }
}
