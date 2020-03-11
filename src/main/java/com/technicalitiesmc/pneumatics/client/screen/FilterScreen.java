package com.technicalitiesmc.pneumatics.client.screen;

import com.technicalitiesmc.Technicalities;
import com.technicalitiesmc.lib.client.screen.TKContainerScreen;
import com.technicalitiesmc.lib.container.TKContainerAdapter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class FilterScreen extends TKContainerScreen {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Technicalities.MODID, "textures/gui/filter.png");

    public FilterScreen(TKContainerAdapter container, PlayerInventory inv, ITextComponent name) {
        super(container, inv, name);
        this.xSize = 176;
        this.ySize = 168;
    }

    @Override
    protected ResourceLocation getBackgroundTexture() {
        return TEXTURE;
    }

}
