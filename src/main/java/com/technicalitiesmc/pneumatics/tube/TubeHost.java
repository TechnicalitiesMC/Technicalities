package com.technicalitiesmc.pneumatics.tube;

import com.technicalitiesmc.api.tube.ITubeStack;
import com.technicalitiesmc.pneumatics.tube.route.RoutingStrategy;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Optional;

public interface TubeHost {

    World getWorld();

    BlockPos getPos();

    TubeManager getManager();

    RoutingStrategy getRoutingStrategy();

    boolean isDeterministic(Direction direction);

    @Nonnull
    Optional<FlowPriority> getFlowPriority(Direction direction, ITubeStack stack);

    int output(ITubeStack stack, Direction side);

    void onStackRouted(ITubeStack stack);

    void markDirty();

    boolean isValid();

}
