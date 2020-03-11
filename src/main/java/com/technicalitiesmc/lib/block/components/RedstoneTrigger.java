package com.technicalitiesmc.lib.block.components;

import com.technicalitiesmc.lib.block.Component;
import com.technicalitiesmc.lib.block.TKBlockComponent;
import net.minecraft.block.BlockState;
import net.minecraft.state.IProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

@Component
public class RedstoneTrigger extends TKBlockComponent {

    private static final IProperty<Boolean> TRIGGERED = BlockStateProperties.TRIGGERED;

    private final Set<Callback> risingEdgeCallbacks = new HashSet<>();
    private final Set<Callback> fallingEdgeCallbacks = new HashSet<>();

    public RedstoneTrigger() {
    }

    public RedstoneTrigger(Callback risingEdgeCallback) {
        if (risingEdgeCallback != null) risingEdgeCallbacks.add(risingEdgeCallback);
    }

    public RedstoneTrigger(Callback risingEdgeCallback, Callback fallingEdgeCallback) {
        if (risingEdgeCallback != null) risingEdgeCallbacks.add(risingEdgeCallback);
        if (fallingEdgeCallback != null) fallingEdgeCallbacks.add(fallingEdgeCallback);
    }

    public void onRisingEdge(Callback callback) {
        risingEdgeCallbacks.add(callback);
    }

    public void onFallingEdge(Callback callback) {
        fallingEdgeCallbacks.add(callback);
    }

    public boolean isTriggered(BlockState state) {
        return state.get(TRIGGERED);
    }

    public BlockState set(BlockState state, boolean triggered) {
        return state.with(TRIGGERED, triggered);
    }

    @Override
    protected void buildState(BlockStateBuilder state) {
        state.add(TRIGGERED).withDefault(false);
    }

    @Override
    protected void onNeighborChanged(World world, BlockPos pos, BlockState state, BlockPos neighborPos, boolean isMoving) {
        int input = world.getRedstonePowerFromNeighbors(pos);
        boolean on = input != 0;
        boolean wasOn = state.get(TRIGGERED);
        if (on != wasOn) {
            BlockState newState = state.cycle(TRIGGERED);
            if (on) risingEdgeCallbacks.forEach(c -> c.onTriggered(world, pos, newState));
            else fallingEdgeCallbacks.forEach(c -> c.onTriggered(world, pos, newState));
            world.setBlockState(pos, newState);
        }
    }

    public interface Callback {
        void onTriggered(World world, BlockPos pos, BlockState state);
    }

}
