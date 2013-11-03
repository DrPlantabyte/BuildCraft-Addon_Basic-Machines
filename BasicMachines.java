package cyano.basicmachines;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.transformers.ForgeAccessTransformer;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cyano.basicmachines.client.ClientProxy;
import cyano.basicmachines.graphics.BasicMachinesGUIHandler;
import cyano.basicmachines.items.*;
import cyano.basicmachines.blocks.*;


/*
 * TODO list
 BuildCraft Basics Addon
Machines: (in order of priority)
+ Iron Furnace - Buildcraft powered furnace
+ Energy Cell - Stores buildcraft energy (top+bottom=input, sides=output)
+ Charger - Recharge items that use buildcraft energy
- Sorter - uses a filter to divert specific items
+ Light Box - Buildcraft powered torch
- Lamp - Oil-powered torch
- Growth Chamber
- Composter
Tools: All have max capacity of 1024 MJ, but use different amounts of energy per action
+ Pneumatic Hammer - Buildcraft powered pickaxe
+ Pneumatic Saw - Buildcraft powered axe
+ Pneumatic Gun - Buildcraft powered bow
Other Items:
+ Basic Machine Frame - crafting component
+ Pneumatic Motor - crafting component
 */

@Mod(modid="basicmachines", name="Cyano's Basic Machines for BuildCraft", version="0.4.1")
@NetworkMod(clientSideRequired=true, serverSideRequired=false)
public class BasicMachines {
	// The instance of your mod that Forge uses.
		@Instance("basicmachines")
		public static BasicMachines instance;
		
		// Says where the client and server 'proxy' code is loaded.
		@SidedProxy(clientSide="cyano.basicmachines.client.ClientProxy", serverSide="cyano.basicmachines.CommonProxy")
		public static CommonProxy proxy;
		
		static boolean alternateRecipe = false;

		public static int blockID_basicMachineFrame;
		public static int blockID_ironFurnace_on;
		public static int blockID_ironFurnace_off;
		public static int blockID_storageCell;
		public static int blockID_charger;
		public static int blockID_lightbox_on;
		public static int blockID_lightbox_off;

		public static int itemID_pneumaticMotor;
		public static int itemID_pneumaticHammer;
		public static int itemID_pneumaticSaw;
		public static int itemID_pneumaticGun;
		
		public static BasicMachineFrame block_BasicMachineFrame = null;
		public static IronFurnaceGlowingBlock block_IronFurnaceGlowing = null;
		public static IronFurnaceBlock block_IronFurnace = null;
		public static StorageCellBlock block_StorageCell = null;
		public static LightBoxOffBlock block_LightBoxOff = null;
		public static LightBoxOnBlock block_LightBoxOn = null;
		public static ChargerBlock block_Charger = null;
		public static PneumaticMotor item_PneumaticMotor = null;
		public static PneumaticHammer item_PneumaticHammer = null;
		public static PneumaticSaw item_PneumaticSaw = null;
		public static PneumaticGun item_PneumaticGun = null;
		
		public static int pneumaticEnergyCapacity = 1024;
		public static float MJperChargeUnit = 8f;
		
		public static float storageCellCapacity = 5000f;
		
		public static Set<Integer> additionalRechargableItemIDs = new java.util.HashSet<Integer>();
		 
		// graphics resources

		public static ResourceLocation ironFurnaceGUILayer = null;
		public static ResourceLocation storageCellGUILayer = null;
		public static ResourceLocation chargerGUILayer = null;
		// Mark this method for receiving an FMLEvent (in this case, it's the FMLPreInitializationEvent)
	    @EventHandler public void preInit(FMLPreInitializationEvent event)
	    {
	        // Do stuff in pre-init phase (read config, create blocks and items, register them)
	    	Configuration config = new Configuration(event.getSuggestedConfigurationFile());
	    	
	    	config.get("options", "UseAlternativeRecipe", alternateRecipe,"If the crafting recipe for " +
	    			"the basic machine frame clashes with another recipe, set this to true to use a " +
	    			"different recipe.");
	    	
			int blockID = 1100;
			blockID = config.get("Blocks","blockID_BasicMachineFrame", getNextBlockID(++blockID)).getInt();
			blockID_basicMachineFrame = blockID;
			block_BasicMachineFrame = new BasicMachineFrame(blockID_basicMachineFrame);
			
			blockID = config.get("Blocks","blockID_IronFurnaceA", getNextBlockID(++blockID)).getInt();
			blockID_ironFurnace_off = blockID;
			block_IronFurnace = new IronFurnaceBlock(blockID_ironFurnace_off);
			blockID = config.get("Blocks","blockID_IronFurnaceB", getNextBlockID(++blockID)).getInt();
			blockID_ironFurnace_on = blockID;
			block_IronFurnaceGlowing = new IronFurnaceGlowingBlock(blockID_ironFurnace_on);
			
			blockID = config.get("Blocks","blockID_storageCell", getNextBlockID(++blockID)).getInt();
			blockID_storageCell = blockID;
			block_StorageCell = new StorageCellBlock(blockID_storageCell);
			
			blockID = config.get("Blocks","blockID_charger", getNextBlockID(++blockID)).getInt();
			blockID_charger = blockID;
			block_Charger = new ChargerBlock(blockID_charger);

			blockID = config.get("Blocks","blockID_lightbox_off", getNextBlockID(++blockID)).getInt();
			blockID_lightbox_off = blockID;
			block_LightBoxOff = new LightBoxOffBlock(blockID_lightbox_off);
			blockID = config.get("Blocks","blockID_lightbox_on", getNextBlockID(++blockID)).getInt();
			blockID_lightbox_on = blockID;
			block_LightBoxOn = new LightBoxOnBlock(blockID_lightbox_on);
			
			int itemID = blockID+256;
			itemID = config.get("Items","itemID_pneumaticMotor", getNextItemID(++itemID)).getInt();
			itemID_pneumaticMotor = itemID;
			item_PneumaticMotor = new PneumaticMotor(itemID_pneumaticMotor);
			
			itemID = config.get("Items","itemID_pneumaticHammer", getNextItemID(++itemID)).getInt();
			itemID_pneumaticHammer = itemID;
			item_PneumaticHammer = new PneumaticHammer(itemID_pneumaticHammer);
			itemID = config.get("Items","itemID_pneumaticSaw", getNextItemID(++itemID)).getInt();
			itemID_pneumaticSaw = itemID;
			item_PneumaticSaw = new PneumaticSaw(itemID_pneumaticSaw);
			itemID = config.get("Items","itemID_pneumaticGun", getNextItemID(++itemID)).getInt();
			itemID_pneumaticGun = itemID;
			item_PneumaticGun = new PneumaticGun(itemID_pneumaticGun);
			
			
			MJperChargeUnit = (float)config.get("Options", "MJ_per_charge_unit", MJperChargeUnit,
					"Energy use efficiency of the Charger block expressed as MJ of energy per charge unit. " +
					"This value should be more than 1 (typically 10-ish) or rechargeable items will be " +
					"overpowered.").getDouble(MJperChargeUnit);
			storageCellCapacity = (float)config.get("Options", "storagecell_capacity", storageCellCapacity,
					"This is the amount of MJ energy that a Storage Cell can store. Increase this number to " +
					"make the game easier and decrease it to make it harder.").getDouble(MJperChargeUnit);
			String moreChargeables = config.get("Options", "additional_Rechargeable_Items", "",
					"Supply the itemIDs of items that should be 'recharged' by the Charger block as a " +
					"comma-separated list. These items will have their damage value repaired at the cost of " +
					"BuildCraft energy (with the energy per gamade repaired determined by the " +
					"MJ_per_charge_unit setting.").getString();
			if(moreChargeables.trim().length() > 0){
				String[] chargeables = moreChargeables.split(",");
				for(String c : chargeables){
					try{
						FMLLog.log(Level.INFO,"basicmachines: adding itemID " + c + " to list of rechargeable items");
						additionalRechargableItemIDs.add(Integer.parseInt(c));
					}catch(NumberFormatException ex){
						FMLLog.log(Level.WARNING, ex, "Could not parse the itemID '"+c+"' in the additionalRechargeableItems field of the basicmachines config file.");
					}
				}
			}
			
			config.save();
			 
			// set resource locations
			ironFurnaceGUILayer = new ResourceLocation("basicmachines:textures/gui/ironfurnace.png");
			storageCellGUILayer = new ResourceLocation("basicmachines:textures/gui/storagecell.png");
			chargerGUILayer = new ResourceLocation("basicmachines:textures/gui/charger.png");
	    }
	    
	    
	    @EventHandler
		public void init(FMLInitializationEvent event) {
			proxy.registerRenderers();

			if (event.getSide().isClient())	{ClientProxy.setCustomRenderers();}


			//Register the tile entities
			GameRegistry.registerTileEntity(IronFurnaceTileEntity.class, "ironFurnaceTileEntity");
			GameRegistry.registerTileEntity(StorageCellTileEntity.class, "storageCellTileEntity");
			GameRegistry.registerTileEntity(ChargerTileEntity.class, "chargerTileEntity");
			GameRegistry.registerTileEntity(LightBoxTileEntity.class, "lightBoxTileEntity");

			//Register the guis
			NetworkRegistry.instance().registerGuiHandler(this, new BasicMachinesGUIHandler());
			
	
			// language registry and crafting recipes 
			block_BasicMachineFrame.setUnlocalizedName("basicmachines.basicMachineFrame");
			LanguageRegistry.addName(block_BasicMachineFrame, "Basic Machine Frame");
			GameRegistry.registerBlock(block_BasicMachineFrame,"basicMachineFrame");
			if(alternateRecipe == false){
				// normal recipe
				final ItemStack output = new ItemStack(block_BasicMachineFrame);
				// now add an oak-plank version to make it show up in NEI
				GameRegistry.addRecipe(output, "pip", "i i", "pip", 'p', Block.planks, 'i',
						Item.ingotIron);
			} else {
				// alternative recipe, bricks instead of wood
				final ItemStack output = new ItemStack(block_BasicMachineFrame);
				GameRegistry.addRecipe(output, "bib", "i i", "bib", 'b', Item.brick, 'i',
						Item.ingotIron);
			}
			
			
			
			block_IronFurnace.setUnlocalizedName("basicmachines.ironFurnace");
			LanguageRegistry.addName(block_IronFurnace, "Iron Furnace");
			GameRegistry.registerBlock(block_IronFurnace,"basicmachines.ironFurnace");
			ItemStack craft = new ItemStack(block_IronFurnace);
			GameRegistry.addRecipe(craft, " i ", "ibi", " i ", 'b', block_BasicMachineFrame, 'i', Item.ingotIron);
			block_IronFurnaceGlowing.setUnlocalizedName("basicmachines.ironFurnaceActive");
			LanguageRegistry.addName(block_IronFurnaceGlowing, "Iron Furnace (Active)");
			GameRegistry.registerBlock(block_IronFurnaceGlowing,"basicmachines.ironFurnaceActive");
			

			block_StorageCell.setUnlocalizedName("basicmachines.storageCell");
			LanguageRegistry.addName(block_StorageCell, "Storage Cell");
			GameRegistry.registerBlock(block_StorageCell,"basicmachines.storageCell");
			craft = new ItemStack(block_StorageCell);
			GameRegistry.addRecipe(craft, " p ","pbp"," p ",'b',block_BasicMachineFrame,'p',buildcraft.BuildCraftTransport.pipePowerGold);
			
			block_Charger.setUnlocalizedName("basicmachines.charger");
			LanguageRegistry.addName(block_Charger, "Charger");
			GameRegistry.registerBlock(block_Charger,"basicmachines.charger");
			craft = new ItemStack(block_Charger);
			GameRegistry.addRecipe(craft, "   ","pbp"," r ",'b',block_BasicMachineFrame,'p',buildcraft.BuildCraftTransport.pipePowerGold, 'r', item_PneumaticMotor);
			
			block_LightBoxOff.setUnlocalizedName("basicmachines.lightboxOff");
			LanguageRegistry.addName(block_LightBoxOff, "Light Box");
			GameRegistry.registerBlock(block_LightBoxOff,"basicmachines.lightboxOff");
			block_LightBoxOn.setUnlocalizedName("basicmachines.lightboxOn");
			LanguageRegistry.addName(block_LightBoxOn, "Light Box (on)");
			GameRegistry.registerBlock(block_LightBoxOn,"basicmachines.lightboxOn");
			craft = new ItemStack(block_LightBoxOff);
			ItemStack charcoal = new ItemStack(net.minecraft.item.Item.coal);
			charcoal.setItemDamage(1);
			GameRegistry.addRecipe(craft, " p ","gbg"," c ",'b',block_BasicMachineFrame,'g',net.minecraft.block.Block.thinGlass,'p',buildcraft.BuildCraftTransport.pipePowerGold, 'c',charcoal );
			
			
			
			item_PneumaticMotor.setUnlocalizedName("basicmachines.pneumaticMotor");
			LanguageRegistry.addName(item_PneumaticMotor, "Pneumatic Motor");
			GameRegistry.registerItem(item_PneumaticMotor, "basicmachines.pneumaticMotor");
			craft = new ItemStack(item_PneumaticMotor);
			GameRegistry.addRecipe(craft, " i ","ipi","igi",'i',Item.ingotIron,'g',buildcraft.BuildCraftCore.ironGearItem, 'p', buildcraft.BuildCraftTransport.pipeFluidsIron);
			
			item_PneumaticHammer.setUnlocalizedName("basicmachines.pneumaticHammer");
			LanguageRegistry.addName(item_PneumaticHammer, "Pneumatic Hammer");
			GameRegistry.registerItem(item_PneumaticHammer, "basicmachines.pneumaticHammer");
			craft = new ItemStack(item_PneumaticHammer);
			GameRegistry.addRecipe(craft, " d ","did"," m ",'i',Item.ingotIron,'d',Item.diamond, 'm', item_PneumaticMotor);
			
			item_PneumaticSaw.setUnlocalizedName("basicmachines.pneumaticSaw");
			LanguageRegistry.addName(item_PneumaticSaw, "Pneumatic Saw");
			GameRegistry.registerItem(item_PneumaticSaw, "basicmachines.pneumaticSaw");
			craft = new ItemStack(item_PneumaticSaw);
			GameRegistry.addRecipe(craft, "d ","m ",'d',buildcraft.BuildCraftCore.diamondGearItem, 'm', item_PneumaticMotor);
			
			item_PneumaticGun.setUnlocalizedName("basicmachines.pneumaticGun");
			LanguageRegistry.addName(item_PneumaticGun, "Pneumatic Gun");
			GameRegistry.registerItem(item_PneumaticGun, "basicmachines.pneumaticGun");
			craft = new ItemStack(item_PneumaticGun);
			GameRegistry.addRecipe(craft, " i "," i ","imp",'i',Item.ingotIron,'p',buildcraft.BuildCraftTransport.pipeItemsGold, 'm', item_PneumaticMotor);
			
		}
	    
	    
	    private int getNextItemID(int startingID){
			int i = startingID;
			while(Item.itemsList[i] != null && Block.blocksList[i] != null){
				i++;
				if(i >= Item.itemsList.length){
					throw new IndexOutOfBoundsException("No open Item ID numbers available above " + startingID);
				}
			}
			return i;
		}

	    private int getNextBlockID(int startingID){
			int i = startingID;
			while(Block.blocksList[i] != null && Item.itemsList[i] != null){
				i++;
				if(i >= Block.blocksList.length){
					throw new IndexOutOfBoundsException("No open Item ID numbers available above " + startingID);
				}
			}
			return i;
		}
		
}
