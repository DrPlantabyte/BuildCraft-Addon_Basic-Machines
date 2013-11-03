package cyano.basicmachines.blocks;

import cpw.mods.fml.common.network.PacketDispatcher;
import cyano.basicmachines.BasicMachines;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.IPowerEmitter;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.core.TileBuffer;
import buildcraft.core.utils.Utils;

public class StorageCellTileEntity  extends TileEntity implements  IPowerReceptor, IPowerEmitter{

	static final float maxCurrent = 16;
	static final float minimumChargeCurrent = 1.5f;
	static final int GUIupdateInterval = 8;
	private int GUIupdateCounter = Utils.RANDOM.nextInt();
	final float inputChargeBufferSize = maxCurrent*8;
	final float outputChargeBufferSize = maxCurrent*8;
	
	float inputCurrent = 0f;
	float outputCurrent = 0f;
	final float maxCurrentDelta = 1.0f;
	
	float charge = 0;

	final PowerHandler inputHandler;
//	final PowerHandler outputHandler; // not used for power distribution
	
	public StorageCellTileEntity(){
		super();
		inputHandler = new PowerHandler(this,PowerHandler.Type.STORAGE);
		inputHandler.configure(minimumChargeCurrent, maxCurrent, minimumChargeCurrent, inputChargeBufferSize);
		inputHandler.configurePowerPerdition(1, 100);
		
		
	}
	
	
	@Override
	public boolean canEmitPowerFrom(ForgeDirection dir) {
		return (dir != ForgeDirection.UP && dir != ForgeDirection.DOWN);
	}

	/** pulls power from input buffer */
	@Override
	public void doWork(PowerHandler ph) {
		float empty = BasicMachines.storageCellCapacity - charge;
		float in = 0;
		if(empty > maxCurrent){
			in = ph.useEnergy(0, maxCurrent, true);
		}else if(empty > 0){
			in = ph.useEnergy(0, BasicMachines.storageCellCapacity-charge, true);
		}
		charge += in;
		inputCurrent = in;
	}
	 /** puts energy in the output power handler */
//	private void chargeOutputBuffer() {
//			if(charge <= 0) {
//				return;
//			}
//			float requirement = outputHandler.getMaxEnergyStored() - outputHandler.getEnergyStored();
//			if(requirement > charge){
//				requirement = charge;
//			}
//			if(requirement > maxCurrent){
//				requirement = maxCurrent;
//			}
//			charge -= requirement;
//			outputHandler.addEnergy(requirement);
//			outputCurrent = requirement;
//		}

	@Override
	public PowerReceiver getPowerReceiver(ForgeDirection dir) {
		if (dir == ForgeDirection.DOWN || dir == ForgeDirection.UP) {
			return inputHandler.getPowerReceiver();
		} else {
		//	return outputHandler.getPowerReceiver();
			return null;
		}
	}

	@Override
	public World getWorld() {
		return worldObj;
	}
	
	private int redstoneSignal = 0;
	private float oldInputCurrent = 0;
	private float oldOutputCurrent = 0;
	private float oldCharge = 0;
	
	/** 
	 * This is the all-important tick update loop
	 */
	@Override public void updateEntity(){
		if (this.worldObj.isRemote){
			// client world, do nothing (need to do everything server side to keep everything synchronized
			return;
		}
		
		
		redstoneSignal = getWorld().getBlockPowerInput(this.xCoord,	this.yCoord, this.zCoord);
		
		

		boolean flagChargeChange = false,flagRedstoneChange=false; // if true, send updates
		
		int oldRedstone = getComparatorOutput();
		
		inputHandler.update();
		
		if(redstoneSignal > 0){
			// disable with redstone
		} else {
			// charge the output buffer 
		//	chargeOutputBuffer();
			// send power out
			float power = charge;
			if(power > maxCurrent){
				power = maxCurrent;
			}
			outputCurrent = sendPowerToAll(power);
			charge -= outputCurrent;
		}
		
		// calculate flags
		if(oldCharge != charge ){
			flagChargeChange = true;
		}
		
		if(oldRedstone != getComparatorOutput()){
			flagRedstoneChange = true;
		}
		
		// act upon flags
		if (flagChargeChange) {
			this.onInventoryChanged(); // marks the chunk as changed so that it will be saved when the game exits
		}
		
		if(flagRedstoneChange){
			worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, this.getBlockType().blockID); // update any adjacent comparators
		}
		
		if(GUIupdateCounter % GUIupdateInterval == 0 ){
			if(oldCharge != charge || oldInputCurrent != inputCurrent || oldOutputCurrent != outputCurrent){
				sendChangeToClients();
			}
			
			oldInputCurrent = inputCurrent;
			oldOutputCurrent = outputCurrent;
			oldCharge = charge;
		}
		GUIupdateCounter++;
	}

	


	/**
     * Reads a tile entity from NBT.
     */
	@Override public void readFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readFromNBT(par1NBTTagCompound);

        this.charge = par1NBTTagCompound.getFloat("Charge");
       
    }

    /**
     * Writes a tile entity to NBT.
     */
	@Override public void writeToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeToNBT(par1NBTTagCompound);
        par1NBTTagCompound.setFloat("Charge", this.charge);
    }
	
	 
	
	/** 
	 * update current and charge
	 */
	protected void sendChangeToClients(){
		if(worldObj.isRemote == true){
			// client invokation (shouldn't happen)
			return; 
		}
		PacketDispatcher.sendPacketToAllAround( xCoord, yCoord, zCoord, 16.0, worldObj.provider.dimensionId, this.getDescriptionPacket());
	//	worldObj.markBlockForUpdate(xCoord, yCoord, zCoord); // alternatively, use PacketDispatcher.sendPacketToAllPlayerAround(Packet, x, y, z, range);
	}
	
	@Override
	public Packet getDescriptionPacket()
    {
        NBTTagCompound nbtData = new NBTTagCompound();
        this.writeToNBT(nbtData);
        nbtData.setFloat("CurrentOut", this.outputCurrent);
        nbtData.setFloat("CurrentIn", this.inputCurrent);
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
    	if(pkt.data.hasKey("Charge") ){
    		// load data
    		this.readFromNBT(pkt.data);
    	}
    	// display sync data
        if(pkt.data.hasKey("CurrentOut")){
        	this.outputCurrent = pkt.data.getFloat("CurrentOut");
        }
        if(pkt.data.hasKey("CurrentIn")){
        	this.inputCurrent = pkt.data.getFloat("CurrentIn");
        }
	}
	
   
    
	/**
	 * 
	 * @param limit the amount of power available to send
	 * @return Returns the amount of power used
	 */
	protected float sendPowerToAll(float limit){
		if(limit <= 0) return 0f;
		float used = 0;
		float[] w = new float[4]; // weights for dividing power
		w[0] = getPowerRequest(ForgeDirection.NORTH);
		w[1] = getPowerRequest(ForgeDirection.WEST);
		w[2] = getPowerRequest(ForgeDirection.SOUTH);
		w[3] = getPowerRequest(ForgeDirection.EAST);
		float sum = w[0]+w[1]+w[2]+w[3];
		if (sum > limit) {
			// divide the energy in proportion to demand
			for (int i = 0; i < w.length; i++) {
				w[i] = w[i] / sum * limit;
			}
		}
		used += sendPower(ForgeDirection.NORTH, w[0]);
		used += sendPower(ForgeDirection.WEST, w[1]);
		used += sendPower(ForgeDirection.SOUTH, w[2]);
		used += sendPower(ForgeDirection.EAST, w[3]);
		return used;
		
	}
	/**
	 * 
	 * @param dir Direction to send energy
	 * @param limit energy available to send
	 * @return energy sent
	 */
	private float sendPower(ForgeDirection dir, float limit){
		PowerReceiver receptor = getPoweredTile(dir);
		if(receptor != null){
			return receptor.receiveEnergy(PowerHandler.Type.STORAGE, limit, dir.getOpposite());
		}
		return 0f;
	}
	
	
	private float getPowerRequest(ForgeDirection dir){
		PowerReceiver receptor = getPoweredTile(dir);
		if(receptor != null){
			return receptor.powerRequest();
		}
		return 0f;
	}
	
	public static int getChargeAsMetadata(World w, int x, int y, int z){
		TileEntity t = w.getBlockTileEntity(x, y, z);
		if(t instanceof StorageCellTileEntity){
			StorageCellTileEntity tile = ((StorageCellTileEntity)t);
			return (int)(15*tile.getCharge()/tile.getMaxCharge());
		}
		return 0;
	}
	
	public static float getChargeFromMetadata(int meta){
		return ((float)meta)*BasicMachines.storageCellCapacity/15f;
	}
	
	public float getCharge(){
		return charge;
	}
	
	public float getInputCurrent(){
		return inputCurrent;
	}

	public float getOutputCurrent(){
		return outputCurrent;
	}
	
	///// from Buildcraft Engine code ///
	private PowerReceiver getPoweredTile( ForgeDirection side) {
		TileEntity tile = getTileBuffer(side).getTile();
		if (tile instanceof IPowerReceptor){
			return((IPowerReceptor) tile).getPowerReceiver(side.getOpposite());
		}
		return null;
	}
	
    private TileBuffer[] tileCache = null;

	private TileBuffer getTileBuffer(ForgeDirection side) {
		if (tileCache == null)
			tileCache = TileBuffer.makeBuffer(worldObj, xCoord, yCoord, zCoord,
					false);
		return tileCache[side.ordinal()];
	}


	public void setCharge(float newCharge) {
		charge = newCharge;
		
	}


	
	
	///////////
	/**
     * Just checks if the player is close enough to interact with this block
     */
    public boolean isUseableByPlayer(EntityPlayer par1EntityPlayer)
    {
    	return getWorld().getBlockTileEntity(this.xCoord, this.yCoord, this.zCoord) != this ? false : par1EntityPlayer.getDistanceSq((double)this.xCoord + 0.5D, (double)this.yCoord + 0.5D, (double)this.zCoord + 0.5D) <= 64.0D;
    }


	public float getMaxCharge() {
		return BasicMachines.storageCellCapacity;
	}

	public float getInputCurrentScaled(float i) {
		return (i*(inputCurrent / maxCurrent));
		
	}
	public float getOutputCurrentScaled(float i) {
		return (i*(outputCurrent / maxCurrent));
		
	}


	public int getComparatorOutput() {
		return ((int)(15 * getCharge() / getMaxCharge())) & 0x0F;
	}
}
