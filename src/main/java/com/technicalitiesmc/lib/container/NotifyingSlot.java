package com.technicalitiesmc.lib.container;

import net.minecraft.inventory.container.Slot;

import java.util.function.Consumer;

public interface NotifyingSlot {
    void onChanged(Consumer<Slot> callback);
}
