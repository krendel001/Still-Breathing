package com.knockoutmod.client;

import com.knockoutmod.knockout.KnockoutPoseApplier;
import net.minecraft.world.entity.player.Player;

public final class KnockoutClientReset {
    private KnockoutClientReset() {
    }

    public static void reset(Player player) {
        if (player == null) {
            return;
        }
        KnockoutClientState.clear(player);
        SelfReviveClientState.clear();
        ReviveClientState.clear();
        KnockoutPoseApplier.clearClient(player);
        KnockoutClientEvents.resetCameraAndCursor();
    }
}
