package com.technicalitiesmc.lib.client;

import com.technicalitiesmc.lib.block.TKBlock;
import com.technicalitiesmc.lib.block.TKBlockAdapter;
import com.technicalitiesmc.lib.block.TKBlockComponent;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.IdentityHashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
@SuppressWarnings("rawtypes")
public class TKSpecialRenderer extends TileEntityRenderer<TKBlockAdapter.TileEntityAdapter> {

    private static final Map<Class<? extends TKBlock.WithData>, TKBlockRenderer> blockRenderers = new IdentityHashMap<>();
    private static final Map<Class<? extends TKBlockComponent.WithData>, TKBlockRenderer> componentRenderers = new IdentityHashMap<>();

    private static TKBlockRenderer parseAnnotation(Class<?> clazz) {
        BindRenderer annotation = clazz.getAnnotation(BindRenderer.class);
        if (annotation == null) return null;
        try {
            return annotation.value().newInstance();
        } catch (InstantiationException | IllegalAccessException ignored) {
            return null;
        }
    }

    private static TKBlockRenderer get(TKBlock.WithData block) {
        return blockRenderers.computeIfAbsent(block.getClass(), TKSpecialRenderer::parseAnnotation);
    }

    private static TKBlockRenderer get(TKBlockComponent.WithData component) {
        return componentRenderers.computeIfAbsent(component.getClass(), TKSpecialRenderer::parseAnnotation);
    }

    public TKSpecialRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(TKBlockAdapter.TileEntityAdapter te, float partialTicks, MatrixStack matrix, IRenderTypeBuffer buffer, int light, int overlay) {
        TKBlock block = te.getBlock();
        if (block instanceof TKBlock.WithData) {
            TKBlockRenderer renderer = get(((TKBlock.WithData) block));
            if (renderer != null) {
                renderer.render(te.getBlockData(), partialTicks, matrix, buffer, light, overlay);
            }
        }

        te.getComponentData().forEach((component, data) -> {
            TKBlockRenderer renderer = get(component);
            if (renderer != null) {
                renderer.render(data, partialTicks, matrix, buffer, light, overlay);
            }
        });
    }

}
