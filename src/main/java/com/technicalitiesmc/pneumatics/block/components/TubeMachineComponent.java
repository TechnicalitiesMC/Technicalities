package com.technicalitiesmc.pneumatics.block.components;

import com.technicalitiesmc.api.tube.ITubeStack;
import com.technicalitiesmc.api.tube.ITubeStackConsumer;
import com.technicalitiesmc.lib.util.ColoredStack;
import com.technicalitiesmc.lib.block.Component;
import com.technicalitiesmc.lib.block.TKBlockComponent;
import com.technicalitiesmc.lib.block.TKBlockData;
import com.technicalitiesmc.lib.block.components.BlockDirection;
import com.technicalitiesmc.lib.inventory.InventoryUtils;
import net.minecraft.block.BlockState;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Component
public class TubeMachineComponent extends TKBlockComponent.WithNoData {

    @CapabilityInject(IItemHandler.class)
    private static Capability<IItemHandler> ITEM_HANDLER;
    @CapabilityInject(ITubeStackConsumer.class)
    private static Capability<ITubeStackConsumer> TUBE_STACK_CONSUMER;

    @Component
    private BlockDirection direction;

    private final StackProcessor processor;

    public TubeMachineComponent(StackProcessor processor) {
        this.processor = processor;
    }

    private ItemStack insertStack(IWorld world, BlockPos pos, BlockState state, ItemStack stack, DyeColor color, boolean simulate) {
        ProcessingResult result = processor.process(world, pos, new ColoredStack(stack, color), simulate);
        if (result == null) {
            return ItemStack.EMPTY;
        } else if (result instanceof RejectStack) {
            return stack.copy().split(((RejectStack) result).amount);
        } else if (!(result instanceof PassthroughStack)) {
            throw new IllegalStateException("A processing result must only be a rejection or passthrough request.");
        }

        ITubeStack outStack = ITubeStack
            .of(((PassthroughStack) result).stack.getStack())
            .withColor(((PassthroughStack) result).stack.getColor())
            .build();
        Direction front = direction.get(state);

        ITubeStackConsumer tsc = InventoryUtils.getNeighborTubeStackConsumer(world, pos, front);
        if (tsc != null) {
            int rejectedAmt = tsc.accept(outStack, simulate);
            return outStack.getStack().copy().split(rejectedAmt);
        }

        IItemHandler neighbor = InventoryUtils.getNeighborItemHandlerOrDrop(world, pos, front, 1);
        if (neighbor != null) {
            return InventoryUtils.transferStack(outStack.getStack(), neighbor, simulate);
        }

        return stack;
    }

    @Nonnull
    @Override
    protected <T> LazyOptional<T> getCapability(IWorld world, BlockPos pos, @Nullable Direction face, TKBlockData data, Capability<T> capability) {
        BlockState state = world.getBlockState(pos);
        Direction front = direction.get(state), back = front.getOpposite();

        if (face == front) {
            return ITEM_HANDLER.orEmpty(capability, LazyOptional.of(EmptyHandler::new));
        } else if (face == back) {
            LazyOptional<InputInventory> backCap = LazyOptional.of(() -> new InputInventory(world, pos, state));
            if (capability == ITEM_HANDLER) {
                return ITEM_HANDLER.orEmpty(capability, backCap.cast());
            } else if (capability == TUBE_STACK_CONSUMER) {
                return TUBE_STACK_CONSUMER.orEmpty(capability, backCap.cast());
            }
        }

        return LazyOptional.empty();
    }

    private class InputInventory implements IItemHandler, ITubeStackConsumer {

        private final IWorld world;
        private final BlockPos pos;
        private final BlockState state;

        private InputInventory(IWorld world, BlockPos pos, BlockState state) {
            this.world = world;
            this.pos = pos;
            this.state = state;
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            return insertStack(world, pos, state, stack, null, simulate);
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

        @Override
        public int accept(ITubeStack stack, boolean simulate) {
            return insertStack(world, pos, state, stack.getStack(), stack.getColor(), simulate).getCount();
        }

    }

    public interface StackProcessor {
        ProcessingResult process(IWorld world, BlockPos pos, ColoredStack stack, boolean simulate);
    }

    public static abstract class ProcessingResult {
    }

    public static final class RejectStack extends ProcessingResult {

        private final int amount;

        public RejectStack(int amount) {
            this.amount = amount;
        }

    }

    public static final class PassthroughStack extends ProcessingResult {

        private final ColoredStack stack;

        public PassthroughStack(ColoredStack stack) {
            this.stack = stack;
        }

    }

}
