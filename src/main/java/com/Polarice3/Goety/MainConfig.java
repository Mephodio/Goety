package com.Polarice3.Goety;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeConfigSpec;

import java.io.File;
import java.util.List;

/**
 * Learned how to add Config from codes by @AlexModGuy
 */
public class MainConfig {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Integer> MaxSouls;
    public static final ForgeConfigSpec.ConfigValue<Integer> MaxArcaSouls;
    public static final ForgeConfigSpec.ConfigValue<Integer> SoulGuiHorizontal;
    public static final ForgeConfigSpec.ConfigValue<Integer> SoulGuiVertical;
    public static final ForgeConfigSpec.ConfigValue<Integer> FocusGuiHorizontal;
    public static final ForgeConfigSpec.ConfigValue<Integer> FocusGuiVertical;

    public static final ForgeConfigSpec.ConfigValue<Integer> SoulTakenMultiplier;
    public static final ForgeConfigSpec.ConfigValue<Integer> UndeadSouls;
    public static final ForgeConfigSpec.ConfigValue<Integer> AnthropodSouls;
    public static final ForgeConfigSpec.ConfigValue<Integer> AnimalSouls;
    public static final ForgeConfigSpec.ConfigValue<Integer> IllagerSouls;
    public static final ForgeConfigSpec.ConfigValue<Integer> VillagerSouls;
    public static final ForgeConfigSpec.ConfigValue<Integer> PiglinSouls;
    public static final ForgeConfigSpec.ConfigValue<Integer> EnderDragonSouls;
    public static final ForgeConfigSpec.ConfigValue<Integer> EndermanSouls;
    public static final ForgeConfigSpec.ConfigValue<Integer> WardenSouls;
    public static final ForgeConfigSpec.ConfigValue<Integer> PlayerSouls;
    public static final ForgeConfigSpec.ConfigValue<Integer> DefaultSouls;

    public static final ForgeConfigSpec.ConfigValue<Integer> LichHealCost;
    public static final ForgeConfigSpec.ConfigValue<Integer> LichHealSeconds;
    public static final ForgeConfigSpec.ConfigValue<Integer> DarkAnvilRepairCost;
    public static final ForgeConfigSpec.ConfigValue<Integer> DarkAnvilSoulCost;
    public static final ForgeConfigSpec.ConfigValue<Integer> SoulMenderCost;
    public static final ForgeConfigSpec.ConfigValue<Integer> SculkGrowerCost;
    public static final ForgeConfigSpec.ConfigValue<Integer> SculkGrowerCharge;
    public static final ForgeConfigSpec.ConfigValue<Double> SoulMenderSeconds;
    public static final ForgeConfigSpec.ConfigValue<Double> LichHealAmount;

    public static final ForgeConfigSpec.ConfigValue<Boolean> SpecialBossBar;
    public static final ForgeConfigSpec.ConfigValue<Boolean> BossMusic;

    public static final ForgeConfigSpec.ConfigValue<Boolean> TotemUndying;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ArcaUndying;
    public static final ForgeConfigSpec.ConfigValue<Boolean> StarterTotem;
    public static final ForgeConfigSpec.ConfigValue<Boolean> StarterBook;
    public static final ForgeConfigSpec.ConfigValue<Boolean> StarterWitchBook;
    public static final ForgeConfigSpec.ConfigValue<Boolean> SoulGuiShow;
    public static final ForgeConfigSpec.ConfigValue<Boolean> FocusGuiShow;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ShowWandCooldown;
    public static final ForgeConfigSpec.ConfigValue<Boolean> WheelGuiMovement;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ShowNum;

    public static final ForgeConfigSpec.ConfigValue<Boolean> EnableNightBeacon;
    public static final ForgeConfigSpec.ConfigValue<Boolean> DarkAnvilCap;
    public static final ForgeConfigSpec.ConfigValue<Boolean> DarkAnvilTakePoints;
    public static final ForgeConfigSpec.ConfigValue<Boolean> DarkAnvilIgnoreMaxLevels;
    public static final ForgeConfigSpec.ConfigValue<Boolean> SculkGrowerContinue;
    public static final ForgeConfigSpec.ConfigValue<Boolean> SculkGrowerPotency;
    public static final ForgeConfigSpec.ConfigValue<Boolean> PithosRespawn;
    public static final ForgeConfigSpec.ConfigValue<Boolean> CrystalBallRespawn;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> HookBellBlackList;

    public static final ForgeConfigSpec.ConfigValue<Boolean> LichSoulHeal;
    public static final ForgeConfigSpec.ConfigValue<Boolean> LichNightVision;
    public static final ForgeConfigSpec.ConfigValue<Boolean> LichDamageHelmet;
    public static final ForgeConfigSpec.ConfigValue<Boolean> LichUndeadFriends;
    public static final ForgeConfigSpec.ConfigValue<Boolean> LichMagicResist;
    public static final ForgeConfigSpec.ConfigValue<Boolean> LichPowerfulFoes;
    public static final ForgeConfigSpec.ConfigValue<Boolean> LichVillagerHate;
    public static final ForgeConfigSpec.ConfigValue<Boolean> LichScrollRequirement;

    static {
        BUILDER.push("General");
        MaxSouls = BUILDER.comment("Totem Maximum Soul Count and Threshold to save the Player, Default: 10000")
                .defineInRange("maxSouls", 10000, 10, Integer.MAX_VALUE);
        MaxArcaSouls = BUILDER.comment("Arca Maximum Soul Count, Default: 100000")
                .defineInRange("maxArcaSouls", 100000, 10, Integer.MAX_VALUE);
        TotemUndying = BUILDER.comment("Totem of Souls will save the Player if full of Soul Energy and its in Curio Slot, hotbar or off-hand, Default: true")
                .define("totemUndying", true);
        ArcaUndying = BUILDER.comment("Arca will save the Player if past Totem Maximum Soul Count, Default: true")
                .define("arcaUndying", true);
        StarterTotem = BUILDER.comment("Gives Players a Totem of Roots when first entering World, Default: false")
                .define("starterTotem", false);
        StarterBook = BUILDER.comment("Gives Players the Black Book when first entering World and Patchouli is loaded, Default: false")
                .define("starterBook", false);
        StarterWitchBook = BUILDER.comment("Gives Players the Witch's Brew when first entering World and Patchouli is loaded, Default: false")
                .define("starterWitchBook", false);
        SoulGuiShow = BUILDER.comment("Show the Soul Energy Bar if Player has Totem of Souls/Arca, Default: true")
                .define("soulGuiShow", true);
        FocusGuiShow = BUILDER.comment("Show currently equipped focus on Wand/Staff in Gui, Default: true")
                .define("focusGuiShow", true);
        ShowWandCooldown = BUILDER.comment("Whether Wands and Staffs show cooldown if the focus it's equipped with is, Default: true")
                .define("showWandCooldown", true);
        WheelGuiMovement = BUILDER.comment("Allows player movement if Focus/Brew Wheel are open, Default: true")
                .define("wheelGuiMovement", true);
        ShowNum = BUILDER.comment("Show numerical amount of Souls on the Soul Energy Bar, Default: false")
                .define("showNumber", false);
        SoulGuiHorizontal = BUILDER.comment("Horizontal Position of where the Soul Energy Bar is located, Default: 100")
                .defineInRange("soulGuiHorizontal", 100, -Integer.MAX_VALUE, Integer.MAX_VALUE);
        SoulGuiVertical = BUILDER.comment("Vertical Position of where the Soul Energy Bar is located, Default: -5")
                .defineInRange("soulGuiVertical", -5, -Integer.MAX_VALUE, Integer.MAX_VALUE);
        FocusGuiHorizontal = BUILDER.comment("Move where the equipped Focus is located horizontally from its original position (- = Left, + = Right), Default: 0")
                .defineInRange("focusGuiHorizontal", 0, -Integer.MAX_VALUE, Integer.MAX_VALUE);
        FocusGuiVertical = BUILDER.comment("Move where the equipped Focus is located vertically from its original position (- = Up, + = Down), Default: 0")
                .defineInRange("focusGuiVertical", 0, -Integer.MAX_VALUE, Integer.MAX_VALUE);
        SpecialBossBar = BUILDER.comment("Bosses from the Mod has custom looking Boss Bars. Default: true")
                .define("specialBossBar", true);
        BossMusic = BUILDER.comment("Bosses from the Mod has custom Music Playing. Default: true")
                .define("bossMusic", true);
        BUILDER.pop();
        BUILDER.push("Blocks");
        HookBellBlackList = BUILDER.comment("""
                        Add mobs that Hook Bells don't work on.\s
                        To do so, enter the namespace ID of the mob, like "minecraft:zombie, minecraft:skeleton".""")
                .defineList("hookBellBlackList", Lists.newArrayList(),
                        (itemRaw) -> itemRaw instanceof String);
        EnableNightBeacon = BUILDER.comment("Whether Night Beacons are allowed to function, turning Daytime to Midnight so long as it's activated, Default: true")
                .define("enableNightBeacon", true);
        DarkAnvilRepairCost = BUILDER.comment("Maximum level of experience the Dark Anvil will stick to instead of capping if Cap is disabled, Default: 30")
                .defineInRange("darkAnvilRepairCost", 30, 1, Integer.MAX_VALUE);
        DarkAnvilCap = BUILDER.comment("Whether Dark Anvils have a cap that prevents items from being repaired or enchanted if repair cost is exceeded, Default: false")
                .define("darkAnvilCap", false);
        DarkAnvilTakePoints = BUILDER.comment("Whether Dark Anvils will take Experience Points instead of Levels, Default: false")
                .define("darkAnvilTakePoints", false);
        DarkAnvilIgnoreMaxLevels = BUILDER.comment("Whether Dark Anvils ignores enchantment max levels when combining same enchantments, Default: true")
                .define("darkAnvilIgnoreMaxLevels", true);
        DarkAnvilSoulCost = BUILDER.comment("How much Soul Energy is required for the Dark Anvil to repair itself, Default: 1000")
                .defineInRange("darkAnvilSoulCost", 1000, 0, Integer.MAX_VALUE);
        SoulMenderCost = BUILDER.comment("The amount of Soul Energy used up to repair Items per the configured amount of seconds, Default: 1")
                .defineInRange("soulMenderCost", 1, 0, Integer.MAX_VALUE);
        SoulMenderSeconds = BUILDER.comment("How many seconds the Soul Menderer repairs 1 durability from an item, Default: 0.5")
                .defineInRange("soulMenderSeconds", 0.5, 0.0, Double.MAX_VALUE);
        SculkGrowerCost = BUILDER.comment("The amount of Soul Energy used to give Charges to the Sculk Grower, Default: 20")
                .defineInRange("sculkGrowerCost", 20, 0, Integer.MAX_VALUE);
        SculkGrowerCharge = BUILDER.comment("How much Charges is given to the Sculk Grower whenever it absorbs Soul Energy, Default: 100")
                .defineInRange("sculkGrowerCharge", 100, 0, Integer.MAX_VALUE);
        SculkGrowerContinue = BUILDER.comment("Whether Sculk Grower continues to grow plants without Redstone if it still contains charges. Setting it to false will cause the Sculk Grower to slowly decay its charges, Default: true")
                .define("sculkGrowerContinue", true);
        SculkGrowerPotency = BUILDER.comment("Whether Sculk Grower can accept Potency enchantment, which allows the Grower to grow plants faster, Default: true")
                .define("sculkGrowerPotency", true);
        PithosRespawn = BUILDER.comment("Allow looted Pithos to regenerate Loot and spawn Skull Lord again when right-clicked with respawn_boss tagged item, Default: true")
                .define("pithosRespawn", true);
        CrystalBallRespawn = BUILDER.comment("Allow used Crystal Balls to spawn Crones again when right-clicked with respawn_boss tagged item, Default: true")
                .define("crystalBallRespawn", true);
        BUILDER.pop();
        BUILDER.push("Soul Taken");
        SoulTakenMultiplier = BUILDER.comment("Multiplies the amount of Souls taken by this amount, Default: 2")
                .defineInRange("soulTakenMultiplier", 2, 1, Integer.MAX_VALUE);
        UndeadSouls = BUILDER.comment("Undead Killed, Default: 5")
                .defineInRange("undeadSouls", 5, 0, Integer.MAX_VALUE);
        AnthropodSouls = BUILDER.comment("Anthropods Killed, Default: 5")
                .defineInRange("anthropodSouls", 5, 0, Integer.MAX_VALUE);
        AnimalSouls = BUILDER.comment("Animals Killed, Default: 5")
                .defineInRange("animalSouls", 5, 0, Integer.MAX_VALUE);
        IllagerSouls = BUILDER.comment("Illagers, Witches, Cultists Killed, Default: 25")
                .defineInRange("illagerSouls", 25, 0, Integer.MAX_VALUE);
        VillagerSouls = BUILDER.comment("Villagers Killed, Default: 100")
                .defineInRange("villagerSouls", 100, 0, Integer.MAX_VALUE);
        PiglinSouls = BUILDER.comment("Non-Undead Piglin Killed, Default: 10")
                .defineInRange("piglinSouls", 10, 0, Integer.MAX_VALUE);
        EnderDragonSouls = BUILDER.comment("Ender Dragon Killed, Default: 1000")
                .defineInRange("enderDragonSouls", 1000, 0, Integer.MAX_VALUE);
        EndermanSouls = BUILDER.comment("Enderman Killed, Default: 10")
                .defineInRange("endermanSouls", 10, 0, Integer.MAX_VALUE);
        WardenSouls = BUILDER.comment("Warden Killed, Default: 10000")
                .defineInRange("wardenSouls", 10000, 0, Integer.MAX_VALUE);
        PlayerSouls = BUILDER.comment("Players Killed, Default: 100")
                .defineInRange("playerSouls", 100, 0, Integer.MAX_VALUE);
        DefaultSouls = BUILDER.comment("Others Killed, Default: 5")
                .defineInRange("otherSouls", 5, 0, Integer.MAX_VALUE);
        BUILDER.pop();
        BUILDER.push("Lich");
        LichSoulHeal = BUILDER.comment("Enable Liches healing using Soul Energy, Default: true")
                .define("lichSoulHeal", true);
        LichHealCost = BUILDER.comment("How much Soul Energy is cost to heal the Player per configured second if they've become a Lich, Default: 5")
                .defineInRange("lichHealCost", 5, 0, Integer.MAX_VALUE);
        LichHealSeconds = BUILDER.comment("How many seconds until Lich Players heals using Soul Energy, Default: 1")
                .defineInRange("lichHealSeconds", 1, 0, Integer.MAX_VALUE);
        LichHealAmount = BUILDER.comment("How much health Lich Players heals using Soul Energy, Default: 1.0")
                .defineInRange("lichHealAmount", 1.0, 0.0, Double.MAX_VALUE);
        LichNightVision = BUILDER.comment("Enable to get infinite Night Vision when being a Lich, Default: true")
                .define("lichNightVision", true);
        LichDamageHelmet = BUILDER.comment("Wearing Helmet in Sunlight as a Lich periodically damages it, Default: true")
                .define("lichDamageHelmet", true);
        LichUndeadFriends = BUILDER.comment("Undead Mobs will not attack you if you're a Lich and will even defend you if you're attack by another mob and wearing the Necro Set, Default: true")
                .define("lichUndeadFriendly", true);
        LichMagicResist = BUILDER.comment("Enable to make Liches 85% more resistant to Magic Attacks, Default: false")
                .define("lichMagicResist", false);
        LichPowerfulFoes = BUILDER.comment("If Lich Undead Friendly is set to true, Only undead that have lower than 50 Hearts are friendly, Default: true")
                .define("lichPowerfulHostile", true);
        LichVillagerHate = BUILDER.comment("If Villagers provide negative Reputation to Liches and non-Player Iron Golems are automatically aggressive against them, Default: true")
                .define("lichVillagerHate", true);
        LichScrollRequirement = BUILDER.comment("Whether the player needs to read a Forbidden Scroll to start the Potion of Transformation ritual, Default: true")
                .define("lichScrollRequirement", true);
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
