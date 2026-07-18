package panetina.elarion.core.placeholder;

import java.util.Locale;

public enum PlaceholderTransform {
    IDENTITY,
    UPPER,
    LOWER,
    TITLE;

    public String apply(String value) {
        String safe = value == null ? "" : value;
        return switch (this) {
            case IDENTITY -> safe;
            case UPPER -> safe.toUpperCase(Locale.ROOT);
            case LOWER -> safe.toLowerCase(Locale.ROOT);
            case TITLE -> titleCase(safe);
        };
    }

    private static String titleCase(String value) {
        StringBuilder result = new StringBuilder(value.length());
        boolean upper = true;
        for (char character : value.toCharArray()) {
            if (Character.isLetter(character)) {
                result.append(upper ? Character.toTitleCase(character) : Character.toLowerCase(character));
                upper = false;
            } else {
                result.append(character);
                upper = Character.isWhitespace(character) || character == '-' || character == '_';
            }
        }
        return result.toString();
    }
}
