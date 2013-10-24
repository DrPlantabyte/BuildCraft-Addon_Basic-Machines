package cyano.basicmachines.graphics;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.registry.LanguageRegistry;

import buildcraft.core.utils.StringUtils;

import cyano.basicmachines.BasicMachines;
import cyano.basicmachines.blocks.IronFurnaceTileEntity;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

public class IronFurnaceGUI extends GuiContainer{

	private IronFurnaceTileEntity furnace;
	
	public IronFurnaceGUI(InventoryPlayer inventory, IronFurnaceTileEntity tile) {
		super(new IronFurnaceContainer(inventory,tile));
		furnace = tile;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(BasicMachines.ironFurnaceGUILayer); 
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
        this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize); // x, y, textureOffsetX, textureOffsetY, width, height)

		// draw temperature gauge
        float temp = furnace.getTemperature();
        float needleAngle = 0.010472f * temp - 2.0944f; 
	//	this.mc.renderEngine.bindTexture(BasicMachines.ironFurnaceGUILayer);
		drawNeedle(x+87f, y+29f, needleAngle, 12f, 176, 0, 8, 8);
		
		// draw cooking progress bar
		int cookBar = furnace.getCookProgressScaled(24);
        this.drawTexturedModalRect(x + 80, y + 54, 176, 14, cookBar + 1, 16);
        
        // power display for debugging only
       // float energyStore = furnace.getEnergyStore() / furnace.getEnergyStoreMaximum();
       // drawNeedle(x+87f, y+29f, 2 * energyStore * 2.0944f - 2.0944f , 8f, 2, 2, 8, 8);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int param1, int param2) {
		String title = LanguageRegistry.instance().getStringLocalization("basicmachines.ironFurnace");
        fontRenderer.drawString(title, 8, 6, 0x404040);
        fontRenderer.drawString(StringUtils.localize("gui.inventory"), 8, (ySize - 96) + 2, 0x404040);
     
        
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
