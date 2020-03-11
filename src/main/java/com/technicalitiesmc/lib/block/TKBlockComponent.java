package com.technicalitiesmc.lib.block;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public abstract class TKBlockComponent extends TKBlockBehavior {

    public static abstract class WithData<D extends TKBlockData> extends TKBlockComponent {

        private final BiFunction<WithData<D>, TKBlockDataHost, D> dataSupplier;

        public <C extends WithData<D>> WithData(BiFunction<C, TKBlockDataHost, D> dataSupplier) {
            this.dataSupplier = (BiFunction) dataSupplier;
        }

        public <C extends WithData<D>> WithData(TKComponentDataFactory<D> dataSupplier) {
            this.dataSupplier = (component, host) -> dataSupplier.create((C) component);
        }

        public WithData(TKBlockData.Factory<D> dataSupplier) {
            this.dataSupplier = (component, host) -> dataSupplier.create(host);
        }

        public WithData(Supplier<D> dataSupplier) {
            this.dataSupplier = (component, host) -> dataSupplier.get();
        }

        final D createData(TKBlockDataHost host) {
            D data = dataSupplier.apply(this, host);
            if (data != null) data.init(host);
            return data;
        }

        protected final D getData(IBlockReader world, BlockPos pos) {
            return TKBlockAdapter.getComponentData(world, pos, this);
        }

        protected void initData(IWorld world, BlockPos pos, D data) {
        }

        protected void tick(IWorld world, BlockPos pos, D data) {
        }

        @Nonnull
        protected <T> LazyOptional<T> getCapability(IWorld world, BlockPos pos, @Nullable Direction face, D data, Capability<T> capability) {
            return LazyOptional.empty();
        }

        protected void appendModelData(IWorld world, BlockPos pos, D data, ModelDataMap.Builder builder) {
        }

    }

    public static abstract class WithNoData extends WithData<TKBlockData> {
        public WithNoData() {
            super(() -> null);
        }
    }

    public interface TKComponentDataFactory<D extends TKBlockData> {
        <C extends WithData<D>> D create(C component);
    }

}
