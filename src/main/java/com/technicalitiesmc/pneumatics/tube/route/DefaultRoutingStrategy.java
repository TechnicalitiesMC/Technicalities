package com.technicalitiesmc.pneumatics.tube.route;

import com.technicalitiesmc.pneumatics.tube.FlowPriority;
import com.technicalitiesmc.lib.util.CollectionUtils;
import com.technicalitiesmc.pneumatics.tube.MovingTubeStack;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * This routing strategy prioritizes stacks travelling in a straight line.<br/>
 * If a stack can't travel in a straight line and there are multiple routes at
 * a 90deg angle, one will be picked at random.<br/>
 * Stacks that can't travel straight or make a 90deg turn will be marked as
 * unrouted.<br/>
 * <br/>
 * This routing strategy respects flow priorities.
 */
public class DefaultRoutingStrategy implements RoutingStrategy {

    public static final DefaultRoutingStrategy INSTANCE = new DefaultRoutingStrategy();

    @Nonnull
    @Override
    public Route calculateRoute(RoutingContext ctx, MovingTubeStack stack) {
        Direction from = stack.getFrom();
        Direction opposite = from.getOpposite();

        class PossibleRoute {
            private final Direction direction;
            private final boolean deterministic;
            private final FlowPriority priority;
            private final Lazy<Boolean> traversable;

            private PossibleRoute(Direction direction, FlowPriority priority, Lazy<Boolean> traversable) {
                this.direction = direction;
                this.deterministic = ctx.isDeterministic(direction);
                this.priority = priority;
                this.traversable = traversable;
            }

            private boolean isTraversable() {
                return traversable.get();
            }

        }

        // Work out which are our potential routes
        Map<Direction, PossibleRoute> routeMap = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.values()) {
            if (direction == from) continue;
            Optional<FlowPriority> priority = ctx.getPriority(direction, stack);
            if (!priority.isPresent()) continue;
            routeMap.put(direction, new PossibleRoute(direction, priority.get(), Lazy.of(() -> ctx.canTraverse(direction, stack))));
        }

        // If there aren't any routes, drop the stack
        if (routeMap.isEmpty()) {
            return new SimpleRoute(true, null);
        }

        // If there is just one route, pick that one
        if (routeMap.size() == 1) {
            PossibleRoute route = routeMap.values().iterator().next();
            if (!route.deterministic && ctx.isRemote()) return SimpleRoute.NON_DETERMINISTIC;
            if (!route.isTraversable()) return new SimpleRoute(route.deterministic, null);
            return new SimpleRoute(route.deterministic, route.direction);
        }

        // If there's multiple routes, try going straight first
        if (routeMap.containsKey(opposite)) {
            PossibleRoute route = routeMap.get(opposite);
            if (!route.deterministic && ctx.isRemote()) return SimpleRoute.NON_DETERMINISTIC;
            if (route.isTraversable() && route.priority == FlowPriority.NORMAL) {
                return new SimpleRoute(route.deterministic, route.direction);
            }
        }

        // If we didn't go straight, let's see if the other routes are all deterministic
        if (ctx.isRemote()) {
            for (PossibleRoute route : routeMap.values()) {
                if (!route.deterministic) return SimpleRoute.NON_DETERMINISTIC;
            }
        }

        // Discard non-traversable routes
        List<PossibleRoute> routes = new ArrayList<>(routeMap.values());
        routes.removeIf(r -> !r.isTraversable());

        // If there are no routes, drop the stack
        if (routes.isEmpty()) return new SimpleRoute(false, null);
        // If there is just one route, return it
        if (routes.size() == 1) return new SimpleRoute(false, routes.get(0).direction);

        // Sort by priority (normal priority first)
        routes.sort(Comparator.comparing(r -> r.priority));

        // If the first item has normal priority, discard all low priority options
        if (routes.get(0).priority == FlowPriority.NORMAL) {
            routes.removeIf(r -> r.priority != FlowPriority.NORMAL);
            // If there is just one normal priority route, return it
            if (routes.size() == 1) return new SimpleRoute(false, routes.get(0).direction);
        }

        // Try going straight once again if possible
        for (PossibleRoute route : routes) {
            if (route.direction != opposite) continue;
            return new SimpleRoute(false, route.direction);
        }

        // Pick a direction at random, since they're all perpendiculars
        PossibleRoute route = CollectionUtils.random(routes, ctx.getRandom());
        return new SimpleRoute(false, route.direction);
    }

}
