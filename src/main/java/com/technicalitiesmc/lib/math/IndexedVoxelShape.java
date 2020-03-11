package com.technicalitiesmc.lib.math;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;

import javax.annotation.Nullable;

public class IndexedVoxelShape extends CustomRayTraceVoxelShape {

    private final int subHit;
    private final Object hitInfo;

    public IndexedVoxelShape(VoxelShape shape, int subHit, Object hitInfo) {
        super(shape);
        this.subHit = subHit;
        this.hitInfo = hitInfo;
    }

    @Override
    @Nullable
    public BlockRayTraceResult rayTrace(Vec3d start, Vec3d end, BlockPos pos) {
        BlockRayTraceResult hit = shape.rayTrace(start, end, pos);
        if (hit == null) return null;
        hit = new IndexedRayTraceResult(hit);
        hit.subHit = subHit;
        hit.hitInfo = hitInfo;
        return hit;
    }

}
