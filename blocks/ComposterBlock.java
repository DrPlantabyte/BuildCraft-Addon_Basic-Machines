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

public class ComposterBlock extends BlockContainer {
	

	/**
     * Is the random generator used by furnace to drop the inventory contents in random directions.
     */
    private final Random localRand = new Random();
    
    
    private Icon top = null;
    private Icon side = null;
	
	public ComposterBlock(int blockID){
		super(blockID, Material.iron);
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
        return BasicMachines.blockID_composter;
    }
	 /**
     * Determines the damage (metadata) on the item the block drops. Used in cloth and wood.
     */
    @Override public int damageDropped(int par1)
    {
    	return 0; 
    }
	
	/**
     * Called whenever the block is added into the world. Args: world, x, y, z
     */
    public void onBlockAdded(World par1World, int par2, int par3, int par4)
    {
        super.onBlockAdded(par1World, par2, par3, par4);
    }


    @SideOnly(Side.CLIENT)

    /**
     * From the specified side and block metadata retrieves the blocks texture. Args: side, metadata
     */
    public Icon getIcon(int blockside, int meta)
    {
    	switch(blockside){
    	case 1:
    		return top;
    	case 0:
    		return blockIcon;
    	default:
    		return side;	
    	}
    }

    
    /**
     * When this method is called, your block should register all the icons it needs with the given IconRegister. This
     * is the only chance you get to register icons.
     */
    @SideOnly(Side.CLIENT)
    @Override public void registerIcons(IconRegister par1IconRegister)
    {
        this.blockIcon = par1IconRegister.registerIcon("basicmachines:machinewall");
        top = par1IconRegister
				.registerIcon("basicmachines:composter_top");
        side = par1IconRegister
				.registerIcon("basicmachines:composter_side");
	
    }

	@Override
	public TileEntity createNewTileEntity(World world) {
		return new ComposterTileEntity();
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
        	ComposterTileEntity tile = (ComposterTileEntity)world.getBlockTileEntity(x, y, z);

            if (tile != null)
            {
                player.openGui(BasicMachines.instance, 5, world, x, y, z);
            }
            return true;
        }
    }
    
    
    @SideOnly(Side.CLIENT)

    /**
     * only called by clickMiddleMouseButton , and passed to inventory.setCurrentItem (along with isCreative)
     */
    @Override public int idPicked(World par1World, int par2, int par3, int par4)
    {
        return BasicMachines.blockID_composter;
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
    	// TODO
    	TileEntity e = w.getBlockTileEntity(x, y, z);
    	if(e instanceof ComposterTileEntity){
    		ComposterTileEntity tile = (ComposterTileEntity)e;
    			return 0;//((int)(15 * tile.getOutputStackSize() / 64)) & 0x0F;
    	}
    	return 0;
    }
    
    /**
     * Called on server worlds only when the block has been replaced by a different block ID, or the same block with a
     * different metadata value, but before the new metadata value is set. Args: World, x, y, z, old block ID, old
     * metadata
     */
    @Override public void breakBlock(World par1World, int par2, int par3, int par4, int par5, int par6)
    {
        
    	ComposterTileEntity tile = (ComposterTileEntity)par1World.getBlockTileEntity(par2, par3, par4);

            if (tile != null)
            {
                for (int j1 = 0; j1 < tile.getSizeInventory(); ++j1)
                {
                    ItemStack itemstack = tile.getStackInSlot(j1);

                    if (itemstack != null)
                    {
                        float f = this.localRand.nextFloat() * 0.8F + 0.1F;
                        float f1 = this.localRand.nextFloat() * 0.8F + 0.1F;
                        float f2 = this.localRand.nextFloat() * 0.8F + 0.1F;

                        while (itemstack.stackSize > 0)
                        {
                            int k1 = this.localRand.nextInt(21) + 10;

                            if (k1 > itemstack.stackSize)
                            {
                                k1 = itemstack.stackSize;
                            }

                            itemstack.stackSize -= k1;
                            EntityItem entityitem = new EntityItem(par1World, (double)((float)par2 + f), (double)((float)par3 + f1), (double)((float)par4 + f2), new ItemStack(itemstack.itemID, k1, itemstack.getItemDamage()));

                            if (itemstack.hasTagCompound())
                            {
                                entityitem.getEntityItem().setTagCompound((NBTTagCompound)itemstack.getTagCompound().copy());
                            }

                            float f3 = 0.05F;
                            entityitem.motionX = (double)((float)this.localRand.nextGaussian() * f3);
                            entityitem.motionY = (double)((float)this.localRand.nextGaussian() * f3 + 0.2F);
                            entityitem.motionZ = (double)((float)this.localRand.nextGaussian() * f3);
                            par1World.spawnEntityInWorld(entityitem);
                        }
                    }
                }

                par1World.func_96440_m(par2, par3, par4, par5);
            }
        

        super.breakBlock(par1World, par2, par3, par4, par5, par6);
    }
}
