package panetina.elarion.addons.npcs;

import panetina.elarion.core.model.ChronicleProjection;
import panetina.elarion.core.model.ChronicleRenderContext;
import panetina.elarion.core.model.ChronicleRenderer;
import panetina.elarion.core.model.ChronicleTemplate;
import panetina.elarion.core.model.ChronicleTemplateFamily;
import panetina.elarion.core.model.PublicHistoryEntry;
import panetina.elarion.core.service.ChronicleVariantSelector;

import java.util.List;
import java.util.Set;

public final class NpcChronicleText implements ChronicleRenderer {
    public static final NpcChronicleText INSTANCE = new NpcChronicleText();
    private static final ChronicleVariantSelector SELECTOR = new ChronicleVariantSelector();
    private static final ChronicleTemplateFamily STORY_OUTCOME = new ChronicleTemplateFamily(
            "npc.story-outcome", "npc", Set.of("story-outcome"),
            "A Story Took Root", "NPC", "Story outcome",
            "An Ember reached a meaningful turning point with an NPC.",
            List.of(
                    new ChronicleTemplate("npc.story-outcome.01", "{actor} reached {outcome} in {npc}'s story."),
                    new ChronicleTemplate("npc.story-outcome.02", "A choice before {npc} led {actor} to {outcome}."),
                    new ChronicleTemplate("npc.story-outcome.03", "{npc} will remember when {actor} reached {outcome}."),
                    new ChronicleTemplate("npc.story-outcome.04", "The tale of {actor} and {npc} turned toward {outcome}."),
                    new ChronicleTemplate("npc.story-outcome.05", "{actor}'s dealings with {npc} culminated in {outcome}."),
                    new ChronicleTemplate("npc.story-outcome.06", "A lasting chapter opened between {actor} and {npc}: {outcome}."),
                    new ChronicleTemplate("npc.story-outcome.07", "The record marks {outcome} for {actor} before {npc}."),
                    new ChronicleTemplate("npc.story-outcome.08", "Through words and choices, {actor} reached {outcome} with {npc}."),
                    new ChronicleTemplate("npc.story-outcome.09", "{outcome} became part of {actor}'s history with {npc}."),
                    new ChronicleTemplate("npc.story-outcome.10", "The meeting of {actor} and {npc} ended in {outcome}.")
            ),
            Set.of("actor", "npc", "outcome"),
            Set.of("npcDefinition", "dialogue", "node", "option"));

    private NpcChronicleText() {
    }

    @Override
    public boolean supports(PublicHistoryEntry entry) {
        return STORY_OUTCOME.supports(entry);
    }

    @Override
    public ChronicleProjection render(PublicHistoryEntry entry, ChronicleRenderContext context) {
        String variantId = SELECTOR.selectVariantId(entry, STORY_OUTCOME);
        ChronicleTemplate template = STORY_OUTCOME.templateByVariantId(variantId);
        String body = STORY_OUTCOME.hasRequiredMetadata(entry)
                ? template.render(entry, context, STORY_OUTCOME.missingContextBody())
                : STORY_OUTCOME.missingContextBody();
        return new ChronicleProjection(STORY_OUTCOME.title(), body, STORY_OUTCOME.categoryLabel(),
                STORY_OUTCOME.detailLabel(), variantId);
    }

    public static ChronicleTemplateFamily storyOutcomeFamily() {
        return STORY_OUTCOME;
    }
}
