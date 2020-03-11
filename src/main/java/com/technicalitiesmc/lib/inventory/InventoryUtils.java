package com.technicalitiesmc.lib.inventory;

import com.technicalitiesmc.api.tube.ITubeStackConsumer;
import com.technicalitiesmc.lib.util.CapabilityUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.PrimitiveIterator;

public class InventoryUtils {

    @CapabilityInject(IItemHandler.class)
    private static Capability<IItemHandler> ITEM_HANDLER;
    @CapabilityInject(ITubeStackConsumer.class)
    private static Capability<ITubeStackConsumer> TUBE_STACK_CONSUMER;

    public static ITubeStackConsumer getNeighborTubeStackConsumer(IWorld world, BlockPos pos, Direction side) {
        return CapabilityUtils.getCapability(world, pos.offset(side), TUBE_STACK_CONSUMER, side.getOpposite());
    }

    public static IItemHandler getNeighborItemHandler(IWorld world, BlockPos pos, Direction side) {
        return CapabilityUtils.getCapability(world, pos.offset(side), ITEM_HANDLER, side.getOpposite());
    }

    public static IItemHandler getNeighborItemHandlerOrDrop(IWorld world, BlockPos pos, Direction side, int maxStacks) {
        if (!world.isAirBlock(pos.offset(side))) {
            return getNeighborItemHandler(world, pos, side);
        } else {
            return new DroppingItemHandler(world, maxStacks, DroppingItemHandler.dropInFront(pos, side));
        }
    }

    public static IItemHandler getNeighborItemHandlerOrPickUp(IWorld world, BlockPos pos, Direction side, AxisAlignedBB area) {
        if (!world.isAirBlock(pos.offset(side))) {
            return getNeighborItemHandler(world, pos, side);
        } else {
            return new DroppedItemHandler(world, area);
        }
    }

    public static int calculateComparatorOutput(Inventory... inventories) {
        return calculateComparatorOutput(Arrays.asList(inventories));
    }

    public static int calculateComparatorOutput(Iterable<? extends Inventory> inventories) {
        float total = 0;
        int totalSlots = 0;
        for (Inventory inventory : inventories) {
            for (Inventory.Slot slot : inventory) {
                ItemStack stack = slot.get();
                if (!stack.isEmpty()) {
                    total += stack.getCount() / (float) stack.getMaxStackSize();
                }
            }
            totalSlots += inventory.getSize();
        }
        return (int) Math.ceil(15 * total / totalSlots);
    }

    public static NonNullList<ItemStack> collectDrops(Inventory... inventories) {
        return collectDrops(Arrays.asList(inventories));
    }

    public static NonNullList<ItemStack> collectDrops(Iterable<? extends Inventory> inventories) {
        NonNullList<ItemStack> drops = NonNullList.create();
        for (Inventory inventory : inventories) {
            for (Inventory.Slot slot : inventory) {
                if (!slot.isEmpty()) {
                    drops.add(slot.get());
                }
            }
        }
        return drops;
    }

    public static boolean transferStack(IItemHandler src, IItemHandler dst) {
        return transferStack(src, dst, ItemFilter.atMost(64).ofAnyItem());
    }

    public static boolean transferStack(IItemHandler src, IItemHandler dst, ItemFilter filter) {
        return transferStack(src, dst, filter, ItemHandlerExtractionQuery.defaultVisitOrder(src.getSlots()));
    }

    public static boolean transferStack(IItemHandler src, IItemHandler dst, ItemFilter filter, PrimitiveIterator.OfInt visitOrder) {
        ItemHandlerExtractionQuery extractionQuery = new ItemHandlerExtractionQuery(src);
        ItemHandlerExtractionQuery.Extraction extraction = extractionQuery.extract(filter, visitOrder);
        if (extraction.getExtracted().isEmpty()) return false;

        ItemHandlerInsertionQuery insertionQuery = new ItemHandlerInsertionQuery(dst);
        ItemHandlerInsertionQuery.Insertion insertion = insertionQuery.insert(extraction.getExtracted());

        int extracted = extraction.getExtracted().getCount();
        int leftover = insertion.getLeftover().getCount();
        int inserted = extracted - leftover;
        if (inserted == 0) return false;

        if (extraction.commitAtMost(inserted)) {
            insertion.commit();
            extractionQuery.commit();
            insertionQuery.commit();
            return true;
        }
        return false;
    }

    public static ItemStack transferStack(ItemStack stack, IItemHandler dst, boolean simulate) {
        ItemHandlerInsertionQuery insertionQuery = new ItemHandlerInsertionQuery(dst);
        ItemHandlerInsertionQuery.Insertion insertion = insertionQuery.insert(stack);
        if (!simulate) {
            insertion.commit();
            insertionQuery.commit();
        }
        return insertion.getLeftover();
    }

    @Nonnull
    public static ItemSet collectInventory(IItemHandler inventory) {
        ItemSet set = new ItemSet();
        for (int i = 0; i < inventory.getSlots(); i++) {
            set.add(inventory.getStackInSlot(i));
        }
        return set;
    }

    @Nonnull
    public static ItemSet collectInventory(Inventory inventory) {
        ItemSet set = new ItemSet();
        for (Inventory.Slot slot : inventory) {
            set.add(slot.get());
        }
        return set;
    }

}
