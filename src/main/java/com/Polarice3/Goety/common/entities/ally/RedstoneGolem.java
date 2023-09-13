package com.Polarice3.Goety.common.entities.ally;

import com.Polarice3.Goety.AttributesConfig;
import com.Polarice3.Goety.client.particles.ModParticleTypes;
import com.Polarice3.Goety.common.blocks.ModBlocks;
import com.Polarice3.Goety.common.entities.ai.SummonTargetGoal;
import com.Polarice3.Goety.common.entities.neutral.Owned;
import com.Polarice3.Goety.common.entities.projectiles.ScatterMine;
import com.Polarice3.Goety.common.items.RedstoneGolemSkullItem;
import com.Polarice3.Goety.common.items.magic.DarkWand;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.MathHelper;
import com.Polarice3.Goety.utils.MobUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class RedstoneGolem extends Summoned {
    protected static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(RedstoneGolem.class, EntityDataSerializers.BYTE);
    public static float SUMMON_SECONDS_TIME = 5.15F;
    private int idleTime;
    private int summonTick;
    private int summonCool;
    private int mineCount;
    public int attackTick;
    public int postAttackTick;
    public float getGlow;
    public float glowAmount = 0.01F;
    public int noveltyTick;
    public int deathTime = 0;
    public float deathRotation = 0.0F;
    public boolean markChanged = true;
    public boolean isNovelty = false;
    public boolean isPostAttack = false;
    public AnimationState activateAnimationState = new AnimationState();
    public AnimationState idleAnimationState = new AnimationState();
    public AnimationState noveltyAnimationState = new AnimationState();
    public AnimationState attackAnimationState = new AnimationState();
    public AnimationState walkAnimationState = new AnimationState();
    public AnimationState summonAnimationState = new AnimationState();
    public AnimationState sitAnimationState = new AnimationState();
    public AnimationState deathAnimationState = new AnimationState();

    public RedstoneGolem(EntityType<? extends Owned> type, Level worldIn) {
        super(type, worldIn);
    }

    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new SummonMinesGoal());
        this.goalSelector.addGoal(2, new MeleeGoal());
        this.goalSelector.addGoal(5, new AttackGoal(1.2D));
        this.goalSelector.addGoal(8, new WanderGoal(this, 1.0D, 10));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
    }

    public static AttributeSupplier.Builder setCustomAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, AttributesConfig.RedstoneGolemHealth.get())
                .add(Attributes.MOVEMENT_SPEED, (double)0.3F)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 3.0D)
                .add(ForgeMod.STEP_HEIGHT_ADDITION.get(), 1.0D)
                .add(Attributes.ATTACK_DAMAGE, AttributesConfig.RedstoneGolemDamage.get())
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    public AttributeSupplier.Builder getConfiguredAttributes(){
        return setCustomAttributes();
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_FLAGS_ID, (byte)0);
    }

    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putInt("SummonTick", this.summonTick);
        pCompound.putInt("CoolDown", this.summonCool);
        pCompound.putInt("MineCount", this.mineCount);
    }

    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        if (pCompound.contains("SummonTick")) {
            this.summonTick = pCompound.getInt("SummonTick");
        }
        if (pCompound.contains("CoolDown")){
            this.summonCool = pCompound.getInt("CoolDown");
        }
        if (pCompound.contains("MineCount")){
            this.mineCount = pCompound.getInt("MineCount");
        }
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        if (pReason == MobSpawnType.MOB_SUMMONED){
            this.activateAnimationState.start(this.tickCount);
            this.level.broadcastEntityEvent(this, (byte) 8);
        }
        return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
    }

    public boolean causeFallDamage(float p_148711_, float p_148712_, DamageSource p_148713_) {
        return false;
    }

    public int getAmbientSoundInterval() {
        return 120;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.REDSTONE_GOLEM_AMBIENT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource p_21239_) {
        return ModSounds.REDSTONE_GOLEM_HURT.get();
    }

    @Override
    protected void playStepSound(BlockPos p_20135_, BlockState p_20136_) {
        this.playSound(ModSounds.REDSTONE_GOLEM_STEP.get());
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.REDSTONE_GOLEM_DEATH.get();
    }

    private boolean getGolemFlag(int mask) {
        int i = this.entityData.get(DATA_FLAGS_ID);
        return (i & mask) != 0;
    }

    private void setGolemFlags(int mask, boolean value) {
        int i = this.entityData.get(DATA_FLAGS_ID);
        if (value) {
            i = i | mask;
        } else {
            i = i & ~mask;
        }

        this.entityData.set(DATA_FLAGS_ID, (byte)(i & 255));
    }

    public boolean isMeleeAttacking() {
        return this.getGolemFlag(1);
    }

    public void setMeleeAttacking(boolean attacking) {
        this.setGolemFlags(1, attacking);
        this.attackTick = 0;
        this.level.broadcastEntityEvent(this, (byte) 5);
    }

    protected int decreaseAirSupply(int p_28882_) {
        return p_28882_;
    }

    protected boolean isImmobile() {
        return super.isImmobile() || this.isSummoning();
    }

    public boolean hasLineOfSight(Entity p_149755_) {
        return this.summonTick <= 0 && super.hasLineOfSight(p_149755_);
    }

    public boolean isSummoning(){
        return this.summonTick > 0;
    }

    public boolean canBeCollidedWith() {
        return true;
    }

    protected void doPush(Entity p_28839_) {
        if (p_28839_ instanceof LivingEntity livingEntity && SummonTargetGoal.predicate(this).test(livingEntity) && this.getRandom().nextInt(20) == 0) {
            this.setTarget(livingEntity);
        }

        super.doPush(p_28839_);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    public boolean isMoving() {
        return this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6D;
    }

    public boolean isSitting(){
        return this.isStaying() && !this.isMeleeAttacking() && !this.isSummoning() && !this.isMoving();
    }

    public List<AnimationState> getAnimations(){
        List<AnimationState> animationStates = new ArrayList<>();
        animationStates.add(this.activateAnimationState);
        animationStates.add(this.attackAnimationState);
        animationStates.add(this.idleAnimationState);
        animationStates.add(this.noveltyAnimationState);
        animationStates.add(this.summonAnimationState);
        animationStates.add(this.sitAnimationState);
        animationStates.add(this.walkAnimationState);
        animationStates.add(this.deathAnimationState);
        return animationStates;
    }

    protected void tickDeath() {
        ++this.deathTime;
        if (this.deathTime >= 30) {
            this.spawnAnim();
            ItemStack itemStack = new ItemStack(ModBlocks.REDSTONE_GOLEM_SKULL_ITEM.get());
            if (this.getTrueOwner() != null){
                RedstoneGolemSkullItem.setOwner(this.getTrueOwner(), itemStack);
                if (this.getCustomName() != null){
                    RedstoneGolemSkullItem.setCustomName(this.getCustomName().getString(), itemStack);
                }
                ItemEntity itemEntity = this.spawnAtLocation(itemStack);
                if (itemEntity != null){
                    itemEntity.setExtendedLifetime();
                }
            } else if (this.level.random.nextFloat() <= 0.11F){
                this.spawnAtLocation(itemStack);
            }
            this.remove(RemovalReason.KILLED);
        }
        this.hurtTime = 1;
        this.setYRot(this.deathRotation);
        this.setYBodyRot(this.deathRotation);
    }

    public void die(DamageSource p_21014_) {
        this.level.broadcastEntityEvent(this, (byte) 7);
        this.deathRotation = this.getYRot();
        super.die(p_21014_);
    }

    public EntityDimensions getDimensions(Pose p_29531_) {
        if (this.isSitting()) {
            return super.getDimensions(p_29531_).scale(1.0F, 0.75F);
        } else {
            return super.getDimensions(p_29531_);
        }
    }

    public void stopMostAnimations(AnimationState animationState0){
        for (AnimationState animationState : this.getAnimations()){
            if (animationState != animationState0) {
                animationState.stop();
            }
        }
    }

    public void stopAnimations(){
        for (AnimationState animationState : this.getAnimations()){
            animationState.stop();
        }
    }

    public void tick() {
        super.tick();
        if (this.isDeadOrDying()){
            for (AnimationState animationState : this.getAnimations()){
                if (animationState != this.deathAnimationState) {
                    animationState.stop();
                }
            }
            this.setYRot(this.deathRotation);
            this.setYBodyRot(this.deathRotation);
        }
        //Nobody constantly calls refreshDimensions(). Only calls them once if there's certain changes. No idea why, probably for performance
        if (this.isSitting() && this.markChanged){
            this.refreshDimensions();
            this.markChanged = false;
        } else if (!this.isSitting() && !this.markChanged){
            this.refreshDimensions();
            this.markChanged = true;
        }
        if (this.level.isClientSide()) {
            if (this.isAlive()) {
                this.glow();
                if (!this.isSummoning()){
                    this.summonAnimationState.stop();
                }
                if (!this.isMeleeAttacking() && !this.isSummoning() && !this.isMoving()) {
                    this.walkAnimationState.stop();
                    if (this.isStaying()) {
                        this.sitAnimationState.startIfStopped(this.tickCount);
                    } else {
                        this.idleAnimationState.startIfStopped(this.tickCount);
                        this.sitAnimationState.stop();
                    }
                } else {
                    this.noveltyAnimationState.stop();
                    this.idleAnimationState.stop();
                    this.sitAnimationState.stop();
                }
                if (!this.isMeleeAttacking() && !this.isSummoning()) {
                    if (this.isMoving()) {
                        this.walkAnimationState.startIfStopped(this.tickCount);
                    } else {
                        this.walkAnimationState.stop();
                    }
                } else {
                    this.walkAnimationState.stop();
                }
                if (this.isMeleeAttacking()) {
                    ++this.attackTick;
                }
                if (this.summonTick > 0) {
                    --this.summonTick;
                } else {
                    this.summonAnimationState.stop();
                }
            }
        }
        if (!this.level.isClientSide){
            if (this.isAlive()) {
                if (this.isNovelty){
                    this.jumping = false;
                    this.xxa = 0.0F;
                    this.zza = 0.0F;
                    ++this.noveltyTick;
                    if (this.noveltyTick == 8 || this.noveltyTick == 13 || this.noveltyTick == 18 || this.noveltyTick == 23 || this.noveltyTick == 28 || this.noveltyTick == 33){
                        this.playSound(ModSounds.REDSTONE_GOLEM_CHEST.get());
                    }
                    if (this.noveltyTick == 42){
                        this.playSound(ModSounds.REDSTONE_GOLEM_GROWL.get());
                        this.gameEvent(GameEvent.ENTITY_ROAR, this);
                    }
                    if (this.noveltyTick >= 92 || this.getTarget() != null || this.hurtTime > 0){
                        this.isNovelty = false;
                    }
                } else {
                    this.level.broadcastEntityEvent(this, (byte) 10);
                    this.noveltyTick = 0;
                }
                if (!this.isMeleeAttacking() && !this.isSummoning() && !this.isMoving() && !this.isStaying()) {
                    ++this.idleTime;
                    if (this.level.random.nextFloat() <= 0.05F && this.hurtTime <= 0 && (this.getTarget() == null || this.getTarget().isDeadOrDying()) && !this.isNovelty && this.idleTime >= MathHelper.minutesToTicks(5)) {
                        this.idleTime = 0;
                        this.isNovelty = true;
                        this.level.broadcastEntityEvent(this, (byte) 9);
                    }
                } else {
                    this.idleTime = 0;
                    this.isNovelty = false;
                }
                if (this.summonTick > 0) {
                    --this.summonTick;
                }
                if (this.summonCool > 0) {
                    --this.summonCool;
                }
                if (this.isMeleeAttacking()) {
                    ++this.attackTick;
                }
                if (this.isPostAttack){
                    ++this.postAttackTick;
                }
                if (this.postAttackTick >= 15){
                    this.level.broadcastEntityEvent(this, (byte) 11);
                    this.postAttackTick = 0;
                    this.isPostAttack = false;
                }
                if (this.isSummoning()) {
                    if (this.level instanceof ServerLevel serverLevel) {
                        for (int i = 0; i < 5; ++i) {
                            double d0 = serverLevel.random.nextGaussian() * 0.02D;
                            double d1 = serverLevel.random.nextGaussian() * 0.02D;
                            double d2 = serverLevel.random.nextGaussian() * 0.02D;
                            serverLevel.sendParticles(ModParticleTypes.BIG_ELECTRIC.get(), this.getRandomX(0.5D), this.getEyeY() - serverLevel.random.nextInt(5), this.getRandomZ(0.5D), 0, d0, d1, d2, 0.5F);
                        }
                    }
                }
                if (this.summonTick <= (MathHelper.secondsToTicks(SUMMON_SECONDS_TIME) - 10) && this.mineCount > 0) {
                    int time = (int) (MathHelper.secondsToTicks(SUMMON_SECONDS_TIME) / 14);
                    if (this.tickCount % time == 0 && this.isOnGround()) {
                        BlockPos blockPos = this.blockPosition();
                        blockPos = blockPos.offset(-8 + this.level.random.nextInt(16), 0, -8 + this.level.random.nextInt(16));
                        ScatterMine scatterMine = new ScatterMine(this.level, this, blockPos);
                        if (this.level.addFreshEntity(scatterMine)) {
                            if (this.level.random.nextBoolean()) {
                                scatterMine.playSound(ModSounds.REDSTONE_GOLEM_MINE_SPAWN.get());
                            }
                            --this.mineCount;
                        }
                    }
                }
            }
        }
    }

    private void glow() {
        this.getGlow = Mth.clamp(this.getGlow + this.glowAmount, 0, 1);
        if (this.getGlow == 0 || this.getGlow == 1) {
            this.glowAmount *= -1;
        }
    }

    public void handleEntityEvent(byte pId) {
        if (pId == 4){
            this.stopAnimations();
            this.summonAnimationState.start(this.tickCount);
            this.playSound(ModSounds.REDSTONE_GOLEM_SUMMON.get());
            this.summonTick = (int) (MathHelper.secondsToTicks(SUMMON_SECONDS_TIME) + 5);
            this.getGlow = 1.0F;
        } else if (pId == 5){
            this.attackTick = 0;
        } else if (pId == 6){
            this.stopAnimations();
            this.attackAnimationState.start(this.tickCount);
            this.playSound(ModSounds.REDSTONE_GOLEM_ATTACK.get());
        } else if (pId == 7){
            this.stopAnimations();
            this.deathAnimationState.start(this.tickCount);
            this.deathRotation = this.getYRot();
            this.playSound(ModSounds.REDSTONE_GOLEM_DEATH.get(), 1.0F, 1.0F);
        } else if (pId == 8){
            this.stopAnimations();
            this.activateAnimationState.start(this.tickCount);
        } else if (pId == 9){
            this.stopAnimations();
            this.noveltyAnimationState.start(this.tickCount);
        } else if (pId == 10) {
            this.noveltyAnimationState.stop();
        } else if (pId == 11){
            this.attackAnimationState.stop();
        } else {
            super.handleEntityEvent(pId);
        }
    }

    protected double getAttackReachSqr(LivingEntity enemy) {
        return (double)(this.getBbWidth() * 6.0F + enemy.getBbWidth());
    }

    public boolean targetClose(LivingEntity enemy, double distToEnemySqr){
        return distToEnemySqr <= this.getAttackReachSqr(enemy) || RedstoneGolem.this.getBoundingBox().intersects(enemy.getBoundingBox());
    }

    public boolean doHurtTarget(Entity entityIn) {
        if (!this.level.isClientSide && !this.isMeleeAttacking()) {
            this.setMeleeAttacking(true);
        }
        return true;
    }

    public InteractionResult mobInteract(Player pPlayer, InteractionHand p_230254_2_) {
        if (!this.level.isClientSide) {
            ItemStack itemstack = pPlayer.getItemInHand(p_230254_2_);
            Item item = itemstack.getItem();
            if (this.getTrueOwner() != null && pPlayer == this.getTrueOwner() && !pPlayer.isShiftKeyDown() && !pPlayer.isCrouching()) {
                if ((item == Items.REDSTONE_BLOCK || item == Items.REDSTONE) && this.getHealth() < this.getMaxHealth()) {
                    if (!pPlayer.getAbilities().instabuild) {
                        itemstack.shrink(1);
                    }

                    if (item == Items.REDSTONE_BLOCK){
                        this.heal(this.getMaxHealth() / 4.0F);
                        this.playSound(SoundEvents.IRON_GOLEM_REPAIR, 1.0F, 1.25F);
                    } else {
                        this.heal((this.getMaxHealth() / 4.0F) / 8.0F);
                        this.playSound(SoundEvents.IRON_GOLEM_REPAIR, 0.25F, 1.0F);
                    }

                    if (this.level instanceof ServerLevel serverLevel) {
                        for (int i = 0; i < 7; ++i) {
                            double d0 = serverLevel.random.nextGaussian() * 0.02D;
                            double d1 = serverLevel.random.nextGaussian() * 0.02D;
                            double d2 = serverLevel.random.nextGaussian() * 0.02D;
                            serverLevel.sendParticles(DustParticleOptions.REDSTONE, this.getRandomX(1.0D), this.getRandomY(), this.getRandomZ(1.0D), 0, d0, d1, d2, 0.5F);
                        }
                    }
                    return InteractionResult.SUCCESS;
                } else if (pPlayer.getMainHandItem().getItem() instanceof DarkWand) {
                    this.updateMoveMode(pPlayer);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }

    /**
     * Attack and Melee Goals based of @Infamous-Misadventures codes to make animation sync up: <a href="https://github.com/Infamous-Misadventures/Dungeons-Mobs/blob/1.19/src/main/java/com/infamous/dungeons_mobs/entities/redstone/RedstoneGolemEntity.java#L92">...</a>
     */
    class AttackGoal extends MeleeAttackGoal {
        private int maxAttackTimer = 20;
        private final double moveSpeed;
        private int delayCounter;
        private int attackTimer;

        public AttackGoal(double moveSpeed) {
            super(RedstoneGolem.this, moveSpeed, true);
            this.moveSpeed = moveSpeed;
        }

        public AttackGoal setMaxAttackTick(int max) {
            this.maxAttackTimer = max;
            return this;
        }

        @Override
        public boolean canUse() {
            return RedstoneGolem.this.getTarget() != null && RedstoneGolem.this.getTarget().isAlive();
        }

        @Override
        public void start() {
            RedstoneGolem.this.setAggressive(true);
            this.delayCounter = 0;
        }

        @Override
        public void stop() {
            RedstoneGolem.this.getNavigation().stop();
            if (RedstoneGolem.this.getTarget() == null) {
                RedstoneGolem.this.setAggressive(false);
            }
        }

        @Override
        public void tick() {
            LivingEntity livingentity = RedstoneGolem.this.getTarget();
            if (livingentity == null) {
                return;
            }

            RedstoneGolem.this.lookControl.setLookAt(livingentity, 30.0F, 30.0F);

            if (--this.delayCounter <= 0) {
                this.delayCounter = 10;
                RedstoneGolem.this.getNavigation().moveTo(livingentity, this.moveSpeed);
            }

            this.attackTimer = Math.max(this.attackTimer - 1, 0);
            this.checkAndPerformAttack(livingentity, RedstoneGolem.this.distanceToSqr(livingentity.getX(), livingentity.getY(), livingentity.getZ()));
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity enemy, double distToEnemySqr) {
            if (RedstoneGolem.this.targetClose(enemy, distToEnemySqr)) {
                if (RedstoneGolem.this.summonCool > 10 && this.attackTimer == 5){
                    RedstoneGolem.this.playSound(ModSounds.REDSTONE_GOLEM_PRE_ATTACK.get(), 1.5F, 1.0F);
                }
                if (this.attackTimer <= 0) {
                    this.attackTimer = this.maxAttackTimer;
                    RedstoneGolem.this.doHurtTarget(enemy);
                }
            }
        }

    }

    class MeleeGoal extends Goal {
        public MeleeGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.LOOK, Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return RedstoneGolem.this.getTarget() != null && RedstoneGolem.this.isMeleeAttacking();
        }

        /**
         * Is short so that the Redstone Golem can immediately move after attacking, else it would stay in place until animation is finished.
         */
        @Override
        public boolean canContinueToUse() {
            return RedstoneGolem.this.attackTick < 5;
        }

        @Override
        public void start() {
            RedstoneGolem.this.setMeleeAttacking(true);
        }

        /**
         * Using Post Attack Tick to ensure that attack animation is stopped properly.
         */
        @Override
        public void stop() {
            RedstoneGolem.this.setMeleeAttacking(false);
            RedstoneGolem.this.isPostAttack = true;
        }

        @Override
        public void tick() {
            if (RedstoneGolem.this.getTarget() != null && RedstoneGolem.this.getTarget().isAlive()) {
                LivingEntity livingentity = RedstoneGolem.this.getTarget();
                double d0 = RedstoneGolem.this.distanceToSqr(livingentity.getX(), livingentity.getY(), livingentity.getZ());
                RedstoneGolem.this.getLookControl().setLookAt(livingentity, 30.0F, 30.0F);
                if (RedstoneGolem.this.targetClose(livingentity, d0)){
                    RedstoneGolem.this.setYBodyRot(RedstoneGolem.this.getYHeadRot());
                    if (RedstoneGolem.this.attackTick == 1) {
                        RedstoneGolem.this.playSound(ModSounds.REDSTONE_GOLEM_ATTACK.get());
                        RedstoneGolem.this.level.broadcastEntityEvent(RedstoneGolem.this, (byte) 6);
                    }
                    if (RedstoneGolem.this.attackTick == 3) {
                        if (RedstoneGolem.this.targetClose(livingentity, d0)) {
                            this.hurtTarget(livingentity);
                            this.massiveSweep(RedstoneGolem.this, livingentity, 3.0D, 100.0D);
                        }
                    }
                }
            }
        }

        public void hurtTarget(Entity target) {
            float f = (float)RedstoneGolem.this.getAttributeValue(Attributes.ATTACK_DAMAGE);
            float f1 = (float)RedstoneGolem.this.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
            boolean flag = target.hurt(DamageSource.mobAttack(RedstoneGolem.this), f);
            if (flag) {
                if (f1 > 0.0F && target instanceof LivingEntity livingEntity) {
                    if (livingEntity.getBoundingBox().getSize() > RedstoneGolem.this.getBoundingBox().getSize()){
                        livingEntity.knockback((double)(f1 * 0.5F), (double)Mth.sin(RedstoneGolem.this.getYRot() * ((float)Math.PI / 180F)), (double)(-Mth.cos(RedstoneGolem.this.getYRot() * ((float)Math.PI / 180F))));
                    } else {
                        MobUtil.forcefulKnockBack(livingEntity, (double)(f1 * 0.5F), (double)Mth.sin(RedstoneGolem.this.getYRot() * ((float)Math.PI / 180F)), (double)(-Mth.cos(RedstoneGolem.this.getYRot() * ((float)Math.PI / 180F))), 0.5D);
                    }
                    RedstoneGolem.this.setDeltaMovement(RedstoneGolem.this.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
                }

                RedstoneGolem.this.doEnchantDamageEffects(RedstoneGolem.this, target);
                RedstoneGolem.this.setLastHurtMob(target);
            }
        }

        public void massiveSweep(LivingEntity source, LivingEntity exempt, double range, double arc){
            List<LivingEntity> hits = MobUtil.getAttackableLivingEntitiesNearby(source, range, 1.0F, range, range);
            for (LivingEntity target : hits) {
                float targetAngle = (float) ((Math.atan2(target.getZ() - source.getZ(), target.getX() - source.getX()) * (180 / Math.PI) - 90) % 360);
                float attackAngle = source.yBodyRot % 360;
                if (targetAngle < 0) {
                    targetAngle += 360;
                }
                if (attackAngle < 0) {
                    attackAngle += 360;
                }
                float relativeAngle = targetAngle - attackAngle;
                float hitDistance = (float) Math.sqrt((target.getZ() - source.getZ()) * (target.getZ() - source.getZ()) + (target.getX() - source.getX()) * (target.getX() - source.getX())) - target.getBbWidth() / 2f;
                if (target != exempt) {
                    if (hitDistance <= range && (relativeAngle <= arc / 2 && relativeAngle >= -arc / 2) || (relativeAngle >= 360 - arc / 2 || relativeAngle <= -360 + arc / 2)) {
                        this.hurtTarget(target);
                    }
                }
            }
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }
    }

    public class SummonMinesGoal extends Goal{

        @Override
        public boolean canUse() {
            LivingEntity livingentity = RedstoneGolem.this.getTarget();
            if (livingentity != null && livingentity.isAlive()) {
                double d0 = RedstoneGolem.this.distanceToSqr(livingentity.getX(), livingentity.getY(), livingentity.getZ());
                return RedstoneGolem.this.summonCool <= 0 && RedstoneGolem.this.isOnGround() && RedstoneGolem.this.targetClose(livingentity, d0);
            } else {
                return false;
            }
        }

        @Override
        public void start() {
            super.start();
            RedstoneGolem.this.playSound(ModSounds.REDSTONE_GOLEM_SUMMON.get());
            RedstoneGolem.this.level.broadcastEntityEvent(RedstoneGolem.this, (byte) 4);
            RedstoneGolem.this.summonTick = (int) (MathHelper.secondsToTicks(SUMMON_SECONDS_TIME) + 5);
            RedstoneGolem.this.summonCool = (int) MathHelper.secondsToTicks(10 + SUMMON_SECONDS_TIME);
            RedstoneGolem.this.mineCount = 14;
        }
    }

}
