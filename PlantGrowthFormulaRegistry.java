package cyano.basicmachines;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFlower;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.IPlantable;

import cpw.mods.fml.common.FMLLog;

public class PlantGrowthFormulaRegistry {

	private HashMap<ItemEntry,ItemEntry[]> registry = new HashMap<ItemEntry,ItemEntry[]>();
	
	
	private static PlantGrowthFormulaRegistry instance =  null;
	
	private PlantGrowthFormulaRegistry(){
		// private constructor
	}
	/** get singleton instance */
	public static PlantGrowthFormulaRegistry getInstance(){
		if(instance == null){
			instance = new PlantGrowthFormulaRegistry();
		}
		return instance;
	}
	
	/**
	 * Adds a new plant growth formula to the registry
	 * @param seed Seed item
	 * @param results Resulting items
	 */
	public void addEntry(ItemEntry seed, ItemEntry... results){
		registry.put(seed, results);
		StringBuilder notif  = new StringBuilder();
		boolean first = true;
		for(ItemEntry i : results){
			if(!first){
				notif.append("+");
			}
			first = false;
			notif.append(i.itemID+":"+i.metaData);
		}
	//	FMLLog.fine("Added plant growth formula "+seed.itemID+":"+seed.metaData+"="+notif.toString());
	}
	/**
	 * Adds a new plant growth formula to the registry
	 * @param seed Seed item
	 * @param results Resulting items
	 */
	public void addEntry(ItemStack seed, ItemStack... results){
		ItemEntry s = new ItemEntry(seed.itemID,seed.getItemDamage());
		ItemEntry[] o = new ItemEntry[results.length];
		for(int i = 0; i < o.length; i++){
			o[i] = new ItemEntry(results[i].itemID,results[i].getItemDamage());
		}
		this.addEntry(s, o);
	}
	
	public ItemStack[] growPlant(ItemStack seed){
//FMLLog.finest("Attempting to grow plant item "+seed.itemID+":"+seed.getItemDamage());
		if(this.isRegistered(seed)){
			ItemEntry[] items = registry.get(new ItemEntry(seed.itemID,seed.getItemDamage(),false));
			ItemStack[] out = new ItemStack[items.length];
			for(int i = 0; i < out.length; i++){
				out[i] = items[i].toItemStack(1);
			}
			return out;
		} else {
			return null;
		}
	}
	
	/**
	 * Additional formulas for growing plants in the growth chamber machine block. Each formula must  
	 * be in the format seed=result1+result2+result3+... where 
	 * seed is the itemID of the seed item and each result is an itemID of an item 
	 * created by growing the seed. For example, to make a carrot grow into 2 carrots, the 
	 * formula is 391=391+391 and to make a watermelon seed become a watermelon block, the 
	 * formula is 362=103. You can specify metadata with a colon (e.g. 144=144+159:3)
	 * @param formattedEntry A String in the format X or X:Y where X is an itemID and Y is a metadata value
	 */
	public void addEntry(String formattedEntry){
		try{
			ArrayList<ItemEntry> list = new ArrayList<ItemEntry>();
			ItemEntry seed;
			String seedStr = formattedEntry.substring(0,formattedEntry.indexOf('='));
			String[] outStrs =  formattedEntry.substring(formattedEntry.indexOf('=')+1).split("\\+");
			seed = ItemEntry.parseString(seedStr);
			for(String s : outStrs){
				if(s.trim().length() > 0){
					list.add(ItemEntry.parseString(s.trim()));
				}
			}
			this.addEntry(seed, list.toArray(new ItemEntry[list.size()]));
			
		}catch(Exception ex){
			FMLLog.log(Level.SEVERE, ex, "basicmachines: ERROR: plant growth formula entry '"
					+formattedEntry+"' is not properly formatted. It should look something like X:Y or X where X is an itemID and Y is a metadata value");
		}
	}
	/**
	 * Tests whether a given item is in this registry.
	 * @param itemID ID of the item
	 * @param metaData meta data (damage value) of the item
	 * @return true if the item has been registered, false otherwise. Note that the 
	 * registry entry might accept all metadata values or it might be metadata specific.
	 */
	public boolean isRegistered(int itemID, int metaData){
		ItemEntry test = new ItemEntry(itemID,metaData,false);
		return registry.containsKey(test);
	}
	/**
	 * Tests whether a given item is in this registry.
	 * @param itemID ID of the item
	 * @return true if the item has been registered, false otherwise. Note that the 
	 * registry entry might accept all metadata values or it might be metadata specific.
	 */
	public boolean isRegistered(int itemID){
		ItemEntry test = new ItemEntry(itemID);
		return registry.containsKey(test);
	}
	/**
	 * Tests whether a given item is in this registry.
	 * @param item Item to check
	 * @return true if the item has been registered, false otherwise. Note that the 
	 * registry entry might accept all metadata values or it might be metadata specific.
	 */
	public boolean isRegistered(ItemStack item){
		return isRegistered(item.itemID,item.getItemDamage());
	}
	/**
	 * Tests whether a given item is in this registry.
	 * @param item Item to check
	 * @return true if the item has been registered, false otherwise. Note that the 
	 * registry entry might accept all metadata values or it might be metadata specific.
	 */
	public boolean isRegistered(Item item){
		return isRegistered(item.itemID);
	}
	/**
	 * Tests whether a given item is in this registry.
	 * @param item Item to check
	 * @return true if the item has been registered, false otherwise. Note that the 
	 * registry entry might accept all metadata values or it might be metadata specific.
	 */
	public boolean isRegistered(Block item){
		return isRegistered(item.blockID);
	}
	
	
	public static boolean isGenericPlant(ItemStack item){
		if(BasicMachines.autodetect_plant_formulas == false){
			return false;
		}
		if(item.getItem() instanceof IPlantable){
			return true;
		} else if(item.itemID < 4096 && Block.blocksList[item.itemID] != null && Block.blocksList[item.itemID] instanceof BlockFlower){
			return true;
		} else if(item.getItem().getClass().getCanonicalName().equals("extrabiomes.items.ItemFlower")){
			// extra-biomes compatibility
			return true;
		}
		return false;
	}

}
