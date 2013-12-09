package cyano.basicmachines;

import java.util.HashSet;
import java.util.logging.Level;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.FMLLog;

public class CompostablesRegistry {

	private HashSet<ItemEntry> registry = new HashSet<ItemEntry>();
	
	
	private static CompostablesRegistry instance =  null;
	
	private CompostablesRegistry(){
		// private constructor
	}
	/** get singleton instance */
	public static CompostablesRegistry getInstance(){
		if(instance == null){
			instance = new CompostablesRegistry();
		}
		return instance;
	}
	
	/**
	 * Adds the given item with a specific mete data value to the registry.  
	 * @param itemID The item id
	 * @param metaData the meta data of the item
	 */
	public void addEntry(int itemID, int metaData){
		registry.add(new ItemEntry(itemID,metaData));
	}
	/**
	 * Adds the given item without a specific meta data value to the registry. 
	 * Any item of the same item id, regardless of its meta data, will be 
	 * considered to be equal to this entry.   
	 * @param itemID The item id
	 */
	public void addEntry(int itemID){
		registry.add(new ItemEntry(itemID));
	}
	/**
	 * Adds the given item without a specific meta data value to the registry. 
	 * Any item of the same item id, regardless of its meta data, will be 
	 * considered to be equal to this entry.   
	 * @param item The item to register
	 */
	public void addEntry(Item item){
		registry.add(new ItemEntry(item.itemID));
	}
	/**
	 * Adds the given item with a specific meta data value to the registry. 
	 * Any item of the same item id and damage value will be 
	 * considered to be equal to this entry.   
	 * @param item The item to register
	 */
	public void addEntry(ItemStack item){
		registry.add(new ItemEntry(item.itemID,item.getItemDamage()));
	}
	/**
	 * Adds an entry based on a text representation of an item. If you want to specify the 
	 * item by both id and meta data, you can by using a colon like this<br/>
	 * 12044:2<br/>
	 * Here, 12044 is teh itemID and 2 is the meta data.<p/>
	 * If you want any metadata value to apply, then simply omit the colon<br/>
	 * 12043<br/>
	 * In this case, any item with id=12043 will match, no matter what the metadata/damage value is
	 * @param formattedEntry A String in the format X or X:Y where X is an itemID and Y is a metadata value
	 */
	public void addEntry(String formattedEntry){
		try{
			if(formattedEntry.contains(":")){
				String itemIDstr  = formattedEntry.substring(0,formattedEntry.indexOf(':')).trim();
				String itemMetastr = formattedEntry.substring(formattedEntry.indexOf(':')+1).trim();
				if(itemMetastr.equals("*")){
					// treat as not having meta data value
					addEntry(Integer.parseInt(itemIDstr));
				} else {
					addEntry(Integer.parseInt(itemIDstr),Integer.parseInt(itemMetastr));
				}
			} else {
				addEntry(Integer.parseInt(formattedEntry.trim()));
			}
		}catch(Exception ex){
			FMLLog.log(Level.SEVERE, ex, "basicmachines: ERROR: Compostables entry '"
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
		return registry.contains(test);
	}
	/**
	 * Tests whether a given item is in this registry.
	 * @param itemID ID of the item
	 * @return true if the item has been registered, false otherwise. Note that the 
	 * registry entry might accept all metadata values or it might be metadata specific.
	 */
	public boolean isRegistered(int itemID){
		ItemEntry test = new ItemEntry(itemID);
		return registry.contains(test);
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
	

}
