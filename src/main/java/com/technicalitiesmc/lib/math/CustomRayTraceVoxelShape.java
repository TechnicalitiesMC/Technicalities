package com.technicalitiesmc.lib.math;

import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.SplitVoxelShape;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public abstract class CustomRayTraceVoxelShape extends SplitVoxelShape {

    protected final VoxelShape shape;

    public CustomRayTraceVoxelShape(VoxelShape shape) {
        super(shape, Direction.Axis.Y, 0);
        this.shape = shape;
    }

    @Override
    public double getStart(Direction.Axis p_197762_1_) {
        return shape.getStart(p_197762_1_);
    }

    @Override
    public double getEnd(Direction.Axis p_197758_1_) {
        return shape.getEnd(p_197758_1_);
    }

    @Override
    public AxisAlignedBB getBoundingBox() {
        return shape.getBoundingBox();
    }

    @Override
    public boolean isEmpty() {
        return shape.isEmpty();
    }

    @Override
    public VoxelShape withOffset(double p_197751_1_, double p_197751_3_, double p_197751_5_) {
        return shape.withOffset(p_197751_1_, p_197751_3_, p_197751_5_);
    }

    @Override
    public VoxelShape simplify() {
        return shape.simplify();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void forEachEdge(VoxelShapes.ILineConsumer p_197754_1_) {
        shape.forEachEdge(p_197754_1_);
    }

    @Override
    public void forEachBox(VoxelShapes.ILineConsumer p_197755_1_) {
        shape.forEachBox(p_197755_1_);
    }

    @Override
    public List<AxisAlignedBB> toBoundingBoxList() {
        return shape.toBoundingBoxList();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public double min(Direction.Axis p_197764_1_, double p_197764_2_, double p_197764_4_) {
        return shape.min(p_197764_1_, p_197764_2_, p_197764_4_);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public double max(Direction.Axis p_197760_1_, double p_197760_2_, double p_197760_4_) {
        return shape.max(p_197760_1_, p_197760_2_, p_197760_4_);
    }

    @Override
    @Nullable
    public abstract BlockRayTraceResult rayTrace(Vec3d start, Vec3d end, BlockPos pos);

    @Override
    public VoxelShape project(Direction p_212434_1_) {
        return shape.project(p_212434_1_);
    }

    @Override
    public double getAllowedOffset(Direction.Axis p_212430_1_, AxisAlignedBB p_212430_2_, double p_212430_3_) {
        return shape.getAllowedOffset(p_212430_1_, p_212430_2_, p_212430_3_);
    }

}
