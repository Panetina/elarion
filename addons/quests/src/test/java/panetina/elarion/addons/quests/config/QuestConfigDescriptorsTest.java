package panetina.elarion.addons.quests.config;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.quests.model.QuestActorDefinition;
import panetina.elarion.addons.quests.model.QuestConditionDefinition;
import panetina.elarion.addons.quests.model.QuestConsequenceDefinition;
import panetina.elarion.addons.quests.model.QuestDefinition;
import panetina.elarion.addons.quests.model.QuestEndingDefinition;
import panetina.elarion.addons.quests.model.QuestEvidenceDefinition;
import panetina.elarion.addons.quests.model.QuestStageDefinition;
import panetina.elarion.addons.quests.model.QuestVariableDefinition;
import panetina.elarion.addons.quests.model.QuestVariableScope;
import panetina.elarion.addons.quests.model.QuestVariableType;
import panetina.elarion.core.config.ElarionConfigDomain;
import panetina.elarion.core.config.ElarionConfigRegistry;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class QuestConfigDescriptorsTest {
    @Test
    void registersQuestPackageMetadataAndGraphSummaries() {
        ElarionConfigRegistry registry = new ElarionConfigRegistry();
        QuestConfigDescriptors.register(registry, () -> List.of(quest("Generic Foundation", "realm")));

        ElarionConfigDomain domain = registry.domain("quests").orElseThrow();

        assertEquals("addons:quests", domain.ownerModule());
        assertEquals("/e quest reload", domain.reloadCommand());
        assertEquals(List.of(
                "config/elarion/addons/quests/questlines/*/*.yml",
                "config/elarion/addons/quests/questlines/*.yml"), domain.files());
        assertTrue(domain.category("questlines").isPresent());
        assertEquals(1, domain.entry("questlines", "questlines.count").orElseThrow().currentValue());
        assertEquals("generic_foundation",
                domain.entry("questlines", "questlines.ids").orElseThrow().currentValue());
        assertEquals("Generic Foundation", domain.entry("questlines",
                "questlines.generic_foundation.display-name").orElseThrow().currentValue());
        assertEquals("realm", domain.entry("questlines",
                "questlines.generic_foundation.scope").orElseThrow().currentValue());
        assertEquals(List.of("realm", "global", "world", "player"), domain.entry("questlines",
                "questlines.generic_foundation.scope").orElseThrow().choices());
        assertEquals(1, domain.entry("questlines",
                "questlines.generic_foundation.actors.required-count").orElseThrow().currentValue());
        assertEquals("trust=integer", domain.entry("questlines",
                "questlines.generic_foundation.variables.types").orElseThrow().currentValue());
        assertEquals("trust=player", domain.entry("questlines",
                "questlines.generic_foundation.variables.scopes").orElseThrow().currentValue());
        assertEquals(1, domain.entry("questlines",
                "questlines.generic_foundation.stages.edges").orElseThrow().currentValue());
        assertEquals(1, domain.entry("questlines",
                "questlines.generic_foundation.endings.shrine-projections").orElseThrow().currentValue());
        assertEquals("elarion_quests:variable_at_least", domain.entry("questlines",
                "questlines.generic_foundation.conditions.types").orElseThrow().currentValue());
        assertEquals("elarion_quests:notify", domain.entry("questlines",
                "questlines.generic_foundation.consequences.actions").orElseThrow().currentValue());
    }

    @Test
    void currentValuesFollowReloadedDefinitionsWithoutChangingRows() {
        AtomicReference<List<QuestDefinition>> current = new AtomicReference<>(
                List.of(quest("Generic Foundation", "realm")));
        ElarionConfigDomain domain = QuestConfigDescriptors.domain(current::get);

        current.set(List.of(
                quest("Updated Foundation", "global"),
                new QuestDefinition("later", "Later", "", "player", "", "1", List.of(),
                        Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of())));

        assertEquals(2, domain.entry("questlines", "questlines.count").orElseThrow().currentValue());
        assertEquals("Updated Foundation", domain.entry("questlines",
                "questlines.generic_foundation.display-name").orElseThrow().currentValue());
        assertEquals("global", domain.entry("questlines",
                "questlines.generic_foundation.scope").orElseThrow().currentValue());
        assertTrue(domain.entry("questlines", "questlines.later.display-name").isEmpty());
    }

    private QuestDefinition quest(String displayName, String scope) {
        return new QuestDefinition(
                "generic_foundation",
                displayName,
                "Descriptor test quest.",
                scope,
                "intro",
                "1",
                List.of("template"),
                Map.of("guide", new QuestActorDefinition(
                        "guide", "generic_guide", "Guide", "quest giver", List.of("generic_guide"), true)),
                Map.of("trust", new QuestVariableDefinition(
                        "trust", QuestVariableScope.PLAYER, QuestVariableType.INTEGER, "0")),
                Map.of(
                        "intro", new QuestStageDefinition(
                                "intro", "Intro", "", "", List.of("ending"), Map.of()),
                        "ending", new QuestStageDefinition(
                                "ending", "Ending", "", "", List.of(), Map.of())),
                Map.of("note", new QuestEvidenceDefinition("note", "Note", "", "minecraft:paper")),
                Map.of("complete", new QuestEndingDefinition(
                        "complete", "Complete", "", Map.of("offering_realm_realm1_1", "Foundation Stone"))),
                Map.of("has_trust", new QuestConditionDefinition(
                        "has_trust", "elarion_quests:variable_at_least", Map.of("minimum", "1"))),
                Map.of("notify_done", new QuestConsequenceDefinition(
                        "notify_done", "elarion_quests:notify", Map.of("title", "Complete"))),
                Map.of("layout", "linear"),
                Map.of("chapter", "foundation"));
    }
}
