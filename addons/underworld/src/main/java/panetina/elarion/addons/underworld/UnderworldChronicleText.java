package panetina.elarion.addons.underworld;

import panetina.elarion.core.model.ChronicleProjection;
import panetina.elarion.core.model.ChronicleRenderContext;
import panetina.elarion.core.model.ChronicleRenderer;
import panetina.elarion.core.model.ChronicleTemplate;
import panetina.elarion.core.model.ChronicleTemplateFamily;
import panetina.elarion.core.model.PublicHistoryEntry;
import panetina.elarion.core.service.ChronicleVariantSelector;

import java.util.List;
import java.util.Set;

public final class UnderworldChronicleText implements ChronicleRenderer {
    public static final UnderworldChronicleText INSTANCE = new UnderworldChronicleText();
    private static final ChronicleVariantSelector VARIANT_SELECTOR = new ChronicleVariantSelector();
    private static final ChronicleTemplateFamily PVE_DEATH_FAMILY = new ChronicleTemplateFamily(
            "underworld.death-pve",
            "underworld",
            Set.of("death-pve", "death-void"),
            "Death Recorded",
            "Underworld",
            "PVE death",
            "An Ember died and was drawn toward the Underworld.",
            List.of(
                    new ChronicleTemplate("underworld.death-pve.01", "{actor} fell to the dangers of the world."),
                    new ChronicleTemplate("underworld.death-pve.02", "The living world claimed {actor}."),
                    new ChronicleTemplate("underworld.death-pve.03", "{actor}'s journey was interrupted by death."),
                    new ChronicleTemplate("underworld.death-pve.04", "The Underworld opened its road to {actor}."),
                    new ChronicleTemplate("underworld.death-pve.05", "{actor} perished beyond the safety of hearth and hall."),
                    new ChronicleTemplate("underworld.death-pve.06", "A grave was marked for {actor}."),
                    new ChronicleTemplate("underworld.death-pve.07", "{actor} was overcome by the realm's perils."),
                    new ChronicleTemplate("underworld.death-pve.08", "The world proved fatal to {actor}."),
                    new ChronicleTemplate("underworld.death-pve.09", "{actor}'s soul was sent below after a fatal encounter."),
                    new ChronicleTemplate("underworld.death-pve.10", "The Chronicle records {actor}'s fall to the wilds.")
            ),
            Set.of(),
            Set.of("deathType", "corpseId"));
    private static final ChronicleTemplateFamily PVP_DEATH_FAMILY = new ChronicleTemplateFamily(
            "underworld.death-pvp",
            "underworld",
            Set.of("death-pvp"),
            "Death Recorded",
            "Underworld",
            "PVP death",
            "An Ember was slain by another player.",
            List.of(
                    new ChronicleTemplate("underworld.death-pvp.01", "{actor} was slain in player combat."),
                    new ChronicleTemplate("underworld.death-pvp.02", "Steel turned against {actor}, and the Underworld answered."),
                    new ChronicleTemplate("underworld.death-pvp.03", "{actor} fell by another player's hand."),
                    new ChronicleTemplate("underworld.death-pvp.04", "A contested death sent {actor} below."),
                    new ChronicleTemplate("underworld.death-pvp.05", "{actor}'s body was left after a player battle."),
                    new ChronicleTemplate("underworld.death-pvp.06", "The Chronicle marks {actor}'s death in conflict."),
                    new ChronicleTemplate("underworld.death-pvp.07", "{actor} was defeated in a mortal struggle."),
                    new ChronicleTemplate("underworld.death-pvp.08", "Bloodshed carried {actor} toward the Underworld."),
                    new ChronicleTemplate("underworld.death-pvp.09", "{actor} met death in the clash between Embers."),
                    new ChronicleTemplate("underworld.death-pvp.10", "A player's blow ended {actor}'s living journey.")
            ),
            Set.of(),
            Set.of("deathType", "corpseId", "killer"));
    private static final ChronicleTemplateFamily SUICIDE_DEATH_FAMILY = new ChronicleTemplateFamily(
            "underworld.death-suicide",
            "underworld",
            Set.of("death-suicide"),
            "Death Recorded",
            "Underworld",
            "Self-inflicted death",
            "An Ember died by their own action.",
            List.of(
                    new ChronicleTemplate("underworld.death-suicide.01", "{actor}'s own action ended their life."),
                    new ChronicleTemplate("underworld.death-suicide.02", "{actor} crossed into death by their own hand."),
                    new ChronicleTemplate("underworld.death-suicide.03", "The Underworld received {actor} after a self-inflicted fall."),
                    new ChronicleTemplate("underworld.death-suicide.04", "{actor} became the cause of their own grave."),
                    new ChronicleTemplate("underworld.death-suicide.05", "No enemy claimed {actor}; the death was self-made."),
                    new ChronicleTemplate("underworld.death-suicide.06", "{actor}'s fate turned inward and ended in death."),
                    new ChronicleTemplate("underworld.death-suicide.07", "The Chronicle records {actor}'s self-inflicted passing."),
                    new ChronicleTemplate("underworld.death-suicide.08", "{actor} sent themself toward the Underworld."),
                    new ChronicleTemplate("underworld.death-suicide.09", "By their own action, {actor} left the living world."),
                    new ChronicleTemplate("underworld.death-suicide.10", "{actor}'s death was born from their own choice.")
            ),
            Set.of(),
            Set.of("deathType", "corpseId"));
    private static final ChronicleTemplateFamily TRUE_DEATH_FAMILY = new ChronicleTemplateFamily(
            "underworld.true-death",
            "underworld",
            Set.of("true-death"),
            "True Death",
            "Underworld",
            "True Death",
            "An Ember suffered True Death.",
            List.of(
                    new ChronicleTemplate("underworld.true-death.01", "{actor}'s soul shattered into True Death."),
                    new ChronicleTemplate("underworld.true-death.02", "True Death claimed {actor}."),
                    new ChronicleTemplate("underworld.true-death.03", "{actor}'s thread ended beyond ordinary recovery."),
                    new ChronicleTemplate("underworld.true-death.04", "The Chronicle closes {actor}'s living record in True Death."),
                    new ChronicleTemplate("underworld.true-death.05", "{actor} passed beyond the reach of return."),
                    new ChronicleTemplate("underworld.true-death.06", "The final fracture ended {actor}'s story."),
                    new ChronicleTemplate("underworld.true-death.07", "No grave recovery could answer {actor}'s True Death."),
                    new ChronicleTemplate("underworld.true-death.08", "{actor}'s soul was lost to the last threshold."),
                    new ChronicleTemplate("underworld.true-death.09", "True Death sealed the fate of {actor}."),
                    new ChronicleTemplate("underworld.true-death.10", "{actor}'s final passage was recorded as True Death.")
            ),
            Set.of(),
            Set.of("fractures"));

    private UnderworldChronicleText() {
    }

    @Override
    public boolean supports(PublicHistoryEntry entry) {
        return family(entry) != null;
    }

    @Override
    public ChronicleProjection render(PublicHistoryEntry entry, ChronicleRenderContext context) {
        return project(entry, context == null ? ChronicleRenderContext.EMPTY : context);
    }

    public static ChronicleProjection project(PublicHistoryEntry entry, ChronicleRenderContext context) {
        ChronicleTemplateFamily family = family(entry);
        if (family == null) {
            return new ChronicleProjection("Underworld", "", "Underworld", "Underworld record",
                    "underworld.default");
        }
        String variantId = VARIANT_SELECTOR.selectVariantId(entry, family);
        ChronicleTemplate template = family.templateByVariantId(variantId);
        String body = template.render(entry, context == null ? ChronicleRenderContext.EMPTY : context,
                family.missingContextBody());
        return new ChronicleProjection(family.title(), body, family.categoryLabel(), family.detailLabel(), variantId);
    }

    static ChronicleTemplateFamily pveDeathFamily() {
        return PVE_DEATH_FAMILY;
    }

    static ChronicleTemplateFamily pvpDeathFamily() {
        return PVP_DEATH_FAMILY;
    }

    static ChronicleTemplateFamily suicideDeathFamily() {
        return SUICIDE_DEATH_FAMILY;
    }

    static ChronicleTemplateFamily trueDeathFamily() {
        return TRUE_DEATH_FAMILY;
    }

    private static ChronicleTemplateFamily family(PublicHistoryEntry entry) {
        if (PVE_DEATH_FAMILY.supports(entry)) return PVE_DEATH_FAMILY;
        if (PVP_DEATH_FAMILY.supports(entry)) return PVP_DEATH_FAMILY;
        if (SUICIDE_DEATH_FAMILY.supports(entry)) return SUICIDE_DEATH_FAMILY;
        if (TRUE_DEATH_FAMILY.supports(entry)) return TRUE_DEATH_FAMILY;
        return null;
    }
}
