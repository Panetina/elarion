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
            "city", "confederacy", "confederation", "empire", "federation", "holy",
            "kingdom", "land", "monarchy", "nation", "realm", "republic", "state", "theocracy"
    );

    private RealmIdentityRules() {
    }

    public static String validateName(String value) {
        String name = value == null ? "" : value.trim().replaceAll("\\s+", " ");
        if (name.length() < NAME_MIN_LENGTH || name.length() > NAME_MAX_LENGTH) {
            throw new IllegalArgumentException("Realm name must be 3-24 characters.");
        }
        if (name.split(" ").length > NAME_MAX_WORDS) {
            throw new IllegalArgumentException("Realm name may contain at most two words.");
        }
        String reserved = Arrays.stream(name.toLowerCase(Locale.ROOT).split("[^a-z0-9]+"))
                .filter(RESERVED_WORDS::contains)
                .findFirst()
                .orElse("");
        if (!reserved.isBlank()) {
            throw new IllegalArgumentException(
                    "Realm names cannot contain government or settlement terms such as " + reserved + ".");
        }
        return name;
    }

    public static String validateTag(String value) {
        String tag = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        if (!TAG.matcher(tag).matches()) {
            throw new IllegalArgumentException("Realm tag must be 2-6 uppercase letters or numbers.");
        }
        return tag;
    }
}
