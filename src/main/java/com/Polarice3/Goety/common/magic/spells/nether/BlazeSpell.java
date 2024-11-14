package com.Polarice3.Goety.common.magic.spells.nether;

import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.effects.GoetyEffects;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.entities.hostile.servants.Inferno;
import com.Polarice3.Goety.common.entities.neutral.BlazeServant;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.common.magic.SummonSpell;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class BlazeSpell extends SummonSpell {

    public int defaultSoulCost() {
        return SpellConfig.BlazeCost.get();
    }

    public int defaultCastDuration() {
        return SpellConfig.BlazeDuration.get();
    }

    public int SummonDownDuration() {
        return SpellConfig.BlazeSummonDown.get();
    }

    public SoundEvent CastingSound() {
        return ModSounds.PREPARE_SUMMON.get();
    }

    @Override
    public int defaultSpellCooldown() {
        return SpellConfig.BlazeCoolDown.get();
    }

    @Override
    public SpellType getSpellType() {
        return SpellType.NETHER;
    }

    @Override
    public List<Enchantment> acceptedEnchantments() {
        List<Enchantment> list = new ArrayList<>();
        list.add(ModEnchantments.POTENCY.get());
        list.add(ModEnchantments.DURATION.get());
        return list;
    }

    @Override
    public Predicate<LivingEntity> summonPredicate() {
        return livingEntity -> livingEntity instanceof BlazeServant;
    }

    @Override
    public int summonLimit() {
        return SpellConfig.BlazeLimit.get();
    }

    public void commonResult(ServerLevel worldIn, LivingEntity caster){
        if (isShifting(caster)) {
            for (Entity entity : worldIn.getAllEntities()) {
                if (entity instanceof BlazeServant) {
                    this.teleportServants(caster, entity);
                }
            }
            for (int i = 0; i < caster.level.random.nextInt(35) + 10; ++i) {
                worldIn.sendParticles(ParticleTypes.POOF, caster.getX(), caster.getEyeY(), caster.getZ(), 1, 0.0F, 0.0F, 0.0F, 0);
            }
            worldIn.playSound((Player) null, caster.getX(), caster.getY(), caster.getZ(), ModSounds.SUMMON_SPELL.get(), this.getSoundSource(), 1.0F, 1.0F);
        }
    }

    public void SpellResult(ServerLevel worldIn, LivingEntity caster, ItemStack staff, SpellStat spellStat) {
        this.commonResult(worldIn, caster);
        int potency = spellStat.getPotency();
        int duration = spellStat.getDuration();
        if (WandUtil.enchantedFocus(caster)){
            potency += WandUtil.getLevels(ModEnchantments.POTENCY.get(), caster);
            duration += WandUtil.getLevels(ModEnchantments.DURATION.get(), caster) + 1;
        }
        if (!isShifting(caster)) {
            int i = 1;
            if (rightStaff(staff)){
                i = 2 + caster.level.random.nextInt(4);
            }
            for (int i1 = 0; i1 < i; ++i1) {
                BlazeServant blazeServant = new BlazeServant(ModEntityType.BLAZE_SERVANT.get(), worldIn);
                if (CuriosFinder.hasUnholySet(caster)){
                    blazeServant = new Inferno(ModEntityType.INFERNO.get(), worldIn);
                }
                BlockPos blockPos = BlockFinder.SummonRadius(caster.blockPosition(), blazeServant, worldIn);
                if (caster.isUnderWater()){
                    blockPos = BlockFinder.SummonWaterRadius(caster, worldIn);
                }
                blazeServant.setTrueOwner(caster);
                blazeServant.moveTo(blockPos, 0.0F, 0.0F);
                MobUtil.moveDownToGround(blazeServant);
                blazeServant.setLimitedLife(MobUtil.getSummonLifespan(worldIn) * duration);
                blazeServant.setPersistenceRequired();
                blazeServant.finalizeSpawn(worldIn, caster.level.getCurrentDifficultyAt(caster.blockPosition()), MobSpawnType.MOB_SUMMONED,null,null);
                if (potency > 0){
                    int boost = Mth.clamp(potency - 1, 0, 10);
                    blazeServant.addEffect(new MobEffectInstance(GoetyEffects.BUFF.get(), EffectsUtil.infiniteEffect(), boost, false, false));
                    blazeServant.setFireBallDamage(blazeServant.getFireBallDamage() + potency);
                }
                this.SummonSap(caster, blazeServant);
                this.setTarget(caster, blazeServant);
                worldIn.addFreshEntity(blazeServant);
                this.summonAdvancement(caster, blazeServant);
            }
            this.SummonDown(caster);
            worldIn.playSound((Player) null, caster.getX(), caster.getY(), caster.getZ(), ModSounds.SUMMON_SPELL_FIERY.get(), this.getSoundSource(), 1.0F, 1.0F);
        }
    }
}
