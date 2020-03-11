package com.technicalitiesmc.base.block.components;

import com.technicalitiesmc.base.TKBase;
import com.technicalitiesmc.base.client.LabelRenderer;
import com.technicalitiesmc.base.network.SyncLabelPacket;
import com.technicalitiesmc.base.network.TKBNetworkHandler;
import com.technicalitiesmc.lib.block.Component;
import com.technicalitiesmc.lib.block.TKBlockComponent;
import com.technicalitiesmc.lib.block.TKBlockData;
import com.technicalitiesmc.lib.client.BindRenderer;
import com.technicalitiesmc.lib.util.CapabilityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
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
@BindRenderer(LabelRenderer.class)
public class Labelable extends TKBlockComponent.WithData<Labelable.Data> {

    @CapabilityInject(Labelable.Data.class)
    private static Capability<Labelable.Data> DATA_CAP;

    public Labelable(LabelSupplier labelSupplier) {
        this(labelSupplier, (w, p, s) -> 0.5F);
    }

    public Labelable(LabelSupplier labelSupplier, OffsetSupplier offsetSupplier) {
        super(() -> new Data(labelSupplier, offsetSupplier));
    }

    public void update(IBlockReader world, BlockPos pos) {
        getData(world, pos).sendUpdate();
    }

    @Override
    protected ActionResultType onRightClicked(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        Direction face = hit.getFace();
        if (face.getAxis() == Direction.Axis.Y) return ActionResultType.PASS;

        ItemStack stack = player.getHeldItem(hand);

        Data data = getData(world, pos);
        int horizontalIndex = face.getHorizontalIndex();
        if (stack.isEmpty()) {
            if (player.isCrouching() && data.sides.get(horizontalIndex)) {
                if (!world.isRemote()) {
                    data.sides.clear(horizontalIndex);
                    data.markDirty();
                    if (!player.isCreative()) {
                        ItemStack labelStack = new ItemStack(TKBase.LABEL);
                        if (!player.inventory.addItemStackToInventory(labelStack)) {
                            Block.spawnAsEntity(world, player.getPosition(), labelStack);
                        }
                    }
                }
                return ActionResultType.SUCCESS;
            }
        } else if (stack.getItem() == TKBase.LABEL) {
            if (!data.sides.get(horizontalIndex)) {
                if (!world.isRemote()) {
                    data.sides.set(horizontalIndex);
                    data.markDirty();
                    if (!player.isCreative()) {
                        stack.shrink(1);
                    }
                }
                return ActionResultType.SUCCESS;
            }
        }

        return ActionResultType.PASS;
    }

    protected NonNullList<ItemStack> getAdditionalDrops(World world, BlockPos pos, BlockState state) {
        Data data = getData(world, pos);
        int amt = data.sides.cardinality();
        if (amt == 0) return NonNullList.create();
        return NonNullList.withSize(1, new ItemStack(TKBase.LABEL, amt));
    }

    @Nonnull
    @Override
    protected <T> LazyOptional<T> getCapability(IWorld world, BlockPos pos, @Nullable Direction face, Data data, Capability<T> capability) {
        return DATA_CAP.orEmpty(capability, LazyOptional.of(() -> getData(world, pos)));
    }

    public static class Data extends TKBlockData {

        private final LabelSupplier labelSupplier;
        private final OffsetSupplier offsetSupplier;
        private final BitSet sides = new BitSet(4);
        private ItemStack label = ItemStack.EMPTY;

        public Data(LabelSupplier labelSupplier, OffsetSupplier offsetSupplier) {
            this.labelSupplier = labelSupplier;
            this.offsetSupplier = offsetSupplier;
        }

        public ItemStack getLabel() {
            if (getHost().getWorld().isRemote) return label;
            return labelSupplier.getLabelItem(getHost().getWorld(), getHost().getPos());
        }

        public float getOffset(Direction side) {
            return offsetSupplier.getOffset(getHost().getWorld(), getHost().getPos(), side);
        }

        public BitSet getSides() {
            return sides;
        }

        private void markDirty() {
            getHost().markDirty();
            sendUpdate();
        }

        @Nonnull
        @Override
        protected CompoundNBT serialize() {
            CompoundNBT tag = super.serialize();
            tag.putByteArray("sides", sides.toByteArray());
            return tag;
        }

        @Override
        protected void deserialize(CompoundNBT tag) {
            super.deserialize(tag);
            sides.clear();
            sides.or(BitSet.valueOf(tag.getByteArray("sides")));
        }

        @Override
        protected CompoundNBT serializeSync() {
            CompoundNBT tag = super.serializeSync();
            tag.putByteArray("sides", sides.toByteArray());
            tag.put("label", getLabel().write(new CompoundNBT()));
            return tag;
        }

        @Override
        protected void deserializeSync(CompoundNBT tag) {
            super.deserializeSync(tag);
            sides.clear();
            sides.or(BitSet.valueOf(tag.getByteArray("sides")));
            label = ItemStack.read(tag.getCompound("label"));
        }

        public void sendUpdate() {
            TKBNetworkHandler.INSTANCE.sendToAllWatching(
                new SyncLabelPacket(getHost().getPos(), serializeSync()),
                getHost().getWorld(), getHost().getPos()
            );
        }

        private void handleUpdate(CompoundNBT tag) {
            deserializeSync(tag);
        }

    }

    public interface LabelSupplier {
        ItemStack getLabelItem(IBlockReader world, BlockPos pos);
    }

    public interface OffsetSupplier {
        float getOffset(IBlockReader world, BlockPos pos, Direction side);
    }

    public static void handleUpdate(IBlockReader world, BlockPos pos, CompoundNBT tag) {
        Data data = CapabilityUtils.getCapability(world, pos, DATA_CAP);
        if (data == null) return;
        data.handleUpdate(tag);
    }

}

