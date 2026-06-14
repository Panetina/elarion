package panetina.elarion.core.model;

import java.util.Locale;
import java.util.Map;

public record ServerIdentityConfig(
        String serverName,
        String capitalName,
        String treasuryName,
        String sealName,
        String realmSingular,
        String realmPlural,
        String currencySingular,
        String currencyPlural,
        String offeringSingular,
        String offeringPlural,
        String shrineOfFoundation,
        String localChatLabel,
        String realmChatLabel,
        String allianceChatLabel
) {
    public static ServerIdentityConfig defaults() {
        return new ServerIdentityConfig(
                "Elarion",
                "Worldheart",
                "Worldheart Treasury",
                "Elarion Seal",
                "Realm",
                "Realms",
                "Sigil",
                "Sigils",
                "Offering",
                "Offerings",
                "Shrine of Foundation",
                "Local",
                "Realm",
                "Alliance"
        );
    }

    public String currency(long amount) {
        return amount == 1 ? currencySingular : currencyPlural;
    }

    public String currencyAmount(long amount) {
        return amount + " " + currency(amount).toLowerCase(Locale.ROOT);
    }

    public String realmLabel(String realmId) {
        return realmSingular + " " + (realmId == null || realmId.isBlank() ? "unknown" : realmId);
    }

    public String replace(String value) {
        if (value == null || value.isBlank()) return "";
        String result = value;
        for (Map.Entry<String, String> entry : placeholders().entrySet()) {
            result = result
                    .replace("%" + entry.getKey() + "%", entry.getValue())
                    .replace("%" + entry.getKey() + "_title%", titleCase(entry.getValue()))
                    .replace("%" + entry.getKey() + "_upper%", entry.getValue().toUpperCase(Locale.ROOT))
                    .replace("%" + entry.getKey() + "_lower%", entry.getValue().toLowerCase(Locale.ROOT));
        }
        return result;
    }

    private static String titleCase(String value) {
        if (value == null || value.isBlank()) return "";
        StringBuilder result = new StringBuilder(value.length());
        boolean nextUpper = true;
        for (char character : value.toCharArray()) {
            if (Character.isLetter(character)) {
                result.append(nextUpper ? Character.toTitleCase(character) : Character.toLowerCase(character));
                nextUpper = false;
            } else {
                result.append(character);
                nextUpper = Character.isWhitespace(character) || character == '-' || character == '_';
            }
        }
        return result.toString();
    }

    public Map<String, String> placeholders() {
        return Map.ofEntries(
                Map.entry("server", serverName),
                Map.entry("capital", capitalName),
                Map.entry("treasury", treasuryName),
                Map.entry("seal", sealName),
                Map.entry("realm_term", realmSingular),
                Map.entry("realms_term", realmPlural),
                Map.entry("currency", currencySingular),
                Map.entry("currency_plural", currencyPlural),
                Map.entry("offering", offeringSingular),
                Map.entry("offerings", offeringPlural),
                Map.entry("shrine_of_foundation", shrineOfFoundation),
                Map.entry("local_chat", localChatLabel),
                Map.entry("realm_chat", realmChatLabel),
                Map.entry("alliance_chat", allianceChatLabel)
        );
    }
}
