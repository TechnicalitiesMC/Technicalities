package com.technicalitiesmc.base.container;

import com.technicalitiesmc.Technicalities;
import com.technicalitiesmc.lib.container.TKContainer;
import com.technicalitiesmc.lib.container.TKContainerAdapter;
import com.technicalitiesmc.lib.inventory.Inventory;
import com.technicalitiesmc.lib.inventory.SimpleInventory;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.text.TranslationTextComponent;

public class RegulatorContainer extends TKContainer {

    public static final ContainerType<TKContainerAdapter> TYPE = TKContainerAdapter.typeOf(RegulatorContainer::new);
    public static final TranslationTextComponent NAME = new TranslationTextComponent("container." + Technicalities.MODID + ".regulator");

    private static final int FILTER_ROWS = 1;
    private static final int FILTER_COLUMNS = 9;
    public static final int FILTER_SIZE = FILTER_COLUMNS * FILTER_ROWS;

    public RegulatorContainer(int windowId, PlayerInventory playerInventory, Inventory filterInv) {
        super(TYPE, windowId);

        addGhostSlots(8, 18, FILTER_ROWS, FILTER_COLUMNS, filterInv, 64);
        Region playerInv = addSlots(8, 50, 3, 9, playerInventory, 9);
        Region playerHotbar = addSlots(8, 108, 1, 9, playerInventory, 0);

        addShiftClickTargets(playerInv, playerHotbar);
        addShiftClickTargets(playerHotbar, playerInv);
    }

    private RegulatorContainer(int windowId, PlayerInventory playerInventory) {
        this(windowId, playerInventory, new SimpleInventory(FILTER_SIZE));
    }

}
