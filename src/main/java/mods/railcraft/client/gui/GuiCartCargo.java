/* 
 * Copyright (c) CovertJaguar, 2014 http://railcraft.info
 * 
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 */
package mods.railcraft.client.gui;

import mods.railcraft.common.carts.EntityCartCargo;
import mods.railcraft.common.carts.EntityCartTank;
import mods.railcraft.common.core.RailcraftConstants;
import mods.railcraft.common.gui.containers.ContainerCargoCart;
import mods.railcraft.common.gui.containers.ContainerTankCart;
import mods.railcraft.common.plugins.forge.LocalizationPlugin;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.StatCollector;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class GuiCartCargo extends EntityGui {

    private final String label;
    private final EntityCartCargo cart;

    public GuiCartCargo(InventoryPlayer inv, EntityCartCargo cart) {
        super(cart, new ContainerCargoCart(inv, cart), RailcraftConstants.GUI_TEXTURE_FOLDER + "gui_cart_cargo.png");
        this.cart = cart;
        label = cart.getCommandSenderName();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        int sWidth = fontRendererObj.getStringWidth(label);
        int sPos = xSize / 2 - sWidth / 2;
        fontRendererObj.drawString(label, sPos, 6, 0x404040);
        fontRendererObj.drawString(LocalizationPlugin.translate("railcraft.gui.filter"), 67, 27, 0x404040);
        fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8, (ySize - 96) + 2, 0x404040);
    }
}