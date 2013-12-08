package cyano.basicmachines.blocks;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cyano.basicmachines.BasicMachines;
import cyano.basicmachines.api.IRechargeable;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import buildcraft.core.DefaultProps;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.network.ISynchronizedTile;
import buildcraft.core.network.PacketPayload;
import buildcraft.core.network.PacketPayloadArrays;
import buildcraft.core.network.PacketTileUpdate;
import buildcraft.core.network.PacketUpdate;
import buildcraft.core.network.TilePacketWrapper;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;


/**
 * While powered, it constantly draws MJ and heats up. The hotter it is, 
 * the faster it smelts.
 */
public class ChargerTileEntity extends TileEntity implements  IPowerReceptor, ISidedInventory {

	
	static final float maxPowerUsage = 64;
	static final float minPowerUsage = 2;
	static final float maxPowerStorage = maxPowerUsage*16;
	

	private PowerHandler powerHandler;
	private PowerHandler powerHandlerDummy;
//	private float powerbuffer = 0;

	
	// Inventory slots: 0=input, 1=output (no fuel slot)
	private static final int[] slots_top = new int[] {0};
    private static final int[] slots_bottom = new int[] {0};
    private static final int[] slots_sides = new int[] {0};
    
    
    final int GUIupdateInterval = 4;
    private int updateCounter = Utils.RANDOM.nextInt();
    
    private int redstoneSignal = 0;
    
    
    private String displayName = "";
    /**
     * The ItemStacks that hold the items currently being used in the furnace
     */
    private ItemStack[] chargerItemSlot = new ItemStack[1]; 
    
    public ChargerTileEntity(){
    	powerHandler = new PowerHandler(this, Type.MACHINE);
    	powerHandler.configure(minPowerUsage, maxPowerUsage, BasicMachines.MJperChargeUnit,maxPowerStorage);
    	powerHandler.configurePowerPerdition(BasicMachines.DEFAULT_PERDITION_DRAIN, BasicMachines.DEFAULT_PERDITION_INTERVAL);
    	powerHandlerDummy = new PowerHandler(this, Type.MACHINE);
    	powerHandlerDummy.configure(0, 0, 0, 0);
    }
    
    
    
    /**
     * Returns the number of slots in the inventory.
     */
    @Override
	public int getSizeInventory()
    {
        return this.chargerItemSlot.length;
    }
    /**
     * Returns the stack in slot i
     */
    @Override
	public ItemStack getStackInSlot(int par1)
    {
        return this.chargerItemSlot[par1];
    }
    
    /**
     * Removes from an inventory slot (first arg) up to a specified number (second arg) of items and returns them in a
     * new stack.
     */
    @Override public ItemStack decrStackSize(int par1, int par2)
    {
        if (this.chargerItemSlot[par1] != null)
        {
            ItemStack itemstack;

            if (this.chargerItemSlot[par1].stackSize <= par2)
            {
                itemstack = this.chargerItemSlot[par1];
                this.chargerItemSlot[par1] = null;
                return itemstack;
            }
            else
            {
                itemstack = this.chargerItemSlot[par1].splitStack(par2);

                if (this.chargerItemSlot[par1].stackSize == 0)
                {
                    this.chargerItemSlot[par1] = null;
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
     * When some containers are closed they call this on each slot, then drop whatever it returns as an EntityItem -
     * like when you close a workbench GUI.
     */
    @Override public ItemStack getStackInSlotOnClosing(int par1)
    {
        if (this.chargerItemSlot[par1] != null)
        {
            ItemStack itemstack = this.chargerItemSlot[par1];
            this.chargerItemSlot[par1] = null;
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
    @Override public void setInventorySlotContents(int par1, ItemStack par2ItemStack)
    {
        this.chargerItemSlot[par1] = par2ItemStack;

        if (par2ItemStack != null && par2ItemStack.stackSize > this.getInventoryStackLimit())
        {
            par2ItemStack.stackSize = this.getInventoryStackLimit();
        }
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
     * Returns the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended. *Isn't
     * this more of a set than a get?*
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

    /**
     * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot.
     */
    public boolean isItemValidForSlot(int par1, ItemStack par2ItemStack)
    {
        return ( par1 == 0 ); // items in input slot only
    }

    /**
     * Returns an array containing the indices of the slots that can be accessed by automation on the given side of this
     * block.
     */
    public int[] getAccessibleSlotsFromSide(int par1)
    {
        return par1 == 0 ? slots_bottom : (par1 == 1 ? slots_top : slots_sides);
    }
    
    /**
     * Returns true if automation can insert the given item in the given slot from the given side. Args: Slot, item,
     * side
     */
    public boolean canInsertItem(int slot, ItemStack item, int side)
    {
        return this.isItemValidForSlot(slot, item);
    }

    /**
     * Returns true if automation can extract the given item in the given slot from the given side. Args: Slot, item,
     * side
     */
    public boolean canExtractItem(int slot, ItemStack item, int side)
    {
    	if(getItemChargeLevel() >= getItemMaxCharge()){
    		return true;
    	} else {
    		return false;
    	}
    	
    }
    
    boolean canCharge(){
    	if(chargerItemSlot[0] == null) return false;
    	if(BasicMachines.instance.mod_BCTools&& chargerItemSlot[0].getItem() instanceof maexx.bcTools.api.IBctChargeable){
     		return true;	
     	}
    	return chargerItemSlot[0].getItem() instanceof IRechargeable || BasicMachines.additionalRechargableItemIDs.contains(chargerItemSlot[0].getItem().itemID);
    }
    /**
     * 
     * @param energyInput Energy put in to charge the item
     * @return Energy consumed charging the item
     */
    float chargeItem(float energyInput){
    	if(chargerItemSlot[0] == null) return 0 ; // no item
    	if(getItemChargeLevel() >= getItemMaxCharge()) return 0; // item full
    	int chargePower = (int)(energyInput / BasicMachines.MJperChargeUnit);
    	int powerCharged = 0;
    	if(chargerItemSlot[0].getItem() instanceof IRechargeable){
    		IRechargeable r = (IRechargeable)chargerItemSlot[0].getItem();
    		if(r.getCurrentCharge(chargerItemSlot[0]) < r.getMaxCharge()){
    			powerCharged = r.charge(chargerItemSlot[0], (int)chargePower);
    		} else {
    			return 0;
    		}
    	} else if(BasicMachines.instance.mod_BCTools && chargerItemSlot[0].getItem() instanceof maexx.bcTools.api.IBctChargeable){
    		maexx.bcTools.api.IBctChargeable bctool = ((maexx.bcTools.api.IBctChargeable)chargerItemSlot[0].getItem());
    		int chargeNeeded = bctool.getMaxEnergyStored(chargerItemSlot[0]) - bctool.getEnergyStored(chargerItemSlot[0]);
    		if(chargeNeeded > energyInput){
    			((maexx.bcTools.api.IBctChargeable)chargerItemSlot[0].getItem()).charge(chargerItemSlot[0], (int)energyInput);
    			return (int)energyInput;
    		} else if(chargeNeeded > 0){
    			((maexx.bcTools.api.IBctChargeable)chargerItemSlot[0].getItem()).charge(chargerItemSlot[0], chargeNeeded);
    			return chargeNeeded;
    		} else {
    			return 0;
    		}
    	} else if(BasicMachines.additionalRechargableItemIDs.contains(chargerItemSlot[0].getItem().itemID)){
    		ItemStack target = chargerItemSlot[0];
    		if(chargePower > target.getItemDamage()){
    			powerCharged = target.getItemDamage();
    			target.setItemDamage(0);
    		} else if(target.getItemDamage() > 0){
    			target.setItemDamage(target.getItemDamage() - chargePower);
    			powerCharged = chargePower;
    		} else {
    			return 0;
    		}
    	} else {
    		// not rechargeable!
    		return 0;
    	}
    	return BasicMachines.MJperChargeUnit * powerCharged;
    }
    
    ///// BUILDCRAFT METHODS /////

	@Override
	public void doWork(PowerHandler provider) {
		if(redstoneSignal > 0 || this.chargerItemSlot[0] == null || canCharge() == false){
			// disabled by redstone signal
			return;
		}
		float energy = provider.useEnergy(BasicMachines.MJperChargeUnit, maxPowerUsage, false); // get usable energy
		energy = chargeItem(energy); // get needed energy
		provider.useEnergy(0, energy, true); // consume used energy
	}

	@Override
	public PowerReceiver getPowerReceiver(ForgeDirection arg0) {
		if(redstoneSignal > 0){
			return powerHandlerDummy.getPowerReceiver();
		}
		return powerHandler.getPowerReceiver();
	}

	
	
	///// MEAT AND POTATOES /////


	
	public int getComparatorOutput(){
		if(getItemMaxCharge() == 0) return 0;
		return getItemChargeLevel() * 15 / getItemMaxCharge();
	}
	
	public int getItemChargeLevel(){
    	if(chargerItemSlot[0] == null) return 0 ;
    	if(chargerItemSlot[0].getItem() instanceof IRechargeable){
    		IRechargeable r = (IRechargeable)chargerItemSlot[0].getItem();
    		return r.getCurrentCharge(chargerItemSlot[0]);
    	} else if(BasicMachines.instance.mod_BCTools && chargerItemSlot[0].getItem() instanceof maexx.bcTools.api.IBctChargeable){
    			return ((maexx.bcTools.api.IBctChargeable)chargerItemSlot[0].getItem()).getEnergyStored(chargerItemSlot[0]);
    	} else if(BasicMachines.additionalRechargableItemIDs.contains(chargerItemSlot[0].getItem().itemID)){
    		ItemStack target = chargerItemSlot[0];
    		return target.getMaxDamage() - target.getItemDamage();
    	} else {
    		return 0;
    	}
    	
	}
	public int getItemMaxCharge(){
    	if(chargerItemSlot[0] == null) return 0 ;
    	
    	if(chargerItemSlot[0].getItem() instanceof IRechargeable){
    		IRechargeable r = (IRechargeable)chargerItemSlot[0].getItem();
    		return r.getMaxCharge();
    	} else if(BasicMachines.instance.mod_BCTools && chargerItemSlot[0].getItem() instanceof maexx.bcTools.api.IBctChargeable){
    			return ((maexx.bcTools.api.IBctChargeable)chargerItemSlot[0].getItem()).getMaxEnergyStored(chargerItemSlot[0]);
    	} else if(BasicMachines.additionalRechargableItemIDs.contains(chargerItemSlot[0].getItem().itemID)){
    		ItemStack target = chargerItemSlot[0];
    		return target.getMaxDamage();
    	} else {
    		return 0;
    	}
	}
	
	public float getEnergyStore(){
		return powerHandler.getEnergyStored();
	}
	
	public float getEnergyStoreMaximum(){
		return powerHandler.getMaxEnergyStored();
	}
	
	
	private int oldCharge = 0;
	private ItemStack oldItem = null;
	private float oldEnergyStore = 0;
	/** 
	 * This is the all-important tick update loop
	 */
	@Override public void updateEntity(){
		// BuildCraft boilerplate
		if (this.worldObj.isRemote){
			// client world, do nothing (need to do everything server side to keep everything synchronized
			return;
		}
		// end BuildCraft boilerplate

		boolean flagStateChange = false;

		redstoneSignal = getWorld().getBlockPowerInput(this.xCoord,	this.yCoord, this.zCoord);
		

		powerHandler.update();
		
		updateCounter++;
		if (updateCounter % GUIupdateInterval == 0) {
			int newCharge = getItemChargeLevel();
			flagStateChange = (oldCharge != newCharge) || (oldEnergyStore != getBuffer()) ;
			boolean flag1 = (oldItem != chargerItemSlot[0]);

			
			
			if (flag1 ) {
				this.onInventoryChanged();
			}
			if(flagStateChange){
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			}
			
			oldCharge = newCharge;
			oldItem = chargerItemSlot[0];
			oldEnergyStore = getBuffer();
			// end of update interval
		}
	}
	
	
	 /**
     * Reads a tile entity from NBT.
     */
	@Override public void readFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readFromNBT(par1NBTTagCompound);
        NBTTagList nbttaglist = par1NBTTagCompound.getTagList("Items");
        this.chargerItemSlot = new ItemStack[this.getSizeInventory()];

        for (int i = 0; i < nbttaglist.tagCount(); ++i)
        {
            NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbttaglist.tagAt(i);
            byte b0 = nbttagcompound1.getByte("Slot");

            if (b0 >= 0 && b0 < this.chargerItemSlot.length)
            {
                this.chargerItemSlot[b0] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
            }
        }

        this.powerHandler.setEnergy(par1NBTTagCompound.getFloat("EnergyBuffer"));

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
        par1NBTTagCompound.setFloat("EnergyBuffer", this.powerHandler.getEnergyStored());
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < this.chargerItemSlot.length; ++i)
        {
            if (this.chargerItemSlot[i] != null)
            {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte("Slot", (byte)i);
                this.chargerItemSlot[i].writeToNBT(nbttagcompound1);
                nbttaglist.appendTag(nbttagcompound1);
            }
        }

        par1NBTTagCompound.setTag("Items", nbttaglist);

        if (this.isInvNameLocalized())
        {
            par1NBTTagCompound.setString("CustomName", this.displayName);
        }
    }



	@Override
	public World getWorld() {
		return this.worldObj;
	}

	///// network synching
	/** 
	 * update heat and cook time
	 */
	protected void sendChangeToClients(){
		if(worldObj.isRemote == true){
			// client invokation (shouldn't happen)
			return; 
		}
	    
	    worldObj.markBlockForUpdate(xCoord, yCoord, zCoord); // alternatively, use PacketDispatcher.sendPacketToAllPlayerAround(Packet, x, y, z, range);
	}
	
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
    public void onDataPacket(INetworkManager net, Packet132TileEntityData pkt)
    {
    	// get update from server
    	
	    // sanity check!    
    	if(pkt.data.hasKey("EnergyBuffer")){
    		// load data
    		this.readFromNBT(pkt.data);
    	}
	}



	public int getInputStackSize() {
		if(chargerItemSlot[0] == null) return 0;
		return chargerItemSlot[0].stackSize;
	}
	public int getOutputStackSize() {
		return chargerItemSlot[1].stackSize;
	}



	public float getBuffer() {
		return powerHandler.getEnergyStored();
	}
	public float getMaxBuffer() {
		return powerHandler.getMaxEnergyStored();
	}
	


	

	
}

