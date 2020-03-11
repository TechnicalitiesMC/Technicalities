package com.technicalitiesmc.lib.math;

import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class VoxelShapeHelper {

    public static VoxelShape rotate(VoxelShape down, Direction direction) {
        if (direction == Direction.DOWN) return down;

        return down.toBoundingBoxList().parallelStream()
            .map(b -> VoxelShapes.create(rotate(b, direction)))
            .reduce((a, b) -> VoxelShapes.combine(a, b, IBooleanFunction.OR))
            .map(VoxelShape::simplify).orElse(VoxelShapes.empty());
    }

    public static AxisAlignedBB rotate(AxisAlignedBB down, Direction direction) {
        if (direction == Direction.DOWN)
            return down;
        if (direction == Direction.UP)
            return down.offset(0, 1 - down.maxY, 0);
        if (direction == Direction.NORTH)
            return new AxisAlignedBB(down.minX, down.minZ, down.minY, down.maxX, down.maxZ, down.maxY);
        if (direction == Direction.SOUTH)
            return new AxisAlignedBB(down.minX, down.minZ, 1 - down.maxY, down.maxX, down.maxZ, 1 - down.minY);
        if (direction == Direction.WEST)
            return new AxisAlignedBB(down.minY, down.minX, down.minZ, down.maxY, down.maxX, down.maxZ);
        if (direction == Direction.EAST)
            return new AxisAlignedBB(1 - down.maxY, down.minX, down.minZ, 1 - down.minY, down.maxX, down.maxZ);
        return down;
    }

    public static VoxelShape merge(VoxelShape... shapes) {
        return merge(Arrays.asList(shapes));
    }

    public static VoxelShape merge(List<VoxelShape> shapes) {
        if (shapes.isEmpty()) return VoxelShapes.empty();
        if (shapes.size() == 1) return shapes.iterator().next();

        VoxelShape shape = shapes.stream()
            .filter(Objects::nonNull)
            .reduce((a, b) -> VoxelShapes.combine(unwrap(a), unwrap(b), IBooleanFunction.OR))
            .map(VoxelShape::simplify).orElse(VoxelShapes.empty());

        if (shapes.stream().noneMatch(s -> s instanceof CustomRayTraceVoxelShape)) {
            return shape;
        }

        return new MultiVoxelShape(shape, shapes);
    }

    private static VoxelShape unwrap(VoxelShape shape) {
        return shape instanceof CustomRayTraceVoxelShape ? ((CustomRayTraceVoxelShape) shape).shape : shape;
    }

}
