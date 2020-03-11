package com.technicalitiesmc.base.block;

import com.technicalitiesmc.base.container.WorkbenchContainer;
import com.technicalitiesmc.lib.block.Component;
import com.technicalitiesmc.lib.block.TKBlock;
import com.technicalitiesmc.lib.block.TKBlockData;
import com.technicalitiesmc.lib.block.components.BlockInventory;
import com.technicalitiesmc.lib.block.components.ClickGUI;
import com.technicalitiesmc.lib.container.TKContainer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WorkbenchBlock extends TKBlock.WithNoData {

    @CapabilityInject(IItemHandler.class)
    private static Capability<IItemHandler> ITEM_HANDLER;

    @Component
    private final BlockInventory inventory = new BlockInventory();
    private final BlockInventory.Handle storageInv = inventory.addRegion("storage", WorkbenchContainer.STORAGE_SIZE).outputComparatorSignal().dropOnBreak();
    private final BlockInventory.Handle gridInv = inventory.addRegion("grid", WorkbenchContainer.GRID_SIZE);

    @Component
    private final ClickGUI gui = new ClickGUI.Container(WorkbenchContainer.NAME, this::createContainer);

    public WorkbenchBlock() {
        super(Block.Properties.create(Material.WOOD).hardnessAndResistance(3F));
    }

    private TKContainer createContainer(IWorld world, BlockPos pos, BlockState state, int id, PlayerInventory playerInv, PlayerEntity entity) {
        return new WorkbenchContainer(id, playerInv, storageInv.at(world, pos), gridInv.at(world, pos));
    }

    @Nonnull
    @Override
    protected <T> LazyOptional<T> getCapability(IWorld world, BlockPos pos, @Nullable Direction face, TKBlockData data, Capability<T> capability) {
        return ITEM_HANDLER.orEmpty(capability, LazyOptional.of(() -> storageInv.at(world, pos).asItemHandler()));
    }

}
