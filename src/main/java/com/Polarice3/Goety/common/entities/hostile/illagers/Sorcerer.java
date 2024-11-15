package com.Polarice3.Goety.common.entities.hostile.illagers;

import com.Polarice3.Goety.api.magic.IBreathingSpell;
import com.Polarice3.Goety.api.magic.IChargingSpell;
import com.Polarice3.Goety.common.effects.GoetyEffects;
import com.Polarice3.Goety.common.entities.ai.SurroundGoal;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.common.magic.SummonSpell;
import com.Polarice3.Goety.common.magic.spells.*;
import com.Polarice3.Goety.common.magic.spells.frost.FrostBreathSpell;
import com.Polarice3.Goety.common.magic.spells.frost.IceGolemSpell;
import com.Polarice3.Goety.common.magic.spells.frost.IceSpikeSpell;
import com.Polarice3.Goety.common.magic.spells.frost.IceStormSpell;
import com.Polarice3.Goety.common.magic.spells.geomancy.EruptionSpell;
import com.Polarice3.Goety.common.magic.spells.geomancy.ScatterSpell;
import com.Polarice3.Goety.common.magic.spells.storm.ElectroOrbSpell;
import com.Polarice3.Goety.common.magic.spells.storm.ThunderboltSpell;
import com.Polarice3.Goety.common.magic.spells.void_spells.BlinkSpell;
import com.Polarice3.Goety.common.magic.spells.wild.HuntingSpell;
import com.Polarice3.Goety.common.magic.spells.wild.MaulingSpell;
import com.Polarice3.Goety.common.magic.spells.wind.CycloneSpell;
import com.Polarice3.Goety.config.AttributesConfig;
import com.Polarice3.Goety.config.MobsConfig;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.MobUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import oshi.util.tuples.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class Sorcerer extends HuntingIllagerEntity {
    protected static final EntityDataAccessor<Byte> IS_CASTING_SPELL = SynchedEntityData.defineId(Sorcerer.class, EntityDataSerializers.BYTE);
    protected static final EntityDataAccessor<Boolean> CHARGING = SynchedEntityData.defineId(Sorcerer.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Boolean> SHOOT = SynchedEntityData.defineId(Sorcerer.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Integer> LEVEL = SynchedEntityData.defineId(Sorcerer.class, EntityDataSerializers.INT);
    protected int castingTime;
    protected int[] spellCoolDown = new int[SorcererSpell.values().length + 1];
    public int coolDown = 0;
    public boolean hasSpawned;
    public static int MIN_LEVEL = 1;
    public static int MAX_LEVEL = 5;

    public Sorcerer(EntityType<? extends HuntingIllagerEntity> p_i48551_1_, Level p_i48551_2_) {
        super(p_i48551_1_, p_i48551_2_);
    }

    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new CastingSpellGoal());
        this.goalSelector.addGoal(2, new SpellGoal());
        this.goalSelector.addGoal(3, new SurroundGoal<>(this, 1.0F, 8.0F));
    }

    public static AttributeSupplier.Builder setCustomAttributes(){
        return Mob.createMobAttributes()
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.MAX_HEALTH, AttributesConfig.SorcererHealth.get())
                .add(Attributes.ARMOR, AttributesConfig.SorcererArmor.get())
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.ATTACK_DAMAGE, AttributesConfig.SorcererDamage.get());
    }

    public void setConfigurableAttributes(){
        MobUtil.setBaseAttributes(this.getAttribute(Attributes.MAX_HEALTH), AttributesConfig.SorcererHealth.get());
        MobUtil.setBaseAttributes(this.getAttribute(Attributes.ARMOR), AttributesConfig.SorcererArmor.get());
        MobUtil.setBaseAttributes(this.getAttribute(Attributes.ATTACK_DAMAGE), AttributesConfig.SorcererDamage.get());
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_CASTING_SPELL, (byte)0);
        this.entityData.define(CHARGING, false);
        this.entityData.define(SHOOT, false);
        this.entityData.define(LEVEL, 1);
    }

    public void readAdditionalSaveData(CompoundTag p_33732_) {
        super.readAdditionalSaveData(p_33732_);
        if (p_33732_.contains("Level")){
            boolean heal = !p_33732_.getBoolean("HasSpawned");
            this.setLevels(p_33732_.getInt("Level"), heal);
        }
        this.castingTime = p_33732_.getInt("SorcererSpellTicks");
    }

    public void addAdditionalSaveData(CompoundTag p_33734_) {
        super.addAdditionalSaveData(p_33734_);
        p_33734_.putInt("Level", this.getLevels());
        p_33734_.putInt("SorcererSpellTicks", this.castingTime);
        p_33734_.putBoolean("HasSpawned", this.hasSpawned);
    }

    public AbstractIllager.IllagerArmPose getArmPose() {
        if (this.isShoot()){
            return IllagerArmPose.CROSSBOW_HOLD;
        } else if (this.isCharging()){
            return IllagerArmPose.ATTACKING;
        } else if (this.isCastingSpell2()) {
            return IllagerArmPose.SPELLCASTING;
        } else {
            return this.isCelebrating() ? IllagerArmPose.CELEBRATING : IllagerArmPose.CROSSED;
        }
    }

    public boolean isCastingSpell() {
        return false;
    }

    public boolean isCastingSpell2() {
        if (this.level.isClientSide) {
            return this.entityData.get(IS_CASTING_SPELL) > 0;
        } else {
            return this.castingTime > 0;
        }
    }

    public void setIsCastingSpell(int id) {
        this.entityData.set(IS_CASTING_SPELL, (byte)id);
    }

    public boolean isCharging(){
        return this.entityData.get(CHARGING);
    }

    public void setCharging(boolean charging){
        this.entityData.set(CHARGING, charging);
    }

    public boolean isShoot(){
        return this.entityData.get(SHOOT);
    }

    public void setShoot(boolean shoot){
        this.entityData.set(SHOOT, shoot);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_37856_, DifficultyInstance p_37857_, MobSpawnType p_37858_, @Nullable SpawnGroupData p_37859_, @Nullable CompoundTag p_37860_) {
        SpawnGroupData data = super.finalizeSpawn(p_37856_, p_37857_, p_37858_, p_37859_, p_37860_);
        this.setLevels(1 + p_37856_.getRandom().nextInt(1 + (int) p_37857_.getEffectiveDifficulty()), true);
        return data;
    }

    @Override
    public void heal(float p_21116_) {
        if (this.getLevels() >= 2){
            p_21116_ *= 1.5F;
        }
        super.heal(p_21116_);
    }

    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.castingTime > 0) {
            --this.castingTime;
        }

    }

    @Override
    public void tick() {
        super.tick();
        if (!this.hasSpawned){
            this.hasSpawned = true;
        }
        if (!this.level.isClientSide){
            for (SorcererSpell spell : SorcererSpell.values()){
                if (this.spellCoolDown[spell.trueId] > 0){
                    --this.spellCoolDown[spell.trueId];
                }
            }
            if (this.coolDown > 0){
                --this.coolDown;
            }
        }
    }

    protected int getSpellCastingTime() {
        return this.castingTime;
    }

    public boolean isAlliedTo(Entity pEntity) {
        if (pEntity == this) {
            return true;
        } else if (super.isAlliedTo(pEntity)) {
            return true;
        } else if (pEntity instanceof Vex vex && vex.getOwner() != null) {
            return this.isAlliedTo(vex.getOwner());
        } else if (pEntity instanceof LivingEntity && ((LivingEntity)pEntity).getMobType() == MobType.ILLAGER) {
            return this.getTeam() == null && pEntity.getTeam() == null;
        } else {
            return false;
        }
    }

    public int getLevels(){
        return this.entityData.get(LEVEL);
    }

    public void setLevels(int level, boolean heal) {
        int i = Mth.clamp(level, 1, 5);
        this.entityData.set(LEVEL, i);
        if (MobsConfig.SorcererHPIncrease.get()) {
            AttributeInstance health = this.getAttribute(Attributes.MAX_HEALTH);
            if (health != null && i > 1) {
                float increase = (i - 1) * 1.25F;
                health.setBaseValue(AttributesConfig.SorcererHealth.get() * increase);
            }
            if (heal) {
                this.setHealth(this.getMaxHealth());
            }
        }

        this.xpReward = i * 8;
    }

    protected SoundEvent getAmbientSound() {
        return ModSounds.SORCERER_AMBIENT.get();
    }

    protected SoundEvent getDeathSound() {
        return ModSounds.SORCERER_DEATH.get();
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return ModSounds.SORCERER_HURT.get();
    }

    @Override
    protected SoundEvent getCastingSoundEvent() {
        return ModSounds.CAST_SPELL.get();
    }

    @Override
    public void applyRaidBuffs(int pWave, boolean p_37845_) {
        Raid raid = this.getCurrentRaid();
        if (raid != null) {
            if (pWave >= raid.getNumGroups(Difficulty.EASY)) {
                this.setLevels(this.getLevels() + 1, true);
            } else if (pWave >= raid.getNumGroups(Difficulty.NORMAL)) {
                this.setLevels(this.getLevels() + 2, true);
            } else if (pWave > raid.getNumGroups(Difficulty.HARD)) {
                this.setLevels(5, true);
            }

        }
    }

    public void upgradeAssault(int sePercent){
        int level = this.random.nextInt(2);
        if (sePercent >= 75) {
            this.setLevels(level + 3, true);
        } else if (sePercent >= 50){
            this.setLevels(level + 2, true);
        } else if (sePercent >= 25){
            this.setLevels(level + 1, true);
        } else {
            this.setLevels(level, true);
        }
    }

    @Override
    public SoundEvent getCelebrateSound() {
        return ModSounds.SORCERER_AMBIENT.get();
    }

    @Override
    public void handleEntityEvent(byte p_21375_) {
        if (p_21375_ == 4){
            this.setCharging(true);
        } else if (p_21375_ == 5){
            this.setCharging(false);
        } else if (p_21375_ == 6){
            this.setShoot(true);
        } else if (p_21375_ == 7){
            this.setShoot(false);
        } else {
            super.handleEntityEvent(p_21375_);
        }
    }

    class CastingSpellGoal extends Goal {
        private CastingSpellGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        public boolean canUse() {
            return Sorcerer.this.getSpellCastingTime() > 0;
        }

        public void start() {
            super.start();
            Sorcerer.this.navigation.stop();
        }

        public void stop() {
            super.stop();
            Sorcerer.this.setIsCastingSpell(0);
            Sorcerer.this.level.broadcastEntityEvent(Sorcerer.this, (byte) 5);
            Sorcerer.this.level.broadcastEntityEvent(Sorcerer.this, (byte) 7);
            Sorcerer.this.coolDown = 20;
        }

        public void tick() {
            if (Sorcerer.this.getTarget() != null) {
                MobUtil.instaLook(Sorcerer.this, Sorcerer.this.getTarget());
            }
            Sorcerer.this.getNavigation().stop();
            Sorcerer.this.getMoveControl().strafe(0.0F, 0.0F);

        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }
    }

    protected abstract class SorcererUseSpellGoal extends Goal {
        protected int attackWarmupDelay;

        public boolean canUse() {
            LivingEntity livingentity = Sorcerer.this.getTarget();
            if (livingentity != null && livingentity.isAlive()) {
                return !Sorcerer.this.isCastingSpell2() && Sorcerer.this.hasLineOfSight(livingentity) && Sorcerer.this.coolDown <= 0;
            } else {
                return false;
            }
        }

        public boolean canContinueToUse() {
            LivingEntity livingentity = Sorcerer.this.getTarget();
            return livingentity != null && livingentity.isAlive() && this.attackWarmupDelay > 0;
        }

        public void start() {
            this.attackWarmupDelay = this.adjustedTickDelay(this.getCastWarmupTime());
            Sorcerer.this.castingTime = this.getCastingTime();
            Sorcerer.this.spellCoolDown[this.getSpell().trueId] = this.getCastingInterval();
            SoundEvent soundevent = this.getSpellPrepareSound();
            if (soundevent != null) {
                Sorcerer.this.playSound(soundevent, 1.0F, 1.0F);
            }
            Sorcerer.this.setIsCastingSpell(this.getSpell().trueId);
        }

        public void tick() {
            --this.attackWarmupDelay;
            if (this.attackWarmupDelay == 0) {
                this.performSpellCasting();
            }

        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        protected abstract void performSpellCasting();

        protected int getCastWarmupTime() {
            return 20;
        }

        protected abstract int getCastingTime();

        protected abstract int getCastingInterval();

        @Nullable
        protected abstract SoundEvent getSpellPrepareSound();

        protected abstract SorcererSpell getSpell();
    }

    class SpellGoal extends SorcererUseSpellGoal{
        public SorcererSpell spell;
        public int chargeTicks;

        @Override
        public boolean canUse() {
            List<SorcererSpell> spells = new ArrayList<>();
            for (SorcererSpell spell1 : SorcererSpell.values()){
                if (Sorcerer.this.getLevels() >= spell1.minLevel && Sorcerer.this.getLevels() <= spell1.maxLevel) {
                    if (spell1.getSpell().conditionsMet(Sorcerer.this.level, Sorcerer.this)) {
                        if (Sorcerer.this.spellCoolDown[spell1.trueId] <= 0) {
                            if (spell1.getSpell() instanceof SummonSpell && !Sorcerer.this.hasEffect(GoetyEffects.SUMMON_DOWN.get())) {
                                spells.add(spell1);
                            } else if (!(spell1.getSpell() instanceof SummonSpell)) {
                                spells.add(spell1);
                            }
                        }
                    }
                }
            }
            if (!spells.isEmpty()) {
                this.spell = spells.get(Sorcerer.this.random.nextInt(spells.size()));
            } else {
                this.spell = null;
            }
            if (this.spell != null && this.spell.getSpell() instanceof IChargingSpell){
                this.chargeTicks = 20;
            }
            return this.spell != null && super.canUse();
        }

        public void tick() {
            super.tick();
            if (this.spell.getSpell() instanceof IChargingSpell){
                if (!this.spell.getSpell().conditionsMet(Sorcerer.this.level, Sorcerer.this)){
                    this.cancelSpell();
                }
                --this.chargeTicks;
                if (this.chargeTicks <= 0) {
                    Spell spell1 = this.spell.getSpell();
                    SpellStat spellStat = spell1.defaultStats();
                    if (this.spell.levelIncrease){
                        spellStat.setPotency(Sorcerer.this.getLevels() - this.spell.minLevel);
                    }
                    spell1.SpellResult(Sorcerer.this, Sorcerer.this.getLevels() >= this.spell.upgradeStaff.getB() ? this.spell.upgradeStaff.getA() : ItemStack.EMPTY, spellStat);
                    if (this.spell.getSpell() instanceof IBreathingSpell breathingSpell) {
                        if (Sorcerer.this.getTarget() != null) {
                            MobUtil.instaLook(Sorcerer.this, Sorcerer.this.getTarget());
                        }
                        breathingSpell.showWandBreath(Sorcerer.this);
                    }
                }
                Sorcerer.this.level.broadcastEntityEvent(Sorcerer.this, (byte) 4);
            } else {
                Sorcerer.this.level.broadcastEntityEvent(Sorcerer.this, (byte) 5);
                if (this.spell.throwingSpell()){
                    Sorcerer.this.level.broadcastEntityEvent(Sorcerer.this, (byte) 6);
                } else {
                    Sorcerer.this.level.broadcastEntityEvent(Sorcerer.this, (byte) 7);
                }
            }
            this.spell.getSpell().useParticle(Sorcerer.this.level, Sorcerer.this, ItemStack.EMPTY);
        }

        public void cancelSpell() {
            this.attackWarmupDelay = 0;
            Sorcerer.this.castingTime = 0;
            Sorcerer.this.setIsCastingSpell(0);
            Sorcerer.this.level.broadcastEntityEvent(Sorcerer.this, (byte) 5);
            Sorcerer.this.level.broadcastEntityEvent(Sorcerer.this, (byte) 7);
            Sorcerer.this.coolDown = 20;
        }

        @Override
        protected void performSpellCasting() {
            if (Sorcerer.this.getTarget() != null){
                Spell spell1 = this.spell.getSpell();
                SpellStat spellStat = spell1.defaultStats();
                if (this.spell.levelIncrease){
                    spellStat.setPotency(Sorcerer.this.getLevels() - this.spell.minLevel);
                }
                if (this.spell.throwingSpell()){
                    Sorcerer.this.level.broadcastEntityEvent(Sorcerer.this, (byte) 6);
                } else {
                    Sorcerer.this.level.broadcastEntityEvent(Sorcerer.this, (byte) 7);
                }
                spell1.SpellResult(Sorcerer.this, Sorcerer.this.getLevels() >= this.spell.upgradeStaff.getB() ? this.spell.upgradeStaff.getA() : ItemStack.EMPTY, spellStat);
            }
        }

        @Override
        protected int getCastWarmupTime() {
            if (this.spell.getSpell() instanceof IChargingSpell chargingSpell){
                return chargingSpell.shotsNumber();
            }
            return this.spell.getSpell().defaultCastDuration() + 5;
        }

        protected int getCastingTime() {
            if (this.spell.getSpell() instanceof IChargingSpell chargingSpell){
                return chargingSpell.shotsNumber();
            }
            return this.spell.getSpell().defaultCastDuration() + 5;
        }

        protected int getCastingInterval() {
            if (this.spell.getSpell() instanceof IChargingSpell chargingSpell){
                return chargingSpell.defaultSpellCooldown() * 2;
            }
            return this.spell.getSpell().defaultSpellCooldown();
        }

        @Nullable
        @Override
        protected SoundEvent getSpellPrepareSound() {
            return this.spell.getSpell().CastingSound();
        }

        @Override
        protected SorcererSpell getSpell() {
            return this.spell;
        }
    }

    protected enum SorcererSpell {
        FLAMES(new FireBreathSpell(), nextID(), 1, 3),
        IRON_HIDE(new IronHideSpell(), nextID(), 1, 5, true),
        SUMMON_HOUND(new HuntingSpell(), nextID(), 1, 1),
        HEAL(new SoulHealSpell(), nextID(), 1, 5),
        FROST(new FrostBreathSpell(), nextID(), 2, 3),
        SUMMON_BEAR(new MaulingSpell(), nextID(), 2, 3, true),
        FANGS(new FangSpell(), nextID(), 2, 5, true),
        SUMMON_ICE_GOLEM(new IceGolemSpell(), nextID(), 3, 5, true),
        ICE_SPIKE(new IceSpikeSpell(), nextID(), 3, 5, true),
        THUNDERBOLT(new ThunderboltSpell(), nextID(), 3, 5, true, new Pair<>(new ItemStack(ModItems.STORM_STAFF.get()), 4)),
        SCATTER(new ScatterSpell(), nextID(), 3, 5, true),
        ICE_STORM(new IceStormSpell(), nextID(), 4, 5, true),
        BULWARK(new BulwarkSpell(), nextID(), 4, 5),
        ELECTRO(new ElectroOrbSpell(), nextID(), 4, 5, true),
        BLINK(new BlinkSpell(), nextID(), 4, 5),
        ARROW_RAIN(new ArrowRainSpell(), nextID(), 5, 5),
        VEX(new VexSpell(), nextID(), 5, 5),
        CYCLONE(new CycloneSpell(), nextID(), 5, 5),
        ERUPTION(new EruptionSpell(), nextID(), 5, 5);

        final Spell spell;
        private static int id = 0;
        final int trueId;
        final int minLevel;
        final int maxLevel;
        final boolean levelIncrease;
        final Pair<ItemStack, Integer> upgradeStaff;

        public static int nextID() {
            return id++;
        }

        SorcererSpell(Spell spell, int id, int minLevel, int maxLevel){
            this(spell, id, minLevel, maxLevel, false, new Pair<>(ItemStack.EMPTY, 0));
        }

        SorcererSpell(Spell spell, int id, int minLevel, int maxLevel, boolean levelIncrease){
            this(spell, id, minLevel, maxLevel, levelIncrease, new Pair<>(ItemStack.EMPTY, 0));
        }

        SorcererSpell(Spell spell, int id, int minLevel, int maxLevel, boolean levelIncrease, Pair<ItemStack, Integer> upgradeStaff) {
            this.spell = spell;
            this.trueId = id;
            this.minLevel = minLevel;
            this.maxLevel = maxLevel;
            this.levelIncrease = levelIncrease;
            this.upgradeStaff = upgradeStaff;
        }

        public Spell getSpell() {
            return this.spell;
        }

        public boolean throwingSpell(){
            return this == ICE_SPIKE || this == THUNDERBOLT || this == ELECTRO || this == CYCLONE;
        }
    }
}
