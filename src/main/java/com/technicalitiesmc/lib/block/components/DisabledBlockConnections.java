package com.technicalitiesmc.lib.block.components;

import com.technicalitiesmc.lib.block.Component;
import com.technicalitiesmc.lib.block.TKBlockComponent;
import com.technicalitiesmc.lib.block.TKBlockData;
import com.technicalitiesmc.lib.network.ConnectionDisabledPacket;
import com.technicalitiesmc.lib.network.TKLNetworkHandler;
import com.technicalitiesmc.lib.util.CapabilityUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.BitSet;

@Component
public class DisabledBlockConnections extends TKBlockComponent.WithData<DisabledBlockConnections.Data> {

    @CapabilityInject(Data.class)
    private static Capability<Data> DATA_CAP;

    @Component
    private BlockConnections connections;

    public DisabledBlockConnections() {
        super(Data::new);
    }

    @Override
    protected void initData(IWorld world, BlockPos pos, Data data) {
        data.connections = connections;
    }

    public boolean isSideDisabled(IBlockReader world, BlockPos pos, Direction side) {
        Data data = getData(world, pos);
        return data != null && data.disabledSides.get(side.ordinal());
    }

    @Override
    protected ActionResultType onRightClicked(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        ItemStack item = player.getHeldItem(hand);
        if (item.isEmpty() || item.getItem() != Items.STICK) return ActionResultType.PASS;

        Direction side = connections.getSideHit(hit);
        if (side == null) {
            if (hit.subHit == -1) {
                side = hit.getFace();
            }
        }

        if (side != null) {
            if (!world.isRemote()) {
                getData(world, pos).toggle(side);
            }
            return ActionResultType.SUCCESS;
        }

        return ActionResultType.PASS;
    }

    @Override
    protected BlockState updateStateBasedOnNeighbor(IWorld world, BlockPos pos, BlockState state, Direction face, BlockPos neighborPos, BlockState neighborState) {
        if (!connections.isValidSide(face)) return state;

        if (!world.isRemote()) {
            Data data = getData(world, pos);
            if (data != null && data.disabledSides.get(face.ordinal()) && !connections.shouldBeConnected(world, pos, state, face, neighborPos, neighborState)) {
                data.disabledSides.clear(face.ordinal());
            }
        }

        return state;
    }

    @Nonnull
    @Override
    protected <T> LazyOptional<T> getCapability(IWorld world, BlockPos pos, @Nullable Direction face, Data data, Capability<T> capability) {
        return DATA_CAP.orEmpty(capability, LazyOptional.of(() -> getData(world, pos)));
    }

    public static class Data extends TKBlockData {

        private final BitSet disabledSides = new BitSet(6);
        private BlockConnections connections;

        private void toggle(Direction side) {
            World world = getHost().getWorld();
            BlockPos pos = getHost().getPos();
            Data otherData = CapabilityUtils.getCapability(world, pos.offset(side), DATA_CAP);
            if (connections.isConnected(world, pos, side)) {
                set(side, true);
                if (otherData != null) otherData.set(side.getOpposite(), true);
                connections.disconnect(world, pos, side);
                if (otherData != null) otherData.disconnect(side.getOpposite());
            } else {
                set(side, false);
                if (otherData != null) otherData.set(side.getOpposite(), false);
                connections.tryConnect(world, pos, side);
            }
        }

        private void set(Direction side, boolean disabled) {
            disabledSides.set(side.ordinal(), disabled);
            getHost().markDirty();
            BlockPos pos = getHost().getPos();
            TKLNetworkHandler.INSTANCE.sendToAllWatching(
                new ConnectionDisabledPacket(pos, side, disabled),
                getHost().getWorld(), pos
            );
        }

        private void disconnect(Direction side) {
            connections.disconnect(getHost().getWorld(), getHost().getPos(), side);
        }

        @Nonnull
        @Override
        protected CompoundNBT serialize() {
            CompoundNBT tag = super.serialize();
            tag.putByteArray("disabledSides", disabledSides.toByteArray());
            return tag;
        }

        @Override
        protected void deserialize(CompoundNBT tag) {
            super.deserialize(tag);
            disabledSides.clear();
            disabledSides.or(BitSet.valueOf(tag.getByteArray("disabledSides")));
        }

        @Override
        protected CompoundNBT serializeSync() {
            CompoundNBT tag = super.serializeSync();
            tag.putByteArray("disabledSides", disabledSides.toByteArray());
            return tag;
        }

        @Override
        protected void deserializeSync(CompoundNBT tag) {
            super.deserializeSync(tag);
            disabledSides.clear();
            disabledSides.or(BitSet.valueOf(tag.getByteArray("disabledSides")));
        }

    }

    public static void setState(World world, BlockPos pos, Direction side, boolean disabled) {
        Data data = CapabilityUtils.getCapability(world, pos, DATA_CAP);
        if (data == null) return;
        data.disabledSides.set(side.ordinal(), disabled);
    }

}
