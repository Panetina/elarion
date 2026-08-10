package panetina.elarion.addons.guilds.model;

/** One server-configured Guild level. Index in the ordered config is its level. */
public record GuildProgressionTier(long requiredContributions, int memberCapacity) {
    public GuildProgressionTier {
        requiredContributions = Math.max(0L, requiredContributions);
        memberCapacity = Math.max(1, memberCapacity);
    }
}
