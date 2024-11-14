package com.Polarice3.Goety.common.magic.spells.storm;

import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.effects.GoetyEffects;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.common.network.ModNetwork;
import com.Polarice3.Goety.common.network.server.SThunderBoltPacket;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.entity.PartEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ThunderboltSpell extends Spell {
    @Override
    public int defaultSoulCost() {
        return SpellConfig.ThunderboltCost.get();
    }

    @Override
    public int defaultCastDuration() {
        return SpellConfig.ThunderboltDuration.get();
    }

    @Override
    public SoundEvent CastingSound() {
        return ModSounds.ZAP.get();
    }

    @Override
    public int defaultSpellCooldown() {
        return SpellConfig.ThunderboltCoolDown.get();
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

    @Override
    public void SpellResult(ServerLevel worldIn, LivingEntity caster, ItemStack staff, SpellStat spellStat) {
        int range = spellStat.getRange();
        float damage = SpellConfig.ThunderboltDamage.get().floatValue() * SpellConfig.SpellDamageMultiplier.get();
        if (WandUtil.enchantedFocus(caster)) {
            range += WandUtil.getLevels(ModEnchantments.RANGE.get(), caster);
            damage += WandUtil.getLevels(ModEnchantments.POTENCY.get(), caster);
        }
        damage += spellStat.getPotency();
        Vec3 vec3 = caster.getEyePosition();
        BlockHitResult rayTraceResult = this.blockResult(worldIn, caster, range);
        Entity target = MobUtil.getNearbyTarget(worldIn, caster, range, 1.0F);
        Optional<BlockPos> lightningRod = BlockFinder.findLightningRod(worldIn, BlockPos.containing(rayTraceResult.getLocation()), range);
        if (lightningRod.isPresent() && !rightStaff(staff)){
            BlockPos blockPos = lightningRod.get();
            ModNetwork.sendToALL(new SThunderBoltPacket(vec3, new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ()), 10));
            worldIn.playSound(null, caster.getX(), caster.getY(), caster.getZ(), ModSounds.THUNDERBOLT.get(), this.getSoundSource(), 1.0F, 1.0F);
        } else {
            LivingEntity livingEntity = null;
            if (target instanceof PartEntity<?> partEntity && partEntity.getParent() instanceof LivingEntity parent){
                livingEntity = parent;
            } else if (target instanceof LivingEntity target1 && !target1.isDeadOrDying()){
                livingEntity = target1;
            }
            if (livingEntity != null && ForgeHooks.onLivingAttack(livingEntity, ModDamageSource.directShock(caster), damage)) {
                Vec3 vec31 = new Vec3(target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ());
                ModNetwork.sendToALL(new SThunderBoltPacket(vec3, vec31, 10));
                if (target.hurt(ModDamageSource.directShock(caster), damage)){
                    float chance = rightStaff(staff) ? 0.25F : 0.05F;
                    float chainDamage = damage / 2.0F;
                    if (worldIn.isThundering() && worldIn.isRainingAt(target.blockPosition())){
                        chance += 0.25F;
                        chainDamage = damage;
                    }
                    if (worldIn.random.nextFloat() <= chance){
                        livingEntity.addEffect(new MobEffectInstance(GoetyEffects.SPASMS.get(), MathHelper.secondsToTicks(5)));
                    }
                    if (rightStaff(staff)){
                        WandUtil.chainLightning(livingEntity, caster, range / 4.0D, chainDamage);
                    }
                }
                worldIn.playSound(null, caster.getX(), caster.getY(), caster.getZ(), ModSounds.THUNDERBOLT.get(), this.getSoundSource(), 1.0F, 1.0F);
            } else {
                BlockPos blockPos = rayTraceResult.getBlockPos();
                ModNetwork.sendToALL(new SThunderBoltPacket(vec3, new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ()), 10));
                worldIn.playSound(null, caster.getX(), caster.getY(), caster.getZ(), ModSounds.THUNDERBOLT.get(), this.getSoundSource(), 1.0F, 1.0F);
            }
        }
    }
}
