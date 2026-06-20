package com.knockoutmod.knockout;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public final class KnockoutData {
    private static final String ROOT = "knockoutmod";

    private static final String KNOCKED_OUT = "knockedOut";
    private static final String TICKS = "knockoutTicks";
    private static final String POSE = "knockoutPose";
    private static final String EXHAUSTION = "exhaustionStacks";
    private static final String GRACE = "graceTicks";
    private static final String FINISH_HITS = "finishHits";
    private static final String ALLY_REVIVE = "allyReviveProgress";
    private static final String SELF_REVIVE = "selfReviveProgress";
    private static final String BYPASS = "bypassKnockout";

    private KnockoutData() {
    }

    public static CompoundTag root(Player player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(ROOT)) {
            persistent.put(ROOT, new CompoundTag());
        }
        return persistent.getCompound(ROOT);
    }

    public static boolean isKnockedOut(Player player) {
        return root(player).getBoolean(KNOCKED_OUT);
    }

    public static void setKnockedOut(Player player, boolean knockedOut) {
        root(player).putBoolean(KNOCKED_OUT, knockedOut);
    }

    public static int getKnockoutTicks(Player player) {
        return root(player).getInt(TICKS);
    }

    public static void setKnockoutTicks(Player player, int ticks) {
        root(player).putInt(TICKS, Math.max(0, ticks));
    }

    public static KnockoutPose getPose(Player player) {
        return KnockoutPose.fromId(root(player).getInt(POSE));
    }

    public static void setPose(Player player, KnockoutPose pose) {
        root(player).putInt(POSE, pose.ordinal());
    }

    public static int getExhaustionStacks(Player player) {
        return root(player).getInt(EXHAUSTION);
    }

    public static void setExhaustionStacks(Player player, int stacks) {
        root(player).putInt(EXHAUSTION, Math.max(0, stacks));
    }

    public static int getGraceTicks(Player player) {
        return root(player).getInt(GRACE);
    }

    public static void setGraceTicks(Player player, int ticks) {
        root(player).putInt(GRACE, Math.max(0, ticks));
    }

    public static boolean isInKnockoutGrace(Player player) {
        return getGraceTicks(player) > 0;
    }

    public static int getFinishHits(Player player) {
        return root(player).getInt(FINISH_HITS);
    }

    public static void setFinishHits(Player player, int hits) {
        root(player).putInt(FINISH_HITS, Math.max(0, hits));
    }

    public static int getAllyReviveProgress(Player player) {
        return root(player).getInt(ALLY_REVIVE);
    }

    public static void setAllyReviveProgress(Player player, int progress) {
        root(player).putInt(ALLY_REVIVE, Math.max(0, progress));
    }

    public static int getSelfReviveProgress(Player player) {
        return root(player).getInt(SELF_REVIVE);
    }

    public static void setSelfReviveProgress(Player player, int progress) {
        root(player).putInt(SELF_REVIVE, Math.max(0, progress));
    }

    public static boolean shouldBypassKnockout(Player player) {
        return root(player).getBoolean(BYPASS);
    }

    public static void setBypassKnockout(Player player, boolean bypass) {
        root(player).putBoolean(BYPASS, bypass);
    }

    public static void clearKnockoutState(Player player) {
        CompoundTag tag = root(player);
        tag.putBoolean(KNOCKED_OUT, false);
        tag.putInt(TICKS, 0);
        tag.putInt(FINISH_HITS, 0);
        tag.putInt(ALLY_REVIVE, 0);
        tag.putInt(SELF_REVIVE, 0);
        tag.putBoolean(BYPASS, false);
    }

    public static void copyFrom(Player target, Player source) {
        CompoundTag sourceRoot = source.getPersistentData().getCompound(ROOT);
        if (sourceRoot.isEmpty()) {
            return;
        }
        target.getPersistentData().put(ROOT, sourceRoot.copy());
    }
}
