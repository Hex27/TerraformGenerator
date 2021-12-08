package org.terraform.utils;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.util.Vector;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.utils.version.OneOneSevenBlockHandler;

public class StalactiteBuilder {

	private Material[] solidBlockType;
	private Material[] wallType;
	private boolean isFacingUp;
	private int verticalSpace;
	
	public StalactiteBuilder(Material... wallType) {
		this.wallType = wallType;
	}
	
	public StalactiteBuilder build(Random rand, Wall w) {
		if(verticalSpace < 6) return this;
		
		int stalactiteHeight;
		if(verticalSpace > 60) //massive cave
			stalactiteHeight = GenUtils.randInt(rand, 6, 25);
		else if(verticalSpace > 30) //large cave
			stalactiteHeight = GenUtils.randInt(rand, 5, 17);
		else if(verticalSpace > 15) //medium cave
			stalactiteHeight = GenUtils.randInt(rand, 3, 10);
		else //likely noodle cave
			stalactiteHeight = GenUtils.randInt(rand, 1, 2);
		
		if(stalactiteHeight < 4) {
			//tiny stalactite (1-3 blocks)
			if(isFacingUp)
				w.LPillar(stalactiteHeight, rand, wallType);
			else 
				w.downLPillar(rand, stalactiteHeight, wallType);
		}
		else if(stalactiteHeight < 7) 
		{
			//Bigger stalactite. (4-7 blocks)
			if(isFacingUp) {
				w.LPillar(stalactiteHeight, rand, wallType);
				w.Pillar(GenUtils.randInt(rand, 2, 3), rand, solidBlockType);
			}else { 
				w.downLPillar(rand, stalactiteHeight, wallType);
				w.downPillar(GenUtils.randInt(rand, 2, 3), solidBlockType);
			}
		}
		else
		{
			//Large stalactite (8+ blocks)
			if(isFacingUp)
			{
				stalagmite(rand, w, GenUtils.randDouble(rand, stalactiteHeight/6.0, stalactiteHeight/4.0),stalactiteHeight);
			}
			else
			{
				stalactite(rand, w, GenUtils.randDouble(rand, stalactiteHeight/6.0, stalactiteHeight/4.0),stalactiteHeight);
			}
		}
		
		return this;
	}

	public StalactiteBuilder setSolidBlockType(Material... solidBlockType) {
		this.solidBlockType = solidBlockType;
		return this;
	}

	public StalactiteBuilder setWallType(Material... wallType) {
		this.wallType = wallType;
		return this;
	}

	public StalactiteBuilder setFacingUp(boolean isFacingUp) {
		this.isFacingUp = isFacingUp;
		return this;
	}

	public StalactiteBuilder setVerticalSpace(int verticalSpace) {
		this.verticalSpace = verticalSpace;
		return this;
	}
	
	public void stalagmite(Random random, Wall w, double baseRadius, int height) {
		//Vector one to two;
		Vector base = new Vector(w.getX(),w.getY(),w.getZ());
		Vector base2 = new Vector(w.getX(),w.getY()+height,w.getZ());
		Vector v = base2.subtract(base);
		v.clone().multiply(1 / v.length());
		SimpleBlock one = w.get();
		double radius = baseRadius;
		for (int i = 0; i <= height; i++) {
			Vector seg = v.clone().multiply((float) i / ((float) height));
			SimpleBlock segment = one.getRelative(seg);
			
			BlockUtils.replaceSphere(random.nextInt(9999), (float) radius, 2, (float) radius, segment, false, false, solidBlockType);
			radius = ((double) baseRadius) * (1 - ((double) i) / ((double) height));
		}
	}
	
	public void stalactite(Random random, Wall w, double baseRadius, int height) {
	
	    //Vector one to two;
		Vector base = new Vector(w.getX(),w.getY(),w.getZ());
		Vector base2 = new Vector(w.getX(),w.getY()-height,w.getZ());
		Vector v = base2.subtract(base);
		v.clone().multiply(1 / v.length());
		SimpleBlock one = w.get();
	    double radius = baseRadius;
	    for (int i = 0; i <= height; i++) {
	        Vector seg = v.clone().multiply((float) i / ((float) height));
	        SimpleBlock segment = one.getRelative(seg);
	
	        BlockUtils.replaceSphere(random.nextInt(9999), (float) radius, 2, (float) radius, segment, false, false, solidBlockType);
	        radius = ((double) baseRadius) * (1 - ((double) i) / ((double) height));
	    }
	}
	
}
