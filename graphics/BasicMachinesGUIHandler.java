package cyano.basicmachines.graphics;

import java.util.logging.Level;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.IGuiHandler;
import cyano.basicmachines.blocks.*;

public class BasicMachinesGUIHandler implements IGuiHandler {
	/**
     * Returns a Server side Container to be displayed to the user.
     * 
     * @param ID
     *            The Gui ID Number
     * @param player
     *            The player viewing the Gui
     * @param world
     *            The current world
     * @param x
     *            X Position
     * @param y
     *            Y Position
     * @param z
     *            Z Position
     * @return A GuiScreen/Container to be displayed to the user, null if none.
     */
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
	//	FMLLog.log(Level.FINE, "basicmachines/"+this.getClass().getSimpleName()+": opening server-side GUI #"+ID);
		switch(ID){
		case 0:
			// iron furnace
			if(tileEntity instanceof IronFurnaceTileEntity){
				return new IronFurnaceContainer(player.inventory, (IronFurnaceTileEntity)tileEntity);
			}
		case 1:
			// storage cell
			if(tileEntity instanceof StorageCellTileEntity){
				return new StorageCellContainer(player.inventory, (StorageCellTileEntity)tileEntity);
			}
		case 2:
			// storage cell
			if(tileEntity instanceof ChargerTileEntity){
				return new ChargerContainer(player.inventory, (ChargerTileEntity)tileEntity);
			}
		case 3:
			// oil lamp
			if(tileEntity instanceof OilLampTileEntity){
				return new OilLampContainer(player.inventory, (OilLampTileEntity)tileEntity);
			}
		
		}
		return null;
	}

	 /**
     * Returns a Container to be displayed to the user. On the client side, this
     * needs to return a instance of GuiScreen On the server side, this needs to
     * return a instance of Container
     * 
     * @param ID
     *            The Gui ID Number
     * @param player
     *            The player viewing the Gui
     * @param world
     *            The current world
     * @param x
     *            X Position
     * @param y
     *            Y Position
     * @param z
     *            Z Position
     * @return A GuiScreen/Container to be displayed to the user, null if none.
     */
	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
	//	FMLLog.log(Level.FINE, "basicmachines/"+this.getClass().getSimpleName()+": opening client-side GUI #"+ID);
		switch(ID){
		case 0:
			// iron furnace
			if(tileEntity instanceof IronFurnaceTileEntity){
				return new IronFurnaceGUI(player.inventory, (IronFurnaceTileEntity)tileEntity);
			}
		case 1:
			// storage cell
			if(tileEntity instanceof StorageCellTileEntity){
				return new StorageCellGUI(player.inventory, (StorageCellTileEntity)tileEntity);
			}
		case 2:
			// storage cell
			if(tileEntity instanceof ChargerTileEntity){
				return new ChargerGUI(player.inventory, (ChargerTileEntity)tileEntity);
			}
		case 3:
			// oil lamp
			if(tileEntity instanceof OilLampTileEntity){
				return new OilLampGUI(player.inventory, (OilLampTileEntity)tileEntity);
			}
		}
		return null;
	}

}
