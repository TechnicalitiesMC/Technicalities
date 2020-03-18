package com.technicalitiesmc.base.container;

import com.technicalitiesmc.Technicalities;
import com.technicalitiesmc.lib.container.TKContainer;
import com.technicalitiesmc.lib.container.TKContainerAdapter;
import com.technicalitiesmc.lib.container.component.EnumSelectorComponent;
import com.technicalitiesmc.lib.inventory.Inventory;
import com.technicalitiesmc.lib.inventory.SimpleInventory;
import com.technicalitiesmc.lib.util.MutedState;
import com.technicalitiesmc.lib.util.value.Value;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.text.TranslationTextComponent;

public class BufferContainer extends TKContainer {

    public static final ContainerType<TKContainerAdapter> TYPE = TKContainerAdapter.typeOf(BufferContainer::new);
    public static final TranslationTextComponent NAME = new TranslationTextComponent("container." + Technicalities.MODID + ".buffer");

    private static final int STORAGE_ROWS = 3;
    private static final int STORAGE_COLUMNS = 5;
    public static final int STORAGE_SIZE = STORAGE_COLUMNS * STORAGE_ROWS;

    public BufferContainer(int windowId, PlayerInventory playerInventory, Inventory storageInv, Value<MutedState> muted) {
        super(TYPE, windowId);

        Region storage = addSlots(44, 18, STORAGE_ROWS, STORAGE_COLUMNS, storageInv);
        Region playerInv = addSlots(8, 86, 3, 9, playerInventory, 9);
        Region playerHotbar = addSlots(8, 144, 1, 9, playerInventory, 0);

        addShiftClickTargets(storage, playerHotbar.reversed(), playerInv.reversed());
        addShiftClickTargets(playerInv, storage);
        addShiftClickTargets(playerHotbar, storage);

        addComponent(new EnumSelectorComponent<>(162, 6, 8, 8, 84, 0, MutedState.class, muted, true));
    }

    private BufferContainer(int windowId, PlayerInventory playerInventory) {
        this(windowId, playerInventory, new SimpleInventory(STORAGE_SIZE), new Value<>(MutedState.UNMUTED));
    }

}
