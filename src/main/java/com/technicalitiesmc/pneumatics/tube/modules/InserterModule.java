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

public class InserterModule extends LensTubeModule<TubeModuleState.Default> {

    public static final Type<InserterModule, TubeModuleState.Default> TYPE = new Type<>("inserter", TubeModuleState.Default.class, InserterModule::new);

    public InserterModule(Context context, Direction side) {
        super(TYPE, context, side);
    }

    private InserterModule(Context context, Direction side, CompoundNBT tag) {
        super(TYPE, context, side, tag);
    }

    @Override
    public boolean isDeterministic() {
        return false;
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(TKPneumatics.TM_INSERTER);
    }

    @Override
    public Optional<FlowPriority> getFlowPriority(ITubeStack stack) {
        return Optional.of(FlowPriority.NORMAL);
    }

    @Override
    public boolean canTraverse(ITubeStack stack) {
        return getContext().canOutput(stack);
    }

}
