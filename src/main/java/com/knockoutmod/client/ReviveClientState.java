package com.knockoutmod.client;

import com.knockoutmod.network.SyncReviveProgressPacket;
import net.minecraft.network.chat.Component;

public final class ReviveClientState {
    private static int victimEntityId;
    private static int progress;
    private static int required;
    private static boolean active;
    private static Component victimName = Component.empty();

    private ReviveClientState() {
    }

    public static void apply(SyncReviveProgressPacket packet) {
        victimEntityId = packet.victimEntityId();
        progress = packet.progress();
        required = packet.required();
        active = packet.active();
        victimName = packet.victimName();
    }

    public static void clear() {
        active = false;
        progress = 0;
        required = 0;
        victimName = Component.empty();
    }

    public static boolean isActive() {
        return active && required > 0;
    }

    public static float getRatio() {
        if (required <= 0) {
            return 0.0F;
        }
        return Math.min(1.0F, progress / (float) required);
    }

    public static int getProgress() {
        return progress;
    }

    public static int getRequired() {
        return required;
    }

    public static Component getVictimName() {
        return victimName;
    }

    public static int getVictimEntityId() {
        return victimEntityId;
    }
}
