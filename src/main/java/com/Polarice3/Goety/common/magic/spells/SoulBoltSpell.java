package com.Polarice3.Goety.common.magic.spells;

import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.entities.projectiles.NecroBolt;
import com.Polarice3.Goety.common.entities.projectiles.SoulBolt;
import com.Polarice3.Goety.common.entities.projectiles.SpellHurtingProjectile;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.SoundUtil;
import com.Polarice3.Goety.utils.WandUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Learned you could use this method for better projectile accuracy from codes by @Yunus1903
 */
public class SoulBoltSpell extends Spell {

    @Override
    public int defaultSoulCost() {
        return SpellConfig.SoulBoltCost.get();
    }

    @Override
    public int defaultCastDuration() {
        return SpellConfig.SoulBoltDuration.get();
    }

    @Override
    public int defaultSpellCooldown() {
        return SpellConfig.SoulBoltCoolDown.get();
    }

    @Override
    public SoundEvent CastingSound() {
        return ModSounds.CAST_SPELL.get();
    }

    @Override
    public List<Enchantment> acceptedEnchantments() {
        List<Enchantment> list = new ArrayList<>();
        list.add(ModEnchantments.POTENCY.get());
        list.add(ModEnchantments.VELOCITY.get());
        return list;
    }

    @Override
    public void SpellResult(ServerLevel worldIn, LivingEntity caster, ItemStack staff, SpellStat spellStat) {
        Vec3 vector3d = caster.getViewVector( 1.0F);
        SpellHurtingProjectile soulBolt = new SoulBolt(
                caster.getX() + vector3d.x / 2,
                caster.getEyeY() - 0.2,
                caster.getZ() + vector3d.z / 2,
                vector3d.x,
                vector3d.y,
                vector3d.z, worldIn);
        if (staff.is(ModItems.NAMELESS_STAFF.get())) {
            soulBolt = new NecroBolt(
                    caster.getX() + vector3d.x / 2,
                    caster.getEyeY() - 0.2,
                    caster.getZ() + vector3d.z / 2,
                    vector3d.x,
                    vector3d.y,
                    vector3d.z, worldIn);
            SoundUtil.playNecroBolt(caster);
        } else {
            SoundUtil.playSoulBolt(caster);
        }
        if (soulBolt instanceof SoulBolt soulBolt1){
            if (staff.is(ModItems.NECRO_STAFF.get()) || staff.is(ModItems.NAMELESS_STAFF.get())){
                soulBolt1.setNecro(true);
            }
        }
        if (WandUtil.enchantedFocus(caster)){
            soulBolt.setExtraDamage(spellStat.getPotency() + WandUtil.getLevels(ModEnchantments.POTENCY.get(), caster));
            soulBolt.setBoltSpeed((int) (spellStat.getVelocity() + WandUtil.getLevels(ModEnchantments.VELOCITY.get(), caster)));
        }
        soulBolt.setOwner(caster);
        worldIn.addFreshEntity(soulBolt);
    }
}
