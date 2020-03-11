package com.technicalitiesmc.base.block;

import com.technicalitiesmc.base.TKBase;
import com.technicalitiesmc.base.block.components.Labelable;
import com.technicalitiesmc.lib.block.Component;
import com.technicalitiesmc.lib.block.TKBlock;
import com.technicalitiesmc.lib.block.TKBlockData;
import com.technicalitiesmc.lib.block.components.AxialPlacement;
import com.technicalitiesmc.lib.block.components.BlockAxis;
import com.technicalitiesmc.lib.math.VoxelShapeHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BarrelBlock extends TKBlock.WithData<BarrelBlock.Data> {

    @CapabilityInject(IItemHandler.class)
    private static Capability<IItemHandler> ITEM_HANDLER;

    private static final VoxelShape SHAPE_X = VoxelShapeHelper.merge(
        Block.makeCuboidShape(0, 4, 0, 16, 12, 16),
        Block.makeCuboidShape(0, 2, 1, 16, 14, 15),
        Block.makeCuboidShape(0, 1, 2, 16, 15, 14),
        Block.makeCuboidShape(0, 0, 4, 16, 16, 12)
    );
    private static final VoxelShape SHAPE_Y = VoxelShapeHelper.merge(
        Block.makeCuboidShape(4, 0, 0, 12, 16, 16),
        Block.makeCuboidShape(2, 0, 1, 14, 16, 15),
        Block.makeCuboidShape(1, 0, 2, 15, 16, 14),
        Block.makeCuboidShape(0, 0, 4, 16, 16, 12)
    );
    private static final VoxelShape SHAPE_Z = VoxelShapeHelper.merge(
        Block.makeCuboidShape(4, 0, 0, 12, 16, 16),
        Block.makeCuboidShape(2, 1, 0, 14, 15, 16),
        Block.makeCuboidShape(1, 2, 0, 15, 14, 16),
        Block.makeCuboidShape(0, 4, 0, 16, 12, 16)
    );

    private static final int MAX_STACKS = 32;

    @Component
    private final BlockAxis axis = new BlockAxis();
    @Component
    private final AxialPlacement placement = new AxialPlacement();
    @Component
    private final Labelable label = new Labelable(this::getLabelItem, this::getLabelOffset);

    public BarrelBlock() {
        super(Block.Properties.create(Material.WOOD).hardnessAndResistance(3F), Data::new);
    }

    @Override
    protected void initData(IWorld world, BlockPos pos, Data data) {
        data.label = label;
    }

    private ItemStack getLabelItem(IBlockReader world, BlockPos pos) {
        return getData(world, pos).stack.copy().split(1);
    }

    private float getLabelOffset(IBlockReader world, BlockPos pos, Direction side) {
        Direction.Axis axis = this.axis.get(world.getBlockState(pos));
        return axis != side.getAxis() ? 0.5F : 0.5F - 1 / 16F;
    }

    @Override
    protected VoxelShape getShape(IBlockReader world, BlockPos pos, BlockState state) {
        Direction.Axis axis = this.axis.get(state);
        return axis == Direction.Axis.X ? SHAPE_X : axis == Direction.Axis.Y ? SHAPE_Y : SHAPE_Z;
    }

    @Override
    protected ActionResultType onRightClicked(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        ItemStack heldItem = player.getHeldItem(hand);
        if (heldItem.isEmpty()) return ActionResultType.PASS;
        if (heldItem.getItem() == TKBase.LABEL) return ActionResultType.PASS;

        Data data = getData(world, pos);
        boolean hadItem = !data.stack.isEmpty();
        int inserted = data.insert(heldItem, world.isRemote());
        if (!world.isRemote()) {
            heldItem.shrink(inserted);
            if (!hadItem && !data.stack.isEmpty()) {
                label.update(world, pos);
            }
        }
        return inserted > 0 ? ActionResultType.SUCCESS : ActionResultType.PASS;
    }

    @Override
    protected ActionResultType onLeftClicked(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isRemote()) {
            ItemStack stack = getData(world, pos).extract(64, false);
            if (stack.isEmpty()) return ActionResultType.FAIL;
            if (!player.inventory.addItemStackToInventory(stack)) {
                Block.spawnAsEntity(world, player.getPosition(), stack);
            }
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    protected NonNullList<ItemStack> getAdditionalDrops(World world, BlockPos pos, BlockState state) {
        NonNullList<ItemStack> stacks = NonNullList.create();

        Data data = getData(world, pos);
        if (data.stack.isEmpty()) return stacks;

        ItemStack stack = data.stack.copy();
        while (!stack.isEmpty()) {
            stacks.add(stack.split(stack.getMaxStackSize()));
        }
        return stacks;
    }

    @Override
    protected boolean overridesComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected int getComparatorOutput(World world, BlockPos pos, BlockState state) {
        Data data = getData(world, pos);
        float ratio = data.stack.getCount() / (float) (data.stack.getMaxStackSize() * MAX_STACKS);
        return (int) Math.ceil(ratio * 15);
    }

    @Nonnull
    @Override
    protected <T> LazyOptional<T> getCapability(IWorld world, BlockPos pos, @Nullable Direction face, Data data, Capability<T> capability) {
        return ITEM_HANDLER.orEmpty(capability, LazyOptional.of(() -> new Inventory(getData(world, pos))));
    }

    static class Data extends TKBlockData {

        private ItemStack stack = ItemStack.EMPTY;
        private Labelable label;

        private int insert(ItemStack newStack, boolean simulate) {
            if (stack.isEmpty()) {
                if (!simulate) {
                    stack = newStack.copy();
                    getHost().markDirty();
                    label.update(getHost().getWorld(), getHost().getPos());
                }
                return newStack.getCount();
            } else {
                if (!ItemHandlerHelper.canItemStacksStack(stack, newStack)) return 0;
                int inserted = Math.min(MAX_STACKS * stack.getMaxStackSize() - stack.getCount(), newStack.getCount());
                if (inserted <= 0) return 0;
                if (!simulate) {
                    stack.grow(inserted);
                }
                return inserted;
            }
        }

        private ItemStack extract(int maxAmt, boolean simulate) {
            if (stack.isEmpty()) return ItemStack.EMPTY;
            ItemStack extracted = (simulate ? stack.copy() : stack).split(Math.min(maxAmt, stack.getMaxStackSize()));
            if (!simulate && stack.isEmpty()) {
                getHost().markDirty();
                label.update(getHost().getWorld(), getHost().getPos());
            }
            return extracted;
        }

        @Nonnull
        @Override
        protected CompoundNBT serialize() {
            CompoundNBT tag = super.serialize();
            tag.put("stack", stack.write(new CompoundNBT()));
            return tag;
        }

        @Override
        protected void deserialize(CompoundNBT tag) {
            super.deserialize(tag);
            stack = ItemStack.read(tag.getCompound("stack"));
        }

    }

    private static class Inventory implements IItemHandler {

        private final Data data;

        private Inventory(Data data) {
            this.data = data;
        }

        @Override
        public int getSlots() {
            return 2;
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return slot == 0 ? ItemStack.EMPTY : data.stack.copy().split(data.stack.getMaxStackSize());
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (slot != 0) return stack;
            int inserted = data.insert(stack, simulate);
            return stack.copy().split(stack.getCount() - inserted);
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot != 1) return ItemStack.EMPTY;
            return data.extract(amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return true;
        }

    }

}
