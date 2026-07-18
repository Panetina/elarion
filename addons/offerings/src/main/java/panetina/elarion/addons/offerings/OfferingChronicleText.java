package panetina.elarion.addons.offerings;

import panetina.elarion.core.model.ChronicleProjection;
import panetina.elarion.core.model.ChronicleRenderContext;
import panetina.elarion.core.model.ChronicleRenderer;
import panetina.elarion.core.model.ChronicleTemplate;
import panetina.elarion.core.model.ChronicleTemplateFamily;
import panetina.elarion.core.model.PublicHistoryEntry;
import panetina.elarion.core.service.ChronicleVariantSelector;

import java.util.List;
import java.util.Set;

public final class OfferingChronicleText implements ChronicleRenderer {
    public static final OfferingChronicleText INSTANCE = new OfferingChronicleText();
    private static final ChronicleVariantSelector VARIANT_SELECTOR = new ChronicleVariantSelector();
    private static final ChronicleTemplateFamily PROJECT_COMPLETED_FAMILY = new ChronicleTemplateFamily(
            "offering.project-completed",
            "offering",
            Set.of("project-completed", "project-force-completed"),
            "Offering Project Completed",
            "Offering",
            "Project completed",
            "An Offering project was completed.",
            List.of(
                    new ChronicleTemplate("offering.project-completed.01",
                            "The Offering project {project} was completed."),
                    new ChronicleTemplate("offering.project-completed.02",
                            "{project} reached completion through gathered offerings."),
                    new ChronicleTemplate("offering.project-completed.03",
                            "The final offering was made for {project}."),
                    new ChronicleTemplate("offering.project-completed.04",
                            "{project} now stands complete."),
                    new ChronicleTemplate("offering.project-completed.05",
                            "Contributors finished the Offering project {project}."),
                    new ChronicleTemplate("offering.project-completed.06",
                            "The Shrine marked {project} complete."),
                    new ChronicleTemplate("offering.project-completed.07",
                            "{project} passed its last required offering."),
                    new ChronicleTemplate("offering.project-completed.08",
                            "The Realm's work on {project} came to completion."),
                    new ChronicleTemplate("offering.project-completed.09",
                            "Offering records closed {project} as complete."),
                    new ChronicleTemplate("offering.project-completed.10",
                            "{project} was fulfilled at the Shrine.")
            ),
            Set.of("project"),
            Set.of("scope", "instance"));
    private static final ChronicleTemplateFamily REALM_GLOBAL_ACCESS_FAMILY = new ChronicleTemplateFamily(
            "offering.realm-global-access-changed",
            "offering",
            Set.of("realm-global-access-changed"),
            "Realm Reached The World Stage",
            "Offering",
            "Realm global access",
            "A Realm's global access changed.",
            List.of(
                    new ChronicleTemplate("offering.realm-global-access-changed.01",
                            "{realm} stepped onto the global stage."),
                    new ChronicleTemplate("offering.realm-global-access-changed.02",
                            "The World heard {realm}'s name beyond its borders."),
                    new ChronicleTemplate("offering.realm-global-access-changed.03",
                            "{realm} gained access to wider Elarion affairs."),
                    new ChronicleTemplate("offering.realm-global-access-changed.04",
                            "A global path opened for {realm}."),
                    new ChronicleTemplate("offering.realm-global-access-changed.05",
                            "{realm} crossed from local concern into world concern."),
                    new ChronicleTemplate("offering.realm-global-access-changed.06",
                            "The Chronicle marks {realm} as globally visible."),
                    new ChronicleTemplate("offering.realm-global-access-changed.07",
                            "{realm}'s reach now extends beyond its own lands."),
                    new ChronicleTemplate("offering.realm-global-access-changed.08",
                            "Global recognition was granted to {realm}."),
                    new ChronicleTemplate("offering.realm-global-access-changed.09",
                            "{realm} opened its gates to world-stage events."),
                    new ChronicleTemplate("offering.realm-global-access-changed.10",
                            "The Realm of {realm} joined the wider record of Elarion.")
            ),
            Set.of(),
            Set.of("flag", "enabled"));

    private OfferingChronicleText() {
    }

    @Override
    public boolean supports(PublicHistoryEntry entry) {
        return PROJECT_COMPLETED_FAMILY.supports(entry)
                || REALM_GLOBAL_ACCESS_FAMILY.supports(entry);
    }

    @Override
    public ChronicleProjection render(PublicHistoryEntry entry, ChronicleRenderContext context) {
        return project(entry);
    }

    public static ChronicleProjection project(PublicHistoryEntry entry) {
        ChronicleTemplateFamily family = family(entry);
        if (family == null) {
            return new ChronicleProjection("Offering", "", "Offering", "Offering record",
                    "offering.default");
        }
        String variantId = VARIANT_SELECTOR.selectVariantId(entry, family);
        ChronicleTemplate template = family.templateByVariantId(variantId);
        String body = family.hasRequiredMetadata(entry)
                ? template.render(entry, ChronicleRenderContext.EMPTY, family.missingContextBody())
                : family.missingContextBody();
        return new ChronicleProjection(family.title(), body, family.categoryLabel(), family.detailLabel(), variantId);
    }

    static ChronicleTemplateFamily projectCompletedFamily() {
        return PROJECT_COMPLETED_FAMILY;
    }

    static ChronicleTemplateFamily realmGlobalAccessFamily() {
        return REALM_GLOBAL_ACCESS_FAMILY;
    }

    private static ChronicleTemplateFamily family(PublicHistoryEntry entry) {
        if (entry == null) {
            return null;
        }
        if (PROJECT_COMPLETED_FAMILY.supports(entry)) return PROJECT_COMPLETED_FAMILY;
        if (REALM_GLOBAL_ACCESS_FAMILY.supports(entry)) return REALM_GLOBAL_ACCESS_FAMILY;
        return null;
    }
}
