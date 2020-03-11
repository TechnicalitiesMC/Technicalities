package com.technicalitiesmc.lib.inventory;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.IWorld;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.List;

public class DroppedItemHandler implements IItemHandler {

    private final List<ItemEntity> entities;

    public DroppedItemHandler(IWorld world, AxisAlignedBB area) {
        this.entities = world.getEntitiesWithinAABB(ItemEntity.class, area);
    }

    @Override
    public int getSlots() {
        return entities.size();
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        ItemEntity entity = entities.get(slot);
        return entity.isAlive() ? entity.getItem() : ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        return stack;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemEntity entity = entities.get(slot);
        if (!entity.isAlive()) return ItemStack.EMPTY;
        ItemStack stack = entity.getItem().copy();
        ItemStack extracted = stack.split(amount);
        if (!simulate) {
            if (!stack.isEmpty()) {
                entity.setItem(stack);
            } else {
                entity.remove();
            }
        }
        return extracted;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return false;
    }

}
