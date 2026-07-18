package panetina.elarion.addons.government.config;

import panetina.elarion.addons.government.model.GovernmentFormDefinition;
import panetina.elarion.addons.government.model.GovernmentSettings;
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

public final class GovernmentConfigDescriptors {
    private GovernmentConfigDescriptors() {
    }

    public static void register(
            ElarionConfigRegistry registry,
            Supplier<GovernmentSettings> settings,
            Supplier<Collection<GovernmentFormDefinition>> forms
    ) {
        registry.registerDomain(domain(settings, forms));
    }

    public static ElarionConfigDomain domain(
            Supplier<GovernmentSettings> settings,
            Supplier<Collection<GovernmentFormDefinition>> forms
    ) {
        GovernmentSettings settingsSnapshot = safeSettings(settings);
        List<GovernmentFormDefinition> formSnapshot = sortedForms(forms);
        return new ElarionConfigDomain(
                "government",
                "addons:government",
                "Government",
                "Government authority settings and form definitions.",
                List.of(
                        "config/elarion/addons/government/government.yml",
                        "config/elarion/addons/government/forms/*/form.yml"),
                "/e government reload",
                List.of(
                        new ElarionConfigCategory(
                                "settings",
                                "Settings",
                                "Government-wide authority settings.",
                                settingsEntries(settings, settingsSnapshot)),
                        new ElarionConfigCategory(
                                "forms",
                                "Forms",
                                "Current loaded government form summaries.",
                                formEntries(forms, formSnapshot))));
    }

    private static List<ElarionConfigEntry<?>> settingsEntries(
            Supplier<GovernmentSettings> settings,
            GovernmentSettings snapshot
    ) {
        return List.of(
                intEntry("authority.inactivity-days", "Authority Inactivity Days",
                        "Days after which inactive authority holders can be cleaned up.",
                        "government.yml.authority.inactivity-days",
                        snapshot.authorityInactivityDays(),
                        () -> safeSettings(settings).authorityInactivityDays(),
                        1,
                        Integer.MAX_VALUE),
                intEntry("authority.inactivity-check-interval-seconds", "Authority Check Interval Seconds",
                        "Seconds between authority inactivity checks.",
                        "government.yml.authority.inactivity-check-interval-seconds",
                        snapshot.authorityInactivityCheckIntervalSeconds(),
                        () -> safeSettings(settings).authorityInactivityCheckIntervalSeconds(),
                        60,
                        Integer.MAX_VALUE));
    }

    private static List<ElarionConfigEntry<?>> formEntries(
            Supplier<Collection<GovernmentFormDefinition>> forms,
            List<GovernmentFormDefinition> snapshot
    ) {
        List<ElarionConfigEntry<?>> entries = new ArrayList<>();
        entries.add(intEntry("forms.count", "Form Count",
                "Number of currently loaded government forms.",
                "forms/*/form.yml",
                snapshot.size(),
                () -> sortedForms(forms).size(),
                1,
                Integer.MAX_VALUE));
        entries.add(stringEntry("forms.ids", "Form IDs",
                "Comma-separated government form IDs currently known to Government.",
                "forms/*/form.yml",
                formIds(snapshot),
                () -> formIds(sortedForms(forms)),
                false));
        for (GovernmentFormDefinition form : snapshot) {
            entries.add(formStringEntry(form, "display-name", "Display Name",
                    "Government form display name.",
                    forms, GovernmentFormDefinition::displayName));
            entries.add(formStringEntry(form, "description", "Description",
                    "Government form description.",
                    forms, GovernmentFormDefinition::description, false));
            entries.add(formBoolEntry(form, "enabled", "Enabled",
                    "Whether this government form is available.",
                    forms, GovernmentFormDefinition::enabled));
            entries.add(formStringEntry(form, "official-name-template", "Official Name Template",
                    "Template used to render the Realm's official government name.",
                    forms, GovernmentFormDefinition::officialNameTemplate));
            entries.add(formStringEntry(form, "authority-offices", "Authority Offices",
                    "Comma-separated offices treated as authority holders.",
                    forms, value -> String.join(", ", value.authorityOffices()), false));
            entries.add(formIntEntry(form, "offices.count", "Office Count",
                    "Number of offices in this form.",
                    forms, value -> value.offices().size(), 0, Integer.MAX_VALUE));
            entries.add(formStringEntry(form, "offices.ids", "Office IDs",
                    "Comma-separated office IDs in this form.",
                    forms, GovernmentConfigDescriptors::officeIds, false));
            entries.add(formStringEntry(form, "offices.max-holders", "Office Holder Limits",
                    "Office IDs and max holder counts.",
                    forms, GovernmentConfigDescriptors::officeHolderLimits, false));
            entries.add(formIntEntry(form, "actions.count", "Action Group Count",
                    "Number of configured action groups.",
                    forms, value -> value.actions().size(), 0, Integer.MAX_VALUE));
            entries.add(formStringEntry(form, "actions.keys", "Action Groups",
                    "Comma-separated configured action group keys.",
                    forms, value -> keys(value.actions().keySet()), false));
            entries.add(formIntEntry(form, "transitions.count", "Transition Count",
                    "Number of configured transitions.",
                    forms, value -> value.transitions().size(), 0, Integer.MAX_VALUE));
            entries.add(formStringEntry(form, "transitions.keys", "Transitions",
                    "Comma-separated configured transition keys.",
                    forms, value -> keys(value.transitions().keySet()), false));
        }
        return entries;
    }

    private static ElarionConfigEntry<Boolean> formBoolEntry(
            GovernmentFormDefinition form,
            String field,
            String label,
            String description,
            Supplier<Collection<GovernmentFormDefinition>> forms,
            Function<GovernmentFormDefinition, Boolean> value
    ) {
        return boolEntry(formId(form, field), formLabel(form, label), description,
                formPath(form, field), value.apply(form),
                () -> value.apply(currentForm(forms, form)));
    }

    private static ElarionConfigEntry<Integer> formIntEntry(
            GovernmentFormDefinition form,
            String field,
            String label,
            String description,
            Supplier<Collection<GovernmentFormDefinition>> forms,
            Function<GovernmentFormDefinition, Integer> value,
            int minimum,
            int maximum
    ) {
        return intEntry(formId(form, field), formLabel(form, label), description,
                formPath(form, field), value.apply(form),
                () -> value.apply(currentForm(forms, form)), minimum, maximum);
    }

    private static ElarionConfigEntry<String> formStringEntry(
            GovernmentFormDefinition form,
            String field,
            String label,
            String description,
            Supplier<Collection<GovernmentFormDefinition>> forms,
            Function<GovernmentFormDefinition, String> value
    ) {
        return formStringEntry(form, field, label, description, forms, value, true);
    }

    private static ElarionConfigEntry<String> formStringEntry(
            GovernmentFormDefinition form,
            String field,
            String label,
            String description,
            Supplier<Collection<GovernmentFormDefinition>> forms,
            Function<GovernmentFormDefinition, String> value,
            boolean nonBlank
    ) {
        return stringEntry(
                formId(form, field),
                formLabel(form, label),
                description,
                formPath(form, field),
                value.apply(form),
                () -> value.apply(currentForm(forms, form)),
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
            Supplier<String> currentValue,
            boolean nonBlank
    ) {
        return new ElarionConfigEntry<>(
                id, label, description, path, ElarionConfigCodec.STRING, defaultValue, currentValue,
                nonBlank ? ElarionConfigValidator.nonBlank(path) : ElarionConfigValidator.pass(),
                List.of(), "", "",
                true, false, ElarionConfigPermission.OPERATOR, ElarionConfigPermission.OPERATOR);
    }

    private static GovernmentFormDefinition currentForm(
            Supplier<Collection<GovernmentFormDefinition>> forms,
            GovernmentFormDefinition fallback
    ) {
        for (GovernmentFormDefinition form : sortedForms(forms)) {
            if (form.id().equals(fallback.id())) return form;
        }
        return fallback;
    }

    private static GovernmentSettings safeSettings(Supplier<GovernmentSettings> settings) {
        GovernmentSettings value = settings == null ? null : settings.get();
        return value == null ? GovernmentSettings.defaults() : value;
    }

    private static List<GovernmentFormDefinition> sortedForms(
            Supplier<Collection<GovernmentFormDefinition>> forms
    ) {
        Collection<GovernmentFormDefinition> value = forms == null ? null : forms.get();
        if (value == null) return List.of();
        return value.stream()
                .sorted(Comparator.comparing(GovernmentFormDefinition::id))
                .toList();
    }

    private static String formId(GovernmentFormDefinition form, String field) {
        return "forms." + form.id() + "." + field;
    }

    private static String formPath(GovernmentFormDefinition form, String field) {
        return "forms/" + form.id() + "/form.yml." + field;
    }

    private static String formLabel(GovernmentFormDefinition form, String fieldLabel) {
        return form.id() + " " + fieldLabel;
    }

    private static String formIds(List<GovernmentFormDefinition> forms) {
        return forms.stream().map(GovernmentFormDefinition::id)
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }

    private static String officeIds(GovernmentFormDefinition form) {
        return form.offices().stream()
                .map(office -> office.id())
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }

    private static String officeHolderLimits(GovernmentFormDefinition form) {
        return form.offices().stream()
                .map(office -> office.id() + "=" + office.maxHolders())
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }

    private static String keys(Collection<String> values) {
        return values.stream().sorted().reduce((left, right) -> left + ", " + right).orElse("");
    }
}
