package com.technicalitiesmc.lib.container;

import com.technicalitiesmc.lib.client.screen.widget.Widget;
import com.technicalitiesmc.lib.network.ContainerComponentPacket;
import com.technicalitiesmc.lib.network.TKLNetworkHandler;
import com.technicalitiesmc.lib.util.value.Reference;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Consumer;

public abstract class ContainerComponent {

    TKContainerAdapter container;
    int id;

    public ContainerComponent() {
    }

    @OnlyIn(Dist.CLIENT)
    public abstract Widget createWidget();

    protected final void sendClientEvent(int eventId, Consumer<PacketBuffer> writer) {
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        writer.accept(buf);
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        TKLNetworkHandler.INSTANCE.sendToServer(new ContainerComponentPacket(this.id, eventId, bytes));
    }

    protected void onClientEvent(int id, PacketBuffer buf) {
    }

    protected void requestDataTrackers(TrackerManager manager) {
    }

    public interface TrackerManager {

        void trackInts(int[] array);

        void trackBoolean(Reference<Boolean> reference);

        <E extends Enum<E>> void trackEnum(Class<E> type, Reference<E> reference);

    }

}
