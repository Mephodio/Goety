package com.Polarice3.Goety.common.entities.projectiles;

import com.Polarice3.Goety.common.effects.GoetyEffects;
import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.entities.boss.Apostle;
import com.Polarice3.Goety.utils.ModDamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class HellCloud extends AbstractSpellCloud{
    public HellCloud(EntityType<?> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
        this.setRainParticle(ParticleTypes.FALLING_LAVA);
    }

    public HellCloud(Level pLevel, LivingEntity pOwner, LivingEntity pTarget){
        this(ModEntityType.HELL_CLOUD.get(), pLevel);
        if (pOwner != null){
            this.setOwner(pOwner);
        }
        this.setActivateTime(100);
        if (pTarget != null){
            BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos(pTarget.getX(), pTarget.getY(), pTarget.getZ());

            while(blockpos$mutable.getY() < pTarget.getY() + 4.0D && !this.level.getBlockState(blockpos$mutable).blocksMotion()) {
                blockpos$mutable.move(Direction.UP);
            }
            this.setPos(pTarget.getX(), blockpos$mutable.getY(), pTarget.getZ());
            this.setTarget(pTarget);
        }
        this.playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 1.0F, 1.25F);
    }

    public int getColor(){
        return 0x1f0000;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level.isClientSide) {
            float speed = 0.175F;
            if (this.getOwner() instanceof Apostle apostle && apostle.isAlive() && apostle.isInNether()) {
                this.setDeltaMovement(Vec3.ZERO);
                double d0 = apostle.getX() - this.getX();
                double d1 = (apostle.getY() + 4.0D) - this.getY();
                double d2 = apostle.getZ() - this.getZ();
                double d = Math.sqrt((d0 * d0 + d2 * d2));
                double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                if (d > 0.5) {
                    this.setDeltaMovement(this.getDeltaMovement().add(d0 / d3, d1 / d3, d2 / d3).scale(speed));
                }
            }
            this.move(MoverType.SELF, this.getDeltaMovement());
        }
    }

    public void hurtEntities(LivingEntity livingEntity){
        if (livingEntity != null) {
            float baseDamage = 1.0F;
            baseDamage += this.getExtraDamage();
            if (livingEntity.hurt(ModDamageSource.hellfire(this, this.getOwner()), baseDamage)) {
                if (this.getOwner() instanceof Apostle) {
                    livingEntity.addEffect(new MobEffectInstance(GoetyEffects.BURN_HEX.get(), 1200));
                }
            }
        }
    }
}