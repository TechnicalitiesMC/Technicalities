package com.technicalitiesmc.pneumatics.client;

import com.technicalitiesmc.Technicalities;
import com.technicalitiesmc.pneumatics.client.screen.FilterScreen;
import com.technicalitiesmc.pneumatics.container.FilterContainer;
import com.technicalitiesmc.pneumatics.tube.module.ModuleManager;
import com.technicalitiesmc.pneumatics.tube.module.TubeModule;
import com.technicalitiesmc.pneumatics.tube.module.TubeModuleState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelRotation;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Technicalities.MODID, value = Dist.CLIENT)
public class TKPClientInit {

    private static final Map<StateKey, IBakedModel> MODULE_MODELS = new HashMap<>();

    public static void init(final FMLClientSetupEvent event) {
        ScreenManager.registerFactory(FilterContainer.TYPE, FilterScreen::new);
    }

    @SubscribeEvent
    public static void onModelRegister(ModelRegistryEvent event) {
        for (TubeModule.Type<?, ?> type : ModuleManager.INSTANCE.getTypes()) {
            ResourceLocation moduleName = new ResourceLocation(Technicalities.MODID, type.getName());
            for (TubeModuleState<?> state : type.getPossibleStates()) {
                ModelLoader.addSpecialModel(getModuleModelPath(moduleName, state));
            }
        }
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        MODULE_MODELS.clear();
        for (TubeModule.Type<?, ?> type : ModuleManager.INSTANCE.getTypes()) {
            ResourceLocation moduleName = new ResourceLocation(Technicalities.MODID, type.getName());
            for (TubeModuleState<?> state : type.getPossibleStates()) {
                ResourceLocation path = getModuleModelPath(moduleName, state);
                for (Direction side : Direction.values()) {
                    MODULE_MODELS.put(
                        new StateKey(type, state, side),
                        event.getModelLoader().bake(
                            path,
                            getRotation(side)
                        )
                    );
                }
            }
        }
    }

    public static IBakedModel getModel(TubeModule.Type<?, ?> module, TubeModuleState<?> state, Direction side) {
        IBakedModel model = MODULE_MODELS.get(new StateKey(module, state, side));
        if (model == null) return Minecraft.getInstance().getModelManager().getMissingModel();
        return model;
    }

    private static ModelRotation getRotation(Direction side) {
        switch (side) {
            case DOWN:
                return ModelRotation.X90_Y0;
            case UP:
                return ModelRotation.X270_Y0;
            case NORTH:
                return ModelRotation.X0_Y0;
            case SOUTH:
                return ModelRotation.X0_Y180;
            case WEST:
                return ModelRotation.X0_Y270;
            case EAST:
                return ModelRotation.X0_Y90;
        }
        return ModelRotation.X0_Y0;
    }

    private static ResourceLocation getModuleModelPath(ResourceLocation moduleName, TubeModuleState<?> state) {
        return new ResourceLocation(moduleName.getNamespace(), "tube_module/" + moduleName.getPath() + "/" + state.getStateName());
    }

    private static final class StateKey {
        private final TubeModule.Type<?, ?> type;
        private final TubeModuleState<?> state;
        private final Direction side;

        private StateKey(TubeModule.Type<?, ?> type, TubeModuleState<?> state, Direction side) {
            this.type = type;
            this.state = state;
            this.side = side;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StateKey stateKey = (StateKey) o;
            return stateKey.type == type && stateKey.state == state && stateKey.side == side;
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                .append(type)
                .append(state)
                .append(side)
                .toHashCode();
        }
    }

}
