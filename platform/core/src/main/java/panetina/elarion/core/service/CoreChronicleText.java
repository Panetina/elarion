package panetina.elarion.core.service;

import panetina.elarion.core.model.ChronicleProjection;
import panetina.elarion.core.model.ChronicleRenderContext;
import panetina.elarion.core.model.ChronicleRenderer;
import panetina.elarion.core.model.ChronicleTemplate;
import panetina.elarion.core.model.ChronicleTemplateFamily;
import panetina.elarion.core.model.PublicHistoryEntry;

import java.util.List;
import java.util.Set;

public final class CoreChronicleText implements ChronicleRenderer {
    public static final CoreChronicleText INSTANCE = new CoreChronicleText();
    private static final ChronicleVariantSelector VARIANT_SELECTOR = new ChronicleVariantSelector();
    private static final ChronicleTemplateFamily TITLE_PROGRESSION_UNLOCKED_FAMILY = new ChronicleTemplateFamily(
            "title.progression-unlocked",
            "title",
            Set.of("progression-unlocked"),
            "Title Unlocked",
            "Title",
            "Progression reward",
            "A title was unlocked through progression.",
            List.of(
                    new ChronicleTemplate("title.progression-unlocked.01",
                            "{actor} unlocked the title {title}."),
                    new ChronicleTemplate("title.progression-unlocked.02",
                            "The deeds of {actor} earned the title {title}."),
                    new ChronicleTemplate("title.progression-unlocked.03",
                            "{actor} added {title} to their known titles."),
                    new ChronicleTemplate("title.progression-unlocked.04",
                            "Progression marked {actor} worthy of {title}."),
                    new ChronicleTemplate("title.progression-unlocked.05",
                            "{actor} claimed {title} through completed deeds."),
                    new ChronicleTemplate("title.progression-unlocked.06",
                            "A new title, {title}, was unlocked by {actor}."),
                    new ChronicleTemplate("title.progression-unlocked.07",
                            "{actor} reached the path to {title}."),
                    new ChronicleTemplate("title.progression-unlocked.08",
                            "{title} was added to {actor}'s record."),
                    new ChronicleTemplate("title.progression-unlocked.09",
                            "{actor}'s progress unlocked {title}."),
                    new ChronicleTemplate("title.progression-unlocked.10",
                            "The title {title} now belongs among {actor}'s rewards.")
            ),
            Set.of("title"),
            Set.of("rule", "progress"));

    private CoreChronicleText() {
    }

    @Override
    public boolean supports(PublicHistoryEntry entry) {
        return TITLE_PROGRESSION_UNLOCKED_FAMILY.supports(entry);
    }

    @Override
    public ChronicleProjection render(PublicHistoryEntry entry, ChronicleRenderContext context) {
        return project(entry, context == null ? ChronicleRenderContext.EMPTY : context);
    }

    public static ChronicleProjection project(PublicHistoryEntry entry, ChronicleRenderContext context) {
        if (entry == null) {
            return new ChronicleProjection("Title Unlocked", "", "Title", "Progression reward",
                    "title.progression-unlocked.default");
        }
        String variantId = VARIANT_SELECTOR.selectVariantId(entry, TITLE_PROGRESSION_UNLOCKED_FAMILY);
        ChronicleTemplate template = TITLE_PROGRESSION_UNLOCKED_FAMILY.templateByVariantId(variantId);
        String body = TITLE_PROGRESSION_UNLOCKED_FAMILY.hasRequiredMetadata(entry)
                ? template.render(entry, context, TITLE_PROGRESSION_UNLOCKED_FAMILY.missingContextBody())
                : TITLE_PROGRESSION_UNLOCKED_FAMILY.missingContextBody();
        return new ChronicleProjection("Title Unlocked", body, "Title", "Progression reward", variantId);
    }

    static ChronicleTemplateFamily titleProgressionUnlockedFamily() {
        return TITLE_PROGRESSION_UNLOCKED_FAMILY;
    }
}
