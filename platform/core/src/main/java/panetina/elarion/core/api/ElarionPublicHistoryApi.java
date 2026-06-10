package panetina.elarion.core.api;

import panetina.elarion.core.model.ChronicleArchive;
import panetina.elarion.core.model.PublicHistoryConsumer;
import panetina.elarion.core.model.PublicHistoryQuery;
import panetina.elarion.core.model.PublicHistoryResult;
import panetina.elarion.core.service.HistoryService;

import java.util.List;
import java.util.UUID;

public final class ElarionPublicHistoryApi {
    private final HistoryService history;

    ElarionPublicHistoryApi(HistoryService history) {
        this.history = history;
    }

    public PublicHistoryResult query(PublicHistoryQuery query) {
        return history.publicHistory(query);
    }

    public PublicHistoryResult newspaper(String realmId, int limit) {
        return query(PublicHistoryQuery.forConsumer(PublicHistoryConsumer.NEWSPAPER)
                .forRealm(realmId)
                .limitedTo(limit));
    }

    public PublicHistoryResult ledger(UUID playerId, int limit) {
        return query(PublicHistoryQuery.forConsumer(PublicHistoryConsumer.LEDGER)
                .forPlayer(playerId)
                .limitedTo(limit));
    }

    public PublicHistoryResult npcRumors(String realmId, int limit) {
        return query(PublicHistoryQuery.forConsumer(PublicHistoryConsumer.NPC_RUMOR)
                .forRealm(realmId)
                .limitedTo(limit));
    }

    public PublicHistoryResult search(String text, int limit) {
        return query(PublicHistoryQuery.forConsumer(PublicHistoryConsumer.GUI_SEARCH)
                .matchingText(text)
                .limitedTo(limit));
    }

    public List<ChronicleArchive> recentChronicles(int weeks) {
        return history.recentChronicles(weeks);
    }

    public List<ChronicleArchive> generateChronicles() {
        return history.generateWeeklyChronicleArchives();
    }
}
