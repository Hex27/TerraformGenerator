package org.terraform.biome.flat;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.ChunkGenerator;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeBlender;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.tree.FractalTreeBuilder;
import org.terraform.tree.FractalTypes;
import org.terraform.tree.FractalTypes.Tree;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;

import java.util.Random;

public class PetrifiedCliffs extends BiomeHandler {
    static BiomeBlender biomeBlender;
    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public Biome getBiome() {
        return Biome.BIRCH_FOREST;
    }

    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[]{Material.GRASS_BLOCK,
                Material.DIRT,
                Material.DIRT,
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE)};
    }

    @Override
    public void populateSmallItems(TerraformWorld world, Random random, PopulatorDataAbstract data) {

        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                int y = GenUtils.getTrueHighestBlock(data, x, z);
                if (data.getBiome(x, z) != getBiome()) continue;
                
                for(int i = 0; i < 30; i++)
                	if(data.getType(x, y, z) == Material.DIORITE
                	|| data.getType(x, y, z) == Material.ANDESITE
                	|| data.getType(x, y, z) == Material.GRANITE
                	|| data.getType(x, y, z) == Material.POLISHED_DIORITE
                	|| data.getType(x, y, z) == Material.POLISHED_ANDESITE
                	|| data.getType(x, y, z) == Material.POLISHED_GRANITE)
                	{
                		y--;
                	}
                	else
                		break;
                
                if (data.getType(x, y, z) == Material.GRASS_BLOCK) {
                	SimpleBlock core = new SimpleBlock(data,x,y+1,z);
                	boolean continueOut = false;
                	for(BlockFace face:BlockUtils.directBlockFaces) {
                		if(core.getRelative(face).getType().toString().endsWith("STONE"))
                		{
                			core.setType(Material.DIORITE_SLAB);
                			continueOut = true;
                			break;
                		}
                	}
                	if(continueOut) continue;
                	
                    if (GenUtils.chance(random, 1, 10)) { //Grass
                        if (GenUtils.chance(random, 6, 10)) {
                            data.setType(x, y + 1, z, Material.GRASS);
                            if (random.nextBoolean()) {
                                BlockUtils.setDoublePlant(data, x, y + 1, z, Material.TALL_GRASS);
                            }
                        } else {
                            if (GenUtils.chance(random, 7, 10))
                                data.setType(x, y + 1, z, BlockUtils.pickFlower());
                            else
                                BlockUtils.setDoublePlant(data, x, y + 1, z, BlockUtils.pickTallFlower());
                        }
                    }
                }
            }
        }
    }

    @Override
    public BiomeHandler getTransformHandler() {
        return this;
    }
    
    @Override
    public int getMaxHeightForCaves(TerraformWorld tw, int x, int z) {
    	return (int) HeightMap.CORE.getHeight(tw, x, z);
    }
    

    @Override
    public void transformTerrain(TerraformWorld tw, Random random, ChunkGenerator.ChunkData chunk, int chunkX, int chunkZ) {
    	
        FastNoise noise = NoiseCacheHandler.getNoise(
        		tw, 
        		NoiseCacheEntry.BIOME_PETRIFIEDCLIFFS_CLIFFNOISE, 
        		world -> {
        	    	FastNoise n = new FastNoise(tw.getHashedRand(123, 2222, 1111).nextInt(99999));
        	        n.SetNoiseType(FastNoise.NoiseType.SimplexFractal);
        	        n.SetFractalOctaves(3);
        	        n.SetFrequency(0.03f);
        	        return n;
        		});
        
        FastNoise details = NoiseCacheHandler.getNoise(
        		tw, 
        		NoiseCacheEntry.BIOME_PETRIFIEDCLIFFS_INNERNOISE, 
        		world -> {
        	    	FastNoise n = new FastNoise(tw.getHashedRand(111, 0102, 1).nextInt(99999));
        	        n.SetNoiseType(FastNoise.NoiseType.SimplexFractal);
        	        n.SetFractalOctaves(3);
        	        n.SetFrequency(0.05f);
        	        return n;
        		});
        //Generates -0.8 to 0.8

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int rawX = chunkX * 16 + x;
                int rawZ = chunkZ * 16 + z;

                double preciseHeight = HeightMap.getPreciseHeight(tw, rawX, rawZ);
                int height = (int) preciseHeight;
                
                // Don't touch areas that aren't petrified cliffs
                if (tw.getBiomeBank(rawX, height, rawZ) != BiomeBank.PETRIFIED_CLIFFS) continue;

                double noiseValue = Math.max(0, noise.GetNoise(rawX, rawZ)) * getBiomeBlender(tw).getEdgeFactor(BiomeBank.PETRIFIED_CLIFFS, rawX, rawZ);
                if(noiseValue == 0) continue;
                
                double platformHeight = 7 + noiseValue * 50;
                
                if(platformHeight > 15) platformHeight = 15 + Math.sqrt(0.5*(platformHeight - 15));
                
                for (int y = 1; y <= (int) Math.round(platformHeight); y++) {
                	double detailsNoiseMultiplier = Math.pow(1.0-(1.0/(Math.pow(platformHeight/2.0, 2)))*Math.pow(y-platformHeight/2.0, 2), 2);
                	double detailsNoise = details.GetNoise(rawX, height+y, rawZ);
                    
                	if(0.85+detailsNoise > detailsNoiseMultiplier)
                    	chunk.setBlock(x, height + y, z,
                    			GenUtils.randMaterial(
                    			Material.STONE, 
                    			Material.STONE, 
                    			Material.STONE, 
                    			Material.COBBLESTONE, 
                    			Material.MOSSY_COBBLESTONE
                    			));
                }
                
            }
        }
    }

    private static BiomeBlender getBiomeBlender(TerraformWorld tw) {
        if (biomeBlender == null) biomeBlender = new BiomeBlender(tw, true, true)
                .setRiverThreshold(4).setBlendBeaches(false);
        return biomeBlender;
    }

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
		//Rock trees
        SimpleLocation[] trees = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 13, 0.2f);
        
        for (SimpleLocation sLoc : trees) {
        	if(random.nextBoolean()) {
        		int treeY = GenUtils.getTrueHighestBlock(data, sLoc.getX(),sLoc.getZ());
                sLoc.setY(treeY);
                if(data.getBiome(sLoc.getX(),sLoc.getZ()) == getBiome() &&
                        data.getType(sLoc.getX(),sLoc.getY(),sLoc.getZ()).toString().endsWith("STONE")) {
                    Tree treeType;
                    switch(random.nextInt(3)) {
                    case 0:
                		treeType = FractalTypes.Tree.ANDESITE_PETRIFIED_SMALL;
                		break;
                    case 1:
                		treeType = FractalTypes.Tree.GRANITE_PETRIFIED_SMALL;
                		break;
                	default:
                		treeType = FractalTypes.Tree.DIORITE_PETRIFIED_SMALL;
                		break;
                    }
                	new FractalTreeBuilder(treeType).build(tw, data, sLoc.getX(),sLoc.getY(),sLoc.getZ());
                }
        	}            
        }
	}
	
	@Override
	public BiomeBank getBeachType() {
		return BiomeBank.ROCKY_BEACH;
	}
}
