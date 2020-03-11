package com.technicalitiesmc.lib.inventory;

import com.google.common.collect.Iterators;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Iterator;

public class SimpleInventory implements Inventory, INBTSerializable<CompoundNBT> {

    private final Slot[] slots;
    private final Runnable updateCallback;

    public SimpleInventory(int size, Runnable updateCallback) {
        this.slots = new Slot[size];
        this.updateCallback = updateCallback;
        Arrays.setAll(this.slots, i -> new Slot());
    }

    public SimpleInventory(int size) {
        this(size, null);
    }

    @Override
    public int getSize() {
        return slots.length;
    }

    @Override
    public Inventory.Slot getSlot(int slot) {
        if (slot < 0 || slot >= slots.length) return null;
        return slots[slot];
    }

    @Override
    public ItemStack get(int slot) {
        return getSlot(slot).get();
    }

    @Override
    public void set(int slot, ItemStack stack) {
        getSlot(slot).set(stack);
    }

    @Nonnull
    @Override
    public Iterator<Inventory.Slot> iterator() {
        return Iterators.forArray(slots);
    }

    @Override
    public IItemHandler asItemHandler() {
        return new ItemHandler();
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT tag = new CompoundNBT();
        ListNBT slotList = new ListNBT();
        for (Slot slot : slots) {
            slotList.add(slot.serializeNBT());
        }
        tag.put("slots", slotList);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundNBT tag) {
        ListNBT slotList = tag.getList("slots", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < slots.length; i++) {
            slots[i].deserializeNBT(slotList.getCompound(i));
        }
    }

    private class Slot implements Inventory.Slot, INBTSerializable<CompoundNBT> {

        private ItemStack stack = ItemStack.EMPTY;

        private Slot() {
        }

        @Override
        public ItemStack get() {
            return stack;
        }

        @Override
        public void set(ItemStack stack) {
            this.stack = stack;
            if (updateCallback != null) updateCallback.run();
        }

        @Override
        public boolean isEmpty() {
            return stack.isEmpty();
        }

        @Override
        public CompoundNBT serializeNBT() {
            CompoundNBT tag = new CompoundNBT();
            tag.put("stack", stack.write(new CompoundNBT()));
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundNBT tag) {
            stack = ItemStack.read(tag.getCompound("stack"));
        }

    }

    private class ItemHandler implements IItemHandler {

        @Override
        public int getSlots() {
            return SimpleInventory.this.getSize();
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return SimpleInventory.this.get(slot);
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int s, @Nonnull ItemStack stack, boolean simulate) {
            Slot slot = SimpleInventory.this.slots[s];
            ItemStack currentStack = slot.get();

            if (!currentStack.isEmpty() && !ItemHandlerHelper.canItemStacksStack(currentStack, stack)) {
                return stack;
            }

            if (currentStack.isEmpty()) {
                if (!simulate) {
                    slot.set(stack);
                }
                return ItemStack.EMPTY;
            } else {
                int maxSize = currentStack.getMaxStackSize();
                int currentSize = currentStack.getCount();
                int inserted = Math.min(maxSize - currentSize, stack.getCount());
                if (!simulate) {
                    currentStack.grow(inserted);
                }
                ItemStack leftover = stack.copy();
                leftover.shrink(inserted);
                return leftover;
            }
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int s, int amount, boolean simulate) {
            Slot slot = SimpleInventory.this.slots[s];
            ItemStack extracted = slot.get();

            if (extracted.isEmpty()) {
                return ItemStack.EMPTY;
            }

            if (simulate) extracted = extracted.copy();
            return extracted.split(amount);
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

}