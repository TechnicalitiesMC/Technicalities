package com.technicalitiesmc.pneumatics.block;

import com.technicalitiesmc.lib.util.ColoredStack;
import com.technicalitiesmc.base.TKBase;
import com.technicalitiesmc.base.item.ItemTagItem;
import com.technicalitiesmc.lib.block.Component;
import com.technicalitiesmc.lib.block.TKBlock;
import com.technicalitiesmc.lib.block.TKBlockData;
import com.technicalitiesmc.lib.block.components.BlockDirection;
import com.technicalitiesmc.lib.block.components.BlockInventory;
import com.technicalitiesmc.lib.block.components.ClickGUI;
import com.technicalitiesmc.lib.block.components.DirectionalPlacement;
import com.technicalitiesmc.lib.container.TKContainer;
import com.technicalitiesmc.lib.inventory.Inventory;
import com.technicalitiesmc.lib.inventory.ItemFilter;
import com.technicalitiesmc.lib.serial.Serialize;
import com.technicalitiesmc.lib.util.PaintAction;
import com.technicalitiesmc.lib.util.TooltipEnabled;
import com.technicalitiesmc.lib.util.value.Value;
import com.technicalitiesmc.pneumatics.block.components.TubeMachineComponent;
import com.technicalitiesmc.pneumatics.container.FilterContainer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.tags.Tag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class FilterBlock extends TKBlock.WithData<FilterBlock.Data> {

    @CapabilityInject(IItemHandler.class)
    private static Capability<IItemHandler> ITEM_HANDLER;

    private static final EnumProperty<PaintAction> PAINT_ACTION = EnumProperty.create("paint", PaintAction.class);

    @Component
    private final BlockDirection direction = new BlockDirection();
    @Component
    private final DirectionalPlacement placement = new DirectionalPlacement(true);
    @Component
    private final BlockInventory inventory = new BlockInventory();
    private final BlockInventory.Handle filter = inventory.addRegion("filter", FilterContainer.FILTER_SIZE);
    @Component
    private final ClickGUI gui = new ClickGUI.Container(FilterContainer.NAME, this::createContainer);
    @Component
    private final TubeMachineComponent tubeMachine = new TubeMachineComponent(this::process);

    public FilterBlock() {
        super(Block.Properties.create(Material.ROCK).hardnessAndResistance(3.5F), Data::new);
    }

    private TKContainer createContainer(IWorld world, BlockPos pos, BlockState state, int id, PlayerInventory playerInv, PlayerEntity player) {
        Data data = getData(world, pos);
        if (data.paintAction.get() == null) {
            data.paintAction.set(state.get(PAINT_ACTION));
        }
        return new FilterContainer(id, playerInv, filter.at(world, pos), data.filterType, data.paintAction, () -> {
            PaintAction currentPaintAction = state.get(PAINT_ACTION);
            PaintAction newPaintAction = data.paintAction.get();
            if (newPaintAction != currentPaintAction) {
                world.getWorld().setBlockState(pos, state.with(PAINT_ACTION, newPaintAction));
            }
        });
    }

    private TubeMachineComponent.ProcessingResult process(IWorld world, BlockPos pos, ColoredStack stack, boolean simulate) {
        ItemFilter filter = getFilter(world, pos);
        if (!filter.test(stack.getStack())) {
            return new TubeMachineComponent.RejectStack(stack.getStack().getCount());
        }

//        Data data = getData(world, pos);
        PaintAction paintAction = world.getBlockState(pos).get(PAINT_ACTION);//data.paintAction.get();
        if (paintAction == PaintAction.KEEP_PAINT) {
            return new TubeMachineComponent.PassthroughStack(stack);
        } else if (paintAction == PaintAction.REMOVE_PAINT) {
            return new TubeMachineComponent.PassthroughStack(new ColoredStack(stack.getStack(), null));
        } else {
            return new TubeMachineComponent.PassthroughStack(new ColoredStack(stack.getStack(), paintAction.getColor()));
        }
    }

    private ItemFilter getFilter(IBlockReader world, BlockPos pos) {
        Data data = getData(world, pos);
        boolean isWhitelist = data.filterType.get() == FilterType.WHITELIST;
        Inventory filterStorage = filter.at(world, pos);
        return ItemFilter.atMost(64).matching(stack -> {
            for (Inventory.Slot slot : filterStorage) {
                if (matchesFilter(stack, slot.get())) {
                    return isWhitelist;
                }
            }
            return !isWhitelist;
        });
    }

    @Override
    protected boolean canRenderInLayer(RenderType layer) {
        return layer == RenderType.getCutout();
    }

    @Override
    protected void buildState(BlockStateBuilder state) {
        super.buildState(state);
        state.add(PAINT_ACTION).withDefault(PaintAction.KEEP_PAINT);
    }

    public static class Data extends TKBlockData {
        @Serialize
        private final Value<FilterType> filterType = new Value<>(FilterType.WHITELIST);
        // @Serialize - No need to do this now that the value is in the state
        // Keeping for cross-client syncing of GUI states
        private final Value<PaintAction> paintAction = new Value<>(null);
    }

    public enum FilterType implements TooltipEnabled.Auto {
        WHITELIST, BLACKLIST;

        @Override
        public String getTooltipTranslationKey() {
            return "container.technicalities.filter.filter_type." + name().toLowerCase();
        }

    }

    public static boolean matchesFilter(ItemStack stack, ItemStack filterStack) {
        if (filterStack.getItem() == TKBase.ITEM_TAG) {
            Tag<Item> tag = ItemTagItem.getTag(filterStack);
            return tag != null && tag.contains(stack.getItem());
        }
        return ItemHandlerHelper.canItemStacksStack(stack, filterStack);
    }

}
