package com.technicalitiesmc.pneumatics.tube;

import com.technicalitiesmc.Technicalities;
import com.technicalitiesmc.pneumatics.network.StackJoinedTubePacket;
import com.technicalitiesmc.pneumatics.network.StackMutatedPacket;
import com.technicalitiesmc.pneumatics.network.StackRoutedPacket;
import com.technicalitiesmc.pneumatics.network.TubeNetworkHandler;
import com.technicalitiesmc.pneumatics.tube.route.DummyRoute;
import com.technicalitiesmc.pneumatics.tube.route.Route;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public interface TubeManager {

    long getNextID();

    Route awaitRoute(long id);

    void onStackRouted(long id, BlockPos pos, Route route);

    void onStacksLoaded(Set<MovingTubeStack> stacks);

    void onStackJoinedTube(MovingTubeStack stack);

    void onStackMutated(long id, TubeStackMutation mutation);

    void tick();

    class Server extends WorldSavedData implements TubeManager {

        private final World world;
        private final Map<LongKey, MovingTubeStack> stacks = new ConcurrentHashMap<>();

        private final AtomicLong id = new AtomicLong();

        public Server(World world) {
            super(getName(world));
            this.world = world;
        }

        @Override
        public long getNextID() {
            long nextID = id.getAndIncrement();
            markDirty();
            return nextID;
        }

        @Override
        public Route awaitRoute(long id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void onStackRouted(long id, BlockPos pos, Route route) {
            if (route.isDeterministic()) return;
            TubeNetworkHandler.INSTANCE.sendToAllWatching(
                new StackRoutedPacket(id, pos, route),
                world, pos
            );
        }

        @Override
        public void onStackJoinedTube(MovingTubeStack stack) {
            if (this.stacks.put(new LongKey(stack.getID()), stack) != null) return; // Skip if already tracked
            TubeNetworkHandler.INSTANCE.sendToAllWatching(
                new StackJoinedTubePacket(stack.getID(), stack.getPos(), stack.getFrom(), stack),
                world, stack.getPos()
            );
        }

        @Override
        public void onStacksLoaded(Set<MovingTubeStack> stacks) {
            for (MovingTubeStack stack : stacks) {
                this.stacks.put(new LongKey(stack.getID()), stack);
            }
        }

        @Override
        public void onStackMutated(long id, TubeStackMutation mutation) {
            BlockPos pos = this.stacks.get(new LongKey(id)).getPos();
            TubeNetworkHandler.INSTANCE.sendToAllWatching(
                new StackMutatedPacket(id, mutation),
                world, pos
            );
        }

        @Override
        public void tick() {
            tickStacks(world, this.stacks);
        }

        private static void tickStacks(World world, Map<LongKey, MovingTubeStack> stacks) {
            world.getProfiler().startSection(Technicalities.MODID + ".tube_stack_tick");
            stacks.values().removeIf(stack -> {
                if (!stack.isValid()) return true;
                stack.tick();
                return false;
            });
            world.getProfiler().endSection();
        }

        @Nonnull
        @Override
        public CompoundNBT write(@Nonnull CompoundNBT tag) {
            tag.putLong("id", id.longValue());
            return tag;
        }

        @Override
        public void read(@Nonnull CompoundNBT tag) {
            id.set(tag.getLong("id"));
        }

        @Nonnull
        public static String getName(World world) {
            return Technicalities.MODID + ".tube_manager$" + world.getDimension().getType().getId();
        }

    }

    @OnlyIn(Dist.CLIENT)
    class Client implements TubeManager {

        private final Map<LongKey, Route> routes = new ConcurrentHashMap<>();
        private final Map<LongKey, MovingTubeStack> stacks = new ConcurrentHashMap<>();
        private final World world;

        public Client(World world) {
            this.world = world;
        }

        @Override
        public long getNextID() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Route awaitRoute(long id) {
            LongKey key = new LongKey(id);
            Route existingRoute = routes.remove(key);
            if (existingRoute != null) return existingRoute;

            Route dummyRoute = new DummyRoute();
            routes.put(key, dummyRoute);
            return dummyRoute;
        }

        @Override
        public void onStackRouted(long id, BlockPos pos, Route route) {
            LongKey key = new LongKey(id);
            Route existingRoute = routes.remove(key);
            if (existingRoute instanceof DummyRoute) {
                ((DummyRoute) existingRoute).updateRoute(route);
            } else {
                routes.put(key, route);
            }
        }

        @Override
        public void onStackJoinedTube(MovingTubeStack stack) {
            this.stacks.put(new LongKey(stack.getID()), stack);
        }

        @Override
        public void onStacksLoaded(Set<MovingTubeStack> stacks) {
            for (MovingTubeStack stack : stacks) {
                this.stacks.put(new LongKey(stack.getID()), stack);
            }
        }

        @Override
        public void onStackMutated(long id, TubeStackMutation mutation) {
            MovingTubeStack stack = this.stacks.get(new LongKey(id));
            if (stack == null) return;
            stack.apply(mutation);
        }

        @Override
        public void tick() {
            Server.tickStacks(world, this.stacks);
        }

    }

    final class LongKey {

        private final long key;

        public LongKey(long key) {
            this.key = key;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LongKey longKey = (LongKey) o;
            return key == longKey.key;
        }

        @Override
        public int hashCode() {
            return Long.hashCode(key);
        }

    }

}
