package com.Polarice3.Goety.common.entities.neutral;

import com.Polarice3.Goety.api.entities.IOwned;
import com.Polarice3.Goety.client.particles.ModParticleTypes;
import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.entities.ai.AvoidTargetGoal;
import com.Polarice3.Goety.common.entities.ally.Summoned;
import com.Polarice3.Goety.common.entities.ally.undead.WraithServant;
import com.Polarice3.Goety.common.entities.ally.undead.skeleton.SunkenSkeletonServant;
import com.Polarice3.Goety.common.entities.ally.undead.skeleton.VanguardServant;
import com.Polarice3.Goety.common.entities.ally.undead.zombie.DrownedServant;
import com.Polarice3.Goety.common.entities.projectiles.SpellHurtingProjectile;
import com.Polarice3.Goety.common.entities.projectiles.SteamMissile;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.config.AttributesConfig;
import com.Polarice3.Goety.config.MobsConfig;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.init.ModTags;
import com.Polarice3.Goety.utils.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class DrownedNecromancer extends AbstractNecromancer {
    private boolean searchingForLand;
    protected int stormSpellCool;
    protected int rapidShotCool;
    protected final WaterBoundPathNavigation waterNavigation;
    protected final GroundPathNavigation groundNavigation;
    public static String STORM = "storm";
    public static String RAPID = "rapid";
    public AnimationState stormAnimationState = new AnimationState();
    public AnimationState rapidAnimationState = new AnimationState();

    public DrownedNecromancer(EntityType<? extends AbstractNecromancer> type, Level level) {
        super(type, level);
        this.setMaxUpStep(1.0F);
        this.moveControl = new MoveHelperController(this);
        this.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
        this.waterNavigation = new WaterBoundPathNavigation(this, level);
        this.groundNavigation = new GroundPathNavigation(this, level);
    }

    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new GoToWaterGoal(this, 1.0D));
        this.goalSelector.addGoal(1, new FollowOwnerWaterGoal(this, 1.0D, 10.0F, 2.0F));
        this.goalSelector.addGoal(5, new GoToBeachGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new SwimUpGoal(this, 1.0D, this.level.getSeaLevel()));
        this.goalSelector.addGoal(7, new WaterWanderGoal<>(this));
        this.goalSelector.addGoal(8, new MoveToTarget());
    }

    public void projectileGoal(int priority){
        this.goalSelector.addGoal(priority, new RapidShotGoal());
    }

    public void avoidGoal(int priority){
        this.goalSelector.addGoal(priority, AvoidTargetGoal.AvoidRadiusGoal.newGoalTwo(this, 2, 5, 1.0D, 1.2D));
    }

    public void summonSpells(int priority){
        this.goalSelector.addGoal(priority, new TridentStormGoal());
        this.goalSelector.addGoal(priority + 1, new SummonServantSpell());
    }

    public static AttributeSupplier.Builder setCustomAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, AttributesConfig.DrownedNecromancerHealth.get())
                .add(Attributes.ARMOR, AttributesConfig.DrownedNecromancerArmor.get())
                .add(Attributes.FOLLOW_RANGE, AttributesConfig.DrownedNecromancerFollowRange.get())
                .add(Attributes.MOVEMENT_SPEED, 0.25F)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6D)
                .add(Attributes.ATTACK_DAMAGE, AttributesConfig.DrownedNecromancerDamage.get());
    }

    public void setConfigurableAttributes(){
        MobUtil.setBaseAttributes(this.getAttribute(Attributes.MAX_HEALTH), AttributesConfig.DrownedNecromancerHealth.get());
        MobUtil.setBaseAttributes(this.getAttribute(Attributes.ARMOR), AttributesConfig.DrownedNecromancerArmor.get());
        MobUtil.setBaseAttributes(this.getAttribute(Attributes.FOLLOW_RANGE), AttributesConfig.DrownedNecromancerFollowRange.get());
        MobUtil.setBaseAttributes(this.getAttribute(Attributes.ATTACK_DAMAGE), AttributesConfig.DrownedNecromancerDamage.get());
    }

    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        if (pCompound.contains("StormCoolDown")) {
            this.stormSpellCool = pCompound.getInt("StormCoolDown");
        }
        if (pCompound.contains("RapidCoolDown")) {
            this.rapidShotCool = pCompound.getInt("RapidCoolDown");
        }
        this.setConfigurableAttributes();
    }

    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putInt("StormCoolDown", this.stormSpellCool);
        pCompound.putInt("RapidCoolDown", this.rapidShotCool);
    }

    public void onSyncedDataUpdated(EntityDataAccessor<?> p_33609_) {
        if (ANIM_STATE.equals(p_33609_)) {
            if (this.level.isClientSide) {
                switch (this.entityData.get(ANIM_STATE)) {
                    case 6:
                        this.stormAnimationState.startIfStopped(this.tickCount);
                        this.stopMostAnimation(this.stormAnimationState);
                        break;
                    case 7:
                        this.rapidAnimationState.startIfStopped(this.tickCount);
                        this.stopMostAnimation(this.rapidAnimationState);
                        break;
                }
            }
        }
        super.onSyncedDataUpdated(p_33609_);
    }

    public int getAnimationState(String animation) {
        if (Objects.equals(animation, STORM)){
            return 6;
        } else if (Objects.equals(animation, RAPID)){
            return 7;
        } else {
            return super.getAnimationState(animation);
        }
    }

    public List<AnimationState> getAnimations(){
        List<AnimationState> animationStates = super.getAnimations();
        animationStates.add(this.stormAnimationState);
        animationStates.add(this.rapidAnimationState);
        return animationStates;
    }

    @Override
    public int xpReward() {
        return 40;
    }

    protected SoundEvent getAmbientSound() {
        return ModSounds.DROWNED_NECROMANCER_AMBIENT.get();
    }

    protected SoundEvent getHurtSound(@NotNull DamageSource damageSourceIn) {
        return ModSounds.DROWNED_NECROMANCER_HURT.get();
    }

    protected SoundEvent getDeathSound() {
        return ModSounds.DROWNED_NECROMANCER_DEATH.get();
    }

    @Override
    protected @NotNull SoundEvent getSwimSound() {
        return ModSounds.DROWNED_NECROMANCER_SWIM.get();
    }

    protected SoundEvent getStepSound() {
        return SoundEvents.DROWNED_STEP;
    }

    public boolean isPushedByFluid(FluidType type) {
        return !this.isSwimming();
    }

    public boolean checkSpawnObstruction(LevelReader pLevel) {
        return pLevel.isUnobstructed(this);
    }

    private boolean wantsToSwim() {
        if (this.searchingForLand) {
            return true;
        } else if (this.getTarget() != null && this.getTarget().isInWater()) {
            return true;
        } else {
            return this.getTrueOwner() != null && this.getTrueOwner().isInWater();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.stormSpellCool > 0){
            --this.stormSpellCool;
        }
        if (this.rapidShotCool > 0){
            --this.rapidShotCool;
        }
    }

    public void travel(Vec3 pTravelVector) {
        if (this.isEffectiveAi() && this.isInWater() && this.wantsToSwim()) {
            this.moveRelative(0.01F, pTravelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9D));
        } else {
            super.travel(pTravelVector);
        }

    }

    public void updateSwimming() {
        if (!this.level.isClientSide) {
            if (this.isEffectiveAi() && this.isInWater() && this.wantsToSwim()) {
                this.navigation = this.waterNavigation;
                this.setSwimming(true);
            } else {
                this.navigation = this.groundNavigation;
                this.setSwimming(false);
            }
        }

    }

    protected boolean closeToNextPos() {
        Path path = this.getNavigation().getPath();
        if (path != null) {
            BlockPos blockpos = path.getTarget();
            if (blockpos != null) {
                double d0 = this.distanceToSqr((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ());
                return d0 < 4.0D;
            }
        }

        return false;
    }

    public void setSearchingForLand(boolean p_204713_1_) {
        this.searchingForLand = p_204713_1_;
    }

    public Summoned getDefaultSummon(){
        return new DrownedServant(ModEntityType.DROWNED_SERVANT.get(), this.level);
    }

    public Summoned getSummon(){
        Summoned summoned = this.getDefaultSummon();
        if (this.getSummonList().stream().anyMatch(entityType -> entityType.is(ModTags.EntityTypes.ZOMBIE_SERVANTS))) {
            if (this.level.random.nextBoolean()) {
                summoned = new DrownedServant(ModEntityType.DROWNED_SERVANT.get(), this.level);
            }
        }
        if (this.getSummonList().stream().anyMatch(entityType -> entityType.is(ModTags.EntityTypes.SKELETON_SERVANTS))) {
            if (this.level.random.nextBoolean()) {
                summoned = new SunkenSkeletonServant(ModEntityType.SUNKEN_SKELETON_SERVANT.get(), this.level);
            }
        }
        if (this.getSummonList().contains(ModEntityType.WRAITH_SERVANT.get())) {
            if (this.level.random.nextFloat() <= 0.05F) {
                summoned = new WraithServant(ModEntityType.WRAITH_SERVANT.get(), this.level);
            }
        }
        if (this.getSummonList().contains(ModEntityType.VANGUARD_SERVANT.get())){
            if (this.level.random.nextFloat() <= 0.15F) {
                summoned = new VanguardServant(ModEntityType.VANGUARD_SERVANT.get(), this.level);
            }
        }
        return summoned;
    }

    @Override
    public boolean summonVariants() {
        return false;
    }

    public void spellCastParticles(){

    }

    public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
        if (!this.level.isClientSide) {
            ItemStack itemstack = pPlayer.getItemInHand(pHand);
            Item item = itemstack.getItem();
            if (this.getTrueOwner() != null && pPlayer == this.getTrueOwner()) {
                if (!this.spawnUndeadIdle() && pHand == InteractionHand.MAIN_HAND && itemstack.isEmpty()){
                    if (this.idleSpellCool <= 0){
                        this.setUndeadIdle(true);
                    } else {
                        this.playSound(ModSounds.DROWNED_NECROMANCER_HURT.get());
                        this.level.broadcastEntityEvent(this, (byte) 9);
                    }
                    return InteractionResult.SUCCESS;
                } else if (item == Items.ROTTEN_FLESH && this.getHealth() < this.getMaxHealth()) {
                    if (!pPlayer.getAbilities().instabuild) {
                        itemstack.shrink(1);
                    }
                    this.playSound(ModSounds.DROWNED_NECROMANCER_SWIM.get(), 1.0F, 1.25F);
                    this.heal(2.0F);
                    if (this.level instanceof ServerLevel serverLevel) {
                        for (int i = 0; i < 7; ++i) {
                            double d0 = this.random.nextGaussian() * 0.02D;
                            double d1 = this.random.nextGaussian() * 0.02D;
                            double d2 = this.random.nextGaussian() * 0.02D;
                            serverLevel.sendParticles(ModParticleTypes.HEAL_EFFECT.get(), this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), 0, d0, d1, d2, 0.5F);
                        }
                    }
                    return InteractionResult.SUCCESS;
                } else if (this.getSummonList().stream().noneMatch(entityType -> entityType.is(ModTags.EntityTypes.ZOMBIE_SERVANTS)) && item == ModItems.ROTTING_FOCUS.get()){
                    if (!pPlayer.getAbilities().instabuild) {
                        itemstack.shrink(1);
                    }
                    this.addSummon(ModEntityType.ZOMBIE_SERVANT.get());
                    this.playSound(ModSounds.DROWNED_NECROMANCER_AMBIENT.get(), 1.0F, 1.5F);
                    return InteractionResult.SUCCESS;
                } else if (this.getSummonList().stream().noneMatch(entityType -> entityType.is(ModTags.EntityTypes.SKELETON_SERVANTS)) && item == ModItems.OSSEOUS_FOCUS.get()){
                    if (!pPlayer.getAbilities().instabuild) {
                        itemstack.shrink(1);
                    }
                    this.addSummon(ModEntityType.SKELETON_SERVANT.get());
                    this.playSound(ModSounds.DROWNED_NECROMANCER_AMBIENT.get(), 1.0F, 1.5F);
                    return InteractionResult.SUCCESS;
                } else if (/*this.getNecroLevel() > 0 && */!this.getSummonList().contains(ModEntityType.WRAITH_SERVANT.get()) && item == ModItems.SPOOKY_FOCUS.get()){
                    if (!pPlayer.getAbilities().instabuild) {
                        itemstack.shrink(1);
                    }
                    this.addSummon(ModEntityType.WRAITH_SERVANT.get());
                    this.playSound(ModSounds.DROWNED_NECROMANCER_AMBIENT.get(), 1.0F, 1.5F);
                    return InteractionResult.SUCCESS;
                } else if (/*this.getNecroLevel() > 1 && */!this.getSummonList().contains(ModEntityType.VANGUARD_SERVANT.get()) && item == ModItems.VANGUARD_FOCUS.get()){
                    if (!pPlayer.getAbilities().instabuild) {
                        itemstack.shrink(1);
                    }
                    this.addSummon(ModEntityType.VANGUARD_SERVANT.get());
                    this.playSound(ModSounds.DROWNED_NECROMANCER_AMBIENT.get(), 1.0F, 1.5F);
                    return InteractionResult.SUCCESS;
                } else if (item == ModItems.SOUL_JAR.get()){
                    if (!pPlayer.getAbilities().instabuild) {
                        itemstack.shrink(1);
                    }
                    if (this.getNecroLevel() < 2) {
                        this.setNecroLevel(this.getNecroLevel() + 1);
                    }
                    this.heal(AttributesConfig.NecromancerHealth.get().floatValue());
                    if (this.level instanceof ServerLevel serverLevel) {
                        for (int i = 0; i < 7; ++i) {
                            double d0 = this.random.nextGaussian() * 0.02D;
                            double d1 = this.random.nextGaussian() * 0.02D;
                            double d2 = this.random.nextGaussian() * 0.02D;
                            serverLevel.sendParticles(ParticleTypes.SCULK_SOUL, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), 0, d0, d1, d2, 0.5F);
                        }
                    }
                    this.playSound(ModSounds.DROWNED_NECROMANCER_AMBIENT.get(), 1.0F, 0.5F);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }

    static class MoveHelperController extends MoveControl {
        private final DrownedNecromancer drowned;

        public MoveHelperController(DrownedNecromancer p_i48909_1_) {
            super(p_i48909_1_);
            this.drowned = p_i48909_1_;
        }

        public void tick() {
            LivingEntity livingentity = this.drowned.getTarget();
            LivingEntity owner = this.drowned.getTrueOwner();
            if (this.drowned.wantsToSwim() && this.drowned.isInWater()) {
                if (livingentity != null && livingentity.getY() > this.drowned.getY() || this.drowned.searchingForLand) {
                    this.drowned.setDeltaMovement(this.drowned.getDeltaMovement().add(0.0D, 0.002D, 0.0D));
                } else if (owner != null && owner.getY() > this.drowned.getY()){
                    this.drowned.setDeltaMovement(this.drowned.getDeltaMovement().add(0.0D, 0.002D, 0.0D));
                }

                if (this.operation != Operation.MOVE_TO || this.drowned.getNavigation().isDone()) {
                    this.drowned.setSpeed(0.0F);
                    return;
                }

                double d0 = this.wantedX - this.drowned.getX();
                double d1 = this.wantedY - this.drowned.getY();
                double d2 = this.wantedZ - this.drowned.getZ();
                double d3 = Mth.sqrt((float) (d0 * d0 + d1 * d1 + d2 * d2));
                d1 = d1 / d3;
                float f = (float)(Mth.atan2(d2, d0) * (double)(180F / (float)Math.PI)) - 90.0F;
                this.drowned.setYRot(this.rotlerp(this.drowned.getYRot(), f, 90.0F));
                this.drowned.setYBodyRot(this.drowned.getYRot());
                float f1 = (float)(this.speedModifier * this.drowned.getAttributeValue(Attributes.MOVEMENT_SPEED));
                float f2 = Mth.lerp(0.125F, this.drowned.getSpeed(), f1);
                this.drowned.setSpeed(f2);
                this.drowned.setDeltaMovement(this.drowned.getDeltaMovement().add((double)f2 * d0 * 0.005D, (double)f2 * d1 * 0.1D, (double)f2 * d2 * 0.005D));
            } else {
                if (!this.drowned.onGround()) {
                    this.drowned.setDeltaMovement(this.drowned.getDeltaMovement().add(0.0D, -0.008D, 0.0D));
                }

                super.tick();
            }

        }
    }

    static class GoToBeachGoal extends MoveToBlockGoal {
        private final DrownedNecromancer drowned;

        public GoToBeachGoal(DrownedNecromancer p_i48911_1_, double p_i48911_2_) {
            super(p_i48911_1_, p_i48911_2_, 8, 2);
            this.drowned = p_i48911_1_;
        }

        public boolean canUse() {
            if (this.drowned.getTrueOwner() != null){
                if (this.drowned.getTrueOwner().isUnderWater()){
                    return false;
                }
            } else if (this.drowned.level.isDay()) {
                return false;
            }
            return super.canUse() && this.drowned.isInWater() && this.drowned.getY() >= (double)(this.drowned.level.getSeaLevel() - 3);
        }

        public boolean canContinueToUse() {
            return super.canContinueToUse();
        }

        protected boolean isValidTarget(LevelReader pLevel, BlockPos pPos) {
            BlockPos blockpos = pPos.above();
            return pLevel.isEmptyBlock(blockpos) && pLevel.isEmptyBlock(blockpos.above()) && pLevel.getBlockState(pPos).entityCanStandOn(pLevel, pPos, this.drowned);
        }

        public void start() {
            this.drowned.setSearchingForLand(false);
            this.drowned.navigation = this.drowned.groundNavigation;
            super.start();
        }

        public void stop() {
            super.stop();
        }
    }

    static class SwimUpGoal extends Goal {
        private final DrownedNecromancer drowned;
        private final double speedModifier;
        private final int seaLevel;
        private boolean stuck;

        public SwimUpGoal(DrownedNecromancer p_i48908_1_, double p_i48908_2_, int p_i48908_4_) {
            this.drowned = p_i48908_1_;
            this.speedModifier = p_i48908_2_;
            this.seaLevel = p_i48908_4_;
        }

        public boolean canUse() {
            if (this.drowned.getTrueOwner() != null){
                if (this.drowned.getTrueOwner().isUnderWater()){
                    return false;
                }
            } else if (this.drowned.level.isDay()) {
                return false;
            }
            return this.drowned.isInWater() && this.drowned.getY() < (double)(this.seaLevel - 2);
        }

        public boolean canContinueToUse() {
            return this.canUse() && !this.stuck;
        }

        public void tick() {
            if (this.drowned.getY() < (double)(this.seaLevel - 1) && (this.drowned.getNavigation().isDone() || this.drowned.closeToNextPos())) {
                Vec3 vec3 = DefaultRandomPos.getPosTowards(this.drowned, 4, 8, new Vec3(this.drowned.getX(), (double)(this.seaLevel - 1), this.drowned.getZ()), (double)((float)Math.PI / 2F));
                if (vec3 == null) {
                    this.stuck = true;
                    return;
                }

                this.drowned.getNavigation().moveTo(vec3.x, vec3.y, vec3.z, this.speedModifier);
            }

        }

        public void start() {
            this.drowned.setSearchingForLand(true);
            this.stuck = false;
        }

        public void stop() {
            this.drowned.setSearchingForLand(false);
        }
    }

    public class SummonServantSpell extends SummoningSpellGoal {

        public boolean canUse() {
            Predicate<Entity> predicate = entity -> entity.isAlive() && entity instanceof IOwned owned && owned.getTrueOwner() instanceof AbstractNecromancer;
            int i = DrownedNecromancer.this.level.getEntitiesOfClass(LivingEntity.class, DrownedNecromancer.this.getBoundingBox().inflate(64.0D, 16.0D, 64.0D)
                    , predicate).size();
            return super.canUse() && i < 10 && DrownedNecromancer.this.stormSpellCool > 0;
        }

        protected void castSpell(){
            if (DrownedNecromancer.this.level instanceof ServerLevel serverLevel) {
                for (int i1 = 0; i1 < 2; ++i1) {
                    Summoned summonedentity = DrownedNecromancer.this.getSummon();
                    BlockPos blockPos = BlockFinder.SummonRadius(DrownedNecromancer.this.blockPosition(), summonedentity, serverLevel);
                    if (DrownedNecromancer.this.isUnderWater()){
                        blockPos = BlockFinder.SummonWaterRadius(DrownedNecromancer.this, serverLevel);
                    }
                    summonedentity.setTrueOwner(DrownedNecromancer.this);
                    summonedentity.moveTo(blockPos, 0.0F, 0.0F);
                    if (!DrownedNecromancer.this.isUnderWater()){
                        MobUtil.moveDownToGround(summonedentity);
                    }
                    if (MobsConfig.NecromancerSummonsLife.get()) {
                        summonedentity.setLimitedLife(MobUtil.getSummonLifespan(serverLevel));
                    }
                    summonedentity.setPersistenceRequired();
                    summonedentity.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(DrownedNecromancer.this.blockPosition()), MobSpawnType.MOB_SUMMONED, null, null);
                    if (summonedentity instanceof DrownedServant && serverLevel.random.nextInt(16) == 0) {
                        summonedentity.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.TRIDENT));
                        summonedentity.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
                    }
                    summonedentity.setBaby(false);
                    if (serverLevel.addFreshEntity(summonedentity)){
                        if (!DrownedNecromancer.this.isSilent()) {
                            DrownedNecromancer.this.level.playSound(null, DrownedNecromancer.this.getX(), DrownedNecromancer.this.getY(), DrownedNecromancer.this.getZ(), ModSounds.DROWNED_NECROMANCER_SUMMON.get(), DrownedNecromancer.this.getSoundSource(), 1.4F, 1.0F);
                        }
                        ColorUtil colorUtil = new ColorUtil(0x2ac9cf);
                        ServerParticleUtil.windShockwaveParticle(serverLevel, colorUtil, 0.1F, 0.1F, 0.05F, -1, summonedentity.position());
                    }
                }
            }
        }

        @Override
        protected int getCastingTime() {
            return MathHelper.secondsToTicks(1.8F);
        }

        @Nullable
        @Override
        protected SoundEvent getSpellPrepareSound() {
            return ModSounds.DROWNED_NECROMANCER_PREPARE.get();
        }

        @Override
        protected void playLaughSound() {
        }

        @Override
        protected NecromancerSpellType getNecromancerSpellType() {
            return NecromancerSpellType.ZOMBIE;
        }
    }

    public class TridentStormGoal extends Goal {
        protected int spellTime;

        @Override
        public boolean canUse() {
            LivingEntity target = DrownedNecromancer.this.getTarget();
            if (DrownedNecromancer.this.isSpellCasting()) {
                return false;
            } else {
                return target != null
                        && target.isAlive()
                        && DrownedNecromancer.this.stormSpellCool <= 0;
            }
        }

        public boolean canContinueToUse() {
            return this.spellTime > 0;
        }

        public void start() {
            this.spellTime = MathHelper.secondsToTicks(5);
            DrownedNecromancer.this.getNavigation().stop();
            DrownedNecromancer.this.setSpellCooldown(DrownedNecromancer.this.getSpellCooldown() + 60);
            DrownedNecromancer.this.playSound(ModSounds.TRIDENT_STORM_PRE.get(), 2.0F, 1.1F);
            DrownedNecromancer.this.setSpellCasting(true);
            DrownedNecromancer.this.setNecromancerSpellType(NecromancerSpellType.CLOUD);
            DrownedNecromancer.this.setAnimationState(STORM);
            int warmUp = MathHelper.secondsToTicks(2);
            int i = DrownedNecromancer.this.getRandom().nextInt(4);
            if (i == 0){
                WandUtil.summonTridentSurround(DrownedNecromancer.this, warmUp);
            } else if (i == 1){
                WandUtil.summonTridentSquare(DrownedNecromancer.this, warmUp);
            } else if (i == 2){
                WandUtil.summonTridentWideCircle(DrownedNecromancer.this, warmUp);
            } else {
                WandUtil.summonTridentCross(DrownedNecromancer.this, warmUp);
            }
        }

        @Override
        public void stop() {
            super.stop();
            this.spellTime = 0;
            DrownedNecromancer.this.setSpellCasting(false);
            if (DrownedNecromancer.this.getCurrentAnimation() == DrownedNecromancer.this.getAnimationState(STORM)) {
                DrownedNecromancer.this.setAnimationState(IDLE);
            }
        }

        public void tick() {
            --this.spellTime;
            if (this.spellTime <= MathHelper.secondsToTicks(2.5F)){
                DrownedNecromancer.this.setAnimationState(IDLE);
            }
            if (this.spellTime == 0) {
                DrownedNecromancer.this.setNecromancerSpellType(NecromancerSpellType.NONE);
                DrownedNecromancer.this.stormSpellCool = MathHelper.secondsToTicks(10);
            }
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }
    }

    public class RapidShotGoal extends Goal {
        protected int spellTime;
        protected int shots;
        protected int totalShots;
        @Nullable
        private LivingEntity target;

        @Override
        public boolean canUse() {
            LivingEntity livingentity = DrownedNecromancer.this.getTarget();
            if (livingentity != null && livingentity.isAlive() && DrownedNecromancer.this.rapidShotCool <= 0 && DrownedNecromancer.this.stormSpellCool > 0) {
                this.target = livingentity;
                return !DrownedNecromancer.this.isSpellCasting();
            } else {
                return false;
            }
        }

        public boolean canContinueToUse() {
            return this.target != null && this.target.isAlive() && this.totalShots < 5 && !DrownedNecromancer.this.isSpellCasting() && DrownedNecromancer.this.stormSpellCool > 0;
        }

        public void start() {
            this.spellTime = 0;
            this.shots = 0;
            this.totalShots = 0;
            DrownedNecromancer.this.getNavigation().stop();
            DrownedNecromancer.this.setShooting(true);
            DrownedNecromancer.this.setAnimationState(RAPID);
        }

        @Override
        public void stop() {
            this.spellTime = 0;
            this.shots = 0;
            this.totalShots = 0;
            DrownedNecromancer.this.setShooting(false);
            if (DrownedNecromancer.this.getCurrentAnimation() == DrownedNecromancer.this.getAnimationState(RAPID)) {
                DrownedNecromancer.this.setAnimationState(IDLE);
            }
            DrownedNecromancer.this.rapidShotCool = MathHelper.secondsToTicks(5);
        }

        public void tick() {
            ++this.spellTime;
            Level worldIn = DrownedNecromancer.this.level;
            MobUtil.instaLook(DrownedNecromancer.this, this.target);
            if (this.spellTime % 2 == 0 && this.shots < 5){
                Vec3 vector3d = DrownedNecromancer.this.getViewVector( 1.0F);
                double accuracy = 8.0D;
                Vec3 vec3 = (new Vec3(vector3d.x, vector3d.y, vector3d.z)).normalize().add(worldIn.random.triangle(0.0D, 0.0172275D * (double)accuracy), 0.0D, worldIn.random.triangle(0.0D, 0.0172275D * (double)accuracy));
                SpellHurtingProjectile steamMissile = new SteamMissile(
                        DrownedNecromancer.this.getX() + vector3d.x / 2,
                        DrownedNecromancer.this.getEyeY() - 0.2,
                        DrownedNecromancer.this.getZ() + vector3d.z / 2,
                        vec3.x,
                        vec3.y,
                        vec3.z, worldIn);
                steamMissile.setOwner(DrownedNecromancer.this);
                if (worldIn.addFreshEntity(steamMissile)) {
                    ++this.shots;
                }
            }
            if (this.spellTime % 18 == 0){
                DrownedNecromancer.this.playSound(ModSounds.CAST_STEAM.get(), 1.5F, 1.0F);
                if (this.shots >= 5){
                    this.shots = 0;
                    ++this.totalShots;
                }
            }
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }
    }

    public class MoveToTarget extends Goal {
        @Nullable
        private LivingEntity target;

        @Override
        public boolean canUse() {
            LivingEntity livingentity = DrownedNecromancer.this.getTarget();
            if (DrownedNecromancer.this.isSpellCasting() || DrownedNecromancer.this.isShooting()){
                return false;
            } else if (livingentity != null && livingentity.isAlive()) {
                this.target = livingentity;
                return this.target.distanceTo(DrownedNecromancer.this) > 12.0D;
            } else {
                return false;
            }
        }

        @Override
        public boolean canContinueToUse() {
            return this.target != null && this.target.isAlive() && this.target.distanceTo(DrownedNecromancer.this) > 6.0D && !DrownedNecromancer.this.isSpellCasting() && !DrownedNecromancer.this.isShooting();
        }

        @Override
        public void stop() {
            DrownedNecromancer.this.getNavigation().stop();
            DrownedNecromancer.this.setAnimationState(IDLE);
        }

        public void tick() {
            if (this.target != null) {
                DrownedNecromancer.this.getNavigation().moveTo(this.target, 1.0F);
            }
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }
    }
}
