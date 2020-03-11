package com.technicalitiesmc.pneumatics.network;

import com.technicalitiesmc.api.tube.ITubeStack;
import com.technicalitiesmc.lib.util.CapabilityUtils;
import com.technicalitiesmc.pneumatics.tube.Tube;
import net.minecraft.client.Minecraft;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.network.NetworkEvent;

public class StackJoinedTubePacket extends TubePacket {

    @CapabilityInject(Tube.class)
    private static Capability<Tube> TUBE_CAPABILITY;

    private long id;
    private BlockPos pos;
    private Direction from;
    private ITubeStack stack;

    public StackJoinedTubePacket(long id, BlockPos pos, Direction from, ITubeStack stack) {
        this.id = id;
        this.pos = pos;
        this.from = from;
        this.stack = stack;
    }

    public StackJoinedTubePacket() {
    }

    @Override
    public void serialize(PacketBuffer buf) {
        buf.writeLong(id);
        buf.writeBlockPos(pos);
        buf.writeEnumValue(from);
        buf.writeCompoundTag(stack.getStack().write(new CompoundNBT()));
        DyeColor color = stack.getColor();
        buf.writeBoolean(color != null);
        if (color != null) {
            buf.writeEnumValue(color);
        }
        buf.writeFloat(stack.getOffset());
        buf.writeFloat(stack.getSpeed());
    }

    @Override
    public void deserialize(PacketBuffer buf) {
        this.id = buf.readLong();
        this.pos = buf.readBlockPos();
        this.from = buf.readEnumValue(Direction.class);
        this.stack = ITubeStack
            .of(ItemStack.read(buf.readCompoundTag()))
            .withColor(buf.readBoolean() ? buf.readEnumValue(DyeColor.class) : null)
            .withOffset(buf.readFloat())
            .withSpeed(buf.readFloat())
            .build();
    }

    @Override
    protected boolean handle(NetworkEvent.Context ctx, World world) {
        ctx.enqueueWork(() -> {
            handle(world);
        });
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    private void handle(World world) {
        Minecraft.getInstance().enqueue(() -> {
            Tube tube = CapabilityUtils.getCapability(world, pos, TUBE_CAPABILITY);
            if (tube == null) return;
            tube.insertStack(from, id, stack);
        });
    }

}
