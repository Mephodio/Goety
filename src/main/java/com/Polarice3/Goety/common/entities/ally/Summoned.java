package com.Polarice3.Goety.common.entities.ally;

import com.Polarice3.Goety.MobsConfig;
import com.Polarice3.Goety.api.entities.IOwned;
import com.Polarice3.Goety.api.entities.ally.IServant;
import com.Polarice3.Goety.client.particles.ModParticleTypes;
import com.Polarice3.Goety.common.entities.ai.SummonTargetGoal;
import com.Polarice3.Goety.common.entities.neutral.Owned;
import com.Polarice3.Goety.utils.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.UUID;
import java.util.function.Predicate;

public class Summoned extends Owned implements IServant {
    protected static final EntityDataAccessor<Byte> SUMMONED_FLAGS = SynchedEntityData.defineId(Summoned.class, EntityDataSerializers.BYTE);
    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("9c47949c-b896-4802-8e8a-f08c50791a8a");
    private static final AttributeModifier SPEED_MODIFIER = new AttributeModifier(SPEED_MODIFIER_UUID, "Staying speed penalty", -1.0D, AttributeModifier.Operation.ADDITION);
    public boolean upgraded;
    public LivingEntity commandPosEntity;
    public BlockPos commandPos;
    public int commandTick;

    protected Summoned(EntityType<? extends Owned> type, Level worldIn) {
        super(type, worldIn);
    }

    protected void registerGoals() {
        super.registerGoals();
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.followGoal();
        this.targetSelectGoal();
    }

    public void followGoal(){
        this.goalSelector.addGoal(8, new FollowOwnerGoal(this, 1.0D, 10.0F, 2.0F));
    }

    public void targetSelectGoal(){
        this.targetSelector.addGoal(1, new SummonTargetGoal(this));
    }

    public void checkDespawn() {
        if (this.isHostile()){
            super.checkDespawn();
        }
    }

    public ItemStack getProjectile(ItemStack pShootable) {
        if (pShootable.getItem() instanceof ProjectileWeaponItem) {
            Predicate<ItemStack> predicate = ((ProjectileWeaponItem)pShootable.getItem()).getSupportedHeldProjectiles();
            ItemStack itemstack = ProjectileWeaponItem.getHeldProjectile(this, predicate);
            return itemstack.isEmpty() ? new ItemStack(Items.ARROW) : itemstack;
        } else {
            return ItemStack.EMPTY;
        }
    }

    public void tick(){
        super.tick();
        AttributeInstance modifiableattributeinstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (modifiableattributeinstance != null) {
            if (this.isStaying()){
                if (this.navigation.getPath() != null) {
                    this.navigation.stop();
                }
                if (this.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
                    modifiableattributeinstance.removeModifier(SPEED_MODIFIER);
                    modifiableattributeinstance.addTransientModifier(SPEED_MODIFIER);
                }
                this.stayingPosition();
                if (this.isWandering()) {
                    this.setWandering(false);
                }
            } else {
                if (modifiableattributeinstance.hasModifier(SPEED_MODIFIER)) {
                    modifiableattributeinstance.removeModifier(SPEED_MODIFIER);
                }
            }
        }
        if (this.isCommanded()){
            if (this.getNavigation().isStableDestination(this.commandPos) || this.commandPosEntity != null){
                --this.commandTick;
                if (this.commandPosEntity != null){
                    this.getNavigation().moveTo(this.commandPosEntity, 1.25D);
                } else {
                    this.getNavigation().moveTo(this.commandPos.getX() + 0.5D, this.commandPos.getY(), this.commandPos.getZ() + 0.5D, 1.25D);
                }

                if (this.getNavigation().isStuck() || this.commandTick <= 0){
                    this.commandPosEntity = null;
                    this.commandPos = null;
                } else if (this.commandPos.closerToCenterThan(
                        this.getVehicle() != null ? this.getVehicle().position() : this.position(),
                        this.getVehicle() != null ? this.getVehicle().getBbWidth() + 1.0D : this.getBbWidth() + 1.0D)){
                    if (this.commandPosEntity != null &&
                            this.getBoundingBox().inflate(1.25D).intersects(this.commandPosEntity.getBoundingBox())){
                        if (this.canRide(this.commandPosEntity)) {
                            if (this.startRiding(this.commandPosEntity)) {
                                if (this.getTrueOwner() instanceof Player player){
                                    player.displayClientMessage(Component.translatable("info.goety.servant.dismount"), true);
                                }
                            }
                        }
                        this.commandPosEntity = null;
                    }
                    this.moveTo(this.commandPos, this.getYRot(), this.getXRot());
                    this.commandPos = null;
                }
            } else {
                this.commandPos = null;
            }
        }
        if (this.isWandering()){
            if (this.isStaying()) {
                this.setStaying(false);
            }
        }
        if (this.getTrueOwner() != null){
            if (CuriosFinder.hasUndeadCrown(this.getTrueOwner()) && this.getMobType() == MobType.UNDEAD){
                this.limitedLifespan = false;
            } else if (this.limitedLifeTicks > 0){
                this.limitedLifespan = true;
            }
            if (!this.level.isClientSide) {
                if (this.getMobType() == MobType.UNDEAD) {
                    if (!this.isOnFire() && !this.isDeadOrDying() && (!this.limitedLifespan || this.limitedLifeTicks > 20)) {
                        if (MobsConfig.UndeadMinionHeal.get() && this.getHealth() < this.getMaxHealth()) {
                            if (this.getTrueOwner() instanceof Player owner) {
                                if (CuriosFinder.hasUndeadCape(owner)) {
                                    int SoulCost = MobsConfig.UndeadMinionHealCost.get();
                                    if (SEHelper.getSoulsAmount(owner, SoulCost)) {
                                        if (this.tickCount % MathHelper.secondsToTicks(MobsConfig.UndeadMinionHealTime.get()) == 0) {
                                            this.heal(MobsConfig.UndeadMinionHealAmount.get().floatValue());
                                            Vec3 vector3d = this.getDeltaMovement();
                                            if (this.level instanceof ServerLevel serverWorld) {
                                                SEHelper.decreaseSouls(owner, SoulCost);
                                                serverWorld.sendParticles(ParticleTypes.SCULK_SOUL, this.getRandomX(0.5D), this.getRandomY(), this.getRandomZ(0.5D), 0, vector3d.x * -0.2D, 0.1D, vector3d.z * -0.2D, 0.5F);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        boolean flag = this.isSunSensitive() && this.isSunBurnTick();
        if (flag) {
            ItemStack itemstack = this.getItemBySlot(EquipmentSlot.HEAD);
            if (!itemstack.isEmpty()) {
                if (itemstack.isDamageableItem()) {
                    itemstack.setDamageValue(itemstack.getDamageValue() + this.random.nextInt(2));
                    if (itemstack.getDamageValue() >= itemstack.getMaxDamage()) {
                        this.broadcastBreakEvent(EquipmentSlot.HEAD);
                        this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                    }
                }

                flag = false;
            }

            if (flag) {
                this.setSecondsOnFire(8);
            }
        }
    }

    public boolean canRide(LivingEntity livingEntity){
        if (!(this instanceof PlayerRideable)
                && !(this instanceof AbstractGolemServant)
                && livingEntity instanceof PlayerRideable
                && livingEntity.getFirstPassenger() == null){
            if (livingEntity instanceof AbstractHorse horse){
                return horse.isTamed();
            } else if (livingEntity instanceof OwnableEntity ownable && this.getTrueOwner() != null){
                return ownable.getOwner() == this.getTrueOwner();
            } else if (livingEntity instanceof IOwned owned && this.getTrueOwner() != null){
                return owned.getTrueOwner() == this.getTrueOwner();
            }
        }
        return false;
    }

    public void stayingPosition(){
        if (this.getTarget() != null){
            this.getLookControl().setLookAt(this.getTarget(), this.getMaxHeadYRot(), this.getMaxHeadXRot());
            double d2 = this.getTarget().getX() - this.getX();
            double d1 = this.getTarget().getZ() - this.getZ();
            this.setYRot(-((float) Mth.atan2(d2, d1)) * (180F / (float) Math.PI));
            this.yBodyRot = this.getYRot();
        }
    }

    protected boolean isSunSensitive() {
        return false;
    }

    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        pSpawnData = super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
        if (pReason == MobSpawnType.MOB_SUMMONED && this.getTrueOwner() != null && this.getMobType() == MobType.UNDEAD){
            for (int i = 0; i < pLevel.getLevel().random.nextInt(10) + 10; ++i) {
                pLevel.getLevel().sendParticles(ModParticleTypes.SUMMON.get(), this.getRandomX(1.5D), this.getRandomY(), this.getRandomZ(1.5D), 0, 0.0F, 1.0F, 0.0F, 1.0F);
            }
            pLevel.getLevel().sendParticles(ModParticleTypes.SOUL_EXPLODE.get(), this.getX(), this.getY(), this.getZ(), 0, 0, 2.0D, 0, 1.0F);
        }
        this.setWandering(false);
        this.setStaying(false);
        return pSpawnData;
    }

    public void die(DamageSource pCause) {
        if (!this.level.isClientSide && this.hasCustomName() && this.level.getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES) && this.getTrueOwner() instanceof ServerPlayer) {
            this.getTrueOwner().sendSystemMessage(this.getCombatTracker().getDeathMessage());
        }
        super.die(pCause);
    }

    public boolean hurt(DamageSource source, float amount) {
        if (MobsConfig.MinionsMasterImmune.get()) {
            if (source.getEntity() instanceof Summoned summoned) {
                if (!summoned.isHostile() && !this.isHostile()) {
                    if (this.getTrueOwner() != null && summoned.getTrueOwner() == this.getTrueOwner()) {
                        return false;
                    }
                }
            }
        }
        return super.hurt(source, amount);
    }

    public boolean doHurtTarget(Entity entityIn) {
        boolean flag = super.doHurtTarget(entityIn);
        if (flag) {
            if (this.getMobType() == MobType.UNDEAD){
                float f = this.level.getCurrentDifficultyAt(this.blockPosition()).getEffectiveDifficulty();
                if (this.getMainHandItem().isEmpty() && this.isOnFire() && this.random.nextFloat() < f * 0.3F) {
                    entityIn.setSecondsOnFire(2 * (int)f);
                }
            }
            if (!this.getMainHandItem().isEmpty() && this.getMainHandItem().isDamageableItem()){
                ItemHelper.hurtAndBreak(this.getMainHandItem(), 1, this);
            }
        }

        return flag;
    }

    protected void hurtArmor(DamageSource pDamageSource, float pDamage) {
        if (!(pDamage <= 0.0F)) {
            pDamage = pDamage / 4.0F;
            if (pDamage < 1.0F) {
                pDamage = 1.0F;
            }

            for(EquipmentSlot equipmentSlotType : EquipmentSlot.values()) {
                if (equipmentSlotType.getType() == EquipmentSlot.Type.ARMOR) {
                    ItemStack itemstack = this.getItemBySlot(equipmentSlotType);
                    if ((!pDamageSource.isFire() || !itemstack.getItem().isFireResistant()) && itemstack.getItem() instanceof ArmorItem) {
                        itemstack.hurtAndBreak((int) pDamage, this, (p_214023_1_) -> {
                            p_214023_1_.broadcastBreakEvent(equipmentSlotType);
                        });
                    }
                }
            }

        }
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SUMMONED_FLAGS, (byte)0);
    }

    private boolean getFlag(int mask) {
        int i = this.entityData.get(SUMMONED_FLAGS);
        return (i & mask) != 0;
    }

    private void setFlags(int mask, boolean value) {
        int i = this.entityData.get(SUMMONED_FLAGS);
        if (value) {
            i = i | mask;
        } else {
            i = i & ~mask;
        }

        this.entityData.set(SUMMONED_FLAGS, (byte)(i & 255));
    }

    public boolean isWandering() {
        return this.getFlag(1);
    }

    public void setWandering(boolean wandering) {
        this.setFlags(1, wandering);
    }

    public boolean isStaying(){
        return this.getFlag(2) && !this.isCommanded() && !this.isVehicle();
    }

    public void setStaying(boolean staying){
        this.setFlags(2, staying);
    }

    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setUpgraded(compound.getBoolean("Upgraded"));
        if (compound.contains("wandering")) {
            this.setWandering(compound.getBoolean("wandering"));
        }
        if (compound.contains("staying")) {
            this.setStaying(compound.getBoolean("staying"));
        }
        if (compound.contains("commandPos")){
            this.commandPos = NbtUtils.readBlockPos(compound.getCompound("commandPos"));
        }
        if (compound.contains("commandPosEntity")){
            if (EntityFinder.getLivingEntityByUuiD(compound.getUUID("commandPosEntity")) != null) {
                this.commandPosEntity = EntityFinder.getLivingEntityByUuiD(compound.getUUID("commandPosEntity"));
            }
        }
        if (compound.contains("commandTick")){
            this.commandTick = compound.getInt("commandTick");
        }
    }

    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("Upgraded", this.upgraded);
        compound.putBoolean("wandering", this.isWandering());
        compound.putBoolean("staying", this.isStaying());
        if (this.commandPos != null){
            compound.put("commandPos", NbtUtils.writeBlockPos(this.commandPos));
        }
        if (this.commandPosEntity != null) {
            compound.putUUID("commandPosEntity", this.commandPosEntity.getUUID());
        }
        compound.putInt("commandTick", this.commandTick);
    }

    public boolean canUpdateMove(){
        return this.getMobType() == MobType.UNDEAD;
    }

    public void updateMoveMode(Player player){
        if (!this.isWandering() && !this.isStaying()){
            this.setWandering(true);
            this.setStaying(false);
            player.displayClientMessage(Component.translatable("info.goety.servant.wander", this.getDisplayName()), true);
        } else if (!this.isStaying()){
            this.setWandering(false);
            this.setStaying(true);
            player.displayClientMessage(Component.translatable("info.goety.servant.staying", this.getDisplayName()), true);
        } else {
            this.setWandering(false);
            this.setStaying(false);
            player.displayClientMessage(Component.translatable("info.goety.servant.follow", this.getDisplayName()), true);
        }
        this.playSound(SoundEvents.ZOMBIE_VILLAGER_CONVERTED, 1.0f, 1.0f);

    }

    public boolean isUpgraded() {
        return this.upgraded;
    }

    public void setUpgraded(boolean upgraded) {
        this.upgraded = upgraded;
    }

    public void setCommandPos(BlockPos blockPos){
        this.setCommandPos(blockPos, true);
    }

    public void setCommandPos(BlockPos blockPos, boolean removeEntity){
        if (removeEntity) {
            this.commandPosEntity = null;
        }
        this.commandPos = blockPos;
        this.commandTick = MathHelper.secondsToTicks(10);
    }

    public void setCommandPosEntity(LivingEntity living){
        this.commandPosEntity = living;
        this.setCommandPos(living.blockPosition(), false);
    }

    public boolean isCommanded(){
        return this.commandPos != null;
    }

    public void dropEquipment(EquipmentSlot equipmentSlot, ItemStack stack){
        if (this.getEquipmentDropChance(equipmentSlot) > 0.0F) {
            this.spawnAtLocation(stack);
        }
    }

    public static class FollowOwnerGoal extends Goal {
        private final Summoned summonedEntity;
        private LivingEntity owner;
        private final LevelReader level;
        private final double followSpeed;
        private final PathNavigation navigation;
        private int timeToRecalcPath;
        private final float stopDistance;
        private final float startDistance;
        private float oldWaterCost;

        public FollowOwnerGoal(Summoned summonedEntity, double speed, float startDistance, float stopDistance) {
            this.summonedEntity = summonedEntity;
            this.level = summonedEntity.level;
            this.followSpeed = speed;
            this.navigation = summonedEntity.getNavigation();
            this.startDistance = startDistance;
            this.stopDistance = stopDistance;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
            if (!(summonedEntity.getNavigation() instanceof GroundPathNavigation) && !(summonedEntity.getNavigation() instanceof FlyingPathNavigation)) {
                throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
            }
        }

        public boolean canUse() {
            LivingEntity livingentity = this.summonedEntity.getTrueOwner();
            if (livingentity == null) {
                return false;
            } else if (livingentity.isSpectator()) {
                return false;
            } else if (this.summonedEntity.distanceToSqr(livingentity) < (double)(Mth.square(this.startDistance))) {
                return false;
            } else if (this.summonedEntity.isWandering() || this.summonedEntity.isStaying() || this.summonedEntity.isCommanded()) {
                return false;
            } else if (this.summonedEntity.getTarget() != null) {
                return false;
            } else {
                this.owner = livingentity;
                return true;
            }
        }

        public boolean canContinueToUse() {
            if (this.navigation.isDone()) {
                return false;
            } else if (this.summonedEntity.getTarget() != null){
                return false;
            } else {
                return !(this.summonedEntity.distanceToSqr(this.owner) <= (double)(Mth.square(this.stopDistance)));
            }
        }

        public void start() {
            this.timeToRecalcPath = 0;
            this.oldWaterCost = this.summonedEntity.getPathfindingMalus(BlockPathTypes.WATER);
            this.summonedEntity.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
        }

        public void stop() {
            this.owner = null;
            this.navigation.stop();
            this.summonedEntity.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
        }

        public void tick() {
            if (this.owner != null) {
                this.summonedEntity.getLookControl().setLookAt(this.owner, 10.0F, (float) this.summonedEntity.getMaxHeadXRot());
                if (this.summonedEntity.getVehicle() != null && this.summonedEntity.getVehicle().getFirstPassenger() == this.summonedEntity){
                    this.navigation.moveTo(this.owner, this.followSpeed + 0.25D);
                    if (this.summonedEntity.getVehicle() instanceof Mob mob){
                        mob.getNavigation().moveTo(this.owner, this.followSpeed + 0.25D);
                    }
                } else if (--this.timeToRecalcPath <= 0) {
                    this.timeToRecalcPath = 10;
                    if (!this.summonedEntity.isLeashed() && !this.summonedEntity.isPassenger()) {
                        if (this.summonedEntity.distanceToSqr(this.owner) >= 144.0D && MobsConfig.UndeadTeleport.get()) {
                            this.tryToTeleportNearEntity();
                        } else {
                            this.navigation.moveTo(this.owner, this.followSpeed);
                        }
                    }
                }
            }
        }

        private void tryToTeleportNearEntity() {
            BlockPos blockpos = this.owner.blockPosition();

            for(int i = 0; i < 10; ++i) {
                int j = this.getRandomNumber(-3, 3);
                int k = this.getRandomNumber(-1, 1);
                int l = this.getRandomNumber(-3, 3);
                boolean flag = this.tryToTeleportToLocation(blockpos.getX() + j, blockpos.getY() + k, blockpos.getZ() + l);
                if (flag) {
                    return;
                }
            }

        }

        private boolean tryToTeleportToLocation(int x, int y, int z) {
            if (Math.abs((double)x - this.owner.getX()) < 2.0D && Math.abs((double)z - this.owner.getZ()) < 2.0D) {
                return false;
            } else if (!this.isTeleportFriendlyBlock(new BlockPos(x, y, z))) {
                return false;
            } else {
                this.summonedEntity.moveTo((double)x + 0.5D, (double)y, (double)z + 0.5D, this.summonedEntity.getYRot(), this.summonedEntity.getXRot());
                this.navigation.stop();
                return true;
            }
        }

        private boolean isTeleportFriendlyBlock(BlockPos pos) {
            BlockPathTypes pathnodetype = WalkNodeEvaluator.getBlockPathTypeStatic(this.level, pos.mutable());
            if (pathnodetype != BlockPathTypes.WALKABLE) {
                return false;
            } else {
                BlockState blockstate = this.level.getBlockState(pos.below());
                if (blockstate.getBlock() instanceof LeavesBlock) {
                    return false;
                } else {
                    BlockPos blockpos = pos.subtract(this.summonedEntity.blockPosition());
                    return this.level.noCollision(this.summonedEntity, this.summonedEntity.getBoundingBox().move(blockpos));
                }
            }
        }

        private int getRandomNumber(int min, int max) {
            return this.summonedEntity.getRandom().nextInt(max - min + 1) + min;
        }
    }

    public class WanderGoal extends RandomStrollGoal {
        protected final float probability;

        public WanderGoal(PathfinderMob p_i47301_1_, double p_i47301_2_) {
            this(p_i47301_1_, p_i47301_2_, 0.001F);
        }

        public WanderGoal(PathfinderMob entity, double speedModifier, float probability) {
            super(entity, speedModifier, 120, false);
            this.probability = probability;
        }

        @Nullable
        protected Vec3 getPosition() {
            if (this.mob.isInWaterOrBubble()) {
                Vec3 vec3 = LandRandomPos.getPos(this.mob, 15, 7);
                return vec3 == null ? super.getPosition() : vec3;
            } else {
                return this.mob.getRandom().nextFloat() >= this.probability ? LandRandomPos.getPos(this.mob, 10, 7) : super.getPosition();
            }
        }

        public boolean canUse() {
            if (super.canUse()){
                return !Summoned.this.isStaying() && !Summoned.this.isCommanded() || Summoned.this.getTrueOwner() == null || Summoned.this.getNavigation() instanceof WaterBoundPathNavigation;
            } else {
                return false;
            }
        }
    }
}
