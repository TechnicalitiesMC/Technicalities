package com.technicalitiesmc.lib.block.components;

import com.technicalitiesmc.lib.block.Component;
import com.technicalitiesmc.lib.block.TKBlockComponent;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.util.Direction;

import java.util.EnumSet;

@Component
public class DirectionalPlacement extends TKBlockComponent {

    @Component
    private BlockDirection direction;

    private boolean flip;

    public DirectionalPlacement() {
    }

    public DirectionalPlacement(boolean flip) {
        this.flip = flip;
    }

    @Override
    protected BlockState getStateForPlacement(BlockState state, BlockItemUseContext context) {
        EnumSet<Direction> validDirections = direction.getValid();

        Direction ld = context.getNearestLookingDirection().getOpposite();
        Direction dir = flip ? ld.getOpposite() : ld;
        if (validDirections.contains(dir)) {
            return direction.set(state, dir);
        }

        return state;
    }

}
