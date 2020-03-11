package com.technicalitiesmc.lib.util.intarray;

import net.minecraft.util.IIntArray;

public class IntArrayWrapper implements IIntArray {

    private final int[] ints;

    public IntArrayWrapper(int[] ints) {
        this.ints = ints;
    }

    @Override
    public int get(int i) {
        return ints[i];
    }

    @Override
    public void set(int i, int val) {
        ints[i] = val;
    }

    @Override
    public int size() {
        return ints.length;
    }

}
