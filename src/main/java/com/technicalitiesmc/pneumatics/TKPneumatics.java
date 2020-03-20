package com.technicalitiesmc.pneumatics;

import com.technicalitiesmc.Technicalities;
import com.technicalitiesmc.api.tube.ITubeStackConsumer;
import com.technicalitiesmc.lib.block.components.DisabledBlockConnections;
import com.technicalitiesmc.lib.util.CapabilityUtils;
import com.technicalitiesmc.lib.util.CollectionUtils;
import com.technicalitiesmc.lib.util.MultiRegistry;
import com.technicalitiesmc.pneumatics.block.*;
import com.technicalitiesmc.pneumatics.container.FilterContainer;
import com.technicalitiesmc.pneumatics.item.TubeModuleItem;
import com.technicalitiesmc.pneumatics.network.TubeNetworkHandler;
import com.technicalitiesmc.pneumatics.tube.Tube;
import com.technicalitiesmc.pneumatics.tube.TubeManager;
import com.technicalitiesmc.pneumatics.tube.module.TubeModuleContainer;
import com.technicalitiesmc.pneumatics.tube.module.TubeModuleProvider;
import com.technicalitiesmc.pneumatics.tube.modules.*;
import net.minecraft.block.Block;
import net.minecraft.item.*;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.EnumMap;

public class TKPneumatics {

    public static ItemGroup CREATIVE_TAB = new ItemGroup(Technicalities.MODID + ".pneumatics") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(PNEUMATIC_TUBE);
        }
    };

    public static final Block PNEUMATIC_TUBE = new PneumaticTubeBlock().asMCBlock();
    public static final Block DETECTOR_TUBE = new DetectorTubeBlock().asMCBlock();
    public static final Block ROUND_ROBIN_TUBE = new RoundRobinTubeBlock().asMCBlock();
    public static final Block MANAGED_TUBE = new ManagedTubeBlock().asMCBlock();

    public static final Block FILTER = new FilterBlock().asMCBlock();

    public static final Item TM_GLASS_LENS = itemFor(GlassLensModule::new);
    public static final EnumMap<DyeColor, Item> TM_COLOR_FILTER = CollectionUtils.newFilledEnumMap(
        DyeColor.class,
        color -> itemFor((context, side) -> new ColorFilterModule(context, side, color))
    );
    public static final Item TM_SLIMY_MEMBRANE = itemFor(SlimyMembraneModule::new);
    public static final Item TM_ONE_WAY_VALVE = itemFor(OneWayValveModule::new);
    public static final Item TM_INSERTER = itemFor(InserterModule::new);

    public static void init(final FMLCommonSetupEvent event) {
        com.technicalitiesmc.pneumatics.network.TubeNetworkHandler.initialize();
        TubeNetworkHandler.initialize();
        registerCapabilities();
    }

    public static void register(MultiRegistry registry) {
        registry.register(
            "pneumatic_tube",
            PNEUMATIC_TUBE, itemFor(PNEUMATIC_TUBE)
        );
        registry.register(
            "detector_tube",
            DETECTOR_TUBE, itemFor(DETECTOR_TUBE)
        );
        registry.register(
            "round_robin_tube",
            ROUND_ROBIN_TUBE, itemFor(ROUND_ROBIN_TUBE)
        );
        registry.register(
            "managed_tube",
            MANAGED_TUBE, itemFor(MANAGED_TUBE)
        );

        registry.register(
            "filter",
            FILTER, itemFor(FILTER),
            FilterContainer.TYPE
        );

        registry.register("tm_glass_lens", TM_GLASS_LENS);
        for (DyeColor color : DyeColor.values()) {
            registry.register("tm_color_filter_" + color.getName(), TM_COLOR_FILTER.get(color));
        }
        registry.register("tm_slimy_membrane", TM_SLIMY_MEMBRANE);
        registry.register("tm_one_way_valve", TM_ONE_WAY_VALVE);
        registry.register("tm_inserter", TM_INSERTER);
    }

    private static BlockItem itemFor(Block block) {
        return new BlockItem(block, new Item.Properties().group(CREATIVE_TAB));
    }

    private static TubeModuleItem itemFor(TubeModuleProvider provider) {
        return new TubeModuleItem(provider, new Item.Properties().group(CREATIVE_TAB));
    }

    private static void registerCapabilities() {
        CapabilityUtils.register(ITubeStackConsumer.class);
        CapabilityUtils.register(TubeManager.class);
        CapabilityUtils.register(Tube.class);
        CapabilityUtils.register(TubeModuleContainer.class);
        CapabilityUtils.register(TubeModuleProvider.class);
        CapabilityUtils.register(DisabledBlockConnections.Data.class);
    }

}
