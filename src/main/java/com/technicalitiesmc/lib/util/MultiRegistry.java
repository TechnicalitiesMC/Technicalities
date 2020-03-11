package com.technicalitiesmc.lib.util;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class MultiRegistry {

    private final String modid;
    private final Multimap<Class<?>, IForgeRegistryEntry<?>> entries = MultimapBuilder.hashKeys().arrayListValues().build();

    public MultiRegistry(String modid) {
        this.modid = modid;
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::addEntries);
    }

    public void register(String name, IForgeRegistryEntry<?>... entries) {
        for (IForgeRegistryEntry<?> entry : entries) {
            registerOne(name, entry);

            if (!(entry instanceof RegistryExtendable)) continue;
            for (IForgeRegistryEntry<?> child : ((RegistryExtendable) entry).getExtendedRegistryEntries()) {
                registerOne(name, child);
            }
        }
    }

    private <T extends IForgeRegistryEntry<?>> void registerOne(String name, T entry) {
        if (entry.getRegistryName() == null) {
            entry.setRegistryName(new ResourceLocation(modid, name));
        }
        entries.put(entry.getRegistryType(), entry);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void addEntries(RegistryEvent.Register<?> event) {
        IForgeRegistry registry = event.getRegistry();
        for (IForgeRegistryEntry<?> entry : entries.get((Class) event.getGenericType())) {
            registry.register(entry);
        }
    }

}
