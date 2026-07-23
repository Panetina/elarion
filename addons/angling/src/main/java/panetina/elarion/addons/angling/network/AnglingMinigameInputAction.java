package panetina.elarion.addons.angling.network;

/** The complete client-authorized minigame input vocabulary. */
public enum AnglingMinigameInputAction {
    PRESS,
    RELEASE,
    LAYER_PREVIOUS,
    LAYER_NEXT,
    ABANDON,
    INVALID;

    static AnglingMinigameInputAction fromWire(int value) {
        return value >= 0 && value < INVALID.ordinal() ? values()[value] : INVALID;
    }
}
