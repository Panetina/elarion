package panetina.elarion.core.command;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;

import java.util.List;
import java.util.Objects;

public final class AdvancedIdentitySuggestion extends Suggestion {
    private final String completion;
    private final List<String> aliases;

    public AdvancedIdentitySuggestion(
            StringRange range,
            String display,
            String completion,
            List<String> aliases,
            Message tooltip
    ) {
        super(range, display, tooltip);
        this.completion = completion;
        this.aliases = List.copyOf(aliases);
    }

    public AdvancedIdentitySuggestion(
            StringRange range,
            String display,
            String completion,
            List<String> aliases
    ) {
        this(range, display, completion, aliases, null);
    }

    public String completion() {
        return completion;
    }

    @Override
    public String apply(String input) {
        StringRange range = getRange();
        if (range.getStart() == 0 && range.getEnd() == input.length()) {
            return completion;
        }

        StringBuilder result = new StringBuilder();
        if (range.getStart() > 0) {
            result.append(input, 0, range.getStart());
        }
        result.append(completion);
        if (range.getEnd() < input.length()) {
            result.append(input, range.getEnd(), input.length());
        }
        return result.toString();
    }

    @Override
    public Suggestion expand(String command, StringRange range) {
        if (range.equals(getRange())) {
            return this;
        }

        return new AdvancedIdentitySuggestion(
                range,
                expandText(command, range, getText()),
                expandText(command, range, completion),
                aliases,
                getTooltip());
    }

    @Override
    public boolean equals(Object value) {
        if (this == value) return true;
        if (!(value instanceof AdvancedIdentitySuggestion that)) return false;
        return super.equals(value)
                && Objects.equals(completion, that.completion)
                && Objects.equals(aliases, that.aliases);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), completion, aliases);
    }

    @Override
    public int compareTo(Suggestion other) {
        return displaySortValue().compareTo(other.getText());
    }

    @Override
    public int compareToIgnoreCase(Suggestion other) {
        return displaySortValue().compareToIgnoreCase(other.getText());
    }

    private String displaySortValue() {
        return aliases.isEmpty() ? getText() : aliases.getFirst();
    }

    private String expandText(String command, StringRange expandedRange, String value) {
        StringBuilder result = new StringBuilder();
        if (expandedRange.getStart() < getRange().getStart()) {
            result.append(command, expandedRange.getStart(), getRange().getStart());
        }
        result.append(value);
        if (expandedRange.getEnd() > getRange().getEnd()) {
            result.append(command, getRange().getEnd(), expandedRange.getEnd());
        }
        return result.toString();
    }
}
