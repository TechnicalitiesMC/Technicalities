package com.technicalitiesmc.lib.util.value;

import java.util.ArrayList;
import java.util.List;

public class Value<T> implements Reference<T> {

    private List<Runnable> setCallbacks;
    private T value;

    public Value(T defaultValue, Runnable setCallback) {
        this.value = defaultValue;
        if (setCallback != null) {
            this.setCallbacks = new ArrayList<>();
            this.setCallbacks.add(setCallback);
        }
    }

    public Value(T defaultValue) {
        this(defaultValue, null);
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public void set(T val) {
        value = val;
        if (setCallbacks != null) setCallbacks.forEach(Runnable::run);
    }

    public void onChanged(Runnable callback) {
        if (setCallbacks == null) setCallbacks = new ArrayList<>();
        setCallbacks.add(callback);
    }

}
