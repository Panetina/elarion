package panetina.elarion.addons.mounts.entity;

final class ElarionMountAnimationLogic {
    private ElarionMountAnimationLogic() {
    }

    static double smoothToward(double current, double target, double response) {
        double clampedResponse = Math.max(0.0D, Math.min(1.0D, response));
        return current + (target - current) * clampedResponse;
    }

    static double verticalIntentForInputs(boolean sneakInput, boolean jumpInput) {
        if (jumpInput) {
            return 1.0D;
        }
        if (sneakInput) {
            return -1.0D;
        }
        return 0.0D;
    }

    static String verticalOverlayForInputs(boolean sneakInput, boolean jumpInput) {
        if (jumpInput) {
            return "ascend";
        }
        if (sneakInput) {
            return "descend";
        }
        return "none";
    }
}
