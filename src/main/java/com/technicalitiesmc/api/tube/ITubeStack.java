package com.technicalitiesmc.api.tube;

import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ITubeStack {

    @Nonnull
    ItemStack getStack();

    @Nullable
    DyeColor getColor();

    float getSpeed();

    float getOffset();

    static Builder of(ItemStack stack) {
        return new Builder(stack);
    }

    static Builder copyOf(ITubeStack stack) {
        return new Builder(stack);
    }

    final class Builder {

        @Nonnull
        private ItemStack stack;
        private DyeColor color;
        private float speed, offset;

        private Builder(ItemStack stack) {
            this.stack = stack;
        }

        private Builder(ITubeStack stack) {
            this.stack = stack.getStack();
            this.color = stack.getColor();
            this.speed = stack.getSpeed();
            this.offset = stack.getOffset();
        }

        public Builder withStack(@Nonnull ItemStack stack) {
            this.stack = stack;
            return this;
        }

        public Builder withColor(@Nullable DyeColor color) {
            this.color = color;
            return this;
        }

        public Builder withSpeed(float speed) {
            this.speed = speed;
            return this;
        }

        public Builder withOffset(float offset) {
            this.offset = offset;
            return this;
        }

        public ITubeStack build() {
            return new TubeStack(stack, color, speed, offset);
        }

    }

}
