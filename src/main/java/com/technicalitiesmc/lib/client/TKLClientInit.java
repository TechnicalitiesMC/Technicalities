package com.technicalitiesmc.lib.client;

import com.technicalitiesmc.Technicalities;
import com.technicalitiesmc.lib.block.TKBlockAdapter;
import com.technicalitiesmc.lib.block.TKBlockComponent;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Technicalities.MODID, value = Dist.CLIENT)
public class TKLClientInit {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onModelBake(ModelBakeEvent event) {
        Map<ResourceLocation, Function<IBakedModel, IBakedModel>> transformers = new HashMap<>();

        IForgeRegistry<Block> registry = GameRegistry.findRegistry(Block.class);
        for (Block block : registry) {
            if (block instanceof TKBlockAdapter) {
                ResourceLocation name = block.getRegistryName();
                for (TKBlockComponent component : ((TKBlockAdapter) block).getComponents()) {
                    UnaryOperator<IBakedModel> transformer = getTransformer(component);
                    if (transformer != null) {
                        transformers.compute(name, (n, p) -> p == null ? transformer : p.andThen(transformer));
                    }
                }
                RenderTypeLookup.setRenderLayer(block, ((TKBlockAdapter) block)::canRenderInLayer);
                TileEntityType<TKBlockAdapter.TileEntityAdapter> tileEntityType = ((TKBlockAdapter) block).getTileEntityType();
                if (tileEntityType != null) {
                    ClientRegistry.bindTileEntityRenderer(tileEntityType, TKSpecialRenderer::new);
                }
            }
        }

        event.getModelRegistry().replaceAll((name, model) -> {
            ResourceLocation realName = name instanceof ModelResourceLocation ? new ResourceLocation(name.getNamespace(), name.getPath()) : name;
            Function<IBakedModel, IBakedModel> transformer = transformers.get(realName);
            if (transformer == null) return model;
            return transformer.apply(model);
        });
    }

    private static UnaryOperator<IBakedModel> getTransformer(TKBlockComponent component) {
        BindModelTransformer annotation = component.getClass().getAnnotation(BindModelTransformer.class);
        if (annotation == null) return null;
        return model -> {
            try {
                Constructor<? extends TKModelTransformer> constructor = annotation.value().getConstructor(IBakedModel.class);
                return constructor.newInstance(model);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
                return model;
            }
        };
    }

}
