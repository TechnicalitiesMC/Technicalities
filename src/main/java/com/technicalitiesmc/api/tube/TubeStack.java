package com.technicalitiesmc.api.tube;

import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

final class TubeStack implements ITubeStack {

    @Nonnull
    private final ItemStack stack;
    @Nullable
    private final DyeColor color;
    private final float speed, offset;

    TubeStack(@Nonnull ItemStack stack, @Nullable DyeColor color, float speed, float offset) {
        this.stack = stack;
        this.color = color;
        this.speed = speed;
        this.offset = offset;
    }

    @Nonnull
    @Override
    public ItemStack getStack() {
        return stack;
    }

    @Nullable
    @Override
    public DyeColor getColor() {
        return color;
    }

    @Override
    public float getSpeed() {
        return speed;
    }

    @Override
    public float getOffset() {
        return offset;
    }

}
