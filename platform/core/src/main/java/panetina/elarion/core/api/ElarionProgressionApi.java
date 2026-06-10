package panetina.elarion.core.api;

import panetina.elarion.core.service.HistoryService;
import panetina.elarion.core.service.PlayerStatsService;
import panetina.elarion.core.service.ProgressionService;
import panetina.elarion.core.service.RewardActionService;

public final class ElarionProgressionApi {
    private final PlayerStatsService playerStats;
    private final ProgressionService progression;
    private final RewardActionService rewards;
    private final HistoryService history;

    ElarionProgressionApi(
            PlayerStatsService playerStats,
            ProgressionService progression,
            RewardActionService rewards,
            HistoryService history
    ) {
        this.playerStats = playerStats;
        this.progression = progression;
        this.rewards = rewards;
        this.history = history;
    }

    public PlayerStatsService playerStats() {
        return playerStats;
    }

    public ProgressionService progression() {
        return progression;
    }

    public RewardActionService rewards() {
        return rewards;
    }

    public HistoryService history() {
        return history;
    }
}
