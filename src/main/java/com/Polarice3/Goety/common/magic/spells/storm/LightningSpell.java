package com.Polarice3.Goety.common.magic.spells.storm;

import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.BlockFinder;
import com.Polarice3.Goety.utils.MobUtil;
import com.Polarice3.Goety.utils.WandUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LightningSpell extends Spell {

    public int defaultSoulCost() {
        return SpellConfig.LightningCost.get();
    }

    public int defaultCastDuration() {
        return SpellConfig.LightningDuration.get();
    }

    public SoundEvent CastingSound() {
        return ModSounds.PREPARE_SPELL.get();
    }

    @Override
    public int defaultSpellCooldown() {
        return SpellConfig.LightningCoolDown.get();
    }

    @Override
    public SpellType getSpellType() {
        return SpellType.STORM;
    }

    @Override
    public List<Enchantment> acceptedEnchantments() {
        List<Enchantment> list = new ArrayList<>();
        list.add(ModEnchantments.POTENCY.get());
        list.add(ModEnchantments.RANGE.get());
        return list;
    }

    public void SpellResult(ServerLevel worldIn, LivingEntity caster, ItemStack staff, SpellStat spellStat){
        double radius = spellStat.getRadius();
        int range = spellStat.getRange();
        float damage = SpellConfig.LightningDamage.get().floatValue() * SpellConfig.SpellDamageMultiplier.get();
        if (WandUtil.enchantedFocus(caster)) {
            range += WandUtil.getLevels(ModEnchantments.RANGE.get(), caster);
            damage += WandUtil.getLevels(ModEnchantments.POTENCY.get(), caster);
        }
        damage += spellStat.getPotency();
        HitResult rayTraceResult = this.rayTrace(worldIn, caster, range, radius);
        Optional<BlockPos> lightningRod = BlockFinder.findLightningRod(worldIn, BlockPos.containing(rayTraceResult.getLocation()));
        if (lightningRod.isPresent() && !rightStaff(staff)){
            BlockPos blockPos = lightningRod.get();
            LightningBolt lightningBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, worldIn);
            lightningBolt.setDamage(damage);
            lightningBolt.moveTo(Vec3.atBottomCenterOf(blockPos));
            if (caster instanceof ServerPlayer serverPlayer) {
                lightningBolt.setCause(serverPlayer);
            }
            worldIn.addFreshEntity(lightningBolt);
        } else {
            LivingEntity target = this.getTarget(caster, range);
            if (target != null){
                LightningBolt lightningBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, worldIn);
                lightningBolt.setDamage(damage);
                lightningBolt.setPos(target.position());
                if (caster instanceof ServerPlayer serverPlayer) {
                    lightningBolt.setCause(serverPlayer);
                }
                worldIn.addFreshEntity(lightningBolt);
            } else if (rightStaff(staff)){
                for (LivingEntity livingEntity : worldIn.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(range))){
                    if (!MobUtil.areAllies(caster, livingEntity)){
                        LightningBolt lightningBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, worldIn);
                        lightningBolt.setDamage(damage);
                        lightningBolt.setPos(livingEntity.position());
                        if (caster instanceof ServerPlayer serverPlayer) {
                            lightningBolt.setCause(serverPlayer);
                        }
                        if (worldIn.addFreshEntity(lightningBolt)) {
                            break;
                        }
                    }
                }
            } else if (rayTraceResult instanceof BlockHitResult){
                BlockPos blockPos = ((BlockHitResult) rayTraceResult).getBlockPos();
                LightningBolt lightningBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, worldIn);
                lightningBolt.setDamage(damage);
                lightningBolt.moveTo(Vec3.atBottomCenterOf(blockPos));
                if (caster instanceof ServerPlayer serverPlayer) {
                    lightningBolt.setCause(serverPlayer);
                }
                worldIn.addFreshEntity(lightningBolt);
            }
        }
    }
}
