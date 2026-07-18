package panetina.elarion.addons.government.model;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public final class RealmIdentityRules {
    public static final int NAME_MIN_LENGTH = 3;
    public static final int NAME_MAX_LENGTH = 24;
    public static final int NAME_MAX_WORDS = 2;
    private static final Pattern TAG = Pattern.compile("[A-Z0-9]{2,6}");
    private static final Set<String> RESERVED_WORDS = Set.of(
            "city", "empire", "kingdom", "land", "monarchy", "nation", "realm", "republic", "state"
    );

    private RealmIdentityRules() {
    }

    public static String validateName(String value) {
        String name = value == null ? "" : value.trim().replaceAll("\\s+", " ");
        if (name.length() < NAME_MIN_LENGTH || name.length() > NAME_MAX_LENGTH) {
            throw new IllegalArgumentException("Realm name must be 3-24 characters.");
        }
        String[] words = name.split(" ");
        if (words.length < 1 || words.length > NAME_MAX_WORDS) {
            throw new IllegalArgumentException("Realm name must contain one or two words.");
        }
        for (String word : words) {
            if (word.isBlank() || !word.codePoints().allMatch(Character::isLetter)) {
                throw new IllegalArgumentException("Realm name words may only contain letters.");
            }
        }
        String formatted = Arrays.stream(words)
                .map(RealmIdentityRules::titleCaseWord)
                .reduce((left, right) -> left + " " + right)
                .orElse("");
        String reserved = Arrays.stream(formatted.toLowerCase(Locale.ROOT).split("[^a-z0-9]+"))
                .filter(RESERVED_WORDS::contains)
                .findFirst()
                .orElse("");
        if (!reserved.isBlank()) {
            throw new IllegalArgumentException(
                    "Realm names cannot contain government or settlement terms such as " + reserved + ".");
        }
        return formatted;
    }

    public static String validateTag(String value) {
        String tag = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        if (!TAG.matcher(tag).matches()) {
            throw new IllegalArgumentException("Realm tag must be 2-6 uppercase letters or numbers.");
        }
        return tag;
    }

    private static String titleCaseWord(String word) {
        String lower = word.toLowerCase(Locale.ROOT);
        int first = lower.codePointAt(0);
        int firstLength = Character.charCount(first);
        StringBuilder builder = new StringBuilder();
        builder.appendCodePoint(Character.toTitleCase(first));
        builder.append(lower.substring(firstLength));
        return builder.toString();
    }
}
