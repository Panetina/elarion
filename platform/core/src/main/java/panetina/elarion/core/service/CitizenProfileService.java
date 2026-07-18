package panetina.elarion.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.RealmDefinition;
import panetina.elarion.core.model.TitleDefinition;
import panetina.elarion.core.model.profile.CitizenProfileCard;
import panetina.elarion.core.model.profile.CitizenProfileContributor;
import panetina.elarion.core.model.profile.CitizenProfileField;
import panetina.elarion.core.model.profile.CitizenProfileRequestContext;
import panetina.elarion.core.model.profile.CitizenProfileSection;
import panetina.elarion.core.model.profile.CitizenProfileSnapshot;
import panetina.elarion.core.model.profile.ProfileVisibility;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class CitizenProfileService {
    public static final int MAX_SECTIONS = 16;
    public static final int MAX_FIELDS_PER_SECTION = 24;
    public static final int MAX_CARDS_PER_SECTION = 8;

    private final CitizenService citizens;
    private final RealmService realms;
    private final TitleService titles;
    private final Logger logger;
    private final Map<String, CitizenProfileContributor> contributors = new LinkedHashMap<>();
    private final Set<String> failedContributorWarnings = ConcurrentHashMap.newKeySet();

    public CitizenProfileService(CitizenService citizens, RealmService realms, TitleService titles) {
        this(citizens, realms, titles, LoggerFactory.getLogger(CitizenProfileService.class));
    }

    public CitizenProfileService(CitizenService citizens, RealmService realms, TitleService titles, Logger logger) {
        this.citizens = Objects.requireNonNull(citizens, "citizens");
        this.realms = Objects.requireNonNull(realms, "realms");
        this.titles = Objects.requireNonNull(titles, "titles");
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    public synchronized void registerContributor(CitizenProfileContributor contributor) {
        Objects.requireNonNull(contributor, "contributor");
        String id = normalizeId(contributor.id());
        if (id.isBlank()) throw new IllegalArgumentException("Profile contributor id cannot be blank");
        if (contributors.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate profile contributor id: " + id);
        }
        contributors.put(id, contributor);
    }

    public Optional<CitizenProfileSnapshot> snapshot(CitizenProfileRequestContext context) {
        CitizenProfileRequestContext safeContext = Objects.requireNonNull(context, "context");
        return citizens.find(safeContext.targetId())
                .map(citizen -> snapshot(safeContext, citizen));
    }

    public CitizenProfileSnapshot snapshot(CitizenProfileRequestContext context, CitizenRecord target) {
        CitizenProfileRequestContext safeContext = Objects.requireNonNull(context, "context");
        CitizenRecord safeTarget = Objects.requireNonNull(target, "target");
        List<CitizenProfileSection> rawSections = new ArrayList<>();
        rawSections.add(identitySection(safeTarget));
        rawSections.add(realmSection(safeTarget));
        rawSections.add(titleSection(safeTarget));
        for (CitizenProfileContributor contributor : contributorSnapshot()) {
            String contributorId = normalizeId(contributor.id());
            try {
                List<CitizenProfileSection> sections = contributor.sections(safeContext, safeTarget);
                if (sections != null) rawSections.addAll(sections);
                failedContributorWarnings.remove(contributorId);
            } catch (RuntimeException exception) {
                if (failedContributorWarnings.add(contributorId)) {
                    logger.warn("Citizen profile contributor {} failed; repeated failures are suppressed until recovery",
                            contributorId, exception);
                }
            }
        }
        return new CitizenProfileSnapshot(
                safeTarget.uuid(),
                displayName(safeTarget),
                filterAndBound(safeContext, rawSections)
        );
    }

    public synchronized List<String> contributorIds() {
        return List.copyOf(contributors.keySet());
    }

    int failedContributorCount() {
        return failedContributorWarnings.size();
    }

    private CitizenProfileSection identitySection(CitizenRecord target) {
        return new CitizenProfileSection(
                "core.identity",
                "Identity",
                "elarion_core",
                ProfileVisibility.PUBLIC,
                List.of(
                        field("display-name", "Name", displayName(target), ProfileVisibility.PUBLIC),
                        field("status", "Status", target.status() == null ? "Unknown" : target.status().name(), ProfileVisibility.PUBLIC),
                        field("username", "Username", target.lastKnownUsername(), ProfileVisibility.SELF),
                        field("citizen-id", "Ember ID", target.uuid().toString(), ProfileVisibility.SELF),
                        field("joined-at", "Joined", time(target.joinedAt()), ProfileVisibility.SELF),
                        field("last-seen-at", "Last Seen", time(target.lastSeenAt()), ProfileVisibility.SELF),
                        field("citizenship", "Citizenship", citizenship(target), ProfileVisibility.PUBLIC),
                        field("civic-standing", "Civic Standing",
                                target.isRealmLeader() ? "Realm Leader" : "Ember", ProfileVisibility.PUBLIC),
                        field("abilities-granted", "Abilities", Integer.toString(target.grantedAbilities().size()),
                                ProfileVisibility.SELF)
                )
        );
    }

    private CitizenProfileSection realmSection(CitizenRecord target) {
        Optional<RealmDefinition> realm = realms.forCitizen(target);
        return new CitizenProfileSection(
                "core.realm",
                "Realm",
                "elarion_core",
                ProfileVisibility.PUBLIC,
                List.of(
                        field("realm-name", "Realm", realm.map(realms::displayName).orElse("Unassigned"), ProfileVisibility.PUBLIC),
                        field("realm-short-name", "Short Name", realm.map(realms::shortName).orElse(""), ProfileVisibility.PUBLIC),
                        field("realm-prefix", "Prefix", realm.map(realms::prefix).orElse(""), ProfileVisibility.PUBLIC),
                        field("realm-color", "Color", realm.map(realms::color).orElse(""), ProfileVisibility.PUBLIC),
                        field("realm-id", "Realm ID", realm.map(RealmDefinition::id).orElse(""), ProfileVisibility.SELF)
                )
        );
    }

    private CitizenProfileSection titleSection(CitizenRecord target) {
        Optional<TitleDefinition> title = titles.find(target.activeTitleId());
        return new CitizenProfileSection(
                "core.title",
                "Title",
                "elarion_core",
                ProfileVisibility.PUBLIC,
                List.of(
                        field("active-title", "Active Title", title.map(TitleDefinition::displayName).orElse("None"), ProfileVisibility.PUBLIC),
                        field("title-suffix", "Suffix", title.map(TitleDefinition::suffix).orElse(""), ProfileVisibility.PUBLIC),
                        field("title-id", "Title ID", title.map(TitleDefinition::id).orElse(""), ProfileVisibility.SELF),
                        field("titles-unlocked", "Titles Unlocked",
                                Integer.toString(target.unlockedTitleIds().size()), ProfileVisibility.PUBLIC)
                )
        );
    }

    private List<CitizenProfileSection> filterAndBound(
            CitizenProfileRequestContext context,
            List<CitizenProfileSection> rawSections
    ) {
        List<CitizenProfileSection> visible = new ArrayList<>();
        for (CitizenProfileSection section : rawSections) {
            if (section == null || !section.visibility().canView(context)) continue;
            List<CitizenProfileField> fields = section.fields().stream()
                    .filter(field -> field.visibility().canView(context))
                    .limit(MAX_FIELDS_PER_SECTION)
                    .toList();
            List<CitizenProfileCard> cards = section.cards().stream()
                    .filter(card -> card.visibility().canView(context))
                    .limit(MAX_CARDS_PER_SECTION)
                    .toList();
            if (fields.isEmpty() && cards.isEmpty()) continue;
            visible.add(new CitizenProfileSection(
                    section.id(),
                    section.title(),
                    section.sourceSystem(),
                    section.visibility(),
                    fields,
                    cards));
            if (visible.size() >= MAX_SECTIONS) break;
        }
        return List.copyOf(visible);
    }

    private synchronized List<CitizenProfileContributor> contributorSnapshot() {
        return List.copyOf(contributors.values());
    }

    private static CitizenProfileField field(String id, String label, String value, ProfileVisibility visibility) {
        return new CitizenProfileField(id, label, value, visibility);
    }

    private static String displayName(CitizenRecord target) {
        String nickname = target.nickname();
        if (nickname != null && !nickname.isBlank()) return nickname.trim();
        String username = target.lastKnownUsername();
        return username == null || username.isBlank() ? "Unknown Ember" : username.trim();
    }

    private static String citizenship(CitizenRecord target) {
        if (target.status() == null) return "Unknown";
        return switch (target.status()) {
            case ACTIVE -> "Active Ember";
            default -> titleCase(target.status().name());
        };
    }

    private static String titleCase(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replace('_', ' ');
        if (normalized.isBlank()) return "Unknown";
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }

    private static String time(long epochMillis) {
        return epochMillis <= 0L ? "" : Instant.ofEpochMilli(epochMillis).toString();
    }

    private static String normalizeId(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
