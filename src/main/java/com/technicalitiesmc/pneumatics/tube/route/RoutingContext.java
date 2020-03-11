package com.technicalitiesmc.pneumatics.tube.route;

import com.technicalitiesmc.pneumatics.tube.FlowPriority;
import com.technicalitiesmc.api.tube.ITubeStack;
import net.minecraft.util.Direction;

import java.util.Optional;
import java.util.Random;

public interface RoutingContext {

    boolean isRemote();

    Random getRandom();

    boolean isDeterministic(Direction direction);

    Optional<FlowPriority> getPriority(Direction direction, ITubeStack stack);

}
