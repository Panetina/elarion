package panetina.elarion.addons.quests.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.addons.quests.model.QuestVariableScope;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class QuestConfigLoaderTest {
    @TempDir
    Path temp;

    @Test
    void createsQuestlineDirectoryWithoutLoreDefaults() {
        QuestConfigLoader loader = new QuestConfigLoader(LoggerFactory.getLogger("test"), temp);

        var definitions = loader.load();

        assertTrue(Files.exists(temp.resolve("questlines")));
        assertFalse(Files.exists(temp.resolve("questlines").resolve("red_thread_beneath_foundation.yml")));
        assertTrue(definitions.isEmpty());
    }

    @Test
    void loadsFolderQuestPackage() throws IOException {
        writePackage("generic_foundation");
        QuestConfigLoader loader = new QuestConfigLoader(
                LoggerFactory.getLogger("test"),
                temp,
                Set.of("elarion_quests:variable_at_least")::contains,
                Set.of("elarion_quests:notify")::contains);

        var definitions = loader.load();
        var quest = definitions.get("generic_foundation");

        assertEquals("Generic Foundation", quest.displayName());
        assertEquals("realm", quest.scope());
        assertEquals("intro", quest.rootStage());
        assertEquals("1", quest.version());
        assertEquals(Set.of("template"), Set.copyOf(quest.tags()));
        assertTrue(quest.actors().containsKey("guide"));
        assertEquals(QuestVariableScope.PLAYER, quest.variables().get("trust").scope());
        assertTrue(quest.stages().containsKey("ending"));
        assertTrue(quest.conditions().containsKey("has_trust"));
        assertTrue(quest.consequences().containsKey("notify_done"));
    }

    @Test
    void loadsLegacySingleFileQuestline() throws IOException {
        Files.createDirectories(temp.resolve("questlines"));
        Files.writeString(temp.resolve("questlines").resolve("legacy.yml"), """
                id: legacy
                display-name: Legacy Quest
                scope: player
                root-stage: intro
                stages:
                  intro:
                    display-name: Intro
                """);

        var definitions = new QuestConfigLoader(LoggerFactory.getLogger("test"), temp).load();

        assertEquals("Legacy Quest", definitions.get("legacy").displayName());
        assertEquals("player", definitions.get("legacy").scope());
    }

    @Test
    void rejectsBrokenPackageReferences() throws IOException {
        writePackage("broken");
        Files.writeString(temp.resolve("questlines").resolve("broken").resolve("stages.yml"), """
                stages:
                  intro:
                    display-name: Intro
                    next: [missing]
                """);
        Files.writeString(temp.resolve("questlines").resolve("broken").resolve("conditions.yml"), """
                conditions:
                  bad_condition:
                    type: elarion_quests:variable_at_least
                    variable: missing_variable
                """);

        QuestConfigException exception = assertThrows(QuestConfigException.class,
                () -> new QuestConfigLoader(
                        LoggerFactory.getLogger("test"),
                        temp,
                        Set.of("elarion_quests:variable_at_least")::contains,
                        Set.of("elarion_quests:notify")::contains).load());

        assertTrue(exception.getMessage().contains("unknown next stage missing"));
        assertTrue(exception.getMessage().contains("unknown variable missing_variable"));
    }

    @Test
    void rejectsUnknownRegistryReferences() throws IOException {
        writePackage("bad_registry");

        QuestConfigException exception = assertThrows(QuestConfigException.class,
                () -> new QuestConfigLoader(
                        LoggerFactory.getLogger("test"),
                        temp,
                        Set.of()::contains,
                        Set.of()::contains).load());

        assertTrue(exception.getMessage().contains("unknown condition type elarion_quests:variable_at_least"));
        assertTrue(exception.getMessage().contains("unknown action elarion_quests:notify"));
    }

    private void writePackage(String id) throws IOException {
        Path root = temp.resolve("questlines").resolve(id);
        Files.createDirectories(root);
        Files.writeString(root.resolve("quest.yml"), """
                id: %s
                display-name: Generic Foundation
                scope: realm
                version: "1"
                tags: [template]
                root-stage: intro
                """.formatted(id));
        Files.writeString(root.resolve("actors.yml"), """
                actors:
                  guide:
                    display-name: Guide
                    role: quest giver
                    allowed-npcs: [generic_guide]
                """);
        Files.writeString(root.resolve("variables.yml"), """
                variables:
                  trust:
                    scope: player
                    type: integer
                    default: 0
                """);
        Files.writeString(root.resolve("stages.yml"), """
                stages:
                  intro:
                    display-name: Intro
                    next: [ending]
                  ending:
                    display-name: Ending
                """);
        Files.writeString(root.resolve("evidence.yml"), """
                evidence:
                  note:
                    display-name: Note
                """);
        Files.writeString(root.resolve("endings.yml"), """
                endings:
                  complete:
                    display-name: Complete
                """);
        Files.writeString(root.resolve("conditions.yml"), """
                conditions:
                  has_trust:
                    type: elarion_quests:variable_at_least
                    variable: trust
                    minimum: "1"
                """);
        Files.writeString(root.resolve("consequences.yml"), """
                consequences:
                  notify_done:
                    action: elarion_quests:notify
                    title: Complete
                """);
    }
}
