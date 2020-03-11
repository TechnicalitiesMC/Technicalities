package com.technicalitiesmc.lib.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.Matrix3f;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.math.AxisAlignedBB;

public class RenderHelper {

    public static void renderCuboid(AxisAlignedBB aabb, MatrixStack matrix, IVertexBuilder buffer, int light, float r, float g, float b, float a) {
        Matrix4f mat4 = matrix.getLast().getMatrix();
        Matrix3f mat3 = matrix.getLast().getNormal();
        buffer.pos(mat4, (float) aabb.minX, (float) aabb.minY, (float) aabb.minZ).color(r, g, b, a).tex(0.0F, 0.0F).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(mat3, 0.0F, -1.0F, 0.0F).endVertex();
        buffer.pos(mat4, (float) aabb.maxX, (float) aabb.minY, (float) aabb.minZ).color(r, g, b, a).tex(1.0F, 0.0F).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(mat3, 0.0F, -1.0F, 0.0F).endVertex();
        buffer.pos(mat4, (float) aabb.maxX, (float) aabb.minY, (float) aabb.maxZ).color(r, g, b, a).tex(1.0F, 1.0F).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(mat3, 0.0F, -1.0F, 0.0F).endVertex();
        buffer.pos(mat4, (float) aabb.minX, (float) aabb.minY, (float) aabb.maxZ).color(r, g, b, a).tex(0.0F, 1.0F).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(mat3, 0.0F, -1.0F, 0.0F).endVertex();

        buffer.pos(mat4, (float) aabb.minX, (float) aabb.maxY, (float) aabb.minZ).color(r, g, b, a).tex(0.0F, 0.0F).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(mat3, 0.0F, 1.0F, 0.0F).endVertex();
        buffer.pos(mat4, (float) aabb.minX, (float) aabb.maxY, (float) aabb.maxZ).color(r, g, b, a).tex(0.0F, 1.0F).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(mat3, 0.0F, 1.0F, 0.0F).endVertex();
        buffer.pos(mat4, (float) aabb.maxX, (float) aabb.maxY, (float) aabb.maxZ).color(r, g, b, a).tex(1.0F, 1.0F).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(mat3, 0.0F, 1.0F, 0.0F).endVertex();
        buffer.pos(mat4, (float) aabb.maxX, (float) aabb.maxY, (float) aabb.minZ).color(r, g, b, a).tex(1.0F, 0.0F).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(mat3, 0.0F, 1.0F, 0.0F).endVertex();

        buffer.pos(mat4, (float) aabb.minX, (float) aabb.minY, (float) aabb.minZ).color(r, g, b, a).tex(0.0F, 0.0F).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(mat3, 0.0F, 0.0F, -1.0F).endVertex();
        buffer.pos(mat4, (float) aabb.minX, (float) aabb.maxY, (float) aabb.minZ).color(r, g, b, a).tex(0.0F, 1.0F).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(mat3, 0.0F, 0.0F, -1.0F).endVertex();
        buffer.pos(mat4, (float) aabb.maxX, (float) aabb.maxY, (float) aabb.minZ).color(r, g, b, a).tex(1.0F, 1.0F).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(mat3, 0.0F, 0.0F, -1.0F).endVertex();
        buffer.pos(mat4, (float) aabb.maxX, (float) aabb.minY, (float) aabb.minZ).color(r, g, b, a).tex(1.0F, 0.0F).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(mat3, 0.0F, 0.0F, -1.0F).endVertex();

        buffer.pos(mat4, (float) aabb.minX, (float) aabb.minY, (float) aabb.maxZ).color(r, g, b, a).tex(0.0F, 0.0F).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(mat3, 0.0F, 0.0F, 1.0F).endVertex();
        buffer.pos(mat4, (float) aabb.maxX, (float) aabb.minY, (float) aabb.maxZ).color(r, g, b, a).tex(1.0F, 0.0F).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(mat3, 0.0F, 0.0F, 1.0F).endVertex();
        buffer.pos(mat4, (float) aabb.maxX, (float) aabb.maxY, (float) aabb.maxZ).color(r, g, b, a).tex(1.0F, 1.0F).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(mat3, 0.0F, 0.0F, 1.0F).endVertex();
        buffer.pos(mat4, (float) aabb.minX, (float) aabb.maxY, (float) aabb.maxZ).color(r, g, b, a).tex(0.0F, 1.0F).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(mat3, 0.0F, 0.0F, 1.0F).endVertex();

        buffer.pos(mat4, (float) aabb.minX, (float) aabb.minY, (float) aabb.minZ).color(r, g, b, a).tex(0.0F, 0.0F).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(mat3, -1.0F, 0.0F, 0.0F).endVertex();
        buffer.pos(mat4, (float) aabb.minX, (float) aabb.minY, (float) aabb.maxZ).color(r, g, b, a).tex(0.0F, 1.0F).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(mat3, -1.0F, 0.0F, 0.0F).endVertex();
        buffer.pos(mat4, (float) aabb.minX, (float) aabb.maxY, (float) aabb.maxZ).color(r, g, b, a).tex(1.0F, 1.0F).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(mat3, -1.0F, 0.0F, 0.0F).endVertex();
        buffer.pos(mat4, (float) aabb.minX, (float) aabb.maxY, (float) aabb.minZ).color(r, g, b, a).tex(1.0F, 0.0F).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(mat3, -1.0F, 0.0F, 0.0F).endVertex();

        buffer.pos(mat4, (float) aabb.maxX, (float) aabb.minY, (float) aabb.minZ).color(r, g, b, a).tex(0.0F, 0.0F).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(mat3, 1.0F, 0.0F, 0.0F).endVertex();
        buffer.pos(mat4, (float) aabb.maxX, (float) aabb.maxY, (float) aabb.minZ).color(r, g, b, a).tex(1.0F, 0.0F).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(mat3, 1.0F, 0.0F, 0.0F).endVertex();
        buffer.pos(mat4, (float) aabb.maxX, (float) aabb.maxY, (float) aabb.maxZ).color(r, g, b, a).tex(1.0F, 1.0F).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(mat3, 1.0F, 0.0F, 0.0F).endVertex();
        buffer.pos(mat4, (float) aabb.maxX, (float) aabb.minY, (float) aabb.maxZ).color(r, g, b, a).tex(0.0F, 1.0F).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(mat3, 1.0F, 0.0F, 0.0F).endVertex();
    }

    public static void renderQuad(float minX, float minY, float maxX, float maxY, float z, MatrixStack matrix, IVertexBuilder buffer, int light, float r, float g, float b, float a) {
        Matrix4f mat4 = matrix.getLast().getMatrix();
        Matrix3f mat3 = matrix.getLast().getNormal();

        buffer.pos(mat4, minX, minY, z).color(r, g, b, a).tex(0.0F, 0.0F).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(mat3, 0.0F, 0.0F, 1.0F).endVertex();
        buffer.pos(mat4, maxX, minY, z).color(r, g, b, a).tex(1.0F, 0.0F).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(mat3, 0.0F, 0.0F, 1.0F).endVertex();
        buffer.pos(mat4, maxX, maxY, z).color(r, g, b, a).tex(1.0F, 1.0F).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(mat3, 0.0F, 0.0F, 1.0F).endVertex();
        buffer.pos(mat4, minX, maxY, z).color(r, g, b, a).tex(0.0F, 1.0F).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(mat3, 0.0F, 0.0F, 1.0F).endVertex();
    }

}
