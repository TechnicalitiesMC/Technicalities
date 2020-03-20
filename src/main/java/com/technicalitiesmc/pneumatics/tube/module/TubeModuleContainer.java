package com.technicalitiesmc.pneumatics.tube.module;

import com.technicalitiesmc.api.tube.ITubeStack;
import com.technicalitiesmc.pneumatics.block.components.TubeComponent;
import com.technicalitiesmc.pneumatics.network.ModuleUpdatedPacket;
import com.technicalitiesmc.pneumatics.network.TubeNetworkHandler;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public class TubeModuleContainer {

    private final EnumMap<Direction, TubeModule<?>> modules = new EnumMap<>(Direction.class);
    private final Supplier<World> world;
    private final Supplier<BlockPos> pos;
    private final Runnable markDirty;
    private final Runnable refreshModel;
    private final Map<Direction, ModuleContext> contexts = new EnumMap<>(Direction.class);

    public TubeModuleContainer(Supplier<World> world, Supplier<BlockPos> pos, Runnable markDirty, Runnable refreshModel) {
        this.world = world;
        this.pos = pos;
        this.markDirty = markDirty;
        this.refreshModel = refreshModel;
    }

    public Collection<TubeModule<?>> getAll() {
        return modules.values();
    }

    public TubeModule<?> get(Direction side) {
        return modules.get(side);
    }

    public TubeModule.Context getContext(Direction side) {
        return contexts.computeIfAbsent(side, ModuleContext::new);
    }

    public void place(Direction side, TubeModule<?> module) {
        modules.put(side, module);
        markDirty.run();
        BlockPos blockPos = pos.get();
        TubeNetworkHandler.INSTANCE.sendToAllAround(
            new ModuleUpdatedPacket(blockPos, side, module.getType(), module.serialize()),
            world.get(), blockPos, 64
        );
    }

    public TubeModule<?> remove(Direction side) {
        TubeModule<?> module = modules.remove(side);
        if (module != null) {
            BlockPos blockPos = pos.get();
            TubeNetworkHandler.INSTANCE.sendToAllAround(
                new ModuleUpdatedPacket(blockPos, side, null, new CompoundNBT()),
                world.get(), blockPos, 64
            );
        }
        return module;
    }

    public CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();
        modules.forEach((side, module) -> {
            CompoundNBT t = new CompoundNBT();
            t.putString("type", module.getType().getName());
            t.put("data", module.serialize());
            tag.put("module_" + side.ordinal(), t);
        });
        return tag;
    }

    public void deserialize(CompoundNBT tag) {
        modules.clear();
        for (Direction side : Direction.values()) {
            String key = "module_" + side.ordinal();
            if (!tag.contains(key)) continue;
            CompoundNBT t = tag.getCompound(key);
            TubeModule.Type<?, ?> type = ModuleManager.INSTANCE.get(t.getString("type"));
            modules.put(side, type.create(getContext(side), side, t.getCompound("data")));
        }
        refreshModel.run();
    }

    public void onUpdate(Direction side, TubeModule.Type<?, ?> type, CompoundNBT data) {
        if (type != null) {
            modules.put(side, type.create(getContext(side), side, data));
        } else {
            modules.remove(side);
        }
        refreshModel.run();
    }

    private class ModuleContext implements TubeModule.Context {

        private final Direction side;

        private ModuleContext(Direction side) {
            this.side = side;
        }

        @Override
        public boolean isRemote() {
            return world.get().isRemote();
        }

        @Override
        public boolean canOutput(ITubeStack stack) {
            return TubeComponent.output(world.get(), pos.get(), side, stack, true) == 0;
        }

    }

}
