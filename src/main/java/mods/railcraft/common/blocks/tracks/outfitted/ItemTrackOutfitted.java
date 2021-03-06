/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2016
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.blocks.tracks.outfitted;

import mods.railcraft.api.core.ILocalizedObject;
import mods.railcraft.api.core.items.ITrackItem;
import mods.railcraft.api.tracks.TrackKit;
import mods.railcraft.api.tracks.TrackRegistry;
import mods.railcraft.api.tracks.TrackType;
import mods.railcraft.client.render.models.resource.ModelManager;
import mods.railcraft.common.blocks.tracks.ItemTrack;
import mods.railcraft.common.core.RailcraftConstants;
import mods.railcraft.common.plugins.forge.CreativePlugin;
import mods.railcraft.common.plugins.forge.LocalizationPlugin;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ItemTrackOutfitted extends ItemTrack implements ITrackItem {
    public static final String MODEL_PREFIX = "track_outfitted_item.";

    public ItemTrackOutfitted(Block block) {
        super(block);
        setMaxDamage(0);
        setHasSubtypes(true);
        setCreativeTab(CreativePlugin.TRACK_TAB);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void initializeClient() {
        ArrayList<ModelResourceLocation> textures = new ArrayList<>();
        for (TrackType trackType : TrackRegistry.TRACK_TYPE.getVariants().values()) {
            for (TrackKit trackKit : TrackRegistry.TRACK_KIT.getVariants().values()) {
                textures.add(new ModelResourceLocation(
                        new ResourceLocation(RailcraftConstants.RESOURCE_DOMAIN,
                                MODEL_PREFIX + trackType.getName() + "." + trackKit.getName()), "inventory"));
            }
        }
        ModelManager.registerComplexItemModel(this, (stack -> new ModelResourceLocation(
                new ResourceLocation(RailcraftConstants.RESOURCE_DOMAIN,
                        MODEL_PREFIX + getSuffix(stack)), "inventory")), textures.toArray(new ModelResourceLocation[textures.size()]));
    }

    @Override
    public int getMetadata(int i) {
        return 0;
    }

    private String getSuffix(ItemStack stack) {
        return TrackRegistry.TRACK_TYPE.get(stack).getName() + "." + TrackRegistry.TRACK_KIT.get(stack).getName();
    }

    @Override
    public String getUnlocalizedName() {
        return "tile.railcraft.track_outfitted";
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return getUnlocalizedName() + "." + getSuffix(stack);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        String locTag = getUnlocalizedName(stack) + ".name";
        if (LocalizationPlugin.hasTag(locTag))
            return LocalizationPlugin.translateFast(locTag);
        Map<String, ILocalizedObject> args = new HashMap<>();
        args.put("track_type", TrackRegistry.TRACK_TYPE.get(stack));
        args.put("track_kit", TrackRegistry.TRACK_KIT.get(stack));
        return LocalizationPlugin.translateArgs(getUnlocalizedName() + ".name", args);
    }

    @Override
    public String getTooltipTag(ItemStack stack) {
        return TrackRegistry.TRACK_KIT.get(stack).getLocalizationTag().replace(".name", ".tip");
    }

    @Override
    public BlockTrackOutfitted getPlacedBlock() {
        return (BlockTrackOutfitted) getBlock();
    }

    @Override
    public boolean isPlacedTileEntity(ItemStack stack, TileEntity tile) {
        if (tile instanceof TileTrackOutfitted) {
            TileTrackOutfitted track = (TileTrackOutfitted) tile;
            if (track.getTrackKitInstance().getTrackKit() == TrackRegistry.TRACK_KIT.get(stack))
                return true;
        }
        return false;
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
        TrackKit trackKit = TrackRegistry.TRACK_KIT.get(stack);
        newState = newState.withProperty(BlockTrackOutfitted.TICKING, trackKit.requiresTicks());
        return super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
    }
}
