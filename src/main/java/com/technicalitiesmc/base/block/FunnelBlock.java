package com.technicalitiesmc.base.block;

import com.technicalitiesmc.lib.block.TKBlock;
import com.technicalitiesmc.lib.block.TKBlockData;
import com.technicalitiesmc.lib.math.VoxelShapeHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;

import java.util.HashSet;
import java.util.Set;

public class FunnelBlock extends TKBlock.WithNoData {

    private static final AxisAlignedBB CENTER_BOUNDS = new AxisAlignedBB(0, 12 / 16F, 0, 1, 1, 1);
    private static final VoxelShape SHAPE = VoxelShapeHelper.merge(
        Block.makeCuboidShape(0, 10, 0, 16, 16, 16),
        Block.makeCuboidShape(1, 8, 1, 15, 10, 15),
        Block.makeCuboidShape(2, 6, 2, 14, 8, 14),
        Block.makeCuboidShape(3, 4, 3, 13, 6, 13),
        Block.makeCuboidShape(4, 0, 4, 12, 4, 12)
    );

    public FunnelBlock() {
        super(Block.Properties.create(Material.WOOD).hardnessAndResistance(2.5F));
    }

    @Override
    protected VoxelShape getShape(IBlockReader world, BlockPos pos, BlockState state) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(IBlockReader world, BlockPos pos, BlockState state, ISelectionContext context) {
        if (context.getEntity() != null && context.getEntity() instanceof ItemEntity) {
            return VoxelShapes.empty();
        }
        return super.getCollisionShape(world, pos, state, context);
    }

    @Override
    protected void tick(IWorld world, BlockPos pos, TKBlockData data) {
        Set<ItemEntity> entities = new HashSet<>();
        for (AxisAlignedBB aabb : SHAPE.toBoundingBoxList()) {
            entities.addAll(world.getEntitiesWithinAABB(ItemEntity.class, aabb.offset(pos)));
        }
        for (ItemEntity entity : entities) {
            Vec3d toCenter = new Vec3d(pos).add(0.5, 0.5, 0.5).subtract(entity.getPositionVector());
            Vec3d motion = entity.getMotion().scale(0.5);
            entity.setMotion(
                motion.x + toCenter.x,
                Math.max(-0.2, motion.y),
                motion.z + toCenter.z
            );
        }
    }

}
