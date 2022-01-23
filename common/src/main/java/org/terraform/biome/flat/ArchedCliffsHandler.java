package org.terraform.biome.flat;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.ChunkGenerator;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeBlender;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.tree.FractalTreeBuilder;
import org.terraform.tree.FractalTypes;
import org.terraform.tree.MushroomBuilder;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;

import java.util.Random;

public class ArchedCliffsHandler extends BiomeHandler {
    static BiomeBlender biomeBlender;

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public Biome getBiome() {
        return Biome.PLAINS;
    }
    
    //Remove rivers from arched cliffs.
    //Arched cliffs are slightly higher than other biomes to lower beach sizes
    @Override
    public double calculateHeight(TerraformWorld tw, int x, int z) {
    	double height = super.calculateHeight(tw, x, z);
    	double riverDepth = HeightMap.getRawRiverDepth(tw, x, z); 
    	if(riverDepth > 0)
    		height += riverDepth;
    	return height + 3;
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
                int y = GenUtils.getHighestGround(data, x, z);
                if (data.getBiome(x, z) != getBiome()) continue;

        		SimpleBlock target = new SimpleBlock(data,x,y,z);
        		
                //Highest Ground decorations: grass and flowers
                if (GenUtils.chance(random, 1, 10)) { //Grass
                    if (GenUtils.chance(random, 6, 10)) {
                    	target.getUp().setType(Material.GRASS);
                        if (random.nextBoolean()) {
                            BlockUtils.setDoublePlant(target.getPopData(), target.getX(), target.getY()+1, target.getZ(), 
                            		Material.TALL_GRASS);
                        }
                    } else {
                        if (GenUtils.chance(random, 7, 10))
                        	target.getUp().setType(BlockUtils.pickFlower());
                        else
                            BlockUtils.setDoublePlant(target.getPopData(), target.getX(), target.getY()+1, target.getZ(), 
                            		BlockUtils.pickTallFlower());
                    }
                }
                
                //Underside decorations: Mushrooms
        		SimpleBlock underside = target.findAirPocket(30);
        		if(underside != null && underside.getY() > TerraformGenerator.seaLevel) {
        			SimpleBlock grassBottom = underside.findStonelikeFloor(50);
        			if(grassBottom != null && grassBottom.getY() > TerraformGenerator.seaLevel) {
        				if(grassBottom.getType() == Material.GRASS_BLOCK) {
        					//Indicates that this area is valid for population

        			        if (GenUtils.chance(random, 1, 10)) {
        			        	grassBottom.getUp().setType(Material.RED_MUSHROOM, Material.BROWN_MUSHROOM);
        			        	
        			        }
        			        
        			        //If an underside was valid, you can check the upper area for
        			        //decorating overhangs
        			        for(BlockFace face:BlockUtils.directBlockFaces) {
        			        	if(target.getRelative(face).getType() == Material.AIR) {
        			        		if(GenUtils.chance(random, 1, 5))
        			        			target.getRelative(face).downLPillar(random, random.nextInt(8), Material.OAK_LEAVES);
        			        	}
        			        }
        				}
        			}
        		}
            }
    	}
    }

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {

        //Highest Ground decorations
        //Small trees generate in the presence of light
        SimpleLocation[] trees = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 6);
        
        for (SimpleLocation sLoc : trees) {
    		int highestY = GenUtils.getHighestGround(data, sLoc.getX(),sLoc.getZ());
    		if(BlockUtils.isWet(new SimpleBlock(data, sLoc.getX(), highestY+1, sLoc.getZ())))
    			continue;
    		
            sLoc.setY(highestY);
            if(data.getBiome(sLoc.getX(),sLoc.getZ()) == getBiome() &&
                    BlockUtils.isDirtLike(data.getType(sLoc.getX(),sLoc.getY(),sLoc.getZ()))) {
                new FractalTreeBuilder(FractalTypes.Tree.NORMAL_SMALL).build(tw, data, sLoc.getX(),sLoc.getY(),sLoc.getZ());
            }
            
        }
        
        //Mushrooms generate underneath the overhangs
        SimpleLocation[] shrooms = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 10);
        
        for (SimpleLocation sLoc : shrooms) {
    		SimpleBlock target = new SimpleBlock(data, sLoc.getX(),0,sLoc.getZ()).getGround();
    		SimpleBlock underside = target.findAirPocket(30);
    		if(underside != null && underside.getY() > TerraformGenerator.seaLevel) {
    			SimpleBlock grassBottom = underside.findStonelikeFloor(50);
    			if(grassBottom != null && grassBottom.getY() > TerraformGenerator.seaLevel) {
    				if(grassBottom.getType() == Material.GRASS_BLOCK) {
    					//Indicates that this area is valid for population
    					sLoc.setY(grassBottom.getY());

                    	FractalTypes.Mushroom type;
                    	switch(random.nextInt(6)) {
                    	case 0:
                    		type = FractalTypes.Mushroom.MEDIUM_RED_MUSHROOM;
                    		break;
                    	case 1:
                    		type = FractalTypes.Mushroom.MEDIUM_BROWN_MUSHROOM;
                    		break;
                    	case 2:
                    		type = FractalTypes.Mushroom.MEDIUM_BROWN_FUNNEL_MUSHROOM;
                    		break;
                    	case 3:
                    		type = FractalTypes.Mushroom.SMALL_BROWN_MUSHROOM;
                    		break;
                    	case 4:
                    		type = FractalTypes.Mushroom.SMALL_POINTY_RED_MUSHROOM;
                    		break;
                    	default:
                    		type = FractalTypes.Mushroom.SMALL_RED_MUSHROOM;
                    		break;
                    	}
                    	
                        new MushroomBuilder(type).build(tw, data, sLoc.getX(), sLoc.getY()+1, sLoc.getZ());
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
    public void transformTerrain(TerraformWorld tw, Random random, ChunkGenerator.ChunkData chunk, int chunkX, int chunkZ) {
        FastNoise platformNoise = NoiseCacheHandler.getNoise(
        		tw, 
        		NoiseCacheEntry.BIOME_ARCHEDCLIFFS_PLATFORMNOISE, 
        		world -> {
        	    	FastNoise n = new FastNoise(tw.getRand(12115222).nextInt());
        	        n.SetNoiseType(FastNoise.NoiseType.SimplexFractal);
        	        n.SetFractalOctaves(3);
        	        n.SetFrequency(0.01f);
        	        return n;
        		});

        FastNoise pillarNoise = NoiseCacheHandler.getNoise(
        		tw, 
        		NoiseCacheEntry.BIOME_ARCHEDCLIFFS_PILLARNOISE, 
        		world -> {
        	        FastNoise n = new FastNoise(tw.getRand(12544422).nextInt());
        	        n.SetNoiseType(FastNoise.NoiseType.SimplexFractal);
        	        n.SetFractalOctaves(4);
        	        n.SetFrequency(0.01f);
        	        return n;
        		});


        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int rawX = chunkX * 16 + x;
                int rawZ = chunkZ * 16 + z;

                double preciseHeight = HeightMap.getPreciseHeight(tw, rawX, rawZ);
                int height = (int) preciseHeight;

                // Don't touch areas that aren't arched cliffs
                if (tw.getBiomeBank(rawX, height, rawZ) != BiomeBank.ARCHED_CLIFFS) continue;
                
                //Round to force a 0 if the value is too low. Makes blending better.
                double platformNoiseVal =
        				Math.round(
    						Math.max(
	                		platformNoise.GetNoise(rawX, rawZ)
	                		*70
	                		*getBiomeBlender(tw).getEdgeFactor(BiomeBank.ARCHED_CLIFFS, rawX, rawZ)
	                		, 0)
						);
                
                //if(platformNoiseVal > 10) platformNoiseVal *= 1.3;

                
                if(platformNoiseVal > 0) {
                	int platformHeight = (int) (
                			HeightMap.CORE.getHeight(tw, rawX, rawZ) 
                			- HeightMap.ATTRITION.getHeight(tw, rawX, rawZ) 
                			+ 55);

                	//for higher platform noise vals, make a thicker platform
                	
                	chunk.setBlock(x, platformHeight, z, Material.GRASS_BLOCK);
                	Material[] crust = getSurfaceCrust(random);
                	for(int i = 0; i < platformNoiseVal; i++) {
                		if(i < crust.length)
                        	chunk.setBlock(x, platformHeight-i, z, crust[i]);
                		else
                			chunk.setBlock(x, platformHeight-i, z, Material.STONE);
                	}
                	
                	if(platformNoiseVal > 6) {
                        int pillarNoiseVal = (int) ((platformNoiseVal/10.0)*((0.1+Math.abs(pillarNoise.GetNoise(rawX, rawZ)))*20.0));
	                	if(pillarNoiseVal + height > platformHeight)
	                		pillarNoiseVal = platformHeight - height;
                    	
	                	//Crust cannot be under solids.
	                	boolean applyCrust = !chunk.getType(x, height+pillarNoiseVal+1, z).isSolid();
	                	
                        for(int i = pillarNoiseVal; i >= 1; i--) {
                    		if((pillarNoiseVal-i) < crust.length && applyCrust)
                            	chunk.setBlock(x, height+i, z, crust[pillarNoiseVal-i]);
                    		else
                    			chunk.setBlock(x, height+i, z, Material.STONE);
                    	}
                	}
                }
            }
        }
    }

    private static BiomeBlender getBiomeBlender(TerraformWorld tw) {
        if (biomeBlender == null) biomeBlender = new BiomeBlender(tw, true, true)
                .setGridBlendingFactor(4)
                .setSmoothBlendTowardsRivers(4);
        return biomeBlender;
    }


    @Override
    public int getMaxHeightForCaves(TerraformWorld tw, int x, int z) {
    	return (int) HeightMap.CORE.getHeight(tw, x, z);
    }
    
    

//	@Override
//	public BiomeBank getRiverType() {
//		return BiomeBank.ARCHED_CLIFFS_RIVER;
//	}
//	@Override
//	public BiomeBank getBeachType() {
//		return BiomeBank.ARCHED_CLIFFS_BEACH;
//	}
}
