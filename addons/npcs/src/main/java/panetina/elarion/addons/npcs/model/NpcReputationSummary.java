package panetina.elarion.addons.npcs.model;

public record NpcReputationSummary(int contactCount, long totalScore) {
    public static final NpcReputationSummary EMPTY = new NpcReputationSummary(0, 0L);

    public NpcReputationSummary {
        contactCount = Math.max(0, contactCount);
    }

    public int averageScore() {
        if (contactCount == 0) return 0;
        return (int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, totalScore / contactCount));
    }
}
