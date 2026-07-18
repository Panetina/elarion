package panetina.elarion.addons.portals;

import panetina.elarion.core.model.ChronicleProjection;
import panetina.elarion.core.model.ChronicleRenderContext;
import panetina.elarion.core.model.ChronicleRenderer;
import panetina.elarion.core.model.ChronicleTemplate;
import panetina.elarion.core.model.ChronicleTemplateFamily;
import panetina.elarion.core.model.PublicHistoryEntry;
import panetina.elarion.core.service.ChronicleVariantSelector;

import java.util.List;
import java.util.Set;

public final class PortalChronicleText implements ChronicleRenderer {
    public static final PortalChronicleText INSTANCE = new PortalChronicleText();
    private static final ChronicleVariantSelector VARIANT_SELECTOR = new ChronicleVariantSelector();
    private static final ChronicleTemplateFamily ROUTE_UNLOCKED_FAMILY = new ChronicleTemplateFamily(
            "portal.route-unlocked",
            "portal",
            Set.of("route-unlocked"),
            "Portal Route Opened",
            "Portal",
            "Route unlocked",
            "A portal route opened.",
            List.of(
                    new ChronicleTemplate("portal.route-unlocked.01",
                            "The route {routeId} opened to travelers."),
                    new ChronicleTemplate("portal.route-unlocked.02",
                            "Ancient gatework stirred as {routeId} became available."),
                    new ChronicleTemplate("portal.route-unlocked.03",
                            "Travelers can now use the portal route {routeId}."),
                    new ChronicleTemplate("portal.route-unlocked.04",
                            "The route {routeId} was unlocked for passage."),
                    new ChronicleTemplate("portal.route-unlocked.05",
                            "A new path opened through {routeId}."),
                    new ChronicleTemplate("portal.route-unlocked.06",
                            "The portal network accepted {routeId} into service."),
                    new ChronicleTemplate("portal.route-unlocked.07",
                            "Gatekeepers marked {routeId} open."),
                    new ChronicleTemplate("portal.route-unlocked.08",
                            "The portal route {routeId} awakened."),
                    new ChronicleTemplate("portal.route-unlocked.09",
                            "{routeId} joined the active portal routes."),
                    new ChronicleTemplate("portal.route-unlocked.10",
                            "The way through {routeId} is now open.")
            ),
            Set.of("routeId"),
            Set.of());

    private PortalChronicleText() {
    }

    @Override
    public boolean supports(PublicHistoryEntry entry) {
        return ROUTE_UNLOCKED_FAMILY.supports(entry);
    }

    @Override
    public ChronicleProjection render(PublicHistoryEntry entry, ChronicleRenderContext context) {
        return project(entry);
    }

    public static ChronicleProjection project(PublicHistoryEntry entry) {
        if (entry == null) {
            return new ChronicleProjection("Portal Route Opened", "", "Portal", "Route unlocked",
                    "portal.route-unlocked.default");
        }
        String variantId = VARIANT_SELECTOR.selectVariantId(entry, ROUTE_UNLOCKED_FAMILY);
        ChronicleTemplate template = ROUTE_UNLOCKED_FAMILY.templateByVariantId(variantId);
        String body = ROUTE_UNLOCKED_FAMILY.hasRequiredMetadata(entry)
                ? template.render(entry, ChronicleRenderContext.EMPTY, ROUTE_UNLOCKED_FAMILY.missingContextBody())
                : ROUTE_UNLOCKED_FAMILY.missingContextBody();
        return new ChronicleProjection("Portal Route Opened", body, "Portal", "Route unlocked", variantId);
    }

    static ChronicleTemplateFamily routeUnlockedFamily() {
        return ROUTE_UNLOCKED_FAMILY;
    }
}
