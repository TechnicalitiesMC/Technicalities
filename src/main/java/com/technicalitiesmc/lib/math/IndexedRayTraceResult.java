package com.technicalitiesmc.lib.math;

import net.minecraft.util.math.BlockRayTraceResult;

public class IndexedRayTraceResult extends BlockRayTraceResult {

    public IndexedRayTraceResult(BlockRayTraceResult hit) {
        super(hit.getHitVec(), hit.getFace(), hit.getPos(), hit.isInside());
    }

}
