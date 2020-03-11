package com.technicalitiesmc.lib.block.components;

import com.technicalitiesmc.lib.block.Component;
import com.technicalitiesmc.lib.block.TKBlockComponent;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.util.Direction;

import java.util.EnumSet;

@Component
public class AxialPlacement extends TKBlockComponent {

    @Component
    private BlockAxis axis;

    public AxialPlacement() {
    }

    @Override
    protected BlockState getStateForPlacement(BlockState state, BlockItemUseContext context) {
        EnumSet<Direction.Axis> validAxes = axis.getValid();

        Direction.Axis axis = context.getNearestLookingDirection().getAxis();
        if (validAxes.contains(axis)) {
            return this.axis.set(state, axis);
        }
        return state;
    }

}
