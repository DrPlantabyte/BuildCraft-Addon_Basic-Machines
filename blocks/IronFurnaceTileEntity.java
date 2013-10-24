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
public class IronFurnaceTileEntity extends TileEntity implements  IPowerReceptor, ISidedInventory {

	
	static final float maxPowerUsage = 8;
	static final float minPowerUsage = 2;
	
	float heat = 0;

	private PowerHandler powerHandler;
	private PowerHandler powerHandlerDummy;
//	private float powerbuffer = 0;

	static final float powerConst = 1.4f;
	static final float lossConst = 0.00005f ;
	static final float coolrate = 0.25f;
	
	// Inventory slots: 0=input, 1=output (no fuel slot)
	private static final int[] slots_top = new int[] {0};
    private static final int[] slots_bottom = new int[] {1};
    private static final int[] slots_sides = new int[] {0,1};
    
    /** The number of ticks that the current item has been cooking for */
    int cookTime = 0;
    final int cookTimeDone = 100;
    final float cookingTemperature = 100f;
    final float ticksPerTemp = 0.03f;
    final int updateInterval = 4;
    private int updateCounter = Utils.RANDOM.nextInt();
    
    private int redstoneSignal = 0;
    
    
    private String displayName = "";
    /**
     * The ItemStacks that hold the items currently being used in the furnace
     */
    private ItemStack[] furnaceItemStacks = new ItemStack[2]; // 0=input, 1=output
    
    public IronFurnaceTileEntity(){
    	powerHandler = new PowerHandler(this, Type.MACHINE);
    	powerHandler.configure(minPowerUsage, maxPowerUsage, minPowerUsage, maxPowerUsage*20);
    	powerHandlerDummy = new PowerHandler(this, Type.MACHINE);
    	powerHandlerDummy.configure(0, 0, 0, 0);
    }
    
    
    
    /**
     * Returns the number of slots in the inventory.
     */
    @Override
	public int getSizeInventory()
    {
        return this.furnaceItemStacks.length;
    }
    /**
     * Returns the stack in slot i
     */
    @Override
	public ItemStack getStackInSlot(int par1)
    {
        return this.furnaceItemStacks[par1];
    }
    
    /**
     * Removes from an inventory slot (first arg) up to a specified number (second arg) of items and returns them in a
     * new stack.
     */
    @Override public ItemStack decrStackSize(int par1, int par2)
    {
        if (this.furnaceItemStacks[par1] != null)
        {
            ItemStack itemstack;

            if (this.furnaceItemStacks[par1].stackSize <= par2)
            {
                itemstack = this.furnaceItemStacks[par1];
                this.furnaceItemStacks[par1] = null;
                return itemstack;
            }
            else
            {
                itemstack = this.furnaceItemStacks[par1].splitStack(par2);

                if (this.furnaceItemStacks[par1].stackSize == 0)
                {
                    this.furnaceItemStacks[par1] = null;
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
        if (this.furnaceItemStacks[par1] != null)
        {
            ItemStack itemstack = this.furnaceItemStacks[par1];
            this.furnaceItemStacks[par1] = null;
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
        this.furnaceItemStacks[par1] = par2ItemStack;

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
     * Returns an integer between 0 and the passed value representing how close the current item is to being completely
     * cooked
     */
    @SideOnly(Side.CLIENT)
    public int getCookProgressScaled(int par1)
    {
//	FMLLog.log(Level.FINE, "basicmachines/"+this.getClass().getSimpleName()+": cookTime="+cookTime+", cooking item #"+(this.furnaceItemStacks[0] != null ? this.furnaceItemStacks[0].itemID : 0));
        return (cookTime * par1) / cookTimeDone;
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
     * Returns true if the furnace can smelt an item, i.e. has a source item, destination stack isn't full, etc.
     */
    private boolean canSmelt()
    {
        if (this.furnaceItemStacks[0] == null)
        {
            return false;
        }
        else
        {
            ItemStack itemstack = FurnaceRecipes.smelting().getSmeltingResult(this.furnaceItemStacks[0]);
            if (itemstack == null) {return false;}
            if (this.furnaceItemStacks[1] == null) return true;
            if (!this.furnaceItemStacks[1].isItemEqual(itemstack)) return false;
            int result = furnaceItemStacks[1].stackSize + itemstack.stackSize;
            return (result <= getInventoryStackLimit() && result <= itemstack.getMaxStackSize());
        }
    }

    /**
     * Turn one item from the furnace source stack into the appropriate smelted item in the furnace result stack
     */
    public void smeltItem()
    {
        if (this.canSmelt())
        {
            ItemStack itemstack = FurnaceRecipes.smelting().getSmeltingResult(this.furnaceItemStacks[0]);

            if (this.furnaceItemStacks[1] == null)
            {
                this.furnaceItemStacks[1] = itemstack.copy();
            }
            else if (this.furnaceItemStacks[1].isItemEqual(itemstack))
            {
                furnaceItemStacks[1].stackSize += itemstack.stackSize;
            }

            --this.furnaceItemStacks[0].stackSize;

            if (this.furnaceItemStacks[0].stackSize <= 0)
            {
                this.furnaceItemStacks[0] = null;
            }
        }
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
    	if(slot == 1){
    		return true;
    	}else {
    		return false;
    	}
    }
    
    ///// BUILDCRAFT METHODS /////

	@Override
	public void doWork(PowerHandler provider) {
	/*	if(redstoneSignal > 0){
			// disabled by redstone signal
			return;
		}
		float inputPower = provider.useEnergy(0, maxPowerUsage, true);
		powerbuffer += inputPower;
		// cooking happens on updateEntity()
		 * */
	}

	@Override
	public PowerReceiver getPowerReceiver(ForgeDirection arg0) {
		if(redstoneSignal > 0){
			return powerHandlerDummy.getPowerReceiver();
		}
		return powerHandler.getPowerReceiver();
	}

	
	
	///// MEAT AND POTATOES /////

	
	private void updateHeat(int numTicks, float amountOfEnergy ){
		// current plus gain - loss
		heat =  heat + ((powerConst * amountOfEnergy)/numTicks) - (lossConst * (heat * heat)) - coolrate;
		if(heat < 0){
			heat = 0;
		}
	//	powerbuffer = 0;

	}
	
	public float getTemperature(){
		return heat;
	}
	
	public float getEnergyStore(){
		return powerHandler.getEnergyStored();
	}
	
	public float getEnergyStoreMaximum(){
		return powerHandler.getMaxEnergyStored();
	}
	
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
		if (updateCounter % updateInterval == 0) {
			
			
			boolean flag1 = false;
			boolean wasCooking = (heat > cookingTemperature);

			
			float energy = 0;
			if(redstoneSignal == 0){
				energy = powerHandler.useEnergy(0, maxPowerUsage*updateInterval, true);
			}
			float oldHeat = heat;
			updateHeat(updateInterval,energy);
			if(heat != oldHeat){flagStateChange = true;}

			int oldCookTime = cookTime;
			if (redstoneSignal == 0) {

				// not disabled by redstone signal
				if (canSmelt() && heat > cookingTemperature) {
					// smeltable item in input and temperature is above
					// threshold
					cookTime += (int) (ticksPerTemp * heat);
					if (cookTime >= cookTimeDone) {
						// done cooking
						this.smeltItem(); // decrement the input and increment
											// the output
						flag1 = true; // flag for inventory update
						cookTime = 0;
					}
				} else {
					cookTime = 0;
				}
			} else {

			}
			boolean isCooking = (heat > cookingTemperature);
			
			if(oldCookTime != cookTime){
				flagStateChange = true;
			}

			if(wasCooking != isCooking){
				// change between glowing and non-glowing states
				IronFurnaceBlock.updateFurnaceBlockState(isCooking, worldObj, xCoord, yCoord, zCoord);
			}
			if (flag1 ) {
				this.onInventoryChanged();
			}
			if(flagStateChange){
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			}
			
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
        this.furnaceItemStacks = new ItemStack[this.getSizeInventory()];

        for (int i = 0; i < nbttaglist.tagCount(); ++i)
        {
            NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbttaglist.tagAt(i);
            byte b0 = nbttagcompound1.getByte("Slot");

            if (b0 >= 0 && b0 < this.furnaceItemStacks.length)
            {
                this.furnaceItemStacks[b0] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
            }
        }

        this.heat = par1NBTTagCompound.getFloat("Temperature");
        this.cookTime = par1NBTTagCompound.getShort("CookTime");
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
        par1NBTTagCompound.setFloat("Temperature", this.heat);
        par1NBTTagCompound.setShort("CookTime", (short)this.cookTime);
        par1NBTTagCompound.setFloat("EnergyBuffer", this.powerHandler.getEnergyStored());
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < this.furnaceItemStacks.length; ++i)
        {
            if (this.furnaceItemStacks[i] != null)
            {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte("Slot", (byte)i);
                this.furnaceItemStacks[i].writeToNBT(nbttagcompound1);
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
    	if(pkt.data.hasKey("Temperature") && pkt.data.hasKey("CookTime") && pkt.data.hasKey("EnergyBuffer")){
    		// load data
    		this.readFromNBT(pkt.data);
    	}
	}



	public int getInputStackSize() {
		if(furnaceItemStacks[0] == null) return 0;
		return furnaceItemStacks[0].stackSize;
	}
	public int getOutputStackSize() {
		return furnaceItemStacks[1].stackSize;
	}
	
	/*
	///// COPIED FROM buildcraft.core.TileBuildCraft BECAUSE INHERITANCE FROM BUILDCRAFT CLASSES IS NOT WORKING PROPERLY /////
	@SuppressWarnings("rawtypes")
	private static Map<Class, TilePacketWrapper> updateWrappers = new HashMap<Class, TilePacketWrapper>();
	@SuppressWarnings("rawtypes")
	private static Map<Class, TilePacketWrapper> descriptionWrappers = new HashMap<Class, TilePacketWrapper>();
	private TilePacketWrapper descriptionPacket;
	private TilePacketWrapper updatePacket;
	private boolean init = false;

	private void initBuildCraftTileProperties() {
		if (!updateWrappers.containsKey(this.getClass())) {
			updateWrappers.put(this.getClass(), new TilePacketWrapper(this.getClass()));
		}

		if (!descriptionWrappers.containsKey(this.getClass())) {
			descriptionWrappers.put(this.getClass(), new TilePacketWrapper(this.getClass()));
		}

		updatePacket = updateWrappers.get(this.getClass());
		descriptionPacket = descriptionWrappers.get(this.getClass());

	}

	

	@Override
	public void invalidate() {
		init = false;
		super.invalidate();
	}

	public void initialize() {
		Utils.handleBufferedDescription(this);
	}

	public void destroy() {
	}

	public void sendNetworkUpdate() {
		if (CoreProxy.proxy.isSimulating(worldObj)) {
			CoreProxy.proxy.sendToPlayers(getUpdatePacket(), worldObj, xCoord, yCoord, zCoord, DefaultProps.NETWORK_UPDATE_RANGE);
		}
	}

	@Override
	public Packet getDescriptionPacket() {
		return new PacketTileUpdate(this).getPacket();
	}

	@Override
	public PacketPayload getPacketPayload() {
		return updatePacket.toPayload(this);
	}

	@Override
	public Packet getUpdatePacket() {
		return new PacketTileUpdate(this).getPacket();
	}

	@Override
	public void handleDescriptionPacket(PacketUpdate packet) throws IOException {
		if (packet.payload instanceof PacketPayloadArrays)
			descriptionPacket.fromPayload(this, (PacketPayloadArrays) packet.payload);
	}

	@Override
	public void handleUpdatePacket(PacketUpdate packet) throws IOException {
		if (packet.payload instanceof PacketPayloadArrays)
			updatePacket.fromPayload(this, (PacketPayloadArrays) packet.payload);
	}

	@Override
	public void postPacketHandling(PacketUpdate packet) {
	}

*/


	

	
}

