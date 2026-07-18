package panetina.elarion.core.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ElarionTitlePresentationTest {
    @Test
    void knownTitleFamiliesFallbackToTheirProjectWideRankColors() {
        assertEquals(ElarionCollectionRank.SOVEREIGN.color(),
                ElarionTitlePresentation.fallbackColor("government_monarch", TitleOwnershipMode.UNLIMITED));
        assertEquals(ElarionCollectionRank.SOVEREIGN.color(),
                ElarionTitlePresentation.fallbackColor("government_president", TitleOwnershipMode.UNLIMITED));
        assertEquals(ElarionCollectionRank.RARE.color(),
                ElarionTitlePresentation.fallbackColor("aquatic", TitleOwnershipMode.ONE_PER_PLAYER));
    }

    @Test
    void customTitleFallbackIsSimpleWhiteUnlessItsOwnershipIsUnique() {
        assertEquals(0xFFFFFFFF,
                ElarionTitlePresentation.fallbackColor("custom_builder", TitleOwnershipMode.UNLIMITED));
        assertEquals(ElarionCollectionRank.LEGENDARY.color(),
                ElarionTitlePresentation.fallbackColor("custom_unique", TitleOwnershipMode.GLOBALLY_UNIQUE));
    }
}
