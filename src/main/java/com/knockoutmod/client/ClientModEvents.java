package com.knockoutmod.client;

import com.knockoutmod.KnockoutMod;
import com.knockoutmod.compat.EpicFightCompat;
import com.knockoutmod.client.gecko.KnockoutLyingGroundAlign;
import com.knockoutmod.init.ModEntities;
import com.knockoutmod.menu.ModMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = KnockoutMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void registerScreens(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.KNOCKOUT_LOOT.get(), KnockoutLootScreen::new);
            MenuScreens.register(ModMenus.DUMMY_LOOT.get(), DummyLootScreen::new);
            EntityRenderers.register(ModEntities.TEST_DUMMY.get(), KnockoutTestDummyRenderer::new);
            EpicFightCompat.init();
        });
    }

    @SubscribeEvent
    public static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((preparationBarrier, resourceManager, profiler, profilerF, preparationExecutor, reloadExecutor) ->
                preparationBarrier.wait(null).thenRun(KnockoutLyingGroundAlign::invalidateCache));
    }
}
