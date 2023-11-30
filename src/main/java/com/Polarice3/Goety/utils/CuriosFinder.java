package com.Polarice3.Goety.utils;

import com.Polarice3.Goety.common.entities.neutral.AbstractNecromancer;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.common.items.curios.*;
import com.Polarice3.Goety.compat.curios.CuriosLoaded;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;
import java.util.function.Predicate;

public class CuriosFinder {

    public static ItemStack findCurio(LivingEntity livingEntity, Predicate<ItemStack> filter){
        ItemStack foundStack = ItemStack.EMPTY;
        if (CuriosLoaded.CURIOS.isLoaded()) {
            Optional<SlotResult> optional = CuriosApi.getCuriosHelper().findFirstCurio(livingEntity, filter);
            if (optional.isPresent()) {
                foundStack = optional.get().stack();
            }
        }

        return foundStack;
    }

    public static boolean hasCurio(LivingEntity livingEntity, Predicate<ItemStack> filter){
        return !findCurio(livingEntity, filter).isEmpty();
    }

    public static boolean hasCurio(LivingEntity livingEntity, Item item){
        return !findCurio(livingEntity, item).isEmpty();
    }

    public static ItemStack findCurio(LivingEntity livingEntity, Item item){
        ItemStack foundStack = ItemStack.EMPTY;
        if (CuriosLoaded.CURIOS.isLoaded()) {
            Optional<SlotResult> optional = CuriosApi.getCuriosHelper().findFirstCurio(livingEntity, item);
            if (optional.isPresent()) {
                foundStack = optional.get().stack();
            }
        }

        return foundStack;
    }

    public static boolean hasDarkRobe(LivingEntity livingEntity){
        return hasCurio(livingEntity, (itemStack -> itemStack.getItem() instanceof MagicRobeItem));
    }

    public static boolean hasIllusionRobe(LivingEntity livingEntity){
        return hasCurio(livingEntity, (itemStack -> itemStack.getItem() instanceof IllusionRobeItem));
    }

    public static boolean hasWitchHat(LivingEntity livingEntity){
        return hasCurio(livingEntity, itemStack -> itemStack.getItem() instanceof WitchHatItem) || hasCurio(livingEntity, ModItems.CRONE_HAT.get());
    }

    public static boolean hasWitchRobe(LivingEntity livingEntity){
        return hasCurio(livingEntity, itemStack -> itemStack.getItem() instanceof WitchRobeItem);
    }

    public static boolean hasWarlockRobe(LivingEntity livingEntity){
        return hasCurio(livingEntity, itemStack -> itemStack.getItem() instanceof WarlockRobeItem);
    }

    public static boolean hasWitchSet(LivingEntity livingEntity){
        return hasWitchHat(livingEntity)
                && hasWitchRobe(livingEntity);
    }

    public static boolean hasNecroSet(LivingEntity livingEntity){
        return CuriosFinder.hasCurio(livingEntity, ModItems.NECRO_CROWN.get())
                && CuriosFinder.hasCurio(livingEntity, ModItems.NECRO_CAPE.get());
    }

    public static boolean hasNamelessSet(LivingEntity livingEntity){
        return CuriosFinder.hasCurio(livingEntity, ModItems.NAMELESS_CROWN.get())
                && CuriosFinder.hasCurio(livingEntity, ModItems.NAMELESS_CAPE.get());
    }

    public static boolean hasUndeadCrown(LivingEntity livingEntity){
        return CuriosFinder.hasCurio(livingEntity, ModItems.NECRO_CROWN.get())
                || CuriosFinder.hasCurio(livingEntity, ModItems.NAMELESS_CROWN.get())
                || livingEntity instanceof AbstractNecromancer;
    }

    public static boolean hasUndeadCape(LivingEntity livingEntity){
        return CuriosFinder.hasCurio(livingEntity, ModItems.NECRO_CAPE.get())
                || CuriosFinder.hasCurio(livingEntity, ModItems.NAMELESS_CAPE.get())
                || livingEntity instanceof AbstractNecromancer;
    }

    public static boolean hasUndeadSet(LivingEntity livingEntity){
        return hasUndeadCrown(livingEntity) && hasUndeadCape(livingEntity);
    }

    private static boolean isRing(ItemStack itemStack) {
        return itemStack.getItem() instanceof RingItem;
    }

    public static ItemStack findRing(Player playerEntity){
        ItemStack foundStack = ItemStack.EMPTY;
        if (CuriosLoaded.CURIOS.isLoaded()) {
            Optional<SlotResult> optional = CuriosApi.getCuriosHelper().findFirstCurio(playerEntity, CuriosFinder::isRing);
            if (optional.isPresent()) {
                foundStack = optional.get().stack();
            }
        } else {
            for (int i = 0; i < playerEntity.getInventory().getContainerSize(); i++) {
                ItemStack itemStack = playerEntity.getInventory().getItem(i);
                if (!itemStack.isEmpty() && isRing(itemStack)) {
                    foundStack = itemStack;
                    break;
                }
            }
        }

        return foundStack;
    }
}
