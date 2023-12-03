package org.terraform.structure.caves;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.type.SeaPickle;
import org.terraform.biome.cavepopulators.LushClusterCavePopulator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.coregen.populatordata.PopulatorDataICABiomeWriterAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.SphereBuilder;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.FastNoise.NoiseType;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;
import org.terraform.utils.version.OneOneSevenBlockHandler;

import java.util.Random;

public class LargeLushCavePopulator extends GenericLargeCavePopulator{
	
	@Override
    public void createLargeCave(TerraformWorld tw, Random rand, PopulatorDataAbstract data, int rY, int x, int y, int z) {
        TerraformGeneratorPlugin.logger.info("Generating Large Lush Cave at " + x + "," + y + "," + z);
        int rX = GenUtils.randInt(rand, 30, 50);
        int rZ = GenUtils.randInt(rand, 30, 50);

        //Create main cave hole
        int waterY = carveCaveSphere(tw, rX, rY, rZ, new SimpleBlock(data, x, y, z));

        //Decrease radius to only spawn spikes away from corners
        rX -= 10;
        rZ -= 10;


        //Use the same noise as mushroom cave
        FastNoise raisedGroundNoise = NoiseCacheHandler.getNoise(
        		tw, 
        		NoiseCacheEntry.STRUCTURE_LARGECAVE_RAISEDGROUNDNOISE, 
        		world -> {
        	        FastNoise n = new FastNoise((int) (world.getSeed() * 5));
        	        n.SetNoiseType(NoiseType.SimplexFractal);
        	        n.SetFractalOctaves(3);
        	        n.SetFrequency(0.05f);
        	        return n;
        		});
        

        int lowestPoint = y - rY;

        //Raise some ground up
        for (int nx = x - rX; nx <= x + rX; nx++) {
            for (int nz = z - rZ; nz <= z + rZ; nz++) {
                double noise = raisedGroundNoise.GetNoise(nx, nz);
                if (noise < 0) noise = 0;
                if (noise > 0.5) noise = (noise - 0.5) * 0.5 + 0.5;
                int h = (int) ((rY / 2) * noise) + 2;
                if (h < 0) h = 0;
                BlockUtils.spawnPillar(rand, data, nx, lowestPoint, nz, Material.STONE, h, h);
                BlockUtils.downPillar(nx, lowestPoint - 1, nz, 20, data, Material.STONE);

                data.setType(nx, lowestPoint + h, nz, Material.STONE);
            }
        }

        //Place Stalagmites and Stalactites
        for (int nx = x - rX; nx <= x + rX; nx++) {
            for (int nz = z - rZ; nz <= z + rZ; nz++) {

                //Stalagmites  &Stalactites
                if (GenUtils.chance(rand, 3, 100)) {
                    if (rand.nextBoolean()) {
                        int ceil = getCaveCeiling(data, nx, y, nz);
                        if (ceil != -1) {
                            int r = 2;
                            int h = GenUtils.randInt(rand, rY / 2, (int) ((3f / 2f) * rY));
                            stalactite(tw, rand, data, nx, ceil, nz, r, h);
                        }
                    } else {
                        int ground = getCaveFloor(data, nx, y, nz);
                        if (ground != -1) {
                            int r = 2;
                            int h = GenUtils.randInt(rand, rY / 2, (int) ((3f / 2f) * rY));
                            stalagmite(tw, rand, data, nx, ground, nz, r, h);
                        }
                    }
                }
            }
        }
        
        //Decorate
        for (int nx = x - rX; nx <= x + rX; nx++) {
            for (int nz = z - rZ; nz <= z + rZ; nz++) {
                int groundY = getCaveFloor(data, nx, y, nz);
                int ceilingY = getCaveCeiling(data,nx,y,nz);
                //getCaveFloor failure
                if(groundY == -1 || ceilingY == -1 || !data.getType(nx, groundY, nz).isSolid())
                	continue;
                SimpleBlock floor = new SimpleBlock(data,nx,groundY,nz);
                SimpleBlock ceil = new SimpleBlock(data,nx,ceilingY,nz);
                
                //Low luminosity sea pickles
                if (data.getType(nx, groundY + 1, nz) == Material.WATER) {
                	//SUBMERGED DECORATIONS
                	//sea pickle
                    if (GenUtils.chance(rand, 7, 100)) {
                        SeaPickle sp = (SeaPickle) Bukkit.createBlockData(Material.SEA_PICKLE);
                        sp.setPickles(GenUtils.randInt(3, 4));
                        data.setBlockData(nx, groundY + 1, nz, sp);
                    }
                    //clay
                    if (GenUtils.chance(rand, 7, 100)) {
                        new SphereBuilder(rand, new SimpleBlock(data,nx, groundY, nz), Material.CLAY)
                        .setRadius(3)
                        .addToWhitelist(Material.STONE)
                        .addToWhitelist(OneOneSevenBlockHandler.DEEPSLATE)
                        .build();
                    }
                    if(GenUtils.chance(rand, 7, 100)) {
                    	data.setType(nx, waterY+1, nz, Material.LILY_PAD);
                    }
                }else if(BlockUtils.isAir(data.getType(nx, groundY+1, nz))) {
                	//DRY DECORATIONS
                	if (GenUtils.chance(rand, 5, 100))
                		new LushClusterCavePopulator(GenUtils.randInt(rand, 5, 11), true).populate(tw, rand, ceil, floor);
                }
                
                //set biome
                //large cave uses PopulatorDataAbstract, which should be able to write biomes.
                if(data instanceof PopulatorDataICABiomeWriterAbstract)
                {
                	for(int i = groundY; i <= ceilingY; i++)
                	((PopulatorDataICABiomeWriterAbstract) data).setBiome(nx, i, nz, OneOneSevenBlockHandler.LUSH_CAVES);
                }
            }
        }
    }

    public int getCaveCeiling(PopulatorDataAbstract data, int x, int y, int z) {
        int ny = y;
        int highest = GenUtils.getHighestGround(data, x, z);
        while (ny < highest && !data.getType(x, ny, z).isSolid()) ny++;
        if (ny >= highest) return -1;
        return ny;
    }

    public int getCaveFloor(PopulatorDataAbstract data, int x, int y, int z) {
        int ny = y;
        while (ny > 2 && !data.getType(x, ny, z).isSolid()) ny--;
        return Math.max(ny, 2);
    }

}
