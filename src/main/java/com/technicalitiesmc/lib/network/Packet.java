package com.technicalitiesmc.lib.network;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public interface Packet {

    void serialize(PacketBuffer buf);

    void deserialize(PacketBuffer buf);

    boolean handle(NetworkEvent.Context ctx);

}
