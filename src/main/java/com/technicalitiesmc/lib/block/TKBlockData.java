package com.technicalitiesmc.lib.block;

import com.technicalitiesmc.lib.serial.ObjectSerializer;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nonnull;

public class TKBlockData {

    private TKBlockDataHost host;
    private ObjectSerializer serializer;

    final void init(TKBlockDataHost host) {
        this.host = host;
        this.serializer = new ObjectSerializer(this, host::markDirty);
    }

    public final TKBlockDataHost getHost() {
        return host;
    }

    @Nonnull
    protected CompoundNBT serialize() {
        return this.serializer.serialize();
    }

    protected void deserialize(CompoundNBT tag) {
        this.serializer.deserialize(tag);
    }

    protected CompoundNBT serializeSync() {
        return new CompoundNBT();
    }

    protected void deserializeSync(CompoundNBT tag) {
    }

    protected void onLoad() {
    }

    protected void onUnload() {
    }

    public static interface Factory<D extends TKBlockData> {
        D create(TKBlockDataHost host);
    }
}
