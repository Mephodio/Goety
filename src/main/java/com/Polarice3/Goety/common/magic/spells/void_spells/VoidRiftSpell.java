package com.Polarice3.Goety.common.magic.spells.void_spells;

import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.entities.util.VoidRift;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.WandUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class VoidRiftSpell extends Spell {

    @Override
    public SpellStat defaultStats() {
        return super.defaultStats().setDuration(300).setRadius(0.0D);
    }

    @Override
    public int defaultSoulCost() {
        return SpellConfig.RuptureCost.get();
    }

    @Override
    public int defaultCastDuration() {
        return SpellConfig.RuptureDuration.get();
    }

    @Nullable
    @Override
    public SoundEvent CastingSound() {
        return ModSounds.VOID_PREPARE_SPELL.get();
    }

    @Override
    public int defaultSpellCooldown() {
        return SpellConfig.RuptureCoolDown.get();
    }

    @Override
    public SpellType getSpellType() {
        return SpellType.VOID;
    }

    @Override
    public List<Enchantment> acceptedEnchantments() {
        List<Enchantment> list = new ArrayList<>();
        list.add(ModEnchantments.POTENCY.get());
        list.add(ModEnchantments.DURATION.get());
        list.add(ModEnchantments.RANGE.get());
        list.add(ModEnchantments.RADIUS.get());
        return list;
    }

    @Override
    public void startSpell(ServerLevel worldIn, LivingEntity caster, ItemStack staff, SpellStat spellStat) {
        int warmUp = this.castDuration(caster) - 10;
        int duration = spellStat.getDuration() * (WandUtil.getLevels(ModEnchantments.DURATION.get(), caster) + 1);
        int range = spellStat.getRange() + WandUtil.getLevels(ModEnchantments.RANGE.get(), caster);
        Vec3 vec3 = this.rayTrace(worldIn, caster, range, 3).getLocation();
        VoidRift voidRift = new VoidRift(worldIn, vec3.x, vec3.y, vec3.z);
        voidRift.setOwner(caster);
        voidRift.setDuration(duration);
        voidRift.setWarmUp(warmUp);
        voidRift.setSize((float) (spellStat.getRadius() + WandUtil.getLevels(ModEnchantments.RADIUS.get(), caster)));
        voidRift.setExtraDamage(spellStat.getPotency() + WandUtil.getLevels(ModEnchantments.POTENCY.get(), caster));
        worldIn.addFreshEntity(voidRift);
    }

    @Override
    public void SpellResult(ServerLevel worldIn, LivingEntity caster, ItemStack staff){
    }
}
