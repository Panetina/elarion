package panetina.elarion.core.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ElarionCollectionRankTest {
    @Test
    void commonRankPaletteStaysProjectWide() {
        assertEquals(0xFF72C878, ElarionCollectionRank.COMMON.color());
        assertEquals(0xFF5CB7E8, ElarionCollectionRank.UNCOMMON.color());
        assertEquals(0xFFC084FF, ElarionCollectionRank.RARE.color());
        assertEquals(0xFFE45CBA, ElarionCollectionRank.EPIC.color());
        assertEquals(0xFFFFA83D, ElarionCollectionRank.LEGENDARY.color());
    }

    @Test
    void labelLookupAcceptsCommonFormattingVariants() {
        assertEquals(ElarionCollectionRank.UNCOMMON, ElarionCollectionRank.byLabel("uncommon"));
        assertEquals(ElarionCollectionRank.SOVEREIGN, ElarionCollectionRank.byLabel(" sovereign "));
        assertEquals(ElarionCollectionRank.COUNCIL, ElarionCollectionRank.byLabel("council"));
        assertEquals(ElarionCollectionRank.COMMON, ElarionCollectionRank.byLabel("unknown"));
    }
}
