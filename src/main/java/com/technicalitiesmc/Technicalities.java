package com.technicalitiesmc;

import com.technicalitiesmc.base.TKBase;
import com.technicalitiesmc.base.TKResources;
import com.technicalitiesmc.base.client.TKBClientInit;
import com.technicalitiesmc.lib.TKLInit;
import com.technicalitiesmc.lib.util.MultiRegistry;
import com.technicalitiesmc.pneumatics.TKPneumatics;
import com.technicalitiesmc.pneumatics.client.TKPClientInit;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Technicalities.MODID)
public class Technicalities {

    public static final String MODID = "technicalities";

    public Technicalities() {
        MultiRegistry registry = new MultiRegistry(MODID);
        TKResources.register(registry);
        TKBase.register(registry);
        TKPneumatics.register(registry);

        FMLJavaModLoadingContext.get().getModEventBus().register(this);
    }

    @SubscribeEvent
    public void init(final FMLCommonSetupEvent event) {
        TKLInit.init(event);
        TKBase.init(event);
        TKPneumatics.init(event);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void clientInit(final FMLClientSetupEvent event) {
        TKBClientInit.init(event);
        TKPClientInit.init(event);
    }

}
