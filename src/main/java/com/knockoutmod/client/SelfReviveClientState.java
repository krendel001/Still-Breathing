package com.knockoutmod.client;

import com.knockoutmod.network.SyncSelfReviveProgressPacket;

public final class SelfReviveClientState {
    private static int progress;
    private static int required;
    private static boolean active;

    private SelfReviveClientState() {
    }

    public static void apply(SyncSelfReviveProgressPacket packet) {
        progress = packet.progress();
        required = packet.required();
        active = packet.active();
    }

    public static void clear() {
        progress = 0;
        required = 0;
        active = false;
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
}
