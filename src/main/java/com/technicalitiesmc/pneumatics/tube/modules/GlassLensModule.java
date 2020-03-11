package com.technicalitiesmc.pneumatics.tube.modules;

import com.technicalitiesmc.api.tube.ITubeStack;
import com.technicalitiesmc.pneumatics.TKPneumatics;
import com.technicalitiesmc.pneumatics.tube.FlowPriority;
import com.technicalitiesmc.pneumatics.tube.module.LensTubeModule;
import com.technicalitiesmc.pneumatics.tube.module.TubeModuleState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;

import java.util.Optional;

public class GlassLensModule extends LensTubeModule<TubeModuleState.Default> {

    public static final Type<GlassLensModule, TubeModuleState.Default> TYPE = new Type<>("glass_lens", TubeModuleState.Default.class, GlassLensModule::new);

    public GlassLensModule(Direction side) {
        super(TYPE, side);
    }

    private GlassLensModule(Direction side, CompoundNBT tag) {
        super(TYPE, side, tag);
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(TKPneumatics.TM_GLASS_LENS);
    }

    @Override
    public Optional<FlowPriority> getFlowPriority(ITubeStack stack) {
        if (stack.getColor() != null) return Optional.empty();
        return Optional.of(FlowPriority.NORMAL);
    }

}
