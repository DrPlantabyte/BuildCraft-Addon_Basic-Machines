package cyano.basicmachines.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class LightBoxOnBlock extends LightBoxOffBlock {

	public LightBoxOnBlock(int blockID){
		super(blockID);
		this.setLightValue(1f);
	}
	@SideOnly(Side.CLIENT)
    @Override public void registerIcons(IconRegister par1IconRegister)
    {
        this.blockIcon = par1IconRegister.registerIcon("basicmachines:lightbox_on");
    }
}
