package com.knockoutmod.event;

import com.knockoutmod.command.KnockoutCommands;
import com.knockoutmod.entity.KnockoutTestDummyEntity;
import com.knockoutmod.knockout.AllyReviveHandler;
import com.knockoutmod.knockout.KnockoutData;
import com.knockoutmod.knockout.KnockoutHandler;
import com.knockoutmod.knockout.KnockoutHitboxHelper;
import com.knockoutmod.knockout.KnockoutMobPacifier;
import com.knockoutmod.knockout.KnockoutPoseApplier;
import com.knockoutmod.knockout.SelfReviveHoldTracker;
import com.knockoutmod.menu.DummyLootMenuProvider;
import com.knockoutmod.menu.KnockoutLootMenuProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;

public final class KnockoutEvents {
    private KnockoutEvents() {
    }

    @SuppressWarnings("removal") // EntityEvent.Size has no replacement on Forge 1.20.1 yet.
    @SubscribeEvent
    public static void onEntitySize(EntityEvent.Size event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity) || entity.level().isClientSide()) {
            return;
        }
        if (!KnockoutHitboxHelper.isKnockedOutLying(entity)) {
            return;
        }

        event.setNewSize(KnockoutHitboxHelper.LYING_DIMENSIONS, false);
        event.setNewEyeHeight(KnockoutHitboxHelper.LYING_EYE_HEIGHT);
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        KnockoutCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onDummyInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof KnockoutTestDummyEntity dummy)) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer interactor)) {
            return;
        }

        if (interactor.isShiftKeyDown()) {
            if (!dummy.isKnockedOut()) {
                return;
            }
            event.setCanceled(true);
            return;
        }

        event.setCanceled(true);

        if (dummy.isKnockedOut()) {
            DummyLootMenuProvider.open(interactor, dummy);
            return;
        }

        dummy.attackPlayer(interactor);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (KnockoutData.shouldBypassKnockout(player)) {
            KnockoutData.setBypassKnockout(player, false);
            return;
        }
        if (KnockoutData.isKnockedOut(player)) {
            event.setCanceled(true);
            player.setHealth(1.0F);
            KnockoutHandler.maintainKnockoutState(player);
            return;
        }
        if (!KnockoutHandler.canBeKnockedOut(player)) {
            return;
        }

        event.setCanceled(true);
        KnockoutHandler.enterKnockout(player, event.getSource());
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!KnockoutData.isKnockedOut(player)) {
            return;
        }

        Entity attacker = event.getSource().getEntity();
        if (attacker instanceof ServerPlayer && attacker != player) {
            event.setCanceled(true);
            return;
        }
        if (attacker instanceof LivingEntity && !(attacker instanceof ServerPlayer)) {
            event.setCanceled(true);
            if (attacker instanceof Mob mob) {
                KnockoutMobPacifier.releaseTarget(mob, player);
            }
        }
    }

    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        LivingEntity newTarget = event.getNewTarget();
        if (newTarget instanceof ServerPlayer player && KnockoutData.isKnockedOut(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!KnockoutData.isKnockedOut(player)) {
            return;
        }

        Entity attacker = event.getSource().getEntity();
        if (attacker instanceof ServerPlayer && attacker != player) {
            event.setCanceled(true);
            return;
        }
        if (attacker instanceof LivingEntity && !(attacker instanceof ServerPlayer)) {
            event.setCanceled(true);
            if (attacker instanceof Mob mob) {
                KnockoutMobPacifier.releaseTarget(mob, player);
            }
            return;
        }

        KnockoutHandler.onKnockedOutDamage(player, event.getSource(), event.getAmount());
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        if (KnockoutData.isKnockedOut(event.getEntity())) {
            event.setCanceled(true);
            return;
        }

        if (!(event.getEntity() instanceof ServerPlayer attacker)) {
            return;
        }
        if (!(event.getTarget() instanceof ServerPlayer victim)) {
            return;
        }
        if (!KnockoutData.isKnockedOut(victim) || attacker == victim) {
            return;
        }

        KnockoutHandler.onKnockedOutFinishHit(victim, attacker);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof ServerPlayer victim)) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer interactor)) {
            return;
        }
        if (!KnockoutData.isKnockedOut(victim)) {
            return;
        }

        event.setCanceled(true);

        if (interactor.isShiftKeyDown()) {
            return;
        }

        KnockoutLootMenuProvider.open(interactor, victim);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (KnockoutData.isKnockedOut(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (KnockoutData.isKnockedOut(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) {
            return;
        }
        if (!(event.player instanceof ServerPlayer player)) {
            return;
        }

        if (KnockoutData.isKnockedOut(player)) {
            KnockoutHandler.tickKnockedOut(player);
            return;
        }

        KnockoutHandler.tickGrace(player);

        AllyReviveHandler.tick(player);
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            KnockoutData.clearKnockoutState(event.getEntity());
            KnockoutPoseApplier.clearServer(event.getEntity());
            if (event.getEntity() instanceof ServerPlayer player) {
                KnockoutHandler.syncKnockout(player);
            }
            return;
        }
        KnockoutData.copyFrom(event.getEntity(), event.getOriginal());
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        KnockoutData.clearKnockoutState(player);
        KnockoutPoseApplier.clearServer(player);
        KnockoutHandler.syncKnockout(player);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            AllyReviveHandler.clearAllFor(player);
            SelfReviveHoldTracker.clear(player);
        }
    }

    @SubscribeEvent
    public static void onJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            KnockoutHandler.syncKnockout(player);
        }
    }
}
