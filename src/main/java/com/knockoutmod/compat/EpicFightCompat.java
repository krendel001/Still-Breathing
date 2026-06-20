package com.knockoutmod.compat;

import net.minecraftforge.fml.ModList;

/**
 * Optional Epic Fight integration entry point.
 * This class has no Epic Fight types on its classpath surface, so the mod runs fine without Epic Fight installed.
 */
public final class EpicFightCompat {
    private EpicFightCompat() {
    }

    public static void init() {
        if (!ModList.get().isLoaded("epicfight")) {
            return;
        }
        EpicFightCompatImpl.register();
    }
}
