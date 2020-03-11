package com.technicalitiesmc.pneumatics.tube.route;

import com.technicalitiesmc.pneumatics.tube.MovingTubeStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;

import java.util.EnumMap;
import java.util.EnumSet;

public class RoundRobinRoutingStrategy implements RoutingStrategy {

    public static final RoundRobinRoutingStrategy INSTANCE = new RoundRobinRoutingStrategy();

    private final EnumMap<Direction, Direction> lastDirection = new EnumMap<>(Direction.class);

    @Override
    public Route calculateRoute(RoutingContext context, MovingTubeStack stack) {
        Direction from = stack.getFrom();

        EnumSet<Direction> routes = EnumSet.noneOf(Direction.class);
        for (Direction direction : Direction.values()) {
            if (direction == from) continue;
            if (!context.getPriority(direction, stack).isPresent()) continue;
            routes.add(direction);
        }

        if (routes.isEmpty()) return new SimpleRoute(true, null);
        if (routes.size() == 1) return new SimpleRoute(true, routes.iterator().next());

        if (context.isRemote()) return new SimpleRoute(false, null);

        Direction lastDirection = this.lastDirection.get(from);
        Direction direction = lastDirection == null ? routes.iterator().next() : findNext(lastDirection, routes);
        this.lastDirection.put(from, direction);
        return new SimpleRoute(false, direction);
    }

    @Override
    public CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();
        this.lastDirection.forEach((from, to) -> {
            tag.putInt(from.name().toLowerCase(), to.ordinal());
        });
        return tag;
    }

    @Override
    public void deserialize(CompoundNBT tag) {
        this.lastDirection.clear();
        for (Direction from : Direction.values()) {
            if (!tag.contains(from.name().toLowerCase())) continue;
            this.lastDirection.put(from, Direction.values()[tag.getInt(from.name().toLowerCase())]);
        }
    }

    private static Direction findNext(Direction current, EnumSet<Direction> values) {
        Direction prev = null;
        for (Direction d : values) {
            if (prev == current) return d;
            prev = d;
        }
        return values.iterator().next();
    }

}