package com.technicalitiesmc.lib.util;

public enum MutedState implements TooltipEnabled.Auto {
    UNMUTED, MUTED;

    @Override
    public String getTooltipTranslationKey() {
        return "container.technicalities.generic.muted_state." + name().toLowerCase();
    }

}
