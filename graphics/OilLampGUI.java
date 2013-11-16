package cyano.basicmachines.graphics;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;

import org.lwjgl.opengl.GL11;

import buildcraft.core.utils.StringUtils;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cyano.basicmachines.BasicMachines;
import cyano.basicmachines.blocks.OilLampTileEntity;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.network.packet.Packet250CustomPayload;
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

        // draw flame button
        if(tile.isActivated()){
        	this.drawTexturedModalRect(x+79, y+54, 176, 18, 18, 18);
        } else {
        	this.drawTexturedModalRect(x+79, y+54, 176, 0, 18, 18);
        }
        
        // draw fluid
        int fill = 52 * tile.getFillLevel() / tile.getMaxFill();
        this.mc.renderEngine.bindTexture(BLOCK_TEXTURES);
        Fluid fluid = tile.getCurrentFluid();
        if(fluid != null){
        	// draw whole box filled, then redraw the background over it to cover part of the fluid texture
        	this.drawTexturedModelRectFromIcon(x+109, y+14+(0), fluid.getIcon(), 16, 26);
        	this.drawTexturedModelRectFromIcon(x+109, y+14+(26), fluid.getIcon(), 16, 26);
        }
        this.mc.renderEngine.bindTexture(BasicMachines.oilLampGUILayer); 
    	this.drawTexturedModalRect(x+109, y+14, 109, 14, 16, 52-fill);
		
		// draw tick marks
	//	this.mc.renderEngine.bindTexture(BasicMachines.oilLampGUILayer); 
        drawTexturedModalRect(x+109, y+14, 176, 36, 16, 52);
	}
	
	 /**
     * Called when the mouse is clicked.
     */
	@Override protected void mouseClicked(int x, int y, int button){
		super.mouseClicked(x, y, button);
//FMLLog.fine(this.getClass().getCanonicalName() + ": "+"Mouse clicked in GUI: button"+button+" at ("+x+","+y+")");
		if(button == 0){
			// primary button click
			int dx = this.guiLeft;
			int dy = this.guiTop;
			x -= dx;
			y -= dy;
//FMLLog.fine(this.getClass().getCanonicalName() + ": "+"Recalculated click = button"+button+" at ("+x+","+y+")");
			// check if clicking on the flame button
			if(y < 71 && y > 54 && x > 79 && x < 96){
				toggleFlameButtonClicked();
			}
		}
	}
	
	private void toggleFlameButtonClicked(){
		try{
			ByteArrayOutputStream bos = new ByteArrayOutputStream(13);
			DataOutputStream outputStream = new DataOutputStream(bos);
			outputStream.writeInt(tile.xCoord);
			outputStream.writeInt(tile.yCoord);
			outputStream.writeInt(tile.zCoord);
			if (tile.isActivated()) {
				outputStream.writeByte(0);
			} else {
				outputStream.writeByte(1);
			}
			Packet250CustomPayload packet = new Packet250CustomPayload();
			packet.channel = "BasicMachines";
			packet.data = bos.toByteArray();
			packet.length = bos.size();
			PacketDispatcher.sendPacketToServer(packet);
		} catch (IOException ex){
			FMLLog.log(Level.SEVERE, ex, this.getClass().getCanonicalName()+": Error writing data packet!");
		}
		
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int param1, int param2) {
		String title = LanguageRegistry.instance().getStringLocalization("basicmachines.oilLamp");
        fontRenderer.drawString(title, 8, 6, 0x404040);
	}
	
	
	
}
