package com.technicalitiesmc.lib.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Random;

public abstract class TKBlockBehavior {

    protected void buildState(TKBlockBehavior.BlockStateBuilder state) {
    }

    protected BlockState getStateForPlacement(BlockState state, BlockItemUseContext context) {
        return state;
    }

    protected BlockState updateStateBasedOnNeighbor(IWorld world, BlockPos pos, BlockState state, Direction face, BlockPos neighborPos, BlockState neighborState) {
        return state;
    }

    protected boolean canRenderInLayer(RenderType layer) {
        return false;
    }

    protected VoxelShape getShape(IBlockReader world, BlockPos pos, BlockState state) {
        return VoxelShapes.empty();
    }

    protected VoxelShape getCollisionShape(IBlockReader world, BlockPos pos, BlockState state, ISelectionContext context) {
        return getShape(world, pos, state);
    }

    protected VoxelShape getRenderShape(IBlockReader world, BlockPos pos, BlockState state) {
        return getShape(world, pos, state);
    }

    protected VoxelShape getRayTraceShape(IBlockReader world, BlockPos pos, BlockState state) {
        return VoxelShapes.empty();
    }

    public void onReplaced(World world, BlockPos pos, BlockState state, BlockState newState, boolean isMoving) {
    }

    protected NonNullList<ItemStack> getAdditionalDrops(World world, BlockPos pos, BlockState state) {
        return NonNullList.create();
    }

    protected void spawnAdditionalDrops(World world, BlockPos pos, BlockState state) {
        for (ItemStack stack : getAdditionalDrops(world, pos, state)) {
            Block.spawnAsEntity(world, pos, stack);
        }
    }

    protected boolean canConnectRedstone(IBlockReader world, BlockPos pos, BlockState state, @Nullable Direction side) {
        return false;
    }

    protected int getWeakRedstoneOutput(IBlockReader world, BlockPos pos, BlockState state, Direction side) {
        return 0;
    }

    protected int getStrongRedstoneOutput(IBlockReader world, BlockPos pos, BlockState state, Direction side) {
        return 0;
    }

    protected void onAdded(World world, BlockPos pos, BlockState state, BlockState oldState, boolean moving) {
    }

    protected void onScheduledTick(World world, BlockPos pos, BlockState state, Random rand) {
    }

    protected void onNeighborChanged(World world, BlockPos pos, BlockState state, BlockPos neighborPos, boolean isMoving) {
    }

    protected ActionResultType onRightClicked(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        return ActionResultType.PASS;
    }

    protected ActionResultType onLeftClicked(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        return ActionResultType.PASS;
    }

    protected boolean overridesComparatorOutput(BlockState state) {
        return false;
    }

    protected int getComparatorOutput(World world, BlockPos pos, BlockState state) {
        return 0;
    }

    protected boolean isNormalCube(IBlockReader world, BlockPos pos, BlockState state) {
        return true;
    }

    protected boolean requiresRayTraceReconstruction() {
        return false;
    }

    public static final class BlockStateBuilder {

        private final StateContainer.Builder<Block, BlockState> container;
        private final Map<IProperty, Comparable> defaultProperties;

        BlockStateBuilder(StateContainer.Builder<Block, BlockState> container, Map<IProperty, Comparable> defaultProperties) {
            this.container = container;
            this.defaultProperties = defaultProperties;
        }

        public <T extends Comparable<T>> PropertyConfigurator<T> add(IProperty<T> property) {
            container.add(property);
            return new PropertyConfigurator<>(Collections.singletonList(property));
        }

        public <T extends Comparable<T>> PropertyConfigurator<T> add(IProperty<T>... properties) {
            container.add(properties);
            return new PropertyConfigurator<>(Arrays.asList(properties));
        }

        public <T extends Comparable<T>> PropertyConfigurator<T> add(Iterable<IProperty<T>> properties) {
            properties.forEach(container::add);
            return new PropertyConfigurator<>(properties);
        }

        public class PropertyConfigurator<T extends Comparable<T>> {

            private final Iterable<IProperty<T>> properties;

            private PropertyConfigurator(Iterable<IProperty<T>> properties) {
                this.properties = properties;
            }

            public void withDefault(T value) {
                for (IProperty<T> property : properties) {
                    defaultProperties.put(property, value);
                }
            }

        }

    }

}
