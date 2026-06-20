package com.knockoutmod.client;

import com.knockoutmod.KnockoutMod;
import com.knockoutmod.knockout.KnockoutData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = KnockoutMod.MOD_ID, value = Dist.CLIENT)
public final class KnockoutClientLifecycle {
    private KnockoutClientLifecycle() {
    }

    @SubscribeEvent
    public static void onClientClone(ClientPlayerNetworkEvent.Clone event) {
        KnockoutClientReset.reset(event.getNewPlayer());
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && event.getEntity() == player) {
            KnockoutClientReset.reset(player);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || player.isDeadOrDying()) {
            return;
        }

        if (KnockoutClientState.isKnockedOut(player) && !KnockoutData.isKnockedOut(player)) {
            KnockoutClientReset.reset(player);
        }
    }
}
