package com.knockoutmod.network;

import com.knockoutmod.client.SelfReviveClientState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SyncSelfReviveProgressPacket(int progress, int required, boolean active) {
    public static void encode(SyncSelfReviveProgressPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.progress);
        buf.writeVarInt(packet.required);
        buf.writeBoolean(packet.active);
    }

    public static SyncSelfReviveProgressPacket decode(FriendlyByteBuf buf) {
        return new SyncSelfReviveProgressPacket(buf.readVarInt(), buf.readVarInt(), buf.readBoolean());
    }

    public static void handle(SyncSelfReviveProgressPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> SelfReviveClientState.apply(packet)));
        context.setPacketHandled(true);
    }
}
