package panetina.elarion.core.model;

import java.util.Arrays;

/** Immutable fixed-palette 16x16 artwork contract for compact domain emblems. */
public record ElarionPixelAsset16(long revision, byte[] paletteIndices) {
    public static final int WIDTH = 16;
    public static final int HEIGHT = 16;
    public static final int PIXEL_COUNT = WIDTH * HEIGHT;
    public static final int PALETTE_SIZE = 16;

    public ElarionPixelAsset16 {
        paletteIndices = paletteIndices == null ? new byte[0] : paletteIndices.clone();
        if (paletteIndices.length != 0 && paletteIndices.length != PIXEL_COUNT) {
            throw new IllegalArgumentException("A 16x16 pixel asset must contain exactly " + PIXEL_COUNT + " palette indices.");
        }
        for (byte index : paletteIndices) {
            if (Byte.toUnsignedInt(index) >= PALETTE_SIZE) {
                throw new IllegalArgumentException("Pixel palette index is outside the fixed palette.");
            }
        }
        revision = Math.max(0L, revision);
    }

    @Override public byte[] paletteIndices() { return paletteIndices.clone(); }
    public boolean empty() { return paletteIndices.length == 0; }
    public static ElarionPixelAsset16 blank() { return new ElarionPixelAsset16(0L, new byte[0]); }
    public ElarionPixelAsset16 revised(byte[] next) { return new ElarionPixelAsset16(revision + 1L, next); }
    public boolean samePixels(byte[] other) { return Arrays.equals(paletteIndices, other); }
}
