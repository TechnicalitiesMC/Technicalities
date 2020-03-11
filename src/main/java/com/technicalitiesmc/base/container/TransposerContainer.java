package com.technicalitiesmc.base.container;

import com.technicalitiesmc.Technicalities;
import com.technicalitiesmc.base.block.TransposerBlock;
import com.technicalitiesmc.lib.container.TKContainer;
import com.technicalitiesmc.lib.container.TKContainerAdapter;
import com.technicalitiesmc.lib.container.component.EnumSelectorComponent;
import com.technicalitiesmc.lib.inventory.Inventory;
import com.technicalitiesmc.lib.inventory.SimpleInventory;
import com.technicalitiesmc.lib.util.TooltipEnabled;
import com.technicalitiesmc.lib.util.value.Reference;
import com.technicalitiesmc.lib.util.value.Value;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.List;

public class TransposerContainer extends TKContainer {

    public static final ContainerType<TKContainerAdapter> TYPE = TKContainerAdapter.typeOf(TransposerContainer::new);
    public static final TranslationTextComponent NAME = new TranslationTextComponent("container." + Technicalities.MODID + ".transposer");

    private static final int FILTER_ROWS = 2;
    private static final int FILTER_COLUMNS = 5;
    public static final int FILTER_SIZE = FILTER_COLUMNS * FILTER_ROWS;

    private final List<Mode> modes = new ArrayList<>();
    private final Reference<TransposerBlock.FilterType> filterType;
    private final Reference<TransposerBlock.WhitelistMode> whitelistMode;
    private final Reference<TransposerBlock.BlacklistMode> blacklistMode;

    public TransposerContainer(int windowId, PlayerInventory playerInventory, Inventory filterInv, Reference<TransposerBlock.FilterType> filterType,
                               Reference<TransposerBlock.WhitelistMode> whitelistMode, Reference<TransposerBlock.BlacklistMode> blacklistMode) {
        super(TYPE, windowId);

        this.filterType = filterType;
        this.whitelistMode = whitelistMode;
        this.blacklistMode = blacklistMode;

        addGhostSlots(44, 18, FILTER_ROWS, FILTER_COLUMNS, filterInv, 64);
        Region playerInv = addSlots(8, 68, 3, 9, playerInventory, 9);
        Region playerHotbar = addSlots(8, 126, 1, 9, playerInventory, 0);

        addShiftClickTargets(playerInv, playerHotbar);
        addShiftClickTargets(playerHotbar, playerInv);

        updateModes(filterType.get());

        addComponent(new EnumSelectorComponent<>(136, 20, 12, 12, 0, 0, TransposerBlock.FilterType.class, Reference.of(filterType::get, this::setType), true));
        addComponent(new EnumSelectorComponent<>(136, 38, 12, 12, 24, 0, Mode.class, Reference.of(this::getMode, this::setMode), modes, true));
    }

    private TransposerContainer(int windowId, PlayerInventory playerInventory) {
        this(windowId, playerInventory, new SimpleInventory(FILTER_SIZE), new Value<>(TransposerBlock.FilterType.WHITELIST),
            new Value<>(TransposerBlock.WhitelistMode.STRICT), new Value<>(TransposerBlock.BlacklistMode.SINGLE));
    }

    private void setType(TransposerBlock.FilterType type) {
        filterType.set(type);
        updateModes(type);
    }

    private void updateModes(TransposerBlock.FilterType type) {
        modes.clear();
        if (type == TransposerBlock.FilterType.WHITELIST) {
            modes.add(Mode.STRICT);
            modes.add(Mode.LENIENT);
        } else {
            modes.add(Mode.SINGLE);
            modes.add(Mode.STACK);
        }
    }

    private Mode getMode() {
        return filterType.get() == TransposerBlock.FilterType.WHITELIST ?
            whitelistMode.get() == TransposerBlock.WhitelistMode.STRICT ? Mode.STRICT : Mode.LENIENT :
            blacklistMode.get() == TransposerBlock.BlacklistMode.SINGLE ? Mode.SINGLE : Mode.STACK;
    }

    private void setMode(Mode mode) {
        switch (mode) {
            case STRICT:
                whitelistMode.set(TransposerBlock.WhitelistMode.STRICT);
                break;
            case LENIENT:
                whitelistMode.set(TransposerBlock.WhitelistMode.LENIENT);
                break;
            case SINGLE:
                blacklistMode.set(TransposerBlock.BlacklistMode.SINGLE);
                break;
            case STACK:
                blacklistMode.set(TransposerBlock.BlacklistMode.STACK);
                break;
        }
    }

    public enum Mode implements TooltipEnabled {
        SINGLE(TransposerBlock.BlacklistMode.SINGLE),
        STACK(TransposerBlock.BlacklistMode.STACK),
        STRICT(TransposerBlock.WhitelistMode.STRICT),
        LENIENT(TransposerBlock.WhitelistMode.LENIENT);

        private final TooltipEnabled parent;

        Mode(TooltipEnabled parent) {
            this.parent = parent;
        }

        @Override
        public void addTooltip(List<ITextComponent> tooltip) {
            parent.addTooltip(tooltip);
        }

    }

}
