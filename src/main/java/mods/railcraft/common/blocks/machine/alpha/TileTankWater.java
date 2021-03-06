/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2016
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.blocks.machine.alpha;

import mods.railcraft.common.blocks.machine.MultiBlockPattern;
import mods.railcraft.common.blocks.machine.TileMultiBlock;
import mods.railcraft.common.blocks.machine.TileTank;
import mods.railcraft.common.fluids.FluidHelper;
import mods.railcraft.common.fluids.FluidItemHelper;
import mods.railcraft.common.fluids.Fluids;
import mods.railcraft.common.fluids.TankManager;
import mods.railcraft.common.fluids.tanks.FilteredTank;
import mods.railcraft.common.gui.EnumGui;
import mods.railcraft.common.gui.GuiHandler;
import mods.railcraft.common.gui.slots.SlotWaterOrEmpty;
import mods.railcraft.common.plugins.forge.LocalizationPlugin;
import mods.railcraft.common.util.inventory.InvTools;
import mods.railcraft.common.util.inventory.wrappers.InventoryMapper;
import mods.railcraft.common.util.misc.Game;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class TileTankWater extends TileTank {

    private static final int OUTPUT_RATE = 40;
    private static final int TANK_CAPACITY = FluidHelper.BUCKET_VOLUME * 400;
    private static final int REFILL_INTERVAL = 8;
    private static final float REFILL_RATE = 10f;
    private static final float REFILL_PENALTY_INSIDE = 0.5f;
    private static final float REFILL_PENALTY_SNOW = 0.5f;
    private static final float REFILL_BOOST_RAIN = 3.0f;
    private static final byte REFILL_RATE_MIN = 1;
    private static final int SLOT_INPUT = 0;
    private static final int SLOT_OUTPUT = 1;
    private static final int[] SLOTS = InvTools.buildSlotArray(0, 2);
    private static final EnumFacing[] LIQUID_OUTPUTS = {EnumFacing.DOWN, EnumFacing.EAST, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.SOUTH};
    private static final Predicate<TileEntity> LIQUID_OUTPUT_FILTER = tile -> {
        if (tile instanceof TileTank)
            return false;
        else if (tile instanceof IFluidHandler)
            return true;
        return false;
    };
    private static final List<MultiBlockPattern> patterns = new ArrayList<MultiBlockPattern>();
    private final FilteredTank tank;

    static {
        char[][][] map = {
                {
                        {'O', 'O', 'O', 'O', 'O'},
                        {'O', 'O', 'O', 'O', 'O'},
                        {'O', 'O', 'O', 'O', 'O'},
                        {'O', 'O', 'O', 'O', 'O'},
                        {'O', 'O', 'O', 'O', 'O'}
                },
                {
                        {'O', 'O', 'O', 'O', 'O'},
                        {'O', 'B', 'B', 'B', 'O'},
                        {'O', 'B', 'B', 'B', 'O'},
                        {'O', 'B', 'B', 'B', 'O'},
                        {'O', 'O', 'O', 'O', 'O'}
                },
                {
                        {'O', 'O', 'O', 'O', 'O'},
                        {'O', 'B', 'B', 'B', 'O'},
                        {'O', 'B', 'A', 'B', 'O'},
                        {'O', 'B', 'B', 'B', 'O'},
                        {'O', 'O', 'O', 'O', 'O'}
                },
                {
                        {'O', 'O', 'O', 'O', 'O'},
                        {'O', 'B', 'B', 'B', 'O'},
                        {'O', 'B', 'B', 'B', 'O'},
                        {'O', 'B', 'B', 'B', 'O'},
                        {'O', 'O', 'O', 'O', 'O'}
                },
                {
                        {'O', 'O', 'O', 'O', 'O'},
                        {'O', 'O', 'O', 'O', 'O'},
                        {'O', 'O', 'O', 'O', 'O'},
                        {'O', 'O', 'O', 'O', 'O'},
                        {'O', 'O', 'O', 'O', 'O'}
                },};
        patterns.add(new MultiBlockPattern(map, 2, 1, 2));
    }

    private InventoryMapper invInput = new InventoryMapper(this, SLOT_INPUT, 1);
    private InventoryMapper invOutput = new InventoryMapper(this, SLOT_OUTPUT, 1);

    public TileTankWater() {
        super("gui.tank.water", 2, patterns);
        tank = new FilteredTank(TANK_CAPACITY, Fluids.WATER.get(), this);
        tankManager.add(tank);
    }

    public static void placeWaterTank(World world, BlockPos pos, int water) {
        MultiBlockPattern pattern = TileTankWater.patterns.get(0);
        Map<Character, IBlockState> blockMapping = new HashMap<Character, IBlockState>();
        blockMapping.put('B', EnumMachineAlpha.TANK_WATER.getDefaultState());
        TileEntity tile = pattern.placeStructure(world, pos, blockMapping);
        if (tile instanceof TileTankWater) {
            TileTankWater master = (TileTankWater) tile;
            master.tank.setFluid(Fluids.WATER.get(water));
        }
    }

    @Override
    public EnumMachineAlpha getMachineType() {
        return EnumMachineAlpha.TANK_WATER;
    }

    @Override
    public String getTitle() {
        return LocalizationPlugin.translate("railcraft.gui.tank.water");
    }

    @Override
    public Slot getInputSlot(IInventory inv, int id, int x, int y) {
        return new SlotWaterOrEmpty(inv, id, x, y);
    }

    @Override
    public boolean blockActivated(EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (Game.isHost(worldObj)) {
            if (isStructureValid() && FluidHelper.handleRightClick(getTankManager(), side, player, true, true))
                return true;
        } else if (FluidItemHelper.isContainer(heldItem))
            return true;
        return super.blockActivated(player, hand, heldItem, side, hitX, hitY, hitZ);
    }

    @Override
    public void update() {
        super.update();

        if (Game.isHost(getWorld())) {
            if (isMaster()) {
                if (worldObj.provider.getDimension() != -1 && clock % REFILL_INTERVAL == 0) {
                    float rate = REFILL_RATE;
                    Biome biome = worldObj.getBiome(getPos());
                    float humidity = biome.getRainfall();
                    rate *= humidity;
//                    String debug = "Biome=" + biome.biomeName + ", Humidity=" + humidity;

                    boolean outside = false;
                    for (int x = getX() - 1; x <= getX() + 1; x++) {
                        for (int z = getZ() - 1; z <= getZ() + 1; z++) {
                            outside = worldObj.canBlockSeeSky(new BlockPos(x, getY() + 3, z));
//                            System.out.println(x + ", " + (yCoord + 3) + ", " + z);
                            if (outside)
                                break;
                        }
                    }

//                    debug += ", Outside=" + outside;
                    if (!outside)
                        rate *= REFILL_PENALTY_INSIDE;
                    else if (worldObj.isRaining())
                        if (biome.getEnableSnow())
                            rate *= REFILL_PENALTY_SNOW; //                            debug += ", Snow=true";
                        else
                            rate *= REFILL_BOOST_RAIN; //                            debug += ", Rain=true";
                    int rateFinal = MathHelper.floor_float(rate);
                    if (rateFinal < REFILL_RATE_MIN)
                        rateFinal = REFILL_RATE_MIN;
//                    debug += ", Refill=" + rateFinal;
//                    System.out.println(debug);

                    FluidStack fillStack = Fluids.WATER.get(rateFinal);
                    fill(EnumFacing.UP, fillStack, true);
                }

                if (clock % FluidHelper.BUCKET_FILL_TIME == 0)
                    //noinspection ConstantConditions
                    FluidHelper.processContainers(tankManager.get(0), this, SLOT_INPUT, SLOT_OUTPUT);
            }

            TankManager tMan = getTankManager();
            if (tMan != null)
                tMan.outputLiquid(tileCache, LIQUID_OUTPUT_FILTER, LIQUID_OUTPUTS, 0, OUTPUT_RATE);
        }
    }

    @Override
    public boolean openGui(EntityPlayer player) {
        TileMultiBlock mBlock = getMasterBlock();
        if (mBlock != null) {
            GuiHandler.openGui(EnumGui.TANK, player, worldObj, mBlock.getPos());
            return true;
        }
        return false;
    }

    @Override
    public int fill(EnumFacing from, @Nullable FluidStack resource, boolean doFill) {
        if (from != EnumFacing.UP || resource == null || !Fluids.WATER.is(resource))
            return 0;
        return super.fill(from, resource, doFill);
    }

    @Override
    @Nullable
    public FluidStack drain(EnumFacing from, @Nullable FluidStack resource, boolean doDrain) {
        if (resource == null || !Fluids.WATER.is(resource))
            return null;
        return super.drain(from, resource.amount, doDrain);
    }

    @Override
    public boolean canFill(EnumFacing from, Fluid fluid) {
        return from == EnumFacing.UP && Fluids.WATER.is(fluid);
    }

    @Override
    public boolean canDrain(EnumFacing from, Fluid fluid) {
        return from != EnumFacing.UP && Fluids.WATER.is(fluid);
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return SLOTS;
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        return isItemValidForSlot(index, itemStackIn);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return index == SLOT_OUTPUT;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (!super.isItemValidForSlot(slot, stack))
            return false;
        switch (slot) {
            case SLOT_INPUT:
                return FluidItemHelper.isRoomInContainer(stack, Fluids.WATER.get()) || FluidItemHelper.containsFluid(stack, Fluids.WATER.get());
        }
        return false;
    }
}
