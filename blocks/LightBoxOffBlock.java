package cyano.basicmachines.blocks;

import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cyano.basicmachines.BasicMachines;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;

public class LightBoxOffBlock extends BlockContainer {
	
	
    
	
	
	public LightBoxOffBlock(int blockID){
		super(blockID, Material.iron);
		this.setLightValue(0f);
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
        return BasicMachines.blockID_lightbox_off;
    }
	 /**
     * Determines the damage (metadata) on the item the block drops. Used in cloth and wood.
     */
    @Override public int damageDropped(int par1)
    {
    	return 0; 
    }
	
	

   

    @SideOnly(Side.CLIENT)

    /**
     * From the specified side and block metadata retrieves the blocks texture. Args: side, metadata
     */
    public Icon getIcon(int par1, int par2)
    {
    	return this.blockIcon;
    }

    
    /**
     * When this method is called, your block should register all the icons it needs with the given IconRegister. This
     * is the only chance you get to register icons.
     */
    @SideOnly(Side.CLIENT)
    @Override public void registerIcons(IconRegister par1IconRegister)
    {
        this.blockIcon = par1IconRegister.registerIcon("basicmachines:lightbox_off");
    }

	@Override
	public TileEntity createNewTileEntity(World world) {
		return new LightBoxTileEntity();
	}
	
	/**
     * Called upon block activation (right click on the block.)
     */
    @Override public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9)
    {
        if (world.isRemote) {
            return true;
        } else {
        	// no GUI
            return true;
        }
    }
    /**
     * Update which block ID the furnace is using depending on whether or not it is burning
     */
    public static void updateBlockType(boolean on, World world, int x, int y, int z)
    {
        int l = world.getBlockMetadata(x, y, z);
        TileEntity tileentity = world.getBlockTileEntity(x, y, z);

        if (on)
        {
            world.setBlock(x, y, z, BasicMachines.blockID_lightbox_on);
        }
        else
        {
            world.setBlock(x, y, z, BasicMachines.blockID_lightbox_off);
        }

        world.setBlockMetadataWithNotify(x, y, z, l, 2);

        if (tileentity != null)
        {
            tileentity.validate();
            world.setBlockTileEntity(x, y, z, tileentity);
        }
    }
    
    /**
     * Called when the block is placed in the world.
     */
    @Override public void onBlockPlacedBy(World par1World, int par2, int par3, int par4, EntityLivingBase par5EntityLivingBase, ItemStack par6ItemStack)
    {
      
            par1World.setBlockMetadataWithNotify(par2, par3, par4, 0, 2);
       

        if (par6ItemStack.hasDisplayName())
        {
            ((LightBoxTileEntity)par1World.getBlockTileEntity(par2, par3, par4)).setGuiDisplayName(par6ItemStack.getDisplayName());
        }
    }
    
    /**
     * Called on server worlds only when the block has been replaced by a different block ID, or the same block with a
     * different metadata value, but before the new metadata value is set. Args: World, x, y, z, old block ID, old
     * metadata
     */
    @Override public void breakBlock(World par1World, int par2, int par3, int par4, int par5, int par6)
    {
        
    	LightBoxTileEntity tileentity = (LightBoxTileEntity)par1World.getBlockTileEntity(par2, par3, par4);

            if (tileentity != null)
            {
               
                par1World.func_96440_m(par2, par3, par4, par5);
           }
        

        super.breakBlock(par1World, par2, par3, par4, par5, par6);
    }
    
    @SideOnly(Side.CLIENT)

    /**
     * only called by clickMiddleMouseButton , and passed to inventory.setCurrentItem (along with isCreative)
     */
    @Override public int idPicked(World par1World, int par2, int par3, int par4)
    {
        return BasicMachines.blockID_lightbox_off;
    }
	
    
    /**
     * If this returns true, then comparators facing away from this block will use the value from
     * getComparatorInputOverride instead of the actual redstone signal strength.
     */
    @Override public boolean hasComparatorInputOverride()
    {
        return false;
    }

   
}
