package cyano.basicmachines.graphics;

import buildcraft.core.gui.slots.SlotBase;
import cyano.basicmachines.CompostablesRegistry;
import cyano.basicmachines.PlantGrowthFormulaRegistry;
import cyano.basicmachines.blocks.ComposterTileEntity;
import cyano.basicmachines.blocks.GrowthChamberTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class ComposterContainer  extends Container{

	
	private final ComposterTileEntity te;
	
	public ComposterContainer(InventoryPlayer inventory, ComposterTileEntity tileEntity){
		te = tileEntity;
		for(int i = 0; i < 8; i++){
			addSlot(i,80,10);
		}
		addSlotToContainer(new SlotBase(te,te.ROT_INDEX,80,61));
		addSlotToContainer(new SlotBase(te,te.OUTPUT_INDEX,132,61));
		
		// adds the player's inventory to the screen
		bindPlayerInventory(inventory,84);
	}
	
	private void addSlot(int index, int xoffset, int yoffset){
		int x = xoffset + ((index % 4)*18);
		int y = yoffset + ((index / 4)*18);
		addSlotToContainer(new SlotBase(te,index, x,y));
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
		return te.isUseableByPlayer(entityplayer);
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

            if (slotIndex < te.getSizeInventory()) {
            	// TileEntity item slot, send to player inventory
                if (!this.mergeItemStack(itemstack1, te.getSizeInventory(), inventorySlots.size(), true)) {
                    return null;
                }

                slot.onSlotChange(itemstack1, itemstack);
            } else {
            	// player inventory, send to input slot 
            	if(CompostablesRegistry.getInstance().isRegistered(itemstack1)){
            		// item is a compostable
            		if (!this.mergeItemStack(itemstack1, 0,8, false)) {
                        return null;
                    }
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
