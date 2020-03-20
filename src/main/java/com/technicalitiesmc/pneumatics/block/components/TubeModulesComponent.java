package com.technicalitiesmc.pneumatics.block.components;

import com.technicalitiesmc.lib.block.Component;
import com.technicalitiesmc.lib.block.TKBlockComponent;
import com.technicalitiesmc.lib.block.TKBlockData;
import com.technicalitiesmc.lib.block.TKBlockDataHost;
import com.technicalitiesmc.lib.block.components.BlockConnections;
import com.technicalitiesmc.lib.client.BindModelTransformer;
import com.technicalitiesmc.pneumatics.client.TubeModuleModelTransformer;
import com.technicalitiesmc.pneumatics.tube.module.TubeModule;
import com.technicalitiesmc.pneumatics.tube.module.TubeModuleContainer;
import com.technicalitiesmc.pneumatics.tube.module.TubeModuleProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Component
@BindModelTransformer(TubeModuleModelTransformer.class)
public class TubeModulesComponent extends TKBlockComponent.WithData<TubeModulesComponent.Data> {

    public static final ModelProperty<List<TubeModule<?>>> MODEL_PROPERTY = new ModelProperty<>();

    @CapabilityInject(TubeModuleContainer.class)
    private static Capability<TubeModuleContainer> TUBE_MODULE_CONTAINER;

    @CapabilityInject(TubeModuleProvider.class)
    private static Capability<TubeModuleProvider> TUBE_MODULE_PROVIDER;

    @Component
    private BlockConnections connections;

    @Nullable
    private final Set<TubeModule.Type<?, ?>> supportedModules;

    public TubeModulesComponent() {
        this(null);
    }

    public TubeModulesComponent(@Nullable Set<TubeModule.Type<?, ?>> supportedModules) {
        super(Data::new);
        this.supportedModules = supportedModules;
    }

    public TubeModuleContainer getModuleContainer(IBlockReader world, BlockPos pos) {
        Data data = getData(world, pos);
        if (data == null) return null;
        return data.container;
    }

    @Override
    protected boolean canRenderInLayer(RenderType layer) {
        return true;
    }

    @Override
    protected boolean requiresRayTraceReconstruction() {
        return true;
    }

    @Override
    protected ActionResultType onRightClicked(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        Direction side = connections.getSideHit(hit);
        if (side != null) {
            Data data = Objects.requireNonNull(getData(world, pos));
            TubeModuleContainer container = data.container;
            ItemStack item = player.getHeldItem(hand);

            if (item.isEmpty()) {
                if (player.isCrouching()) {
                    if (container.get(side) == null) return ActionResultType.PASS;
                    if (world.isRemote()) return ActionResultType.SUCCESS;

                    TubeModule<?> module = container.remove(side);
                    if (!player.isCreative()) {
                        ItemStack drops = module.getItem();
                        if (!drops.isEmpty()) {
                            if (!player.inventory.addItemStackToInventory(drops)) {
                                Block.spawnAsEntity(world, pos, drops);
                            }
                        }
                    }
                    world.setBlockState(pos, state, 1 | 3 | 16 | 32);
                    return ActionResultType.SUCCESS;
                } else {
//                    ModuleContainer.ModuleInfo module = container.get(side);
//                    if (module != null) {
//                        return module.onRightClicked(player, hand, hit);
//                    }
                }
            } else {
                if (container.get(side) != null) return ActionResultType.PASS;

                LazyOptional<TubeModuleProvider> cap = item.getCapability(TUBE_MODULE_PROVIDER);
                if (!cap.isPresent()) return ActionResultType.PASS;

                TubeModuleProvider provider = cap.orElse(null);
                TubeModule<?> module = provider.create(container.getContext(side), side);
                if (supportedModules != null && !supportedModules.contains(module.getType())) {
                    return ActionResultType.PASS;
                }

                if (world.isRemote()) return ActionResultType.SUCCESS;

                container.place(side, module);
                if (!player.isCreative()) item.shrink(1);
                world.setBlockState(pos, state, 1 | 3 | 16 | 32);
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.PASS;
    }

    @Override
    public void onReplaced(World world, BlockPos pos, BlockState state, BlockState newState, boolean isMoving) {
        boolean removeAll = state.getBlock() != newState.getBlock();
        TubeModuleContainer container = Objects.requireNonNull(getData(world, pos)).container;
        for (Direction side : Direction.values()) {
            if (removeAll || !connections.isConnected(newState, side)) {
                TubeModule<?> module = container.remove(side);
                if (module != null) {
                    ItemStack item = module.getItem();
                    if (!item.isEmpty()) {
                        Block.spawnAsEntity(world, pos, item);
                    }
                }
            }
        }
    }

    @Override
    protected NonNullList<ItemStack> getAdditionalDrops(World world, BlockPos pos, BlockState state) {
        NonNullList<ItemStack> drops = NonNullList.create();
        Data data = Objects.requireNonNull(getData(world, pos));
        for (Direction side : Direction.values()) {
            TubeModule<?> module = data.container.get(side);
            if (module != null) {
                ItemStack item = module.getItem();
                if (!item.isEmpty()) {
                    drops.add(item);
                }
            }
        }
        return drops;
    }

    @Nonnull
    @Override
    protected <T> LazyOptional<T> getCapability(IWorld world, BlockPos pos, @Nullable Direction face, Data data, Capability<T> capability) {
        if (capability == TUBE_MODULE_CONTAINER) {
            return TUBE_MODULE_CONTAINER.orEmpty(capability, LazyOptional.of(() -> data.container));
        }
        return super.getCapability(world, pos, face, data, capability);
    }

    @Override
    public void appendModelData(IWorld world, BlockPos pos, Data data, ModelDataMap.Builder builder) {
        builder.withInitial(MODEL_PROPERTY, new ArrayList<>(data.container.getAll()));
    }

    public static class Data extends TKBlockData {

        private final TubeModuleContainer container;

        private Data(TKBlockDataHost host) {
            container = new TubeModuleContainer(host::getWorld, host::getPos, host::markDirty, () -> {
                host.markModelDataDirty();
                World world = host.getWorld();
                if (world != null && world.isRemote()) {
                    BlockPos pos = host.getPos();
                    BlockState state = world.getBlockState(pos);
                    world.notifyBlockUpdate(pos, state, state, 2);
                }
            });
        }

        @Override
        protected CompoundNBT serialize() {
            CompoundNBT tag = super.serialize();
            tag.put("modules", container.serialize());
            return tag;
        }

        @Override
        protected void deserialize(CompoundNBT tag) {
            super.deserialize(tag);
            container.deserialize(tag.getCompound("modules"));
        }

        @Override
        protected CompoundNBT serializeSync() {
            CompoundNBT tag = super.serializeSync();
            tag.put("modules", container.serialize());
            return tag;
        }

        @Override
        protected void deserializeSync(CompoundNBT tag) {
            super.deserializeSync(tag);
            container.deserialize(tag.getCompound("modules"));
        }

    }

}
