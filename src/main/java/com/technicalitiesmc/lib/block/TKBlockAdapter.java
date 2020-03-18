package com.technicalitiesmc.lib.block;

import com.technicalitiesmc.lib.math.VoxelShapeHelper;
import com.technicalitiesmc.lib.util.RegistryExtendable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
public class TKBlockAdapter extends Block implements RegistryExtendable {

    private static final ThreadLocal<TKBlockDescriptor> CURRENT_DESCRIPTOR = new ThreadLocal<>();
    private static final ThreadLocal<TileEntityAdapter> CURRENT_TILE = new ThreadLocal<>();

    public static TKBlockAdapter adapt(Block.Properties properties, TKBlock block) {
        try {
            TKBlockDescriptor descriptor = new TKBlockDescriptor(block);
            CURRENT_DESCRIPTOR.set(descriptor);
            TKBlockAdapter adapter = new TKBlockAdapter(properties, block, descriptor.components);
            CURRENT_DESCRIPTOR.set(null);
            return adapter;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static TileEntityAdapter getTE(IBlockReader world, BlockPos pos) {
        TileEntityAdapter current = CURRENT_TILE.get();
        if (current != null) return current;
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof TileEntityAdapter)) return null;
        return (TileEntityAdapter) tile;
    }

    @SuppressWarnings("unchecked")
    static <D extends TKBlockData> D getBlockData(IBlockReader world, BlockPos pos) {
        TileEntityAdapter te = getTE(world, pos);
        if (te == null) return null;
        return (D) te.blockData;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    static <D extends TKBlockData> D getComponentData(IBlockReader world, BlockPos pos, TKBlockComponent.WithData<D> component) {
        TileEntityAdapter te = getTE(world, pos);
        if (te == null) return null;
        return (D) te.componentData.get(component);
    }

    private final TKBlock block;
    private final Set<Util.ComponentInfo> components;
    private final TileEntityType<TileEntityAdapter> tileEntityType;
    @SuppressWarnings("rawtypes")
    private Map<IProperty, Comparable> defaultProperties;

    @SuppressWarnings("unchecked")
    TKBlockAdapter(Properties properties, TKBlock block, Set<Util.ComponentInfo> components) {
        super(properties);
        this.block = block;
        this.components = components;
        this.defaultProperties.forEach((prop, value) -> setDefaultState(getDefaultState().with(prop, value)));
        if (block instanceof TKBlock.WithData || components.stream().anyMatch(c -> c.instance instanceof TKBlockComponent.WithData)) {
            this.tileEntityType = TileEntityType.Builder.create(this::createTile, this).build(null);
        } else {
            this.tileEntityType = null;
        }
    }

    public boolean canRenderInLayer(RenderType layer) {
        if (block.canRenderInLayer(layer)) return true;
        for (Util.ComponentInfo component : components) {
            if (component.instance.canRenderInLayer(layer)) return true;
        }
        return false;
    }

    public Set<TKBlockComponent> getComponents() {
        return components.stream().map(c -> c.instance).collect(Collectors.toSet());
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        TKBlockDescriptor desc = CURRENT_DESCRIPTOR.get();
        this.defaultProperties = new IdentityHashMap<>();
        TKBlockBehavior.BlockStateBuilder bsb = new TKBlockBehavior.BlockStateBuilder(builder, defaultProperties);
        desc.block.buildState(bsb);
        for (Util.ComponentInfo component : desc.components) {
            component.instance.buildState(bsb);
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockState state = getDefaultState();
        state = block.getStateForPlacement(state, context);
        for (Util.ComponentInfo component : components) {
            state = component.instance.getStateForPlacement(state, context);
        }
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        for (Direction face : Direction.values()) {
            BlockPos neighborPos = pos.offset(face);
            BlockState neighborState = world.getBlockState(neighborPos);
            state = block.updateStateBasedOnNeighbor(world, pos, state, face, neighborPos, neighborState);
            for (Util.ComponentInfo component : components) {
                state = component.instance.updateStateBasedOnNeighbor(world, pos, state, face, neighborPos, neighborState);
            }
        }
        return state;
    }

    @Override
    public BlockState updatePostPlacement(BlockState state, Direction face, BlockState neighborState, IWorld world, BlockPos pos, BlockPos neighborPos) {
        state = block.updateStateBasedOnNeighbor(world, pos, state, face, neighborPos, neighborState);
        for (Util.ComponentInfo component : components) {
            state = component.instance.updateStateBasedOnNeighbor(world, pos, state, face, neighborPos, neighborState);
        }
        return state;
    }

    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        VoxelShape shape = block.getShape(world, pos, state);
        if (components.isEmpty()) return shape;

        List<VoxelShape> shapes = new ArrayList<>();
        shapes.add(shape);

        for (Util.ComponentInfo component : components) {
            VoxelShape componentShape = component.instance.getShape(world, pos, state);
            if (!componentShape.isEmpty()) {
                shapes.add(componentShape);
            }
        }

        return VoxelShapeHelper.merge(shapes);
    }

    public VoxelShape getCollisionShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        VoxelShape shape = block.getCollisionShape(world, pos, state, context);
        if (components.isEmpty()) return shape;

        List<VoxelShape> shapes = new ArrayList<>();
        shapes.add(shape);

        for (Util.ComponentInfo component : components) {
            VoxelShape componentShape = component.instance.getCollisionShape(world, pos, state, context);
            if (!componentShape.isEmpty()) {
                shapes.add(componentShape);
            }
        }

        return VoxelShapeHelper.merge(shapes);
    }

    public VoxelShape getRenderShape(BlockState state, IBlockReader world, BlockPos pos) {
        VoxelShape shape = block.getRenderShape(world, pos, state);
        if (components.isEmpty()) return shape;

        List<VoxelShape> shapes = new ArrayList<>();
        shapes.add(shape);

        for (Util.ComponentInfo component : components) {
            VoxelShape componentShape = component.instance.getRenderShape(world, pos, state);
            if (!componentShape.isEmpty()) {
                shapes.add(componentShape);
            }
        }

        return VoxelShapeHelper.merge(shapes);
    }

    public VoxelShape getRaytraceShape(BlockState state, IBlockReader world, BlockPos pos) {
        VoxelShape shape = block.getRayTraceShape(world, pos, state);
        if (components.isEmpty()) return shape;

        List<VoxelShape> shapes = new ArrayList<>();
        shapes.add(shape);

        for (Util.ComponentInfo component : components) {
            VoxelShape componentShape = component.instance.getRayTraceShape(world, pos, state);
            if (!componentShape.isEmpty()) {
                shapes.add(componentShape);
            }
        }

        return VoxelShapeHelper.merge(shapes);
    }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        block.onReplaced(world, pos, state, newState, isMoving);
        for (Util.ComponentInfo component : components) {
            component.instance.onReplaced(world, pos, state, newState, isMoving);
        }
        if (!isMoving && state.getBlock() != newState.getBlock()) {
            block.spawnAdditionalDrops(world, pos, state);
            for (Util.ComponentInfo component : components) {
                component.instance.spawnAdditionalDrops(world, pos, state);
            }
        }
        super.onReplaced(state, world, pos, newState, isMoving);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side) {
        if (block.canConnectRedstone(world, pos, state, side)) return true;
        for (Util.ComponentInfo component : components) {
            if (component.instance.canConnectRedstone(world, pos, state, side)) return true;
        }
        return false;
    }

    @Override
    public int getWeakPower(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        int max = block.getWeakRedstoneOutput(world, pos, state, side);
        for (Util.ComponentInfo component : components) {
            max = Math.max(max, component.instance.getWeakRedstoneOutput(world, pos, state, side));
        }
        return max;
    }

    @Override
    public int getStrongPower(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        int max = block.getStrongRedstoneOutput(world, pos, state, side);
        for (Util.ComponentInfo component : components) {
            max = Math.max(max, component.instance.getStrongRedstoneOutput(world, pos, state, side));
        }
        return max;
    }

    @Override
    public boolean hasComparatorInputOverride(BlockState state) {
        if (block.overridesComparatorOutput(state)) return true;
        for (Util.ComponentInfo component : components) {
            if (component.instance.overridesComparatorOutput(state)) return true;
        }
        return false;
    }

    @Override
    public int getComparatorInputOverride(BlockState state, World world, BlockPos pos) {
        int max = block.getComparatorOutput(world, pos, state);
        for (Util.ComponentInfo component : components) {
            max = Math.max(max, component.instance.getComparatorOutput(world, pos, state));
        }
        return max;
    }

    @Override
    public boolean isNormalCube(BlockState state, IBlockReader world, BlockPos pos) {
        if (!block.isNormalCube(world, pos, state)) return false;
        for (Util.ComponentInfo component : components) {
            if (!component.instance.isNormalCube(world, pos, state)) return false;
        }
        return super.isNormalCube(state, world, pos);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean isMoving) {
        block.onAdded(world, pos, state, oldState, isMoving);
        for (Util.ComponentInfo component : components) {
            component.instance.onAdded(world, pos, state, oldState, isMoving);
        }
    }

    @Override
    public void tick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
        block.onScheduledTick(world, pos, state, rand);
        for (Util.ComponentInfo component : components) {
            component.instance.onScheduledTick(world, pos, state, rand);
        }
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block _block, BlockPos neighborPos, boolean isMoving) {
        block.onNeighborChanged(world, pos, state, neighborPos, isMoving);
        for (Util.ComponentInfo component : components) {
            component.instance.onNeighborChanged(world, pos, state, neighborPos, isMoving);
        }
    }

    private BlockRayTraceResult reconstructHit(World world, BlockState state, BlockPos pos, PlayerEntity player, BlockRayTraceResult hit) {
        VoxelShape shape = getShape(state, world, pos, ISelectionContext.forEntity(player));
        Vec3d start = player.getEyePosition(0);
        Vec3d direction = hit.getHitVec().subtract(start).normalize();
        double reach = player.getAttribute(PlayerEntity.REACH_DISTANCE).getValue() + 3;
        Vec3d end = start.add(direction.scale(reach));
        return shape.rayTrace(start, end, pos);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (block.requiresRayTraceReconstruction()) {
            hit = reconstructHit(world, state, pos, player, hit);
        } else {
            for (Util.ComponentInfo component : components) {
                if (component.instance.requiresRayTraceReconstruction()) {
                    hit = reconstructHit(world, state, pos, player, hit);
                    break;
                }
            }
        }
        ActionResultType blockResult = block.onRightClicked(world, pos, state, player, hand, hit);
        if (blockResult != ActionResultType.PASS) return blockResult;
        for (Util.ComponentInfo component : components) {
            ActionResultType componentResult = component.instance.onRightClicked(world, pos, state, player, hand, hit);
            if (componentResult != ActionResultType.PASS) return componentResult;
        }
        return ActionResultType.PASS;
    }

    @Override
    public void onBlockClicked(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        ActionResultType blockResult = block.onLeftClicked(world, pos, state, player);
        if (blockResult != ActionResultType.PASS) return;
        for (Util.ComponentInfo component : components) {
            ActionResultType componentResult = component.instance.onLeftClicked(world, pos, state, player);
            if (componentResult != ActionResultType.PASS) return;
        }
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return tileEntityType != null;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return hasTileEntity(state) ? createTile() : null;
    }

    public TileEntityType<TileEntityAdapter> getTileEntityType() {
        return tileEntityType;
    }

    @Override
    public Iterable<IForgeRegistryEntry<?>> getExtendedRegistryEntries() {
        if (tileEntityType == null) return Collections.emptyList();
        return Collections.singleton(tileEntityType);
    }

    private TileEntityAdapter createTile() {
        return new TileEntityAdapter(tileEntityType);
    }

    private static class TKBlockDescriptor {

        private final TKBlock block;
        private final Set<Util.ComponentInfo> components;

        public TKBlockDescriptor(TKBlock block) throws IllegalAccessException {
            this.block = block;
            this.components = Util.findComponents(block);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public class TileEntityAdapter extends TileEntity implements ITickableTileEntity {

        private final TKBlockData blockData;
        private final Map<TKBlockComponent.WithData, TKBlockData> componentData = new IdentityHashMap<>();

        public TileEntityAdapter(TileEntityType<?> type) {
            super(type);

            Host host = new Host();
            this.blockData = block instanceof TKBlock.WithData ? ((TKBlock.WithData<?>) block).createData(host) : null;
            if (this.blockData != null) this.blockData.init(host);
            for (Util.ComponentInfo component : components) {
                if (!(component.instance instanceof TKBlockComponent.WithData)) continue;
                TKBlockComponent.WithData<?> comp = (TKBlockComponent.WithData<?>) component.instance;
                TKBlockData data = comp.createData(host);
                this.componentData.put(comp, data);
                if (data != null) data.init(host);
            }

            CURRENT_TILE.set(this);
            World world = getWorld();
            BlockPos pos = getPos();
            if (block instanceof TKBlock.WithData) {
                ((TKBlock.WithData) block).initData(world, pos, blockData);
            }
            componentData.forEach((component, data) -> {
                component.initData(world, pos, data);
            });
            CURRENT_TILE.set(null);
        }

        public TKBlock getBlock() {
            return block;
        }

        public TKBlockData getBlockData() {
            return blockData;
        }

        public Map<TKBlockComponent.WithData, TKBlockData> getComponentData() {
            return componentData;
        }

        @Override
        public void tick() {
            World world = getWorld();
            BlockPos pos = getPos();
            if (block instanceof TKBlock.WithData) {
                ((TKBlock.WithData) block).tick(world, pos, blockData);
            }
            componentData.forEach((component, data) -> {
                component.tick(world, pos, data);
            });
        }

        @Nonnull
        @Override
        public CompoundNBT write(CompoundNBT tag) {
            super.write(tag);
            if (blockData != null) {
                tag.put("block", blockData.serialize());
            }
            componentData.forEach((component, data) -> {
                if (data == null) return;
                String componentName = Util.getComponentType(component.getClass()).getName();
                tag.put("component_" + componentName, data.serialize());
            });
            return tag;
        }

        @Override
        public void read(CompoundNBT tag) {
            super.read(tag);
            if (blockData != null) {
                blockData.deserialize(tag.getCompound("block"));
            }
            componentData.forEach((component, data) -> {
                if (data == null) return;
                String componentName = Util.getComponentType(component.getClass()).getName();
                data.deserialize(tag.getCompound("component_" + componentName));
            });
        }

        private CompoundNBT serializeSync(CompoundNBT tag) {
            if (blockData != null) {
                tag.put("block", blockData.serializeSync());
            }
            componentData.forEach((component, data) -> {
                if (data == null) return;
                String componentName = Util.getComponentType(component.getClass()).getName();
                tag.put("component_" + componentName, data.serializeSync());
            });
            return tag;
        }

        private void deserializeSync(CompoundNBT tag) {
            if (blockData != null) {
                blockData.deserializeSync(tag.getCompound("block"));
            }
            componentData.forEach((component, data) -> {
                if (data == null) return;
                String componentName = Util.getComponentType(component.getClass()).getName();
                data.deserializeSync(tag.getCompound("component_" + componentName));
            });
        }

        @Nonnull
        @Override
        public CompoundNBT getUpdateTag() {
            return serializeSync(super.getUpdateTag());
        }

        @Override
        public void handleUpdateTag(CompoundNBT tag) {
            super.handleUpdateTag(tag);
            deserializeSync(tag);
        }

        @Nonnull
        @Override
        public IModelData getModelData() {
            IWorld world = getWorld();
            BlockPos pos = getPos();
            ModelDataMap.Builder builder = new ModelDataMap.Builder();
            if (block instanceof TKBlock.WithData) {
                ((TKBlock.WithData) block).appendModelData(world, pos, blockData, builder);
            }
            componentData.forEach((component, data) -> {
                component.appendModelData(world, pos, data, builder);
            });
            return builder.build();
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            IWorld world = getWorld();
            BlockPos pos = getPos();
            if (block instanceof TKBlock.WithData) {
                LazyOptional capability = ((TKBlock.WithData) block).getCapability(world, pos, side, blockData, cap);
                if (capability.isPresent()) return capability;
            }
            for (Map.Entry<TKBlockComponent.WithData, TKBlockData> entry : componentData.entrySet()) {
                LazyOptional capability = entry.getKey().getCapability(world, pos, side, entry.getValue(), cap);
                if (capability.isPresent()) return capability;
            }
            return super.getCapability(cap, side);
        }

        @Override
        public void onLoad() {
            if (blockData != null) blockData.onLoad();
            for (TKBlockData data : componentData.values()) {
                if (data == null) continue;
                data.onLoad();
            }
        }

        @Override
        public void onChunkUnloaded() {
            if (blockData != null) blockData.onUnload();
            for (TKBlockData data : componentData.values()) {
                if (data == null) continue;
                data.onUnload();
            }
        }

        private final class Host implements TKBlockDataHost {

            @Override
            public World getWorld() {
                return TileEntityAdapter.this.getWorld();
            }

            @Override
            public BlockPos getPos() {
                return TileEntityAdapter.this.getPos();
            }

            @Override
            public void markDirty() {
                TileEntityAdapter.this.markDirty();
            }

            @Override
            public void markModelDataDirty() {
                TileEntityAdapter.this.requestModelDataUpdate();
            }

            @Override
            public boolean isValid() {
                return !TileEntityAdapter.this.isRemoved();
            }

        }

    }

}
