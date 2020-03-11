package com.technicalitiesmc.pneumatics.tube;

import com.technicalitiesmc.api.tube.ITubeStack;
import com.technicalitiesmc.pneumatics.tube.route.Route;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class MovingTubeStack implements ITubeStack {

    private final long id;
    private Tube tube;
    private Direction from;
    private Route route;

    @Nonnull
    private ITubeStack stack;

    MovingTubeStack(long id, @Nonnull ITubeStack stack) {
        this.id = id;
        this.stack = stack;
    }

    MovingTubeStack(Tube tube, CompoundNBT tag) {
        this.id = tag.getLong("id");
        this.tube = tube;
        this.from = Direction.values()[tag.getInt("from")];
        if (tag.contains("route")) {
            route = Route.deserialize(tag.getCompound("route"));
        }
        CompoundNBT stack = tag.getCompound("stack");
        this.stack = ITubeStack
            .of(ItemStack.read(stack.getCompound("stack")))
            .withColor(stack.getInt("color") == -1 ? null : DyeColor.values()[stack.getInt("color")])
            .withOffset(stack.getFloat("offset"))
            .withSpeed(stack.getFloat("speed"))
            .build();
    }

    long getID() {
        return id;
    }

    public BlockPos getPos() {
        return tube.getPos();
    }

    public Direction getFrom() {
        return from;
    }

    Direction getTo() {
        return route != null ? route.getDirection() : null;
    }

    void leaveTube() {
        this.tube = null;
    }

    void joinTube(Tube tube, Direction from) {
        this.tube = tube;
        this.from = from;
        this.route = null;
    }

    void advanceOffset() {
        float offset = getOffset();
        if (offset >= 1.0F) {
            stack = ITubeStack.copyOf(stack).withOffset(offset - 1.0F).build();
        }
    }

    // TODO: Consider moving mutations to tube ticks
    void apply(TubeStackMutation mutation) {
        Builder builder = ITubeStack.copyOf(stack);
        if (mutation.hasColor()) {
            builder.withColor(mutation.getColor());
        }
        stack = builder.build();
    }

    public void tick() {
        if (route != null && !route.isCalculated()) return;

        float currentOffset = getOffset();
        float nextOffset = currentOffset + getSpeed();

        if (route == null && currentOffset <= 0.5F && nextOffset > 0.5F) {
            route = tube.calculateRoute(this);
        }

        if (route != null && route.isCalculated()) {
            TubeStackMutation mutation = tube.getMutation(this);
            if (mutation != null) apply(mutation);
        }
        if (route != null && !route.isCalculated()) {
            nextOffset = 0.5F + getSpeed();
        }

        stack = ITubeStack.copyOf(stack).withOffset(nextOffset).build();
    }

    @Override
    @Nonnull
    public ItemStack getStack() {
        return stack.getStack();
    }

    @Override
    @Nullable
    public DyeColor getColor() {
        return stack.getColor();
    }

    @Override
    public float getSpeed() {
        return stack.getSpeed();
    }

    @Override
    public float getOffset() {
        return stack.getOffset();
    }

    public boolean isValid() {
        if (tube == null || !tube.isValid()) return false;
        return route == null || !route.isCalculated() || route.getDirection() != null;
    }

    public float getInterpolatedOffset(float partialTicks) {
        float off = getOffset() + getSpeed() * partialTicks - getSpeed();
        if (off > 0.5F && (route == null || !route.isCalculated())) return 0.5F;
        return off;
    }

    public Direction getDirection(float offset) {
        return offset <= 0.5F ? from : route.getDirection();
    }

    CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();
        tag.putLong("id", id);
        tag.putInt("from", from.ordinal());
        if (route != null) {
            tag.put("route", Route.serialize(route));
        }

        CompoundNBT stackTag = new CompoundNBT();
        stackTag.put("stack", stack.getStack().write(new CompoundNBT()));
        stackTag.putInt("color", stack.getColor() == null ? -1 : stack.getColor().ordinal());
        stackTag.putFloat("offset", stack.getOffset());
        stackTag.putFloat("speed", stack.getSpeed());
        tag.put("stack", stackTag);

        return tag;
    }

}
