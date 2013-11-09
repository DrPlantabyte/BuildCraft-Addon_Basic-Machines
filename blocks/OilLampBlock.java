package cyano.basicmachines.blocks;

import static net.minecraftforge.common.ForgeDirection.DOWN;
import static net.minecraftforge.common.ForgeDirection.EAST;
import static net.minecraftforge.common.ForgeDirection.NORTH;
import static net.minecraftforge.common.ForgeDirection.SOUTH;
import static net.minecraftforge.common.ForgeDirection.UP;
import static net.minecraftforge.common.ForgeDirection.WEST;

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

public class OilLampBlock extends BlockContainer {
	
	
    
	
	
	public OilLampBlock(int blockID){
		super(blockID, Material.redstoneLight);
		this.setLightValue(0f);
		this.setCreativeTab(CreativeTabs.tabDecorations);
        setHardness(0.3F);
        setStepSound(soundGlassFootstep);
        float bottom = 3/16F;
        float top = 12/16F;
        setBlockBounds(0.25f,bottom,0.25f,0.75f,top,0.75f);
	}
	
	/**
     * Returns the ID of the items to drop on destruction.
     */
	@Override  public int idDropped(int par1, Random par2Random, int par3)
    {
        return BasicMachines.blockID_oillamp_off;
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
        this.blockIcon = par1IconRegister.registerIcon("basicmachines:blank");
    }
    
    @SideOnly(Side.CLIENT)

    
    /**
     * Is this block (a) opaque and (b) a full 1m cube?  This determines whether or not to render the shared face of two
     * adjacent blocks and also whether the player can attach torches, redstone wire, etc to this block.
     */
	@Override public boolean isOpaqueCube()
    {
        return false;
    }

    /**
     * If this block doesn't render as an ordinary block it will return False (examples: signs, buttons, stairs, etc)
     */
	@Override public boolean renderAsNormalBlock()
    {
        return false;
    }
	@Override
	public TileEntity createNewTileEntity(World world) {
		return new OilLampTileEntity();
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
    private static boolean keepInventory = false;
    /**
     * Update which block ID the furnace is using depending on whether or not it is burning
     */
    public static void updateBlockType(boolean on, World world, int x, int y, int z)
    {
    	
    	keepInventory = true; // this only works because the game update loop is a single thread
        int l = world.getBlockMetadata(x, y, z);
        TileEntity tileentity = world.getBlockTileEntity(x, y, z);

        if (on)
        {
            world.setBlock(x, y, z, BasicMachines.blockID_oillamp_on);
        }
        else
        {
            world.setBlock(x, y, z, BasicMachines.blockID_oillamp_off);
        }

        world.setBlockMetadataWithNotify(x, y, z, l, 2);

        if (tileentity != null)
        {
            tileentity.validate();
            world.setBlockTileEntity(x, y, z, tileentity);
        }
        keepInventory = false;
    }
    
    /**
     * checks to see if you can place this block can be placed on that side of a block: BlockLever overrides
     */
    public boolean canPlaceBlockOnSide(World world, int x, int y, int z, int side)
    {
        ForgeDirection dir = ForgeDirection.getOrientation(side);
        return (dir == DOWN  && world.isBlockSolidOnSide(x, y + 1, z, DOWN )) ||
        	   (dir == DOWN    && Block.blocksList[world.getBlockId(x, y + 1, z)].canPlaceTorchOnTop(world, x, y+1, z)) ||
               (dir == UP    && world.isBlockSolidOnSide(x, y - 1, z, UP   )) ||
               (dir == UP    && Block.blocksList[world.getBlockId(x, y - 1, z)].canPlaceTorchOnTop(world, x, y-1, z)) ||
               (dir == NORTH && world.isBlockSolidOnSide(x, y, z + 1, NORTH)) ||
               (dir == SOUTH && world.isBlockSolidOnSide(x, y, z - 1, SOUTH)) ||
               (dir == WEST  && world.isBlockSolidOnSide(x + 1, y, z, WEST )) ||
               (dir == EAST  && world.isBlockSolidOnSide(x - 1, y, z, EAST ));
    }

    /**
     * Checks to see if its valid to put this block at the specified coordinates. Args: world, x, y, z
     */
    public boolean canPlaceBlockAt(World world, int x, int y, int z)
    {
        return world.isBlockSolidOnSide(x - 1, y, z, EAST ) ||
               world.isBlockSolidOnSide(x + 1, y, z, WEST ) ||
               world.isBlockSolidOnSide(x, y, z - 1, SOUTH) ||
               world.isBlockSolidOnSide(x, y, z + 1, NORTH) ||
               world.isBlockSolidOnSide(x, y - 1, z, UP   ) ||
               world.isBlockSolidOnSide(x, y + 1, z, DOWN );
    }

    /**
     * Called when a block is placed using its ItemBlock. Args: World, X, Y, Z, side, hitX, hitY, hitZ, block metadata
     */
    public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata)
    {
    	int m = metadata;

        if (side == 1 && world.isBlockSolidOnSide(x, y - 1, z, UP   ))
        {
            m = 5;
        }

        if (side == 2 && world.isBlockSolidOnSide(x, y, z + 1, NORTH, true))
        {
            m = 4;
        }

        if (side == 3 && world.isBlockSolidOnSide(x, y, z - 1, SOUTH, true))
        {
            m = 3;
        }

        if (side == 4 && world.isBlockSolidOnSide(x + 1, y, z, WEST, true))
        {
            m = 2;
        }

        if (side == 5 && world.isBlockSolidOnSide(x - 1, y, z, EAST, true))
        {
            m = 1;
        }

        return m;
    }

    /**
     * Called when the block is placed in the world.
     */
    public void onBlockPlacedBy(World par1World, int par2, int par3, int par4, EntityLivingBase par5EntityLivingBase, ItemStack par6ItemStack)
    {

		if (par6ItemStack.hasDisplayName()) {
			((OilLampTileEntity) par1World.getBlockTileEntity(par2, par3, par4))
					.setGuiDisplayName(par6ItemStack.getDisplayName());
		}
    }

    

    /**
     * Lets the block know when one of its neighbor changes. Doesn't know which neighbor changed (coordinates passed are
     * their own) Args: x, y, z, neighbor blockID
     */
    public void onNeighborBlockChange(World par1World, int par2, int par3, int par4, int par5)
    {
        if (this.checkIfAttachedToBlock(par1World, par2, par3, par4))
        {
            int i1 = par1World.getBlockMetadata(par2, par3, par4) & 7;
            boolean flag = false;

            if (!par1World.isBlockSolidOnSide(par2 - 1, par3, par4, EAST) && i1 == 1)
            {
                flag = true;
            }

            if (!par1World.isBlockSolidOnSide(par2 + 1, par3, par4, WEST) && i1 == 2)
            {
                flag = true;
            }

            if (!par1World.isBlockSolidOnSide(par2, par3, par4 - 1, SOUTH) && i1 == 3)
            {
                flag = true;
            }

            if (!par1World.isBlockSolidOnSide(par2, par3, par4 + 1, NORTH) && i1 == 4)
            {
                flag = true;
            }

            if (!par1World.isBlockSolidOnSide(par2, par3 - 1, par4, UP) && i1 == 5)
            {
                flag = true;
            }
            
            if (!par1World.isBlockSolidOnSide(par2, par3 + 1, par4, DOWN) && i1 == 0)
            {
                flag = true;
            }

           
            if (flag)
            {
                this.dropBlockAsItem(par1World, par2, par3, par4, 0/*par1World.getBlockMetadata(par2, par3, par4)*/, 0);
                par1World.setBlockToAir(par2, par3, par4);
            }
        }
    }

    /**
     * Checks if the block is attached to another block. If it is not, it returns false and drops the block as an item.
     * If it is it returns true.
     */
    private boolean checkIfAttachedToBlock(World par1World, int par2, int par3, int par4)
    {
        if (!this.canPlaceBlockAt(par1World, par2, par3, par4))
        {
            this.dropBlockAsItem(par1World, par2, par3, par4, par1World.getBlockMetadata(par2, par3, par4), 0);
            par1World.setBlockToAir(par2, par3, par4);
            return false;
        }
        else
        {
            return true;
        }
    }

    
    private final Random localRand = new Random();
    
    /**
     * Called on server worlds only when the block has been replaced by a different block ID, or the same block with a
     * different metadata value, but before the new metadata value is set. Args: World, x, y, z, old block ID, old
     * metadata
     */
    @Override public void breakBlock(World par1World, int par2, int par3, int par4, int par5, int par6)
    {
    	if (!keepInventory){
    	OilLampTileEntity tileentity = (OilLampTileEntity)par1World.getBlockTileEntity(par2, par3, par4);

            if (tileentity != null)
            {
            	for (int j1 = 0; j1 < tileentity.getSizeInventory(); ++j1)
                {
                    ItemStack itemstack = tileentity.getStackInSlot(j1);

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
        
    	}
        super.breakBlock(par1World, par2, par3, par4, par5, par6);
    	
    }
    
    @SideOnly(Side.CLIENT)

    /**
     * only called by clickMiddleMouseButton , and passed to inventory.setCurrentItem (along with isCreative)
     */
    @Override public int idPicked(World par1World, int par2, int par3, int par4)
    {
        return BasicMachines.blockID_oillamp_off;
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
