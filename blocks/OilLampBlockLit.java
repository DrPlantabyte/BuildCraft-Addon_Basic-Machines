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

public class OilLampBlockLit extends OilLampBlock {
	
	
    
	
	
	public OilLampBlockLit(int blockID){
		super(blockID);
        setLightValue(1f);
	}
	
	
    
    @SideOnly(Side.CLIENT)

    /**
     * A randomly called display update to be able to add particles or other items for display
     */
    @Override public void randomDisplayTick(World par1World, int x, int y, int z, Random par5Random)
    {
      //  if (this.isActive) {
            float fx = (float)x + 0.5F;
            float fy = (float)y + 0.5F;
            float fz = (float)z + 0.5F;
            float noise = 0;// par5Random.nextFloat() * 0.6F - 0.3F;
            par1World.spawnParticle("flame", (double)(fx), (double)fy + noise, (double)(fz), 0.0D, 0.0D, 0.0D);
           
     //   }
    }
    
   
}
