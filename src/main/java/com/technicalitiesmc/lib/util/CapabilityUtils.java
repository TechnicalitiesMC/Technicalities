package com.technicalitiesmc.lib.util;

import net.minecraft.nbt.INBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;

public class CapabilityUtils {

    public static <T> T getCapability(IBlockReader world, BlockPos pos, Capability<T> capability) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile == null) return null;
        return tile.getCapability(capability).orElse(null);
    }

    public static <T> T getCapability(IBlockReader world, BlockPos pos, Capability<T> capability, Direction side) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile == null) return null;
        return tile.getCapability(capability, side).orElse(null);
    }

    public static void register(Class<?> type) {
        CapabilityManager.INSTANCE.register(type, CapabilityUtils.nullStorage(), () -> null);
    }

    public static <T> Capability.IStorage<T> nullStorage() {
        return (Capability.IStorage<T>) NULL_STORAGE;
    }

    private static final Capability.IStorage<?> NULL_STORAGE = new Capability.IStorage() {
        @Nullable
        @Override
        public INBT writeNBT(Capability capability, Object instance, Direction side) {
            return null;
        }

        @Override
        public void readNBT(Capability capability, Object instance, Direction side, INBT nbt) {
        }
    };

}
