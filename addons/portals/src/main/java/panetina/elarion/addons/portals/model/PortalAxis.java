package panetina.elarion.addons.portals.model;

import net.minecraft.util.math.Direction;

public enum PortalAxis {
    X(Direction.Axis.X),
    Y(Direction.Axis.Y),
    Z(Direction.Axis.Z);

    private final Direction.Axis minecraft;

    PortalAxis(Direction.Axis minecraft) {
        this.minecraft = minecraft;
    }

    public Direction.Axis minecraft() {
        return minecraft;
    }
}
