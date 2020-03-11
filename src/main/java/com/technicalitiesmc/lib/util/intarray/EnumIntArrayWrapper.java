package com.technicalitiesmc.lib.util.intarray;

import com.technicalitiesmc.lib.util.value.Reference;
import net.minecraft.util.IIntArray;

public class EnumIntArrayWrapper<E extends Enum<E>> implements IIntArray {

    private final Class<E> type;
    private final Reference<E> reference;

    public EnumIntArrayWrapper(Class<E> type, Reference<E> reference) {
        this.type = type;
        this.reference = reference;
    }

    @Override
    public int get(int i) {
        Enum value = reference.get();
        if (value == null) return -1;
        return value.ordinal();
    }

    @Override
    public void set(int i, int val) {
        if (val == -1) reference.set(null);
        else reference.set(type.getEnumConstants()[val]);
    }

    @Override
    public int size() {
        return 1;
    }

}
