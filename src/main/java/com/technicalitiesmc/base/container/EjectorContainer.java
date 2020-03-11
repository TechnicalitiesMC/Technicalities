package com.technicalitiesmc.base.container;

import com.technicalitiesmc.Technicalities;
import com.technicalitiesmc.base.block.EjectorBlock;
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

public class EjectorContainer extends TKContainer {

    public static final ContainerType<TKContainerAdapter> TYPE = TKContainerAdapter.typeOf(EjectorContainer::new);
    public static final TranslationTextComponent NAME = new TranslationTextComponent("container." + Technicalities.MODID + ".ejector");

    private static final int FILTER_ROWS = 2;
    private static final int FILTER_COLUMNS = 5;
    public static final int FILTER_SIZE = FILTER_COLUMNS * FILTER_ROWS;

    private final List<Mode> modes = new ArrayList<>();
    private final Reference<EjectorBlock.FilterType> filterType;
    private final Reference<EjectorBlock.WhitelistMode> whitelistMode;
    private final Reference<EjectorBlock.BlacklistMode> blacklistMode;

    public EjectorContainer(int windowId, PlayerInventory playerInventory, Inventory filterInv, Reference<EjectorBlock.FilterType> filterType,
                            Reference<EjectorBlock.WhitelistMode> whitelistMode, Reference<EjectorBlock.BlacklistMode> blacklistMode) {
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

        addComponent(new EnumSelectorComponent<>(136, 20, 12, 12, 0, 0, EjectorBlock.FilterType.class, Reference.of(filterType::get, this::setType), true));
        addComponent(new EnumSelectorComponent<>(136, 38, 12, 12, 24, 0, Mode.class, Reference.of(this::getMode, this::setMode), modes, true));
    }

    private EjectorContainer(int windowId, PlayerInventory playerInventory) {
        // TODO: Work out why using a different initial value other than the first enum entry breaks client sync entirely
        this(windowId, playerInventory, new SimpleInventory(FILTER_SIZE), new Value<>(EjectorBlock.FilterType.WHITELIST),
            new Value<>(EjectorBlock.WhitelistMode.STRICT), new Value<>(EjectorBlock.BlacklistMode.SINGLE));
    }

    private void setType(EjectorBlock.FilterType type) {
        filterType.set(type);
        updateModes(type);
    }

    private void updateModes(EjectorBlock.FilterType type) {
        modes.clear();
        if (type == EjectorBlock.FilterType.WHITELIST) {
            modes.add(Mode.STRICT);
            modes.add(Mode.LENIENT);
            modes.add(Mode.EXACT);
        } else {
            modes.add(Mode.SINGLE);
            modes.add(Mode.STACK);
        }
    }

    private Mode getMode() {
        return filterType.get() == EjectorBlock.FilterType.WHITELIST ?
            whitelistMode.get() == EjectorBlock.WhitelistMode.STRICT ? Mode.STRICT : whitelistMode.get() == EjectorBlock.WhitelistMode.LENIENT ? Mode.LENIENT : Mode.EXACT :
            blacklistMode.get() == EjectorBlock.BlacklistMode.SINGLE ? Mode.SINGLE : Mode.STACK;
    }

    private void setMode(Mode mode) {
        switch (mode) {
            case SINGLE:
                blacklistMode.set(EjectorBlock.BlacklistMode.SINGLE);
                break;
            case STACK:
                blacklistMode.set(EjectorBlock.BlacklistMode.STACK);
                break;
            case STRICT:
                whitelistMode.set(EjectorBlock.WhitelistMode.STRICT);
                break;
            case LENIENT:
                whitelistMode.set(EjectorBlock.WhitelistMode.LENIENT);
                break;
            case EXACT:
                whitelistMode.set(EjectorBlock.WhitelistMode.EXACT);
                break;
        }
    }

    public enum Mode implements TooltipEnabled {
        SINGLE(EjectorBlock.BlacklistMode.SINGLE),
        STACK(EjectorBlock.BlacklistMode.STACK),
        STRICT(EjectorBlock.WhitelistMode.STRICT),
        LENIENT(EjectorBlock.WhitelistMode.LENIENT),
        EXACT(EjectorBlock.WhitelistMode.EXACT);

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
