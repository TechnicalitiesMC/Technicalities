package com.technicalitiesmc.lib.util;

import net.minecraft.item.DyeColor;
import net.minecraft.util.IStringSerializable;

public enum PaintAction implements IStringSerializable, TooltipEnabled.Auto {
    KEEP_PAINT(null, "keep"),
    REMOVE_PAINT(null, "remove"),
    PAINT_WHITE(DyeColor.WHITE, "white"),
    PAINT_ORANGE(DyeColor.ORANGE, "orange"),
    PAINT_MAGENTA(DyeColor.MAGENTA, "magenta"),
    PAINT_LIGHT_BLUE(DyeColor.LIGHT_BLUE, "light_blue"),
    PAINT_YELLOW(DyeColor.YELLOW, "yellow"),
    PAINT_LIME(DyeColor.LIME, "lime"),
    PAINT_PINK(DyeColor.PINK, "pink"),
    PAINT_GRAY(DyeColor.GRAY, "gray"),
    PAINT_LIGHT_GRAY(DyeColor.LIGHT_GRAY, "light_gray"),
    PAINT_CYAN(DyeColor.CYAN, "cyan"),
    PAINT_PURPLE(DyeColor.PURPLE, "purple"),
    PAINT_BLUE(DyeColor.BLUE, "blue"),
    PAINT_BROWN(DyeColor.BROWN, "brown"),
    PAINT_GREEN(DyeColor.GREEN, "green"),
    PAINT_RED(DyeColor.RED, "red"),
    PAINT_BLACK(DyeColor.BLACK, "black");

    private final DyeColor color;
    private final String name;

    PaintAction(DyeColor color, String name) {
        this.color = color;
        this.name = name;
    }

    public DyeColor getColor() {
        return color;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getTooltipTranslationKey() {
        return "container.technicalities.generic.paint_action." + getName();
    }


}
