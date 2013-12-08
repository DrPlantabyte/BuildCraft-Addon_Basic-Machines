package cyano.basicmachines.graphics;

import buildcraft.core.gui.slots.SlotBase;
import cyano.basicmachines.PlantGrowthFormulaRegistry;
import cyano.basicmachines.blocks.ChargerTileEntity;
import cyano.basicmachines.blocks.GrowthChamberTileEntity;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class GrowthChamberContainer extends Container{

	private final GrowthChamberTileEntity entity;
	public GrowthChamberContainer(InventoryPlayer inventory, GrowthChamberTileEntity tile) {
	
		entity = tile;
		for(int i = 0; i < 9; i++){
			addSlot(i,61,17);
		}

		addSlotToContainer(new SlotBase(entity,GrowthChamberTileEntity.dirtInvIndex, 36,35));
		addSlotToContainer(new SlotBase(entity,GrowthChamberTileEntity.waterInputInvIndex, 122,17));
		addSlotToContainer(new SlotBase(entity,GrowthChamberTileEntity.waterOutputInvIndex, 122,52));
		
		// adds the player's inventory to the screen
		bindPlayerInventory(inventory,84);
	}

	private void addSlot(int index, int xoffset, int yoffset){
		int x = xoffset + ((index % 3)*18);
		int y = yoffset + ((index / 3)*18);
		addSlotToContainer(new SlotBase(entity,index, x,y));
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
    	
    	// copied from ContainerFurnace
        ItemStack itemstack = null;
        Slot slot = (Slot)this.inventorySlots.get(slotIndex);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (slotIndex < entity.getSizeInventory()) {
            	// TileEntity item slot, send to player inventory
                if (!this.mergeItemStack(itemstack1, entity.getSizeInventory(), inventorySlots.size(), true)) {
                    return null;
                }

                slot.onSlotChange(itemstack1, itemstack);
            } else {
            	// player inventory, send to input slot or to toolbar
            	if(itemstack1.itemID == 3){
            		// item is a block of dirt, put in dirt slot
            		if (!this.mergeItemStack(itemstack1, GrowthChamberTileEntity.dirtInvIndex, 
            				GrowthChamberTileEntity.dirtInvIndex+1, false)) {
                        return null;
                    }
            	} else if(FluidContainerRegistry.isFilledContainer(itemstack1)){
            		// item holds fluid, put in water input
            		FluidStack liquid = FluidContainerRegistry.getFluidForFilledItem(itemstack1);
            		if(liquid.getFluid().getID() == FluidRegistry.WATER.getID()){
	            		if (!this.mergeItemStack(itemstack1, GrowthChamberTileEntity.waterInputInvIndex, 
	            				GrowthChamberTileEntity.waterInputInvIndex+1, false)) {
	                        return null;
	                    }
            		}
            	} else if(FluidContainerRegistry.isEmptyContainer(itemstack1)){
            		// is empty bucket
            		if (!this.mergeItemStack(itemstack1, GrowthChamberTileEntity.waterOutputInvIndex, 
            				GrowthChamberTileEntity.waterOutputInvIndex+1, false)) {
                        return null;
                    }
            	} else if(PlantGrowthFormulaRegistry.getInstance().hasFormula(itemstack1)){
            		// IT'S A PLANT!!!
            		if (!this.mergeItemStack(itemstack1, 0, 9, false)) {
                        return null;
                    }
            	} else {
            		// there's nowhere to put it in the machine
            	/*	if(slotIndex >= inventorySlots.size() - 9){
            			// it is in player's hotbar, move to player's inventory
            			if (!this.mergeItemStack(itemstack1, entity.getSizeInventory(), 
            					inventorySlots.size() - 9, false)) {
                            return null;
                        }
            		} else {
            			// put it in th eplayer's hotbar
            			if (!this.mergeItemStack(itemstack1, inventorySlots.size() - 9, 
            					inventorySlots.size(), true)) {
                            return null;
                        }
            		}*/
            	}
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
