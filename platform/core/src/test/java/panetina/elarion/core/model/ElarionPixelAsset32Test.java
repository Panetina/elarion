package panetina.elarion.core.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ElarionPixelAsset32Test {
    @Test void acceptsOnlyEmptyOrFixedPalette32x32Data() {
        assertTrue(ElarionPixelAsset32.blank().empty());
        byte[] pixels = new byte[ElarionPixelAsset32.PIXEL_COUNT];
        pixels[12] = 15;
        ElarionPixelAsset32 asset = new ElarionPixelAsset32(3, pixels);
        assertEquals(3, asset.revision());
        pixels[12] = 1;
        assertEquals(15, Byte.toUnsignedInt(asset.paletteIndices()[12]));
        assertThrows(IllegalArgumentException.class, () -> new ElarionPixelAsset32(0, new byte[4]));
        byte[] invalid = new byte[ElarionPixelAsset32.PIXEL_COUNT]; invalid[0] = 16;
        assertThrows(IllegalArgumentException.class, () -> new ElarionPixelAsset32(0, invalid));
    }
}
