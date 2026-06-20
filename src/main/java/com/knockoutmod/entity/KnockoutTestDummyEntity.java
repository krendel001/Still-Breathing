package com.knockoutmod.entity;

import com.knockoutmod.dummy.DummyKnockoutHandler;
import com.knockoutmod.init.ModEntities;
import com.knockoutmod.knockout.KnockoutPose;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class KnockoutTestDummyEntity extends Mob {
    private static final EntityDataAccessor<Boolean> KNOCKED_OUT =
            SynchedEntityData.defineId(KnockoutTestDummyEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> KNOCKOUT_POSE =
            SynchedEntityData.defineId(KnockoutTestDummyEntity.class, EntityDataSerializers.INT);

    private final SimpleContainer lootContainer = new SimpleContainer(27);
    private UUID skinUuid = UUID.randomUUID();
    private String dummyName = "Knockout Dummy";

    public KnockoutTestDummyEntity(EntityType<? extends Mob> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(KNOCKED_OUT, false);
        this.entityData.define(KNOCKOUT_POSE, KnockoutPose.LYING.ordinal());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, ServerPlayer.class, 8.0F));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && isKnockedOut()) {
            DummyKnockoutHandler.tickKnockedOut(this);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide && !isKnockedOut() && this.getHealth() - amount <= 0.0F) {
            DummyKnockoutHandler.enterKnockout(this, source);
            return true;
        }
        if (!this.level().isClientSide && isKnockedOut()) {
            DummyKnockoutHandler.onKnockedOutDamage(this, source, amount);
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean isPushable() {
        return !isKnockedOut() && super.isPushable();
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    public SimpleContainer getLootContainer() {
        return lootContainer;
    }

    public boolean isKnockedOut() {
        return this.entityData.get(KNOCKED_OUT);
    }

    public void setKnockedOut(boolean knockedOut) {
        this.entityData.set(KNOCKED_OUT, knockedOut);
    }

    public KnockoutPose getKnockoutPose() {
        return KnockoutPose.fromId(this.entityData.get(KNOCKOUT_POSE));
    }

    public void setKnockoutPose(KnockoutPose pose) {
        this.entityData.set(KNOCKOUT_POSE, pose.ordinal());
    }

    public UUID getSkinUuid() {
        return skinUuid;
    }

    public void setSkinUuid(UUID skinUuid) {
        this.skinUuid = skinUuid;
    }

    public String getDummyName() {
        return dummyName;
    }

    public void setDummyName(String dummyName) {
        this.dummyName = dummyName;
        this.setCustomName(net.minecraft.network.chat.Component.literal(dummyName));
        this.setCustomNameVisible(true);
    }

    public void equipTestGear() {
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
        this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
        this.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
    }

    public void fillTestLoot() {
        lootContainer.setItem(0, new ItemStack(Items.BREAD, 8));
        lootContainer.setItem(1, new ItemStack(Items.GOLDEN_APPLE, 2));
        lootContainer.setItem(2, new ItemStack(Items.ARROW, 32));
        lootContainer.setItem(3, new ItemStack(Items.IRON_INGOT, 16));
        lootContainer.setItem(4, new ItemStack(Items.EMERALD, 4));
        lootContainer.setItem(5, new ItemStack(Items.TORCH, 16));
    }

    public void attackPlayer(ServerPlayer target) {
        if (this.level().isClientSide || isKnockedOut()) {
            return;
        }
        this.lookAt(target, 360.0F, 360.0F);
        target.hurt(this.damageSources().mobAttack(this), 1000.0F);
        this.swing(InteractionHand.MAIN_HAND);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("KnockedOut", isKnockedOut());
        tag.putInt("KnockoutPose", getKnockoutPose().ordinal());
        tag.putUUID("SkinUuid", skinUuid);
        tag.putString("DummyName", dummyName);
        tag.putInt("KnockoutTicks", DummyKnockoutHandler.getKnockoutTicks(this));

        tag.put("LootContainer", lootContainer.createTag());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setKnockedOut(tag.getBoolean("KnockedOut"));
        setKnockoutPose(KnockoutPose.fromId(tag.getInt("KnockoutPose")));
        if (tag.hasUUID("SkinUuid")) {
            skinUuid = tag.getUUID("SkinUuid");
        }
        if (tag.contains("DummyName")) {
            setDummyName(tag.getString("DummyName"));
        }
        DummyKnockoutHandler.setKnockoutTicks(this, tag.getInt("KnockoutTicks"));
        if (tag.contains("LootContainer", Tag.TAG_LIST)) {
            lootContainer.fromTag(tag.getList("LootContainer", Tag.TAG_COMPOUND));
        }
        if (isKnockedOut()) {
            DummyKnockoutHandler.maintainKnockoutState(this);
        }
    }

    public static KnockoutTestDummyEntity spawn(ServerLevel level, ServerPlayer owner, String name) {
        return spawn(level, owner, name, 0);
    }

    public static KnockoutTestDummyEntity spawn(ServerLevel level, ServerPlayer owner, String name, int index) {
        KnockoutTestDummyEntity dummy = ModEntities.TEST_DUMMY.get().create(level);
        if (dummy == null) {
            return null;
        }

        var look = owner.getLookAngle();
        double spread = index * 1.2D;
        double side = (index % 2 == 0 ? -1.0D : 1.0D) * spread * 0.35D;
        dummy.moveTo(
                owner.getX() + look.x * (2.5D + spread * 0.15D) - look.z * side,
                owner.getY(),
                owner.getZ() + look.z * (2.5D + spread * 0.15D) + look.x * side,
                owner.getYRot() + 180.0F,
                0.0F
        );
        dummy.setSkinUuid(UUID.randomUUID());
        dummy.setDummyName(formatDummyName(name, index));
        dummy.equipTestGear();
        dummy.fillTestLoot();
        dummy.setHealth(dummy.getMaxHealth());
        level.addFreshEntity(dummy);
        return dummy;
    }

    public static List<KnockoutTestDummyEntity> spawnMany(ServerLevel level, ServerPlayer owner, String baseName, int count) {
        List<KnockoutTestDummyEntity> spawned = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            KnockoutTestDummyEntity dummy = spawn(level, owner, baseName, i);
            if (dummy != null) {
                spawned.add(dummy);
            }
        }
        return spawned;
    }

    private static String formatDummyName(String baseName, int index) {
        if (index <= 0) {
            return baseName;
        }
        return baseName + " #" + (index + 1);
    }
}
