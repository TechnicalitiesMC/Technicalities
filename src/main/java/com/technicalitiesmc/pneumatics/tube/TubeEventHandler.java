package com.technicalitiesmc.pneumatics.tube;

import com.technicalitiesmc.Technicalities;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = Technicalities.MODID)
public class TubeEventHandler {

    private static ResourceLocation CAPABILITY_KEY = new ResourceLocation(Technicalities.MODID, "tube_manager");

    @CapabilityInject(TubeManager.class)
    private static Capability<TubeManager> TUBE_MANAGER_CAPABILITY;

    @SubscribeEvent
    public static void onWorldCapabilitiesAttached(AttachCapabilitiesEvent<World> event) {
        World world = event.getObject();
        TubeManager manager;
        if (world instanceof ServerWorld) {
            manager = ((ServerWorld) world).getSavedData().getOrCreate(
                () -> new TubeManager.Server(world),
                TubeManager.Server.getName(world)
            );
        } else {
            manager = new TubeManager.Client(world);
        }
        LazyOptional<TubeManager> capInst = LazyOptional.of(() -> manager);
        event.addCapability(CAPABILITY_KEY, new ICapabilityProvider() {
            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
                return TUBE_MANAGER_CAPABILITY.orEmpty(cap, capInst);
            }
        });
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        event.world.getCapability(TUBE_MANAGER_CAPABILITY).orElse(null).tick();
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.isGamePaused()) return;
        if (minecraft.world == null) return;
        minecraft.world.getCapability(TUBE_MANAGER_CAPABILITY).orElse(null).tick();
    }

}
