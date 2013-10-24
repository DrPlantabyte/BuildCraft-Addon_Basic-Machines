package cyano.basicmachines.blocks;

import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

public class BasicMachineFrame extends net.minecraft.block.Block{

	public BasicMachineFrame(int id) {
		super(id, Material.iron);
        this.setCreativeTab(CreativeTabs.tabDecorations);
        setHardness(5.0F);
        setResistance(10.0F);
        setStepSound(soundMetalFootstep);
		setTextureName("basicmachines:machineframe");
	}

	
	
	 /**
     * Returns the quantity of items to drop on block destruction.
     */
	@Override public int quantityDropped(Random par1Random)
    {
        return 1;
    }
	
	@Override public int quantityDropped(int meta, int fortune, Random random)
    {
        return quantityDropped( random);
    }
	@Override public int quantityDroppedWithBonus(int par1, Random random)
    {
		return quantityDropped( random);
    }
	
	@SideOnly(Side.CLIENT)

    /**
     * Returns which pass should this block be rendered on. 0 for solids and 1 for alpha
     */
	@Override public int getRenderBlockPass()
    {
        return 0;
    }

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
}
