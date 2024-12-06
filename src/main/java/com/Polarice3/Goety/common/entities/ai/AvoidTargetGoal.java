package com.Polarice3.Goety.common.entities.ai;

import com.Polarice3.Goety.utils.MobUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;

import java.util.function.Predicate;

public class AvoidTargetGoal<T extends LivingEntity> extends AvoidEntityGoal<T> {

    public AvoidTargetGoal(PathfinderMob p_25040_, Class<T> p_25041_, Predicate<LivingEntity> p_25042_, float p_25043_, double p_25044_, double p_25045_) {
        super(p_25040_, p_25041_, p_25042_, p_25043_, p_25044_, p_25045_, livingEntity -> p_25040_.getTarget() == livingEntity);
    }

    public AvoidTargetGoal(PathfinderMob p_25033_, Class<T> p_25041_, float p_25035_, double p_25036_, double p_25037_) {
        super(p_25033_, p_25041_, p_25035_, p_25036_, p_25037_, livingEntity -> p_25033_.getTarget() == livingEntity);
    }

    public static AvoidTargetGoal<LivingEntity> newGoal(PathfinderMob pathfinderMob, float radius, double minSpeed, double maxSpeed){
        return new AvoidTargetGoal<>(pathfinderMob, LivingEntity.class, radius, minSpeed, maxSpeed);
    }

    public void stop() {
        super.stop();
        this.pathNav.stop();
    }

    public static class AvoidRadiusGoal<T extends LivingEntity> extends ModAvoidGoal<T>{
        public AvoidRadiusGoal(PathfinderMob mob, Class<T> tClass, Predicate<LivingEntity> predicate, float avoidStart, int maxDist, int minDist, double walkSpeed, double runSpeed, Predicate<LivingEntity> target) {
            super(mob, tClass, predicate, avoidStart, maxDist, minDist, walkSpeed, runSpeed, target);
        }

        public AvoidRadiusGoal(PathfinderMob mob, Class<T> tClass, Predicate<LivingEntity> predicate, float avoidStart, int maxDist, int minDist, double walkSpeed, double runSpeed) {
            this(mob, tClass, predicate, avoidStart, maxDist, minDist, walkSpeed, runSpeed, livingEntity -> mob.getTarget() == livingEntity);
        }

        public AvoidRadiusGoal(PathfinderMob mob, Class<T> tClass, float avoidStart, int maxDist, int minDist, double walkSpeed, double runSpeed) {
            super(mob, tClass, avoidStart, maxDist, minDist, walkSpeed, runSpeed, livingEntity -> mob.getTarget() == livingEntity);
        }

        public static AvoidRadiusGoal<LivingEntity> newGoal(PathfinderMob pathfinderMob, float avoidStart, int maxDist, double minSpeed, double maxSpeed){
            return new AvoidRadiusGoal<>(pathfinderMob, LivingEntity.class, avoidStart, maxDist, maxDist, minSpeed, maxSpeed);
        }

        public static AvoidRadiusGoal<LivingEntity> newGoalTwo(PathfinderMob pathfinderMob, float avoidStart, int maxDist, double minSpeed, double maxSpeed){
            return new AvoidRadiusGoal<>(pathfinderMob, LivingEntity.class, livingEntity -> true, avoidStart, maxDist, maxDist, minSpeed, maxSpeed, livingEntity -> pathfinderMob.getTarget() == livingEntity || MobUtil.isOwnedTargetable(pathfinderMob, livingEntity));
        }

        public void stop() {
            super.stop();
            this.pathNav.stop();
        }
    }
}