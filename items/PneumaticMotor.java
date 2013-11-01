package cyano.basicmachines.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;

public class PneumaticMotor extends net.minecraft.item.Item {
	
	public PneumaticMotor(int itemID){
		super(itemID);
		setCreativeTab(CreativeTabs.tabMaterials);
		
	}
	
	@SideOnly(Side.CLIENT) // best way to register icons
	@Override public void registerIcons(IconRegister r){
		this.itemIcon = r.registerIcon("basicmachines:pneumatic_motor");
	}

}
