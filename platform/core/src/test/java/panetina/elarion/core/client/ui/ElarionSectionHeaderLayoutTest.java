package panetina.elarion.core.client.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ElarionSectionHeaderLayoutTest {
    @Test
    void centeredIconHeaderMatchesCharacterIdentityPanelGeometry() {
        ElarionSectionHeaderLayout.CenteredIconHeader header =
                ElarionSectionHeaderLayout.centeredIconHeader(24, 108, 260, 36,
                        14, 9, 20, 14, 10, 34);

        assertEquals(new ElarionSemanticRowLayout.Rect(24, 108, 260, 36), header.bounds());
        assertEquals(new ElarionSemanticRowLayout.Rect(38, 117, 20, 20), header.icon());
        assertEquals(154, header.titleCenterX());
        assertEquals(122, header.titleY());
        assertEquals(new ElarionSemanticRowLayout.Rect(34, 142, 240, 1), header.divider());
    }

    @Test
    void centeredIconHeaderMatchesCharacterBiographyPanelGeometry() {
        ElarionSectionHeaderLayout.CenteredIconHeader header =
                ElarionSectionHeaderLayout.centeredIconHeader(296, 108, 360, 36,
                        14, 9, 20, 14, 10, 34);

        assertEquals(new ElarionSemanticRowLayout.Rect(310, 117, 20, 20), header.icon());
        assertEquals(476, header.titleCenterX());
        assertEquals(new ElarionSemanticRowLayout.Rect(306, 142, 340, 1), header.divider());
    }

    @Test
    void centeredIconHeaderClampsInvalidSizes() {
        ElarionSectionHeaderLayout.CenteredIconHeader header =
                ElarionSectionHeaderLayout.centeredIconHeader(4, 5, 0, -3,
                        -8, -9, 0, -1, -6, -7);

        assertEquals(new ElarionSemanticRowLayout.Rect(4, 5, 1, 1), header.bounds());
        assertEquals(new ElarionSemanticRowLayout.Rect(4, 5, 1, 1), header.icon());
        assertEquals(4, header.titleCenterX());
        assertEquals(5, header.titleY());
        assertEquals(new ElarionSemanticRowLayout.Rect(4, 5, 1, 1), header.divider());
    }
}
