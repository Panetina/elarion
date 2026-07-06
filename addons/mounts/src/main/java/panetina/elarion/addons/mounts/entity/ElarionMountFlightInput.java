package panetina.elarion.addons.mounts.entity;

record ElarionMountFlightInput(
        float forward,
        float sideways,
        boolean jump,
        boolean sneak,
        boolean boost,
        float turnIntent
) {
    static ElarionMountFlightInput neutral() {
        return new ElarionMountFlightInput(0.0F, 0.0F, false, false, false, 0.0F);
    }
}
