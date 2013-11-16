package cyano.basicmachines;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.logging.Level;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import cyano.basicmachines.blocks.OilLampTileEntity;

public class PacketHandler implements IPacketHandler {

	@Override
	public void onPacketData(INetworkManager manager,
			Packet250CustomPayload packet, Player player) {
		if(!packet.channel.equals("BasicMachines")){return;}
		// forwards GUI actions from client to server tile entities
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(packet.data));
		try{
			int x = in.readInt();
			int y = in.readInt();
			int z = in.readInt();
			byte op = in.readByte();
			in.close();

			EntityPlayerMP playerMP = (EntityPlayerMP) player;
			TileEntity te = playerMP.worldObj.getBlockTileEntity(x, y, z);
			if (te != null) {
				if (te instanceof OilLampTileEntity) {
					OilLampTileEntity tet = (OilLampTileEntity) te;
					tet.setActivated(op != 0);
					playerMP.worldObj.markBlockForUpdate(x, y, z);
				}
			}
		} catch (IOException ex){
			FMLLog.log(Level.SEVERE, ex, this.getClass().getCanonicalName()+": Error reading data packet!");
		}
	}

}
