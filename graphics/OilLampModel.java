package cyano.basicmachines.graphics;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraftforge.common.ForgeDirection;

public class OilLampModel  extends ModelBase{

	final float modelScale = 0.0625f;

	final ModelRenderer modelFrame;
	final ModelRenderer modelGlass;
	final ModelRenderer[] supportFrame = new ModelRenderer[8]; 
	
	public OilLampModel(){
		int baseLevel = -10;
		int baseHeight = 2;
		int baseWidth = 8;
		int spaceHeight = 8;
		int spaceFrameThickness = 1;
		int capHeight = 2;
		int blockBottomHeight = -12;
		int blockTopHeight = 4;
		int supportFrameThickness = 2;
		int blockRadius = 8;
		
		modelFrame = new ModelRenderer(this, 0, 0);
		modelGlass = new ModelRenderer(this, 0, 0);
		modelFrame.textureHeight = 8;
		modelFrame.textureWidth = 8;
		modelGlass.textureHeight = 8;
		modelGlass.textureWidth = 8;
		
		modelFrame.addBox(baseWidth / -2F, baseLevel, baseWidth / -2F,
				baseWidth, baseHeight, baseWidth, 0.0F);
		modelFrame.addBox(baseWidth / -2F, baseLevel+baseHeight+spaceHeight, baseWidth / -2F, 
				baseWidth, capHeight, baseWidth, 0.0F);
		
		
		int blockCenterYcoord =  blockBottomHeight + blockRadius;
		modelGlass.addBox(spaceHeight/-2F, (spaceHeight/-2F)+blockCenterYcoord, spaceHeight/-2F, 
				spaceHeight, spaceHeight, spaceHeight); // little cube at center of cube 
		
		// supports
		for(int i = 0; i < supportFrame.length; i++){
			supportFrame[i] = new ModelRenderer(this, 0, 0);
			supportFrame[i].textureHeight = 16;
			supportFrame[i].textureWidth = 16;
		}
		ModelRenderer bottomSupport = supportFrame[5];
		ModelRenderer topSupport = supportFrame[0];
		ModelRenderer posZSupport = supportFrame[4];
		ModelRenderer negZSupport = supportFrame[3];
		ModelRenderer posXSupport = supportFrame[2];
		ModelRenderer negXSupport = supportFrame[1];
		// bottom
		bottomSupport.addBox(baseWidth / -4F, blockBottomHeight, baseWidth / -4F,
				baseWidth/2, baseLevel-blockBottomHeight, baseWidth/2, 0.0F);
		// top (hanging from chain)
		for(int i = blockTopHeight; i > baseLevel+baseHeight+spaceHeight; i--){
			int width = 1 + (i & 1);
			int depth = 3 - width;
			int height = 2;
			topSupport.addBox(-0.5F*width,i-1, -0.5F*depth, width, height, depth, 0.0F);
		}
		// sides
		float bottomSupportY = baseLevel+1;
		float topSupportY = baseLevel+baseHeight+spaceHeight+capHeight-supportFrameThickness-1;
		posZSupport.addBox(supportFrameThickness/-2F, bottomSupportY, baseWidth / 2F, 
				supportFrameThickness, supportFrameThickness, blockRadius-(baseWidth / 2));
		posZSupport.addBox(supportFrameThickness/-2F, topSupportY, baseWidth / 2F, 
				supportFrameThickness, supportFrameThickness, blockRadius-(baseWidth / 2));
		posZSupport.addBox(supportFrameThickness/-2F, bottomSupportY+supportFrameThickness, baseWidth / 2F, 
				supportFrameThickness, (int)(topSupportY-bottomSupportY)-supportFrameThickness, supportFrameThickness);
		
		negZSupport.addBox(supportFrameThickness/-2F, bottomSupportY, baseWidth / -2F, 
				supportFrameThickness, supportFrameThickness, -blockRadius+(baseWidth / 2));
		negZSupport.addBox(supportFrameThickness/-2F, topSupportY, baseWidth / -2F, 
				supportFrameThickness, supportFrameThickness, -blockRadius+(baseWidth / 2));
		negZSupport.addBox(supportFrameThickness/-2F, bottomSupportY+supportFrameThickness, baseWidth / -2F, 
				supportFrameThickness, (int)(topSupportY-bottomSupportY)-supportFrameThickness, supportFrameThickness);

		posXSupport.addBox(baseWidth / 2F, bottomSupportY, supportFrameThickness/-2F,
				blockRadius-(baseWidth / 2), supportFrameThickness,  supportFrameThickness);
		posXSupport.addBox(baseWidth / 2F, topSupportY,  supportFrameThickness/-2F,
				blockRadius-(baseWidth / 2), supportFrameThickness, supportFrameThickness);
		posXSupport.addBox(baseWidth / 2F, bottomSupportY+supportFrameThickness, supportFrameThickness/-2F, 
				supportFrameThickness, (int)(topSupportY-bottomSupportY)-supportFrameThickness, supportFrameThickness);

		negXSupport.addBox(baseWidth / -2F, bottomSupportY, supportFrameThickness/-2F,
				-blockRadius+(baseWidth / 2), supportFrameThickness, supportFrameThickness);
		negXSupport.addBox(baseWidth / -2F, topSupportY,  supportFrameThickness/-2F,
				-blockRadius+(baseWidth / 2), supportFrameThickness, supportFrameThickness);
		negXSupport.addBox(baseWidth / -2F, bottomSupportY+supportFrameThickness, supportFrameThickness/-2F, 
				supportFrameThickness, (int)(topSupportY-bottomSupportY)-supportFrameThickness, supportFrameThickness);
	}
	
	public void drawLampFrame(int metadata){
		modelFrame.render(modelScale);
		supportFrame[metadata & 0x7].render(modelScale);
	}
	public void drawLampGlass(){
		modelGlass.render(modelScale);
	}
}
