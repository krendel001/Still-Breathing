package com.knockoutmod.network;

import com.knockoutmod.knockout.SelfReviveHoldTracker;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SelfReviveHoldPacket(boolean holding) {
    public static void encode(SelfReviveHoldPacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.holding);
    }

    public static SelfReviveHoldPacket decode(FriendlyByteBuf buf) {
        return new SelfReviveHoldPacket(buf.readBoolean());
    }

    public static void handle(SelfReviveHoldPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !player.isAlive()) {
                return;
            }
            SelfReviveHoldTracker.setHolding(player, packet.holding());
        });
        context.setPacketHandled(true);
    }
}
