package com.technicalitiesmc.lib.util;

import net.minecraftforge.registries.IForgeRegistryEntry;

public interface RegistryExtendable {
    Iterable<IForgeRegistryEntry<?>> getExtendedRegistryEntries();
}
