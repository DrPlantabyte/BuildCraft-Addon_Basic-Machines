package cyano.basicmachines.graphics;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;
import cyano.basicmachines.BasicMachines;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

public class OilLampRenderer  extends TileEntitySpecialRenderer{

	final OilLampModel lampModel;
	
	public OilLampRenderer(){
		lampModel = new OilLampModel();
	}
	
	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double par2, double par4, double par6, float par8) {
		int metadata = tileEntity.blockMetadata;
		GL11.glPushMatrix();
		
        GL11.glTranslatef((float)par2 + 0.5F, (float)par4 + 0.75F * 1.0F, (float)par6 + 0.5F);
        GL11.glScalef(1.0F, 1.0F, 1.0F);
        
        FMLClientHandler.instance().getClient().renderEngine.bindTexture(BasicMachines.material_lampmetal);
	    lampModel.drawLampFrame(metadata);
	    
	    GL11.glPopMatrix();
	}

}
