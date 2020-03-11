package com.technicalitiesmc.lib.container.component;

import com.technicalitiesmc.lib.client.screen.widget.EnumSelectorWidget;
import com.technicalitiesmc.lib.client.screen.widget.Widget;
import com.technicalitiesmc.lib.container.ContainerComponent;
import com.technicalitiesmc.lib.util.value.Reference;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Arrays;
import java.util.List;

public class EnumSelectorComponent<E extends Enum<E>> extends ContainerComponent {

    private final int x, y, width, height;
    private final int u, v;
    private final Class<E> type;
    private final Reference<E> reference;
    private final List<E> values;
    private final boolean highlight;

    public EnumSelectorComponent(int x, int y, int width, int height, int u, int v, Class<E> type, Reference<E> reference, boolean highlight) {
        this(x, y, width, height, u, v, type, reference, Arrays.asList(type.getEnumConstants()), highlight);
    }

    public EnumSelectorComponent(int x, int y, int width, int height, int u, int v, Class<E> type, Reference<E> reference, List<E> values, boolean highlight) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.u = u;
        this.v = v;
        this.type = type;
        this.reference = reference;
        this.values = values;
        this.highlight = highlight;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Widget createWidget() {
        return new EnumSelectorWidget<>(x, y, width, height, u, v, Reference.of(this::get, this::set), values, highlight);
    }

    private E get() {
        return reference.get();
    }

    private void set(E reference) {
        this.reference.set(reference);
        sendClientEvent(0, buf -> {
            buf.writeEnumValue(reference);
        });
    }

    @Override
    protected void onClientEvent(int id, PacketBuffer buf) {
        if (id == 0) {
            reference.set(buf.readEnumValue(type));
        }
    }

    @Override
    protected void requestDataTrackers(TrackerManager manager) {
        manager.trackEnum(type, reference);
    }

}
