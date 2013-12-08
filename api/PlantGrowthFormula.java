package cyano.basicmachines.api;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
/** This data class is used to keep track of Growth Chamber plant growth behavior */
public class PlantGrowthFormula {

	/** The plant/seed itemID that will be grown */ 
	final int seed;
	/** The outcome of growing the seed/plant */
	final ItemStack[] spawn;
	
	public PlantGrowthFormula(Item seed, ItemStack... output){
		this.seed = seed.itemID;
		this.spawn = output;
	}
	public PlantGrowthFormula(int seedID, ItemStack... output){
		this.seed = seedID;
		this.spawn = output;
	}
	/**
	 * Determines whether this formula can be used on the given item stack
	 * @param item An item stack to test for growability
	 * @return true if the provided item stack is appropriate for this formula, false otherwise.  
	 */
	public boolean canGrow(ItemStack item){
		return item.itemID == seed;
	}
	/**
	 * Returns the items that are produced by growing the seed/plant in this formula
	 */
	public ItemStack[] getGrowthResults(){
		return spawn;
	}
	
	public int getSeedItemID(){
		return seed;
	}
}
