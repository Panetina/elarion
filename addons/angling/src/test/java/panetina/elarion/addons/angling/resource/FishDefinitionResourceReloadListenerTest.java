package panetina.elarion.addons.angling.resource;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FishDefinitionResourceReloadListenerTest {
    @Test
    void resourcePathFilteringAcceptsOnlyAnglingFishJsonResources() {
        assertTrue(FishDefinitionResourceReloadListener.isDefinitionResource(
                Identifier.of("elarion_angling", "angling/fish/placeholder_fish_001.json")));
        assertFalse(FishDefinitionResourceReloadListener.isDefinitionResource(
                Identifier.of("elarion_angling", "angling/fish/placeholder_fish_001.txt")));
        assertFalse(FishDefinitionResourceReloadListener.isDefinitionResource(
                Identifier.of("elarion_angling", "angling/other/placeholder_fish_001.json")));
        assertFalse(FishDefinitionResourceReloadListener.isDefinitionResource(
                Identifier.of("minecraft", "angling/fish/placeholder_fish_001.json")));
    }

    @Test
    void documentIdsDropOnlyJsonSuffix() {
        assertEquals("angling/fish/placeholder_fish_001", FishDefinitionResourceReloadListener.documentId(
                Identifier.of("elarion_angling", "angling/fish/placeholder_fish_001.json")));
    }

    @Test
    void resourceDocumentOrderingIsDeterministic() {
        var raw = new LinkedHashMap<Identifier, String>();
        raw.put(Identifier.of("elarion_angling", "angling/fish/placeholder_fish_002.json"), "second");
        raw.put(Identifier.of("elarion_angling", "angling/fish/placeholder_fish_001.json"), "first");

        var documents = FishDefinitionResourceReloadListener.toLoaderDocuments(raw);

        assertIterableEquals(
                java.util.List.of("angling/fish/placeholder_fish_001", "angling/fish/placeholder_fish_002"),
                documents.keySet());
    }
}
