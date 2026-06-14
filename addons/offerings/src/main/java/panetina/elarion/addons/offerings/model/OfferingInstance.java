package panetina.elarion.addons.offerings.model;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record OfferingInstance(
        String id,
        String projectId,
        String activeLevelId,
        OfferingScope scope,
        String realmId,
        String worldId,
        int x,
        int y,
        int z,
        String anchorId,
        Map<String, Long> progress,
        Map<String, Long> contributorTotals,
        Set<String> completedMilestones,
        long createdAt,
        long completedAt
) {
    public OfferingInstance(
            String id,
            String projectId,
            OfferingScope scope,
            String realmId,
            String worldId,
            int x,
            int y,
            int z,
            String anchorId,
            Map<String, Long> progress,
            Map<String, Long> contributorTotals,
            Set<String> completedMilestones,
            long createdAt,
            long completedAt
    ) {
        this(id, projectId, "", scope, realmId, worldId, x, y, z, anchorId,
                progress, contributorTotals, completedMilestones, createdAt, completedAt);
    }

    public OfferingInstance {
        id = id == null ? "" : id;
        projectId = projectId == null ? "" : projectId;
        activeLevelId = activeLevelId == null ? "" : activeLevelId;
        scope = scope == null ? OfferingScope.REALM : scope;
        realmId = realmId == null ? "" : realmId;
        worldId = worldId == null ? "" : worldId;
        anchorId = anchorId == null ? "" : anchorId;
        progress = progress == null ? new LinkedHashMap<>() : new LinkedHashMap<>(progress);
        contributorTotals = contributorTotals == null ? new LinkedHashMap<>() : new LinkedHashMap<>(contributorTotals);
        completedMilestones = completedMilestones == null ? new LinkedHashSet<>() : new LinkedHashSet<>(completedMilestones);
        createdAt = createdAt <= 0 ? System.currentTimeMillis() : createdAt;
    }

    public boolean completed() {
        return completedAt > 0;
    }

    public OfferingInstance withProgress(String key, long amount, UUID contributor) {
        Map<String, Long> nextProgress = new LinkedHashMap<>(progress);
        nextProgress.merge(key, amount, Long::sum);
        Map<String, Long> nextContributors = new LinkedHashMap<>(contributorTotals);
        if (contributor != null) {
            nextContributors.merge(contributor.toString(), amount, Long::sum);
        }
        return new OfferingInstance(id, projectId, activeLevelId, scope, realmId, worldId, x, y, z, anchorId,
                nextProgress, nextContributors, completedMilestones, createdAt, completedAt);
    }

    public OfferingInstance withCompletion(long time, Set<String> milestones) {
        return new OfferingInstance(id, projectId, activeLevelId, scope, realmId, worldId, x, y, z, anchorId,
                progress, contributorTotals, milestones, createdAt, time);
    }

    public OfferingInstance withCompletedMilestones(Set<String> milestones) {
        return new OfferingInstance(id, projectId, activeLevelId, scope, realmId, worldId, x, y, z, anchorId,
                progress, contributorTotals, milestones, createdAt, completedAt);
    }

    public OfferingInstance withAnchor(String nextAnchorId) {
        return new OfferingInstance(id, projectId, activeLevelId, scope, realmId, worldId, x, y, z, nextAnchorId,
                progress, contributorTotals, completedMilestones, createdAt, completedAt);
    }

    public OfferingInstance advanceToLevel(String nextLevelId) {
        return new OfferingInstance(id, projectId, nextLevelId, scope, realmId, worldId, x, y, z, anchorId,
                Map.of(), contributorTotals, Set.of(), createdAt, 0L);
    }

    public OfferingInstance reset() {
        return new OfferingInstance(id, projectId, activeLevelId, scope, realmId, worldId, x, y, z, anchorId,
                Map.of(), Map.of(), Set.of(), createdAt, 0L);
    }
}
