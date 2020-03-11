package com.technicalitiesmc.pneumatics.client;

import com.technicalitiesmc.lib.client.TKModelTransformer;
import com.technicalitiesmc.pneumatics.block.components.TubeModulesComponent;
import com.technicalitiesmc.pneumatics.tube.module.TubeModule;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.Direction;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TubeModuleModelTransformer extends TKModelTransformer {

    public TubeModuleModelTransformer(IBakedModel parent) {
        super(parent);
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
        RenderType renderLayer = MinecraftForgeClient.getRenderLayer();

        List<TubeModule<?>> modules = extraData.getData(TubeModulesComponent.MODEL_PROPERTY);
        if (modules == null || modules.isEmpty()) {
            if (renderLayer == null || renderLayer == RenderType.getCutout()) {
                return super.getQuads(state, side, rand, extraData);
            }
            return Collections.emptyList();
        }

        List<BakedQuad> quads = new ArrayList<>();
        if (renderLayer == null || renderLayer == RenderType.getCutout()) {
            quads.addAll(super.getQuads(state, side, rand, extraData));
        }

        for (TubeModule<?> module : modules) {
            if (renderLayer != null && renderLayer != module.getRenderLayer()) continue;
            IBakedModel model = TKPClientInit.getModel(module.getType(), module.getState(), module.getSide());
            quads.addAll(model.getQuads(null, side, rand));
        }

        return quads;
    }

}
