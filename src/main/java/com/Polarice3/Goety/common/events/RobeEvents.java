package com.Polarice3.Goety.common.events;

import com.Polarice3.Goety.Goety;
import com.Polarice3.Goety.api.entities.ally.IServant;
import com.Polarice3.Goety.common.blocks.ModBlocks;
import com.Polarice3.Goety.common.blocks.SnapWartsBlock;
import com.Polarice3.Goety.common.effects.GoetyEffects;
import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.entities.ally.AllyIrk;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.common.world.structures.ModStructureTags;
import com.Polarice3.Goety.config.AttributesConfig;
import com.Polarice3.Goety.config.ItemConfig;
import com.Polarice3.Goety.config.MainConfig;
import com.Polarice3.Goety.config.MobsConfig;
import com.Polarice3.Goety.utils.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static net.minecraftforge.event.entity.living.LivingChangeTargetEvent.LivingTargetType.MOB_TARGET;

@Mod.EventBusSubscriber(modid = Goety.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RobeEvents {

    @SubscribeEvent
    public static void LivingEffects(LivingEvent.LivingTickEvent event){
        LivingEntity livingEntity = event.getEntity();
        if (MobsConfig.UndeadMinionHeal.get()) {
            if (livingEntity != null) {
                if (!livingEntity.level.isClientSide) {
                    if (livingEntity instanceof OwnableEntity ownable && !(livingEntity instanceof IServant)) {
                        if (ownable.getOwnerUUID() != null) {
                            Player owner = livingEntity.level.getPlayerByUUID(ownable.getOwnerUUID());
                            if (owner != null) {
                                if (livingEntity.getMobType() == MobType.UNDEAD && livingEntity.getHealth() < livingEntity.getMaxHealth() && !livingEntity.isOnFire() && !livingEntity.isDeadOrDying()) {
                                    if (CuriosFinder.hasUndeadCape(owner)) {
                                        int SoulCost = MobsConfig.UndeadMinionHealCost.get();
                                        if (SEHelper.getSoulsAmount(owner, SoulCost)) {
                                            if (livingEntity.tickCount % MathHelper.secondsToTicks(MobsConfig.UndeadMinionHealTime.get()) == 0) {
                                                livingEntity.heal(MobsConfig.UndeadMinionHealAmount.get().floatValue());
                                                Vec3 vector3d = livingEntity.getDeltaMovement();
                                                if (livingEntity.level instanceof ServerLevel serverWorld) {
                                                    SEHelper.decreaseSouls(owner, SoulCost);
                                                    serverWorld.sendParticles(ParticleTypes.SCULK_SOUL, livingEntity.getRandomX(0.5D), livingEntity.getRandomY(), livingEntity.getRandomZ(0.5D), 0, vector3d.x * -0.2D, 0.1D, vector3d.z * -0.2D, 0.5F);
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
        }
    }

    @SubscribeEvent
    public static void onBreakingBlock(BlockEvent.BreakEvent event){
        Player player = event.getPlayer();
        if (CuriosFinder.hasWarlockRobe(player)){
            if (event.getState().is(ModBlocks.SNAP_WARTS.get()) && event.getState().getValue(SnapWartsBlock.AGE) >= 2){
                if (!player.level.isClientSide) {
                    if (!player.getAbilities().instabuild) {
                        if (player.getRandom().nextFloat() <= 0.25F) {
                            Block.dropResources(event.getState(), player.level, event.getPos(), null, player, player.getUseItem());
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void HurtEvent(LivingHurtEvent event){
        LivingEntity victim = event.getEntity();
        if (CuriosFinder.hasFrostRobes(victim)){
            if (ModDamageSource.freezeAttacks(event.getSource()) || event.getSource().is(DamageTypeTags.IS_FREEZING)){
                float resistance = 1.0F - (ItemConfig.FrostRobeResistance.get() / 100.0F);
                event.setAmount(event.getAmount() * resistance);
            }
        }
        if (CuriosFinder.hasNetherRobe(victim)){
            float resistance = 1.0F - (ItemConfig.NetherRobeResistance.get() / 100.0F);
            if (resistance <= 0.0F){
                event.setCanceled(true);
            } else {
                if (event.getSource().is(DamageTypeTags.IS_FIRE) || ModDamageSource.isMagicFire(event.getSource())){
                    event.setAmount(event.getAmount() * resistance);
                }
                if (ModDamageSource.hellfireAttacks(event.getSource())){
                    resistance = Math.max(0.75F, resistance);
                    event.setAmount(event.getAmount() * resistance);
                }
            }
        }
        if (CuriosFinder.hasCurio(victim, ModItems.GRAND_TURBAN.get())){
            if (victim instanceof Player player) {
                if (SEHelper.getSoulsAmount(player, ItemConfig.ItemsRepairAmount.get())) {
                    int irks = victim.level.getEntitiesOfClass(AllyIrk.class, victim.getBoundingBox().inflate(32)).size();
                    if ((victim.level.random.nextBoolean() || victim.getHealth() <= victim.getMaxHealth() / 2) && irks < 16) {
                        AllyIrk irk = new AllyIrk(ModEntityType.ALLY_IRK.get(), victim.level);
                        irk.setPos(victim.getX(), victim.getY(), victim.getZ());
                        irk.setLimitedLife(MobUtil.getSummonLifespan(victim.level));
                        irk.setTrueOwner(victim);
                        if (victim.level.addFreshEntity(irk)) {
                            SEHelper.decreaseSouls(player, ItemConfig.ItemsRepairAmount.get());
                        }
                    }
                }
            }
        }
        if (CuriosFinder.hasCurio(victim, ModItems.GRAND_ROBE.get())){
            if (MobUtil.isSpellCasting(victim)){
                event.setAmount(event.getAmount() / 2.0F);
            }
        }
        float damage = event.getAmount();
        if (CuriosFinder.hasUnholyHat(victim)){
            if (victim.level.dimension() == Level.NETHER){
                damage /= 2.0F;
            }
            if (!event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)){
                damage = Math.min(damage, AttributesConfig.ApostleDamageCap.get().floatValue());
            }
            event.setAmount(damage);
        }
        if (CuriosFinder.hasUnholyRobe(victim)){
            float resistance = 1.0F - (ItemConfig.NetherRobeResistance.get() / 100.0F);
            if (ModDamageSource.hellfireAttacks(event.getSource())){
                resistance = Math.max(0.75F, resistance);
                damage *= resistance;
            }
            event.setAmount(damage);
        }
        if(CuriosFinder.hasCurio(victim, ModItems.STORM_ROBE.get())){
            if (ModDamageSource.shockAttacks(event.getSource()) || event.getSource().is(DamageTypes.LIGHTNING_BOLT)){
                float resistance = 1.0F - (ItemConfig.StormRobeResistance.get() / 100.0F);
                event.setAmount(event.getAmount() * resistance);
            }
            if (event.getSource().is(DamageTypes.LIGHTNING_BOLT)){
                victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300));
            }
        }
        if (CuriosFinder.hasWitchRobe(victim) || CuriosFinder.hasWarlockRobe(victim)){
            if (event.getSource().is(DamageTypeTags.WITCH_RESISTANT_TO)){
                if (!(LichdomHelper.isLich(victim) && MainConfig.LichMagicResist.get())){
                    float resistance = 1.0F - (ItemConfig.WitchRobeResistance.get() / 100.0F);
                    if (CuriosFinder.hasWarlockRobe(victim)){
                        resistance = 1.0F - (ItemConfig.WarlockRobeResistance.get() / 100.0F);
                    }
                    event.setAmount(event.getAmount() * resistance);
                }
            }
        }
    }

    @SubscribeEvent
    public static void AttackEvent(LivingAttackEvent event){
        LivingEntity victim = event.getEntity();
        Entity source = event.getSource().getEntity();
        Entity direct = event.getSource().getDirectEntity();
        if (!event.getEntity().level.isClientSide) {
            if (event.getSource().is(DamageTypeTags.IS_FIRE)){
                LivingEntity source1 = null;
                Entity direct1 = direct;
                if (source instanceof LivingEntity living1) {
                    source1 = living1;
                } else if (source instanceof OwnableEntity ownable && ownable.getOwner() != null) {
                    source1 = ownable.getOwner();
                }
                if (event.getSource() instanceof NoKnockBackDamageSource damageSource){
                    if (damageSource.getOwner() instanceof LivingEntity living1) {
                        source1 = living1;
                    } else if (damageSource.getOwner() instanceof OwnableEntity ownable && ownable.getOwner() != null) {
                        source1 = ownable.getOwner();
                    }
                    direct1 = damageSource.getDirectAttacker();
                }
                if (CuriosFinder.hasNetherRobe(source1)){
                    if (victim.isInvulnerableTo(event.getSource()) || victim.hasEffect(MobEffects.FIRE_RESISTANCE)){
                        DamageSource damageSource = ModDamageSource.magicFireBreath(direct1, source1);
                        if (CuriosFinder.hasUnholyRobe(source1)){
                            damageSource = ModDamageSource.hellfire(direct1, source1);
                        }
                        victim.hurt(damageSource, event.getAmount());
                        event.setCanceled(true);
                    }
                }
            }
        }

        if (source instanceof MagmaCube magmaCube){
            if (CuriosFinder.neutralNetherSet(victim)){
                if (magmaCube.getLastHurtByMob() != victim) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void TargetEvents(LivingChangeTargetEvent event) {
        LivingEntity attacker = event.getEntity();
        LivingEntity target = event.getOriginalTarget();
        if (attacker instanceof Mob mobAttacker) {
            if (target != null) {
                if (target instanceof Player) {
                    if (MobUtil.isWitchType(mobAttacker)) {
                        if (CuriosFinder.isWitchFriendly(target)) {
                            if (mobAttacker.getLastHurtByMob() != target) {
                                if (event.getTargetType() == MOB_TARGET) {
                                    event.setNewTarget(null);
                                } else {
                                    event.setCanceled(true);
                                }
                            } else {
                                mobAttacker.setLastHurtByMob(target);
                            }
                        }
                    }
                    if (CuriosFinder.neutralNecroSet(target) || CuriosFinder.neutralNamelessSet(target)) {
                        boolean undead = CuriosFinder.validNecroUndead(mobAttacker);
                        if (target.level instanceof ServerLevel serverLevel) {
                            if (MobsConfig.HostileCryptUndead.get()) {
                                if (BlockFinder.findStructure(serverLevel, target.blockPosition(), ModStructureTags.NECRO_HOSTILE)
                                        && !CuriosFinder.neutralNamelessSet(target)) {
                                    undead = false;
                                }
                            }
                        }
                        if (undead || (CuriosFinder.neutralNamelessSet(target) && CuriosFinder.validNamelessUndead(mobAttacker))) {
                            if (mobAttacker.getLastHurtByMob() != target) {
                                if (event.getTargetType() == MOB_TARGET) {
                                    event.setNewTarget(null);
                                } else {
                                    event.setCanceled(true);
                                }
                            } else {
                                mobAttacker.setLastHurtByMob(target);
                            }
                        }
                    }
                    if (CuriosFinder.neutralFrostSet(target)) {
                        if (CuriosFinder.validFrostMob(mobAttacker)) {
                            if (mobAttacker.getLastHurtByMob() != target) {
                                if (event.getTargetType() == MOB_TARGET) {
                                    event.setNewTarget(null);
                                } else {
                                    event.setCanceled(true);
                                }
                            } else {
                                mobAttacker.setLastHurtByMob(target);
                            }
                        }
                    }
                    if (CuriosFinder.neutralNetherSet(target)) {
                        if (CuriosFinder.validNetherMob(mobAttacker)) {
                            if (mobAttacker.getLastHurtByMob() != target) {
                                if (event.getTargetType() == MOB_TARGET) {
                                    event.setNewTarget(null);
                                } else {
                                    event.setCanceled(true);
                                }
                            } else {
                                mobAttacker.setLastHurtByMob(target);
                            }
                        }
                    }
                    if (CuriosFinder.hasUnholyRobe(target) || CuriosFinder.hasUnholyHat(target)) {
                        if (mobAttacker instanceof Ghast || mobAttacker instanceof Blaze || mobAttacker instanceof MagmaCube) {
                            if (event.getTargetType() == MOB_TARGET) {
                                event.setNewTarget(null);
                            } else {
                                event.setCanceled(true);
                            }
                        }
                    }
                    if (CuriosFinder.neutralWildSet(target)) {
                        if (CuriosFinder.validWildMob(mobAttacker)) {
                            if (mobAttacker.getLastHurtByMob() != target) {
                                if (event.getTargetType() == MOB_TARGET) {
                                    event.setNewTarget(null);
                                } else {
                                    event.setCanceled(true);
                                }
                            } else {
                                mobAttacker.setLastHurtByMob(target);
                            }
                        }
                    }
                    if (CuriosFinder.hasWarlockRobe(event.getOriginalTarget())) {
                        if (mobAttacker.getMobType() == MobType.ARTHROPOD){
                            if (mobAttacker.getLastHurtByMob() != target) {
                                if (event.getTargetType() == MOB_TARGET) {
                                    event.setNewTarget(null);
                                } else {
                                    event.setCanceled(true);
                                }
                            } else {
                                mobAttacker.setLastHurtByMob(target);
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void VisibilityEvent(LivingEvent.LivingVisibilityEvent event){
        LivingEntity entity = event.getEntity();
        if (event.getLookingEntity() instanceof LivingEntity looker && entity instanceof Player) {
            boolean undead = CuriosFinder.validNecroUndead(looker);
            if (entity.level instanceof ServerLevel serverLevel){
                if (MobsConfig.HostileCryptUndead.get()) {
                    if (BlockFinder.findStructure(serverLevel, entity.blockPosition(), ModStructureTags.NECRO_HOSTILE)
                            && !(CuriosFinder.neutralNamelessCrown(entity) || CuriosFinder.neutralNamelessCape(entity))) {
                        undead = false;
                    }
                }
            }
            if (CuriosFinder.neutralNecroCrown(entity)) {
                if (undead) {
                    event.modifyVisibility(0.5);
                }
            } else if (CuriosFinder.neutralNamelessCrown(entity)) {
                if (CuriosFinder.validNamelessUndead(looker)) {
                    event.modifyVisibility(0.5);
                }
            }
            if (CuriosFinder.neutralNecroCape(entity)) {
                if (undead) {
                    event.modifyVisibility(0.5);
                }
            } else if (CuriosFinder.neutralNamelessCape(entity)) {
                if (CuriosFinder.validNamelessUndead(looker)) {
                    event.modifyVisibility(0.5);
                }
            }
            if (ItemConfig.FrostSetMobNeutral.get()) {
                if (CuriosFinder.validFrostMob(looker)) {
                    if (CuriosFinder.hasFrostCrown(entity)) {
                        event.modifyVisibility(0.5);
                    }
                    if (CuriosFinder.hasFrostRobes(looker)) {
                        event.modifyVisibility(0.5);
                    }
                }
            }
            if (ItemConfig.NetherSetMobNeutral.get()) {
                if (CuriosFinder.validNetherMob(looker)) {
                    if (CuriosFinder.hasNetherCrown(entity)) {
                        event.modifyVisibility(0.5);
                    }
                    if (CuriosFinder.hasNetherRobe(looker)) {
                        event.modifyVisibility(0.5);
                    }
                }
            }
            if (ItemConfig.WildSetMobNeutral.get()) {
                if (CuriosFinder.validWildMob(looker)) {
                    if (CuriosFinder.hasWildCrown(entity)) {
                        event.modifyVisibility(0.5);
                    }
                    if (CuriosFinder.hasWildRobe(looker)) {
                        event.modifyVisibility(0.5);
                    }
                }
            }
            if (CuriosFinder.hasIllusionRobe(entity)){
                if (entity.isInvisible()){
                    event.modifyVisibility(0.0);
                }
            }
        }
    }

    @SubscribeEvent
    public static void OnLivingFall(LivingFallEvent event){
        if (CuriosFinder.hasWindyRobes(event.getEntity())){
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void PotionApplicationEvents(MobEffectEvent.Applicable event){
        if (event.getEffectInstance().getEffect() == GoetyEffects.FREEZING.get()){
            if (CuriosFinder.hasFrostRobes(event.getEntity())){
                event.setResult(Event.Result.DENY);
            }
        }
        if (event.getEffectInstance().getEffect() == GoetyEffects.BURN_HEX.get()){
            if (CuriosFinder.hasUnholyHat(event.getEntity()) || CuriosFinder.hasUnholyRobe(event.getEntity())){
                event.setResult(Event.Result.DENY);
            }
        }
        if (event.getEffectInstance().getEffect() == MobEffects.POISON
                || event.getEffectInstance().getEffect() == GoetyEffects.ACID_VENOM.get()) {
            if (CuriosFinder.hasWildRobe(event.getEntity())) {
                event.setResult(Event.Result.DENY);
            }
        }
        if (event.getEffectInstance().getEffect() == MobEffects.BLINDNESS){
            if (CuriosFinder.hasIllusionRobe(event.getEntity())){
                event.setResult(Event.Result.DENY);
            }
        }
    }
}
