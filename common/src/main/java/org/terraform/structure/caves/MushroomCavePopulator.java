package org.terraform.structure.caves;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.type.SeaPickle;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.tree.FractalTypes;
import org.terraform.tree.MushroomBuilder;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.FastNoise.NoiseType;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;

import java.util.Random;

public class MushroomCavePopulator extends GenericLargeCavePopulator {

    public MushroomCavePopulator(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }
/*
    public void createLargeCave(TerraformWorld tw, Random rand, PopulatorDataAbstract data, int rY, int x, int y, int z) {
        TerraformGeneratorPlugin.logger.info("Generating Large Mushroom Cave at " + x + "," + y + "," + z);
        int rX = GenUtils.randInt(rand, 30, 50);
        int rZ = GenUtils.randInt(rand, 30, 50);

        //Create main cave hole
        carveCaveSphere(tw, rX, rY, rZ, new SimpleBlock(data, x, y, z));

        //Decrease radius to only spawn spikes away from corners
        //rX -= 10;
        //rZ -= 10;


        FastNoise mycelNoise = NoiseCacheHandler.getNoise(
        		tw, 
        		NoiseCacheEntry.STRUCTURE_LARGECAVE_RAISEDGROUNDNOISE, 
        		world -> {
        	        FastNoise n = new FastNoise((int) (world.getSeed() * 5));
        	        n.SetNoiseType(NoiseType.SimplexFractal);
        	        n.SetFractalOctaves(3);
        	        n.SetFrequency(0.04f);
        	        return n;
        		});
        

        int lowestPoint = y - rY;

        for (int nx = x - rX; nx <= x + rX; nx++) {
            for (int nz = z - rZ; nz <= z + rZ; nz++) {
                double noise = mycelNoise.GetNoise(nx, nz);
                if (noise < 0) noise = 0;
                if (noise > 0.5) noise = (noise - 0.5) * 0.5 + 0.5;
                int h = (int) ((rY / 2) * noise) + 2;
                if (h < 0) h = 0;
                BlockUtils.spawnPillar(rand, data, nx, lowestPoint, nz, Material.DIRT, h, h);
                BlockUtils.downPillar(nx, lowestPoint - 1, nz, 20, data, Material.DIRT);

                if(data.getType(nx, lowestPoint + h + 1, nz) != Material.WATER)
                	data.setType(nx, lowestPoint + h, nz, Material.MYCELIUM);
            }
        }
        
        for (int nx = x - rX; nx <= x + rX; nx++) {
            for (int nz = z - rZ; nz <= z + rZ; nz++) {


                
                int ground = getCaveFloor(data, nx, y, nz);
                if (data.getType(nx, ground, nz).isSolid()) {
                    if(data.getType(nx, ground + 1, nz) == Material.WATER) {
                    	//Low luminosity sea pickles
                        if (GenUtils.chance(rand, 4, 100)) {
	                        SeaPickle sp = (SeaPickle) Bukkit.createBlockData(Material.SEA_PICKLE);
	                        sp.setPickles(GenUtils.randInt(3, 4));
	                        data.setBlockData(nx, ground + 1, nz, sp);
                        }
                    }
                    else if(!data.getType(nx, ground+1, nz).isSolid())
                    {
                    	//Set most ground to mycelium
                        data.setType(nx, ground, nz, Material.MYCELIUM);
                    }
                }
                

                //Medium Shrooms
                if (GenUtils.chance(1, 150)) {
                    if (data.getType(nx, ground, nz) == Material.MYCELIUM
                            && data.getType(nx, ground + 1, nz) == Material.CAVE_AIR) {
                    	FractalTypes.Mushroom type;
                    	switch(rand.nextInt(6)) {
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
                    	
                        new MushroomBuilder(type).build(tw, data, nx, ground, nz);
                    }
                }

                //Only Stalactites, no stalagmites.
                if(nx <= x + rX - 10 && nx >= x - rX + 10
                		&& nz <= z + rZ - 10 && nz >= z - rZ + 10)
	                if (GenUtils.chance(rand, 3, 100)) {
	                    if (rand.nextBoolean()) {
	                        int ceil = getCaveCeiling(data, nx, y, nz);
	                        if (ceil != -1) {
	                            int r = 2;
	                            int h = GenUtils.randInt(rand, rY / 2, (int) ((3f / 2f) * rY));
	                            stalactite(tw, rand, data, nx, ceil, nz, r, h);
	                        }
	                    } 
	                }
            }
        }
    }*/
}