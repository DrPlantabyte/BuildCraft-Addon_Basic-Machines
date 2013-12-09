package cyano.basicmachines;

import java.util.logging.Level;

import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.FMLLog;


public class ItemEntry{
	final int itemID;
	final int metaData;
	final boolean enforceMetaData;
	
	/**
	 * Makes an item entry that uses metadata
	 * @param itemID ID of the item
	 * @param metaData meta data of the item
	 */
	public ItemEntry(int itemID, int metaData){
		enforceMetaData = true;
		this.itemID = itemID;
		this.metaData = metaData;
	}
	/**
	 * 
	 * Makes an item entry that may or may not use metadata
	 * @param itemID ID of the item
	 * @param metaData meta data of the item
	 * @param enforceMetaData if true, the meta data will always be used
	 */
	public ItemEntry(int itemID, int metaData, boolean enforceMetaData){
		this.enforceMetaData = enforceMetaData;
		this.itemID = itemID;
		this.metaData = metaData;
	}
	/**
	 * Makes an item entry that ignores metadata
	 * @param itemID ID of the item
	 */
	public ItemEntry(int itemID){
		enforceMetaData = false;
		this.itemID = itemID;
		this.metaData = 0;
	}
	/**
	 * Adds an entry based on a text representation of an item. If you want to specify the 
	 * item by both id and meta data, you can by using a colon like this<br/>
	 * 12044:2<br/>
	 * Here, 12044 is the itemID and 2 is the meta data.<p/>
	 * If you want any metadata value to apply, then simply omit the colon<br/>
	 * 12043<br/>
	 * In this case, any item with id=12043 will match, no matter what the metadata/damage value is
	 * @param formattedEntry A String in the format X or X:Y where X is an itemID and Y is a metadata value
	 * @throws NumberFormatException
	 */
	public static ItemEntry parseString(String formattedEntry) throws NumberFormatException{
			if(formattedEntry.contains(":")){
				String itemIDstr  = formattedEntry.substring(0,formattedEntry.indexOf(':')).trim();
				String itemMetastr = formattedEntry.substring(formattedEntry.indexOf(':')+1).trim();
				if(itemMetastr.equals("*")){
					// treat as not having meta data value
					return new ItemEntry(Integer.parseInt(itemIDstr));
				} else {
					return new ItemEntry(Integer.parseInt(itemIDstr),Integer.parseInt(itemMetastr));
				}
			} else {
				return new ItemEntry(Integer.parseInt(formattedEntry.trim()));
			}
		
	}
	
	@Override public int hashCode(){
		return (itemID * 57);
	}
	
	@Override public boolean equals(Object other){
		if(other instanceof ItemEntry){
			return compare(this,(ItemEntry)other);
		} else {
			return false;
		}
	}
	
	public static boolean compare(ItemEntry ref, ItemEntry testCase){
		if(ref.enforceMetaData || testCase.enforceMetaData){
			return ref.itemID == testCase.itemID && ref.metaData == testCase.metaData;
		} else {
			return ref.itemID == testCase.itemID ;
		}
	}
	/** Makes an ItemStack from the stored values */
	public ItemStack toItemStack(int stackSize) {
		return new ItemStack(this.itemID,stackSize,this.metaData);
	}
}