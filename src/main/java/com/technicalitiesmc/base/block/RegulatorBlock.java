package com.technicalitiesmc.base.block;

import com.technicalitiesmc.base.container.RegulatorContainer;
import com.technicalitiesmc.base.item.ItemTagItem;
import com.technicalitiesmc.lib.block.Component;
import com.technicalitiesmc.lib.block.TKBlock;
import com.technicalitiesmc.lib.block.TKBlockData;
import com.technicalitiesmc.lib.block.components.BlockDirection;
import com.technicalitiesmc.lib.block.components.BlockInventory;
import com.technicalitiesmc.lib.block.components.ClickGUI;
import com.technicalitiesmc.lib.block.components.DirectionalPlacement;
import com.technicalitiesmc.lib.container.TKContainer;
import com.technicalitiesmc.lib.inventory.Inventory;
import com.technicalitiesmc.lib.inventory.InventoryUtils;
import com.technicalitiesmc.lib.inventory.ItemSet;
import com.technicalitiesmc.lib.serial.Serialize;
import com.technicalitiesmc.lib.util.CapabilityUtils;
import com.technicalitiesmc.lib.util.value.Value;
import com.technicalitiesmc.pneumatics.block.FilterBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

public class RegulatorBlock extends TKBlock.WithData<RegulatorBlock.Data> {

    @CapabilityInject(IItemHandler.class)
    private static Capability<IItemHandler> ITEM_HANDLER;

    @Component
    private final BlockDirection direction = new BlockDirection();
    @Component
    private final DirectionalPlacement placement = new DirectionalPlacement(true);
    @Component
    private final BlockInventory inventory = new BlockInventory();
    private final BlockInventory.Handle filter = inventory.addRegion("filter", RegulatorContainer.FILTER_SIZE);
    @Component
    private final ClickGUI gui = new ClickGUI.Container(RegulatorContainer.NAME, this::createContainer);

    public RegulatorBlock() {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(4.5F), Data::new);
    }

    private TKContainer createContainer(IWorld world, BlockPos pos, BlockState state, int id, PlayerInventory playerInv, PlayerEntity player) {
        return new RegulatorContainer(id, playerInv, filter.at(world, pos));
    }

    @Override
    protected boolean isNormalCube(IBlockReader world, BlockPos pos, BlockState state) {
        return false;
    }

    @Override
    protected void onAdded(World world, BlockPos pos, BlockState state, BlockState oldState, boolean moving) {
        world.getPendingBlockTicks().scheduleTick(pos, asMCBlock(), 0);
    }

    @Override
    protected void onScheduledTick(World world, BlockPos pos, BlockState state, Random rand) {
        Direction front = direction.get(state);
        IItemHandler inventory = CapabilityUtils.getCapability(world, pos.offset(front), ITEM_HANDLER, front.getOpposite());
        if (inventory == null) {
            scheduleNext(world, pos, getData(world, pos));
            return;
        }

        ItemSet allItems = InventoryUtils.collectInventory(inventory);
        NonNullList<ItemStack> filters = getPrioritizedFilters(filter.at(world, pos));

        for (ItemStack s : allItems) {
            ItemStack left = s.copy();
            for (ItemStack filter : filters) {
                if (FilterBlock.matchesFilter(s, filter)) {
                    int amt = Math.min(filter.getCount(), left.getCount());
                    filter.shrink(amt);
                    left.shrink(amt);
                    if (left.isEmpty()) break;
                }
            }
        }

        Data data = getData(world, pos);
        for (ItemStack filter : filters) {
            if (!filter.isEmpty()) {
                data.fulfilled.set(FulfilledState.NOT_FULFILLED);
                data.getHost().markDirty();
                scheduleNext(world, pos, data);
                return;
            }
        }
        data.fulfilled.set(FulfilledState.FULFILLED);
        data.getHost().markDirty();
        scheduleNext(world, pos, data);
    }

    private void scheduleNext(World world, BlockPos pos, Data data) {
        long now = world.getGameTime();
        if (now - data.lastTick >= 10) {
            data.lastTick = now;
            world.getPendingBlockTicks().scheduleTick(pos, asMCBlock(), 10);
        }
    }

    @Override
    protected boolean overridesComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected int getComparatorOutput(World world, BlockPos pos, BlockState state) {
        return getData(world, pos).fulfilled.get() == FulfilledState.FULFILLED ? 15 : 0;
    }

    @Nonnull
    @Override
    protected <T> LazyOptional<T> getCapability(IWorld world, BlockPos pos, @Nullable Direction face, Data data, Capability<T> capability) {
        BlockState state = world.getBlockState(pos);
        Direction front = direction.get(state);
        if (face != front) {
            return ITEM_HANDLER.orEmpty(capability, LazyOptional.of(() -> new ShadowInventory(world, pos, front)));
        }
        return LazyOptional.empty();
    }

    static class Data extends TKBlockData {
        @Serialize
        private final Value<FulfilledState> fulfilled = new Value<>(FulfilledState.NOT_FULFILLED);
        private long lastTick = 0;
    }

    private enum FulfilledState {
        NOT_FULFILLED, FULFILLED
    }

    private class ShadowInventory implements IItemHandler {

        private final IWorld world;
        private final BlockPos pos;
        private final Direction front;
        private Inventory filterInv;

        private ShadowInventory(IWorld world, BlockPos pos, Direction front) {
            this.world = world;
            this.pos = pos;
            this.front = front;
        }

        private IItemHandler getInventory() {
            return CapabilityUtils.getCapability(world, pos.offset(front), ITEM_HANDLER, front.getOpposite());
        }

        private Inventory getFilter() {
            if (filterInv != null) return filterInv;
            return filterInv = filter.at(world, pos);
        }

        @Nullable
        private ItemSet getInventoryItems() {
            IItemHandler inventory = getInventory();
            if (inventory == null) return null;
            return InventoryUtils.collectInventory(inventory);
        }

        private ItemSet getFilterItems() {
            return InventoryUtils.collectInventory(getFilter());
        }

        private NonNullList<ItemStack> getFilters() {
            return getPrioritizedFilters(getFilter());
        }

        private NonNullList<ItemStack> getMatchedFilters(ItemStack stack) {
            NonNullList<ItemStack> filters = NonNullList.create();
            for (ItemStack filter : getFilters()) {
                if (FilterBlock.matchesFilter(stack, filter)) {
                    filters.add(filter);
                }
            }
            return filters;
        }

        @Override
        public int getSlots() {
            IItemHandler inventory = getInventory();
            if (inventory == null) return 0;
            return inventory.getSlots() + RegulatorContainer.FILTER_SIZE;
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            if (slot < RegulatorContainer.FILTER_SIZE) return ItemStack.EMPTY;
            IItemHandler inventory = getInventory();
            if (inventory == null) return ItemStack.EMPTY;
            return inventory.getStackInSlot(slot - RegulatorContainer.FILTER_SIZE);
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (slot >= RegulatorContainer.FILTER_SIZE) return stack;

            NonNullList<ItemStack> filters = getFilters();
            if (filters.size() <= slot) return stack;
            ItemStack filterStack = filters.get(slot);
            if (!FilterBlock.matchesFilter(stack, filterStack)) {
                return stack;
            }

            ItemSet inventory = getInventoryItems();
            if (inventory == null) return stack;

            for (ItemStack s : inventory) {
                ItemStack left = s.copy();
                for (ItemStack filter : filters) {
                    if (FilterBlock.matchesFilter(s, filter)) {
                        int amt = Math.min(filter.getCount(), left.getCount());
                        filter.shrink(amt);
                        left.shrink(amt);
                        if (left.isEmpty()) break;
                    }
                }
            }
            if (filterStack.isEmpty()) return stack;

            ItemStack leftover = stack.copy();
            ItemStack allowed = leftover.split(filterStack.getCount());
            ItemStack insertLeftover = InventoryUtils.transferStack(allowed, getInventory(), simulate);
            if (!simulate) {
                world.getPendingBlockTicks().scheduleTick(pos, asMCBlock(), 0);
            }
            if (leftover.isEmpty()) return insertLeftover;
            leftover.grow(insertLeftover.getCount());
            return leftover;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot < RegulatorContainer.FILTER_SIZE) {
                return ItemStack.EMPTY;
            }

            IItemHandler inventory = getInventory();
            if (inventory == null) return ItemStack.EMPTY;

            int slotIdx = slot - RegulatorContainer.FILTER_SIZE;

            ItemStack stack = inventory.extractItem(slotIdx, 64, true);
            if (stack.isEmpty()) return ItemStack.EMPTY;

            NonNullList<ItemStack> matchedFilters = getMatchedFilters(stack);
            if (matchedFilters.isEmpty()) {
                return inventory.extractItem(slotIdx, amount, simulate);
            }

            for (int i = 0; i < slotIdx; i++) {
                ItemStack s = inventory.extractItem(i, 64, true);
                if (s.isEmpty()) continue;
                ItemStack left = s.copy();
                for (ItemStack filter : matchedFilters) {
                    if (FilterBlock.matchesFilter(s, filter)) {
                        int amt = Math.min(filter.getCount(), left.getCount());
                        filter.shrink(amt);
                        left.shrink(amt);
                        if (left.isEmpty()) break;
                    }
                }
            }

            int required = 0;
            for (ItemStack filter : matchedFilters) {
                required += filter.getCount();
            }
            int maxExtracted = stack.getCount() - required;
            if (maxExtracted <= 0) return ItemStack.EMPTY;

            if (!simulate) {
                world.getPendingBlockTicks().scheduleTick(pos, asMCBlock(), 0);
            }
            return inventory.extractItem(slotIdx, Math.min(maxExtracted, amount), simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            if (slot < RegulatorContainer.FILTER_SIZE) return getFilter().get(slot).getCount();
            IItemHandler inventory = getInventory();
            if (inventory == null) return 64;
            return inventory.getSlotLimit(slot - RegulatorContainer.FILTER_SIZE);
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            if (slot < RegulatorContainer.FILTER_SIZE) return FilterBlock.matchesFilter(stack, getFilter().get(slot));
            IItemHandler inventory = getInventory();
            if (inventory == null) return false;
            return inventory.isItemValid(slot - RegulatorContainer.FILTER_SIZE, stack);
        }

    }

    private static NonNullList<ItemStack> getPrioritizedFilters(Inventory filter) {
        NonNullList<ItemStack> filters = NonNullList.create();
        for (Inventory.Slot slot : filter) {
            ItemStack filterStack = slot.get();
            if (filterStack.isEmpty()) continue;
            if (filterStack.getItem() instanceof ItemTagItem) continue;
            filters.add(filterStack.copy());
        }
        for (Inventory.Slot slot : filter) {
            ItemStack filterStack = slot.get();
            if (filterStack.isEmpty()) continue;
            if (!(filterStack.getItem() instanceof ItemTagItem)) continue;
            filters.add(filterStack.copy());
        }
        return filters;
    }

}
