package com.Polarice3.Goety.common.magic.spells.storm;

import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.entities.projectiles.MonsoonCloud;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.WandUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.List;

public class MonsoonSpell extends Spell {

    @Override
    public SpellStat defaultStats() {
        return super.defaultStats().setDuration(100);
    }

    public int defaultSoulCost() {
        return SpellConfig.MonsoonCost.get();
    }

    public int defaultCastDuration() {
        return SpellConfig.MonsoonDuration.get();
    }

    public SoundEvent CastingSound() {
        return ModSounds.PREPARE_SPELL.get();
    }

    @Override
    public int defaultSpellCooldown() {
        return SpellConfig.MonsoonCoolDown.get();
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
        list.add(ModEnchantments.DURATION.get());
        list.add(ModEnchantments.RADIUS.get());
        return list;
    }

    public void SpellResult(ServerLevel worldIn, LivingEntity caster, ItemStack staff, SpellStat spellStat){
        int range = spellStat.getRange();
        int duration = spellStat.getDuration();
        double radius = spellStat.getRadius();
        float potency = spellStat.getPotency();
        if (WandUtil.enchantedFocus(caster)) {
            range += WandUtil.getLevels(ModEnchantments.RANGE.get(), caster);
            duration *= WandUtil.getLevels(ModEnchantments.DURATION.get(), caster) + 1;
            potency += WandUtil.getLevels(ModEnchantments.POTENCY.get(), caster);
            radius += WandUtil.getLevels(ModEnchantments.RADIUS.get(), caster);
        }
        HitResult rayTraceResult = this.rayTrace(worldIn, caster, range, radius);
        LivingEntity target = this.getTarget(caster, range);
        if (target != null){
            MonsoonCloud monsoonCloud = new MonsoonCloud(worldIn, caster, (LivingEntity) target);
            monsoonCloud.setExtraDamage(potency);
            monsoonCloud.setRadius((float) radius);
            monsoonCloud.setLifeSpan(duration);
            monsoonCloud.setStaff(rightStaff(staff));
            worldIn.addFreshEntity(monsoonCloud);
            worldIn.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, this.getSoundSource(), 0.5F, 1.25F);
        } else if (rayTraceResult instanceof BlockHitResult){
            BlockPos blockPos = ((BlockHitResult) rayTraceResult).getBlockPos();
            MonsoonCloud monsoonCloud = new MonsoonCloud(worldIn, caster, null);
            monsoonCloud.setExtraDamage(potency);
            monsoonCloud.setRadius((float) radius);
            monsoonCloud.setLifeSpan(duration);
            monsoonCloud.setStaff(rightStaff(staff));
            monsoonCloud.setPos(blockPos.getX() + 0.5F, blockPos.getY() + 4, blockPos.getZ() + 0.5F);
            worldIn.addFreshEntity(monsoonCloud);
            worldIn.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, this.getSoundSource(), 0.5F, 1.25F);
        }
    }
}
