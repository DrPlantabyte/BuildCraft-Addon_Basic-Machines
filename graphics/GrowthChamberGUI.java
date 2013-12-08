package cyano.basicmachines.graphics;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.registry.LanguageRegistry;
import buildcraft.core.gui.slots.SlotBase;
import cyano.basicmachines.BasicMachines;
import cyano.basicmachines.blocks.GrowthChamberTileEntity;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fluids.Fluid;

public class GrowthChamberGUI extends GuiContainer {
	GrowthChamberTileEntity tile;
	
	public GrowthChamberGUI(InventoryPlayer inventory, GrowthChamberTileEntity entity){
		super(new GrowthChamberContainer(inventory,entity));
		tile = entity;
	}
	
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(BasicMachines.growthChamberGUILayer); 
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
        this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize); // x, y, textureOffsetX, textureOffsetY, width, height)

		
		// draw indicator bars
        float charge = tile.getBuffer() / tile.getMaxBuffer();
		float in = charge*50;
		drawPin(x+10,y+67,in,9f,176,0,16,16);
		
		 // draw fluid
        int fill = 52 * tile.getFillLevel() / tile.getMaxFill();
        this.mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        Fluid fluid = tile.getCurrentFluid();
        if(fluid != null){
        	// draw whole box filled, then redraw the background over it to cover part of the fluid texture
        	this.drawTexturedModelRectFromIcon(x+151, y+17+(0), fluid.getIcon(), 16, 26);
        	this.drawTexturedModelRectFromIcon(x+151, y+17+(26), fluid.getIcon(), 16, 26);
        }
        this.mc.renderEngine.bindTexture(BasicMachines.growthChamberGUILayer); 
    	this.drawTexturedModalRect(x+151, y+17, 151, 17, 16, 52-fill);
		// draw fluid tick marks 
        drawTexturedModalRect(x+151, y+17, 176, 17, 16, 52);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int param1, int param2) {
		String title = LanguageRegistry.instance().getStringLocalization("basicmachines.growthChamber");
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
