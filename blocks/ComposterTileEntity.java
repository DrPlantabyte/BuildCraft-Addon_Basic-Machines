package cyano.basicmachines.blocks;

import cpw.mods.fml.common.FMLLog;
import cyano.basicmachines.BasicMachines;
import cyano.basicmachines.CompostablesRegistry;
import cyano.basicmachines.PlantGrowthFormulaRegistry;
import cyano.basicmachines.items.RottingMass;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;

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
	public static int TICKS_PER_UPDATE = 64;
	static final float maxPowerUsage = 8;
	static final float minPowerUsage = 2;
	static final float powerStorage = 256;
	

	private PowerHandler powerHandlerDummy;
	
	// these variables need to be saved/read 
	protected int timer = 0;
	protected PowerHandler powerHandler;
	private ItemStack[] inventory = new ItemStack[10];
	public static final int ROT_INDEX = 8;
	public static final int OUTPUT_INDEX = 9;

	private static final int[] slots_top = new int[] {0,1,2,3,4,5,6,7};
    private static final int[] slots_bottom = new int[] {OUTPUT_INDEX};
    private static final int[] slots_sides = new int[] {0,1,2,3,4,5,6,7};
	private String displayName = "";
	

    int redstoneSignal = 0;
	
	
	public ComposterTileEntity(){

		powerHandler = new PowerHandler(this, Type.MACHINE);
    	powerHandler.configure(minPowerUsage, maxPowerUsage, maxPowerUsage,powerStorage);
    	powerHandler.configurePowerPerdition(1, 64);
    	powerHandlerDummy = new PowerHandler(this, Type.MACHINE);
    	powerHandlerDummy.configure(0, 0, 1, 0);
	}
	
	@Override public void updateEntity(){
		// Meat and potatoes
		if (this.worldObj.isRemote){
			// client world, do nothing (need to do everything server side to keep everything synchronized
			return;
		}
		
		powerHandler.update();

		
		
		boolean flagChange = false;
		boolean flagInventoryChange = false;
		
		// merge rotting mass
		rotMerge:{
			for(int i = 0; i < 8; i++){
				if(inventory[i] != null && inventory[i].itemID == BasicMachines.item_RottingMass.itemID && outputOpen()){
					// move some rot into the 
					inventory[i].setItemDamage(inventory[i].getItemDamage()+1);
					if(inventory[i].getItemDamage() >= RottingMass.ITEMS_PER_COMPOST){
						inventory[i] = null;
					}
					doRot();
					flagChange = true;
					flagInventoryChange = true;
					break rotMerge;
				}
			}
		}
		
		timer = (timer + 1)%TICKS_PER_UPDATE;
		if(timer == 0){
			redstoneSignal = getWorld().getBlockPowerInput(this.xCoord,	this.yCoord, this.zCoord);
			
			doWork:{
				if (redstoneSignal > 0){
					// disabled by redstone signal
					break doWork;
				}
				// check if there are even plants in the chamber
				boolean compostablesPresent = false;
				int[] compostableIndices = new int[8];
				int size = 0;
				for(int s = 0; s < 8; s++){


					if(inventory[s] != null && CompostablesRegistry.getInstance().isRegistered(inventory[s])){
						compostablesPresent = true; 
						compostableIndices[size] = s;
						size++;
					}
				}
				
				if(compostablesPresent == false){
					break doWork; // no raw material
				}
				// time to try to do some work
				float availableEnergy = powerHandler.getEnergyStored();
				if(availableEnergy >= MJ_PER_WORK_UNIT){
					powerHandler.useEnergy(MJ_PER_WORK_UNIT, MJ_PER_WORK_UNIT, true);
					this.decrStackSize(compostableIndices[worldObj.rand.nextInt(size)],1);
					doRot();
					flagChange = true;
					flagInventoryChange = true;
				}
			}
		}
		
		if(flagInventoryChange){
			this.onInventoryChanged();
		}
		if(flagChange){
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}

	 @Override
	 public void doWork(PowerHandler provider) {
		 // work not implemented here
	}
	
	 
	 private void doRot(){
		 if(inventory[ROT_INDEX] != null && inventory[ROT_INDEX].itemID != BasicMachines.item_RottingMass.itemID){
			 // inventory slot is cloged with a wrong item
			 return;
		 }
		 // increment decay
		 if(inventory[ROT_INDEX] == null){
			 inventory[ROT_INDEX] = new ItemStack(BasicMachines.item_RottingMass);
			 inventory[ROT_INDEX].setItemDamage(RottingMass.ITEMS_PER_COMPOST);
		 } else if(inventory[ROT_INDEX].getItemDamage() > 0) {
			 inventory[ROT_INDEX].setItemDamage(inventory[ROT_INDEX].getItemDamage()-1); 
		 }
		 if(inventory[ROT_INDEX].getItemDamage() <= 0){
			 if(outputOpen()){
				 if(inventory[OUTPUT_INDEX] == null){
					 inventory[OUTPUT_INDEX] = new ItemStack(BasicMachines.item_Compost);
				 } else {
					 inventory[OUTPUT_INDEX].stackSize++;
				 }
				 inventory[ROT_INDEX] = null;
			 }
		 }
	 }
	 
	 private boolean outputOpen(){
		 return (inventory[OUTPUT_INDEX] == null) || (inventory[OUTPUT_INDEX].itemID == BasicMachines.item_Compost.itemID && inventory[OUTPUT_INDEX].stackSize < 64);
	 }
	/** Output is proportional to the number of items in the input area.*/
	public int getComparatorOutput(){
		int sum = 0;
		for(int i = 0; i < 8; i++){
			if(this.getStackInSlot(i) != null){
				sum+=this.getStackInSlot(i).stackSize;
			}
		}
		int output = sum/cyano.basicmachines.items.RottingMass.ITEMS_PER_COMPOST;
		if(output > 15){
			output = 15;
		}
		return output;
	}
	
	/**
     * Reads a tile entity from NBT.
     */
	@Override public void readFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readFromNBT(par1NBTTagCompound);
        NBTTagList nbttaglist = par1NBTTagCompound.getTagList("Items");
        this.inventory = new ItemStack[this.getSizeInventory()];

        for (int i = 0; i < nbttaglist.tagCount(); ++i)
        {
            NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbttaglist.tagAt(i);
            byte b0 = nbttagcompound1.getByte("Slot");

            if (b0 >= 0 && b0 < this.inventory.length)
            {
                this.inventory[b0] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
            }
        }

        this.powerHandler.setEnergy(par1NBTTagCompound.getFloat("Energy"));
        this.timer = par1NBTTagCompound.getShort("Time");

        if (par1NBTTagCompound.hasKey("CustomName"))
        {
            this.displayName = par1NBTTagCompound.getString("CustomName");
        }
    }

    /**
     * Writes a tile entity to NBT.
     */
	@Override public void writeToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeToNBT(par1NBTTagCompound);
        par1NBTTagCompound.setFloat("Energy", this.powerHandler.getEnergyStored());
        par1NBTTagCompound.setShort("Time", (short)timer);
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < this.inventory.length; ++i)
        {
            if (this.inventory[i] != null)
            {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte("Slot", (byte)i);
                this.inventory[i].writeToNBT(nbttagcompound1);
                nbttaglist.appendTag(nbttagcompound1);
            }
        }
        par1NBTTagCompound.setTag("Items", nbttaglist);
        
        if (this.isInvNameLocalized())
        {
            par1NBTTagCompound.setString("CustomName", this.displayName);
        }
    }
	/** used to synchronize clients to server */
	@Override
	public Packet getDescriptionPacket()
    {
        NBTTagCompound nbtData = new NBTTagCompound();
        this.writeToNBT(nbtData);
        return new Packet132TileEntityData(this.xCoord, this.yCoord, this.zCoord, 0, nbtData);
    }
	
	/**
     * Called when you receive a TileEntityData packet for the location this
     * TileEntity is currently in. On the client, the NetworkManager will always
     * be the remote server. On the server, it will be whomever is responsible for
     * sending the packet.
     *
     * @param net The NetworkManager the packet originated from
     * @param pkt The data packet
     */
    @Override public void onDataPacket(INetworkManager net, Packet132TileEntityData pkt)
    {
    	// get update from server
    	
	    // sanity check! Check for distinctive fields that tile entities machines won't have   
    	if(pkt.data.hasKey("Energy") && pkt.data.hasKey("Items") && pkt.data.hasKey("Time")){
    		// load data
    		this.readFromNBT(pkt.data);
    	}
	}
    
    @Override public int getSizeInventory() {
		return inventory.length;
	}

	@Override public ItemStack getStackInSlot(int i) {
		return inventory[i];
	}

	/**
     * Removes from an inventory slot (first arg) up to a specified number (second arg) of items and returns them in a
     * new stack.
     */
    @Override public ItemStack decrStackSize(int slot, int increment)
    {
        if (this.inventory[slot] != null)
        {
            ItemStack itemstack;

            if (this.inventory[slot].stackSize <= increment)
            {
                itemstack = this.inventory[slot];
                this.inventory[slot] = null;
                return itemstack;
            }
            else
            {
                itemstack = this.inventory[slot].splitStack(increment);

                if (this.inventory[slot].stackSize == 0)
                {
                    this.inventory[slot] = null;
                }

                return itemstack;
            }
        }
        else
        {
            return null;
        }
    }
    
    /**
     * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot.
     */
    @Override public boolean isItemValidForSlot(int slot, ItemStack item)
    {
        return (slot < 8 && CompostablesRegistry.getInstance().isRegistered(item));
    }
    
    /**
     * Returns an array containing the indices of the slots that can be accessed by automation on the given side of this
     * block.
     */
    @Override public int[] getAccessibleSlotsFromSide(int slot)
    {
        return slot == 0 ? slots_bottom : (slot == 1 ? slots_top : slots_sides);
    }
    
    /**
     * Returns true if automation can insert the given item in the given slot from the given side. Args: Slot, item,
     * side
     */
    @Override public boolean canInsertItem(int slot, ItemStack item, int side)
    {
        return this.isItemValidForSlot(slot, item);
    }
    
    /**
     * Returns true if automation can extract the given item in the given slot from the given side. Args: Slot, item,
     * side
     */
    @Override public boolean canExtractItem(int slot, ItemStack item, int side)
    {
    	if(slot == OUTPUT_INDEX){
    		return true;
    	}else {
    		return false;
    	}
    }
    
    
    @Override
	public PowerReceiver getPowerReceiver(ForgeDirection arg0) {
		if(redstoneSignal > 0){
			return powerHandlerDummy.getPowerReceiver();
		}
		return powerHandler.getPowerReceiver();
	}
    
    /**
     * When some containers are closed they call this on each slot, then drop whatever it returns as an EntityItem -
     * like when you close a workbench GUI.
     */
    @Override public ItemStack getStackInSlotOnClosing(int slot)
    {
        if (this.inventory[slot] != null)
        {
            ItemStack itemstack = this.inventory[slot];
            this.inventory[slot] = null;
            return itemstack;
        }
        else
        {
            return null;
        }
    }
    /**
     * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
     */
    @Override public void setInventorySlotContents(int slot, ItemStack item)
    {
        this.inventory[slot] = item;

        if (item != null && item.stackSize > this.getInventoryStackLimit())
        {
            item.stackSize = this.getInventoryStackLimit();
        }
    }
    /**
     * Returns the maximum stack size for a inventory slot. Seems to always be 64
     */
    @Override
	public int getInventoryStackLimit()
    {
        return 64;
    }
    /** just to be sure! */
    @Override public boolean canUpdate()
    {
        return true;
    }
    /**
     * Sets the custom display name to use when opening a GUI linked to this tile entity.
     */
    public void setGuiDisplayName(String par1Str)
    {
    	displayName = par1Str;
    }
    /**
     * Returns the name of the inventory.
     */
    @Override
	 public String getInvName()
    {
        return this.isInvNameLocalized() ? displayName : "container.ironfurnace";
    } 
    /**
     * If this returns false, the inventory name will be used as an unlocalized name, and translated into the player's
     * language. Otherwise it will be used directly.
     */
    @Override
	public boolean isInvNameLocalized()
    {
        return displayName != null && displayName.length() > 0;
    }
    /**
     * Just checks if the player is close enough to interact with this block
     */
    @Override public boolean isUseableByPlayer(EntityPlayer par1EntityPlayer)
    {
    	return getWorld().getBlockTileEntity(this.xCoord, this.yCoord, this.zCoord) != this ? false : par1EntityPlayer.getDistanceSq((double)this.xCoord + 0.5D, (double)this.yCoord + 0.5D, (double)this.zCoord + 0.5D) <= 64.0D;
    }
    
    @Override public void openChest() {} // do nothing

    @Override public void closeChest() {} // do nothing


	public float getBuffer() {
		return powerHandler.getEnergyStored();
	}
	public float getMaxBuffer() {
		return powerHandler.getMaxEnergyStored();
	}
	

	@Override
	public World getWorld() {
		return this.worldObj;
	}
    
	
	private int[] shuffle(int[] src, int size){
		int[] output = new int[size];
		boolean[] set = new boolean[size];
		java.util.Arrays.fill(set, false);
		int range = worldObj.rand.nextInt(size);
		for(int i = 0; i < size; i++){
			int index = range % size;
			while(set[index]){
				range += 7691; // 7691 is a prime number
				index = range % size;
			}
			output[index] = src[i];
			set[index] = true;
		}
		return output;
	}
}
