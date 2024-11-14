package com.Polarice3.Goety.common.magic.spells.storm;

import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.magic.EverChargeSpell;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.common.network.ModNetwork;
import com.Polarice3.Goety.common.network.server.SLightningPacket;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.BlockFinder;
import com.Polarice3.Goety.utils.MobUtil;
import com.Polarice3.Goety.utils.ModDamageSource;
import com.Polarice3.Goety.utils.WandUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.entity.PartEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ShockingSpell extends EverChargeSpell {

    @Override
    public SpellStat defaultStats() {
        return super.defaultStats().setRange(8);
    }

    @Override
    public int defaultSoulCost() {
        return SpellConfig.ShockingCost.get();
    }

    @Override
    public int defaultCastUp() {
        return SpellConfig.ShockingChargeUp.get();
    }

    @Override
    public int shotsNumber() {
        return SpellConfig.ShockingDuration.get();
    }

    @Override
    public int defaultSpellCooldown() {
        return SpellConfig.ShockingCoolDown.get();
    }

    @Override
    public SoundEvent CastingSound() {
        return ModSounds.ZAP.get();
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
        list.add(ModEnchantments.BURNING.get());
        return list;
    }

    @Override
    public boolean conditionsMet(ServerLevel worldIn, LivingEntity caster, SpellStat spellStat) {
        if (caster instanceof Mob mob){
            if (mob.getTarget() != null){
                int range = spellStat.getRange();
                if (WandUtil.enchantedFocus(caster)) {
                    range += WandUtil.getLevels(ModEnchantments.RANGE.get(), caster);
                }
                return mob.hasLineOfSight(mob.getTarget()) && mob.distanceTo(mob.getTarget()) <= range + 4.0D;
            }
        }
        return super.conditionsMet(worldIn, caster, spellStat);
    }

    @Override
    public void SpellResult(ServerLevel worldIn, LivingEntity caster, ItemStack staff, SpellStat spellStat) {
        float damage = SpellConfig.ShockingDamage.get().floatValue() * SpellConfig.SpellDamageMultiplier.get();
        int range = spellStat.getRange();
        int burning = spellStat.getBurning();
        if (WandUtil.enchantedFocus(caster)) {
            range += WandUtil.getLevels(ModEnchantments.RANGE.get(), caster);
            damage += WandUtil.getLevels(ModEnchantments.POTENCY.get(), caster);
            burning += WandUtil.getLevels(ModEnchantments.BURNING.get(), caster);
        }
        damage += spellStat.getPotency();
        Vec3 vec3 = caster.getEyePosition();
        BlockHitResult rayTraceResult = this.blockResult(worldIn, caster, range);
        Entity target = MobUtil.getNearbyTarget(worldIn, caster, range, 1.0F);
        Optional<BlockPos> lightningRod = BlockFinder.findLightningRod(worldIn, BlockPos.containing(rayTraceResult.getLocation()), range);
        if (lightningRod.isPresent() && !this.rightStaff(staff)){
            BlockPos blockPos = lightningRod.get();
            ModNetwork.sendToALL(new SLightningPacket(vec3, new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ()), 5));
            worldIn.playSound(null, caster.getX(), caster.getY(), caster.getZ(), ModSounds.ZAP.get(), this.getSoundSource(), 1.0F, 1.0F);
        } else {
            LivingEntity livingEntity = null;
            if (target instanceof PartEntity<?> partEntity && partEntity.getParent() instanceof LivingEntity parent){
                livingEntity = parent;
            } else if (target instanceof LivingEntity target1 && !target1.isDeadOrDying()){
                livingEntity = target1;
            }
            if (livingEntity != null && ForgeHooks.onLivingAttack(livingEntity, ModDamageSource.directShock(caster), damage)) {
                Vec3 vec31 = new Vec3(target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ());
                ModNetwork.sendToALL(new SLightningPacket(vec3, vec31, 5));
                if (target.hurt(ModDamageSource.directShock(caster), damage)){
                    float chainDamage = damage / 2.0F;
                    if (worldIn.isThundering() && worldIn.isRainingAt(target.blockPosition())){
                        chainDamage = damage;
                    }
                    if (burning > 0){
                        if (worldIn.random.nextFloat() < 0.05F){
                            target.setSecondsOnFire(5 * burning);
                        }
                    }
                    if (this.rightStaff(staff)){
                        WandUtil.chainLightning(livingEntity, caster, range / 4.0D, chainDamage, true);
                    }
                }
                worldIn.playSound(null, caster.getX(), caster.getY(), caster.getZ(), ModSounds.ZAP.get(), this.getSoundSource(), 1.0F, 1.0F);
            } else {
                BlockPos blockPos = rayTraceResult.getBlockPos();
                ModNetwork.sendToALL(new SLightningPacket(vec3, new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ()), 5));
                worldIn.playSound(null, caster.getX(), caster.getY(), caster.getZ(), ModSounds.ZAP.get(), this.getSoundSource(), 1.0F, 1.0F);
            }
        }
    }
}
