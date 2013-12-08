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
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;

public class GrowthChamberBlock extends BlockContainer {
	
	/**
     * Is the random generator used by furnace to drop the inventory contents in random directions.
     */
    private final Random localRand = new Random();
    
    Icon front = null;
	
    
	
	
	public GrowthChamberBlock(int blockID){
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
        return BasicMachines.blockID_growthChamber;
    }
	 /**
     * Determines the damage (metadata) on the item the block drops. Used in cloth and wood.
     */
    @Override public int damageDropped(int par1)
    {
    	return 0; //using 0 as the display metadata (it is always 1-4 when placed)
    }
	
	/**
     * Called whenever the block is added into the world. Args: world, x, y, z
     */
    public void onBlockAdded(World par1World, int par2, int par3, int par4)
    {
        super.onBlockAdded(par1World, par2, par3, par4);
        this.setDefaultDirection(par1World, par2, par3, par4);
    }

    /**
     * set a blocks direction
     */
    private void setDefaultDirection(World par1World, int par2, int par3, int par4)
    {
        if (!par1World.isRemote)
        {
            int l = par1World.getBlockId(par2, par3, par4 - 1);
            int i1 = par1World.getBlockId(par2, par3, par4 + 1);
            int j1 = par1World.getBlockId(par2 - 1, par3, par4);
            int k1 = par1World.getBlockId(par2 + 1, par3, par4);
            byte b0 = 3;

            if (Block.opaqueCubeLookup[l] && !Block.opaqueCubeLookup[i1])
            {
                b0 = 3;
            }

            if (Block.opaqueCubeLookup[i1] && !Block.opaqueCubeLookup[l])
            {
                b0 = 2;
            }

            if (Block.opaqueCubeLookup[j1] && !Block.opaqueCubeLookup[k1])
            {
                b0 = 5;
            }

            if (Block.opaqueCubeLookup[k1] && !Block.opaqueCubeLookup[j1])
            {
                b0 = 4;
            }

            par1World.setBlockMetadataWithNotify(par2, par3, par4, b0, 2);
        }
    }

    @SideOnly(Side.CLIENT)

    /**
     * From the specified side and block metadata retrieves the blocks texture. Args: side, metadata
     */
    public Icon getIcon(int side, int metadata)
    {
    	// for metadata 0 (NEI display){
    	if(metadata == 0 && side == 4){
    		return front;
    	}
    	// normal
    	if(side == metadata){
    		return front;
    	} else {
    		return this.blockIcon;
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
		
			front = par1IconRegister
					.registerIcon("basicmachines:growthchamber");
		
    }

	@Override
	public TileEntity createNewTileEntity(World world) {
		return new GrowthChamberTileEntity();
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
        	GrowthChamberTileEntity tileentity = (GrowthChamberTileEntity)world.getBlockTileEntity(x, y, z);

            if (tileentity != null)
            {
                player.openGui(BasicMachines.instance, 4, world, x, y, z);
            }
            return true;
        }
    }
   
    
    /**
     * Called when the block is placed in the world.
     */
    @Override public void onBlockPlacedBy(World par1World, int par2, int par3, int par4, EntityLivingBase par5EntityLivingBase, ItemStack par6ItemStack)
    {
        int l = MathHelper.floor_double((double)(par5EntityLivingBase.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;

        if (l == 0)
        {
            par1World.setBlockMetadataWithNotify(par2, par3, par4, 2, 2);
        }

        if (l == 1)
        {
            par1World.setBlockMetadataWithNotify(par2, par3, par4, 5, 2);
        }

        if (l == 2)
        {
            par1World.setBlockMetadataWithNotify(par2, par3, par4, 3, 2);
        }

        if (l == 3)
        {
            par1World.setBlockMetadataWithNotify(par2, par3, par4, 4, 2);
        }

        if (par6ItemStack.hasDisplayName())
        {
            ((GrowthChamberTileEntity)par1World.getBlockTileEntity(par2, par3, par4)).setGuiDisplayName(par6ItemStack.getDisplayName());
        }
    }
    
    /**
     * Called on server worlds only when the block has been replaced by a different block ID, or the same block with a
     * different metadata value, but before the new metadata value is set. Args: World, x, y, z, old block ID, old
     * metadata
     */
    @Override public void breakBlock(World par1World, int par2, int par3, int par4, int par5, int par6)
    {
        
    	GrowthChamberTileEntity tile = (GrowthChamberTileEntity)par1World.getBlockTileEntity(par2, par3, par4);

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
    
    @SideOnly(Side.CLIENT)

    /**
     * only called by clickMiddleMouseButton , and passed to inventory.setCurrentItem (along with isCreative)
     */
    @Override public int idPicked(World par1World, int par2, int par3, int par4)
    {
        return BasicMachines.blockID_growthChamber;
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
    	if(e instanceof GrowthChamberTileEntity){
    		GrowthChamberTileEntity tile = (GrowthChamberTileEntity)e;
    			return tile.getComparatorOutput();
    	}
    	return 0;
    }
}
