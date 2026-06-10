package panetina.elarion.core.model;

import java.util.Set;
import java.util.UUID;

public record PublicHistoryQuery(
        PublicHistoryConsumer consumer,
        Set<String> categories,
        String realmId,
        UUID playerId,
        String text,
        int limit,
        int weeks,
        boolean includeArchives,
        boolean includeLiveIndex
) {
    public PublicHistoryQuery {
        consumer = consumer == null ? PublicHistoryConsumer.GUI_SEARCH : consumer;
        categories = categories == null ? Set.of() : Set.copyOf(categories);
        realmId = clean(realmId);
        text = clean(text);
    }

    public static PublicHistoryQuery forConsumer(PublicHistoryConsumer consumer) {
        return new PublicHistoryQuery(consumer, Set.of(), "", null, "", 0, 0, true, true);
    }

    public PublicHistoryQuery withCategories(Set<String> values) {
        return new PublicHistoryQuery(consumer, values, realmId, playerId, text,
                limit, weeks, includeArchives, includeLiveIndex);
    }

    public PublicHistoryQuery forRealm(String value) {
        return new PublicHistoryQuery(consumer, categories, value, playerId, text,
                limit, weeks, includeArchives, includeLiveIndex);
    }

    public PublicHistoryQuery forPlayer(UUID value) {
        return new PublicHistoryQuery(consumer, categories, realmId, value, text,
                limit, weeks, includeArchives, includeLiveIndex);
    }

    public PublicHistoryQuery matchingText(String value) {
        return new PublicHistoryQuery(consumer, categories, realmId, playerId, value,
                limit, weeks, includeArchives, includeLiveIndex);
    }

    public PublicHistoryQuery limitedTo(int value) {
        return new PublicHistoryQuery(consumer, categories, realmId, playerId, text,
                value, weeks, includeArchives, includeLiveIndex);
    }

    public PublicHistoryQuery withinWeeks(int value) {
        return new PublicHistoryQuery(consumer, categories, realmId, playerId, text,
                limit, value, includeArchives, includeLiveIndex);
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
