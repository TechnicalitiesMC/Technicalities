package com.technicalitiesmc.lib.util.value;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface Reference<T> {

    T get();

    void set(T val);

    static <T> Reference<T> of(Supplier<T> getter, Consumer<T> setter) {
        return new Reference<T>() {
            @Override
            public T get() {
                return getter.get();
            }

            @Override
            public void set(T val) {
                setter.accept(val);
            }
        };
    }

}
