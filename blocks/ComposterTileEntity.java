package cyano.basicmachines.blocks;

import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

/**
 * 
 * Here's how composting works:
 * Everytime the work variable progresses to the threshold (each increment costs energy),
 * an item is consumed from the input area and the rotting mass item is repaired a little.
 * When the rotting mass item is complete, it is removed and a compost item is added to 
 * the output slot.
 *
 */
public class ComposterTileEntity extends TileEntity implements  IPowerReceptor, ISidedInventory{

	public static float MJ_PER_WORK_UNIT = 8;
	public static int WORK_UNITS_PER_TURNOVER = 8;
	public static int TICKS_PER_UPDATE = 16;
	static final float maxPowerUsage = 8;
	static final float minPowerUsage = 2;
	

	private PowerHandler powerHandlerDummy;
	
	// these variables need to be saved/read 
	protected int ticksToNextUpdate = 0;
	protected int workDone = 0;
	protected PowerHandler powerHandler;
	private ItemStack[] inventory = new ItemStack[10];
	private String displayName = "";
	
	public int getSizeInventory() {
		return inventory.length;
	}

	public ItemStack getStackInSlot(int i) {
		return inventory[i];
	}

	@Override
	public ItemStack decrStackSize(int par1, int par2)
    {
        if (this.inventory[par1] != null)
        {
            ItemStack itemstack;

            if (this.inventory[par1].stackSize <= par2)
            {
                itemstack = this.inventory[par1];
                this.inventory[par1] = null;
                return itemstack;
            }
            else
            {
                itemstack = this.inventory[par1].splitStack(par2);

                if (this.inventory[par1].stackSize == 0)
                {
                    this.inventory[par1] = null;
                }

                return itemstack;
            }
        }
        else
        {
            return null;
        }
    }

	@Override
	public ItemStack getStackInSlotOnClosing(int par1)
    {
        if (this.inventory[par1] != null)
        {
            ItemStack itemstack = this.inventory[par1];
            this.inventory[par1] = null;
            return itemstack;
        }
        else
        {
            return null;
        }
    }

	@Override
	public void setInventorySlotContents(int par1, ItemStack par2ItemStack)
    {
        this.inventory[par1] = par2ItemStack;

        if (par2ItemStack != null && par2ItemStack.stackSize > this.getInventoryStackLimit())
        {
            par2ItemStack.stackSize = this.getInventoryStackLimit();
        }
    }

	
	@Override
	public String getInvName() {
		return this.isInvNameLocalized() ? displayName : "basicmachines.composter";
	}

	@Override
	public boolean isInvNameLocalized() {
		return displayName != null && displayName.length() > 0;
	}

	@Override
	public int getInventoryStackLimit() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void openChest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void closeChest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int var1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, int j) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void doWork(PowerHandler arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public PowerReceiver getPowerReceiver(ForgeDirection arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public World getWorld() {
		// TODO Auto-generated method stub
		return null;
	}

}
