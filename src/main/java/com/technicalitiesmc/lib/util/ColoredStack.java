package com.technicalitiesmc.lib.util;

import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nullable;

public final class ColoredStack {

    private final ItemStack stack;
    private final DyeColor color;

    public ColoredStack(ItemStack stack, @Nullable DyeColor color) {
        this.stack = stack;
        this.color = color;
    }

    public ColoredStack(PacketBuffer buf) {
        this.stack = buf.readItemStack();
        this.color = buf.readBoolean() ? buf.readEnumValue(DyeColor.class) : null;
    }

    public ColoredStack(CompoundNBT tag) {
        this.stack = ItemStack.read(tag.getCompound("stack"));
        this.color = tag.contains("color") ? DyeColor.values()[tag.getInt("color")] : null;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    @Nullable
    public DyeColor getColor() {
        return this.color;
    }

    public ColoredStack withColor(DyeColor color) {
        return new ColoredStack(this.stack, color);
    }

    public void write(PacketBuffer buf) {
        buf.writeItemStack(this.stack);
        if (this.color == null) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            buf.writeEnumValue(this.color);
        }
    }

    public CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();
        tag.put("stack", stack.write(new CompoundNBT()));
        if (color != null) {
            tag.putInt("color", color.ordinal());
        }
        return tag;
    }

}
