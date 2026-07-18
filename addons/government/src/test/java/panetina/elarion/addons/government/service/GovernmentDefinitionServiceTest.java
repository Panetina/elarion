package panetina.elarion.addons.government.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.government.model.GovernmentFormDefinition;
import panetina.elarion.addons.government.model.GovernmentSettings;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class GovernmentDefinitionServiceTest {
    @Test
    void loadReplacesSettingsAndFormsTogether() {
        GovernmentDefinitionService service = new GovernmentDefinitionService(null);
        GovernmentSettings settings = new GovernmentSettings(10, 900);

        service.load(() -> settings, () -> Map.of("republic", form("republic")));

        assertSame(settings, service.settings());
        assertEquals(1, service.forms().size());
        assertEquals("Republic", service.form("republic").orElseThrow().displayName());
    }

    @Test
    void failedFormReloadPreservesPreviousSettingsAndForms() {
        GovernmentDefinitionService service = new GovernmentDefinitionService(null);
        GovernmentSettings previousSettings = new GovernmentSettings(10, 900);
        service.load(() -> previousSettings, () -> Map.of("republic", form("republic")));

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> service.load(() -> new GovernmentSettings(20, 1200), () -> {
                    throw new IllegalStateException("bad form");
                }));

        assertEquals("bad form", thrown.getMessage());
        assertSame(previousSettings, service.settings());
        assertEquals(1, service.forms().size());
        assertEquals("Republic", service.form("republic").orElseThrow().displayName());
        assertEquals(0, service.form("monarchy").stream().count());
    }

    private static GovernmentFormDefinition form(String id) {
        return new GovernmentFormDefinition(id, title(id), "", true, "%realm%", List.of(),
                false, List.of(), Map.of(), Map.of());
    }

    private static String title(String id) {
        if ("republic".equals(id)) return "Republic";
        if ("monarchy".equals(id)) return "Monarchy";
        return id;
    }
}
