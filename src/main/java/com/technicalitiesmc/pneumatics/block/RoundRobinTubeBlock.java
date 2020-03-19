package com.technicalitiesmc.pneumatics.block;

import com.technicalitiesmc.api.tube.ITubeStackConsumer;
import com.technicalitiesmc.lib.block.Component;
import com.technicalitiesmc.lib.block.TKBlock;
import com.technicalitiesmc.lib.block.components.BlockConnections;
import com.technicalitiesmc.lib.block.components.DisabledBlockConnections;
import com.technicalitiesmc.lib.util.CollectionUtils;
import com.technicalitiesmc.pneumatics.block.components.TubeComponent;
import com.technicalitiesmc.pneumatics.block.components.TubeModulesComponent;
import com.technicalitiesmc.pneumatics.tube.module.TubeModule;
import com.technicalitiesmc.pneumatics.tube.modules.ColorFilterModule;
import com.technicalitiesmc.pneumatics.tube.modules.GlassLensModule;
import com.technicalitiesmc.pneumatics.tube.modules.OneWayValveModule;
import com.technicalitiesmc.pneumatics.tube.route.RoundRobinRoutingStrategy;
import com.technicalitiesmc.pneumatics.tube.route.RoutingStrategy;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.items.IItemHandler;

import java.util.EnumSet;
import java.util.Set;

public class RoundRobinTubeBlock extends TKBlock {

    @CapabilityInject(ITubeStackConsumer.class)
    private static Capability<ITubeStackConsumer> TUBE_STACK_CONSUMER;
    @CapabilityInject(IItemHandler.class)
    private static Capability<IItemHandler> ITEM_HANDLER;

    private static final VoxelShape CENTER_SHAPE = Block.makeCuboidShape(4, 4, 4, 12, 12, 12);
    private static final VoxelShape SIDE_SHAPE = Block.makeCuboidShape(4, 0, 4, 12, 4, 12);
    private static final Set<TubeModule.Type<?, ?>> SUPPORTED_MODULES = CollectionUtils.setOf(
        GlassLensModule.TYPE,
        ColorFilterModule.TYPE,
        OneWayValveModule.TYPE
    );

    @Component
    private final BlockConnections connections = new BlockConnections(
        EnumSet.allOf(Direction.class),
        SIDE_SHAPE,
        this::canConnect
    );
    @Component
    private final DisabledBlockConnections disabledConnections = new DisabledBlockConnections();
    @Component
    private final TubeComponent tube = new TubeComponent(this::createRoutingStrategy);
    @Component
    private final TubeModulesComponent modules = new TubeModulesComponent(SUPPORTED_MODULES);

    public RoundRobinTubeBlock() {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(1.5F));
    }

    private RoutingStrategy createRoutingStrategy() {
        return new RoundRobinRoutingStrategy();
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

}
