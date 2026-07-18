package panetina.elarion.core.client.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ElarionPanelHeaderLayoutTest {
    @Test
    void leftTitleMatchesNpcTradeCatalogPanelGeometry() {
        ElarionPanelHeaderLayout.LeftTitle header =
                ElarionPanelHeaderLayout.leftTitle(14, 124, 492, 222,
                        24, 10, 8, 10, 34);

        assertEquals(new ElarionSemanticRowLayout.Rect(14, 124, 492, 222), header.bounds());
        assertEquals(24, header.headerHeight());
        assertEquals(24, header.titleX());
        assertEquals(132, header.titleY());
        assertEquals(472, header.titleMaxWidth());
        assertEquals(new ElarionSemanticRowLayout.Rect(24, 158, 472, 1), header.divider());
        assertEquals(148, header.bodyY());
    }

    @Test
    void leftTitleMatchesNpcBankAmountPanelTitleGeometry() {
        ElarionPanelHeaderLayout.LeftTitle header =
                ElarionPanelHeaderLayout.leftTitle(14, 122, 472, 126,
                        24, 10, 8, 10, 34);

        assertEquals(new ElarionSemanticRowLayout.Rect(14, 122, 472, 126), header.bounds());
        assertEquals(24, header.titleX());
        assertEquals(130, header.titleY());
        assertEquals(452, header.titleMaxWidth());
        assertEquals(new ElarionSemanticRowLayout.Rect(24, 156, 452, 1), header.divider());
        assertEquals(146, header.bodyY());
    }

    @Test
    void leftTitleClampsInvalidSizes() {
        ElarionPanelHeaderLayout.LeftTitle header =
                ElarionPanelHeaderLayout.leftTitle(3, 4, 0, -6,
                        20, -8, -2, -10, -5);

        assertEquals(new ElarionSemanticRowLayout.Rect(3, 4, 1, 1), header.bounds());
        assertEquals(1, header.headerHeight());
        assertEquals(3, header.titleX());
        assertEquals(4, header.titleY());
        assertEquals(1, header.titleMaxWidth());
        assertEquals(new ElarionSemanticRowLayout.Rect(3, 4, 1, 1), header.divider());
        assertEquals(5, header.bodyY());
    }
}
