package cyano.basicmachines;


import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;
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
import cyano.basicmachines.blocks.*;


/*
 * TODO list
 BuildCraft Basics Addon
Machines: (in order of priority)
+ Iron Furnace - Buildcraft powered furnace
+ Energy Cell - Stores buildcraft energy (top+bottom=input, sides=output)
- Sorter - uses a filter to divert specific items
- Lightbulb - Buildcraft powered torch
More Machines: (lower priority)
- Lamp - Oil-powered torch
- Charger - Recharge items that use buildcraft energy
Tools: All have max capacity of 1024 MJ, but use different amounts of energy per action
- Pneumatic Drill - Buildcraft powered pickaxe
- Pneumatic Saw - Buildcraft powered axe
- Pneumatic Gun - Buildcraft powered bow
Other Items:
+ Basic Machine Frame
 */

@Mod(modid="basicmachines", name="Cyano's Basic Machines for BuildCraft", version="0.2.0")
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
		
		public static BasicMachineFrame bmf = null;
		public static IronFurnaceGlowingBlock ifbon = null;
		public static IronFurnaceBlock ifboff = null;
		public static StorageCellBlock scb = null;
		
		 
		// graphics resources

		public static ResourceLocation ironFurnaceGUILayer = null;
		public static ResourceLocation storageCellGUILayer = null;
		// Mark this method for receiving an FMLEvent (in this case, it's the FMLPreInitializationEvent)
	    @EventHandler public void preInit(FMLPreInitializationEvent event)
	    {
	        // Do stuff in pre-init phase (read config, create blocks and items, register them)
	    	Configuration config = new Configuration(event.getSuggestedConfigurationFile());
	    	
	    	config.get("options", "UseAlternativeRecipe", alternateRecipe,"If the crafting recipe for " +
	    			"the basic machine frame clashes with another recipe, set this to true to use a " +
	    			"different recipe.");
	    	
			int itemID = 1100;
			itemID = config.get("Blocks","blockID_BasicMachineFrame", getNextBlockID(itemID++)).getInt();
			blockID_basicMachineFrame = itemID;
			bmf = new BasicMachineFrame(blockID_basicMachineFrame);
			
			itemID = config.get("Blocks","blockID_IronFurnaceA", getNextBlockID(itemID++)).getInt();
			blockID_ironFurnace_off = itemID;
			ifboff = new IronFurnaceBlock(blockID_ironFurnace_off);
			itemID = config.get("Blocks","blockID_IronFurnaceB", getNextBlockID(itemID++)).getInt();
			blockID_ironFurnace_on = itemID;
			ifbon = new IronFurnaceGlowingBlock(blockID_ironFurnace_on);
			
			itemID = config.get("Blocks","blockID_storageCell", getNextBlockID(itemID++)).getInt();
			blockID_storageCell = itemID;
			scb = new StorageCellBlock(blockID_storageCell);
			
		
			config.save();
			 
			// set resource locations
			ironFurnaceGUILayer = new ResourceLocation("basicmachines:textures/gui/ironfurnace.png");
			storageCellGUILayer = new ResourceLocation("basicmachines:textures/gui/storagecell.png");
	    }
	    
	    
	    @EventHandler
		public void init(FMLInitializationEvent event) {
			proxy.registerRenderers();

			if (event.getSide().isClient())	{ClientProxy.setCustomRenderers();}


			//Register the tile entities
			GameRegistry.registerTileEntity(IronFurnaceTileEntity.class, "ironFurnaceTileEntity");
			GameRegistry.registerTileEntity(StorageCellTileEntity.class, "storageCellTileEntity");

			//Register the guis
			NetworkRegistry.instance().registerGuiHandler(this, new BasicMachinesGUIHandler());
			
	
			// language registry and crafting recipes 
			bmf.setUnlocalizedName("basicmachines.basicMachineFrame");
			LanguageRegistry.addName(bmf, "Basic Machine Frame");
			GameRegistry.registerBlock(bmf,"basicMachineFrame");
			if(alternateRecipe == false){
				// normal recipe
				final ItemStack output = new ItemStack(bmf);
				// now add an oak-plank version to make it show up in NEI
				GameRegistry.addRecipe(output, "pip", "i i", "pip", 'p', Block.planks, 'i',
						Item.ingotIron);
			} else {
				// alternative recipe, bricks instead of wood
				final ItemStack output = new ItemStack(bmf);
				GameRegistry.addRecipe(output, "bib", "i i", "bib", 'b', Item.brick, 'i',
						Item.ingotIron);
			}
			
			
			
			ifboff.setUnlocalizedName("basicmachines.ironFurnace");
			LanguageRegistry.addName(ifboff, "Iron Furnace");
			GameRegistry.registerBlock(ifboff,"ironFurnace");
			ItemStack craft = new ItemStack(ifboff);
			GameRegistry.addRecipe(craft, " i ", "ibi", " i ", 'b', bmf, 'i', Item.ingotIron);
			ifbon.setUnlocalizedName("basicmachines.ironFurnaceActive");
			LanguageRegistry.addName(ifbon, "Iron Furnace (Active)");
			GameRegistry.registerBlock(ifbon,"ironFurnaceActive");
			

			scb.setUnlocalizedName("basicmachines.storageCell");
			LanguageRegistry.addName(scb, "Storage Cell");
			GameRegistry.registerBlock(scb,"storageCell");
			craft = new ItemStack(scb);
			GameRegistry.addRecipe(craft, " p ","pbp"," p ",'b',bmf,'p',buildcraft.BuildCraftTransport.pipePowerGold);
		}
	    
	    
	    private int getNextItemID(int startingID){
			int i = startingID;
			while(Item.itemsList[i] != null){
				i++;
				if(i >= Item.itemsList.length){
					throw new IndexOutOfBoundsException("No open Item ID numbers available above " + startingID);
				}
			}
			return i;
		}

	    private int getNextBlockID(int startingID){
			int i = startingID;
			while(Block.blocksList[i] != null){
				i++;
				if(i >= Block.blocksList.length){
					throw new IndexOutOfBoundsException("No open Item ID numbers available above " + startingID);
				}
			}
			return i;
		}
		
}
