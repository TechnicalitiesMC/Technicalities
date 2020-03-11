package com.technicalitiesmc.base.block;

import com.technicalitiesmc.base.TKBase;
import com.technicalitiesmc.base.container.QueueContainer;
import com.technicalitiesmc.lib.block.Component;
import com.technicalitiesmc.lib.block.TKBlock;
import com.technicalitiesmc.lib.block.TKBlockData;
import com.technicalitiesmc.lib.block.components.*;
import com.technicalitiesmc.lib.container.TKContainer;
import com.technicalitiesmc.lib.inventory.Inventory;
import com.technicalitiesmc.lib.inventory.InventoryUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

public class QueueBlock extends TKBlock.WithNoData {

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
    private final BlockInventory.Handle queue =
        inventory.addRegion("queue", QueueContainer.QUEUE_SIZE)
            .outputComparatorSignal().dropOnBreak()
            .onChanged(this::onInventoryChanged);
    @Component
    private final ClickGUI gui = new ClickGUI.Container(QueueContainer.NAME, this::createContainer);

    public QueueBlock() {
        super(Block.Properties.create(Material.ROCK).hardnessAndResistance(3.5F));
    }

    private TKContainer createContainer(IWorld world, BlockPos pos, BlockState state, int id, PlayerInventory playerInv, PlayerEntity player) {
        return new QueueContainer(id, playerInv, queue.at(world, pos));
    }

    private void onInventoryChanged(World world, BlockPos pos) {
        Inventory queueStorage = queue.at(world, pos);
        if (queueStorage.get(0).isEmpty()) {
            world.getPendingBlockTicks().scheduleTick(pos, asMCBlock(), 0);
        }
    }

    @Override
    protected void onScheduledTick(World world, BlockPos pos, BlockState state, Random rand) {
        Inventory queueStorage = queue.at(world, pos);
        if (queueStorage.get(0).isEmpty()) {
            shiftItems(queueStorage);
        }
    }

    private void onTriggered(World world, BlockPos pos, BlockState state) {
        Direction front = direction.get(state);
        Inventory queueStorage = queue.at(world, pos);
        ItemStack head = queueStorage.get(0);
        if (!head.isEmpty()) {
            IItemHandler neighbor = InventoryUtils.getNeighborItemHandlerOrDrop(world, pos, front, 1);
            if (neighbor != null) {
                ItemStack leftover = InventoryUtils.transferStack(head, neighbor, false);
                queueStorage.set(0, leftover);
            }
        }
        world.playSound(null, pos, TKBase.SOUND_SMALL_PISTON, SoundCategory.BLOCKS, 0.25F, 1F);
    }

    @Nonnull
    @Override
    protected <T> LazyOptional<T> getCapability(IWorld world, BlockPos pos, @Nullable Direction face, TKBlockData data, Capability<T> capability) {
        BlockState state = world.getBlockState(pos);
        Direction front = direction.get(state), back = front.getOpposite();

        if (face == front) {
            return ITEM_HANDLER.orEmpty(capability, LazyOptional.of(EmptyHandler::new));
        } else if (face == back) {
            return ITEM_HANDLER.orEmpty(capability, LazyOptional.of(() -> new InputInventory(queue.at(world, pos))));
        }

        return LazyOptional.empty();
    }

    private static class InputInventory implements IItemHandler {

        private final Inventory inventory;

        private InputInventory(Inventory inventory) {
            this.inventory = inventory;
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
            int i = inventory.getSize() - 1;
            while (i >= 0 && inventory.get(i).isEmpty()) {
                i--;
            }
            i++;
            if (i == inventory.getSize() || !inventory.get(i).isEmpty()) return stack;
            if (!simulate) {
                inventory.set(i, stack);
            }
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

    public static void shiftItems(Inventory queue) {
        for (int i = 0; i < queue.getSize() - 1; i++) {
            ItemStack nextStack = queue.get(i + 1);
            if (nextStack.isEmpty()) {
                if (i != 0) {
                    queue.set(i, ItemStack.EMPTY);
                }
                return;
            }
            queue.set(i, nextStack);
        }
        queue.set(queue.getSize() - 1, ItemStack.EMPTY);
    }

}
