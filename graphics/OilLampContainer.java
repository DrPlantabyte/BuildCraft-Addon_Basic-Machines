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
		entity = tile;
		addSlotToContainer(new SlotBase(entity,0, 80,14));
		addSlotToContainer(new SlotBase(entity,1, 138,50));
		
		// add the player's inventory to the screen
		bindPlayerInventory(inventory,84);
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
    	ItemStack itemstack = null;
        Slot slot = (Slot)this.inventorySlots.get(slotIndex);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (slotIndex == 0 || slotIndex == 1) {
            	// the GUI slot, send to player inventory
                if (!this.mergeItemStack(itemstack1, 2, inventorySlots.size(), true)) {
                    return null;
                }

                slot.onSlotChange(itemstack1, itemstack);
            } else if (slotIndex != 1 && slotIndex != 0) {
            	// player inventory, send to input slot or to toolbar
                if (entity.isItemValidForSlot(0, itemstack)) {
                	// stick it in the input slot
                    if (!this.mergeItemStack(itemstack1, 0, 1, false)) {
                        return null;
                    }
                } else if (entity.isItemValidForSlot(1, itemstack)) {
                	// stick it in the input slot
                    if (!this.mergeItemStack(itemstack1, 1, 1, false)) {
                        return null;
                    }
                } else if (slotIndex >= 2 && slotIndex < (inventorySlots.size() - 9)) {
                	// main inventory, send to toolbar
                    if (!this.mergeItemStack(itemstack1, inventorySlots.size() - 9, inventorySlots.size(), false)) {
                        return null;
                    }
                } else if (slotIndex >= (inventorySlots.size() - 9) && slotIndex < inventorySlots.size() && !this.mergeItemStack(itemstack1, 2, (inventorySlots.size() - 9), false)) {
                    return null;
                }
            } else if (!this.mergeItemStack(itemstack1, 2, inventorySlots.size(), false)) {
                return null;
            }

            if (itemstack1.stackSize == 0) {
                slot.putStack((ItemStack)null);
            } else {
                slot.onSlotChanged();
            }

            if (itemstack1.stackSize == itemstack.stackSize) {
                return null;
            }

            slot.onPickupFromSlot(par1EntityPlayer, itemstack1);
        }

        return itemstack;
    }

}
