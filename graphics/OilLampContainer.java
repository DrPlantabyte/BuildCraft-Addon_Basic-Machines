package cyano.basicmachines.graphics;

import java.util.logging.Level;

import cpw.mods.fml.common.FMLLog;
import cyano.basicmachines.blocks.ChargerTileEntity;
import cyano.basicmachines.blocks.IronFurnaceTileEntity;
import cyano.basicmachines.blocks.OilLampTileEntity;
import buildcraft.core.gui.BuildCraftContainer;
import buildcraft.core.gui.slots.SlotBase;
import buildcraft.core.gui.slots.SlotOutput;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.tileentity.TileEntityFurnace;

public class OilLampContainer extends Container {
 
	
	private final OilLampTileEntity entity;
	public OilLampContainer(InventoryPlayer inventory, OilLampTileEntity tile) {
		// TODO
		entity = tile;
	}

	protected void bindPlayerInventory(InventoryPlayer inventoryPlayer, int yOffset) {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, yOffset + i * 18));
			}
		}
		
		for (int i = 0; i < 9; i++) {
			addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, yOffset+58));
		}
	}
	
	@Override
    public boolean canInteractWith(EntityPlayer entityplayer) {
		return entity.isUseableByPlayer(entityplayer);
    }
	
	/**
     * Called when a player shift-clicks on a slot. You must override this or you will crash when someone does that.
     */
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int slotIndex)
    {
    	// TODO
    	return null;
    }

}
