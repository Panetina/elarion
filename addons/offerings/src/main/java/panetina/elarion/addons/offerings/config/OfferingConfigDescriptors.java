package panetina.elarion.addons.offerings.config;

import panetina.elarion.addons.offerings.model.OfferingProjectDefinition;
import panetina.elarion.addons.offerings.model.OfferingScope;
import panetina.elarion.addons.offerings.model.OfferingUiConfig;
import panetina.elarion.core.config.ElarionConfigCategory;
import panetina.elarion.core.config.ElarionConfigCodec;
import panetina.elarion.core.config.ElarionConfigDomain;
import panetina.elarion.core.config.ElarionConfigEntry;
import panetina.elarion.core.config.ElarionConfigPermission;
import panetina.elarion.core.config.ElarionConfigRegistry;
import panetina.elarion.core.config.ElarionConfigValidator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public final class OfferingConfigDescriptors {
    private OfferingConfigDescriptors() {
    }

    public static void register(
            ElarionConfigRegistry registry,
            Supplier<Collection<OfferingProjectDefinition>> projects,
            Supplier<OfferingUiConfig> ui
    ) {
        registry.registerDomain(domain(projects, ui));
    }

    public static ElarionConfigDomain domain(
            Supplier<Collection<OfferingProjectDefinition>> projects,
            Supplier<OfferingUiConfig> ui
    ) {
        List<OfferingProjectDefinition> snapshot = sortedProjects(projects);
        OfferingUiConfig uiSnapshot = safeUi(ui);
        return new ElarionConfigDomain(
                "offerings",
                "addons:offerings",
                "Offerings",
                "Shrine UI and reusable Offering project definitions.",
                List.of(
                        "config/elarion/addons/offerings/society.yml",
                        "config/elarion/addons/offerings/ui.yml",
                        "config/elarion/addons/offerings/projects/*.yml"),
                "/e offerings reload",
                List.of(
                        new ElarionConfigCategory(
                                "general",
                                "General",
                                "Offering project discovery and reserved society metadata.",
                                generalEntries(projects, snapshot)),
                        new ElarionConfigCategory(
                                "ui",
                                "Shrine UI",
                                "Shrine of Foundation layout and placeholder text.",
                                uiEntries(ui, uiSnapshot)),
                        new ElarionConfigCategory(
                                "projects",
                                "Projects",
                                "Current loaded project definition summaries.",
                                projectEntries(projects, snapshot))));
    }

    private static List<ElarionConfigEntry<?>> generalEntries(
            Supplier<Collection<OfferingProjectDefinition>> projects,
            List<OfferingProjectDefinition> snapshot
    ) {
        return List.of(
                stringEntry("society.status", "Society Status",
                        "Reserved society.yml surface; V1 does not parse this into a runtime model.",
                        "society.yml.society",
                        "reserved",
                        () -> "reserved"),
                intEntry("projects.count", "Project Count",
                        "Number of currently loaded Offering project definitions.",
                        "projects/*.yml",
                        snapshot.size(),
                        () -> sortedProjects(projects).size(),
                        1,
                        Integer.MAX_VALUE),
                stringEntry("projects.ids", "Project IDs",
                        "Comma-separated Offering project IDs currently known to Offerings.",
                        "projects/*.yml",
                        projectIds(snapshot),
                        () -> projectIds(sortedProjects(projects)),
                        false));
    }

    private static List<ElarionConfigEntry<?>> uiEntries(
            Supplier<OfferingUiConfig> ui,
            OfferingUiConfig snapshot
    ) {
        return List.of(
                stringEntry("ui.theme-variant", "Theme Variant",
                        "Shared Elarion UI theme variant used by Shrine screens.",
                        "ui.yml.theme-variant",
                        snapshot.themeVariant(),
                        () -> safeUi(ui).themeVariant()),
                intEntry("ui.logical-width", "Logical Width",
                        "Shrine UI logical width.",
                        "ui.yml.logical-width",
                        snapshot.logicalWidth(),
                        () -> safeUi(ui).logicalWidth(),
                        360,
                        960),
                intEntry("ui.logical-height", "Logical Height",
                        "Shrine UI logical height.",
                        "ui.yml.logical-height",
                        snapshot.logicalHeight(),
                        () -> safeUi(ui).logicalHeight(),
                        260,
                        720),
                intEntry("ui.minimum-scale-percent", "Minimum Scale Percent",
                        "Minimum Shrine UI scale percentage.",
                        "ui.yml.minimum-scale-percent",
                        snapshot.minimumScalePercent(),
                        () -> safeUi(ui).minimumScalePercent(),
                        25,
                        100),
                intEntry("ui.summary-width", "Summary Width",
                        "Shrine summary column width.",
                        "ui.yml.summary-width",
                        snapshot.summaryWidth(),
                        () -> safeUi(ui).summaryWidth(),
                        100,
                        Math.max(100, snapshot.logicalWidth() / 2)),
                intEntry("ui.tab-height", "Tab Height",
                        "Shrine tab height.",
                        "ui.yml.tab-height",
                        snapshot.tabHeight(),
                        () -> safeUi(ui).tabHeight(),
                        1,
                        Integer.MAX_VALUE),
                intEntry("ui.row-height", "Row Height",
                        "Shrine requirement/history row height.",
                        "ui.yml.row-height",
                        snapshot.rowHeight(),
                        () -> safeUi(ui).rowHeight(),
                        16,
                        64),
                intEntry("ui.icon-size", "Icon Size",
                        "Shrine reward icon size.",
                        "ui.yml.icon-size",
                        snapshot.iconSize(),
                        () -> safeUi(ui).iconSize(),
                        16,
                        96),
                intEntry("ui.close-button-width", "Close Button Width",
                        "Shrine close-button width.",
                        "ui.yml.close-button-width",
                        snapshot.closeButtonWidth(),
                        () -> safeUi(ui).closeButtonWidth(),
                        1,
                        Integer.MAX_VALUE),
                stringEntry("ui.rewards-placeholder", "Rewards Placeholder",
                        "Empty rewards text.",
                        "ui.yml.rewards-placeholder",
                        snapshot.rewardsPlaceholder(),
                        () -> safeUi(ui).rewardsPlaceholder(),
                        false),
                stringEntry("ui.history-placeholder", "History Placeholder",
                        "Empty donation-history text.",
                        "ui.yml.history-placeholder",
                        snapshot.historyPlaceholder(),
                        () -> safeUi(ui).historyPlaceholder(),
                        false),
                stringEntry("ui.contribution-placeholder", "Contribution Placeholder",
                        "Contribution prompt placeholder text.",
                        "ui.yml.contribution-placeholder",
                        snapshot.contributionPlaceholder(),
                        () -> safeUi(ui).contributionPlaceholder(),
                        false),
                stringEntry("ui.event-title", "Event Title",
                        "Shrine event panel title.",
                        "ui.yml.event-title",
                        snapshot.eventTitle(),
                        () -> safeUi(ui).eventTitle(),
                        false),
                stringEntry("ui.event-body", "Event Body",
                        "Shrine event panel body.",
                        "ui.yml.event-body",
                        snapshot.eventBody(),
                        () -> safeUi(ui).eventBody(),
                        false),
                stringEntry("ui.event-locked-body", "Event Locked Body",
                        "Shrine event locked-state body.",
                        "ui.yml.event-locked-body",
                        snapshot.eventLockedBody(),
                        () -> safeUi(ui).eventLockedBody(),
                        false));
    }

    private static List<ElarionConfigEntry<?>> projectEntries(
            Supplier<Collection<OfferingProjectDefinition>> projects,
            List<OfferingProjectDefinition> snapshot
    ) {
        List<ElarionConfigEntry<?>> entries = new ArrayList<>();
        for (OfferingProjectDefinition project : snapshot) {
            entries.add(projectStringEntry(project, "display-name", "Display Name",
                    "Project display name.",
                    projects, OfferingProjectDefinition::displayName));
            entries.add(projectStringEntry(project, "description", "Description",
                    "Project description.",
                    projects, OfferingProjectDefinition::description, false));
            entries.add(projectBoolEntry(project, "enabled", "Enabled",
                    "Whether this project definition can be used.",
                    projects, OfferingProjectDefinition::enabled));
            entries.add(projectStringEntry(project, "scope", "Scope",
                    "Project scope.",
                    projects, value -> value.scope().name().toLowerCase(java.util.Locale.ROOT),
                    scopeChoices()));
            entries.add(projectBoolEntry(project, "repeatable", "Repeatable",
                    "Whether completed instances may repeat.",
                    projects, OfferingProjectDefinition::repeatable));
            entries.add(projectBoolEntry(project, "allow-multiple-instances", "Allow Multiple Instances",
                    "Whether more than one runtime instance can exist.",
                    projects, OfferingProjectDefinition::allowMultipleInstances));
            entries.add(projectIntEntry(project, "requirements.count", "Requirement Count",
                    "Number of root-level configured requirements.",
                    projects, value -> value.requirements().size(), 0, Integer.MAX_VALUE));
            entries.add(projectIntEntry(project, "milestones.count", "Milestone Count",
                    "Number of root-level configured milestones.",
                    projects, value -> value.milestones().size(), 0, Integer.MAX_VALUE));
            entries.add(projectIntEntry(project, "levels.count", "Level Count",
                    "Number of configured project levels.",
                    projects, value -> value.levels().size(), 1, Integer.MAX_VALUE));
            entries.add(projectStringEntry(project, "first-level", "First Level",
                    "First configured project level ID.",
                    projects, value -> value.firstLevel().id()));
            entries.add(projectStringEntry(project, "presentation.level-text", "Presentation Text",
                    "Default project presentation level text.",
                    projects, value -> value.presentation().levelText()));
            entries.add(projectStringEntry(project, "presentation.icon", "Presentation Icon",
                    "Default project presentation icon.",
                    projects, value -> value.presentation().icon()));
        }
        return entries;
    }

    private static ElarionConfigEntry<Boolean> projectBoolEntry(
            OfferingProjectDefinition project,
            String field,
            String label,
            String description,
            Supplier<Collection<OfferingProjectDefinition>> projects,
            Function<OfferingProjectDefinition, Boolean> value
    ) {
        return boolEntry(projectId(project, field), projectLabel(project, label), description,
                projectPath(project, field), value.apply(project),
                () -> value.apply(currentProject(projects, project)));
    }

    private static ElarionConfigEntry<Integer> projectIntEntry(
            OfferingProjectDefinition project,
            String field,
            String label,
            String description,
            Supplier<Collection<OfferingProjectDefinition>> projects,
            Function<OfferingProjectDefinition, Integer> value,
            int minimum,
            int maximum
    ) {
        return intEntry(projectId(project, field), projectLabel(project, label), description,
                projectPath(project, field), value.apply(project),
                () -> value.apply(currentProject(projects, project)), minimum, maximum);
    }

    private static ElarionConfigEntry<String> projectStringEntry(
            OfferingProjectDefinition project,
            String field,
            String label,
            String description,
            Supplier<Collection<OfferingProjectDefinition>> projects,
            Function<OfferingProjectDefinition, String> value
    ) {
        return projectStringEntry(project, field, label, description, projects, value, List.of(), true);
    }

    private static ElarionConfigEntry<String> projectStringEntry(
            OfferingProjectDefinition project,
            String field,
            String label,
            String description,
            Supplier<Collection<OfferingProjectDefinition>> projects,
            Function<OfferingProjectDefinition, String> value,
            boolean nonBlank
    ) {
        return projectStringEntry(project, field, label, description, projects, value, List.of(), nonBlank);
    }

    private static ElarionConfigEntry<String> projectStringEntry(
            OfferingProjectDefinition project,
            String field,
            String label,
            String description,
            Supplier<Collection<OfferingProjectDefinition>> projects,
            Function<OfferingProjectDefinition, String> value,
            List<String> choices
    ) {
        return projectStringEntry(project, field, label, description, projects, value, choices, true);
    }

    private static ElarionConfigEntry<String> projectStringEntry(
            OfferingProjectDefinition project,
            String field,
            String label,
            String description,
            Supplier<Collection<OfferingProjectDefinition>> projects,
            Function<OfferingProjectDefinition, String> value,
            List<String> choices,
            boolean nonBlank
    ) {
        return stringEntry(
                projectId(project, field),
                projectLabel(project, label),
                description,
                projectPath(project, field),
                value.apply(project),
                () -> value.apply(currentProject(projects, project)),
                choices,
                nonBlank);
    }

    private static ElarionConfigEntry<Boolean> boolEntry(
            String id,
            String label,
            String description,
            String path,
            boolean defaultValue,
            Supplier<Boolean> currentValue
    ) {
        return new ElarionConfigEntry<>(
                id, label, description, path, ElarionConfigCodec.BOOLEAN, defaultValue, currentValue,
                ElarionConfigValidator.pass(), List.of("true", "false"), "", "",
                true, false, ElarionConfigPermission.OPERATOR, ElarionConfigPermission.OPERATOR);
    }

    private static ElarionConfigEntry<Integer> intEntry(
            String id,
            String label,
            String description,
            String path,
            int defaultValue,
            Supplier<Integer> currentValue,
            int minimum,
            int maximum
    ) {
        return new ElarionConfigEntry<>(
                id, label, description, path, ElarionConfigCodec.INTEGER, defaultValue, currentValue,
                ElarionConfigValidator.integerRange(path, minimum, maximum), List.of(),
                Integer.toString(minimum), maximum == Integer.MAX_VALUE ? "" : Integer.toString(maximum),
                true, false, ElarionConfigPermission.OPERATOR, ElarionConfigPermission.OPERATOR);
    }

    private static ElarionConfigEntry<String> stringEntry(
            String id,
            String label,
            String description,
            String path,
            String defaultValue,
            Supplier<String> currentValue
    ) {
        return stringEntry(id, label, description, path, defaultValue, currentValue, List.of(), true);
    }

    private static ElarionConfigEntry<String> stringEntry(
            String id,
            String label,
            String description,
            String path,
            String defaultValue,
            Supplier<String> currentValue,
            boolean nonBlank
    ) {
        return stringEntry(id, label, description, path, defaultValue, currentValue, List.of(), nonBlank);
    }

    private static ElarionConfigEntry<String> stringEntry(
            String id,
            String label,
            String description,
            String path,
            String defaultValue,
            Supplier<String> currentValue,
            List<String> choices,
            boolean nonBlank
    ) {
        return new ElarionConfigEntry<>(
                id, label, description, path, ElarionConfigCodec.STRING, defaultValue, currentValue,
                nonBlank ? ElarionConfigValidator.nonBlank(path) : ElarionConfigValidator.pass(),
                choices, "", "",
                true, false, ElarionConfigPermission.OPERATOR, ElarionConfigPermission.OPERATOR);
    }

    private static OfferingProjectDefinition currentProject(
            Supplier<Collection<OfferingProjectDefinition>> projects,
            OfferingProjectDefinition fallback
    ) {
        for (OfferingProjectDefinition project : sortedProjects(projects)) {
            if (project.id().equals(fallback.id())) return project;
        }
        return fallback;
    }

    private static List<OfferingProjectDefinition> sortedProjects(
            Supplier<Collection<OfferingProjectDefinition>> projects
    ) {
        Collection<OfferingProjectDefinition> value = projects == null ? null : projects.get();
        if (value == null) return List.of();
        return value.stream()
                .sorted(Comparator.comparing(OfferingProjectDefinition::id))
                .toList();
    }

    private static OfferingUiConfig safeUi(Supplier<OfferingUiConfig> ui) {
        OfferingUiConfig value = ui == null ? null : ui.get();
        return value == null ? OfferingUiConfig.defaults() : value;
    }

    private static String projectId(OfferingProjectDefinition project, String field) {
        return "projects." + project.id() + "." + field;
    }

    private static String projectPath(OfferingProjectDefinition project, String field) {
        return "projects/" + project.id() + ".yml." + field;
    }

    private static String projectLabel(OfferingProjectDefinition project, String fieldLabel) {
        return project.id() + " " + fieldLabel;
    }

    private static String projectIds(List<OfferingProjectDefinition> projects) {
        return projects.stream().map(OfferingProjectDefinition::id)
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }

    private static List<String> scopeChoices() {
        List<String> choices = new ArrayList<>();
        for (OfferingScope scope : OfferingScope.values()) {
            choices.add(scope.name().toLowerCase(java.util.Locale.ROOT));
        }
        return choices;
    }
}
