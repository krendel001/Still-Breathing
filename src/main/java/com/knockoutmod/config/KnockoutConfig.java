package com.knockoutmod.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class KnockoutConfig {
    public static final ForgeConfigSpec SERVER_SPEC;
    public static final Server SERVER;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        SERVER = new Server(builder);
        SERVER_SPEC = builder.build();
    }

    private KnockoutConfig() {
    }

    public static final class Server {
        public final ForgeConfigSpec.IntValue bleedOutSeconds;
        public final ForgeConfigSpec.IntValue selfReviveCheckSeconds;
        public final ForgeConfigSpec.DoubleValue baseSelfReviveChance;
        public final ForgeConfigSpec.DoubleValue maxSelfReviveChance;
        public final ForgeConfigSpec.DoubleValue activeSelfReviveChance;
        public final ForgeConfigSpec.IntValue allyReviveHoldSeconds;
        public final ForgeConfigSpec.IntValue selfReviveHoldSeconds;
        public final ForgeConfigSpec.DoubleValue recoveryHealthHearts;
        public final ForgeConfigSpec.IntValue recoveryWeaknessSeconds;
        public final ForgeConfigSpec.IntValue knockoutGraceSeconds;
        public final ForgeConfigSpec.DoubleValue exhaustionPenaltyPerStack;
        public final ForgeConfigSpec.IntValue maxExhaustionStacks;
        public final ForgeConfigSpec.IntValue finishHitsRequired;
        public final ForgeConfigSpec.BooleanValue allowHotbarLoot;
        public final ForgeConfigSpec.BooleanValue knockoutCreativePlayers;
        public final ForgeConfigSpec.DoubleValue environmentalDamageMultiplier;

        private Server(ForgeConfigSpec.Builder builder) {
            builder.push("knockout");

            bleedOutSeconds = builder
                    .comment("Seconds before an unattended knocked-out player bleeds out and dies.")
                    .defineInRange("bleedOutSeconds", 90, 20, 600);

            selfReviveCheckSeconds = builder
                    .comment("How often the server rolls for passive self-recovery while knocked out.")
                    .defineInRange("selfReviveCheckSeconds", 10, 1, 30);

            baseSelfReviveChance = builder
                    .comment("Base chance (0.0-1.0) to recover on each passive self-revive check.")
                    .defineInRange("baseSelfReviveChance", 0.005, 0.0, 1.0);

            maxSelfReviveChance = builder
                    .comment("Maximum passive self-recovery chance as knockout time increases.")
                    .defineInRange("maxSelfReviveChance", 0.03, 0.0, 1.0);

            activeSelfReviveChance = builder
                    .comment("Chance (0.0-1.0) to recover after holding sneak for the full active self-revive duration.")
                    .defineInRange("activeSelfReviveChance", 0.05, 0.0, 1.0);

            allyReviveHoldSeconds = builder
                    .comment("Seconds an ally must hold Shift + right mouse button while looking at a knocked-out player.")
                    .defineInRange("allyReviveHoldSeconds", 7, 5, 15);

            selfReviveHoldSeconds = builder
                    .comment("Seconds a knocked-out player must hold sneak to attempt active self-recovery.")
                    .defineInRange("selfReviveHoldSeconds", 8, 1, 20);

            recoveryHealthHearts = builder
                    .comment("Hearts restored when a player wakes up from knockout.")
                    .defineInRange("recoveryHealthHearts", 4.0, 1.0, 10.0);

            recoveryWeaknessSeconds = builder
                    .comment("Weakness effect duration after waking up.")
                    .defineInRange("recoveryWeaknessSeconds", 30, 0, 300);

            knockoutGraceSeconds = builder
                    .comment("Seconds after waking up when the next lethal hit causes full death instead of another knockout.")
                    .defineInRange("knockoutGraceSeconds", 18, 0, 120);

            exhaustionPenaltyPerStack = builder
                    .comment("Self-recovery chance penalty per recent knockout stack.")
                    .defineInRange("exhaustionPenaltyPerStack", 0.05, 0.0, 0.5);

            maxExhaustionStacks = builder
                    .comment("Maximum knockout exhaustion stacks tracked per player.")
                    .defineInRange("maxExhaustionStacks", 5, 0, 20);

            finishHitsRequired = builder
                    .comment("Melee hits required from another player to finish a knocked-out target.")
                    .defineInRange("finishHitsRequired", 4, 1, 20);

            allowHotbarLoot = builder
                    .comment("If false, looters cannot take items from the knocked-out hotbar.")
                    .define("allowHotbarLoot", true);

            knockoutCreativePlayers = builder
                    .comment("If true, creative/spectator players can also be knocked out.")
                    .define("knockoutCreativePlayers", false);

            environmentalDamageMultiplier = builder
                    .comment("Multiplier applied to bleed-out timer when taking environmental damage while down.")
                    .defineInRange("environmentalDamageMultiplier", 1.5, 1.0, 5.0);

            builder.pop();
        }
    }
}
