package com.technicalitiesmc.lib.block.components;

import com.technicalitiesmc.lib.block.Component;
import com.technicalitiesmc.lib.block.TKBlockComponent;
import com.technicalitiesmc.lib.block.TKBlockData;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;

@Component
public class BlockCapabilities extends TKBlockComponent.WithData<BlockCapabilities.Data> {

    public BlockCapabilities(Consumer<Manager> initializer) {
        super(() -> {
            Data data = new Data();
            initializer.accept(data);
            return data;
        });
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    @Override
    protected <T> LazyOptional<T> getCapability(IWorld world, BlockPos pos, @Nullable Direction face, Data data, Capability<T> capability) {
        CapabilityProvider<?> provider = data.providers.get(capability);
        if (provider != null) {
            LazyOptional<?> cap = provider.provide(world, pos, face);
            if (cap.isPresent()) {
                return (LazyOptional<T>) cap;
            }
        }
        return super.getCapability(world, pos, face, data, capability);
    }

    static class Data extends TKBlockData implements Manager {

        private final Map<Capability<?>, CapabilityProvider<?>> providers = new IdentityHashMap<>();

        @Override
        public <T> void provide(Capability<T> capability, CapabilityProvider<T> provider) {
            providers.put(capability, provider);
        }

        @Override
        public <T> void provideNonNull(Capability<T> capability, NonNullCapabilityProvider<T> provider) {
            providers.put(capability, (world, pos, face) -> LazyOptional.of(() -> provider.provide(world, pos, face)));
        }

    }

    public interface Manager {

        <T> void provide(Capability<T> capability, CapabilityProvider<T> provider);

        <T> void provideNonNull(Capability<T> capability, NonNullCapabilityProvider<T> provider);

    }

    public interface CapabilityProvider<T> {
        LazyOptional<T> provide(IWorld world, BlockPos pos, @Nullable Direction face);
    }

    public interface NonNullCapabilityProvider<T> {
        @Nonnull
        T provide(IWorld world, BlockPos pos, @Nullable Direction face);
    }

}
