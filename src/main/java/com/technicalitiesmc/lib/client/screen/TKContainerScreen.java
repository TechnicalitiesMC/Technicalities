package com.technicalitiesmc.lib.client.screen;

import com.technicalitiesmc.Technicalities;
import com.technicalitiesmc.lib.client.screen.widget.Widget;
import com.technicalitiesmc.lib.container.*;
import com.technicalitiesmc.lib.network.GhostSlotClickPacket;
import com.technicalitiesmc.lib.network.TKLNetworkHandler;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public abstract class TKContainerScreen extends ContainerScreen<TKContainerAdapter> {

    private static final ResourceLocation WIDGET_TEXTURE = new ResourceLocation(Technicalities.MODID, "textures/gui/widgets.png");

    private final List<Widget> widgets = new ArrayList<>();

    public TKContainerScreen(TKContainerAdapter container, PlayerInventory inv, ITextComponent name) {
        super(container, inv, name);
        for (ContainerComponent component : container.getComponents()) {
            widgets.add(component.createWidget());
        }
        widgets.removeIf(Objects::isNull);
    }

    protected abstract ResourceLocation getBackgroundTexture();

    @Override
    public int getSlotColor(int index) { // Hover color, not overlay color
        Slot slot = container.inventorySlots.get(index);
        return slot instanceof LockableSlot && ((LockableSlot) slot).isLocked() ? 0x50000001 : super.getSlotColor(index);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.renderBackground();
        // Render vanilla elements (slots, buttons)
        super.render(mouseX, mouseY, partialTicks);

        // Render widgets
        GlStateManager.pushMatrix();
        GlStateManager.translated(guiLeft, guiTop, 0);
        for (Widget widget : widgets) {
            GlStateManager.disableLighting();
            GlStateManager.color4f(1, 1, 1, 1);
            minecraft.getTextureManager().bindTexture(WIDGET_TEXTURE);
            widget.render(mouseX - guiLeft, mouseY - guiTop, partialTicks);
        }
        GlStateManager.popMatrix();

        // Render tooltips
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        minecraft.getTextureManager().bindTexture(getBackgroundTexture());
        blit(guiLeft, (height - ySize) / 2, 0, 0, xSize, ySize);

        // Render slot backgrounds
        for (Slot slot : container.inventorySlots) {
            if (!(slot instanceof ColoredSlot)) continue;
            int color = ((ColoredSlot) slot).getColor();
            if (color == 0) continue;
            fill(guiLeft + slot.xPos, guiTop + slot.yPos, guiLeft + slot.xPos + 16, guiTop + slot.yPos + 16, color);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        font.drawString(title.getFormattedText(), 8.0F, 6.0F, 0x404040);
        font.drawString(playerInventory.getDisplayName().getFormattedText(), 8.0F, ySize - 96 + 2, 0x404040);

        // Render locked slot overlays
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
        GlStateManager.disableDepthTest();
        for (Slot slot : container.inventorySlots) {
            if (!(slot instanceof LockableSlot) || !((LockableSlot) slot).isLocked()) continue;
            fill(slot.xPos - 1, slot.yPos - 1, slot.xPos + 17, slot.yPos + 17, 0x50000001);
        }
        GlStateManager.enableDepthTest();
    }

    @Override
    protected void renderHoveredToolTip(int mouseX, int mouseY) {
        super.renderHoveredToolTip(mouseX, mouseY);
        for (Widget widget : widgets) {
            int x = guiLeft + widget.getX(), y = guiTop + widget.getY();
            int width = widget.getWidth(), height = widget.getHeight();
            if (mouseX < x || mouseX >= x + width || mouseY < y || mouseY >= y + height) continue;

            List<ITextComponent> tooltip = new ArrayList<>();
            widget.addTooltip(mouseX - x, mouseY - y, tooltip);
            if (tooltip.isEmpty()) continue;

            List<String> text = new ArrayList<>();
            for (ITextComponent component : tooltip) {
                text.addAll(Arrays.asList(component.getFormattedText().split("\n")));
            }
            renderTooltip(text, mouseX, mouseY, font);
        }
    }

    @Override
    public boolean mouseClicked(double x, double y, int btn) {
        if (hoveredSlot != null && hoveredSlot instanceof TKGhostSlot) {
            ItemStack stack = Minecraft.getInstance().player.inventory.getItemStack();
            stack = stack.copy().split(hoveredSlot.getSlotStackLimit());
            hoveredSlot.putStack(stack); // Temporarily update the client for continuity purposes
            TKLNetworkHandler.INSTANCE.sendToServer(new GhostSlotClickPacket(hoveredSlot.slotNumber, stack));
            return true;
        }
        for (Widget widget : widgets) {
            int x1 = guiLeft + widget.getX(), y1 = guiTop + widget.getY();
            int x2 = x1 + widget.getWidth(), y2 = y1 + widget.getHeight();
            if (x >= x1 && x < x2 && y >= y1 && y < y2) {
                if (widget.onMouseDown(x - guiLeft, y - guiTop, btn)) {
                    return true;
                }
            }
        }
        return super.mouseClicked(x, y, btn);
    }

    @Override
    public boolean mouseReleased(double x, double y, int btn) {
        if (hoveredSlot != null && hoveredSlot instanceof TKGhostSlot) {
            return true;
        }
        for (Widget widget : widgets) {
            int x1 = guiLeft + widget.getX(), y1 = guiTop + widget.getY();
            int x2 = x1 + widget.getWidth(), y2 = y1 + widget.getHeight();
            if (x >= x1 && x < x2 && y >= y1 && y < y2) {
                if (widget.onMouseUp(x - guiLeft, y - guiTop, btn)) {
                    return true;
                }
            }
        }
        return super.mouseReleased(x, y, btn);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double amt) {
        if (hoveredSlot != null && hoveredSlot instanceof TKGhostSlot) {
            ItemStack stack = hoveredSlot.getStack();
            if (stack.isEmpty()) return true;

            int dif = amt > 0 ? 1 : -1;
            int newSize = stack.getCount() + dif;
            if (newSize <= 0 || newSize > ((TKGhostSlot) hoveredSlot).getMaxStackSize(stack))
                return true;

            ItemStack newStack = stack.copy();
            newStack.setCount(newSize);
            hoveredSlot.putStack(newStack); // Temporarily update the client for continuity purposes
            TKLNetworkHandler.INSTANCE.sendToServer(new GhostSlotClickPacket(hoveredSlot.slotNumber, newStack));
            return true;
        }
        for (Widget widget : widgets) {
            int x1 = guiLeft + widget.getX(), y1 = guiTop + widget.getY();
            int x2 = x1 + widget.getWidth(), y2 = y1 + widget.getHeight();
            if (x >= x1 && x < x2 && y >= y1 && y < y2) {
                if (widget.onMouseScrolled(x - guiLeft, y - guiTop, amt)) {
                    return true;
                }
            }
        }
        return super.mouseScrolled(x, y, amt);
    }

}
