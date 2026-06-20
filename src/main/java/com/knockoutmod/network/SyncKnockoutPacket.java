package com.knockoutmod.network;

import com.knockoutmod.client.KnockoutClientState;
import com.knockoutmod.client.SelfReviveClientState;
import com.knockoutmod.knockout.KnockoutData;
import com.knockoutmod.knockout.KnockoutHitboxHelper;
import com.knockoutmod.knockout.KnockoutPose;
import com.knockoutmod.knockout.KnockoutPoseApplier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SyncKnockoutPacket(int entityId, boolean knockedOut, int poseId) {
    public static SyncKnockoutPacket fromPlayer(Player player) {
        return new SyncKnockoutPacket(
                player.getId(),
                KnockoutData.isKnockedOut(player),
                KnockoutData.getPose(player).ordinal()
        );
    }

    public static void encode(SyncKnockoutPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.entityId);
        buf.writeBoolean(packet.knockedOut);
        buf.writeVarInt(packet.poseId);
    }

    public static SyncKnockoutPacket decode(FriendlyByteBuf buf) {
        return new SyncKnockoutPacket(buf.readVarInt(), buf.readBoolean(), buf.readVarInt());
    }

    public static void handle(SyncKnockoutPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> applyClient(packet)));
        context.setPacketHandled(true);
    }

    private static void applyClient(SyncKnockoutPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        Player player = (Player) minecraft.level.getEntity(packet.entityId);
        if (player == null) {
            return;
        }

        KnockoutClientState.set(player, packet.knockedOut(), KnockoutPose.fromId(packet.poseId));
        if (!packet.knockedOut()) {
            SelfReviveClientState.clear();
        }
        if (packet.knockedOut()) {
            KnockoutPose pose = KnockoutPose.fromId(packet.poseId);
            KnockoutPoseApplier.applyClient(player, pose);
            if (pose == KnockoutPose.LYING) {
                KnockoutHitboxHelper.applyKnockoutHitbox(player);
            }
        } else {
            KnockoutPoseApplier.clearClient(player);
        }
    }
}
