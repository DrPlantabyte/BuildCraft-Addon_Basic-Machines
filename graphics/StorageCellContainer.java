package cyano.basicmachines.graphics;

import cyano.basicmachines.blocks.StorageCellTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;

public class StorageCellContainer extends Container{

	StorageCellTileEntity tile;
	
	public StorageCellContainer(InventoryPlayer inv, StorageCellTileEntity te){
		tile = te;
		// no actual inventory slots
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return tile.isUseableByPlayer(entityplayer);
	}
}
