package com.Polarice3.Goety.common;

import com.Polarice3.Goety.init.ModProxy;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class CommonProxy implements ModProxy {
    @Override
    public Player getPlayer() {
        return null;
    }

    @Override
    public void addBossBar(UUID id, Mob mob) {
    }

    @Override
    public void removeBossBar(UUID id, Mob mob) {
    }

    @Override
    public void soulExplode(BlockPos blockPos, int radius) {
    }

}
