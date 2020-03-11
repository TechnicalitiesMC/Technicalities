package com.technicalitiesmc.pneumatics.item;

import com.technicalitiesmc.pneumatics.tube.module.TubeModuleProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TubeModuleItem extends Item implements ICapabilityProvider {

    @CapabilityInject(TubeModuleProvider.class)
    private static Capability<TubeModuleProvider> TUBE_MODULE_PROVIDER;

    private final TubeModuleProvider provider;

    public TubeModuleItem(TubeModuleProvider provider, Item.Properties props) {
        super(props);
        this.provider = provider;
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        return this;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return TUBE_MODULE_PROVIDER.orEmpty(cap, LazyOptional.of(() -> provider));
    }

}
