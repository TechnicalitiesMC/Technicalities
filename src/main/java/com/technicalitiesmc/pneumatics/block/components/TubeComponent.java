package com.technicalitiesmc.pneumatics.block.components;

import com.technicalitiesmc.api.tube.ITubeStack;
import com.technicalitiesmc.api.tube.ITubeStackConsumer;
import com.technicalitiesmc.lib.block.Component;
import com.technicalitiesmc.lib.block.TKBlockComponent;
import com.technicalitiesmc.lib.block.TKBlockData;
import com.technicalitiesmc.lib.block.TKBlockDataHost;
import com.technicalitiesmc.lib.block.components.BlockConnections;
import com.technicalitiesmc.lib.client.BindRenderer;
import com.technicalitiesmc.lib.inventory.InventoryUtils;
import com.technicalitiesmc.lib.util.CapabilityUtils;
import com.technicalitiesmc.pneumatics.client.TubeRenderer;
import com.technicalitiesmc.pneumatics.tube.*;
import com.technicalitiesmc.pneumatics.tube.module.TubeModule;
import com.technicalitiesmc.pneumatics.tube.route.RoutingStrategy;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

@Component
@BindRenderer(TubeRenderer.class)
public class TubeComponent extends TKBlockComponent.WithData<TubeComponent.Data> {

    @CapabilityInject(TubeManager.class)
    private static Capability<TubeManager> TUBE_MANAGER;
    @CapabilityInject(Tube.class)
    private static Capability<Tube> TUBE;

    @CapabilityInject(ITubeStackConsumer.class)
    private static Capability<ITubeStackConsumer> TUBE_STACK_CONSUMER;
    @CapabilityInject(IItemHandler.class)
    private static Capability<IItemHandler> ITEM_HANDLER;

    @Component
    private BlockConnections connections;
    @Component(optional = true)
    private TubeModulesComponent modules;

    public TubeComponent(Supplier<RoutingStrategy> routingStrategyFactory) {
        this(routingStrategyFactory, null);
    }

    public TubeComponent(Supplier<RoutingStrategy> routingStrategyFactory, RouteCallback stackRoutedCallback) {
        super((TKBlockDataHost host) -> new Data(
            host,
            routingStrategyFactory.get(),
            stackRoutedCallback
        ));
    }

    @Override
    protected NonNullList<ItemStack> getAdditionalDrops(World world, BlockPos pos, BlockState state) {
        NonNullList<ItemStack> list = NonNullList.create();
        Data data = getData(world, pos);
        if (data != null) {
            for (MovingTubeStack stack : data.tube.getStacks()) {
                list.add(stack.getStack());
            }
        }
        return list;
    }

    @Override
    protected void initData(IWorld world, BlockPos pos, Data data) {
        super.initData(world, pos, data);
        data.connections = connections;
        data.modules = modules;
    }

    @Override
    protected void tick(IWorld world, BlockPos pos, Data data) {
        data.tube.tick();
    }

    @Nonnull
    @Override
    protected <T> LazyOptional<T> getCapability(IWorld world, BlockPos pos, @Nullable Direction face, Data data, Capability<T> capability) {
        if (capability == TUBE) return TUBE.orEmpty(capability, LazyOptional.of(() -> data.tube));
        if (capability == TUBE_STACK_CONSUMER && face != null) {
            return TUBE_STACK_CONSUMER.orEmpty(capability, LazyOptional.of(() -> new Input(data, face)));
        }
        if (capability == ITEM_HANDLER && face != null) {
            return ITEM_HANDLER.orEmpty(capability, LazyOptional.of(() -> new Input(data, face)));
        }
        return super.getCapability(world, pos, face, data, capability);
    }

    public static class Data extends TKBlockData implements TubeHost, TubeStackMutator {

        private final TKBlockDataHost host;
        private final RoutingStrategy routingStrategy;
        private final RouteCallback stackRoutedCallback;
        private BlockConnections connections;
        private TubeModulesComponent modules;

        private final Tube tube = new Tube(this, this);

        private Data(TKBlockDataHost host, RoutingStrategy routingStrategy, RouteCallback stackRoutedCallback) {
            this.host = host;
            this.routingStrategy = routingStrategy;
            this.stackRoutedCallback = stackRoutedCallback;
        }

        @Override
        public World getWorld() {
            return host.getWorld();
        }

        @Override
        public BlockPos getPos() {
            return host.getPos();
        }

        @Override
        public TubeManager getManager() {
            return host.getWorld().getCapability(TUBE_MANAGER).orElse(null);
        }

        @Override
        public RoutingStrategy getRoutingStrategy() {
            return routingStrategy;
        }

        @Override
        public boolean isDeterministic(Direction direction) {
            if (modules != null) {
                TubeModule<?> tubeModule = modules.getModuleContainer(getWorld(), getPos()).get(direction);
                if (tubeModule != null) {
                    return tubeModule.isDeterministic();
                }
            }
            return true;
        }

        @Nonnull
        @Override
        public Optional<FlowPriority> getFlowPriority(Direction direction, ITubeStack stack) {
            if (modules != null) {
                TubeModule<?> tubeModule = modules.getModuleContainer(getWorld(), getPos()).get(direction);
                if (tubeModule != null) {
                    return tubeModule.getFlowPriority(stack);
                }
            }
            if (connections.isConnected(host.getWorld(), host.getPos(), direction)) {
                return Optional.of(FlowPriority.NORMAL);
            }
            return Optional.empty();
        }

        @Override
        public boolean canTraverse(Direction direction, ITubeStack stack) {
            if (modules != null) {
                TubeModule<?> tubeModule = modules.getModuleContainer(getWorld(), getPos()).get(direction);
                if (tubeModule != null) {
                    return tubeModule.canTraverse(stack);
                }
            }
            return true;
        }

        @Override
        public int output(ITubeStack stack, Direction side) {
            return TubeComponent.output(getWorld(), getPos(), side, stack, false);
        }

        @Override
        public void onStackRouted(ITubeStack stack) {
            if (stackRoutedCallback != null) {
                stackRoutedCallback.onStackRouted(getWorld(), getPos(), stack);
            }
        }

        @Override
        public void markDirty() {
            host.markDirty();
        }

        @Override
        public boolean isValid() {
            return host.isValid();
        }

        @Nullable
        @Override
        public TubeStackMutation mutate(Direction direction, ITubeStack stack) {
            return null;
        }

        public Set<MovingTubeStack> getStacks() {
            return tube.getStacks();
        }

        @Nonnull
        @Override
        protected CompoundNBT serialize() {
            CompoundNBT tag = super.serialize();
            tag.put("tube", tube.serialize());
            tag.put("routing_strategy", routingStrategy.serialize());
            return tag;
        }

        @Override
        protected void deserialize(CompoundNBT tag) {
            super.deserialize(tag);
            tube.deserialize(tag.getCompound("tube"), false);
            routingStrategy.deserialize(tag.getCompound("routing_strategy"));
        }

        @Nonnull
        @Override
        protected CompoundNBT serializeSync() {
            CompoundNBT tag = super.serializeSync();
            tag.put("tube", tube.serialize());
            return tag;
        }

        @Override
        protected void deserializeSync(CompoundNBT tag) {
            super.deserializeSync(tag);
            tube.deserialize(tag.getCompound("tube"), true);
        }

        @Override
        protected void onLoad() {
            tube.onLoaded();
        }

    }

    private static final class Input implements ITubeStackConsumer, IItemHandler {

        private final Data data;
        private final Direction side;

        private Input(Data data, Direction side) {
            this.data = data;
            this.side = side;
        }

        @Override
        public int accept(ITubeStack stack, boolean simulate) {
            if (simulate) return 0;
            data.tube.insertStack(side, stack);
            return 0;
        }

        @Override
        public int getSlots() {
            return 16;
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (simulate) return ItemStack.EMPTY;
            data.tube.insertStack(side, ITubeStack.of(stack).build());
            return ItemStack.EMPTY;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return true;
        }

    }

    public interface RouteCallback {
        void onStackRouted(World world, BlockPos pos, ITubeStack stack);
    }

    public static int output(World world, BlockPos pos, Direction side, ITubeStack stack, boolean simulate) {
        if (side == null) {
            if (world.isRemote()) return 0;
            if (!simulate) {
                Block.spawnAsEntity(world, pos, stack.getStack());
            }
            return 0;
        }

        ITubeStackConsumer tsc = CapabilityUtils.getCapability(world, pos.offset(side), TUBE_STACK_CONSUMER, side.getOpposite());
        if (!(tsc instanceof Input) && world.isRemote()) return 0;
        if (tsc != null) {
            return tsc.accept(stack, simulate);
        }
        if (world.isRemote()) return 0;
        IItemHandler itemHandler = CapabilityUtils.getCapability(world, pos.offset(side), ITEM_HANDLER, side.getOpposite());
        if (itemHandler != null) {
            return InventoryUtils.transferStack(stack.getStack(), itemHandler, simulate).getCount();
        }
        return 0;
    }

}

