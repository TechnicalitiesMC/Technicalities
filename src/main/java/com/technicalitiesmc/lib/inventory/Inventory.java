package com.technicalitiesmc.lib.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public interface Inventory extends Iterable<Inventory.Slot> {

    int getSize();

    Slot getSlot(int slot);

    ItemStack get(int slot);

    void set(int slot, ItemStack stack);

    IItemHandler asItemHandler();

    default IInventory asVanillaInventory() {
        return new VanillaInventoryAdapter(this);
    }

    default boolean isEmpty() {
        for (Slot slot : this) {
            if (!slot.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    interface Slot {

        ItemStack get();

        void set(ItemStack stack);

        boolean isEmpty();

    }

}
