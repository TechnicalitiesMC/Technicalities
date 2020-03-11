package com.technicalitiesmc.base;

import com.technicalitiesmc.Technicalities;
import com.technicalitiesmc.lib.util.MultiRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class TKResources {

    public static ItemGroup RESOURCES_TAB = new ItemGroup(Technicalities.MODID + ".materials") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(COPPER_INGOT);
        }
    };

    public static final Item COPPER_INGOT = new Item(new Item.Properties().group(RESOURCES_TAB));
    public static final Item ZINC_INGOT = new Item(new Item.Properties().group(RESOURCES_TAB));
    public static final Item BRASS_INGOT = new Item(new Item.Properties().group(RESOURCES_TAB));

    public static final Item IRON_DUST = new Item(new Item.Properties().group(RESOURCES_TAB));
    public static final Item GOLD_DUST = new Item(new Item.Properties().group(RESOURCES_TAB));
    public static final Item COPPER_DUST = new Item(new Item.Properties().group(RESOURCES_TAB));
    public static final Item ZINC_DUST = new Item(new Item.Properties().group(RESOURCES_TAB));
    public static final Item BRASS_DUST = new Item(new Item.Properties().group(RESOURCES_TAB));

    public static void register(MultiRegistry registry) {
        registry.register("copper_ingot", COPPER_INGOT);
        registry.register("zinc_ingot", ZINC_INGOT);
        registry.register("brass_ingot", BRASS_INGOT);

        registry.register("iron_dust", IRON_DUST);
        registry.register("gold_dust", GOLD_DUST);
        registry.register("copper_dust", COPPER_DUST);
        registry.register("zinc_dust", ZINC_DUST);
        registry.register("brass_dust", BRASS_DUST);
    }

}
