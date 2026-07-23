package panetina.elarion.addons.angling.minigame;

public enum AnglingServerMinigameStatus {
    ACTIVE,
    SUCCEEDED,
    FAILED,
    ABANDONED,
    EXPIRED;

    public boolean terminal() {
        return this != ACTIVE;
    }
}
