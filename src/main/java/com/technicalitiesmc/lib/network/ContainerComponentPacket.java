package com.technicalitiesmc.lib.network;

import com.technicalitiesmc.lib.container.TKContainerAdapter;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ContainerComponentPacket implements Packet {

    private int componentId;
    private int eventId;
    private byte[] data;

    public ContainerComponentPacket(int componentId, int eventId, byte[] data) {
        this.componentId = componentId;
        this.eventId = eventId;
        this.data = data;
    }

    public ContainerComponentPacket() {
    }

    @Override
    public void serialize(PacketBuffer buf) {
        buf.writeInt(componentId);
        buf.writeInt(eventId);
        buf.writeByteArray(data);
    }

    @Override
    public void deserialize(PacketBuffer buf) {
        componentId = buf.readInt();
        eventId = buf.readInt();
        data = buf.readByteArray();
    }

    @Override
    public boolean handle(NetworkEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayerEntity sender = ctx.getSender();
            if (sender == null) return;
            Container container = sender.openContainer;
            if (!(container instanceof TKContainerAdapter)) return;
            TKContainerAdapter adapter = ((TKContainerAdapter) container);
            adapter.onClientEvent(componentId, eventId, data);
        });
        return true;
    }

}
