package com.technicalitiesmc.lib.client.screen.widget;

import com.technicalitiesmc.lib.util.TooltipEnabled;
import com.technicalitiesmc.lib.util.value.Reference;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class EnumSelectorWidget<E extends Enum<E>> extends SimpleWidget {

    private final int u, v;
    private final Reference<E> reference;
    private final List<E> values;
    private final boolean highlight;

    public EnumSelectorWidget(int x, int y, int width, int height, int u, int v, Reference<E> reference, List<E> values, boolean highlight) {
        super(x, y, width, height);
        this.u = u;
        this.v = v;
        this.reference = reference;
        this.values = values;
        this.highlight = highlight;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        E value = reference.get();
        if (value == null) return;
        blit(x, y, u + value.ordinal() * width, v, width, height);
        if (highlight && mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height) {
            fill(x, y, x + width, y + height, 0x40FFFFFE);
        }
    }

    @Override
    protected void onClicked(double x, double y, int btn) {
        if (btn == 0) {
            cycle(true);
        } else if (btn == 1) {
            cycle(false);
        }
    }

    @Override
    public boolean onMouseScrolled(double x, double y, double amt) {
        cycle(amt > 0);
        return true;
    }

    @Override
    public void addTooltip(double x, double y, List<ITextComponent> tooltip) {
        E value = reference.get();
        if (value instanceof TooltipEnabled) {
            ((TooltipEnabled) value).addTooltip(tooltip);
        }
    }

    private void cycle(boolean forward) {
        E value = reference.get();
        if (value == null) return;
        int idx = values.indexOf(value);
        int total = values.size();
        int newIdx = (idx + (forward ? 1 : total - 1)) % total;
        reference.set(values.get(newIdx));
    }

}
