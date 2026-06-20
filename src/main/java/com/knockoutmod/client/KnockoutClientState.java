package com.knockoutmod.client;

import com.knockoutmod.entity.KnockoutTestDummyEntity;
import com.knockoutmod.knockout.KnockoutData;
import com.knockoutmod.knockout.KnockoutPose;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class KnockoutClientState {
    private static final Map<Integer, Entry> STATES = new ConcurrentHashMap<>();
    private static final Map<Integer, Integer> KNOCKOUT_START_TICKS = new ConcurrentHashMap<>();

    private KnockoutClientState() {
    }

    public static void set(Player player, boolean knockedOut, KnockoutPose pose) {
        if (knockedOut) {
            if (!isKnockedOut(player)) {
                KNOCKOUT_START_TICKS.put(player.getId(), player.tickCount);
            }
            STATES.put(player.getId(), new Entry(true, pose));
            KnockoutData.setKnockedOut(player, true);
            KnockoutData.setPose(player, pose);
            return;
        }
        clear(player);
    }

    public static void clear(Player player) {
        STATES.remove(player.getId());
        KNOCKOUT_START_TICKS.remove(player.getId());
        SelfReviveClientState.clear();
        KnockoutData.setKnockedOut(player, false);
    }

    public static void noteKnockoutStart(LivingEntity entity) {
        KNOCKOUT_START_TICKS.putIfAbsent(entity.getId(), entity.tickCount);
    }

    public static int getKnockoutStartTick(LivingEntity entity) {
        return KNOCKOUT_START_TICKS.getOrDefault(entity.getId(), entity.tickCount);
    }

    public static float getLayProgress(LivingEntity entity, float partialTick) {
        int startTick = getKnockoutStartTick(entity);
        float age = entity.tickCount + partialTick - startTick;
        float raw = Math.min(1.0F, Math.max(0.0F, age / 14.0F));
        return 1.0F - (1.0F - raw) * (1.0F - raw);
    }

    public static boolean isKnockedOut(Player player) {
        Entry entry = STATES.get(player.getId());
        if (entry != null) {
            return entry.knockedOut();
        }
        return KnockoutData.isKnockedOut(player);
    }

    public static boolean isReviveTarget(Player player) {
        Entry entry = STATES.get(player.getId());
        return entry != null && entry.knockedOut();
    }

    public static KnockoutPose getPose(Player player) {
        Entry entry = STATES.get(player.getId());
        if (entry != null) {
            return entry.pose();
        }
        return KnockoutData.getPose(player);
    }

    public static boolean isKnockedOutLying(LivingEntity entity) {
        if (entity instanceof Player player) {
            return isKnockedOut(player) && getPose(player) == KnockoutPose.LYING;
        }
        if (entity instanceof KnockoutTestDummyEntity dummy) {
            return dummy.isKnockedOut() && dummy.getKnockoutPose() == KnockoutPose.LYING;
        }
        return false;
    }

    private record Entry(boolean knockedOut, KnockoutPose pose) {
    }
}
