package com.technicalitiesmc.lib.util.intarray;

import com.technicalitiesmc.lib.util.value.Reference;
import net.minecraft.util.IIntArray;

public class BooleanIntArrayWrapper implements IIntArray {

    private final Reference<Boolean> reference;

    public BooleanIntArrayWrapper(Reference<Boolean> reference) {
        this.reference = reference;
    }

    @Override
    public int get(int i) {
        Boolean bool = reference.get();
        return bool != null ? (bool ? 1 : 0) : 0;
    }

    @Override
    public void set(int i, int val) {
        reference.set(val != 0);
    }

    @Override
    public int size() {
        return 1;
    }

}
