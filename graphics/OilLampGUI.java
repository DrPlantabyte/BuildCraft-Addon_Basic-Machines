package cyano.basicmachines.graphics;

import java.util.logging.Level;

import org.lwjgl.opengl.GL11;

import buildcraft.core.utils.StringUtils;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cyano.basicmachines.BasicMachines;
import cyano.basicmachines.blocks.OilLampTileEntity;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;

public class OilLampGUI  extends GuiContainer{

	OilLampTileEntity tile;
	
	private static final ResourceLocation BLOCK_TEXTURES = TextureMap.locationBlocksTexture;
	
	public OilLampGUI(InventoryPlayer inventory, OilLampTileEntity entity){
		super(new OilLampContainer(inventory,entity));
		tile = entity;
	}
	private boolean smooth = false;
	private float lastin = 0f;
	private float lastout = 0f;
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(BasicMachines.oilLampGUILayer); 
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
        this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize); // x, y, textureOffsetX, textureOffsetY, width, height)

        // draw fluid
        int fill = 52 * tile.getFillLevel() / tile.getMaxFill();
        this.mc.renderEngine.bindTexture(BLOCK_TEXTURES);
        Fluid fluid = tile.getCurrentFluid();
        if(fluid != null){this.drawTexturedModelRectFromIcon(x+109, y+14+(52-fill), fluid.getIcon(), 16, fill);}
		
		// draw tick marks
		this.mc.renderEngine.bindTexture(BasicMachines.oilLampGUILayer); 
        drawTexturedModalRect(x+109, y+14, 176, 36, 16, 52);
        
        
        
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int param1, int param2) {
		String title = LanguageRegistry.instance().getStringLocalization("basicmachines.charger");
        fontRenderer.drawString(title, 8, 6, 0x404040);
     
        
	}
	
	
	
}
