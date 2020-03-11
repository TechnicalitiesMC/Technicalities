package com.technicalitiesmc.pneumatics.network;

import com.technicalitiesmc.lib.network.Packet;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.network.INetHandler;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

public abstract class TubePacket implements Packet {

    TubePacket() {
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean handle(NetworkEvent.Context ctx) {
        INetHandler netHandler = ctx.getNetworkManager().getNetHandler();
        if (!(netHandler instanceof ClientPlayNetHandler)) return false;
        World world = ((ClientPlayNetHandler) netHandler).getWorld();
        return handle(ctx, world);
    }

    protected abstract boolean handle(NetworkEvent.Context ctx, World world);

}
