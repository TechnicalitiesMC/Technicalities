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

public class SlimyMembraneModule extends LensTubeModule<TubeModuleState.Default> {

    public static final Type<SlimyMembraneModule, TubeModuleState.Default> TYPE = new Type<>("slimy_membrane", TubeModuleState.Default.class, SlimyMembraneModule::new);

    public SlimyMembraneModule(Direction side) {
        super(TYPE, side);
    }

    private SlimyMembraneModule(Direction side, CompoundNBT tag) {
        super(TYPE, side, tag);
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(TKPneumatics.TM_SLIMY_MEMBRANE);
    }

    @Override
    public Optional<FlowPriority> getFlowPriority(ITubeStack stack) {
        return Optional.of(FlowPriority.LOW);
    }

}
