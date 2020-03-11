package com.technicalitiesmc.pneumatics.tube;

import net.minecraft.item.DyeColor;

import javax.annotation.Nullable;

public final class TubeStackMutation {

    private final boolean hasColor;
    @Nullable
    private final DyeColor color;

    private TubeStackMutation(boolean hasColor, @Nullable DyeColor color) {
        this.hasColor = hasColor;
        this.color = color;
    }

    public boolean hasColor() {
        return hasColor;
    }

    @Nullable
    public DyeColor getColor() {
        return color;
    }

    public static Builder create() {
        return new Builder();
    }

    public static final class Builder {

        private boolean hasColor;
        @Nullable
        private DyeColor color;

        public Builder withColor(@Nullable DyeColor color) {
            this.hasColor = true;
            this.color = color;
            return this;
        }

        public TubeStackMutation build() {
            return new TubeStackMutation(
                hasColor, color
            );
        }

    }

}
