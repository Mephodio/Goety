package com.Polarice3.Goety.common.magic.spells.necromancy;

import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.effects.GoetyEffects;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.entities.ally.Summoned;
import com.Polarice3.Goety.common.entities.ally.undead.zombie.*;
import com.Polarice3.Goety.common.entities.neutral.ZPiglinBruteServant;
import com.Polarice3.Goety.common.entities.neutral.ZPiglinServant;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.common.magic.SummonSpell;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.init.ModTags;
import com.Polarice3.Goety.utils.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.Tags;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ZombieSpell extends SummonSpell {

    public int defaultSoulCost() {
        return SpellConfig.ZombieCost.get();
    }

    public int defaultCastDuration() {
        return SpellConfig.ZombieDuration.get();
    }

    public int SummonDownDuration() {
        return SpellConfig.ZombieSummonDown.get();
    }

    public SoundEvent CastingSound() {
        return ModSounds.PREPARE_SUMMON.get();
    }

    @Override
    public int defaultSpellCooldown() {
        return SpellConfig.ZombieCoolDown.get();
    }

    @Override
    public SpellType getSpellType() {
        return SpellType.NECROMANCY;
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
        return livingEntity -> livingEntity instanceof ZombieServant;
    }

    @Override
    public int summonLimit() {
        return SpellConfig.ZombieLimit.get();
    }

    public void commonResult(ServerLevel worldIn, LivingEntity caster){
        if (isShifting(caster)) {
            for (Entity entity : worldIn.getAllEntities()) {
                if (entity instanceof ZombieServant) {
                    this.teleportServants(caster, entity);
                }
            }
            for (int i = 0; i < caster.level.random.nextInt(35) + 10; ++i) {
                worldIn.sendParticles(ParticleTypes.POOF, caster.getX(), caster.getEyeY(), caster.getZ(), 1, 0.0F, 0.0F, 0.0F, 0);
            }
            worldIn.playSound((Player) null, caster.getX(), caster.getY(), caster.getZ(), ModSounds.SUMMON_SPELL.get(), this.getSoundSource(), 1.0F, 1.0F);
        }
    }

    public boolean specialStaffs(ItemStack stack){
        return typeStaff(stack, SpellType.FROST)
                || typeStaff(stack, SpellType.WILD)
                || typeStaff(stack, SpellType.NETHER)
                || stack.is(ModItems.OMINOUS_STAFF.get());
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
            if (staff.is(ModItems.NAMELESS_STAFF.get())){
                i = 7;
            } else if (rightStaff(staff)){
                i = 2 + caster.level.random.nextInt(4);
            } else if (specialStaffs(staff)){
                i = 2;
            }
            for (int i1 = 0; i1 < i; ++i1) {
                Summoned summonedentity = new ZombieServant(ModEntityType.ZOMBIE_SERVANT.get(), worldIn);
                BlockPos blockPos = BlockFinder.SummonRadius(caster.blockPosition(), summonedentity, worldIn);
                if (caster.isUnderWater()){
                    blockPos = BlockFinder.SummonWaterRadius(caster, worldIn);
                }
                if (specialStaffs(staff)) {
                    if (typeStaff(staff, SpellType.FROST)) {
                        summonedentity = new FrozenZombieServant(ModEntityType.FROZEN_ZOMBIE_SERVANT.get(), worldIn);
                    } else if (typeStaff(staff, SpellType.WILD)) {
                        summonedentity = new JungleZombieServant(ModEntityType.JUNGLE_ZOMBIE_SERVANT.get(), worldIn);
                    } else if (typeStaff(staff, SpellType.NETHER)) {
                        summonedentity = new ZPiglinServant(ModEntityType.ZPIGLIN_SERVANT.get(), worldIn);
                    } else if (staff.is(ModItems.OMINOUS_STAFF.get())) {
                        summonedentity = new ZombieVindicatorServant(ModEntityType.ZOMBIE_VINDICATOR_SERVANT.get(), worldIn);
                    }
                } else if (caster.isUnderWater() && worldIn.isWaterAt(blockPos)){
                    summonedentity = new DrownedServant(ModEntityType.DROWNED_SERVANT.get(), worldIn);
                } else if (worldIn.getBiome(blockPos).is(Tags.Biomes.IS_DESERT) && worldIn.canSeeSky(blockPos)){
                    summonedentity = new HuskServant(ModEntityType.HUSK_SERVANT.get(), worldIn);
                } else if (worldIn.dimension() == Level.NETHER){
                    Summoned summoned = new ZPiglinServant(ModEntityType.ZPIGLIN_SERVANT.get(), worldIn);
                    if (worldIn.random.nextFloat() <= 0.25F && BlockFinder.findStructure(worldIn, caster, ModTags.Structures.CAN_SUMMON_BRUTES)){
                        summoned = new ZPiglinBruteServant(ModEntityType.ZPIGLIN_BRUTE_SERVANT.get(), worldIn);
                    }
                    summonedentity = summoned;
                } else if (BlockFinder.findStructure(worldIn, blockPos, StructureTags.ON_WOODLAND_EXPLORER_MAPS)){
                    summonedentity = new ZombieVindicatorServant(ModEntityType.ZOMBIE_VINDICATOR_SERVANT.get(), worldIn);
                } else if (worldIn.getBiome(blockPos).get().coldEnoughToSnow(blockPos)){
                    summonedentity = new FrozenZombieServant(ModEntityType.FROZEN_ZOMBIE_SERVANT.get(), worldIn);
                } else if (worldIn.getBiome(blockPos).is(BiomeTags.IS_JUNGLE) && worldIn.random.nextBoolean()){
                    summonedentity = new JungleZombieServant(ModEntityType.JUNGLE_ZOMBIE_SERVANT.get(), worldIn);
                }
                summonedentity.setTrueOwner(caster);
                summonedentity.moveTo(blockPos, 0.0F, 0.0F);
                if (summonedentity.getType() != ModEntityType.DROWNED_SERVANT.get()){
                    MobUtil.moveDownToGround(summonedentity);
                }
                summonedentity.setLimitedLife(MobUtil.getSummonLifespan(worldIn) * duration);
                summonedentity.setPersistenceRequired();
                summonedentity.finalizeSpawn(worldIn, caster.level.getCurrentDifficultyAt(caster.blockPosition()), MobSpawnType.MOB_SUMMONED,null,null);
                if (potency > 0){
                    int boost = Mth.clamp(potency - 1, 0, 10);
                    summonedentity.addEffect(new MobEffectInstance(GoetyEffects.BUFF.get(), EffectsUtil.infiniteEffect(), boost, false, false));
                }
                this.SummonSap(caster, summonedentity);
                this.setTarget(caster, summonedentity);
                if (worldIn.addFreshEntity(summonedentity)) {
                    ColorUtil colorUtil = new ColorUtil(0x2ac9cf);
                    ServerParticleUtil.windShockwaveParticle(worldIn, colorUtil, 0.1F, 0.1F, 0.05F, -1, summonedentity.position());
                }
                this.summonAdvancement(caster, summonedentity);
            }
            this.SummonDown(caster);
            SoundUtil.playNecromancerSummon(caster);
        }
    }
}
