/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2016
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.items;

import mods.railcraft.api.core.IVariantEnum;
import mods.railcraft.common.gui.tooltips.ToolTip;
import mods.railcraft.common.plugins.forge.CreativePlugin;
import mods.railcraft.common.plugins.forge.LocalizationPlugin;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemRailcraft extends Item implements IRailcraftItem {
    private float smeltingExperience = -1;
    private int rarity;
    private static final IItemPropertyGetter HELD_GETTER = new IItemPropertyGetter() {
        @Override
        @SideOnly(Side.CLIENT)
        public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
            return entityIn != null && (entityIn.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND) == stack
                    || entityIn.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND) == stack) ? 1.0F : 0.0F;
        }
    };

    public ItemRailcraft() {
        setCreativeTab(CreativePlugin.RAILCRAFT_TAB);
        addPropertyOverride(new ResourceLocation("held"), HELD_GETTER);
    }

    @Override
    public Item getObject() {
        return this;
    }

    public ItemRailcraft setRarity(int rarity) {
        this.rarity = rarity;
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public EnumRarity getRarity(ItemStack par1ItemStack) {
        return EnumRarity.values()[rarity];
    }

    public int getHeatValue(ItemStack stack) {
        return 0;
    }

    public ItemRailcraft setSmeltingExperience(float smeltingExperience) {
        this.smeltingExperience = smeltingExperience;
        return this;
    }

    @Override
    public float getSmeltingExperience(ItemStack item) {
        return smeltingExperience;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }

    public String getTooltipTag(ItemStack stack) {
        return stack.getUnlocalizedName() + ".tip";
    }

    @Nullable
    public ToolTip getToolTip(ItemStack stack, EntityPlayer player, boolean adv) {
        String tipTag = getTooltipTag(stack);
        if (LocalizationPlugin.hasTag(tipTag))
            return ToolTip.buildToolTip(tipTag);
        return null;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> info, boolean adv) {
        ToolTip toolTip = getToolTip(stack, player, adv);
        if (toolTip != null)
            info.addAll(toolTip.convertToStrings());
    }

    @Override
    public Object getRecipeObject(@Nullable IVariantEnum variant) {
        checkVariant(variant);
        String oreTag = getOreTag(variant);
        if (oreTag != null)
            return oreTag;
        if (variant != null && getHasSubtypes())
            return new ItemStack(this, 1, variant.ordinal());
        return this;
    }

    @Nullable
    public String getOreTag(@Nullable IVariantEnum variant) {
        return null;
    }
}
