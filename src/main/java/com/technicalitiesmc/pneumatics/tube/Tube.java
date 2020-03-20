package com.technicalitiesmc.pneumatics.tube;

import com.technicalitiesmc.api.tube.ITubeStack;
import com.technicalitiesmc.pneumatics.tube.route.Route;
import com.technicalitiesmc.pneumatics.tube.route.RoutingContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Tube {

    private final TubeHost host;
    private final TubeStackMutator mutator;
    private final RoutingContext routingContext = new LocalRoutingContext();

    private final Set<MovingTubeStack> stacks = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public Tube(TubeHost host, TubeStackMutator mutator) {
        this.host = host;
        this.mutator = mutator;
    }

    boolean isValid() {
        return host.isValid();
    }

    BlockPos getPos() {
        return host.getPos();
    }

    Route calculateRoute(MovingTubeStack stack) {
        Route route = host.getRoutingStrategy().calculateRoute(routingContext, stack);
        if (!route.isCalculated()) {
            throw new IllegalStateException("Routes, even if not deterministic, must always be calculated by the routing strategy.");
        }
        if (!route.isDeterministic() && host.getWorld().isRemote()) {
            return host.getManager().awaitRoute(stack.getID());
        }
        if (!host.getWorld().isRemote()) {
            host.getManager().onStackRouted(stack.getID(), host.getPos(), route);
        }
        host.markDirty();
        host.onStackRouted(stack);
        return route;
    }

    @Nullable
    TubeStackMutation getMutation(MovingTubeStack stack) {
        float offset = stack.getOffset();
        if (offset < 0.5F) return null; // Don't mutate incoming stacks;

        Direction direction = stack.getDirection(1);

        float nextOffset = offset + stack.getSpeed();
        if (offset >= 0.75F || 0.75F > nextOffset) return null;

        TubeStackMutation mutation = mutator.mutate(direction, stack);
        if (mutation != null) {
            host.getManager().onStackMutated(stack.getID(), mutation);
        }
        return mutation;
    }

    public void insertStack(Direction side, ITubeStack stack) {
        insertStack(side, stack instanceof MovingTubeStack ? ((MovingTubeStack) stack).getID() : host.getManager().getNextID(), stack);
    }

    public void insertStack(Direction side, long id, ITubeStack stack) {
        if (stack.getSpeed() == 0) {
            stack = ITubeStack.copyOf(stack).withSpeed(0.075F).build();
        }
        MovingTubeStack mts = stack instanceof MovingTubeStack ? ((MovingTubeStack) stack) : new MovingTubeStack(id, stack);
        mts.joinTube(this, side);
        stacks.add(mts);
        host.getManager().onStackJoinedTube(mts);
        host.markDirty();
    }

    public void tick() {
        boolean removedAny = stacks.removeIf(stack -> {
            if (stack.getOffset() < 1.0F && stack.isValid()) return false;
            stack.leaveTube();
            output(stack);
            return true;
        });
        if (removedAny) {
            host.markDirty();
        }
    }

    public void onLoaded() {
        host.getManager().onStacksLoaded(stacks);
    }

    public Set<MovingTubeStack> getStacks() {
        return stacks;
    }

    public CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();
        ListNBT stacksTag = stacks.stream()
            .map(MovingTubeStack::serialize)
            .collect(Collectors.toCollection(ListNBT::new));
        tag.put("stacks", stacksTag);
        return tag;
    }

    public void deserialize(CompoundNBT tag, boolean client) {
        ListNBT stacksTag = tag.getList("stacks", Constants.NBT.TAG_COMPOUND);

        for (MovingTubeStack stack : stacks) stack.leaveTube();
        stacks.clear();

        Set<MovingTubeStack> newStacks = IntStream.range(0, stacksTag.size())
            .mapToObj(stacksTag::getCompound)
            .map(t -> new MovingTubeStack(this, t))
            .collect(Collectors.toSet());
        stacks.addAll(newStacks);

        if (client) {
            host.getManager().onStacksLoaded(newStacks);
        }
    }

    private void output(MovingTubeStack stack) {
        Direction direction = stack.getTo();
        stack.advanceOffset();
        int leftover = host.output(stack, direction);
        if (leftover <= 0 || direction == null) return;
        ItemStack leftoverItems = stack.getStack().copy().split(leftover);
        insertStack(direction, ITubeStack.of(leftoverItems).withColor(stack.getColor()).build());
    }

    private class LocalRoutingContext implements RoutingContext {

        @Override
        public boolean isRemote() {
            return host.getWorld().isRemote();
        }

        @Override
        public Random getRandom() {
            return host.getWorld().getRandom();
        }

        @Override
        public boolean isDeterministic(Direction direction) {
            return host.isDeterministic(direction);
        }

        @Override
        public Optional<FlowPriority> getPriority(Direction direction, ITubeStack stack) {
            return host.getFlowPriority(direction, stack);
        }

        @Override
        public boolean canTraverse(Direction direction, ITubeStack stack) {
            return host.canTraverse(direction, stack);
        }

    }

}
