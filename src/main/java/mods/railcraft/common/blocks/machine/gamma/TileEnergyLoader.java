/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2016
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.blocks.machine.gamma;

import mods.railcraft.api.carts.CartToolsAPI;
import mods.railcraft.api.carts.IEnergyTransfer;
import mods.railcraft.common.gui.EnumGui;
import mods.railcraft.common.gui.GuiHandler;
import mods.railcraft.common.plugins.ic2.ISinkDelegate;
import mods.railcraft.common.plugins.ic2.TileIC2SinkDelegate;
import mods.railcraft.common.util.misc.Game;
import mods.railcraft.common.util.network.IGuiReturnHandler;
import mods.railcraft.common.util.network.RailcraftInputStream;
import mods.railcraft.common.util.network.RailcraftOutputStream;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import java.io.IOException;

public class TileEnergyLoader extends TileLoaderEnergyBase implements ISinkDelegate, IGuiReturnHandler {

    private static final short[] INPUT_LEVELS = {512, 2048};
    private boolean waitTillFull = false;
    private boolean waitIfEmpty = true;
    private TileEntity sinkDelegate;

    @Override
    public EnumMachineGamma getMachineType() {
        return EnumMachineGamma.ENERGY_LOADER;
    }

    @Override
    public boolean openGui(EntityPlayer player) {
        GuiHandler.openGui(EnumGui.LOADER_ENERGY, player, worldObj, getPos());
        return true;
    }

    @Override
    public void update() {
        super.update();

        if (Game.isClient(getWorld()))
            return;

        transferredEnergy = false;
        transferRate = 0;

        EntityMinecart cart = CartToolsAPI.getMinecartOnSide(worldObj, getPos(), 0.1f, direction);

        if (cart != currentCart) {
            setPowered(false);
            currentCart = cart;
            cartWasSent();
        }

        if (cart == null)
            return;

        if (!canHandleCart(cart)) {
            sendCart(cart);
            return;
        }

        if (isPaused())
            return;

        IEnergyTransfer energyCart = (IEnergyTransfer) cart;

        if (energy > 0 && energyCart.getEnergy() < energyCart.getCapacity()) {
            double usage = (int) (energyCart.getTransferLimit() * Math.pow(1.5, overclockerUpgrades));
            double injection = (int) (energyCart.getTransferLimit() * Math.pow(1.3, overclockerUpgrades));
            if (usage > energy) {
                double ratio = (double) energy / usage;
                usage = energy;
                injection = (int) (injection * ratio);
            }

            transferRate = (int) injection;
            double extra = energyCart.injectEnergy(this, injection, getTier(), true, false, false);
            energy -= usage - extra;
            transferredEnergy = extra != injection;
        }

        if (!transferredEnergy && !isPowered() && shouldSendCart(cart))
            sendCart(cart);
    }

    @Override
    public boolean canHandleCart(EntityMinecart cart) {
        if (!super.canHandleCart(cart))
            return false;
        IEnergyTransfer energyCart = (IEnergyTransfer) cart;
        return energyCart.canInjectEnergy();
    }

    @Override
    protected boolean shouldSendCart(EntityMinecart cart) {
        if (!(cart instanceof IEnergyTransfer))
            return true;
        IEnergyTransfer energyCart = (IEnergyTransfer) cart;
        if (!waitTillFull && energyCart.getEnergy() > 0)
            return true;
        else if (!waitIfEmpty && !waitTillFull && energyCart.getEnergy() == 0)
            return true;
        else if (energyCart.getEnergy() >= energyCart.getCapacity())
            return true;
        return false;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("WaitIfEmpty", waitIfEmpty());
        data.setBoolean("WaitTillFull", waitTillFull());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        setWaitIfEmpty(data.getBoolean("WaitIfEmpty"));
        setWaitTillFull(data.getBoolean("WaitTillFull"));
    }

    @Override
    public void writePacketData(RailcraftOutputStream data) throws IOException {
        super.writePacketData(data);

        byte bits = 0;
        bits |= waitIfEmpty ? 1 : 0;
        bits |= waitTillFull ? 2 : 0;
        data.writeByte(bits);
    }

    @Override
    public void readPacketData(RailcraftInputStream data) throws IOException {
        super.readPacketData(data);

        byte bits = data.readByte();
        waitIfEmpty = (bits & 1) != 0;
        waitTillFull = (bits & 2) != 0;
    }

    @Override
    public void writeGuiData(RailcraftOutputStream data) throws IOException {
        data.writeBoolean(waitIfEmpty);
        data.writeBoolean(waitTillFull);
    }

    @Override
    public void readGuiData(RailcraftInputStream data, EntityPlayer sender) throws IOException {
        waitIfEmpty = data.readBoolean();
        waitTillFull = data.readBoolean();
    }

    @Override
    public double injectEnergy(EnumFacing directionFrom, double amount) {
        energy += amount;
        return 0;
    }

    @Override
    public boolean acceptsEnergyFrom(TileEntity emitter, EnumFacing direction) {
        return this.direction != direction;
    }

    public boolean waitTillFull() {
        return waitTillFull;
    }

    public void setWaitTillFull(boolean waitTillFull) {
        this.waitTillFull = waitTillFull;
    }

    public boolean waitIfEmpty() {
        return waitIfEmpty;
    }

    public void setWaitIfEmpty(boolean waitIfEmpty) {
        this.waitIfEmpty = waitIfEmpty;
    }

    @Override
    public double getDemandedEnergy() {
        return getCapacity() - energy;
    }

    @Override
    public int getSinkTier() {
        return transformerUpgrades > 0 ? 4 : 3;
    }

    @Override
    public TileEntity getIC2Delegate() {
        if (sinkDelegate == null)
            try {
                sinkDelegate = new TileIC2SinkDelegate(this);
            } catch (Throwable error) {
                Game.logErrorAPI("IndustrialCraft", error);
            }
        return sinkDelegate;
    }

    @Override
    public TileEntity getTile() {
        return this;
    }
}
