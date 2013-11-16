package cyano.basicmachines.items;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cyano.basicmachines.BasicMachines;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;

public class OilCan extends Item{

	final Fluid type;
	final String iconName;
	
	public final static int DISPENSE_VOLUME =  FluidContainerRegistry.BUCKET_VOLUME / 10;
	
	public OilCan(int itemID, Fluid fluidType, String iconName){
		super(itemID);
		this.iconName = iconName;
		type = fluidType;
		setMaxStackSize(1);
		setMaxDamage(FluidContainerRegistry.BUCKET_VOLUME);
		setCreativeTab(CreativeTabs.tabTools);
	}
	
	@Override public boolean isRepairable(){
		return false;
	}
	
	 /**
     * Callback for item usage. If the item does something special on right clicking, he will have one of those. Return
     * True if something happen and false if it don't. This is for ITEMS, not BLOCKS
     */
    @Override public boolean onItemUse(ItemStack src, EntityPlayer player, World world, int blockX, int blockY, int blockZ, 
    		int blockSide, float hitVectorX, float hitVectorY, float hitVectorZ)
    {
    	TileEntity te = world.getBlockTileEntity(blockX, blockY, blockZ);
    	if(te instanceof net.minecraftforge.fluids.IFluidHandler){
    //		FMLLog.fine(this.getClass().getCanonicalName()+": "+"found fluid handler");
    		IFluidHandler fh = (IFluidHandler)te;
    		
    			// put into targat
    //			FMLLog.fine(this.getClass().getCanonicalName()+": "+" canFill=="+fh.canFill(ForgeDirection.getOrientation(blockSide), type));
	    		if(fh.canFill(ForgeDirection.getOrientation(blockSide), type)){
	//    			FMLLog.fine(this.getClass().getCanonicalName()+": "+"fluid handler can be filled with "+type.getName());
	    			int input = DISPENSE_VOLUME;
	    			int cap = getVolume(src);
	    			if(DISPENSE_VOLUME > cap){input = cap;}
	    			FluidStack rep = new FluidStack(type,input);
	    			int volume = fh.fill(ForgeDirection.getOrientation(blockSide), rep, true);
	//    			FMLLog.fine(this.getClass().getCanonicalName()+": "+"filled fluid handler with "+volume+" mB "+type.getName());
	    			changeVolume(src,-volume);
	    		}
    		
    		turnIntoEmptyOilCanIfEmpty(src,player);
    		return true;
    	}
        return false;
    }
	
    
    protected void turnIntoEmptyOilCanIfEmpty(ItemStack src, EntityPlayer player){
    	if(getVolume(src) <= 0){
    		player.inventory.setInventorySlotContents(player.inventory.currentItem, new ItemStack(BasicMachines.item_OilCan_empty));
    	}
    }
    
    public int getVolume(ItemStack src){
    	return src.getMaxDamage() - src.getItemDamage();
    }
    
    public int getMaxVolume(){
    	return FluidContainerRegistry.BUCKET_VOLUME;
    }
    
    public void changeVolume(ItemStack src, int volume){
    	src.setItemDamage(src.getItemDamage() - volume);
    }
    public void setVolume(ItemStack src, int volume){
    	src.setItemDamage(src.getMaxDamage() - volume);
    }
    public void setToFull(ItemStack src){
    	src.setItemDamage(0);
    }

	@SideOnly(Side.CLIENT) // best way to register icons
	@Override public void registerIcons(IconRegister r){
		this.itemIcon = r.registerIcon(iconName);
	}
}
