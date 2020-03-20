package com.technicalitiesmc.pneumatics.tube.module;

import net.minecraft.util.Direction;

public interface TubeModuleProvider {
    TubeModule<?> create(TubeModule.Context context, Direction side);
}
