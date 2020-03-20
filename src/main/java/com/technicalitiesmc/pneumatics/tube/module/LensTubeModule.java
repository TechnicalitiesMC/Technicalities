package com.technicalitiesmc.pneumatics.tube.module;

import com.technicalitiesmc.lib.math.VoxelShapeHelper;
import com.technicalitiesmc.lib.util.CollectionUtils;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.shapes.VoxelShape;

import java.util.EnumMap;

public abstract class LensTubeModule<S extends Enum<S> & TubeModuleState<S>> extends TubeModule<S> {

    private static final VoxelShape SHAPE = Block.makeCuboidShape(3.5, 2.5, 3.5, 12.5, 3.5, 12.5);
    private static final EnumMap<Direction, VoxelShape> SHAPES = CollectionUtils.newFilledEnumMap(
        Direction.class,
        s -> VoxelShapeHelper.rotate(SHAPE, s)
    );

    protected LensTubeModule(Type<?, S> type, Context context, Direction side) {
        super(type, context, side);
    }

    protected LensTubeModule(Type<?, S> type, Context context, Direction side, CompoundNBT tag) {
        super(type, context, side, tag);
    }

    @Override
    public boolean isDeterministic() {
        return true;
    }

    public RenderType getRenderLayer() {
        return RenderType.getTranslucent();
    }

    @Override
    public VoxelShape getShape() {
        return SHAPES.get(getSide());
    }

}
