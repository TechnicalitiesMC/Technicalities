package com.technicalitiesmc.lib;

import com.technicalitiesmc.lib.network.TKLNetworkHandler;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class TKLInit {

    public static void init(final FMLCommonSetupEvent event) {
        TKLNetworkHandler.initialize();
    }

}
