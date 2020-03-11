package com.technicalitiesmc.base.client;

import com.technicalitiesmc.Technicalities;
import com.technicalitiesmc.base.client.screen.*;
import com.technicalitiesmc.base.container.*;
import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Technicalities.MODID, value = Dist.CLIENT)
public class TKBClientInit {

    public static void init(final FMLClientSetupEvent event) {
        ScreenManager.registerFactory(WorkbenchContainer.TYPE, WorkbenchScreen::new);
        ScreenManager.registerFactory(CrateContainer.TYPE, CrateScreen::new);
        ScreenManager.registerFactory(TransposerContainer.TYPE, TransposerScreen::new);
        ScreenManager.registerFactory(EjectorContainer.TYPE, EjectorScreen::new);
        ScreenManager.registerFactory(BufferContainer.TYPE, BufferScreen::new);
        ScreenManager.registerFactory(QueueContainer.TYPE, QueueScreen::new);
        ScreenManager.registerFactory(RegulatorContainer.TYPE, RegulatorScreen::new);
    }

}
