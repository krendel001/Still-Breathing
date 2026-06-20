package com.knockoutmod.network;

import com.knockoutmod.knockout.KnockoutHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record GiveUpKnockoutPacket() {
    public static void encode(GiveUpKnockoutPacket packet, FriendlyByteBuf buf) {
    }

    public static GiveUpKnockoutPacket decode(FriendlyByteBuf buf) {
        return new GiveUpKnockoutPacket();
    }

    public static void handle(GiveUpKnockoutPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }
            KnockoutHandler.giveUp(player);
        });
        context.setPacketHandled(true);
    }
}
