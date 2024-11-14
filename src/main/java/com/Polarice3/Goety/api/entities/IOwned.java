package com.Polarice3.Goety.api.entities;

import com.Polarice3.Goety.common.effects.GoetyEffects;
import com.Polarice3.Goety.common.entities.boss.Apostle;
import com.Polarice3.Goety.config.MobsConfig;
import com.Polarice3.Goety.init.ModTags;
import com.Polarice3.Goety.utils.MobUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;

import javax.annotation.Nullable;
import java.util.UUID;

public interface IOwned {

    UUID SPEED_MODIFIER_UUID = UUID.fromString("9c47949c-b896-4802-8e8a-f08c50791a8a");

    AttributeModifier SPEED_MODIFIER = new AttributeModifier(SPEED_MODIFIER_UUID, "Staying speed penalty", -1.0D, AttributeModifier.Operation.ADDITION);

    LivingEntity getTrueOwner();

    UUID getOwnerId();

    void setOwnerId(@Nullable UUID p_184754_1_);

    default void setOwnerClientId(int id) {
    }

    void setTrueOwner(@Nullable LivingEntity livingEntity);

    void setHostile(boolean hostile);

    boolean isHostile();

    @Nullable
    default EntityType<?> getVariant(Level level, BlockPos blockPos){
        return null;
    };

    default LivingEntity getMasterOwner(){
        if (this.getTrueOwner() instanceof IOwned owned){
            return owned.getTrueOwner();
        } else {
            return this.getTrueOwner();
        }
    }

    default int getOwnerClientId(){
        return -1;
    }

    default void convertNewEquipment(Entity entity) {
    }

    default void setLimitedLife(int limitedLifeTicksIn) {
        this.setHasLifespan(true);
        this.setLifespan(limitedLifeTicksIn);
    }

    default void setHasLifespan(boolean lifespan){
    }

    default boolean hasLifespan(){
        return false;
    }

    default void setLifespan(int lifespan){
    }

    default int getLifespan(){
        return 0;
    }

    default void setNatural(boolean natural){
    }

    default boolean isNatural(){
        return false;
    }

    default boolean isChargingCrossbow() {
        return false;
    }

    default boolean isFamiliar(){
        return false;
    }

    default void checkHostility() {
        if (this instanceof Entity entity) {
            if (!entity.level.isClientSide) {
                if (this.getTrueOwner() instanceof Enemy) {
                    this.setHostile(true);
                }
                if (this.getTrueOwner() instanceof IOwned owned) {
                    if (owned.isHostile()) {
                        this.setHostile(true);
                    }
                }
                if (this instanceof Enemy) {
                    this.setHostile(true);
                }
            }
        }
    }

    default void ownedTick(){
        if (this instanceof Mob mob) {
            if (!mob.level.isClientSide) {
                if (!mob.hasEffect(GoetyEffects.WILD_RAGE.get())) {
                    if (mob.getTarget() instanceof IOwned ownedEntity) {
                        if (this.getTrueOwner() != null && (ownedEntity.getTrueOwner() == this.getTrueOwner())) {
                            mob.setTarget(null);
                            if (mob.getLastHurtByMob() == ownedEntity) {
                                mob.setLastHurtByMob(null);
                            }
                        }
                        if (ownedEntity.getTrueOwner() == this) {
                            mob.setTarget(null);
                            if (mob.getLastHurtByMob() == ownedEntity) {
                                mob.setLastHurtByMob(null);
                            }
                        }
                        if (MobUtil.ownerStack(this, ownedEntity)) {
                            mob.setTarget(null);
                            if (mob.getLastHurtByMob() == ownedEntity) {
                                mob.setLastHurtByMob(null);
                            }
                        }
                    }
                    if (this.getTrueOwner() != null) {
                        if (mob.getLastHurtByMob() == this.getTrueOwner()) {
                            mob.setLastHurtByMob(null);
                        }
                    }
                }
                if (this.getTrueOwner() != null) {
                    if (this.getTrueOwner() instanceof Mob mobOwner) {
                        if (mobOwner.getTarget() != null && mob.getTarget() == null) {
                            mob.setTarget(mobOwner.getTarget());
                        }
                        if (mobOwner instanceof Apostle apostle) {
                            if (mob.distanceTo(apostle) > 32) {
                                this.teleportTowards(apostle);
                            }
                        }
                        if (mobOwner.getType().is(Tags.EntityTypes.BOSSES) || mobOwner.getType().is(ModTags.EntityTypes.SUMMON_KILL)) {
                            if (mobOwner.isRemoved() || mobOwner.isDeadOrDying()) {
                                mob.kill();
                            }
                        }
                    }
                    if (this.getTrueOwner() instanceof IOwned owned) {
                        if (this.getTrueOwner().isDeadOrDying() || !this.getTrueOwner().isAlive()) {
                            if (owned.getTrueOwner() != null) {
                                this.setTrueOwner(owned.getTrueOwner());
                            } else if (!this.isHostile() && !this.isNatural() && !(owned instanceof Enemy) && !owned.isHostile()) {
                                mob.kill();
                            }
                        }
                    }
                    for (Mob target : mob.level.getEntitiesOfClass(Mob.class, mob.getBoundingBox().inflate(mob.getAttributeValue(Attributes.FOLLOW_RANGE)))) {
                        if (target instanceof IOwned owned) {
                            if (this.getTrueOwner() != owned.getTrueOwner()
                                    && target.getTarget() == this.getTrueOwner()) {
                                mob.setTarget(target);
                            }
                        }
                    }
                }
                if (mob.getTarget() != null) {
                    if (mob.getTarget().isRemoved() || mob.getTarget().isDeadOrDying()) {
                        mob.setTarget(null);
                    }
                }
                this.mobSense();
                if (this.hasLifespan()) {
                    this.setLifespan(this.getLifespan() - 1);
                    if (this.getLifespan() <= 0) {
                        this.lifeSpanDamage();
                    }
                }
            }
        }
    }

    default void mobSense(){
        if (this instanceof Mob owned) {
            if (MobsConfig.MobSense.get()) {
                if (owned.isAlive()) {
                    if (owned.getTarget() != null) {
                        if (owned.getTarget() instanceof Mob mob) {
                            if (owned.getTarget() instanceof Animal animal) {
                                animal.setLastHurtByMob(owned);
                            } else if (mob.getTarget() == null || mob.getTarget().isDeadOrDying()) {
                                mob.setTarget(owned);
                            }
                            if (!mob.getBrain().isActive(Activity.FIGHT) && !(mob instanceof Warden)) {
                                mob.getBrain().setMemoryWithExpiry(MemoryModuleType.ANGRY_AT, owned.getUUID(), 600L);
                                mob.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_TARGET, owned, 600L);
                            }
                        }
                    }
                }
            }
        }
    }

    default void lifeSpanDamage(){
        if (this instanceof LivingEntity owned) {
            this.setLifespan(20);
            owned.hurt(owned.damageSources().starve(), 1.0F);
        }
    }

    default void teleportTowards(Entity entity) {
        if (this instanceof LivingEntity owned) {
            if (!owned.level.isClientSide() && owned.isAlive()) {
                for (int i = 0; i < 128; ++i) {
                    Vec3 vector3d = new Vec3(owned.getX() - entity.getX(), owned.getY(0.5D) - entity.getEyeY(), owned.getZ() - entity.getZ());
                    vector3d = vector3d.normalize();
                    double d0 = 16.0D;
                    double d1 = owned.getX() + (owned.getRandom().nextDouble() - 0.5D) * 8.0D - vector3d.x * d0;
                    double d2 = owned.getY() + (double) (owned.getRandom().nextInt(16) - 8) - vector3d.y * d0;
                    double d3 = owned.getZ() + (owned.getRandom().nextDouble() - 0.5D) * 8.0D - vector3d.z * d0;
                    net.minecraftforge.event.entity.EntityTeleportEvent.EnderEntity event = net.minecraftforge.event.ForgeEventFactory.onEnderTeleport(owned, d1, d2, d3);
                    if (event.isCanceled()) {
                        break;
                    }
                    if (owned.randomTeleport(event.getTargetX(), event.getTargetY(), event.getTargetZ(), false)) {
                        this.teleportHits();
                        break;
                    }
                }
            }
        }
    }

    default void teleportHits(){
        if (this instanceof LivingEntity owned) {
            owned.level.broadcastEntityEvent(owned, (byte) 46);
            if (!owned.isSilent()) {
                owned.level.playSound((Player) null, owned.xo, owned.yo, owned.zo, SoundEvents.ENDERMAN_TELEPORT, owned.getSoundSource(), 1.0F, 1.0F);
                owned.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
            }
        }
    }

    default void readOwnedData(CompoundTag compound){
        if (compound.hasUUID("Owner")) {
            this.setOwnerId(compound.getUUID("Owner"));
        }

        if (compound.contains("OwnerClient")){
            this.setOwnerClientId(compound.getInt("OwnerClient"));
        }

        if (compound.contains("isHostile")){
            this.setHostile(compound.getBoolean("isHostile"));
        } else {
            this.checkHostility();
        }

        if (compound.contains("isNatural")){
            this.setNatural(compound.getBoolean("isNatural"));
        }

        if (compound.contains("LifeTicks")) {
            this.setLimitedLife(compound.getInt("LifeTicks"));
        }
    }

    default void saveOwnedData(CompoundTag compound){
        if (this.getOwnerId() != null) {
            compound.putUUID("Owner", this.getOwnerId());
        }
        if (this.getOwnerClientId() > -1) {
            compound.putInt("OwnerClient", this.getOwnerClientId());
        }
        if (this.isHostile()){
            compound.putBoolean("isHostile", this.isHostile());
        }
        if (this.isNatural()){
            compound.putBoolean("isNatural", this.isNatural());
        }
        if (this.hasLifespan()) {
            compound.putInt("LifeTicks", this.getLifespan());
        }
    }
}
