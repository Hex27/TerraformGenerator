package org.terraform.biome.flat;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Biome;
import org.bukkit.block.data.Snowable;
import org.bukkit.util.Vector;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class IceSpikesHandler extends BiomeHandler {
    
	public static void genSpike(TerraformWorld tw, Random random, PopulatorDataAbstract data, int x, int y, int z, int baseRadius, int height) {
        y -= height / 5;
        //Vector one to two;
        Vector base = new Vector(x, y, z);
        Vector base2 = new Vector(
        		x + GenUtils.randInt(random, (int) (-1.5 * baseRadius), (int) (1.5 * baseRadius)), 
        		y + height, 
        		z + GenUtils.randInt(random, (int) (-1.5 * baseRadius), (int) (1.5 * baseRadius)));
        
        Vector v = base2.subtract(base);

        SimpleBlock one = new SimpleBlock(data, x, y, z);
        double radius = baseRadius;
        for (int i = 0; i <= height; i++) {
            Vector seg = v.clone().multiply((float) i / ((float) height));
            SimpleBlock segment = one.getRelative(seg);

            BlockUtils.replaceSphere((int) (tw.getSeed() * 12), (float) radius, 2, (float) radius, segment, false, false, Material.PACKED_ICE);

            radius = ((double) baseRadius) * (1 - ((double) i) / ((double) height));
        }
    }

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public Biome getBiome() {
        return Biome.ICE_SPIKES;
    }

    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[]{GenUtils.weightedRandomMaterial(rand, Material.SNOW_BLOCK, 5, Material.SNOW_BLOCK, 25),
                Material.SNOW_BLOCK,
                GenUtils.randMaterial(rand, Material.SNOW_BLOCK, Material.DIRT),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE)};
    }

    @Override
    public void populateSmallItems(TerraformWorld world, Random random, PopulatorDataAbstract data) {

        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                int y = GenUtils.getTrueHighestBlock(data, x, z);
                if (data.getBiome(x, z) != getBiome()) continue;

                if (data.getType(x, y + 1, z) == Material.AIR 
                		&& !Tag.ICE.isTagged(data.getType(x, y, z))) {
                    data.setType(x, y + 1, z, Material.SNOW);
                    if (data.getBlockData(x, y, z) instanceof Snowable) {
                        Snowable snowable = (Snowable) data.getBlockData(x, y, z);
                        snowable.setSnowy(true);
                        data.setBlockData(x, y, z, snowable);
                    }
                }
            }
        }

    }

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {

		//Ice Spikes
        SimpleLocation[] spikes = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 16, 0.5f);
        
        for (SimpleLocation sLoc : spikes) {
            int spikeY = GenUtils.getHighestGround(data, sLoc.getX(),sLoc.getZ());
            sLoc.setY(spikeY);
            if(data.getBiome(sLoc.getX(),sLoc.getZ()) == getBiome() &&
                    data.getType(sLoc.getX(),sLoc.getY(),sLoc.getZ()) == Material.SNOW_BLOCK) {
            	
            	if (GenUtils.chance(random, 1, 10)) { //big spike
                    genSpike(tw, random, data, sLoc.getX(), sLoc.getY(), sLoc.getZ(),
                            GenUtils.randInt(3, 7), //radius
                            GenUtils.randInt(40, 55)); //height
                } else //Small spike
                    genSpike(tw, random, data, sLoc.getX(), sLoc.getY(), sLoc.getZ(),
                            GenUtils.randInt(3, 5), //radius
                            GenUtils.randInt(13, 24)); //height
            	
            }
        }
	}
	

	@Override
	public BiomeBank getBeachType() {
		return BiomeBank.ICY_BEACH;
	}

	@Override
	public BiomeBank getRiverType() {
		return BiomeBank.FROZEN_RIVER;
	}
}
