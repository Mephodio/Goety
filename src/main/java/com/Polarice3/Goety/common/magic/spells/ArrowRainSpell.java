package com.Polarice3.Goety.common.magic.spells;

import com.Polarice3.Goety.client.particles.FoggyCloudParticleOption;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.entities.projectiles.RainArrow;
import com.Polarice3.Goety.common.magic.EverChargeSpell;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.utils.ColorUtil;
import com.Polarice3.Goety.utils.WandUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Based on @AlexModGuy's Dreadbow codes: <a href="https://github.com/AlexModGuy/AlexsCaves/blob/main/src/main/java/com/github/alexmodguy/alexscaves/server/item/DreadbowItem.java">...</a>
 */
public class ArrowRainSpell extends EverChargeSpell {

    @Override
    public SpellStat defaultStats() {
        return super.defaultStats().setVelocity(1.0F);
    }

    @Override
    public int defaultSoulCost() {
        return SpellConfig.ArrowRainCost.get();
    }

    @Override
    public int defaultCastUp() {
        return SpellConfig.ArrowRainChargeUp.get();
    }

    @Override
    public int shotsNumber() {
        return SpellConfig.ArrowRainDuration.get();
    }

    @Override
    public int defaultSpellCooldown() {
        return SpellConfig.ArrowRainCoolDown.get();
    }

    @Nullable
    @Override
    public SoundEvent CastingSound() {
        return SoundEvents.CROSSBOW_LOADING_MIDDLE;
    }

    @Override
    public List<Enchantment> acceptedEnchantments() {
        List<Enchantment> list = new ArrayList<>();
        list.add(ModEnchantments.POTENCY.get());
        list.add(ModEnchantments.RANGE.get());
        list.add(ModEnchantments.BURNING.get());
        list.add(ModEnchantments.VELOCITY.get());
        return list;
    }

    @Override
    public void SpellResult(ServerLevel worldIn, LivingEntity caster, ItemStack staff, SpellStat spellStat) {
        int potency = spellStat.getPotency();
        int burning = spellStat.getBurning();
        int range = spellStat.getRange();
        float velocity = spellStat.getVelocity();
        if (WandUtil.enchantedFocus(caster)) {
            potency += WandUtil.getLevels(ModEnchantments.POTENCY.get(), caster);
            burning += WandUtil.getLevels(ModEnchantments.BURNING.get(), caster);
            range += WandUtil.getLevels(ModEnchantments.RANGE.get(), caster);
            velocity += WandUtil.getLevels(ModEnchantments.VELOCITY.get(), caster) / 2.0F;
        }
        HitResult rayTrace = this.rayTrace(worldIn, caster, range, 3.0D);
        Vec3 location = rayTrace.getLocation();
        LivingEntity target = this.getTarget(caster, range);
        if (target != null){
            location = target.position();
        }
        BlockPos mutableBlockPos = new BlockPos.MutableBlockPos(location.x, location.y + 1.5D, location.z);
        int maxHeight = 15;
        int i = 0;
        while (mutableBlockPos.getY() < worldIn.getMaxBuildHeight() && worldIn.isEmptyBlock(mutableBlockPos) && i < maxHeight){
            mutableBlockPos = mutableBlockPos.above();
            i++;
        }
        for (int j = 0; j < potency + 1; ++j){
            RainArrow arrow = new RainArrow(worldIn, caster);
            Vec3 vec3 = mutableBlockPos.getCenter()
                    .add(worldIn.random.nextFloat() * 16.0F - 8.0F,
                            worldIn.random.nextFloat() * 4.0F - 2.0F,
                            worldIn.random.nextFloat() * 16.0F - 8.0F);
            int clearTries = 0;
            while (clearTries < 6 && !worldIn.isEmptyBlock(BlockPos.containing(vec3)) && worldIn.getFluidState(BlockPos.containing(vec3)).isEmpty()){
                clearTries++;
                vec3 = mutableBlockPos.getCenter()
                        .add(worldIn.random.nextFloat() * 16.0F - 8.0F,
                                worldIn.random.nextFloat() * 4.0F - 2.0F,
                                worldIn.random.nextFloat() * 16.0F - 8.0F);
            }
            if (!worldIn.isEmptyBlock(BlockPos.containing(vec3)) && worldIn.getFluidState(BlockPos.containing(vec3)).isEmpty()){
                vec3 = mutableBlockPos.getCenter();
            }
            worldIn.sendParticles(new FoggyCloudParticleOption(new ColorUtil(0x4b4b4b), 1.5F, 6), vec3.x(), vec3.y(), vec3.z(), 1, 0, 0, 0, 0);
            arrow.setPos(vec3);
            Vec3 vec31 = location.subtract(vec3);
            float randomness = 20.0F + worldIn.random.nextFloat() * 10.0F;
            if (worldIn.random.nextFloat() < 0.25F){
                randomness = worldIn.random.nextFloat();
            }
            if (burning > 0){
                arrow.setSecondsOnFire(100);
            }
            arrow.shoot(vec31.x, vec31.y, vec31.z, velocity + (1.5F * worldIn.random.nextFloat()), randomness);
            if (worldIn.addFreshEntity(arrow)){
                worldIn.playSound(null, arrow.getX(), arrow.getY(), arrow.getZ(), SoundEvents.ARROW_SHOOT, this.getSoundSource(), 2.0F, 1.0F / (worldIn.getRandom().nextFloat() * 0.4F + 1.2F) + 0.5F);
            }
        }
    }
}