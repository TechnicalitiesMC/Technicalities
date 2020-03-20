package com.technicalitiesmc.base.item;

import com.technicalitiesmc.Technicalities;
import com.technicalitiesmc.base.TKBase;
import com.technicalitiesmc.lib.item.TKItem;
import com.technicalitiesmc.lib.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.RepairContainer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = Technicalities.MODID)
public class ItemTagItem extends TKItem {

    private boolean wasShowingDetailedTooltip = false;
    private long currentSeed = 0;

    public ItemTagItem() {
        super(new Properties().group(TKBase.TAB_UTILS).maxStackSize(1));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        Tag<Item> tag = getTag(stack);
        if (tag == null) {
            tooltip.addAll(TextUtils.getLocalized("item.technicalities.item_tag.tooltip"));
            return;
        }
        tooltip.add(new StringTextComponent("\u00A7aMatches: " + tag.getId()));

        long windowHandle = Minecraft.getInstance().getMainWindow().getHandle();
        boolean detailed = InputMappings.isKeyDown(windowHandle, 340) || InputMappings.isKeyDown(windowHandle, 344);
        if (!detailed) {
            tooltip.addAll(TextUtils.getLocalized("item.technicalities.item_tag.show_examples", "Shift"));
            wasShowingDetailedTooltip = false;
            return;
        }

        tooltip.addAll(TextUtils.getLocalized("item.technicalities.item_tag.examples"));

        if (!wasShowingDetailedTooltip) {
            currentSeed = new Random(currentSeed).nextLong();
        }
        Random rnd = new Random(currentSeed);

        List<Item> tagItems = new ArrayList<>(tag.getAllElements());
        Collections.shuffle(tagItems, rnd);
        tagItems
            .stream()
            .map(Item::getDefaultInstance)
            .filter(((Predicate<ItemStack>) ItemStack::isEmpty).negate())
            .map(s -> {
                try {
                    TextComponent tc = new StringTextComponent("\u00A77 - ");
                    tc.appendSibling(s.getDisplayName());
                    return tc;
                } catch (Exception ignored) {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .limit(5)
            .forEach(tooltip::add);

        tooltip.addAll(TextUtils.getLocalized("item.technicalities.item_tag.examples_release", "Shift"));

        wasShowingDetailedTooltip = true;
    }

    private static ResourceLocation tryParseResourceName(String str) {
        try {
            return new ResourceLocation(str);
        } catch (Exception ex) {
            return null;
        }
    }

    public static Tag<Item> getTag(ItemStack stack) {
        if (stack.isEmpty()) return null;
        if (!(stack.getItem() instanceof ItemTagItem)) return null;
        CompoundNBT tag = stack.getTag();
        if (tag == null || !tag.contains("tag")) return null;
        ResourceLocation tagName = tryParseResourceName(tag.getString("tag"));
        if (tagName == null) return null;
        return ItemTags.getCollection().get(tagName);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTooltipRender(ItemTooltipEvent event) {
        PlayerEntity player = event.getPlayer();
        if (player == null) return;
        Container container = player.openContainer;
        if (!(container instanceof RepairContainer)) return;
        RepairContainer anvilContainer = ((RepairContainer) container);
        ItemStack inputStack = anvilContainer.inventorySlots.get(0).getStack();
        if (inputStack.isEmpty() || !(inputStack.getItem() instanceof ItemTagItem)) return;
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty() || stack.getItem() instanceof ItemTagItem) return;

        List<ITextComponent> tooltip = event.getToolTip();
        ITextComponent first = tooltip.get(0);
        tooltip.clear();
        tooltip.add(first);

        Set<ResourceLocation> tags = stack.getItem().getTags();
        if (tags.isEmpty()) return;

        tooltip.add(new TranslationTextComponent("item.technicalities.item_tag.tags"));
        for (ResourceLocation tag : tags) {
            tooltip.add(new StringTextComponent("\u00A77 - " + tag));
        }
    }


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        String name = event.getName();

        if (left.isEmpty() || !(left.getItem() instanceof ItemTagItem)) return;
        if (right.isEmpty()) {
            event.setOutput(ItemStack.EMPTY);
            return;
        }

        ResourceLocation tagLocation;
        try {
            tagLocation = new ResourceLocation(name);
        } catch (Exception ex) {
            event.setOutput(ItemStack.EMPTY);
            return;
        }

        if (!right.getItem().getTags().contains(tagLocation)) {
            event.setOutput(ItemStack.EMPTY);
            return;
        }

        ItemStack output = left.copy();
        CompoundNBT tag = output.getOrCreateTag();
        tag.putString("tag", name);
        event.setOutput(output);
        event.setCost(1);
        event.setMaterialCost(1);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onAnvilRepair(AnvilRepairEvent event) {

    }

}
