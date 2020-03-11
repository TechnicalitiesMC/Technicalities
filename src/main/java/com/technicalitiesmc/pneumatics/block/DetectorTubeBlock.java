package com.technicalitiesmc.pneumatics.block;

import com.technicalitiesmc.api.tube.ITubeStack;
import com.technicalitiesmc.api.tube.ITubeStackConsumer;
import com.technicalitiesmc.lib.block.Component;
import com.technicalitiesmc.lib.block.TKBlock;
import com.technicalitiesmc.lib.block.components.BlockConnections;
import com.technicalitiesmc.lib.block.components.DisabledBlockConnections;
import com.technicalitiesmc.pneumatics.block.components.TubeComponent;
import com.technicalitiesmc.pneumatics.block.components.TubeModulesComponent;
import com.technicalitiesmc.pneumatics.tube.route.DefaultRoutingStrategy;
import com.technicalitiesmc.pneumatics.tube.route.RoutingStrategy;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Random;

public class DetectorTubeBlock extends TKBlock {

    @CapabilityInject(ITubeStackConsumer.class)
    private static Capability<ITubeStackConsumer> TUBE_STACK_CONSUMER;
    @CapabilityInject(IItemHandler.class)
    private static Capability<IItemHandler> ITEM_HANDLER;

    private static final VoxelShape CENTER_SHAPE = Block.makeCuboidShape(4, 4, 4, 12, 12, 12);
    private static final VoxelShape SIDE_SHAPE = Block.makeCuboidShape(4, 0, 4, 12, 4, 12);

    @Component
    private final BlockConnections connections = new BlockConnections(
        EnumSet.allOf(Direction.class),
        SIDE_SHAPE,
        this::canConnect
    );
    @Component
    private final DisabledBlockConnections disabledConnections = new DisabledBlockConnections();
    @Component
    private final TubeComponent tube = new TubeComponent(this::createRoutingStrategy, this::onStackRouted);
    @Component
    private final TubeModulesComponent modules = new TubeModulesComponent();

    public DetectorTubeBlock() {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(1.5F));
    }

    @Override
    protected void buildState(BlockStateBuilder state) {
        state.add(BlockStateProperties.TRIGGERED).withDefault(false);
    }

    private RoutingStrategy createRoutingStrategy() {
        return new DefaultRoutingStrategy();
    }

    @Override
    protected boolean canRenderInLayer(RenderType layer) {
        return layer == RenderType.getCutout();
    }

    @Override
    protected VoxelShape getShape(IBlockReader world, BlockPos pos, BlockState state) {
        return CENTER_SHAPE;
    }

    protected boolean canConnect(IWorld world, BlockPos pos, BlockState state, Direction face, BlockPos neighborPos, BlockState neighborState) {
        TileEntity te = world.getTileEntity(neighborPos);
        if (te == null) return false;
        return te.getCapability(TUBE_STACK_CONSUMER, face.getOpposite()).isPresent()
            || te.getCapability(ITEM_HANDLER, face.getOpposite()).isPresent();
    }

    @Override
    public boolean canConnectRedstone(IBlockReader world, BlockPos pos, BlockState state, @Nullable Direction side) {
        return true;
    }

    @Override
    public int getWeakRedstoneOutput(IBlockReader world, BlockPos pos, BlockState state, Direction side) {
        return state.get(BlockStateProperties.TRIGGERED) ? 15 : 0;
    }

    @Override
    public void onScheduledTick(World world, BlockPos pos, BlockState state, Random rand) {
        if (state.get(BlockStateProperties.TRIGGERED)) {
            world.setBlockState(pos, state.with(BlockStateProperties.TRIGGERED, false));
        }
    }

    private void onStackRouted(World world, BlockPos pos, ITubeStack stack) {
        BlockState state = world.getBlockState(pos);
        world.setBlockState(pos, state.with(BlockStateProperties.TRIGGERED, true));

        world.getPendingBlockTicks().scheduleTick(pos, asMCBlock(), 4);
    }

}
