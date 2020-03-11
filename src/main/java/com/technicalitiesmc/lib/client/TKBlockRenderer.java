package com.technicalitiesmc.lib.client;

import com.technicalitiesmc.lib.block.TKBlockData;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;

public abstract class TKBlockRenderer<D extends TKBlockData> {

    public abstract void render(D data, float partialTicks, MatrixStack matrix, IRenderTypeBuffer buffer, int light, int overlay);

}
