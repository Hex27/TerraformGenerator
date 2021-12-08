package org.terraform.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.terraform.data.SimpleBlock;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.FastNoise.NoiseType;

public class SphereBuilder {
	
	private Random random;
	private int seed;
	private float rX = 1f;
	private float rY = 1f;
	private float rZ = 1f;
	private SimpleBlock core;
	private boolean hardReplace = false;
	private Collection<Material> replaceWhitelist = new ArrayList<Material>();
	private Material[] types;
	private Material[] upperType;
	private Material[] lowerType;
	private int staticWaterLevel = -9999;
	
	public SphereBuilder(Random random, SimpleBlock core, Material... types) {
		this.random = random;
		this.seed = random.nextInt(99999999);
		this.types = types;
		this.core = core;
	}
	
	public SphereBuilder setUpperType(Material... upperType) {
		this.upperType = upperType;
		return this;
	}

	public SphereBuilder setLowerType(Material... lowerType) {
		this.lowerType = lowerType;
		return this;
	}
	
	public SphereBuilder setStaticWaterLevel(int staticWaterLevel) {
		this.staticWaterLevel = staticWaterLevel;
		return this;
	}
	
	public SphereBuilder addToWhitelist(Material... mats) {
		for(Material mat:mats)
			replaceWhitelist.add(mat);
		return this;
	}
	
	public SphereBuilder setRadius(float radius) {
		this.rX = radius; this.rY = radius; this.rZ = radius;
		return this;
	}
	
	public SphereBuilder setRX(float rX) {
		this.rX = rX;
		return this;
	}
	public SphereBuilder setRZ(float rZ) {
		this.rZ = rZ;
		return this;
	}
	public SphereBuilder setRY(float rY) {
		this.rY = rY;
		return this;
	}
	public SphereBuilder setSnowy() {
		this.upperType = new Material[] {Material.SNOW};
		return this;
	}
	public SphereBuilder setHardReplace(boolean hardReplace) {
		this.hardReplace = hardReplace;
		return this;
	}

    public void build() {
        if (rX <= 0 && rY <= 0 && rZ <= 0) return;
        if (rX <= 0.5 && rY <= 0.5 && rZ <= 0.5) {
            unitReplace(core);
            return;
        }

        FastNoise noise = new FastNoise(seed);
        noise.SetNoiseType(NoiseType.Simplex);
        noise.SetFrequency(0.09f);

        for (float x = -rX; x <= rX; x++) {
            for (float y = -rY; y <= rY; y++) {
                for (float z = -rZ; z <= rZ; z++) {
                    SimpleBlock rel = core.getRelative(Math.round(x), Math.round(y), Math.round(z));
                    //double radiusSquared = Math.pow(trueRadius+noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())*2,2);
                    double equationResult = Math.pow(x, 2) / Math.pow(rX, 2)
                            + Math.pow(y, 2) / Math.pow(rY, 2)
                            + Math.pow(z, 2) / Math.pow(rZ, 2);
                    if (equationResult <= 1 + 0.7 * noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())) {
                        Material[] original = types;
                    	if(rel.getY() <= staticWaterLevel) {
                        	types = new Material[] {Material.WATER};
                        	for(BlockFace face:new BlockFace[] {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.DOWN})
                        	{
                        		if(BlockUtils.isAir(rel.getRelative(face).getType())) {
                        			types = new Material[] { Material.STONE};
                        		}
                        	}
                        }
                    	unitReplace(rel);
                    	types = original;
                    }
                }
            }
        }
    }
    
    private boolean unitReplace(SimpleBlock rel) {
    	if(replaceWhitelist.size() == 0) {
    		if (hardReplace || !rel.getType().isSolid()) {
                rel.setType(GenUtils.randMaterial(random, types));
            }
    		else
    			return false;
    	} else if(replaceWhitelist.contains(rel.getType())) {
            rel.setType(GenUtils.randMaterial(random, types));
    	}
    	else
    		return false;
    	
    	if(rel.getRelative(0,-1,0).getType().isSolid()) {
	    	if(upperType != null)
	    		rel.getRelative(0,1,0).lsetType(upperType);
	    	if(lowerType != null)
	    		rel.getRelative(0,-1,0).setType(lowerType);
    	}
    	return true;
    }


}
