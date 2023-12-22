package com.Polarice3.Goety.common.magic;

import com.Polarice3.Goety.api.magic.IChargingSpell;

public abstract class ChargingSpells extends Spells implements IChargingSpell {

    public abstract int Cooldown();

    public int defaultCastDuration() {
        return 72000;
    }

    @Override
    public int defaultSpellCooldown() {
        return 0;
    }
}
