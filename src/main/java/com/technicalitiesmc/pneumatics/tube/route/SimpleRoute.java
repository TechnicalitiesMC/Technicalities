package com.technicalitiesmc.pneumatics.tube.route;

import net.minecraft.util.Direction;

public class SimpleRoute implements Route {

    public static final SimpleRoute NON_DETERMINISTIC = new SimpleRoute(false, null);

    private final boolean deterministic;
    private final Direction direction;

    public SimpleRoute(boolean deterministic, Direction direction) {
        this.deterministic = deterministic;
        this.direction = direction;
    }

    @Override
    public boolean isCalculated() {
        return true;
    }

    @Override
    public boolean isDeterministic() {
        return deterministic;
    }

    @Override
    public Direction getDirection() {
        return direction;
    }

}
