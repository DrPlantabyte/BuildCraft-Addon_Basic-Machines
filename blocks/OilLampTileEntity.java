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
import buildcraft.core.fluids.Tank;
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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;


/**
 * While powered, it constantly draws MJ and heats up. The hotter it is, 
 * the faster it smelts.
 */
public class OilLampTileEntity extends TileEntity implements  ISidedInventory, IFluidHandler {

	
	public final Tank tank = new Tank("tankFuel", FluidContainerRegistry.BUCKET_VOLUME * 2, this);

	
	ItemStack[] inventory = new ItemStack[1];
	int[] slotAvailability = {0};
    
    private int burnTime = 0;// how manymore ticks to burn before using more fuel
    
    private String displayName = "";

    
    public OilLampTileEntity(){
    	// TODO init
    }
    
    private boolean wasBurning = false;
    
    @Override
    public void updateEntity() {
    	if(worldObj.isRemote){
    		// client world
    		return;
    	}
    }
    
    
    public int getMetadata(){
    	return worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
    }
    
    public boolean isUseableByPlayer(EntityPlayer par1EntityPlayer)
    {
    	return this.worldObj.getBlockTileEntity(this.xCoord, this.yCoord, this.zCoord) != this ? false : par1EntityPlayer.getDistanceSq((double)this.xCoord + 0.5D, (double)this.yCoord + 0.5D, (double)this.zCoord + 0.5D) <= 64.0D;
    }
    
    private boolean isBurning(){
    	// TODO
    	return true;
    }
    /**
     * Reads a tile entity from NBT.
     */
	@Override public void readFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readFromNBT(par1NBTTagCompound);

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
    //	if( pkt.data.hasKey("EnergyBuffer")){
    		// load data
    		this.readFromNBT(pkt.data);
    //	}
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
		// only except liquid fuel sources
		
		// TODO Auto-generated method stub
		return false;
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


	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource,
			boolean doDrain) {
		return null; // consume the item
	}


	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		return tank.drain(maxDrain, doDrain); // pull the fuel back out
	}


	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		// TODO Auto-generated method stub
		return null;
	}
	
}

