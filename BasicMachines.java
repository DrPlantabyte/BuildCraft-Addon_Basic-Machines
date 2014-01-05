package cyano.basicmachines;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import buildcraft.api.power.PowerHandler;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import cyano.basicmachines.blocks.*;
import cyano.basicmachines.client.ClientProxy;
import cyano.basicmachines.graphics.BasicMachinesGUIHandler;
import cyano.basicmachines.items.*;


/*
 * TODO list:
 * - update to 1.7
 * - add Fertilizer Spreader tool to fertilize in a large area
 */

@Mod(modid="basicmachines", name="Cyano's Basic Machines for BuildCraft", version="1.0.1")
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
		public static int blockID_oillamp_on;
		public static int blockID_oillamp_off;
		public static int blockID_growthChamber;
		public static int blockID_composter;

		public static int itemID_rottingMass;
		public static int itemID_compost;
		public static int itemID_pneumaticMotor;
		public static int itemID_pneumaticHammer;
		public static int itemID_pneumaticSaw;
		public static int itemID_pneumaticGun;
		public static int itemID_oilcan_empty;
		public static int itemID_oilcan_water;
		public static int itemID_oilcan_lava;
		public static int itemID_oilcan_oil;
		public static int itemID_oilcan_fuel;
		
		public static BasicMachineFrame block_BasicMachineFrame = null;
		public static IronFurnaceGlowingBlock block_IronFurnaceGlowing = null;
		public static IronFurnaceBlock block_IronFurnace = null;
		public static StorageCellBlock block_StorageCell = null;
		public static LightBoxOffBlock block_LightBoxOff = null;
		public static LightBoxOnBlock block_LightBoxOn = null;
		public static ChargerBlock block_Charger = null;
		public static OilLampBlock block_OilLampOff = null;
		public static OilLampBlockLit block_OilLampOn = null;
		public static GrowthChamberBlock block_GrowthChamber = null;
		public static ComposterBlock block_Composter = null;
		public static PneumaticMotor item_PneumaticMotor = null;
		public static PneumaticHammer item_PneumaticHammer = null;
		public static PneumaticSaw item_PneumaticSaw = null;
		public static PneumaticGun item_PneumaticGun = null;
		public static Compost item_Compost = null;
		public static RottingMass item_RottingMass = null;
		public static OilCanEmpty item_OilCan_empty = null;
		public static Map<Fluid, OilCan> oilCanItems = new HashMap<Fluid, OilCan>();
		
		public static int pneumaticEnergyCapacity = 1024;
		public static float MJperChargeUnit = 8f;
		
		public static float storageCellCapacity = 10000f;
		
		public static Set<Integer> additionalRechargableItemIDs = new java.util.HashSet<Integer>();
		
		public static boolean autodetect_plant_formulas = true;
		
		// graphics resources

		public static ResourceLocation ironFurnaceGUILayer = null;
		public static ResourceLocation storageCellGUILayer = null;
		public static ResourceLocation chargerGUILayer = null;
		public static ResourceLocation oilLampGUILayer = null;
		public static ResourceLocation growthChamberGUILayer = null;
		public static ResourceLocation composterGUILayer = null;

		public static ResourceLocation material_lampmetal = null;
		public static ResourceLocation material_lampglass = null;
		
		
		// mod compatibility
		public boolean mod_BCTools = false; 
		
		

	    // Constants
	    public static final int DEFAULT_PERDITION_DRAIN = 1;
	    public static final int DEFAULT_PERDITION_INTERVAL = 20;
		
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
			
			blockID = config.get("Blocks","blockID_oillamp_off", getNextBlockID(++blockID)).getInt();
			blockID_oillamp_off = blockID;
			block_OilLampOff = new OilLampBlock(blockID_oillamp_off);
			blockID = config.get("Blocks","blockID_oillamp_on", getNextBlockID(++blockID)).getInt();
			blockID_oillamp_on = blockID;
			block_OilLampOn = new OilLampBlockLit(blockID_oillamp_on);
			

			blockID = config.get("Blocks","blockID_growthChamber", getNextBlockID(++blockID)).getInt();
			blockID_growthChamber = blockID;
			block_GrowthChamber = new GrowthChamberBlock(blockID_growthChamber);
			
			blockID = config.get("Blocks","blockID_composter", getNextBlockID(++blockID)).getInt();
			blockID_composter = blockID;
			block_Composter = new ComposterBlock(blockID_composter);
			
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
			itemID = config.get("Items","itemID_compost", getNextItemID(++itemID)).getInt();
			itemID_compost = itemID;
			item_Compost = new Compost(itemID_compost);
			itemID = config.get("Items","itemID_rottingMass", getNextItemID(++itemID)).getInt();
			itemID_rottingMass = itemID;
			item_RottingMass = new RottingMass(itemID_rottingMass);
			
			// oil cans
			itemID = config.get("Items","itemID_oilcan_empty", getNextItemID(++itemID)).getInt();
			itemID_oilcan_empty = itemID;
			itemID = config.get("Items","itemID_oilcan_lava", getNextItemID(++itemID)).getInt();
			itemID_oilcan_lava = itemID;
			itemID = config.get("Items","itemID_oilcan_water", getNextItemID(++itemID)).getInt();
			itemID_oilcan_water = itemID;
			itemID = config.get("Items","itemID_oilcan_oil", getNextItemID(++itemID)).getInt();
			itemID_oilcan_oil = itemID;
			itemID = config.get("Items","itemID_oilcan_fuel", getNextItemID(++itemID)).getInt();
			itemID_oilcan_fuel = itemID;
			item_OilCan_empty = new OilCanEmpty(itemID_oilcan_empty);
			
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
			
			autodetect_plant_formulas = config.get("Options", "autodetect_growthchamber_formulas",true,"If true, " +
					"then all plantable blocks and items (as far as this mod can determine) will be growable in " +
					"the growth chamber. Items and blocks from other mods are not guarenteed to be detected as there " +
					"is no 'plant' superclass.").getBoolean(true); 
			
			String plantGrowthFormulas = config.get("Options", "additional_growthchamber_formulas", 
					"37=37+37,38=38+38,81=81+81,83=83+83,106=106+106,111=111+111,31=31+31",
					"Additional formulas for growing plants in the growth chamber machine block. Each formula must " +
					"be in the format seed=result1+result2+result3+... where "+
					"seed is the itemID of the seed item and each result is an itemID of an item "+
					"created by growing the seed. For example, to make a carrot grow into 2 carrots, the "+
					"formula is 391=391+391 and to make a watermelon seed become a watermelon block, the "+
					"formula is 362=103. You can specify metadata with a colon (e.g. 144=144+159:3)").getString();
			String[] formulas = plantGrowthFormulas.trim().split(",");
			for(String f : formulas){
				PlantGrowthFormulaRegistry.getInstance().addEntry(f);
			}
			
			String compostableList = config.get("Options", "additional_compostable_items", 
					"349,400,351:0,351:1,351:2,351:3,351:11,351:15",
					"Additional compostable items. You can put either specify the item by " +
					"ID:metadata or just an itemID and make all forms of that item " +
					"compostable, regardless of the metadata value.").getString();
			String[] compostables = compostableList.split(",");
			for(String c : compostables){
				CompostablesRegistry.getInstance().addEntry(c);
			}
			
			// done with config file
			config.save();
			 
			// set resource locations
			ironFurnaceGUILayer = new ResourceLocation("basicmachines:textures/gui/ironfurnace.png");
			storageCellGUILayer = new ResourceLocation("basicmachines:textures/gui/storagecell.png");
			chargerGUILayer = new ResourceLocation("basicmachines:textures/gui/charger.png");
			oilLampGUILayer = new ResourceLocation("basicmachines:textures/gui/oillamp.png");
			material_lampmetal = new ResourceLocation("basicmachines:textures/model/lamp_metal.png");
			material_lampglass = new ResourceLocation("basicmachines:textures/model/lamp_glass.png");
			growthChamberGUILayer = new ResourceLocation("basicmachines:textures/gui/growthchamber.png");
			composterGUILayer = new ResourceLocation("basicmachines:textures/gui/composter.png");
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
			GameRegistry.registerTileEntity(OilLampTileEntity.class, "oilLampTileEntity");
			GameRegistry.registerTileEntity(ComposterTileEntity.class, "composterTileEntity");
			GameRegistry.registerTileEntity(GrowthChamberTileEntity.class, "growthChamberTileEntity");

			//Register the guis
			NetworkRegistry.instance().registerGuiHandler(this, new BasicMachinesGUIHandler());
			NetworkRegistry.instance().registerChannel(new PacketHandler(), "BasicMachines", Side.SERVER);
		
			// plant growth chamber formulas
			PlantGrowthFormulaRegistry plantReg = PlantGrowthFormulaRegistry.getInstance();
			plantReg.addEntry(new ItemStack(Item.appleRed),new ItemStack(Block.sapling));
			plantReg.addEntry(new ItemStack(Block.mushroomBrown), new ItemStack(Block.mushroomBrown), new ItemStack(Block.mushroomBrown));
			plantReg.addEntry(new ItemStack(Block.mushroomRed), new ItemStack(Block.mushroomRed), new ItemStack(Block.mushroomRed));
			plantReg.addEntry(new ItemStack(Item.seeds), new ItemStack(Item.wheat));
			plantReg.addEntry(new ItemStack(Item.wheat), new ItemStack(Item.seeds),new ItemStack(Item.seeds),new ItemStack(Item.seeds));
			plantReg.addEntry(new ItemStack(Item.carrot), new ItemStack(Item.carrot), new ItemStack(Item.carrot));
			plantReg.addEntry(new ItemStack(Item.potato), new ItemStack(Item.potato), new ItemStack(Item.potato));
			plantReg.addEntry(new ItemStack(Item.melonSeeds), new ItemStack(Block.melon));
			plantReg.addEntry(new ItemStack(Item.pumpkinSeeds), new ItemStack(Block.pumpkin));
			
			// register compostable items
			CompostablesRegistry compReg = CompostablesRegistry.getInstance();
			compReg.addEntry(net.minecraft.item.Item.rottenFlesh);
			compReg.addEntry(net.minecraft.item.Item.slimeBall);
			// food items are added to registry in post-init
			
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
			GameRegistry.addRecipe(craft, " g ","rbr"," g ",'b',block_BasicMachineFrame,'g',buildcraft.BuildCraftCore.goldGearItem, 'r', net.minecraft.block.Block.blockRedstone);
			
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
			
			block_OilLampOff.setUnlocalizedName("basicmachines.oilLamp");
			LanguageRegistry.addName(block_OilLampOff, "Oil Lamp");
			GameRegistry.registerBlock(block_OilLampOff,"basicmachines.oilLamp");
			block_OilLampOn.setUnlocalizedName("basicmachines.oilLampActive");
			LanguageRegistry.addName(block_OilLampOn, "Oil Lamp (lit)");
			GameRegistry.registerBlock(block_OilLampOn,"basicmachines.oilLampActive");
			craft = new ItemStack(block_OilLampOff);
			GameRegistry.addRecipe(craft, " g ","lfl"," b ",'f',block_BasicMachineFrame,'b',Item.bucketEmpty, 'g',Item.flintAndSteel,'l',Block.thinGlass);
			
			block_GrowthChamber.setUnlocalizedName("basicmachines.growthChamber");
			LanguageRegistry.addName(block_GrowthChamber, "Growth Chamber");
			GameRegistry.registerBlock(block_GrowthChamber,"basicmachines.growthChamber");
			craft = new ItemStack(block_GrowthChamber);
			GameRegistry.addRecipe(craft, " L ","tbt"," p ",'b',block_BasicMachineFrame,'L',block_LightBoxOff,'t',buildcraft.BuildCraftFactory.tankBlock,'p',Item.flowerPot);

			block_Composter.setUnlocalizedName("basicmachines.composter");
			LanguageRegistry.addName(block_Composter, "Composter");
			GameRegistry.registerBlock(block_Composter,"basicmachines.composter");
			craft = new ItemStack(block_Composter);
			GameRegistry.addRecipe(craft, " h ","ibi"," f ",'b',block_BasicMachineFrame,'h',Block.hopperBlock,'i',Item.ingotIron,'f',Block.furnaceIdle);
			
		
			
			item_Compost.setUnlocalizedName("basicmachines.compost");
			LanguageRegistry.addName(item_Compost, "Compost");
			GameRegistry.registerItem(item_Compost, "basicmachines.compost");
			
			item_RottingMass.setUnlocalizedName("basicmachines.rottingMass");
			LanguageRegistry.addName(item_RottingMass, "Rotting Sludge");
			GameRegistry.registerItem(item_RottingMass, "basicmachines.rottingMass");
			
			
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
			
			
			item_OilCan_empty.setUnlocalizedName("basicmachines.emptyOilCan");
			LanguageRegistry.addName(item_OilCan_empty, "Oil Can (Empty)");
			GameRegistry.registerItem(item_OilCan_empty, "basicmachines.emptyOilCan");
			craft = new ItemStack(item_OilCan_empty);
			GameRegistry.addRecipe(craft, "gi","b ",'i',Item.ingotIron,'g',buildcraft.BuildCraftCore.woodenGearItem, 'b', Item.bucketEmpty);
			GameRegistry.addRecipe(craft, "ig"," b",'i',Item.ingotIron,'g',buildcraft.BuildCraftCore.woodenGearItem, 'b', Item.bucketEmpty);
			
			
			// liquids
			// fluids with BuildCraft installed:
			// 		water, lava, fuel, oil
			Map<String, Fluid> fluids = net.minecraftforge.fluids.FluidRegistry.getRegisteredFluids();
			addOilCan(itemID_oilcan_water,"water",fluids);
			addOilCan(itemID_oilcan_lava,"lava",fluids);
			addOilCan(itemID_oilcan_fuel,"fuel",fluids);
			addOilCan(itemID_oilcan_oil,"oil",fluids);
			
			
			
			// activate compatibility with BCTools mod
			if (Loader.isModLoaded("BCTools")){
				mod_BCTools = true;
				 try {
				      Class.forName("maexx.bcTools.api.IBctChargeable");
				      // it exists on the classpath
				   } catch(ClassNotFoundException e) {
				      // it does not exist on the classpath
					  FMLLog.log(Level.WARNING, e, "Mod 'basicmachines' failed to initialize API for mod 'BCTools', BCTools interoperability will not occur");
					  mod_BCTools = false; 
				   }
			}
		}
	    
	 // Mark this method for receiving an FMLEvent (in this case, it's the FMLPreInitializationEvent)
	    @EventHandler public void postInit(FMLPostInitializationEvent event)
	    {
	    	// stuff to do after initialization
	    	
	    	// register compostable food items
	    	FMLLog.info("basicmachines: Scanning for items to add to compostables regirstry...");
	    	CompostablesRegistry compReg = CompostablesRegistry.getInstance();
			PlantGrowthFormulaRegistry plantReg = PlantGrowthFormulaRegistry.getInstance();
			for(int i = 1; i < Block.blocksList.length; i++){
				if(Block.blocksList[i] != null){
	    			Block block = Block.blocksList[i];
	    			if(block instanceof net.minecraftforge.common.IPlantable || block instanceof net.minecraft.block.BlockFlower){
	    				compReg.addEntry(block.blockID);
	    				FMLLog.info("\tbasicmachines: made "+block.getUnlocalizedName()+" compostable");
	    			}
				}
	    	}
			for(int i = 1; i < Item.itemsList.length; i++){
	    		if(Item.itemsList[i] != null){
	    			Item item = Item.itemsList[i];
	    			if(item instanceof net.minecraft.item.ItemFood){
	    				compReg.addEntry(item);
	    				FMLLog.info("\tbasicmachines: made "+item.getUnlocalizedName()+" compostable");
	    			} else if(item instanceof net.minecraftforge.common.IPlantable){
	    				compReg.addEntry(item);
	    				FMLLog.info("\tbasicmachines: made "+item.getUnlocalizedName()+" compostable");
	    			}
	    		}
	    	}
	    }
	    
	    /**
	     * Adds a new Oil Can item for a given fluid.
	     * @param itemID
	     * @param modID
	     * @param fluid
	     * @param fluids
	     */
	    public static void addOilCan(int itemID, String modID, String fluid, Map<String, Fluid> fluids){
	    	OilCan oc = new OilCan(itemID,fluids.get(fluid),modID+":oilcan_"+fluid);
			oc.setUnlocalizedName(modID+".oilCan_"+fluid);
			LanguageRegistry.addName(oc, "Oil Can ("+fluid+")");
			GameRegistry.registerItem(oc, modID+".oilCan_"+fluid);
			oilCanItems.put(fluids.get(fluid), oc);
	    }
	    public static void addOilCan(int itemID, String fluid, Map<String, Fluid> fluids){
	    	addOilCan(itemID,"basicmachines",fluid,fluids);
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
