package com.technicalitiesmc.lib.container;

import com.technicalitiesmc.lib.util.intarray.BooleanIntArrayWrapper;
import com.technicalitiesmc.lib.util.intarray.EnumIntArrayWrapper;
import com.technicalitiesmc.lib.util.intarray.IntArrayWrapper;
import com.technicalitiesmc.lib.util.value.Reference;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class TKContainerAdapter extends Container {

    private final Multimap<Slot, Slot> shiftMap = MultimapBuilder.hashKeys().arrayListValues().build();
    private final List<ContainerComponent> components = new ArrayList<>();
    private final ContainerComponent.TrackerManager trackerManager = new TrackerManager();
    private final TKContainer tkContainer;

    TKContainerAdapter(ContainerType<?> type, int windowId, TKContainer tkContainer) {
        super(type, windowId);
        this.tkContainer = tkContainer;
    }

    void add(ContainerComponent component) {
        component.container = this;
        component.id = components.size();
        components.add(component);
        component.requestDataTrackers(trackerManager);
    }

    void add(Slot slot) {
        super.addSlot(slot);
    }

    void addShiftBehavior(Slot src, Slot dst) {
        shiftMap.put(src, dst);
    }

    public List<ContainerComponent> getComponents() {
        return components;
    }

    @Override
    public boolean canInteractWith(PlayerEntity player) {
        return true;
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int slot) {
        ItemStack result = ItemStack.EMPTY;
        Slot s = inventorySlots.get(slot);
        if (s != null && s.getHasStack()) {
            ItemStack stack = s.getStack();
            result = stack.copy();
            Collection<Slot> targets = shiftMap.get(s);
            if (!mergeItemStack(stack, targets)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
                s.putStack(ItemStack.EMPTY);
            }
            s.onSlotChanged();
        }
        return result;
    }

    private boolean mergeItemStack(ItemStack stack, Iterable<Slot> slots) {
        boolean result = false;
        Iterator<Slot> it;

        it = slots.iterator();
        if (stack.isStackable()) {
            while (!stack.isEmpty()) {
                if (!it.hasNext()) {
                    break;
                }

                Slot slot = it.next();
                ItemStack currentStack = slot.getStack();
                if (!currentStack.isEmpty() && areItemsAndTagsEqual(stack, currentStack) && slot.isItemValid(stack)) {
                    int totalSize = currentStack.getCount() + stack.getCount();
                    int maxSize = Math.min(slot.getSlotStackLimit(), stack.getMaxStackSize());
                    if (totalSize <= maxSize) {
                        stack.setCount(0);
                        currentStack.setCount(totalSize);
                        slot.onSlotChanged();
                        result = true;
                    } else if (currentStack.getCount() < maxSize) {
                        stack.shrink(maxSize - currentStack.getCount());
                        currentStack.setCount(maxSize);
                        slot.onSlotChanged();
                        result = true;
                    }
                }
            }
        }

        it = slots.iterator();
        if (!stack.isEmpty()) {
            while (it.hasNext()) {
                Slot slot = it.next();
                ItemStack currentStack = slot.getStack();
                if (currentStack.isEmpty() && slot.isItemValid(stack)) {
                    if (stack.getCount() > slot.getSlotStackLimit()) {
                        slot.putStack(stack.split(slot.getSlotStackLimit()));
                    } else {
                        slot.putStack(stack.split(stack.getCount()));
                    }

                    slot.onSlotChanged();
                    result = true;
                    break;
                }
            }
        }

        return result;
    }

    public void onClientEvent(int componentId, int eventId, byte[] data) {
        ContainerComponent component = components.get(componentId);
        component.onClientEvent(eventId, new PacketBuffer(Unpooled.copiedBuffer(data)));
    }

    @Override
    public void onContainerClosed(PlayerEntity player) {
        super.onContainerClosed(player);
        tkContainer.onClosed(player);
    }

    private class TrackerManager implements ContainerComponent.TrackerManager {

        @Override
        public void trackInts(int[] array) {
            TKContainerAdapter.this.trackIntArray(new IntArrayWrapper(array));
        }

        @Override
        public void trackBoolean(Reference<Boolean> reference) {
            TKContainerAdapter.this.trackIntArray(new BooleanIntArrayWrapper(reference));
        }

        @Override
        public <E extends Enum<E>> void trackEnum(Class<E> type, Reference<E> reference) {
            TKContainerAdapter.this.trackIntArray(new EnumIntArrayWrapper<>(type, reference));
        }

    }

    public static ContainerType<TKContainerAdapter> typeOf(TKContainerFactory factory) {
        return new ContainerType<>((id, inv) -> factory.create(id, inv).asVanillaContainer());
    }

    public interface TKContainerFactory {
        TKContainer create(int windowId, PlayerInventory playerInventory);
    }

}
