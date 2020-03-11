package com.technicalitiesmc.pneumatics.network;

import com.technicalitiesmc.pneumatics.tube.TubeStackMutation;
import com.technicalitiesmc.pneumatics.tube.TubeManager;
import net.minecraft.item.DyeColor;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.network.NetworkEvent;

public class StackMutatedPacket extends TubePacket {

    @CapabilityInject(TubeManager.class)
    private static Capability<TubeManager> TUBE_MANAGER_CAPABILITY;

    private long id;
    private TubeStackMutation mutation;

    public StackMutatedPacket(long id, TubeStackMutation mutation) {
        this.id = id;
        this.mutation = mutation;
    }

    public StackMutatedPacket() {
    }

    @Override
    public void serialize(PacketBuffer buf) {
        buf.writeLong(id);
        buf.writeBoolean(mutation.hasColor());
        if (mutation.hasColor()) {
            DyeColor color = mutation.getColor();
            buf.writeBoolean(color != null);
            if (color != null) {
                buf.writeEnumValue(color);
            }
        }
    }

    @Override
    public void deserialize(PacketBuffer buf) {
        this.id = buf.readLong();
        TubeStackMutation.Builder builder = TubeStackMutation.create();
        if (buf.readBoolean()) {
            builder.withColor(buf.readBoolean() ? buf.readEnumValue(DyeColor.class) : null);
        }
        this.mutation = builder.build();
    }

    @Override
    protected boolean handle(NetworkEvent.Context ctx, World world) {
        TubeManager tubeManager = world.getCapability(TUBE_MANAGER_CAPABILITY).orElse(null);
        tubeManager.onStackMutated(id, mutation);
        return true;
    }

}
