package com.technicalitiesmc.pneumatics.tube.route;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;

public interface Route {

    boolean isCalculated();

    boolean isDeterministic();

    Direction getDirection();

    static CompoundNBT serialize(Route route) {
        CompoundNBT tag = new CompoundNBT();
        tag.putBoolean("deterministic", route.isDeterministic());
        Direction direction = route.getDirection();
        if(direction != null) tag.putInt("direction", direction.ordinal());
        return tag;
    }

    static Route deserialize(CompoundNBT tag) {
        return new SimpleRoute(
            tag.getBoolean("deterministic"),
            Direction.values()[tag.getInt("direction")]
        );
    }

}
