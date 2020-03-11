package com.technicalitiesmc.pneumatics.tube;

import com.technicalitiesmc.api.tube.ITubeStack;
import net.minecraft.util.Direction;

import javax.annotation.Nullable;

public interface TubeStackMutator {

    @Nullable
    TubeStackMutation mutate(Direction direction, ITubeStack stack);

}
