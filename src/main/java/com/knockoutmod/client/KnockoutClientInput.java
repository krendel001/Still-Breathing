package com.knockoutmod.client;

import com.knockoutmod.KnockoutMod;
import com.knockoutmod.network.ModNetwork;
import com.knockoutmod.network.ReviveHoldPacket;
import com.knockoutmod.network.SelfReviveHoldPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = KnockoutMod.MOD_ID, value = Dist.CLIENT)
public final class KnockoutClientInput {
    private static boolean lastSentSelfReviveHolding;
    private static int lastSentVictimId = -1;

    private KnockoutClientInput() {
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getSide().isServer()) {
            return;
        }
        if (!(event.getEntity() instanceof LocalPlayer player) || !player.isShiftKeyDown()) {
            return;
        }

        Entity target = event.getTarget();
        if (!ReviveTargetHelper.isKnockoutTarget(target, player)) {
            return;
        }

        lastSentVictimId = target.getId();
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            lastSentSelfReviveHolding = false;
            return;
        }

        tickSelfReviveInput(minecraft);
        tickAllyReviveInput(minecraft);
    }

    private static void tickSelfReviveInput(Minecraft minecraft) {
        boolean knockedOut = KnockoutClientState.isKnockedOut(minecraft.player);
        boolean holding = knockedOut && minecraft.options.keyShift.isDown();

        if (!knockedOut) {
            if (lastSentSelfReviveHolding) {
                ModNetwork.CHANNEL.sendToServer(new SelfReviveHoldPacket(false));
                lastSentSelfReviveHolding = false;
            }
            return;
        }

        if (holding == lastSentSelfReviveHolding && holding && minecraft.player.tickCount % 20 != 0) {
            return;
        }

        ModNetwork.CHANNEL.sendToServer(new SelfReviveHoldPacket(holding));
        lastSentSelfReviveHolding = holding;
    }

    private static void tickAllyReviveInput(Minecraft minecraft) {
        if (minecraft.level == null || minecraft.screen != null) {
            clearAllyReviveHold();
            return;
        }

        boolean holding = minecraft.options.keyShift.isDown() && minecraft.options.keyUse.isDown();
        if (!holding) {
            clearAllyReviveHold();
            return;
        }

        Entity target = ReviveTargetHelper.getCrosshairKnockoutTarget(minecraft);
        if (target == null) {
            clearAllyReviveHold();
            return;
        }

        lastSentVictimId = target.getId();
        ModNetwork.CHANNEL.sendToServer(new ReviveHoldPacket(target.getId()));
    }

    private static void clearAllyReviveHold() {
        if (lastSentVictimId != -1) {
            ModNetwork.CHANNEL.sendToServer(new ReviveHoldPacket(-1));
            ReviveClientState.clear();
            lastSentVictimId = -1;
        }
    }
}
