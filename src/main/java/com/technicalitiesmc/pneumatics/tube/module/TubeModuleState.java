package com.technicalitiesmc.pneumatics.tube.module;

public interface TubeModuleState<T extends Enum<T> & TubeModuleState<T>> {

    default String getStateName() {
        return ((Enum<?>) this).name().toLowerCase();
    }

    enum Default implements TubeModuleState<Default> {
        DEFAULT
    }

}
