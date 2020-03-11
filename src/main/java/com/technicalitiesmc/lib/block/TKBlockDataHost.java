package com.technicalitiesmc.lib.block;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface TKBlockDataHost {

    World getWorld();

    BlockPos getPos();

    void markDirty();

    void markModelDataDirty();

    boolean isValid();

}
