package panetina.elarion.addons.government.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class RealmHeraldryTest {
    @Test void revisionsRetainTheFixed32By32PaletteContract() {
        byte[] pixels = new byte[1024];
        pixels[64] = 12;
        RealmHeraldry updated = RealmHeraldry.blank().revised(pixels);
        assertEquals(1L, updated.revision());
        assertArrayEquals(pixels, updated.paletteIndices());
    }

    @Test void rejectsMalformedArt() {
        assertThrows(IllegalArgumentException.class, () -> new RealmHeraldry(0L, new byte[8]));
    }
}
