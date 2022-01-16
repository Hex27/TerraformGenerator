package org.terraform.structure.caves;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.SeaPickle;
import org.bukkit.util.Vector;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.FastNoise.NoiseType;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;
import org.terraform.utils.version.OneOneSevenBlockHandler;

import java.util.Random;

public class GenericLargeCavePopulator {
	
	private static boolean isReplaceable(Material type) {
		return(BlockUtils.isStoneLike(type)
                && type != Material.COBBLESTONE)
                || !type.isSolid()
                || type == Material.STONE_SLAB
                || type == Material.ICE
                || type == Material.PACKED_ICE
                || type == Material.BLUE_ICE
                || type == Material.OBSIDIAN
                || type == Material.MAGMA_BLOCK
                || type.toString().endsWith("WALL")
                || type.toString().endsWith("MOSS")
                || type == OneOneSevenBlockHandler.AMETHYST_CLUSTER
                || type == OneOneSevenBlockHandler.MOSS_BLOCK
                || type == OneOneSevenBlockHandler.MOSS_CARPET
                || type == OneOneSevenBlockHandler.POINTED_DRIPSTONE;
	}
	
    /**
     * @return water level.
     */
    public static int carveCaveSphere(TerraformWorld tw, float rX, float rY, float rZ, SimpleBlock block) {
        if (rX <= 0.5 &&
                rY <= 0.5 &&
                rZ <= 0.5) {
            return -1;
        }
        

        FastNoise noise = NoiseCacheHandler.getNoise(
        		tw, 
        		NoiseCacheEntry.STRUCTURE_LARGECAVE_CARVER, 
        		world -> {
        	        FastNoise n = new FastNoise((int) (world.getSeed()*8726));
        	        n.SetNoiseType(NoiseType.Simplex);
        	        n.SetFrequency(0.09f);
                
        	        return n;
        		});
        
        int waterLevel = -1;
        for (float x = -rX; x <= rX; x++) {
            for (float y = -rY; y <= rY; y++) {
                for (float z = -rZ; z <= rZ; z++) {

                    SimpleBlock rel = block.getRelative(Math.round(x), Math.round(y), Math.round(z));

                    //Never above surface.
                    if (rel.getY() >= GenUtils.getHighestGround(rel.getPopData(), rel.getX(), rel.getZ()) - 10)
                        continue;

                    double equationResult = Math.pow(x, 2) / Math.pow(rX, 2)
                            + Math.pow(y, 2) / Math.pow(rY, 2)
                            + Math.pow(z, 2) / Math.pow(rZ, 2);
                    double n = 0.7 * noise.GetNoise(rel.getX(), rel.getY(), rel.getZ());
                    if (n < 0) n = 0;
                    if (equationResult <= 1 + n) {
                        if (isReplaceable(rel.getType())) {

                            //Lower areas are water.
                            if (y < 0 && Math.abs(y) >= 0.8 * rY) {
                                rel.setType(Material.WATER);
                                if(rel.getY() > waterLevel)
                                	waterLevel = rel.getY();
                            } else {
                                //Replace drop blocks and water
                                if (rel.getRelative(0, 1, 0).getType() == Material.SAND
                                        || rel.getRelative(0, 1, 0).getType() == Material.GRAVEL
                                        || rel.getRelative(0, 1, 0).getType() == Material.WATER
                                        || rel.getRelative(0, 1, 0).getType() == OneOneSevenBlockHandler.POINTED_DRIPSTONE)
                                    rel.getRelative(0, 1, 0).setType(Material.DIRT);

                                //Replace water
                                for (BlockFace face : BlockUtils.directBlockFaces) {
                                    if (rel.getRelative(face).getType() == Material.WATER)
                                        rel.getRelative(face).setType(Material.DIRT);
                                }

                                //Carve the cave.
                                rel.setType(Material.CAVE_AIR);
                            }
                        }
                    }
                }
            }
        }
        return waterLevel;
    }

    public static void stalagmite(TerraformWorld tw, Random random, PopulatorDataAbstract data, int x, int y, int z, int baseRadius, int height) {

        //Vector one to two;
        Vector base = new Vector(x, y, z);
        Vector base2 = new Vector(x, y + height, z);
        Vector v = base2.subtract(base);
        v.clone().multiply(1 / v.length());
        SimpleBlock one = new SimpleBlock(data, x, y, z);
        double radius = baseRadius;
        for (int i = 0; i <= height; i++) {
            Vector seg = v.clone().multiply((float) i / ((float) height));
            SimpleBlock segment = one.getRelative(seg);

            BlockUtils.replaceSphere((int) (tw.getSeed() * 12), (float) radius, 2, (float) radius, segment, false, false, Material.STONE);
            radius = ((double) baseRadius) * (1 - ((double) i) / ((double) height));
        }
    }

    public static void stalactite(TerraformWorld tw, Random random, PopulatorDataAbstract data, int x, int y, int z, int baseRadius, int height) {

        //Vector one to two;
        Vector base = new Vector(x, y, z);
        Vector base2 = new Vector(x, y - height, z);
        Vector v = base2.subtract(base);
        v.clone().multiply(1 / v.length());
        SimpleBlock one = new SimpleBlock(data, x, y, z);
        double radius = baseRadius;
        for (int i = 0; i <= height; i++) {
            Vector seg = v.clone().multiply((float) i / ((float) height));
            SimpleBlock segment = one.getRelative(seg);

            BlockUtils.replaceSphere((int) (tw.getSeed() * 12), (float) radius, 2, (float) radius, segment, false, false, Material.STONE);
            radius = ((double) baseRadius) * (1 - ((double) i) / ((double) height));
        }
    }

    public void createLargeCave(TerraformWorld tw, Random rand, PopulatorDataAbstract data, int rY, int x, int y, int z) {
        TerraformGeneratorPlugin.logger.info("Generating Large Cave at " + x + "," + y + "," + z);
        int rX = GenUtils.randInt(rand, 30, 50);
        int rZ = GenUtils.randInt(rand, 30, 50);

        //Create main cave hole
        carveCaveSphere(tw, rX, rY, rZ, new SimpleBlock(data, x, y, z));

        //Decrease radius to only spawn spikes away from corners
        rX -= 10;
        rZ -= 10;

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

                //Low luminosity sea pickles
                if (GenUtils.chance(rand, 4, 100)) {
                    int ground = getCaveFloor(data, nx, y, nz);
                    if (data.getType(nx, ground, nz).isSolid()
                            && data.getType(nx, ground + 1, nz) == Material.WATER) {
                        SeaPickle sp = (SeaPickle) Bukkit.createBlockData(Material.SEA_PICKLE);
                        sp.setPickles(GenUtils.randInt(1, 2));
                        data.setBlockData(nx, ground + 1, nz, sp);
                    }
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
        while (ny > 2 
        		&& !BlockUtils.isStoneLike(data.getType(x, ny, z))) ny--;
        return Math.max(ny, 2);
    }

}
