package com.technicalitiesmc.pneumatics.tube;

import com.technicalitiesmc.Technicalities;
import com.technicalitiesmc.pneumatics.network.StackJoinedTubePacket;
import com.technicalitiesmc.pneumatics.network.StackMutatedPacket;
import com.technicalitiesmc.pneumatics.network.StackRoutedPacket;
import com.technicalitiesmc.pneumatics.network.TubeNetworkHandler;
import com.technicalitiesmc.pneumatics.tube.route.DummyRoute;
import com.technicalitiesmc.pneumatics.tube.route.Route;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongConsumer;

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
        private final Long2ObjectMap<MovingTubeStack> stacks = new Long2ObjectOpenHashMap<>();

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
            synchronized (this.stacks) {
                if (this.stacks.put(stack.getID(), stack) != null) return; // Skip if already tracked
            }
            TubeNetworkHandler.INSTANCE.sendToAllWatching(
                new StackJoinedTubePacket(stack.getID(), stack.getPos(), stack.getFrom(), stack),
                world, stack.getPos()
            );
        }

        @Override
        public void onStacksLoaded(Set<MovingTubeStack> stacks) {
            for (MovingTubeStack stack : stacks) {
                synchronized (this.stacks) {
                    this.stacks.put(stack.getID(), stack);
                }
            }
        }

        @Override
        public void onStackMutated(long id, TubeStackMutation mutation) {
            BlockPos pos;
            synchronized (this.stacks) {
                pos = this.stacks.get(id).getPos();
            }
            TubeNetworkHandler.INSTANCE.sendToAllWatching(
                new StackMutatedPacket(id, mutation),
                world, pos
            );
        }

        @Override
        public void tick() {
            synchronized (this.stacks) {
                tickStacks(world, this.stacks);
            }
        }

        private static void tickStacks(World world, Long2ObjectMap<MovingTubeStack> stacks) {
            world.getProfiler().startSection(Technicalities.MODID + ".tube_stack_tick");
            LongList removed = new LongArrayList();
            for (MovingTubeStack stack : stacks.values()) {
                if (!stack.isValid()) {
                    removed.add(stack.getID());
                    continue;
                }
                stack.tick();
            }
            removed.forEach((LongConsumer) stacks::remove);
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

        private final Long2ObjectMap<Route> routes = new Long2ObjectOpenHashMap<>();
        private final Long2ObjectMap<MovingTubeStack> stacks = new Long2ObjectOpenHashMap<>();
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
            synchronized (routes) {
                Route existingRoute = routes.remove(id);
                if (existingRoute != null) return existingRoute;

                Route dummyRoute = new DummyRoute();
                routes.put(id, dummyRoute);
                return dummyRoute;
            }
        }

        @Override
        public void onStackRouted(long id, BlockPos pos, Route route) {
            synchronized (routes) {
                Route existingRoute = routes.remove(id);
                if (existingRoute instanceof DummyRoute) {
                    ((DummyRoute) existingRoute).updateRoute(route);
                } else {
                    routes.put(id, route);
                }
            }
        }

        @Override
        public void onStackJoinedTube(MovingTubeStack stack) {
            synchronized (this.stacks) {
                this.stacks.put(stack.getID(), stack);
            }
        }

        @Override
        public void onStacksLoaded(Set<MovingTubeStack> stacks) {
            for (MovingTubeStack stack : stacks) {
                synchronized (this.stacks) {
                    this.stacks.put(stack.getID(), stack);
                }
            }
        }

        @Override
        public void onStackMutated(long id, TubeStackMutation mutation) {
            MovingTubeStack stack;
            synchronized (this.stacks) {
                stack = this.stacks.get(id);
            }
            if (stack == null) return;
            stack.apply(mutation);
        }

        @Override
        public void tick() {
            synchronized (this.stacks) {
                Server.tickStacks(world, this.stacks);
            }
        }

    }

}
