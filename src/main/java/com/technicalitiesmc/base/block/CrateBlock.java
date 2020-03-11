package com.technicalitiesmc.base.block;

import com.technicalitiesmc.base.container.CrateContainer;
import com.technicalitiesmc.lib.block.Component;
import com.technicalitiesmc.lib.block.TKBlock;
import com.technicalitiesmc.lib.block.components.BlockCapabilities;
import com.technicalitiesmc.lib.block.components.BlockInventory;
import com.technicalitiesmc.lib.block.components.ClickGUI;
import com.technicalitiesmc.lib.container.TKContainer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.items.IItemHandler;

public class CrateBlock extends TKBlock {

    @CapabilityInject(IItemHandler.class)
    private static Capability<IItemHandler> ITEM_HANDLER;

    @Component
    private final BlockInventory inventory = new BlockInventory();
    private final BlockInventory.Handle inv = inventory.addRegion("inventory", CrateContainer.STORAGE_SIZE).outputComparatorSignal().dropOnBreak();
    @Component
    private final ClickGUI gui = new ClickGUI.Container(CrateContainer.NAME, this::createContainer);
    @Component
    private final BlockCapabilities caps = new BlockCapabilities(this::addCaps);
//    @Component
//    private final Labelable label = new Labelable(this::getLabelItem);

    public CrateBlock() {
        super(Block.Properties.create(Material.WOOD).hardnessAndResistance(3F));
    }

    private TKContainer createContainer(IWorld world, BlockPos pos, BlockState state, int id, PlayerInventory playerInv, PlayerEntity player) {
        return new CrateContainer(id, playerInv, inv.at(world, pos));
    }

    private void addCaps(BlockCapabilities.Manager manager) {
        manager.provideNonNull(ITEM_HANDLER, (world, pos, face) -> inv.at(world, pos).asItemHandler());
    }

    private ItemStack getLabelItem(World world, BlockPos pos) {
//        Inventory inventory = inv.at(world, pos);
//        for (Inventory.Slot slot : inventory) {
//            if (slot.isEmpty()) continue;
//            return slot.get();
//        }
//        return ItemStack.EMPTY;
        int i = ((pos.getX() % 3) + 3) % 3;
        return new ItemStack(i == 0 ? Blocks.TNT : i == 1 ? Blocks.PISTON : Blocks.SPONGE);
    }

    @Override
    protected boolean isNormalCube(IBlockReader world, BlockPos pos, BlockState state) {
        return false;
    }

}
