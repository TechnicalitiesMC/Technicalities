package com.technicalitiesmc.lib.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class VanillaInventoryAdapter implements IInventory {

    private final Inventory inventory;

    public VanillaInventoryAdapter(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public int getSizeInventory() {
        return inventory.getSize();
    }

    @Override
    public boolean isEmpty() {
        for (Inventory.Slot slot : inventory) {
            if (!slot.get().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return inventory.get(slot);
    }

    @Override
    public ItemStack decrStackSize(int slot, int amt) {
        ItemStack stack = inventory.get(slot);
        ItemStack split = stack.split(amt);
        inventory.set(slot, stack);
        return split;
    }

    @Override
    public ItemStack removeStackFromSlot(int slot) {
        ItemStack stack = inventory.get(slot);
        inventory.set(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        inventory.set(slot, stack);
    }

    @Override
    public void markDirty() {
    }

    @Override
    public boolean isUsableByPlayer(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        for (Inventory.Slot slot : inventory) {
            slot.set(ItemStack.EMPTY);
        }
    }

}
