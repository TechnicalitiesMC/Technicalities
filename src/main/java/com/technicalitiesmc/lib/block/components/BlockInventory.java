package com.technicalitiesmc.lib.block.components;

import com.technicalitiesmc.lib.block.Component;
import com.technicalitiesmc.lib.block.TKBlockComponent;
import com.technicalitiesmc.lib.block.TKBlockData;
import com.technicalitiesmc.lib.block.TKBlockDataHost;
import com.technicalitiesmc.lib.inventory.Inventory;
import com.technicalitiesmc.lib.inventory.SimpleInventory;
import com.technicalitiesmc.lib.inventory.InventoryUtils;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.BiConsumer;

@Component
public class BlockInventory extends TKBlockComponent.WithData<BlockInventory.Data> {

    private final Set<Handle> handles = new HashSet<>();

    private final Set<Handle> comparatorHandles = new HashSet<>();
    private int totalComparatorSlots = 0;

    private final Set<Handle> dropHandles = new HashSet<>();

    public BlockInventory() {
        super(Data::new);
    }

    public Handle addRegion(String name, int size) {
        Handle handle = new Handle(name, size);
        handles.add(handle);
        return handle;
    }

    @Override
    protected boolean overridesComparatorOutput(BlockState state) {
        return totalComparatorSlots != 0;
    }

    @Override
    protected int getComparatorOutput(World world, BlockPos pos, BlockState state) {
        if (totalComparatorSlots == 0) return 0;
        Set<Inventory> inventories = new HashSet<>();
        Map<Handle, SimpleInventory> inventoryMap = getData(world, pos).inventories;
        for (Handle handle : comparatorHandles) {
            inventories.add(inventoryMap.get(handle));
        }
        return InventoryUtils.calculateComparatorOutput(inventories);
    }

    @Override
    protected NonNullList<ItemStack> getAdditionalDrops(World world, BlockPos pos, BlockState state) {
        Set<Inventory> inventories = new HashSet<>();
        Map<Handle, SimpleInventory> inventoryMap = getData(world, pos).inventories;
        for (Handle handle : dropHandles) {
            inventories.add(inventoryMap.get(handle));
        }
        return InventoryUtils.collectDrops(inventories);
    }

    static class Data extends TKBlockData {

        private final Map<Handle, SimpleInventory> inventories = new IdentityHashMap<>();

        Data(BlockInventory inv, TKBlockDataHost host) {
            for (Handle handle : inv.handles) {
                inventories.put(handle, new SimpleInventory(handle.size, () -> {
                    host.markDirty();
                    World world = host.getWorld();
                    BlockPos pos = host.getPos();
                    for (BiConsumer<World, BlockPos> callback : handle.callbacks) {
                        callback.accept(world, pos);
                    }
                }));
            }
        }

        @Nonnull
        @Override
        protected CompoundNBT serialize() {
            CompoundNBT tag = super.serialize();
            inventories.forEach((handle, inv) -> tag.put(handle.name, inv.serializeNBT()));
            return tag;
        }

        @Override
        protected void deserialize(CompoundNBT tag) {
            super.deserialize(tag);
            inventories.forEach((handle, inv) -> inv.deserializeNBT(tag.getCompound(handle.name)));
        }

    }

    public class Handle {

        private final List<BiConsumer<World, BlockPos>> callbacks = new ArrayList<>();
        private final String name;
        private final int size;

        private Handle(String name, int size) {
            this.name = name;
            this.size = size;
        }

        public Inventory at(IBlockReader world, BlockPos pos) {
            return getData(world, pos).inventories.get(this);
        }

        public Handle onChanged(BiConsumer<World, BlockPos> callback) {
            callbacks.add(callback);
            return this;
        }

        public Handle outputComparatorSignal() {
            if (comparatorHandles.add(this)) {
                totalComparatorSlots += size;
            }
            return this;
        }

        public Handle dropOnBreak() {
            dropHandles.add(this);
            return this;
        }

    }

}
