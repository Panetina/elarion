package panetina.elarion.addons.guilds.model;

import java.util.ArrayList;
import java.util.List;

/** Immutable, ascending Guild progression contract owned by the Guilds config. */
public record GuildProgressionConfig(List<GuildProgressionTier> tiers) {
    public GuildProgressionConfig {
        ArrayList<GuildProgressionTier> normalized = new ArrayList<>();
        long previousRequired = -1L;
        if (tiers != null) {
            for (GuildProgressionTier tier : tiers) {
                if (tier == null || tier.requiredContributions() < previousRequired) continue;
                normalized.add(tier);
                previousRequired = tier.requiredContributions();
            }
        }
        if (normalized.isEmpty() || normalized.getFirst().requiredContributions() != 0L) {
            normalized = new ArrayList<>(defaults().tiers());
        }
        tiers = List.copyOf(normalized);
    }

    public int levelFor(long contributions) {
        int level = 1;
        for (int index = 1; index < tiers.size(); index++) {
            if (contributions < tiers.get(index).requiredContributions()) break;
            level = index + 1;
        }
        return level;
    }

    public GuildProgressionTier tierFor(long contributions) {
        return tiers.get(levelFor(contributions) - 1);
    }

    public static GuildProgressionConfig defaults() {
        return new GuildProgressionConfig(List.of(
                new GuildProgressionTier(0L, 10),
                new GuildProgressionTier(250L, 15),
                new GuildProgressionTier(750L, 20),
                new GuildProgressionTier(1_750L, 30),
                new GuildProgressionTier(3_500L, 40)));
    }
}
