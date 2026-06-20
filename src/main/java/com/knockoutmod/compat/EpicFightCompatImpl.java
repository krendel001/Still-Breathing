package com.knockoutmod.compat;

import com.knockoutmod.client.KnockoutClientState;
import net.minecraft.world.entity.player.Player;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.client.forgeevent.RenderEpicFightPlayerEvent;
import yesman.epicfight.api.client.forgeevent.UpdatePlayerMotionEvent;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

/** Loaded only when Epic Fight is present. */
final class EpicFightCompatImpl {
    private EpicFightCompatImpl() {
    }

    static void register() {
        var eventBus = net.minecraftforge.common.MinecraftForge.EVENT_BUS;
        eventBus.addListener(EpicFightCompatImpl::onRenderEpicFightPlayer);
        eventBus.addListener(EpicFightCompatImpl::onBaseMotion);
        eventBus.addListener(EpicFightCompatImpl::onCompositeMotion);
    }

    private static void onRenderEpicFightPlayer(RenderEpicFightPlayerEvent event) {
        Player player = playerFrom(event.getPlayerPatch());
        if (player == null || !KnockoutClientState.isKnockedOutLying(player)) {
            return;
        }
        event.setShouldRender(false);
    }

    private static void onBaseMotion(UpdatePlayerMotionEvent.BaseLayer event) {
        Player player = playerFrom(event.getPlayerPatch());
        if (player == null || !KnockoutClientState.isKnockedOutLying(player)) {
            return;
        }
        event.setMotion(LivingMotions.IDLE);
    }

    private static void onCompositeMotion(UpdatePlayerMotionEvent.CompositeLayer event) {
        Player player = playerFrom(event.getPlayerPatch());
        if (player == null || !KnockoutClientState.isKnockedOutLying(player)) {
            return;
        }
        event.setMotion(LivingMotions.NONE);
    }

    private static Player playerFrom(PlayerPatch<?> patch) {
        return patch.getOriginal();
    }
}
