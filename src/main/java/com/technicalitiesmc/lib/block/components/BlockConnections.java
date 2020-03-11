package com.technicalitiesmc.lib.block.components;

import com.technicalitiesmc.lib.block.Component;
import com.technicalitiesmc.lib.block.TKBlockComponent;
import com.technicalitiesmc.lib.math.IndexedVoxelShape;
import com.technicalitiesmc.lib.math.VoxelShapeHelper;
import com.technicalitiesmc.lib.util.CollectionUtils;
import net.minecraft.block.BlockState;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IProperty;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.*;

@Component
public class BlockConnections extends TKBlockComponent {

    public static final EnumMap<Direction, IProperty<Boolean>> PROPERTIES =
        CollectionUtils.newFilledEnumMap(
            Direction.class,
            d -> BooleanProperty.create(d.getName().toLowerCase())
        );

    @Component(optional = true)
    private DisabledBlockConnections disabledConnections;

    private final EnumSet<Direction> sides;
    private final EnumMap<Direction, IndexedVoxelShape> sideShapes;
    private final Map<BlockState, VoxelShape> shapes = new IdentityHashMap<>();
    private final ConnectionStateProvider stateProvider;

    public BlockConnections(EnumSet<Direction> sides, VoxelShape shape, ConnectionStateProvider stateProvider) {
        this.sides = sides;
        this.sideShapes = CollectionUtils.newFilledEnumMap(Direction.class, side -> {
            VoxelShape rotated = VoxelShapeHelper.rotate(shape, side);
            return new IndexedVoxelShape(rotated, side.ordinal(), new HitInfo(side, rotated));
        });
        this.stateProvider = stateProvider;
    }

    public boolean isValidSide(Direction side) {
        return sides.contains(side);
    }

    public boolean isConnected(IBlockReader world, BlockPos pos, Direction side) {
        return isConnected(world.getBlockState(pos), side);
    }

    public boolean isConnected(BlockState state, Direction side) {
        if (!sides.contains(side)) return false;
        return state.get(PROPERTIES.get(side));
    }

    public boolean tryConnect(World world, BlockPos pos, Direction side) {
        if (!sides.contains(side)) return false;

        BlockState state = world.getBlockState(pos);
        if (state.get(PROPERTIES.get(side))) return false;

        BlockState newState = updateStateBasedOnNeighbor(world, pos, state, side, pos.offset(side), world.getBlockState(pos.offset(side)));
        if (newState == state) return false;

        if (world.isRemote()) return true;
        world.setBlockState(pos, newState);
        return true;
    }

    public boolean disconnect(World world, BlockPos pos, Direction side) {
        if (!sides.contains(side)) return false;

        BlockState state = world.getBlockState(pos);
        if (!state.get(PROPERTIES.get(side))) return false;

        if (world.isRemote()) return true;

        BlockState newState = state.with(PROPERTIES.get(side), false);
        world.setBlockState(pos, newState, 1 | 3 | 16);
        return true;
    }

    @Nullable
    public Direction getSideHit(BlockRayTraceResult hit) {
        if (hit.hitInfo instanceof HitInfo) {
            return ((HitInfo) hit.hitInfo).getSide();
        }
        return null;
    }

    public boolean shouldBeConnected(IWorld world, BlockPos pos, BlockState state, Direction face, BlockPos neighborPos, BlockState neighborState) {
        return stateProvider.shouldConnect(world, pos, state, face, neighborPos, neighborState);
    }

    @Override
    protected void buildState(BlockStateBuilder state) {
        for (Direction side : sides) {
            state.add(PROPERTIES.get(side)).withDefault(false);
        }
    }

    @Override
    protected BlockState updateStateBasedOnNeighbor(IWorld world, BlockPos pos, BlockState state, Direction face, BlockPos neighborPos, BlockState neighborState) {
        boolean connected = shouldBeConnected(world, pos, state, face, neighborPos, neighborState);
        if (disabledConnections != null && disabledConnections.isSideDisabled(world, pos, face)) {
            connected = false;
        }
        return state.with(PROPERTIES.get(face), connected);
    }

    @Override
    protected VoxelShape getShape(IBlockReader world, BlockPos pos, BlockState state) {
        VoxelShape shape = shapes.get(state);
        if (shape != null) return shape;

        List<VoxelShape> shapes = new ArrayList<>();
        for (Direction side : sides) {
            if (!state.get(PROPERTIES.get(side))) continue;
            shapes.add(sideShapes.get(side));
        }
        VoxelShape merged = VoxelShapeHelper.merge(shapes);
        this.shapes.put(state, merged);
        return merged;
    }

    public interface ConnectionStateProvider {

        boolean shouldConnect(IWorld world, BlockPos pos, BlockState state, Direction face, BlockPos neighborPos, BlockState neighborState);

        default ConnectionStateProvider or(ConnectionStateProvider other) {
            return (world, pos, state, face, neighborPos, neighborState) ->
                shouldConnect(world, pos, state, face, neighborPos, neighborState)
                    || other.shouldConnect(world, pos, state, face, neighborPos, neighborState);
        }

    }

    public static class HitInfo {

        private final Direction side;
        private final VoxelShape shape;

        private HitInfo(Direction side, VoxelShape shape) {
            this.side = side;
            this.shape = shape;
        }

        public Direction getSide() {
            return side;
        }

        public VoxelShape getShape() {
            return shape;
        }

    }

    public static HitInfo getHitInfo(RayTraceResult hit) {
        return hit.hitInfo instanceof HitInfo ? ((HitInfo) hit.hitInfo) : null;
    }

}
