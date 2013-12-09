package cyano.basicmachines.graphics;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.registry.LanguageRegistry;
import cyano.basicmachines.BasicMachines;
import cyano.basicmachines.blocks.ComposterTileEntity;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fluids.Fluid;

public class ComposterGUI extends GuiContainer{

	private final ComposterTileEntity te;
	
	public ComposterGUI(InventoryPlayer inventory, ComposterTileEntity tileEntity){
		super(new ComposterContainer(inventory,tileEntity));
		te = tileEntity;
		
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(BasicMachines.composterGUILayer); 
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
        this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize); // x, y, textureOffsetX, textureOffsetY, width, height)

		
		// draw indicator bars
        float charge = te.getBuffer() / te.getMaxBuffer();
		float in = charge*50;
		drawPin(x+26,y+64,in,9f,176,0,16,16);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int param1, int param2) {
		String title = LanguageRegistry.instance().getStringLocalization("basicmachines.composter");
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
}
