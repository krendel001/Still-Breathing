package com.knockoutmod;

import com.knockoutmod.config.KnockoutConfig;
import com.knockoutmod.event.KnockoutEvents;
import com.knockoutmod.init.ModEntities;
import com.knockoutmod.menu.ModMenus;
import com.knockoutmod.network.ModNetwork;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(KnockoutMod.MOD_ID)
public class KnockoutMod {
    public static final String MOD_ID = "knockoutmod";
    private static final Logger LOGGER = LogUtils.getLogger();

    public KnockoutMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        ModMenus.register(modEventBus);
        ModEntities.register(modEventBus);
        modEventBus.addListener(this::commonSetup);

        context.registerConfig(ModConfig.Type.SERVER, KnockoutConfig.SERVER_SPEC);

        MinecraftForge.EVENT_BUS.register(KnockoutEvents.class);
        LOGGER.info("{} loaded", MOD_ID);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetwork::register);
    }
}
