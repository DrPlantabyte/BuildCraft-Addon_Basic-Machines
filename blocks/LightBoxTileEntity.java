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
public class LightBoxTileEntity extends TileEntity implements  IPowerReceptor {

	
	static final float maxPowerUsage = 8;
	static final float minPowerUsage = 0;
	static final float powerCapacity = 32;
	static float powerConsumption = 0f;
	

	private PowerHandler powerHandler;
	private PowerHandler powerHandlerDummy;
	
    
    final int updateInterval = 4;
    private int updateCounter = Utils.RANDOM.nextInt();
    
    private int redstoneSignal = 0;
    
    
    private String displayName = "";

    
    public LightBoxTileEntity(){
    	powerHandler = new PowerHandler(this, Type.MACHINE);
    	powerHandler.configure(minPowerUsage, maxPowerUsage, maxPowerUsage, powerCapacity);
    	powerHandler.configurePowerPerdition(1, 32);
    	powerHandlerDummy = new PowerHandler(this, Type.MACHINE);
    	powerHandlerDummy.configure(0, 0, 0, 0);
    }
    
    
    
   
    
    /**
     * Sets the custom display name to use when opening a GUI linked to this tile entity.
     */
    public void setGuiDisplayName(String par1Str)
    {
    	displayName = par1Str;
    }
    
    /** just to be sure! */
    @Override public boolean canUpdate()
    {
        return true;
    }
   
    ///// BUILDCRAFT METHODS /////

	@Override
	public void doWork(PowerHandler provider) {
	// do nothing
	}

	@Override
	public PowerReceiver getPowerReceiver(ForgeDirection arg0) {
		if(redstoneSignal > 0){
			return powerHandlerDummy.getPowerReceiver();
		}
		return powerHandler.getPowerReceiver();
	}

	
	
	///// MEAT AND POTATOES /////

	
	boolean wasOn = false;
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

		redstoneSignal = getWorld().getBlockPowerInput(this.xCoord,	this.yCoord, this.zCoord);
		

		powerHandler.update();
		powerHandler.useEnergy(0, powerConsumption, true); // burn a little power
		
		updateCounter++;
		if (updateCounter % updateInterval == 0) {
			
		
			
			boolean isOn = powerHandler.getEnergyStored() > 0.1;
			if(redstoneSignal != 0){
				isOn = false;
			}
			LightBoxOffBlock.updateBlockType(isOn, worldObj, xCoord, yCoord, zCoord);
			
			if(isOn != wasOn){
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				wasOn = isOn;
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





	

	
}

