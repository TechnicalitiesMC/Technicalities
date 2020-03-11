package com.technicalitiesmc.lib.network;

import com.technicalitiesmc.lib.container.TKGhostSlot;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class GhostSlotClickPacket implements Packet {

    private int slotNumber;
    private ItemStack stack;

    public GhostSlotClickPacket(int slotNumber, ItemStack stack) {
        this.slotNumber = slotNumber;
        this.stack = stack;
    }

    public GhostSlotClickPacket() {
    }

    @Override
    public void serialize(PacketBuffer buf) {
        buf.writeInt(slotNumber);
        buf.writeItemStack(stack);
    }

    @Override
    public void deserialize(PacketBuffer buf) {
        slotNumber = buf.readInt();
        stack = buf.readItemStack();
    }

    @Override
    public boolean handle(NetworkEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayerEntity sender = ctx.getSender();
            if (sender == null) return;
            Container container = sender.openContainer;
            if (container == null) return;
            Slot slot = container.inventorySlots.get(slotNumber);
            if (slot instanceof TKGhostSlot) {
                slot.putStack(stack);
            }
        });
        return true;
    }

}
