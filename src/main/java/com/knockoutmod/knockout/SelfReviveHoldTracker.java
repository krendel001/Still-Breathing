package com.knockoutmod.knockout;

import net.minecraft.server.level.ServerPlayer;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SelfReviveHoldTracker {
    private static final Set<UUID> HOLDING = ConcurrentHashMap.newKeySet();

    private SelfReviveHoldTracker() {
    }

    public static void setHolding(ServerPlayer player, boolean holding) {
        if (holding) {
            HOLDING.add(player.getUUID());
        } else {
            HOLDING.remove(player.getUUID());
        }
    }

    public static boolean isHolding(ServerPlayer player) {
        return HOLDING.contains(player.getUUID());
    }

    public static void clear(ServerPlayer player) {
        HOLDING.remove(player.getUUID());
    }
}
