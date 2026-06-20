package com.knockoutmod.network;

import com.knockoutmod.client.ReviveClientState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SyncReviveProgressPacket(
        int victimEntityId,
        int progress,
        int required,
        boolean active,
        Component victimName
) {
    public static void encode(SyncReviveProgressPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.victimEntityId);
        buf.writeVarInt(packet.progress);
        buf.writeVarInt(packet.required);
        buf.writeBoolean(packet.active);
        buf.writeComponent(packet.victimName);
    }

    public static SyncReviveProgressPacket decode(FriendlyByteBuf buf) {
        return new SyncReviveProgressPacket(
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readBoolean(),
                buf.readComponent()
        );
    }

    public static void handle(SyncReviveProgressPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ReviveClientState.apply(packet)));
        context.setPacketHandled(true);
    }
}
