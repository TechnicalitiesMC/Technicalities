package com.technicalitiesmc.lib.client.screen.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public abstract class Widget extends AbstractGui {

    public abstract int getX();

    public abstract int getY();

    public abstract int getWidth();

    public abstract int getHeight();

    public abstract void render(int mouseX, int mouseY, float partialTicks);

    public boolean onMouseDown(double x, double y, int btn) {
        return false;
    }

    public boolean onMouseUp(double x, double y, int btn) {
        return false;
    }

    public boolean onMouseScrolled(double x, double y, double amt) {
        return false;
    }

    public void addTooltip(double x, double y, List<ITextComponent> tooltip) {
    }

    protected final void bindTexture(ResourceLocation path) {
        Minecraft.getInstance().textureManager.bindTexture(path);
    }

}
