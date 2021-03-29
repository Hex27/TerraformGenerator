package org.terraform.biome.mountainous;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.FastNoise.NoiseType;

import java.util.Random;

public class RockyMountainsHandler extends AbstractMountainHandler {

    private static void dirtStack(PopulatorDataAbstract data, Random rand, int x, int y, int z) {
        data.setType(x, y, z, Material.GRASS_BLOCK);

        if (GenUtils.chance(rand, 1, 10))
            data.setType(x, y + 1, z, Material.GRASS);
        
        int depth = GenUtils.randInt(rand, 3, 7);
        for (int i = 1; i < depth; i++) {
        	if(!BlockUtils.isStoneLike(data.getType(x, y-i, z)))
        		break;
            data.setType(x, y - i, z, Material.DIRT);
            if(BlockUtils.isExposedToNonSolid(new SimpleBlock(data, x, y-i, z))) {
            	depth++;
            }
        }
    }

    @Override
    public boolean isOcean() {
        return false;
    }
//
//	@Override
//	public int getHeight(int x, int z, Random rand) {
//		SimplexOctaveGenerator gen = new SimplexOctaveGenerator(rand, 8);
//		gen.setScale(0.005);
//		
//		return (int) ((gen.noise(x, z, 0.5, 0.5)*7D+50D)*1.5);
//	}

    @Override
    public Biome getBiome() {
        return Biome.MOUNTAINS;
    }

    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[]{GenUtils.randMaterial(rand, Material.STONE, Material.STONE, Material.STONE, Material.STONE, Material.COBBLESTONE),
                GenUtils.randMaterial(rand, Material.COBBLESTONE, Material.STONE, Material.STONE),
                GenUtils.randMaterial(rand, Material.COBBLESTONE, Material.STONE, Material.STONE),
                GenUtils.randMaterial(rand, Material.COBBLESTONE, Material.STONE, Material.STONE),
                GenUtils.randMaterial(rand, Material.COBBLESTONE, Material.STONE, Material.STONE),};
    }

    @Override
    public void populateSmallItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
        boolean spawnedWaterfall = false;
    	for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                int y = GenUtils.getHighestGround(data, x, z);
                
                //Don't touch submerged blocks
                if(y < TerraformGenerator.seaLevel)
                	continue;
                //Make patches of dirt that extend on the mountain sides
                if (GenUtils.chance(random, 1, 25)) {
                    dirtStack(data, random, x, y, z);
                    for (int nx = -2; nx <= 2; nx++)
                        for (int nz = -2; nz <= 2; nz++) {
                            if (GenUtils.chance(random, 1, 5)) continue;
                            y = GenUtils.getHighestGround(data, x + nx, z + nz);
                            dirtStack(data, random, x + nx, y, z + nz);
                        }
                }
                
                //This area is steep and could have been a river
                if(!spawnedWaterfall)
	                if(HeightMap.getTrueHeightGradient(data, x, z, 3) > 1.5)
		                if(HeightMap.CORE.getHeight(tw, x, z) - HeightMap.getRawRiverDepth(tw, x, z) < TerraformGenerator.seaLevel) {
		                	//If this face is at least 4 blocks wide, carve a waterfall opening
		                	SimpleBlock block = new SimpleBlock(data,x,y,z);
		                	if(checkWaterfallSpace(block)) {
		                		block = block.getRelative(0,-4,0);
		                		placeWaterFall(x + 11*z + 31*y, block);
		                		spawnedWaterfall = true;
		                	}
		                }
            }
        }
    }
    
    public static void placeWaterFall(int seed, SimpleBlock base) {
    	float radius = 4.0f;

        FastNoise noise = new FastNoise(seed);
        noise.SetNoiseType(NoiseType.Simplex);
        noise.SetFrequency(0.09f);

        for (float x = -radius; x <= radius; x++) {
            for (float y = -radius/2.0f; y <= radius/2.0f; y++) {
                for (float z = -radius; z <= radius; z++) {

                    SimpleBlock rel = base.getRelative(Math.round(x), Math.round(y), Math.round(z));
                    //double radiusSquared = Math.pow(trueRadius+noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())*2,2);
                    double equationResult = Math.pow(x, 2) / Math.pow(radius, 2)
                            + Math.pow(y, 2) / Math.pow(radius, 2)
                            + Math.pow(z, 2) / Math.pow(radius, 2);
                    if (equationResult <= 1 + 0.7 * noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())) {
                        //if(rel.getLocation().distanceSquared(block.getLocation()) <= radiusSquared){
                        if(y > 0) { //Upper half of sphere is air
                        	rel.setType(Material.AIR);
                        }else if(rel.getType().isSolid()) {
                        	//Lower half is water, if replaced block was solid.
                        	rel.setType(Material.WATER);
                        }
                    }
                }
            }
        }
    }
    
    public boolean checkWaterfallSpace(SimpleBlock b) {
    	for(int i = 0; i < 5; i++) {
    		if(!b.getRelative(0,-i,0).getType().isSolid())
    			return false;
    	}
    	return BlockUtils.isExposedToNonSolid(b.getRelative(0,-4,0));
    }

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public BiomeBank getBeachType() {
		return BiomeBank.ROCKY_BEACH;
	}
}
