package com.technicalitiesmc.lib.container;

import com.technicalitiesmc.base.item.ItemTagItem;
import com.technicalitiesmc.lib.inventory.Inventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class TKGhostSlot extends TKInventorySlot {

    private final int limit;

    public TKGhostSlot(int x, int y, Inventory inventory, int slot, int limit) {
        super(x, y, inventory, slot);
        this.limit = limit;
    }

    @Override
    public boolean canTakeStack(PlayerEntity player) {
        return false;
    }

    @Override
    public int getSlotStackLimit() {
        return limit;
    }

    public int getMaxStackSize(ItemStack stack) {
        if (stack.getItem() instanceof ItemTagItem) {
            return getSlotStackLimit();
        }
        return Math.min(stack.getMaxStackSize(), getSlotStackLimit());
    }

}
