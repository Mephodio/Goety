package com.Polarice3.Goety.common.magic.spells.wind;

import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.entities.projectiles.AbstractCyclone;
import com.Polarice3.Goety.common.entities.projectiles.Cyclone;
import com.Polarice3.Goety.common.entities.projectiles.FireTornado;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.CuriosFinder;
import com.Polarice3.Goety.utils.WandUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class CycloneSpell extends Spell {

    @Override
    public int defaultSoulCost() {
        return SpellConfig.CycloneCost.get();
    }

    @Override
    public int defaultCastDuration() {
        return SpellConfig.CycloneDuration.get();
    }

    @Override
    public SoundEvent CastingSound() {
        return ModSounds.WIND.get();
    }

    @Override
    public int defaultSpellCooldown() {
        return SpellConfig.CycloneCoolDown.get();
    }

    @Override
    public SpellType getSpellType() {
        return SpellType.WIND;
    }

    @Override
    public List<Enchantment> acceptedEnchantments() {
        List<Enchantment> list = new ArrayList<>();
        list.add(ModEnchantments.POTENCY.get());
        list.add(ModEnchantments.RADIUS.get());
        list.add(ModEnchantments.DURATION.get());
        list.add(ModEnchantments.VELOCITY.get());
        return list;
    }

    @Override
    public void SpellResult(ServerLevel worldIn, LivingEntity caster, ItemStack staff, SpellStat spellStat) {
        Vec3 vector3d = caster.getViewVector( 1.0F);
        Vec3 vec3 = vector3d;
        LivingEntity livingEntity = this.getTarget(caster);
        if (livingEntity != null) {
            double d1 = livingEntity.getX() - caster.getX();
            double d2 = livingEntity.getY(0.5D) - caster.getY(0.5D);
            double d3 = livingEntity.getZ() - caster.getZ();
            vec3 = new Vec3(d1, d2, d3);
        }
        AbstractCyclone cyclone = new Cyclone(worldIn,
                caster,
                vec3.x,
                vec3.y,
                vec3.z);
        if (CuriosFinder.hasUnholySet(caster)){
            cyclone = new FireTornado(worldIn,
                    caster,
                    vec3.x,
                    vec3.y,
                    vec3.z);
        }
        cyclone.setOwner(caster);
        if (livingEntity != null) {
            cyclone.setTarget(livingEntity);
        }
        float size = 1.0F;
        float damage = 0.0F;
        if (rightStaff(staff)){
            size += 1.0F;
            damage += 1.0F;
        }
        cyclone.setDamage(damage * (WandUtil.getLevels(ModEnchantments.POTENCY.get(), caster) + 1));
        cyclone.setTotalLife(600 * (WandUtil.getLevels(ModEnchantments.DURATION.get(), caster) + 1));
        cyclone.setBoltSpeed(WandUtil.getLevels(ModEnchantments.VELOCITY.get(), caster));
        cyclone.setSize(size + (WandUtil.getLevels(ModEnchantments.RADIUS.get(), caster) / 10.0F));
        cyclone.setPos(caster.getX() + vector3d.x / 2,
                caster.getEyeY() - 0.2,
                caster.getZ() + vector3d.z / 2);
        worldIn.addFreshEntity(cyclone);
        worldIn.playSound(null, caster.getX(), caster.getY(), caster.getZ(), CastingSound(), this.getSoundSource(), 1.0F, 1.0F);
    }
}
