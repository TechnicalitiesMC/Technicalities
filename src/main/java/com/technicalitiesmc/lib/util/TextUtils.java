package com.technicalitiesmc.lib.util;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TextUtils {

    public static List<ITextComponent> getLocalized(String key, Object... args) {
        return breakUp(new TranslationTextComponent(key, args));
    }

    public static List<ITextComponent> breakUp(ITextComponent component) {
        return Arrays.stream(component.getFormattedText().split("\n"))
            .map(StringTextComponent::new)
            .collect(Collectors.toList());
    }

}
