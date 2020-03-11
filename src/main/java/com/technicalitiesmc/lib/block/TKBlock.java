package com.technicalitiesmc.lib.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public abstract class TKBlock extends TKBlockBehavior {

    private final Block.Properties properties;
    private TKBlockAdapter mcBlock;

    public TKBlock(Block.Properties properties) {
        this.properties = properties;
    }

    public synchronized final TKBlockAdapter asMCBlock() {
        if (mcBlock != null) return mcBlock;
        return mcBlock = TKBlockAdapter.adapt(properties, this);
    }

    @Override
    protected boolean canRenderInLayer(RenderType layer) {
        return layer == RenderType.getSolid();
    }

    protected VoxelShape getShape(IBlockReader world, BlockPos pos, BlockState state) {
        return VoxelShapes.fullCube();
    }

    @Override
    protected boolean isNormalCube(IBlockReader world, BlockPos pos, BlockState state) {
        return state.getMaterial().isOpaque() && state.isCollisionShapeOpaque(world, pos) && !state.canProvidePower();
    }

    public static abstract class WithData<D extends TKBlockData> extends TKBlock {

        private final BiFunction<WithData<D>, TKBlockDataHost, D> dataSupplier;

        public <B extends WithData<D>> WithData(Block.Properties properties, BiFunction<B, TKBlockDataHost, D> dataSupplier) {
            super(properties);
            this.dataSupplier = (BiFunction) dataSupplier;
        }

        public <B extends WithData<D>> WithData(Block.Properties properties, TKBlockDataFactory<D> dataSupplier) {
            super(properties);
            this.dataSupplier = (component, host) -> dataSupplier.create((B) component);
        }

        public WithData(Block.Properties properties, TKBlockData.Factory<D> dataSupplier) {
            super(properties);
            this.dataSupplier = (component, host) -> dataSupplier.create(host);
        }

        public WithData(Block.Properties properties, Supplier<D> dataSupplier) {
            super(properties);
            this.dataSupplier = (component, host) -> dataSupplier.get();
        }

        final D createData(TKBlockDataHost host) {
            D data = dataSupplier.apply(this, host);
            if (data != null) data.init(host);
            return data;
        }

        protected final D getData(IBlockReader world, BlockPos pos) {
            return TKBlockAdapter.getBlockData(world, pos);
        }

        protected void initData(IWorld world, BlockPos pos, D data) {
        }

        protected void tick(IWorld world, BlockPos pos, D data) {
        }

        @Nonnull
        protected <T> LazyOptional<T> getCapability(IWorld world, BlockPos pos, @Nullable Direction face, D data, Capability<T> capability) {
            return LazyOptional.empty();
        }

        public void appendModelData(IWorld world, BlockPos pos, D data, ModelDataMap.Builder builder) {
        }

    }

    public static abstract class WithNoData extends WithData<TKBlockData> {
        public WithNoData(Block.Properties properties) {
            super(properties, () -> null);
        }
    }

    public interface TKBlockDataFactory<D extends TKBlockData> {
        <C extends WithData<D>> D create(C component);
    }

}
