package com.technicalitiesmc.pneumatics.tube.route;

import com.technicalitiesmc.pneumatics.tube.MovingTubeStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayDeque;
import java.util.EnumSet;
import java.util.Queue;

public class RoundRobinRoutingStrategy implements RoutingStrategy {

    public static final RoundRobinRoutingStrategy INSTANCE = new RoundRobinRoutingStrategy();

    private final Queue<Direction> visitOrder = new ArrayDeque<>();

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

        Direction nextDirection = null;

        EnumSet<Direction> unvisitedRoutes = EnumSet.copyOf(routes);
        unvisitedRoutes.removeAll(visitOrder);
        if (!unvisitedRoutes.isEmpty()) {
            nextDirection = unvisitedRoutes.iterator().next();
            visitOrder.add(nextDirection);
        }

        if(nextDirection == null) {
            for (Direction possibleDirection : visitOrder) {
                if (routes.contains(possibleDirection)) {
                    nextDirection = possibleDirection;
                    // Push to the end of the queue
                    visitOrder.remove(nextDirection);
                    visitOrder.add(nextDirection);
                    break;
                }
            }
        }

        return new SimpleRoute(false, nextDirection);
    }

    @Override
    public CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();
        ListNBT list = new ListNBT();
        for (Direction direction : this.visitOrder) {
            list.add(IntNBT.valueOf(direction.ordinal()));
        }
        tag.put("visit_order", list);
        return tag;
    }

    @Override
    public void deserialize(CompoundNBT tag) {
        this.visitOrder.clear();
        ListNBT list = tag.getList("visit_order", Constants.NBT.TAG_INT);
        for (int i = 0; i < list.size(); i++) {
            this.visitOrder.add(Direction.values()[list.getInt(i)]);
        }
    }

}