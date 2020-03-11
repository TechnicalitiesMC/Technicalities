package com.technicalitiesmc.lib.network;

import com.technicalitiesmc.Technicalities;
import net.minecraftforge.fml.network.NetworkDirection;

public class TKLNetworkHandler {

    public static final NetworkHandler INSTANCE = new NetworkHandler(Technicalities.MODID, "lib", 0);

    public static void initialize() {
        INSTANCE.register(GhostSlotClickPacket.class, NetworkDirection.PLAY_TO_SERVER);
        INSTANCE.register(ContainerComponentPacket.class, NetworkDirection.PLAY_TO_SERVER);
        INSTANCE.register(ConnectionDisabledPacket.class, NetworkDirection.PLAY_TO_CLIENT);
    }

}
