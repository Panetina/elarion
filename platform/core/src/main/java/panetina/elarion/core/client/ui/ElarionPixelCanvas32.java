package panetina.elarion.core.client.ui;

import net.minecraft.client.gui.DrawContext;
import panetina.elarion.core.model.ElarionPixelAsset32;

/** Reusable fixed-palette 32×32 editor state. Owners retain persistence and authority. */
public final class ElarionPixelCanvas32 {
    private static final int[] PALETTE = {0xFF18151D,0xFF3B2B36,0xFF70434B,0xFFA66753,0xFFE0B86B,0xFFF4E8B4,0xFF6E8E5A,0xFF9CBF70,0xFF4A7187,0xFF78A9BA,0xFF574B78,0xFF8E75A5,0xFF9A3F48,0xFFD96A63,0xFF353B50,0xFFF0F0F0};
    private final byte[] pixels = new byte[ElarionPixelAsset32.PIXEL_COUNT];
    private int selectedColor = 1;

    public byte[] pixels() { return pixels.clone(); }
    public int selectedColor() { return selectedColor; }
    public void selectColor(int color) { selectedColor = Math.max(0, Math.min(15, color)); }
    public void load(byte[] source) { if (source != null && source.length == pixels.length) System.arraycopy(source, 0, pixels, 0, pixels.length); }
    public void clear() { java.util.Arrays.fill(pixels, (byte) 0); }
    public void render(DrawContext context, int x, int y, int pixelSize) {
        renderPreview(context, x, y, pixelSize, pixels);
    }
    public static void renderPreview(DrawContext context, int x, int y, int pixelSize, byte[] source) {
        if (source == null || source.length != ElarionPixelAsset32.PIXEL_COUNT) return;
        for (int row = 0; row < 32; row++) for (int column = 0; column < 32; column++) {
            int index = row * 32 + column;
            context.fill(x + column * pixelSize, y + row * pixelSize, x + (column + 1) * pixelSize,
                    y + (row + 1) * pixelSize, PALETTE[Byte.toUnsignedInt(source[index])]);
        }
    }
    public void renderPalette(DrawContext context, int x, int y, int size) {
        for (int index = 0; index < 16; index++) {
            int px = x + (index % 8) * size; int py = y + (index / 8) * size;
            context.fill(px, py, px + size - 1, py + size - 1, PALETTE[index]);
            if (index == selectedColor) context.drawBorder(px - 1, py - 1, size + 1, size + 1, 0xFFFFFFFF);
        }
    }
    public boolean click(int mouseX, int mouseY, int x, int y, int pixelSize) {
        int col = (mouseX - x) / pixelSize, row = (mouseY - y) / pixelSize;
        if (col < 0 || col >= 32 || row < 0 || row >= 32) return false;
        pixels[row * 32 + col] = (byte) selectedColor; return true;
    }
    /** Paints every cell crossed by a drag, so fast mouse motion cannot leave gaps. */
    public boolean drag(int fromX, int fromY, int toX, int toY, int x, int y, int pixelSize) {
        int startColumn = (fromX - x) / pixelSize, startRow = (fromY - y) / pixelSize;
        int endColumn = (toX - x) / pixelSize, endRow = (toY - y) / pixelSize;
        boolean painted = false;
        int steps = Math.max(Math.abs(endColumn - startColumn), Math.abs(endRow - startRow));
        for (int step = 0; step <= steps; step++) {
            int column = startColumn + (endColumn - startColumn) * step / Math.max(1, steps);
            int row = startRow + (endRow - startRow) * step / Math.max(1, steps);
            if (column >= 0 && column < 32 && row >= 0 && row < 32) {
                pixels[row * 32 + column] = (byte) selectedColor;
                painted = true;
            }
        }
        return painted;
    }
    public boolean selectPalette(int mouseX, int mouseY, int x, int y, int size) {
        int col = (mouseX - x) / size, row = (mouseY - y) / size;
        if (col < 0 || col >= 8 || row < 0 || row >= 2) return false;
        selectColor(row * 8 + col); return true;
    }
}
