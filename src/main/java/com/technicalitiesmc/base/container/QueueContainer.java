package com.technicalitiesmc.base.container;

import com.technicalitiesmc.Technicalities;
import com.technicalitiesmc.lib.container.TKContainer;
import com.technicalitiesmc.lib.container.TKContainerAdapter;
import com.technicalitiesmc.lib.container.TKInventorySlot;
import com.technicalitiesmc.lib.container.component.EnumSelectorComponent;
import com.technicalitiesmc.lib.inventory.Inventory;
import com.technicalitiesmc.lib.inventory.SimpleInventory;
import com.technicalitiesmc.lib.util.MutedState;
import com.technicalitiesmc.lib.util.value.Value;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TranslationTextComponent;

public class QueueContainer extends TKContainer {

    public static final ContainerType<TKContainerAdapter> TYPE = TKContainerAdapter.typeOf(QueueContainer::new);
    public static final TranslationTextComponent NAME = new TranslationTextComponent("container." + Technicalities.MODID + ".queue");

    private static final int QUEUE_ROWS = 2;
    private static final int QUEUE_COLUMNS = 9;
    public static final int QUEUE_SIZE = QUEUE_COLUMNS * QUEUE_ROWS;

    private final Inventory storageInv;

    public QueueContainer(int windowId, PlayerInventory playerInventory, Inventory storageInv, Value<MutedState> muted) {
        super(TYPE, windowId);
        this.storageInv = storageInv;

        Region storage = addSlots(8, 18, QUEUE_ROWS, QUEUE_COLUMNS, this::createStorageSlot);
        Region playerInv = addSlots(8, 68, 3, 9, playerInventory, 9);
        Region playerHotbar = addSlots(8, 126, 1, 9, playerInventory, 0);

        addShiftClickTargets(storage, playerHotbar.reversed(), playerInv.reversed());
        addShiftClickTargets(playerInv, storage);
        addShiftClickTargets(playerHotbar, storage);

        addComponent(new EnumSelectorComponent<>(162, 6, 8, 8, 84, 0, MutedState.class, muted, true));
    }

    private QueueContainer(int windowId, PlayerInventory playerInventory) {
        this(windowId, playerInventory, new SimpleInventory(QUEUE_SIZE), new Value<>(MutedState.UNMUTED));
    }

    private Slot createStorageSlot(int x, int y, int index) {
        return new TKInventorySlot(x, y, storageInv, index) {

            private boolean isInput() {
                ItemStack stack = storageInv.get(index);
                if (index == 0) return stack.isEmpty() && storageInv.get(1).isEmpty();
                if (stack.getCount() == stack.getMaxStackSize()) return false;
                return stack.isEmpty() && !storageInv.get(index - 1).isEmpty();
            }

            private boolean isOutput() {
                return index == 0;
            }

            @Override
            public boolean isLocked() {
                return !isOutput() && !isInput();
            }

            @Override
            public boolean isItemValid(ItemStack stack) {
                return isInput();
            }

            @Override
            public boolean canTakeStack(PlayerEntity playerIn) {
                return isOutput();
            }

            @Override
            public ItemStack decrStackSize(int amt) {
                if (!isOutput()) return ItemStack.EMPTY;
                return super.decrStackSize(amt);
            }

            @Override
            public int getColor() {
                return isInput() ? 0x7F5B89DC : isOutput() ? 0x7FBC9541 : 0;
            }

        };
    }

}
