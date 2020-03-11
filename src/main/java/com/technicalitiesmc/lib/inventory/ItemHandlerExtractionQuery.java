package com.technicalitiesmc.lib.inventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.PrimitiveIterator;
import java.util.stream.IntStream;

public class ItemHandlerExtractionQuery {

    public static PrimitiveIterator.OfInt defaultVisitOrder(int size) {
        return IntStream.range(0, size).iterator();
    }

    private final IItemHandler inventory;
    private final int size;

    private final List<ItemStack> items;
    private final int[] extracted;

    private Extraction lastExtraction;
    private boolean committed;

    public ItemHandlerExtractionQuery(IItemHandler inventory) {
        this.inventory = inventory;
        this.size = inventory.getSlots();

        this.items = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            items.add(null);
        }
        this.extracted = new int[size];
    }

    public Extraction extract(ItemFilter filter) {
        return extract(filter, defaultVisitOrder(size));
    }

    public Extraction extract(ItemFilter filter, PrimitiveIterator.OfInt visitOrder) {
        ItemStack stack = ItemStack.EMPTY;
        int extractedAmount = 0;
        int minExtracted = 0, maxExtracted = 0;
        int[] extractedAmounts = new int[size];
        while (visitOrder.hasNext()) {
            int i = visitOrder.nextInt();
            if (stack.isEmpty()) {
                ItemFilter.Simple matchedFilter = getMatch(i, filter);
                if (matchedFilter == null) continue;
                stack = items.get(i).copy();
                ItemFilter.AmountMatchMode mode = matchedFilter.getMode();
                int amount = matchedFilter.getAmount();
                minExtracted = mode == ItemFilter.AmountMatchMode.AT_MOST ? 0 : amount;
                maxExtracted = mode == ItemFilter.AmountMatchMode.AT_LEAST ? 64 : amount;
            } else if (!ItemHandlerHelper.canItemStacksStack(stack, getStack(i))) {
                continue;
            }
            ItemStack s = getStack(i);
            int extracted = Math.min(s.getCount(), maxExtracted - extractedAmount);
            extractedAmounts[i] = extracted;
            extractedAmount += extracted;
            if (extractedAmount == maxExtracted) break;
        }
        if (extractedAmount < minExtracted) {
            return new Extraction(ItemStack.EMPTY, null, 0);
        }
        stack.setCount(extractedAmount);
        return lastExtraction = new Extraction(stack, extractedAmounts, minExtracted);
    }

    public void commit() {
        if (committed) {
            throw new IllegalStateException("This query has already been committed.");
        }
        committed = true;

        for (int i = 0; i < size; i++) {
            int amt = extracted[i];
            if (amt == 0) continue;
            inventory.extractItem(i, amt, false);
        }
    }

    private ItemStack getStack(int slot) {
        ItemStack currentItem = items.get(slot);
        if (currentItem == null) {
            items.set(slot, currentItem = inventory.extractItem(slot, 64, true).copy());
        }
        return currentItem;
    }

    private ItemFilter.Simple getMatch(int slot, ItemFilter filter) {
        ItemStack currentItem = getStack(slot);
        if (currentItem.isEmpty()) return null;
        if (filter instanceof ItemFilter.Multi) {
            return ((ItemFilter.Multi) filter).getMatchingFilter(currentItem);
        }
        if (!(filter instanceof ItemFilter.Simple)) {
            throw new IllegalArgumentException("Custom filter implementations are not allowed in extraction queries.");
        }
        return filter.test(currentItem) ? (ItemFilter.Simple) filter : null;
    }

    public class Extraction {

        private final ItemStack extracted;
        private final int[] extractedAmounts;
        private final int minExtracted;

        private Extraction(ItemStack extracted, int[] extractedAmounts, int minExtracted) {
            this.extracted = extracted;
            this.extractedAmounts = extractedAmounts;
            this.minExtracted = minExtracted;
        }

        public ItemStack getExtracted() {
            return extracted;
        }

        public void commit() {
            if (lastExtraction != this) {
                throw new IllegalStateException("Only the most recent extraction can be committed.");
            }
            lastExtraction = null;

            if (extracted.isEmpty()) {
                throw new IllegalStateException("Attempted to commit a failed extraction.");
            }

            for (int i = 0; i < size; i++) {
                int amt = extractedAmounts[i];
                if (amt == 0) continue;

                ItemStack currentItem = items.get(i);
                currentItem.shrink(amt);
                ItemHandlerExtractionQuery.this.extracted[i] += amt;
            }
        }

        public boolean commitAtMost(int maxExtracted) {
            if (lastExtraction != this) {
                throw new IllegalStateException("Only the most recent extraction can be committed.");
            }
            lastExtraction = null;

            if (extracted.isEmpty()) {
                throw new IllegalStateException("Attempted to commit a failed extraction.");
            }

            if (maxExtracted < minExtracted) return false;

            int left = maxExtracted;
            for (int i = 0; i < size && left > 0; i++) {
                int amt = extractedAmounts[i];
                if (amt == 0) continue;

                int actualAmt = Math.min(left, amt);

                ItemStack currentItem = items.get(i);
                currentItem.shrink(actualAmt);
                ItemHandlerExtractionQuery.this.extracted[i] += actualAmt;
                left -= actualAmt;
            }
            return true;
        }

    }

}
