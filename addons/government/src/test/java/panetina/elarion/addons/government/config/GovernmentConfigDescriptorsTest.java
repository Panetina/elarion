package panetina.elarion.addons.government.config;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.government.model.GovernmentFormDefinition;
import panetina.elarion.addons.government.model.GovernmentOfficeDefinition;
import panetina.elarion.addons.government.model.GovernmentSettings;
import panetina.elarion.core.config.ElarionConfigDomain;
import panetina.elarion.core.config.ElarionConfigEntry;
import panetina.elarion.core.config.ElarionConfigRegistry;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class GovernmentConfigDescriptorsTest {
    @Test
    void registersGovernmentDomain() {
        ElarionConfigRegistry registry = new ElarionConfigRegistry();

        GovernmentConfigDescriptors.register(registry, this::settings, this::forms);

        ElarionConfigDomain domain = registry.domain("government").orElseThrow();
        assertEquals("government", domain.id());
        assertEquals("addons:government", domain.ownerModule());
        assertEquals("Government", domain.label());
        assertEquals("/e government reload", domain.reloadCommand());
        assertEquals(List.of(
                "config/elarion/addons/government/government.yml",
                "config/elarion/addons/government/forms/*/form.yml"), domain.files());
        assertTrue(domain.category("settings").isPresent());
        assertTrue(domain.category("forms").isPresent());
    }

    @Test
    void domainExposesSettingsAndFormSnapshotValues() {
        ElarionConfigDomain domain = GovernmentConfigDescriptors.domain(this::settings, this::forms);

        ElarionConfigEntry<?> inactivityDays = domain.entry(
                "settings", "authority.inactivity-days").orElseThrow();
        assertEquals(10, inactivityDays.defaultValue());
        assertEquals(10, inactivityDays.currentValue());
        assertEquals("1", inactivityDays.minimum());

        ElarionConfigEntry<?> checkInterval = domain.entry(
                "settings", "authority.inactivity-check-interval-seconds").orElseThrow();
        assertEquals(900, checkInterval.defaultValue());
        assertEquals(900, checkInterval.currentValue());
        assertEquals("60", checkInterval.minimum());

        assertEquals(2, domain.entry("forms", "forms.count").orElseThrow().currentValue());
        assertEquals("monarchy, republic", domain.entry("forms", "forms.ids").orElseThrow().currentValue());
        assertEquals("Monarchy", domain.entry("forms", "forms.monarchy.display-name").orElseThrow().currentValue());
        assertEquals("monarch, heir",
                domain.entry("forms", "forms.monarchy.authority-offices").orElseThrow().currentValue());
        assertEquals(3, domain.entry("forms", "forms.monarchy.offices.count").orElseThrow().currentValue());
        assertEquals("heir=1, monarch=1, officer=3",
                domain.entry("forms", "forms.monarchy.offices.max-holders").orElseThrow().currentValue());
        assertEquals(2, domain.entry("forms", "forms.monarchy.actions.count").orElseThrow().currentValue());
        assertEquals(1, domain.entry("forms", "forms.monarchy.transitions.count").orElseThrow().currentValue());
    }

    @Test
    void formEntriesReadCurrentSupplierValues() {
        AtomicReference<List<GovernmentFormDefinition>> current = new AtomicReference<>(forms());
        ElarionConfigDomain domain = GovernmentConfigDescriptors.domain(this::settings, current::get);

        ElarionConfigEntry<?> displayName = domain.entry(
                "forms", "forms.monarchy.display-name").orElseThrow();
        assertEquals("Monarchy", displayName.currentValue());

        current.set(List.of(monarchy("Crown Rule"), republic()));

        assertEquals("Crown Rule", displayName.currentValue());
    }

    private GovernmentSettings settings() {
        return new GovernmentSettings(10, 900);
    }

    private List<GovernmentFormDefinition> forms() {
        return List.of(republic(), monarchy("Monarchy"));
    }

    private GovernmentFormDefinition monarchy(String displayName) {
        return new GovernmentFormDefinition(
                "monarchy",
                displayName,
                "Royal rule.",
                true,
                "%realm% Crown",
                List.of("monarch", "heir"),
                false,
                List.of(
                        new GovernmentOfficeDefinition("heir", "Heir", "Next in line.", 1),
                        new GovernmentOfficeDefinition("monarch", "Monarch", "Rules.", 1),
                        new GovernmentOfficeDefinition("officer", "Officer", "Enforcement.", 3)),
                Map.of(
                        "succession", List.of("appoint_heir"),
                        "records", List.of("create_law")),
                Map.of("founding", "monarch_election"));
    }

    private GovernmentFormDefinition republic() {
        return new GovernmentFormDefinition(
                "republic",
                "Republic",
                "Ember government.",
                true,
                "%realm% Republic",
                List.of("president", "council_member"),
                false,
                List.of(
                        new GovernmentOfficeDefinition("president", "President", "Leads.", 1),
                        new GovernmentOfficeDefinition("council_member", "Council Member", "Votes.", 5)),
                Map.of("votes", List.of("create_proposal", "vote")),
                Map.of());
    }
}
