package com.technicalitiesmc.lib.inventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ItemSet implements Set<ItemStack> {

    private final Set<ItemStack> stacks = new HashSet<>();

    @Nonnull
    public ItemStack findStack(ItemStack type) {
        for (ItemStack stack : stacks) {
            if (ItemHandlerHelper.canItemStacksStack(stack, type)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return stacks.size();
    }

    @Override
    public boolean isEmpty() {
        return stacks.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof ItemStack) {
            ItemStack stack = (ItemStack) o;
            ItemStack storedStack = findStack(stack);
            return !storedStack.isEmpty();
        }
        return false;
    }

    @Override
    public Iterator<ItemStack> iterator() {
        return stacks.iterator();
    }

    @Override
    public Object[] toArray() {
        return stacks.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return stacks.toArray(a);
    }

    @Override
    public boolean add(ItemStack stack) {
        if (stack.isEmpty()) return true;
        ItemStack currentStack = findStack(stack);
        if (currentStack.isEmpty()) {
            stacks.add(stack.copy());
        }
        currentStack.grow(stack.getCount());
        return true;
    }

    @Override
    public boolean remove(Object o) {
        if (!(o instanceof ItemStack)) return false;
        ItemStack stack = (ItemStack) o;
        ItemStack currentStack = findStack(stack);
        if (currentStack.isEmpty()) return false;
        if (currentStack.getCount() > stack.getCount()) return false;
        currentStack.split(stack.getCount());
        if (currentStack.isEmpty()) {
            stacks.remove(currentStack);
        }
        return true;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) return false;
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends ItemStack> c) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) throws UnsupportedOperationException {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        for (Object o : c) {
            if (!remove(o)) return false;
        }
        return true;
    }

    @Override
    public void clear() {
        stacks.clear();
    }

}
