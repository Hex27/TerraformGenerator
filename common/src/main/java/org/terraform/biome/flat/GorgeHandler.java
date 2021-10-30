package org.terraform.biome.flat;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.ChunkGenerator;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeBlender;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.bukkit.PhysicsUpdaterPopulator;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfigOption;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;

import java.util.Random;

public class GorgeHandler extends BiomeHandler {
    static BiomeBlender biomeBlender;
    static BiomeHandler plainsHandler = BiomeBank.PLAINS.getHandler();
    static boolean slabs = TConfigOption.MISC_USE_SLABS_TO_SMOOTH.getBoolean();

    @Override
    public boolean isOcean() {
        return plainsHandler.isOcean();
    }

    @Override
    public Biome getBiome() {
        return plainsHandler.getBiome();
    }

    @Override
    public Material[] getSurfaceCrust(Random rand) { return plainsHandler.getSurfaceCrust(rand); }

    @Override
    public void populateSmallItems(TerraformWorld world, Random random, PopulatorDataAbstract data) {
    	for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                int y = GenUtils.getHighestGround(data, x, z);
                if (data.getBiome(x, z) != getBiome()) continue;
                
                //Lower parts of the gorge have water
        		SimpleBlock target = new SimpleBlock(data,x,y+1,z);
        		while(target.getY() <= TerraformGenerator.seaLevel - 20) {        			
        			for(BlockFace face:new BlockFace[] {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.DOWN}) {
        				if(target.getRelative(face).getType() == Material.AIR)
        					target.getRelative(face).setType(Material.STONE);
        			}
        			target = target.getRelative(0,1,0);
        		}
        		
        		//No water was placed
        		if(target.getY() == y+1) {
        			target.getRelative(0,-1,0).setType(Material.GRASS_BLOCK);
        			target.getRelative(0,-2,0).setType(Material.DIRT);
        			if(random.nextBoolean()) {
        				target.getRelative(0,-3,0).setType(Material.DIRT);
        				if(random.nextBoolean())
        					target.getRelative(0,-4,0).setType(Material.DIRT);
        			}
        		}
            }
    	}
    	
    		
    	plainsHandler.populateSmallItems(world, random, data);
    }

    @Override
    public BiomeHandler getTransformHandler() {
        return this;
    }

    @Override
    public void transformTerrain(TerraformWorld tw, Random random, ChunkGenerator.ChunkData chunk, int chunkX, int chunkZ) {
    	
        FastNoise cliffNoise = NoiseCacheHandler.getNoise(
        		tw, 
        		NoiseCacheEntry.BIOME_GORGE_CLIFFNOISE, 
        		world -> {
        	    	FastNoise n = new FastNoise();
        	        n.SetNoiseType(FastNoise.NoiseType.CubicFractal);
        	        n.SetFractalOctaves(3);
        	        n.SetFrequency(0.04f);
        	        return n;
        		});

        FastNoise detailsNoise = NoiseCacheHandler.getNoise(
        		tw, 
        		NoiseCacheEntry.BIOME_GORGE_DETAILS, 
        		world -> {
        	        FastNoise n = new FastNoise();
        	        n.SetNoiseType(FastNoise.NoiseType.SimplexFractal);
        	        n.SetFrequency(0.03f);
        	        return n;
        		});


        double threshold = 0.1;
        int heightFactor = 12;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int rawX = chunkX * 16 + x;
                int rawZ = chunkZ * 16 + z;

                double preciseHeight = HeightMap.getPreciseHeight(tw, rawX, rawZ);
                int height = (int) preciseHeight;

                // Don't touch areas that aren't gorges
                if (tw.getBiomeBank(rawX, height, rawZ) != BiomeBank.GORGE) continue;
                
                double rawCliffNoiseVal = cliffNoise.GetNoise(rawX, rawZ);
                double noiseValue = rawCliffNoiseVal * getBiomeBlender(tw).getEdgeFactor(BiomeBank.GORGE, rawX, rawZ);
            	double detailsValue = detailsNoise.GetNoise(rawX, rawZ);
                
                //Raise up a tall area
                if(noiseValue >= 0) {
                    double d = (noiseValue / threshold) - (int) (noiseValue / threshold) - 0.5;
                    double platformHeight = (int) (noiseValue / threshold) * heightFactor
                            + (64 * Math.pow(d, 7) * heightFactor)
                            + detailsValue * heightFactor * 0.5;

                    for (int y = 1; y <= (int) Math.round(platformHeight); y++) {
                        Material material = GenUtils.randMaterial(Material.STONE, Material.STONE, Material.STONE, Material.STONE,
                                Material.COBBLESTONE, Material.COBBLESTONE, Material.ANDESITE, Material.ANDESITE);
                        
                        if (slabs 
                        		&& material != Material.GRASS_BLOCK 
                        		&& y == (int) Math.round(platformHeight) 
                        		&& platformHeight - (int) platformHeight >= 0.5) 
                        	material = Material.getMaterial(material.name() + "_SLAB");
                        chunk.setBlock(x, height + y, z, material);
                    }
                    if (detailsValue < 0.2 && GenUtils.chance(3, 4))
                    	chunk.setBlock(x, height + (int) Math.round(platformHeight), z, Material.GRASS_BLOCK);
                }
                else //Burrow a gorge deep down like a ravine
                {
                	int depth = (int) Math.sqrt(Math.abs(rawCliffNoiseVal * getBiomeBlender(tw).getEdgeFactor(BiomeBank.GORGE, rawX, rawZ)) * 200 * 50);
                	
                	//Smooth out anything that crosses the water threshold
                	if(height - depth < TerraformGenerator.seaLevel - 20) {
                		int depthToPreserve = height - (TerraformGenerator.seaLevel - 20);
                		depth = (int) (depthToPreserve + Math.round(Math.sqrt(depth - depthToPreserve)));
                	}
                	
                	//Prevent going beneath y = 10
                    if(depth > height - 10) depth = height-10;
                    
                	for (int y = 0; y < depth; y++) {
                        if(TerraformGenerator.seaLevel - 20 >= height-y)
                            chunk.setBlock(x, height - y, z, Material.WATER);
                        else
                        {
                            chunk.setBlock(x, height - y, z, Material.AIR);
                            //Force adjacent water to flow
                            for(BlockFace face:BlockUtils.directBlockFaces) {
                            	if(chunk.getType(
                            			x + face.getModX(), 
                            			height - y, 
                            			z + face.getModZ()) == Material.WATER)
                            	{
                                	PhysicsUpdaterPopulator.pushChange(
                                			tw.getName(), 
                                			new SimpleLocation(
                                					rawX + face.getModX(), 
                                					height - y, 
                                					rawZ + face.getModZ()));
                            	}
                            }
                        }
                    }
                	
                }
            }
        }
    }

    private static BiomeBlender getBiomeBlender(TerraformWorld tw) {
        if (biomeBlender == null) biomeBlender = new BiomeBlender(tw, true, true)
                .setGridBlendingFactor(2)
                .setSmoothBlendTowardsRivers(4);
        return biomeBlender;
    }

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
		plainsHandler.populateLargeItems(tw, random, data);
		
		//Spawn rocks
		SimpleLocation[] rocks = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 17, 0.4f);
        
        for (SimpleLocation sLoc : rocks) {
            if (data.getBiome(sLoc.getX(),sLoc.getZ()) == getBiome()) {
                int rockY = GenUtils.getHighestGround(data, sLoc.getX(),sLoc.getZ());
                sLoc.setY(rockY);
                if(rockY > TerraformGenerator.seaLevel - 18)
                		continue;
                
                BlockUtils.replaceSphere(
                		random.nextInt(91822),
                		(float) GenUtils.randDouble(random, 3, 6), 
                		(float) GenUtils.randDouble(random, 4, 7), 
                		(float) GenUtils.randDouble(random, 3, 6), 
                		new SimpleBlock(data,sLoc), 
                		true, 
                		GenUtils.randMaterial(
                				Material.STONE,
                				Material.GRANITE,
                				Material.ANDESITE,
                				Material.DIORITE
                		));
            }
        }
	}
}
