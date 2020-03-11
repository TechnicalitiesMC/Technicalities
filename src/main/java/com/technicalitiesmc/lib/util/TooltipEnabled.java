package com.technicalitiesmc.lib.util;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;

public interface TooltipEnabled {

    void addTooltip(List<ITextComponent> tooltip);

    interface Auto extends TooltipEnabled {

        String getTooltipTranslationKey();

        @Override
        default void addTooltip(List<ITextComponent> tooltip) {
            String key = getTooltipTranslationKey();
            tooltip.addAll(TextUtils.getLocalized(key));
            TranslationTextComponent subtitle = new TranslationTextComponent(key + ".subtitle");
            if (subtitle.getKey().equals(subtitle.getFormattedText())) return;
            tooltip.addAll(TextUtils.breakUp(subtitle));
        }

    }

}
