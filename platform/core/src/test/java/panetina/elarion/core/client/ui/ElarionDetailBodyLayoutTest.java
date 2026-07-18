package panetina.elarion.core.client.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ElarionDetailBodyLayoutTest {
    @Test
    void sectionTitlePreservesIconAndTextOffsets() {
        ElarionDetailBodyLayout.SectionTitle layout =
                ElarionDetailBodyLayout.sectionTitle(40, 70, 16, 6, -3);

        assertEquals(40, layout.icon().x());
        assertEquals(67, layout.icon().y());
        assertEquals(16, layout.icon().width());
        assertEquals(16, layout.icon().height());
        assertEquals(62, layout.textX());
        assertEquals(70, layout.textY());
    }

    @Test
    void bodyTextClampsToDrawableBounds() {
        ElarionDetailBodyLayout.BodyText layout =
                ElarionDetailBodyLayout.bodyText(8, 9, 0, -4);

        assertEquals(8, layout.body().x());
        assertEquals(9, layout.body().y());
        assertEquals(1, layout.body().width());
        assertEquals(1, layout.body().height());
    }

    @Test
    void keyValueRowKeepsValueInsideAvailableWidth() {
        ElarionDetailBodyLayout.KeyValueRow layout =
                ElarionDetailBodyLayout.keyValueRow(20, 30, 160, 58, 8);

        assertEquals(20, layout.labelX());
        assertEquals(86, layout.valueX());
        assertEquals(30, layout.textY());
        assertEquals(58, layout.labelWidth());
        assertEquals(94, layout.valueWidth());
    }

    @Test
    void keyValueRowClampsOversizedLabel() {
        ElarionDetailBodyLayout.KeyValueRow layout =
                ElarionDetailBodyLayout.keyValueRow(20, 30, 40, 80, 8);

        assertEquals(40, layout.labelWidth());
        assertEquals(68, layout.valueX());
        assertEquals(1, layout.valueWidth());
    }
}
