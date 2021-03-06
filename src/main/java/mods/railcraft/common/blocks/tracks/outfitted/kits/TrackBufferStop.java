/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2016
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.blocks.tracks.outfitted.kits;

import mods.railcraft.api.tracks.ITrackKitCustomShape;
import mods.railcraft.api.tracks.ITrackKitReversible;
import mods.railcraft.common.blocks.tracks.outfitted.TrackKits;
import mods.railcraft.common.util.misc.AABBFactory;
import mods.railcraft.common.util.misc.MiscTools;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TrackBufferStop extends TrackKitRailcraft implements ITrackKitReversible, ITrackKitCustomShape {

    private static final float CBOX = -0.0625f;
    private static final float SBOX = -0.0625f * 3;
    private static final float SBOXY = -0.0625f * 5;
    private static final AxisAlignedBB SELECTION_BOX = AABBFactory.start().box().expandHorizontally(SBOX).raiseCeiling(SBOXY).build();
    private static final AxisAlignedBB COLLISION_BOX = AABBFactory.start().box().expandHorizontally(CBOX).build();
    private boolean reversed;

    @Override
    public TrackKits getTrackKitContainer() {
        return TrackKits.BUFFER_STOP;
    }

    @Override
    public int getRenderState() {
        return reversed ? 1 : 0;
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBox() {
        return SELECTION_BOX;
    }

    @Override
    public RayTraceResult collisionRayTrace(Vec3d vec3d, Vec3d vec3d1) {
        return MiscTools.rayTraceBlock(vec3d, vec3d1, getPos());
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state) {
        return COLLISION_BOX;
    }

    @Override
    public boolean isReversed() {
        return reversed;
    }

    @Override
    public void setReversed(boolean reversed) {
        this.reversed = reversed;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbttagcompound) {
        super.writeToNBT(nbttagcompound);
        nbttagcompound.setBoolean("direction", reversed);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);
        reversed = nbttagcompound.getBoolean("direction");
    }

    @Override
    public void writePacketData(DataOutputStream data) throws IOException {
        super.writePacketData(data);

        data.writeBoolean(reversed);
    }

    @Override
    public void readPacketData(DataInputStream data) throws IOException {
        super.readPacketData(data);

        boolean r = data.readBoolean();

        if (reversed != r) {
            reversed = r;
            markBlockNeedsUpdate();
        }
    }
}
