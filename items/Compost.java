package cyano.basicmachines.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import cyano.basicmachines.BasicMachines;

public class Compost extends net.minecraft.item.Item {

	public Compost(int itemID){
		super(itemID);
		setMaxStackSize(64);
		setCreativeTab(CreativeTabs.tabMisc);
	}
	

	@SideOnly(Side.CLIENT) // best way to register icons
	@Override public void registerIcons(IconRegister r){
		this.itemIcon = r.registerIcon("basicmachines:compost");
	}
	
	
	@Override public boolean onItemUse(ItemStack srcItemStack, EntityPlayer playerEntity, World world, int targetX, int targetY, int targetZ, int par7, float par8, float par9, float par10){
		
		return ItemDye.applyBonemeal(srcItemStack,world,targetX, targetY, targetZ, playerEntity);
		
	}
	
}
