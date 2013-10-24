package cyano.basicmachines.client;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cyano.basicmachines.CommonProxy;
import net.minecraft.client.model.ModelChicken;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderChicken;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;


public class ClientProxy extends CommonProxy {
        
        @Override
        public void registerRenderers() {
               // 
    //  	// register item renderers
    //  	// entity renderers
   	//   	RenderingRegistry.registerEntityRenderingHandler(EarthFaerie.class, new RenderFaerie(0.5f));
        	
        }
        
        public static void setCustomRenderers(){
        	// register item renderers
        	// entity renderers
        	
        	//RenderingRegistry.registerEntityRenderingHandler(Faerie.class, new RenderFaerie(0.5f));
        }

    	
}