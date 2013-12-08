package cyano.basicmachines;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import cpw.mods.fml.common.FMLLog;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import cyano.basicmachines.api.PlantGrowthFormula;

public class PlantGrowthFormulaRegistry {
	// singleton
	private static PlantGrowthFormulaRegistry instance = null;
	/** Map<item ID, formula> */
	private final Map<Integer,PlantGrowthFormula> data;
	
	private PlantGrowthFormulaRegistry(){
		// init
		data = new HashMap<Integer,PlantGrowthFormula>();
	}
	/** Adds a new plant growth formula to the game registry */
	public void addPlantGrowthFormula(PlantGrowthFormula f){
		data.put(f.getSeedItemID(), f);
	}
	/**
	 * Adds a new plant growth formula to the game registry
	 * @param seed The seed/plant to grow
	 * @param results The outcome of growing the seed/plant
	 */
	public void addPlantGrowthFormula(Item seed, ItemStack... results){
		this.addPlantGrowthFormula(new PlantGrowthFormula(seed,results));
	}
	/**
	 * Adds a new plant growth formula to the game registry
	 * @param seed The seed/plant to grow
	 * @param results The outcome of growing the seed/plant
	 */
	public void addPlantGrowthFormula(Block seed, ItemStack... results){
		data.put(seed.blockID,new PlantGrowthFormula(seed.blockID,results));
	}
	/**
	 * Adds a new plant growth formula to the game registry
	 * @param seed The seed/plant to grow
	 * @param results The outcome of growing the seed/plant
	 */
	public void addPlantGrowthFormula(Item seed, Item... results){
		ItemStack[] stacks = new ItemStack[results.length];
		for(int i = 0; i < results.length; i++){
			stacks[i] = new ItemStack(results[i]);
		}
		this.addPlantGrowthFormula(new PlantGrowthFormula(seed,stacks));
	}
	/**
	 * Adds a new plant growth formula to the game registry
	 * @param seed The seed/plant to grow
	 * @param results The outcome of growing the seed/plant
	 */
	public void addPlantGrowthFormula(Block seed, Block... results){
		ItemStack[] stacks = new ItemStack[results.length];
		for(int i = 0; i < results.length; i++){
			stacks[i] = new ItemStack(results[i]);
		}
		data.put(seed.blockID,new PlantGrowthFormula(seed.blockID,stacks));
	}
	/**
	 * Parses a string in the format seedItemID=resultItemID1+ResultItemID2 to generate  
	 * and register a new plant growth formula.
	 * <p/>
	 * The formula string needs to be in the format seed=result1+result2+result3+... where 
	 * seed is the itemID of the seed item and each result is an itemID of an item 
	 * created by growing the seed. For example, to make a carrot grow into 2 carrots, the 
	 * formula is 391=391+391 and to make a watermelon seed become a wtaermelon block, the 
	 * formula is 362=103
	 * @param formulaString String in the format seed=result1+result2+result3+... where 
	 * seed is the itemID of the seed item and each result is an itemID of an item 
	 * created by growing the seed.
	 */
	public void addPlantGrowthFormula(String formulaString){
		try{
			int i = formulaString.indexOf('=');
			String seedStr = formulaString.substring(0,i);
			String[] resultStrs = formulaString.substring(i+1, formulaString.length()).trim().split("\\+");
			int seedID = Integer.parseInt(seedStr);
			ItemStack[] results = new ItemStack[resultStrs.length];
			for(int n = 0; n < results.length; n++){
				results[n] = new ItemStack(Integer.parseInt(resultStrs[n]),1,0);
			}
			PlantGrowthFormula f = new PlantGrowthFormula(seedID,results);
			this.addPlantGrowthFormula(f);
		}catch(Exception ex){
			FMLLog.log(Level.SEVERE, ex, "basicmachines: ERROR: Plant growth formula '"
					+formulaString+"' is not properly formatted. It should look something like 295=295+296");
		}
	}
	/** Returns true if there is a registered formula for growing the given item, false otherwise
	 * @param item An item stack that you want to grow 
	 */
	public boolean hasFormula(ItemStack item){
		return data.containsKey(item.itemID);
	}
	/**
	 * Returns a singleton instance of this class
	 */
	public static PlantGrowthFormulaRegistry getInstance(){
		if(instance == null){
			instance = new PlantGrowthFormulaRegistry();
		}
		return instance;
	}
	/**
	 * Gets the result of growing a seed/plant in a growth chamber.
	 * @param seed The seed/plant to grow
	 * @return An array of plants produced by growing the seed.
	 */
	public ItemStack[] growPlant(ItemStack seed) {
		PlantGrowthFormula f = data.get(seed.itemID);
		if(f == null){
			return new ItemStack[0];
		}
		return f.getGrowthResults();
	}
	
	
}
