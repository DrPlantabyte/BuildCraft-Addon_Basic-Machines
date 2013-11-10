package cyano.basicmachines.graphics;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;
import cyano.basicmachines.BasicMachines;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

public class OilLampItemRenderer implements IItemRenderer{

	final OilLampModel lampModel;
	
	public OilLampItemRenderer(){
		lampModel = new OilLampModel();
	}
	
	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		return true;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item,
			ItemRendererHelper helper) {
		return true;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
		 switch(type)
	        {
	            case ENTITY:{
	                renderLampItem(0f, 0f, 0f, 1.0f);
	                return;
	            }
	             
	            case EQUIPPED:{
	                renderLampItem(0f, 1f, 1f, 1f);
	                return;
	            }
	            case EQUIPPED_FIRST_PERSON:{
	            	renderLampItem(0f, 0f, 0f, 1.0f);
	                return;
	            }
	            case INVENTORY:{
	                renderLampItem(0f, 0f, 0f, 1.0f);
	                return;
	            }
	             
	            default:return;
	        }
	}

	
	 private void renderLampItem(float x, float y, float z, float scale)
	    {
	    GL11.glPushMatrix();
		
		
	    GL11.glTranslatef((float)x + 0.5F, (float)y + 0.75F * 1.0F, (float)z + 0.5F);

	    GL11.glScalef(scale, scale, scale);
		
	    FMLClientHandler.instance().getClient().renderEngine.bindTexture(BasicMachines.material_lampmetal);
	    lampModel.drawLampFrame(6); // metadata 6 will not have any supports rendered
	    FMLClientHandler.instance().getClient().renderEngine.bindTexture(BasicMachines.material_lampglass);
	    lampModel.drawLampGlass();
		
		GL11.glPopMatrix();
	    }
}
