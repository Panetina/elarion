package panetina.elarion.core.client.ui;

public final class ElarionTextInput {
    private final int maxLength;
    private final boolean multiline;
    private final long startedAt;
    private String value = "";
    private boolean focused;
    private int scrollLine;

    public ElarionTextInput(int maxLength, boolean multiline) {
        this.maxLength = Math.max(1, maxLength);
        this.multiline = multiline;
        this.startedAt = System.currentTimeMillis();
    }

    public boolean type(char character) {
        return append(String.valueOf(character));
    }

    public boolean append(String raw) {
        if (raw == null || raw.isEmpty()) return false;
        StringBuilder builder = new StringBuilder(value);
        int before = builder.length();
        raw.codePoints().forEach(codePoint -> {
            if (builder.length() >= maxLength) return;
            if (codePoint == '\r') return;
            if (codePoint == '\n') {
                if (multiline) builder.append('\n');
                return;
            }
            if (isAllowedTextCharacter((char) codePoint)) builder.appendCodePoint(codePoint);
        });
        if (builder.length() > maxLength) builder.setLength(maxLength);
        value = builder.toString();
        return value.length() != before;
    }

    public boolean backspace() {
        if (value.isEmpty()) return false;
        value = value.substring(0, value.offsetByCodePoints(value.length(), -1));
        return true;
    }

    public void text(String value) {
        this.value = value == null ? "" : value;
        if (this.value.length() > maxLength) this.value = this.value.substring(0, maxLength);
        scrollLine = 0;
    }

    public String text() {
        return value;
    }

    public int length() {
        return value.length();
    }

    public void focused(boolean focused) {
        this.focused = focused;
    }

    public boolean focused() {
        return focused;
    }

    public int scrollLine() {
        return scrollLine;
    }

    public void scrollLine(int scrollLine) {
        this.scrollLine = Math.max(0, scrollLine);
    }

    public void clampScroll(int maxScroll) {
        scrollLine = Math.max(0, Math.min(Math.max(0, maxScroll), scrollLine));
    }

    public void scrollBy(int delta, int maxScroll) {
        scrollLine = Math.max(0, Math.min(Math.max(0, maxScroll), scrollLine + delta));
    }

    public void scrollToBottom(int lineCount, int visibleLines) {
        scrollLine = Math.max(0, lineCount - Math.max(1, visibleLines));
    }

    public boolean caretVisible() {
        return ((System.currentTimeMillis() - startedAt) / 500L) % 2L == 0L;
    }

    public static boolean isAllowedTextCharacter(char character) {
        return character >= 32 && character != 127;
    }
}
