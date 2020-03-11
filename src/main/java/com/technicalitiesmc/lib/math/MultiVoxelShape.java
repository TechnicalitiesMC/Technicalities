package com.technicalitiesmc.lib.math;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.List;

public class MultiVoxelShape extends CustomRayTraceVoxelShape {

    private final List<VoxelShape> shapes;

    public MultiVoxelShape(VoxelShape parent, List<VoxelShape> shapes) {
        super(parent);
        this.shapes = shapes;
    }

    @Override
    @Nullable
    public BlockRayTraceResult rayTrace(Vec3d start, Vec3d end, BlockPos pos) {
        BlockRayTraceResult closest = null;
        double closestDist = Double.POSITIVE_INFINITY;
        for (VoxelShape shape : shapes) {
            BlockRayTraceResult hit = shape.rayTrace(start, end, pos);
            if (hit == null) continue;
            double dist = hit.getHitVec().squareDistanceTo(start);
            if (closestDist < dist) continue;
            closest = hit;
            closestDist = dist;
        }
        return closest;
    }

}
