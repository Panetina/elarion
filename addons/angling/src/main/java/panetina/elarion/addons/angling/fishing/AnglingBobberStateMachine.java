package panetina.elarion.addons.angling.fishing;

import java.util.Objects;
import java.util.function.DoubleSupplier;

/**
 * Server-only transition core for one bobber. World physics and reward side
 * effects remain in the owning entity, while timing is deterministic here.
 */
public final class AnglingBobberStateMachine {
    public static final int BITE_WINDOW_TICKS = 80;
    public static final int VOID_FLIGHT_TICKS = 50;

    private final int minimumTicksToFish;
    private final int maximumTicksToFish;
    private final double chancePerTick;
    private final boolean noGravity;

    private AnglingBobberState state = AnglingBobberState.FLYING;
    private int flyingTicks;
    private int ticksInFluid;
    private int bitingTicks;
    private boolean biteExpired;

    public AnglingBobberStateMachine(
            int minimumTicksToFish,
            int maximumTicksToFish,
            double chancePerTick,
            boolean noGravity
    ) {
        if (minimumTicksToFish < 1 || maximumTicksToFish < 1
                || minimumTicksToFish > maximumTicksToFish
                || !Double.isFinite(chancePerTick) || chancePerTick < 0.0 || chancePerTick > 1.0) {
            throw new IllegalArgumentException("Invalid bobber timing specification");
        }
        this.minimumTicksToFish = minimumTicksToFish;
        this.maximumTicksToFish = maximumTicksToFish;
        this.chancePerTick = chancePerTick;
        this.noGravity = noGravity;
    }

    public Transition tick(boolean inFluid, DoubleSupplier randomUnit) {
        Objects.requireNonNull(randomUnit, "randomUnit");
        if (biteExpired || state == AnglingBobberState.FISHING) return Transition.NONE;

        if (state == AnglingBobberState.FLYING) {
            flyingTicks++;
            if (inFluid || noGravity && flyingTicks > VOID_FLIGHT_TICKS) {
                state = AnglingBobberState.BOBBING;
                return Transition.STARTED_BOBBING;
            }
            return Transition.NONE;
        }

        if (!inFluid && !noGravity) {
            state = AnglingBobberState.FLYING;
            flyingTicks = 0;
            ticksInFluid = 0;
            return Transition.STARTED_FLYING;
        }

        if (state == AnglingBobberState.BOBBING) {
            ticksInFluid++;
            double roll = randomUnit.getAsDouble();
            if (!Double.isFinite(roll) || roll < 0.0 || roll >= 1.0) {
                throw new IllegalArgumentException("Bobber random source must return [0, 1)");
            }
            if ((roll < chancePerTick || ticksInFluid > maximumTicksToFish)
                    && ticksInFluid > minimumTicksToFish) {
                state = AnglingBobberState.BITING;
                bitingTicks = 0;
                return Transition.BITE_STARTED;
            }
            return Transition.NONE;
        }

        bitingTicks++;
        if (bitingTicks > BITE_WINDOW_TICKS) {
            biteExpired = true;
            return Transition.BITE_EXPIRED;
        }
        return Transition.NONE;
    }

    public boolean reel() {
        if (biteExpired || state != AnglingBobberState.BITING) return false;
        state = AnglingBobberState.FISHING;
        return true;
    }

    public AnglingBobberState state() { return state; }
    public int ticksInFluid() { return ticksInFluid; }
    public int bitingTicks() { return bitingTicks; }
    public boolean biteExpired() { return biteExpired; }
    public boolean noGravity() { return noGravity; }

    public enum Transition {
        NONE,
        STARTED_FLYING,
        STARTED_BOBBING,
        BITE_STARTED,
        BITE_EXPIRED
    }
}
