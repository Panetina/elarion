package panetina.elarion.core.model;

import java.util.Arrays;

/** Immutable fixed-palette artwork contract shared by domain-owned heraldry. */
public record ElarionPixelAsset32(long revision, byte[] paletteIndices) {
    public static final int WIDTH = 32;
    public static final int HEIGHT = 32;
    public static final int PIXEL_COUNT = WIDTH * HEIGHT;
    public static final int PALETTE_SIZE = 16;

    public ElarionPixelAsset32 {
        paletteIndices = paletteIndices == null ? new byte[0] : paletteIndices.clone();
        if (paletteIndices.length != 0 && paletteIndices.length != PIXEL_COUNT) {
            throw new IllegalArgumentException("A 32x32 pixel asset must contain exactly " + PIXEL_COUNT + " palette indices.");
        }
        for (byte index : paletteIndices) {
            int unsigned = Byte.toUnsignedInt(index);
            if (unsigned >= PALETTE_SIZE) throw new IllegalArgumentException("Pixel palette index is outside the fixed palette.");
        }
        revision = Math.max(0L, revision);
    }

    @Override public byte[] paletteIndices() { return paletteIndices.clone(); }
    public boolean empty() { return paletteIndices.length == 0; }
    public static ElarionPixelAsset32 blank() { return new ElarionPixelAsset32(0L, new byte[0]); }
    public ElarionPixelAsset32 revised(byte[] next) { return new ElarionPixelAsset32(revision + 1L, next); }
    public boolean samePixels(byte[] other) { return Arrays.equals(paletteIndices, other); }
}
