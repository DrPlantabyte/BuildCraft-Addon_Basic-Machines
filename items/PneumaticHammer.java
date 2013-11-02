package cyano.basicmachines.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cyano.basicmachines.BasicMachines;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumToolMaterial;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import cyano.basicmachines.api.IRechargeable;

/** This is a pickaxe powered by buildcraft energy */
public class PneumaticHammer extends ItemPickaxe implements IRechargeable{
	
	public PneumaticHammer(int itemID){
		super(itemID, EnumToolMaterial.EMERALD);
		setMaxStackSize(1);
		setMaxDamage(BasicMachines.pneumaticEnergyCapacity);
		setCreativeTab(CreativeTabs.tabTools);
	}
	
	@Override public boolean isRepairable()
    {
        return false;
    }
	@Override public boolean getIsRepairable(ItemStack par1ItemStack, ItemStack par2ItemStack)
    {
        return false;
    }
	@Override public int getItemEnchantability()
    {
        return 0;
    }
	/**
	 * Damages item after breaking a block
	 */
	@Override public boolean onBlockDestroyed(ItemStack par1ItemStack, World par2World, int par3, int par4, int par5, int par6, EntityLivingBase par7EntityLivingBase){
		if(getCurrentCharge(par1ItemStack) > 0){
			return super.onBlockDestroyed( par1ItemStack,  par2World,  par3,  par4,  par5,  par6,  par7EntityLivingBase);
		} else {
			return true;// I don't know why this method returns a boolean
		}
	}
	/**
     * Returns the strength of the stack against a given block. 1.0F base, (Quality+1)*2 if correct blocktype, 1.5F if
     * sword
     */
    @Override public float getStrVsBlock(ItemStack par1ItemStack, Block par2Block)
    {
    	if(getCurrentCharge(par1ItemStack) > 0){
    		return super.getStrVsBlock(par1ItemStack, par2Block);
    	} else {
    		return 1.0F;
    	}
    }
    
    @Override public float getStrVsBlock(ItemStack itemstack, Block block, int metadata){
    	return getStrVsBlock(itemstack, block);
    }
    
    @Override public boolean canHarvestBlock(Block par1Block, ItemStack itemStack)
    {
    	if(itemStack.getItem() instanceof IRechargeable){
    		if(((IRechargeable)itemStack.getItem()).getCurrentCharge(itemStack) > 0){
    			return super.canHarvestBlock(par1Block, itemStack);
    		} else {
    	        return false;
    		}
    	}
    	return super.canHarvestBlock(par1Block, itemStack);
    }
    
   
	
	@SideOnly(Side.CLIENT) // best way to register icons
	@Override public void registerIcons(IconRegister r){
		this.itemIcon = r.registerIcon("basicmachines:pneumatic_hammer");
	}
	
	/** set newly-crafted itmo to 0 charge */
	@Override public void onCreated(ItemStack itemstack, World world, EntityPlayer player){
		itemstack.setItemDamage(itemstack.getMaxDamage() - 1);
	}

	@Override
	public int getCurrentCharge(ItemStack itemStack) {
		return this.getMaxDamage() - this.getDamage(itemStack); // don't want to destroy item be going all the way to 0
	}

	@Override
	public int getMaxCharge() {
		return this.getMaxDamage();
	}

	@Override
	public int charge(ItemStack target, int energyAvailable) {
		if(energyAvailable > getDamage(target)){
			int remainder = energyAvailable - getDamage(target);
			this.setDamage(target, 0);
			return remainder;
		} else {
			this.setDamage(target, getDamage(target) - energyAvailable);
			return 0;
		}
	}
}
