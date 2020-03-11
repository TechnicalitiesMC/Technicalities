package com.technicalitiesmc.pneumatics.client;

import com.technicalitiesmc.Technicalities;
import com.technicalitiesmc.lib.client.RenderHelper;
import com.technicalitiesmc.lib.client.TKBlockRenderer;
import com.technicalitiesmc.pneumatics.block.components.TubeComponent;
import com.technicalitiesmc.pneumatics.tube.MovingTubeStack;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

public class TubeRenderer extends TKBlockRenderer<TubeComponent.Data> {

    private static RenderType COLOR_MARKER_TYPE = RenderType.getEntityCutout(new ResourceLocation(Technicalities.MODID, "textures/blocks/pneumatic_tube/stack_border.png"));
    private static RenderType COLOR_MARKER_INNER_TYPE = RenderType.getEntityTranslucentCull(new ResourceLocation(Technicalities.MODID, "textures/blocks/pneumatic_tube/stack_border_back.png"));

    @Override
    public void render(TubeComponent.Data data, float partialTicks, MatrixStack matrix, IRenderTypeBuffer buffer, int light, int overlay) {
        for (MovingTubeStack stack : data.getStacks()) {
            float offset = stack.getInterpolatedOffset(partialTicks);
            Vec3d off = new Vec3d(stack.getDirection(offset).getDirectionVec()).scale(Math.abs(0.5 - offset));

            matrix.push();
            matrix.translate(off.x + 0.5, off.y + 0.5, off.z + 0.5);
            renderStack(stack.getStack(), stack.getColor(), matrix, buffer, partialTicks, light, overlay);
            matrix.pop();
        }
    }

    private void renderStack(ItemStack stack, DyeColor color, MatrixStack matrix, IRenderTypeBuffer buffer, float partialTicks, int light, int overlay) {
        Minecraft minecraft = Minecraft.getInstance();
        ItemRenderer ir = minecraft.getItemRenderer();

        matrix.push();
        matrix.scale(0.375F, 0.375F, 0.375F);
        matrix.rotate(new Quaternion(0, (float) ((minecraft.world.getGameTime() / 20D) % (Math.PI * 2) + (partialTicks / 20D)), 0, false));
        ir.renderItem(stack, ItemCameraTransforms.TransformType.FIXED, light, overlay, matrix, buffer);
        matrix.pop();

        if (color == null) return;

        double hw = 0.1375;
        AxisAlignedBB aabb = new AxisAlignedBB(-hw, -hw, -hw, hw, hw, hw);

        float[] colComp = color.getColorComponentValues();

        matrix.push();
        RenderHelper.renderCuboid(aabb, matrix, buffer.getBuffer(COLOR_MARKER_TYPE), light, colComp[0], colComp[1], colComp[2], 1.0F);
        matrix.pop();

        matrix.scale(-1, -1, -1);
        RenderHelper.renderCuboid(aabb, matrix, buffer.getBuffer(COLOR_MARKER_INNER_TYPE), light, colComp[0], colComp[1], colComp[2], 1.0F);
        matrix.scale(-1, -1, -1);

    }

}
