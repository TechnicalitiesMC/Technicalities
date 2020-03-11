package com.technicalitiesmc.base.client;

import com.technicalitiesmc.Technicalities;
import com.technicalitiesmc.base.block.components.Labelable;
import com.technicalitiesmc.lib.client.RenderHelper;
import com.technicalitiesmc.lib.client.TKBlockRenderer;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.BitSet;

@OnlyIn(Dist.CLIENT)
public class LabelRenderer extends TKBlockRenderer<Labelable.Data> {

    private static RenderType LABEL_BACKGROUND_TYPE = RenderType.getEntityCutout(new ResourceLocation(Technicalities.MODID, "textures/blocks/label.png"));

    @Override
    public void render(Labelable.Data data, float partialTicks, MatrixStack matrix, IRenderTypeBuffer buffer, int light, int overlay) {
        ItemStack label = data.getLabel();

        BitSet sides = data.getSides();
        for (int i = 0; i < 4; i++) {
            if (sides.get(i)) {
                Direction side = Direction.byHorizontalIndex(i);
                light = WorldRenderer.getCombinedLight(data.getHost().getWorld(), data.getHost().getPos().offset(side));
                float offset = data.getOffset(side);
                drawItem(matrix, buffer, light, i, offset, label);
            }
        }
    }

    private void drawItem(MatrixStack matrix, IRenderTypeBuffer buffer, int light, int rotation, float offset, ItemStack stack) {
        Matrix3f normal = matrix.getLast().getNormal();
        matrix.push();
        matrix.translate(0.5, 0.5, 0.5);
        matrix.rotate(new Quaternion(0, rotation * -90, 0, true));
        matrix.translate(0, 0, offset + 0.01F);

        RenderHelper.renderQuad(-0.5F, -0.5F, 0.5F, 0.5F, -0.005F, matrix, buffer.getBuffer(LABEL_BACKGROUND_TYPE), light, 1, 1, 1, 1);

        if (!stack.isEmpty()) {
            matrix.scale(0.375F, 0.375F, 0.005F);
            matrix.getLast().getNormal().set(normal);

            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            // TODO: try to tweak lighting to match face
            itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.GUI, light, OverlayTexture.NO_OVERLAY, matrix, buffer);
        }

        matrix.pop();
    }

}
