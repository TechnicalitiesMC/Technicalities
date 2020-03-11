package com.technicalitiesmc.base.block;

import com.technicalitiesmc.lib.block.TKBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

public class CraftingSlabBlock extends TKBlock {

    private static final VoxelShape SHAPE = Block.makeCuboidShape(0, 0, 0, 16, 6, 16);

    public CraftingSlabBlock() {
        super(Block.Properties.create(Material.ROCK).hardnessAndResistance(3F));
    }

    @Override
    protected VoxelShape getShape(IBlockReader world, BlockPos pos, BlockState state) {
        return SHAPE;
    }

}
