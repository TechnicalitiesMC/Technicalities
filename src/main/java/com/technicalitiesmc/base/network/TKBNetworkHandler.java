package com.technicalitiesmc.base.network;

import com.technicalitiesmc.Technicalities;
import com.technicalitiesmc.lib.network.NetworkHandler;
import net.minecraftforge.fml.network.NetworkDirection;

public class TKBNetworkHandler {

    public static final NetworkHandler INSTANCE = new NetworkHandler(Technicalities.MODID, "base", 0);

    public static void initialize() {
        INSTANCE.register(SyncLabelPacket.class, NetworkDirection.PLAY_TO_CLIENT);
    }

}
