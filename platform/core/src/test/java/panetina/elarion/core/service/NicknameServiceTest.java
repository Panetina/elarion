package panetina.elarion.core.service;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class NicknameServiceTest {
    @Test
    void equivalentVariantsShareOneComparisonKey() {
        List<String> variants = List.of(
                "Alexander The Great",
                "ALEXANDER THE GREAT",
                "AlexanderTheGreat",
                "Alexander-The-Great",
                "Alexander_The_Great",
                "Alexander.The.Great",
                "A l e x a n d e r T h e G r e a t");

        assertEquals(List.of("alexanderthegreat"),
                variants.stream().map(NicknameService::comparisonKey).distinct().toList());
    }

    @Test
    void formatsRoleplayNamesConsistently() {
        assertEquals("Alexander The Great",
                NicknameService.normalizeDisplay("  aLEXANDER   tHE gREAT "));
        assertEquals("Jean-Luc O'Connor",
                NicknameService.normalizeDisplay(" jean - luc o\u2019connor "));
    }

    @Test
    void detectsLookalikeProtectedNames() {
        assertEquals("admin", NicknameService.comparisonKey("\u0410\u0434min"));
        assertEquals("system", NicknameService.comparisonKey("\u0405ystem"));
    }

    @Test
    void protectedNameMatchingCanRequireExactTerms() {
        assertTrue(NicknameService.findProtectedNameMatch(
                NicknameService.comparisonKey("Admin"), Set.of("admin"), false).orElseThrow().exact());
        assertTrue(NicknameService.findProtectedNameMatch(
                NicknameService.comparisonKey("Halo Maker"), Set.of("Halo"), false).isEmpty());
        NicknameService.ProtectedNameMatch containing = NicknameService.findProtectedNameMatch(
                NicknameService.comparisonKey("Halo Maker"), Set.of("Halo"), true).orElseThrow();
        assertEquals("Halo", containing.term());
        assertFalse(containing.exact());
    }

    @Test
    void acceptsOnlyValidRoleplayCharactersAndStructure() {
        assertTrue(NicknameService.hasAllowedCharacters("Jean-Luc O'Connor"));
        assertTrue(NicknameService.hasValidStructure("Jean-Luc O'Connor"));
        assertFalse(NicknameService.hasAllowedCharacters("Jean.Luc"));
        assertFalse(NicknameService.hasAllowedCharacters("Jean123"));
        assertFalse(NicknameService.hasValidStructure("Jean--Luc"));
        assertFalse(NicknameService.hasValidStructure("'Jean"));
    }
}
