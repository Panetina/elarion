package panetina.elarion.addons.offerings.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.offerings.model.OfferingPresentation;
import panetina.elarion.addons.offerings.model.OfferingProjectDefinition;
import panetina.elarion.addons.offerings.model.OfferingScope;
import panetina.elarion.addons.offerings.model.OfferingUiConfig;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class OfferingDefinitionServiceTest {
    @Test
    void loadReplacesDefinitionsAndUiTogether() {
        OfferingDefinitionService service = new OfferingDefinitionService(null);
        OfferingUiConfig ui = OfferingUiConfig.defaults();

        service.load(() -> Map.of("foundation", project("foundation")), () -> ui);

        assertEquals(1, service.all().size());
        assertEquals("Foundation", service.find("foundation").orElseThrow().displayName());
        assertSame(ui, service.ui());
    }

    @Test
    void failedUiReloadPreservesPreviousDefinitionsAndUi() {
        OfferingDefinitionService service = new OfferingDefinitionService(null);
        OfferingUiConfig previousUi = OfferingUiConfig.defaults();
        service.load(() -> Map.of("foundation", project("foundation")), () -> previousUi);

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> service.load(() -> Map.of("replacement", project("replacement")), () -> {
                    throw new IllegalStateException("bad ui");
                }));

        assertEquals("bad ui", thrown.getMessage());
        assertEquals(1, service.all().size());
        assertEquals("Foundation", service.find("foundation").orElseThrow().displayName());
        assertEquals(0, service.find("replacement").stream().count());
        assertSame(previousUi, service.ui());
    }

    private static OfferingProjectDefinition project(String id) {
        return new OfferingProjectDefinition(id, title(id), "", true, OfferingScope.REALM,
                false, true, List.of(), List.of(), OfferingPresentation.defaults());
    }

    private static String title(String id) {
        if ("foundation".equals(id)) return "Foundation";
        if ("replacement".equals(id)) return "Replacement";
        return id;
    }
}
