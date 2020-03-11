package com.technicalitiesmc.pneumatics.network;

import com.technicalitiesmc.lib.util.CapabilityUtils;
import com.technicalitiesmc.pneumatics.tube.module.ModuleManager;
import com.technicalitiesmc.pneumatics.tube.module.TubeModule;
import com.technicalitiesmc.pneumatics.tube.module.TubeModuleContainer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.network.NetworkEvent;

public class ModuleUpdatedPacket extends TubePacket {

    @CapabilityInject(TubeModuleContainer.class)
    private static Capability<TubeModuleContainer> TUBE_MODULE_CONTAINER;

    private BlockPos pos;
    private Direction side;
    private TubeModule.Type<?, ?> type;
    private CompoundNBT data;

    public ModuleUpdatedPacket(BlockPos pos, Direction side, TubeModule.Type<?, ?> type, CompoundNBT data) {
        this.pos = pos;
        this.side = side;
        this.type = type;
        this.data = data;
    }

    public ModuleUpdatedPacket() {
    }

    @Override
    public void serialize(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        buf.writeEnumValue(side);
        if (type == null) {
            buf.writeBoolean(false);
            return;
        }
        buf.writeBoolean(true);
        buf.writeString(type.getName());
        buf.writeCompoundTag(data);
    }

    @Override
    public void deserialize(PacketBuffer buf) {
        this.pos = buf.readBlockPos();
        this.side = buf.readEnumValue(Direction.class);
        if (!buf.readBoolean()) {
            this.type = null;
            return;
        }
        this.type = ModuleManager.INSTANCE.get(buf.readString());
        this.data = buf.readCompoundTag();
    }

    @Override
    protected boolean handle(NetworkEvent.Context ctx, World world) {
        ctx.enqueueWork(() -> {
            TubeModuleContainer container = CapabilityUtils.getCapability(world, pos, TUBE_MODULE_CONTAINER);
            if (container == null) return;
            container.onUpdate(side, type, data);
        });
        return true;
    }

}
