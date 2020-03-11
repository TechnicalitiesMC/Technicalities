package com.technicalitiesmc.base.block;

import com.technicalitiesmc.base.TKBase;
import com.technicalitiesmc.base.container.EjectorContainer;
import com.technicalitiesmc.lib.block.Component;
import com.technicalitiesmc.lib.block.TKBlock;
import com.technicalitiesmc.lib.block.TKBlockData;
import com.technicalitiesmc.lib.block.components.*;
import com.technicalitiesmc.lib.container.TKContainer;
import com.technicalitiesmc.lib.inventory.*;
import com.technicalitiesmc.lib.serial.Serialize;
import com.technicalitiesmc.lib.util.TooltipEnabled;
import com.technicalitiesmc.lib.util.value.Value;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
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
import java.util.Arrays;
import java.util.List;

public class EjectorBlock extends TKBlock.WithData<EjectorBlock.Data> {

    private static final int MAX_STACKS = EjectorContainer.FILTER_SIZE;

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
    private final BlockInventory.Handle filter = inventory.addRegion("filter", EjectorContainer.FILTER_SIZE);
    @Component
    private final ClickGUI gui = new ClickGUI.Container(EjectorContainer.NAME, this::createContainer);

    public EjectorBlock() {
        super(Block.Properties.create(Material.ROCK).hardnessAndResistance(3.5F), Data::new);
    }

    private TKContainer createContainer(IWorld world, BlockPos pos, BlockState state, int id, PlayerInventory playerInv, PlayerEntity player) {
        Data data = getData(world, pos);
        return new EjectorContainer(id, playerInv, filter.at(world, pos), data.filterType, data.whitelistMode, data.blacklistMode);
    }

    private void onTriggered(World world, BlockPos pos, BlockState state) {
        Direction front = direction.get(state), back = front.getOpposite();

        IItemHandler frontInv = InventoryUtils.getNeighborItemHandlerOrDrop(world, pos, front, MAX_STACKS);
        IItemHandler backInv = InventoryUtils.getNeighborItemHandler(world, pos, back);
        if (frontInv == null || backInv == null) {
            playSound(world, pos);
            return;
        }

        ItemSet rejects = new ItemSet();
        ItemFilter[] filters = getFilters(world, pos, rejects);

        Data data = getData(world, pos);
        boolean isWhitelist = data.filterType.get() == FilterType.WHITELIST;
        boolean isExact = isWhitelist && data.whitelistMode.get() == WhitelistMode.EXACT;

        ItemHandlerExtractionQuery extractionQuery = new ItemHandlerExtractionQuery(backInv);
        ItemHandlerInsertionQuery insertionQuery = new ItemHandlerInsertionQuery(frontInv);
        for (ItemFilter filter : filters) {
            rejects.clear();
            ItemStack extracted;
            do {
                ItemHandlerExtractionQuery.Extraction extraction = extractionQuery.extract(filter);
                extracted = extraction.getExtracted();
                if (extracted.isEmpty()) {
                    if (isExact) {
                        playSound(world, pos);
                        return;
                    }
                    break;
                }

                ItemHandlerInsertionQuery.Insertion insertion = insertionQuery.insert(extracted);
                int leftover = insertion.getLeftover().getCount();
                if (leftover > 0 && isExact) {
                    playSound(world, pos);
                    return;
                }
                int inserted = extracted.getCount() - leftover;
                if (inserted == 0) {
                    rejects.add(extracted);
                    continue;
                }
                if (extraction.commitAtMost(inserted)) {
                    insertion.commit();
                    break;
                }
            } while (!extracted.isEmpty());
            if (extracted.isEmpty()) {
                break;
            }
        }
        extractionQuery.commit();
        insertionQuery.commit();
        playSound(world, pos);
    }

    private void playSound(World world, BlockPos pos) {
        world.playSound(null, pos, TKBase.SOUND_SMALL_PISTON, SoundCategory.BLOCKS, 0.25F, 1F);
    }

    private ItemFilter[] getFilters(IBlockReader world, BlockPos pos, ItemSet rejects) {
        Data data = getData(world, pos);
        Inventory filterInv = filter.at(world, pos);
        if (data.filterType.get() == FilterType.WHITELIST) {
            List<ItemFilter> filters = TransposerBlock.getWhitelistFilters(filterInv, data.whitelistMode.get() == WhitelistMode.LENIENT);
            return filters.toArray(new ItemFilter[0]);
        } else {
            ItemFilter filter = TransposerBlock.getBlacklistFilter(filterInv, data.blacklistMode.get() == BlacklistMode.SINGLE, rejects);
            ItemFilter[] filters = new ItemFilter[MAX_STACKS];
            Arrays.fill(filters, filter);
            return filters;
        }
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
            return "container.technicalities.ejector.filter_type." + name().toLowerCase();
        }

    }

    public enum WhitelistMode implements TooltipEnabled.Auto {
        STRICT, LENIENT, EXACT;

        @Override
        public String getTooltipTranslationKey() {
            return "container.technicalities.ejector.whitelist_mode." + name().toLowerCase();
        }

    }

    public enum BlacklistMode implements TooltipEnabled.Auto {
        SINGLE, STACK;

        @Override
        public String getTooltipTranslationKey() {
            return "container.technicalities.ejector.blacklist_mode." + name().toLowerCase();
        }

    }

}
