package com.technicalitiesmc.lib.block.components;

import com.technicalitiesmc.lib.block.Component;
import com.technicalitiesmc.lib.block.TKBlockComponent;
import net.minecraft.block.BlockState;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IProperty;
import net.minecraft.util.Direction;

import java.util.EnumSet;

@Component
public class BlockAxis extends TKBlockComponent {

    private final EnumSet<Direction.Axis> validAxes;
    private final IProperty<Direction.Axis> property;

    public BlockAxis() {
        this(EnumSet.allOf(Direction.Axis.class));
    }

    public BlockAxis(EnumSet<Direction.Axis> validAxes) {
        this(validAxes, EnumProperty.create("axis", Direction.Axis.class, validAxes));
    }

    public BlockAxis(EnumSet<Direction.Axis> validAxes, IProperty<Direction.Axis> property) {
        this.validAxes = validAxes;
        this.property = property;
    }

    public EnumSet<Direction.Axis> getValid() {
        return validAxes;
    }

    public BlockState set(BlockState state, Direction.Axis direction) {
        return state.with(property, direction);
    }

    public Direction.Axis get(BlockState state) {
        return state.get(property);
    }

    @Override
    protected void buildState(BlockStateBuilder state) {
        state.add(property).withDefault(validAxes.iterator().next());
    }

}
