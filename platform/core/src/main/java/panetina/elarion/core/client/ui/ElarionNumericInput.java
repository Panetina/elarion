package panetina.elarion.core.client.ui;

public final class ElarionNumericInput {
    private final int maxDigits;
    private final long startedAt;
    private String value = "";

    public ElarionNumericInput(int maxDigits) {
        this.maxDigits = Math.max(1, Math.min(maxDigits, 10));
        this.startedAt = System.currentTimeMillis();
    }

    public boolean type(char character) {
        if (character < '0' || character > '9' || value.length() >= maxDigits) return false;
        value += character;
        return true;
    }

    public boolean backspace() {
        if (value.isEmpty()) return false;
        value = value.substring(0, value.length() - 1);
        return true;
    }

    public void clear() {
        value = "";
    }

    public String value() {
        return value;
    }

    public boolean empty() {
        return value.isEmpty();
    }

    public boolean caretVisible() {
        return ((System.currentTimeMillis() - startedAt) / 450L) % 2L == 0L;
    }
}
