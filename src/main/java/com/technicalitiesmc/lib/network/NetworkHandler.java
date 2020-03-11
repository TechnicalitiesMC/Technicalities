package com.technicalitiesmc.lib.network;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.server.ChunkManager;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class NetworkHandler {

    public final SimpleChannel channel;

    private int i = 0;

    public NetworkHandler(String modid, int protocol) {
        this(modid, "default", protocol);
    }

    public NetworkHandler(String modid, String channelName, int protocol) {
        String protocolStr = protocol + "";
        channel = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(modid, channelName))
            .networkProtocolVersion(() -> protocolStr)
            .clientAcceptedVersions(protocolStr::equals)
            .serverAcceptedVersions(protocolStr::equals)
            .simpleChannel();
    }

    public <T extends Packet> void register(Class<T> clazz, NetworkDirection dir) {
        Function<PacketBuffer, T> decoder = (buf) -> {
            try {
                T packet = clazz.newInstance();
                packet.deserialize(buf);
                return packet;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        BiConsumer<T, Supplier<NetworkEvent.Context>> consumer = (msg, supp) -> {
            NetworkEvent.Context context = supp.get();
            if (context.getDirection() != dir) return;
            context.setPacketHandled(msg.handle(context));
        };

        channel.registerMessage(i, clazz, Packet::serialize, decoder, consumer);
        i++;
    }

    public void sendToWorld(Packet packet, IWorld world) {
        if (world.isRemote()) return;
        ServerWorld sw = (ServerWorld) world;
        for (PlayerEntity player : sw.getPlayers()) {
            if (player.getEntityWorld() != world) continue;
            sendToPlayer(packet, player);
        }
    }

    public void sendToAllAround(Packet packet, IWorld world, BlockPos pos, float radius) {
        if (world.isRemote()) return;
        ServerWorld sw = (ServerWorld) world;
        for (PlayerEntity player : sw.getPlayers()) {
            if (player.getPosition().distanceSq(pos) > radius * radius) continue;
            sendToPlayer(packet, player);
        }
    }

    public void sendToAllWatching(Packet packet, IWorld world, BlockPos pos) {
        if (world.isRemote()) return;
        ChunkManager chunkManager = ((ServerWorld) world).getChunkProvider().chunkManager;
        chunkManager.getTrackingPlayers(new ChunkPos(pos), false).forEach(player -> {
            sendToPlayer(packet, player);
        });
    }

    public void sendToPlayer(Packet packet, PlayerEntity player) {
        if (!(player instanceof ServerPlayerEntity)) return;
        channel.sendTo(packet, ((ServerPlayerEntity) player).connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
    }

    public void sendToServer(Packet packet) {
        channel.sendToServer(packet);
    }

}
