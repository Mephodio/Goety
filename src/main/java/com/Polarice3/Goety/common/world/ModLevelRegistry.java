package com.Polarice3.Goety.common.world;

import com.Polarice3.Goety.Goety;
import com.Polarice3.Goety.MobsConfig;
import com.Polarice3.Goety.common.entities.ModEntityType;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Goety.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModLevelRegistry {

    public static void addBiomeSpawns(Holder<Biome> biome, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (biome.containsTag(BiomeTags.IS_OVERWORLD) && !biome.containsTag(Tags.Biomes.IS_MUSHROOM) && !biome.is(Biomes.DEEP_DARK)
                && !biome.is(biomeResourceKey -> biomeResourceKey.registry().getNamespace().contains("alexscaves"))){
            builder.getMobSpawnSettings().getSpawner(MobCategory.MONSTER).add(new MobSpawnSettings.SpawnerData(ModEntityType.WARLOCK.get(), MobsConfig.WarlockSpawnWeight.get(), 1, 1));
            builder.getMobSpawnSettings().getSpawner(MobCategory.MONSTER).add(new MobSpawnSettings.SpawnerData(ModEntityType.WRAITH.get(), MobsConfig.WraithSpawnWeight.get(), 1, 1));
        }
        if (biome.is(Biomes.SOUL_SAND_VALLEY)){
            builder.getMobSpawnSettings().getSpawner(MobCategory.MONSTER).add(new MobSpawnSettings.SpawnerData(ModEntityType.WRAITH.get(), MobsConfig.WraithSpawnWeight.get(), 1, 1));
            builder.getMobSpawnSettings().addMobCharge(ModEntityType.WRAITH.get(), 0.7D, 0.15D);
        }
    }
}
