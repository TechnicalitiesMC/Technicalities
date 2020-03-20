package com.technicalitiesmc.pneumatics.tube.modules;

import com.technicalitiesmc.api.tube.ITubeStack;
import com.technicalitiesmc.pneumatics.TKPneumatics;
import com.technicalitiesmc.pneumatics.tube.FlowPriority;
import com.technicalitiesmc.pneumatics.tube.module.LensTubeModule;
import com.technicalitiesmc.pneumatics.tube.module.TubeModuleState;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;

import java.util.Optional;

public class ColorFilterModule extends LensTubeModule<ColorFilterModule.Color> {

    public static final Type<ColorFilterModule, Color> TYPE = new Type<>("color_filter", Color.class, ColorFilterModule::new);

    private final DyeColor color;

    public ColorFilterModule(Context context, Direction side, DyeColor color) {
        super(TYPE, context, side);
        this.color = color;
    }

    private ColorFilterModule(Context context, Direction side, CompoundNBT tag) {
        super(TYPE, context, side, tag);
        this.color = DyeColor.values()[tag.getInt("color")];
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(TKPneumatics.TM_COLOR_FILTER.get(color));
    }

    @Override
    public Color getState() {
        return Color.values()[color.ordinal()];
    }

    @Override
    public Optional<FlowPriority> getFlowPriority(ITubeStack stack) {
        DyeColor stackColor = stack.getColor();
        if (stackColor == null || stackColor == this.color) {
            return Optional.of(FlowPriority.NORMAL);
        }
        return Optional.empty();
    }

    @Override
    public CompoundNBT serialize() {
        CompoundNBT tag = super.serialize();
        tag.putInt("color", color.ordinal());
        return tag;
    }

    public enum Color implements TubeModuleState<Color> {
        WHITE, ORANGE, MAGENTA, LIGHT_BLUE,
        YELLOW, LIME, PINK, GRAY,
        LIGHT_GRAY, CYAN, PURPLE, BLUE,
        BROWN, GREEN, RED, BLACK
    }

}
