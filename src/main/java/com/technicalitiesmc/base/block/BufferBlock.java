package com.technicalitiesmc.base.block;

import com.technicalitiesmc.base.TKBase;
import com.technicalitiesmc.base.container.BufferContainer;
import com.technicalitiesmc.lib.block.Component;
import com.technicalitiesmc.lib.block.TKBlock;
import com.technicalitiesmc.lib.block.components.*;
import com.technicalitiesmc.lib.container.TKContainer;
import com.technicalitiesmc.lib.inventory.InventoryUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

public class BufferBlock extends TKBlock.WithNoData {

    @CapabilityInject(IItemHandler.class)
    private static Capability<IItemHandler> ITEM_HANDLER;

    @Component
    private final BlockDirection direction = new BlockDirection();
    @Component
    private final DirectionalPlacement placement = new DirectionalPlacement(true);
    @Component
    private final RedstoneTrigger trigger = new RedstoneTrigger(this::onTriggered);
    @Component
    private final BlockInventory inventory = new BlockInventory();
    private final BlockInventory.Handle inv = inventory.addRegion("inventory", BufferContainer.STORAGE_SIZE).outputComparatorSignal().dropOnBreak();
    @Component
    private final ClickGUI gui = new ClickGUI.Container(BufferContainer.NAME, this::createContainer);
    @Component
    private final BlockCapabilities caps = new BlockCapabilities(manager -> {
        manager.provide(ITEM_HANDLER, this::getItemHandler);
    });

    public BufferBlock() {
        super(Block.Properties.create(Material.ROCK).hardnessAndResistance(3.5F));
    }

    private TKContainer createContainer(IWorld world, BlockPos pos, BlockState state, int id, PlayerInventory playerInv, PlayerEntity entity) {
        return new BufferContainer(id, playerInv, inv.at(world, pos));
    }

    private void onTriggered(World world, BlockPos pos, BlockState state) {
        Direction front = direction.get(state);

        IItemHandler neighbor = InventoryUtils.getNeighborItemHandlerOrDrop(world, pos, front, 1);
        if (neighbor != null) {
            InventoryUtils.transferStack(inv.at(world, pos).asItemHandler(), neighbor);
        }

        world.playSound(null, pos, TKBase.SOUND_SMALL_PISTON, SoundCategory.BLOCKS, 0.25F, 1F);
    }

    private LazyOptional<IItemHandler> getItemHandler(IWorld world, BlockPos pos, Direction face) {
        Direction front = direction.get(world.getBlockState(pos));
        if (face == front) {
            return LazyOptional.of(EmptyHandler::new);
        } else {
            return LazyOptional.of(() -> inv.at(world, pos).asItemHandler());
        }
    }

    @Override
    protected boolean isNormalCube(IBlockReader world, BlockPos pos, BlockState state) {
        return false;
    }

}
