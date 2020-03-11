package com.technicalitiesmc.lib.network;

import com.technicalitiesmc.lib.block.components.DisabledBlockConnections;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

public class ConnectionDisabledPacket implements Packet {

    private BlockPos pos;
    private Direction side;
    private boolean disabled;

    public ConnectionDisabledPacket(BlockPos pos, Direction side, boolean disabled) {
        this.pos = pos;
        this.side = side;
        this.disabled = disabled;
    }

    public ConnectionDisabledPacket() {
    }

    @Override
    public void serialize(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        buf.writeEnumValue(side);
        buf.writeBoolean(disabled);
    }

    @Override
    public void deserialize(PacketBuffer buf) {
        pos = buf.readBlockPos();
        side = buf.readEnumValue(Direction.class);
        disabled = buf.readBoolean();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean handle(NetworkEvent.Context ctx) {
        INetHandler netHandler = ctx.getNetworkManager().getNetHandler();
        if (!(netHandler instanceof ClientPlayNetHandler)) return false;
        World world = ((ClientPlayNetHandler) netHandler).getWorld();
        ctx.enqueueWork(() -> {
            DisabledBlockConnections.setState(world, pos, side, disabled);
        });
        return true;
    }

}
