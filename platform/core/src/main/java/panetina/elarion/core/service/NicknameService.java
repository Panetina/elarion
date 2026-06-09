package panetina.elarion.core.service;

import panetina.elarion.core.config.CoreConfigManager;
import panetina.elarion.core.model.CitizenRecord;

import java.text.Normalizer;
import java.util.Locale;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public final class NicknameService {
    private static final Set<String> BUILT_IN_PROTECTED_NAMES = Set.of(
            "admin",
            "administrator",
            "moderator",
            "head moderator",
            "server owner",
            "staff",
            "staff team",
            "system",
            "console",
            "server",
            "server notice",
            "bot",
            "announcements",
            "announcement",
            "operator",
            "elarion"
    );

    private final CoreConfigManager config;
    private final CitizenService citizens;

    public NicknameService(CoreConfigManager config, CitizenService citizens) {
        this.config = config;
        this.citizens = citizens;
    }

    public Validation validate(UUID owner, String input) {
        if (!config.nicknamesEnabled()) {
            return Validation.invalid("Nicknames are disabled.");
        }

        String displayName = normalizeDisplay(input);
        if (displayName.isBlank()) {
            return Validation.invalid("Nickname cannot be empty.");
        }
        if (!hasAllowedCharacters(input)) {
            return Validation.invalid(
                    "Nickname may contain only letters, spaces, apostrophes, and hyphens.");
        }
        if (!hasValidStructure(displayName)) {
            return Validation.invalid("Apostrophes and hyphens must appear between letters.");
        }
        if (config.nicknameMaxLength() > 0
                && displayName.codePointCount(0, displayName.length()) > config.nicknameMaxLength()) {
            return Validation.invalid(
                    "Nickname cannot exceed " + config.nicknameMaxLength() + " characters.");
        }

        String key = comparisonKey(displayName);
        if (key.isBlank()) {
            return Validation.invalid("Nickname must contain at least one letter.");
        }
        if (config.nicknameProtectionEnabled()) {
            for (String protectedName : protectedNames()) {
                String protectedKey = comparisonKey(protectedName);
                if (protectedKey.isBlank()) continue;
                boolean matches = key.equals(protectedKey)
                        || config.nicknameRejectContainingProtectedName() && key.contains(protectedKey);
                if (matches) {
                    return Validation.invalid("That nickname uses protected official presentation.");
                }
            }
        }

        for (CitizenRecord citizen : citizens.all()) {
            if (citizen.uuid().equals(owner)) continue;
            if (config.nicknameUnique()
                    && citizen.nickname() != null
                    && key.equals(comparisonKey(citizen.nickname()))) {
                return Validation.invalid("That nickname is already in use.");
            }
            if (config.nicknameReservePlayerUsernames()
                    && citizen.lastKnownUsername() != null
                    && key.equals(comparisonKey(citizen.lastKnownUsername()))) {
                return Validation.invalid("That nickname conflicts with a player's username.");
            }
        }

        return Validation.valid(displayName);
    }

    private Set<String> protectedNames() {
        Set<String> names = new LinkedHashSet<>(BUILT_IN_PROTECTED_NAMES);
        names.addAll(config.nicknameReservedNames());
        if (config.nicknameProtectCommunityPresentation()) {
            config.communities().forEach((id, community) -> {
                names.add(id);
                names.add(community.displayName());
                names.add(community.shortName());
                names.add(community.prefix());
            });
        }
        if (config.nicknameProtectTitlePresentation()) {
            config.titles().forEach((id, title) -> {
                names.add(id);
                names.add(title.displayName());
                names.add(title.prefix());
                names.add(title.suffix());
            });
        }
        return names;
    }

    public static String comparisonKey(String value) {
        if (value == null) return "";
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKD)
                .toLowerCase(Locale.ROOT);
        StringBuilder result = new StringBuilder();
        normalized.codePoints().forEach(codePoint -> {
            if (Character.getType(codePoint) == Character.NON_SPACING_MARK) return;
            int mapped = confusableLatin(codePoint);
            if (Character.isLetter(mapped)) result.appendCodePoint(mapped);
        });
        return result.toString();
    }

    private static int confusableLatin(int codePoint) {
        return switch (codePoint) {
            // Cyrillic lookalikes commonly used to impersonate Latin names.
            case 'а', 'α' -> 'a';
            case 'в', 'β' -> 'b';
            case 'с' -> 'c';
            case 'д' -> 'd';
            case 'е', 'ε' -> 'e';
            case 'һ' -> 'h';
            case 'і', 'ι' -> 'i';
            case 'ј' -> 'j';
            case 'к', 'κ' -> 'k';
            case 'м', 'μ' -> 'm';
            case 'о', 'ο' -> 'o';
            case 'р', 'ρ' -> 'p';
            case 'ѕ' -> 's';
            case 'т', 'τ' -> 't';
            case 'у', 'υ' -> 'y';
            case 'х', 'χ' -> 'x';
            default -> codePoint;
        };
    }

    public static String normalizeDisplay(String value) {
        if (value == null) return "";
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC);
        StringBuilder result = new StringBuilder();
        boolean pendingSpace = false;
        boolean wordStart = true;
        for (int offset = 0; offset < normalized.length();) {
            int codePoint = normalized.codePointAt(offset);
            offset += Character.charCount(codePoint);
            if (Character.isWhitespace(codePoint)) {
                pendingSpace = result.length() > 0
                        && result.charAt(result.length() - 1) != '\''
                        && result.charAt(result.length() - 1) != '-';
                wordStart = true;
            } else if (isApostrophe(codePoint) || isHyphen(codePoint)) {
                pendingSpace = false;
                result.append(isApostrophe(codePoint) ? '\'' : '-');
                wordStart = true;
            } else {
                if (pendingSpace) result.append(' ');
                result.appendCodePoint(wordStart
                        ? Character.toTitleCase(codePoint)
                        : Character.toLowerCase(codePoint));
                pendingSpace = false;
                wordStart = false;
            }
        }
        return result.toString();
    }

    public static boolean hasAllowedCharacters(String value) {
        if (value == null) return false;
        return value.codePoints().allMatch(codePoint ->
                Character.isLetter(codePoint)
                        || Character.isWhitespace(codePoint)
                        || isApostrophe(codePoint)
                        || isHyphen(codePoint));
    }

    public static boolean hasValidStructure(String value) {
        int[] codePoints = value.codePoints().toArray();
        for (int index = 0; index < codePoints.length; index++) {
            if (!isApostrophe(codePoints[index]) && !isHyphen(codePoints[index])) continue;
            if (index == 0 || index == codePoints.length - 1
                    || !Character.isLetter(codePoints[index - 1])
                    || !Character.isLetter(codePoints[index + 1])) {
                return false;
            }
        }
        return true;
    }

    private static boolean isApostrophe(int codePoint) {
        return codePoint == '\'' || codePoint == '\u2019';
    }

    private static boolean isHyphen(int codePoint) {
        return codePoint == '-'
                || codePoint == '\u2010'
                || codePoint == '\u2011';
    }

    public record Validation(boolean valid, String nickname, String error) {
        private static Validation valid(String nickname) {
            return new Validation(true, nickname, "");
        }

        private static Validation invalid(String error) {
            return new Validation(false, "", error);
        }
    }
}
