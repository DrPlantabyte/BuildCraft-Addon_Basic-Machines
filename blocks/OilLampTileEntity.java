package cyano.basicmachines.blocks;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import buildcraft.api.fuels.IronEngineFuel;
import buildcraft.api.fuels.IronEngineFuel.Fuel;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import buildcraft.core.DefaultProps;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.fluids.Tank;
import buildcraft.core.fluids.TankManager;
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
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;


/**
 * While powered, it constantly draws MJ and heats up. The hotter it is, 
 * the faster it smelts.
 */
public class OilLampTileEntity extends TileEntity implements  ISidedInventory, IFluidHandler {

	
	public final FluidTank tank = new FluidTank( FluidContainerRegistry.BUCKET_VOLUME * 2);  
    /** fuel burning in a lamp lasts this many times longer than when burned in a machine/furnace */ 
    public static int lampBurnTimeFactor = 16;
	
	ItemStack[] inventory = new ItemStack[2]; // 0 for input, 1 for output
	int[] slotAvailability = {0};
    
    private int burnTime = 0;// how many more ticks to burn before using more fuel
    
    private String displayName = "";

    
    boolean activated = false;
    
    public OilLampTileEntity(){
    	// nothing to init
    }
    
    private boolean wasBurning = false;
    private int oldVolume  = 0;
    @Override
    public void updateEntity() {
    	if(worldObj.isRemote){
    		// client world
    		return;
    	}
    	
    	boolean flagChange = false;
    	
		ItemStack stackIn = inventory[0];
		ItemStack stackOut = inventory[1];
		if (stackIn != null) {
			FluidStack liquid = FluidContainerRegistry
					.getFluidForFilledItem(stackIn);

			if (liquid != null
					&& IronEngineFuel.getFuelForFluid(liquid.getFluid()) != null
					&& (tank.getCapacity() - tank.getFluidAmount() >= FluidContainerRegistry.BUCKET_VOLUME)) {
				// the item in the intevory slot is a fuel liquid
				if (fill(ForgeDirection.UNKNOWN, liquid, false) == liquid.amount) {
					fill(ForgeDirection.UNKNOWN, liquid, true);
					setInventorySlotContents(0, Utils.consumeItem(stackIn));
					flagChange = true;
				}
			}
		} else if (stackOut != null) {
			if (FluidContainerRegistry.isEmptyContainer(stackOut) && this.getFillLevel() >= FluidContainerRegistry.BUCKET_VOLUME) {
				ItemStack result = FluidContainerRegistry
						.fillFluidContainer(tank.drain(
								FluidContainerRegistry.BUCKET_VOLUME, false),
								stackOut);
				if (result != null) {
					inventory[1] = result;
					tank.drain(FluidContainerRegistry.BUCKET_VOLUME, true);
					flagChange = true;
				}
			}
		}
		
		burn();
		if(isBurning() != wasBurning){
			OilLampBlock.updateBlockType(isBurning(), worldObj, xCoord, yCoord, zCoord);
			wasBurning=isBurning();
		}
		
		if(oldVolume != this.getFillLevel()){
			flagChange = true;
			oldVolume = getFillLevel();
		}
		
		// update clients to changes
		if(flagChange){
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
    	
	}
    
    public void burn() {
    	if(activated == false){return;}
    	if (--burnTime > 0) {
			return;
		}
    	
    	FluidStack fuel = this.tank.getFluid();
    	if(fuel == null){return;}
		if (fuel.amount > 0) {
			Fuel currentFuel = IronEngineFuel.getFuelForFluid(fuel.getFluid());
			if(currentFuel == null){return;}
//FMLLog.fine(this.getClass().getCanonicalName() + ": "+"out of burn time, going to throw some "+fuel.getFluid().getName()+" onto the fire #"+currentFuel);
			if (fuel != null && fuel.amount > 0) {
				if (--fuel.amount <= 0) {
					tank.setFluid(null);
				}
				burnTime = lampBurnTimeFactor * currentFuel.totalBurningTime
						/ FluidContainerRegistry.BUCKET_VOLUME;
FMLLog.fine(this.getClass().getCanonicalName() + ": "+ " consuming 1 " + fuel.getFluid().getName() + " ("+ currentFuel.totalBurningTime+ " ticks per unit) to burn for another " + burnTime+ " ticks");
			} else {
				currentFuel = null;
				return;
			}

		}

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
    
    public int getMetadata(){
    	return worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
    }
    
    public boolean isUseableByPlayer(EntityPlayer par1EntityPlayer)
    {
    	return this.worldObj.getBlockTileEntity(this.xCoord, this.yCoord, this.zCoord) != this ? false : par1EntityPlayer.getDistanceSq((double)this.xCoord + 0.5D, (double)this.yCoord + 0.5D, (double)this.zCoord + 0.5D) <= 64.0D;
    }
    
    public boolean isBurning(){
    	return activated && burnTime > 0; 
    }
    
    public boolean isActivated(){
    	return activated;
    }
    
    public void setActivated(boolean set){
    	this.activated = set;
    	if(worldObj.isRemote){
    		// send update from client to server
    		
    	}
    }
    /**
     * Reads a tile entity from NBT.
     */
	@Override public void readFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readFromNBT(par1NBTTagCompound);
        this.activated = par1NBTTagCompound.getBoolean("Active");
        this.burnTime = par1NBTTagCompound.getShort("BurnTime");
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
        if(par1NBTTagCompound.hasKey("Items")){
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
        }
    }

    /**
     * Writes a tile entity to NBT.
     */
	@Override public void writeToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeToNBT(par1NBTTagCompound);
        par1NBTTagCompound.setBoolean("Active", this.activated);
        par1NBTTagCompound.setShort("BurnTime", (short)this.burnTime);
        NBTTagCompound tankTag = new NBTTagCompound();
        tank.writeToNBT(tankTag);
        par1NBTTagCompound.setCompoundTag("Tank", tankTag);
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
	
///// network synching
	
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
    	NBTTagCompound nbt = pkt.data;
    	this.readFromNBT(nbt);
    
	}


	@Override
	public int getSizeInventory() {
		return inventory.length;
	}


	@Override
	public ItemStack getStackInSlot(int i) {
		return inventory[i];
	}


	@Override
	public ItemStack decrStackSize(int i, int j) {
		if (this.inventory[i] != null)
        {
            ItemStack itemstack;

            if (this.inventory[i].stackSize <= j)
            {
                itemstack = this.inventory[i];
                this.inventory[i] = null;
                return itemstack;
            }
            else
            {
                itemstack = this.inventory[i].splitStack(j);

                if (this.inventory[i].stackSize == 0)
                {
                    this.inventory[i] = null;
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
	public ItemStack getStackInSlotOnClosing(int i) {
		if (this.inventory[i] != null)
        {
            ItemStack itemstack = this.inventory[i];
            this.inventory[i] = null;
            return itemstack;
        }
        else
        {
            return null;
        }
	}


	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		this.inventory[i] = itemstack;

        if (itemstack != null && itemstack.stackSize > this.getInventoryStackLimit())
        {
        	itemstack.stackSize = this.getInventoryStackLimit();
        }
	}


	@Override
	public String getInvName() {
		return this.isInvNameLocalized() ? displayName : "container.oillamp";
	}


	@Override
	public boolean isInvNameLocalized() {
		return displayName != null && displayName.length() > 0;
	}


	@Override
	public int getInventoryStackLimit() {
		// only hold 1 item at a time
		return 1;
	}


	@Override
	public void openChest() {	}


	@Override
	public void closeChest() { }


	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		switch(i){
			case 0:
				return FluidContainerRegistry.isFilledContainer(itemstack);
			case 1:
				return FluidContainerRegistry.isEmptyContainer(itemstack);
			default:
				return false;
		}
	}


	
	@Override
	public int[] getAccessibleSlotsFromSide(int var1) {
		return slotAvailability;
	}


	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, int j) {
		// do not except items from hoppers and the like
		return false;
	}


	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j) {
		// do not give items to hoppers and the like
		return false;
	}


	public void setGuiDisplayName(String displayName) {
		this.displayName = displayName;
		
	}


	/* ITANKCONTAINER */
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

    public int getFluidLightLevel() {
            FluidStack tankFluid = tank.getFluid();
            return tankFluid == null ? 0 : tankFluid.getFluid().getLuminosity(tankFluid);
    }
    
    
   
	
}

