package com.technicalitiesmc.api.tube;

public interface ITubeStackConsumer {

    /**
     * @param stack
     * @param simulate
     * @return The amount that was rejected.
     */
    int accept(ITubeStack stack, boolean simulate);

}
