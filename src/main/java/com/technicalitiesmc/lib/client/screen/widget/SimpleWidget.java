package com.technicalitiesmc.lib.client.screen.widget;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class SimpleWidget extends Widget {

    protected final int x, y, width, height;
    protected boolean focused;

    protected SimpleWidget(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public boolean onMouseDown(double x, double y, int btn) {
        focused = true;
        return true;
    }

    @Override
    public boolean onMouseUp(double x, double y, int btn) {
        if (focused) {
            focused = false;
            onClicked(x, y, btn);
            return true;
        }
        return false;
    }

    protected void onClicked(double x, double y, int btn) {
    }

}
