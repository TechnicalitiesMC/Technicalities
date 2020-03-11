package com.technicalitiesmc.pneumatics.container;

import com.technicalitiesmc.Technicalities;
import com.technicalitiesmc.pneumatics.block.FilterBlock;
import com.technicalitiesmc.lib.container.TKContainer;
import com.technicalitiesmc.lib.container.TKContainerAdapter;
import com.technicalitiesmc.lib.container.component.EnumSelectorComponent;
import com.technicalitiesmc.lib.inventory.Inventory;
import com.technicalitiesmc.lib.inventory.SimpleInventory;
import com.technicalitiesmc.lib.util.PaintAction;
import com.technicalitiesmc.lib.util.value.Reference;
import com.technicalitiesmc.lib.util.value.Value;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.text.TranslationTextComponent;

public class FilterContainer extends TKContainer {

    public static final ContainerType<TKContainerAdapter> TYPE = TKContainerAdapter.typeOf(FilterContainer::new);
    public static final TranslationTextComponent NAME = new TranslationTextComponent("container." + Technicalities.MODID + ".filter");

    private static final int FILTER_ROWS = 3;
    private static final int FILTER_COLUMNS = 5;
    public static final int FILTER_SIZE = FILTER_COLUMNS * FILTER_ROWS;
    private final Runnable paintUpdateCallback;

    public FilterContainer(int windowId, PlayerInventory playerInventory, Inventory filterInv,
                           Reference<FilterBlock.FilterType> filterType, Reference<PaintAction> outputColor,
                           Runnable paintUpdateCallback) {
        super(TYPE, windowId);
        this.paintUpdateCallback = paintUpdateCallback;

        addGhostSlots(44, 18, FILTER_ROWS, FILTER_COLUMNS, filterInv, 1);
        Region playerInv = addSlots(8, 86, 3, 9, playerInventory, 9);
        Region playerHotbar = addSlots(8, 144, 1, 9, playerInventory, 0);

        addShiftClickTargets(playerInv, playerHotbar);
        addShiftClickTargets(playerHotbar, playerInv);

        addComponent(new EnumSelectorComponent<>(145, 29, 12, 12, 0, 0, FilterBlock.FilterType.class, filterType, true));
        addComponent(new EnumSelectorComponent<>(145, 47, 12, 12, 0, 12, PaintAction.class, outputColor, true));
    }

    private FilterContainer(int windowId, PlayerInventory playerInventory) {
        this(windowId, playerInventory, new SimpleInventory(FILTER_SIZE),
            new Value<>(FilterBlock.FilterType.WHITELIST), new Value<>(PaintAction.KEEP_PAINT), null);
    }

    @Override
    protected void onClosed(PlayerEntity player) {
        super.onClosed(player);
        if (paintUpdateCallback != null) {
            paintUpdateCallback.run();
        }
    }

}
