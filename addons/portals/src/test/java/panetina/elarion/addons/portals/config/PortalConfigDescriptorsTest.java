package panetina.elarion.addons.portals.config;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.portals.model.PortalRouteDefinition;
import panetina.elarion.addons.portals.model.PortalRouteMode;
import panetina.elarion.addons.portals.model.PortalScheduleDefinition;
import panetina.elarion.addons.portals.model.PortalUiConfig;
import panetina.elarion.addons.portals.model.PortalVisualDefinition;
import panetina.elarion.core.config.ElarionConfigDomain;
import panetina.elarion.core.config.ElarionConfigRegistry;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PortalConfigDescriptorsTest {
    @Test
    void registersPortalDomain() {
        ElarionConfigRegistry registry = new ElarionConfigRegistry();

        PortalConfigDescriptors.register(registry, this::routes, this::ui);

        assertTrue(registry.domain("portals").isPresent());
        assertEquals("Portals", registry.domain("portals").orElseThrow().label());
    }

    @Test
    void domainExposesRouteAndUiConfigValues() {
        ElarionConfigDomain domain = PortalConfigDescriptors.domain(this::routes, this::ui);

        assertEquals("portals", domain.id());
        assertEquals("addons:portals", domain.ownerModule());
        assertEquals("/e portal reload", domain.reloadCommand());
        assertEquals(2, domain.categories().size());
        assertTrue(domain.files().contains("config/elarion/addons/portals/routes.yml"));
        assertTrue(domain.files().contains("config/elarion/addons/portals/ui.yml"));

        assertEquals("2", domain.entry("general", "routes.count").orElseThrow().currentDisplayValue());
        assertEquals("end, nether", domain.entry("general", "routes.ids").orElseThrow().currentDisplayValue());
        assertEquals("portal", domain.entry("general", "ui.theme-variant").orElseThrow().currentDisplayValue());
        assertEquals("360", domain.entry("general", "ui.logical-width").orElseThrow().currentDisplayValue());
        assertEquals("320", domain.entry("general", "ui.logical-width").orElseThrow().minimum());
        assertEquals("960", domain.entry("general", "ui.logical-width").orElseThrow().maximum());

        var mode = domain.entry("routes", "routes.nether.mode").orElseThrow();
        assertEquals("scheduled_ticketed", mode.currentDisplayValue());
        assertTrue(mode.choices().contains("fee_passage"));

        assertEquals("minecraft:the_nether", domain.entry("routes",
                "routes.nether.destination-dimension").orElseThrow().currentDisplayValue());
        assertEquals("portal_ticket.nether", domain.entry("routes",
                "routes.nether.ticket.price-key").orElseThrow().currentDisplayValue());
        assertEquals("7d", domain.entry("routes",
                "routes.nether.schedule.interval").orElseThrow().currentDisplayValue());
        assertEquals("4h", domain.entry("routes",
                "routes.nether.schedule.duration").orElseThrow().currentDisplayValue());
        assertEquals("#A82929", domain.entry("routes",
                "routes.nether.visual.color").orElseThrow().currentDisplayValue());
        assertEquals("minecraft:netherrack", domain.entry("routes",
                "routes.nether.visual.status-icon-item").orElseThrow().currentDisplayValue());
    }

    @Test
    void routeEntriesReadCurrentSupplierValues() {
        AtomicReference<List<PortalRouteDefinition>> current = new AtomicReference<>(routes());
        ElarionConfigDomain domain = PortalConfigDescriptors.domain(current::get, this::ui);
        var displayName = domain.entry("routes", "routes.nether.display-name").orElseThrow();
        var width = domain.entry("general", "ui.logical-width").orElseThrow();

        current.set(List.of(nether("Changed Gate"), end()));

        assertEquals("Changed Gate", displayName.currentDisplayValue());
        assertEquals("Nether Gate", displayName.defaultDisplayValue());
        assertEquals("360", width.currentDisplayValue());
    }

    private List<PortalRouteDefinition> routes() {
        return List.of(nether("Nether Gate"), end());
    }

    private PortalRouteDefinition nether(String displayName) {
        return new PortalRouteDefinition(
                "nether",
                displayName,
                "A scheduled passage.",
                "elarion:lobby",
                "minecraft:the_nether",
                true,
                PortalRouteMode.SCHEDULED_TICKETED,
                "nether",
                "Nether Ticket",
                "One outward passage.",
                "portal_ticket.nether",
                "",
                false,
                new PortalScheduleDefinition(
                        ZoneId.of("Europe/Bucharest"),
                        Instant.parse("2026-01-03T18:00:00Z"),
                        Duration.ofDays(7),
                        Duration.ofHours(4),
                        List.of(Duration.ofHours(1))),
                new PortalVisualDefinition(
                        0xA82929,
                        1.0F,
                        0.82F,
                        2,
                        "minecraft:block/nether_portal",
                        "elarion:portal_ticket",
                        "minecraft:netherrack",
                        0));
    }

    private PortalRouteDefinition end() {
        return new PortalRouteDefinition(
                "end",
                "End Gate",
                "A scheduled passage.",
                "elarion:lobby",
                "minecraft:the_end",
                true,
                PortalRouteMode.SCHEDULED_TICKETED,
                "end",
                "End Ticket",
                "One outward passage.",
                "portal_ticket.end",
                "",
                false,
                new PortalScheduleDefinition(
                        ZoneId.of("Europe/Bucharest"),
                        Instant.parse("2026-01-10T18:00:00Z"),
                        Duration.ofDays(14),
                        Duration.ofHours(4),
                        List.of()),
                new PortalVisualDefinition(
                        0x8A5BC7,
                        1.0F,
                        0.82F,
                        2,
                        "minecraft:block/nether_portal",
                        "elarion:portal_ticket",
                        "minecraft:end_stone",
                        0));
    }

    private PortalUiConfig ui() {
        return new PortalUiConfig("portal", 360, 200, 50, 104, 80);
    }
}
