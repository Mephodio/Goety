package com.Polarice3.Goety.compat.jei;

import com.Polarice3.Goety.Goety;
import com.Polarice3.Goety.common.blocks.ModBlocks;
import com.Polarice3.Goety.common.crafting.RitualRecipe;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.common.items.research.ResearchScroll;
import com.Polarice3.Goety.common.research.ResearchList;
import com.Polarice3.Goety.common.ritual.EnchantItemRitual;
import com.Polarice3.Goety.common.ritual.RitualTypes;
import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ModRitualCategory implements IRecipeCategory<RitualRecipe> {
    @Nullable
    private final String ritualType;
    private final IDrawable background;
    private final IDrawable arrow;
    private final Component localizedName;
    private final ItemStack darkAltar = new ItemStack(ModBlocks.DARK_ALTAR.get());
    private final ItemStack pedestals = new ItemStack(ModItems.PEDESTAL_DUMMY.get());
    private final int iconWidth = 16;
    private final int ritualCenterX;
    private final int ritualCenterY;
    private int recipeOutputOffsetX = 50;

    public ModRitualCategory(IGuiHelper guiHelper, @Nullable String ritualType) {
        this.ritualType = ritualType;
        this.background = guiHelper.createBlankDrawable(176, 140);
        this.ritualCenterX = this.background.getWidth() / 2 - this.iconWidth / 2 - 24;
        this.ritualCenterY = this.background.getHeight() / 2 - this.iconWidth / 2 + 10;
        this.localizedName = this.ritualType == null || Objects.equals(this.ritualType, "") ? Component.translatable("goety.jei.ritual") : Component.translatable( "jei.goety.craftType." + ritualType).append(" ").append(Component.translatable("goety.jei.ritualType"));
        this.darkAltar.getOrCreateTag().putBoolean("RenderFull", true);
        this.pedestals.getOrCreateTag().putBoolean("RenderFull", true);
        this.arrow = guiHelper.createDrawable(
                new ResourceLocation(Goety.MOD_ID, "textures/gui/jei/arrow.png"), 0, 0, 64, 46);
    }

    @Override
    public RecipeType<RitualRecipe> getRecipeType() {
        if (this.ritualType == null || Objects.equals(this.ritualType, "")) {
            return JeiRecipeTypes.RITUAL;
        } else {
            return JeiRecipeTypes.getRitual(this.ritualType);
        }
    }

    @Override
    public Component getTitle() {
        return this.localizedName;
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return null;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder recipeLayout, RitualRecipe recipe, IFocusGroup ingredients) {
        this.recipeOutputOffsetX = 75;

        Ingredient activationItem = recipe.getActivationItem();

        if (recipe.getRitual() instanceof EnchantItemRitual){
            activationItem = Ingredient.of(Items.BOOK);
        }

        recipeLayout.addSlot(RecipeIngredientRole.INPUT, this.ritualCenterX, this.ritualCenterY - 15)
                .addIngredients(activationItem);
        recipeLayout.addSlot(RecipeIngredientRole.CATALYST, this.ritualCenterX, this.ritualCenterY)
                .addItemStack(this.darkAltar);

        int sacrificialCircleRadius = 30;
        int pedestalsPaddingVertical = 30;
        int pedestalsPaddingHorizontal = 15;
        List<Vec3i> pedestalsPosition = Stream.of(
                new Vec3i(this.ritualCenterX, this.ritualCenterY - sacrificialCircleRadius, 0),
                new Vec3i(this.ritualCenterX + sacrificialCircleRadius, this.ritualCenterY, 0),
                new Vec3i(this.ritualCenterX, this.ritualCenterY + sacrificialCircleRadius, 0),
                new Vec3i(this.ritualCenterX - sacrificialCircleRadius, this.ritualCenterY, 0),

                new Vec3i(this.ritualCenterX + pedestalsPaddingHorizontal,
                        this.ritualCenterY - sacrificialCircleRadius,
                        0),
                new Vec3i(this.ritualCenterX + sacrificialCircleRadius,
                        this.ritualCenterY - pedestalsPaddingVertical, 0),
                new Vec3i(this.ritualCenterX - pedestalsPaddingHorizontal,
                        this.ritualCenterY + sacrificialCircleRadius,
                        0),
                new Vec3i(this.ritualCenterX - sacrificialCircleRadius,
                        this.ritualCenterY + pedestalsPaddingVertical, 0),

                new Vec3i(this.ritualCenterX - pedestalsPaddingHorizontal,
                        this.ritualCenterY - sacrificialCircleRadius,
                        0),
                new Vec3i(this.ritualCenterX + sacrificialCircleRadius,
                        this.ritualCenterY + pedestalsPaddingVertical, 0),
                new Vec3i(this.ritualCenterX + pedestalsPaddingHorizontal,
                        this.ritualCenterY + sacrificialCircleRadius,
                        0),
                new Vec3i(this.ritualCenterX - sacrificialCircleRadius,
                        this.ritualCenterY - pedestalsPaddingVertical, 0)
        ).collect(Collectors.toList());

        for (int i = 0; i < recipe.getIngredients().size(); i++) {
            Vec3i pos = pedestalsPosition.get(i);

            recipeLayout.addSlot(RecipeIngredientRole.INPUT, pos.getX(), pos.getY() - 5)
                    .addIngredients(recipe.getIngredients().get(i));

            recipeLayout.addSlot(RecipeIngredientRole.RENDER_ONLY, pos.getX(), pos.getY())
                    .addItemStack(this.pedestals);

        }

        List<ItemStack> results = new ArrayList<>();

        if (recipe.getRitual() instanceof EnchantItemRitual && recipe.getEnchantment() != null){
            for (int i = 1; i <= recipe.getEnchantment().getMaxLevel(); ++i){
                EnchantmentInstance enchantmentInstance = new EnchantmentInstance(recipe.getEnchantment(), i);
                results.add(EnchantedBookItem.createForEnchantment(enchantmentInstance));
            }
        } else {
            results.add(recipe.getResultItem(null));
        }

        recipeLayout.addSlot(RecipeIngredientRole.OUTPUT, this.ritualCenterX + this.recipeOutputOffsetX, this.ritualCenterY - 15)
                .addItemStacks(results);

        recipeLayout.addSlot(RecipeIngredientRole.CATALYST, this.ritualCenterX + this.recipeOutputOffsetX, this.ritualCenterY)
                .addItemStack(this.darkAltar);

        recipeLayout.addSlot(RecipeIngredientRole.RENDER_ONLY, 0, 0)
                .addItemStack(this.getTypeIcon(recipe));

        ItemStack scrollStack = ItemStack.EMPTY;
        if (ResearchList.getResearch(recipe.getResearch()) != null){
            for (Item item : ForgeRegistries.ITEMS){
                if (item instanceof ResearchScroll scroll){
                    if (scroll.research == ResearchList.getResearch(recipe.getResearch())){
                        scrollStack = new ItemStack(scroll);
                        break;
                    }
                }
            }
        }
        if (!scrollStack.isEmpty()) {
            recipeLayout.addSlot(RecipeIngredientRole.RENDER_ONLY, 0, 16)
                    .addItemStack(scrollStack);
        }
    }

    public ItemStack getTypeIcon(RitualRecipe recipe){
        ItemStack itemStack = new ItemStack(Items.OBSIDIAN);
        if (recipe.getCraftType().contains(RitualTypes.ANIMATION)){
            itemStack = new ItemStack(ModItems.ANIMATION_CORE.get());
        } else if (recipe.getCraftType().contains(RitualTypes.NECROTURGY)){
            itemStack = new ItemStack(Items.SCULK);
        } else if (recipe.getCraftType().contains(RitualTypes.FORGE)){
            itemStack = new ItemStack(Items.ANVIL);
        } else if (recipe.getCraftType().contains(RitualTypes.MAGIC)){
            itemStack = new ItemStack(Items.ENCHANTING_TABLE);
        } else if (recipe.getCraftType().contains(RitualTypes.ADEPT_NETHER)){
            itemStack = new ItemStack(Items.BLACKSTONE);
        } else if (recipe.getCraftType().contains(RitualTypes.EXPERT_NETHER)){
            itemStack = new ItemStack(Items.NETHER_BRICKS);
        } else if (recipe.getCraftType().contains(RitualTypes.SABBATH)){
            itemStack = new ItemStack(Items.CRYING_OBSIDIAN);
        } else if (recipe.getCraftType().contains(RitualTypes.SKY)){
            itemStack = new ItemStack(ModBlocks.MARBLE_BLOCK.get());
        } else if (recipe.getCraftType().contains(RitualTypes.STORM)){
            itemStack = new ItemStack(Items.LIGHTNING_ROD);
        } else if (recipe.getCraftType().contains(RitualTypes.GEOTURGY)) {
            itemStack = new ItemStack(Items.AMETHYST_BLOCK);
        } else if (recipe.getCraftType().contains(RitualTypes.FROST)) {
            itemStack = new ItemStack(ModBlocks.FREEZING_LAMP.get());
        }
        return itemStack;
    }

    @Override
    public void draw(RitualRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics stack, double mouseX, double mouseY) {
        RenderSystem.enableBlend();
        this.arrow.draw(stack, this.ritualCenterX + this.recipeOutputOffsetX - 20, this.ritualCenterY);
        RenderSystem.disableBlend();

        int infoTextX = this.background.getWidth() / 2;
        int infotextY = 0;
        int initial = 14;
        if (recipe.requiresSacrifice()) {
            infotextY += initial;
            this.drawStringCentered(stack, Minecraft.getInstance().font,
                    I18n.get("jei.goety.sacrifice", I18n.get(recipe.getEntityToSacrificeDisplayName())), infoTextX, infotextY);
        }
        int sequence = 9;

        if (recipe.getRitual() instanceof EnchantItemRitual) {
            infotextY += infotextY == 0 ? initial : sequence;
            this.drawStringCentered(stack, Minecraft.getInstance().font,
                    I18n.get("jei.goety.xp", recipe.getXPLevelCost()),
                    infoTextX, infotextY);
        }

        if (recipe.getEnchantment() != null){
            infotextY += infotextY == 0 ? initial : sequence;
            this.drawStringCentered(stack, Minecraft.getInstance().font,
                    I18n.get("jei.goety.enchantment", I18n.get(recipe.getEnchantment().getDescriptionId())),
                    infoTextX, infotextY);
        }

        if (recipe.getEntityToSummon() != null) {
            infotextY += infotextY == 0 ? initial : sequence;
            this.drawStringCentered(stack, Minecraft.getInstance().font,
                    I18n.get("jei.goety.summon", I18n.get(recipe.getEntityToSummon().getDescriptionId())),
                    infoTextX, infotextY);
        }

        if (recipe.getEntityToConvert() != null) {
            infotextY += infotextY == 0 ? initial : sequence;
            this.drawStringCentered(stack, Minecraft.getInstance().font,
                    I18n.get("jei.goety.convert", I18n.get(recipe.getEntityToConvertDisplayName())), infoTextX, infotextY);
        }

        if (recipe.getEntityToConvertInto() != null) {
            infotextY += infotextY == 0 ? initial : sequence;
            this.drawStringCentered(stack, Minecraft.getInstance().font,
                    I18n.get("jei.goety.convertInto", I18n.get(recipe.getEntityToConvertInto().getDescriptionId())),
                    infoTextX, infotextY);
        }

        if (recipe.getCraftType() != null) {
            this.drawStringCentered(stack, Minecraft.getInstance().font,
                    I18n.get("jei.goety.craftType") +
                    I18n.get("jei.goety.craftType." + I18n.get(recipe.getCraftType())),
                    infoTextX, 5);
        }

        this.drawStringCentered(stack, Minecraft.getInstance().font,
                I18n.get("jei.goety.soulCost", recipe.getSoulCost()),
                infoTextX, 120);

        this.drawStringCentered(stack, Minecraft.getInstance().font,
                I18n.get("jei.goety.duration", recipe.getDuration()),
                infoTextX, 130);
    }

    protected int getStringCenteredMaxX(Font fontRenderer, String text, int x, int y) {
        int width = fontRenderer.width(text);
        int actualX = (int) (x - width / 2.0f);
        return actualX + width;
    }

    protected void drawStringCentered(GuiGraphics matrixStack, Font fontRenderer, String text, int x, int y) {
        matrixStack.drawString(fontRenderer, text, (int) (x - fontRenderer.width(text) / 2.0f), y, 0, false);
    }

}
/*
 * MIT License
 *
 * Copyright 2020 klikli-dev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */