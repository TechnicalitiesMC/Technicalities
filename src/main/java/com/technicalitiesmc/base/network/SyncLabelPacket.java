package com.technicalitiesmc.base.network;

import com.technicalitiesmc.base.block.components.Labelable;
import com.technicalitiesmc.lib.network.Packet;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

public class SyncLabelPacket implements Packet {

    private BlockPos pos;
    private CompoundNBT tag;

    public SyncLabelPacket(BlockPos pos, CompoundNBT tag) {
        this.pos = pos;
        this.tag = tag;
    }

    public SyncLabelPacket() {
    }

    @Override
    public void serialize(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        buf.writeCompoundTag(tag);
    }

    @Override
    public void deserialize(PacketBuffer buf) {
        pos = buf.readBlockPos();
        tag = buf.readCompoundTag();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean handle(NetworkEvent.Context ctx) {
        INetHandler netHandler = ctx.getNetworkManager().getNetHandler();
        if (!(netHandler instanceof ClientPlayNetHandler)) return false;
        World world = ((ClientPlayNetHandler) netHandler).getWorld();
        ctx.enqueueWork(() -> {
            Labelable.handleUpdate(world, pos, tag);
        });
        return true;
    }

}
