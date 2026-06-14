package panetina.elarion.addons.angling.resource;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import panetina.elarion.addons.angling.loader.FishDefinitionParseException;
import panetina.elarion.addons.angling.model.AnglingRarity;
import panetina.elarion.addons.angling.model.FishDefinition;
import panetina.elarion.addons.angling.model.FishDefinitionIndex;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FishDefinitionRepositoryTest {
    @Test
    void startsWithEmptyImmutableIndex() {
        FishDefinitionRepository repository = new FishDefinitionRepository();

        assertTrue(repository.current().all().isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> repository.current().all().add(fish("placeholder_fish_001")));
    }

    @Test
    void successfulPublicationReplacesSnapshot() {
        FishDefinitionRepository repository = new FishDefinitionRepository();
        FishDefinitionIndex index = new FishDefinitionIndex(List.of(fish("placeholder_fish_001")));

        repository.publish(index);

        assertEquals(index, repository.current());
    }

    @Test
    void failedReloadLeavesPreviousSnapshotIntact() {
        FishDefinitionRepository repository = new FishDefinitionRepository();
        FishDefinitionIndex previous = new FishDefinitionIndex(List.of(fish("placeholder_fish_001")));
        repository.publish(previous);

        assertThrows(FishDefinitionParseException.class, () -> repository.reload(Map.of("bad", """
                {
                  "id": "elarion_angling:placeholder_fish_002",
                  "translation_key": "fish.elarion_angling.placeholder_fish_002",
                  "rarity": "UNKNOWN",
                  "weight": 10
                }
                """)));

        assertEquals(previous, repository.current());
    }

    private static FishDefinition fish(String path) {
        return new FishDefinition(
                Identifier.of("elarion_angling", path),
                "fish.elarion_angling." + path,
                AnglingRarity.PLACEHOLDER_COMMON,
                10,
                List.of());
    }
}
