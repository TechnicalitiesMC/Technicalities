package com.technicalitiesmc.lib.container;

import com.technicalitiesmc.lib.inventory.Inventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class TKInventorySlot extends Slot implements LockableSlot, ColoredSlot, NotifyingSlot {

    private static final IInventory EMPTY_INVENTORY = new net.minecraft.inventory.Inventory(0);

    private final Inventory inventory;
    private final int slot;
    private final Set<Consumer<Slot>> updateCallbacks = new HashSet<>();

    public TKInventorySlot(int x, int y, Inventory inventory, int slot) {
        super(EMPTY_INVENTORY, 0, x, y);
        this.inventory = inventory;
        this.slot = slot;
    }

    @Override
    public ItemStack getStack() {
        return inventory.get(slot);
    }

    @Override
    public void putStack(@Nonnull ItemStack stack) {
        inventory.set(slot, stack);
        for (Consumer<Slot> c : updateCallbacks) {
            c.accept(this);
        }
    }

    @Override
    public ItemStack decrStackSize(int amt) {
        ItemStack stack = getStack();
        ItemStack split = stack.split(amt);
        putStack(stack);
        return split;
    }

    @Override
    public int getSlotStackLimit() {
        return 64;
    }

    @Override
    public boolean isSameInventory(Slot other) {
        return other instanceof TKInventorySlot && ((TKInventorySlot) other).inventory == inventory;
    }

    @Override
    public boolean isLocked() {
        return false;
    }

    @Override
    public int getColor() {
        return 0;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return !isLocked() && super.isItemValid(stack);
    }

    @Override
    public boolean canTakeStack(PlayerEntity playerIn) {
        return !isLocked() && super.canTakeStack(playerIn);
    }

    @Override
    public void onChanged(Consumer<Slot> callback) {
        updateCallbacks.add(callback);
    }

}
