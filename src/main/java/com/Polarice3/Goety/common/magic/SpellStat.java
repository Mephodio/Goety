package com.Polarice3.Goety.common.magic;

public class SpellStat {
    public int potency;
    public int duration;
    public int range;
    public double radius;
    public int burning;
    public float velocity;

    public SpellStat(int potency, int duration, int range, double radius, int burning, float velocity) {
        this.potency = potency;
        this.duration = duration;
        this.range = range;
        this.radius = radius;
        this.burning = burning;
        this.velocity = velocity;
    }

    public SpellStat setPotency(int potency) {
        SpellStat spellStat = this;
        spellStat.potency = potency;
        return spellStat;
    }

    public SpellStat setDuration(int duration) {
        SpellStat spellStat = this;
        spellStat.duration = duration;
        return spellStat;
    }

    public SpellStat setRange(int range) {
        SpellStat spellStat = this;
        spellStat.range = range;
        return spellStat;
    }

    public SpellStat setRadius(double radius) {
        SpellStat spellStat = this;
        spellStat.radius = radius;
        return spellStat;
    }

    public SpellStat setBurning(int burning) {
        SpellStat spellStat = this;
        spellStat.burning = burning;
        return spellStat;
    }

    public SpellStat setVelocity(float velocity) {
        SpellStat spellStat = this;
        spellStat.velocity = velocity;
        return spellStat;
    }

    public int getPotency() {
        return this.potency;
    }

    public int getDuration() {
        return this.duration;
    }

    public int getRange() {
        return this.range;
    }

    public double getRadius() {
        return this.radius;
    }

    public int getBurning() {
        return this.burning;
    }

    public float getVelocity() {
        return this.velocity;
    }
}
