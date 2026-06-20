package com.knockoutmod.client;

import com.knockoutmod.KnockoutMod;
import com.knockoutmod.entity.KnockoutTestDummyEntity;
import com.knockoutmod.knockout.KnockoutHitboxHelper;
import com.knockoutmod.knockout.KnockoutPose;
import com.knockoutmod.knockout.KnockoutPoseApplier;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = KnockoutMod.MOD_ID, value = Dist.CLIENT)
public final class KnockoutClientEvents {
    private static final int SMOKE_INTERVAL_TICKS = 16;
    private static final float LYING_LOOK_PITCH = 25.0F;

    private static CameraType savedCameraType;
    private static boolean wasKnockedOutForCamera;
    private static boolean wasKnockedOutForCursor;

    private KnockoutClientEvents() {
    }

    @SubscribeEvent
    public static void onMovement(MovementInputUpdateEvent event) {
        if (!KnockoutClientState.isKnockedOut(event.getEntity())) {
            return;
        }
        event.getInput().leftImpulse = 0.0F;
        event.getInput().forwardImpulse = 0.0F;
        event.getInput().jumping = false;
        event.getInput().shiftKeyDown = false;
        event.getEntity().setShiftKeyDown(false);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !event.player.level().isClientSide()) {
            return;
        }

        Player player = event.player;
        if (KnockoutClientState.isKnockedOut(player)) {
            KnockoutPose pose = KnockoutClientState.getPose(player);
            KnockoutPoseApplier.applyClient(player, pose);
            if (pose == KnockoutPose.LYING) {
                KnockoutHitboxHelper.maintainKnockoutHitbox(player);
            }
            return;
        }

        if (player.getForcedPose() != null) {
            KnockoutPoseApplier.clearClient(player);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (event.phase == TickEvent.Phase.START) {
            enforceKnockoutCamera(minecraft);
            return;
        }

        enforceKnockoutCursor(minecraft);
        spawnKnockoutSmoke(minecraft);
    }

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            enforceKnockoutCamera(Minecraft.getInstance());
        }
    }

    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !KnockoutClientState.isKnockedOutLying(minecraft.player)) {
            return;
        }

        float partialTick = (float) event.getPartialTick();
        float bodyYaw = Mth.rotLerp(partialTick, minecraft.player.yBodyRotO, minecraft.player.yBodyRot);
        event.setYaw(bodyYaw);
        event.setPitch(LYING_LOOK_PITCH);
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && KnockoutClientState.isKnockedOutLying(minecraft.player)) {
            event.setCanceled(true);
        }
    }

    public static void resetCameraAndCursor() {
        Minecraft minecraft = Minecraft.getInstance();
        if (wasKnockedOutForCamera && savedCameraType != null && minecraft.options != null) {
            minecraft.options.setCameraType(savedCameraType);
            savedCameraType = null;
        }
        wasKnockedOutForCamera = false;

        if (wasKnockedOutForCursor
                && minecraft.screen == null
                && minecraft.mouseHandler != null
                && !minecraft.mouseHandler.isMouseGrabbed()) {
            minecraft.mouseHandler.grabMouse();
        }
        wasKnockedOutForCursor = false;
    }

    private static void enforceKnockoutCamera(Minecraft minecraft) {
        if (minecraft.player == null) {
            return;
        }

        boolean knockedOut = KnockoutClientState.isKnockedOut(minecraft.player);
        if (knockedOut) {
            if (!wasKnockedOutForCamera) {
                savedCameraType = minecraft.options.getCameraType();
            }
            if (minecraft.options.getCameraType() != CameraType.FIRST_PERSON) {
                minecraft.options.setCameraType(CameraType.FIRST_PERSON);
            }
        } else if (wasKnockedOutForCamera && savedCameraType != null) {
            minecraft.options.setCameraType(savedCameraType);
            savedCameraType = null;
        }

        wasKnockedOutForCamera = knockedOut;
    }

    private static void enforceKnockoutCursor(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.screen != null) {
            return;
        }

        boolean knockedOut = KnockoutClientState.isKnockedOut(minecraft.player);
        if (knockedOut) {
            if (minecraft.mouseHandler.isMouseGrabbed()) {
                minecraft.mouseHandler.releaseMouse();
            }
        } else if (wasKnockedOutForCursor && !minecraft.mouseHandler.isMouseGrabbed()) {
            minecraft.mouseHandler.grabMouse();
        }

        wasKnockedOutForCursor = knockedOut;
    }

    private static void spawnKnockoutSmoke(Minecraft minecraft) {
        if (minecraft.level == null) {
            return;
        }

        for (var entity : minecraft.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living) || !KnockoutClientState.isKnockedOutLying(living)) {
                continue;
            }
            if (living instanceof KnockoutTestDummyEntity dummy) {
                dummy.setPose(Pose.STANDING);
                KnockoutHitboxHelper.maintainKnockoutHitbox(dummy);
            }
            if (living.tickCount % SMOKE_INTERVAL_TICKS != 0) {
                continue;
            }

            Vec3 anchor = KnockoutHitboxHelper.getLyingSmokePosition(living);
            RandomSource random = living.getRandom();
            double spread = 0.12D;
            minecraft.level.addParticle(
                    ParticleTypes.SMOKE,
                    anchor.x + (random.nextDouble() - 0.5D) * spread,
                    anchor.y + random.nextDouble() * 0.04D,
                    anchor.z + (random.nextDouble() - 0.5D) * spread,
                    0.0D,
                    0.006D,
                    0.0D
            );
        }
    }
}
