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

public class OneWayValveModule extends LensTubeModule<TubeModuleState.Default> {

    public static final Type<OneWayValveModule, TubeModuleState.Default> TYPE = new Type<>("one_way_valve", TubeModuleState.Default.class, OneWayValveModule::new);

    public OneWayValveModule(Direction side) {
        super(TYPE, side);
    }

    private OneWayValveModule(Direction side, CompoundNBT tag) {
        super(TYPE, side, tag);
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(TKPneumatics.TM_ONE_WAY_VALVE);
    }

    @Override
    public Optional<FlowPriority> getFlowPriority(ITubeStack stack) {
        return Optional.empty();
    }

}
