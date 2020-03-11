package com.technicalitiesmc.pneumatics.tube.module;

import com.technicalitiesmc.api.tube.ITubeStack;
import com.technicalitiesmc.pneumatics.tube.FlowPriority;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.shapes.VoxelShape;

import java.util.EnumSet;
import java.util.Optional;
import java.util.function.BiFunction;

public abstract class TubeModule<S extends Enum<S> & TubeModuleState<S>> {

    private final Type<?, S> type;
    private final Direction side;

    protected TubeModule(Type<?, S> type, Direction side) {
        this.type = type;
        this.side = side;
    }

    protected TubeModule(Type<?, S> type, Direction side, CompoundNBT tag) {
        this(type, side);
    }

    public final Type<?, S> getType() {
        return type;
    }

    public final Direction getSide() {
        return side;
    }

    public RenderType getRenderLayer() {
        return RenderType.getCutout();
    }

    public S getState() {
        return type.getPossibleStates().iterator().next();
    }

    public abstract ItemStack getItem();

    public abstract VoxelShape getShape();

    public abstract Optional<FlowPriority> getFlowPriority(ITubeStack stack);

    public CompoundNBT serialize() {
        return new CompoundNBT();
    }

    public static class Type<T extends TubeModule<S>, S extends Enum<S> & TubeModuleState<S>> {

        private final String name;
        private final EnumSet<S> possibleStates;
        private final BiFunction<Direction, CompoundNBT, T> factory;

        public Type(String name, Class<S> stateType, BiFunction<Direction, CompoundNBT, T> factory) {
            this.name = name;
            this.possibleStates = EnumSet.allOf(stateType);
            this.factory = factory;
        }

        public String getName() {
            return name;
        }

        public EnumSet<S> getPossibleStates() {
            return possibleStates;
        }

        public T create(Direction side, CompoundNBT tag) {
            return factory.apply(side, tag);
        }

    }

}
