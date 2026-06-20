package com.knockoutmod.knockout;

import com.knockoutmod.config.KnockoutConfig;
import com.knockoutmod.dummy.DummyKnockoutHandler;
import com.knockoutmod.entity.KnockoutTestDummyEntity;
import com.knockoutmod.network.ModNetwork;
import com.knockoutmod.network.SyncReviveProgressPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class AllyReviveHandler {
    private static final Map<UUID, Session> SESSIONS = new HashMap<>();
    private static final int STALE_TICKS = 8;

    private AllyReviveHandler() {
    }

    public static void hold(ServerPlayer reviver, int victimEntityId) {
        ReviveTarget target = getReviveTarget(reviver, victimEntityId);
        if (target == null) {
            clearReviveProgress(reviver, victimEntityId);
            return;
        }

        if (!reviver.isShiftKeyDown()) {
            clearReviveProgress(reviver, victimEntityId);
            return;
        }

        int required = KnockoutConfig.SERVER.allyReviveHoldSeconds.get() * 20;
        Session session = SESSIONS.get(reviver.getUUID());
        if (session == null || session.victimEntityId != victimEntityId) {
            session = new Session(victimEntityId, 0, reviver.server.getTickCount());
            SESSIONS.put(reviver.getUUID(), session);
        }

        session.progress = Math.min(required, session.progress + 1);
        session.lastTick = reviver.server.getTickCount();

        syncProgress(reviver, target, session.progress, required);

        if (session.progress >= required) {
            SESSIONS.remove(reviver.getUUID());
            completeRevive(reviver, target);
            syncProgress(reviver, target, 0, required, false);
        }
    }

    public static void tick(ServerPlayer reviver) {
        Session session = SESSIONS.get(reviver.getUUID());
        if (session == null) {
            return;
        }

        long now = reviver.server.getTickCount();
        if (now - session.lastTick <= STALE_TICKS) {
            return;
        }

        clear(reviver);
        syncInactive(reviver, session.victimEntityId);
    }

    public static void clear(ServerPlayer reviver) {
        SESSIONS.remove(reviver.getUUID());
    }

    private static void clearReviveProgress(ServerPlayer reviver, int victimEntityId) {
        boolean hadSession = SESSIONS.remove(reviver.getUUID()) != null;
        if (hadSession || victimEntityId >= 0) {
            syncInactive(reviver, victimEntityId);
        }
    }

    private static void syncInactive(ServerPlayer reviver, int victimEntityId) {
        ModNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> reviver),
                new SyncReviveProgressPacket(
                        victimEntityId,
                        0,
                        KnockoutConfig.SERVER.allyReviveHoldSeconds.get() * 20,
                        false,
                        Component.empty()
                )
        );
    }

    public static void clearAllFor(ServerPlayer player) {
        SESSIONS.remove(player.getUUID());
    }

    private static void completeRevive(ServerPlayer reviver, ReviveTarget target) {
        if (target.player() != null) {
            KnockoutHandler.wakeUp(target.player(), reviver, true);
            return;
        }
        if (target.dummy() != null) {
            DummyKnockoutHandler.wakeUp(target.dummy());
            reviver.displayClientMessage(
                    Component.translatable("knockoutmod.command.dummy_woken", target.dummy().getDummyName()),
                    true
            );
        }
    }

    private static ReviveTarget getReviveTarget(ServerPlayer reviver, int victimEntityId) {
        Entity entity = reviver.level().getEntity(victimEntityId);
        if (entity == null || reviver.distanceToSqr(entity) > 9.0D) {
            return null;
        }
        if (entity instanceof ServerPlayer victim && KnockoutData.isKnockedOut(victim) && reviver != victim) {
            return new ReviveTarget(victim, null, victim.getDisplayName());
        }
        if (entity instanceof KnockoutTestDummyEntity dummy && dummy.isKnockedOut()) {
            return new ReviveTarget(null, dummy, dummy.getDisplayName());
        }
        return null;
    }

    private static void syncProgress(ServerPlayer reviver, ReviveTarget target, int progress, int required) {
        syncProgress(reviver, target, progress, required, progress > 0);
    }

    private static void syncProgress(
            ServerPlayer reviver,
            ReviveTarget target,
            int progress,
            int required,
            boolean active
    ) {
        ModNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> reviver),
                new SyncReviveProgressPacket(
                        target.entityId(),
                        progress,
                        required,
                        active,
                        target.displayName()
                )
        );
    }

    private record ReviveTarget(ServerPlayer player, KnockoutTestDummyEntity dummy, Component displayName) {
        private int entityId() {
            if (player != null) {
                return player.getId();
            }
            return dummy.getId();
        }
    }

    private static final class Session {
        private final int victimEntityId;
        private int progress;
        private long lastTick;

        private Session(int victimEntityId, int progress, long lastTick) {
            this.victimEntityId = victimEntityId;
            this.progress = progress;
            this.lastTick = lastTick;
        }
    }
}
