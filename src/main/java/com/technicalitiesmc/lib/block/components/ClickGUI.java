package com.technicalitiesmc.lib.block.components;

import com.technicalitiesmc.lib.block.Component;
import com.technicalitiesmc.lib.block.TKBlockComponent;
import com.technicalitiesmc.lib.container.TKContainer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

@Component
public abstract class ClickGUI extends TKBlockComponent {

    public static class Container extends ClickGUI {

        private final ITextComponent name;
        private final ContainerProvider containerProvider;

        public Container(ITextComponent name, ContainerProvider containerProvider) {
            this.name = name;
            this.containerProvider = containerProvider;
        }

        public Container(ITextComponent name, TKContainerProvider containerProvider) {
            this.name = name;
            this.containerProvider = (world, pos, state, id, playerInventory, player) -> containerProvider.create(world, pos, state, id, playerInventory, player).asVanillaContainer();
        }

        @Override
        protected ActionResultType onRightClicked(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
            player.openContainer(new SimpleNamedContainerProvider(
                (id, inv, entity) -> containerProvider.create(world, pos, state, id, inv, entity),
                name
            ));
            return ActionResultType.SUCCESS;
        }

        public interface ContainerProvider {
            @Nonnull
            net.minecraft.inventory.container.Container create(IWorld world, BlockPos pos, BlockState state, int id, PlayerInventory playerInventory, PlayerEntity player);
        }

        public interface TKContainerProvider {
            @Nonnull
            TKContainer create(IWorld world, BlockPos pos, BlockState state, int id, PlayerInventory playerInventory, PlayerEntity player);
        }

    }

}
