package com.Polarice3.Goety.common.magic.spells.nether;

import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.client.particles.ModParticleTypes;
import com.Polarice3.Goety.client.particles.ShockwaveParticleOption;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.List;

public class FireBlastSpell extends Spell {

    @Override
    public SpellStat defaultStats() {
        return super.defaultStats().setRadius(3.0D);
    }

    @Override
    public int defaultSoulCost() {
        return SpellConfig.FireBlastCost.get();
    }

    @Override
    public int defaultCastDuration() {
        return SpellConfig.FireBlastDuration.get();
    }

    @Override
    public int defaultSpellCooldown() {
        return SpellConfig.FireBlastCoolDown.get();
    }

    @Override
    public SoundEvent CastingSound() {
        return ModSounds.PREPARE_SPELL.get();
    }

    @Override
    public SpellType getSpellType() {
        return SpellType.NETHER;
    }

    @Override
    public List<Enchantment> acceptedEnchantments() {
        List<Enchantment> list = new ArrayList<>();
        list.add(ModEnchantments.POTENCY.get());
        list.add(ModEnchantments.RADIUS.get());
        list.add(ModEnchantments.BURNING.get());
        return list;
    }

    public void SpellResult(ServerLevel worldIn, LivingEntity caster, ItemStack staff, SpellStat spellStat){
        int radius = (int) spellStat.getRadius();
        float damage = SpellConfig.FireBlastDamage.get().floatValue() * SpellConfig.SpellDamageMultiplier.get();
        float maxDamage = SpellConfig.FireBlastMaxDamage.get().floatValue() * SpellConfig.SpellDamageMultiplier.get();
        if (WandUtil.enchantedFocus(caster)){
            radius += WandUtil.getLevels(ModEnchantments.RADIUS.get(), caster);
            damage += WandUtil.getLevels(ModEnchantments.POTENCY.get(), caster) / 2.0F;
            maxDamage += WandUtil.getLevels(ModEnchantments.POTENCY.get(), caster) / 2.0F;
        }
        damage += spellStat.getPotency();
        maxDamage += spellStat.getPotency();
        for (int i = -radius; i < radius; ++i){
            for (int k = -radius; k < radius; ++k){
                BlockPos blockPos = caster.blockPosition().offset(i, 0, k);
                worldIn.sendParticles(ModParticleTypes.BIG_FIRE.get(), blockPos.getX(), blockPos.getY() + 0.5F, blockPos.getZ(), 0, 0, 0.04D, 0, 0.5F);
            }
        }
        ColorUtil colorUtil = new ColorUtil(0xdd9c16);
        worldIn.sendParticles(new ShockwaveParticleOption(colorUtil.red(), colorUtil.green(), colorUtil.blue()), caster.getX(), caster.getY() + 0.5F, caster.getZ(), 0, 0, 0, 0, 0);
        worldIn.sendParticles(ModParticleTypes.ELECTRIC_EXPLODE.get(), caster.getX(), caster.getY() + 0.5F, caster.getZ(), 0, colorUtil.red(), colorUtil.green(), colorUtil.blue(), 1.0F);
        float trueDamage = Mth.clamp(damage + worldIn.random.nextInt((int) (maxDamage - damage)), damage, maxDamage);

        DamageSource damageSource = ModDamageSource.fireBreath(caster, caster);
        if (CuriosFinder.hasNetherRobe(caster)){
            damageSource = ModDamageSource.magicFireBreath(caster, caster);
        }
        if (caster instanceof OwnableEntity ownable && ownable.getOwner() != null){
            if (CuriosFinder.hasNetherRobe(ownable.getOwner())){
                damageSource = ModDamageSource.magicFireBreath(caster, caster);
            }
        }
        if (CuriosFinder.hasUnholySet(caster)){
            damageSource = ModDamageSource.hellfire(caster, caster);
        }

        new SpellExplosion(worldIn, caster, damageSource, caster.blockPosition(), radius, trueDamage){
            @Override
            public void explodeHurt(Entity target, DamageSource damageSource, double x, double y, double z, double seen, float actualDamage) {
                if (target instanceof LivingEntity target1){
                    super.explodeHurt(target, damageSource, x, y, z, seen, actualDamage);
                    int i = WandUtil.getLevels(ModEnchantments.BURNING.get(), caster) + 1;
                    target1.setSecondsOnFire(5 * i);
                }
            }
        };
        worldIn.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.GENERIC_EXPLODE, this.getSoundSource(), 2.0F, 1.0F);
        worldIn.playSound(null, caster.getX(), caster.getY(), caster.getZ(), ModSounds.HELL_BLAST_IMPACT.get(), this.getSoundSource(), 2.0F, 1.0F);
    }
}
