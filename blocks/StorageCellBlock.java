package cyano.basicmachines.blocks;

import java.util.ArrayList;
import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import cyano.basicmachines.BasicMachines;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
/**
 * The storage cell is like a much smaller version of the Redstone Energy Cell from Thermal Expansion.
 */
public class StorageCellBlock extends BlockContainer {

	Icon inputFace = null;
	Icon outputFace = null;
	
	public StorageCellBlock(int itemID) {
		super(itemID, Material.iron);
		this.setCreativeTab(CreativeTabs.tabDecorations);
        setHardness(5.0F);
        setResistance(10.0F);
        setStepSound(soundMetalFootstep);
        
	}

	
    
	
	/**
     * Returns the ID of the items to drop on destruction.
     */
	@Override  public int idDropped(int par1, Random par2Random, int par3)
    {
        return BasicMachines.blockID_storageCell;
    }
	
	@Override
	public TileEntity createNewTileEntity(World world) {
		return new StorageCellTileEntity();
	}
	
	
	
	
	/**
     * When this method is called, your block should register all the icons it needs with the given IconRegister. This
     * is the only chance you get to register icons.
     */
    @SideOnly(Side.CLIENT)
    @Override public void registerIcons(IconRegister iconRegister)
    {
    	inputFace = iconRegister.registerIcon("basicmachines:storagecell_inlet");
    	outputFace = iconRegister.registerIcon("basicmachines:storagecell_outlet");
		this.blockIcon = outputFace;
    }

	@SideOnly(Side.CLIENT)

    /**
     * From the specified side and block metadata retrieves the blocks texture. Args: side, metadata
     */
    @Override public Icon getIcon(int par1, int par2)
    {
    	
    	if(par1 == 0 || par1 == 1){
    		return inputFace;
    	} else {
    		return outputFace;
    	}
    }
	
	/**
     * Called upon block activation (right click on the block.)
     */
    @Override public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9)
    {
        if (world.isRemote)
        {
            return true;
        }
        else
        {
        	StorageCellTileEntity tile = (StorageCellTileEntity)world.getBlockTileEntity(x, y, z);

            if (tile != null)
            {
                player.openGui(BasicMachines.instance, 1, world, x, y, z);
            }
            return true;
        }
    }
    
    /**
     * If this returns true, then comparators facing away from this block will use the value from
     * getComparatorInputOverride instead of the actual redstone signal strength.
     */
    @Override public boolean hasComparatorInputOverride()
    {
        return true;
    }

    /**
     * If hasComparatorInputOverride returns true, the return value from this is used instead of the redstone signal
     * strength when this block inputs to a comparator.
     */
    @Override public int getComparatorInputOverride(World w, int x, int y, int z, int meta)
    {
    	TileEntity e = w.getBlockTileEntity(x, y, z);
    	if(e instanceof StorageCellTileEntity){
    		StorageCellTileEntity tile = (StorageCellTileEntity)e;
    			return tile.getComparatorOutput();
    	}
    	return 0;
    }
}
