package com.Polarice3.Goety;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;

import java.io.File;

public class MobsConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Integer> RavagerRoarCooldown;

    public static final ForgeConfigSpec.ConfigValue<Integer> UndeadMinionHealCost;
    public static final ForgeConfigSpec.ConfigValue<Integer> UndeadMinionHealTime;
    public static final ForgeConfigSpec.ConfigValue<Double> UndeadMinionHealAmount;
    public static final ForgeConfigSpec.ConfigValue<Double> ZombieServantBabyChance;

    public static final ForgeConfigSpec.ConfigValue<Integer> IllagerAssaultSpawnFreq;
    public static final ForgeConfigSpec.ConfigValue<Integer> IllagerAssaultSpawnChance;
    public static final ForgeConfigSpec.ConfigValue<Integer> IllagerAssaultSEThreshold;
    public static final ForgeConfigSpec.ConfigValue<Integer> IllagerAssaultSELimit;

    public static final ForgeConfigSpec.ConfigValue<Integer> VillagerHateSpells;

    public static final ForgeConfigSpec.ConfigValue<Integer> WarlockSpawnWeight;
    public static final ForgeConfigSpec.ConfigValue<Integer> WraithSpawnWeight;

    public static final ForgeConfigSpec.ConfigValue<Boolean> ZombieServantTexture;
    public static final ForgeConfigSpec.ConfigValue<Boolean> DrownedServantTexture;
    public static final ForgeConfigSpec.ConfigValue<Boolean> HuskServantTexture;
    public static final ForgeConfigSpec.ConfigValue<Boolean> FrozenZombieServantTexture;
    public static final ForgeConfigSpec.ConfigValue<Boolean> JungleZombieServantTexture;

    public static final ForgeConfigSpec.ConfigValue<Boolean> SkeletonServantTexture;
    public static final ForgeConfigSpec.ConfigValue<Boolean> StrayServantTexture;
    public static final ForgeConfigSpec.ConfigValue<Boolean> MossySkeletonServantTexture;
    public static final ForgeConfigSpec.ConfigValue<Boolean> SunkenSkeletonServantTexture;
    public static final ForgeConfigSpec.ConfigValue<Boolean> VanguardServantTexture;

    public static final ForgeConfigSpec.ConfigValue<Boolean> WraithServantTexture;

    public static final ForgeConfigSpec.ConfigValue<Boolean> ZPiglinServantTexture;

    public static final ForgeConfigSpec.ConfigValue<Boolean> VexTexture;

    public static final ForgeConfigSpec.ConfigValue<Boolean> RedstoneGolemCrack;
    public static final ForgeConfigSpec.ConfigValue<Boolean> HolidaySkins;

    public static final ForgeConfigSpec.ConfigValue<Boolean> UndeadTeleport;
    public static final ForgeConfigSpec.ConfigValue<Boolean> VexTeleport;
    public static final ForgeConfigSpec.ConfigValue<Boolean> MinionsAttackCreepers;
    public static final ForgeConfigSpec.ConfigValue<Boolean> NecroRobeUndead;
    public static final ForgeConfigSpec.ConfigValue<Boolean> MinionsMasterImmune;
    public static final ForgeConfigSpec.ConfigValue<Boolean> OwnerAttackCancel;
    public static final ForgeConfigSpec.ConfigValue<Boolean> MobSense;
    public static final ForgeConfigSpec.ConfigValue<Boolean> UndeadMinionHeal;

    public static final ForgeConfigSpec.ConfigValue<Boolean> VillagerHate;
    public static final ForgeConfigSpec.ConfigValue<Boolean> VillagerHateRavager;
    public static final ForgeConfigSpec.ConfigValue<Boolean> VillagerConvertWarlock;

    public static final ForgeConfigSpec.ConfigValue<Boolean> IllagerAssault;
    public static final ForgeConfigSpec.ConfigValue<Boolean> SoulEnergyBadOmen;
    public static final ForgeConfigSpec.ConfigValue<Boolean> IllagueSpread;
    public static final ForgeConfigSpec.ConfigValue<Boolean> IllagerSteal;
    public static final ForgeConfigSpec.ConfigValue<Boolean> IllagerRaid;
    public static final ForgeConfigSpec.ConfigValue<Boolean> PikerRaid;
    public static final ForgeConfigSpec.ConfigValue<Boolean> PreacherRaid;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ConquillagerRaid;
    public static final ForgeConfigSpec.ConfigValue<Boolean> InquillagerRaid;
    public static final ForgeConfigSpec.ConfigValue<Boolean> EnviokerRaid;
    public static final ForgeConfigSpec.ConfigValue<Boolean> MinisterRaid;
    public static final ForgeConfigSpec.ConfigValue<Boolean> HostileRedstoneGolemRaid;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ArmoredRavagerRaid;
    public static final ForgeConfigSpec.ConfigValue<Boolean> WarlockRaid;

    public static final ForgeConfigSpec.ConfigValue<Boolean> InterDimensionalMobs;
    public static final ForgeConfigSpec.ConfigValue<Boolean> TallSkullDrops;
    public static final ForgeConfigSpec.ConfigValue<Boolean> WraithAggressiveTeleport;

    public static final ForgeConfigSpec.ConfigValue<Boolean> VizierMinion;

    public static final ForgeConfigSpec.ConfigValue<Boolean> ApocalypseMode;
    public static final ForgeConfigSpec.ConfigValue<Boolean> FancierApostleDeath;
    public static final ForgeConfigSpec.ConfigValue<Boolean> RedstoneGolemMold;

    static {
        BUILDER.push("Textures");
        HolidaySkins = BUILDER.comment("If certain mobs have a different texture during some holiday months, Default: true")
                .define("holidaySkins", true);
            BUILDER.push("Summoned Mobs");
                BUILDER.push("Zombie Servants");
                ZombieServantTexture = BUILDER.comment("If Zombie Servants have custom textures, Default: true")
                        .define("zombieServantTexture", true);
                DrownedServantTexture = BUILDER.comment("If Drowned Servants have custom textures, Default: true")
                        .define("drownedServantTexture", true);
                HuskServantTexture = BUILDER.comment("If Husk Servants have custom textures, Default: true")
                        .define("huskServantTexture", true);
                FrozenZombieServantTexture = BUILDER.comment("If Frozen Zombie Servants have custom textures, Default: true")
                        .define("frozenZombieServantTexture", true);
                JungleZombieServantTexture = BUILDER.comment("If Jungle Zombie Servants have custom textures, Default: true")
                        .define("jungleZombieServantTexture", true);
                BUILDER.pop();
                BUILDER.push("Skeleton Servants");
                SkeletonServantTexture = BUILDER.comment("If Skeleton Servants have custom textures, Default: true")
                        .define("skeletonServantTexture", true);
                StrayServantTexture = BUILDER.comment("If Stray Servants have custom textures, Default: true")
                        .define("strayServantTexture", true);
                MossySkeletonServantTexture = BUILDER.comment("If Mossy Skeleton Servants have custom textures, Default: true")
                        .define("mossySkeletonServantTexture", true);
                SunkenSkeletonServantTexture = BUILDER.comment("If Sunken Skeleton Servants have custom textures, Default: true")
                        .define("sunkenSkeletonServantTexture", true);
                VanguardServantTexture = BUILDER.comment("If Vanguard Servants have custom textures, Default: true")
                        .define("vanguardServantTexture", true);
                BUILDER.pop();
                BUILDER.push("Wraith Servants");
                WraithServantTexture = BUILDER.comment("If Wraith Servants have custom textures, Default: true")
                        .define("wraithServantTexture", true);
                BUILDER.pop();
                BUILDER.push("Zombified Piglin Servants");
                ZPiglinServantTexture = BUILDER.comment("If Zombified Piglin Servants have custom textures, Default: true")
                        .define("zPiglinServantTexture", true);
                BUILDER.pop();
                BUILDER.push("Vexes");
                VexTexture = BUILDER.comment("If Vexes have custom textures, Default: true")
                        .define("vexTexture", true);
                BUILDER.pop();
                BUILDER.push("Redstone Golem");
                RedstoneGolemCrack = BUILDER.comment("If Redstone Golems show cracks when damaged sufficiently, Default: false")
                        .define("redstoneGolemCrack", false);
                BUILDER.pop();
            BUILDER.pop();
        BUILDER.pop();
        BUILDER.push("Servants");
            BUILDER.push("Undead Servants");
            UndeadTeleport = BUILDER.comment("Whether Undead Servants can teleport to Players, Default: false")
                    .define("undeadTeleport", false);
            NecroRobeUndead = BUILDER.comment("Whether Servants can attack Undead mobs if owner wears a full Necro Set, Default: false")
                    .define("necroRobeUndead", false);
            UndeadMinionHeal = BUILDER.comment("Whether Undead Servants can heal if summoned while wearing Necro Cape, Default: true")
                    .define("undeadServantsHeal", true);
            UndeadMinionHealCost = BUILDER.comment("How much Soul Energy it cost per second for an Undead Servant to heal, Default: 5")
                    .defineInRange("undeadServantsHealCost", 5, 0, Integer.MAX_VALUE);
            UndeadMinionHealTime = BUILDER.comment("How frequent Undead Servants heal, count seconds, Default: 1")
                    .defineInRange("undeadServantsHealTime", 1, 0, Integer.MAX_VALUE);
            UndeadMinionHealAmount = BUILDER.comment("How much Health Undead Servants heal, numerically, Default: 0.5")
                    .defineInRange("undeadServantsHealAmount", 0.5, 0.0, Double.MAX_VALUE);
            ZombieServantBabyChance = BUILDER.comment("Chance that a zombie (or subclass) servant is summoned as a baby, Default: 0.05")
                    .defineInRange("zombieServantBabyChance", 0.05, 0.0, 1.0D);
            BUILDER.pop();
        RedstoneGolemMold = BUILDER.comment("Whether creating a Redstone Golem causes the mold to change blocks, Default: true")
                .define("redstoneGolemMold", true);
        VexTeleport = BUILDER.comment("Whether Vex Servants can teleport to Players, Default: true")
                .define("vexTeleport", true);
        MinionsAttackCreepers = BUILDER.comment("Whether Servants can attack Creepers if Mob Griefing Rule is False, Default: true")
                .define("servantsAttackCreepers", true);
        MinionsMasterImmune = BUILDER.comment("Whether Servants or their owner are immune to attacks made by other servants that are summoned by the same owner, Default: true")
                .define("servantsMasterImmune", true);
        OwnerAttackCancel = BUILDER.comment("Owners can't attack or hurt their servants, Default: true")
                .define("ownerAttackCancel", true);
        MobSense = BUILDER.comment("Mobs will automatically be hostile to servants, if servant is hostile towards the mob, Default: true")
                .define("mobSense", true);
        RavagerRoarCooldown = BUILDER.comment("How many seconds it takes before Ravager can manually roar again, Default: 10")
                .defineInRange("ravagerRoarCooldown", 10, 0, Integer.MAX_VALUE);
        BUILDER.pop();
        BUILDER.push("Illagers");
            BUILDER.push("Illager Assaults");
            IllagerAssault = BUILDER.comment("Modded Illagers Spawning based of Player's Soul Energy amount, Default: true")
                    .define("illagerAssault", true);
            IllagerAssaultSpawnFreq = BUILDER.comment("Spawn Frequency for Illagers Hunting the Player, Default: 12000")
                    .defineInRange("illagerAssaultSpawnFreq", 12000, 0, Integer.MAX_VALUE);
            IllagerAssaultSpawnChance = BUILDER.comment("Spawn Chance for Illagers Hunting the Player every Spawn Frequency, the lower the more likelier, Default: 5")
                    .defineInRange("illagerAssaultSpawnChance", 5, 0, Integer.MAX_VALUE);
            IllagerAssaultSEThreshold = BUILDER.comment("How much Soul Energy the Player has is required for Special Illagers to spawn, Default: 2500")
                    .defineInRange("illagerAssaultThreshold", 2500, 0, Integer.MAX_VALUE);
            IllagerAssaultSELimit = BUILDER.comment("The maximum amount of Soul Energy the Player has that is taken consideration for the Assaults, Default: 30000")
                    .defineInRange("illagerAssaultLimit", 30000, 0, Integer.MAX_VALUE);
            SoulEnergyBadOmen = BUILDER.comment("Hitting the Illager Assault Limit of Soul Energy have a chance of giving Player Bad Omen effect, Default: true")
                    .define("soulEnergyBadOmen", true);
            BUILDER.pop();
            BUILDER.push("Raid");
            IllagerRaid = BUILDER.comment("Whether Modded Illagers appears in Raids, Default: true")
                    .define("specialIllagerRaid", true);
            PikerRaid = BUILDER.comment("Whether Pikers appear in Raids, Default: true")
                    .define("pikerRaid", true);
            PreacherRaid = BUILDER.comment("Whether Preachers appear in Raids, Default: true")
                    .define("preacherRaid", true);
            ConquillagerRaid = BUILDER.comment("Whether Conquillagers appear in Raids, Default: true")
                    .define("conquillagerRaid", true);
            InquillagerRaid = BUILDER.comment("Whether Inquillagers appear in Raids, Default: true")
                    .define("inquillagerRaid", true);
            EnviokerRaid = BUILDER.comment("Whether Enviokers appear in Raids, Default: true")
                    .define("enviokerRaid", true);
            MinisterRaid = BUILDER.comment("Whether Ministers appear in Raids, Default: true")
                    .define("ministerRaid", true);
            HostileRedstoneGolemRaid = BUILDER.comment("Whether Hostile Redstone Golems appear in Raids, Default: false")
                    .define("hostileRedstoneGolemRaid", false);
            ArmoredRavagerRaid = BUILDER.comment("Whether Armored Ravagers spawn in Raids, Default: true")
                    .define("armoredRavagerRaid", true);
            WarlockRaid = BUILDER.comment("Whether Warlocks appear in Raids, Default: true")
                    .define("warlockRaid", true);
            BUILDER.pop();
        IllagueSpread = BUILDER.comment("Whether Illague Effect can spread from non Conquillagers that has the effect, Default: true")
                .define("illagueSpread", true);
        IllagerSteal = BUILDER.comment("Whether Enviokers, Inquillagers and Conquillagers can steal Totems of Souls or Totems of Undying, Default: true")
                .define("illagerSteal", true);
        BUILDER.pop();
        BUILDER.push("Villagers");
        VillagerHate = BUILDER.comment("Wearing a Dark Robe, along with variants, causes Villagers around the Player to have a negative Reputation unless said Player has 25 or more reputation among them, Default: false")
                .define("villagerHate", false);
        VillagerHateRavager = BUILDER.comment("Having an owned Ravaged or Ravager, causes Villagers around the Player to have a negative Reputation, Default: false")
                .define("villagerHateRavager", false);
        VillagerHateSpells = BUILDER.comment("Casting Spell in the presence of Villagers will cause the Player to lose a number of Reputation, set 0 to disable, Default: 0")
                .defineInRange("villagerHateSpells", 0, 0, Integer.MAX_VALUE);
        VillagerConvertWarlock = BUILDER.comment("Villagers have a chance of converting into Warlocks if they're underneath a Block of Crying Obsidian, Default: true")
                .define("villagerConvertToWarlock", true);
        BUILDER.pop();
        BUILDER.push("Misc");
            BUILDER.push("Apostle");
            ApocalypseMode = BUILDER.comment("Nether Meteors deals environmental damage. WARNING: Causes lots of lag. Default: false")
                    .define("apocalypseMode", false);
            FancierApostleDeath = BUILDER.comment("Gives Apostle an even more fancier death animation, Default: false")
                    .define("fancierApostleDeath", false);
            BUILDER.pop();
            BUILDER.push("Vizier");
            VizierMinion = BUILDER.comment("Viziers spawn Vexes instead of Irks, Default: false")
                    .define("vizierMinion", false);
            BUILDER.pop();
        InterDimensionalMobs = BUILDER.comment("Whether Goety Mobs can spawn in Overworld-like modded dimensions, Default: false")
                .define("interDimensionalMobs", false);
        WarlockSpawnWeight = BUILDER.comment("Spawn Weight for Warlock, Default: 5")
                .defineInRange("warlockSpawnWeight", 5, 0, Integer.MAX_VALUE);
        WraithSpawnWeight = BUILDER.comment("Spawn Weight for Wraith, Default: 20")
                .defineInRange("wraithSpawnWeight", 20, 0, Integer.MAX_VALUE);
        TallSkullDrops = BUILDER.comment("Whether Mobs with Tall Heads(ie. Villagers, Illagers, etc.) will drop Tall Skulls, Default: true")
                .define("tallSkullDrop", true);
        WraithAggressiveTeleport = BUILDER.comment("Whether Wraiths should teleport towards their targets if they can't see them instead of just teleporting away when they're near them, Default: true")
                .define("wraithAggressiveTeleport", true);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    public static void loadConfig(ForgeConfigSpec config, String path) {
        final CommentedFileConfig file = CommentedFileConfig.builder(new File(path))
                .sync()
                .autosave()
                .writingMode(WritingMode.REPLACE)
                .build();
        file.load();
        config.setConfig(file);
    }
}
