package com.knockoutmod.network;

import com.knockoutmod.knockout.AllyReviveHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ReviveHoldPacket(int victimEntityId) {
    public static void encode(ReviveHoldPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.victimEntityId);
    }

    public static ReviveHoldPacket decode(FriendlyByteBuf buf) {
        return new ReviveHoldPacket(buf.readVarInt());
    }

    public static void handle(ReviveHoldPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }
            AllyReviveHandler.hold(player, packet.victimEntityId());
        });
        context.setPacketHandled(true);
    }
}
