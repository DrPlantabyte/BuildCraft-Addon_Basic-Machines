package cyano.basicmachines.blocks;

import cpw.mods.fml.common.FMLLog;
import cyano.basicmachines.BasicMachines;
import cyano.basicmachines.PlantGrowthFormulaRegistry;
import buildcraft.api.fuels.IronEngineFuel;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import buildcraft.core.utils.Utils;
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
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class GrowthChamberTileEntity extends TileEntity implements IPowerReceptor, ISidedInventory, IFluidHandler{

	
	static final float maxPowerUsage = 4;
	static final float minPowerUsage = 2;
	static final float powerStorage = 256;
	
	static final int waterCapacity = 8; // 8 buckets
	
	
	
    /**
     * The ItemStacks that hold the items 
     */
    private ItemStack[] inventory = new ItemStack[12];
    public static final int dirtInvIndex = 9;
    public static final int waterInputInvIndex = 10;
    public static final int waterOutputInvIndex = 11;
	

	private static final int[] slots_top = new int[] {dirtInvIndex,waterInputInvIndex};
    private static final int[] slots_bottom = new int[] {0,1,2,3,4,5,6,7,8,waterOutputInvIndex};
    private static final int[] slots_sides = new int[] {dirtInvIndex,waterInputInvIndex};
    
    public final FluidTank tank = new FluidTank( FluidContainerRegistry.BUCKET_VOLUME * waterCapacity);  
    
    protected int redstoneSignal = 0;
    
    

	private PowerHandler powerHandler;
	private PowerHandler powerHandlerDummy;
	
	
	byte work = 0;
	int timer = 0;
	final int workUnitsPerGrowth = 32;
	final float energyPerWorkUnit = 5;
	final int ticksPerWorkUnit = 16;
	final int waterPerWorkUnit = 3;
	// dirt is consumed when plants multiply, not per work unit
    
	public GrowthChamberTileEntity(){
		powerHandler = new PowerHandler(this, Type.MACHINE);
    	powerHandler.configure(minPowerUsage, maxPowerUsage, BasicMachines.MJperChargeUnit,powerStorage);
    	powerHandler.configurePowerPerdition(BasicMachines.DEFAULT_PERDITION_DRAIN, BasicMachines.DEFAULT_PERDITION_INTERVAL);
    	powerHandlerDummy = new PowerHandler(this, Type.MACHINE);
    	powerHandlerDummy.configure(0, 0, 0, 0);
	}
	
	@Override public void updateEntity(){
		// Meat and potatoes
		if (this.worldObj.isRemote){
			// client world, do nothing (need to do everything server side to keep everything synchronized
			return;
		}
		
		
		
		powerHandler.update();
		
		boolean flagChange = handleFluidSlots();
		
		timer = (timer + 1)%ticksPerWorkUnit;
		if(timer == 0){
			redstoneSignal = getWorld().getBlockPowerInput(this.xCoord,	this.yCoord, this.zCoord);
			
			doWork:{
				if (redstoneSignal > 0){
					// disabled by redstone signal
					break doWork;
				}
				// check if there are even plants in the chamber
				boolean plantsPresent = false;
				for(int s = 0; s < 9; s++){
					if(inventory[s] != null 
							&& (PlantGrowthFormulaRegistry.getInstance().isRegistered(inventory[s])
									|| PlantGrowthFormulaRegistry.getInstance().isGenericPlant(inventory[s]))){
						plantsPresent = true; 
						break;
					}
				}
				if(plantsPresent == false){
					// no plants, no progress
					work = 0;
					break doWork;
				}
				
				// time to try to do some work
				float availableEnergy = powerHandler.getEnergyStored();
				int availableWater = this.getFillLevel();
				int availableDirt;
				if(work != 0 || inventory[dirtInvIndex] == null){
					availableDirt = 0; // we only use dirt at stark of work cycle
				} else {
					availableDirt = inventory[dirtInvIndex].stackSize;
				}
				if(availableWater > waterPerWorkUnit && availableEnergy > energyPerWorkUnit){
					if(work == 0){
						// need dirt to start work
						if(availableDirt == 0){
							break doWork;
						} else {
							this.decrStackSize(dirtInvIndex, 1);
						}
					}
					powerHandler.useEnergy(energyPerWorkUnit, energyPerWorkUnit, true);
					this.drain(ForgeDirection.UNKNOWN, waterPerWorkUnit, true);
					work++;
					flagChange = true;
				}
				if(work >= workUnitsPerGrowth){
					// Plant growth event
					growPlant();
					work = 0;
					flagChange = true;
				}
			}
		}
		
		if(flagChange){
			this.onInventoryChanged();
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}
	/**
	 * causes one random plant to grow 
	 */
	private void growPlant(){
		int size1 = 0;
		int size2 = 0;
		int[] filledIndices = new int[9];
		int[] emptyIndices = new int[9];
		for(int i = 0; i < 9; i++){
			if(inventory[i] != null){
				filledIndices[size1] = i;
				size1++;
			} else {
				emptyIndices[size2] = i;
				size2++;
			}
		}
		int targetIndex = filledIndices[worldObj.rand.nextInt(size1)];
		ItemStack seed = inventory[targetIndex];
		if(PlantGrowthFormulaRegistry.getInstance().isRegistered(seed)){
			ItemStack[] results = PlantGrowthFormulaRegistry.getInstance().growPlant(seed);
			// first, collect a shuffled list of available spaces, not including the target
			if(size2 > 1){emptyIndices = shuffle(emptyIndices,size2);}
			// next, remove the seed and add its empty space to the beginning of the list of empty spaces
			this.decrStackSize(targetIndex, 1);
			if(inventory[targetIndex] == null){
				emptyIndices[size2] = emptyIndices[0];
				size2++;
				emptyIndices[0] = targetIndex;
			}
			// now put the results into the empty slots until there's no room or no items left, starting with the seed's space
			for(int i = 0; i < results.length && i < emptyIndices.length; i++){
				inventory[emptyIndices[i]] = ItemStack.copyItemStack(results[i]);
				inventory[emptyIndices[i]].stackSize = 1;
			}
		} else if(PlantGrowthFormulaRegistry.getInstance().isGenericPlant(seed)){
			// generic plant replication
			if(size2 > 0){
				inventory[emptyIndices[worldObj.rand.nextInt(size2)]] = ItemStack.copyItemStack(seed);
			}
		}
	}
	
	private int[] shuffle(int[] src, int size){
		int[] output = new int[src.length];
		boolean[] set = new boolean[src.length];
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
	
	
	/** Output is the number of full slots in the growing area. If all are 
	 * full, then output is max signal*/
	public int getComparatorOutput(){
		int fullSlotCount = 0;
		for(int i = 0; i < 9; i++){
			if(this.getStackInSlot(i) != null){
				fullSlotCount++;
			}
		}
		if(fullSlotCount == 9){
			fullSlotCount = 15;
		}
		return fullSlotCount;
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
        this.work = (par1NBTTagCompound.getByte("Work"));
        this.timer = par1NBTTagCompound.getShort("Time");
        NBTTagCompound tankTag = par1NBTTagCompound.getCompoundTag("Tank");
        tank.readFromNBT(tankTag);
        if(tankTag.hasKey("Empty")){
        	// empty the tank if NBT says its empty (not default behavior got Tank.readFromNBT(...) )
        	tank.setFluid(null); 
        }

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
        par1NBTTagCompound.setByte("Work", work);
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

        NBTTagCompound tankTag = new NBTTagCompound();
        tank.writeToNBT(tankTag);
        par1NBTTagCompound.setCompoundTag("Tank", tankTag);
        
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
    	if(pkt.data.hasKey("Energy") && pkt.data.hasKey("Work") && pkt.data.hasKey("Time")){
    		// load data
    		this.readFromNBT(pkt.data);
    	}
	}
    
	private boolean handleFluidSlots(){
		boolean flagChange = false;
		ItemStack stackIn = inventory[waterInputInvIndex];
		ItemStack stackOut = inventory[waterOutputInvIndex];
		if (stackIn != null) {
			FluidStack liquid = FluidContainerRegistry
					.getFluidForFilledItem(stackIn);

			if (liquid != null
					&& liquid.fluidID == FluidRegistry.WATER.getID()
					&& (tank.getCapacity() - tank.getFluidAmount() >= FluidContainerRegistry.BUCKET_VOLUME)) {
				// the item in the intevory slot is a fuel liquid
				if (fill(ForgeDirection.UNKNOWN, liquid, false) == liquid.amount) {
					fill(ForgeDirection.UNKNOWN, liquid, true);
					setInventorySlotContents(waterInputInvIndex, Utils.consumeItem(stackIn));
					flagChange = true;
				}
			}
		} else if (stackOut != null) {
			if (FluidContainerRegistry.isEmptyContainer(stackOut) && stackOut.stackSize == 1 && this.getFillLevel() >= FluidContainerRegistry.BUCKET_VOLUME) {
				ItemStack result = FluidContainerRegistry
						.fillFluidContainer(tank.drain(
								FluidContainerRegistry.BUCKET_VOLUME, false),
								stackOut);
				if (result != null) {
					inventory[waterOutputInvIndex] = result;
					tank.drain(FluidContainerRegistry.BUCKET_VOLUME, true);
					flagChange = true;
				}
			}
		}
		return flagChange;
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
    
    private FluidStack waterStack = new FluidStack(FluidRegistry.WATER,1);
    /**
     * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot.
     */
    @Override public boolean isItemValidForSlot(int slot, ItemStack item)
    {
        switch(slot){
        case dirtInvIndex:
        	return item.itemID == 3; // must be dirt
        case waterInputInvIndex:
        	if(!FluidContainerRegistry.isFilledContainer(item)){
        		return false;
        	}
        	FluidStack liquid = FluidContainerRegistry.getFluidForFilledItem(item);
        	return liquid.containsFluid(waterStack);
        case waterOutputInvIndex:
        	return FluidContainerRegistry.isEmptyContainer(item);
        default:
        	return false;
        }
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
    	if(slot == waterOutputInvIndex){
    		return true;
    	}else {
    		return false;
    	}
    }
    
    @Override
	public void doWork(PowerHandler provider) {
		// work not implemented here
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
    String displayName = null;
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
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
            if (resource == null) {
                    return 0;
            }

            FluidStack resourceCopy = resource.copy();
            int totalUsed = 0;
            
            totalUsed += tank.fill(resourceCopy, doFill);
            
            return totalUsed;
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxEmpty, boolean doDrain) {
            return tank.drain(maxEmpty, doDrain);
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
            if (resource == null)
                    return null;
            if (!resource.isFluidEqual(tank.getFluid()))
                    return null;
            return drain(from, resource.amount, doDrain);
    }

    private FluidTankInfo[] tankInfo = new FluidTankInfo[1];
    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection direction) {
    	tankInfo[0] = tank.getInfo();
            return tankInfo;
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
            return true;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
            return false;
    }
    
    public int getFillLevel(){
    	return tank.getFluidAmount();
    }
    public int getMaxFill(){
    	return tank.getCapacity();
    }
    public Fluid getCurrentFluid(){
    	if(tank.getFluid() == null)return null;
    	return tank.getFluid().getFluid();
    }
	@Override
	public World getWorld() {
		return this.worldObj;
	}
}
