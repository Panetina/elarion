package panetina.elarion.core.model.profile;

import java.util.Map;
import java.util.Set;

public final class CitizenProfileSummaryFields {
    public static final String SOURCE_PROGRESSION = "progression";
    public static final String SOURCE_OFFERINGS = "offerings";
    public static final String SOURCE_QUESTS = "quests";
    public static final String SOURCE_NPCS = "npcs";
    public static final String SOURCE_GROUPS = "groups";
    public static final String SOURCE_GOVERNMENT = "government";
    public static final String SOURCE_UNDERWORLD = "underworld";
    public static final String SOURCE_PORTALS = "portals";
    public static final String SOURCE_HISTORY = "history";
    public static final String SOURCE_MOUNTS = "mounts";

    public static final String FIELD_ADVANCEMENTS_COMPLETED = "advancements-completed";
    public static final String FIELD_MILESTONES = "milestones";
    public static final String FIELD_OFFERING_SCORE = "offering-score";
    public static final String FIELD_QUESTS_COMPLETED = "quests-completed";
    public static final String FIELD_REPUTATION = "reputation";
    public static final String FIELD_MEMBERSHIPS = "memberships";
    public static final String FIELD_ACTIVE_OFFICE = "active-office";
    public static final String FIELD_OFFICE_HISTORY = "office-history";
    public static final String FIELD_DEATHS = "deaths";
    public static final String FIELD_PORTAL_JOURNEYS = "journeys";
    public static final String FIELD_RECENT_SUMMARY = "recent-summary";
    public static final String FIELD_MOUNTS_UNLOCKED = "mounts-unlocked";

    public static final Map<String, Set<String>> RESERVED_BY_SOURCE = Map.of(
            SOURCE_PROGRESSION, Set.of(FIELD_ADVANCEMENTS_COMPLETED, FIELD_MILESTONES),
            SOURCE_OFFERINGS, Set.of(FIELD_OFFERING_SCORE),
            SOURCE_QUESTS, Set.of(FIELD_QUESTS_COMPLETED),
            SOURCE_NPCS, Set.of(FIELD_REPUTATION),
            SOURCE_GROUPS, Set.of(FIELD_MEMBERSHIPS),
            SOURCE_GOVERNMENT, Set.of(FIELD_ACTIVE_OFFICE, FIELD_OFFICE_HISTORY),
            SOURCE_UNDERWORLD, Set.of(FIELD_DEATHS),
            SOURCE_PORTALS, Set.of(FIELD_PORTAL_JOURNEYS),
            SOURCE_HISTORY, Set.of(FIELD_RECENT_SUMMARY),
            SOURCE_MOUNTS, Set.of(FIELD_MOUNTS_UNLOCKED)
    );

    private CitizenProfileSummaryFields() {
    }
}
