package cyano.basicmachines.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cyano.basicmachines.BasicMachines;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumToolMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;

public class PneumaticGun extends ItemBow implements cyano.basicmachines.api.IRechargeable{
	/** minimum number of tick to fire a shot */
	public static int FIRE_INTERVAL = 12;
	
	public static float FIRE_POWER = 1.0f;
	
	public static double DAMAGE = 2.5;
	
	public static String noChargeAttackSound = "random.click";
	
	public PneumaticGun(int itemID){
		super(itemID);
		setMaxStackSize(1);
		setMaxDamage(BasicMachines.pneumaticEnergyCapacity);
		setCreativeTab(CreativeTabs.tabCombat);
	}
	
	/**
     * Called when you start shooting (start aiming)
     */
	@Override public ItemStack onItemRightClick(ItemStack srcItemStack, World world, EntityPlayer playerEntity)
    {
		playerEntity.setItemInUse(srcItemStack, getMaxItemUseDuration(srcItemStack));
        return srcItemStack;
    }
	 /**
	  * Called while holding the trigger.
	  */
	 @Override public ItemStack onEaten (ItemStack srcItemStack, World world, EntityPlayer playerEntity) { 
		 ArrowLooseEvent event = new ArrowLooseEvent(playerEntity, srcItemStack, 1);
	     MinecraftForge.EVENT_BUS.post(event);
	     if (event.isCanceled()) {
	            return srcItemStack;
	     }
	       		 
		if (playerEntity.capabilities.isCreativeMode == false) {
			if (getCurrentCharge(srcItemStack) <= 0 || (playerEntity.inventory.hasItem(Item.arrow.itemID) == false)) {
				// out of charge/arrows
				playSound(noChargeAttackSound, world, playerEntity);
				return srcItemStack;
			}
			// comsume ammo and power
			playerEntity.inventory.consumeInventoryItem(Item.arrow.itemID);
			srcItemStack.damageItem(1, playerEntity);
		}

		playSound("random.bow", world, playerEntity);

		if (!world.isRemote) {
			EntityArrow arrow = new EntityArrow(world, playerEntity, FIRE_POWER*2f);
			arrow.setDamage(DAMAGE);
			world.spawnEntityInWorld(arrow);
		}
		return srcItemStack;
	 }
	/**
     * called you release the trigger. Args: itemstack, world, entityplayer, itemInUseCount
     */
	@Override  public void onPlayerStoppedUsing(ItemStack srcStack, World world, EntityPlayer player, int holdDurationRemaining)
    {
        // do nothing
        return;
        
    }
	
	

    /**
     * How long it takes to use or consume an item
     */
	@Override public int getMaxItemUseDuration(ItemStack par1ItemStack)
    {
        return FIRE_INTERVAL;
    }

    /**
     * returns the action that specifies what animation to play when the items is being used
     */
    @Override public EnumAction getItemUseAction(ItemStack par1ItemStack)
    {
        return EnumAction.bow;
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
	
	@SideOnly(Side.CLIENT) // best way to register icons
	@Override public void registerIcons(IconRegister r){
		this.itemIcon = r.registerIcon("basicmachines:pneumatic_gun");
	}
	
	@Override
	public int getCurrentCharge(ItemStack itemStack) {
		return this.getMaxDamage() - this.getDamage(itemStack); 
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
	
	/** plays a sound at the player location */
    protected void playSound(String soundID, World world, EntityPlayer playerEntity){
    	 if (!world.isRemote)
         {
    		 world.playSoundAtEntity(playerEntity, soundID, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
         }
    }
}
