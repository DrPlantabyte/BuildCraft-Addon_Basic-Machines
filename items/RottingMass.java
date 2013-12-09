package cyano.basicmachines.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cyano.basicmachines.BasicMachines;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;

public class RottingMass extends net.minecraft.item.Item{
	
	public static final int ITEMS_PER_COMPOST = 16;
	public RottingMass(int itemID){
		super(itemID);
		setMaxStackSize(1);
		setMaxDamage(ITEMS_PER_COMPOST+1);
		setCreativeTab(CreativeTabs.tabMisc);
	}
	
	@SideOnly(Side.CLIENT) // best way to register icons
	@Override public void registerIcons(IconRegister r){
		this.itemIcon = r.registerIcon("basicmachines:rottingmass");
	}
}
