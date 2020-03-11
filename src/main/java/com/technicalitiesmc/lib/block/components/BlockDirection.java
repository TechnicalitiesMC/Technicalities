package com.technicalitiesmc.lib.block.components;

import com.technicalitiesmc.lib.block.Component;
import com.technicalitiesmc.lib.block.TKBlockComponent;
import net.minecraft.block.BlockState;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;

import java.util.EnumSet;

@Component
public class BlockDirection extends TKBlockComponent {

    private final EnumSet<Direction> validDirections;
    private final IProperty<Direction> property;

    public BlockDirection() {
        this(EnumSet.allOf(Direction.class), BlockStateProperties.FACING);
    }

    public BlockDirection(EnumSet<Direction> validDirections) {
        this(validDirections, EnumProperty.create("facing", Direction.class, validDirections));
    }

    public BlockDirection(EnumSet<Direction> validDirections, IProperty<Direction> property) {
        this.validDirections = validDirections;
        this.property = property;
    }

    public EnumSet<Direction> getValid() {
        return validDirections;
    }

    public BlockState set(BlockState state, Direction direction) {
        return state.with(property, direction);
    }

    public Direction get(BlockState state) {
        return state.get(property);
    }

    @Override
    protected void buildState(BlockStateBuilder state) {
        state.add(property).withDefault(validDirections.iterator().next());
    }

}
