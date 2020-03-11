package com.technicalitiesmc.lib.container;

import com.technicalitiesmc.lib.inventory.Inventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;

import java.util.*;
import java.util.function.Consumer;

public abstract class TKContainer {

    final ContainerType<TKContainerAdapter> type;

    private final TKContainerAdapter vanillaContainer;

    protected TKContainer(ContainerType<TKContainerAdapter> type, int windowId) {
        this.type = type;
        this.vanillaContainer = new TKContainerAdapter(type, windowId, this);
    }

    // Inventory

    protected final Region addSlots(int x, int y, int rows, int columns, Inventory inventory) {
        return addSlots(x, y, rows, columns, inventory, 0);
    }

    protected final Region addSlots(int x, int y, int rows, int columns, Inventory inventory, int offset) {
        return addSlots(x, y, rows, columns, offset, (x1, y1, id) -> new TKInventorySlot(x1, y1, inventory, id));
    }

    protected final Region addSlot(int x, int y, Inventory inventory, int slot) {
        return addSlot(new TKInventorySlot(x, y, inventory, slot));
    }

    // IInventory

    protected final Region addSlots(int x, int y, int rows, int columns, IInventory inventory) {
        return addSlots(x, y, rows, columns, inventory, 0);
    }

    protected final Region addSlots(int x, int y, int rows, int columns, IInventory inventory, int offset) {
        return addSlots(x, y, rows, columns, offset, (x1, y1, id) -> new Slot(inventory, id, x1, y1));
    }

    protected final Region addSlot(int x, int y, IInventory inventory, int slot) {
        return addSlot(new Slot(inventory, slot, x, y));
    }

    // Ghost Slots

    protected final Region addGhostSlots(int x, int y, int rows, int columns, Inventory inventory, int limit) {
        return addGhostSlots(x, y, rows, columns, inventory, 0, limit);
    }

    protected final Region addGhostSlots(int x, int y, int rows, int columns, Inventory inventory, int offset, int limit) {
        return addSlots(x, y, rows, columns, offset, (x1, y1, id) -> new TKGhostSlot(x1, y1, inventory, id, limit));
    }

    protected final Region addGhostSlot(int x, int y, Inventory inventory, int slot, int limit) {
        return addSlot(new TKGhostSlot(x, y, inventory, slot, limit));
    }

    // Slot factory

    protected final Region addSlots(int x, int y, int rows, int columns, SlotFactory slotFactory) {
        return addSlots(x, y, rows, columns, 0, slotFactory);
    }

    protected final Region addSlots(int x, int y, int rows, int columns, int offset, SlotFactory slotFactory) {
        List<Region> regions = new ArrayList<>();
        for (int j = 0; j < rows; j++) {
            for (int i = 0; i < columns; i++) {
                Slot slot = slotFactory.createSlot(x + i * 18, y + j * 18, offset + i + (j * columns));
                regions.add(addSlot(slot));
            }
        }
        return new MergedRegion(regions);
    }

    protected final Region addSlot(Slot slot) {
        vanillaContainer.add(slot);
        return new SlotRegion(slot);
    }

    // Shift-clicking

    protected final void addShiftClickTargets(Region source, Region... targets) {
        for (Slot srcSlot : source.getSlots()) {
            for (Region target : targets) {
                for (Slot dstSlot : target.getSlots()) {
                    vanillaContainer.addShiftBehavior(srcSlot, dstSlot);
                }
            }
        }
    }

    // Components

    protected final void addComponent(ContainerComponent component) {
        vanillaContainer.add(component);
    }

    public TKContainerAdapter asVanillaContainer() {
        return vanillaContainer;
    }

    // Misc

    protected void onClosed(PlayerEntity player) {
    }

    public static abstract class Region {

        public abstract List<Slot> getSlots();

        public Region reversed() {
            Region self = this;
            return new Region() {
                @Override
                public List<Slot> getSlots() {
                    List<Slot> slots = new ArrayList<>(self.getSlots());
                    Collections.reverse(slots);
                    return slots;
                }
            };
        }

        public Region onChanged(Consumer<Slot> callback) {
            for (Slot slot : getSlots()) {
                if (!(slot instanceof NotifyingSlot)) {
                    throw new IllegalStateException("One of the slots is not able to notify changes.");
                }
                ((NotifyingSlot) slot).onChanged(callback);
            }
            return this;
        }

    }

    private static class SlotRegion extends Region {

        private final List<Slot> slots;

        private SlotRegion(Slot slot) {
            this.slots = Collections.singletonList(slot);
        }

        @Override
        public List<Slot> getSlots() {
            return slots;
        }

    }

    private static class MergedRegion extends Region {

        private final List<Slot> slots = new ArrayList<>();

        private MergedRegion(List<Region> regions) {
            for (Region region : regions) {
                slots.addAll(region.getSlots());
            }
        }

        @Override
        public List<Slot> getSlots() {
            return slots;
        }

    }

    public interface SlotFactory {
        Slot createSlot(int x, int y, int index);
    }

}
