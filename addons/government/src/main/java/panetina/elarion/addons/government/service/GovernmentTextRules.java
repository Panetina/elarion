package panetina.elarion.addons.government.service;

import panetina.elarion.addons.government.model.GovernmentVoteType;

import java.util.List;
import java.util.Set;

final class GovernmentTextRules {
    private static final List<String> REALM_COLORS = List.of(
            "dark_red", "red", "gold", "yellow", "dark_green", "green", "aqua", "dark_aqua",
            "dark_blue", "blue", "light_purple", "dark_purple", "white", "gray", "dark_gray", "black");
    private static final Set<String> PROPOSAL_CATEGORIES = Set.of(
            "law", "realm_project", "civic_rule", "other");
    private static final Set<String> RECORD_CATEGORIES = Set.of(
            "law", "public_notice", "realm_project", "civic_rule", "other");

    private GovernmentTextRules() {
    }

    static List<String> realmColors() {
        return REALM_COLORS;
    }

    static String validateProposalCategory(String category) {
        String clean = normalize(category);
        if (!PROPOSAL_CATEGORIES.contains(clean)) {
            throw new IllegalArgumentException("Unknown proposal category " + category + ".");
        }
        return clean;
    }

    static String validateRecordCategory(String category) {
        String clean = normalize(category);
        if (!RECORD_CATEGORIES.contains(clean)) {
            throw new IllegalArgumentException("Unknown civic record category " + category + ".");
        }
        return clean;
    }

    static String validateShortText(String value, String label, int min, int max) {
        String clean = value == null ? "" : value.trim().replaceAll("\\s+", " ");
        if (clean.length() < min || clean.length() > max) {
            throw new IllegalArgumentException(label + " must be " + min + "-" + max + " characters.");
        }
        return clean;
    }

    static String validateFaithName(String value) {
        String clean = value == null ? "" : value.trim().replaceAll("\\s+", " ");
        if (clean.length() < 3 || clean.length() > 40) {
            throw new IllegalArgumentException("Faith name must be 3-40 characters.");
        }
        if (clean.split(" ").length > 5) {
            throw new IllegalArgumentException("Faith name can use at most five words.");
        }
        if (!clean.matches("[A-Za-z0-9 ':-]+")) {
            throw new IllegalArgumentException("Faith name can only use letters, numbers, spaces, apostrophes, hyphens, and colons.");
        }
        return clean;
    }

    static String validateFaithTag(String value) {
        String clean = value == null ? "" : value.trim().toUpperCase(java.util.Locale.ROOT);
        if (clean.length() < 2 || clean.length() > 6 || !clean.matches("[A-Z0-9]+")) {
            throw new IllegalArgumentException("Faith mark must be 2-6 uppercase letters or numbers.");
        }
        return clean;
    }

    static String normalizeColor(String color) {
        String normalized = normalize(color).replace(' ', '_');
        if (!REALM_COLORS.contains(normalized)) {
            throw new IllegalArgumentException("Unknown Realm color " + color + ".");
        }
        return normalized;
    }

    static String colorLabel(String color) {
        String normalized = normalizeColor(color);
        String[] parts = normalized.split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) continue;
            if (!builder.isEmpty()) builder.append(' ');
            builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return builder.toString();
    }

    static String recordTypeLabel(String category) {
        return switch (normalize(category)) {
            case "law" -> "Law";
            case "public_notice" -> "Notice";
            case "civic_rule" -> "Rule";
            case "realm_project" -> "Project Record";
            default -> "Civic Record";
        };
    }

    static String voteTitle(GovernmentVoteType type) {
        return switch (type) {
            case REALM_NAME -> "Realm Name Voting Open";
            case REALM_COLOR -> "Realm Color Voting Open";
            case GOVERNMENT_FORM -> "Government Form Voting Open";
            case THEOCRACY_FAITH -> "Theocracy Faith Voting Open";
            case FOUNDING_ELECTION -> "Founding Election Open";
        };
    }

    static String voteBody(GovernmentVoteType type) {
        return switch (type) {
            case REALM_NAME -> "Review the proposed names and cast one private ballot.";
            case REALM_COLOR -> "Choose the color that will represent the Realm publicly.";
            case GOVERNMENT_FORM -> "Choose the form that will govern the Realm.";
            case THEOCRACY_FAITH -> "Choose the faith identity the High Priest and Synod will represent.";
            case FOUNDING_ELECTION -> "Vote for the Realm's founding authority holders.";
        };
    }

    static String nextId(Set<String> existing, String prefix, String title) {
        String slug = title == null ? "entry" : title.toLowerCase(java.util.Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        if (slug.isBlank()) slug = "entry";
        slug = slug.length() > 24 ? slug.substring(0, 24).replaceAll("_+$", "") : slug;
        String base = prefix + slug;
        String id = base;
        int suffix = 2;
        while (existing.contains(id)) {
            id = base + "_" + suffix++;
        }
        return id;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
