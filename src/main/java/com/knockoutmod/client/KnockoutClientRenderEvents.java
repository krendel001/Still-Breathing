package com.knockoutmod.client;

import com.knockoutmod.KnockoutMod;
import com.knockoutmod.knockout.KnockoutHitboxHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = KnockoutMod.MOD_ID, value = Dist.CLIENT)
public final class KnockoutClientRenderEvents {
    private KnockoutClientRenderEvents() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        LivingEntity entity = event.getEntity();
        if (!KnockoutClientState.isKnockedOutLying(entity)) {
            return;
        }

        event.setCanceled(true);
        KnockoutBodyRender.renderLyingBody(
                entity,
                event.getPoseStack(),
                event.getMultiBufferSource(),
                event.getPackedLight(),
                event.getPartialTick()
        );
    }

    @SuppressWarnings("removal") // EntityEvent.Size has no replacement on Forge 1.20.1 yet.
    @SubscribeEvent
    public static void onEntitySize(EntityEvent.Size event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity living) || !KnockoutClientState.isKnockedOutLying(living)) {
            return;
        }

        event.setNewSize(KnockoutHitboxHelper.LYING_DIMENSIONS, false);
    }
}
