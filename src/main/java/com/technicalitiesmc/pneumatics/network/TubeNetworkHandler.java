package com.technicalitiesmc.pneumatics.network;

import com.technicalitiesmc.Technicalities;
import com.technicalitiesmc.lib.network.NetworkHandler;
import net.minecraftforge.fml.network.NetworkDirection;

public class TubeNetworkHandler {

    public static final NetworkHandler INSTANCE = new NetworkHandler(Technicalities.MODID, "tube", 0);

    public static void initialize() {
        INSTANCE.register(StackJoinedTubePacket.class, NetworkDirection.PLAY_TO_CLIENT);
        INSTANCE.register(StackRoutedPacket.class, NetworkDirection.PLAY_TO_CLIENT);
        INSTANCE.register(StackMutatedPacket.class, NetworkDirection.PLAY_TO_CLIENT);
        INSTANCE.register(ModuleUpdatedPacket.class, NetworkDirection.PLAY_TO_CLIENT);
    }

}
