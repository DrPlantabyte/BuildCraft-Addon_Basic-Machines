package cyano.basicmachines.items;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSand;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cyano.basicmachines.BasicMachines;

public class OilCanEmpty extends Item {

	public OilCanEmpty(int itemID){
		super(itemID);
		setMaxStackSize(1);
		setCreativeTab(CreativeTabs.tabTools);
	}
	
	/**
     * Callback for item usage. If the item does something special on right clicking, he will have one of those. Return
     * True if something happen and false if it don't. This is for ITEMS, not BLOCKS
     */
    @Override public boolean onItemUse(ItemStack src, EntityPlayer player, World world, int blockX, int blockY, int blockZ, 
    		int blockSide, float hitVectorX, float hitVectorY, float hitVectorZ)
    {
    	TileEntity te = world.getBlockTileEntity(blockX, blockY, blockZ);
		ForgeDirection dir = ForgeDirection.getOrientation(blockSide);
    	if(te instanceof net.minecraftforge.fluids.IFluidHandler){
    	//	FMLLog.fine(this.getClass().getCanonicalName()+": "+"IFluidHandler found");
    		IFluidHandler fh = (IFluidHandler)te;
    		FluidTankInfo[] tanks = fh.getTankInfo(dir);
    		tankScan:{
	    		for(int i = 0; i < tanks.length; i++){
	    //			FMLLog.fine(this.getClass().getCanonicalName()+": "+"tank["+i+"] contains "+(tanks[i].fluid == null ? null : (tanks[i].fluid.getFluid() == null ? null : tanks[i].fluid.getFluid().getName())));
	    			if(tanks[i].fluid == null) continue; // empty tank
	    			Fluid f = tanks[i].fluid.getFluid();
	    //			FMLLog.fine(this.getClass().getCanonicalName()+": "+"tank["+i+"] canDrain=="+fh.canDrain(dir,f));
	    			if(canMakeCanFor(f)){
	    				// fh.canDrain(...) always returns false (unimplemented in BuildCraft)
	    			//	if(fh.canDrain(dir,f)){
	    					OilCan oilCan = BasicMachines.oilCanItems.get(f);
	    					ItemStack oilCanItem = new ItemStack(oilCan);
	    //					FMLLog.fine(this.getClass().getCanonicalName()+": "+"attempting to drain "+oilCan.getMaxVolume()+" mB from fluid handler");
	    					FluidStack volume = fh.drain(ForgeDirection.getOrientation(blockSide), oilCan.getMaxVolume(), true);
	    					if(volume == null){
	    						// tank was empty?
	    						return true;
	    					}
	    //					FMLLog.fine(this.getClass().getCanonicalName()+": "+"sucked up " + volume.amount + " mB " + volume.getFluid().getName());
	    					if(volume.amount > 0){
	    						oilCan.setVolume(oilCanItem, volume.amount);
	    						turnIntoItem(player,oilCanItem);
	    	    				break tankScan;
	    					}
	    			//	}
	    			}
	    		}
    		}
    		return true;
    	}
    	// not pulling from machine, try acting like a bucket
    	blockX += dir.offsetX;
    	blockY += dir.offsetY;
    	blockZ += dir.offsetZ;
    	int blockID = world.getBlockId(blockX, blockY, blockZ);
    	Fluid f = FluidRegistry.lookupFluidForBlock(Block.blocksList[blockID]);
    	if(canMakeCanFor(f)){
//    		FMLLog.fine(this.getClass().getCanonicalName()+": "+"sucking up "+f.getName());
    		OilCan oilCan = BasicMachines.oilCanItems.get(f);
			ItemStack oilCanItem = new ItemStack(oilCan);
			oilCan.setToFull(oilCanItem);
			turnIntoItem(player,oilCanItem);
    		world.setBlockToAir(blockX, blockY, blockZ);
    		return true;
    	}
        return false;
    }
    @Override public boolean getIsRepairable(ItemStack a, ItemStack b){
    	return false;
    }
    @Override public boolean isRepairable(){
		return false;
	}
    
    
    protected void turnIntoItem(EntityPlayer player, ItemStack item){
    	player.inventory.setInventorySlotContents(player.inventory.currentItem, item);
    }
    
    protected boolean canMakeCanFor(Fluid fluid){
    	return BasicMachines.oilCanItems.containsKey(fluid);
    }
	
	@SideOnly(Side.CLIENT) // best way to register icons
	@Override public void registerIcons(IconRegister r){
		this.itemIcon = r.registerIcon("basicmachines:oilcan_empty");
	}
}
