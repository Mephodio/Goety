package com.Polarice3.Goety.common.items;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public class UnholyBloodItem extends Item {
    public static final String TAG_PURE = "Pure";

    public UnholyBloodItem() {
        super(new Item.Properties().fireResistant().rarity(Rarity.RARE));
    }

    public static boolean isPure(ItemStack p_40737_) {
        CompoundTag compoundtag = p_40737_.getTag();
        return compoundtag != null && compoundtag.contains(TAG_PURE);
    }

    public static void addPure(ItemStack p_40737_){
        CompoundTag compoundTag = p_40737_.getOrCreateTag();
        compoundTag.putBoolean(TAG_PURE, true);
    }

    public boolean isFoil(ItemStack p_40739_) {
        return isPure(p_40739_) || super.isFoil(p_40739_);
    }

    public String getDescriptionId(ItemStack p_40741_) {
        return isPure(p_40741_) ? "item.goety.pure_unholy_blood" : super.getDescriptionId(p_40741_);
    }
}
