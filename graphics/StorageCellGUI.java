package cyano.basicmachines.graphics;

import java.util.logging.Level;

import org.lwjgl.opengl.GL11;

import buildcraft.core.utils.StringUtils;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cyano.basicmachines.BasicMachines;
import cyano.basicmachines.blocks.StorageCellTileEntity;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.MathHelper;

public class StorageCellGUI  extends GuiContainer{

	StorageCellTileEntity tile;
	
	public StorageCellGUI(InventoryPlayer inventory, StorageCellTileEntity entity){
		super(new StorageCellContainer(inventory,entity));
		tile = entity;
	}
	private boolean smooth = false;
	private float lastin = 0f;
	private float lastout = 0f;
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(BasicMachines.storageCellGUILayer); 
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
        this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize); // x, y, textureOffsetX, textureOffsetY, width, height)

		// draw temperature gauge
        float charge = tile.getCharge() / tile.getMaxCharge();
        float needleAngle = 4.1888f * charge - 2.0944f; 
		drawNeedle(x+87f, y+40f, needleAngle, 12f, 176, 34, 10, 10);
		
		// draw current bars
		float in = tile.getInputCurrentScaled(50);
		float out = tile.getOutputCurrentScaled(50);
		if(smooth){
			if(in - lastin > 1){
				in = lastin + 1;
			} else if (in - lastin < -1){
				in = lastin - 1;
			}
			if(out - lastout > 1){
				out = lastout + 1;
			} else if (out - lastout < -1){
				out = lastout - 1;
			}
		}else{
			smooth = true;
		}
		lastin = in;
		lastout = out;
		
		drawPin(x+39,y+65,in,9f,176,16,16,16);
		drawPin(x+121,y+65,out,9f,176,0,16,16);
		
        
        // power display for debugging only
       // float energyStore = furnace.getEnergyStore() / furnace.getEnergyStoreMaximum();
       // drawNeedle(x+87f, y+29f, 2 * energyStore * 2.0944f - 2.0944f , 8f, 2, 2, 8, 8);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int param1, int param2) {
		String title = LanguageRegistry.instance().getStringLocalization("basicmachines.storageCell");
        fontRenderer.drawString(title, 8, 6, 0x404040);
     
        
	}
	
	private void drawPin(float centerX, float centerY, float verticalOffset,  float size, int texOffsetX, int texOffsetY, int texWidth, int texHeight){
		float fx = 0.00390625F; // image is 256x256 pixels, so this conversion factor normalizes to a number from 0 to 1
		float fy = 0.00390625F;
		
		float needleWidth = 0.3f*size;
		float backLength = needleWidth;
		
		float pointX = centerX+size;
		float pointY = centerY-verticalOffset;
		float backX = centerX-backLength;
		float backY = centerY-verticalOffset;
		float topX = centerX;
		float topY = centerY-needleWidth-verticalOffset;
		float botX = centerX;
		float botY = centerY+needleWidth-verticalOffset;
		
		int pointU = texOffsetX;
		int pointV = texOffsetY;
		int backU = texOffsetX+texWidth;
		int backV = texOffsetY+texHeight;
		int topU = texOffsetX;
		int topV = texOffsetY+texHeight;
		int botU = texOffsetX+texWidth;
		int botV = texOffsetY;
		
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV((double) pointX,
				(double) pointY, (double) this.zLevel,
				(double) ((float) pointU * fx),
				(double) ((float) pointV * fy));
		tessellator.addVertexWithUV((double) topX,
				(double) topY, (double) this.zLevel,
				(double) ((float) topU * fx),
				(double) ((float) topV * fy));
		tessellator.addVertexWithUV((double) backX,
				(double) backY, (double) this.zLevel,
				(double) ((float) backU * fx),
				(double) ((float) backV * fy));
		tessellator.addVertexWithUV((double) botX, (double) botY,
				(double) this.zLevel, (double) ((float) botU * fx),
				(double) ((float) botV * fy));
		tessellator.draw();
	}
	
	private void drawNeedle(float centerX, float centerY, float angle, float size, int texOffsetX, int texOffsetY, int texWidth, int texHeight){
	//	int x=0,  y=0,  u=0,  v=0,  w=0,  h=0; // x,y,u,v,width,height
		float fx = 0.00390625F; // image is 256x256 pixels, so this conversion factor normalizes to a number from 0 to 1
		float fy = 0.00390625F;
		
		float needleWidth = 0.2f;
		float buttLength = 0.5f;
		
		float pointX = MathHelper.sin(angle)*size + centerX;
		float pointY = -MathHelper.cos(angle)*size + centerY;
		float buttX = -buttLength * MathHelper.sin(angle)*size + centerX;
		float buttY = buttLength * MathHelper.cos(angle)*size + centerY;
		float leftX = -needleWidth * MathHelper.cos(angle)*size + centerX;
		float leftY = -needleWidth * MathHelper.sin(angle)*size + centerY;
		float rightX = needleWidth * MathHelper.cos(angle)*size + centerX;
		float rightY = needleWidth * MathHelper.sin(angle)*size + centerY;
		
		int pointU = texOffsetX;
		int pointV = texOffsetY;
		int buttU = texOffsetX+texWidth;
		int buttV = texOffsetY+texHeight;
		int leftU = texOffsetX;
		int leftV = texOffsetY+texHeight;
		int rightU = texOffsetX+texWidth;
		int rightV = texOffsetY;
		
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV((double) pointX,
				(double) pointY, (double) this.zLevel,
				(double) ((float) pointU * fx),
				(double) ((float) pointV * fy));
		tessellator.addVertexWithUV((double) leftX,
				(double) leftY, (double) this.zLevel,
				(double) ((float) leftU * fx),
				(double) ((float) leftV * fy));
		tessellator.addVertexWithUV((double) buttX,
				(double) buttY, (double) this.zLevel,
				(double) ((float) buttU * fx),
				(double) ((float) buttV * fy));
		tessellator.addVertexWithUV((double) rightX, (double) rightY,
				(double) this.zLevel, (double) ((float) rightU * fx),
				(double) ((float) rightV * fy));
		tessellator.draw();
	}
}
