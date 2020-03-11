package com.technicalitiesmc.pneumatics.tube.route;

import net.minecraft.util.Direction;

public final class DummyRoute implements Route {

    private Route parent;

    public void updateRoute(Route route) {
        this.parent = route;
    }

    @Override
    public boolean isCalculated() {
        return parent != null;
    }

    @Override
    public boolean isDeterministic() {
        return parent != null && parent.isDeterministic();
    }

    @Override
    public Direction getDirection() {
        return parent != null ? parent.getDirection() : null;
    }

}
