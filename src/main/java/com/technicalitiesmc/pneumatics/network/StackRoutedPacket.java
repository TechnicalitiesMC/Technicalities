package com.technicalitiesmc.pneumatics.network;

import com.technicalitiesmc.pneumatics.tube.route.Route;
import com.technicalitiesmc.pneumatics.tube.route.SimpleRoute;
import com.technicalitiesmc.pneumatics.tube.TubeManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.network.NetworkEvent;

public class StackRoutedPacket extends TubePacket {

    @CapabilityInject(TubeManager.class)
    private static Capability<TubeManager> TUBE_MANAGER_CAPABILITY;

    private long id;
    private BlockPos pos;
    private Route route;

    public StackRoutedPacket(long id, BlockPos pos, Route route) {
        this.id = id;
        this.pos = pos;
        this.route = route;
    }

    public StackRoutedPacket() {
    }

    @Override
    public void serialize(PacketBuffer buf) {
        buf.writeLong(id);
        buf.writeBlockPos(pos);
        Direction direction = route.getDirection();
        buf.writeBoolean(direction != null);
        if (direction != null) {
            buf.writeEnumValue(direction);
        }
    }

    @Override
    public void deserialize(PacketBuffer buf) {
        this.id = buf.readLong();
        this.pos = buf.readBlockPos();
        Direction direction = buf.readBoolean() ? buf.readEnumValue(Direction.class) : null;
        this.route = new SimpleRoute(false, direction);
    }

    @Override
    protected boolean handle(NetworkEvent.Context ctx, World world) {
        ctx.enqueueWork(() -> {
            TubeManager tubeManager = world.getCapability(TUBE_MANAGER_CAPABILITY).orElse(null);
            tubeManager.onStackRouted(id, pos, route);
        });
        return true;
    }

}
