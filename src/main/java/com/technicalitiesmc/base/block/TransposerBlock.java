package com.technicalitiesmc.base.block;

import com.technicalitiesmc.base.TKBase;
import com.technicalitiesmc.base.container.TransposerContainer;
import com.technicalitiesmc.lib.block.Component;
import com.technicalitiesmc.lib.block.TKBlock;
import com.technicalitiesmc.lib.block.TKBlockData;
import com.technicalitiesmc.lib.block.components.*;
import com.technicalitiesmc.lib.container.TKContainer;
import com.technicalitiesmc.lib.inventory.Inventory;
import com.technicalitiesmc.lib.inventory.InventoryUtils;
import com.technicalitiesmc.lib.inventory.ItemFilter;
import com.technicalitiesmc.lib.inventory.ItemSet;
import com.technicalitiesmc.lib.serial.Serialize;
import com.technicalitiesmc.lib.util.TooltipEnabled;
import com.technicalitiesmc.lib.util.value.Value;
import com.technicalitiesmc.pneumatics.block.FilterBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TransposerBlock extends TKBlock.WithData<TransposerBlock.Data> {

    @CapabilityInject(IItemHandler.class)
    private static Capability<IItemHandler> ITEM_HANDLER;

    @Component
    private final BlockDirection direction = new BlockDirection();
    @Component
    private final DirectionalPlacement placement = new DirectionalPlacement(true);
    @Component
    private final RedstoneTrigger trigger = new RedstoneTrigger(this::onTriggered);
    @Component
    private final BlockInventory inventory = new BlockInventory();
    private final BlockInventory.Handle filter = inventory.addRegion("filter", TransposerContainer.FILTER_SIZE);
    @Component
    private final ClickGUI gui = new ClickGUI.Container(TransposerContainer.NAME, this::createContainer);

    public TransposerBlock() {
        super(Block.Properties.create(Material.ROCK).hardnessAndResistance(3.5F), Data::new);
    }

    private TKContainer createContainer(IWorld world, BlockPos pos, BlockState state, int id, PlayerInventory playerInv, PlayerEntity entity) {
        Data data = getData(world, pos);
        return new TransposerContainer(id, playerInv, filter.at(world, pos), data.filterType, data.whitelistMode, data.blacklistMode);
    }

    private void onTriggered(World world, BlockPos pos, BlockState state) {
        Direction front = direction.get(state), back = front.getOpposite();
        ItemFilter filter = getFilter(world, pos);

        IItemHandler src = InventoryUtils.getNeighborItemHandlerOrPickUp(world, pos, back, new AxisAlignedBB(pos.offset(back)));
        IItemHandler dst = InventoryUtils.getNeighborItemHandlerOrDrop(world, pos, front, 1);
        if (src != null && dst != null) {
            InventoryUtils.transferStack(src, dst, filter);
        }

        world.playSound(null, pos, TKBase.SOUND_SMALL_PISTON, SoundCategory.BLOCKS, 0.25F, 1F);
    }

    private ItemFilter getFilter(IBlockReader world, BlockPos pos) {
        Data data = getData(world, pos);
        Inventory filterInv = filter.at(world, pos);
        if (data.filterType.get() == FilterType.WHITELIST) {
            List<ItemFilter> filters = getWhitelistFilters(filterInv, data.whitelistMode.get() == WhitelistMode.LENIENT);
            return ItemFilter.combining(filters);
        } else {
            return getBlacklistFilter(filterInv, data.blacklistMode.get() == BlacklistMode.SINGLE, new ItemSet());
        }
    }

    static List<ItemFilter> getWhitelistFilters(Inventory filterInv, boolean isLenient) {
        List<ItemFilter> filters = new ArrayList<>();
        for (Inventory.Slot slot : filterInv) {
            ItemStack filterStack = slot.get();
            if (filterStack.isEmpty()) continue;
            ItemFilter.Prototype filterPrototype =
                isLenient ?
                    ItemFilter.atMost(filterStack.getCount()) :
                    ItemFilter.exactly(filterStack.getCount());
            filters.add(filterPrototype.matching(s -> FilterBlock.matchesFilter(s, filterStack)));
        }
        return filters;
    }

    static ItemFilter getBlacklistFilter(Inventory filterInv, boolean isSingleMode, ItemSet rejects) {
        ItemFilter.Prototype filterPrototype =
            isSingleMode ?
                ItemFilter.exactly(1) :
                ItemFilter.atMost(64);
        return filterPrototype.matching(s -> {
            for (Inventory.Slot slot : filterInv) {
                ItemStack filterStack = slot.get();
                if (filterStack.isEmpty()) continue;
                if (FilterBlock.matchesFilter(s, filterStack)) {
                    return false;
                }
            }
            return !rejects.contains(s);
        });
    }

    @Override
    protected boolean isNormalCube(IBlockReader world, BlockPos pos, BlockState state) {
        return false;
    }

    @Nonnull
    @Override
    protected <T> LazyOptional<T> getCapability(IWorld world, BlockPos pos, @Nullable Direction face, Data data, Capability<T> capability) {
        BlockState state = world.getBlockState(pos);
        Direction front = direction.get(state);
        if (face == front) {
            return ITEM_HANDLER.orEmpty(capability, LazyOptional.of(EmptyHandler::new));
        }
        return LazyOptional.empty();
    }

    public static class Data extends TKBlockData {
        @Serialize
        private final Value<FilterType> filterType = new Value<>(FilterType.BLACKLIST);
        @Serialize
        private final Value<WhitelistMode> whitelistMode = new Value<>(WhitelistMode.STRICT);
        @Serialize
        private final Value<BlacklistMode> blacklistMode = new Value<>(BlacklistMode.STACK);
    }

    public enum FilterType implements TooltipEnabled.Auto {
        WHITELIST, BLACKLIST;

        @Override
        public String getTooltipTranslationKey() {
            return "container.technicalities.transposer.filter_type." + name().toLowerCase();
        }

    }

    public enum WhitelistMode implements TooltipEnabled.Auto {
        STRICT, LENIENT;

        @Override
        public String getTooltipTranslationKey() {
            return "container.technicalities.transposer.whitelist_mode." + name().toLowerCase();
        }

    }

    public enum BlacklistMode implements TooltipEnabled.Auto {
        SINGLE, STACK;

        @Override
        public String getTooltipTranslationKey() {
            return "container.technicalities.transposer.blacklist_mode." + name().toLowerCase();
        }

    }

}
