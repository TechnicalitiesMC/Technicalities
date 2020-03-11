package com.technicalitiesmc.pneumatics.tube.route;

import com.technicalitiesmc.pneumatics.tube.MovingTubeStack;
import net.minecraft.nbt.CompoundNBT;

public interface RoutingStrategy {

    Route calculateRoute(RoutingContext context, MovingTubeStack stack);

    default CompoundNBT serialize() {
        return new CompoundNBT();
    }

    default void deserialize(CompoundNBT tag) {
    }

}
