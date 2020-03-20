package com.technicalitiesmc.base;

import com.technicalitiesmc.Technicalities;
import com.technicalitiesmc.base.block.*;
import com.technicalitiesmc.base.block.components.Labelable;
import com.technicalitiesmc.base.container.*;
import com.technicalitiesmc.base.item.ItemTagItem;
import com.technicalitiesmc.base.network.TKBNetworkHandler;
import com.technicalitiesmc.lib.item.TKBlockItem;
import com.technicalitiesmc.lib.item.TKItem;
import com.technicalitiesmc.lib.util.CapabilityUtils;
import com.technicalitiesmc.lib.util.MultiRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class TKBase {

    public static ItemGroup TAB_UTILS = new ItemGroup(Technicalities.MODID + ".utils") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(WORKBENCH);
        }
    };

    public static final Block WORKBENCH = new WorkbenchBlock().asMCBlock();
    public static final Block CRAFTING_SLAB = new CraftingSlabBlock().asMCBlock();
    public static final Block FUNNEL = new FunnelBlock().asMCBlock();
    public static final Block CRATE = new CrateBlock().asMCBlock();
    public static final Block BARREL = new BarrelBlock().asMCBlock();

    public static final Block TRANSPOSER = new TransposerBlock().asMCBlock();
    public static final Block EJECTOR = new EjectorBlock().asMCBlock();
    public static final Block BUFFER = new BufferBlock().asMCBlock();
    public static final Block QUEUE = new QueueBlock().asMCBlock();
    public static final Block REGULATOR = new RegulatorBlock().asMCBlock();

    public static final Item ITEM_TAG = new ItemTagItem();
    public static final Item LABEL = item();

    public static final SoundEvent SOUND_SMALL_PISTON = soundFor("small_piston");

    public static void init(final FMLCommonSetupEvent event) {
        TKBNetworkHandler.initialize();

        CapabilityUtils.register(Labelable.Data.class);
    }

    public static void register(MultiRegistry registry) {
        registry.register(
            "workbench",
            WORKBENCH, itemFor(WORKBENCH),
            WorkbenchContainer.TYPE
        );
        registry.register(
            "crafting_slab",
            CRAFTING_SLAB, itemFor(CRAFTING_SLAB)
        );
        registry.register(
            "funnel",
            FUNNEL, itemFor(FUNNEL)
        );
        registry.register(
            "crate",
            CRATE, itemFor(CRATE),
            CrateContainer.TYPE
        );
        registry.register(
            "barrel",
            BARREL, itemFor(BARREL)
        );

        registry.register(
            "transposer",
            TRANSPOSER, itemFor(TRANSPOSER),
            TransposerContainer.TYPE
        );
        registry.register(
            "ejector",
            EJECTOR, itemFor(EJECTOR),
            EjectorContainer.TYPE
        );
        registry.register(
            "buffer",
            BUFFER, itemFor(BUFFER),
            BufferContainer.TYPE
        );
        registry.register(
            "queue",
            QUEUE, itemFor(QUEUE),
            QueueContainer.TYPE
        );
        registry.register(
            "regulator",
            REGULATOR, itemFor(REGULATOR),
            RegulatorContainer.TYPE
        );

        registry.register("item_tag", ITEM_TAG);
        registry.register("label", LABEL);

        registry.register("small_piston", SOUND_SMALL_PISTON);
    }

    private static Item item() {
        return item(new Item.Properties());
    }

    private static Item item(Item.Properties properties) {
        return new TKItem(properties.group(TAB_UTILS));
    }

    private static BlockItem itemFor(Block block) {
        return new TKBlockItem(block, new Item.Properties().group(TAB_UTILS));
    }

    private static SoundEvent soundFor(String name) {
        return new SoundEvent(new ResourceLocation(Technicalities.MODID, name));
    }

    public static void playSmallPistonSound(World world, BlockPos pos) {
        world.playSound(null, pos, TKBase.SOUND_SMALL_PISTON, SoundCategory.BLOCKS, 0.2F, 1F);
    }
}
